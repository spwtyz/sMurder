package me.spwtyz.murder.cosmetics;

import java.util.*;
import java.lang.reflect.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;
import java.lang.reflect.Field;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Item;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.util.Vector;
import org.bukkit.util.EulerAngle;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.scheduler.BukkitRunnable;

import me.spwtyz.murder.Arenas;
import me.spwtyz.murder.Main;

/**
 * Faz Auras e Gadgets funcionarem no lobby principal.
 * Nao roda dentro de partida/sala para nao pesar e nao interferir na gameplay.
 */
public class LobbyCosmeticManager implements Listener {

    private final Main plugin;
    private final Map<UUID, Long> gadgetCooldown = new HashMap<UUID, Long>();
    private final Map<UUID, ItemStack> originalEmoteHelmets = new HashMap<UUID, ItemStack>();
    private final Map<UUID, Integer> emoteTicks = new HashMap<UUID, Integer>();
    private final Map<UUID, String> lastEmote = new HashMap<UUID, String>();
    private final Map<UUID, ArmorStand> companionStands = new HashMap<UUID, ArmorStand>();
    private final Map<UUID, List<ArmorStand>> companionParts = new HashMap<UUID, List<ArmorStand>>();
    private final Map<UUID, String> lastCompanion = new HashMap<UUID, String>();
    private final Map<UUID, Integer> companionTicks = new HashMap<UUID, Integer>();
    private final Map<UUID, ArmorStand> balloonStands = new HashMap<UUID, ArmorStand>();
    private final Map<UUID, ItemStack> originalAnimatedHatHelmets = new HashMap<UUID, ItemStack>();
    private final Map<UUID, String> lastAnimatedHat = new HashMap<UUID, String>();
    private final Map<UUID, Location> lastTrailLocation = new HashMap<UUID, Location>();

    public LobbyCosmeticManager(Main plugin) {
        this.plugin = plugin;
        startAuraTask();
        startGadgetItemTask();
        startEmoteTask();
        startCompanionTask();
        startBalloonTask();
        startAnimatedHatTask();
        startWalkTrailTask();
    }

    private boolean canUseLobbyCosmetics(Player p) {
        return p != null && p.isOnline() && !Arenas.isInArena(p);
    }

    private void startAuraTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!canUseLobbyCosmetics(p)) continue;
                    String aura = CosmeticEffectManager.getSelected(plugin, p, "aura");
                    if (aura == null || aura.equalsIgnoreCase("nenhum")) continue;
                    playAura(p, aura);
                }
            }
        }.runTaskTimer(plugin, 20L, 10L);
    }

    private void startGadgetItemTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!canUseLobbyCosmetics(p)) continue;
                    String gadget = CosmeticEffectManager.getSelected(plugin, p, "gadget");
                    if (gadget == null || gadget.equalsIgnoreCase("nenhum")) {
                        clearGadgetItem(p);
                        continue;
                    }
                    giveGadgetItem(p, gadget);
                }
            }
        }.runTaskTimer(plugin, 40L, 60L);
    }

    private void giveGadgetItem(Player p, String gadget) {
        ItemStack current = p.getInventory().getItem(6);
        if (isGadgetItem(current)) {
            if (current.hasItemMeta() && current.getItemMeta().hasLore()) {
                for (String line : current.getItemMeta().getLore()) {
                    if (line != null && line.equals("§8gadget-id:" + gadget)) return;
                }
            }
        }

        ItemStack item = new ItemStack(gadgetMaterial(gadget));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(gadgetName(gadget));
        java.util.List<String> lore = new java.util.ArrayList<String>();
        lore.add("§8altamc:gadget");
        lore.add("§8gadget-id:" + gadget);
        lore.add("§7Clique direito para usar.");
        lore.add("§7Funciona apenas no lobby.");
        meta.setLore(lore);
        item.setItemMeta(meta);
        p.getInventory().setItem(6, item);
    }

    private boolean isGadgetItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasLore()) return false;
        for (String line : item.getItemMeta().getLore()) {
            if (line != null && line.equals("§8altamc:gadget")) return true;
        }
        return false;
    }

    private void clearGadgetItem(Player p) {
        ItemStack current = p.getInventory().getItem(6);
        if (isGadgetItem(current)) {
            p.getInventory().setItem(6, null);
        }
    }


    private void startEmoteTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!canUseLobbyCosmetics(p)) {
                        removeEmote(p.getUniqueId());
                        continue;
                    }
                    String emote = CosmeticEffectManager.getSelected(plugin, p, "emote");
                    if (emote == null || emote.equalsIgnoreCase("nenhum")) {
                        removeEmote(p.getUniqueId());
                        continue;
                    }
                    updateEmote(p, emote);
                }
            }
        }.runTaskTimer(plugin, 20L, 4L);
    }

    private void updateEmote(Player p, String emoteId) {
        UUID uuid = p.getUniqueId();
        String last = lastEmote.get(uuid);

        if (last == null || !last.equalsIgnoreCase(emoteId)) {
            restoreEmoteHelmet(uuid);
            ItemStack currentHelmet = p.getInventory().getHelmet();
            originalEmoteHelmets.put(uuid, currentHelmet == null ? null : currentHelmet.clone());
            lastEmote.put(uuid, emoteId);
            emoteTicks.put(uuid, 0);
        }

        int tick = emoteTicks.containsKey(uuid) ? emoteTicks.get(uuid) + 1 : 1;
        emoteTicks.put(uuid, tick);

        // A head fica equipada na cabeça do jogador. Para animar, trocamos a textura.
        if (tick == 1 || tick % 3 == 0) {
            p.getInventory().setHelmet(createEmoteHead(emoteId, tick));
        }

        if (tick % 12 == 0) playEmoteParticles(p, emoteId, p.getLocation().clone().add(0, 1.85D, 0));
    }

    private void removeEmote(UUID uuid) {
        restoreEmoteHelmet(uuid);
        emoteTicks.remove(uuid);
        lastEmote.remove(uuid);
    }

    private void restoreEmoteHelmet(UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        ItemStack old = originalEmoteHelmets.remove(uuid);
        if (p != null && p.isOnline()) {
            p.getInventory().setHelmet(old == null ? null : old.clone());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        removeEmote(e.getPlayer().getUniqueId());
        removeCompanion(e.getPlayer().getUniqueId());
        removeBalloon(e.getPlayer().getUniqueId());
        restoreAnimatedHat(e.getPlayer().getUniqueId());
        lastTrailLocation.remove(e.getPlayer().getUniqueId());
    }


    private void startCompanionTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!canUseLobbyCosmetics(p)) {
                        removeCompanion(p.getUniqueId());
                        continue;
                    }

                    String companion = CosmeticEffectManager.getSelected(plugin, p, "companion");
                    if (companion == null || companion.equalsIgnoreCase("nenhum")) {
                        removeCompanion(p.getUniqueId());
                        continue;
                    }
                    updateCompanion(p, companion);
                }
            }
        }.runTaskTimer(plugin, 20L, 2L);
    }

    private void updateCompanion(Player p, String companionId) {
        UUID uuid = p.getUniqueId();
        List<ArmorStand> parts = companionParts.get(uuid);
        String last = lastCompanion.get(uuid);

        if (parts == null || parts.isEmpty() || hasDeadPart(parts) || last == null || !last.equalsIgnoreCase(companionId)) {
            removeCompanion(uuid);
            parts = spawnCompanionModel(p, companionId);
            companionParts.put(uuid, parts);
            lastCompanion.put(uuid, companionId);
            companionTicks.put(uuid, 0);
            if (!parts.isEmpty()) companionStands.put(uuid, parts.get(0));
        }

        int tick = companionTicks.containsKey(uuid) ? companionTicks.get(uuid) + 1 : 1;
        companionTicks.put(uuid, tick);

        Location center = getStableCompanionLocation(p, tick);
        moveStableCompanion(parts, companionId, center, tick);

        if (tick % 16 == 0) playCompanionParticles(p, companionId, center.clone().add(0, 0.95D, 0));
        if (tick % 80 == 0) refreshCompanionHeads(parts, companionId, tick);
    }

    private Location getStableCompanionLocation(Player p, int tick) {
        Location base = p.getLocation().clone();
        double yaw = Math.toRadians(base.getYaw());

        // Estilo kCosmetics, mas estavel para 1.8: fica ao lado/atras do jogador,
        // sem orbitar em cima do player e sem ficar atravessando o corpo.
        double side = 0.85D;
        double back = 0.95D;
        double x = (-Math.cos(yaw) * side) + (Math.sin(yaw) * back);
        double z = (-Math.sin(yaw) * side) - (Math.cos(yaw) * back);
        double y = 0.05D + Math.sin(tick * 0.18D) * 0.06D;

        Location loc = base.add(x, y, z);
        loc.setYaw(base.getYaw());
        return loc;
    }

    private boolean hasDeadPart(List<ArmorStand> parts) {
        for (ArmorStand st : parts) {
            if (st == null || st.isDead()) return true;
        }
        return false;
    }

    private List<ArmorStand> spawnCompanionModel(Player p, String companionId) {
        List<ArmorStand> list = new ArrayList<ArmorStand>();
        Location base = getStableCompanionLocation(p, 0);

        // Versao estavel: cada companion usa poucas partes bem alinhadas.
        // Isso evita o bug visual de varias heads separando/voando na 1.8.
        addCompanionPart(list, base, companionId, "body");
        addCompanionPart(list, base, companionId, "head");

        if (isDragonCompanion(companionId)) {
            addCompanionPart(list, base, companionId, "l_wing");
            addCompanionPart(list, base, companionId, "r_wing");
            addCompanionPart(list, base, companionId, "tail");
        } else if (companionId != null && companionId.toLowerCase().contains("robot")) {
            addCompanionPart(list, base, companionId, "l_arm");
            addCompanionPart(list, base, companionId, "r_arm");
        } else if (companionId != null && companionId.toLowerCase().contains("ghost")) {
            addCompanionPart(list, base, companionId, "aura1");
        } else if (companionId != null && companionId.toLowerCase().contains("among_us")) {
            addCompanionPart(list, base, companionId, "backpack");
        }

        if (!list.isEmpty()) {
            ArmorStand first = list.get(0);
            first.setCustomName(companionDisplayName(companionId));
            first.setCustomNameVisible(false);
        }
        return list;
    }

    private ArmorStand addCompanionPart(List<ArmorStand> list, Location base, String companionId, String part) {
        ArmorStand stand = (ArmorStand) base.getWorld().spawnEntity(base, EntityType.ARMOR_STAND);
        stand.setGravity(false);
        stand.setVisible(false);
        stand.setSmall(true);
        stand.setCanPickupItems(false);
        try { stand.setBasePlate(false); } catch (Throwable ignored) {}
        try { stand.setArms(false); } catch (Throwable ignored) {}
        stand.setHelmet(createCompanionPartItem(companionId, part, 0));
        stand.setMetadata("altamc_companion", new FixedMetadataValue(plugin, true));
        list.add(stand);
        return stand;
    }

    private void moveStableCompanion(List<ArmorStand> parts, String companionId, Location center, int tick) {
        if (parts == null || parts.isEmpty()) return;

        String lower = companionId == null ? "" : companionId.toLowerCase();
        double bounce = Math.sin(tick * 0.18D) * 0.06D;
        double look = Math.sin(tick * 0.12D) * 10.0D;

        // body
        movePart(parts, 0, center, 0.0D, 0.35D + bounce, 0.0D, 0, 0, 0);
        // head
        movePart(parts, 1, center, 0.0D, 0.77D + bounce, -0.10D, 0, look, 0);

        if (isDragonCompanion(companionId)) {
            double flap = Math.sin(tick * 0.35D) * 28.0D;
            movePart(parts, 2, center, 0.34D, 0.58D + bounce, 0.06D, 90, 70, 150 + flap);
            movePart(parts, 3, center, -0.34D, 0.58D + bounce, 0.06D, -90, -70, 30 - flap);
            movePart(parts, 4, center, 0.0D, 0.30D + bounce, 0.42D, 70, 0, 180);
            return;
        }

        if (lower.contains("robot")) {
            double swing = Math.sin(tick * 0.22D) * 18.0D;
            movePart(parts, 2, center, 0.26D, 0.48D + bounce, 0.0D, swing, 0, 12);
            movePart(parts, 3, center, -0.26D, 0.48D + bounce, 0.0D, -swing, 0, -12);
            return;
        }

        if (lower.contains("ghost")) {
            double spin = tick * 6.0D;
            movePart(parts, 2, center, Math.cos(Math.toRadians(spin)) * 0.25D, 0.55D + bounce, Math.sin(Math.toRadians(spin)) * 0.25D, 0, spin, 0);
            return;
        }

        if (lower.contains("among_us")) {
            movePart(parts, 2, center, 0.0D, 0.47D + bounce, 0.30D, 0, 0, 0);
        }
    }

    private void movePart(List<ArmorStand> parts, int index, Location center, double x, double y, double z, double pitch, double yaw, double roll) {
        if (index < 0 || index >= parts.size()) return;
        ArmorStand stand = parts.get(index);
        if (stand == null || stand.isDead()) return;
        Location loc = center.clone().add(rotateOffset(x, y, z, center.getYaw()));
        loc.setYaw(center.getYaw());
        stand.teleport(loc);
        try { stand.setHeadPose(new EulerAngle(Math.toRadians(pitch), Math.toRadians(yaw), Math.toRadians(roll))); } catch (Throwable ignored) {}
    }

    private Vector rotateOffset(double x, double y, double z, float yaw) {
        double rad = Math.toRadians(-yaw);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        double nx = x * cos - z * sin;
        double nz = x * sin + z * cos;
        return new Vector(nx, y, nz);
    }

    private void refreshCompanionHeads(List<ArmorStand> parts, String companionId, int tick) {
        if (parts == null) return;
        String[] names = companionPartNames(companionId);
        for (int i = 0; i < parts.size() && i < names.length; i++) {
            ArmorStand st = parts.get(i);
            if (st != null && !st.isDead()) st.setHelmet(createCompanionPartItem(companionId, names[i], tick));
        }
    }

    private String[] companionPartNames(String companionId) {
        if (isDragonCompanion(companionId)) return new String[] {"body", "head", "l_wing", "r_wing", "tail"};
        if (companionId != null && companionId.toLowerCase().contains("robot")) return new String[] {"body", "head", "l_arm", "r_arm"};
        if (companionId != null && companionId.toLowerCase().contains("among_us")) return new String[] {"body", "head", "backpack"};
        if (companionId != null && companionId.toLowerCase().contains("ghost")) return new String[] {"body", "head", "aura1"};
        return new String[] {"body", "head"};
    }

    private boolean isDragonCompanion(String id) {
        return id != null && id.toLowerCase().contains("dragon");
    }

    private void removeCompanion(UUID uuid) {
        ArmorStand stand = companionStands.remove(uuid);
        if (stand != null && !stand.isDead()) stand.remove();
        List<ArmorStand> parts = companionParts.remove(uuid);
        if (parts != null) {
            for (ArmorStand part : parts) {
                if (part != null && !part.isDead()) part.remove();
            }
        }
        lastCompanion.remove(uuid);
        companionTicks.remove(uuid);
    }

    private void playCompanionParticles(Player p, String companionId, Location loc) {
        if (companionId == null) return;
        if (companionId.contains("dragon_fire")) {
            p.getWorld().playEffect(loc, Effect.MOBSPAWNER_FLAMES, 0);
            return;
        }
        if (companionId.contains("dragon_ice")) {
            p.getWorld().playEffect(loc, Effect.SNOWBALL_BREAK, 0);
            return;
        }
        if (companionId.contains("ghost")) {
            p.getWorld().playEffect(loc, Effect.SMOKE, 0);
            return;
        }
        if (companionId.contains("robot")) {
            p.getWorld().playEffect(loc, Effect.CRIT, 0);
            return;
        }
        if (companionId.contains("among_us")) {
            p.getWorld().playEffect(loc, Effect.HAPPY_VILLAGER, 0);
            return;
        }
        p.getWorld().playEffect(loc, Effect.ENDER_SIGNAL, 0);
    }

    private String companionDisplayName(String id) {
        if (id == null) return "§eCompanion";
        if (id.equalsIgnoreCase("companion_mini_murder")) return "§cMini Murder";
        if (id.equalsIgnoreCase("companion_mini_detective")) return "§bMini Detective";
        if (id.equalsIgnoreCase("companion_ghost")) return "§fFantasma";
        if (id.equalsIgnoreCase("companion_dragon_fire")) return "§6Dragão de Fogo";
        if (id.equalsIgnoreCase("companion_dragon_ice")) return "§bDragão de Gelo";
        if (id.equalsIgnoreCase("companion_among_us")) return "§aMini Among Us";
        if (id.equalsIgnoreCase("companion_koala")) return "§7Koala";
        if (id.equalsIgnoreCase("companion_panda")) return "§fPanda";
        if (id.equalsIgnoreCase("companion_robot")) return "§8R3D3";
        return "§eCompanion";
    }


    private ItemStack createCompanionPartItem(String companionId, String part, int tick) {
        String lower = companionId == null ? "" : companionId.toLowerCase();
        String partLower = part == null ? "head" : part.toLowerCase();

        // kCosmetics 1.8: asas e cauda do dragao NAO sao skulls/custom heads.
        // Sao BANNER/WOOL/CARPET; usar skull nelas faz virar Steve quando a texture nao existe.
        if (isDragonCompanion(companionId)) {
            short color = (short) (lower.contains("ice") ? 3 : lower.contains("magic") ? 10 : lower.contains("mythic") ? 5 : 14);
            if (partLower.contains("wing")) return namedItem(new ItemStack(Material.BANNER, 1, color), companionDisplayName(companionId));
            if (partLower.contains("tail") || partLower.contains("body")) return namedItem(new ItemStack(Material.WOOL, 1, color), companionDisplayName(companionId));
            return createCompanionPartHead(companionId, part, tick);
        }

        return createCompanionPartHead(companionId, part, tick);
    }

    private ItemStack namedItem(ItemStack item, String name) {
        try {
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        } catch (Throwable ignored) {}
        return item;
    }

    private ItemStack createCompanionPartHead(String companionId, String part, int tick) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setDisplayName(companionDisplayName(companionId));
        try {
            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            profile.getProperties().put("textures", new Property("textures", companionPartTextureValue(companionId, part, tick)));
            Field field = meta.getClass().getDeclaredField("profile");
            field.setAccessible(true);
            field.set(meta, profile);
        } catch (Throwable ignored) {
            meta.setOwner("MHF_Chest");
        }
        head.setItemMeta(meta);
        return head;
    }

    private String companionPartTextureValue(String id, String part, int tick) {
        if (id == null) return textureValueFromHash("a116e69a845e227f7ca1fdde8c357c8c821ebd4ba619382ea4a1f87d4ae94");
        String lower = id.toLowerCase();
        if (part == null) part = "head";
        part = part.toLowerCase();

        if (lower.contains("dragon_fire")) {
            if (part.contains("wing")) return textureValueFromHash("e65f5182d372a4a05aeb0c6b2e503d3529711b4ec45b2a9885b7a9123f96976");
            if (part.contains("tail")) return textureValueFromHash("b6c4a751bd8529e1f4532ac759ce29ff46d9bb89d2a0a255e6a863d8b4e2c71");
            if (part.contains("body")) return textureValueFromHash("4b36d79ba407053c251bd4aa913a5e281ccd24d8d9ebe93d28bb62f140adc5");
            return textureValueFromHash("4b36d79ba407053c251bd4aa913a5e281ccd24d8d9ebe93d28bb62f140adc5");
        }
        if (lower.contains("dragon_ice")) {
            if (part.contains("wing")) return textureValueFromHash("1cdcecff4f6ba0b246f7775753d791f16c9c6de5535d795236a4f3c8c9b293b");
            if (part.contains("tail")) return textureValueFromHash("6d6bb6c4f6419f44ab2e17c59483da8e21fd47eec3c422173166f2f02e52e57");
            return textureValueFromHash("1cdcecff4f6ba0b246f7775753d791f16c9c6de5535d795236a4f3c8c9b293b");
        }
        if (lower.contains("robot")) {
            if (part.contains("arm")) return textureValueFromHash("7cebc97798c2e360551cab3dd5db6d53497fe63040941c9ac491a59cbf383a7a");
            if (part.contains("body")) return textureValueFromHash("9d9d3c8d50f1e92e836b98ff95a164f7db1f24d870b6e7e45d52e949dc90018");
            return textureValueFromHash("7cebc97798c2e360551cab3dd5db6d53497fe63040941c9ac491a59cbf383a7a");
        }
        if (lower.contains("among_us")) {
            if (part.contains("visor")) return textureValueFromHash("47b4f84e19b52f31217712e7ba9f51d56da59d2445b4d7f39ef6c323b8166");
            if (part.contains("backpack")) return textureValueFromHash("a116e69a845e227f7ca1fdde8c357c8c821ebd4ba619382ea4a1f87d4ae94");
            return textureValueFromHash("a116e69a845e227f7ca1fdde8c357c8c821ebd4ba619382ea4a1f87d4ae94");
        }
        if (lower.contains("ghost")) {
            if (part.contains("aura")) return textureValueFromHash("9b2fe2e18fdb53c8ed8b8eb7a17f88b93f92522c80ee26e65ae1647d4a3f2c");
            return textureValueFromHash("e5d9a69a3ec84e48f05af40f3dad1c9d0514d6bd6f7f15c89f9576a9f7db9b6");
        }

        // Animais: usa as textures principais do kCosmetics como base, e partes extras com tons parecidos.
        String main = "947b68ed021632f408fc223ef7957c24786ae509a84e6f18a371a55c3d8cf909"; // panda
        if (lower.contains("koala")) main = "8d87e829f2d489ff6ee6682639e339a2123d3449d27f8bda51528c6076fb9f2a";
        if (lower.contains("panda")) main = "947b68ed021632f408fc223ef7957c24786ae509a84e6f18a371a55c3d8cf909";
        if (lower.contains("mini_murder")) main = "a116e69a845e227f7ca1fdde8c357c8c821ebd4ba619382ea4a1f87d4ae94";
        if (lower.contains("mini_detective")) main = "47b4f84e19b52f31217712e7ba9f51d56da59d2445b4d7f39ef6c323b8166";
        return textureValueFromHash(main);
    }

    private void playEmoteParticles(Player p, String emoteId, Location loc) {
        if (emoteId.equalsIgnoreCase("emote_cry")) {
            p.getWorld().playEffect(loc.clone().add(0, -0.15D, 0), Effect.WATERDRIP, 0);
            p.playSound(p.getLocation(), Sound.NOTE_BASS, 0.15f, 1.8f);
            return;
        }
        if (emoteId.equalsIgnoreCase("emote_rage")) {
            p.getWorld().playEffect(loc, Effect.MOBSPAWNER_FLAMES, 0);
            return;
        }
        if (emoteId.equalsIgnoreCase("emote_heart")) {
            p.getWorld().playEffect(loc, Effect.HEART, 0);
            return;
        }
        if (emoteId.equalsIgnoreCase("emote_cool")) {
            p.getWorld().playEffect(loc, Effect.CRIT, 0);
            return;
        }
        p.getWorld().playEffect(loc, Effect.HAPPY_VILLAGER, 0);
    }

    private ItemStack createEmoteHead(String emoteId, int tick) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setDisplayName(emoteDisplayName(emoteId));
        try {
            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            profile.getProperties().put("textures", new Property("textures", emoteTexture(emoteId, tick)));
            Field field = meta.getClass().getDeclaredField("profile");
            field.setAccessible(true);
            field.set(meta, profile);
        } catch (Throwable ignored) {
            meta.setOwner("MHF_Smile");
        }
        head.setItemMeta(meta);
        return head;
    }

    private String emoteDisplayName(String id) {
        if (id == null) return "§dEmote";
        if (id.equalsIgnoreCase("emote_cry")) return "§bEmote Chorando";
        if (id.equalsIgnoreCase("emote_rage")) return "§cEmote Bravo";
        if (id.equalsIgnoreCase("emote_cool")) return "§9Emote Cool";
        if (id.equalsIgnoreCase("emote_heart")) return "§dEmote Apaixonado";
        return "§eEmote Feliz";
    }

    private String emoteTexture(String id, int tick) {
        return textureValueFromHash(emoteHashAt(id, tick));
    }

    private String textureValueFromHash(String hash) {
        if (hash == null || hash.isEmpty()) return "";
        String json = "{textures:{SKIN:{url:\"http://textures.minecraft.net/texture/" + hash + "\"}}}";
        return Base64.getEncoder().encodeToString(json.getBytes());
    }

    private String emoteHashAt(String id, int tick) {
        if (id == null) id = "emote_smile";

        if (id.equalsIgnoreCase("emote_cry")) {
            return frameByDuration(tick, new int[] {21, 3, 3, 3, 1, 1, 1, 1, 1}, new String[] {
                    "bd40f71c40fc308718d39813b2a8fd9602f44b803a5b587d3f3b131e4b8c61",
                    "1b9932b5658f4cac4f4f8d9e98f6dcee2e17744169ccb5c8145365f17d445f3",
                    "8987e5c3859823aec3cdebd2be7ada04aeed30a3a83381c4df9c4762f0c9d1",
                    "b0966259f2e1f97dbf19662ad1d003d5af5dd8b85384bfbb6237839ae9925",
                    "3e6ef5e168ed65936c74a3351e9bb7e4ff0133bed5af27dccc625d92a3fe91f",
                    "a8468f09a57bc3bbe614f6bcfdf203f52dfe57b401471acc18977bffa9bfecf",
                    "652631c8593bcd643761aa81d36aa96dd39c899d2b90eca70e035c4410ae9e",
                    "a1226aa3c1429363ddb5bcdf6f28556287eb2c554c847789d508c47cfae3b8d",
                    "45436e46e4885eeb9882089601e9852f6546ad7b2101cf47b4369156a44b7ba"
            });
        }

        if (id.equalsIgnoreCase("emote_rage")) {
            return frameByDuration(tick, new int[] {7, 4, 3, 4, 1, 1}, new String[] {
                    "2c74e6f64c837671b17694259b07cd5c79fbf8bfd3227332a5191e6ab11f3d5",
                    "ad96543cf9304e9a97ecccbdc416b45bb48feb5a0bd1518a52da4b2f7386",
                    "2f9aad5c98a444576fe9330e0a36e5b614956b1901ed4c8b1b97114544099",
                    "973fd955ca4389b68642483e53e5e2f1fabafc2416fc8e95d43694b76c5a81",
                    "1a9ee344c3def5b3f3593e26ce0f52471d95993393e497ed6a77e38ec8a221",
                    "9cb2253e5f3e41953757724d458698f3c3137279764dbee3d56be776cddccf79"
            });
        }

        if (id.equalsIgnoreCase("emote_cool")) {
            return frameByDuration(tick, new int[] {16, 1, 61}, new String[] {
                    "a21e6dbfd74a1859ddbae3380fc1ab71f2389745945fc92329b164635bd14f",
                    "3733db9a94bfe15cdbb7ca5832c85cfada98ad2c839934766bdc41f977b5c163",
                    "766b3eef3c726ecb816c43839189eeb8e36382e3e5fe41128372785185a322"
            });
        }

        if (id.equalsIgnoreCase("emote_heart")) {
            return frameByDuration(tick, new int[] {10, 2, 2, 2, 2, 2, 10, 5}, new String[] {
                    "901b958ed2c36e45bae72b42d4ee719d45240b233669091b1cc9e070e31119",
                    "895f6415bd9424a664d694371a846838c20fb36c3b4a22f385fe7e3dce2996",
                    "96fbb52a4d0c62d8e6cae8c485e551b37fec68e6daab23d85f2ff52faa4c4",
                    "fd26ae4b5793d087e62a2cf3f34359829d02869aae6626bfcff59de1469f51",
                    "96fbb52a4d0c62d8e6cae8c485e551b37fec68e6daab23d85f2ff52faa4c4",
                    "fd26ae4b5793d087e62a2cf3f34359829d02869aae6626bfcff59de1469f51",
                    "895f6415bd9424a664d694371a846838c20fb36c3b4a22f385fe7e3dce2996",
                    "901b958ed2c36e45bae72b42d4ee719d45240b233669091b1cc9e070e31119"
            });
        }

        return frameByDuration(tick, new int[] {5, 1, 11, 1, 11, 1, 3}, new String[] {
                "264614ad4bb2eb61b06b1a8b5d57f02448a975a8217ec16571f87c49227cbd",
                "60c432cbc490a8af6e9dfeb28095c0a0ec79fff705fb184674d1e743bd05baa",
                "41ac21d93ce17f2b7ee2e0e07a983eeb4a539e341ce5c77c36c722f77a2235",
                "4168b716281635ceafc3268dfa7d5f46466c8032e11c1cfb7db711a9f647d",
                "41ac21d93ce17f2b7ee2e0e07a983eeb4a539e341ce5c77c36c722f77a2235",
                "4168b716281635ceafc3268dfa7d5f46466c8032e11c1cfb7db711a9f647d",
                "41ac21d93ce17f2b7ee2e0e07a983eeb4a539e341ce5c77c36c722f77a2235"
        });
    }

    private String frameByDuration(int tick, int[] durations, String[] hashes) {
        if (hashes == null || hashes.length == 0) return "";
        int total = 0;
        for (int duration : durations) total += Math.max(1, duration);
        int cursor = tick % Math.max(1, total);
        int passed = 0;
        for (int i = 0; i < hashes.length; i++) {
            passed += Math.max(1, durations[Math.min(i, durations.length - 1)]);
            if (cursor < passed) return hashes[i];
        }
        return hashes[hashes.length - 1];
    }

    @EventHandler
    public void onUseGadget(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player p = e.getPlayer();
        if (!canUseLobbyCosmetics(p)) return;
        if (!isGadgetItem(p.getItemInHand())) return;

        e.setCancelled(true);
        String selected = CosmeticEffectManager.getSelected(plugin, p, "gadget");
        if (selected == null || selected.equalsIgnoreCase("nenhum")) {
            p.sendMessage("§cVoce nao possui gadget equipado.");
            return;
        }

        long now = System.currentTimeMillis();
        long last = gadgetCooldown.containsKey(p.getUniqueId()) ? gadgetCooldown.get(p.getUniqueId()) : 0L;
        if (now - last < 5000L) {
            p.sendMessage("§cAguarde para usar o gadget novamente.");
            return;
        }
        gadgetCooldown.put(p.getUniqueId(), now);
        playGadget(p, selected);
    }

    private void playAura(Player p, String aura) {
        Location loc = p.getLocation().clone().add(0, 0.25D, 0);
        if (aura.equalsIgnoreCase("aura_blood")) {
            p.getWorld().playEffect(loc, Effect.STEP_SOUND, Material.REDSTONE_BLOCK.getId());
            return;
        }
        if (aura.equalsIgnoreCase("aura_dark")) {
            p.getWorld().playEffect(loc, Effect.SMOKE, 0);
            return;
        }
        if (aura.equalsIgnoreCase("aura_magic")) {
            p.getWorld().playEffect(loc.clone().add(0, 0.7D, 0), Effect.ENDER_SIGNAL, 0);
            return;
        }
        if (aura.startsWith("seasonal_")) {
            if (aura.contains("halloween")) {
                p.getWorld().playEffect(loc, Effect.SMOKE, 0);
                p.getWorld().playEffect(loc.clone().add(0, 0.6D, 0), Effect.MOBSPAWNER_FLAMES, 0);
            } else if (aura.contains("christmas")) {
                p.getWorld().playEffect(loc, Effect.SNOWBALL_BREAK, 0);
            } else if (aura.contains("easter")) {
                p.getWorld().playEffect(loc.clone().add(0, 0.7D, 0), Effect.HAPPY_VILLAGER, 0);
            } else {
                p.getWorld().playEffect(loc, Effect.ENDER_SIGNAL, 0);
            }
        }
    }

    private Material gadgetMaterial(String gadget) {
        if (gadget == null) return Material.ENDER_CHEST;
        if (gadget.equalsIgnoreCase("gadget_firework")) return Material.FIREWORK;
        if (gadget.equalsIgnoreCase("gadget_smoke")) return Material.SULPHUR;
        if (gadget.equalsIgnoreCase("gadget_heart")) return Material.RED_ROSE;
        if (gadget.equalsIgnoreCase("gadget_grappling")) return Material.FISHING_ROD;
        if (gadget.equalsIgnoreCase("gadget_trampoline")) return Material.SLIME_BLOCK;
        if (gadget.equalsIgnoreCase("gadget_tnt_fake")) return Material.TNT;
        if (gadget.equalsIgnoreCase("gadget_paint")) return Material.INK_SACK;
        if (gadget.equalsIgnoreCase("gadget_carpet")) return Material.CARPET;
        return Material.ENDER_CHEST;
    }

    private String gadgetName(String gadget) {
        if (gadget == null) return "§aGadget";
        if (gadget.equalsIgnoreCase("gadget_firework")) return "§aFirework Gadget";
        if (gadget.equalsIgnoreCase("gadget_smoke")) return "§7Smoke Bomb";
        if (gadget.equalsIgnoreCase("gadget_heart")) return "§dLove Bomb";
        if (gadget.equalsIgnoreCase("gadget_grappling")) return "§bGrappling Hook";
        if (gadget.equalsIgnoreCase("gadget_trampoline")) return "§aTrampolim";
        if (gadget.equalsIgnoreCase("gadget_tnt_fake")) return "§cTNT Fake";
        if (gadget.equalsIgnoreCase("gadget_paint")) return "§dPaint Gun";
        if (gadget.equalsIgnoreCase("gadget_carpet")) return "§5Magic Carpet";
        return "§aGadget";
    }

    private void pulse(final Player p, final Effect effect, final Sound sound, final int loops, final long delay) {
        if (sound != null) p.playSound(p.getLocation(), sound, 1f, 1.2f);
        new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                if (!canUseLobbyCosmetics(p) || i++ >= loops) { cancel(); return; }
                Location base = p.getLocation().clone().add(0, 0.9D, 0);
                for (int a = 0; a < 12; a++) {
                    double angle = (a / 12.0D) * Math.PI * 2D + (i * 0.35D);
                    Location l = base.clone().add(Math.cos(angle) * 1.2D, Math.sin(i * 0.4D) * 0.2D, Math.sin(angle) * 1.2D);
                    p.getWorld().playEffect(l, effect, 0);
                }
            }
        }.runTaskTimer(plugin, 0L, delay);
    }

    private void dropVisualItem(final Player p, ItemStack stack, Vector velocity, long removeAfterTicks) {
        final Item item = p.getWorld().dropItem(p.getEyeLocation().add(p.getLocation().getDirection().multiply(0.7D)), stack);
        item.setPickupDelay(999999);
        item.setVelocity(velocity);
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() { public void run() {
            if (item != null && !item.isDead()) item.remove();
        }}, removeAfterTicks);
    }

    private void playGadget(final Player p, String gadget) {
        final Location loc = p.getLocation();
        if (gadget.equalsIgnoreCase("gadget_firework")) {
            p.playSound(loc, Sound.FIREWORK_LAUNCH, 1f, 1.1f);
            pulse(p, Effect.FIREWORKS_SPARK, null, 8, 2L);
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() { public void run() {
                p.getWorld().playEffect(p.getLocation().clone().add(0, 1.2D, 0), Effect.EXPLOSION_LARGE, 0);
                p.playSound(p.getLocation(), Sound.FIREWORK_BLAST, 1f, 1.4f);
            }}, 18L);
            return;
        }
        if (gadget.equalsIgnoreCase("gadget_smoke")) {
            p.playSound(loc, Sound.FIZZ, 1f, 0.7f);
            new BukkitRunnable() {
                int i = 0;
                @Override public void run() {
                    if (!canUseLobbyCosmetics(p) || i++ > 18) { cancel(); return; }
                    for (int a = 0; a < 8; a++) p.getWorld().playEffect(p.getLocation().clone().add((Math.random() - 0.5D) * 2.2D, Math.random() * 1.5D, (Math.random() - 0.5D) * 2.2D), Effect.SMOKE, a);
                }
            }.runTaskTimer(plugin, 0L, 2L);
            return;
        }
        if (gadget.equalsIgnoreCase("gadget_heart")) {
            p.playSound(loc, Sound.ORB_PICKUP, 1f, 1.7f);
            pulse(p, Effect.HEART, null, 12, 3L);
            return;
        }
        if (gadget.equalsIgnoreCase("gadget_grappling")) {
            p.setVelocity(p.getLocation().getDirection().normalize().multiply(2.0D).setY(0.62D));
            p.playSound(loc, Sound.SHOOT_ARROW, 1f, 1.4f);
            dropVisualItem(p, new ItemStack(Material.FISHING_ROD), p.getLocation().getDirection().multiply(1.4D).setY(0.25D), 25L);
            return;
        }
        if (gadget.equalsIgnoreCase("gadget_trampoline")) {
            final org.bukkit.block.Block block = loc.clone().subtract(0, 1, 0).getBlock();
            final Material oldType = block.getType();
            final byte oldData = block.getData();
            if (oldType == Material.AIR || oldType == Material.STATIONARY_WATER || oldType == Material.WATER) {
                block.setType(Material.SLIME_BLOCK);
                Bukkit.getScheduler().runTaskLater(plugin, new Runnable() { public void run() { block.setType(oldType); block.setData(oldData); }}, 20L * 4);
            }
            p.setVelocity(p.getVelocity().setY(1.55D));
            p.playSound(loc, Sound.SLIME_WALK, 1f, 1.2f);
            pulse(p, Effect.HAPPY_VILLAGER, null, 7, 3L);
            return;
        }
        if (gadget.equalsIgnoreCase("gadget_tnt_fake")) {
            dropVisualItem(p, new ItemStack(Material.TNT), p.getLocation().getDirection().multiply(0.7D).setY(0.55D), 30L);
            p.playSound(loc, Sound.FUSE, 1f, 1f);
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() { public void run() {
                p.getWorld().playEffect(p.getLocation(), Effect.EXPLOSION_HUGE, 0);
                p.playSound(p.getLocation(), Sound.EXPLODE, 1f, 1f);
            }}, 28L);
            return;
        }
        if (gadget.equalsIgnoreCase("gadget_paint")) {
            p.playSound(loc, Sound.CHICKEN_EGG_POP, 1f, 1.4f);
            new BukkitRunnable() {
                int i = 0;
                @Override public void run() {
                    if (!canUseLobbyCosmetics(p) || i++ > 16) { cancel(); return; }
                    Location paint = p.getLocation().clone().add((Math.random() - 0.5D) * 5, 0.05D, (Math.random() - 0.5D) * 5);
                    p.getWorld().playEffect(paint, Effect.STEP_SOUND, Material.WOOL.getId());
                }
            }.runTaskTimer(plugin, 0L, 2L);
            return;
        }
        if (gadget.equalsIgnoreCase("gadget_carpet")) {
            p.setVelocity(p.getLocation().getDirection().normalize().multiply(1.25D).setY(0.55D));
            p.playSound(loc, Sound.ENDERMAN_TELEPORT, 1f, 1.5f);
            pulse(p, Effect.ENDER_SIGNAL, null, 10, 2L);
            return;
        }
        if (gadget.startsWith("seasonal_")) {
            pulse(p, Effect.ENDER_SIGNAL, Sound.ORB_PICKUP, 8, 3L);
        }
    }

    private void startWalkTrailTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    UUID id = p.getUniqueId();
                    if (!canUseLobbyCosmetics(p)) { lastTrailLocation.remove(id); continue; }
                    String selected = CosmeticEffectManager.getSelected(plugin, p, "walk_trail");
                    if (selected == null || selected.equalsIgnoreCase("nenhum")) { lastTrailLocation.remove(id); continue; }
                    Location now = p.getLocation();
                    Location old = lastTrailLocation.get(id);
                    lastTrailLocation.put(id, now.clone());
                    if (old == null || old.getWorld() != now.getWorld() || old.distanceSquared(now) < 0.035D) continue;
                    Location at = now.clone().subtract(0, 0.05D, 0);
                    if (selected.equalsIgnoreCase("walk_trail_fire")) p.getWorld().playEffect(at, Effect.MOBSPAWNER_FLAMES, 0);
                    else if (selected.equalsIgnoreCase("walk_trail_magic")) p.getWorld().playEffect(at, Effect.WITCH_MAGIC, 0);
                    else if (selected.equalsIgnoreCase("walk_trail_heart")) p.getWorld().playEffect(at.clone().add(0, 0.25D, 0), Effect.HEART, 0);
                    else p.getWorld().playEffect(at, Effect.CLOUD, 0);
                }
            }
        }.runTaskTimer(plugin, 20L, 4L);
    }

    private void startBalloonTask() {
        new BukkitRunnable() {
            int tick = 0;
            @Override
            public void run() {
                tick++;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    UUID id = p.getUniqueId();
                    String selected = CosmeticEffectManager.getSelected(plugin, p, "balloon");
                    if (!canUseLobbyCosmetics(p) || selected == null || selected.equalsIgnoreCase("nenhum")) {
                        removeBalloon(id);
                        continue;
                    }
                    ArmorStand stand = balloonStands.get(id);
                    if (stand != null && !stand.isDead() && stand.getWorld() != p.getWorld()) {
                        removeBalloon(id);
                        stand = null;
                    }
                    if (stand == null || stand.isDead()) {
                        stand = (ArmorStand) p.getWorld().spawnEntity(p.getLocation(), EntityType.ARMOR_STAND);
                        stand.setVisible(false); stand.setGravity(false); stand.setSmall(true); stand.setCanPickupItems(false);
                        try { stand.setBasePlate(false); } catch (Throwable ignored) {}
                        stand.setMetadata("altamc_balloon", new FixedMetadataValue(plugin, true));
                        balloonStands.put(id, stand);
                    }
                    double yaw = Math.toRadians(p.getLocation().getYaw());
                    Location loc = p.getLocation().clone().add(Math.sin(yaw) * 1.15D, 2.15D + Math.sin(tick * 0.16D) * 0.12D, -Math.cos(yaw) * 1.15D);
                    stand.teleport(loc);
                    stand.setHelmet(balloonItem(selected, tick));
                    if (tick % 8 == 0) p.getWorld().playEffect(loc.clone().subtract(0, 1.0D, 0), Effect.CLOUD, 0);
                }
            }
        }.runTaskTimer(plugin, 20L, 3L);
    }

    private ItemStack balloonItem(String id, int tick) {
        if (id == null) return new ItemStack(Material.WOOL, 1, (short) 14);

        // Textures reais no estilo kCosmetics, compatíveis com 1.8.8.
        if (id.equalsIgnoreCase("balloon_skull")) {
            return createBalloonHead(
                    "eyJ0aW1lc3RhbXAiOjE1MTI1OTM3NDg3NzUsInByb2ZpbGVJZCI6IjQxZDNhYmMyZDc0OTQwMGM5MDkwZDU0MzRkMDM4MzFiIiwicHJvZmlsZU5hbWUiOiJNZWdha2xvb24iLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2NiODUyYmExNTg0ZGE5ZTU3MTQ4NTk5OTU0NTFlNGI5NDc0OGM0ZGQ2M2FlNDU0M2MxNWY5ZjhhZWM2NWM4In19fQ==",
                    "§8Balão Caveira");
        }
        if (id.equalsIgnoreCase("balloon_red")) {
            return createBalloonHead(
                    "eyJ0aW1lc3RhbXAiOjE1MTMwNDQ2NzkwNTUsInByb2ZpbGVJZCI6IjQxZDNhYmMyZDc0OTQwMGM5MDkwZDU0MzRkMDM4MzFiIiwicHJvZmlsZU5hbWUiOiJNZWdha2xvb24iLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2QzMTVmY2I1YjI4MGMzNDhjNjczYmE2YWVlMjZiMzRmOTFhZDQ4NDYxZjQ1MWM3MzU2YjI2NTNlOWY3Y2NjIn19fQ==",
                    "§cBalão Vermelho");
        }

        // O arco-íris troca de cor suavemente sem recriar entidades.
        short data = (short) ((tick / 5) % 16);
        return namedItem(new ItemStack(Material.WOOL, 1, data), "§dBalão Arco-Íris");
    }

    private ItemStack createBalloonHead(String texture, String name) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setDisplayName(name);
        try {
            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            profile.getProperties().put("textures", new Property("textures", texture));
            Field field = meta.getClass().getDeclaredField("profile");
            field.setAccessible(true);
            field.set(meta, profile);
        } catch (Throwable ignored) {
            meta.setOwner("MHF_Chest");
        }
        head.setItemMeta(meta);
        return head;
    }

    private void removeBalloon(UUID id) {
        ArmorStand stand = balloonStands.remove(id);
        if (stand != null && !stand.isDead()) stand.remove();
    }

    private void startAnimatedHatTask() {
        new BukkitRunnable() {
            int tick = 0;
            @Override
            public void run() {
                tick++;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    UUID id = p.getUniqueId();
                    String selected = CosmeticEffectManager.getSelected(plugin, p, "animated_hat");
                    String emote = CosmeticEffectManager.getSelected(plugin, p, "emote");
                    if (!canUseLobbyCosmetics(p) || selected == null || selected.equalsIgnoreCase("nenhum") || (emote != null && !emote.equalsIgnoreCase("nenhum"))) {
                        restoreAnimatedHat(id);
                        continue;
                    }
                    if (!selected.equals(lastAnimatedHat.get(id))) {
                        restoreAnimatedHat(id);
                        ItemStack old = p.getInventory().getHelmet();
                        originalAnimatedHatHelmets.put(id, old == null ? null : old.clone());
                        lastAnimatedHat.put(id, selected);
                    }
                    p.getInventory().setHelmet(animatedHatItem(selected, tick));
                    if (tick % 10 == 0) p.getWorld().playEffect(p.getLocation().clone().add(0, 2.0D, 0), Effect.FIREWORKS_SPARK, 0);
                }
            }
        }.runTaskTimer(plugin, 20L, 6L);
    }

    private ItemStack animatedHatItem(String id, int tick) {
        if (id.equalsIgnoreCase("animated_hat_rainbow")) {
            ItemStack item = new ItemStack(Material.LEATHER_HELMET);
            LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
            Color[] colors = new Color[] { Color.RED, Color.ORANGE, Color.YELLOW, Color.LIME, Color.AQUA, Color.BLUE, Color.PURPLE };
            meta.setColor(colors[(tick / 2) % colors.length]);
            meta.setDisplayName("§dChapéu Arco-Íris"); item.setItemMeta(meta); return item;
        }
        if (id.equalsIgnoreCase("animated_hat_disco")) {
            Material[] mats = new Material[] { Material.GLOWSTONE, Material.REDSTONE_BLOCK, Material.LAPIS_BLOCK, Material.EMERALD_BLOCK };
            return new ItemStack(mats[(tick / 2) % mats.length]);
        }
        Material[] mats = new Material[] { Material.BLAZE_POWDER, Material.ICE, Material.ENDER_PEARL };
        return new ItemStack(mats[(tick / 2) % mats.length]);
    }

    private void restoreAnimatedHat(UUID id) {
        Player p = Bukkit.getPlayer(id);
        ItemStack old = originalAnimatedHatHelmets.remove(id);
        lastAnimatedHat.remove(id);
        if (p != null && p.isOnline() && old != null) p.getInventory().setHelmet(old.clone());
    }

}
