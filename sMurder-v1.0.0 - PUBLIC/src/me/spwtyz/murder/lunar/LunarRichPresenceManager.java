package me.spwtyz.murder.lunar;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.spwtyz.murder.Arena;
import me.spwtyz.murder.Arenas;
import me.spwtyz.murder.GameModeType;
import me.spwtyz.murder.GameState;
import me.spwtyz.murder.Main;
import me.spwtyz.murder.PlayerState;

/**
 * Integração opcional com Lunar Client Apollo Rich Presence.
 *
 * IMPORTANTE:
 * - Não adiciona dependência obrigatória no plugin.
 * - Se o Apollo não estiver instalado, o sistema apenas fica desligado.
 * - Para aparecer no Discord, o servidor precisa estar configurado no ServerMappings do Lunar.
 */
public class LunarRichPresenceManager implements Listener {

    private final Main plugin;
    private final Map<UUID, String> lastPresence = new HashMap<UUID, String>();
    private boolean apolloChecked = false;
    private boolean apolloAvailable = false;
    private Object richPresenceModule;
    private Class<?> apolloClass;
    private Class<?> richPresenceClass;

    public LunarRichPresenceManager(Main plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (!isEnabled()) return;
        Bukkit.getPluginManager().registerEvents(this, plugin);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isEnabled()) return;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    update(player);
                }
            }
        }.runTaskTimer(plugin, 60L, 100L);

        Bukkit.getConsoleSender().sendMessage("§b[sMurder] Lunar Rich Presence carregado em modo opcional.");
    }

    public void stop() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            reset(player);
        }
        lastPresence.clear();
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                update(event.getPlayer());
            }
        }.runTaskLater(plugin, 60L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastPresence.remove(event.getPlayer().getUniqueId());
    }

    public void forceUpdate(Player player) {
        if (player == null) return;
        lastPresence.remove(player.getUniqueId());
        update(player);
    }

    public void update(Player player) {
        if (player == null || !player.isOnline() || !isEnabled()) return;
        if (!loadApollo()) return;

        PresenceData data = buildPresence(player);
        String hash = data.asHash();
        String old = lastPresence.get(player.getUniqueId());
        if (hash.equals(old)) return;

        if (override(player, data)) {
            lastPresence.put(player.getUniqueId(), hash);
        }
    }

    private PresenceData buildPresence(Player player) {
        Arena arena = Arenas.getArena(player);
        PlayerState playerState = plugin.getPlayerState(player);

        String serverName = colorless(plugin.getConfig().getString("LunarRichPresence.Server-Name", "sMurder"));
        String gameName = colorless(plugin.getConfig().getString("LunarRichPresence.Game-Name", "Murder"));
        String variant = "Lobby Principal";
        String gameState = "Lobby";
        String playerStateText = "No lobby";
        String mapName = colorless(plugin.getConfig().getString("LunarRichPresence.Default-Map", "Lobby"));
        int currentPlayers = Bukkit.getOnlinePlayers().size();
        int maxPlayers = Bukkit.getMaxPlayers();

        if (arena != null) {
            GameModeType mode = arena.getGameMode();
            variant = mode == null ? "Normal" : mode.getDisplayName();
            mapName = arena.getTemplateName();
            currentPlayers = arena.getPlayers().size();
            maxPlayers = arena.getMaxPlayers();

            if (arena.getState() == GameState.INGAME) {
                gameState = "Em partida";
                playerStateText = playerState == PlayerState.SPECTATOR ? "Espectando" : "Jogando";
            } else if (arena.getState() == GameState.STARTING) {
                gameState = "Iniciando";
                playerStateText = "Esperando iniciar";
            } else {
                gameState = "Aguardando jogadores";
                playerStateText = "Na sala";
            }

            if (mode == GameModeType.RANKED && plugin.rankedManager != null) {
                gameName = "Murder Ranked";
                String rank = plugin.rankedManager.getRankName(player);
                if (rank != null && !rank.isEmpty()) {
                    playerStateText = playerStateText + " - " + colorless(rank);
                }
            }
        } else if (playerState == PlayerState.ROOM_LOBBY) {
            variant = "Sala";
            gameState = "Lobby de sala";
            playerStateText = "Criando partida";
        }

        return new PresenceData(gameName, variant, gameState, playerStateText, mapName, serverName, currentPlayers, maxPlayers);
    }

    private boolean override(Player player, PresenceData data) {
        try {
            Object apolloPlayerOpt = apolloClass.getMethod("getPlayerManager").invoke(null);
            Object optional = apolloPlayerOpt.getClass().getMethod("getPlayer", UUID.class).invoke(apolloPlayerOpt, player.getUniqueId());

            if (!(optional instanceof Optional)) return false;
            Optional<?> opt = (Optional<?>) optional;
            if (!opt.isPresent()) return false;
            Object apolloPlayer = opt.get();

            Object builder = richPresenceClass.getMethod("builder").invoke(null);
            builder = builder.getClass().getMethod("gameName", String.class).invoke(builder, data.gameName);
            builder = builder.getClass().getMethod("gameVariantName", String.class).invoke(builder, data.variantName);
            builder = builder.getClass().getMethod("gameState", String.class).invoke(builder, data.gameState);
            builder = builder.getClass().getMethod("playerState", String.class).invoke(builder, data.playerState);
            builder = builder.getClass().getMethod("mapName", String.class).invoke(builder, data.mapName);
            builder = builder.getClass().getMethod("subServerName", String.class).invoke(builder, data.subServerName);
            builder = builder.getClass().getMethod("teamCurrentSize", Integer.class).invoke(builder, Integer.valueOf(data.currentPlayers));
            builder = builder.getClass().getMethod("teamMaxSize", Integer.class).invoke(builder, Integer.valueOf(data.maxPlayers));
            Object presence = builder.getClass().getMethod("build").invoke(builder);

            richPresenceModule.getClass().getMethod("overrideServerRichPresence", apolloPlayer.getClass().getInterfaces()[0], richPresenceClass)
                    .invoke(richPresenceModule, apolloPlayer, presence);
            return true;
        } catch (Throwable ignored) {
            // Tentativa alternativa: alguns builds usam a classe concreta ApolloPlayer no método.
            try {
                Object playerManager = apolloClass.getMethod("getPlayerManager").invoke(null);
                Object optional = playerManager.getClass().getMethod("getPlayer", UUID.class).invoke(playerManager, player.getUniqueId());
                if (!(optional instanceof Optional)) return false;
                Optional<?> opt = (Optional<?>) optional;
                if (!opt.isPresent()) return false;
                Object apolloPlayer = opt.get();

                Object builder = richPresenceClass.getMethod("builder").invoke(null);
                builder = builder.getClass().getMethod("gameName", String.class).invoke(builder, data.gameName);
                builder = builder.getClass().getMethod("gameVariantName", String.class).invoke(builder, data.variantName);
                builder = builder.getClass().getMethod("gameState", String.class).invoke(builder, data.gameState);
                builder = builder.getClass().getMethod("playerState", String.class).invoke(builder, data.playerState);
                builder = builder.getClass().getMethod("mapName", String.class).invoke(builder, data.mapName);
                builder = builder.getClass().getMethod("subServerName", String.class).invoke(builder, data.subServerName);
                builder = builder.getClass().getMethod("teamCurrentSize", Integer.class).invoke(builder, Integer.valueOf(data.currentPlayers));
                builder = builder.getClass().getMethod("teamMaxSize", Integer.class).invoke(builder, Integer.valueOf(data.maxPlayers));
                Object presence = builder.getClass().getMethod("build").invoke(builder);

                Method target = null;
                for (Method method : richPresenceModule.getClass().getMethods()) {
                    if (!method.getName().equals("overrideServerRichPresence")) continue;
                    if (method.getParameterTypes().length == 2) {
                        target = method;
                        break;
                    }
                }
                if (target == null) return false;
                target.invoke(richPresenceModule, apolloPlayer, presence);
                return true;
            } catch (Throwable ignored2) {
                return false;
            }
        }
    }

    private void reset(Player player) {
        if (player == null || !loadApollo()) return;
        try {
            Object playerManager = apolloClass.getMethod("getPlayerManager").invoke(null);
            Object optional = playerManager.getClass().getMethod("getPlayer", UUID.class).invoke(playerManager, player.getUniqueId());
            if (!(optional instanceof Optional)) return;
            Optional<?> opt = (Optional<?>) optional;
            if (!opt.isPresent()) return;
            Object apolloPlayer = opt.get();
            Method target = null;
            for (Method method : richPresenceModule.getClass().getMethods()) {
                if (method.getName().equals("resetServerRichPresence") && method.getParameterTypes().length == 1) {
                    target = method;
                    break;
                }
            }
            if (target != null) target.invoke(richPresenceModule, apolloPlayer);
        } catch (Throwable ignored) {}
    }

    private boolean loadApollo() {
        if (apolloChecked) return apolloAvailable;
        apolloChecked = true;
        try {
            apolloClass = Class.forName("com.lunarclient.apollo.Apollo");
            Class<?> moduleClass = Class.forName("com.lunarclient.apollo.module.richpresence.RichPresenceModule");
            richPresenceClass = Class.forName("com.lunarclient.apollo.module.richpresence.ServerRichPresence");
            Object moduleManager = apolloClass.getMethod("getModuleManager").invoke(null);
            richPresenceModule = moduleManager.getClass().getMethod("getModule", Class.class).invoke(moduleManager, moduleClass);
            apolloAvailable = true;
            Bukkit.getConsoleSender().sendMessage("§a[sMurder] Apollo detectado: Rich Presence ativo.");
        } catch (Throwable error) {
            apolloAvailable = false;
            Bukkit.getConsoleSender().sendMessage("§e[sMurder] Apollo não detectado. Rich Presence Lunar ficará desligado.");
        }
        return apolloAvailable;
    }

    private boolean isEnabled() {
        return plugin.getConfig().getBoolean("LunarRichPresence.Enabled", true);
    }

    private String colorless(String text) {
        if (text == null) return "";
        return text.replaceAll("§[0-9A-FK-ORa-fk-or]", "").replace('&', ' ').trim();
    }

    private static class PresenceData {
        private final String gameName;
        private final String variantName;
        private final String gameState;
        private final String playerState;
        private final String mapName;
        private final String subServerName;
        private final int currentPlayers;
        private final int maxPlayers;

        private PresenceData(String gameName, String variantName, String gameState, String playerState, String mapName, String subServerName, int currentPlayers, int maxPlayers) {
            this.gameName = gameName;
            this.variantName = variantName;
            this.gameState = gameState;
            this.playerState = playerState;
            this.mapName = mapName;
            this.subServerName = subServerName;
            this.currentPlayers = Math.max(0, currentPlayers);
            this.maxPlayers = Math.max(1, maxPlayers);
        }

        private String asHash() {
            return gameName + "|" + variantName + "|" + gameState + "|" + playerState + "|" + mapName + "|" + subServerName + "|" + currentPlayers + "/" + maxPlayers;
        }
    }
}
