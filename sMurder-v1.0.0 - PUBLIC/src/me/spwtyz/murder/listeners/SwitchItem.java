package me.spwtyz.murder.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import me.spwtyz.murder.Arenas;
import me.spwtyz.murder.Main;

public class SwitchItem implements Listener {

	Main plugin;

	public SwitchItem(Main plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSwap(PlayerSwapHandItemsEvent e) {
		if (Arenas.isInArena(e.getPlayer())) {
			e.setCancelled(true);
		}
		if (plugin.getConfig().getBoolean("bungee") && !e.getPlayer().isOp()
				&& !e.getPlayer().hasPermission("murder.admin")) {
			e.setCancelled(true);
		}
	}

}
