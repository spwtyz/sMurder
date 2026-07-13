package me.spwtyz.murder.sabotage;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import me.spwtyz.murder.Arena;
import me.spwtyz.murder.Arenas;
import me.spwtyz.murder.GameModeType;
import me.spwtyz.murder.GameState;
import me.spwtyz.murder.Main;

/**
 * Esconde o nome acima da cabeca dos jogadores apenas durante partidas AMONG US.
 * Compatível com Spigot 1.8.8: usa Scoreboard Teams e NameTagVisibility.NEVER.
 */
@SuppressWarnings("deprecation")
public class AmongUsNameTagManager {

    private static final String TEAM_NAME = "amog_hidden";
    private final Main plugin;

    public AmongUsNameTagManager(Main plugin) {
        this.plugin = plugin;
    }

    public void refresh(Player viewer) {
        if (viewer == null || !viewer.isOnline()) return;

        if (!Arenas.isInArena(viewer)) {
            restore(viewer);
            return;
        }

        Arena arena = Arenas.getArena(viewer);
        if (arena == null || arena.getGameMode() != GameModeType.SABOTAGE || arena.getState() != GameState.INGAME) {
            restore(viewer);
            return;
        }

        applyArena(arena);
    }

    public void applyArena(Arena arena) {
        if (arena == null || arena.getGameMode() != GameModeType.SABOTAGE || arena.getState() != GameState.INGAME) return;

        ArrayList<Player> players = arena.getPlayers();
        for (Player viewer : players) {
            if (viewer == null || !viewer.isOnline()) continue;
            applyToBoard(viewer.getScoreboard(), players);
        }
    }

    public void restoreArena(Arena arena) {
        if (arena == null) return;
        for (Player p : arena.getPlayers()) {
            restore(p);
        }
    }

    public void restore(Player viewer) {
        if (viewer == null || !viewer.isOnline()) return;
        Scoreboard board = viewer.getScoreboard();
        if (board == null) return;
        Team hidden = board.getTeam(TEAM_NAME);
        if (hidden != null) {
            try {
                hidden.unregister();
            } catch (IllegalStateException ignored) {}
        }
        if (plugin.tagManager != null) {
            plugin.tagManager.applyVisuals(viewer);
        }
    }

    private void applyToBoard(Scoreboard board, ArrayList<Player> players) {
        if (board == null || players == null) return;

        Team hidden = board.getTeam(TEAM_NAME);
        if (hidden == null) {
            hidden = board.registerNewTeam(TEAM_NAME);
        }
        try {
            hidden.setNameTagVisibility(NameTagVisibility.NEVER);
        } catch (Throwable ignored) {}
        hidden.setPrefix("");
        hidden.setSuffix("");

        for (Player target : players) {
            if (target == null || !target.isOnline()) continue;

            // Um player só pode estar em uma team por scoreboard.
            // Remove das teams de tag/prefixo para garantir que o nome fique escondido.
            for (Team team : board.getTeams()) {
                if (team == null || team.equals(hidden)) continue;
                try {
                    if (team.hasPlayer(target)) {
                        team.removePlayer(target);
                    }
                } catch (Throwable ignored) {}
            }

            if (!hidden.hasPlayer(target)) {
                hidden.addPlayer(target);
            }
        }
    }
}
