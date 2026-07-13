package me.spwtyz.murder.rooms;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import me.spwtyz.murder.Main;

public class RoomPasswordGUI implements Listener {

    private static final String TITLE_SET = "§8Senha da Sala";
    private static final String TITLE_JOIN = "§8Digite a Senha";

    private static final Map<UUID, String> input = new HashMap<UUID, String>();
    private static final Map<UUID, Room> setRoom = new HashMap<UUID, Room>();
    private static final Map<UUID, Room> joinRoom = new HashMap<UUID, Room>();

    private final Main plugin;

    public RoomPasswordGUI(Main plugin) {
        this.plugin = plugin;
    }

    public static void openSetPassword(Main plugin, Player player, Room room) {
        if (player == null || room == null) return;
        input.put(player.getUniqueId(), "");
        setRoom.put(player.getUniqueId(), room);
        joinRoom.remove(player.getUniqueId());
        player.openInventory(createInventory(player, "" , TITLE_SET));
    }

    public static void openJoinPassword(Main plugin, Player player, Room room) {
        if (player == null || room == null) return;
        input.put(player.getUniqueId(), "");
        joinRoom.put(player.getUniqueId(), room);
        setRoom.remove(player.getUniqueId());
        player.openInventory(createInventory(player, "" , TITLE_JOIN));
    }

    private static Inventory createInventory(Player player, String current, String title) {
        Inventory inv = Bukkit.createInventory(null, 54, title);

        ItemStack filler = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
        ItemMeta fm = filler.getItemMeta();
        fm.setDisplayName(" ");
        filler.setItemMeta(fm);
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, filler);

        inv.setItem(4, statusItem(current));

        inv.setItem(19, numberHead("1"));
        inv.setItem(20, numberHead("2"));
        inv.setItem(21, numberHead("3"));
        inv.setItem(28, numberHead("4"));
        inv.setItem(29, numberHead("5"));
        inv.setItem(30, numberHead("6"));
        inv.setItem(37, numberHead("7"));
        inv.setItem(38, numberHead("8"));
        inv.setItem(39, numberHead("9"));
        inv.setItem(47, numberHead("0"));

        inv.setItem(24, actionItem(Material.EMERALD_BLOCK, "§aConfirmar", "§7Clique para confirmar."));
        inv.setItem(25, actionItem(Material.REDSTONE_BLOCK, "§cApagar", "§7Clique para apagar o último número."));
        inv.setItem(33, actionItem(Material.BARRIER, "§4Limpar", "§7Clique para limpar tudo."));
        inv.setItem(34, actionItem(Material.ARROW, "§cCancelar", "§7Clique para cancelar."));

        return inv;
    }

    private static ItemStack statusItem(String current) {
        ItemStack item = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§eSenha Atual");
        meta.setLore(Arrays.asList(
                "§7Senha: §f" + (current == null || current.isEmpty() ? "Nenhuma" : mask(current)),
                "",
                "§7Use as heads numéricas para digitar."
        ));
        item.setItemMeta(meta);
        return item;
    }

    private static String mask(String value) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) sb.append("*");
        return sb.toString();
    }

    private static ItemStack actionItem(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack numberHead(String number) {
        return customHead(getTexture(number), "§a" + number, "§7Clique para adicionar o número §f" + number + "§7.");
    }

    private static ItemStack customHead(String value, String name, String lore) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        if (value != null && !value.isEmpty()) {
            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            profile.getProperties().put("textures", new Property("textures", value));
            try {
                Field field = meta.getClass().getDeclaredField("profile");
                field.setAccessible(true);
                field.set(meta, profile);
            } catch (Exception e) {
                meta.setOwner("MHF_Question");
            }
        } else {
            meta.setOwner("MHF_Question");
        }

        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        head.setItemMeta(meta);
        return head;
    }

    private static String getTexture(String number) {
        // Textures Base64 para heads numéricas. Se quiser trocar o visual depois,
        // basta substituir os valores abaixo por outras textures Base64.
        if (number.equals("0")) return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmQ5YzQ0N2M5ZjA2MWE2Y2E0NjQ2ZmZiZmM4ZTk4YjQ2YzE2NzYzOTYifX19";
        if (number.equals("1")) return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2I4M2Q0ZjQ0YmNkNmM4YjMwYmI4NzQ3NGE2MWRkYjY4YzRlY2IwZTA3ZGU0YzU1ODU0Y2Y4ZDFhYjgifX19";
        if (number.equals("2")) return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjEwY2U2ZTY3YjQ2YmY5Y2Y0YjQxZTI0NmQzYjU2N2NkNmQ4NDY5YjYxYTRlYzY4ZTY5Y2Y4YjY5MSJ9fX0=";
        if (number.equals("3")) return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjQ0ZjI5NmU0ZjQzNmI3NmQ4YTUzNjQzOGM4Y2MzYjE5MmJhY2Y3NmQzZjRiNzM3OTRkMzgxNWI2In19fQ==";
        if (number.equals("4")) return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzQ0YjY2ZWRjY2YzY2YzYzA0NzU3MWFhY2NhYmQ1YzI4MjQ4YmNmYjYxNjU2N2ZhNjY0MmMifX19";
        if (number.equals("5")) return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2E3YjM5NmY0ZjM3NjM0NTQ0MTM3NjYxODNhYzY5Nzg4N2Y2NTVkZWEzYjA0ZjUyOWQ2ZCJ9fX0=";
        if (number.equals("6")) return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzY5ZWEzNmQ4YzE1YWI2ZjNjYmU5YjFkY2Q0NTc3YjA4MWY1NjM4M2FhNmM0YmUxNzM0OTYifX19";
        if (number.equals("7")) return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzY1OGVkNzQ1MWU2YzQ4NzIxZTQ3MjU0MmY0MjQ4ZWM1YmNmMTA1NzYzYzY5ZmQifX19";
        if (number.equals("8")) return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmQ5Y2Y4YTUwNDU4MWM0OTI4OGIwNmE2ODBhMzQ1M2E4NTRjODFiNmYzYzE5MmQ4OSJ9fX0=";
        if (number.equals("9")) return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjEzNzRiYjFjYmQ0MzJhODQ1YjRhODI2NmI0Y2I1NjRmMzYxN2IifX19";
        return "";
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        String title = event.getView().getTitle();
        if (!title.equals(TITLE_SET) && !title.equals(TITLE_JOIN)) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;

        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        UUID uuid = player.getUniqueId();
        String current = input.containsKey(uuid) ? input.get(uuid) : "";

        if (name.matches("[0-9]")) {
            if (current.length() >= 6) {
                player.sendMessage(ChatColor.RED + "A senha pode ter no máximo 6 números.");
                return;
            }
            current += name;
            input.put(uuid, current);
            player.openInventory(createInventory(player, current, title));
            return;
        }

        if (name.equalsIgnoreCase("Apagar")) {
            if (current.length() > 0) current = current.substring(0, current.length() - 1);
            input.put(uuid, current);
            player.openInventory(createInventory(player, current, title));
            return;
        }

        if (name.equalsIgnoreCase("Limpar")) {
            input.put(uuid, "");
            player.openInventory(createInventory(player, "", title));
            return;
        }

        if (name.equalsIgnoreCase("Cancelar")) {
            clear(player);
            player.closeInventory();
            return;
        }

        if (name.equalsIgnoreCase("Confirmar")) {
            if (title.equals(TITLE_SET)) {
                Room room = setRoom.get(uuid);
                if (room == null) {
                    player.sendMessage(ChatColor.RED + "Sala não encontrada.");
                    clear(player);
                    player.closeInventory();
                    return;
                }
                room.setPassword(current);
                if (current == null || current.isEmpty()) {
                    room.setPrivateRoom(false);
                    player.sendMessage(ChatColor.RED + "Senha removida da sala.");
                } else {
                    room.setPrivateRoom(true);
                    player.sendMessage(ChatColor.GREEN + "Senha da sala definida para: " + ChatColor.YELLOW + current);
                }
                clear(player);
                player.closeInventory();
                return;
            }

            if (title.equals(TITLE_JOIN)) {
                Room room = joinRoom.get(uuid);
                if (room == null || room.getArena() == null) {
                    player.sendMessage(ChatColor.RED + "Sala não encontrada.");
                    clear(player);
                    player.closeInventory();
                    return;
                }
                if (!room.checkPassword(current)) {
                    player.sendMessage(ChatColor.RED + "Senha incorreta.");
                    input.put(uuid, "");
                    player.openInventory(createInventory(player, "", title));
                    return;
                }

                clear(player);
                player.closeInventory();
                if (plugin.roomManager != null) {
                    plugin.roomManager.addPlayerToRoom(player, room);
                } else {
                    room.getArena().addPlayer(player);
                }
                player.sendMessage(ChatColor.GREEN + "Senha correta! Entrando na sala...");
            }
        }
    }

    private static void clear(Player player) {
        if (player == null) return;
        UUID uuid = player.getUniqueId();
        input.remove(uuid);
        setRoom.remove(uuid);
        joinRoom.remove(uuid);
    }
}
