package me.spwtyz.murder.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpeedKit extends Kit {

    public SpeedKit() {
        super("Speed", Material.SUGAR, KitType.INNOCENT);
    }

    @Override
    public void apply(Player p) {

        p.addPotionEffect(
            new PotionEffect(
                PotionEffectType.SPEED,
                Integer.MAX_VALUE,
                0
            )
        );
    }

    @Override
    public void remove(Player p) {

        p.removePotionEffect(PotionEffectType.SPEED);
    }
}