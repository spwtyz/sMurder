package me.spwtyz.murder.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import me.spwtyz.murder.Arena;
import me.spwtyz.murder.Arenas;
import me.spwtyz.murder.GameState;
import me.spwtyz.murder.Main;
import me.spwtyz.murder.Utils;

public class Motd implements Listener {

	Main plugin;

	public Motd(Main plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void MotdEvent(ServerListPingEvent e) {
		if (plugin.getConfig().getBoolean("bungee")) {
			if (plugin.bungee != null) {
				Arena a = plugin.bungee;
				if (a.getState() == GameState.INGAME) {
					e.setMotd(Utils.FormatText2(plugin.messages.getConfig().getString("ingame-motd")));
				}
				if (a.getState() == GameState.LOBBY) {
					e.setMotd(Utils.FormatText2(plugin.messages.getConfig().getString("lobby-motd")));
				}
				if (a.getState() == GameState.STARTING) {
					e.setMotd(Utils.FormatText2(plugin.messages.getConfig().getString("starting-motd")));
				}
				return;
			}
			if (plugin.bungee == null) {

				if (Arenas.getArenas().size() > 1) {
					e.setMotd(Utils.FormatText2(plugin.messages.getConfig().getString("voting-motd")));
				}

				if (Arenas.getArenas().size() == 1) {
					Arena a = Arenas.getArenas().get(0);
					if (a.getState() == GameState.INGAME) {
						e.setMotd(Utils.FormatText2(plugin.messages.getConfig().getString("ingame-motd")));
					}
					if (a.getState() == GameState.LOBBY) {
						e.setMotd(Utils.FormatText2(plugin.messages.getConfig().getString("lobby-motd")));
					}
					if (a.getState() == GameState.STARTING) {
						e.setMotd(Utils.FormatText2(plugin.messages.getConfig().getString("starting-motd")));
					}

				}
			}
		}
	}

}
