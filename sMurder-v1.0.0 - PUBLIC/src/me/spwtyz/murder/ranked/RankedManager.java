package me.spwtyz.murder.ranked;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import me.spwtyz.murder.Main;

public class RankedManager {


    public static class RankedEntry {
        private final String uuid;
        private final String name;
        private final int rp;
        private final String rank;
        private final int wins;
        private final int losses;
        private final int kills;
        private final int gamesPlayed;

        public RankedEntry(String uuid, String name, int rp, String rank, int wins, int losses, int kills, int gamesPlayed) {
            this.uuid = uuid;
            this.name = name;
            this.rp = rp;
            this.rank = rank;
            this.wins = wins;
            this.losses = losses;
            this.kills = kills;
            this.gamesPlayed = gamesPlayed;
        }

        public String getUuid() { return uuid; }
        public String getName() { return name; }
        public int getRp() { return rp; }
        public String getRank() { return rank; }
        public int getWins() { return wins; }
        public int getLosses() { return losses; }
        public int getKills() { return kills; }
        public int getGamesPlayed() { return gamesPlayed; }
    }

    private final Main plugin;
    private final File file;
    private YamlConfiguration data;

    public RankedManager(Main plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "ranked.yml");
        load();
    }

    public void load() {
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        data = YamlConfiguration.loadConfiguration(file);
        if (!data.contains("default-rp")) data.set("default-rp", 1000);
        save();
    }

    public void save() {
        try {
            if (data != null) data.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String path(Player p) {
        UUID uuid = p.getUniqueId();
        return "players." + uuid.toString();
    }

    public int getDefaultRP() {
        return data.getInt("default-rp", 1000);
    }

    public int getRP(Player p) {
        if (p == null) return getDefaultRP();
        String path = path(p) + ".rp";
        if (!data.contains(path)) {
            data.set(path, getDefaultRP());
            save();
        }
        return data.getInt(path, getDefaultRP());
    }

    public void setRP(Player p, int amount) {
        if (p == null) return;
        if (amount < 0) amount = 0;
        data.set(path(p) + ".name", p.getName());
        data.set(path(p) + ".rp", amount);
        data.set(path(p) + ".rank", getRankName(amount));
        save();
        if (plugin.leaderboardManager != null) plugin.leaderboardManager.updateAllSoon();
    }

    public void addRP(Player p, int amount, String reason) {
        if (p == null) return;
        int before = getRP(p);
        int after = before + amount;
        setRP(p, after);

        if (amount > 0) data.set(path(p) + ".positive-rp", data.getInt(path(p) + ".positive-rp", 0) + amount);
        if (amount < 0) data.set(path(p) + ".negative-rp", data.getInt(path(p) + ".negative-rp", 0) + Math.abs(amount));
        save();

        String sign = amount >= 0 ? "+" : "";
        p.sendMessage(ChatColor.GOLD + "Ranked " + ChatColor.GRAY + "> " +
                ChatColor.YELLOW + sign + amount + " RP " + ChatColor.GRAY + "(" + reason + ")");
        p.sendMessage(ChatColor.GOLD + "Ranked " + ChatColor.GRAY + "> " +
                ChatColor.WHITE + "RP atual: " + ChatColor.AQUA + after + ChatColor.GRAY + " | " + getRankColor(after) + getRankName(after));
    }


    public void recordMatch(Player p, boolean win, int kills, int rpChange, String reason) {
        if (p == null) return;

        String base = path(p);
        int before = getRP(p);
        int after = before + rpChange;
        if (after < 0) after = 0;

        data.set(base + ".name", p.getName());
        data.set(base + ".rp", after);
        data.set(base + ".rank", getRankName(after));
        data.set(base + ".games-played", data.getInt(base + ".games-played", 0) + 1);
        data.set(base + ".kills", data.getInt(base + ".kills", 0) + Math.max(0, kills));

        if (win) {
            data.set(base + ".wins", data.getInt(base + ".wins", 0) + 1);
            data.set(base + ".win-streak", data.getInt(base + ".win-streak", 0) + 1);
            int streak = data.getInt(base + ".win-streak", 0);
            if (streak > data.getInt(base + ".best-streak", 0)) data.set(base + ".best-streak", streak);
        } else {
            data.set(base + ".losses", data.getInt(base + ".losses", 0) + 1);
            data.set(base + ".win-streak", 0);
        }

        if (rpChange > 0) data.set(base + ".positive-rp", data.getInt(base + ".positive-rp", 0) + rpChange);
        if (rpChange < 0) data.set(base + ".negative-rp", data.getInt(base + ".negative-rp", 0) + Math.abs(rpChange));
        if (after > data.getInt(base + ".best-rp", getDefaultRP())) data.set(base + ".best-rp", after);

        save();
        if (plugin.leaderboardManager != null) plugin.leaderboardManager.updateAllSoon();

        String sign = rpChange >= 0 ? "+" : "";
        p.sendMessage(ChatColor.GOLD + "Ranked V3 " + ChatColor.GRAY + "> "
                + (win ? ChatColor.GREEN + "Vitória" : ChatColor.RED + "Derrota")
                + ChatColor.GRAY + " | " + ChatColor.YELLOW + sign + rpChange + " RP"
                + ChatColor.GRAY + " | Kills: " + ChatColor.RED + Math.max(0, kills));
        p.sendMessage(ChatColor.GOLD + "Ranked V3 " + ChatColor.GRAY + "> "
                + ChatColor.WHITE + "RP atual: " + ChatColor.AQUA + after
                + ChatColor.GRAY + " | " + getRankColor(after) + getRankName(after));
    }

    public int getGamesPlayed(Player p) {
        if (p == null) return 0;
        return data.getInt(path(p) + ".games-played", 0);
    }

    public int getBestRP(Player p) {
        if (p == null) return getDefaultRP();
        return data.getInt(path(p) + ".best-rp", getRP(p));
    }

    public int getWinStreak(Player p) {
        if (p == null) return 0;
        return data.getInt(path(p) + ".win-streak", 0);
    }

    public void addKill(Player p) {
        if (p == null) return;
        data.set(path(p) + ".kills", data.getInt(path(p) + ".kills", 0) + 1);
        save();
    }

    public void addWin(Player p) {
        if (p == null) return;
        data.set(path(p) + ".wins", data.getInt(path(p) + ".wins", 0) + 1);
        save();
    }

    public void addLoss(Player p) {
        if (p == null) return;
        data.set(path(p) + ".losses", data.getInt(path(p) + ".losses", 0) + 1);
        save();
    }

    public int getWins(Player p) {
        if (p == null) return 0;
        return data.getInt(path(p) + ".wins", 0);
    }

    public int getKills(Player p) {
        if (p == null) return 0;
        return data.getInt(path(p) + ".kills", 0);
    }

    public int getLosses(Player p) {
        if (p == null) return 0;
        return data.getInt(path(p) + ".losses", 0);
    }

    public String getRankName(Player p) {
        return getRankName(getRP(p));
    }

    public String getRankColor(Player p) {
        return getRankColor(getRP(p));
    }

    public String getRankName(int rp) {
        if (rp >= 3000) return "Lendário";
        if (rp >= 2500) return "Mestre";
        if (rp >= 2000) return "Diamante";
        if (rp >= 1500) return "Ouro";
        if (rp >= 1000) return "Prata";
        return "Bronze";
    }

    public String getRankColor(int rp) {
        if (rp >= 3000) return ChatColor.DARK_RED.toString() + ChatColor.BOLD;
        if (rp >= 2500) return ChatColor.LIGHT_PURPLE.toString();
        if (rp >= 2000) return ChatColor.AQUA.toString();
        if (rp >= 1500) return ChatColor.GOLD.toString();
        if (rp >= 1000) return ChatColor.GRAY.toString();
        return ChatColor.DARK_GREEN.toString();
    }

    public String getRankDisplay(Player p) {
        int rp = getRP(p);
        return getRankColor(rp) + getRankName(rp) + ChatColor.GRAY + " (" + rp + " RP)";
    }


    public void reset(Player p) {
        if (p == null) return;
        String base = path(p);
        data.set(base + ".name", p.getName());
        data.set(base + ".rp", getDefaultRP());
        data.set(base + ".rank", getRankName(getDefaultRP()));
        data.set(base + ".games-played", 0);
        data.set(base + ".kills", 0);
        data.set(base + ".wins", 0);
        data.set(base + ".losses", 0);
        data.set(base + ".win-streak", 0);
        data.set(base + ".best-streak", 0);
        data.set(base + ".positive-rp", 0);
        data.set(base + ".negative-rp", 0);
        data.set(base + ".best-rp", getDefaultRP());
        save();
        if (plugin.leaderboardManager != null) plugin.leaderboardManager.updateAllSoon();
    }

    public List<RankedEntry> getTop(int limit) {
        ArrayList<RankedEntry> top = new ArrayList<RankedEntry>();
        ConfigurationSection section = data.getConfigurationSection("players");
        if (section == null) return top;

        for (String uuid : section.getKeys(false)) {
            String base = "players." + uuid;
            String name = data.getString(base + ".name", uuid.substring(0, Math.min(8, uuid.length())));
            int rp = data.getInt(base + ".rp", getDefaultRP());
            String rank = data.getString(base + ".rank", getRankName(rp));
            int wins = data.getInt(base + ".wins", 0);
            int losses = data.getInt(base + ".losses", 0);
            int kills = data.getInt(base + ".kills", 0);
            int games = data.getInt(base + ".games-played", wins + losses);
            top.add(new RankedEntry(uuid, name, rp, rank, wins, losses, kills, games));
        }

        Collections.sort(top, new Comparator<RankedEntry>() {
            @Override
            public int compare(RankedEntry a, RankedEntry b) {
                if (a.getRp() != b.getRp()) return b.getRp() - a.getRp();
                if (a.getWins() != b.getWins()) return b.getWins() - a.getWins();
                return b.getKills() - a.getKills();
            }
        });

        if (limit > 0 && top.size() > limit) {
            return new ArrayList<RankedEntry>(top.subList(0, limit));
        }
        return top;
    }

    public int getPosition(Player p) {
        if (p == null) return -1;
        List<RankedEntry> top = getTop(0);
        String uuid = p.getUniqueId().toString();
        for (int i = 0; i < top.size(); i++) {
            if (top.get(i).getUuid().equalsIgnoreCase(uuid)) return i + 1;
        }
        return -1;
    }

    public void sendTop(Player p, int limit) {
        if (p == null) return;
        List<RankedEntry> top = getTop(limit);
        p.sendMessage("§6§m--------------------------------");
        p.sendMessage("§6§lTOP RANKED §7- §fMelhores jogadores por RP");
        if (top.isEmpty()) {
            p.sendMessage("§cAinda não existem dados ranked salvos.");
        } else {
            int pos = 1;
            for (RankedEntry entry : top) {
                p.sendMessage("§e#" + pos + " §f" + entry.getName() + " §7- §b" + entry.getRp() + " RP §7(" + getRankColor(entry.getRp()) + entry.getRank() + "§7)");
                pos++;
            }
        }
        int myPos = getPosition(p);
        if (myPos > 0) p.sendMessage("§7Sua posição: §e#" + myPos + " §7com §b" + getRP(p) + " RP§7.");
        p.sendMessage("§6§m--------------------------------");
    }

    public void sendProfile(Player p) {
        if (p == null) return;
        int games = getGamesPlayed(p);
        int wins = getWins(p);
        int losses = getLosses(p);
        int kills = getKills(p);
        int pos = getPosition(p);
        p.sendMessage("§6§m--------------------------------");
        p.sendMessage("§6§lPERFIL RANKED §7- §f" + p.getName());
        p.sendMessage("§fPatente: " + getRankDisplay(p));
        p.sendMessage("§fPosição: " + (pos > 0 ? "§e#" + pos : "§7Sem posição"));
        p.sendMessage("§fPartidas: §e" + games + " §7| §fVitórias: §a" + wins + " §7| §fDerrotas: §c" + losses);
        p.sendMessage("§fKills: §c" + kills + " §7| §fWin streak: §e" + getWinStreak(p) + " §7| §fMelhor RP: §b" + getBestRP(p));
        p.sendMessage("§6§m--------------------------------");
    }

    public void debug(Player p, String msg) {
        if (plugin.getConfig().getBoolean("debug-ranked", false)) {
            Bukkit.getConsoleSender().sendMessage("[sMurder Ranked] " + p.getName() + ": " + msg);
        }
    }
}
