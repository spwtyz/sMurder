package me.spwtyz.murder.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import me.spwtyz.murder.Arena;
import me.spwtyz.murder.Arenas;
import me.spwtyz.murder.Main;
import me.spwtyz.murder.Utils;
import me.spwtyz.murder.chat.ChatChannelManager;
import me.spwtyz.murder.rooms.Room;

public class ChatEvent implements Listener {

    Main plugin;

    public ChatEvent(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void Chat(AsyncPlayerChatEvent e) {

        if (plugin.roomManager != null && plugin.roomManager.isRenaming(e.getPlayer())) {
            e.setCancelled(true);
            final Player player = e.getPlayer();
            final String rawMessage = e.getMessage();
            Bukkit.getScheduler().runTask(plugin, new Runnable() {
                @Override
                public void run() {
                    Room room = plugin.roomManager.consumeRename(player);
                    if (room == null) {
                        player.sendMessage("§cSala não encontrada.");
                        return;
                    }
                    if (rawMessage.equalsIgnoreCase("cancelar") || rawMessage.equalsIgnoreCase("cancel")) {
                        player.sendMessage("§cRenomeação da sala cancelada.");
                        return;
                    }
                    if (rawMessage.equalsIgnoreCase("resetar") || rawMessage.equalsIgnoreCase("reset")) {
                        room.resetCustomName();
                        player.sendMessage("§aNome da sala voltou para: §f" + room.getDisplayName());
                    } else {
                        // Permite cores no nome da sala privada usando &a, &b, &l etc.
                        String plainName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', rawMessage)).trim();
                        String coloredName = ChatColor.translateAlternateColorCodes('&', rawMessage).trim();

                        if (plainName.length() < 3 || plainName.length() > 24) {
                            player.sendMessage("§cO nome da sala precisa ter entre 3 e 24 caracteres, sem contar as cores.");
                            return;
                        }
                        if (!plainName.matches("[A-Za-z0-9À-ÿ _-]+")) {
                            player.sendMessage("§cUse apenas letras, números, espaços, _ ou - no nome da sala. Cores com & são permitidas.");
                            return;
                        }
                        // Limite de segurança para evitar nomes gigantes por spam de códigos de cor.
                        if (coloredName.length() > 64) {
                            player.sendMessage("§cUse menos códigos de cor no nome da sala.");
                            return;
                        }
                        room.setCustomName(coloredName);
                        player.sendMessage("§aNome da sala alterado para: §f" + room.getDisplayName());
                    }
                    if (room.getArena() != null) {
                        room.getArena().updateWaitingScoreboards();
                    }
                }
            });
            return;
        }

        // Chat local padrão opcional. /g continua sendo global.
        if (plugin.getConfig().getBoolean("chat.local-default", false)) {
            e.setCancelled(true);
            Bukkit.getScheduler().runTask(plugin, new Runnable() {
                @Override
                public void run() {
                    ChatChannelManager.sendLocal(plugin, e.getPlayer(), e.getMessage());
                }
            });
            return;
        }

        String tag = "";
        if (plugin.tagManager != null) {
            tag = plugin.tagManager.getTag(e.getPlayer());
        }
        if (tag != null && !tag.trim().isEmpty()) tag = tag + " ";
        else tag = "";

        String format = plugin.getConfig().getString("chat.global-format", "&8[&aG&8] %tag%&f%player% &8» &f%message%");
        format = format.replace("%tag%", tag)
                .replace("%player%", e.getPlayer().getName())
                .replace("%displayname%", e.getPlayer().getName())
                .replace("%message%", "%2$s");
        e.setFormat(Utils.FormatText(e.getPlayer(), format));

        if (plugin.settings.getConfig().getBoolean("per-arena-chat")) {
            if (!Arenas.isInArena(e.getPlayer())) {

                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (Arenas.isInArena(p)) {
                        e.getRecipients().remove(p);
                    }
                }
            }
        }
        if (Arenas.isInArena(e.getPlayer())) {

            Arena a = Arenas.getArena(e.getPlayer());

            if (plugin.settings.getConfig().getBoolean("per-arena-chat")) {
                if (!a.specs.contains(e.getPlayer())) {

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (e.getRecipients().contains(p)) {
                            e.getRecipients().remove(p);
                        }
                    }
                    for (Player p : a.getPlayers()) {
                        if (!e.getRecipients().contains(p)) {
                            e.getRecipients().add(p);
                        }
                    }
                }
            }

            if (a.specs.contains(e.getPlayer())) {

                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (e.getRecipients().contains(p)) {
                        e.getRecipients().remove(p);
                    }
                }
                for (Player p : a.getSpectators()) {
                    if (!e.getRecipients().contains(p)) {
                        e.getRecipients().add(p);
                    }
                }

                e.setFormat(Utils.FormatText(e.getPlayer(), plugin.messages.getConfig().getString("spec-chat-prefix") + e.getFormat()));
            }
        }
    }
}
