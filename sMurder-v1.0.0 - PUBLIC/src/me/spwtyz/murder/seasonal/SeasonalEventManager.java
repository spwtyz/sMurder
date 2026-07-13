package me.spwtyz.murder.seasonal;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang.StringEscapeUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.spwtyz.murder.Main;

/**
 * Sistema de eventos sazonais da AltaMC.
 *
 * Agora os textos/temas ficam em plugins/sMurder/events/*.yml:
 * - halloween.yml
 * - christmas.yml
 * - easter.yml
 * - custom.yml
 *
 * A config principal guarda apenas qual evento está ativo.
 */
public class SeasonalEventManager implements Listener {

    private final Main plugin;
    private SeasonalEventType active;
    private File eventsFolder;

    public SeasonalEventManager(Main plugin) {
        this.plugin = plugin;
        setupDefaults();
        this.active = SeasonalEventType.fromString(plugin.getConfig().getString("SeasonalEvents.Active", "NONE"));
    }

    public void setupDefaults() {
        if (!plugin.getConfig().contains("ServerName")) plugin.getConfig().set("ServerName", "AltaMC");
        if (!plugin.getConfig().contains("SeasonalEvents.Active")) plugin.getConfig().set("SeasonalEvents.Active", "NONE");
        plugin.saveConfig();

        eventsFolder = new File(plugin.getDataFolder(), "events");
        if (!eventsFolder.exists()) eventsFolder.mkdirs();

        createDefaultEventFile(SeasonalEventType.HALLOWEEN);
        createDefaultEventFile(SeasonalEventType.CHRISTMAS);
        createDefaultEventFile(SeasonalEventType.EASTER);
        createDefaultEventFile(SeasonalEventType.CUSTOM);
    }

    private File fileFor(SeasonalEventType type) {
        if (eventsFolder == null) eventsFolder = new File(plugin.getDataFolder(), "events");
        return new File(eventsFolder, type.getId().toLowerCase() + ".yml");
    }

    private FileConfiguration configFor(SeasonalEventType type) {
        if (type == null || type == SeasonalEventType.NONE) return null;
        File file = fileFor(type);
        if (!file.exists()) createDefaultEventFile(type);

        try {
            YamlConfiguration cfg = new YamlConfiguration();
            cfg.load(file);
            return cfg;
        } catch (Throwable ex) {
            // Spigot 1.8/SnakeYAML antigo quebra quando arquivos antigos foram salvos
            // com emoji real/!!binary. Faz backup e recria sem emojis.
            File broken = new File(file.getParentFile(), file.getName() + ".broken");
            try {
                if (broken.exists()) broken.delete();
                file.renameTo(broken);
            } catch (Throwable ignored) {}
            createDefaultEventFile(type);
            return YamlConfiguration.loadConfiguration(file);
        }
    }

    private void createDefaultEventFile(SeasonalEventType type) {
        if (type == SeasonalEventType.NONE) return;
        File file = fileFor(type);
        if (file.exists()) return;

        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("Enabled", true);
        cfg.set("ServerName", "AltaMC");

        if (type == SeasonalEventType.HALLOWEEN) {
            cfg.set("DisplayName", "&6&lHalloween");
            cfg.set("ScoreboardTitle", "&6&lAltaMC Halloween");
            cfg.set("MenuPrefix", "&6&lHALLOWEEN &e");
            cfg.set("ShopTitle", "&6&lLoja Assombrada");
            cfg.set("MysteryBoxName", "&6&lHalloween Crate");
            cfg.set("MysteryBoxLore", Arrays.asList("&7Evento especial de Halloween.", "&7Cosméticos sombrios e raros."));
            cfg.set("LobbyItemName", "&6&lLoja Assombrada &7(Clique)");
            cfg.set("LobbyMessage", "&6Evento de Halloween ativo na AltaMC!");
            cfg.set("PrimaryColor", "&6");
            cfg.set("SecondaryColor", "&e");
            cfg.set("Particles", true);
        } else if (type == SeasonalEventType.CHRISTMAS) {
            cfg.set("DisplayName", "&c&lNatal");
            cfg.set("ScoreboardTitle", "&c&lAltaMC Natal");
            cfg.set("MenuPrefix", "&c&lNATAL &f");
            cfg.set("ShopTitle", "&c&lLoja Natalina");
            cfg.set("MysteryBoxName", "&c&lChristmas Crate");
            cfg.set("MysteryBoxLore", Arrays.asList("&7Evento especial de Natal.", "&7Presentes, neve e cosméticos limitados."));
            cfg.set("LobbyItemName", "&c&lLoja Natalina &7(Clique)");
            cfg.set("LobbyMessage", "&cEvento de Natal ativo na AltaMC!");
            cfg.set("PrimaryColor", "&c");
            cfg.set("SecondaryColor", "&f");
            cfg.set("Particles", true);
        } else if (type == SeasonalEventType.EASTER) {
            cfg.set("DisplayName", "&d&lPascoa");
            cfg.set("ScoreboardTitle", "&d&lAltaMC Pascoa");
            cfg.set("MenuPrefix", "&d&lPASCOA &f");
            cfg.set("ShopTitle", "&d&lLoja de Pascoa");
            cfg.set("MysteryBoxName", "&d&lEaster Crate");
            cfg.set("MysteryBoxLore", Arrays.asList("&7Evento especial de Pascoa.", "&7Ovos, coelhos e cosméticos limitados."));
            cfg.set("LobbyItemName", "&d&lLoja de Pascoa &7(Clique)");
            cfg.set("LobbyMessage", "&dEvento de Pascoa ativo na AltaMC!");
            cfg.set("PrimaryColor", "&d");
            cfg.set("SecondaryColor", "&f");
            cfg.set("Particles", true);
        } else {
            cfg.set("DisplayName", "&b&lEvento Custom");
            cfg.set("ScoreboardTitle", "&b&lAltaMC Event");
            cfg.set("MenuPrefix", "&b&lEVENTO &f");
            cfg.set("ShopTitle", "&b&lLoja do Evento");
            cfg.set("MysteryBoxName", "&b&lEvent Crate");
            cfg.set("MysteryBoxLore", Arrays.asList("&7Edite este arquivo para criar", "&7seu próprio evento."));
            cfg.set("LobbyItemName", "&b&lLoja do Evento &7(Clique)");
            cfg.set("LobbyMessage", "&bEvento custom ativo na AltaMC!");
            cfg.set("PrimaryColor", "&b");
            cfg.set("SecondaryColor", "&f");
            cfg.set("Particles", true);
        }

        cfg.set("CosmeticCategoryName", getColor(cfg.getString("PrimaryColor", "&d")) + "Cosmeticos do Evento");
        cfg.set("ExclusiveLore", Arrays.asList("&7Cosmetico sazonal.", "&7Disponivel durante este evento."));

        // Cosméticos sazonais: aparecem na loja somente quando este evento está ativo.
        // Sem emojis para evitar !!binary/ReaderException no SnakeYAML antigo do Spigot 1.8.
        if (type == SeasonalEventType.HALLOWEEN) {
            cfg.set("Cosmetics.Death.Name", "&6Morcegos Sombrios");
            cfg.set("Cosmetics.Death.Price", 3500);
            cfg.set("Cosmetics.Victory.Name", "&6Ritual Assombrado");
            cfg.set("Cosmetics.Victory.Price", 4500);
            cfg.set("Cosmetics.Aura.Name", "&6Aura de Abobora");
            cfg.set("Cosmetics.Aura.Price", 3000);
            cfg.set("Cosmetics.Gadget.Name", "&6Lanterna Assombrada");
            cfg.set("Cosmetics.Gadget.Price", 2500);
        } else if (type == SeasonalEventType.CHRISTMAS) {
            cfg.set("Cosmetics.Death.Name", "&cNeve Final");
            cfg.set("Cosmetics.Death.Price", 3500);
            cfg.set("Cosmetics.Victory.Name", "&cFogos Natalinos");
            cfg.set("Cosmetics.Victory.Price", 4500);
            cfg.set("Cosmetics.Aura.Name", "&cAura Congelante");
            cfg.set("Cosmetics.Aura.Price", 3000);
            cfg.set("Cosmetics.Gadget.Name", "&cPresente Misterioso");
            cfg.set("Cosmetics.Gadget.Price", 2500);
        } else if (type == SeasonalEventType.EASTER) {
            cfg.set("Cosmetics.Death.Name", "&dOvos Explosivos");
            cfg.set("Cosmetics.Death.Price", 3500);
            cfg.set("Cosmetics.Victory.Name", "&dChuva Colorida");
            cfg.set("Cosmetics.Victory.Price", 4500);
            cfg.set("Cosmetics.Aura.Name", "&dAura de Pascoa");
            cfg.set("Cosmetics.Aura.Price", 3000);
            cfg.set("Cosmetics.Gadget.Name", "&dCesta de Ovos");
            cfg.set("Cosmetics.Gadget.Price", 2500);
        } else {
            cfg.set("Cosmetics.Death.Name", "&bEfeito Custom");
            cfg.set("Cosmetics.Death.Price", 3500);
            cfg.set("Cosmetics.Victory.Name", "&bVitoria Custom");
            cfg.set("Cosmetics.Victory.Price", 4500);
            cfg.set("Cosmetics.Aura.Name", "&bAura Custom");
            cfg.set("Cosmetics.Aura.Price", 3000);
            cfg.set("Cosmetics.Gadget.Name", "&bGadget Custom");
            cfg.set("Cosmetics.Gadget.Price", 2500);
        }

        try {
            cfg.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String color(String s) {
        if (s == null) return "";
        try {
            s = StringEscapeUtils.unescapeJava(s);
        } catch (Throwable ignored) {}
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    private String getColor(String s) {
        return color(s);
    }

    public SeasonalEventType getActiveEvent() {
        return active == null ? SeasonalEventType.NONE : active;
    }

    public boolean isEventActive() {
        return getActiveEvent() != SeasonalEventType.NONE;
    }

    public boolean isEnabled(SeasonalEventType type) {
        if (type == null || type == SeasonalEventType.NONE) return true;
        FileConfiguration cfg = configFor(type);
        return cfg == null || cfg.getBoolean("Enabled", true);
    }

    public String getString(String key, String def) {
        if (!isEventActive()) return color(def);
        FileConfiguration cfg = configFor(getActiveEvent());
        if (cfg == null) return color(def);
        return color(cfg.getString(key, def));
    }

    public boolean getBoolean(String key, boolean def) {
        if (!isEventActive()) return def;
        FileConfiguration cfg = configFor(getActiveEvent());
        return cfg == null ? def : cfg.getBoolean(key, def);
    }

    public String getDisplayName() {
        if (!isEventActive()) return "§7Nenhum";
        return getString("DisplayName", getActiveEvent().getDisplayName());
    }

    public String getMenuPrefix() {
        return getString("MenuPrefix", "");
    }

    public String themedName(String normalName) {
        if (!isEventActive()) return normalName;
        return getMenuPrefix() + ChatColor.stripColor(normalName == null ? "" : normalName);
    }

    public String getLobbyTitle() {
        return getString("ScoreboardTitle", "§d§lAltaMC");
    }

    public String getShopTitle() {
        return getString("ShopTitle", "§6§lLoja");
    }

    public String getMysteryBoxName() {
        return getString("MysteryBoxName", "§d§lMYSTERY BOX");
    }

    public String getLobbyShopItemName() {
        return getString("LobbyItemName", "§eLoja §7(Clique)");
    }

    public String getCosmeticName(String key, String def) {
        return getString("Cosmetics." + key + ".Name", def);
    }

    public int getCosmeticPrice(String key, int def) {
        if (!isEventActive()) return def;
        FileConfiguration cfg = configFor(getActiveEvent());
        return cfg == null ? def : cfg.getInt("Cosmetics." + key + ".Price", def);
    }

    public String getActiveEventIdLower() {
        return getActiveEvent().name().toLowerCase();
    }

    public void setActiveEvent(SeasonalEventType type) {
        if (type == null) type = SeasonalEventType.NONE;
        if (!isEnabled(type)) {
            type = SeasonalEventType.NONE;
        }
        this.active = type;
        plugin.getConfig().set("SeasonalEvents.Active", type.name());
        plugin.saveConfig();
    }

    public void reload() {
        setupDefaults();
        this.active = SeasonalEventType.fromString(plugin.getConfig().getString("SeasonalEvents.Active", "NONE"));
    }

    public void openMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, "§8Eventos Sazonais");
        ItemStack glass = item(Material.STAINED_GLASS_PANE, "§8", "");
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, glass);

        inv.setItem(4, item(Material.NETHER_STAR, "§d§lEventos da AltaMC", "§7Evento atual: " + getDisplayName(), "§7Editável por arquivos em §f/plugins/sMurder/events§7."));
        inv.setItem(10, eventItem(SeasonalEventType.HALLOWEEN));
        inv.setItem(12, eventItem(SeasonalEventType.CHRISTMAS));
        inv.setItem(14, eventItem(SeasonalEventType.EASTER));
        inv.setItem(16, eventItem(SeasonalEventType.CUSTOM));
        inv.setItem(31, eventItem(SeasonalEventType.NONE));
        inv.setItem(22, item(Material.BOOK, "§eArquivos", "§7halloween.yml", "§7christmas.yml", "§7easter.yml", "§7custom.yml", "", "§eEdite e use /m evento reload"));
        player.openInventory(inv);
    }

    private ItemStack eventItem(SeasonalEventType type) {
        boolean selected = getActiveEvent() == type;
        String name = type == SeasonalEventType.NONE ? type.getDisplayName() : (selected ? "§a§lATIVO §8- " : "") + colorFromFile(type, "DisplayName", type.getDisplayName());
        return item(type.getIcon(), name,
                type.getDescription(),
                "§7Status: " + (selected ? "§aAtivo" : "§cDesativado"),
                type == SeasonalEventType.CUSTOM ? "§7Arquivo: §fcustom.yml" : "§7Arquivo: §f" + type.getId() + ".yml",
                "§eClique para ativar");
    }

    private String colorFromFile(SeasonalEventType type, String path, String def) {
        if (type == SeasonalEventType.NONE) return color(def);
        FileConfiguration cfg = configFor(type);
        return cfg == null ? color(def) : color(cfg.getString(path, def));
    }

    private ItemStack item(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material == null ? Material.PAPER : material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getView().getTitle().equals("§8Eventos Sazonais")) return;
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        if (!player.isOp() && !player.hasPermission("murder.admin") && !player.hasPermission("murder.staff") && !player.hasPermission("smurder.admin")) {
            player.closeInventory();
            player.sendMessage("§cApenas staff pode alterar eventos.");
            return;
        }
        int slot = event.getRawSlot();
        SeasonalEventType type = null;
        if (slot == 10) type = SeasonalEventType.HALLOWEEN;
        if (slot == 12) type = SeasonalEventType.CHRISTMAS;
        if (slot == 14) type = SeasonalEventType.EASTER;
        if (slot == 16) type = SeasonalEventType.CUSTOM;
        if (slot == 31) type = SeasonalEventType.NONE;
        if (type == null) return;
        setActiveEvent(type);
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1f, 1.2f);
        player.sendMessage("§aEvento sazonal alterado para: " + getDisplayName());
        openMenu(player);
    }
}
