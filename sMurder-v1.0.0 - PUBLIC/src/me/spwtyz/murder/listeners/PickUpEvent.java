package me.spwtyz.murder.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import me.spwtyz.murder.Arena;
import me.spwtyz.murder.Arenas;
import me.spwtyz.murder.GameState;
import me.spwtyz.murder.GameModeType;
import me.spwtyz.murder.Main;
import me.spwtyz.murder.PlayerType;
import me.spwtyz.murder.Utils;
import me.spwtyz.murder.events.TitleAPI;

@SuppressWarnings("deprecation")
public class PickUpEvent implements Listener {
	Main plugin;
	
	 private boolean allowPickup = false; // Variável para controlar se o jogador pode pegar itens

	public PickUpEvent(Main plugin) {
		this.plugin = plugin;
	}

    private boolean isDetectiveGun(ItemStack item) {
        if (item == null) return false;
        return plugin != null && plugin.gunSkinManager != null ? plugin.gunSkinManager.isGun(item) : item.getType() == Material.DIAMOND_HOE;
    }

    private boolean canPickupDetectiveGun(Arena a, Player player) {
        return a != null
                && a.getState() == GameState.INGAME
                && !a.specs.contains(player)
                && (a.getType(player) == PlayerType.Innocents || a.getType(player) == PlayerType.Detective);
    }

    private void giveDetectiveGun(Arena a, Player player, ItemStack stack) {
        ItemStack gun = stack == null ? new ItemStack(Material.DIAMOND_HOE) : stack.clone();
        gun.setAmount(1);
        player.getInventory().addItem(gun);
        player.updateInventory();

        if (plugin.settings.getConfig().getBoolean("enable-sounds")) {
            try {
                player.playSound(player.getLocation(), Sound.valueOf(plugin.settings.getConfig().getString("PICK_UP")), 1, 1);
            } catch (Exception ignored) {
                player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 1, 1);
            }
        }

        for (Player b : a.getInnocents()) {
            plugin.removeCompass(b);
        }
        for (Player xc : a.getPlayers()) {
            xc.sendMessage(Utils.FormatText(xc, plugin.messages.getConfig().getString("pickup-bow")));
        }
    }

    @EventHandler
    public void onMoveNearDroppedDetectiveGun(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Arena a = Arenas.getArena(player);
        if (!canPickupDetectiveGun(a, player)) return;

        for (Entity ent : player.getNearbyEntities(1.7D, 1.7D, 1.7D)) {
            if (ent instanceof Item) {
                Item dropped = (Item) ent;
                if (dropped.getItemStack() != null && isDetectiveGun(dropped.getItemStack())) {
                    giveDetectiveGun(a, player, dropped.getItemStack());
                    dropped.remove();
                    return;
                }
            }
            if (ent instanceof ArmorStand) {
                ArmorStand stand = (ArmorStand) ent;
                if (stand.getItemInHand() != null && isDetectiveGun(stand.getItemInHand())) {
                    giveDetectiveGun(a, player, stand.getItemInHand());
                    stand.remove();
                    return;
                }
            }
        }
    }
	
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
	    Player player = event.getPlayer();
	    ItemStack item = event.getItem().getItemStack();
	    
	    if (item.getType() == Material.IRON_INGOT) {
	        event.setCancelled(true); 
	        
	        
	        event.getItem().remove();
	        
	        
	        player.getInventory().setItem(8, item);
	    }
	}
	
	@EventHandler
	public void onPlayerPickupItemGun(PlayerPickupItemEvent event) {
	    Player player = event.getPlayer();
	    ItemStack pickupItem = event.getItem().getItemStack();
	    Arena a = Arenas.getArena(player);
	    if (a == null) return;
	    if (a.getGameMode() == GameModeType.TNT_TAG || a.getGameMode() == GameModeType.ALL_MURDER) {
	        if (pickupItem.getType() == Material.IRON_INGOT) {
	            event.setCancelled(true);
	            event.getItem().remove();
	        }
	        return;
	    }

	    if (a.getType(player) == PlayerType.Innocents) {
	        // Verifica se o jogador coletou uma iron ingot com o nome "Fragmentos"
	        if (pickupItem.getType() == Material.IRON_INGOT && pickupItem.hasItemMeta() && pickupItem.getItemMeta().hasDisplayName() && pickupItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Fragmentos")) {
	            int ironIngotCount = 0;

	            // Conta quantas iron ingots o jogador possui no inventário
	            for (ItemStack item : player.getInventory().getContents()) {
	                if (item != null && item.getType() == Material.IRON_INGOT && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Fragmentos")) {
	                    ironIngotCount += item.getAmount();
	                }
	            }

	            // Verifica se o jogador possui pelo menos 10 iron ingots
	            if (ironIngotCount >= 10) {
	                // Adiciona uma diamond_hoe ao inventário do jogador
	                ItemStack diamondHoe = new ItemStack(Material.DIAMOND_HOE);
	                ItemMeta meta = diamondHoe.getItemMeta();
	                meta.setDisplayName("§6§lMURDER> §aFive Seven");
	                List<String> lore = new ArrayList<>();
	                lore.add("");
	                lore.add("§eEficiencia: §72");
	                lore.add("§eTempo: §75 Segundos");
	                meta.setLore(lore);
	                diamondHoe.setItemMeta(meta);
	                player.getInventory().addItem(diamondHoe);

	                // Informa ao jogador que ele obteve a diamond_hoe (opcional)
	                player.sendMessage(ChatColor.GREEN + "Você obteve uma Five Seven!");
	            }
	        }
	    }
	}

	 

	@EventHandler
	public void onPickup(PlayerPickupItemEvent e) {
		if (Arenas.isInArena(e.getPlayer())) {
			Arena a = Arenas.getArena(e.getPlayer());
			// Arma do detetive dropada no chão: inocentes podem pegar durante a partida.
			if (a != null && a.getState() == GameState.INGAME && isDetectiveGun(e.getItem().getItemStack())) {
				Player player = e.getPlayer();
				if (canPickupDetectiveGun(a, player)) {
					e.setCancelled(true);
					giveDetectiveGun(a, player, e.getItem().getItemStack());
					e.getItem().remove();
					return;
				}
				e.setCancelled(true);
				return;
			}
			if (a != null && (a.getGameMode() == GameModeType.TNT_TAG || a.getGameMode() == GameModeType.ALL_MURDER)) {
				if (e.getItem().getItemStack().getType() == Material.IRON_INGOT) {
					e.setCancelled(true);
					e.getItem().remove();
				}
				return;
			}
			if (a != null && (a.getGameMode() == GameModeType.TNT_TAG || a.getGameMode() == GameModeType.ALL_MURDER)) {
				if (e.getItem().getItemStack().getType() == Material.IRON_INGOT ||
						e.getItem().getItemStack().getType() == Material.getMaterial(plugin.settings.getConfig().getInt("dropped-item-id"))) {
					e.setCancelled(true);
					e.getItem().remove();
				}
				return;
			}
			if (a.getState() != GameState.INGAME) {
				e.setCancelled(true);
			}
			if (a.specs.contains(e.getPlayer())) {
				e.setCancelled(true);
				return;
			}
			if (e.getItem().getItemStack().getType() == Material
					.getMaterial(plugin.settings.getConfig().getInt("dropped-item-id"))) {
				if (a.getState() != GameState.INGAME) {
					return;
				}
		            // Verifique se o jogador tem permissão para pegar o iron ingot
		            if (allowPickup) {
		                // Execute a lógica apenas se o jogador pegou a iron ingot
		                // Sua lógica para adicionar pontos e exibir mensagens
		                a.addscore(e.getPlayer(), plugin.settings.getConfig().getInt("score-on-gold"),
		                        Utils.FormatText(e.getPlayer(), plugin.messages.getConfig().getString("gold-reason")));

		                if (a.getType(e.getPlayer()) == PlayerType.Innocents) {
		                    if (a.golds.contains(e.getItem())) {
		                        a.golds.remove(e.getItem());
		                    }
		                }
		            }
		            
			        if (e.getItem().getItemStack().getType() == Material.IRON_INGOT) {
			            // Verifique se o jogador tem permissão para pegar o iron ingot
			            if (allowPickup) {
				
					if (plugin.settings.getConfig().getBoolean("enable-sounds")) {
						e.getPlayer().playSound(e.getPlayer().getLocation(),
								Sound.valueOf(plugin.settings.getConfig().getString("PICK_UP")), 1, 1);
					}
			     }
			  }
					


					new BukkitRunnable() {

						private Player p;

						@Override
						public void run() {

							if (e.getPlayer().getInventory().containsAtLeast(
									new ItemStack(Material
											.getMaterial(plugin.settings.getConfig().getInt("dropped-item-id"))),
									plugin.settings.getConfig().getInt("gold-amount-to-get-bow"))) {
								e.getPlayer().getInventory()
										.removeItem(new ItemStack(
												Material.getMaterial(
														plugin.settings.getConfig().getInt("dropped-item-id")),
												plugin.settings.getConfig().getInt("gold-amount-to-get-bow")));

								if (!e.getPlayer().getInventory().contains(new ItemStack(Material.DIAMOND_HOE))
										&& !e.getPlayer().getInventory().contains(Material.DIAMOND_HOE)) {
									e.getPlayer().getInventory().addItem(plugin.gunSkinManager != null ? plugin.gunSkinManager.createGun(e.getPlayer()) : new ItemStack(Material.DIAMOND_HOE));
								}

								//e.getPlayer().getInventory().addItem(new ItemStack(Material.ARROW, 1));

								TitleAPI.sendTitle(e.getPlayer(), 0, 40, 0, Utils.FormatText(e.getPlayer(), plugin.messages.getConfig()
										.getString("you-have-bow-title")));
								TitleAPI.sendSubtitle(e.getPlayer(), 0, 40, 0, Utils.FormatText(e.getPlayer(), plugin.messages.getConfig()
										.getString("you-have-bow-subtitle")));
								e.getPlayer().updateInventory();
								p = null;
								p.setFoodLevel(4);
							}

						}
					}.runTaskLater(plugin, 20);
				}
			}
		}
	}


