package me.spwtyz.murder.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import me.spwtyz.murder.Arena;
import me.spwtyz.murder.Arenas;
import me.spwtyz.murder.Main;

public class SpectatorItem implements Listener {
    Main plugin;

    public SpectatorItem(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void InteractItem(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!Arenas.isInArena(p)) return;
        Arena a = Arenas.getArena(p);
        if (a == null || !a.specs.contains(p)) return;

        e.setCancelled(true);
        p.setAllowFlight(true);
        p.setFlying(true);
        p.updateInventory();

        if (!e.getAction().name().toLowerCase().contains("right")) return;
        ItemStack hand = p.getItemInHand();
        if (hand == null || hand.getType() == Material.AIR) return;

        if (hand.getType() == Material.COMPASS) {
            if (plugin.spectatorManager != null) plugin.spectatorManager.openTeleportMenu(p);
            return;
        }
        if (hand.getType() == Material.WATCH) {
            if (plugin.spectatorManager != null) plugin.spectatorManager.teleportRandom(p);
            return;
        }
        if (hand.getType() == Material.FEATHER) {
            p.setAllowFlight(true);
            p.setFlying(true);
            p.sendMessage("§bFly de espectador ativado.");
            return;
        }
    }
}
