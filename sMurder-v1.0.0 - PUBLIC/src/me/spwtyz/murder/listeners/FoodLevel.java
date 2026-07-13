package me.spwtyz.murder.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import me.spwtyz.murder.Arenas;
import me.spwtyz.murder.Main;

public class FoodLevel implements Listener {
	Main plugin;

	public FoodLevel(Main plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void Food(FoodLevelChangeEvent e) {
		Player p = (Player) e.getEntity();
		if (Arenas.isInArena(p)) {
			e.setCancelled(true);
		}
		if (plugin.getConfig().getBoolean("bungee")) {
			e.setCancelled(true);
		}
	}

}
