package me.spwtyz.murder.builder;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class ItemEvent implements Listener {
	

	@EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        //Player player = event.getPlayer();
        Block block = event.getBlockPlaced();

        // Check if the placed block is a chest
        if (block.getType() == Material.CHEST) {
            BlockState state = block.getState();

            // Check if the block state implements InventoryHolder (usually for chests)
            if (state instanceof InventoryHolder) {
                InventoryHolder inventoryHolder = (InventoryHolder) state;

                // Check the inventory holder's inventory and its display name
                if (inventoryHolder.getInventory().getName().equalsIgnoreCase("§eCosmeticos §7(Clique)")) {
                    event.setCancelled(true); // Cancel the placement
                    //player.sendMessage("You can't place a chest with the name 'bukkit'!");
                }
            }
        }
	}
        
    	//@EventHandler
        //public void onBlockPlace2(BlockPlaceEvent event) {
            //Player player = event.getPlayer();
            //Block block = event.getBlockPlaced();

            // Check if the placed block is a chest
            //if (block.getType() == Material.SKULL_ITEM) {
               // BlockState state = block.getState();

                // Check if the block state implements InventoryHolder (usually for chests)
                //if (state instanceof InventoryHolder) {
                    //InventoryHolder inventoryHolder = (InventoryHolder) state;

                    // Check the inventory holder's inventory and its display name
                    //if (inventoryHolder.getInventory().getName().equalsIgnoreCase("§bPerfil §7(Clique)")) {
                       // event.setCancelled(true); // Cancel the placement
                        //player.sendMessage("You can't place a chest with the name 'bukkit'!");
                    //}
                //}
            //}
        //}
    
    
        
    	//@EventHandler
    	//public void dropEvent(PlayerDropItemEvent e) {
    		//Player p = e.getPlayer();
    		//if(p.getInventory().getHeldItemSlot() == 0) { 
    			//e.setCancelled(true);
    			//p.updateInventory();
    		//}
    	//}
    	
     // BUSSOLA
    	
        @EventHandler
        public void onPlayerDropItemCompass(PlayerDropItemEvent event) {
            Player player = event.getPlayer();
            ItemStack droppedItem = event.getItemDrop().getItemStack();
            
            // Verifica se o item descartado é uma bússola
            if (isCompass(droppedItem)) { 
                event.setCancelled(true);
                player.updateInventory();
            }
        }
        
        // Verifica se o item é uma bússola
        private boolean isCompass(ItemStack item) {
            return item != null && item.getType().name().contains("COMPASS");
        }
        
      // FINAL BUSSOLA  

    	//@EventHandler
    	//public void dropEvent1(PlayerDropItemEvent e) {
    		//Player p = e.getPlayer();
    		//if(p.getInventory().getHeldItemSlot() == 4) {
    			//e.setCancelled(true);
    			//p.updateInventory();
    		//}
    	//}
        
       // CHEST
        
        @EventHandler
        public void onPlayerDropItemChest(PlayerDropItemEvent event) {
            Player player = event.getPlayer();
            ItemStack droppedItem = event.getItemDrop().getItemStack();
            
            // Verifica se o item descartado é um baú
            if (isChest(droppedItem)) { 
                event.setCancelled(true);
                player.updateInventory();
            }
        }
        
        // Verifica se o item é um baú
        private boolean isChest(ItemStack item) {
            return item != null && item.getType().name().contains("CHEST");
        }
        
        
       // FINAL CHEST
    	
    	//@EventHandler
    	//public void dropEvent2(PlayerDropItemEvent e) {
    		//Player p = e.getPlayer();
    		//if(p.getInventory().getHeldItemSlot() == 1) {
    			//e.setCancelled(true);
    			//p.updateInventory();
    		//}
    	//} 
    	            


    	@EventHandler(priority = EventPriority.LOWEST) 
    	public void onMoveCompass(InventoryClickEvent e){
    	    ItemStack i = e.getWhoClicked().getInventory().getItem(0);
    	    if(i != null)
    	    {
    	    	
    	        if(e.getSlot() == 0 && i.getType() == Material.COMPASS)
    	        {
    	            e.setCancelled(true);
    	        }
    	    }
    	}

    	@EventHandler(priority = EventPriority.LOWEST) 
    	public void onMoveChest(InventoryClickEvent e){
    	    ItemStack i = e.getWhoClicked().getInventory().getItem(4);
    	    if(i != null)
    	    {
    	        if(e.getSlot() == 4 && i.getType() == Material.CHEST)
    	        {
    	            e.setCancelled(true);
    	        }
    	    }
    	}
    	
    	//@EventHandler(priority = EventPriority.LOWEST) 
    	//public void onMove2(InventoryClickEvent e){
    	    //ItemStack i = e.getWhoClicked().getInventory().getItem(1);
    	    //if(i != null)
    	   // {
    	       // if(e.getSlot() == 1 && i.getType() == Material.SKULL_ITEM)
    	        //{
    	           // e.setCancelled(true);
    	        //}
    	    //}
    	//}


    			@EventHandler
    			public void aoClicar1(org.bukkit.event.inventory.InventoryClickEvent e) {
    				if (e.getInventory().getTitle().equalsIgnoreCase("Cosmeticos:")) {
    					e.setCancelled(true);
    					if (e.getCurrentItem() == null)
    						return;

    					if (e.getRawSlot() == 22) {
    						org.bukkit.entity.Player p = (org.bukkit.entity.Player) e.getWhoClicked();
    						//p.sendMessage("");
    						p.closeInventory();
    						p.chat("/cmenu");
    					}
    				}
    			}
           }
    						
    		   // @EventHandler
    		    //public void aoClicar2(InventoryClickEvent e) {
    		        //if (e.getInventory().getTitle().equalsIgnoreCase("Cosmeticos:")) {
    		           // e.setCancelled(true);
    		           //if (e.getCurrentItem() == null || e.getCurrentItem().getType() != Material.SKULL_ITEM)
    		                //return;

    		            //SkullMeta skullMeta = (SkullMeta) e.getCurrentItem().getItemMeta();
    		            //if (skullMeta == null || !skullMeta.hasDisplayName())
    		                //return;

    		            //if (skullMeta.getDisplayName().equals(ChatColor.AQUA + "Perfil" + ChatColor.GRAY + " (Clique)")) {
    		                //org.bukkit.entity.Player p = (org.bukkit.entity.Player) e.getWhoClicked();
    		                // Coloque aqui o código que você quer executar quando o jogador clicar na skull com o nome correto
    		                // Por exemplo:
    		               // p.chat("/profile");
    		               // p.closeInventory();
    		                //p.openInventory(HatsMenu.createHatsInventory(p));
    		   //}
    	   //}    				
