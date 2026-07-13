package me.spwtyz.murder.ranked;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.spwtyz.murder.Arena;
import me.spwtyz.murder.GameState;
import me.spwtyz.murder.Main;

/**
 * Ranked V3 zone system.
 * This is NOT Minecraft WorldBorder.
 * It is a virtual shrinking zone based on X/Z limits.
 */
public class RankedBorderManager {

    private final Main plugin;
    private final Arena arena;

    private double minX;
    private double maxX;
    private double minZ;
    private double maxZ;

    private int taskId = -1;
    private int secondsUntilShrink;
    private boolean active = false;
    private int currentSize;
    private int elapsedAfterStart = 0;
    private Location center;
    private int visualTick = 0;
    private final Set<UUID> warned = new HashSet<UUID>();

    public RankedBorderManager(Main plugin, Arena arena) {
        this.plugin = plugin;
        this.arena = arena;
    }

    public void start() {
        if (plugin == null || arena == null) return;
        stop();

        int initialRadius = plugin.getConfig().getInt("ranked-zone.initial-radius", 80);
        secondsUntilShrink = plugin.getConfig().getInt("ranked-zone.start-delay-seconds", 120);
        currentSize = initialRadius * 2;

        center = getConfiguredZoneCenter();
        applyBounds(center, initialRadius);

        active = true;
        elapsedAfterStart = 0;
        warned.clear();

        Bukkit.getConsoleSender().sendMessage("§a[sMurder Ranked] Zona virtual criada: arena=" + arena.getName()
                + " center=" + center.getBlockX() + "," + center.getBlockY() + "," + center.getBlockZ()
                + " initialRadius=" + initialRadius
                + " startDelay=" + secondsUntilShrink
                + " minSize=" + plugin.getConfig().getInt("ranked-zone.min-size", 18));

        broadcast("§6§lRANKED §7> §eA zona vai começar a fechar em §c" + secondsUntilShrink + "s§e!");
        broadcast("§6§lRANKED §7> §7Fique dentro da área segura para não tomar dano.");
        broadcast("§6§lRANKED §7> §aA borda verde mostra a área segura.");

        taskId = Bukkit.getScheduler().runTaskTimer(plugin, new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }, 20L, 20L).getTaskId();
    }

    private Location getConfiguredZoneCenter() {
        Location configured = plugin.getRankedZoneCenter(arena);
        if (configured != null && configured.getWorld() != null) {
            return configured.clone();
        }

        // Fallback seguro: se o admin ainda nao setou a zona, usa o centro medio dos spawns.
        Location spawnCenter = getSpawnAverageCenter();
        if (spawnCenter != null) {
            Bukkit.getConsoleSender().sendMessage("§e[sMurder Ranked] Arena " + arena.getName()
                    + " ainda nao tem ZoneCenter configurado. Usando centro medio dos spawns como fallback.");
            return spawnCenter;
        }

        Location wait = plugin.getWait(arena);
        if (wait != null && wait.getWorld() != null) {
            Bukkit.getConsoleSender().sendMessage("§e[sMurder Ranked] Arena " + arena.getName()
                    + " ainda nao tem ZoneCenter/spawns validos. Usando lobby de espera como fallback.");
            return wait.clone();
        }

        return plugin.getLobby() != null ? plugin.getLobby().clone() : Bukkit.getWorlds().get(0).getSpawnLocation();
    }

    private Location getSpawnAverageCenter() {
        try {
            int spawnSize = plugin.SpawnSize2(arena);
            if (spawnSize <= 0) return null;

            double x = 0.0D;
            double y = 0.0D;
            double z = 0.0D;
            int count = 0;
            Location first = null;

            for (int i = 0; i < spawnSize; i++) {
                Location loc = plugin.getSpawn(arena, i);
                if (loc == null || loc.getWorld() == null) continue;
                if (first == null) first = loc.clone();
                if (!loc.getWorld().equals(first.getWorld())) continue;

                x += loc.getX();
                y += loc.getY();
                z += loc.getZ();
                count++;
            }

            if (count <= 0 || first == null) return null;
            return new Location(first.getWorld(), x / count, y / count, z / count, first.getYaw(), first.getPitch());
        } catch (Exception ignored) {
            return null;
        }
    }

    private void applyBounds(Location c, int radius) {
        if (c == null) return;
        center = c.clone();
        minX = center.getX() - radius;
        maxX = center.getX() + radius;
        minZ = center.getZ() - radius;
        maxZ = center.getZ() + radius;
    }

    private boolean isInsideBounds(Location loc) {
        if (loc == null) return false;
        return loc.getX() >= minX && loc.getX() <= maxX && loc.getZ() >= minZ && loc.getZ() <= maxZ;
    }

    private void tick() {
        if (!active) return;
        if (arena.getState() != GameState.INGAME || arena.wincheck) {
            stop();
            return;
        }

        if (secondsUntilShrink > 0) {
            if (secondsUntilShrink == 60 || secondsUntilShrink == 30 || secondsUntilShrink == 10 || secondsUntilShrink <= 5) {
                broadcast("§6§lRANKED §7> §eA zona fecha em §c" + secondsUntilShrink + "s§e!");
            }
            secondsUntilShrink--;
            drawVisibleBorder();
            updatePlayersDamage(false);
            return;
        }

        int interval = plugin.getConfig().getInt("ranked-zone.shrink-interval-seconds", 10);
        int minSize = plugin.getConfig().getInt("ranked-zone.min-size", 18);
        int shrinkAmount = plugin.getConfig().getInt("ranked-zone.shrink-amount", 3);

        elapsedAfterStart++;

        if (elapsedAfterStart % Math.max(1, interval) == 0 && currentSize > minSize) {
            shrink(shrinkAmount);
        }

        drawVisibleBorder();
        updatePlayersDamage(true);
    }

    private void shrink(int amount) {
        if (currentSize <= plugin.getConfig().getInt("ranked-zone.min-size", 18)) return;

        currentSize -= amount * 2;

        if (currentSize < plugin.getConfig().getInt("ranked-zone.min-size", 18)) {
            currentSize = plugin.getConfig().getInt("ranked-zone.min-size", 18);
        }

        int radius = Math.max(1, currentSize / 2);
        Location newCenter = getConfiguredZoneCenter();
        applyBounds(newCenter, radius);

        broadcast("§6§lRANKED §7> §cA zona está fechando! §7Tamanho: §e" + currentSize + " blocos");
        playSound(Sound.NOTE_PLING, 1f, 0.7f);
        drawVisibleBorder();
    }

    private void updatePlayersDamage(boolean damageEnabled) {
        for (Player p : arena.getPlayers()) {
            if (p == null || !p.isOnline()) continue;
            if (arena.specs.contains(p)) continue;
            if (arena.getState() != GameState.INGAME) continue;

            if (isOutside(p.getLocation())) {
                if (!warned.contains(p.getUniqueId())) {
                    warned.add(p.getUniqueId());
                    p.sendMessage("§c§lZONA §7> §cVocê está fora da área segura!");
                    p.playSound(p.getLocation(), Sound.WITHER_HURT, 1f, 1f);
                }

                if (damageEnabled) {
                    double damage = plugin.getConfig().getDouble("ranked-zone.damage", 1.0D);
                    p.damage(damage);
                    p.getWorld().playEffect(p.getLocation(), Effect.SMOKE, 1);
                }
            } else {
                warned.remove(p.getUniqueId());
            }
        }
    }

    public boolean isOutside(Location loc) {
        if (loc == null) return false;
        return loc.getX() < minX || loc.getX() > maxX || loc.getZ() < minZ || loc.getZ() > maxZ;
    }

    private void drawVisibleBorder() {
        if (!plugin.getConfig().getBoolean("ranked-zone.visible", true)) return;

        visualTick++;
        int everyTicks = Math.max(1, plugin.getConfig().getInt("ranked-zone.visual-every-seconds", 1));
        if (visualTick % everyTicks != 0) return;

        double step = Math.max(1.0D, plugin.getConfig().getDouble("ranked-zone.visual-step", 4.0D));
        int height = Math.max(1, plugin.getConfig().getInt("ranked-zone.visual-height", 3));

        for (Player p : arena.getPlayers()) {
            if (p == null || !p.isOnline()) continue;
            drawBorderFor(p, step, height);
        }
        for (Player p : arena.getSpectators()) {
            if (p == null || !p.isOnline()) continue;
            drawBorderFor(p, step, height);
        }
    }

    private void drawBorderFor(Player p, double step, int height) {
        if (center == null || center.getWorld() == null || p.getWorld() == null) return;
        if (!p.getWorld().equals(center.getWorld())) return;

        double yBase = Math.max(1, p.getLocation().getY());

        for (double x = minX; x <= maxX; x += step) {
            showParticle(p, new Location(center.getWorld(), x, yBase, minZ), height);
            showParticle(p, new Location(center.getWorld(), x, yBase, maxZ), height);
        }

        for (double z = minZ; z <= maxZ; z += step) {
            showParticle(p, new Location(center.getWorld(), minX, yBase, z), height);
            showParticle(p, new Location(center.getWorld(), maxX, yBase, z), height);
        }
    }

    private void showParticle(Player p, Location loc, int height) {
        try {
            for (int y = 0; y < height; y++) {
                Location particleLoc = loc.clone().add(0.0D, y, 0.0D);
                p.playEffect(particleLoc, Effect.HAPPY_VILLAGER, 0);
            }
        } catch (Exception ignored) {
        }
    }


    public int getSecondsUntilShrink() {
        return Math.max(0, secondsUntilShrink);
    }

    public int getCurrentSize() {
        return currentSize;
    }

    public boolean isActive() {
        return active;
    }

    public String getZoneDisplay() {
        if (!active) return "§7Inativa";
        if (secondsUntilShrink > 0) return "§e" + secondsUntilShrink + "s";
        return "§c" + currentSize + " blocos";
    }

    public void stop() {
        active = false;
        warned.clear();

        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    private void broadcast(String msg) {
        for (Player p : arena.getPlayers()) {
            if (p != null && p.isOnline()) p.sendMessage(msg);
        }
        for (Player p : arena.getSpectators()) {
            if (p != null && p.isOnline()) p.sendMessage(msg);
        }
    }

    private void playSound(Sound sound, float volume, float pitch) {
        for (Player p : arena.getPlayers()) {
            if (p != null && p.isOnline()) p.playSound(p.getLocation(), sound, volume, pitch);
        }
    }
}
