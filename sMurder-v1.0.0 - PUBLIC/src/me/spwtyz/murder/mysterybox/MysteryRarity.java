
package me.spwtyz.murder.mysterybox;

import org.bukkit.ChatColor;

public enum MysteryRarity {
    COMMON("Comum", ChatColor.WHITE),
    RARE("Raro", ChatColor.GREEN),
    EPIC("Épico", ChatColor.DARK_PURPLE),
    LEGENDARY("Lendário", ChatColor.GOLD),
    MYTHIC("Mítico", ChatColor.LIGHT_PURPLE);

    private final String display;
    private final ChatColor color;

    MysteryRarity(String display, ChatColor color) {
        this.display = display;
        this.color = color;
    }

    public String getDisplay() {
        return display;
    }

    public ChatColor getColor() {
        return color;
    }
}
