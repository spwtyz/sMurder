package me.spwtyz.murder.listeners;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;

import me.spwtyz.murder.Arenas;
import me.spwtyz.murder.Main;

public class NoPainting implements Listener {

	Main plugin;

	public NoPainting(Main plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onArrow(ProjectileHitEvent e) {
		if (e.getEntity() instanceof Arrow) {
			Arrow arrow = (Arrow) e.getEntity();
			if (arrow.getShooter() instanceof Player) {
				Player p = (Player) arrow.getShooter();
				if (Arenas.isInArena(p)) {
					e.getEntity().remove();
				}
			}

		}
	}

	@EventHandler
	public void onFrameBrake(HangingBreakByEntityEvent e) {
		if (e.getEntity().getType() == EntityType.ITEM_FRAME) {

			if (e.getRemover() instanceof Player) {
				Player p = (Player) e.getRemover();
				if (Arenas.isInArena(p) || plugin.getConfig().getBoolean("bungee")) {
					e.setCancelled(true);
				}
			}
		}
		if (e.getEntity().getType() == EntityType.PAINTING) {

			if (e.getRemover() instanceof Player) {
				Player p = (Player) e.getRemover();
				if (Arenas.isInArena(p) || plugin.getConfig().getBoolean("bungee")) {
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void Target2(org.bukkit.event.entity.EntityTargetEvent e) {
		if (e.getTarget() instanceof Player) {
			Player p = (Player) e.getTarget();
			if (Arenas.isInArena(p)) {
				e.setCancelled(true);
			}
		}

	}

	@EventHandler
	public void Target3(org.bukkit.event.player.PlayerInteractAtEntityEvent e) {

		Player p = e.getPlayer();
		if (Arenas.isInArena(p) || plugin.getConfig().getBoolean("bungee")) {
			if (e.getRightClicked().getType() == EntityType.ARMOR_STAND) {
				e.setCancelled(true);
			}

		}

	}

	@EventHandler
	public void Target4(org.bukkit.event.player.PlayerInteractEntityEvent e) {

		Player p = e.getPlayer();
		if (Arenas.isInArena(p) || plugin.getConfig().getBoolean("bungee")) {
			if (e.getRightClicked().getType() == EntityType.PAINTING

					|| e.getRightClicked().getType() == EntityType.ITEM_FRAME) {
				e.setCancelled(true);
			}

		}

	}

	@EventHandler
	public void Target5(org.bukkit.event.entity.EntityDamageByEntityEvent e) {
		if (e.getDamager() instanceof ItemFrame) {
			Player p = (Player) e.getDamager();
			if (Arenas.isInArena(p) || plugin.getConfig().getBoolean("bungee")) {
				if (e.getEntity() instanceof ItemFrame) {
					e.setCancelled(true);
				}
				if (e.getEntity() instanceof Painting) {
					e.setCancelled(true);
				}
			}
		}

	}

}
