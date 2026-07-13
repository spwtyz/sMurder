package me.spwtyz.murder.sabotage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.golde.bukkit.corpsereborn.nms.Corpses.CorpseData;

import me.spwtyz.murder.Arena;
import me.spwtyz.murder.Arenas;
import me.spwtyz.murder.GameModeType;
import me.spwtyz.murder.GameState;
import me.spwtyz.murder.Main;
import me.spwtyz.murder.PlayerType;
import me.spwtyz.murder.Utils;
import me.spwtyz.murder.scoreboard.ScoreboardManager;

public class SabotageTaskManager implements Listener {

    private final Main plugin;
    private final Random random = new Random();

    private final Map<String, Set<UUID>> completedTasks = new HashMap<String, Set<UUID>>();
    private final Map<String, Integer> completedCount = new HashMap<String, Integer>();
    private final Map<UUID, List<String>> assignedTasks = new HashMap<UUID, List<String>>();
    private final Map<UUID, SabotageCorpse> corpses = new HashMap<UUID, SabotageCorpse>();
    private final Map<UUID, TaskPoint> taskMarkers = new HashMap<UUID, TaskPoint>();
    private final Map<UUID, PendingTask> pendingTask = new HashMap<UUID, PendingTask>();
    // Estado das GUIs interativas (Wiring, Manifolds, Simon Says etc.).
    private final Map<UUID, Integer> taskGuiStep = new HashMap<UUID, Integer>();
    private final Map<UUID, List<Integer>> taskGuiSequence = new HashMap<UUID, List<Integer>>();
    private final Map<String, Boolean> meetingActive = new HashMap<String, Boolean>();
    private final Map<String, Map<UUID, UUID>> votes = new HashMap<String, Map<UUID, UUID>>();
    private final Map<String, Integer> voteTasks = new HashMap<String, Integer>();
    private final Map<String, Long> meetingEndTime = new HashMap<String, Long>();
    private final Map<String, Set<UUID>> emergencyMeetingsUsed = new HashMap<String, Set<UUID>>();
    private final Set<UUID> meetingFrozen = new HashSet<UUID>();
    private final Map<String, Boolean> voteOpen = new HashMap<String, Boolean>();
    private final Map<String, Map<UUID, ItemStack[]>> meetingInventory = new HashMap<String, Map<UUID, ItemStack[]>>();
    private final Map<String, Map<UUID, ItemStack[]>> meetingArmor = new HashMap<String, Map<UUID, ItemStack[]>>();
    private final Map<UUID, Long> sabotageKillCooldown = new HashMap<UUID, Long>();
    // AMONG US: sabotagens ativas que precisam de uma task específica para serem resolvidas.
    // Ex.: O2 -> Clean O2, Reactor -> Start Reactor, Lights/Electrical -> Calibrate Distributor.
    private final Map<String, TaskType> activeSabotageRepairs = new HashMap<String, TaskType>();
    private final Map<String, String> activeSabotageNames = new HashMap<String, String>();
    private final Map<UUID, Integer> murderCooldownTaskIds = new HashMap<UUID, Integer>();

    public SabotageTaskManager(Main plugin) {
        this.plugin = plugin;
        setupDefaults();
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() { public void run() { respawnTaskHolograms(); } }, 20L);
    }

    private void setupDefaults() {
        if (!plugin.getConfig().contains("Sabotage.TasksPerPlayer")) plugin.getConfig().set("Sabotage.TasksPerPlayer", 12);
        if (!plugin.getConfig().contains("Sabotage.TaskRadius")) plugin.getConfig().set("Sabotage.TaskRadius", 2.5D);
        if (!plugin.getConfig().contains("Sabotage.TaskWinPercent")) plugin.getConfig().set("Sabotage.TaskWinPercent", 100);
        if (!plugin.getConfig().contains("Sabotage.TaskGuiSeconds")) plugin.getConfig().set("Sabotage.TaskGuiSeconds", 3);
        if (!plugin.getConfig().contains("Sabotage.TaskHolograms.Enabled")) plugin.getConfig().set("Sabotage.TaskHolograms.Enabled", true);
        if (!plugin.getConfig().contains("Sabotage.MeetingDiscussionSeconds")) plugin.getConfig().set("Sabotage.MeetingDiscussionSeconds", 15);
        if (!plugin.getConfig().contains("Sabotage.MeetingVoteSeconds")) plugin.getConfig().set("Sabotage.MeetingVoteSeconds", 20);
        if (!plugin.getConfig().contains("Sabotage.LightsDuration")) plugin.getConfig().set("Sabotage.LightsDuration", 20);
        if (!plugin.getConfig().contains("Sabotage.ReactorDuration")) plugin.getConfig().set("Sabotage.ReactorDuration", 25);
        if (!plugin.getConfig().contains("Sabotage.O2Duration")) plugin.getConfig().set("Sabotage.O2Duration", 18);
        if (!plugin.getConfig().contains("Sabotage.CommunicationsDuration")) plugin.getConfig().set("Sabotage.CommunicationsDuration", 20);
        if (!plugin.getConfig().contains("Sabotage.ElectricalDuration")) plugin.getConfig().set("Sabotage.ElectricalDuration", 12);
        if (!plugin.getConfig().contains("Sabotage.DoorsDuration")) plugin.getConfig().set("Sabotage.DoorsDuration", 15);
        if (!plugin.getConfig().contains("Sabotage.Corpses.Enabled")) plugin.getConfig().set("Sabotage.Corpses.Enabled", true);
        if (!plugin.getConfig().contains("Sabotage.Corpses.ReportRadius")) plugin.getConfig().set("Sabotage.Corpses.ReportRadius", 3.0D);
        if (!plugin.getConfig().contains("Sabotage.EmergencyMeetingsPerPlayer")) plugin.getConfig().set("Sabotage.EmergencyMeetingsPerPlayer", 1);
        if (!plugin.getConfig().contains("Sabotage.FreezeDuringMeeting")) plugin.getConfig().set("Sabotage.FreezeDuringMeeting", true);
        if (!plugin.getConfig().contains("Sabotage.DetectiveClues.Enabled")) plugin.getConfig().set("Sabotage.DetectiveClues.Enabled", true);
        if (!plugin.getConfig().contains("Sabotage.DetectiveClues.Seconds")) plugin.getConfig().set("Sabotage.DetectiveClues.Seconds", 15);
        if (!plugin.getConfig().contains("Sabotage.MurderKillCooldownSeconds")) plugin.getConfig().set("Sabotage.MurderKillCooldownSeconds", 25);
        if (!plugin.getConfig().contains("Sabotage.Repairs.Enabled")) plugin.getConfig().set("Sabotage.Repairs.Enabled", true);
        plugin.saveConfig();
    }

    public boolean isSabotageArena(Arena arena) {
        return arena != null && arena.getGameMode() == GameModeType.SABOTAGE;
    }

    private boolean shouldShowTaskHologram(TaskPoint point) {
        if (point == null || point.location == null || point.location.getWorld() == null) return false;

        String worldName = point.location.getWorld().getName();
        boolean hasActiveAmongUsArena = false;

        for (Arena arena : Arenas.getArenas()) {
            if (arena == null) continue;
            if (!isArenaUsingTaskWorld(arena, worldName)) continue;

            // Se o mesmo mapa/world estiver sendo usado por uma sala que NAO e AMONG US,
            // nao spawnamos hologramas globais nesse mundo. ArmorStand/Holograma e entidade
            // global no 1.8, entao se spawnar ela aparece para todos no mapa.
            if (!isSabotageArena(arena)) {
                try {
                    if (arena.getPlayers() != null && !arena.getPlayers().isEmpty()) return false;
                } catch (Throwable ignored) {
                    return false;
                }
            }

            // Mostra task somente quando o AMONG US realmente esta rodando/contando.
            // No lobby de espera, outros modos usando o mesmo mapa nao devem ver nada.
            if (isSabotageArena(arena) && (arena.getState() == GameState.INGAME || arena.getState() == GameState.STARTING)) {
                hasActiveAmongUsArena = true;
            }
        }
        return hasActiveAmongUsArena;
    }

    private boolean isArenaUsingTaskWorld(Arena arena, String worldName) {
        if (arena == null || worldName == null) return false;
        try {
            Location spawn = plugin.getSpawn(arena, 0);
            if (spawn != null && spawn.getWorld() != null && spawn.getWorld().getName().equalsIgnoreCase(worldName)) return true;
        } catch (Throwable ignored) {}

        try {
            Location spec = plugin.getSpec(arena);
            if (spec != null && spec.getWorld() != null && spec.getWorld().getName().equalsIgnoreCase(worldName)) return true;
        } catch (Throwable ignored) {}

        // Fallback para setups em que o nome do mapa é igual ao nome da world.
        return arena.getTemplateName() != null && arena.getTemplateName().equalsIgnoreCase(worldName);
    }


    public void respawnTaskHolograms() {
        cleanupTaskHolograms();
        if (!plugin.getConfig().getBoolean("Sabotage.TaskHolograms.Enabled", true)) return;
        for (TaskPoint point : getTaskPoints()) {
            if (point == null || point.location == null || point.location.getWorld() == null) continue;

            // IMPORTANTE:
            // As tasks podem estar configuradas em mapas que também são usados no Murder normal
            // ou em outros modos. O holograma só deve existir quando alguma sala/arena usando
            // esse mesmo mundo estiver realmente no modo AMONG US/Sabotage.
            if (!shouldShowTaskHologram(point)) continue;

            Location loc = point.location.clone().add(0, 0.35D, 0);
            try {
                ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
                stand.setVisible(false);
                stand.setGravity(false);
                stand.setSmall(true);
                stand.setCustomNameVisible(true);
                stand.setCustomName("§a§lTASK §7- §f" + point.name + " §8[" + point.type.getDisplay() + "] §e(Clique)");
                taskMarkers.put(stand.getUniqueId(), point);
            } catch (Throwable ignored) {}
        }
    }

    public void cleanupTaskHolograms() {
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                try {
                    if (taskMarkers.containsKey(entity.getUniqueId())) {
                        entity.remove();
                        continue;
                    }
                    // Remove tambem hologramas antigos/orfaos de tasks que ficaram de versions anteriores.
                    // Isso impede que as tasks continuem aparecendo no Murder normal apos trocar o modo.
                    if (entity instanceof ArmorStand && entity.getCustomName() != null
                            && entity.getCustomName().contains("TASK")
                            && entity.getCustomName().contains("Clique")) {
                        entity.remove();
                    }
                } catch (Throwable ignored) {}
            }
        }
        taskMarkers.clear();
    }

    public void addTaskLocation(Player staff, String name) {
        String key = "Sabotage.TaskLocations";
        int id = 1;
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection(key);
        if (sec != null) id = sec.getKeys(false).size() + 1;

        Location loc = staff.getLocation();
        String path = key + ".task" + id;
        plugin.getConfig().set(path + ".name", name.replace("_", " "));
        plugin.getConfig().set(path + ".type", detectTaskType(name).name());
        plugin.getConfig().set(path + ".world", loc.getWorld().getName());
        plugin.getConfig().set(path + ".x", loc.getX());
        plugin.getConfig().set(path + ".y", loc.getY());
        plugin.getConfig().set(path + ".z", loc.getZ());
        plugin.getConfig().set(path + ".yaw", loc.getYaw());
        plugin.getConfig().set(path + ".pitch", loc.getPitch());
        plugin.saveConfig();
        respawnTaskHolograms();
        staff.sendMessage("§aTask do Sabotage setada: §e" + name + " §7(#" + id + ")");
    }

    public void listTaskLocations(Player staff) {
        staff.sendMessage("§6§lTasks disponíveis para adicionar no mapa:");
        staff.sendMessage("§7Use: §e/m sabotage task add <task> §7em cima do local da task.");
        for (TaskType type : TaskType.values()) {
            staff.sendMessage("§8- §e" + type.getCommandName() + " §7(" + type.name() + ") §8- §f" + type.getDisplay());
        }

        List<TaskPoint> points = getTaskPoints();
        staff.sendMessage("");
        staff.sendMessage("§6Tasks já configuradas neste servidor: §f" + points.size());
        int i = 1;
        for (TaskPoint point : points) {
            staff.sendMessage("§7#" + i++ + " §e" + point.name + " §8[§f" + point.type.name() + "§8] §8- §f" + point.location.getWorld().getName() + " "
                    + point.location.getBlockX() + ", " + point.location.getBlockY() + ", " + point.location.getBlockZ());
        }
    }

    public void clearTaskLocations(Player staff) {
        plugin.getConfig().set("Sabotage.TaskLocations", null);
        plugin.saveConfig();
        cleanupTaskHolograms();
        staff.sendMessage("§aTodas as tasks do Sabotage foram removidas.");
    }

    public ItemStack createTaskItem() {
        ItemStack item = new ItemStack(Material.WATCH);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§aTasks §7(Clique perto de uma task)");
        List<String> lore = new ArrayList<String>();
        lore.add("§7Complete as tasks para vencer.");
        lore.add("§7Use perto de um ponto configurado.");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createEmergencyItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§cReunião de Emergência");
        List<String> lore = new ArrayList<String>();
        lore.add("§7Use para chamar uma reunião.");
        lore.add("§7Disponível apenas no Sabotage.");
        lore.add("§8Limite configurável por partida.");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private boolean isEmergencyItem(ItemStack item) {
        return item != null && item.getType() == Material.NETHER_STAR && item.hasItemMeta()
                && item.getItemMeta().hasDisplayName()
                && org.bukkit.ChatColor.stripColor(item.getItemMeta().getDisplayName()).toLowerCase().contains("reunião");
    }

    public void giveSabotageItems(Arena arena) {
        if (!isSabotageArena(arena)) return;
        resetArenaProgress(arena);
        for (Player p : arena.getPlayers()) {
            if (p == null || !p.isOnline() || arena.specs.contains(p)) continue;
            PlayerType type = arena.getType(p);
            if (type == PlayerType.Innocents || type == PlayerType.Detective) {
                assignTasks(p, arena);
                // Tasks agora são feitas pelo holograma/local configurado; não damos mais item de task no inventário.
                if (!p.getInventory().contains(Material.NETHER_STAR)) p.getInventory().setItem(5, createEmergencyItem());
            }
        }
    }


    public void resetArenaProgress(Arena arena) {
        if (arena == null) return;
        String prefix = getArenaKey(arena) + ":";
        List<String> remove = new ArrayList<String>();
        for (String key : completedTasks.keySet()) {
            if (key != null && key.startsWith(prefix)) remove.add(key);
        }
        for (String key : remove) completedTasks.remove(key);
        completedCount.remove(getArenaKey(arena));
        List<UUID> removeAssigned = new ArrayList<UUID>();
        for (UUID uuid : assignedTasks.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || Arenas.getArena(player) == arena) removeAssigned.add(uuid);
        }
        for (UUID uuid : removeAssigned) assignedTasks.remove(uuid);
    }

    private void assignTasks(Player player, Arena arena) {
        if (player == null || arena == null) return;
        if (assignedTasks.containsKey(player.getUniqueId())) return;
        List<TaskPoint> points = getTaskPoints(arena);
        List<String> list = new ArrayList<String>();
        int max = Math.min(plugin.getConfig().getInt("Sabotage.TasksPerPlayer", 3), points.size());
        List<TaskPoint> copy = new ArrayList<TaskPoint>(points);
        java.util.Collections.shuffle(copy);
        for (int i = 0; i < max; i++) list.add(copy.get(i).id);
        assignedTasks.put(player.getUniqueId(), list);
    }

    public String getPlayerTaskList(Player player, Arena arena) {
        if (player == null || arena == null) return "§7-";
        assignTasks(player, arena);
        List<String> ids = assignedTasks.get(player.getUniqueId());
        if (ids == null || ids.isEmpty()) return "§7Sem tasks";

        List<TaskPoint> points = getTaskPoints(arena);
        List<String> pending = new ArrayList<String>();
        List<String> done = new ArrayList<String>();

        for (String id : ids) {
            TaskPoint found = null;
            for (TaskPoint point : points) {
                if (point.id.equalsIgnoreCase(id)) {
                    found = point;
                    break;
                }
            }
            if (found == null) continue;

            if (isTaskCompleted(player, arena, found)) {
                done.add("§aOK " + found.name);
            } else {
                // Scoreboard 1.8 tem limite curto de prefix/suffix.
                // Deixa só o nome da próxima task para não virar "Uplo".
                pending.add("§f" + found.name);
            }
        }

        if (!pending.isEmpty()) {
            return pending.get(0) + (pending.size() > 1 ? " §7+" + (pending.size() - 1) : "");
        }
        if (!done.isEmpty()) return "§aTodas completas";
        return "§7Sem tasks";
    }

    public int getPlayerCompletedTasks(Player player, Arena arena) {
        if (player == null || arena == null) return 0;
        int done = 0;
        for (TaskPoint point : getTaskPoints(arena)) {
            if (isTaskCompleted(player, arena, point)) done++;
        }
        return done;
    }

    @EventHandler
    public void onTaskInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Arena arena = Arenas.getArena(player);
        if (!isSabotageArena(arena) || arena.getState() != GameState.INGAME) return;
        ItemStack hand = player.getItemInHand();
        if (isEmergencyItem(hand)) {
            event.setCancelled(true);
            callEmergencyMeeting(player, arena);
            return;
        }

        if (Boolean.TRUE.equals(meetingActive.get(getArenaKey(arena)))) {
            event.setCancelled(true);
            player.sendMessage("§cVocê não pode fazer tasks durante reunião.");
            return;
        }
        PlayerType type = arena.getType(player);
        if (type != PlayerType.Innocents && type != PlayerType.Detective) return;

        // Sem item de task: se o player clicar perto de uma task configurada, abre o menu.
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
            if (getNearestTask(player, arena) != null) {
                event.setCancelled(true);
                openNearbyTaskGui(player, arena);
            }
        }
    }

    public boolean openNearbyTaskGui(Player player, Arena arena) {
        TaskPoint nearest = getNearestTask(player, arena);
        double radius = plugin.getConfig().getDouble("Sabotage.TaskRadius", 2.5D);
        if (nearest == null || nearest.location.distance(player.getLocation()) > radius) {
            player.sendMessage("§cVocê precisa estar perto de uma task.");
            return false;
        }
        assignTasks(player, arena);
        List<String> playerTasks = assignedTasks.get(player.getUniqueId());
        if (playerTasks != null && !playerTasks.isEmpty() && !playerTasks.contains(nearest.id)) {
            player.sendMessage("§cEssa task não está na sua lista atual.");
            return false;
        }
        if (isTaskCompleted(player, arena, nearest)) {
            player.sendMessage("§eVocê já completou essa task.");
            return false;
        }

        openTaskInventory(player, arena, nearest);
        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        if (title == null) return;

        if (title.startsWith("Task: ")) {
            event.setCancelled(true);
            ItemStack item = event.getCurrentItem();
            if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return;
            handleTaskGuiClick(player, item, event.getRawSlot());
            return;
        }

        if (title.equals("Votação AMONG US")) {
            event.setCancelled(true);
            handleVoteClick(player, event.getCurrentItem());
        }
    }

    @EventHandler
    public void onVoteClose(final InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        if (event.getInventory() == null || event.getView() == null) return;
        if (!"Votação AMONG US".equals(event.getView().getTitle())) return;
        final Player player = (Player) event.getPlayer();
        final Arena arena = Arenas.getArena(player);
        if (!isSabotageArena(arena) || arena.getState() != GameState.INGAME) return;
        final String key = getArenaKey(arena);
        if (!Boolean.TRUE.equals(meetingActive.get(key)) || !Boolean.TRUE.equals(voteOpen.get(key))) return;
        Map<UUID, UUID> arenaVotes = votes.get(key);
        if (arenaVotes != null && arenaVotes.containsKey(player.getUniqueId())) return;
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            public void run() {
                Arena current = Arenas.getArena(player);
                if (current != arena || !Boolean.TRUE.equals(meetingActive.get(key)) || !Boolean.TRUE.equals(voteOpen.get(key))) return;
                Map<UUID, UUID> currentVotes = votes.get(key);
                if (currentVotes != null && currentVotes.containsKey(player.getUniqueId())) return;
                openVoteMenu(player, arena);
            }
        }, 2L);
    }

    private void openTaskInventory(Player player, Arena arena, TaskPoint nearest) {
        Inventory inv = Bukkit.createInventory(null, 54, "Task: " + nearest.name);
        ItemStack border = glass("§8", (short) 15);
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, border);

        ItemStack info = new ItemStack(nearest.type.getMaterial());
        ItemMeta meta = info.getItemMeta();
        meta.setDisplayName("§a§l" + nearest.type.getDisplay());
        List<String> lore = new ArrayList<String>();
        lore.add("§7Task: §f" + nearest.name);
        lore.add("§7GUI propria do Sabotage.");
        lore.add("§7Sem item no inventario: use o holograma.");
        lore.add("");
        lore.add("§eComplete o mini-game para finalizar.");
        meta.setLore(lore);
        info.setItemMeta(meta);
        inv.setItem(4, info);

        taskGuiStep.put(player.getUniqueId(), 0);
        taskGuiSequence.remove(player.getUniqueId());

        if (nearest.type == TaskType.WIRING) {
            inv.setItem(10, colorWool("§c1. Fio Vermelho", (short) 14));
            inv.setItem(12, colorWool("§91. Conector Azul", (short) 11));
            inv.setItem(14, colorWool("§e1. Cabo Amarelo", (short) 4));
            inv.setItem(16, colorWool("§a1. Terminal Verde", (short) 5));
            inv.setItem(37, named(Material.REDSTONE, "§c2. Fio Vermelho"));
            inv.setItem(39, named(Material.INK_SACK, "§92. Conector Azul"));
            inv.setItem(41, named(Material.GOLD_INGOT, "§e2. Cabo Amarelo"));
            inv.setItem(43, named(Material.EMERALD, "§a2. Terminal Verde"));
            inv.setItem(22, named(Material.TRIPWIRE_HOOK, "§eClique na sequencia: §cVermelho §7> §9Azul §7> §eAmarelo §7> §aVerde"));
        } else if (nearest.type == TaskType.UNLOCK_MANIFOLDS) {
            for (int i = 1; i <= 9; i++) inv.setItem(9 + i, named(Material.STONE_BUTTON, "§e" + i));
            inv.setItem(31, named(Material.REDSTONE_COMPARATOR, "§7Clique de §e1 §7até §e9§7."));
        } else if (nearest.type == TaskType.START_REACTOR) {
            List<Integer> seq = new ArrayList<Integer>();
            int[] slots = new int[] {20, 21, 22, 23, 24};
            for (int i = 0; i < 5; i++) seq.add(slots[random.nextInt(slots.length)]);
            taskGuiSequence.put(player.getUniqueId(), seq);
            for (int slot : slots) inv.setItem(slot, named(Material.REDSTONE_TORCH_ON, "§cSimon"));
            inv.setItem(31, named(Material.NOTE_BLOCK, "§eMemorize: " + sequenceText(seq) + " §7e clique na ordem."));
        } else if (nearest.type == TaskType.SWIPE_CARD) {
            inv.setItem(20, named(Material.EMPTY_MAP, "§eCartao"));
            inv.setItem(21, glass("§aPasse", (short) 5));
            inv.setItem(22, glass("§aPasse", (short) 5));
            inv.setItem(23, glass("§aPasse", (short) 5));
            inv.setItem(24, named(Material.PAPER, "§aFinalizar Swipe Card"));
        } else if (nearest.type == TaskType.UPLOAD_DATA) {
            inv.setItem(20, named(Material.PAPER, "§fArquivo 1"));
            inv.setItem(22, named(Material.BOOK, "§fArquivo 2"));
            inv.setItem(24, named(Material.COMPASS, "§aUpload Data"));
        } else if (nearest.type == TaskType.PRIME_SHIELDS) {
            for (int slot : new int[] {19,20,21,28,29,30,37,38,39}) inv.setItem(slot, glass("§aEscudo", (short) 5));
            inv.setItem(24, named(Material.INK_SACK, "§aPrime Shields"));
        } else if (nearest.type == TaskType.FUEL_ENGINE) {
            inv.setItem(20, named(Material.BUCKET, "§7Galão vazio"));
            inv.setItem(22, named(Material.LAVA_BUCKET, "§eEncher combustivel"));
            inv.setItem(24, named(Material.MINECART, "§aFuel Engine"));
        } else if (nearest.type == TaskType.CALIBRATE_DISTRIBUTOR) {
            inv.setItem(20, named(Material.WATCH, "§e1. Calibrar"));
            inv.setItem(22, named(Material.REDSTONE_COMPARATOR, "§e2. Sincronizar"));
            inv.setItem(24, named(Material.REDSTONE, "§a3. Ligar energia"));
        } else if (nearest.type == TaskType.CLEAN_O2) {
            inv.setItem(20, named(Material.LEAVES, "§2Filtro sujo"));
            inv.setItem(22, named(Material.SHEARS, "§aLimpar O2"));
            inv.setItem(24, named(Material.SAPLING, "§aFiltro limpo"));
        } else if (nearest.type == TaskType.EMPTY_GARBAGE) {
            inv.setItem(20, named(Material.ROTTEN_FLESH, "§cLixo"));
            inv.setItem(22, named(Material.HOPPER, "§eEsvaziar"));
            inv.setItem(24, named(Material.CAULDRON_ITEM, "§aEmpty Garbage"));
        } else if (nearest.type == TaskType.BOARDING_PASS) {
            inv.setItem(20, named(Material.MAP, "§eBoarding Pass"));
            inv.setItem(22, named(Material.EYE_OF_ENDER, "§bScanner"));
            inv.setItem(24, named(Material.EMERALD, "§aConfirmar identidade"));
        } else if (nearest.type == TaskType.REPAIR_DRILL) {
            inv.setItem(20, named(Material.IRON_PICKAXE, "§cDrill quebrado"));
            inv.setItem(22, named(Material.ANVIL, "§eAjustar peca"));
            inv.setItem(24, named(Material.DIAMOND_PICKAXE, "§aRepair Drill"));
        } else {
            inv.setItem(22, named(nearest.type.getMaterial(), "§a" + nearest.type.getActionName()));
        }

        pendingTask.put(player.getUniqueId(), new PendingTask(arena, nearest));
        player.openInventory(inv);
    }

    private String sequenceText(List<Integer> seq) {
        if (seq == null || seq.isEmpty()) return "-";
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < seq.size(); i++) {
            if (i > 0) out.append("-");
            out.append(seq.get(i) - 19);
        }
        return out.toString();
    }

    private ItemStack glass(String name, short data) {
        ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE, 1, data);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack named(Material mat, String name) {
        ItemStack item = new ItemStack(mat == null ? Material.STONE : mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack colorWool(String name, short data) {
        ItemStack wool = new ItemStack(Material.WOOL, 1, data);
        ItemMeta meta = wool.getItemMeta();
        meta.setDisplayName(name);
        wool.setItemMeta(meta);
        return wool;
    }

    private void handleTaskGuiClick(Player player, ItemStack item, int rawSlot) {
        PendingTask task = pendingTask.get(player.getUniqueId());
        if (task == null || task.point == null) return;
        String name = item.getItemMeta().hasDisplayName() ? org.bukkit.ChatColor.stripColor(item.getItemMeta().getDisplayName()).toLowerCase() : "";
        TaskType type = task.point.type;

        if (type == TaskType.WIRING) {
            String[] order = new String[] {"vermelho", "azul", "amarelo", "verde"};
            int step = taskGuiStep.containsKey(player.getUniqueId()) ? taskGuiStep.get(player.getUniqueId()) : 0;
            if (step < order.length && name.contains(order[step])) {
                taskGuiStep.put(player.getUniqueId(), step + 1);
                player.playSound(player.getLocation(), Sound.CLICK, 1f, 1.4f);
                if (step + 1 >= order.length) startTaskProgress(player);
                else player.sendMessage("§aFio conectado. §7Proximo: §e" + order[step + 1]);
            } else {
                failTaskClick(player);
            }
            return;
        }

        if (type == TaskType.UNLOCK_MANIFOLDS) {
            int step = taskGuiStep.containsKey(player.getUniqueId()) ? taskGuiStep.get(player.getUniqueId()) : 0;
            int expected = step + 1;
            if (name.equals(String.valueOf(expected))) {
                taskGuiStep.put(player.getUniqueId(), expected);
                player.playSound(player.getLocation(), Sound.CLICK, 1f, 1.5f);
                if (expected >= 9) startTaskProgress(player);
            } else if (name.matches("[1-9]")) {
                taskGuiStep.put(player.getUniqueId(), 0);
                player.sendMessage("§cOrdem errada! Comece do 1 novamente.");
                player.playSound(player.getLocation(), Sound.NOTE_BASS, 1f, 0.6f);
            }
            return;
        }

        if (type == TaskType.START_REACTOR) {
            List<Integer> seq = taskGuiSequence.get(player.getUniqueId());
            int step = taskGuiStep.containsKey(player.getUniqueId()) ? taskGuiStep.get(player.getUniqueId()) : 0;
            int clicked = rawSlot;
            if (seq != null && step < seq.size() && clicked == seq.get(step)) {
                taskGuiStep.put(player.getUniqueId(), step + 1);
                player.playSound(player.getLocation(), Sound.NOTE_PLING, 1f, 1.2f + (step * 0.1f));
                if (step + 1 >= seq.size()) startTaskProgress(player);
            } else if (name.contains("simon")) {
                taskGuiStep.put(player.getUniqueId(), 0);
                player.sendMessage("§cSequencia errada! Olhe a dica e tente de novo.");
                player.playSound(player.getLocation(), Sound.NOTE_BASS, 1f, 0.6f);
            }
            return;
        }

        // Tasks simples: 3 cliques corretos em partes verdes/confirmar, estilo mini progresso.
        if (name.contains("finalizar") || name.contains("upload") || name.contains("prime") || name.contains("fuel")
                || name.contains("ligar") || name.contains("limpar") || name.contains("empty") || name.contains("confirmar")
                || name.contains("repair") || name.contains("calibrar") || name.contains("sincronizar") || name.contains("esvaziar")) {
            int step = taskGuiStep.containsKey(player.getUniqueId()) ? taskGuiStep.get(player.getUniqueId()) : 0;
            taskGuiStep.put(player.getUniqueId(), step + 1);
            player.playSound(player.getLocation(), Sound.CLICK, 1f, 1.2f);
            if (step + 1 >= 2 || name.contains("finalizar") || name.contains("confirmar") || name.contains("repair") || name.contains("empty") || name.contains("upload") || name.contains("fuel") || name.contains("prime")) {
                startTaskProgress(player);
            } else {
                player.sendMessage("§eTask em progresso... §7clique na proxima parte.");
            }
            return;
        }

        failTaskClick(player);
    }

    private void failTaskClick(Player player) {
        player.playSound(player.getLocation(), Sound.NOTE_BASS, 0.8f, 0.7f);
        player.sendMessage("§cClique errado nessa task.");
    }

    private void startTaskProgress(final Player player) {
        final PendingTask task = pendingTask.get(player.getUniqueId());
        if (task == null || task.arena == null || task.point == null) return;
        if (task.running) {
            player.sendMessage("§eVocê já está fazendo essa task.");
            return;
        }
        task.running = true;
        player.closeInventory();
        player.sendMessage("§aCompletando task... §7Não saia de perto.");
        player.playSound(player.getLocation(), Sound.CLICK, 1f, 1.2f);
        final int seconds = plugin.getConfig().getInt("Sabotage.TaskGuiSeconds", 3);
        new BukkitRunnable() {
            @Override
            public void run() {
                PendingTask current = pendingTask.remove(player.getUniqueId());
                taskGuiStep.remove(player.getUniqueId());
                taskGuiSequence.remove(player.getUniqueId());
                if (current == null || !player.isOnline()) return;
                Arena arena = Arenas.getArena(player);
                if (arena == null || arena != current.arena || arena.getState() != GameState.INGAME) return;
                if (current.point.location.distance(player.getLocation()) > plugin.getConfig().getDouble("Sabotage.TaskRadius", 2.5D) + 1.0D) {
                    player.sendMessage("§cTask cancelada: você saiu de perto.");
                    return;
                }
                completeTask(player, arena, current.point);
            }
        }.runTaskLater(plugin, Math.max(1, seconds) * 20L);
    }

    private boolean isTaskCompleted(Player player, Arena arena, TaskPoint point) {
        String key = getTaskKey(player, arena, point);
        Set<UUID> playerCompleted = completedTasks.get(key);
        return playerCompleted != null && playerCompleted.contains(player.getUniqueId());
    }

    private boolean completeTask(Player player, Arena arena, TaskPoint point) {
        String arenaKey = getArenaKey(arena);
        String key = getTaskKey(player, arena, point);
        Set<UUID> playerCompleted = completedTasks.get(key);
        if (playerCompleted == null) {
            playerCompleted = new HashSet<UUID>();
            completedTasks.put(key, playerCompleted);
        }
        if (playerCompleted.contains(player.getUniqueId())) {
            player.sendMessage("§eVocê já completou essa task.");
            return false;
        }

        playerCompleted.add(player.getUniqueId());
        int done = completedCount.containsKey(arenaKey) ? completedCount.get(arenaKey) : 0;
        done++;
        completedCount.put(arenaKey, done);

        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1f, 1.5f);
        player.getWorld().playEffect(point.location, Effect.MOBSPAWNER_FLAMES, 1);
        broadcast(arena, "§a" + player.getName() + " completou uma task! §7(" + getTaskPercent(arena) + "%)");

        // Atualiza a scoreboard do AMONG US instantaneamente.
        // Antes ela dependia somente do loop normal da scoreboard, causando delay ao trocar para a próxima task.
        forceUpdateSabotageScoreboard(arena);

        // Se uma sabotagem ativa precisava exatamente desta task, resolve na hora.
        resolveSabotageRepair(player, arena, point.type);

        if (getTaskPercent(arena) >= plugin.getConfig().getInt("Sabotage.TaskWinPercent", 100)) {
            sendAmongUsVictory(arena, true);
            arena.win("i");
        }
        return true;
    }

    public void forceUpdateSabotageScoreboard(Arena arena) {
        if (arena == null || !isSabotageArena(arena)) return;
        for (Player viewer : arena.getPlayers()) {
            if (viewer == null || !viewer.isOnline()) continue;
            ScoreboardManager board = plugin.scoreboards.get(viewer.getName());
            if (board == null) continue;
            board.setTitle(0, Utils.FormatText(viewer, plugin.getScoreboardTitleByMode(arena)));
            List<String> lines = plugin.messages.getConfig().getStringList("sabotage-scoreboard-lines");
            int size = lines.size();
            for (String line : lines) {
                if (line != null && line.contains("%sabotage_tasks%")) {
                    line = "&eTask: %sabotage_tasks%";
                }
                board.setLine(0, size, Utils.FormatText(viewer, line
                        .replaceAll("%spectators%", String.valueOf(arena.specs.size()))
                        .replaceAll("%innocents%", String.valueOf(arena.innocents.size()))
                        .replaceAll("%kills%", String.valueOf(arena.getkill(viewer)))
                        .replaceAll("%score%", String.valueOf(arena.getscore(viewer)))
                        .replaceAll("%coins%", plugin.getPlayerData(viewer) != null ? String.valueOf(plugin.getPlayerData(viewer).getcoins()) : "0")
                        .replaceAll("%role%", String.valueOf(arena.getRole(viewer)))
                        .replaceAll("%mode%", arena.getRoomModeNamePlain())
                        .replaceAll("%room%", arena.getRoomDisplayName())
                        .replaceAll("%alive%", String.valueOf(arena.getAlivePlayersCount()))
                        .replaceAll("%tasks_percent%", String.valueOf(getTaskPercent(arena)))
                        .replaceAll("%sabotage_tasks%", getPlayerTaskList(viewer, arena))
                        .replaceAll("%sabotage_corpses%", String.valueOf(getAliveCorpseCount(arena)))
                        .replaceAll("%sabotage_meeting_time%", String.valueOf(getMeetingTimeLeft(arena)))
                        .replaceAll("%map%", plugin.getScoreboardMapName(arena, viewer, false))
                        .replaceAll("%time%", Utils.formattominutes(arena.time))));
                size--;
            }
            if (board.getScoreboard() != viewer.getScoreboard()) board.toggleScoreboard();
        }
    }

    public void sendAmongUsVictory(Arena arena, boolean crewWin) {
        if (arena == null) return;
        String title = crewWin ? "§a§lCREWMATES VENCERAM" : "§c§lMURDER VENCEU";
        String subtitle = crewWin ? "§fTodas as tasks foram concluídas!" : "§fO Murder dominou a nave!";
        broadcast(arena, "§8§m----------------------------------------");
        broadcast(arena, crewWin ? "§a§lAMONG US §7> §fOs tripulantes venceram!" : "§c§lAMONG US §7> §fO Murder venceu!");
        broadcast(arena, crewWin ? "§7Motivo: §aTasks completas." : "§7Motivo: §cTripulação eliminada.");
        broadcast(arena, "§8§m----------------------------------------");
        for (Player p : arena.getPlayers()) {
            if (p == null || !p.isOnline()) continue;
            try { me.spwtyz.murder.events.TitleAPI.sendTitle(p, 5, 60, 10, title, subtitle); } catch (Throwable ignored) {}
            p.playSound(p.getLocation(), crewWin ? Sound.LEVEL_UP : Sound.WITHER_DEATH, 1f, 1f);
        }
    }

    public boolean tryCompleteNearbyTask(Player player, Arena arena) {
        return openNearbyTaskGui(player, arena);
    }

    public int getTaskPercent(Arena arena) {
        if (arena == null) return 0;
        int max = 0;
        for (Player p : arena.getPlayers()) {
            if (p == null || !p.isOnline() || arena.specs.contains(p)) continue;
            PlayerType type = arena.getType(p);
            if (type == PlayerType.Innocents || type == PlayerType.Detective) {
                assignTasks(p, arena);
                List<String> list = assignedTasks.get(p.getUniqueId());
                max += list == null ? 0 : list.size();
            }
        }
        max = Math.max(1, max);
        int done = completedCount.containsKey(getArenaKey(arena)) ? completedCount.get(getArenaKey(arena)) : 0;
        return Math.min(100, (int) Math.round((done * 100.0D) / max));
    }


    public int getAliveCorpseCount(Arena arena) {
        if (arena == null) return 0;
        int total = 0;
        for (SabotageCorpse corpse : corpses.values()) {
            if (corpse != null && !corpse.reported && corpse.location != null) total++;
        }
        return total;
    }

    public long getMeetingTimeLeft(Arena arena) {
        if (arena == null) return 0L;
        Long end = meetingEndTime.get(getArenaKey(arena));
        if (end == null) return 0L;
        return Math.max(0L, (end - System.currentTimeMillis()) / 1000L);
    }

    public void handleDeath(Player victim, Player killer, Arena arena, CorpseData corpseData) {
        if (!plugin.getConfig().getBoolean("Sabotage.Corpses.Enabled", true)) return;
        if (!isSabotageArena(arena) || victim == null) return;
        if (arena.getType(victim) == PlayerType.Murderer) return;

        Location loc = victim.getLocation().clone();
        ArmorStand marker = (ArmorStand) loc.getWorld().spawnEntity(loc.clone().add(0, 0.15, 0), EntityType.ARMOR_STAND);
        marker.setVisible(false);
        marker.setGravity(false);
        marker.setSmall(true);
        marker.setCustomNameVisible(true);
        marker.setCustomName("§c§lREPORTAR CORPO §7(" + victim.getName() + ")");

        SabotageCorpse corpse = new SabotageCorpse(victim.getUniqueId(), killer == null ? null : killer.getUniqueId(), victim.getName(), loc, System.currentTimeMillis(), marker, corpseData);
        corpses.put(marker.getUniqueId(), corpse);
        broadcast(arena, "§7Um corpo caiu no mapa. §eClique no corpo para reportar.");
        startDetectiveClues(arena, loc, killer == null ? null : killer.getLocation().clone());
    }

    @EventHandler
    public void onReport(PlayerInteractAtEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (!(entity instanceof ArmorStand)) return;

        TaskPoint taskPoint = taskMarkers.get(entity.getUniqueId());
        if (taskPoint != null) {
            Player player = event.getPlayer();
            Arena arena = Arenas.getArena(player);
            if (isSabotageArena(arena) && arena.getState() == GameState.INGAME) {
                event.setCancelled(true);
                openNearbyTaskGui(player, arena);
            }
            return;
        }

        SabotageCorpse corpse = corpses.get(entity.getUniqueId());
        if (corpse == null || corpse.reported) return;

        Player reporter = event.getPlayer();
        Arena arena = Arenas.getArena(reporter);
        if (!isSabotageArena(arena) || arena.getState() != GameState.INGAME) return;
        if (arena.specs.contains(reporter) || arena.getType(reporter) == PlayerType.Murderer) return;

        event.setCancelled(true);
        corpse.reported = true;

        broadcast(arena, "§6§lCORPO REPORTADO! §e" + reporter.getName() + " encontrou o corpo de §c" + corpse.victimName + "§e.");
        if (arena.getType(reporter) == PlayerType.Detective) {
            long seconds = Math.max(1L, (System.currentTimeMillis() - corpse.deathTime) / 1000L);
            reporter.sendMessage("§b§lINVESTIGAÇÃO §7» §fA morte aconteceu há §e" + seconds + "s§f.");
            if (corpse.killer != null && random.nextInt(100) < 35) {
                reporter.sendMessage("§b§lPERÍCIA §7» §fO assassino esteve por perto da cena.");
            }
        }
        startMeeting(arena, reporter, corpse);
    }

    private void callEmergencyMeeting(Player caller, Arena arena) {
        if (caller == null || arena == null) return;
        if (arena.specs.contains(caller)) {
            caller.sendMessage("§cEspectadores não podem chamar reunião.");
            return;
        }
        if (arena.getType(caller) == PlayerType.Murderer) {
            caller.sendMessage("§cO Murder não pode chamar reunião de emergência.");
            return;
        }
        String key = getArenaKey(arena);
        if (Boolean.TRUE.equals(meetingActive.get(key))) {
            caller.sendMessage("§cJá existe uma reunião acontecendo.");
            return;
        }
        Set<UUID> used = emergencyMeetingsUsed.get(key);
        if (used == null) {
            used = new HashSet<UUID>();
            emergencyMeetingsUsed.put(key, used);
        }
        int limit = plugin.getConfig().getInt("Sabotage.EmergencyMeetingsPerPlayer", 1);
        if (used.contains(caller.getUniqueId()) && limit <= 1) {
            caller.sendMessage("§cVocê já usou sua reunião de emergência nesta partida.");
            return;
        }
        used.add(caller.getUniqueId());
        SabotageCorpse emergency = new SabotageCorpse(caller.getUniqueId(), null, "Reunião de Emergência", caller.getLocation().clone(), System.currentTimeMillis(), null, null);
        broadcast(arena, "§c§lREUNIÃO DE EMERGÊNCIA! §e" + caller.getName() + " chamou uma reunião.");
        startMeeting(arena, caller, emergency);
    }

    private void startMeeting(final Arena arena, Player reporter, SabotageCorpse corpse) {
        final String key = getArenaKey(arena);
        if (Boolean.TRUE.equals(meetingActive.get(key))) return;
        meetingActive.put(key, true);
        cleanupCorpses(arena);
        votes.put(key, new HashMap<UUID, UUID>());
        voteOpen.put(key, false);
        meetingEndTime.put(key, System.currentTimeMillis() + ((plugin.getConfig().getInt("Sabotage.MeetingDiscussionSeconds", 15) + plugin.getConfig().getInt("Sabotage.MeetingVoteSeconds", 20)) * 1000L));
        saveAndClearMeetingInventories(arena);
        freezeMeetingPlayers(arena);

        int discussion = plugin.getConfig().getInt("Sabotage.MeetingDiscussionSeconds", 15);
        for (Player p : arena.getPlayers()) {
            if (p == null || !p.isOnline()) continue;
            p.playSound(p.getLocation(), Sound.NOTE_PLING, 1f, 1f);
            p.sendMessage("§8§m-----------------------------");
            p.sendMessage("§5§lREUNIÃO DE EMERGÊNCIA");
            p.sendMessage("§7Reportado por: §f" + reporter.getName());
            p.sendMessage("§7Corpo: §c" + corpse.victimName);
            p.sendMessage("§7Discussão: §e" + discussion + "s");
            p.sendMessage("§8§m-----------------------------");
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (arena.getState() != GameState.INGAME || !Boolean.TRUE.equals(meetingActive.get(key))) return;
                openVoteForAll(arena);
                startVoteCountdown(arena);
            }
        }.runTaskLater(plugin, Math.max(1, discussion) * 20L);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!meetingFrozen.contains(event.getPlayer().getUniqueId())) return;
        if (event.getFrom().getBlockX() != event.getTo().getBlockX()
                || event.getFrom().getBlockY() != event.getTo().getBlockY()
                || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            event.setTo(event.getFrom());
        }
    }

    private void saveAndClearMeetingInventories(Arena arena) {
        String key = getArenaKey(arena);
        Map<UUID, ItemStack[]> contents = new HashMap<UUID, ItemStack[]>();
        Map<UUID, ItemStack[]> armors = new HashMap<UUID, ItemStack[]>();
        for (Player p : arena.getPlayers()) {
            if (p == null || !p.isOnline() || arena.specs.contains(p)) continue;
            contents.put(p.getUniqueId(), p.getInventory().getContents());
            armors.put(p.getUniqueId(), p.getInventory().getArmorContents());
            p.getInventory().clear();
            p.getInventory().setArmorContents(new ItemStack[4]);
            p.updateInventory();
        }
        meetingInventory.put(key, contents);
        meetingArmor.put(key, armors);
    }

    private void restoreMeetingInventories(Arena arena) {
        if (arena == null) return;
        String key = getArenaKey(arena);
        Map<UUID, ItemStack[]> contents = meetingInventory.remove(key);
        Map<UUID, ItemStack[]> armors = meetingArmor.remove(key);
        if (contents == null) return;
        for (Player p : arena.getPlayers()) {
            if (p == null || !p.isOnline() || arena.specs.contains(p)) continue;
            ItemStack[] inv = contents.get(p.getUniqueId());
            if (inv != null) p.getInventory().setContents(inv);
            if (armors != null && armors.get(p.getUniqueId()) != null) p.getInventory().setArmorContents(armors.get(p.getUniqueId()));
            p.updateInventory();
        }
    }

    public boolean canMurderKill(Player murder, Arena arena) {
        if (murder == null || !isSabotageArena(arena)) return true;
        if (arena.getType(murder) != PlayerType.Murderer) return false;
        Long end = sabotageKillCooldown.get(murder.getUniqueId());
        return end == null || end <= System.currentTimeMillis();
    }

    public void resetMurderKillCooldown(Player murder) {
        if (murder == null) return;
        int seconds = plugin.getConfig().getInt("Sabotage.MurderKillCooldownSeconds", 25);
        startMurderKillCooldown(murder, seconds, Arenas.getArena(murder));
    }

    public void applyMurderKillCooldown(final Player murder, final Arena arena) {
        if (murder == null || !murder.isOnline() || !isSabotageArena(arena)) return;
        int seconds = plugin.getConfig().getInt("Sabotage.MurderKillCooldownSeconds", 25);
        startMurderKillCooldown(murder, seconds, arena);
    }

    private void startMurderKillCooldown(final Player murder, int seconds, final Arena arena) {
        if (murder == null || !murder.isOnline()) return;
        final UUID id = murder.getUniqueId();
        final long end = System.currentTimeMillis() + (Math.max(1, seconds) * 1000L);
        sabotageKillCooldown.put(id, end);
        setWaitingKnife(murder);

        Integer oldTask = murderCooldownTaskIds.remove(id);
        if (oldTask != null) {
            try { Bukkit.getScheduler().cancelTask(oldTask.intValue()); } catch (Throwable ignored) {}
        }

        int taskId = new BukkitRunnable() {
            @Override
            public void run() {
                Player p = Bukkit.getPlayer(id);
                if (p == null || !p.isOnline()) {
                    murderCooldownTaskIds.remove(id);
                    cancel();
                    return;
                }
                Arena current = Arenas.getArena(p);
                if (current == null || current != arena || current.getState() != GameState.INGAME || current.getType(p) != PlayerType.Murderer || current.specs.contains(p)) {
                    murderCooldownTaskIds.remove(id);
                    cancel();
                    return;
                }
                Long currentEnd = sabotageKillCooldown.get(id);
                if (currentEnd != null && currentEnd <= System.currentTimeMillis()) {
                    sabotageKillCooldown.remove(id);
                    murderCooldownTaskIds.remove(id);
                    giveRealKnife(p);
                    cancel();
                    return;
                }
                setWaitingKnife(p);
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
        murderCooldownTaskIds.put(id, taskId);
    }

    public void setWaitingKnife(Player murder) {
        if (murder == null || !murder.isOnline()) return;
        ItemStack waitSword = new ItemStack(Material.WOOD_SWORD);
        ItemMeta meta = waitSword.getItemMeta();
        long left = Math.max(1L, getMurderKillCooldownLeft(murder));
        meta.setDisplayName("§cAguarde §e" + left + "s");
        List<String> lore = new ArrayList<String>();
        lore.add("§7Cooldown do AMONG US: §c" + left + "s");
        lore.add("§8amongus-wait-knife");
        meta.setLore(lore);
        waitSword.setItemMeta(meta);
        murder.getInventory().setItem(0, waitSword);
        murder.updateInventory();
    }

    public void giveRealKnife(Player murder) {
        if (murder == null || !murder.isOnline()) return;
        Arena arena = Arenas.getArena(murder);
        if (isSabotageArena(arena) && !canMurderKill(murder, arena)) {
            setWaitingKnife(murder);
            return;
        }
        ItemStack knife = plugin.knifeSkinManager != null ? plugin.knifeSkinManager.createKnife(murder) : new ItemStack(Material.IRON_SWORD);
        murder.getInventory().setItem(0, knife);
        murder.updateInventory();
        murder.playSound(murder.getLocation(), Sound.ITEM_PICKUP, 1f, 1f);
    }

    public long getMurderKillCooldownLeft(Player murder) {
        if (murder == null) return 0L;
        Long end = sabotageKillCooldown.get(murder.getUniqueId());
        if (end == null) return 0L;
        return Math.max(0L, (end - System.currentTimeMillis() + 999L) / 1000L);
    }

    public boolean isWaitingKnife(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta.hasDisplayName() && meta.getDisplayName().startsWith("§cAguarde")) return true;
        if (meta.hasLore()) {
            for (String line : meta.getLore()) {
                if (line != null && line.contains("amongus-wait-knife")) return true;
            }
        }
        return false;
    }

    public void activateSabotageRepair(Arena arena, TaskType repairTask, String sabotageName) {
        if (arena == null || repairTask == null || !plugin.getConfig().getBoolean("Sabotage.Repairs.Enabled", true)) return;
        String key = getArenaKey(arena);
        activeSabotageRepairs.put(key, repairTask);
        activeSabotageNames.put(key, sabotageName == null ? repairTask.getDisplay() : sabotageName);
        broadcast(arena, "§c§lSABOTAGEM ATIVA! §7Resolva a task §e" + repairTask.getDisplay() + " §7para consertar.");
        forceUpdateSabotageScoreboard(arena);
    }

    private boolean resolveSabotageRepair(Player player, Arena arena, TaskType completedType) {
        if (arena == null || completedType == null) return false;
        String key = getArenaKey(arena);
        TaskType needed = activeSabotageRepairs.get(key);
        if (needed == null || needed != completedType) return false;
        String name = activeSabotageNames.containsKey(key) ? activeSabotageNames.remove(key) : needed.getDisplay();
        activeSabotageRepairs.remove(key);
        for (Player p : arena.getPlayers()) {
            if (p == null || !p.isOnline()) continue;
            p.removePotionEffect(PotionEffectType.POISON);
            p.removePotionEffect(PotionEffectType.BLINDNESS);
            p.removePotionEffect(PotionEffectType.CONFUSION);
            p.removePotionEffect(PotionEffectType.SLOW_DIGGING);
            p.removePotionEffect(PotionEffectType.SLOW);
            p.playSound(p.getLocation(), Sound.LEVEL_UP, 0.7F, 1.6F);
        }
        broadcast(arena, "§a§l" + name + " RESOLVIDO! §7" + player.getName() + " consertou a sabotagem.");
        forceUpdateSabotageScoreboard(arena);
        return true;
    }

    private void freezeMeetingPlayers(Arena arena) {
        if (!plugin.getConfig().getBoolean("Sabotage.FreezeDuringMeeting", true)) return;
        for (Player p : arena.getPlayers()) {
            if (p == null || !p.isOnline() || arena.specs.contains(p)) continue;
            meetingFrozen.add(p.getUniqueId());
        }
    }

    private void unfreezeMeetingPlayers(Arena arena) {
        if (arena == null) return;
        for (Player p : arena.getPlayers()) {
            if (p != null) meetingFrozen.remove(p.getUniqueId());
        }
    }

    private void openVoteForAll(Arena arena) {
        voteOpen.put(getArenaKey(arena), true);
        for (Player p : arena.getPlayers()) {
            if (p == null || !p.isOnline() || arena.specs.contains(p)) continue;
            openVoteMenu(p, arena);
        }
    }

    private void openVoteMenu(Player voter, Arena arena) {
        Inventory inv = Bukkit.createInventory(null, 54, "Votação AMONG US");
        for (Player target : arena.getPlayers()) {
            if (target == null || !target.isOnline() || arena.specs.contains(target)) continue;
            ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwner(target.getName());
            meta.setDisplayName("§eVotar em §f" + target.getName());
            List<String> lore = new ArrayList<String>();
            lore.add("§7Clique para votar neste jogador.");
            if (plugin.amongUsColorManager != null) {
                lore.add("§7Cor: " + plugin.amongUsColorManager.getColorDisplay(target));
            }
            meta.setLore(lore);
            head.setItemMeta(meta);
            inv.addItem(head);
        }
        ItemStack skip = new ItemStack(Material.BARRIER);
        ItemMeta meta = skip.getItemMeta();
        meta.setDisplayName("§7Pular Voto");
        List<String> lore = new ArrayList<String>();
        lore.add("§7Não expulsar ninguém nesta rodada.");
        meta.setLore(lore);
        skip.setItemMeta(meta);
        inv.setItem(53, skip);
        voter.openInventory(inv);
    }

    private void handleVoteClick(Player voter, ItemStack item) {
        Arena arena = Arenas.getArena(voter);
        if (!isSabotageArena(arena) || arena.getState() != GameState.INGAME || arena.specs.contains(voter)) {
            voter.closeInventory();
            return;
        }
        String key = getArenaKey(arena);
        if (!Boolean.TRUE.equals(meetingActive.get(key))) {
            voter.closeInventory();
            return;
        }
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;
        String name = org.bukkit.ChatColor.stripColor(item.getItemMeta().getDisplayName());

        Map<UUID, UUID> arenaVotes = votes.get(key);
        if (arenaVotes == null) {
            arenaVotes = new HashMap<UUID, UUID>();
            votes.put(key, arenaVotes);
        }

        if (item.getType() == Material.BARRIER || name.toLowerCase().contains("pular")) {
            arenaVotes.put(voter.getUniqueId(), null);
            voter.sendMessage("§7Você pulou o voto.");
            voter.closeInventory();
            return;
        }

        String targetName = name.replace("Votar em ", "").trim();
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null || Arenas.getArena(target) != arena || arena.specs.contains(target)) {
            voter.sendMessage("§cEsse jogador não está mais disponível.");
            return;
        }
        arenaVotes.put(voter.getUniqueId(), target.getUniqueId());
        voter.sendMessage("§aVocê votou em §f" + target.getName() + "§a.");
        voter.closeInventory();
    }

    private void startVoteCountdown(final Arena arena) {
        final String key = getArenaKey(arena);
        int seconds = plugin.getConfig().getInt("Sabotage.MeetingVoteSeconds", 20);
        int old = voteTasks.containsKey(key) ? voteTasks.get(key) : -1;
        if (old != -1) Bukkit.getScheduler().cancelTask(old);
        int id = new BukkitRunnable() {
            @Override
            public void run() {
                finishVoting(arena);
            }
        }.runTaskLater(plugin, Math.max(1, seconds) * 20L).getTaskId();
        voteTasks.put(key, id);
        broadcast(arena, "§5Votação aberta! §7Você tem §e" + seconds + "s§7 para votar.");
    }

    private void finishVoting(Arena arena) {
        if (arena == null) return;
        String key = getArenaKey(arena);
        if (!Boolean.TRUE.equals(meetingActive.get(key))) return;

        Map<UUID, UUID> arenaVotes = votes.get(key);
        Map<UUID, Integer> count = new HashMap<UUID, Integer>();
        int skips = 0;
        if (arenaVotes != null) {
            for (UUID voted : arenaVotes.values()) {
                if (voted == null) skips++;
                else count.put(voted, count.containsKey(voted) ? count.get(voted) + 1 : 1);
            }
        }

        UUID top = null;
        int topVotes = 0;
        boolean tie = false;
        for (Map.Entry<UUID, Integer> entry : count.entrySet()) {
            if (entry.getValue() > topVotes) {
                top = entry.getKey();
                topVotes = entry.getValue();
                tie = false;
            } else if (entry.getValue() == topVotes) {
                tie = true;
            }
        }

        meetingActive.remove(key);
        voteOpen.remove(key);
        meetingEndTime.remove(key);
        votes.remove(key);
        voteTasks.remove(key);
        unfreezeMeetingPlayers(arena);
        restoreMeetingInventories(arena);

        if (top == null || topVotes <= skips || tie) {
            broadcast(arena, "§7Ninguém foi expulso. §8(" + skips + " votos para pular)");
            return;
        }

        Player ejected = Bukkit.getPlayer(top);
        if (ejected == null || Arenas.getArena(ejected) != arena || arena.specs.contains(ejected)) {
            broadcast(arena, "§7Ninguém foi expulso.");
            return;
        }

        broadcast(arena, "§c" + ejected.getName() + " foi expulso da nave! §7(" + topVotes + " votos)");
        if (arena.getType(ejected) == PlayerType.Murderer) {
            for (Player viewer : arena.getPlayers()) {
                if (viewer != null && viewer.isOnline()) {
                    me.spwtyz.murder.events.TitleAPI.sendTitle(viewer, 5, 60, 10, "§a§l" + ejected.getName() + " ERA o MURDER.");
                    me.spwtyz.murder.events.TitleAPI.sendSubtitle(viewer, 5, 60, 10, "§7Os inocentes venceram.");
                }
            }
            broadcast(arena, "§aO expulso era o Murder! Inocentes venceram.");
            arena.win("i");
        } else {
            for (Player viewer : arena.getPlayers()) {
                if (viewer != null && viewer.isOnline()) {
                    me.spwtyz.murder.events.TitleAPI.sendTitle(viewer, 5, 50, 10, "§c§l" + ejected.getName() + " NÃO era o MURDER.");
                    me.spwtyz.murder.events.TitleAPI.sendSubtitle(viewer, 5, 50, 10, "§7A partida continua...");
                }
            }
            broadcast(arena, "§7O expulso não era o Murder...");
            if (plugin.spectatorManager != null) {
                plugin.spectatorManager.makeSpectator(ejected, arena, "§cVocê foi expulso pela votação.");
            } else {
                arena.players.remove(ejected);
                if (!arena.specs.contains(ejected)) arena.specs.add(ejected);
            }
        }
    }

    private void startDetectiveClues(final Arena arena, final Location deathLocation, final Location killerLocation) {
        if (!plugin.getConfig().getBoolean("Sabotage.DetectiveClues.Enabled", true)) return;
        if (arena == null || deathLocation == null) return;
        final int seconds = plugin.getConfig().getInt("Sabotage.DetectiveClues.Seconds", 15);
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (arena.getState() != GameState.INGAME || ticks++ >= seconds) {
                    cancel();
                    return;
                }
                for (Player detective : arena.getPlayers()) {
                    if (detective == null || !detective.isOnline() || arena.specs.contains(detective)) continue;
                    if (arena.getType(detective) != PlayerType.Detective) continue;
                    detective.playEffect(deathLocation.clone().add(0, 0.2, 0), Effect.SMOKE, 4);
                    if (killerLocation != null && killerLocation.getWorld() != null && killerLocation.getWorld().equals(deathLocation.getWorld())) {
                        detective.playEffect(killerLocation.clone().add(0, 0.2, 0), Effect.COLOURED_DUST, 1);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void cleanupCorpses(Arena arena) {
        List<UUID> remove = new ArrayList<UUID>();
        for (Map.Entry<UUID, SabotageCorpse> entry : corpses.entrySet()) {
            SabotageCorpse corpse = entry.getValue();
            if (arena == null || corpse.location.getWorld() != null) {
                if (corpse.marker != null && !corpse.marker.isDead()) corpse.marker.remove();
                remove.add(entry.getKey());
            }
        }
        for (UUID uuid : remove) corpses.remove(uuid);
    }

    public void cleanupArena(Arena arena) {
        if (arena == null) return;
        cleanupCorpses(arena);
        String key = getArenaKey(arena);
        completedCount.remove(key);
        List<String> completedRemove = new ArrayList<String>();
        for (String taskKey : completedTasks.keySet()) {
            if (taskKey != null && taskKey.startsWith(key + ":")) completedRemove.add(taskKey);
        }
        for (String taskKey : completedRemove) completedTasks.remove(taskKey);
        meetingActive.remove(key);
        voteOpen.remove(key);
        meetingEndTime.remove(key);
        emergencyMeetingsUsed.remove(key);
        unfreezeMeetingPlayers(arena);
        restoreMeetingInventories(arena);
        votes.remove(key);
        Integer taskId = voteTasks.remove(key);
        if (taskId != null && taskId != -1) Bukkit.getScheduler().cancelTask(taskId);
        List<UUID> toRemove = new ArrayList<UUID>();
        for (UUID uuid : assignedTasks.keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null || Arenas.getArena(p) == arena) toRemove.add(uuid);
        }
        for (UUID uuid : toRemove) assignedTasks.remove(uuid);
        toRemove.clear();
        for (UUID uuid : pendingTask.keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null || Arenas.getArena(p) == arena) toRemove.add(uuid);
        }
        for (UUID uuid : toRemove) pendingTask.remove(uuid);
    }

    private TaskPoint getNearestTask(Player player, Arena arena) {
        List<TaskPoint> points = getTaskPoints(arena);
        if (points.isEmpty()) return null;
        TaskPoint nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (TaskPoint point : points) {
            if (point.location.getWorld() == null || !point.location.getWorld().equals(player.getWorld())) continue;
            double distance = point.location.distance(player.getLocation());
            if (distance < nearestDistance) {
                nearest = point;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private List<TaskPoint> getTaskPoints(Arena arena) {
        List<TaskPoint> all = getTaskPoints();
        if (arena == null) return all;
        List<TaskPoint> filtered = new ArrayList<TaskPoint>();
        for (TaskPoint point : all) {
            if (point == null || point.location == null || point.location.getWorld() == null) continue;
            if (isArenaUsingTaskWorld(arena, point.location.getWorld().getName())) filtered.add(point);
        }
        return filtered;
    }

    private List<TaskPoint> getTaskPoints() {
        List<TaskPoint> points = new ArrayList<TaskPoint>();
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("Sabotage.TaskLocations");
        if (sec == null) return points;
        for (String id : sec.getKeys(false)) {
            String path = "Sabotage.TaskLocations." + id;
            String world = plugin.getConfig().getString(path + ".world");
            if (world == null || Bukkit.getWorld(world) == null) continue;
            Location loc = new Location(Bukkit.getWorld(world), plugin.getConfig().getDouble(path + ".x"), plugin.getConfig().getDouble(path + ".y"), plugin.getConfig().getDouble(path + ".z"));
            String name = plugin.getConfig().getString(path + ".name", id);
            String typeName = plugin.getConfig().getString(path + ".type", detectTaskType(name).name());
            TaskType type = parseTaskType(typeName);
            points.add(new TaskPoint(id, name, loc, type));
        }
        return points;
    }

    private String getTaskKey(Player player, Arena arena, TaskPoint point) {
        return getArenaKey(arena) + ":" + player.getUniqueId().toString() + ":" + point.id;
    }

    private String getArenaKey(Arena arena) {
        return arena == null ? "null" : arena.getName();
    }

    private void broadcast(Arena arena, String message) {
        for (Player p : arena.getPlayers()) {
            if (p != null && p.isOnline()) p.sendMessage(message);
        }
    }

    private static class PendingTask {
        final Arena arena;
        final TaskPoint point;
        boolean running;
        PendingTask(Arena arena, TaskPoint point) {
            this.arena = arena;
            this.point = point;
            this.running = false;
        }
    }

    private TaskType detectTaskType(String rawName) {
        if (rawName == null) return TaskType.UPLOAD_DATA;
        String name = rawName.toLowerCase();
        if (name.contains("wire") || name.contains("fio")) return TaskType.WIRING;
        if (name.contains("card") || name.contains("cartao") || name.contains("cartão")) return TaskType.SWIPE_CARD;
        if (name.contains("reactor") || name.contains("reator")) return TaskType.START_REACTOR;
        if (name.contains("manifold") || name.contains("codigo") || name.contains("código")) return TaskType.UNLOCK_MANIFOLDS;
        if (name.contains("shield") || name.contains("escudo")) return TaskType.PRIME_SHIELDS;
        if (name.contains("fuel") || name.contains("combustivel") || name.contains("combustível")) return TaskType.FUEL_ENGINE;
        if (name.contains("calibrate") || name.contains("calibrar")) return TaskType.CALIBRATE_DISTRIBUTOR;
        if (name.contains("o2") || name.contains("filtro")) return TaskType.CLEAN_O2;
        if (name.contains("garbage") || name.contains("lixo")) return TaskType.EMPTY_GARBAGE;
        if (name.contains("pass") || name.contains("ticket")) return TaskType.BOARDING_PASS;
        if (name.contains("drill") || name.contains("broca")) return TaskType.REPAIR_DRILL;
        return TaskType.UPLOAD_DATA;
    }

    private TaskType parseTaskType(String raw) {
        if (raw == null) return TaskType.UPLOAD_DATA;
        try { return TaskType.valueOf(raw.toUpperCase()); } catch (Exception ignored) { return detectTaskType(raw); }
    }

    public static enum TaskType {
        SWIPE_CARD("Swipe Card", "Passar Cartao", Material.PAPER),
        WIRING("Wiring", "Conectar Fios", Material.REDSTONE),
        UPLOAD_DATA("Upload Data", "Enviar Dados", Material.COMPASS),
        PRIME_SHIELDS("Prime Shields", "Ativar Escudos", Material.INK_SACK),
        UNLOCK_MANIFOLDS("Unlock Manifolds", "Desbloquear Codigo", Material.STONE_BUTTON),
        FUEL_ENGINE("Fuel Engine", "Abastecer Motor", Material.LAVA_BUCKET),
        CALIBRATE_DISTRIBUTOR("Calibrate", "Calibrar Energia", Material.WATCH),
        CLEAN_O2("Clean O2", "Limpar O2", Material.LEAVES),
        EMPTY_GARBAGE("Empty Garbage", "Esvaziar Lixo", Material.CAULDRON_ITEM),
        BOARDING_PASS("Boarding Pass", "Escanear Cartao", Material.MAP),
        REPAIR_DRILL("Repair Drill", "Reparar Broca", Material.IRON_PICKAXE),
        START_REACTOR("Start Reactor", "Iniciar Reator", Material.REDSTONE_TORCH_ON);

        private final String display;
        private final String action;
        private final Material material;
        TaskType(String display, String action, Material material) { this.display = display; this.action = action; this.material = material; }
        public String getDisplay() { return display; }
        public String getActionName() { return action; }
        public Material getMaterial() { return material; }
        public String getCommandName() { return display.toLowerCase().replace(" ", "_"); }
    }

    private static class TaskPoint {
        final String id;
        final String name;
        final Location location;
        final TaskType type;
        TaskPoint(String id, String name, Location location, TaskType type) {
            this.id = id;
            this.name = name;
            this.location = location;
            this.type = type == null ? TaskType.UPLOAD_DATA : type;
        }
    }

    private static class SabotageCorpse {
        final UUID victim;
        final UUID killer;
        final String victimName;
        final Location location;
        final long deathTime;
        final ArmorStand marker;
        final CorpseData corpseData;
        boolean reported;
        SabotageCorpse(UUID victim, UUID killer, String victimName, Location location, long deathTime, ArmorStand marker, CorpseData corpseData) {
            this.victim = victim;
            this.killer = killer;
            this.victimName = victimName;
            this.location = location;
            this.deathTime = deathTime;
            this.marker = marker;
            this.corpseData = corpseData;
            this.reported = false;
        }
    }
}
