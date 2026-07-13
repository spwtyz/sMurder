package me.spwtyz.murder.hats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.spwtyz.murder.Arena;
import me.spwtyz.murder.GameState;
import me.spwtyz.murder.Main;
import me.spwtyz.murder.PlayerType;

public class HatAbilityManager implements Listener {

    public enum Hat {
        NONE("none", "§7Sem Chapéu", Material.BARRIER, 0, 0,
                "§7Remove o chapéu equipado.", Ability.NONE),

        SPONGE("sponge", "§eEsponja Veloz", Material.SPONGE, 0, 0,
                "§7Habilidade: §fVelocidade I durante a partida.", Ability.SPEED),

        TNT("tnt", "§cAnti-Void", Material.TNT, 0, 0,
                "§7Habilidade: §fPequena chance de sobreviver ao void no TNTTag.", Ability.TNT_VOID_SAVE),

        ASTRONAUT("astronaut", "§bCamuflagem", Material.BEACON, 0, 0,
                "§7Habilidade: §fInocente pode ficar invisível por alguns segundos.", Ability.INNOCENT_INVISIBILITY),

        CHEST("chest", "§6Olho Cego", Material.CHEST, 0, 0,
                "§7Habilidade: §fChance de cegar o Murder ao tentar te matar.", Ability.BLIND_MURDER),

        GUARDIAN("guardian", "§aGuardião", Material.EMERALD_BLOCK, 0, 0,
                "§7Habilidade: §fChance pequena de não morrer para faca/arma uma vez.", Ability.ONE_HIT_SAVE);

        private final String id;
        private final String display;
        private final Material material;
        private final short data;
        private final int price;
        private final String description;
        private final Ability ability;

        Hat(String id, String display, Material material, int data, int price, String description, Ability ability) {
            this.id = id;
            this.display = display;
            this.material = material;
            this.data = (short) data;
            this.price = price;
            this.description = description;
            this.ability = ability;
        }

        public String getId() { return id; }
        public String getDisplay() { return display; }
        public Material getMaterial() { return material; }
        public short getData() { return data; }
        public int getPrice() { return price; }
        public String getDescription() { return description; }
        public Ability getAbility() { return ability; }
    }

    public enum Ability {
        NONE,
        SPEED,
        TNT_VOID_SAVE,
        ONE_HIT_SAVE,
        INNOCENT_INVISIBILITY,
        BLIND_MURDER
    }

    private final Main plugin;
    private final Random random = new Random();
    private final Map<UUID, Hat> selected = new HashMap<UUID, Hat>();
    private final Map<UUID, Set<String>> owned = new HashMap<UUID, Set<String>>();

    // Controle por partida. As habilidades fortes só podem ativar uma vez por partida.
    private final Set<UUID> usedVoidSave = new HashSet<UUID>();
    private final Set<UUID> usedOneHitSave = new HashSet<UUID>();
    private final Set<UUID> usedBlindMurder = new HashSet<UUID>();
    private final Set<UUID> usedInvisibility = new HashSet<UUID>();

    public HatAbilityManager(Main plugin) {
        this.plugin = plugin;
        setupDefaultConfig();
    }

    private void setupDefaultConfig() {
        if (plugin == null) return;
        if (!plugin.getConfig().contains("hats.abilities.void-save-chance")) plugin.getConfig().set("hats.abilities.void-save-chance", 12);
        if (!plugin.getConfig().contains("hats.abilities.one-hit-save-chance")) plugin.getConfig().set("hats.abilities.one-hit-save-chance", 8);
        if (!plugin.getConfig().contains("hats.abilities.invisibility-chance")) plugin.getConfig().set("hats.abilities.invisibility-chance", 12);
        if (!plugin.getConfig().contains("hats.abilities.invisibility-seconds")) plugin.getConfig().set("hats.abilities.invisibility-seconds", 5);
        if (!plugin.getConfig().contains("hats.abilities.blind-murder-chance")) plugin.getConfig().set("hats.abilities.blind-murder-chance", 10);
        if (!plugin.getConfig().contains("hats.abilities.blind-murder-seconds")) plugin.getConfig().set("hats.abilities.blind-murder-seconds", 4);
        try { plugin.saveConfig(); } catch (Throwable ignored) {}
    }

    public List<Hat> getHats() {
        return Arrays.asList(Hat.values());
    }

    public Hat getHatById(String id) {
        if (id == null) return null;
        for (Hat h : Hat.values()) if (h.getId().equalsIgnoreCase(id)) return h;
        return null;
    }

    private void load(Player p) {
        if (p == null) return;
        UUID uuid = p.getUniqueId();
        if (!selected.containsKey(uuid)) {
            String id = plugin.data != null ? plugin.data.getConfig().getString("HatData." + uuid + ".selected", Hat.NONE.getId()) : Hat.NONE.getId();
            Hat hat = getHatById(id);
            selected.put(uuid, hat == null ? Hat.NONE : hat);
        }
        if (!owned.containsKey(uuid)) {
            Set<String> set = new HashSet<String>();
            if (plugin.data != null) set.addAll(plugin.data.getConfig().getStringList("HatData." + uuid + ".owned"));
            set.add(Hat.NONE.getId());
            // Mantém os hats grátis para não quebrar jogadores que já tinham selecionado.
            for (Hat h : Hat.values()) set.add(h.getId());
            owned.put(uuid, set);
        }
    }

    private void save(Player p) {
        if (p == null || plugin.data == null) return;
        UUID uuid = p.getUniqueId();
        Hat hat = selected.containsKey(uuid) ? selected.get(uuid) : Hat.NONE;
        plugin.data.getConfig().set("HatData." + uuid + ".selected", hat.getId());
        plugin.data.getConfig().set("HatData." + uuid + ".owned", new ArrayList<String>(ownedSet(p)));
        plugin.data.save();
    }

    private Set<String> ownedSet(Player p) {
        load(p);
        return owned.get(p.getUniqueId());
    }

    public boolean owns(Player p, Hat hat) {
        return hat == null || hat == Hat.NONE || ownedSet(p).contains(hat.getId());
    }

    public void unlock(Player p, Hat hat) {
        if (hat == null) return;
        ownedSet(p).add(hat.getId());
        save(p);
    }

    public Hat getSelected(Player p) {
        load(p);
        Hat h = selected.get(p.getUniqueId());
        return h == null ? Hat.NONE : h;
    }

    public void setSelected(Player p, Hat hat) {
        load(p);
        selected.put(p.getUniqueId(), hat == null ? Hat.NONE : hat);
        save(p);
        applyVisualHat(p);
    }

    public ItemStack createHatItem(Hat hat) {
        ItemStack item = new ItemStack(hat.getMaterial(), 1, hat.getData());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(hat.getDisplay());
        List<String> lore = new ArrayList<String>();
        lore.add("§8hat:" + hat.getId());
        lore.add(hat.getDescription());
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public List<String> buildLore(Player p, Hat hat) {
        List<String> lore = new ArrayList<String>();
        lore.add(hat.getDescription());
        lore.add(" ");
        lore.add("§8Habilidade: §7" + hat.getAbility().name());
        lore.add(" ");
        if (owns(p, hat)) {
            lore.add(getSelected(p) == hat ? "§a§lEQUIPADO" : "§eClique para equipar.");
        } else {
            lore.add("§7Preço: §e" + hat.getPrice() + " coins");
            lore.add("§eClique para comprar.");
        }
        return lore;
    }

    public void applyVisualHat(Player p) {
        if (p == null || !p.isOnline()) return;
        Hat hat = getSelected(p);
        if (hat == Hat.NONE) {
            p.getInventory().setHelmet(null);
        } else {
            p.getInventory().setHelmet(createHatItem(hat));
        }
        p.updateInventory();
    }

    public void resetMatchUses(Player p) {
        if (p == null) return;
        UUID id = p.getUniqueId();
        usedVoidSave.remove(id);
        usedOneHitSave.remove(id);
        usedBlindMurder.remove(id);
        usedInvisibility.remove(id);
    }

    public void clearAll(Player p) {
        resetMatchUses(p);
        if (p == null) return;
        try { p.removePotionEffect(PotionEffectType.SPEED); } catch (Throwable ignored) {}
        try { p.removePotionEffect(PotionEffectType.INVISIBILITY); } catch (Throwable ignored) {}
        try { p.removePotionEffect(PotionEffectType.BLINDNESS); } catch (Throwable ignored) {}
    }

    public void applyGameAbility(Player p, Arena arena) {
        if (p == null || arena == null || !p.isOnline()) return;
        resetMatchUses(p);
        Hat hat = getSelected(p);
        applyVisualHat(p);

        if (hat.getAbility() == Ability.SPEED) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0), true);
            p.sendMessage("§aSeu chapéu ativou: §eVelocidade I§a.");
            return;
        }

        if (hat.getAbility() == Ability.INNOCENT_INVISIBILITY && arena.getType(p) == PlayerType.Innocents) {
            int chance = getChance("hats.abilities.invisibility-chance", 12);
            if (roll(chance)) {
                int seconds = plugin.getConfig().getInt("hats.abilities.invisibility-seconds", 5);
                usedInvisibility.add(p.getUniqueId());
                p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Math.max(1, seconds) * 20, 0), true);
                p.sendMessage("§dSeu chapéu ativou: §fCamuflagem por " + seconds + "s§d.");
                p.playSound(p.getLocation(), Sound.ENDERMAN_TELEPORT, 0.7F, 1.6F);
            }
        }
    }

    public boolean tryVoidSave(Player p, Arena arena) {
        if (p == null || arena == null || arena.getState() != GameState.INGAME) return false;
        if (getSelected(p).getAbility() != Ability.TNT_VOID_SAVE) return false;
        UUID id = p.getUniqueId();
        if (usedVoidSave.contains(id)) return false;

        int chance = getChance("hats.abilities.void-save-chance", 12);
        if (!roll(chance)) return false;

        usedVoidSave.add(id);
        try {
            p.setFallDistance(0F);
            p.setFireTicks(0);
            p.teleport(plugin.getSpawn(arena, 0));
            p.setHealth(p.getMaxHealth());
        } catch (Throwable ignored) {}
        p.sendMessage("§aSeu chapéu ativou: §fAnti-Void§a! Você escapou dessa vez.");
        p.playSound(p.getLocation(), Sound.ENDERMAN_TELEPORT, 1F, 1.1F);
        return true;
    }

    public boolean tryPreventLethalHit(Player victim, Player damager, Arena arena, String source) {
        if (victim == null || arena == null || arena.getState() != GameState.INGAME) return false;
        Hat hat = getSelected(victim);

        // Guardião: pequena chance de sobreviver a faca/arma, uma vez por partida.
        if (hat.getAbility() == Ability.ONE_HIT_SAVE && !usedOneHitSave.contains(victim.getUniqueId())) {
            int chance = getChance("hats.abilities.one-hit-save-chance", 8);
            if (roll(chance)) {
                usedOneHitSave.add(victim.getUniqueId());
                safeRestore(victim);
                victim.sendMessage("§aSeu chapéu ativou: §fGuardião§a! Você sobreviveu ao ataque.");
                if (damager != null) damager.sendMessage("§eO chapéu de §f" + victim.getName() + " §ebloqueou seu ataque!");
                playBlockEffect(victim);
                return true;
            }
        }

        // Olho Cego: se o Murder tentar matar, pequena chance de cegar o Murder e salvar a vítima.
        if (hat.getAbility() == Ability.BLIND_MURDER && damager != null && arena.getType(damager) == PlayerType.Murderer
                && !usedBlindMurder.contains(victim.getUniqueId())) {
            int chance = getChance("hats.abilities.blind-murder-chance", 10);
            if (roll(chance)) {
                usedBlindMurder.add(victim.getUniqueId());
                safeRestore(victim);
                int seconds = plugin.getConfig().getInt("hats.abilities.blind-murder-seconds", 4);
                damager.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Math.max(1, seconds) * 20, 1), true);
                victim.sendMessage("§aSeu chapéu ativou: §fOlho Cego§a! O assassino ficou cego.");
                damager.sendMessage("§cO chapéu de §f" + victim.getName() + " §cte cegou por " + seconds + "s!");
                playBlockEffect(victim);
                return true;
            }
        }

        return false;
    }

    private int getChance(String path, int def) {
        int value = plugin.getConfig().getInt(path, def);
        if (value < 0) value = 0;
        if (value > 100) value = 100;
        return value;
    }

    private boolean roll(int chancePercent) {
        return chancePercent > 0 && random.nextInt(100) < chancePercent;
    }

    private void safeRestore(Player p) {
        try {
            p.setFireTicks(0);
            p.setFallDistance(0F);
            p.setHealth(p.getMaxHealth());
        } catch (Throwable ignored) {}
    }

    private void playBlockEffect(Player p) {
        try {
            p.getWorld().playEffect(p.getLocation().add(0, 1, 0), Effect.ENDER_SIGNAL, 1);
            p.playSound(p.getLocation(), Sound.ITEM_BREAK, 0.7F, 0.8F);
        } catch (Throwable ignored) {}
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        if (e.getSlot() == 39 && getSelected(p) != Hat.NONE) {
            e.setCancelled(true);
            p.sendMessage("§cUse a Loja para trocar ou remover seu chapéu.");
        }
    }

    public void playSelect(Player p) {
        p.playSound(p.getLocation(), Sound.LEVEL_UP, 1.0F, 1.2F);
    }
}
