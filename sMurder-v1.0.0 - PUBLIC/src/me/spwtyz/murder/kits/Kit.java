package me.spwtyz.murder.kits;


import org.bukkit.Material;
import org.bukkit.entity.Player;

public abstract class Kit {

    private String name;
    private KitType type;
    private Material icon;

    public Kit(String name, Material icon, KitType type) {
        this.name = name;
        this.icon = icon;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public KitType getType() {
        return type;
    }

    public Material getIcon() {
        return icon;
    }

    public abstract void apply(Player p);

    public void remove(Player p) {
        p.getInventory().clear();
        p.getActivePotionEffects()
                .forEach(e -> p.removePotionEffect(e.getType()));
    }
}