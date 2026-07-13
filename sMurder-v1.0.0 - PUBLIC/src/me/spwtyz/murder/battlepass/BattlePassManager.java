package me.spwtyz.murder.battlepass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.sql.ResultSet;
import java.sql.Statement;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.spwtyz.murder.Main;

public class BattlePassManager {

    private final Main plugin;
    private final List<BattlePassReward> rewards = new ArrayList<BattlePassReward>();

    public BattlePassManager(Main plugin) {
        this.plugin = plugin;
        setupRewards();
    }

    private void setupRewards() {
        rewards.clear();

        rewards.add(new BattlePassReward(1, false, BattlePassRewardType.COINS, "100 Coins", 100));
        rewards.add(new BattlePassReward(2, false, BattlePassRewardType.MYSTERY_BOX, "Caixa Misteriosa", 1));
        rewards.add(new BattlePassReward(3, false, BattlePassRewardType.XP, "150 XP", 150));
        rewards.add(new BattlePassReward(4, false, BattlePassRewardType.COINS, "150 Coins", 150));
        rewards.add(new BattlePassReward(5, false, BattlePassRewardType.COSMETIC, "Trail Cloud", 0));
        rewards.add(new BattlePassReward(7, false, BattlePassRewardType.MYSTERY_BOX, "Caixa Misteriosa", 1));
        rewards.add(new BattlePassReward(10, false, BattlePassRewardType.MYSTERY_BOX, "2 Caixas Misteriosas", 2));

        rewards.add(new BattlePassReward(1, true, BattlePassRewardType.MYSTERY_BOX, "2 Premium Boxes", 2));
        rewards.add(new BattlePassReward(3, true, BattlePassRewardType.COINS, "300 Coins", 300));
        rewards.add(new BattlePassReward(4, true, BattlePassRewardType.COSMETIC, "Death Effect Funeral", 0));
        rewards.add(new BattlePassReward(6, true, BattlePassRewardType.MYSTERY_BOX, "3 Caixas Misteriosas", 3));
        rewards.add(new BattlePassReward(8, true, BattlePassRewardType.COINS, "500 Coins", 500));
        rewards.add(new BattlePassReward(12, true, BattlePassRewardType.COSMETIC, "Trail Flame", 0));
        rewards.add(new BattlePassReward(15, true, BattlePassRewardType.COSMETIC, "Faca Blood", 0));
    }

    public int getTier(Player p) {
        if (isSQL()) {
            try {
                Statement st = plugin.sql.getConnection().createStatement();
                ResultSet rs = st.executeQuery("SELECT xp FROM MurderBattlePass WHERE uuid='" + p.getUniqueId().toString() + "'");
                int xp = 0;
                if (rs.next()) xp = rs.getInt("xp");
                rs.close();
                st.close();
                return tierFromTotalXP(xp);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (plugin.data == null) return 1;
        return plugin.data.getConfig().getInt(path(p) + ".tier", 1);
    }

    public int getXP(Player p) {
        if (isSQL()) {
            try {
                Statement st = plugin.sql.getConnection().createStatement();
                ResultSet rs = st.executeQuery("SELECT xp FROM MurderBattlePass WHERE uuid='" + p.getUniqueId().toString() + "'");
                int total = 0;
                if (rs.next()) total = rs.getInt("xp");
                rs.close();
                st.close();
                return currentXPFromTotal(total);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (plugin.data == null) return 0;
        return plugin.data.getConfig().getInt(path(p) + ".xp", 0);
    }

    public int getXPToNext(Player p) {
        int tier = getTier(p);
        return 100 + (tier * 40);
    }

    public boolean hasPremium(Player p) {
        if (p.hasPermission("murder.battlepass.premium")) return true;
        if (isSQL()) {
            try {
                Statement st = plugin.sql.getConnection().createStatement();
                ResultSet rs = st.executeQuery("SELECT premium FROM MurderBattlePass WHERE uuid='" + p.getUniqueId().toString() + "'");
                boolean premium = false;
                if (rs.next()) premium = rs.getBoolean("premium");
                rs.close();
                st.close();
                return premium;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (plugin.data == null) return false;
        return plugin.data.getConfig().getBoolean(path(p) + ".premium", false);
    }

    public String getProgressBar(Player p) {
        int xp = getXP(p);
        int next = getXPToNext(p);
        int bars = 0;

        if (next > 0) {
            bars = Math.min(10, (int) Math.floor((xp * 10.0D) / next));
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            sb.append(i < bars ? "§a■" : "§7■");
        }

        return sb.toString();
    }

    public void addXP(Player p, int amount, String reason) {
        if (isSQL()) {
            int before = getTotalXP(p);
            int after = Math.max(0, before + amount);
            int oldTier = tierFromTotalXP(before);
            int newTier = tierFromTotalXP(after);
            saveTotalXP(p, after);
            if (newTier > oldTier) {
                p.sendMessage("§6§lPASSE §8» §aVocê subiu para o tier §f" + newTier + "§a!");
                p.playSound(p.getLocation(), Sound.LEVEL_UP, 1f, 1f);
            }
            return;
        }

        if (plugin.data == null) return;
        int xp = getXP(p) + amount;
        int tier = getTier(p);
        int needed = 100 + (tier * 40);

        while (xp >= needed) {
            xp -= needed;
            tier++;
            needed = 100 + (tier * 40);
            p.sendMessage("§6§lPASSE §8» §aVocê subiu para o tier §f" + tier + "§a!");
            p.playSound(p.getLocation(), Sound.LEVEL_UP, 1f, 1f);
        }

        plugin.data.getConfig().set(path(p) + ".tier", tier);
        plugin.data.getConfig().set(path(p) + ".xp", xp);
        plugin.data.save();
    }

    public void openMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8✦ Battle Pass");

        fill(inv, (short) 15);
        border(inv);

        inv.setItem(4, item(Material.NETHER_STAR, "§6§lBATTLE PASS",
                Arrays.asList(
                        "§8Temporada Atual",
                        "",
                        "§7Tier atual: §e" + getTier(p),
                        "§7XP: §b" + getXP(p) + "§7/§b" + getXPToNext(p),
                        "§7Progresso: " + getProgressBar(p),
                        "§7Premium: " + (hasPremium(p) ? "§aAtivo" : "§cNão"),
                        "",
                        "§eClique nas recompensas para resgatar."
                )));

        inv.setItem(19, item(Material.IRON_INGOT, "§a§lTRILHA FREE",
                Arrays.asList("§7Rewards gratuitos para todos.")));

        inv.setItem(37, item(Material.GOLD_INGOT, "§6§lTRILHA PREMIUM",
                Arrays.asList("§7Rewards extras para VIP/Premium.")));

        int[] freeSlots = {10,11,12,13,14,15,16};
        int[] premiumSlots = {28,29,30,31,32,33,34};

        int freeIndex = 0;
        int premiumIndex = 0;

        for (BattlePassReward reward : rewards) {
            if (!reward.isPremium()) {
                if (freeIndex < freeSlots.length) {
                    inv.setItem(freeSlots[freeIndex], reward.toItem(p, this));
                    freeIndex++;
                }
            } else {
                if (premiumIndex < premiumSlots.length) {
                    inv.setItem(premiumSlots[premiumIndex], reward.toItem(p, this));
                    premiumIndex++;
                }
            }
        }

        inv.setItem(49, item(Material.EMERALD, "§aMissões",
                Arrays.asList(
                        "§7Complete partidas, colete",
                        "§7fragmentos e vença jogos",
                        "§7para ganhar XP do passe."
                )));

        p.openInventory(inv);
    }

    public void handleClick(Player p, ItemStack clicked) {
        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String name = clicked.getItemMeta().getDisplayName();

        for (BattlePassReward reward : rewards) {
            if (name.contains(reward.getName())) {
                claim(p, reward);
                return;
            }
        }
    }

    public void claim(Player p, BattlePassReward reward) {
        if (plugin.data == null) return;

        if (getTier(p) < reward.getTier()) {
            p.sendMessage("§cVocê ainda não chegou nesse tier.");
            p.playSound(p.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
            return;
        }

        if (reward.isPremium() && !hasPremium(p)) {
            p.sendMessage("§cEsse reward é Premium.");
            p.playSound(p.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
            return;
        }

        String claimKey = (reward.isPremium() ? "premium." : "free.") + reward.getTier();
        String claimPath = path(p) + ".claimed." + claimKey;

        if (hasClaimed(p, claimKey, claimPath)) {
            p.sendMessage("§cVocê já resgatou esse reward.");
            p.playSound(p.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
            return;
        }

        setClaimed(p, claimKey, claimPath);
        giveReward(p, reward);

        p.sendMessage("§6§lPASSE §8» §aReward resgatado: §f" + reward.getName());
        p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1f, 1f);
        openMenu(p);
    }

    private void giveReward(Player p, BattlePassReward reward) {
        switch (reward.getType()) {
            case COINS:
                if (plugin.getPlayerData(p) != null) plugin.getPlayerData(p).addcoins(reward.getAmount());
                break;
            case XP:
                addXP(p, reward.getAmount(), "Passe de Batalha");
                break;
            case MYSTERY_BOX:
                if (plugin.mysteryBoxManager != null) plugin.mysteryBoxManager.addBoxes(p, reward.getAmount());
                break;
            case COSMETIC:
                plugin.data.getConfig().set("players." + p.getUniqueId() + ".cosmetics.owned.BATTLEPASS." + reward.getName().replace(" ", "_").toLowerCase(), true);
                break;
        }
    }


    private boolean isSQL() {
        return plugin.getConfig().getBoolean("mysql") && plugin.sql != null && plugin.sql.isConnected();
    }

    private int getTotalXP(Player p) {
        if (!isSQL()) return 0;
        try {
            Statement st = plugin.sql.getConnection().createStatement();
            ResultSet rs = st.executeQuery("SELECT xp FROM MurderBattlePass WHERE uuid='" + p.getUniqueId().toString() + "'");
            int total = 0;
            if (rs.next()) total = rs.getInt("xp");
            rs.close();
            st.close();
            return total;
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    private void saveTotalXP(Player p, int total) {
        if (!isSQL()) return;
        try {
            Statement st = plugin.sql.getConnection().createStatement();
            st.executeUpdate("INSERT INTO MurderBattlePass (uuid, xp, premium, claimed, boxes) VALUES ('" + p.getUniqueId().toString() + "', " + total + ", false, '', 0) ON DUPLICATE KEY UPDATE xp=" + total);
            st.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private int tierFromTotalXP(int total) {
        int tier = 1;
        int xp = total;
        int needed = 100 + (tier * 40);
        while (xp >= needed) {
            xp -= needed;
            tier++;
            needed = 100 + (tier * 40);
        }
        return tier;
    }

    private int currentXPFromTotal(int total) {
        int tier = 1;
        int xp = total;
        int needed = 100 + (tier * 40);
        while (xp >= needed) {
            xp -= needed;
            tier++;
            needed = 100 + (tier * 40);
        }
        return xp;
    }

    private boolean hasClaimed(Player p, String claimKey, String yamlPath) {
        if (isSQL()) {
            try {
                Statement st = plugin.sql.getConnection().createStatement();
                ResultSet rs = st.executeQuery("SELECT claimed FROM MurderBattlePass WHERE uuid='" + p.getUniqueId().toString() + "'");
                String claimed = "";
                if (rs.next()) claimed = rs.getString("claimed");
                rs.close();
                st.close();
                return claimed != null && ("," + claimed + ",").contains("," + claimKey + ",");
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }
        return plugin.data != null && plugin.data.getConfig().getBoolean(yamlPath, false);
    }

    private void setClaimed(Player p, String claimKey, String yamlPath) {
        if (isSQL()) {
            try {
                Statement st = plugin.sql.getConnection().createStatement();
                ResultSet rs = st.executeQuery("SELECT claimed FROM MurderBattlePass WHERE uuid='" + p.getUniqueId().toString() + "'");
                String claimed = "";
                if (rs.next()) claimed = rs.getString("claimed");
                rs.close();
                if (claimed == null || claimed.length() == 0) claimed = claimKey;
                else if (!("," + claimed + ",").contains("," + claimKey + ",")) claimed = claimed + "," + claimKey;
                st.executeUpdate("INSERT INTO MurderBattlePass (uuid, xp, premium, claimed, boxes) VALUES ('" + p.getUniqueId().toString() + "', 0, false, '" + claimed + "', 0) ON DUPLICATE KEY UPDATE claimed='" + claimed + "'");
                st.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return;
        }
        if (plugin.data != null) {
            plugin.data.getConfig().set(yamlPath, true);
            plugin.data.save();
        }
    }

    private String path(Player p) {
        UUID uuid = p.getUniqueId();
        return "players." + uuid + ".battlepass";
    }

    private void fill(Inventory inv, short data) {
        ItemStack glass = item(Material.STAINED_GLASS_PANE, " ", new ArrayList<String>());
        glass.setDurability(data);
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, glass);
    }

    private void border(Inventory inv) {
        ItemStack orange = item(Material.STAINED_GLASS_PANE, " ", new ArrayList<String>());
        orange.setDurability((short) 1);

        int[] slots = {0,1,2,3,5,6,7,8,9,17,18,26,27,35,36,44,45,46,47,48,50,51,52,53};

        for (int s : slots) {
            if (s < inv.getSize()) inv.setItem(s, orange);
        }
    }

    private ItemStack item(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
