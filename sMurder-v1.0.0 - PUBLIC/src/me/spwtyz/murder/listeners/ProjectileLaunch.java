package me.spwtyz.murder.listeners;

import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
//import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.spwtyz.murder.Arenas;
import me.spwtyz.murder.Main;
import me.spwtyz.murder.Utils;

public class ProjectileLaunch implements Listener {

	Main plugin;

	public ProjectileLaunch(Main plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onMove(ProjectileLaunchEvent e) {
		if (plugin.settings.getConfig().getBoolean("bow-delay")) {
			if (e.getEntity().getShooter() instanceof Player) {
				Player p = (Player) e.getEntity().getShooter();

				if (e.getEntity() instanceof Snowball) {
					if (Arenas.isInArena(p)) {

						if (plugin.cooldownTime.containsKey(p.getName())
								|| plugin.cooldownTask.containsKey(p.getName())) {
							p.sendMessage(Utils.FormatText(p, plugin.messages.getConfig().getString("bow-cooldown")));
							e.setCancelled(true);
							return;
						}

						if (!plugin.cooldownTime.containsKey(p.getName())) {

							plugin.cooldownTime.put(p.getName(),
									plugin.settings.getConfig().getInt("gun-delay-seconds"));

							int progress = 0;

							if (plugin.cooldownTime.containsKey(p.getName())) {
								progress = plugin.cooldownTime.get(p.getName());
							}
							StringBuilder veryStringWow = new StringBuilder();

							for (int i = 0; i < plugin.settings.getConfig().getInt("gun-delay-seconds"); i++) {
								if (i < progress) {
									veryStringWow.append(
											Utils.FormatText(p, plugin.messages.getConfig().getString("progress-bar-1")));
								} else {
									veryStringWow.append(
											Utils.FormatText(p, plugin.messages.getConfig().getString("progress-bar-2")));
								}
							}

							String sx = veryStringWow.toString();

							plugin.api.sendActionBar(p, Utils.FormatText(p, plugin.messages.getConfig()
									.getString("bow-actionbar-cooldown").replaceAll("%progress%", sx)));

							plugin.cooldownTask.put(p.getName(), new BukkitRunnable() {
								@Override
								public void run() {
									if (!plugin.cooldownTask.containsKey(p.getName())
											|| !plugin.cooldownTime.containsKey(p.getName())) {

										cancel();

									}
									if (plugin.cooldownTime.containsKey(p.getName())) {

										plugin.cooldownTime.put(p.getName(), plugin.cooldownTime.get(p.getName()) - 1);
									}
									if (plugin.cooldownTime.containsKey(p.getName())) {
										if (plugin.cooldownTime.get(p.getName()) <= 0) {
											if (plugin.cooldownTask.containsKey(p.getName())) {
												plugin.cooldownTask.remove(p.getName());
											}
											if (plugin.cooldownTime.containsKey(p.getName())) {
												plugin.cooldownTime.remove(p.getName());
											}
											cancel();

										}
									}
								}

							});

							if (plugin.cooldownTask.containsKey(p.getName())) {
								plugin.cooldownTask.get(p.getName()).runTaskTimer(plugin, 20, 20);
							}

							new BukkitRunnable() {

								@Override
								public void run() {

									if (!plugin.cooldownTask.containsKey(p.getName())
											|| !plugin.cooldownTime.containsKey(p.getName())) {

										cancel();
										return;

									}

									if (!p.isOnline() && plugin.cooldownTime.containsKey(p.getName())
											&& plugin.cooldownTask.containsKey(p.getName())) {

										if (plugin.cooldownTask.containsKey(p.getName())) {
											plugin.cooldownTask.remove(p.getName());
										}
										if (plugin.cooldownTime.containsKey(p.getName())) {
											plugin.cooldownTime.remove(p.getName());
										}
										cancel();
										return;
									}
									if (plugin.cooldownTime.containsKey(p.getName())) {

										int progress = plugin.cooldownTime.get(p.getName());
										StringBuilder veryStringWow = new StringBuilder();

										for (int i = 0; i < plugin.settings.getConfig()
												.getInt("bow-delay-seconds"); i++) {
											if (i < progress - 1) {
												veryStringWow.append(Utils.FormatText(
														p, plugin.messages.getConfig().getString("progress-bar-1")));
											} else {
												veryStringWow.append(Utils.FormatText(
														p, plugin.messages.getConfig().getString("progress-bar-2")));
											}
										}
										String s = veryStringWow.toString();

										plugin.api.sendActionBar(p, Utils.FormatText(p, plugin.messages.getConfig()
												.getString("gun-actionbar-cooldown").replaceAll("%progress%", s)));
									} else {

										this.cancel();

										plugin.api.sendActionBar(p, Utils.FormatText(
												p, plugin.messages.getConfig().getString("gun-can-use-again")));

									}

								}
							}.runTaskTimer(plugin, 10, 10);

						}
					}
				}
			
				  }
				}
		}
	}

