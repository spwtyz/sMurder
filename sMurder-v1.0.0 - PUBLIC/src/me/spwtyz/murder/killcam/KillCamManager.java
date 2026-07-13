package me.spwtyz.murder.killcam;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.spwtyz.murder.Arena;
import me.spwtyz.murder.Main;

public class KillCamManager implements Listener {

    private final Main plugin;
    private final Set<UUID> watching = new HashSet<UUID>();

    public KillCamManager(Main plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public boolean isWatching(Player player) {
        return player != null && watching.contains(player.getUniqueId());
    }

    public boolean shouldUseKillCam(Arena arena, Player victim, Player killer) {
        if (arena == null || victim == null || killer == null) return false;
        if (!plugin.getConfig().getBoolean("ranked-killcam.enabled", true)) return false;
        if (!arena.isRankedMode()) return false;
        if (!killer.isOnline() || !victim.isOnline()) return false;
        if (killer.equals(victim)) return false;
        return true;
    }

    public void start(final Player victim, final Player killer, final Arena arena, final String finalSpectatorMessage) {
        if (!shouldUseKillCam(arena, victim, killer)) {
            sendToSpectator(victim, arena, finalSpectatorMessage);
            return;
        }

        watching.add(victim.getUniqueId());
        victim.closeInventory();
        victim.getInventory().clear();
        victim.getInventory().setArmorContents(null);
        victim.setAllowFlight(true);
        victim.setFlying(true);
        victim.setFallDistance(0.0F);

        // Esconde a vitima durante a kill cam para ela nao atrapalhar a partida.
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online != null && !online.equals(victim)) {
                try { online.hidePlayer(victim); } catch (Throwable ignored) {}
            }
        }

        Location view = getViewLocation(killer);
        try { victim.teleport(view); } catch (Throwable ignored) {}

        int seconds = plugin.getConfig().getInt("ranked-killcam.seconds", 4);
        if (seconds < 1) seconds = 1;
        if (seconds > 10) seconds = 10;

        victim.sendMessage(ChatColor.DARK_RED + "§lKILL CAM" + ChatColor.GRAY + " | " + ChatColor.RED + "Voce foi eliminado por " + ChatColor.WHITE + killer.getName());
        victim.sendMessage(ChatColor.GRAY + "Assistindo o assassino por " + ChatColor.YELLOW + seconds + "s" + ChatColor.GRAY + "...");
        try { victim.playSound(victim.getLocation(), Sound.NOTE_BASS, 1.0F, 0.7F); } catch (Throwable ignored) {}

        // Spectator target por reflection para nao quebrar builds antigas do Spigot 1.8.
        try {
            victim.setGameMode(GameMode.SPECTATOR);
            Method m = Player.class.getMethod("setSpectatorTarget", org.bukkit.entity.Entity.class);
            m.invoke(victim, killer);
        } catch (Throwable ignored) {
            try { victim.setGameMode(GameMode.ADVENTURE); } catch (Throwable ignored2) {}
        }

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!watching.contains(victim.getUniqueId())) {
                    cancel();
                    return;
                }
                if (!victim.isOnline()) {
                    watching.remove(victim.getUniqueId());
                    cancel();
                    return;
                }
                if (killer.isOnline()) {
                    followKiller(victim, killer);
                }
                ticks += 5;
                int totalTicks = plugin.getConfig().getInt("ranked-killcam.seconds", 4) * 20;
                if (ticks >= totalTicks) {
                    watching.remove(victim.getUniqueId());
                    sendToSpectator(victim, arena, finalSpectatorMessage);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 5L, 5L);
    }

    private void followKiller(Player victim, Player killer) {
        if (victim == null || killer == null || !victim.isOnline() || !killer.isOnline()) return;
        try {
            if (victim.getGameMode() == GameMode.SPECTATOR) {
                Method m = Player.class.getMethod("setSpectatorTarget", org.bukkit.entity.Entity.class);
                m.invoke(victim, killer);
                return;
            }
        } catch (Throwable ignored) {}
        try { victim.teleport(getViewLocation(killer)); } catch (Throwable ignored) {}
    }

    private Location getViewLocation(Player killer) {
        Location base = killer.getLocation().clone();
        Location eye = killer.getEyeLocation().clone();
        Location back = eye.subtract(base.getDirection().normalize().multiply(4.0D));
        back.setY(base.getY() + 2.2D);
        back.setYaw(base.getYaw());
        back.setPitch(Math.min(35.0F, Math.max(-10.0F, base.getPitch())));
        return back;
    }

    private void sendToSpectator(Player victim, Arena arena, String message) {
        if (victim == null || !victim.isOnline() || arena == null) return;
        watching.remove(victim.getUniqueId());
        clearSpectatorTarget(victim);
        if (plugin.spectatorManager != null) {
            plugin.spectatorManager.makeSpectator(victim, arena, message == null ? "§cVoce morreu!" : message);
        } else {
            try { victim.setGameMode(GameMode.ADVENTURE); } catch (Throwable ignored) {}
        }
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online != null && !online.equals(victim)) {
                try { online.hidePlayer(victim); } catch (Throwable ignored) {}
            }
        }
    }

    private void clearSpectatorTarget(Player player) {
        if (player == null) return;
        try {
            Method m = Player.class.getMethod("setSpectatorTarget", org.bukkit.entity.Entity.class);
            m.invoke(player, new Object[] { null });
        } catch (Throwable ignored) {}
    }

    public void cancel(Player player) {
        if (player == null) return;
        watching.remove(player.getUniqueId());
        clearSpectatorTarget(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cancel(event.getPlayer());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (isWatching(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (isWatching(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (isWatching(event.getPlayer())) {
            String msg = event.getMessage() == null ? "" : event.getMessage().toLowerCase();
            if (!msg.startsWith("/m leave") && !msg.startsWith("/leave")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "Aguarde a Kill Cam terminar.");
            }
        }
    }
}
