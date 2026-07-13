package me.spwtyz.murder.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.spwtyz.murder.Main;
import me.spwtyz.murder.Utils;

public class OpenVoteGUI implements Listener {

	Main plugin;

	public OpenVoteGUI(Main plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	private void ClickItem23(PlayerInteractEvent e) {

		if (e.getPlayer().getItemInHand() != null) {

			final Player p = e.getPlayer();

			if (e.getAction().name().toLowerCase().contains("right")) {
				if (p.getItemInHand().getType() == Material.AIR) {
					return;
				}
				ItemStack is;
				if ((is = p.getItemInHand()).hasItemMeta()) {
					ItemMeta im;
					if ((im = is.getItemMeta()).hasDisplayName()) {
						String name;
						if ((name = im.getDisplayName()) != null) {

							if (name.equalsIgnoreCase(
									Utils.FormatText(p, plugin.settings.getConfig().getString("map.item-name")))) {
								e.setCancelled(true);
								plugin.OpenVote(p);

							}
						}
					}
				}
			}
		}
	}

}
