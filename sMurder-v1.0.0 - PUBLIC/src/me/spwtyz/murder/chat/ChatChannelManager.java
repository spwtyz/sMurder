package me.spwtyz.murder.chat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import me.spwtyz.murder.Main;
import me.spwtyz.murder.Utils;

public class ChatChannelManager {

    public static String format(Main plugin, Player p, String message, boolean global) {
        String tag = "";
        if (plugin.tagManager != null) tag = plugin.tagManager.getTag(p);
        if (tag != null && !tag.trim().isEmpty()) tag = tag + " ";
        else tag = "";

        String path = global ? "chat.global-format" : "chat.local-format";
        String def = global ? "&8[&aG&8] %tag%&f%player% &8» &f%message%" : "&8[&eL&8] %tag%&f%player% &8» &7%message%";
        String format = plugin.getConfig().getString(path, def);
        format = format.replace("%tag%", tag)
                .replace("%player%", p.getName())
                .replace("%displayname%", p.getName())
                .replace("%message%", message);
        return Utils.FormatText(p, ChatColor.translateAlternateColorCodes('&', format));
    }

    public static void sendGlobal(Main plugin, Player p, String message) {
        if (message == null || message.trim().isEmpty()) {
            p.sendMessage("§cUse: /g <mensagem>");
            return;
        }
        String formatted = format(plugin, p, message, true);
        for (Player all : Bukkit.getOnlinePlayers()) {
            all.sendMessage(formatted);
        }
        Bukkit.getConsoleSender().sendMessage(ChatColor.stripColor(formatted));
    }

    public static void sendLocal(Main plugin, Player p, String message) {
        if (message == null || message.trim().isEmpty()) {
            p.sendMessage("§cUse: /local <mensagem>");
            return;
        }
        int radius = plugin.getConfig().getInt("chat.local-radius", 50);
        String formatted = format(plugin, p, message, false);
        int sent = 0;
        for (Player all : Bukkit.getOnlinePlayers()) {
            if (!all.getWorld().equals(p.getWorld())) continue;
            if (all.getLocation().distanceSquared(p.getLocation()) > radius * radius) continue;
            all.sendMessage(formatted);
            sent++;
        }
        if (sent <= 1) {
            p.sendMessage("§7Ninguém perto ouviu sua mensagem local.");
        }
        Bukkit.getConsoleSender().sendMessage(ChatColor.stripColor(formatted));
    }
}
