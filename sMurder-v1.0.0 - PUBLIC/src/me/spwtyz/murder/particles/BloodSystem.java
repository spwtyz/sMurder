package me.spwtyz.murder.particles;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.spwtyz.murder.Arenas;

public class BloodSystem implements Listener {

    private JavaPlugin plugin;

    public BloodSystem(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        
        Location deathLocation = event.getEntity().getLocation();

        if (Arenas.isInArena(event.getEntity())) {
            for (int i = 0; i < 10; i++) { 
                createBloodDrop(deathLocation);
            }
        }
    }

    public void createBloodDrop(Location location) {
        
        double radius = 1.0;
        double angle = Math.random() * 360; 

        double x = location.getX() + radius * Math.cos(Math.toRadians(angle));
        double z = location.getZ() + radius * Math.sin(Math.toRadians(angle));

        Location dropLocation = new Location(location.getWorld(), x, location.getY(), z);

        ItemStack bloodItem = new ItemStack(Material.INK_SACK, 1, (short) 1); 

        Item itemEntity = location.getWorld().dropItem(dropLocation, bloodItem);
        itemEntity.setVelocity(new Vector(0, 0.1, 0)); 

        
        itemEntity.setPickupDelay(Integer.MAX_VALUE);

        
        new BukkitRunnable() {
            @Override
            public void run() {
                itemEntity.remove();
            }
        }.runTaskLater(plugin, 100L);
    }
}

