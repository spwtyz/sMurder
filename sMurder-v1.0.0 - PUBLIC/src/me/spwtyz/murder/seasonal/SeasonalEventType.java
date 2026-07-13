package me.spwtyz.murder.seasonal;

import org.bukkit.Material;

public enum SeasonalEventType {
    NONE("none", "§7Nenhum", Material.BARRIER, "§7Visual padrão da AltaMC."),
    HALLOWEEN("halloween", "§6§lHalloween", Material.PUMPKIN, "§7Tema de abóboras, sombras e partículas."),
    CHRISTMAS("christmas", "§c§lNatal", Material.SNOW_BALL, "§7Tema de neve, presentes e cores natalinas."),
    EASTER("easter", "§d§lPascoa", Material.EGG, "§7Tema de ovos, coelhos e cores claras."),
    CUSTOM("custom", "§b§lCustom", Material.NETHER_STAR, "§7Evento customizado pelo arquivo events/custom.yml.");

    private final String id;
    private final String displayName;
    private final Material icon;
    private final String description;

    SeasonalEventType(String id, String displayName, Material icon, String description) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public Material getIcon() { return icon; }
    public String getDescription() { return description; }

    public static SeasonalEventType fromString(String text) {
        if (text == null) return NONE;
        String t = text.trim().replace("-", "_").replace(" ", "_").toUpperCase();
        if (t.equals("NATAL")) return CHRISTMAS;
        if (t.equals("PASCOA") || t.equals("PASCOA")) return EASTER;
        if (t.equals("PERSONALIZADO")) return CUSTOM;
        for (SeasonalEventType type : values()) {
            if (type.name().equalsIgnoreCase(t) || type.getId().equalsIgnoreCase(text)) return type;
        }
        return NONE;
    }
}
