package me.spwtyz.murder.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import me.spwtyz.murder.Arena;
import me.spwtyz.murder.Arenas;
import me.spwtyz.murder.Main;
import me.spwtyz.murder.Utils;

public class VoteEvent implements Listener {

	Main plugin;

	public VoteEvent(Main plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void Vote(InventoryClickEvent e) {

		Player p = (Player) e.getWhoClicked();
		if (plugin.getConfig().getBoolean("bungee") && !e.getWhoClicked().isOp() && !p.hasPermission("murder.admin")) {
			if (e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {

				e.setCancelled(true);

			}
		}
		if (e.getInventory().getTitle()
				.contains(Utils.FormatText(p, plugin.settings.getConfig().getString("vote-inventory.name")))) {

			if (e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {

				e.setCancelled(true);

			}

			if (!e.getInventory().contains(e.getCurrentItem())) {
				return;
			}

			if (plugin.bungee != null) {
				e.getWhoClicked().sendMessage(Utils.FormatText(p, plugin.messages.getConfig().getString("vote-error")));
				return;
			}
			if (!e.getWhoClicked().hasPermission("murder.vote")) {
				e.getWhoClicked().closeInventory();
				e.getWhoClicked()
						.sendMessage(Utils.FormatText(p, plugin.messages.getConfig().getString("vote-error-perm")));
				return;
			}
			if (e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
				if (e.getCurrentItem().getItemMeta().hasDisplayName()) {
					Arena a = Arenas.getArena(ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()));
					if (a != null) {
						if (plugin.votes.containsKey(p.getName())) {
							if (plugin.point.get(plugin.votes.get(p.getName())) > 0) {
								plugin.point.put(plugin.votes.get(p.getName()),
										plugin.point.get(plugin.votes.get(p.getName())) - 1);
							}
						}
						if (!plugin.votes.containsKey(p.getName())
								|| !plugin.votes.get(p.getName()).contains(a.getName())) {
							plugin.votes.put(p.getName(), a.getName());

							plugin.point.put(a.getName(), plugin.point.get(a.getName()) + 1);
							p.sendMessage(Utils.FormatText(p, plugin.settings.getConfig().getString("vote-message")
									.replaceAll("%map%", a.getName())));
							p.closeInventory();

						}
					}
				}

			}
		}

	}

}
