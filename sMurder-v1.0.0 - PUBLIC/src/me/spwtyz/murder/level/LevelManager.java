package me.spwtyz.murder.level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import me.spwtyz.murder.Main;

public class LevelManager {

    private final Main plugin;

    public LevelManager(Main plugin) {
        this.plugin = plugin;
    }

    public int getLevel(Player p) {
        return plugin.data.getConfig().getInt("Levels." + p.getUniqueId() + ".level", 1);
    }

    public int getXP(Player p) {
        return plugin.data.getConfig().getInt("Levels." + p.getUniqueId() + ".xp", 0);
    }

    public int getTotalXP(Player p) {
        return plugin.data.getConfig().getInt("Levels." + p.getUniqueId() + ".total", 0);
    }

    public int getXPToNext(int level) {
        if (level < 1) level = 1;
        return 100 + (level * 35) + (level * level * 8);
    }

    public void ensure(Player p) {
        if (p == null) return;
        String path = "Levels." + p.getUniqueId() + ".";
        if (!plugin.data.getConfig().contains(path + "level")) plugin.data.getConfig().set(path + "level", 1);
        if (!plugin.data.getConfig().contains(path + "xp")) plugin.data.getConfig().set(path + "xp", 0);
        if (!plugin.data.getConfig().contains(path + "total")) plugin.data.getConfig().set(path + "total", 0);
        plugin.data.getConfig().set(path + "name", p.getName());
        plugin.data.save();
        syncXPBar(p);
    }

    public void addXP(Player p, int amount, String reason) {
        if (p == null || amount <= 0) return;
        ensure(p);

        double multiplier = 1.0D;
        if (p.hasPermission("murder.xp.double")) multiplier += 1.0D;
        if (p.hasPermission("murder.xp.1_5")) multiplier += 0.5D;

        int finalAmount = Math.max(1, (int) Math.round(amount * multiplier));
        String path = "Levels." + p.getUniqueId() + ".";
        int level = plugin.data.getConfig().getInt(path + "level", 1);
        int xp = plugin.data.getConfig().getInt(path + "xp", 0) + finalAmount;
        int total = plugin.data.getConfig().getInt(path + "total", 0) + finalAmount;

        boolean leveled = false;
        while (xp >= getXPToNext(level)) {
            xp -= getXPToNext(level);
            level++;
            leveled = true;
        }

        plugin.data.getConfig().set(path + "name", p.getName());
        plugin.data.getConfig().set(path + "level", level);
        plugin.data.getConfig().set(path + "xp", xp);
        plugin.data.getConfig().set(path + "total", total);
        plugin.data.save();

        saveSQL(p.getUniqueId().toString(), p.getName(), level, xp, total);
        syncXPBar(p);

        if (reason != null && !reason.trim().isEmpty()) {
            p.sendMessage(ChatColor.AQUA + "+" + finalAmount + " XP " + ChatColor.GRAY + "(" + reason + ")");
        }

        if (leveled) {
            p.sendMessage(ChatColor.GOLD + "✦ Level Up! " + ChatColor.YELLOW + "Agora você está no nível " + ChatColor.WHITE + level + ChatColor.YELLOW + "!");
            p.playSound(p.getLocation(), org.bukkit.Sound.LEVEL_UP, 1f, 1f);
            updateLeaderboardNPCs();
        }
    }

    public void syncXPBar(Player p) {
        if (p == null || !p.isOnline()) return;
        int level = getLevel(p);
        int xp = getXP(p);
        int needed = getXPToNext(level);
        float progress = needed <= 0 ? 0F : Math.max(0F, Math.min(1F, (float) xp / (float) needed));
        p.setLevel(level);
        p.setExp(progress);
    }

    private void saveSQL(String uuid, String name, int level, int xp, int total) {
        if (!plugin.getConfig().getBoolean("mysql")) return;
        if (plugin.sql == null || !plugin.sql.isConnected()) return;
        String safeName = name == null ? "Unknown" : name.replace("'", "");
        plugin.sql.update("INSERT INTO MurderLevelData (uuid, name, level, xp, total_xp) VALUES ('" + uuid + "', '" + safeName + "', " + level + ", " + xp + ", " + total + ") ON DUPLICATE KEY UPDATE name='" + safeName + "', level=" + level + ", xp=" + xp + ", total_xp=" + total);
    }

    public List<TopEntry> getTop(int limit) {
        List<TopEntry> list = new ArrayList<TopEntry>();

        if (plugin.data.getConfig().isConfigurationSection("Levels")) {
            for (String key : plugin.data.getConfig().getConfigurationSection("Levels").getKeys(false)) {
                String path = "Levels." + key + ".";
                String name = plugin.data.getConfig().getString(path + "name", "Unknown");
                int level = plugin.data.getConfig().getInt(path + "level", 1);
                int xp = plugin.data.getConfig().getInt(path + "xp", 0);
                int total = plugin.data.getConfig().getInt(path + "total", 0);
                list.add(new TopEntry(name, level, xp, total));
            }
        }

        Collections.sort(list, new Comparator<TopEntry>() {
            @Override
            public int compare(TopEntry a, TopEntry b) {
                return Integer.compare(b.totalXP, a.totalXP);
            }
        });

        if (list.size() > limit) return new ArrayList<TopEntry>(list.subList(0, limit));
        return list;
    }

    public void sendStats(Player p) {
        ensure(p);
        p.sendMessage(ChatColor.DARK_GRAY + "§m----------------------");
        p.sendMessage(ChatColor.GOLD + "Seu nível: " + ChatColor.WHITE + getLevel(p));
        p.sendMessage(ChatColor.AQUA + "XP: " + ChatColor.WHITE + getXP(p) + ChatColor.GRAY + "/" + ChatColor.WHITE + getXPToNext(getLevel(p)));
        p.sendMessage(ChatColor.YELLOW + "XP total: " + ChatColor.WHITE + getTotalXP(p));
        p.sendMessage(ChatColor.DARK_GRAY + "§m----------------------");
    }

    public void sendTop(Player p) {
        List<TopEntry> top = getTop(10);
        p.sendMessage(ChatColor.DARK_GRAY + "§m------" + ChatColor.GOLD + " Top Levels " + ChatColor.DARK_GRAY + "§m------");
        if (top.isEmpty()) {
            p.sendMessage(ChatColor.GRAY + "Nenhum jogador no ranking ainda.");
            return;
        }
        int pos = 1;
        for (TopEntry e : top) {
            p.sendMessage(ChatColor.YELLOW + "#" + pos + " " + ChatColor.WHITE + e.name + ChatColor.GRAY + " - Level " + ChatColor.AQUA + e.level + ChatColor.GRAY + " (" + e.totalXP + " XP)");
            pos++;
        }
    }

    /**
     * Sistema antigo de NPC de níveis removido.
     * O /m leaderboard novo continua sendo usado para hologramas/leaderboards.
     */
    public void setLeaderboard(Location loc) {
        removeLeaderboardNPCs();
        if (plugin.data != null) {
            plugin.data.getConfig().set("level-leaderboard", null);
            plugin.data.save();
        }
    }

    public void spawnSavedLeaderboard() {
        removeLeaderboardNPCs();
    }

    public void updateLeaderboardNPCs() {
        // removido para não respawnar NPC antigo de níveis
    }

    private void removeLeaderboardNPCs() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof ArmorStand)) continue;
                ArmorStand stand = (ArmorStand) entity;
                if (stand.getCustomName() == null) continue;
                String name = ChatColor.stripColor(stand.getCustomName());
                if (name != null && name.startsWith("Murder Level Top")) {
                    stand.remove();
                }
            }
        }
    }

    public static class TopEntry {
        public final String name;
        public final int level;
        public final int xp;
        public final int totalXP;
        public TopEntry(String name, int level, int xp, int totalXP) {
            this.name = name;
            this.level = level;
            this.xp = xp;
            this.totalXP = totalXP;
        }
    }
}
