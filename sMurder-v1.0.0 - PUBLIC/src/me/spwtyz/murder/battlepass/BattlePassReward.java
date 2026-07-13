
package me.spwtyz.murder.battlepass;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BattlePassReward {

    private final int tier;
    private final boolean premium;
    private final BattlePassRewardType type;
    private final String name;
    private final int amount;

    public BattlePassReward(int tier, boolean premium, BattlePassRewardType type, String name, int amount) {
        this.tier = tier;
        this.premium = premium;
        this.type = type;
        this.name = name;
        this.amount = amount;
    }

    public ItemStack toItem(Player p, BattlePassManager manager) {
        Material mat = premium ? Material.GOLD_INGOT : Material.IRON_INGOT;

        if (type == BattlePassRewardType.MYSTERY_BOX) mat = Material.CHEST;
        if (type == BattlePassRewardType.COSMETIC) mat = Material.DIAMOND;
        if (type == BattlePassRewardType.XP) mat = Material.EXP_BOTTLE;

        boolean unlocked = manager.getTier(p) >= tier;
        boolean blockedPremium = premium && !manager.hasPremium(p);

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName((premium ? "§6Premium " : "§aFree ") + "§7Tier " + tier + " §8- §f" + name);
        meta.setLore(Arrays.asList(
                "§7Tipo: §f" + type.name(),
                "§7Status: " + (blockedPremium ? "§cPremium necessário" : unlocked ? "§aDisponível" : "§cBloqueado"),
                "",
                unlocked && !blockedPremium ? "§aClique para resgatar!" : "§7Jogue para liberar."
        ));

        item.setItemMeta(meta);
        return item;
    }

    public int getTier() {
        return tier;
    }

    public boolean isPremium() {
        return premium;
    }

    public BattlePassRewardType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public int getAmount() {
        return amount;
    }
}
