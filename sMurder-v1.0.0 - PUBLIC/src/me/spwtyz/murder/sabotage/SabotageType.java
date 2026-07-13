package me.spwtyz.murder.sabotage;

import org.bukkit.Material;

public enum SabotageType {
    LIGHTS("Lights", Material.REDSTONE_TORCH_ON, 45),
    REACTOR("Reactor", Material.REDSTONE_COMPARATOR, 60),
    O2("O2", Material.LEAVES, 60),
    COMMUNICATIONS("Communications", Material.NOTE_BLOCK, 45),
    DOORS("Doors", Material.IRON_DOOR, 50),
    ELECTRICAL("Electrical", Material.REDSTONE, 40);

    private final String displayName;
    private final Material material;
    private final int cooldownSeconds;

    SabotageType(String displayName, Material material, int cooldownSeconds) {
        this.displayName = displayName;
        this.material = material;
        this.cooldownSeconds = cooldownSeconds;
    }

    public String getDisplayName() { return displayName; }
    public Material getMaterial() { return material; }
    public int getCooldownSeconds() { return cooldownSeconds; }
}
