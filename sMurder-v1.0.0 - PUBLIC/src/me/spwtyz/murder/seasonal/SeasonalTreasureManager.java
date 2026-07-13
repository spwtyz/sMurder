package me.spwtyz.murder.seasonal;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import me.spwtyz.murder.Main;

public class SeasonalTreasureManager implements Listener {

    private final Main plugin;
    private final Map<UUID, String> treasures = new HashMap<UUID, String>();
    private File file;
    private FileConfiguration cfg;

    private static final String HALLOWEEN_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjA1ODllNmU5MzEzNjg4M2YzNGU0NThjMWJhZDc3NmQ2ZjQzMmJiYmYyNTYwZjI3OTFkMzk3ZDk2NmY5YjM2NSJ9fX0=";
    private static final String CHRISTMAS_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzU0NTUyNjY0MzViYzM4YmM2MzA2ZjA2OTYzZDkxMzllM2NmNDU1N2JiMzc4YzhlMzRjMWNkZjEyYzM0NTZkIn19fQ==";
    private static final String EASTER_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTY5MzM5MzZiYjA1NTEzYjEwYzIzZWI5NjhhNDA1NjM2ZDYyZGRkZDU4NzFiOTI4ZDJkZDBhZjA0OTNiNTkzIn19fQ==";

    public SeasonalTreasureManager(Main plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "seasonal-treasures.yml");
        reload();
    }

    public void reload() {
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        this.cfg = YamlConfiguration.loadConfiguration(file);
        if (!cfg.contains("Rewards.Coins")) cfg.set("Rewards.Coins", 100);
        if (!cfg.contains("Rewards.Message")) cfg.set("Rewards.Message", "&aVoce encontrou um tesouro sazonal! &e+%coins% coins");
        save();
        respawnAll();
    }

    public void save() {
        try { cfg.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    public void addTreasure(Player player) {
        SeasonalEventType type = plugin.seasonalEventManager == null ? SeasonalEventType.NONE : plugin.seasonalEventManager.getActiveEvent();
        if (type == SeasonalEventType.NONE) {
            player.sendMessage("§cAtive um evento antes de setar tesouros.");
            return;
        }
        String id = String.valueOf(System.currentTimeMillis());
        Location l = player.getLocation();
        String path = "Treasures." + type.name() + "." + id;
        cfg.set(path + ".world", l.getWorld().getName());
        cfg.set(path + ".x", l.getX());
        cfg.set(path + ".y", l.getY());
        cfg.set(path + ".z", l.getZ());
        cfg.set(path + ".yaw", l.getYaw());
        cfg.set(path + ".pitch", l.getPitch());
        save();
        respawnAll();
        player.sendMessage("§aTesouro do evento " + type.name() + " setado.");
    }

    public void clearCurrent(Player player) {
        SeasonalEventType type = plugin.seasonalEventManager == null ? SeasonalEventType.NONE : plugin.seasonalEventManager.getActiveEvent();
        if (type == SeasonalEventType.NONE) return;
        cfg.set("Treasures." + type.name(), null);
        cfg.set("Found." + type.name(), null);
        save();
        respawnAll();
        player.sendMessage("§aTesouros do evento atual removidos.");
    }

    public void list(Player player) {
        SeasonalEventType type = plugin.seasonalEventManager == null ? SeasonalEventType.NONE : plugin.seasonalEventManager.getActiveEvent();
        ConfigurationSection sec = cfg.getConfigurationSection("Treasures." + type.name());
        int count = sec == null ? 0 : sec.getKeys(false).size();
        player.sendMessage("§eTesouros do evento atual: §f" + count);
        player.sendMessage("§7/m evento tesouro add §f- adiciona no local atual");
        player.sendMessage("§7/m evento tesouro clear §f- remove todos do evento atual");
    }

    public int getFoundCount(Player p) {
        SeasonalEventType type = plugin.seasonalEventManager == null ? SeasonalEventType.NONE : plugin.seasonalEventManager.getActiveEvent();
        if (type == SeasonalEventType.NONE) return 0;
        return cfg.getStringList("Found." + type.name() + "." + p.getUniqueId()).size();
    }

    public int getTotalCount() {
        SeasonalEventType type = plugin.seasonalEventManager == null ? SeasonalEventType.NONE : plugin.seasonalEventManager.getActiveEvent();
        if (type == SeasonalEventType.NONE) return 0;
        ConfigurationSection sec = cfg.getConfigurationSection("Treasures." + type.name());
        return sec == null ? 0 : sec.getKeys(false).size();
    }

    public String getDisplay(Player p) {
        int total = getTotalCount();
        if (total <= 0) return "§7-";
        return "§e" + getFoundCount(p) + "§7/§e" + total;
    }

    public void respawnAll() {
        // Compatibilidade 1.8.8: Bukkit.getEntity(UUID) nao existe nessa API.
        // Procuramos os ArmorStands pelas worlds e removemos os que estao no mapa de tesouros.
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (Entity e : world.getEntities()) {
                if (treasures.containsKey(e.getUniqueId())) {
                    e.remove();
                }
            }
        }
        treasures.clear();
        SeasonalEventType type = plugin.seasonalEventManager == null ? SeasonalEventType.NONE : plugin.seasonalEventManager.getActiveEvent();
        if (type == SeasonalEventType.NONE) return;
        ConfigurationSection sec = cfg.getConfigurationSection("Treasures." + type.name());
        if (sec == null) return;
        for (String id : sec.getKeys(false)) {
            String path = "Treasures." + type.name() + "." + id;
            if (Bukkit.getWorld(cfg.getString(path + ".world")) == null) continue;
            Location l = new Location(Bukkit.getWorld(cfg.getString(path + ".world")), cfg.getDouble(path + ".x"), cfg.getDouble(path + ".y"), cfg.getDouble(path + ".z"), (float) cfg.getDouble(path + ".yaw"), (float) cfg.getDouble(path + ".pitch"));
            ArmorStand stand = l.getWorld().spawn(l, ArmorStand.class);
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setSmall(true);
            // ArmorStand#setMarker(boolean) nao existe no Spigot/Paper 1.8.8.
            // setSmall + sem gravidade + invisivel deixa o tesouro leve e clicavel.
            stand.setCustomName(ChatColor.translateAlternateColorCodes('&', treasureName(type)));
            stand.setCustomNameVisible(true);
            stand.setHelmet(customHead(textureFor(type)));
            treasures.put(stand.getUniqueId(), id);
        }
    }

    private String treasureName(SeasonalEventType type) {
        if (type == SeasonalEventType.CHRISTMAS) return "&c&lPresente de Natal &7(Clique)";
        if (type == SeasonalEventType.HALLOWEEN) return "&6&lAbobora Perdida &7(Clique)";
        if (type == SeasonalEventType.EASTER) return "&d&lOvo de Pascoa &7(Clique)";
        return "&b&lTesouro do Evento &7(Clique)";
    }

    private String textureFor(SeasonalEventType type) {
        if (type == SeasonalEventType.CHRISTMAS) return CHRISTMAS_HEAD;
        if (type == SeasonalEventType.HALLOWEEN) return HALLOWEEN_HEAD;
        if (type == SeasonalEventType.EASTER) return EASTER_HEAD;
        return CHRISTMAS_HEAD;
    }

    private ItemStack customHead(String value) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        try {
            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            profile.getProperties().put("textures", new Property("textures", value));
            Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (Throwable ex) {
            meta.setOwner("MHF_Present1");
        }
        head.setItemMeta(meta);
        return head;
    }

    @EventHandler
    public void onManipulate(PlayerArmorStandManipulateEvent e) {
        ArmorStand stand = e.getRightClicked();
        if (stand == null || !treasures.containsKey(stand.getUniqueId())) return;
        e.setCancelled(true);
        Player p = e.getPlayer();
        SeasonalEventType type = plugin.seasonalEventManager == null ? SeasonalEventType.NONE : plugin.seasonalEventManager.getActiveEvent();
        if (type == SeasonalEventType.NONE) return;
        String id = treasures.get(stand.getUniqueId());
        String path = "Found." + type.name() + "." + p.getUniqueId();
        java.util.List<String> found = cfg.getStringList(path);
        if (found.contains(id)) {
            p.sendMessage("§cVoce ja encontrou esse tesouro.");
            return;
        }
        found.add(id);
        cfg.set(path, found);
        save();
        int coins = cfg.getInt("Rewards.Coins", 100);
        try { plugin.getPlayerData(p).addcoins(coins); } catch (Throwable ignored) {}
        String msg = ChatColor.translateAlternateColorCodes('&', cfg.getString("Rewards.Message", "&aVoce encontrou um tesouro! &e+%coins% coins").replace("%coins%", String.valueOf(coins)));
        p.sendMessage(msg);
        p.playSound(p.getLocation(), Sound.LEVEL_UP, 1f, 1.4f);
    }
}
