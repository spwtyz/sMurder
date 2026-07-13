package me.spwtyz.murder.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import me.spwtyz.murder.Arena;
import me.spwtyz.murder.Arenas;
import me.spwtyz.murder.GameState;
import me.spwtyz.murder.Main;

public class DamageEvent implements Listener {

	Main plugin;

	public DamageEvent(Main plugin) {
		this.plugin = plugin;
	}
    
	@EventHandler
	public void entitydamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if (!Arenas.isInArena(p)) {
				if (plugin.getConfig().getBoolean("bungee")) {
					e.setCancelled(true);
				}
			}

			if (Arenas.isInArena(p)) {

				Arena a = Arenas.getArena(p);
				if (a.wincheck) {
					e.setCancelled(true);
					return;
				}
				if (a.specs.contains(p)) {
					e.setCancelled(true);
				}

				if (e.getCause() == DamageCause.VOID) {
					if (a.getState() == GameState.INGAME && a.isTntTagMode() && a.players.contains(p)) {
						e.setCancelled(true);
						a.eliminateTntTagPlayer(p, "pelo void");
						return;
					}
				}

				if (plugin.settings.getConfig().getBoolean("no-fall-damage")) {
					if (e.getCause() == DamageCause.FALL) {
						e.setCancelled(true);
					}
				}
				if (e.getCause() == DamageCause.LAVA) {
					e.setCancelled(true);

					if (a.getState() == GameState.INGAME) {
						if (Arenas.isInArena(p) && a.players.contains(p)) {
							a.removePlayer(p, "death");
						}
					}
				}
				if (e.getCause() == DamageCause.FIRE) {
					e.setCancelled(true);
					p.setFireTicks(0);
					p.damage(1000);
				}
				
				if (e.getCause() == DamageCause.DROWNING) {
					e.setCancelled(true);
		
				}
				if (e.getCause() == DamageCause.FIRE_TICK) {
					e.setCancelled(true);
					p.setFireTicks(0);
				}
				if (e.getCause() == DamageCause.SUFFOCATION) {
					e.setCancelled(true);
				}

				if (a.getState() != GameState.INGAME) {
					e.setCancelled(true);
				}

			}
		}
	}

}
