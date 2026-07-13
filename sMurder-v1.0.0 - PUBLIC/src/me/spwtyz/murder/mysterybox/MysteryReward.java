
package me.spwtyz.murder.mysterybox;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MysteryReward {

    private final String id;
    private final String name;
    private final MysteryRewardType type;
    private final MysteryRarity rarity;
    private final Material icon;

    public MysteryReward(String id, String name, MysteryRewardType type, MysteryRarity rarity, Material icon) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.rarity = rarity;
        this.icon = icon;
    }

    public ItemStack toItem(boolean finalReward) {
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(getColoredName());
        meta.setLore(Arrays.asList(
                "§7Tipo: §f" + type.name(),
                "§7Raridade: " + rarity.getColor() + rarity.getDisplay(),
                "",
                finalReward ? "§aRecompensa recebida!" : "§7Sorteando..."
        ));
        item.setItemMeta(meta);
        return item;
    }

    public String getColoredName() {
        return rarity.getColor() + name;
    }

    public String getId() {
        return id;
    }

    public MysteryRewardType getType() {
        return type;
    }

    public MysteryRarity getRarity() {
        return rarity;
    }
}
