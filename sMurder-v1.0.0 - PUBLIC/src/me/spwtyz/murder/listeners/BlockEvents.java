package me.spwtyz.murder.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import me.spwtyz.murder.Arenas;
import me.spwtyz.murder.Main;

public class BlockEvents implements Listener {

	Main plugin;

	public BlockEvents(Main plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBreak(BlockBreakEvent e) {
		if (Arenas.isInArena(e.getPlayer())) {
			e.setCancelled(true);
		}
		if (plugin.getConfig().getBoolean("bungee") && !e.getPlayer().isOp()
				&& !e.getPlayer().hasPermission("murder.admin")) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlace(BlockPlaceEvent e) {
		if (Arenas.isInArena(e.getPlayer())) {
			e.setCancelled(true);

		}
		if (plugin.getConfig().getBoolean("bungee") && !e.getPlayer().isOp()
				&& !e.getPlayer().hasPermission("murder.admin")) {
			e.setCancelled(true);
		}
	}
}
