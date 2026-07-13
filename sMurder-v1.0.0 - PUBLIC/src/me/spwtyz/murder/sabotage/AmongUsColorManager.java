package me.spwtyz.murder.sabotage;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import me.spwtyz.murder.Arena;
import me.spwtyz.murder.Arenas;
import me.spwtyz.murder.GameModeType;
import me.spwtyz.murder.GameState;
import me.spwtyz.murder.Main;
import me.spwtyz.murder.rooms.Room;
import me.spwtyz.murder.rooms.RoomModifier;

@SuppressWarnings("deprecation")
public class AmongUsColorManager implements Listener {

    public enum AmongUsColor {
        RED("red", "§cVermelho", Color.RED, (short) 14),
        BLUE("blue", "§9Azul", Color.BLUE, (short) 11),
        GREEN("green", "§aVerde", Color.GREEN, (short) 5),
        YELLOW("yellow", "§eAmarelo", Color.YELLOW, (short) 4),
        ORANGE("orange", "§6Laranja", Color.ORANGE, (short) 1),
        PURPLE("purple", "§5Roxo", Color.PURPLE, (short) 10),
        PINK("pink", "§dRosa", Color.FUCHSIA, (short) 6),
        BLACK("black", "§8Preto", Color.BLACK, (short) 15),
        WHITE("white", "§fBranco", Color.WHITE, (short) 0),
        CYAN("cyan", "§bCiano", Color.AQUA, (short) 3),
        LIME("lime", "§aLime", Color.LIME, (short) 5),
        BROWN("brown", "§6Marrom", Color.fromRGB(102, 51, 0), (short) 12);

        private final String id;
        private final String display;
        private final Color armorColor;
        private final short woolData;

        AmongUsColor(String id, String display, Color armorColor, short woolData) {
            this.id = id;
            this.display = display;
            this.armorColor = armorColor;
            this.woolData = woolData;
        }

        public String getId() { return id; }
        public String getDisplay() { return display; }
        public Color getArmorColor() { return armorColor; }
        public short getWoolData() { return woolData; }
    }

    private final Main plugin;
    private ArmorStand colorNpc;
    private final Map<UUID, ItemStack> oldChestplates = new HashMap<UUID, ItemStack>();

    // Texture simples estilo tripulante/visor. Se o servidor não carregar skin externa, o NPC ainda fica com roupa colorida.
    private static final String AMONG_US_HEAD_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2Y4ZTAyYzI1YWE5NTUzNTRiMWQ5ZDE2ZTA2NDk5NjVlYWM2Y2EwMmI0NjVmMzU2NjQzYmJhNjU5M2UyNzdhOSJ9fX0=";

    public AmongUsColorManager(Main plugin) {
        this.plugin = plugin;
        startUpdater();
    }

    public void setNpc(Player staff) {
        saveLocation("amongus-color-npc", staff.getLocation());
        staff.sendMessage("§aNPC de cores do AMONG US setado aqui.");
        respawnNpc();
    }

    public void deleteNpc(Player staff) {
        removeNpc();
        plugin.data.getConfig().set("cosmetic-npcs.amongus-color-npc", null);
        plugin.data.save();
        if (staff != null) staff.sendMessage("§aNPC de cores do AMONG US removido.");
    }

    public void respawnNpc() {
        removeNpc();
        if (!shouldShowNpc()) return;
        Location loc = getLocation("amongus-color-npc");
        if (loc == null || loc.getWorld() == null) return;

        ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setVisible(true);
        stand.setGravity(false);
        stand.setCanPickupItems(false);
        stand.setCustomName("§c§lAMONG US §7- §fCores §e(Clique)");
        stand.setCustomNameVisible(true);
        stand.setBasePlate(false);
        try { stand.setArms(true); } catch (Throwable ignored) {}
        try { stand.setHelmet(createTextureHead(AMONG_US_HEAD_TEXTURE, "§cAMONG US")); } catch (Throwable ignored) {}
        try { stand.setChestplate(createChestplate(AmongUsColor.RED)); } catch (Throwable ignored) {}
        try { stand.setItemInHand(new ItemStack(Material.WOOL, 1, AmongUsColor.RED.getWoolData())); } catch (Throwable ignored) {}

        colorNpc = stand;
    }

    public void removeNpc() {
        if (colorNpc != null && !colorNpc.isDead()) colorNpc.remove();
        colorNpc = null;
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof ArmorStand)) continue;
                if (entity.getCustomName() == null) continue;
                String name = ChatColor.stripColor(entity.getCustomName());
                if (name != null && name.startsWith("AMONG US - Cores")) entity.remove();
            }
        }
    }

    public boolean isColorNpc(Entity entity) {
        if (!(entity instanceof ArmorStand)) return false;
        if (entity.getCustomName() == null) return false;
        String name = ChatColor.stripColor(entity.getCustomName());
        return name != null && name.startsWith("AMONG US - Cores");
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (isColorNpc(event.getRightClicked())) {
            event.setCancelled(true);
            openColorMenu(event.getPlayer());
        }
    }

    @EventHandler
    public void onInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (isColorNpc(event.getRightClicked())) {
            event.setCancelled(true);
            openColorMenu(event.getPlayer());
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.getView() == null || event.getView().getTitle() == null) return;
        if (!event.getView().getTitle().equals("Cores AMONG US")) return;
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return;
        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName()).toLowerCase();
        for (AmongUsColor color : AmongUsColor.values()) {
            if (name.contains(ChatColor.stripColor(color.getDisplay()).toLowerCase())) {
                Arena arena = Arenas.getArena(player);
                Player owner = getColorOwner(arena, color, player);
                if (owner != null) {
                    player.sendMessage("§cEssa cor já está sendo usada por " + owner.getName() + ".");
                    openColorMenu(player);
                    return;
                }
                setSelectedColor(player, color);
                applyChestplate(player);
                player.closeInventory();
                player.sendMessage("§aSua cor do AMONG US agora é " + color.getDisplay() + "§a.");
                return;
            }
        }
    }

    public void openColorMenu(Player player) {
        Arena arena = Arenas.getArena(player);
        if (!isWaitingAmongUs(arena)) {
            player.sendMessage("§cEsse NPC só funciona no lobby de espera do AMONG US.");
            return;
        }
        Inventory inv = Bukkit.createInventory(null, 27, "Cores AMONG US");
        int slot = 10;
        for (AmongUsColor color : AmongUsColor.values()) {
            Player owner = getColorOwner(arena, color, player);
            ItemStack item = new ItemStack(owner == null ? Material.WOOL : Material.BARRIER, 1, color.getWoolData());
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName((owner == null ? "" : "§c") + color.getDisplay());
            List<String> lore = new ArrayList<String>();
            lore.add("§7Clique para escolher essa cor.");
            lore.add("§7A chestplate do lobby ficará nessa cor.");
            lore.add("");
            if (owner != null) {
                lore.add("§cJá está sendo usada por " + owner.getName() + ".");
            } else {
                lore.add(getSelectedColor(player) == color ? "§aSelecionada" : "§eClique para selecionar.");
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
            if (slot == 17) slot = 19;
            if (slot > 25) break;
        }
        player.openInventory(inv);
    }

    public AmongUsColor getSelectedColor(Player player) {
        if (player == null || plugin.data == null) return AmongUsColor.RED;
        String id = plugin.data.getConfig().getString("AmongUsColors." + player.getUniqueId(), "red");
        for (AmongUsColor color : AmongUsColor.values()) {
            if (color.getId().equalsIgnoreCase(id)) return color;
        }
        return AmongUsColor.RED;
    }

    public String getColorDisplay(Player player) {
        return getSelectedColor(player).getDisplay();
    }

    public void setSelectedColor(Player player, AmongUsColor color) {
        if (player == null || color == null || plugin.data == null) return;
        plugin.data.getConfig().set("AmongUsColors." + player.getUniqueId(), color.getId());
        plugin.data.save();
    }

    public Player getColorOwner(Arena arena, AmongUsColor color, Player requester) {
        if (arena == null || color == null) return null;
        for (Player other : arena.getPlayers()) {
            if (other == null || !other.isOnline()) continue;
            if (requester != null && other.getUniqueId().equals(requester.getUniqueId())) continue;
            if (arena.specs.contains(other)) continue;
            if (getSelectedColor(other) == color) return other;
        }
        return null;
    }

    public void assignMissingUniqueColor(Player player) {
        if (player == null || !player.isOnline()) return;
        Arena arena = Arenas.getArena(player);
        if (!isWaitingAmongUs(arena)) return;
        AmongUsColor selected = getSelectedColor(player);
        if (getColorOwner(arena, selected, player) == null) return;
        for (AmongUsColor color : AmongUsColor.values()) {
            if (getColorOwner(arena, color, player) == null) {
                setSelectedColor(player, color);
                applyChestplate(player);
                return;
            }
        }
    }

    public void applyChestplate(Player player) {
        if (player == null || !player.isOnline()) return;
        Arena arena = Arenas.getArena(player);
        if (!isWaitingAmongUs(arena)) return;
        if (!oldChestplates.containsKey(player.getUniqueId())) {
            oldChestplates.put(player.getUniqueId(), player.getInventory().getChestplate() == null ? null : player.getInventory().getChestplate().clone());
        }
        player.getInventory().setChestplate(createChestplate(getSelectedColor(player)));
        player.updateInventory();
    }

    public void restoreChestplate(Player player) {
        if (player == null) return;
        if (!oldChestplates.containsKey(player.getUniqueId())) return;
        player.getInventory().setChestplate(oldChestplates.remove(player.getUniqueId()));
        player.updateInventory();
    }

    public ItemStack createChestplate(AmongUsColor color) {
        ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setColor(color.getArmorColor());
        meta.setDisplayName(color.getDisplay() + " §7(AMONG US)");
        List<String> lore = new ArrayList<String>();
        lore.add("§7Cor selecionada para o modo AMONG US.");
        lore.add("§8amongus-color:" + color.getId());
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private boolean isWaitingAmongUs(Arena arena) {
        if (arena == null) return false;
        if (!(arena.getState() == GameState.LOBBY || arena.getState() == GameState.STARTING)) return false;
        if (arena.getGameMode() == GameModeType.SABOTAGE) return true;
        if (plugin.roomManager == null) return false;
        Room room = plugin.roomManager.getRoomByArena(arena);
        return room != null && room.hasModifier(RoomModifier.SABOTAGE);
    }

    private boolean shouldShowNpc() {
        for (Arena arena : Arenas.getArenas()) {
            if (isWaitingAmongUs(arena)) return true;
        }
        return false;
    }

    private void startUpdater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.isEnabled()) return;
                if (shouldShowNpc()) {
                    if (colorNpc == null || colorNpc.isDead()) respawnNpc();
                } else {
                    removeNpc();
                }
                for (Player p : Bukkit.getOnlinePlayers()) {
                    Arena arena = Arenas.getArena(p);
                    if (isWaitingAmongUs(arena)) { assignMissingUniqueColor(p); applyChestplate(p); }
                    else restoreChestplate(p);
                }
            }
        }.runTaskTimer(plugin, 20L, 40L);
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

    private ItemStack createTextureHead(String texture, String display) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setDisplayName(display);
        if (texture != null && !texture.isEmpty()) {
            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            profile.getProperties().put("textures", new Property("textures", texture));
            try {
                Field profileField = meta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(meta, profile);
            } catch (Exception ignored) {}
        }
        skull.setItemMeta(meta);
        return skull;
    }
}
