package me.spwtyz.murder.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class CustomHead implements Listener {
	
	List<String> lore = new ArrayList<>();
	
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        Inventory inventory = event.getInventory();

        if (inventory.getName().equals("Cosmeticos:")) {
            ItemStack playerHeadItem = getPlayerHead(player);
            int slot = 18;
            ItemStack currentItem = inventory.getItem(slot);

            if (currentItem != null && currentItem.getType() == Material.SKULL_ITEM && currentItem.getDurability() == 3) {
                SkullMeta meta = (SkullMeta) currentItem.getItemMeta();
                updateSkullMeta(player, meta);
                currentItem.setItemMeta(meta);
            } else {
                inventory.setItem(slot, playerHeadItem);
            }
        }
    }

    private ItemStack getPlayerHead(Player player) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        meta.setOwner(player.getName());
        updateSkullMeta(player, meta);

        head.setItemMeta(meta);
        return head;
    }

    private void updateSkullMeta(Player player, SkullMeta meta) {
        meta.setDisplayName("§aSeu Perfil");
        meta.setLore(Arrays.asList("§7Desative opcoes como players, seu chat", "§7e muitas outras opcoes."));
    }
}
