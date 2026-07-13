package me.spwtyz.murder.kits;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;

import me.spwtyz.murder.Arena;
import me.spwtyz.murder.Main;
import me.spwtyz.murder.rooms.RoomFeatureLockManager;

public class MenuManager implements Listener {
	
    private Main plugin;

    public MenuManager(Main plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onKitMenuClick(InventoryClickEvent e) {

        Player p = (Player) e.getWhoClicked();

        if (!e.getView().getTitle().equals("Selecionar Kit")) return;

        e.setCancelled(true);

        ItemStack item = e.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;

        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());

        for (Kit kit : plugin.kitManager.getAllKits()) {

            if (kit.getName().equalsIgnoreCase(name)) {

                if (RoomFeatureLockManager.isKitDisabled(plugin, kit)) {
                    p.sendMessage("§cEsse kit está desativado pela staff no momento.");
                    p.closeInventory();
                    return;
                }

                if (!plugin.kitManager.ownsKit(p, kit)) {
                    int price = plugin.kitManager.getPrice(kit);
                    if (plugin.api == null || !plugin.api.removeCoins(p, price)) {
                        p.sendMessage("§cVocê precisa de §e" + price + " coins §cpara comprar esse kit.");
                        p.closeInventory();
                        return;
                    }
                    plugin.kitManager.unlockKit(p, kit);
                    p.sendMessage("§aKit comprado: §f" + kit.getName() + " §7(-" + price + " coins)");
                }

            	plugin.kitManager.setSelectedKit(p, kit);

                p.sendMessage("§aKit selecionado: " + kit.getName());
                p.closeInventory();
                return;
            }
        }
    }
	
	public void openKitMenu(Player p, Arena arena) {

	    Inventory inv = Bukkit.createInventory(
	        null,
	        27,
	        "Selecionar Kit"
	    );

	    //PlayerType role = arena.getType(p);

	    for (Kit kit : plugin.kitManager.getAvailableKits(
	            p,
	            arena.getGameMode())) {

            if (RoomFeatureLockManager.isKitDisabled(plugin, kit)) continue;

	        ItemStack item = new ItemStack(kit.getIcon());

	        ItemMeta meta = item.getItemMeta();

	        meta.setDisplayName("§a" + kit.getName());

	        List<String> lore = new ArrayList<String>();
	        lore.add("§7Tipo: " + plugin.kitManager.getTypeDisplay(kit));
	        if (kit instanceof AdvancedKit) {
	            lore.add("§7" + ((AdvancedKit) kit).getDescription());
	        } else {
	            lore.add("§7Kit básico do Murder.");
	        }
	        lore.add("");
            if (plugin.kitManager.ownsKit(p, kit)) {
                lore.add("§aComprado");
                lore.add(plugin.kitManager.getSelectedKit(p) == kit ? "§a§lEQUIPADO" : "§eClique para selecionar.");
            } else {
                lore.add("§7Preço: §e" + plugin.kitManager.getPrice(kit) + " coins");
                lore.add("§eClique para comprar.");
            }
	        meta.setLore(lore);

	        item.setItemMeta(meta);

	        inv.addItem(item);
	    }

	    p.openInventory(inv);
	}

}
