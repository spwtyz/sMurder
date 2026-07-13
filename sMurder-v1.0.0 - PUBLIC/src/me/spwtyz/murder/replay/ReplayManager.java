package me.spwtyz.murder.replay;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

import me.spwtyz.murder.Arena;
import me.spwtyz.murder.Arenas;
import me.spwtyz.murder.GameState;
import me.spwtyz.murder.Main;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class ReplayManager implements Listener {

    private final Main plugin;

    // Buffer temporario: fica gravando SOMENTE players em partida INGAME.
    private final Map<UUID, Deque<ReplayFrame>> liveFrames = new HashMap<UUID, Deque<ReplayFrame>>();

    // Eventos do replay: faca arremessada, hit, etc. Sem isso o replay so mostra o player andando.
    private final Map<UUID, List<ReplayEvent>> liveEvents = new HashMap<UUID, List<ReplayEvent>>();

    // Replays salvos por vitima. Eles ficam disponiveis somente para a ultima partida.
    // Quando uma nova partida entra em INGAME, os replays antigos sao limpos.
    private final Map<UUID, KillReplay> killReplays = new HashMap<UUID, KillReplay>();

    // Arenas que ja tiveram o inicio da partida detectado pelo sistema de replay.
    private final Set<String> activeMatchArenas = new HashSet<String>();

    // Ultimo hit conhecido. Isso resolve kills que nao passam pelo PlayerDeathEvent
    // ou mortes feitas por arena.removePlayer(victim, "death").
    private final Map<UUID, LastHit> lastHits = new HashMap<UUID, LastHit>();
    private final long lastHitExpireMs = 15000L;

    private final int maxFrames = 160; // 40 segundos gravando a cada 5 ticks. Evita replay parar antes da kill.

    private final Map<UUID, ReplaySession> activeSessions = new HashMap<UUID, ReplaySession>();

    public ReplayManager(Main plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startRecorder();
        startReplayCleaner();
    }


    private void startReplayCleaner() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                List<UUID> remove = new ArrayList<UUID>();
                for (Map.Entry<UUID, KillReplay> e : killReplays.entrySet()) {
                    KillReplay replay = e.getValue();
                    if (replay != null && now - replay.createdAt > 600000L) {
                        remove.add(e.getKey());
                    }
                }
                for (UUID id : remove) {
                    killReplays.remove(id);
                }
                List<UUID> onlineIngame = getOnlineIngamePlayerIds();
                liveFrames.keySet().retainAll(onlineIngame);
                liveEvents.keySet().retainAll(onlineIngame);
            }
        }.runTaskTimer(plugin, 20L * 60L, 20L * 60L);
    }

    private List<UUID> getOnlineIngamePlayerIds() {
        List<UUID> ids = new ArrayList<UUID>();
        for (Arena arena : Arenas.getArenas()) {
            if (arena == null || arena.getState() != GameState.INGAME) continue;
            for (Player p : arena.getPlayers()) {
                if (p != null && p.isOnline()) ids.add(p.getUniqueId());
            }
        }
        return ids;
    }

    private void startRecorder() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Set<String> stillIngame = new HashSet<String>();

                for (Arena arena : Arenas.getArenas()) {
                    if (arena == null) continue;
                    String arenaName = arena.getName();

                    // Nao grava lobby / sala de espera. Replay e so da partida.
                    if (arena.getState() != GameState.INGAME) {
                        activeMatchArenas.remove(arenaName);
                        continue;
                    }

                    stillIngame.add(arenaName);

                    // Nova partida detectada nessa arena: limpa os replays antigos.
                    // Assim o menu sempre mostra somente replay(s) da partida atual/ultima partida.
                    if (!activeMatchArenas.contains(arenaName)) {
                        activeMatchArenas.add(arenaName);
                        resetReplaysForNewMatch(arenaName);
                    }

                    for (Player player : arena.getPlayers()) {
                        if (player == null || !player.isOnline()) continue;
                        if (arena.specs.contains(player)) continue;
                        recordLiveFrame(player);
                    }
                }

                // Remove arenas que nao existem mais ou sairam de INGAME sem passar no loop.
                activeMatchArenas.retainAll(stillIngame);
            }
        }.runTaskTimer(plugin, 20L, 5L);
    }

    private void resetReplaysForNewMatch(String arenaName) {
        int old = killReplays.size();
        killReplays.clear();
        lastHits.clear();
        liveFrames.clear();
        liveEvents.clear();
        debug("Nova partida detectada em " + arenaName + ": replays antigos limpos (" + old + ").");
    }

    private void recordLiveFrame(Player player) {
        Deque<ReplayFrame> list = liveFrames.get(player.getUniqueId());
        if (list == null) {
            list = new ArrayDeque<ReplayFrame>();
            liveFrames.put(player.getUniqueId(), list);
        }

        ItemStack hand = player.getItemInHand() == null ? null : player.getItemInHand().clone();
        list.addLast(new ReplayFrame(player.getLocation().clone(), hand));

        while (list.size() > maxFrames) {
            list.removeFirst();
        }
    }



    public void recordKnifeThrow(Player killer, Item knife, Arena arena) {
        if (killer == null || knife == null || arena == null) return;
        if (arena.getState() != GameState.INGAME) return;

        Deque<ReplayFrame> frames = liveFrames.get(killer.getUniqueId());
        int index = frames == null ? 0 : Math.max(0, frames.size() - 1);

        List<ReplayEvent> events = liveEvents.get(killer.getUniqueId());
        if (events == null) {
            events = new ArrayList<ReplayEvent>();
            liveEvents.put(killer.getUniqueId(), events);
        }

        ItemStack stack = knife.getItemStack() == null ? null : knife.getItemStack().clone();
        Location start = knife.getLocation() == null ? killer.getEyeLocation().clone() : knife.getLocation().clone();
        Location end = start.clone().add(killer.getLocation().getDirection().normalize().multiply(8.0));

        ReplayEvent event = new ReplayEvent("THROW_KNIFE", index, Math.min(maxFrames - 1, index + 12), start, end, stack);
        events.add(event);

        while (events.size() > 20) {
            events.remove(0);
        }
        debug("Evento gravado: THROW_KNIFE de " + killer.getName() + " frame=" + index);
    }

    public void recordKnifeHit(Player killer, Location hitLocation) {
        if (killer == null || hitLocation == null) return;
        List<ReplayEvent> events = liveEvents.get(killer.getUniqueId());
        if (events == null || events.isEmpty()) return;

        ReplayEvent last = null;
        for (int i = events.size() - 1; i >= 0; i--) {
            ReplayEvent e = events.get(i);
            if (e != null && "THROW_KNIFE".equals(e.type)) {
                last = e;
                break;
            }
        }
        if (last == null) return;

        Deque<ReplayFrame> frames = liveFrames.get(killer.getUniqueId());
        int index = frames == null ? last.startIndex + 8 : Math.max(last.startIndex + 1, frames.size() - 1);
        last.endIndex = Math.min(maxFrames - 1, index + 4);
        last.end = hitLocation.clone();
        debug("Evento atualizado: THROW_KNIFE hit frame=" + index);
    }

    public void saveKillReplay(Player victim, Player killer, Arena arena) {
        if (victim == null || killer == null || arena == null) {
            debug("saveKillReplay cancelado: victim/killer/arena null");
            return;
        }

        // Salva o ultimo hit tambem. Se outro sistema remover o player depois, ainda temos o killer.
        markHit(victim, killer, arena);

        if (arena.getState() != GameState.INGAME) {
            debug("saveKillReplay cancelado: arena nao esta INGAME | arena=" + arena.getName() + " state=" + arena.getState());
            return;
        }

        Deque<ReplayFrame> killerSaved = liveFrames.get(killer.getUniqueId());
        Deque<ReplayFrame> victimSaved = liveFrames.get(victim.getUniqueId());

        // Mesmo se o recorder ainda nao tiver frames (kill muito rapida / faca arremessada),
        // cria replay com pelo menos o frame final da kill para nao ficar "SemReplay".
        List<ReplayFrame> killerFrames = killerSaved == null ? new ArrayList<ReplayFrame>() : new ArrayList<ReplayFrame>(killerSaved);
        List<ReplayFrame> victimFrames = victimSaved == null ? new ArrayList<ReplayFrame>() : new ArrayList<ReplayFrame>(victimSaved);

        List<ReplayEvent> replayEvents = new ArrayList<ReplayEvent>();
        List<ReplayEvent> savedEvents = liveEvents.get(killer.getUniqueId());
        if (savedEvents != null) {
            for (ReplayEvent event : savedEvents) {
                if (event != null) replayEvents.add(event.copy());
            }
        }

        // Garante o frame final da kill.
        killerFrames.add(new ReplayFrame(killer.getLocation().clone(), killer.getItemInHand() == null ? null : killer.getItemInHand().clone()));
        victimFrames.add(new ReplayFrame(victim.getLocation().clone(), victim.getItemInHand() == null ? null : victim.getItemInHand().clone()));

        killReplays.put(victim.getUniqueId(), new KillReplay(victim.getName(), killer.getName(), victim.getUniqueId(), killer.getUniqueId(), victimFrames, killerFrames, replayEvents));
        debug("Replay salvo: " + killer.getName() + " matou " + victim.getName() + " | frames killer=" + killerFrames.size() + " victim=" + victimFrames.size() + " eventos=" + replayEvents.size());
    }

    public void markHit(Player victim, Player killer, Arena arena) {
        if (victim == null || killer == null || arena == null) return;
        lastHits.put(victim.getUniqueId(), new LastHit(killer.getUniqueId(), killer.getName(), System.currentTimeMillis(), arena.getName()));
    }

    // Chame isso em qualquer lugar antes/depois de removePlayer(victim, "death").
    // Ele salva o replay usando o ultimo hitter conhecido, mesmo se PlayerDeathEvent nao disparar.
    public void saveDeathReplay(Player victim, Arena arena) {
        if (victim == null || arena == null) {
            debug("saveDeathReplay cancelado: victim/arena null");
            return;
        }
        if (hasReplay(victim.getUniqueId())) {
            debug("saveDeathReplay ignorado: ja existe replay para " + victim.getName());
            return;
        }
        LastHit hit = lastHits.get(victim.getUniqueId());
        if (hit == null) {
            debug("saveDeathReplay: sem ultimo hit para " + victim.getName());
            return;
        }
        if (System.currentTimeMillis() - hit.time > lastHitExpireMs) {
            debug("saveDeathReplay: ultimo hit expirado para " + victim.getName());
            return;
        }
        Player killer = Bukkit.getPlayer(hit.killerId);
        if (killer == null || !killer.isOnline()) {
            killer = Bukkit.getPlayerExact(hit.killerName);
        }
        if (killer == null || !killer.isOnline()) {
            debug("saveDeathReplay: killer offline/null para " + victim.getName() + " killer=" + hit.killerName);
            return;
        }
        saveKillReplay(victim, killer, arena);
    }



    public Player getLastKiller(Player victim, Arena arena) {
        if (victim == null || arena == null) return null;
        LastHit hit = lastHits.get(victim.getUniqueId());
        if (hit == null) return null;
        if (System.currentTimeMillis() - hit.time > lastHitExpireMs) return null;
        if (hit.arenaName != null && !hit.arenaName.equalsIgnoreCase(arena.getName())) return null;
        Player killer = Bukkit.getPlayer(hit.killerId);
        if (killer == null || !killer.isOnline()) killer = Bukkit.getPlayerExact(hit.killerName);
        if (killer == null || !killer.isOnline()) return null;
        return killer;
    }

    public int getReplayCount() {
        return killReplays.size();
    }

    private void debug(String msg) {
        if (plugin != null && plugin.getConfig().getBoolean("debug-replay", true)) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "[sMurder Replay] " + msg);
        }
    }

    public boolean hasReplay(Player target) {
        if (target == null) return false;
        return hasReplay(target.getUniqueId());
    }

    public boolean hasReplay(UUID uuid) {
        if (uuid == null) return false;
        KillReplay replay = killReplays.get(uuid);
        return replay != null && (!replay.killerFrames.isEmpty() || !replay.victimFrames.isEmpty());
    }

    public List<String> getReplayVictimNames() {
        List<String> names = new ArrayList<String>();
        for (KillReplay replay : killReplays.values()) {
            if (replay != null && (!replay.killerFrames.isEmpty() || !replay.victimFrames.isEmpty())) {
                names.add(replay.victimName);
            }
        }
        return names;
    }

    public boolean hasReplayByName(String name) {
        return getReplayByName(name) != null;
    }

    public String getReplayKillerNameByVictim(String victimName) {
        KillReplay replay = getReplayByName(victimName);
        return replay == null ? null : replay.killerName;
    }

    private KillReplay getReplayByName(String name) {
        if (name == null) return null;
        for (KillReplay replay : killReplays.values()) {
            if (replay != null && replay.victimName.equalsIgnoreCase(name)) {
                return replay;
            }
        }
        return null;
    }

    public void clear(Player player) {
        if (player == null) return;
        liveFrames.remove(player.getUniqueId());
        // NAO remove killReplays aqui: o replay precisa continuar disponivel no menu mesmo apos morte/saida.
        lastHits.remove(player.getUniqueId());
    }

    public void playReplayByName(final Player viewer, String targetName) {
        if (viewer == null || !viewer.isOnline() || targetName == null) return;
        KillReplay replay = getReplayByName(targetName);
        if (replay == null) {
            viewer.sendMessage(ChatColor.RED + "Nenhum kill replay disponível para este jogador.");
            return;
        }
        playReplay(viewer, replay);
    }

    public void playReplay(final Player viewer, Player target) {
        if (viewer == null || !viewer.isOnline() || target == null) return;
        KillReplay replay = killReplays.get(target.getUniqueId());
        if (replay == null) {
            viewer.sendMessage(ChatColor.RED + "Nenhum kill replay disponível para este jogador.");
            return;
        }
        playReplay(viewer, replay);
    }

    private void playReplay(final Player viewer, final KillReplay replay) {
        if (viewer == null || !viewer.isOnline() || replay == null) return;

        if (!Bukkit.getPluginManager().isPluginEnabled("Citizens")) {
            viewer.sendMessage(ChatColor.RED + "Para replay com NPC real de PLAYER, instale Citizens. Nao uso villager/armorstand neste replay.");
            return;
        }

        if (replay.killerFrames.isEmpty() && replay.victimFrames.isEmpty()) {
            viewer.sendMessage(ChatColor.RED + "Nenhum kill replay disponível para este jogador.");
            return;
        }

        stopSession(viewer, false);

        final Location oldLocation = viewer.getLocation().clone();
        final GameMode oldMode = viewer.getGameMode();
        final boolean oldAllowFlight = viewer.getAllowFlight();
        final boolean oldFlying = viewer.isFlying();
        final ItemStack[] oldContents = viewer.getInventory().getContents().clone();
        final ItemStack[] oldArmor = viewer.getInventory().getArmorContents().clone();

        final NPC killerNpc = createPlayerNPC(replay.killerName, replay.killerName);
        final NPC victimNpc = createPlayerNPC(replay.victimName, replay.victimName);

        Location start = getFirstLocation(replay);
        if (start == null || start.getWorld() == null) {
            viewer.sendMessage(ChatColor.RED + "Replay sem localização válida.");
            destroyNPC(killerNpc);
            destroyNPC(victimNpc);
            return;
        }

        Location killerStart = firstOrStart(replay.killerFrames, start);
        Location victimStart = firstOrStart(replay.victimFrames, start);

        if (!spawnNPC(killerNpc, killerStart)) {
            viewer.sendMessage(ChatColor.RED + "Nao consegui spawnar NPC PLAYER do assassino.");
            destroyNPC(killerNpc);
            destroyNPC(victimNpc);
            restoreViewer(viewer, oldLocation, oldMode, oldAllowFlight, oldFlying, oldContents, oldArmor);
            return;
        }
        spawnNPC(victimNpc, victimStart);

        moveNPC(killerNpc, getFrame(replay.killerFrames, 0));
        moveNPC(victimNpc, getFrame(replay.victimFrames, 0));

        viewer.closeInventory();
        viewer.getInventory().clear();
        viewer.getInventory().setArmorContents(new ItemStack[4]);
        giveReplayItems(viewer, replay, true, 0);

        viewer.sendMessage(ChatColor.GOLD + "Kill Replay carregado e pausado: " + ChatColor.RED + replay.killerName + ChatColor.GRAY + " matou " + ChatColor.YELLOW + replay.victimName);
        viewer.sendMessage(ChatColor.GRAY + "Use os itens no hotbar para pausar, voltar, adiantar e sair.");
        viewer.playSound(viewer.getLocation(), Sound.NOTE_PLING, 1f, 1f);

        // Nao prende a camera no assassino. O staff fica com voo livre vendo a cena.
        viewer.setGameMode(GameMode.CREATIVE);
        viewer.setAllowFlight(true);
        viewer.setFlying(true);
        Location viewLoc = getCameraLocation(killerStart, victimStart);
        viewer.teleport(viewLoc);

        final ReplaySession session = new ReplaySession(viewer.getUniqueId(), replay, killerNpc, victimNpc,
                oldLocation, oldMode, oldAllowFlight, oldFlying, oldContents, oldArmor);
        activeSessions.put(viewer.getUniqueId(), session);

        session.task = new BukkitRunnable() {
            @Override
            public void run() {
                Player current = Bukkit.getPlayer(session.viewerId);
                if (current == null || !current.isOnline()) {
                    stopSession(session.viewerId, false);
                    cancel();
                    return;
                }

                if (!activeSessions.containsKey(session.viewerId)) {
                    cancel();
                    return;
                }

                if (!session.paused) {
                    int advance = session.getFrameAdvanceThisTick();
                    if (advance <= 0) {
                        return;
                    }

                    session.index += advance;
                    if (session.index >= session.maxFrames()) {
                        session.index = session.maxFrames() - 1;
                        session.paused = true;
                        current.sendMessage(ChatColor.YELLOW + "Replay chegou no final. Use voltar ou sair.");
                    }
                    applyReplayFrame(session);
                    giveReplayItems(current, session.replay, session.paused, session.index);
                }
            }
        };
        session.task.runTaskTimer(plugin, 2L, 2L);
    }

    private Location getCameraLocation(Location killerStart, Location victimStart) {
        Location base = killerStart != null ? killerStart.clone() : victimStart.clone();
        if (victimStart != null && killerStart != null && victimStart.getWorld().equals(killerStart.getWorld())) {
            base.setX((killerStart.getX() + victimStart.getX()) / 2.0);
            base.setY(Math.max(killerStart.getY(), victimStart.getY()));
            base.setZ((killerStart.getZ() + victimStart.getZ()) / 2.0);
        }
        return base.add(0, 4.0, -7.0);
    }

    private void applyReplayFrame(ReplaySession session) {
        if (session == null) return;
        moveNPC(session.killerNpc, getFrame(session.replay.killerFrames, session.index));
        moveNPC(session.victimNpc, getFrame(session.replay.victimFrames, session.index));
        updateReplayEvents(session);
    }

    private void giveReplayItems(Player viewer, KillReplay replay, boolean paused, int index) {
        if (viewer == null || replay == null) return;
        PlayerInventory inv = viewer.getInventory();
        inv.setItem(0, controlItem(Material.ARROW, ChatColor.AQUA + "Voltar 2s", ChatColor.GRAY + "Clique para voltar o replay."));
        inv.setItem(1, controlItem(paused ? Material.REDSTONE_TORCH_ON : Material.WATCH,
                paused ? ChatColor.GREEN + "Continuar Replay" : ChatColor.YELLOW + "Pausar Replay",
                ChatColor.GRAY + "Clique para pausar/continuar."));
        inv.setItem(2, controlItem(Material.FEATHER, ChatColor.AQUA + "Adiantar 2s", ChatColor.GRAY + "Clique para adiantar o replay."));
        ReplaySession session = activeSessions.get(viewer.getUniqueId());
        String speedText = session == null ? "1x" : session.getSpeedText();

        inv.setItem(4, controlItem(Material.SKULL_ITEM, ChatColor.RED + "Assassino: " + ChatColor.WHITE + replay.killerName,
                ChatColor.GRAY + "Skin/NPC do assassino aparece no replay.",
                ChatColor.GRAY + "Vitima: " + ChatColor.YELLOW + replay.victimName,
                ChatColor.DARK_GRAY + "Frame: " + (index + 1) + "/" + Math.max(1, Math.max(replay.killerFrames.size(), replay.victimFrames.size()))));
        inv.setItem(6, controlItem(Material.SUGAR, ChatColor.AQUA + "Velocidade: " + ChatColor.WHITE + speedText,
                ChatColor.GRAY + "Clique para trocar a velocidade.",
                ChatColor.GRAY + "Opcoes: 0.5x, 1x, 2x, 4x."));
        inv.setItem(8, controlItem(Material.BARRIER, ChatColor.RED + "Sair do Replay", ChatColor.GRAY + "Clique para voltar para onde estava."));
        viewer.updateInventory();
    }

    private ItemStack controlItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        if (mat == Material.SKULL_ITEM) item.setDurability((short) 3);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onReplayControlInteract(PlayerInteractEvent event) {
        if (event.getPlayer() == null) return;
        if (!activeSessions.containsKey(event.getPlayer().getUniqueId())) return;
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_AIR && action != Action.LEFT_CLICK_BLOCK) return;
        handleControl(event.getPlayer(), event.getPlayer().getItemInHand());
        event.setCancelled(true);
    }

    @EventHandler
    public void onReplayControlInventory(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (!activeSessions.containsKey(player.getUniqueId())) return;
        event.setCancelled(true);
        handleControl(player, event.getCurrentItem());
    }

    @EventHandler
    public void onReplayQuit(PlayerQuitEvent event) {
        if (event.getPlayer() == null) return;
        stopSession(event.getPlayer(), false);
    }

    private void handleControl(Player viewer, ItemStack item) {
        if (viewer == null || item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;
        ReplaySession session = activeSessions.get(viewer.getUniqueId());
        if (session == null) return;
        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName()).toLowerCase();

        if (name.contains("sair")) {
            stopSession(viewer, true);
            return;
        }
        if (name.contains("pausar") || name.contains("continuar")) {
            session.paused = !session.paused;
            viewer.sendMessage(session.paused ? ChatColor.YELLOW + "Replay pausado." : ChatColor.GREEN + "Replay continuando.");
        } else if (name.contains("voltar")) {
            session.index -= 8; // ~2 segundos, pois o replay grava/anda a cada 5 ticks
            if (session.index < 0) session.index = 0;
            session.paused = true;
            viewer.sendMessage(ChatColor.AQUA + "Replay voltou 2 segundos.");
        } else if (name.contains("adiantar")) {
            session.index += 8;
            if (session.index >= session.maxFrames()) session.index = session.maxFrames() - 1;
            session.paused = true;
            viewer.sendMessage(ChatColor.AQUA + "Replay adiantou 2 segundos.");
        } else if (name.contains("velocidade")) {
            session.nextSpeed();
            viewer.sendMessage(ChatColor.AQUA + "Velocidade do replay alterada para " + ChatColor.WHITE + session.getSpeedText() + ChatColor.AQUA + ".");
        }

        applyReplayFrame(session);
        giveReplayItems(viewer, session.replay, session.paused, session.index);
    }

    private void stopSession(Player viewer, boolean restore) {
        if (viewer == null) return;
        stopSession(viewer.getUniqueId(), restore);
    }

    private void stopSession(UUID viewerId, boolean restore) {
        ReplaySession session = activeSessions.remove(viewerId);
        if (session == null) return;
        if (session.task != null) {
            try { session.task.cancel(); } catch (Throwable ignored) {}
        }
        destroyNPC(session.killerNpc);
        destroyNPC(session.victimNpc);
        clearReplayItems(session);
        Player viewer = Bukkit.getPlayer(viewerId);
        if (viewer != null && viewer.isOnline() && restore) {
            restoreViewer(viewer, session.oldLocation, session.oldMode, session.oldAllowFlight, session.oldFlying, session.oldContents, session.oldArmor);
            viewer.sendMessage(ChatColor.GREEN + "Voce saiu do replay.");
        }
    }



    private void clearReplayItems(ReplaySession session) {
        if (session == null || session.spawnedItems == null) return;
        for (Item item : new ArrayList<Item>(session.spawnedItems)) {
            if (item != null && !item.isDead()) {
                try { item.remove(); } catch (Throwable ignored) {}
            }
        }
        session.spawnedItems.clear();
    }

    private void updateReplayEvents(ReplaySession session) {
        if (session == null || session.replay == null) return;

        // Recria os itens de evento em cada frame para suportar pausar, voltar e adiantar sem bug visual.
        clearReplayItems(session);

        if (session.replay.events == null || session.replay.events.isEmpty()) return;

        for (ReplayEvent event : session.replay.events) {
            if (event == null || !"THROW_KNIFE".equals(event.type)) continue;
            if (session.index < event.startIndex || session.index > event.endIndex + 10) continue;
            if (event.start == null || event.start.getWorld() == null || event.stack == null) continue;

            Location loc;
            if (session.index <= event.endIndex && event.end != null && event.end.getWorld() != null && event.end.getWorld().equals(event.start.getWorld())) {
                double total = Math.max(1.0, event.endIndex - event.startIndex);
                double progress = Math.max(0.0, Math.min(1.0, (session.index - event.startIndex) / total));
                loc = event.start.clone();
                loc.setX(event.start.getX() + (event.end.getX() - event.start.getX()) * progress);
                loc.setY(event.start.getY() + (event.end.getY() - event.start.getY()) * progress);
                loc.setZ(event.start.getZ() + (event.end.getZ() - event.start.getZ()) * progress);
                loc.setYaw(event.start.getYaw());
                loc.setPitch(event.start.getPitch());
            } else {
                loc = event.end == null ? event.start.clone() : event.end.clone();
            }

            try {
                Item item = loc.getWorld().dropItem(loc, event.stack.clone());
                item.setPickupDelay(Integer.MAX_VALUE);
                item.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
                session.spawnedItems.add(item);
            } catch (Throwable ignored) {}
        }
    }

    private NPC createPlayerNPC(String name, String skinName) {
        // Citizens 1.8 pode falhar para NPC PLAYER se o nome tiver cor, espaco estranho
        // ou passar de 16 caracteres. O replay precisa ser PLAYER real, entao mantemos
        // EntityType.PLAYER e usamos nome limpo/seguro.
        String cleanName = ChatColor.stripColor(name == null ? "ReplayNPC" : name);
        if (cleanName == null || cleanName.trim().isEmpty()) cleanName = "ReplayNPC";
        cleanName = cleanName.replaceAll("[^A-Za-z0-9_]{1,}", "");
        if (cleanName.length() > 16) cleanName = cleanName.substring(0, 16);
        if (cleanName.length() < 3) cleanName = "ReplayNPC";

        NPC npc = null;
        try {
            npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, cleanName);
            try { npc.setProtected(true); } catch (Throwable ignored) {}
            applySkinCompat(npc, skinName == null || skinName.trim().isEmpty() ? cleanName : skinName);
        } catch (Throwable t) {
            debug("Erro criando NPC PLAYER Citizens para " + cleanName + ": " + t.getClass().getSimpleName() + " - " + t.getMessage());
        }
        return npc;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void applySkinCompat(NPC npc, String skinName) {
        if (npc == null || skinName == null || skinName.trim().isEmpty()) return;

        // Compatibilidade Citizens antigo/1.8:
        // nao importamos SkinTrait direto porque algumas builds antigas dao erro no getTrait(Class<T>).
        try {
            Class skinTraitClass = Class.forName("net.citizensnpcs.trait.SkinTrait");
            Object skinTrait = npc.getTrait(skinTraitClass);
            if (skinTrait != null) {
                try {
                    skinTraitClass.getMethod("setSkinName", String.class).invoke(skinTrait, skinName);
                    return;
                } catch (Throwable ignored) {
                }
                try {
                    skinTraitClass.getMethod("setSkinName", String.class, boolean.class).invoke(skinTrait, skinName, true);
                    return;
                } catch (Throwable ignored) {
                }
            }
        } catch (Throwable ignored) {
        }

        // Fallback por metadata do Citizens. Mantem EntityType.PLAYER, nunca villager/armorstand.
        try {
            npc.data().setPersistent("player-skin-name", skinName);
        } catch (Throwable ignored) {
        }
        try {
            npc.data().setPersistent("cached-skin-uuid-name", skinName);
        } catch (Throwable ignored) {
        }
    }

    private boolean spawnNPC(NPC npc, Location location) {
        if (npc == null) {
            debug("spawnNPC falhou: npc null");
            return false;
        }
        if (location == null || location.getWorld() == null) {
            debug("spawnNPC falhou: location/world null");
            return false;
        }
        try {
            Location safe = location.clone();
            if (!safe.getChunk().isLoaded()) {
                safe.getChunk().load(true);
            }
            // Pequeno ajuste evita spawn dentro do bloco/void em alguns mapas.
            safe.setY(Math.max(1.0, safe.getY() + 0.05));

            if (npc.isSpawned()) {
                if (npc.getEntity() != null) npc.getEntity().teleport(safe);
                return true;
            }

            boolean spawned = npc.spawn(safe);
            if (!spawned) {
                debug("Citizens retornou false ao spawnar NPC PLAYER em " + safe.getWorld().getName() + " " + safe.getBlockX() + "," + safe.getBlockY() + "," + safe.getBlockZ());
            }
            return spawned;
        } catch (Throwable t) {
            debug("Erro spawnando NPC PLAYER: " + t.getClass().getSimpleName() + " - " + t.getMessage());
            return false;
        }
    }

    private void moveNPC(NPC npc, ReplayFrame frame) {
        if (npc == null || frame == null || frame.location == null || frame.location.getWorld() == null) return;
        try {
            if (!npc.isSpawned()) {
                npc.spawn(frame.location);
            } else if (npc.getEntity() != null) {
                npc.getEntity().teleport(frame.location);
            }
            if (npc.getEntity() instanceof Player && frame.hand != null) {
                ((Player) npc.getEntity()).getInventory().setItemInHand(frame.hand.clone());
            }
        } catch (Throwable ignored) {
        }
    }

    private void destroyNPC(NPC npc) {
        if (npc == null) return;
        try {
            npc.destroy();
        } catch (Throwable ignored) {
        }
    }

    private void restoreViewer(Player viewer, Location loc, GameMode mode, boolean allowFlight, boolean flying, ItemStack[] contents, ItemStack[] armor) {
        if (viewer == null || !viewer.isOnline()) return;
        viewer.getInventory().setContents(contents);
        viewer.getInventory().setArmorContents(armor);
        viewer.teleport(loc);
        viewer.setGameMode(mode);
        viewer.setAllowFlight(allowFlight);
        viewer.setFlying(flying);
        viewer.updateInventory();
    }

    private Location getFirstLocation(KillReplay replay) {
        if (replay == null) return null;
        if (!replay.killerFrames.isEmpty() && replay.killerFrames.get(0).location != null) return replay.killerFrames.get(0).location.clone();
        if (!replay.victimFrames.isEmpty() && replay.victimFrames.get(0).location != null) return replay.victimFrames.get(0).location.clone();
        return null;
    }

    private Location firstOrStart(List<ReplayFrame> frames, Location start) {
        if (frames != null && !frames.isEmpty() && frames.get(0).location != null) return frames.get(0).location.clone();
        return start.clone();
    }

    private ReplayFrame getFrame(List<ReplayFrame> frames, int index) {
        if (frames == null || frames.isEmpty()) return null;
        if (index < frames.size()) return frames.get(index);
        return frames.get(frames.size() - 1);
    }

    private static class ReplaySession {
        UUID viewerId;
        KillReplay replay;
        NPC killerNpc;
        NPC victimNpc;
        Location oldLocation;
        GameMode oldMode;
        boolean oldAllowFlight;
        boolean oldFlying;
        ItemStack[] oldContents;
        ItemStack[] oldArmor;
        int index = 0;
        boolean paused = true;
        BukkitRunnable task;
        List<Item> spawnedItems = new ArrayList<Item>();

        // 0 = 0.5x, 1 = 1x, 2 = 2x, 3 = 4x
        int speedMode = 1;
        int slowTick = 0;

        ReplaySession(UUID viewerId, KillReplay replay, NPC killerNpc, NPC victimNpc,
                      Location oldLocation, GameMode oldMode, boolean oldAllowFlight, boolean oldFlying,
                      ItemStack[] oldContents, ItemStack[] oldArmor) {
            this.viewerId = viewerId;
            this.replay = replay;
            this.killerNpc = killerNpc;
            this.victimNpc = victimNpc;
            this.oldLocation = oldLocation;
            this.oldMode = oldMode;
            this.oldAllowFlight = oldAllowFlight;
            this.oldFlying = oldFlying;
            this.oldContents = oldContents;
            this.oldArmor = oldArmor;
        }

        void nextSpeed() {
            speedMode++;
            if (speedMode > 3) speedMode = 0;
            slowTick = 0;
        }

        String getSpeedText() {
            if (speedMode == 0) return "0.5x";
            if (speedMode == 2) return "2x";
            if (speedMode == 3) return "4x";
            return "1x";
        }

        int getFrameAdvanceThisTick() {
            if (speedMode == 0) {
                slowTick++;
                if (slowTick < 2) return 0;
                slowTick = 0;
                return 1;
            }
            if (speedMode == 2) return 2;
            if (speedMode == 3) return 4;
            return 1;
        }

        int maxFrames() {
            int max = Math.max(1, Math.max(replay.killerFrames.size(), replay.victimFrames.size()));
            if (replay.events != null) {
                for (ReplayEvent event : replay.events) {
                    if (event != null) max = Math.max(max, event.endIndex + 12);
                }
            }
            return max;
        }
    }

    private static class LastHit {
        UUID killerId;
        String killerName;
        long time;
        String arenaName;

        LastHit(UUID killerId, String killerName, long time, String arenaName) {
            this.killerId = killerId;
            this.killerName = killerName;
            this.time = time;
            this.arenaName = arenaName;
        }
    }

    private static class KillReplay {
        private final String victimName;
        private final String killerName;
        @SuppressWarnings("unused")
        private final UUID victimUuid;
        @SuppressWarnings("unused")
        private final UUID killerUuid;
        private final List<ReplayFrame> victimFrames;
        private final List<ReplayFrame> killerFrames;
        private final List<ReplayEvent> events;
        private final long createdAt;

        private KillReplay(String victimName, String killerName, UUID victimUuid, UUID killerUuid,
                           List<ReplayFrame> victimFrames, List<ReplayFrame> killerFrames, List<ReplayEvent> events) {
            this.victimName = victimName;
            this.killerName = killerName;
            this.victimUuid = victimUuid;
            this.killerUuid = killerUuid;
            this.victimFrames = victimFrames == null ? new ArrayList<ReplayFrame>() : victimFrames;
            this.killerFrames = killerFrames == null ? new ArrayList<ReplayFrame>() : killerFrames;
            this.events = events == null ? new ArrayList<ReplayEvent>() : events;
            this.createdAt = System.currentTimeMillis();
        }
    }



    private static class ReplayEvent {
        private final String type;
        private final int startIndex;
        private int endIndex;
        private final Location start;
        private Location end;
        private final ItemStack stack;

        private ReplayEvent(String type, int startIndex, int endIndex, Location start, Location end, ItemStack stack) {
            this.type = type;
            this.startIndex = Math.max(0, startIndex);
            this.endIndex = Math.max(this.startIndex + 1, endIndex);
            this.start = start == null ? null : start.clone();
            this.end = end == null ? null : end.clone();
            this.stack = stack == null ? new ItemStack(Material.IRON_SWORD) : stack.clone();
        }

        private ReplayEvent copy() {
            return new ReplayEvent(type, startIndex, endIndex, start, end, stack);
        }
    }

    private static class ReplayFrame {
        private final Location location;
        private final ItemStack hand;

        private ReplayFrame(Location location, ItemStack hand) {
            this.location = location;
            this.hand = hand;
        }
    }
}
