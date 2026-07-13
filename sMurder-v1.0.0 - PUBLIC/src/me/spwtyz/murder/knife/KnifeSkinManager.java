package me.spwtyz.murder.knife;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.spwtyz.murder.Main;
import me.spwtyz.murder.Utils;
import me.spwtyz.murder.api.MurderAPI;

public class KnifeSkinManager {

    public enum KnifeTrail {
        NENHUM("nenhum", "§7Sem Efeito", Material.BARRIER, 0, ""),
        BARREIRA("barreira", "§cBarreira", Material.BARRIER, 500, "BARRIER"),
        HEART("heart", "§dCorações", Material.RED_ROSE, 750, "HEART"),
        CLOUD("cloud", "§fNuvem", Material.WEB, 750, "CLOUD");

        private final String id;
        private final String displayName;
        private final Material material;
        private final int price;
        private final String effectName;

        KnifeTrail(String id, String displayName, Material material, int price, String effectName) {
            this.id = id;
            this.displayName = displayName;
            this.material = material;
            this.price = price;
            this.effectName = effectName;
        }

        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public Material getMaterial() { return material; }
        public int getPrice() { return price; }
        public String getEffectName() { return effectName; }
    }

    public enum KnifeSkin {
        PADRAO("padrao", "§6§lMURDER> §bFaca §7(§a1§7)", Material.IRON_SWORD, 0, 0, 1, 5, 5, 5, true),
        MADEIRA("madeira", "§6Faca de Madeira", Material.WOOD_SWORD, 0, 500, 1, 3, 3, 3, false),
        PEDRA("pedra", "§7Faca de Pedra", Material.STONE_SWORD, 0, 1000, 1, 4, 4, 4, false),
        OURO("ouro", "§eFaca Dourada", Material.GOLD_SWORD, 0, 2500, 2, 7, 4, 6, false),
        FERRO("ferro", "§fFaca de Ferro", Material.IRON_SWORD, 0, 3500, 2, 5, 6, 5, false),
        DIAMANTE("diamante", "§bFaca de Diamante", Material.DIAMOND_SWORD, 0, 8000, 3, 6, 8, 10, false),
        MACHADO("machado", "§cMachado Assassino", Material.DIAMOND_AXE, 0, 10000, 4, 4, 9, 8, false),
        FISH("Peixe", "§cPeixe Matador", Material.RAW_FISH, 0, 20000, 4, 4, 9, 8, false),
        CARROT("Cenoura", "§cCenourita", Material.CARROT, 0, 30000, 4, 4, 9, 8, false),
        GOLDEN_CARROT("Cenoura Dourada", "§cCenoura Dourada", Material.GOLDEN_CARROT, 0, 40000, 4, 4, 9, 8, false),

        // Facas sazonais: aparecem na loja somente quando o evento esta ativo
        // ou quando o jogador ja comprou. A compra fica salva permanentemente.
        HALLOWEEN_SCYTHE("seasonal_halloween_knife", "§6Foice Sombria", Material.DIAMOND_SPADE, 0, 12000, 5, 7, 8, 9, false),
        CHRISTMAS_CANDY("seasonal_christmas_knife", "§cCandy Cane Knife", Material.GOLD_SPADE, 0, 12000, 5, 8, 7, 8, false),
        EASTER_EGG("seasonal_easter_knife", "§dEgg Blade", Material.STONE_SPADE, 0, 12000, 5, 7, 8, 8, false),
        CUSTOM_EVENT("seasonal_custom_knife", "§bEvent Knife", Material.IRON_SPADE, 0, 12000, 5, 7, 7, 9, false);

        private final String id;
        private final String displayName;
        private final Material material;
        private final short data;
        private final int price;
        private final int level;
        private final int agility;
        private final int precision;
        private final int speed;
        private final boolean free;

        KnifeSkin(String id, String displayName, Material material, int data, int price, int level, int agility, int precision, int speed, boolean free) {
            this.id = id;
            this.displayName = displayName;
            this.material = material;
            this.data = (short) data;
            this.price = price;
            this.level = level;
            this.agility = agility;
            this.precision = precision;
            this.speed = speed;
            this.free = free;
        }

        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public Material getMaterial() { return material; }
        public short getData() { return data; }
        public int getPrice() { return price; }
        public int getLevel() { return level; }
        public int getAgility() { return agility; }
        public int getPrecision() { return precision; }
        public int getSpeed() { return speed; }
        public boolean isFree() { return free; }
    }

    private final Map<UUID, KnifeSkin> selected = new HashMap<>();
    private final Map<UUID, KnifeTrail> selectedTrail = new HashMap<>();
    private final Map<UUID, Set<String>> ownedSkins = new HashMap<>();
    private final Map<UUID, Set<String>> ownedTrails = new HashMap<>();
    private final Map<UUID, Map<String, Integer>> agilityUpgrades = new HashMap<>();
    private final Map<UUID, Map<String, Integer>> precisionUpgrades = new HashMap<>();
    private final Map<UUID, Map<String, Integer>> speedUpgrades = new HashMap<>();
    private final Random random = new Random();
    private Main plugin;

    public KnifeSkinManager() {
    }

    public KnifeSkinManager(Main plugin) {
        this.plugin = plugin;
    }

    public List<KnifeSkin> getSkins() {
        return Arrays.asList(KnifeSkin.values());
    }

    public boolean isSeasonalSkin(KnifeSkin skin) {
        return skin != null && skin.getId().toLowerCase().startsWith("seasonal_");
    }

    public String getSeasonalEventId(KnifeSkin skin) {
        if (skin == null) return "";
        String id = skin.getId().toLowerCase();
        if (id.contains("halloween")) return "halloween";
        if (id.contains("christmas")) return "christmas";
        if (id.contains("easter")) return "easter";
        if (id.contains("custom")) return "custom";
        return "";
    }

    public List<KnifeTrail> getTrails() {
        return Arrays.asList(KnifeTrail.values());
    }

    public KnifeSkin getSkinById(String id) {
        if (id == null) return null;
        for (KnifeSkin skin : KnifeSkin.values()) {
            if (skin.getId().equalsIgnoreCase(id)) return skin;
        }
        return null;
    }

    private String sqlEscape(String value) {
        return value == null ? "" : value.replace("'", "''");
    }

    private void savePlayerKnifeData(Player p) {
        if (p == null || plugin == null) return;
        UUID uuid = p.getUniqueId();

        KnifeSkin skin = selected.containsKey(uuid) ? selected.get(uuid) : KnifeSkin.PADRAO;
        KnifeTrail trail = selectedTrail.containsKey(uuid) ? selectedTrail.get(uuid) : KnifeTrail.NENHUM;

        if (plugin.data != null) {
            String base = "KnifeData." + uuid + ".";
            plugin.data.getConfig().set(base + "selectedSkin", skin.getId());
            plugin.data.getConfig().set(base + "selectedTrail", trail.getId());
            plugin.data.getConfig().set(base + "ownedSkins", new ArrayList<String>(skinSet(p)));
            plugin.data.getConfig().set(base + "ownedTrails", new ArrayList<String>(trailSet(p)));
            plugin.data.save();
        }

        if (plugin.getConfig().getBoolean("mysql") && plugin.sql != null) {
            String ownedSkinString = String.join(",", skinSet(p));
            String ownedTrailString = String.join(",", trailSet(p));
            plugin.sql.update("INSERT INTO MurderKnifeData (uuid, selected_skin, selected_trail, owned_skins, owned_trails) VALUES ('"
                    + uuid.toString() + "', '" + sqlEscape(skin.getId()) + "', '" + sqlEscape(trail.getId()) + "', '"
                    + sqlEscape(ownedSkinString) + "', '" + sqlEscape(ownedTrailString) + "') "
                    + "ON DUPLICATE KEY UPDATE selected_skin='" + sqlEscape(skin.getId()) + "', selected_trail='"
                    + sqlEscape(trail.getId()) + "', owned_skins='" + sqlEscape(ownedSkinString) + "', owned_trails='"
                    + sqlEscape(ownedTrailString) + "';");
        }
    }

    private void loadPlayerKnifeData(Player p) {
        if (p == null || plugin == null || plugin.data == null) return;
        UUID uuid = p.getUniqueId();
        String base = "KnifeData." + uuid + ".";

        if (!selected.containsKey(uuid)) {
            KnifeSkin skin = getSkinById(plugin.data.getConfig().getString(base + "selectedSkin", KnifeSkin.PADRAO.getId()));
            selected.put(uuid, skin == null ? KnifeSkin.PADRAO : skin);
        }

        if (!selectedTrail.containsKey(uuid)) {
            KnifeTrail trail = getTrailById(plugin.data.getConfig().getString(base + "selectedTrail", KnifeTrail.NENHUM.getId()));
            selectedTrail.put(uuid, trail == null ? KnifeTrail.NENHUM : trail);
        }

        if (!ownedSkins.containsKey(uuid)) {
            Set<String> set = new HashSet<String>(plugin.data.getConfig().getStringList(base + "ownedSkins"));
            set.add(KnifeSkin.PADRAO.getId());
            ownedSkins.put(uuid, set);
        }

        if (!ownedTrails.containsKey(uuid)) {
            Set<String> set = new HashSet<String>(plugin.data.getConfig().getStringList(base + "ownedTrails"));
            set.add(KnifeTrail.NENHUM.getId());
            ownedTrails.put(uuid, set);
        }
    }

    public KnifeTrail getTrailById(String id) {
        if (id == null) return null;
        for (KnifeTrail trail : KnifeTrail.values()) {
            if (trail.getId().equalsIgnoreCase(id)) return trail;
        }
        return null;
    }

    private int getStoredUpgrade(Player p, KnifeSkin skin, String attr, Map<UUID, Map<String, Integer>> cache) {
        UUID uuid = p.getUniqueId();
        if (!cache.containsKey(uuid)) cache.put(uuid, new HashMap<String, Integer>());
        Map<String, Integer> map = cache.get(uuid);
        if (!map.containsKey(skin.getId())) {
            int value = 0;
            if (plugin != null && plugin.data != null) {
                value = plugin.data.getConfig().getInt("KnifeUpgrades." + uuid + "." + skin.getId() + "." + attr, 0);
            }
            map.put(skin.getId(), value);
        }
        return map.get(skin.getId());
    }

    private void setStoredUpgrade(Player p, KnifeSkin skin, String attr, int value, Map<UUID, Map<String, Integer>> cache) {
        UUID uuid = p.getUniqueId();
        if (!cache.containsKey(uuid)) cache.put(uuid, new HashMap<String, Integer>());
        cache.get(uuid).put(skin.getId(), value);
        if (plugin != null && plugin.data != null) {
            plugin.data.getConfig().set("KnifeUpgrades." + uuid + "." + skin.getId() + "." + attr, value);
            plugin.data.save();
        }
        if (plugin != null && plugin.getConfig().getBoolean("mysql") && plugin.sql != null) {
            plugin.sql.update("INSERT INTO MurderKnifeUpgrades (uuid, skin_id, attr, value) VALUES ('"
                    + uuid.toString() + "', '" + sqlEscape(skin.getId()) + "', '" + sqlEscape(attr) + "', " + value + ") "
                    + "ON DUPLICATE KEY UPDATE value=" + value + ";");
        }
    }

    public int getAgility(Player p, KnifeSkin skin) {
        return Math.min(10, skin.getAgility() + getStoredUpgrade(p, skin, "agility", agilityUpgrades));
    }

    public int getPrecision(Player p, KnifeSkin skin) {
        return Math.min(10, skin.getPrecision() + getStoredUpgrade(p, skin, "precision", precisionUpgrades));
    }

    public int getSpeed(Player p, KnifeSkin skin) {
        return Math.min(10, skin.getSpeed() + getStoredUpgrade(p, skin, "speed", speedUpgrades));
    }

    public int getUpgradeCost(Player p, KnifeSkin skin, String attr) {
        int current = 0;
        if ("agility".equalsIgnoreCase(attr)) current = getStoredUpgrade(p, skin, "agility", agilityUpgrades);
        if ("precision".equalsIgnoreCase(attr)) current = getStoredUpgrade(p, skin, "precision", precisionUpgrades);
        if ("speed".equalsIgnoreCase(attr)) current = getStoredUpgrade(p, skin, "speed", speedUpgrades);
        return 500 + (current * 350);
    }

    public boolean upgradeAttribute(Player p, KnifeSkin skin, String attr, MurderAPI api) {
        if (p == null || skin == null || attr == null || api == null) return false;
        if (!ownsSkin(p, skin)) {
            p.sendMessage("§cVocê precisa comprar essa faca primeiro!");
            return false;
        }

        int currentValue;
        Map<UUID, Map<String, Integer>> cache;
        String path;

        if ("agility".equalsIgnoreCase(attr)) {
            currentValue = getAgility(p, skin);
            cache = agilityUpgrades;
            path = "agility";
        } else if ("precision".equalsIgnoreCase(attr)) {
            currentValue = getPrecision(p, skin);
            cache = precisionUpgrades;
            path = "precision";
        } else if ("speed".equalsIgnoreCase(attr)) {
            currentValue = getSpeed(p, skin);
            cache = speedUpgrades;
            path = "speed";
        } else {
            return false;
        }

        if (currentValue >= 10) {
            p.sendMessage("§cEsse atributo já está no nível máximo.");
            return false;
        }

        int cost = getUpgradeCost(p, skin, path);
        if (api.getCoins(p) < cost) {
            p.sendMessage("§cVocê precisa de " + cost + " coins para melhorar esse atributo.");
            return false;
        }

        api.removeCoins(p, cost);
        int stored = getStoredUpgrade(p, skin, path, cache);
        setStoredUpgrade(p, skin, path, stored + 1, cache);
        return true;
    }

    public List<String> buildUpgradeLore(Player p, KnifeSkin skin, String attr) {
        List<String> lore = new ArrayList<String>();
        int value = 0;
        String label = "";
        if ("agility".equalsIgnoreCase(attr)) { value = getAgility(p, skin); label = "Agilidade"; }
        if ("precision".equalsIgnoreCase(attr)) { value = getPrecision(p, skin); label = "Precisão"; }
        if ("speed".equalsIgnoreCase(attr)) { value = getSpeed(p, skin); label = "Velocidade"; }
        lore.add("§7" + label + ": " + bar(value, 10, "§a"));
        lore.add(" ");
        if (value >= 10) {
            lore.add("§aNível máximo.");
        } else {
            lore.add("§7Custo: §a" + getUpgradeCost(p, skin, attr) + " coins");
            lore.add("§eClique para melhorar.");
        }
        return lore;
    }

    public KnifeSkin getSelected(Player p) {
        loadPlayerKnifeData(p);
        KnifeSkin skin = selected.get(p.getUniqueId());
        return skin == null ? KnifeSkin.PADRAO : skin;
    }

    public void setSelected(Player p, KnifeSkin skin) {
        loadPlayerKnifeData(p);
        selected.put(p.getUniqueId(), skin == null ? KnifeSkin.PADRAO : skin);
        savePlayerKnifeData(p);
    }

    public KnifeTrail getSelectedTrail(Player p) {
        loadPlayerKnifeData(p);
        KnifeTrail trail = selectedTrail.get(p.getUniqueId());
        return trail == null ? KnifeTrail.NENHUM : trail;
    }

    public void setSelectedTrail(Player p, KnifeTrail trail) {
        loadPlayerKnifeData(p);
        selectedTrail.put(p.getUniqueId(), trail == null ? KnifeTrail.NENHUM : trail);
        savePlayerKnifeData(p);
    }

    private Set<String> skinSet(Player p) {
        loadPlayerKnifeData(p);
        UUID id = p.getUniqueId();
        if (!ownedSkins.containsKey(id)) ownedSkins.put(id, new HashSet<String>());
        ownedSkins.get(id).add(KnifeSkin.PADRAO.getId());
        return ownedSkins.get(id);
    }

    private Set<String> trailSet(Player p) {
        loadPlayerKnifeData(p);
        UUID id = p.getUniqueId();
        if (!ownedTrails.containsKey(id)) ownedTrails.put(id, new HashSet<String>());
        ownedTrails.get(id).add(KnifeTrail.NENHUM.getId());
        return ownedTrails.get(id);
    }

    public boolean ownsSkin(Player p, KnifeSkin skin) {
        return skin == null || skin.isFree() || skinSet(p).contains(skin.getId());
    }

    public void unlockSkin(Player p, KnifeSkin skin) {
        if (skin != null) {
            skinSet(p).add(skin.getId());
            savePlayerKnifeData(p);
        }
    }

    public boolean ownsTrail(Player p, KnifeTrail trail) {
        return trail == null || trail == KnifeTrail.NENHUM || trailSet(p).contains(trail.getId());
    }

    public void unlockTrail(Player p, KnifeTrail trail) {
        if (trail != null) {
            trailSet(p).add(trail.getId());
            savePlayerKnifeData(p);
        }
    }

    public long getCooldownTicks(Player p) {
        KnifeSkin skin = getSelected(p);
        long cooldown = 220L - (getAgility(p, skin) * 15L);
        return Math.max(60L, cooldown);
    }

    public double getVelocityMultiplier(Player p) {
        KnifeSkin skin = getSelected(p);
        return 1.2D + (getSpeed(p, skin) * 0.12D);
    }

    public double getSpread(Player p) {
        KnifeSkin skin = getSelected(p);
        double spread = (10 - getPrecision(p, skin)) * 0.012D;
        return Math.max(0D, spread);
    }

    public double randomSpread() {
        return (random.nextDouble() - 0.5D);
    }

    public ItemStack createKnife(Player p) {
        KnifeSkin skin = getSelected(p);

        if (skin == KnifeSkin.PADRAO && plugin != null) {
            Material mat = Material.getMaterial(plugin.settings.getConfig().getInt("murderer-weapon.item-id"));
            if (mat != null) {
                ItemStack item = new ItemStack(mat, 1, (short) plugin.settings.getConfig().getInt("murderer-weapon.item-subid"));
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(Utils.FormatText(p, plugin.settings.getConfig().getString("murderer-weapon.item-name")));
                List<String> lore = new ArrayList<String>();
                lore.add("§8skin:" + skin.getId());
                meta.setLore(lore);
                item.setItemMeta(meta);
                return item;
            }
        }

        ItemStack item = new ItemStack(skin.getMaterial(), 1, skin.getData());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(skin.getDisplayName());
        List<String> lore = new ArrayList<String>();
        lore.add("§8skin:" + skin.getId());
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public List<String> buildSkinLore(Player p, KnifeSkin skin) {
        List<String> lore = new ArrayList<String>();
        lore.add("§7Nível: " + bar(skin.getLevel(), 5, "§a"));
        lore.add("§7Agilidade: " + bar(getAgility(p, skin), 10, "§c"));
        lore.add("§7Precisão: " + bar(getPrecision(p, skin), 10, "§c"));
        lore.add("§7Velocidade: " + bar(getSpeed(p, skin), 10, "§c"));
        lore.add(" ");
        if (ownsSkin(p, skin)) {
            lore.add(getSelected(p) == skin ? "§aSelecionada" : "§eClique esquerdo para selecionar.");
            lore.add("§bClique direito para melhorar atributos.");
        } else {
            lore.add("§7Preço: §a" + skin.getPrice() + " moedas.");
            lore.add("§eClique para comprar.");
        }
        return lore;
    }

    public List<String> buildTrailLore(Player p, KnifeTrail trail) {
        List<String> lore = new ArrayList<String>();
        lore.add("§7Efeito visual da faca arremessada.");
        lore.add(" ");
        if (ownsTrail(p, trail)) {
            lore.add(getSelectedTrail(p) == trail ? "§aSelecionado" : "§eClique para selecionar.");
        } else {
            lore.add("§7Preço: §a" + trail.getPrice() + " moedas.");
            lore.add("§eClique para comprar.");
        }
        return lore;
    }

    private double getVelocityForSkin(KnifeSkin skin) {
        return 1.2D + (skin.getSpeed() * 0.12D);
    }

    private String bar(int value, int max, String color) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= max; i++) {
            sb.append(i <= value ? color + "▌" : "§8▌");
        }
        return sb.toString();
    }

    public void playTrail(Player owner, org.bukkit.Location loc) {
        KnifeTrail trail = getSelectedTrail(owner);
        if (trail == KnifeTrail.NENHUM) return;

        Effect effect = getEffect(trail);
        try {
            loc.getWorld().playEffect(loc, effect, 0);
        } catch (Throwable ignored) {
            loc.getWorld().playEffect(loc, Effect.CRIT, 0);
        }
    }

    private Effect getEffect(KnifeTrail trail) {
        if (trail == null || trail.getEffectName() == null || trail.getEffectName().isEmpty()) return Effect.CRIT;
        try {
            return Effect.valueOf(trail.getEffectName());
        } catch (Exception ex) {
            if (trail == KnifeTrail.BARREIRA) {
                try { return Effect.valueOf("SMOKE"); } catch (Exception ignored) {}
            }
            return Effect.CRIT;
        }
    }

    public boolean isKnife(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (!item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();

        // Reconhece qualquer faca criada pelo KnifeSkinManager pelo lore interno,
        // inclusive skins novas que nao sao swords/axes, como peixe, cenoura e spades.
        if (meta.hasLore()) {
            for (String line : meta.getLore()) {
                if (line != null && line.startsWith("§8skin:")) {
                    String id = line.replace("§8skin:", "").trim();
                    return getSkinById(id) != null;
                }
            }
        }

        if (!meta.hasDisplayName()) return false;

        String name = meta.getDisplayName();

        for (KnifeSkin skin : KnifeSkin.values()) {
            if (skin.getMaterial() == item.getType() && skin.getDisplayName().equals(name)) {
                return true;
            }
        }

        if (plugin != null) {
            Material cfgMat = Material.getMaterial(plugin.settings.getConfig().getInt("murderer-weapon.item-id"));
            String cfgName = Utils.FormatText2(plugin.settings.getConfig().getString("murderer-weapon.item-name"));

            if (cfgMat != null && item.getType() == cfgMat && name.equals(cfgName)) {
                return true;
            }
        }

        return false;
    }
}
