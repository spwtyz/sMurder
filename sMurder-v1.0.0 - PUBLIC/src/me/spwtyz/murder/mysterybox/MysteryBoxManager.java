package me.spwtyz.murder.mysterybox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;
import java.sql.ResultSet;
import java.sql.Statement;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.bukkit.scheduler.BukkitRunnable;

import me.spwtyz.murder.Main;

public class MysteryBoxManager {

    private final Main plugin;
    private final Random random = new Random();
    private final Set<UUID> opening = new HashSet<UUID>();

    public MysteryBoxManager(Main plugin) {
        this.plugin = plugin;
        setupDefaults();
    }

    private void setupDefaults() {
        if (plugin == null || plugin.data == null || plugin.data.getConfig() == null) return;

        FileConfiguration data = plugin.data.getConfig();

        if (!data.contains("mysterybox.defaults-created")) {
            data.set("mysterybox.defaults-created", true);
            plugin.data.save();
        }
    }

    public int getBoxes(Player p) {
        if (plugin.getConfig().getBoolean("mysql") && plugin.sql != null && plugin.sql.isConnected()) {
            try {
                Statement st = plugin.sql.getConnection().createStatement();
                ResultSet rs = st.executeQuery("SELECT boxes FROM MurderBattlePass WHERE uuid='" + p.getUniqueId().toString() + "'");
                int boxes = 0;
                if (rs.next()) boxes = rs.getInt("boxes");
                rs.close();
                st.close();
                return boxes;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (plugin.data == null) return 0;
        return plugin.data.getConfig().getInt("players." + p.getUniqueId() + ".mystery-boxes", 0);
    }

    public void addBoxes(Player p, int amount) {
        int current = getBoxes(p);
        int next = Math.max(0, current + amount);

        if (plugin.getConfig().getBoolean("mysql") && plugin.sql != null && plugin.sql.isConnected()) {
            try {
                Statement st = plugin.sql.getConnection().createStatement();
                st.executeUpdate("INSERT INTO MurderBattlePass (uuid, xp, premium, claimed, boxes) VALUES ('" + p.getUniqueId().toString() + "', 0, false, '', " + next + ") ON DUPLICATE KEY UPDATE boxes=" + next);
                st.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return;
        }

        if (plugin.data == null) return;
        UUID uuid = p.getUniqueId();
        plugin.data.getConfig().set("players." + uuid + ".mystery-boxes", next);
        plugin.data.save();
    }

    public void removeBox(Player p) {
        addBoxes(p, -1);
    }

    public boolean isOpening(Player p) {
        return p != null && opening.contains(p.getUniqueId());
    }

    public void openMainMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8✦ Mystery Box");

        fill(inv, (short) 15);
        border(inv);

        String boxName = (plugin.seasonalEventManager != null && plugin.seasonalEventManager.isEventActive())
                ? plugin.seasonalEventManager.getMysteryBoxName()
                : "§d§lMYSTERY BOX";
        inv.setItem(4, item(Material.NETHER_STAR, boxName,
                Arrays.asList(
                        "§8Sistema de Cosméticos",
                        "",
                        "§7Abra caixas para desbloquear",
                        "§7facas, trails, efeitos, auras",
                        "§7e títulos exclusivos.",
                        "",
                        "§fCaixas disponíveis: §d" + getBoxes(p),
                        "",
                        "§eClique no baú para abrir."
                )));

        inv.setItem(20, item(Material.CHEST, (plugin.seasonalEventManager != null && plugin.seasonalEventManager.isEventActive()) ? plugin.seasonalEventManager.themedName("§a§lABRIR 1 CAIXA") : "§a§lABRIR 1 CAIXA",
                Arrays.asList(
                        "§7Custo: §f1 Caixa Misteriosa",
                        "§7Você possui: §d" + getBoxes(p),
                        "",
                        "§aClique para iniciar a animação!",
                        "§8Estilo GadgetsMenu: caixa física + reveal."
                )));

        inv.setItem(22, item(Material.ENDER_CHEST, (plugin.seasonalEventManager != null && plugin.seasonalEventManager.isEventActive()) ? plugin.seasonalEventManager.themedName("§6§lRECOMPENSAS RARAS") : "§6§lRECOMPENSAS RARAS",
                Arrays.asList(
                        "§7• Facas exclusivas",
                        "§7• Trails de arremesso",
                        "§7• Efeitos de morte",
                        "§7• Auras",
                        "§7• Títulos",
                        "",
                        "§8Duplicatas viram coins."
                )));

        inv.setItem(24, item(Material.BOOK, "§b§lCHANCES",
                Arrays.asList(
                        "§fComum §8» §745%",
                        "§aRaro §8» §730%",
                        "§5Épico §8» §715%",
                        "§6Lendário §8» §78%",
                        "§dMítico §8» §72%"
                )));

        inv.setItem(40, item(Material.ARROW, "§cFechar", Arrays.asList("§7Clique para fechar.")));

        p.openInventory(inv);
    }

    public void handleClick(Player p, ItemStack clicked) {
        if (p == null) return;
        if (opening.contains(p.getUniqueId())) return;
        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String name = clicked.getItemMeta().getDisplayName();

        if (name.contains("Fechar")) {
            p.closeInventory();
            return;
        }

        if (name.contains("ABRIR 1 CAIXA") || name.contains("Caixa Misteriosa")) {
            if (getBoxes(p) <= 0) {
                p.sendMessage("§cVocê não possui caixas misteriosas.");
                p.playSound(p.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
                return;
            }

            removeBox(p);
            playAnimation(p);
        }
    }

    private void playAnimation(final Player p) {
        if (p == null || opening.contains(p.getUniqueId())) return;
        opening.add(p.getUniqueId());

        final Inventory inv = Bukkit.createInventory(null, 54, "§8✦ Mystery Vault ✦");
        fill(inv, (short) 15);
        border(inv);
        drawVaultStage(inv, 0, null);

        inv.setItem(4, item(Material.NETHER_STAR, "§d§lMYSTERY BOX",
                Arrays.asList("§7A caixa misteriosa está abrindo...", "§7O prêmio só aparece no final.")));

        p.openInventory(inv);
        p.playSound(p.getLocation(), Sound.CHEST_OPEN, 1f, 0.65f);
        // Sem item dropado no chão durante a animação. Tudo fica travado no menu.

        new BukkitRunnable() {
            int tick = 0;
            MysteryReward finalReward = rollReward();

            @Override
            public void run() {
                if (!p.isOnline()) {
                    opening.remove(p.getUniqueId());
                    cancel();
                    return;
                }

                if (tick <= 54) {
                    int stage = 0;
                    if (tick >= 16) stage = 1;
                    if (tick >= 34) stage = 2;

                    drawVaultStage(inv, stage, null);
                    playVaultEffects(p, tick, stage);

                    if (tick % 4 == 0) p.playSound(p.getLocation(), Sound.CLICK, 1f, 0.8f + (stage * 0.2f));
                    if (tick == 16) p.playSound(p.getLocation(), Sound.PISTON_EXTEND, 1f, 0.9f);
                    if (tick == 34) p.playSound(p.getLocation(), Sound.PISTON_RETRACT, 1f, 1.1f);
                    if (tick == 48) p.playSound(p.getLocation(), Sound.NOTE_PLING, 1f, 1.5f);

                    tick++;
                    return;
                }

                clearMenu(inv);

                inv.setItem(13, item(Material.FIREWORK, finalReward.getRarity().getColor() + "§lPARABÉNS!",
                        Arrays.asList("§7Você abriu uma Caixa Misteriosa.", "§7Raridade: " + finalReward.getRarity().getColor() + finalReward.getRarity().getDisplay())));
                inv.setItem(22, finalReward.toItem(true));
                inv.setItem(31, item(Material.NETHER_STAR, "§e§lRECOMPENSA REVELADA",
                        Arrays.asList("§f" + ChatColor.stripColor(finalReward.getColoredName()), "", "§7Clique em fechar para sair.")));
                inv.setItem(40, item(Material.ARROW, "§cFechar", Arrays.asList("§7Clique para fechar.")));

                giveReward(p, finalReward);

                p.playSound(p.getLocation(), Sound.LEVEL_UP, 1f, 1f);
                p.playSound(p.getLocation(), Sound.FIREWORK_LARGE_BLAST, 1f, 1f);
                p.sendMessage("§d§lMYSTERY BOX §8» §fVocê ganhou: " + finalReward.getColoredName());
                opening.remove(p.getUniqueId());
                cancel();
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void drawClosedCrate(Inventory inv) {
        drawVaultStage(inv, 0, null);
    }

    private void drawOpenCrate(Inventory inv) {
        drawVaultStage(inv, 3, null);
    }

    private void animateCrate(Inventory inv, int tick) {
        int stage = 0;
        if (tick >= 16) stage = 1;
        if (tick >= 34) stage = 2;
        drawVaultStage(inv, stage, null);
    }

    private void animateRoulette(Inventory inv, int tick) {
        // Removido: o usuário não quer roleta de itens.
        animateCrate(inv, tick);
    }

    private void playCrateEffects(Player p, int tick) {
        int stage = 0;
        if (tick >= 16) stage = 1;
        if (tick >= 34) stage = 2;
        playVaultEffects(p, tick, stage);
    }

    private void drawVaultStage(Inventory inv, int stage, MysteryReward reward) {
        ItemStack dark = item(Material.STAINED_GLASS_PANE, " ", new ArrayList<String>());
        dark.setDurability((short) 15);
        ItemStack purple = item(Material.STAINED_GLASS_PANE, "§5✦", new ArrayList<String>());
        purple.setDurability((short) 10);
        ItemStack pink = item(Material.STAINED_GLASS_PANE, "§d✦", new ArrayList<String>());
        pink.setDurability((short) 2);
        ItemStack yellow = item(Material.STAINED_GLASS_PANE, "§e✦", new ArrayList<String>());
        yellow.setDurability((short) 4);
        ItemStack green = item(Material.STAINED_GLASS_PANE, "§a✦", new ArrayList<String>());
        green.setDurability((short) 5);

        int[] aura = {10,11,12,13,14,15,16,19,25,28,34,37,38,39,40,41,42,43};
        for (int slot : aura) inv.setItem(slot, stage >= 3 ? green : (stage == 2 ? yellow : (stage == 1 ? pink : purple)));

        int[] crateOuter = {20,21,23,24,29,30,32,33};
        ItemStack wood = item(Material.WOOD, stage >= 2 ? "§6§lCAIXA DESTRAVADA" : "§6§lCAIXA FECHADA", Arrays.asList("§7Animação estilo Mystery Vault."));
        for (int slot : crateOuter) inv.setItem(slot, wood);

        if (stage == 0) {
            inv.setItem(22, item(Material.CHEST, "§d§lMYSTERY BOX", Arrays.asList("§7Trancada...", "§8Sem roleta de itens.")));
            inv.setItem(31, item(Material.TRIPWIRE_HOOK, "§e§lCHAVEANDO...", Arrays.asList("§7Aguarde a abertura.")));
        } else if (stage == 1) {
            inv.setItem(22, item(Material.ENDER_CHEST, "§5§lMYSTERY BOX", Arrays.asList("§7A energia está acumulando...")));
            inv.setItem(31, item(Material.REDSTONE_TORCH_ON, "§c§lCARREGANDO...", Arrays.asList("§7Partículas e sons de suspense.")));
        } else if (stage == 2) {
            inv.setItem(22, item(Material.ENDER_CHEST, "§e§lABRINDO...", Arrays.asList("§7O prêmio será revelado em instantes.")));
            inv.setItem(31, item(Material.GLOWSTONE_DUST, "§e§lLUZ FORTE", Arrays.asList("§7A caixa está brilhando.")));
        } else {
            inv.setItem(22, reward == null ? item(Material.CHEST, "§a§lCAIXA ABERTA", Arrays.asList("§7O prêmio foi revelado!")) : reward.toItem(true));
            inv.setItem(31, item(Material.NETHER_STAR, "§a§lABERTA!", Arrays.asList("§7Sem roleta, apenas reveal final.")));
        }
    }

    private void playVaultEffects(Player p, int tick, int stage) {
        Location loc = p.getLocation().add(0, 1.2, 0);
        try {
            if (stage == 0) {
                p.getWorld().playEffect(loc, Effect.CLOUD, 0);
            } else if (stage == 1) {
                p.getWorld().playEffect(loc, Effect.MOBSPAWNER_FLAMES, 0);
                if (tick % 3 == 0) p.getWorld().playEffect(loc, Effect.SMOKE, 0);
            } else if (stage == 2) {
                p.getWorld().playEffect(loc, Effect.HAPPY_VILLAGER, 0);
                p.getWorld().playEffect(loc, Effect.MOBSPAWNER_FLAMES, 0);
            } else {
                p.getWorld().playEffect(loc, Effect.HAPPY_VILLAGER, 0);
            }
        } catch (Throwable ignored) {}
    }

    private void spawnPreviewItem(final Player p, ItemStack stack) {
        if (p == null || stack == null || stack.getType() == Material.AIR) return;
        try {
            Location loc = p.getLocation().add(p.getLocation().getDirection().normalize().multiply(1.2)).add(0, 1.35, 0);
            final Item item = p.getWorld().dropItem(loc, stack.clone());
            item.setPickupDelay(999999);
            item.setVelocity(new Vector(0, 0.18, 0));
            new BukkitRunnable() {
                int life = 0;
                @Override
                public void run() {
                    if (item == null || item.isDead() || life++ > 35) {
                        if (item != null && !item.isDead()) item.remove();
                        cancel();
                        return;
                    }
                    try {
                        item.getWorld().playEffect(item.getLocation(), Effect.HAPPY_VILLAGER, 0);
                        item.setVelocity(new Vector(0, 0.03, 0));
                    } catch (Throwable ignored) {}
                }
            }.runTaskTimer(plugin, 1L, 2L);
        } catch (Throwable ignored) {}
    }

    private ItemStack randomVisualReward() {
        return rollReward().toItem(false);
    }

    private MysteryReward rollReward() {
        int roll = random.nextInt(100) + 1;

        MysteryRarity rarity;
        if (roll <= 45) rarity = MysteryRarity.COMMON;
        else if (roll <= 75) rarity = MysteryRarity.RARE;
        else if (roll <= 90) rarity = MysteryRarity.EPIC;
        else if (roll <= 98) rarity = MysteryRarity.LEGENDARY;
        else rarity = MysteryRarity.MYTHIC;

        List<MysteryReward> pool = getPool();
        List<MysteryReward> filtered = new ArrayList<MysteryReward>();

        for (MysteryReward reward : pool) {
            if (reward.getRarity() == rarity) filtered.add(reward);
        }

        if (filtered.isEmpty()) filtered = pool;

        return filtered.get(random.nextInt(filtered.size()));
    }

    private List<MysteryReward> getPool() {
        List<MysteryReward> pool = new ArrayList<MysteryReward>();

        pool.add(new MysteryReward("knife_classic", "Faca Classic", MysteryRewardType.KNIFE, MysteryRarity.COMMON, Material.IRON_SWORD));
        pool.add(new MysteryReward("knife_shadow", "Faca Shadow", MysteryRewardType.KNIFE, MysteryRarity.EPIC, Material.IRON_SWORD));
        pool.add(new MysteryReward("knife_blood", "Faca Blood", MysteryRewardType.KNIFE, MysteryRarity.LEGENDARY, Material.DIAMOND_SWORD));
        pool.add(new MysteryReward("knife_void", "Faca Void", MysteryRewardType.KNIFE, MysteryRarity.MYTHIC, Material.DIAMOND_SWORD));

        pool.add(new MysteryReward("trail_cloud", "Trail Cloud", MysteryRewardType.KNIFE_TRAIL, MysteryRarity.COMMON, Material.SNOW_BALL));
        pool.add(new MysteryReward("trail_heart", "Trail Heart", MysteryRewardType.KNIFE_TRAIL, MysteryRarity.RARE, Material.RED_ROSE));
        pool.add(new MysteryReward("trail_flame", "Trail Flame", MysteryRewardType.KNIFE_TRAIL, MysteryRarity.EPIC, Material.BLAZE_POWDER));
        pool.add(new MysteryReward("trail_lightning", "Trail Lightning", MysteryRewardType.KNIFE_TRAIL, MysteryRarity.LEGENDARY, Material.BLAZE_ROD));

        pool.add(new MysteryReward("death_smoke", "Smoke Death", MysteryRewardType.DEATH_EFFECT, MysteryRarity.COMMON, Material.COAL));
        pool.add(new MysteryReward("death_lightning", "Lightning Death", MysteryRewardType.DEATH_EFFECT, MysteryRarity.EPIC, Material.BLAZE_ROD));
        pool.add(new MysteryReward("death_funeral", "Funeral", MysteryRewardType.DEATH_EFFECT, MysteryRarity.LEGENDARY, Material.BONE));

        pool.add(new MysteryReward("title_hunter", "Título Hunter", MysteryRewardType.TITLE, MysteryRarity.RARE, Material.NAME_TAG));
        pool.add(new MysteryReward("title_legend", "Título Legend", MysteryRewardType.TITLE, MysteryRarity.LEGENDARY, Material.NAME_TAG));
        pool.add(new MysteryReward("aura_blood", "Aura Blood", MysteryRewardType.AURA, MysteryRarity.EPIC, Material.REDSTONE));
        pool.add(new MysteryReward("aura_void", "Aura Void", MysteryRewardType.AURA, MysteryRarity.MYTHIC, Material.ENDER_PEARL));

        return pool;
    }

    private void giveReward(Player p, MysteryReward reward) {
        if (plugin.data == null) return;

        String path = "players." + p.getUniqueId() + ".cosmetics.owned." + reward.getType().name() + "." + reward.getId();

        if (plugin.data.getConfig().getBoolean(path, false)) {
            int coins = duplicateCoins(reward.getRarity());

            if (plugin.getPlayerData(p) != null) {
                plugin.getPlayerData(p).addcoins(coins);
            }

            p.sendMessage("§7Duplicata convertida em §e" + coins + " coins§7.");
            return;
        }

        plugin.data.getConfig().set(path, true);
        plugin.data.save();
    }

    private int duplicateCoins(MysteryRarity rarity) {
        switch (rarity) {
            case COMMON: return 25;
            case RARE: return 75;
            case EPIC: return 150;
            case LEGENDARY: return 350;
            case MYTHIC: return 750;
            default: return 25;
        }
    }

    private void clearMenu(Inventory inv) {
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, null);
    }

    private void fill(Inventory inv, short data) {
        ItemStack glass = item(Material.STAINED_GLASS_PANE, " ", new ArrayList<String>());
        glass.setDurability(data);
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, glass);
    }

    private void border(Inventory inv) {
        ItemStack purple = item(Material.STAINED_GLASS_PANE, " ", new ArrayList<String>());
        purple.setDurability((short) 10);

        int[] slots = {0,1,2,3,5,6,7,8,9,17,18,26,27,35,36,37,38,39,41,42,43,44,45,46,47,48,49,50,51,52,53};

        for (int s : slots) {
            if (s < inv.getSize()) inv.setItem(s, purple);
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
