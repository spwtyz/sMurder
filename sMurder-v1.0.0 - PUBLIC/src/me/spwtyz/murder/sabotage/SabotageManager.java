package me.spwtyz.murder.sabotage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.spwtyz.murder.Arena;
import me.spwtyz.murder.Arenas;
import me.spwtyz.murder.GameState;
import me.spwtyz.murder.GameModeType;
import me.spwtyz.murder.Main;
import me.spwtyz.murder.PlayerType;
import me.spwtyz.murder.rooms.Room;
import me.spwtyz.murder.rooms.RoomModifier;

public class SabotageManager implements Listener {

    private final Main plugin;
    private final Random random = new Random();
    private final Map<UUID, Map<SabotageType, Long>> cooldowns = new HashMap<>();

    public SabotageManager(Main plugin) {
        this.plugin = plugin;
    }

    public ItemStack createSabotageItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_PURPLE + "Sabotagem " + ChatColor.GRAY + "(Clique)");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Abra o menu de sabotagens.",
                ChatColor.GRAY + "Disponivel apenas para o Murder.",
                "",
                ChatColor.YELLOW + "Clique para abrir."
        ));
        item.setItemMeta(meta);
        return item;
    }

    public boolean isSabotageItem(ItemStack item) {
        return item != null
                && item.hasItemMeta()
                && item.getItemMeta().hasDisplayName()
                && ChatColor.stripColor(item.getItemMeta().getDisplayName()).toLowerCase().contains("sabotagem");
    }

    public boolean isEnabled(Arena arena) {
        if (arena == null || plugin.roomManager == null) return false;
        if (arena.getGameMode() == GameModeType.SABOTAGE) return true;
        Room room = plugin.roomManager.getRoomByArena(arena);
        return room != null && room.hasModifier(RoomModifier.SABOTAGE);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getItemInHand();
        if (!isSabotageItem(item)) return;

        event.setCancelled(true);

        Arena arena = Arenas.getArena(player);
        if (arena == null || arena.getState() != GameState.INGAME) {
            player.sendMessage(ChatColor.RED + "Voce so pode usar sabotagens durante a partida.");
            return;
        }

        if (!isEnabled(arena)) {
            player.sendMessage(ChatColor.RED + "Sabotagens nao estao ativadas nesta sala.");
            return;
        }

        if (arena.getType(player) != PlayerType.Murderer) {
            player.sendMessage(ChatColor.RED + "Apenas o Murder pode usar sabotagens.");
            return;
        }

        openMenu(player, arena);
    }

    public void openMenu(Player player, Arena arena) {
        Inventory inv = Bukkit.createInventory(null, 27, "Sabotagens");

        int[] slots = {10, 11, 13, 15, 16};
        SabotageType[] values = SabotageType.values();

        for (int i = 0; i < values.length && i < slots.length; i++) {
            SabotageType type = values[i];
            boolean cooling = isCooldown(player, type);
            long left = getCooldownLeft(player, type);

            ItemStack item = new ItemStack(type.getMaterial());
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName((cooling ? ChatColor.RED : ChatColor.GREEN) + type.getDisplayName());
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + getDescription(type),
                    "",
                    ChatColor.GRAY + "Cooldown: " + ChatColor.WHITE + type.getCooldownSeconds() + "s",
                    ChatColor.GRAY + "Status: " + (cooling ? ChatColor.RED + "Aguarde " + left + "s" : ChatColor.GREEN + "Pronto"),
                    "",
                    ChatColor.YELLOW + "Clique para usar."
            ));
            item.setItemMeta(meta);
            inv.setItem(slots[i], item);
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getView().getTitle().equals("Sabotagens")) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;

        Arena arena = Arenas.getArena(player);
        if (arena == null || arena.getState() != GameState.INGAME || !isEnabled(arena)) {
            player.closeInventory();
            player.sendMessage(ChatColor.RED + "Sabotagens indisponiveis agora.");
            return;
        }

        if (arena.getType(player) != PlayerType.Murderer) {
            player.closeInventory();
            player.sendMessage(ChatColor.RED + "Apenas o Murder pode usar sabotagens.");
            return;
        }

        String clicked = ChatColor.stripColor(item.getItemMeta().getDisplayName()).toLowerCase();
        for (SabotageType type : SabotageType.values()) {
            if (clicked.contains(type.getDisplayName().toLowerCase())) {
                use(player, arena, type);
                return;
            }
        }
    }

    public void use(Player murder, Arena arena, SabotageType type) {
        if (isCooldown(murder, type)) {
            murder.sendMessage(ChatColor.RED + "Aguarde " + getCooldownLeft(murder, type) + "s para usar essa sabotagem novamente.");
            return;
        }

        setCooldown(murder, type);
        murder.closeInventory();

        if (type == SabotageType.LIGHTS) {
            int seconds = plugin.getConfig().getInt("Sabotage.LightsDuration", 20);
            for (Player p : getTargets(arena)) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * seconds, 0));
                p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 20, 0));
                p.playSound(p.getLocation(), Sound.AMBIENCE_THUNDER, 0.8f, 1f);
            }
            broadcast(arena, ChatColor.DARK_GRAY + "§lLIGHTS! " + ChatColor.GRAY + "As luzes foram sabotadas.");
            if (plugin.sabotageTaskManager != null) plugin.sabotageTaskManager.activateSabotageRepair(arena, SabotageTaskManager.TaskType.CALIBRATE_DISTRIBUTOR, "LIGHTS");
        } else if (type == SabotageType.REACTOR) {
            int seconds = plugin.getConfig().getInt("Sabotage.ReactorDuration", 25);
            for (Player p : getTargets(arena)) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 20 * seconds, 0));
                p.playSound(p.getLocation(), Sound.NOTE_BASS, 1f, 0.5f);
                p.sendMessage(ChatColor.RED + "§lREACTOR! §7Corram para resolver a sabotagem.");
            }
            broadcast(arena, ChatColor.RED + "§lREACTOR SABOTADO! " + ChatColor.GRAY + "Completem tasks rapidamente.");
            if (plugin.sabotageTaskManager != null) plugin.sabotageTaskManager.activateSabotageRepair(arena, SabotageTaskManager.TaskType.START_REACTOR, "REACTOR");
        } else if (type == SabotageType.O2) {
            int seconds = plugin.getConfig().getInt("Sabotage.O2Duration", 18);
            for (Player p : getTargets(arena)) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 20 * seconds, 0));
                p.playSound(p.getLocation(), Sound.FIZZ, 0.8f, 0.6f);
            }
            broadcast(arena, ChatColor.AQUA + "§lO2 SABOTADO! " + ChatColor.GRAY + "O oxigênio está falhando.");
            if (plugin.sabotageTaskManager != null) plugin.sabotageTaskManager.activateSabotageRepair(arena, SabotageTaskManager.TaskType.CLEAN_O2, "O2");
        } else if (type == SabotageType.COMMUNICATIONS) {
            int seconds = plugin.getConfig().getInt("Sabotage.CommunicationsDuration", 20);
            for (Player p : getTargets(arena)) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 20 * seconds, 1));
                p.playSound(p.getLocation(), Sound.NOTE_PLING, 1f, 0.6f);
                p.sendMessage(ChatColor.DARK_PURPLE + "§lCOMMUNICATIONS " + ChatColor.GRAY + "Tasks e informações ficaram confusas.");
            }
            broadcast(arena, ChatColor.DARK_PURPLE + "§lCOMMUNICATIONS SABOTADO!");
            if (plugin.sabotageTaskManager != null) plugin.sabotageTaskManager.activateSabotageRepair(arena, SabotageTaskManager.TaskType.UPLOAD_DATA, "COMMUNICATIONS");
        } else if (type == SabotageType.DOORS) {
            int seconds = plugin.getConfig().getInt("Sabotage.DoorsDuration", 15);
            for (Player p : getTargets(arena)) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * seconds, 2));
                p.playSound(p.getLocation(), Sound.DOOR_CLOSE, 1f, 0.7f);
            }
            broadcast(arena, ChatColor.DARK_PURPLE + "§lDOORS! " + ChatColor.GRAY + "Portas trancadas temporariamente.");
        } else if (type == SabotageType.ELECTRICAL) {
            int seconds = plugin.getConfig().getInt("Sabotage.ElectricalDuration", 12);
            for (Player p : getTargets(arena)) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * seconds, 0));
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 5, 0));
                p.playSound(p.getLocation(), Sound.FIZZ, 1f, 0.7f);
                p.getWorld().playEffect(p.getLocation(), Effect.SMOKE, 1);
            }
            broadcast(arena, ChatColor.YELLOW + "§lELECTRICAL! " + ChatColor.GRAY + "Falha elétrica no mapa.");
            if (plugin.sabotageTaskManager != null) plugin.sabotageTaskManager.activateSabotageRepair(arena, SabotageTaskManager.TaskType.CALIBRATE_DISTRIBUTOR, "ELECTRICAL");
        }
    }

    private List<Player> getTargets(Arena arena) {
        List<Player> targets = new ArrayList<>();
        for (Player p : arena.getPlayers()) {
            if (p == null || !p.isOnline()) continue;
            if (arena.specs.contains(p)) continue;
            if (arena.getType(p) == PlayerType.Murderer) continue;
            targets.add(p);
        }
        return targets;
    }

    private void randomTeleport(Arena arena, Player murder) {
        List<Player> targets = getTargets(arena);
        if (targets.size() < 2) return;

        List<Location> locations = new ArrayList<>();
        for (Player p : targets) locations.add(p.getLocation().clone());

        for (Player p : targets) {
            Location loc = locations.get(random.nextInt(locations.size()));
            p.teleport(loc);
            p.playSound(p.getLocation(), Sound.ENDERMAN_TELEPORT, 1f, 1f);
        }
    }

    private void broadcast(Arena arena, String msg) {
        for (Player p : arena.getPlayers()) {
            if (p != null && p.isOnline()) p.sendMessage(msg);
        }
    }

    private String getDescription(SabotageType type) {
        if (type == SabotageType.LIGHTS) return "Reduz a visão dos inocentes.";
        if (type == SabotageType.REACTOR) return "Cria pressão e confusão no mapa.";
        if (type == SabotageType.O2) return "Aplica dano leve/pressão por oxigênio.";
        if (type == SabotageType.COMMUNICATIONS) return "Atrasa e confunde os jogadores.";
        if (type == SabotageType.DOORS) return "Trava/segura jogadores por alguns segundos.";
        if (type == SabotageType.ELECTRICAL) return "Cegueira curta com falha elétrica.";
        return "Sabotagem especial.";
    }

    private boolean isCooldown(Player player, SabotageType type) {
        return getCooldownLeft(player, type) > 0;
    }

    private long getCooldownLeft(Player player, SabotageType type) {
        Map<SabotageType, Long> map = cooldowns.get(player.getUniqueId());
        if (map == null) return 0;
        Long end = map.get(type);
        if (end == null) return 0;
        long left = (end - System.currentTimeMillis()) / 1000L;
        return Math.max(0, left);
    }

    private void setCooldown(Player player, SabotageType type) {
        Map<SabotageType, Long> map = cooldowns.get(player.getUniqueId());
        if (map == null) {
            map = new HashMap<>();
            cooldowns.put(player.getUniqueId(), map);
        }
        map.put(type, System.currentTimeMillis() + (type.getCooldownSeconds() * 1000L));
    }
}
