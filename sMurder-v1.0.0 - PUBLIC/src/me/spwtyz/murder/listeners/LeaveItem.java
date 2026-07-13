package me.spwtyz.murder.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import me.spwtyz.murder.Arena;
import me.spwtyz.murder.Arenas;
import me.spwtyz.murder.GameState;
import me.spwtyz.murder.Main;
import me.spwtyz.murder.Utils;

public class LeaveItem implements Listener {
	Main plugin;

	public LeaveItem(Main plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	private void ClickItem(PlayerInteractEvent e) {

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
									Utils.FormatText(p, plugin.settings.getConfig().getString("quit.item-name")))) {

								e.setCancelled(true);
								if (Arenas.isInArena(p)) {
									Arena a = Arenas.getArena(p);
									if (a.specs.contains(p)) {

										plugin.setup(p);

										a.specs.remove(p);
										plugin.restoreInventory(p);
										Arenas.removeArena(p);
										if (!plugin.getConfig().getBoolean("send-to-server-on-leave")) {
											p.teleport(plugin.getLobby());
										}
										if (plugin.getConfig().getBoolean("send-to-server-on-leave")) {
											ByteArrayDataOutput out = ByteStreams.newDataOutput();
											out.writeUTF("Connect");
											out.writeUTF(plugin.getConfig().getString("lobby-server"));

											p.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
										}
										p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
										return;
									}

								}
							}

							if (name.equalsIgnoreCase(
									Utils.FormatText(p, plugin.settings.getConfig().getString("rejoin.item-name")))) {

								e.setCancelled(true);
								if (Arenas.isInArena(p)) {
									Arena a = Arenas.getArena(p);
									if (a.specs.contains(p)) {

										plugin.setup(p);

										a.specs.remove(p);
										plugin.restoreInventory(p);
										Arenas.removeArena(p);
										p.teleport(plugin.getLobby());
										new BukkitRunnable() {

											@Override
											public void run() {
												if (Arenas.getArenas().size() > 0) {
													for (Arena arena1 : Arenas.getArenas()) {
														if (arena1.getState() == GameState.STARTING
																|| arena1.getState() == GameState.LOBBY) {
															if (!Arenas.isInArena(p)) {
																arena1.addPlayer(p);
															}
														}
													}
												}

											}
										}.runTaskLater(plugin,
												20 * plugin.settings.getConfig().getInt("rejoin-interval"));

										return;
									}

								}
							}

							if (name.equalsIgnoreCase(
									Utils.FormatText(p, plugin.settings.getConfig().getString("quit3.item-name")))) {

								e.setCancelled(true);
								if (!Arenas.isInArena(e.getPlayer()) && plugin.getConfig().getBoolean("bungee")) {
									if (plugin.getConfig().getBoolean("send-to-server-on-leave")) {
										ByteArrayDataOutput out = ByteStreams.newDataOutput();
										out.writeUTF("Connect");
										out.writeUTF(plugin.getConfig().getString("lobby-server"));
										if (plugin.isEnabled()) {
											p.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
										}
									}

									if (!plugin.getConfig().getBoolean("send-to-server-on-leave")) {
										p.kickPlayer(Utils
												.FormatText(p, plugin.messages.getConfig().getString("kick-message")));
									}
									return;
								}

							}

							if (name.equalsIgnoreCase(
									Utils.FormatText(p, plugin.settings.getConfig().getString("quit2.item-name")))) {
								e.setCancelled(true);
								plugin.leave2(p);
							}

						}
					}
				}
			}
		}

	}
}
