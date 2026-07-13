package me.spwtyz.murder.objects;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.spwtyz.murder.Arenas;

public class HidePlayers implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        updateVisibility(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Nada para limpar: não guardamos mais estado local de hide/show.
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        updateVisibility(event.getPlayer());
    }

    private void updateVisibility(Player player) {
        if (player == null) return;

        // Dentro de arena/sala/wait lobby a visibilidade é controlada pela própria Arena.
        if (Arenas.isInArena(player)) {
            Arenas.getArena(player).refreshVisibility();
            return;
        }

        World playerWorld = player.getWorld();

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer == null || onlinePlayer.equals(player)) continue;

            // Players que estão em qualquer sala/arena/wait lobby devem ficar escondidos
            // para quem está no lobby principal.
            if (Arenas.isInArena(onlinePlayer)) {
                player.hidePlayer(onlinePlayer);
                Arenas.getArena(onlinePlayer).refreshVisibility();
                continue;
            }

            // Lobby principal: só mostra quem está no mesmo mundo.
            if (onlinePlayer.getWorld().equals(playerWorld)) {
                onlinePlayer.showPlayer(player);
                player.showPlayer(onlinePlayer);
            } else {
                onlinePlayer.hidePlayer(player);
                player.hidePlayer(onlinePlayer);
            }
        }
    }
}
