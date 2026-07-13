package me.spwtyz.murder.builder;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;
import java.lang.reflect.Field;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import me.spwtyz.murder.Arenas;
import me.spwtyz.murder.Main;
import me.spwtyz.murder.PlayerState;

public class Modes implements Listener {

    private final Main plugin;

    public Modes(Main plugin) {
        this.plugin = plugin;
    }

    // =========================
    // INVENTORY CACHE
    // =========================

    private final HashMap<UUID, ItemStack[]> inventoryCache = new HashMap<>();
    private final Set<UUID> hiddenPlayers = new HashSet<UUID>();

    // =========================
    // LOBBY
    // =========================

    public Location getLobby() {
        if (plugin == null || plugin.arenas == null || plugin.arenas.getConfig() == null) return null;
        if (!plugin.arenas.getConfig().contains("Lobby.main.lobby.world")) return null;

        String world = plugin.arenas.getConfig().getString("Lobby.main.lobby.world");
        if (world == null || Bukkit.getWorld(world) == null) return null;

        double x = plugin.arenas.getConfig().getDouble("Lobby.main.lobby.x");
        double y = plugin.arenas.getConfig().getDouble("Lobby.main.lobby.y");
        double z = plugin.arenas.getConfig().getDouble("Lobby.main.lobby.z");
        float yaw = (float) plugin.arenas.getConfig().getDouble("Lobby.main.lobby.yaw");
        float pitch = (float) plugin.arenas.getConfig().getDouble("Lobby.main.lobby.pitch");

        return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
    }

    // 🔥 FIX PRINCIPAL: não usar distância no join
    private boolean isLobbyWorld(Player p) {
        Location lobby = getLobby();
        return lobby != null && p.getWorld().equals(lobby.getWorld());
    }

    // =========================
    // ITEMS
    // =========================

    private void giveItems(Player player) {

        player.getInventory().clear();
        
        player.getInventory().setItem(0, createItem(Material.COMPASS,
                "§aPartidas §7(Clique)",
                "§7Entre em uma partida de Murder.",
                "§eClique para abrir."));

        //player.getInventory().setItem(0, createCustomHead(
                //"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2EzZDc1ZmE4ZDRkYjEzZWNmM2Y4M2NkYWE1MGQxY2Q1M2I1NTVhMzIyZTdkYjk4NGYzYjZlNjliYmM1YWJhYyJ9fX0=",
                //"§aPartidas §7(Clique)",
                //"§7Entre em uma partida de Murder.",
                //"§eClique para abrir."));

        // Slot 2 (índice 1): Perfil ao lado de Partidas
        player.getInventory().setItem(1, createPlayerHead(player,
                "§bPerfil §7(Clique)",
                "§7Veja suas estatísticas, level e tags.",
                "§eClique para abrir."));

        String shopName = (plugin.seasonalEventManager != null && plugin.seasonalEventManager.isEventActive())
                ? plugin.seasonalEventManager.getLobbyShopItemName()
                : "§6Loja §7(Clique)";
        player.getInventory().setItem(4, createCustomHead(
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjEyNjBkNzg2MGQ4YzJkNmM3ZjVmNTc4YjAyOWMzNzVlNGM0NmNlY2IwNGMzYjAyYmE0MzFiOTJlMzVlNzUzNSJ9fX0=",
                shopName,
                "§7Cosméticos, títulos e melhorias.",
                "§eClique para abrir."));

        // Slot 8 (índice 7): Esconder Jogadores
        player.getInventory().setItem(7, createItem(Material.INK_SACK,
                "§fEsconder Jogadores §7(Clique)",
                "§7Deixa o lobby mais limpo.",
                "§eClique para alternar."));

        // Slot 9 (índice 8): Sistema de Lobbies sem Bungee
        player.getInventory().setItem(8, createItem(Material.NETHER_STAR,
                "§bEscolher Lobby §7(Clique)",
                "§7Troque de lobby dentro do mesmo servidor.",
                "§eClique para abrir."));
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        if (mat == Material.INK_SACK) item.setDurability((short) 8);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }


    private ItemStack createCustomHead(String textureValue, String name, String... lore) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));

        try {
            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            profile.getProperties().put("textures", new Property("textures", textureValue));
            Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (Exception ex) {
            meta.setOwner("MHF_Question");
        }

        head.setItemMeta(meta);
        return head;
    }

    private ItemStack createPlayerHead(Player p, String name, String... lore) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwner(p.getName());
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        head.setItemMeta(meta);
        return head;
    }

    // =========================
    // INVENTORY HANDLING
    // =========================

    private void saveInventory(Player p) {
        inventoryCache.put(p.getUniqueId(), p.getInventory().getContents());
    }

    private void restoreInventory(Player p) {
        ItemStack[] items = inventoryCache.remove(p.getUniqueId());

        if (items != null) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                p.getInventory().clear();
                p.getInventory().setContents(items);
                p.updateInventory();
            });
        }
    }

    public void giveLobbySafe(Player p) {
        if (p == null || !p.isOnline()) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (p == null || !p.isOnline()) return;
            if (Arenas.isInArena(p)) return;
            if (plugin.getPlayerState(p) != PlayerState.MAIN_LOBBY) return;

            if (isLobbyWorld(p)) {
                giveItems(p);
                p.updateInventory();
            }
        }, 20L);
    }

    // =========================
    // EVENTS
    // =========================

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        e.setJoinMessage(null);

        // salva inventário original
        plugin.setPlayerState(p, PlayerState.MAIN_LOBBY);
        saveInventory(p);

        // força depois do spawn carregar
        giveLobbySafe(p);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        inventoryCache.remove(p.getUniqueId());
        hiddenPlayers.remove(p.getUniqueId());
        plugin.setPlayerState(p, null);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        Player p = e.getPlayer();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {

            if (isLobbyWorld(p) && plugin.getPlayerState(p) == PlayerState.MAIN_LOBBY) {
                giveLobbySafe(p);
            } else if (!Arenas.isInArena(p)) {
                restoreInventory(p);
            }

        }, 10L);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        Player p = e.getPlayer();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {

            if (isLobbyWorld(p) && plugin.getPlayerState(p) == PlayerState.MAIN_LOBBY) {
                giveLobbySafe(p);
            }

        }, 10L);
    }

    // =========================
    // INTERACTIONS
    // =========================

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        if (!isLobbyWorld(p)) return;
        if (plugin.getPlayerState(p) != PlayerState.MAIN_LOBBY) return;

        ItemStack item = p.getItemInHand();
        if (item == null || item.getType() == Material.AIR) return;

        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;

        String name = item.getItemMeta().getDisplayName();

        if (name.equals("§aPartidas §7(Clique)") || name.equals("§ePartidas §7(Clique)")) {
            if (e.getAction().toString().contains("RIGHT")) {
                e.setCancelled(true);
                p.performCommand("m partidas");
            }
        }

        if (ChatColor.stripColor(name).toLowerCase().contains("loja")) {
            if (e.getAction().toString().contains("RIGHT")) {
                e.setCancelled(true);
                p.performCommand("m loja");
            }
        }


        if (name.equals("§bPerfil §7(Clique)")) {
            if (e.getAction().toString().contains("RIGHT")) {
                e.setCancelled(true);
                new ProfileMenu(plugin).open(p);
            }
        }

        if (name.equals("§bEscolher Lobby §7(Clique)")) {
            if (e.getAction().toString().contains("RIGHT")) {
                e.setCancelled(true);
                openLobbySelector(p);
            }
        }

        if (name.equals("§fEsconder Jogadores §7(Clique)") || name.equals("§aMostrar Jogadores §7(Clique)")) {
            if (e.getAction().toString().contains("RIGHT")) {
                e.setCancelled(true);
                toggleHidePlayers(p);
            }
        }
    }



    @EventHandler
    public void onLobbyInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;

        Player p = (Player) e.getWhoClicked();

        if (!isLobbyWorld(p)) return;
        if (plugin.getPlayerState(p) != PlayerState.MAIN_LOBBY) return;

        ItemStack item = e.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;

        String name = item.getItemMeta().getDisplayName();

        if (name.equals("§aPartidas §7(Clique)") || name.equals("§ePartidas §7(Clique)")) {
            e.setCancelled(true);
            p.performCommand("m partidas");
            return;
        }

        if (ChatColor.stripColor(name).toLowerCase().contains("loja")) {
            e.setCancelled(true);
            p.performCommand("m loja");
            return;
        }

        if (name.equals("§bPerfil §7(Clique)")) {
            e.setCancelled(true);
            new ProfileMenu(plugin).open(p);
            return;
        }

        if (name.equals("§bEscolher Lobby §7(Clique)")) {
            e.setCancelled(true);
            openLobbySelector(p);
            return;
        }

        if (name.equals("§fEsconder Jogadores §7(Clique)") || name.equals("§aMostrar Jogadores §7(Clique)")) {
            e.setCancelled(true);
            toggleHidePlayers(p);
            p.closeInventory();
        }
    }


    @EventHandler
    public void onLobbySelectorClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (e.getInventory() == null || e.getInventory().getTitle() == null) return;
        if (!e.getInventory().getTitle().equals("§8Escolher Lobby")) return;

        e.setCancelled(true);
        Player p = (Player) e.getWhoClicked();

        ItemStack item = e.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;

        String name = item.getItemMeta().getDisplayName();
        if (!name.startsWith("§aLobby #")) return;

        String numberText = name.replace("§aLobby #", "").trim();
        int lobbyNumber;
        try {
            lobbyNumber = Integer.parseInt(numberText);
        } catch (Exception ex) {
            return;
        }

        Location loc = getLobbyLocation(lobbyNumber);
        if (loc == null) {
            p.sendMessage("§cEsse lobby ainda não está configurado.");
            return;
        }

        p.closeInventory();
        plugin.setPlayerState(p, PlayerState.MAIN_LOBBY);
        p.teleport(loc);
        Bukkit.getScheduler().runTaskLater(plugin, () -> giveLobbySafe(p), 5L);
        p.sendMessage("§aVocê entrou no Lobby #" + lobbyNumber + ".");
    }

    private void openLobbySelector(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8Escolher Lobby");

        int amount = Math.max(1, plugin.getConfig().getInt("LobbySystem.Amount", 1));
        if (amount > 7) amount = 7;

        int[] slots = new int[] {10, 11, 12, 13, 14, 15, 16};
        for (int i = 1; i <= amount; i++) {
            Location loc = getLobbyLocation(i);
            String status = loc == null ? "§cNão configurado" : "§aDisponível";
            inv.setItem(slots[i - 1], createItem(Material.EYE_OF_ENDER,
                    "§aLobby #" + i,
                    "§7Status: " + status,
                    "§7",
                    "§eClique para entrar."));
        }

        inv.setItem(22, createItem(Material.BARRIER, "§cFechar", "§7Clique para fechar."));
        p.openInventory(inv);
    }

    private Location getLobbyLocation(int number) {
        if (number <= 1) return getLobby();

        String path = "LobbySystem.Lobbies." + number;
        if (!plugin.getConfig().contains(path + ".world")) return null;

        String worldName = plugin.getConfig().getString(path + ".world");
        if (worldName == null || Bukkit.getWorld(worldName) == null) return null;

        double x = plugin.getConfig().getDouble(path + ".x");
        double y = plugin.getConfig().getDouble(path + ".y");
        double z = plugin.getConfig().getDouble(path + ".z");
        float yaw = (float) plugin.getConfig().getDouble(path + ".yaw");
        float pitch = (float) plugin.getConfig().getDouble(path + ".pitch");
        return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        String msg = e.getMessage();
        if (msg == null) return;
        if (msg.equalsIgnoreCase("/hideplayers") || msg.equalsIgnoreCase("/esconder") || msg.equalsIgnoreCase("/players")) {
            e.setCancelled(true);
            toggleHidePlayers(e.getPlayer());
        }
    }
    
    public ItemStack createItem(Material material, int data, String name, String... lore) {
        ItemStack item = new ItemStack(material, 1, (short) data);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));

        item.setItemMeta(meta);
        return item;
    }

    private void toggleHidePlayers(Player p) {
        if (p == null) return;

        if (hiddenPlayers.contains(p.getUniqueId())) {
            hiddenPlayers.remove(p.getUniqueId());
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online != null && !online.equals(p) && online.getWorld().equals(p.getWorld()) && !Arenas.isInArena(online)) {
                    p.showPlayer(online);
                }
            }
            p.sendMessage("§aJogadores visíveis novamente.");
            p.getInventory().setItem(7,
            	    createItem(Material.INK_SACK, 10,
            	        "§fEsconder Jogadores §7(Clique)",
            	        "§7Deixa o lobby mais limpo.",
            	        "§eClique para alternar."
            	    )
            	);
        } else {
            hiddenPlayers.add(p.getUniqueId());
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online != null && !online.equals(p) && online.getWorld().equals(p.getWorld()) && !Arenas.isInArena(online)) {
                    p.hidePlayer(online);
                }
            }
            p.sendMessage("§cJogadores escondidos no seu lobby.");
            p.getInventory().setItem(7, createItem(Material.INK_SACK, "§aMostrar Jogadores §7(Clique)", "§7Mostra os jogadores novamente.", "§eClique para alternar."));
        }
        p.updateInventory();
    }

}