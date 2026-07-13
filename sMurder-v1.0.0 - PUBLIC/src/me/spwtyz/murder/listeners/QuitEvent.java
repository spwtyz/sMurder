package me.spwtyz.murder.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import me.spwtyz.murder.Arena;
import me.spwtyz.murder.Arenas;
import me.spwtyz.murder.Main;

public class QuitEvent implements Listener {

	Main plugin;

	public QuitEvent(Main plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void entitydamage(PlayerQuitEvent e) {

		Player p = e.getPlayer();
		if (plugin.getConfig().getBoolean("update-data-on-player-quit")) {
			if (plugin.getPlayerData(p) != null) {
				plugin.removePlayerData(p);
			}
		}

		if (Arenas.isInArena(p)) {
			Arena a = Arenas.getArena(p);
			if (a.pic.contains(p.getName())) {
				a.pic.remove(p.getName());
			}
			if (a.specs.contains(p)) {
				plugin.setup(p);

				a.specs.remove(p);
				plugin.restoreInventory(p);
				Arenas.removeArena(p);
				if (!plugin.getConfig().getBoolean("send-to-server-on-leave")) {
					p.teleport(plugin.getLobby());
				}

				return;
			}

			a.removePlayer(p, "leave");
		}
		if (plugin.getConfig().getBoolean("bungee")) {
			e.setQuitMessage("");
			if (plugin.votes.containsKey(p.getName())) {
				if (plugin.point.get(plugin.votes.get(p.getName())) > 0) {
					plugin.point.put(plugin.votes.get(p.getName()),
							plugin.point.get(plugin.votes.get(p.getName())) - 1);
				}
			}
		}
	}

}
