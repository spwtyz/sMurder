package me.spwtyz.murder.builder;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.spwtyz.murder.listeners.ItemBuilder;

public class HatsMenu implements Listener {
	   
	        
	//@EventHandler
	//public void onInteract1(PlayerInteractEvent e) {
		
		//ItemStack chestHat = new ItemStack(Material.CHEST);
		
	    //if (!e.getPlayer().isOp()) {
	       // e.setCancelled(true);
	        //return;
	    //}

	        //}
	
	//private static Inventory hatsInventory;
	
	@EventHandler
	public void aoClicar3(org.bukkit.event.inventory.InventoryClickEvent e) {
		if (e.getInventory().getTitle().equalsIgnoreCase("Cosmeticos:")) {
			//e.setCancelled(true);
			if (e.getCurrentItem() == null)
				return;

			if (e.getRawSlot() == 10) {
				Player p = (Player) e.getWhoClicked();
	            if (p != null) {  // Check if the Player object is not null
	            	Inventory hatsInventory = createHatsInventory(p);

	                if (hatsInventory != null) {
	                    p.openInventory(hatsInventory);
			   }
		   }
	    }
    }  
}
				
		
	        public static Inventory createHatsInventory(Player p) {
	            Inventory hatsInventory = Bukkit.createInventory(null , 4 * 9 , "Hats:");


	        hatsInventory.setItem(19, new ItemBuilder(Material.TNT)
	                .name("§cTNT")
	                .lore("§7Encontrado nas Caixas:",
	                        "§7Caixas Misteriosas do Murder.", "&a", "&ePreco: &70", "&eRaridade: &7Normal", "&a", "&aClique para selecionar")
	                .removeAttributes()
	                .build());
            //p.getInventory().setHelmet(new ItemStack(Material.TNT));
            //p.closeInventory();
            //p.sendMessage("&eSelecionado §cTNT");
            //p.getWorld().playSound(p.getLocation(), Sound.LEVEL_UP, 1.0F, 1.0F);


	        hatsInventory.setItem(20, new ItemBuilder(Material.SPONGE)
	                .name("§eESPONJA")
	                .lore("§7Encontrado nas Caixas:",
	                        "§7Caixas Misteriosas do Murder.", "&a", "&ePreco: &70", "&eRaridade: &7Normal", "&a", "&aClique para selecionar")
	                .removeAttributes()
	                .build());
            //p.getInventory().setHelmet(new ItemStack(Material.SPONGE));
            //p.closeInventory();
            //p.sendMessage("&eSelecionado §eESPONJA");
            //p.getWorld().playSound(p.getLocation(), Sound.LEVEL_UP, 1.0F, 1.0F);

	        hatsInventory.setItem(21, new ItemBuilder(Material.BEACON)
	                .name("§bASTRONALTA")
	                .lore("§7Encontrado nas Caixas:",
	                        "§7Caixas Misteriosas do Murder.", "&a", "&ePreco: &70", "&eRaridade: &7Normal", "&a", "&aClique para selecionar")
	                .removeAttributes()
	                .build());
            //p.getInventory().setHelmet(new ItemStack(Material.BEACON));
            //p.closeInventory();
            //p.sendMessage("&eSelecionado §bASTRONALTA");
            //p.getWorld().playSound(p.getLocation(), Sound.LEVEL_UP, 1.0F, 1.0F);
	        
	        hatsInventory.setItem(22, new ItemBuilder(Material.CHEST)
	                .name("§6BAU")
	                .lore("§7Encontrado nas Caixas:",
                        "§7Caixas Misteriosas do Murder.", "&a", "&ePreco: &70", "&eRaridade: &7Normal", "&a", "&aClique para selecionar")
	                .removeAttributes()
	                .build());
	         //p.getInventory().setHelmet(new ItemStack(Material.CHEST));
	         //p.closeInventory();
	         //p.sendMessage("&eSelecionado §6BAU");
	         //p.getWorld().playSound(p.getLocation(), Sound.LEVEL_UP, 1.0F, 1.0F);
	         
	         //// Gera um ID único para o chapéu
	         //String hatId = "tnt_hat_" + System.currentTimeMillis(); // Cria um ID usando a hora atual (pode ser qualquer string única)
	         //tntHat.getItemMeta().getPersistentDataContainer().set(Bukkit.getPluginManager().getPlugin("SeuPlugin"), hatId, new FixedMetadataValue(plugin.getPlugin(HatsMenu.class), true));

	        p.openInventory(hatsInventory);
	        return hatsInventory;
	    }
	        
			@EventHandler
			public void aoClicar5(org.bukkit.event.inventory.InventoryClickEvent e) {
				if (e.getInventory().getTitle().equalsIgnoreCase("Hats:")) {
					e.setCancelled(true);
					if (e.getCurrentItem() == null)
						return;
					
					if (e.getRawSlot() == 19) {
						org.bukkit.entity.Player p = (org.bukkit.entity.Player) e.getWhoClicked();
						p.closeInventory();
						
				        ItemStack tnt = new ItemStack(Material.TNT);
				        p.sendMessage("&eSelecionado §cTNT");
				        p.getWorld().playSound(p.getLocation(), Sound.LEVEL_UP, 1.0F, 1.0F);
				        p.getInventory().setHelmet(tnt);					        
				        
					}
					
					if (e.getRawSlot() == 20) {
						org.bukkit.entity.Player p = (org.bukkit.entity.Player) e.getWhoClicked();
						p.closeInventory();
						
				        ItemStack sponge = new ItemStack(Material.SPONGE);
				        p.sendMessage("§aVoce selecionou o capacete §eESPONJA!");
				        p.getWorld().playSound(p.getLocation(), Sound.LEVEL_UP, 1.0F, 1.0F);
				        p.getInventory().setHelmet(sponge);
				        												        																																					    								
					}
					
					if (e.getRawSlot() == 21) {
						org.bukkit.entity.Player p = (org.bukkit.entity.Player) e.getWhoClicked();
						p.closeInventory();
						
				        ItemStack beacon = new ItemStack(Material.BEACON);
				        p.sendMessage("§aVoce selecionou o capacete §bASTRONALTA!");
				        p.getWorld().playSound(p.getLocation(), Sound.LEVEL_UP, 1.0F, 1.0F);
				        p.getInventory().setHelmet(beacon);
				        												        																																					    								
					}
					
					if (e.getRawSlot() == 22) {
						org.bukkit.entity.Player p = (org.bukkit.entity.Player) e.getWhoClicked();
						p.closeInventory();
						
				        ItemStack chest = new ItemStack(Material.CHEST);
				        p.sendMessage("§aVoce selecionou o capacete §6BAU!");
				        p.getWorld().playSound(p.getLocation(), Sound.LEVEL_UP, 1.0F, 1.0F);
				        p.getInventory().setHelmet(chest);
				        												        																																					    								
					}
					
				}
			}   

	        
	        @EventHandler
	        public void onInventoryClick(InventoryClickEvent event) {	        	
	            if (event.getInventory().getTitle().equals("Hats:")) {
	            	
	                event.setCancelled(true);
	            }
	        }
        }
	   
	        
	       // @EventHandler
	        //public void onArmorUnequip(InventoryClickEvent event) {
	           // int slot = event.getSlot();
	            //if ((slot >= 5 && slot <= 8) || (slot >= 6 && slot <= 9) || (slot >= 7 && slot <= 10) || (slot >= 8 && slot <= 11)) {
	                //event.setCancelled(true);
	                //event.getWhoClicked().closeInventory(); // Fecha o inventário para evitar interações indesejadas
	            //}
	        //}
	   // }
		
	        




