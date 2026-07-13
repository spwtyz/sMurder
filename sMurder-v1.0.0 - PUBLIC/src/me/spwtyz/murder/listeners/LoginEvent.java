package me.spwtyz.murder.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import me.spwtyz.murder.Arena;
import me.spwtyz.murder.Arenas;
import me.spwtyz.murder.GameState;
import me.spwtyz.murder.Main;
import me.spwtyz.murder.Utils;

public class LoginEvent implements Listener {
	Main plugin;

	public LoginEvent(Main plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onLogin(PlayerLoginEvent e) {

		if (plugin.getConfig().getBoolean("bungee")) {
			if (Arenas.getArenas().size() == 1) {
				Arena a = Arenas.getArenas().get(0);
				if (a.getState() == GameState.INGAME) {
					e.setKickMessage(
							Utils.FormatText(e.getPlayer(), plugin.messages.getConfig().getString("join-error")));
					e.setResult(Result.KICK_FULL);
				}
			}
		}

		if (plugin.getConfig().getBoolean("bungee")) {
			if (Arenas.getArenas().size() > 1) {
				if (plugin.bungee != null) {
					if (plugin.bungee.getState() == GameState.INGAME) {
						e.setKickMessage(
								Utils.FormatText(e.getPlayer(), plugin.messages.getConfig().getString("join-error")));
						e.setResult(Result.KICK_FULL);
					}
				}

			}
		}
	}

}
