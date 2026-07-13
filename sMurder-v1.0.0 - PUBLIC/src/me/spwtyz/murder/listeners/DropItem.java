package me.spwtyz.murder.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.spwtyz.murder.Arenas;
import me.spwtyz.murder.Main;

public class DropItem implements Listener {

	Main plugin;

	public DropItem(Main plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent e) {
	    Player player = e.getPlayer();
	    if (plugin.mysteryBoxManager != null && plugin.mysteryBoxManager.isOpening(player)) {
	        e.setCancelled(true);
	        return;
	    }
	    if (isProtectedLobbyItem(e.getItemDrop().getItemStack())) {
	        e.setCancelled(true);
	        return;
	    }
	    if (Arenas.isInArena(player)) {
	        ItemStack item = e.getItemDrop().getItemStack();
	        
	        // Verifica se o item é um iron ingot com o nome "Fragmentos"
	        if (item.getType() == Material.IRON_INGOT && item.hasItemMeta()) {
	            ItemMeta meta = item.getItemMeta();
	            if (meta.hasDisplayName() && meta.getDisplayName().equals("§eFragmentos")) {
	                // Permite o drop do iron ingot com nome "Fragmentos"
	                return;
	            }
	        }
	        
	        // Cancela o drop de outros itens
	        e.setCancelled(true);
	    }
	    
	    
	//@EventHandler
	//public void onDrop(PlayerDropItemEvent e) {
		//Player player = e.getPlayer();
		//if (Arenas.isInArena(e.getPlayer())) {
			//e.setCancelled(true);
			
		if (plugin.getConfig().getBoolean("bungee") && !e.getPlayer().isOp()
				&& !e.getPlayer().hasPermission("murder.admin")) {
			e.setCancelled(true);
		}
	}
	
    @EventHandler
    public void onPlayerDropItemFalso(PlayerDropItemEvent event) {
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        if (isFakeItem(droppedItem)) {
            event.setCancelled(true);
        }
    }

    private boolean isFakeItem(ItemStack item) {
        if (item != null && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasDisplayName() && meta.getDisplayName().equals(ChatColor.YELLOW + "Item Falso")) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onLobbyInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        if (plugin.mysteryBoxManager != null && plugin.mysteryBoxManager.isOpening(p)) {
            e.setCancelled(true);
            return;
        }
        if (Arenas.isInArena(p)) return;
        if (isProtectedLobbyItem(e.getCurrentItem()) || isProtectedLobbyItem(e.getCursor())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onLobbyInventoryDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        if (plugin.mysteryBoxManager != null && plugin.mysteryBoxManager.isOpening(p)) {
            e.setCancelled(true);
            return;
        }
        if (Arenas.isInArena(p)) return;
        for (ItemStack item : e.getNewItems().values()) {
            if (isProtectedLobbyItem(item)) {
                e.setCancelled(true);
                return;
            }
        }
    }

    private boolean isProtectedLobbyItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName()).toLowerCase();
        if (name.contains("partidas")) return true;
        if (name.contains("loja")) return true;
        if (name.contains("perfil")) return true;
        if (name.contains("esconder")) return true;
        if (name.contains("lobby")) return true;
        if (name.contains("gadget")) return true;
        if (name.contains("battle pass")) return true;
        if (name.contains("sair")) return true;
        return false;
    }

}
