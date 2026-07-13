package me.spwtyz.murder.npcs;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import me.spwtyz.murder.Main;
import me.spwtyz.murder.SmartInventory;
import me.spwtyz.murder.builder.CosmeticsMenu;

public class NPCManager implements Listener {

    private final Main plugin;
    private ArmorStand partidasNPC;
    private ArmorStand lojaNPC;

    public NPCManager(Main plugin) {
        this.plugin = plugin;
    }

    public void setNPC(String type, Location location) {
        if (location == null || location.getWorld() == null) return;

        String path = "npcs." + type + ".";
        plugin.data.getConfig().set(path + "world", location.getWorld().getName());
        plugin.data.getConfig().set(path + "x", location.getX());
        plugin.data.getConfig().set(path + "y", location.getY());
        plugin.data.getConfig().set(path + "z", location.getZ());
        plugin.data.getConfig().set(path + "yaw", location.getYaw());
        plugin.data.getConfig().set(path + "pitch", location.getPitch());
        plugin.data.save();

        spawnNPC(type, location);
    }

    public void spawnSavedNPCs() {
        spawnFromConfig("partidas");
        spawnFromConfig("loja");
    }

    private void spawnFromConfig(String type) {
        String path = "npcs." + type + ".";
        String worldName = plugin.data.getConfig().getString(path + "world");
        if (worldName == null || worldName.trim().isEmpty()) return;

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            Bukkit.getConsoleSender().sendMessage("§c[sMurder] Mundo do NPC não carregado: " + worldName + " | NPC: " + type);
            return;
        }

        Location location = new Location(
                world,
                plugin.data.getConfig().getDouble(path + "x"),
                plugin.data.getConfig().getDouble(path + "y"),
                plugin.data.getConfig().getDouble(path + "z"),
                (float) plugin.data.getConfig().getDouble(path + "yaw"),
                (float) plugin.data.getConfig().getDouble(path + "pitch")
        );

        spawnNPC(type, location);
    }

    private void spawnNPC(String type, Location location) {
        removeNPC(type);

        ArmorStand stand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        stand.setVisible(true);
        stand.setGravity(false);
        stand.setCanPickupItems(false);
        stand.setCustomNameVisible(true);

        if (type.equalsIgnoreCase("partidas")) {
            stand.setCustomName(ChatColor.GREEN + "Partidas " + ChatColor.GRAY + "(Clique)");
            stand.setHelmet(new org.bukkit.inventory.ItemStack(Material.COMPASS));
            partidasNPC = stand;
        } else {
            stand.setCustomName(ChatColor.YELLOW + "Loja " + ChatColor.GRAY + "(Clique)");
            stand.setHelmet(new org.bukkit.inventory.ItemStack(Material.EMERALD));
            lojaNPC = stand;
        }
    }

    private void removeNPC(String type) {
        ArmorStand current = type.equalsIgnoreCase("partidas") ? partidasNPC : lojaNPC;
        if (current != null && !current.isDead()) {
            current.remove();
        }

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof ArmorStand)) continue;
                ArmorStand stand = (ArmorStand) entity;
                if (stand.getCustomName() == null) continue;
                String name = ChatColor.stripColor(stand.getCustomName());
                if (type.equalsIgnoreCase("partidas") && name.startsWith("Partidas")) {
                    stand.remove();
                }
                if (type.equalsIgnoreCase("loja") && name.startsWith("Loja")) {
                    stand.remove();
                }
            }
        }
    }

    @EventHandler
    public void onNPCClick(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand)) return;

        ArmorStand stand = (ArmorStand) event.getRightClicked();
        if (stand.getCustomName() == null) return;

        String name = ChatColor.stripColor(stand.getCustomName());
        Player player = event.getPlayer();

        if (name.startsWith("Partidas")) {
            event.setCancelled(true);
            if (!plugin.sd.containsKey(player.getName())) {
                SmartInventory si = new SmartInventory(player);
                plugin.sd.put(player.getName(), si);
            }
            plugin.sd.get(player.getName()).openInventory();
            return;
        }

        if (name.startsWith("Loja")) {
            event.setCancelled(true);
            new CosmeticsMenu(plugin).openMainMenu(player);
        }
    }
}
