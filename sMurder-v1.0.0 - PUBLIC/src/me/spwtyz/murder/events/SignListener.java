package me.spwtyz.murder.events;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import me.spwtyz.murder.Arena;
import me.spwtyz.murder.Arenas;
import me.spwtyz.murder.GameState;
import me.spwtyz.murder.Main;
import me.spwtyz.murder.SignManager;
import me.spwtyz.murder.Utils;

public class SignListener implements Listener {

	Main plugin;
	SignManager sm;

	public SignListener(Main plugin) {
		this.plugin = plugin;
		this.sm = plugin.sm;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSignBreak(BlockBreakEvent e) {
		if (e.getBlock().getState() instanceof Sign) {
			Sign sign = (Sign) e.getBlock().getState();
			if (e.getPlayer().hasPermission("murder.sign")) {
				if (sign.getLine(0)
						.equalsIgnoreCase(Utils.FormatText2(plugin.messages.getConfig().getString("sign-header")))) {
					if (e.getPlayer().isSneaking()) {

						for (Arena arena : Arenas.getArenas()) {
							if (ChatColor.stripColor(sign.getLine(1)).equalsIgnoreCase(arena.getName())) {
								sm.removeSign(arena, e.getBlock().getLocation());

								break;
							}
						}

					} else {
						e.setCancelled(true);
						sign.update(true);

					}

				}
			} else {
				if (sign.getLine(0)
						.equalsIgnoreCase(Utils.FormatText2(plugin.messages.getConfig().getString("sign-header")))) {
					e.setCancelled(true);
					sign.update(true);
				}

			}

		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSignCreate(SignChangeEvent e) {
		Player player = e.getPlayer();
		if ((e.getLine(0).equalsIgnoreCase("[Murder]")) && (player.hasPermission("murder.admin"))) {
			for (Arena arena : Arenas.getArenas()) {

				if (ChatColor.stripColor(e.getLine(1)).equalsIgnoreCase(arena.getName())) {

					e.setLine(0, Utils.FormatText2(plugin.messages.getConfig().getString("sign-header")));
					e.setLine(1, Utils.FormatText2(plugin.messages.getConfig().getString("sign-arena")
							.replaceAll("%arena%", arena.getName())));

					if (arena.getState() == GameState.INGAME) {
						e.setLine(2, Utils.FormatText2(plugin.messages.getConfig().getString("sign-ingame")));
					} else {
						if (arena.getState() == GameState.LOBBY) {
							e.setLine(2, Utils.FormatText2(plugin.messages.getConfig().getString("sign-lobby")));

						}
					}

					sm.addSign(arena, e.getBlock().getLocation());

				}
			}
		}

	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSignInteract(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (!e.getPlayer().isSneaking()) {
				Block b = e.getClickedBlock();
				if (b != null) {
					if (b.getState() instanceof Sign) {
						Sign sign = (Sign) b.getState();
						if (sign.getLine(0).equalsIgnoreCase(
								Utils.FormatText2(plugin.messages.getConfig().getString("sign-header")))) {

							for (Arena arena : Arenas.getArenas()) {
								if (ChatColor.stripColor(sign.getLine(1)).equalsIgnoreCase(arena.getName())) {

									arena.addPlayer(e.getPlayer());

									break;

								}
							}
						}
					}
				}
			}
		}
	}
}