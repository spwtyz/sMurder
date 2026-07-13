package me.spwtyz.murder.events;

import org.bukkit.entity.Player;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PlayerPlaceholder extends PlaceholderExpansion {
	
    public String getIdentifier() {
        return "smurder"; // Use a unique identifier for your plugin
    }

    public String getAuthor() {
        return "spdoprimeiro";
    }

    public String getVersion() {
        return "1.0";
    }

    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        // Check the requested placeholder
        if (identifier.equals("player")) {
            return player.getName();
        }

        // Return an empty string for unknown placeholders
        return "";
    }
}
