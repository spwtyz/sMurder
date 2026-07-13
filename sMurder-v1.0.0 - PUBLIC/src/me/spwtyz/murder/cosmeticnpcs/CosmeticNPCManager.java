package me.spwtyz.murder.cosmeticnpcs;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import me.spwtyz.murder.Main;

/**
 * Sistema dos pontos cosméticos:
 * - Battle Pass usa NPC fake com ArmorStand + cabeça de skin escolhida.
 * - Mystery Box usa BAÚ físico no chão, estilo Hypixel, não NPC.
 * - Todos os NPCs são protegidos contra movimento/empurrão/dano por respawn/teleport.
 */
@SuppressWarnings("deprecation")
public class CosmeticNPCManager implements Listener {

    private final Main plugin;
    private final List<Entity> spawned = new ArrayList<Entity>();
    private ArmorStand battlePassNPC;

    public CosmeticNPCManager(Main plugin) {
        this.plugin = plugin;
        startProtectionTask();
    }

    public void setBattlePassNPC(Player p) {
        setBattlePassNPC(p, p.getName());
    }

    public void setBattlePassNPC(Player p, String skinName) {
        if (skinName == null || skinName.trim().isEmpty()) skinName = p.getName();

        saveLocation("battlepass", p.getLocation());
        plugin.data.getConfig().set("cosmetic-npcs.battlepass.skin", skinName);
        plugin.data.save();

        p.sendMessage("§aNPC do Passe de Batalha setado aqui com skin de §f" + skinName + "§a.");
        respawnAll();
    }

    public void setMysteryBoxChest(Player p) {
        Block target = p.getTargetBlock((java.util.HashSet<Byte>) null, 6);

        if (target == null || target.getType() == Material.AIR) {
            target = p.getLocation().getBlock();
        }

        target.setType(Material.CHEST);
        saveBlockLocation("mysterybox", target.getLocation());
        p.sendMessage("§aCaixa Misteriosa setada como baú no chão.");
    }

    /** Compatibilidade com comando antigo. Agora Mystery Box é baú, não NPC. */
    public void setMysteryBoxNPC(Player p) {
        setMysteryBoxChest(p);
    }

    public void deleteBattlePassNPC(Player p) {
        deleteLocation("battlepass");
        removeBattlePassNPC();
        if (p != null) p.sendMessage("§aLocal/NPC do Battle Pass deletado.");
    }

    public void deleteMysteryBoxChest(Player p) {
        Location loc = getBlockLocation("mysterybox");
        if (loc != null && loc.getBlock().getType() == Material.CHEST) {
            loc.getBlock().setType(Material.AIR);
        }
        deleteLocation("mysterybox");
        if (p != null) p.sendMessage("§aLocal/baú da Mystery Box deletado.");
    }

    public void respawnAll() {
        removeAll();
        spawnBattlePassNPC();
        ensureMysteryBoxChest();
    }

    public void removeAll() {
        for (Entity e : new ArrayList<Entity>(spawned)) {
            if (e != null && !e.isDead()) {
                e.remove();
            }
        }
        spawned.clear();
        removeBattlePassNPC();
    }

    private void spawnBattlePassNPC() {
        Location loc = getLocation("battlepass");
        if (loc == null || loc.getWorld() == null) return;

        removeBattlePassNPC();

        ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setVisible(true);
        stand.setGravity(false);
        stand.setCanPickupItems(false);
        stand.setCustomName(ChatColor.GOLD + "§lPASSE DE BATALHA §7(Clique)");
        stand.setCustomNameVisible(true);

        try {
            stand.setHelmet(createSkinHead(getBattlePassSkin()));
            stand.setItemInHand(new ItemStack(Material.BOOK));
        } catch (Exception ignored) {}

        battlePassNPC = stand;
        spawned.add(stand);
    }

    private ItemStack createSkinHead(String owner) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwner(owner);
        meta.setDisplayName("§6Battle Pass NPC");
        skull.setItemMeta(meta);
        return skull;
    }

    private String getBattlePassSkin() {
        return plugin.data.getConfig().getString("cosmetic-npcs.battlepass.skin", "MHF_Steve");
    }

    private void ensureMysteryBoxChest() {
        Location loc = getBlockLocation("mysterybox");
        if (loc == null || loc.getWorld() == null) return;
        if (loc.getBlock().getType() != Material.CHEST) {
            loc.getBlock().setType(Material.CHEST);
        }
    }

    private void removeBattlePassNPC() {
        if (battlePassNPC != null && !battlePassNPC.isDead()) battlePassNPC.remove();

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof ArmorStand)) continue;
                ArmorStand stand = (ArmorStand) entity;
                if (stand.getCustomName() == null) continue;
                String name = ChatColor.stripColor(stand.getCustomName());
                if (name != null && name.startsWith("PASSE DE BATALHA")) {
                    stand.remove();
                }
            }
        }
    }

    private void saveLocation(String id, Location loc) {
        String path = "cosmetic-npcs." + id + ".";
        plugin.data.getConfig().set(path + "world", loc.getWorld().getName());
        plugin.data.getConfig().set(path + "x", loc.getX());
        plugin.data.getConfig().set(path + "y", loc.getY());
        plugin.data.getConfig().set(path + "z", loc.getZ());
        plugin.data.getConfig().set(path + "yaw", loc.getYaw());
        plugin.data.getConfig().set(path + "pitch", loc.getPitch());
        plugin.data.save();
    }

    private void saveBlockLocation(String id, Location loc) {
        String path = "cosmetic-npcs." + id + ".";
        plugin.data.getConfig().set(path + "world", loc.getWorld().getName());
        plugin.data.getConfig().set(path + "x", loc.getBlockX());
        plugin.data.getConfig().set(path + "y", loc.getBlockY());
        plugin.data.getConfig().set(path + "z", loc.getBlockZ());
        plugin.data.save();
    }

    private void deleteLocation(String id) {
        plugin.data.getConfig().set("cosmetic-npcs." + id, null);
        plugin.data.save();
    }

    private Location getLocation(String id) {
        String path = "cosmetic-npcs." + id + ".";
        if (!plugin.data.getConfig().contains(path + "world")) return null;

        World world = Bukkit.getWorld(plugin.data.getConfig().getString(path + "world"));
        if (world == null) return null;

        double x = plugin.data.getConfig().getDouble(path + "x");
        double y = plugin.data.getConfig().getDouble(path + "y");
        double z = plugin.data.getConfig().getDouble(path + "z");
        float yaw = (float) plugin.data.getConfig().getDouble(path + "yaw");
        float pitch = (float) plugin.data.getConfig().getDouble(path + "pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }

    private Location getBlockLocation(String id) {
        String path = "cosmetic-npcs." + id + ".";
        if (!plugin.data.getConfig().contains(path + "world")) return null;

        World world = Bukkit.getWorld(plugin.data.getConfig().getString(path + "world"));
        if (world == null) return null;

        int x = plugin.data.getConfig().getInt(path + "x");
        int y = plugin.data.getConfig().getInt(path + "y");
        int z = plugin.data.getConfig().getInt(path + "z");

        return new Location(world, x, y, z);
    }

    public boolean isBattlePassNPC(Entity entity) {
        if (!(entity instanceof ArmorStand)) return false;
        if (entity.getCustomName() == null) return false;
        String name = ChatColor.stripColor(entity.getCustomName());
        return name != null && name.startsWith("PASSE DE BATALHA");
    }

    public boolean isMysteryBoxChest(Block block) {
        Location loc = getBlockLocation("mysterybox");
        if (loc == null || block == null) return false;
        return block.getWorld().getName().equals(loc.getWorld().getName())
                && block.getX() == loc.getBlockX()
                && block.getY() == loc.getBlockY()
                && block.getZ() == loc.getBlockZ();
    }

    /** Compatibilidade com listener antigo. */
    public boolean isMysteryBoxNPC(Entity entity) {
        return false;
    }

    private void startProtectionTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.isEnabled()) return;

                Location saved = getLocation("battlepass");
                if (saved != null) {
                    if (battlePassNPC == null || battlePassNPC.isDead()) {
                        spawnBattlePassNPC();
                    } else if (battlePassNPC.getLocation().distanceSquared(saved) > 0.01D) {
                        battlePassNPC.teleport(saved);
                        battlePassNPC.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
                    }
                }

                ensureMysteryBoxChest();
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }
}
