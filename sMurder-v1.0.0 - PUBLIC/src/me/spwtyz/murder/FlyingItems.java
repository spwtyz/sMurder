package me.spwtyz.murder;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
public class FlyingItems {
	String name;
	Player p;
	Location loc;
	ItemStack m;
	Main plugin;
	ArmorStand a;
	Item i;

	public FlyingItems(Player p, String name, ItemStack m, Location loc, Main plugin) {
		this.p = p;
		this.name = name;
		this.loc = loc;

		this.plugin = plugin;
		this.m = m;
		spawn();
	}

	public void remove() {
		new BukkitRunnable() {

			@Override
			public void run() {
				if (a.getPassenger() != null && !a.getPassenger().isDead()) {
					a.getPassenger().remove();

				}
				if (!a.isDead() && a != null) {
					a.remove();

				}
				if (!i.isDead() && i != null) {
					i.remove();

				}
				p = null;
				loc = null;
				m = null;

				a = null;
				i = null;
			}
		}.runTaskLater(plugin, 20);

	}

	
	public void spawn() {
		if (this.a == null) {
			this.a = loc.getWorld().spawn(loc, ArmorStand.class);
			a.setGravity(true);
			a.setVisible(false);
			a.setArms(false);
			a.setBasePlate(false);
		}
		if (this.i == null) {
			this.i = this.loc.getWorld().dropItem(loc, m);
			i.setCustomName(name);
			i.setCustomNameVisible(true);
			i.setPickupDelay(Integer.MAX_VALUE);
			a.setPassenger(i);
		}
	}

}