package me.spwtyz.murder.leaderboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import me.spwtyz.murder.Main;
import me.spwtyz.murder.Utils;
import me.spwtyz.murder.ranked.RankedManager;

public class HologramLeaderboardManager implements Listener {

    private final Main plugin;
    private final String ROOT = "leaderboards";
    private final String MENU_TITLE = "§8Leaderboards";
    private boolean updateQueued = false;
    private final Map<LeaderboardType, List<ArmorStand>> spawned = new HashMap<LeaderboardType, List<ArmorStand>>();

    public HologramLeaderboardManager(Main plugin) {
        this.plugin = plugin;
    }

    public void startAutoUpdate() {
        int seconds = plugin.getConfig().getInt("leaderboard.update-seconds", 300);
        if (seconds < 30) seconds = 30;
        new BukkitRunnable() {
            @Override
            public void run() {
                updateAll();
            }
        }.runTaskTimer(plugin, 20L * seconds, 20L * seconds);
    }

    public void updateAllSoon() {
        if (updateQueued) return;
        updateQueued = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                updateQueued = false;
                updateAll();
            }
        }.runTaskLater(plugin, 40L);
    }

    public void openMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, MENU_TITLE);
        int[] slots = new int[] {10, 11, 12, 13, 14, 15, 16, 20, 22};
        LeaderboardType[] types = LeaderboardType.values();
        for (int i = 0; i < types.length && i < slots.length; i++) {
            LeaderboardType type = types[i];
            ItemStack item = new ItemStack(type.getIcon());
            if (type == LeaderboardType.MORTES) item = new ItemStack(Material.SKULL_ITEM, 1, (short) 1);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§e" + type.getDisplay());
            List<String> lore = new ArrayList<String>();
            lore.add("§7Clique para setar esse holograma");
            lore.add("§7no bloco que você está olhando.");
            lore.add("");
            lore.add("§a/m leaderboard set " + type.getId());
            lore.add("§c/m leaderboard remove " + type.getId());
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(slots[i], item);
        }
        p.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (e.getInventory() == null || !e.getInventory().getTitle().equals(MENU_TITLE)) return;
        e.setCancelled(true);
        Player p = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return;
        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        LeaderboardType selected = null;
        for (LeaderboardType type : LeaderboardType.values()) {
            if (type.getDisplay().equalsIgnoreCase(name)) selected = type;
        }
        if (selected == null) return;
        p.closeInventory();
        setLeaderboard(p, selected);
    }

    public void setLeaderboard(Player p, LeaderboardType type) {
        if (p == null || type == null) return;
        Location loc = getLookLocation(p);
        if (loc == null) {
            p.sendMessage("§cOlhe para um bloco dentro de 50 blocos para setar o leaderboard.");
            return;
        }
        String path = ROOT + "." + type.getId() + ".";
        plugin.data.getConfig().set(path + "world", loc.getWorld().getName());
        plugin.data.getConfig().set(path + "x", loc.getX());
        plugin.data.getConfig().set(path + "y", loc.getY());
        plugin.data.getConfig().set(path + "z", loc.getZ());
        plugin.data.getConfig().set(path + "yaw", loc.getYaw());
        plugin.data.getConfig().set(path + "pitch", loc.getPitch());
        plugin.data.save();
        spawn(type);
        p.sendMessage("§aLeaderboard §e" + type.getDisplay() + " §asetado onde você estava olhando.");
    }

    public void remove(Player p, LeaderboardType type) {
        if (type == null) {
            p.sendMessage("§cTipo inválido.");
            return;
        }
        removeEntities(type);
        plugin.data.getConfig().set(ROOT + "." + type.getId(), null);
        plugin.data.save();
        p.sendMessage("§aLeaderboard §e" + type.getDisplay() + " §aremovido.");
    }

    public void spawnSaved() {
        for (LeaderboardType type : LeaderboardType.values()) {
            spawn(type);
        }
    }

    public void updateAll() {
        spawnSaved();
    }

    public void spawn(LeaderboardType type) {
        Location loc = getSavedLocation(type);
        if (loc == null || loc.getWorld() == null) return;
        removeEntities(type);
        List<TopEntry> top = getTop(type, plugin.getConfig().getInt("leaderboard.top-limit", 10));
        List<String> lines = new ArrayList<String>();
        lines.add(type.getColoredTitle());
        lines.add("§7Atualizado automaticamente");
        lines.add("§8--------------------");
        if (top.isEmpty()) {
            lines.add("§cNenhum jogador salvo ainda.");
        } else {
            int pos = 1;
            for (TopEntry e : top) {
                lines.add(getPositionColor(pos) + "#" + pos + " §f" + e.name + " §7- §e" + e.value + " §7" + type.getValueName());
                pos++;
            }
        }
        lines.add("§8--------------------");

        double lineSpace = plugin.getConfig().getDouble("leaderboard.line-space", 0.27D);
        List<ArmorStand> created = new ArrayList<ArmorStand>();
        for (int i = 0; i < lines.size(); i++) {
            Location lineLoc = loc.clone().add(0, -(i * lineSpace), 0);
            ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(lineLoc, EntityType.ARMOR_STAND);
            stand.setGravity(false);
            stand.setVisible(false);
            stand.setCanPickupItems(false);
            stand.setCustomNameVisible(true);
            stand.setCustomName(lines.get(i));
            stand.setRemoveWhenFarAway(false);
            created.add(stand);
        }
        spawned.put(type, created);
    }

    private Location getSavedLocation(LeaderboardType type) {
        String path = ROOT + "." + type.getId() + ".";
        if (!plugin.data.getConfig().contains(path + "world")) return null;
        World world = Bukkit.getWorld(plugin.data.getConfig().getString(path + "world"));
        if (world == null) return null;
        return new Location(world,
                plugin.data.getConfig().getDouble(path + "x"),
                plugin.data.getConfig().getDouble(path + "y"),
                plugin.data.getConfig().getDouble(path + "z"),
                (float) plugin.data.getConfig().getDouble(path + "yaw"),
                (float) plugin.data.getConfig().getDouble(path + "pitch"));
    }

    private Location getLookLocation(Player p) {
        try {
            Block block = p.getTargetBlock((HashSet<Byte>) null, 50);
            if (block == null || block.getType() == Material.AIR) return p.getLocation().add(0, 2.2, 0);
            return block.getLocation().add(0.5, 2.2, 0.5);
        } catch (Exception ex) {
            return p.getLocation().add(0, 2.2, 0);
        }
    }

    private void removeEntities(LeaderboardType type) {
        // Remove somente as linhas desse leaderboard.
        // A versão anterior removia qualquer ArmorStand em um raio de 4 blocos,
        // então um holograma novo podia apagar outros leaderboards próximos.
        List<ArmorStand> previous = spawned.remove(type);
        if (previous != null) {
            for (ArmorStand stand : previous) {
                if (stand != null && !stand.isDead()) stand.remove();
            }
        }

        String marker = getMarker(type);
        Location saved = getSavedLocation(type);
        double lineSpace = plugin.getConfig().getDouble("leaderboard.line-space", 0.27D);
        int maxLines = plugin.getConfig().getInt("leaderboard.top-limit", 10) + 5;
        double maxDown = Math.max(2.0D, maxLines * lineSpace + 0.5D);

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof ArmorStand)) continue;
                ArmorStand stand = (ArmorStand) entity;
                if (stand.getCustomName() == null || !stand.isCustomNameVisible()) continue;
                String plain = ChatColor.stripColor(stand.getCustomName());

                if (plain.contains(marker)) {
                    stand.remove();
                    continue;
                }

                // Fallback para limpar linhas antigas somente se estiverem praticamente
                // na mesma coluna desse leaderboard. Não remove hologramas próximos.
                if (saved != null && stand.getWorld().equals(saved.getWorld())) {
                    Location sl = stand.getLocation();
                    double dx = sl.getX() - saved.getX();
                    double dz = sl.getZ() - saved.getZ();
                    double dy = saved.getY() - sl.getY();
                    boolean sameColumn = (dx * dx + dz * dz) <= 0.64D;
                    boolean sameVerticalStack = dy >= -0.30D && dy <= maxDown;
                    if (sameColumn && sameVerticalStack) stand.remove();
                }
            }
        }
    }

    private String getMarker(LeaderboardType type) {
        return "TOP " + type.getDisplay().toUpperCase();
    }

    private ChatColor getPositionColor(int pos) {
        if (pos == 1) return ChatColor.GOLD;
        if (pos == 2) return ChatColor.GRAY;
        if (pos == 3) return ChatColor.YELLOW;
        return ChatColor.WHITE;
    }

    public List<TopEntry> getTop(LeaderboardType type, int limit) {
        if (type.name().startsWith("RANKED")) return getRankedTop(type, limit);
        List<TopEntry> list = new ArrayList<TopEntry>();
        if (type == LeaderboardType.NIVEIS) {
            ConfigurationSection sec = plugin.data.getConfig().getConfigurationSection("Levels");
            if (sec != null) {
                for (String uuid : sec.getKeys(false)) {
                    String base = "Levels." + uuid + ".";
                    String name = plugin.data.getConfig().getString(base + "name", uuid.substring(0, Math.min(8, uuid.length())));
                    int level = plugin.data.getConfig().getInt(base + "level", 1);
                    int total = plugin.data.getConfig().getInt(base + "total", 0);
                    list.add(new TopEntry(name, level, total));
                }
            }
        } else {
            String root = getDataRoot(type);
            String statKey = getDataKey(type);
            ConfigurationSection sec = plugin.data.getConfig().getConfigurationSection(root);
            if (sec != null) {
                for (String uuid : sec.getKeys(false)) {
                    String name = getKnownName(uuid);
                    int value = plugin.data.getConfig().getInt(root + "." + uuid + "." + statKey, 0);
                    if (type == LeaderboardType.PARTIDAS_JOGADAS) {
                        int wins = plugin.data.getConfig().getInt("Wins." + uuid + ".win", 0);
                        int losses = plugin.data.getConfig().getInt("Loses." + uuid + ".lose", 0);
                        value = wins + losses;
                    }
                    list.add(new TopEntry(name, value, value));
                }
            }
        }
        sortAndLimit(list, limit);
        return list;
    }

    private List<TopEntry> getRankedTop(LeaderboardType type, int limit) {
        List<TopEntry> list = new ArrayList<TopEntry>();
        if (plugin.rankedManager == null) return list;
        List<RankedManager.RankedEntry> ranked = plugin.rankedManager.getTop(0);
        for (RankedManager.RankedEntry e : ranked) {
            int value = e.getRp();
            if (type == LeaderboardType.RANKED_ABATES) value = e.getKills();
            if (type == LeaderboardType.RANKED_PARTIDAS) value = e.getGamesPlayed();
            if (type == LeaderboardType.RANKED_PERDIDAS) value = e.getLosses();
            list.add(new TopEntry(e.getName(), value, e.getRp()));
        }
        sortAndLimit(list, limit);
        return list;
    }

    private void sortAndLimit(List<TopEntry> list, int limit) {
        Collections.sort(list, new Comparator<TopEntry>() {
            @Override
            public int compare(TopEntry a, TopEntry b) {
                if (a.value != b.value) return b.value - a.value;
                return b.tie - a.tie;
            }
        });
        if (limit > 0 && list.size() > limit) {
            while (list.size() > limit) list.remove(list.size() - 1);
        }
    }

    private String getDataRoot(LeaderboardType type) {
        if (type == LeaderboardType.ABATES) return "Kills";
        if (type == LeaderboardType.MORTES) return "Deaths";
        if (type == LeaderboardType.PARTIDAS_PERDIDAS) return "Loses";
        return "Wins";
    }

    private String getDataKey(LeaderboardType type) {
        if (type == LeaderboardType.ABATES) return "kill";
        if (type == LeaderboardType.MORTES) return "death";
        if (type == LeaderboardType.PARTIDAS_PERDIDAS) return "lose";
        return "win";
    }

    private String getKnownName(String uuid) {
        try {
            Player online = Bukkit.getPlayer(UUID.fromString(uuid));
            if (online != null) return online.getName();
        } catch (Exception ignored) {}
        String name = plugin.data.getConfig().getString("Names." + uuid);
        if (name != null) return name;
        return uuid.substring(0, Math.min(8, uuid.length()));
    }

    public static class TopEntry {
        public final String name;
        public final int value;
        public final int tie;
        public TopEntry(String name, int value, int tie) {
            this.name = name;
            this.value = value;
            this.tie = tie;
        }
    }
}
