package me.spwtyz.murder.listeners;
import java.util.ArrayList;

import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.spwtyz.murder.Arena;
import me.spwtyz.murder.Arenas;
import me.spwtyz.murder.GameState;
import me.spwtyz.murder.Main;

public class JoinEvent implements Listener {
	Main plugin;

	public JoinEvent(Main plugin) {
		this.plugin = plugin;
	}
	
	  ArrayList<ArmorStand> axes = new ArrayList<>();

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {

		if (plugin.settings.getConfig().getBoolean("tp-lobby-on-join")
				&& plugin.arenas.getConfig().contains("Lobby.main.lobby") && plugin.getLobby() != null) {
			e.getPlayer().teleport(plugin.getLobby());
		}

		plugin.registerNewData(e.getPlayer());
		plugin.setScoreboard(e.getPlayer());
		new BukkitRunnable() {
			@Override
			public void run() {
				if (plugin.tagManager != null && e.getPlayer().isOnline()) {
					plugin.tagManager.applyVisuals(e.getPlayer());
				}
			}
		}.runTaskLater(plugin, 20L);
		if (plugin.getConfig().getBoolean("bungee")) {
			e.setJoinMessage("");
			if (Arenas.getArenas().size() == 1) {
				Arena a = Arenas.getArenas().get(0);
				if (a.getState() != GameState.INGAME) {
					if (!Arenas.isInArena(e.getPlayer())) {
						a.addPlayer(e.getPlayer());
					}
				}
			}
		}
		
		
		if (plugin.getConfig().getBoolean("bungee")) {
			if (plugin.bungee != null) {
				plugin.bungee.addPlayer(e.getPlayer());
				return;
			}
			if (Arenas.getArenas().size() > 1) {
				plugin.setUpForMultiMaps(e.getPlayer());
			}
		 }
	  } 
   }