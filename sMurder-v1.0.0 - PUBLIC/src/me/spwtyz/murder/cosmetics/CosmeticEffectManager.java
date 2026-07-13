package me.spwtyz.murder.cosmetics;

import java.util.*;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import me.spwtyz.murder.Main;

/**
 * Aplica os cosmeticos selecionados na loja dentro da partida.
 * A loja salva os selecionados em data.yml em:
 * Cosmetics.<uuid>.<category>.selected
 */
public class CosmeticEffectManager {

    private CosmeticEffectManager() {}

    public static String getSelected(Main plugin, Player p, String category) {
        if (plugin == null || plugin.data == null || p == null || category == null) return "nenhum";
        return plugin.data.getConfig().getString("Cosmetics." + p.getUniqueId() + "." + category + ".selected", "nenhum");
    }

    public static void playDeathEffect(Main plugin, Player victim) {
        if (victim == null) return;
        String selected = getSelected(plugin, victim, "death");
        // Sem efeito selecionado: o sistema antigo de fogos funciona como fallback.
        // Quando existe efeito equipado, somente este método toca o cosmético.
        if (selected == null || selected.trim().isEmpty() || selected.equalsIgnoreCase("nenhum")) return;

        Location loc = victim.getLocation();
        World world = loc.getWorld();
        if (world == null) return;

        if (selected.equalsIgnoreCase("death_lightning")) {
            world.strikeLightningEffect(loc);
            world.playSound(loc, Sound.AMBIENCE_THUNDER, 0.9f, 1.2f);
            return;
        }

        if (selected.equalsIgnoreCase("death_explosion")) {
            world.playEffect(loc, Effect.EXPLOSION_HUGE, 0);
            world.playSound(loc, Sound.EXPLODE, 0.8f, 1.0f);
            return;
        }

        if (selected.equalsIgnoreCase("death_funeral")) {
            playFuneralMeme(plugin, loc, victim.getName());
            return;
        }

        if (selected.equalsIgnoreCase("death_ghost")) {
            world.playEffect(loc, Effect.GHAST_SHOOT, 0);
            world.playEffect(loc.add(0, 0.7, 0), Effect.SMOKE, 0);
            world.playSound(victim.getLocation(), Sound.GHAST_MOAN, 0.7f, 1.5f);
            return;
        }

        if (selected.startsWith("seasonal_")) {
            if (selected.contains("halloween")) {
                world.playEffect(loc, Effect.SMOKE, 0);
                world.playEffect(loc.clone().add(0, 0.8, 0), Effect.MOBSPAWNER_FLAMES, 0);
                world.playSound(loc, Sound.BAT_DEATH, 1f, 0.7f);
            } else if (selected.contains("christmas")) {
                world.playEffect(loc, Effect.SNOWBALL_BREAK, 0);
                world.playSound(loc, Sound.NOTE_PLING, 1f, 1.6f);
            } else if (selected.contains("easter")) {
                world.playEffect(loc, Effect.HAPPY_VILLAGER, 0);
                world.playSound(loc, Sound.CHICKEN_EGG_POP, 1f, 1.2f);
            } else {
                world.playEffect(loc, Effect.ENDER_SIGNAL, 0);
                world.playSound(loc, Sound.ORB_PICKUP, 1f, 1.2f);
            }
        }
    }

    /**
     * Funeral Meme V2: substitui o Funeral antigo por um efeito mais marcante.
     * Leve para 1.8: usa apenas ArmorStands temporários, blocos na cabeça e sons vanilla.
     */
    private static void playFuneralMeme(final Main plugin, final Location deathLoc, final String victimName) {
        if (plugin == null || deathLoc == null || deathLoc.getWorld() == null) return;

        final World world = deathLoc.getWorld();
        final Location center = deathLoc.clone();
        center.setY(center.getY() + 0.15D);

        final List<ArmorStand> stands = new ArrayList<ArmorStand>();

        // Caixão: armorstand invisível com bloco escuro na cabeça.
        ArmorStand coffin = spawnFuneralStand(plugin, center.clone().add(0, 0.45D, 0), true, "§8⚰ §5Funeral §8⚰");
        coffin.setHelmet(new ItemStack(Material.COAL_BLOCK));
        stands.add(coffin);

        // Quatro carregadores pequenos em volta do caixão.
        double[][] offsets = new double[][] {
                { 0.75D, 0.00D },
                {-0.75D, 0.00D },
                { 0.00D, 0.75D },
                { 0.00D,-0.75D }
        };

        for (double[] off : offsets) {
            ArmorStand bearer = spawnFuneralStand(plugin, center.clone().add(off[0], 0.0D, off[1]), false, "");
            bearer.setSmall(true);
            bearer.setHelmet(new ItemStack(Material.SKULL_ITEM, 1, (short) 1));
            bearer.setItemInHand(new ItemStack(Material.BONE));
            stands.add(bearer);
        }

        world.playSound(center, Sound.NOTE_BASS, 1.0f, 0.6f);
        world.playSound(center, Sound.NOTE_PLING, 1.0f, 1.4f);
        world.playEffect(center, Effect.SMOKE, 0);

        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                tick++;

                if (tick > 70 || stands.isEmpty()) {
                    for (ArmorStand stand : stands) {
                        if (stand != null && !stand.isDead()) stand.remove();
                    }
                    cancel();
                    return;
                }

                double angle = tick * 0.22D;

                for (int i = 0; i < stands.size(); i++) {
                    ArmorStand stand = stands.get(i);
                    if (stand == null || stand.isDead()) continue;

                    if (i == 0) {
                        // Caixão sobe/desce levemente.
                        Location l = center.clone().add(0, 0.50D + Math.sin(tick * 0.35D) * 0.08D, 0);
                        l.setYaw((float) Math.toDegrees(angle));
                        stand.teleport(l);
                        continue;
                    }

                    double r = 0.95D;
                    double a = angle + (i * Math.PI / 2.0D);
                    Location l = center.clone().add(Math.cos(a) * r, 0.02D, Math.sin(a) * r);
                    l.setYaw((float) Math.toDegrees(a + Math.PI));
                    stand.teleport(l);
                }

                if (tick % 8 == 0) {
                    world.playEffect(center.clone().add(0, 0.6D, 0), Effect.SMOKE, 0);
                    world.playSound(center, tick % 16 == 0 ? Sound.NOTE_BASS : Sound.NOTE_PLING, 0.75f, tick % 16 == 0 ? 0.7f : 1.7f);
                }

                if (tick == 55) {
                    world.playSound(center, Sound.WITHER_DEATH, 0.35f, 1.6f);
                    world.playEffect(center.clone().add(0, 0.8D, 0), Effect.ENDER_SIGNAL, 0);
                    ArmorStand rip = spawnFuneralStand(plugin, center.clone().add(0, 1.55D, 0), true, "§8⚰ §7RIP §f" + victimName);
                    stands.add(rip);
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    private static ArmorStand spawnFuneralStand(Main plugin, Location loc, boolean invisible, String name) {
        ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setGravity(false);
        stand.setVisible(!invisible);
        stand.setCanPickupItems(false);
        stand.setMetadata("windmc_funeral_effect", new FixedMetadataValue(plugin, true));
        if (name != null && !name.isEmpty()) {
            stand.setCustomName(name);
            stand.setCustomNameVisible(true);
        }
        return stand;
    }

    public static void playVictoryEffect(Main plugin, Player winner) {
        if (winner == null) return;
        String selected = getSelected(plugin, winner, "victory");
        if (selected == null || selected.equalsIgnoreCase("nenhum")) return;

        Location loc = winner.getLocation();
        World world = loc.getWorld();
        if (world == null) return;

        if (selected.equalsIgnoreCase("victory_firework")) {
            world.playEffect(loc, Effect.FIREWORKS_SPARK, 0);
            world.playSound(loc, Sound.FIREWORK_LAUNCH, 1f, 1.2f);
            return;
        }

        if (selected.equalsIgnoreCase("victory_lightning")) {
            world.strikeLightningEffect(loc);
            world.playSound(loc, Sound.AMBIENCE_THUNDER, 0.9f, 1.3f);
            return;
        }

        if (selected.equalsIgnoreCase("victory_music")) {
            winner.playSound(loc, Sound.LEVEL_UP, 1f, 1.0f);
            winner.playSound(loc, Sound.NOTE_PLING, 1f, 1.6f);
            return;
        }

        if (selected.startsWith("seasonal_")) {
            if (selected.contains("halloween")) {
                world.playEffect(loc, Effect.MOBSPAWNER_FLAMES, 0);
                world.playSound(loc, Sound.WITHER_SPAWN, 0.45f, 1.7f);
            } else if (selected.contains("christmas")) {
                world.playEffect(loc, Effect.FIREWORKS_SPARK, 0);
                world.playEffect(loc.clone().add(0, 1, 0), Effect.SNOWBALL_BREAK, 0);
                world.playSound(loc, Sound.LEVEL_UP, 1f, 1.5f);
            } else if (selected.contains("easter")) {
                world.playEffect(loc, Effect.HAPPY_VILLAGER, 0);
                world.playSound(loc, Sound.CHICKEN_EGG_POP, 1f, 1.5f);
            } else {
                world.playEffect(loc, Effect.ENDER_SIGNAL, 0);
                world.playSound(loc, Sound.LEVEL_UP, 1f, 1.2f);
            }
        }
    }
}
