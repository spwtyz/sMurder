package me.spwtyz.murder.level;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.spwtyz.murder.Main;

public class LevelListener implements Listener {

    private final Main plugin;

    public LevelListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                if (event.getPlayer().isOnline() && plugin.levelManager != null) {
                    plugin.levelManager.ensure(event.getPlayer());
                }
            }
        }, 20L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (plugin.levelManager != null) {
            plugin.levelManager.syncXPBar(event.getPlayer());
        }
    }
}
