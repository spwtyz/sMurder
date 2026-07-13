package me.spwtyz.murder.guns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.spwtyz.murder.Main;

public class GunSkinManager {

    public enum GunSkin {
        FIVE_SEVEN("five_seven", "§6§lMURDER> §aFive Seven", Material.DIAMOND_HOE, 0, 0, 1.5D, 3, true, "§7Arma padrão do Detetive."),
        REVOLVER("revolver", "§6§lMURDER> §5Revólver Sombrio", Material.IRON_HOE, 0, 2500, 1.65D, 3, false, "§7Mais pesado, porém com tiro um pouco mais rápido."),
        SHERIFF("sheriff", "§6§lMURDER> §eSheriff Dourada", Material.GOLD_HOE, 0, 5000, 1.8D, 4, false, "§7Tiro forte, cooldown maior."),
        PHANTOM("phantom", "§6§lMURDER> §dPhantom", Material.STONE_HOE, 0, 8000, 1.75D, 2, false, "§7Arma épica com cooldown menor."),

        // Armas sazonais. Aparecem na loja quando o evento esta ativo ou se o player ja comprou.
        HALLOWEEN_SOUL("seasonal_halloween_gun", "§6Soul Revolver", Material.IRON_HOE, 0, 10000, 1.85D, 3, false, "§7Arma exclusiva de Halloween."),
        CHRISTMAS_SNOW("seasonal_christmas_gun", "§cSnow Blaster", Material.DIAMOND_HOE, 0, 10000, 1.75D, 2, false, "§7Arma exclusiva de Natal."),
        EASTER_BLASTER("seasonal_easter_gun", "§dEaster Blaster", Material.GOLD_HOE, 0, 10000, 1.80D, 3, false, "§7Arma exclusiva de Pascoa."),
        CUSTOM_EVENT("seasonal_custom_gun", "§bEvent Blaster", Material.STONE_HOE, 0, 10000, 1.80D, 3, false, "§7Arma exclusiva do evento custom.");

        private final String id;
        private final String display;
        private final Material material;
        private final short data;
        private final int price;
        private final double velocity;
        private final int cooldown;
        private final boolean free;
        private final String description;

        GunSkin(String id, String display, Material material, int data, int price, double velocity, int cooldown, boolean free, String description) {
            this.id = id;
            this.display = display;
            this.material = material;
            this.data = (short) data;
            this.price = price;
            this.velocity = velocity;
            this.cooldown = cooldown;
            this.free = free;
            this.description = description;
        }

        public String getId() { return id; }
        public String getDisplay() { return display; }
        public Material getMaterial() { return material; }
        public short getData() { return data; }
        public int getPrice() { return price; }
        public double getVelocity() { return velocity; }
        public int getCooldown() { return cooldown; }
        public boolean isFree() { return free; }
        public String getDescription() { return description; }
    }

    private final Main plugin;
    private final Map<UUID, GunSkin> selected = new HashMap<UUID, GunSkin>();
    private final Map<UUID, Set<String>> owned = new HashMap<UUID, Set<String>>();

    public GunSkinManager(Main plugin) {
        this.plugin = plugin;
    }

    public List<GunSkin> getSkins() { return Arrays.asList(GunSkin.values()); }

    public boolean isSeasonalSkin(GunSkin skin) {
        return skin != null && skin.getId().toLowerCase().startsWith("seasonal_");
    }

    public String getSeasonalEventId(GunSkin skin) {
        if (skin == null) return "";
        String id = skin.getId().toLowerCase();
        if (id.contains("halloween")) return "halloween";
        if (id.contains("christmas")) return "christmas";
        if (id.contains("easter")) return "easter";
        if (id.contains("custom")) return "custom";
        return "";
    }

    public GunSkin getSkinById(String id) {
        if (id == null) return null;
        for (GunSkin skin : GunSkin.values()) if (skin.getId().equalsIgnoreCase(id)) return skin;
        return null;
    }

    private void load(Player p) {
        if (p == null) return;
        UUID uuid = p.getUniqueId();
        if (!selected.containsKey(uuid)) {
            String id = plugin.data != null ? plugin.data.getConfig().getString("GunData." + uuid + ".selected", GunSkin.FIVE_SEVEN.getId()) : GunSkin.FIVE_SEVEN.getId();
            GunSkin skin = getSkinById(id);
            selected.put(uuid, skin == null ? GunSkin.FIVE_SEVEN : skin);
        }
        if (!owned.containsKey(uuid)) {
            Set<String> set = new HashSet<String>();
            if (plugin.data != null) set.addAll(plugin.data.getConfig().getStringList("GunData." + uuid + ".owned"));
            set.add(GunSkin.FIVE_SEVEN.getId());
            owned.put(uuid, set);
        }
    }

    private void save(Player p) {
        if (p == null || plugin.data == null) return;
        UUID uuid = p.getUniqueId();
        plugin.data.getConfig().set("GunData." + uuid + ".selected", getSelected(p).getId());
        plugin.data.getConfig().set("GunData." + uuid + ".owned", new ArrayList<String>(ownedSet(p)));
        plugin.data.save();
    }

    private Set<String> ownedSet(Player p) {
        load(p);
        return owned.get(p.getUniqueId());
    }

    public GunSkin getSelected(Player p) {
        load(p);
        GunSkin skin = selected.get(p.getUniqueId());
        return skin == null ? GunSkin.FIVE_SEVEN : skin;
    }

    public void setSelected(Player p, GunSkin skin) {
        load(p);
        selected.put(p.getUniqueId(), skin == null ? GunSkin.FIVE_SEVEN : skin);
        save(p);
    }

    public boolean owns(Player p, GunSkin skin) {
        return skin == null || skin.isFree() || ownedSet(p).contains(skin.getId());
    }

    public void unlock(Player p, GunSkin skin) {
        if (skin == null) return;
        ownedSet(p).add(skin.getId());
        save(p);
    }

    public ItemStack createGun(Player p) {
        GunSkin skin = getSelected(p);
        ItemStack item = new ItemStack(skin.getMaterial(), 1, skin.getData());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(skin.getDisplay());
        List<String> lore = new ArrayList<String>();
        lore.add("§8gun:" + skin.getId());
        lore.add("");
        lore.add("§eForça: §7" + String.format(java.util.Locale.US, "%.1f", skin.getVelocity()));
        lore.add("§eTempo: §7" + skin.getCooldown() + " Segundos");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public boolean isGun(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        for (GunSkin skin : GunSkin.values()) if (item.getType() == skin.getMaterial()) return true;
        return false;
    }

    public GunSkin getSkinFromItem(ItemStack item) {
        if (item == null) return GunSkin.FIVE_SEVEN;
        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            for (String line : item.getItemMeta().getLore()) {
                if (line != null && line.startsWith("§8gun:")) {
                    GunSkin skin = getSkinById(line.replace("§8gun:", ""));
                    if (skin != null) return skin;
                }
            }
        }
        for (GunSkin skin : GunSkin.values()) if (item.getType() == skin.getMaterial()) return skin;
        return GunSkin.FIVE_SEVEN;
    }

    public List<String> buildLore(Player p, GunSkin skin) {
        List<String> lore = new ArrayList<String>();
        lore.add(skin.getDescription());
        lore.add("§7Força: §f" + String.format(java.util.Locale.US, "%.1f", skin.getVelocity()));
        lore.add("§7Cooldown: §f" + skin.getCooldown() + "s");
        lore.add(" ");
        if (owns(p, skin)) {
            lore.add(getSelected(p) == skin ? "§a§lEQUIPADA" : "§eClique para equipar.");
        } else {
            lore.add("§7Preço: §e" + skin.getPrice() + " coins");
            lore.add("§eClique para comprar.");
        }
        return lore;
    }
}
