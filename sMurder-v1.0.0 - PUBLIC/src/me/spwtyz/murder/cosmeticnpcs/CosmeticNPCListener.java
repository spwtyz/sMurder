package me.spwtyz.murder.cosmeticnpcs;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import me.spwtyz.murder.Main;

public class CosmeticNPCListener implements Listener {

    private final Main plugin;

    public CosmeticNPCListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteractAt(PlayerInteractAtEntityEvent e) {
        handleEntityClick(e.getPlayer(), e.getRightClicked());
        if (plugin.cosmeticNPCManager != null && plugin.cosmeticNPCManager.isBattlePassNPC(e.getRightClicked())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent e) {
        handleEntityClick(e.getPlayer(), e.getRightClicked());
        if (plugin.cosmeticNPCManager != null && plugin.cosmeticNPCManager.isBattlePassNPC(e.getRightClicked())) {
            e.setCancelled(true);
        }
    }

    private void handleEntityClick(Player p, Entity clicked) {
        if (plugin.cosmeticNPCManager == null) return;

        if (plugin.cosmeticNPCManager.isBattlePassNPC(clicked)) {
            if (plugin.battlePassManager != null) {
                plugin.battlePassManager.openMenu(p);
            }
        }
    }

    @EventHandler
    public void onChestClick(PlayerInteractEvent e) {
        if (plugin.cosmeticNPCManager == null) return;
        if (e.getClickedBlock() == null) return;

        Block block = e.getClickedBlock();
        if (block.getType() != Material.CHEST) return;
        if (!plugin.cosmeticNPCManager.isMysteryBoxChest(block)) return;

        e.setCancelled(true);
        Player p = e.getPlayer();
        if (plugin.mysteryBoxManager != null) {
            plugin.mysteryBoxManager.openMainMenu(p);
        }
    }

    @EventHandler
    public void onBreakBox(BlockBreakEvent e) {
        if (plugin.cosmeticNPCManager == null) return;
        if (plugin.cosmeticNPCManager.isMysteryBoxChest(e.getBlock())) {
            if (!e.getPlayer().hasPermission("murder.admin") && !e.getPlayer().isOp()) {
                e.setCancelled(true);
                e.getPlayer().sendMessage("§cVocê não pode quebrar a Mystery Box.");
            }
        }
    }

    @EventHandler
    public void onDamageNPC(EntityDamageByEntityEvent e) {
        if (plugin.cosmeticNPCManager == null) return;
        if (plugin.cosmeticNPCManager.isBattlePassNPC(e.getEntity())) {
            e.setCancelled(true);
        }
    }
}
