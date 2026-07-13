package me.spwtyz.murder.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import me.spwtyz.murder.Arena;
import me.spwtyz.murder.Arenas;
import me.spwtyz.murder.Main;

public class NoSpecDamage implements Listener {
	Main plugin;

	public NoSpecDamage(Main plugin) {

		this.plugin = plugin;
	}

	@EventHandler
	public void entitydamages(EntityDamageByEntityEvent e) {
		if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {

			Player d = (Player) e.getDamager();
			if (Arenas.isInArena(d)) {
				Arena a = Arenas.getArena(d);
				if (a.specs.contains(d)) {
					e.setCancelled(true);
				}
			}
		}
	}

}
