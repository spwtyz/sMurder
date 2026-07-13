package me.spwtyz.murder.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class AdvancedKit extends Kit {

    private final String id;
    private final String description;

    public AdvancedKit(String id, String name, Material icon, KitType type, String description) {
        super(name, icon, type);
        this.id = id;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public void apply(Player p) {
        if (p == null) return;


        if (id.equalsIgnoreCase("chance_murder") || id.equalsIgnoreCase("chance_detective")) {
            // Kit de chance atua antes do sorteio da partida em Arena.SetUp().
            return;
        }

        if (id.equalsIgnoreCase("teleporter")) {
            ItemStack pearl = new ItemStack(Material.ENDER_PEARL, 1);
            org.bukkit.inventory.meta.ItemMeta meta = pearl.getItemMeta();
            meta.setDisplayName("§aTeleportador do Kit");
            java.util.List<String> lore = new java.util.ArrayList<String>();
            lore.add("§7Uso único.");
            lore.add("§7Teleporta para um spawn aleatório do mapa.");
            meta.setLore(lore);
            pearl.setItemMeta(meta);
            p.getInventory().addItem(pearl);
            msg(p, "Teleporter");
            return;
        }

        if (id.equalsIgnoreCase("ninja")) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
            p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 0));
            p.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 1));
            msg(p, "Ninja");
            return;
        }

        if (id.equalsIgnoreCase("medic")) {
            p.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 1));
            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 8, 0));
            msg(p, "Médico");
            return;
        }

        if (id.equalsIgnoreCase("tracker")) {
            p.getInventory().addItem(new ItemStack(Material.COMPASS, 1));
            p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0));
            msg(p, "Rastreador");
            return;
        }

        if (id.equalsIgnoreCase("tank")) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 45, 0));
            msg(p, "Tank");
            return;
        }

        if (id.equalsIgnoreCase("acrobat")) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 1));
            p.getInventory().addItem(new ItemStack(Material.FEATHER, 1));
            msg(p, "Acrobata");
            return;
        }

        if (id.equalsIgnoreCase("ghost")) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 12, 0));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 12, 0));
            msg(p, "Fantasma");
            return;
        }

        if (id.equalsIgnoreCase("trapster")) {
            p.getInventory().addItem(new ItemStack(Material.WEB, 3));
            msg(p, "Trapster");
            return;
        }

        if (id.equalsIgnoreCase("detective_plus")) {
            p.getInventory().addItem(new ItemStack(Material.ARROW, 2));
            p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0));
            msg(p, "Detective+");
            return;
        }

        if (id.equalsIgnoreCase("assassin")) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 60, 0));
            p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20 * 60, 0));
            msg(p, "Assassino");
            return;
        }

        if (id.equalsIgnoreCase("vampire")) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 15, 0));
            p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0));
            msg(p, "Vampiro");
            return;
        }


        if (id.equalsIgnoreCase("hide_runner")) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
            msg(p, "Corredor do Esconde-Esconde");
            return;
        }

        if (id.equalsIgnoreCase("hide_camouflage")) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 6, 0));
            msg(p, "Camuflagem");
            return;
        }

        if (id.equalsIgnoreCase("tnt_void_saver")) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 12, 0));
            msg(p, "Proteção TNTTag");
            return;
        }

        if (id.equalsIgnoreCase("tnt_sprinter")) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
            msg(p, "Velocista TNTTag");
            return;
        }

        if (id.equalsIgnoreCase("sabotage_engineer")) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 0));
            p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0));
            msg(p, "Engenheiro");
            return;
        }

        if (id.equalsIgnoreCase("sabotage_detective")) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0));
            msg(p, "Perito");
            return;
        }
        if (id.equalsIgnoreCase("shadow_step")) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 8, 0));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 8, 1));
            msg(p, "Shadow Step");
            return;
        }
    }

    private void msg(Player p, String kit) {
        p.sendMessage("§6§lMURDER §7> §aKit aplicado: §f" + kit + "§a.");
    }
}
