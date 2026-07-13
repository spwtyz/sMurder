package me.spwtyz.murder.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import me.spwtyz.murder.Arena;
import me.spwtyz.murder.Arenas;
import me.spwtyz.murder.Main;

public class SpectateEvent implements Listener {
    Main plugin;

    public SpectateEvent(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();

        if (Arenas.isInArena(p)) {
            Arena arena = Arenas.getArena(p);
            if (arena != null && arena.specs.contains(p)) {
                e.setCancelled(true);
            }
        }

        if (e.getInventory() == null || e.getInventory().getTitle() == null) return;
        if (!e.getInventory().getTitle().equalsIgnoreCase(plugin.settings.getConfig().getString("spectate-inventory-title"))) return;

        e.setCancelled(true);
        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;
        if (!e.getCurrentItem().hasItemMeta() || !e.getCurrentItem().getItemMeta().hasDisplayName()) return;

        String name = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
        Player target = Bukkit.getPlayerExact(name);
        if (target == null) {
            if (plugin.spectatorManager != null) plugin.spectatorManager.openTeleportMenu(p);
            return;
        }
        p.teleport(target.getLocation());
        p.sendMessage("§aTeleportado para §f" + target.getName() + "§a.");
        if (Arenas.isInArena(p)) {
            Arena arena = Arenas.getArena(p);
            if (arena != null) arena.refreshVisibility();
        }
    }
}
