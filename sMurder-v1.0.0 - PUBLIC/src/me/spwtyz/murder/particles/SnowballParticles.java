package me.spwtyz.murder.particles;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import me.spwtyz.murder.Main;

public class SnowballParticles implements Listener {
	
	Main plugin;

    @EventHandler
    public void onSnowballLaunch(ProjectileLaunchEvent event) {
        Entity projectile = event.getEntity();
        if (projectile instanceof Snowball) {
            Snowball snowball = (Snowball) projectile;
            final World world = snowball.getWorld();
            final Vector direction = snowball.getVelocity();

            Bukkit.getScheduler().runTaskTimer(JavaPlugin.getPlugin(Main.class), new Runnable() {
                Location prevLocation = snowball.getLocation();

                @Override
                public void run() {
                    Location currentLocation = snowball.getLocation();
                    double distance = currentLocation.distance(prevLocation);

                    if (distance > 0.1) {

                        for (int i = 0; i < distance * 5; i++) {
                            Location particleLocation = prevLocation.add(direction);
                            world.playEffect(particleLocation, org.bukkit.Effect.SMOKE, 0);
                        }
                    }

                    prevLocation = currentLocation;
                }
            }, 0L, 1L);
        }
    }
            
            @EventHandler
            public void onArrowLaunch(ProjectileLaunchEvent event) {
                Entity projectile = event.getEntity();
                if (projectile instanceof Arrow) {
                    Arrow arrow = (Arrow) projectile;
                    final World world = arrow.getWorld();
                    final Vector direction = arrow.getVelocity();

                    Bukkit.getScheduler().runTaskTimer(JavaPlugin.getPlugin(Main.class), new Runnable() {
                        Location prevLocation = arrow.getLocation();

                        @Override
                        public void run() {
                            Location currentLocation = arrow.getLocation();
                            double distance = currentLocation.distance(prevLocation);

                            if (distance > 0.1) {

                                for (int i = 0; i < distance * 5; i++) {
                                    Location particleLocation = prevLocation.add(direction);
                                    world.playEffect(particleLocation, org.bukkit.Effect.HEART, 0);
                                    //world.playEffect(particleLocation, Effect.valueOf(plugin.settings.getConfig().getString("sword-particles.effect")), 0);
                                }
                            }

                            prevLocation = currentLocation;
                        }
                    }, 0L, 1L);
        }
    }
}

