package me.spwtyz.murder.builder;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import me.spwtyz.murder.Main;
import me.spwtyz.murder.PlayerData;

public class ProfileMenu implements Listener {

    private final Main plugin;
    public static final String TITLE = "§8Perfil";

    public ProfileMenu(Main plugin) {
        this.plugin = plugin;
    }

    public void open(Player p) {
        if (p == null) return;

        Inventory inv = Bukkit.createInventory(null, 45, TITLE);
        fill(inv, glass((short) 15));

        PlayerData data = plugin.getPlayerData(p);
        int coins = data == null ? (plugin.getPlayerData(p) != null ? plugin.getPlayerData(p).getcoins() : 0) : data.getcoins();
        int kills = data == null ? plugin.getKills(p) : data.getkill();
        int deaths = data == null ? plugin.getDeaths(p) : data.getdeaths();
        int wins = data == null ? plugin.getWins(p) : data.getwins();
        int loses = data == null ? plugin.getLoses(p) : data.getloses();
        int score = data == null ? plugin.getScore(p) : data.getscore();
        String level = plugin.getLevelDisplay(p);
        String xp = plugin.getXPDisplay(p);
        String tag = plugin.tagManager == null ? "§7Nenhum" : (plugin.tagManager != null ? plugin.tagManager.getTag(p) : "");
        if (tag == null || tag.trim().isEmpty()) tag = "§7Nenhum";
        int rankedRP = plugin.rankedManager == null ? 1000 : plugin.rankedManager.getRP(p);
        String rankedRank = plugin.rankedManager == null ? "§7Bronze" : plugin.rankedManager.getRankColor(p) + plugin.rankedManager.getRankName(p);
        int rankedWins = plugin.rankedManager == null ? 0 : plugin.rankedManager.getWins(p);
        int rankedKills = plugin.rankedManager == null ? 0 : plugin.rankedManager.getKills(p);
        int rankedLosses = plugin.rankedManager == null ? 0 : plugin.rankedManager.getLosses(p);

        inv.setItem(4, playerHead(p,
                "§b§l" + p.getName(),
                "§7Seu perfil no Murder.",
                "",
                "§fTítulo: " + tag,
                "§fLevel: §a" + level,
                "§fXP: §b" + xp,
                "§fCoins: §6" + coins,
                "",
                "§6§lRanked",
                "§fRank: " + rankedRank,
                "§fRP: §b" + rankedRP));

        inv.setItem(19, item(Material.GOLD_INGOT, "§6Coins", "§fVocê possui: §6" + coins));
        inv.setItem(20, item(Material.EXP_BOTTLE, "§aLevel", "§fLevel atual: §a" + level, "§fXP: §b" + xp));
        inv.setItem(21, item(Material.DIAMOND_SWORD, "§cAbates", "§fKills: §c" + kills));
        inv.setItem(22, item(Material.SKULL_ITEM, "§7Mortes", "§fDeaths: §7" + deaths));
        inv.setItem(23, item(Material.EMERALD, "§aGanhou", "§fWins: §a" + wins));
        inv.setItem(24, item(Material.REDSTONE, "§cPerdeu", "§fLoses: §c" + loses));
        inv.setItem(25, item(Material.NETHER_STAR, "§eScore", "§fScore: §e" + score));
        inv.setItem(31, item(Material.GOLDEN_APPLE, "§6§lRanked",
                "§fRank: " + rankedRank,
                "§fRP: §b" + rankedRP,
                "§fVitórias Ranked: §a" + rankedWins,
                "§fAbates Ranked: §c" + rankedKills,
                "§fDerrotas Ranked: §7" + rankedLosses,
                "",
                "§7Jogue salas no modo Ranked para subir."));

        inv.setItem(37, item(Material.NAME_TAG, "§bTags", "§7Clique para escolher sua tag."));
        inv.setItem(39, item(Material.EMERALD, "§bLoja", "§7Clique para abrir a loja."));
        inv.setItem(41, item(Material.BOOK, "§bSkins", "§7Clique para abrir os kits."));
        inv.setItem(43, item(Material.BARRIER, "§cFechar", "§7Clique para fechar."));

        p.openInventory(inv);
        p.playSound(p.getLocation(), Sound.CLICK, 1f, 1.2f);
    }

    private void fill(Inventory inv, ItemStack item) {
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, item);
    }

    private ItemStack glass(short data) {
        ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE, 1, data);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack item(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        if (mat == Material.SKULL_ITEM) item.setDurability((short) 3);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack playerHead(Player p, String name, String... lore) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwner(p.getName());
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        head.setItemMeta(meta);
        return head;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView() == null || !TITLE.equals(e.getView().getTitle())) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;
        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName()).toLowerCase();

        if (name.contains("fechar")) {
            p.closeInventory();
        } else if (name.contains("tags")) {
            p.closeInventory();
            p.performCommand("tag");
        } else if (name.contains("loja")) {
            p.closeInventory();
            p.performCommand("loja");
        } else if (name.contains("kits")) {
            p.closeInventory();
            p.sendMessage("§eOs kits ficam disponíveis no lobby de espera da sala.");
        }
    }
}
