package me.spwtyz.murder.leaderboard;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public enum LeaderboardType {
    NIVEIS("niveis", "Níveis", "Nível", Material.EXP_BOTTLE),
    ABATES("abates", "Abates", "Abates", Material.DIAMOND_SWORD),
    MORTES("mortes", "Mortes", "Mortes", Material.SKULL_ITEM),
    PARTIDAS_JOGADAS("partidas", "Partidas Jogadas", "Partidas", Material.BOOK),
    PARTIDAS_PERDIDAS("perdidas", "Partidas Perdidas", "Derrotas", Material.REDSTONE),

    RANKED_RP("ranked_rp", "Ranked RP", "RP", Material.NETHER_STAR),
    RANKED_ABATES("ranked_abates", "Ranked Abates", "Abates", Material.IRON_SWORD),
    RANKED_PARTIDAS("ranked_partidas", "Ranked Partidas", "Partidas", Material.PAPER),
    RANKED_PERDIDAS("ranked_perdidas", "Ranked Perdidas", "Derrotas", Material.BLAZE_POWDER);

    private final String id;
    private final String display;
    private final String valueName;
    private final Material icon;

    LeaderboardType(String id, String display, String valueName, Material icon) {
        this.id = id;
        this.display = display;
        this.valueName = valueName;
        this.icon = icon;
    }

    public String getId() { return id; }
    public String getDisplay() { return display; }
    public String getValueName() { return valueName; }
    public Material getIcon() { return icon; }

    public String getColoredTitle() {
        if (name().startsWith("RANKED")) return ChatColor.GOLD + "TOP " + display.toUpperCase();
        return ChatColor.AQUA + "TOP " + display.toUpperCase();
    }

    public static LeaderboardType fromId(String text) {
        if (text == null) return null;
        String t = text.toLowerCase().replace("-", "_");
        for (LeaderboardType type : values()) {
            if (type.id.equalsIgnoreCase(t) || type.name().equalsIgnoreCase(t)) return type;
        }
        if (t.equals("kills") || t.equals("kill")) return ABATES;
        if (t.equals("deaths") || t.equals("death")) return MORTES;
        if (t.equals("levels") || t.equals("level")) return NIVEIS;
        if (t.equals("played") || t.equals("jogadas")) return PARTIDAS_JOGADAS;
        if (t.equals("losses") || t.equals("loses") || t.equals("derrotas")) return PARTIDAS_PERDIDAS;
        if (t.equals("ranked") || t.equals("rp") || t.equals("topranked")) return RANKED_RP;
        return null;
    }
}
