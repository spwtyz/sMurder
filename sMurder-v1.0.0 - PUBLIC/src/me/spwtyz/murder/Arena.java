package me.spwtyz.murder;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.golde.bukkit.corpsereborn.CorpseAPI.CorpseAPI;
import org.golde.bukkit.corpsereborn.nms.Corpses.CorpseData;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import me.spwtyz.murder.events.TitleAPI;
import me.spwtyz.murder.kits.AdvancedKit;
import me.spwtyz.murder.kits.Kit;
import me.spwtyz.murder.knife.KnifeManager;
import me.spwtyz.murder.win.WinMessageManager;
import me.spwtyz.murder.rooms.Room;
import me.spwtyz.murder.rooms.RoomPasswordGUI;
import me.spwtyz.murder.rooms.RoomModifier;
import me.spwtyz.murder.rooms.RoomFeatureLockManager;
import me.spwtyz.murder.ranked.RankedBorderManager;
import me.spwtyz.murder.scoreboard.ScoreboardManager;

public class Arena implements Listener {
	

	// Boolean - Locations (5)
	public boolean said = false;
	public Location bowloc = null;
	public boolean start = false;
	public boolean isdead = false;
	public boolean wincheck = false;
	public boolean persistentRoom = false;
	public boolean countdownPaused = false;

	// Integers (5)
	public int murdereramount = 0;
	public int detectiveamount = 0;
	public int time = 0;
	public int spawns = 0;
	public int countdown = 0;
	public Player owner;
	//public int maxPlayers = 10;
	public int maxPlayers;
	public int originalMaxPlayers;
	public Player selectingRoleFor = null;
	public PlayerType selectedRole = null;
	private HashMap<UUID, PlayerType> forcedRoles = new HashMap<>();
    private final Map<UUID, Long> staffMenuClickCooldown = new HashMap<UUID, Long>();
	private final Random random = new Random();

	// TNT TAG
	private UUID tntHolder;
	private int tntTagTask = -1;
	private int tntRoundTime = 30;
	private final Set<UUID> tntHitProtectedPlayers = new HashSet<>();
	private final Map<UUID, Long> tntSpeedCooldown = new HashMap<UUID, Long>();

	// HIDE AND SEEK
	private final Set<UUID> hideSeekFrozenSeekers = new HashSet<>();
	private final Map<UUID, Location> hideSeekFrozenLocations = new HashMap<>();
	private int hideSeekReleaseTask = -1;
	private boolean hideSeekReleased = false;

	// Strings
	public String Murderer = "Nenhum";
	public String Detective = "Nenhum";
	public String Hero = "Nenhum";
	public String name;
	public String templateName;
	public String roomId;

	// ArrayLists
	public ArrayList<CorpseData> data = new ArrayList<>();

	public ArrayList<ArmorStand> armor = new ArrayList<>();
	public ArrayList<ArmorStand> sword = new ArrayList<>();

	public ArrayList<Entity> golds = new ArrayList<>();
	public ArrayList<FlyingItems> items = new ArrayList<>();

	public ArrayList<String> pic = new ArrayList<>();
	public ArrayList<String> lists = new ArrayList<>();

	public ArrayList<Player> specs = new ArrayList<>();
	public ArrayList<Player> players = new ArrayList<>();
	public ArrayList<Player> murder = new ArrayList<>();
	public ArrayList<Player> innocents = new ArrayList<>();
	public ArrayList<Player> detective = new ArrayList<>();
	public ArrayList<Player> heros = new ArrayList<>();

	// HashMaps
	public HashMap<String, Integer> kills = new HashMap<>();
	public HashMap<String, Integer> score = new HashMap<>();

	public GameState state;
	public Main plugin;
    private final Map<Player, ItemStack[]> inventoryBackup = new HashMap<>();
    private final Map<Player, ItemStack[]> armorBackup = new HashMap<>();
    private Map<UUID, Integer> swordTask = new HashMap<>();
    private Set<UUID> alreadyAssigned = new HashSet<>();
    public Map<UUID, Kit> selectedKits = new HashMap<>();
    private KnifeManager knifeManager;
    private RankedBorderManager rankedBorderManager;
    
    private GameModeType gameType;

    public GameModeType getGameType() {
        return gameType;
    }
    
    public static Arena getArena(Player p) {
        return Arenas.getArena(p);
    }
    
    public KnifeManager getKnifeManager() {
        return knifeManager;
    }
    
    public Arena(String name, Main plugin) {
        this(name, name, plugin);
    }

    public Arena(String roomId, String templateName, Main plugin) {
        this.name = roomId;
        this.roomId = roomId;
        this.templateName = templateName;
        this.state = GameState.LOBBY;
        this.plugin = plugin;
        this.countdown = 0;
        this.spawns = 0;
        this.said = false;
        this.isdead = false;
        this.start = false;
        this.wincheck = false;
        this.Murderer = "Nenhum";
        this.Detective = "Nenhum";
        this.Hero = "Nenhum";
        this.score.clear();
        this.kills.clear();
        this.lists.clear();
        this.pic.clear();
        this.bowloc = null;
        this.detectiveamount = 0;
        this.murdereramount = 0;

        this.originalMaxPlayers = plugin.SpawnSizeByName(templateName);
        this.maxPlayers = this.originalMaxPlayers;

        this.knifeManager = new KnifeManager(this);

        if (plugin.getConfig().contains("ArenaMode." + templateName)) {
            gameMode = GameModeType.valueOf(plugin.getConfig().getString("ArenaMode." + templateName));
        } else {
            gameMode = GameModeType.NORMAL;
        }
    }

	
	public Plugin getPlugin() {
	    return plugin;
	}
	
	GameModeType gameMode = GameModeType.NORMAL;

	public GameModeType getGameMode() {
	    Room room = getRoomForThisArena();
	    if (room != null) {
	        return room.getGameMode();
	    }
	    return gameMode == null ? GameModeType.NORMAL : gameMode;
	}


	public int getMaxPlayers() {
	    return maxPlayers;
	}

	public int getOriginalMaxPlayers() {
	    return originalMaxPlayers;
	}

	public void setGameMode(GameModeType gameMode) {
	    GameModeType safeMode = gameMode == null ? GameModeType.NORMAL : gameMode;
	    Room room = getRoomForThisArena();
	    if (room != null) {
	        room.setGameMode(safeMode);
	        refreshSabotageTaskHologramsLater();
	        return;
	    }
	    this.gameMode = safeMode;
	    refreshSabotageTaskHologramsLater();
	}

	private void refreshSabotageTaskHologramsLater() {
	    if (plugin == null || plugin.sabotageTaskManager == null) return;
	    org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
	        @Override
	        public void run() {
	            if (plugin != null && plugin.sabotageTaskManager != null) {
	                plugin.sabotageTaskManager.respawnTaskHolograms();
	            }
	        }
	    }, 2L);
	}

	private Room getRoomForThisArena() {
	    if (plugin == null || plugin.roomManager == null) return null;
	    return plugin.roomManager.getRoomByArena(this);
	}

	public String getRoomLeaderName() {
	    Room room = getRoomForThisArena();
	    if (room == null || room.isMainRoom()) return "Nenhum";
	    Player leader = room.getOwner();
	    return leader != null ? leader.getName() : "Nenhum";
	}

	public String getRoomDisplayName() {
	    Room room = getRoomForThisArena();
	    if (room != null) {
	        if (room.isMainRoom() && plugin != null && plugin.roomManager != null) {
	            return plugin.roomManager.getPublicRoomDisplayName(room);
	        }
	        return room.getDisplayName();
	    }
	    if (name != null && name.toUpperCase().startsWith("ROOM-")) {
	        return "Sala Privada";
	    }
	    if (plugin != null && plugin.roomManager != null) {
	        return plugin.roomManager.getPublicArenaDisplayName(this);
	    }
	    return name == null ? "Sala" : name;
	}

	public boolean isCountdownPaused() {
	    return countdownPaused;
	}

	public String getCountdownPauseStatus() {
	    return countdownPaused ? "Pausado" : "Ativo";
	}

	public String getRoomModeNamePlain() {
	    return getGameMode() == null ? "Normal" : getGameMode().getDisplayName();
	}

	public boolean isRoomLeader(Player p) {
	    if (p == null) return false;
	    Room room = getRoomForThisArena();
	    if (room == null || room.isMainRoom()) return false;
	    Player leader = room.getOwner();
	    return leader != null && leader.equals(p);
	}

	public boolean isStaff(Player p) {
	    return p != null && (p.hasPermission("murder.admin") || p.hasPermission("smurder.admin")
	            || p.hasPermission("murder.staff") || p.isOp());
	}

	public boolean canManageRoom(Player p) {
	    if (p == null) return false;
	    Room room = getRoomForThisArena();
	    if (room == null || room.isMainRoom()) return isStaff(p);
	    return isRoomLeader(p) || isStaff(p) || room.isModerator(p);
	}

    public boolean canFullyManageRoom(Player p) {
        if (p == null) return false;
        Room room = getRoomForThisArena();
        if (room == null || room.isMainRoom()) return isStaff(p);
        return isRoomLeader(p) || isStaff(p);
    }

    private void clearForcedRoleSelections() {
        forcedRoles.clear();
        selectedRole = null;
    }

	private void giveRoomLobbyItems(Player p) {
	    if (p == null || !p.isOnline()) return;
	    p.getInventory().clear();

	    // Slot 1 visual / index 0
	    ItemStack kit = new ItemStack(Material.CHEST);
	    ItemMeta kitMeta = kit.getItemMeta();
	    kitMeta.setDisplayName(ChatColor.GREEN + "Kits (Clique)");
	    kitMeta.setLore(Arrays.asList(ChatColor.GRAY + "Clique para escolher seu kit."));
	    kit.setItemMeta(kitMeta);
	    p.getInventory().setItem(0, kit);

	    // Slot 2 visual / index 1
	    ItemStack map = new ItemStack(Material.EMPTY_MAP);
	    ItemMeta mapMeta = map.getItemMeta();
	    mapMeta.setDisplayName(ChatColor.AQUA + "Votar Mapa " + ChatColor.GRAY + "(Clique)");
	    mapMeta.setLore(Arrays.asList(ChatColor.GRAY + "Clique para votar no mapa", ChatColor.GRAY + "da próxima partida."));
	    map.setItemMeta(mapMeta);
	    p.getInventory().setItem(1, map);

	    // Slot 5 visual / index 4 - Loja/Cosméticos com custom head
	    String shopName = (plugin.seasonalEventManager != null && plugin.seasonalEventManager.isEventActive())
	            ? plugin.seasonalEventManager.getLobbyShopItemName()
	            : ChatColor.YELLOW + "Loja " + ChatColor.GRAY + "(Clique)";
	    p.getInventory().setItem(4, createCustomHeadItem(
	            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjEyNjBkNzg2MGQ4YzJkNmM3ZjVmNTc4YjAyOWMzNzVlNGM0NmNlY2IwNGMzYjAyYmE0MzFiOTJlMzVlNzUzNSJ9fX0=",
	            shopName,
	            Arrays.asList(ChatColor.GRAY + "Clique para abrir a loja.")));

	    if (canManageRoom(p)) {
	        ItemStack manage = new ItemStack(Material.WORKBENCH);
	        ItemMeta manageMeta = manage.getItemMeta();
	        manageMeta.setDisplayName(ChatColor.YELLOW + "Gerenciar Sala (Clique)");
	        manage.setItemMeta(manageMeta);
	        p.getInventory().setItem(7, manage);
	    }

	    ItemStack bed = new ItemStack(Material.getMaterial(plugin.settings.getConfig().getInt("quit2.item-id")), 1,
	            (short) plugin.settings.getConfig().getInt("quit2.item-subid"));
	    ItemMeta bedMeta = bed.getItemMeta();
	    bedMeta.setDisplayName(Utils.FormatText(p, plugin.settings.getConfig().getString("quit2.item-name")));
	    bedMeta.setLore(Arrays.asList(Utils.FormatText(p, plugin.settings.getConfig().getString("quit2.item-lore"))));
	    bed.setItemMeta(bedMeta);
	    p.getInventory().setItem(8, bed);
	    p.updateInventory();
	}

	public void refreshRoomLobbyItems() {
	    if (state != GameState.LOBBY && state != GameState.STARTING) return;
	    for (Player player : new ArrayList<Player>(players)) {
	        giveRoomLobbyItems(player);
	    }
	}

	public void refreshRoomScoreboards() {
	    if (plugin == null) return;
	    for (Player player : new ArrayList<Player>(players)) {
	        if (player == null) continue;
	        plugin.scoreboards.remove(player.getName());
	        plugin.scorestate.remove(player.getName());
	        if (player.isOnline()) {
	            plugin.setScoreboard(player);
	        }
	    }
	}

	public void transferRoomOwnerIfNeeded(Player leaving) {
	    // Liderança fixa: quem criou a sala continua sendo líder mesmo se sair.
	    // Staff ainda consegue gerenciar pelo canManageRoom().
	    refreshRoomLobbyItems();
	}
	
	
	
	public static String getGameModeName(GameModeType mode) {

	    switch (mode) {

	    case NORMAL:
	        return "§aNormal";

	    case ALL_MURDER:
	        return "§cTodos Assassinos";

	    case TNT_TAG:
	        return "§6TntTag";

	    case RANKED:
	        return "§eRanked";

	    case HIDE_AND_SEEK:
	        return "§dEsconde-Esconde";

    case SABOTAGE:
        return "§5AMONG US";

	    default:
	        return "§aNormal";
	    }
	}
	
	/**
	 * Atualiza a visibilidade dos players desta arena sem loop global.
	 * Regra: vivos veem vivos, vivos não veem specs, specs veem todos da arena.
	 */
	public void refreshVisibility() {
	    if (plugin != null && plugin.spectatorManager != null) {
	        plugin.spectatorManager.updateVisibility(this);
	        return;
	    }
	}


	public void killPlayer(Player victim, Player killer) {

	    if (plugin.killCamManager != null && plugin.killCamManager.shouldUseKillCam(this, victim, killer)) {
	        if (players.contains(victim)) players.remove(victim);
	        if (!specs.contains(victim)) specs.add(victim);
	        plugin.killCamManager.start(victim, killer, this, "§cVocê morreu!");
	        return;
	    }
	    if (plugin.spectatorManager != null) {
	        plugin.spectatorManager.makeSpectator(victim, this, "§cVocê morreu!");
	        return;
	    }
	    players.remove(victim);
	    if (!specs.contains(victim)) specs.add(victim);
	    plugin.setPlayerState(victim, PlayerState.SPECTATOR);
	    victim.setGameMode(GameMode.ADVENTURE);
	    victim.teleport(plugin.getSpec(this));
	    refreshVisibility();
	}
	
	@EventHandler
	public void onLobbyItem(PlayerInteractEvent e) {

	    Player p = e.getPlayer();

	    if (!Arenas.isInArena(p)) return;

	    if (getState() == GameState.INGAME) {
	        ItemStack item = p.getItemInHand();
	        if (item == null) return;

	        Material type = item.getType();

	        // bloqueia qualquer item de lobby comum
	        if (type == Material.CHEST ||
	            type == Material.COMPASS ||
	            type == Material.NETHER_STAR) {

	            e.setCancelled(true);
	        }
	    }
	}
	
	   // =========================
    //  INVENTORY SAVER
    // =========================
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {

	    Player p = e.getPlayer();

	    if (e.getItem() == null) return;

	    if (!e.getItem().hasItemMeta()) return;

	    if (!e.getItem().getItemMeta().hasDisplayName()) return;

	    String name = e.getItem().getItemMeta().getDisplayName();

	    if (name.contains(ChatColor.GREEN + "Kits (Clique)")) {

	        Arena arena = plugin.playerArena.get(p.getUniqueId());

	        if (arena == null) return;

	        plugin.menuManager.openKitMenu(p, arena);

	        e.setCancelled(true);
	        return;
	    }

	    if (name.contains(ChatColor.AQUA + "Votar Mapa")) {
	        Arena arena = plugin.playerArena.get(p.getUniqueId());
	        if (arena == null) return;
	        e.setCancelled(true);
	        arena.openMapVoteMenu(p);
	        return;
	    }

	    if (name.contains(ChatColor.YELLOW + "Loja")) {
	        e.setCancelled(true);
	        p.performCommand("m loja");
	        return;
	    }
	}
    
    public void saveInventory(Player p) {
        inventoryBackup.put(p, p.getInventory().getContents());
        armorBackup.put(p, p.getInventory().getArmorContents());
    }
    
    public void restoreInventory(Player p) {
        if (inventoryBackup.containsKey(p)) {
            p.getInventory().setContents(inventoryBackup.get(p));
        }

        if (armorBackup.containsKey(p)) {
            p.getInventory().setArmorContents(armorBackup.get(p));
        }

        inventoryBackup.remove(p);
        armorBackup.remove(p);
    }
	
	private ItemStack createMenuItem(Material material, String name, List<String> lore) {
	    ItemStack item = new ItemStack(material);
	    ItemMeta meta = item.getItemMeta();
	    meta.setDisplayName(name);
	    if (lore != null) meta.setLore(lore);
	    item.setItemMeta(meta);
	    return item;
	}

	public void openMapVoteMenu(Player p) {
	    Room room = getRoomForThisArena();
	    Inventory inv = Bukkit.createInventory(null, 27, "Votar Mapa");

	    int slot = 10;
	    Set<String> addedMaps = new HashSet<String>();
	    for (Arena mapArena : Arenas.getArenas()) {
	        if (mapArena == null) continue;
	        String mapName = mapArena.getTemplateName();
	        if (mapName == null || mapName.isEmpty()) mapName = mapArena.getName();
	        if (mapName == null || mapName.trim().isEmpty()) continue;
	        if (!isMapAllowedForCurrentMode(mapName)) continue;
	        String key = normalizeMapName(mapName);
	        if (addedMaps.contains(key)) continue;
	        addedMaps.add(key);

	        int votes = room == null ? 0 : room.getVotesForMap(mapName);
	        String selected = room == null ? getTemplateName() : room.getSelectedMapName();
	        boolean isSelected = selected != null && selected.equalsIgnoreCase(mapName);

	        ItemStack item = createMenuItem(Material.MAP,
	                (isSelected ? ChatColor.GREEN : ChatColor.YELLOW) + mapName,
	                Arrays.asList(
	                        ChatColor.GRAY + "Votos: " + ChatColor.WHITE + votes,
	                        ChatColor.GRAY + "Atual: " + (isSelected ? ChatColor.GREEN + "Sim" : ChatColor.RED + "Não"),
	                        ChatColor.GRAY + "Modo: " + ChatColor.WHITE + getRoomModeNamePlain(),
	                        "",
	                        ChatColor.YELLOW + "Clique para votar neste mapa."
	                ));
	        inv.setItem(slot, item);
	        slot++;
	        if (slot == 17) slot = 19;
	        if (slot >= 26) break;
	    }

	    if (addedMaps.isEmpty()) {
	        inv.setItem(13, createMenuItem(Material.BARRIER, ChatColor.RED + "Nenhum mapa liberado", Arrays.asList(
	                ChatColor.GRAY + "Configure os mapas em:",
	                ChatColor.YELLOW + "hide-and-seek.maps",
	                ChatColor.GRAY + "ou remova o mapa desta lista."
	        )));
	    }

	    p.openInventory(inv);
	}

	private String normalizeMapName(String mapName) {
	    return mapName == null ? "" : ChatColor.stripColor(mapName).trim().toLowerCase().replace(" ", "_");
	}

	public boolean isMapAllowedForCurrentMode(String mapName) {
	    List<String> hideMaps = plugin.getConfig().getStringList("hide-and-seek.maps");
	    if (hideMaps == null || hideMaps.isEmpty()) return true;

	    String key = normalizeMapName(mapName);
	    boolean isHideMap = false;
	    for (String configured : hideMaps) {
	        if (key.equals(normalizeMapName(configured))) {
	            isHideMap = true;
	            break;
	        }
	    }

	    if (RoomFeatureLockManager.isMapDisabled(plugin, mapName)) return false;

	    if (getGameMode() == GameModeType.HIDE_AND_SEEK) {
	        return isHideMap;
	    }

	    return !isHideMap;
	}

	private String getRandomAllowedMapForCurrentMode() {
	    List<String> maps = new ArrayList<String>();
	    for (Arena mapArena : Arenas.getArenas()) {
	        if (mapArena == null) continue;
	        String mapName = mapArena.getTemplateName();
	        if (mapName == null || mapName.isEmpty()) mapName = mapArena.getName();
	        if (mapName != null && !mapName.trim().isEmpty() && isMapAllowedForCurrentMode(mapName)) {
	            String key = normalizeMapName(mapName);
	            boolean exists = false;
	            for (String added : maps) {
	                if (normalizeMapName(added).equals(key)) { exists = true; break; }
	            }
	            if (!exists) maps.add(mapName);
	        }
	    }
	    if (maps.isEmpty()) return getTemplateName();
	    return maps.get(random.nextInt(maps.size()));
	}

	public void openGameModeMenu(Player p) {
	    Inventory inv = Bukkit.createInventory(null, 36, "Selecionar Modo");

	    GameModeType current = getGameMode();

	    inv.setItem(11, createMenuItem(Material.EYE_OF_ENDER,
	            (current == GameModeType.NORMAL ? ChatColor.GREEN : ChatColor.YELLOW) + "Modo Normal",
	            Arrays.asList(ChatColor.GRAY + "Murder clássico com detective.", "", ChatColor.YELLOW + "Clique para selecionar.")));

	    inv.setItem(13, createMenuItem(Material.TNT,
	            (current == GameModeType.TNT_TAG ? ChatColor.GREEN : ChatColor.GOLD) + "TntTag",
	            Arrays.asList(ChatColor.GRAY + "Passe a TNT batendo nos players.", ChatColor.GRAY + "Quem ficar com ela explode.", "", ChatColor.YELLOW + "Clique para selecionar.")));

	    inv.setItem(15, createMenuItem(Material.IRON_SWORD,
	            (current == GameModeType.ALL_MURDER ? ChatColor.GREEN : ChatColor.RED) + "Todos Assassinos",
	            Arrays.asList(ChatColor.GRAY + "Todos entram como assassinos.", "", ChatColor.YELLOW + "Clique para selecionar.")));

        Room roomForModeMenu = getRoomForThisArena();
        boolean canShowRanked = roomForModeMenu == null || roomForModeMenu.isMainRoom();
        if (canShowRanked) {
	    inv.setItem(22, createMenuItem(Material.GOLD_INGOT,
	            (current == GameModeType.RANKED ? ChatColor.GREEN : ChatColor.GOLD) + "Ranked",
	            Arrays.asList(ChatColor.GRAY + "Modo competitivo com RP e patentes.",
	                    ChatColor.GRAY + "Exclusivo de sala publica.",
                            ChatColor.GRAY + "Limite: 20 jogadores.",
	                    "",
	                    ChatColor.YELLOW + "Clique para selecionar.")));
        } else {
            inv.setItem(22, createMenuItem(Material.BARRIER,
                    ChatColor.RED + "Ranked indisponivel",
                    Arrays.asList(ChatColor.GRAY + "Ranked agora e exclusivo",
                            ChatColor.GRAY + "das salas publicas do servidor.",
                            "",
                            ChatColor.RED + "Salas privadas nao podem usar Ranked.")));
        }

	    inv.setItem(31, createMenuItem(Material.STICK,
	            (current == GameModeType.HIDE_AND_SEEK ? ChatColor.GREEN : ChatColor.LIGHT_PURPLE) + "Esconde-Esconde",
	            Arrays.asList(ChatColor.GRAY + "Estilo TazerCraft.",
	                    ChatColor.GRAY + "Um procurador espera enquanto os outros se escondem.",
	                    ChatColor.GRAY + "Quem for pego vira procurador.",
	                    "",
	                    ChatColor.YELLOW + "Clique para selecionar.")));

        inv.setItem(29, createMenuItem(Material.NETHER_STAR,
                (current == GameModeType.SABOTAGE ? ChatColor.GREEN : ChatColor.DARK_PURPLE) + "AMONG US",
                Arrays.asList(ChatColor.GRAY + "Murder com mecanicas estilo Among Us.",
                        ChatColor.GRAY + "Roles continuam: Murder, Detective e Inocentes.",
                        ChatColor.GRAY + "O Murder recebe sabotagens durante a partida.",
                        "",
                        ChatColor.YELLOW + "Clique para selecionar.")));

	    p.openInventory(inv);
	}

	private void applyWinningRoomMap() {
	    Room room = getRoomForThisArena();
	    if (room == null) return;
	    String winner = null;
	    if (room.hasMapVotes()) {
	        winner = room.getWinningMapName();
	    }
	    if (winner == null || winner.isEmpty() || !isMapAllowedForCurrentMode(winner)) {
	        winner = getRandomAllowedMapForCurrentMode();
	    }
	    if (winner == null || winner.isEmpty()) return;
	    room.setSelectedMapName(winner);
	    room.clearMapVotes();
	    this.templateName = winner;
	    refreshSabotageTaskHologramsLater();
	}

    private ItemStack createCustomHeadItem(String textureValue, String name, List<String> lore) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        try {
            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            profile.getProperties().put("textures", new Property("textures", textureValue));
            Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (Throwable ex) {
            meta.setOwner("MHF_Question");
        }
        head.setItemMeta(meta);
        return head;
    }

	public void openManageMenu(Player p) {
	    Inventory inv = Bukkit.createInventory(null, 54, "Gerenciar Sala");
	    Room room = getRoomForThisArena();

	    ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
	    ItemMeta glassMeta = glass.getItemMeta();
	    glassMeta.setDisplayName(" ");
	    glass.setItemMeta(glassMeta);
	    for (int i = 0; i < inv.getSize(); i++) {
	        inv.setItem(i, glass);
	    }

	    ItemStack info = createMenuItem(Material.PAPER, ChatColor.YELLOW + "Informações da Sala", Arrays.asList(
	        ChatColor.GRAY + "Sala: " + ChatColor.WHITE + getRoomDisplayName(),
	        ChatColor.GRAY + "Líder: " + ChatColor.WHITE + getRoomLeaderName(),
	        ChatColor.GRAY + "Modo: " + ChatColor.WHITE + getRoomModeNamePlain(),
	        ChatColor.GRAY + "Mapa: " + ChatColor.WHITE + (room != null ? room.getSelectedMapName() : getTemplateName()),
	        ChatColor.GRAY + "Jogadores: " + ChatColor.WHITE + players.size() + "/" + maxPlayers
	    ));

	    ItemStack minus = createMenuItem(Material.REDSTONE, ChatColor.RED + "-1 Jogador", Arrays.asList(
	        ChatColor.GRAY + "Diminui o limite da sala.",
	        ChatColor.GRAY + "Atual: " + ChatColor.WHITE + maxPlayers
	    ));

	    ItemStack plus = createMenuItem(Material.EMERALD, ChatColor.GREEN + "+1 Jogador", Arrays.asList(
	        ChatColor.GRAY + "Aumenta o limite da sala.",
	        ChatColor.GRAY + "Atual: " + ChatColor.WHITE + maxPlayers
	    ));

	    ItemStack start = createMenuItem(Material.DIAMOND_SWORD, ChatColor.GREEN + "Iniciar Partida", Arrays.asList(
	        ChatColor.GRAY + "Inicia a partida desta sala.",
	        "",
	        ChatColor.YELLOW + "Clique para iniciar."
	    ));

	    ItemStack pauseTimer = createMenuItem(Material.WATCH, (countdownPaused ? ChatColor.RED + "Retomar Temporizador" : ChatColor.YELLOW + "Pausar Temporizador"), Arrays.asList(
	        ChatColor.GRAY + "Pausa ou retoma o contador",
	        ChatColor.GRAY + "de início desta sala.",
	        "",
	        ChatColor.GRAY + "Status: " + (countdownPaused ? ChatColor.RED + "Pausado" : ChatColor.GREEN + "Ativo"),
	        ChatColor.YELLOW + "Clique para alternar."
	    ));

	    ItemStack close = createMenuItem(Material.BARRIER, ChatColor.RED + "Fechar Sala", Arrays.asList(
	        ChatColor.GRAY + "Fecha apenas salas criadas por player.",
	        ChatColor.RED + "A sala principal não pode ser fechada."
	    ));

	    ItemStack murderItem = createMenuItem(Material.IRON_SWORD, ChatColor.RED + "Definir Murder", Arrays.asList(
	        ChatColor.GRAY + "Escolha quem será o Murder/Tagger.",
	        ChatColor.GRAY + "A role aplica quando a partida iniciar."
	    ));

	    ItemStack detectiveItem = createMenuItem(Material.BOW, ChatColor.AQUA + "Definir Detective", Arrays.asList(
	        ChatColor.GRAY + "Escolha quem será o Detective.",
	        ChatColor.GRAY + "Indisponível em TNTTag/Todos Assassinos."
	    ));

	    List<String> modeLore = new ArrayList<>();
	    modeLore.add(ChatColor.GRAY + "Atual: " + ChatColor.WHITE + getRoomModeNamePlain());
	    modeLore.add("");
	    modeLore.add(ChatColor.YELLOW + "Clique para abrir o menu de modos.");
	    ItemStack mode = createMenuItem(Material.NETHER_STAR, ChatColor.LIGHT_PURPLE + "Modo de Jogo", modeLore);

	    ItemStack password = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
	    SkullMeta passMeta = (SkullMeta) password.getItemMeta();
	    passMeta.setOwner("MHF_Chest");
	    passMeta.setDisplayName(ChatColor.GOLD + "Senha da Sala");
	    passMeta.setLore(Arrays.asList(
	            ChatColor.GRAY + "Status: " + ((room != null && room.hasPassword()) ? ChatColor.GREEN + "Ativada" : ChatColor.RED + "Sem senha"),
	            "",
	            ChatColor.YELLOW + "Clique para alterar a senha."
	    ));
	    password.setItemMeta(passMeta);

	    ItemStack modifiers = createMenuItem(Material.GOLD_INGOT, ChatColor.GOLD + "Modificadores", Arrays.asList(
	        ChatColor.GRAY + "Ative eventos especiais nesta sala.",
	        ChatColor.GRAY + "Ex: Cursed Gold.",
	        "",
	        ChatColor.YELLOW + "Clique para abrir."
	    ));

	    ItemStack playersManager = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
	    SkullMeta playersMeta = (SkullMeta) playersManager.getItemMeta();
	    playersMeta.setOwner("MHF_Steve");
	    playersMeta.setDisplayName(ChatColor.RED + "Gerenciar Players");
	    playersMeta.setLore(Arrays.asList(ChatColor.GRAY + "Remova jogadores desta sala.", "", ChatColor.YELLOW + "Clique para abrir."));
	    playersManager.setItemMeta(playersMeta);

    ItemStack renameRoom = createMenuItem(Material.NAME_TAG, ChatColor.AQUA + "Renomear Sala", Arrays.asList(
            ChatColor.GRAY + "Nome atual: " + ChatColor.WHITE + getRoomDisplayName(),
            ChatColor.GRAY + "Clique e digite o novo nome",
            ChatColor.GRAY + "da sala no chat.",
            "",
            ChatColor.YELLOW + "Use: cancelar para cancelar",
            ChatColor.YELLOW + "Use: resetar para voltar ao padrão."
    ));

    ItemStack replayItem = createMenuItem(Material.EYE_OF_ENDER, ChatColor.AQUA + "Replays", Arrays.asList(
            ChatColor.GRAY + "Veja os últimos segundos",
            ChatColor.GRAY + "dos jogadores desta sala.",
            "",
            ChatColor.RED + "Apenas staff."
    ));

    ItemStack staffArea = createMenuItem(Material.REDSTONE_COMPARATOR, ChatColor.RED + "Área Staff", Arrays.asList(
            ChatColor.GRAY + "Desative/ative modos e modificadores",
            ChatColor.GRAY + "para todas as salas do servidor.",
            "",
            ChatColor.RED + "Apenas staff."
    ));

    inv.setItem(4, info);
	    inv.setItem(19, minus);
	    inv.setItem(20, plus);
	    inv.setItem(22, start);
	    inv.setItem(13, pauseTimer);
	    inv.setItem(24, close);
	    inv.setItem(28, murderItem);
	    inv.setItem(29, detectiveItem);
	    inv.setItem(31, mode);
	    inv.setItem(32, renameRoom);
	    inv.setItem(33, password);
	    inv.setItem(34, modifiers);
	    inv.setItem(40, playersManager);
    if (isStaff(p)) {
        // slot 42 livre: Replay/KillCam removido por performance.
        inv.setItem(43, staffArea);
    }

    p.openInventory(inv);
	}

	public void openRoomModifiersMenu(Player p) {
    Inventory inv = Bukkit.createInventory(null, 27, "Modificadores da Sala");
    Room room = getRoomForThisArena();
    boolean cursedEnabled = room != null && room.hasModifier(RoomModifier.CURSED_GOLD);
    boolean sabotageEnabled = room != null && room.hasModifier(RoomModifier.SABOTAGE);

    ItemStack cursed = createMenuItem(Material.GOLD_INGOT,
            (cursedEnabled ? ChatColor.GREEN : ChatColor.RED) + "Cursed Gold",
            Arrays.asList(
                    ChatColor.GRAY + "Fragmentos podem virar ouro amaldiçoado.",
                    ChatColor.GRAY + "Usa o mesmo IRON_INGOT do mapa.",
                    "",
                    ChatColor.GRAY + "Status: " + (cursedEnabled ? ChatColor.GREEN + "Ativado" : ChatColor.RED + "Desativado"),
                    ChatColor.YELLOW + "Clique para alternar."
            ));

    ItemStack sabotage = createMenuItem(Material.NETHER_STAR,
            (sabotageEnabled ? ChatColor.GREEN : ChatColor.RED) + "AMONG US",
            Arrays.asList(
                    ChatColor.GRAY + "Permite que o Murder use sabotagens",
                    ChatColor.GRAY + "durante a partida.",
                    "",
                    ChatColor.GRAY + "Status: " + (sabotageEnabled ? ChatColor.GREEN + "Ativado" : ChatColor.RED + "Desativado"),
                    ChatColor.YELLOW + "Clique para alternar."
            ));

    inv.setItem(11, cursed);
    inv.setItem(15, sabotage);
    p.openInventory(inv);
}


    public void openRoomStaffMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 45, "§8Área Staff §7- §cControle");
        ItemStack glass = createMenuItem(Material.STAINED_GLASS_PANE, "§8", Arrays.asList(""));
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, glass);

        inv.setItem(4, createMenuItem(Material.REDSTONE_COMPARATOR, "§c§lÁrea Staff", Arrays.asList(
                "§7Controle global persistente.",
                "§7Tudo fica salvo na config após reiniciar.",
                "",
                "§eEscolha uma categoria abaixo."
        )));

        inv.setItem(11, createMenuItem(Material.NETHER_STAR, "§aModos de Jogo", Arrays.asList(
                "§7Ativar/desativar Normal, Ranked, TNTTag,", "§7Todos Assassinos e Esconde-Esconde.", "", "§eClique para abrir.")));
        inv.setItem(13, createMenuItem(Material.REDSTONE, "§6Modificadores", Arrays.asList(
                "§7Ativar/desativar Cursed Gold, AMONG US", "§7e próximos modificadores.", "", "§eClique para abrir.")));
        inv.setItem(15, createMenuItem(Material.CHEST, "§bKits", Arrays.asList(
                "§7Ativar/desativar kits da sala.", "§7Kits desativados somem do menu.", "", "§eClique para abrir.")));
        inv.setItem(31, createMenuItem(Material.MAP, "§dMapas", Arrays.asList(
                "§7Ativar/desativar mapas globalmente.", "§7Mapas desativados somem da votação", "§7e não são sorteados.", "", "§eClique para abrir.")));
        inv.setItem(40, createMenuItem(Material.ARROW, "§eVoltar", Arrays.asList("§7Voltar para o Gerenciar Sala.")));
        p.openInventory(inv);
    }

    public void openStaffModesMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8Staff §7- §aModos de Jogo");
        ItemStack glass = createMenuItem(Material.STAINED_GLASS_PANE, "§8", Arrays.asList(""));
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, glass);
        inv.setItem(4, createMenuItem(Material.NETHER_STAR, "§a§lModos de Jogo", Arrays.asList("§7Clique para ativar/desativar.")));
        int[] slots = new int[] {10, 11, 12, 13, 14, 15, 16, 19, 20};
        int index = 0;
        for (GameModeType mode : GameModeType.values()) {
            if (index >= slots.length) break;
            boolean disabled = RoomFeatureLockManager.isModeDisabled(plugin, mode);
            inv.setItem(slots[index++], createMenuItem(disabled ? Material.REDSTONE_BLOCK : Material.EMERALD_BLOCK,
                    (disabled ? "§c" : "§a") + "Modo: §f" + mode.getDisplayName(),
                    Arrays.asList("§7Categoria: §fModo de Jogo", "§7Status: " + (disabled ? "§cDesativado" : "§aAtivado"), "", disabled ? "§aClique para ativar." : "§cClique para desativar.", "§8Persistente na config.")));
        }
        inv.setItem(49, createMenuItem(Material.ARROW, "§eVoltar", Arrays.asList("§7Voltar para a Área Staff.")));
        p.openInventory(inv);
    }

    public void openStaffModifiersMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 45, "§8Staff §7- §6Modificadores");
        ItemStack glass = createMenuItem(Material.STAINED_GLASS_PANE, "§8", Arrays.asList(""));
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, glass);
        inv.setItem(4, createMenuItem(Material.REDSTONE, "§6§lModificadores", Arrays.asList("§7Clique para ativar/desativar.")));
        boolean cursedDisabled = RoomFeatureLockManager.isModifierDisabled(plugin, RoomModifier.CURSED_GOLD);
        inv.setItem(20, createMenuItem(cursedDisabled ? Material.REDSTONE_BLOCK : Material.GOLD_BLOCK,
                (cursedDisabled ? "§c" : "§a") + "Modificador: §fCursed Gold",
                Arrays.asList("§7Status: " + (cursedDisabled ? "§cDesativado" : "§aAtivado"), "", cursedDisabled ? "§aClique para ativar." : "§cClique para desativar.")));
        boolean sabotageDisabled = RoomFeatureLockManager.isModifierDisabled(plugin, RoomModifier.SABOTAGE);
        inv.setItem(24, createMenuItem(sabotageDisabled ? Material.REDSTONE_BLOCK : Material.NETHER_STAR,
                (sabotageDisabled ? "§c" : "§a") + "Modificador: §fAMONG US",
                Arrays.asList("§7Status: " + (sabotageDisabled ? "§cDesativado" : "§aAtivado"), "", sabotageDisabled ? "§aClique para ativar." : "§cClique para desativar.")));
        inv.setItem(40, createMenuItem(Material.ARROW, "§eVoltar", Arrays.asList("§7Voltar para a Área Staff.")));
        p.openInventory(inv);
    }

    public void openStaffKitsMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8Staff §7- §bKits");
        ItemStack glass = createMenuItem(Material.STAINED_GLASS_PANE, "§8", Arrays.asList(""));
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, glass);
        inv.setItem(4, createMenuItem(Material.CHEST, "§b§lKits", Arrays.asList("§7Clique para ativar/desativar kits.")));
        int slot = 10;
        for (Kit kit : plugin.kitManager.getAllKits()) {
            boolean disabled = RoomFeatureLockManager.isKitDisabled(plugin, kit);
            inv.setItem(slot, createMenuItem(disabled ? Material.REDSTONE_BLOCK : kit.getIcon(),
                    (disabled ? "§c" : "§a") + "Kit: §f" + kit.getName(),
                    Arrays.asList("§7Tipo: §f" + plugin.kitManager.getTypeDisplay(kit), "§7Status: " + (disabled ? "§cDesativado" : "§aAtivado"), "", disabled ? "§aClique para ativar." : "§cClique para desativar.")));
            slot++;
            if (slot == 17) slot = 19;
            if (slot == 26) slot = 28;
            if (slot >= 44) break;
        }
        inv.setItem(49, createMenuItem(Material.ARROW, "§eVoltar", Arrays.asList("§7Voltar para a Área Staff.")));
        p.openInventory(inv);
    }

    public void openStaffMapsMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8Staff §7- §dMapas");
        ItemStack glass = createMenuItem(Material.STAINED_GLASS_PANE, "§8", Arrays.asList(""));
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, glass);
        inv.setItem(4, createMenuItem(Material.MAP, "§d§lMapas", Arrays.asList("§7Clique para ativar/desativar mapas.")));
        int slot = 10;
        Set<String> added = new HashSet<String>();
        for (Arena mapArena : Arenas.getArenas()) {
            if (mapArena == null) continue;
            String mapName = mapArena.getTemplateName();
            if (mapName == null || mapName.trim().isEmpty()) mapName = mapArena.getName();
            if (mapName == null || mapName.trim().isEmpty()) continue;
            String key = normalizeMapName(mapName);
            if (added.contains(key)) continue;
            added.add(key);
            boolean disabled = RoomFeatureLockManager.isMapDisabled(plugin, mapName);
            inv.setItem(slot, createMenuItem(disabled ? Material.REDSTONE_BLOCK : Material.MAP,
                    (disabled ? "§c" : "§a") + "Mapa: §f" + mapName,
                    Arrays.asList("§7Status: " + (disabled ? "§cDesativado" : "§aAtivado"), "§7Mapa de esconde-esconde: " + (isHideSeekConfiguredMap(mapName) ? "§aSim" : "§cNão"), "", disabled ? "§aClique para ativar." : "§cClique para desativar.")));
            slot++;
            if (slot == 17) slot = 19;
            if (slot == 26) slot = 28;
            if (slot >= 44) break;
        }
        inv.setItem(49, createMenuItem(Material.ARROW, "§eVoltar", Arrays.asList("§7Voltar para a Área Staff.")));
        p.openInventory(inv);
    }

    private boolean isHideSeekConfiguredMap(String mapName) {
        String key = normalizeMapName(mapName);
        for (String configured : plugin.getConfig().getStringList("hide-and-seek.maps")) {
            if (key.equals(normalizeMapName(configured))) return true;
        }
        return false;
    }


	public void openPlayerSelectMenu(Player admin) {

	    Inventory inv = Bukkit.createInventory(null, 54, "Selecionar Jogador");

	    for (Player p : players) {

	        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
	        SkullMeta meta = (SkullMeta) head.getItemMeta();

	        meta.setOwner(p.getName());
	        meta.setDisplayName("§e" + p.getName());

	        head.setItemMeta(meta);

	        inv.addItem(head);
	    }

	    admin.openInventory(inv);
	}

	public void openRemovePlayerMenu(Player admin) {

	    Inventory inv = Bukkit.createInventory(null, 54, "Gerenciar Players");
	    Room room = getRoomForThisArena();

	    for (Player p : players) {
	        if (p == null) continue;

	        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
	        SkullMeta meta = (SkullMeta) head.getItemMeta();

	        meta.setOwner(p.getName());
	        meta.setDisplayName("§eJogador " + p.getName());
	        boolean banned = room != null && room.isBanned(p);
	        boolean mod = room != null && room.isModerator(p);
	        meta.setLore(Arrays.asList(
	                ChatColor.GRAY + "Status: " + (banned ? ChatColor.RED + "Banido" : ChatColor.GREEN + "Liberado"),
	                ChatColor.GRAY + "Moderador: " + (mod ? ChatColor.GREEN + "Sim" : ChatColor.RED + "Não"),
	                "",
	                ChatColor.YELLOW + "Clique esquerdo: remover da sala",
	                ChatColor.YELLOW + "Clique direito: banir/desbanir",
	                ChatColor.YELLOW + "Shift clique: tornar/remover moderador"
	        ));

	        head.setItemMeta(meta);
	        inv.addItem(head);
	    }

	    if (room != null) {
	        for (Map.Entry<UUID, String> entry : room.getBannedNames().entrySet()) {
	            String name = entry.getValue() == null ? entry.getKey().toString() : entry.getValue();
	            Player online = Bukkit.getPlayer(name);
	            if (online != null && players.contains(online)) continue;

	            ItemStack ban = new ItemStack(Material.BARRIER);
	            ItemMeta meta = ban.getItemMeta();
	            meta.setDisplayName(ChatColor.RED + "Banido " + name);
	            meta.setLore(Arrays.asList(
	                    ChatColor.GRAY + "Este jogador está banido da sala.",
	                    ChatColor.YELLOW + "Clique para desbanir."
	            ));
	            ban.setItemMeta(meta);
	            inv.addItem(ban);
	        }
	    }

	    admin.openInventory(inv);
	}

	

	public void openReplayMenu(Player admin) {

        Inventory inv = Bukkit.createInventory(null, 54, "Replays da Sala");

        if (plugin.replayManager == null || plugin.replayManager.getReplayVictimNames().isEmpty()) {
            ItemStack empty = new ItemStack(Material.BARRIER);
            ItemMeta meta = empty.getItemMeta();
            meta.setDisplayName(ChatColor.RED + "Nenhum replay salvo");
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "O replay aparece aqui somente depois",
                    ChatColor.GRAY + "de uma kill real durante a partida.",
                    "",
                    ChatColor.YELLOW + "Dica: kills por faca arremessada agora",
                    ChatColor.YELLOW + "tambem salvam replay."
            ));
            empty.setItemMeta(meta);
            inv.setItem(22, empty);
            admin.openInventory(inv);
            return;
        }

        for (String victimName : plugin.replayManager.getReplayVictimNames()) {
            String killerName = plugin.replayManager.getReplayKillerNameByVictim(victimName);
            if (killerName == null || killerName.trim().isEmpty()) killerName = "Assassino";

            // O icone agora mostra a SKIN DO ASSASSINO, nao da vitima.
            ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta meta = (SkullMeta) head.getItemMeta();

            meta.setOwner(killerName);
            meta.setDisplayName(ChatColor.AQUA + "Replay de " + ChatColor.YELLOW + victimName);

            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Kill replay salvo desta partida.",
                    ChatColor.GRAY + "NPC do assassino usa skin de player.",
                    "",
                    ChatColor.GRAY + "Assassino: " + ChatColor.RED + killerName,
                    ChatColor.GRAY + "Vitima: " + ChatColor.YELLOW + victimName,
                    ChatColor.DARK_GRAY + "ID: " + victimName,
                    ChatColor.GRAY + "Status: " + ChatColor.GREEN + "Disponivel",
                    ChatColor.YELLOW + "Clique para assistir pausado."
            ));

            head.setItemMeta(meta);
            inv.addItem(head);
        }

        admin.openInventory(inv);
    }
	
	@EventHandler
	public void onPlayerInteractManage(PlayerInteractEvent event) {
	    Player p = event.getPlayer();
	    if (Arenas.getArena(p) != this) return;

	    if (!event.getAction().toString().contains("RIGHT_CLICK")) return;

	    ItemStack item = p.getItemInHand();
	    if (item == null || !item.hasItemMeta()) return;

	    if (item.getItemMeta().getDisplayName().contains(ChatColor.YELLOW + "Gerenciar Sala (Clique)")) {
	        event.setCancelled(true);
	        if (!canManageRoom(p)) {
	            p.sendMessage(ChatColor.RED + "Apenas o líder da sala ou staff pode gerenciar!");
	            return;
	        }
	        openManageMenu(p);
	    }
	}
	
	public void updateWaitingScoreboards() {
	    for (Player online : new ArrayList<Player>(players)) {
	        if (online != null && online.isOnline()) {
	            plugin.scorestate.remove(online.getName());
	        }
	    }
	}

	@EventHandler
	public void onInventoryClickManage(org.bukkit.event.inventory.InventoryClickEvent e) {
	    if (!(e.getWhoClicked() instanceof Player)) return;

	    Player p = (Player) e.getWhoClicked();
	    if (Arenas.getArena(p) != this) return;
	    if (!e.getView().getTitle().equals("Gerenciar Sala")) return;

	    e.setCancelled(true);

	    if (!canManageRoom(p)) {
	        p.sendMessage(ChatColor.RED + "Apenas o líder da sala ou staff pode gerenciar!");
	        return;
	    }

	    ItemStack item = e.getCurrentItem();
	    if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return;

	    String nameItem = item.getItemMeta().getDisplayName();

	    if (nameItem.contains("+1")) {
	        maxPlayers++;
	        updateWaitingScoreboards();
	        p.sendMessage(ChatColor.GREEN + "Limite da sala alterado para " + ChatColor.WHITE + maxPlayers + ChatColor.GREEN + " jogadores.");
	    }
	    if (nameItem.contains("-1") && maxPlayers > 2) {
	        maxPlayers--;
	        updateWaitingScoreboards();
	        p.sendMessage(ChatColor.GREEN + "Limite da sala alterado para " + ChatColor.WHITE + maxPlayers + ChatColor.GREEN + " jogadores.");
	    }

	    if (nameItem.contains("Pausar Temporizador") || nameItem.contains("Retomar Temporizador")) {
	        countdownPaused = !countdownPaused;
	        updateWaitingScoreboards();
	        p.sendMessage(countdownPaused ? ChatColor.RED + "Temporizador da sala pausado." : ChatColor.GREEN + "Temporizador da sala retomado.");
	        openManageMenu(p);
	        return;
	    }

	    if (nameItem.contains("Iniciar")) {
	        if (players.size() < 2) {
	            p.sendMessage(ChatColor.RED + "Precisa de pelo menos 2 jogadores!");
	            return;
	        }
	        start();
	        return;
	    }

	    if (nameItem.contains("Fechar")) {
	        Room room = plugin.roomManager != null ? plugin.roomManager.getRoom(p) : null;

	        if (room == null || room.getArena() != this || !persistentRoom || room.isMainRoom()) {
	            p.sendMessage(ChatColor.RED + "Você não pode fechar a sala principal/fixa. Só salas criadas por player podem ser fechadas.");
	            return;
	        }

	        plugin.roomManager.deleteRoom(room);
	        p.sendMessage(ChatColor.GREEN + "Sala fechada com sucesso!");
	        p.closeInventory();
	        return;
	    }

	    // Salva limite apenas para arenas fixas.
	    // Salas temporárias ROOM-* não devem sujar a config com ROOM-123, ROOM-456 etc.
	    if (this.name != null && !this.name.toUpperCase().startsWith("ROOM-")) {
	        plugin.getConfig().set("SpawnSize." + this.name, maxPlayers);
	        plugin.saveConfig();
	    }

	    if (nameItem.contains("Definir Murder")) {
	        selectedRole = PlayerType.Murderer;
	        p.sendMessage("§cSelecione um jogador...");
	        openPlayerSelectMenu(p);
	        return;
	    }

	    if (nameItem.contains("Definir Detective")) {
	        selectedRole = PlayerType.Detective;
	        p.sendMessage("§bSelecione um jogador...");
	        openPlayerSelectMenu(p);
	        return;
	    }

	    if (nameItem.contains("Modo de Jogo")) {
	        openGameModeMenu(p);
	        return;
	    }

	    if (nameItem.contains("Renomear Sala")) {
	        Room room = getRoomForThisArena();
	        if (room == null || room.isMainRoom()) {
	            p.sendMessage(ChatColor.RED + "A sala principal/fixa não pode ser renomeada.");
	            return;
	        }
	        if (plugin.roomManager != null) {
	            plugin.roomManager.startRename(p, room);
	        }
	        p.closeInventory();
	        p.sendMessage(ChatColor.AQUA + "Digite o novo nome da sala no chat.");
	        p.sendMessage(ChatColor.GRAY + "Você pode usar cores com " + ChatColor.YELLOW + "&a, &b, &l" + ChatColor.GRAY + " etc.");
	        p.sendMessage(ChatColor.GRAY + "Use " + ChatColor.YELLOW + "cancelar" + ChatColor.GRAY + " para cancelar ou " + ChatColor.YELLOW + "resetar" + ChatColor.GRAY + " para voltar ao padrão.");
	        return;
	    }

	    if (nameItem.contains("Senha da Sala")) {
	        Room room = getRoomForThisArena();
	        if (room == null || room.isMainRoom()) {
	            p.sendMessage(ChatColor.RED + "A sala principal/fixa não usa senha.");
	            return;
	        }
	        RoomPasswordGUI.openSetPassword(plugin, p, room);
	        return;
	    }

	    if (nameItem.contains("Área Staff")) {
	        if (!isStaff(p)) {
	            p.sendMessage(ChatColor.RED + "Apenas staff pode usar esta área.");
	            return;
	        }
	        openRoomStaffMenu(p);
	        return;
	    }

	    if (nameItem.contains("Modificadores")) {
	        openRoomModifiersMenu(p);
	        return;
	    }

	    if (nameItem.contains("Gerenciar Players")) {
	        if (!canFullyManageRoom(p)) {
	            p.sendMessage(ChatColor.RED + "Apenas o dono da sala ou staff pode gerenciar jogadores.");
	            return;
	        }
	        openRemovePlayerMenu(p);
	        return;
	    }

	    
    if (nameItem.contains("Replays")) {
        if (!isStaff(p)) {
            p.sendMessage(ChatColor.RED + "Apenas staff pode ver replays de outros jogadores.");
            return;
        }
        openReplayMenu(p);
        return;
    }

    openManageMenu(p);
	}

	@EventHandler
	public void onRoomModifierClick(org.bukkit.event.inventory.InventoryClickEvent e) {
	    if (!(e.getWhoClicked() instanceof Player)) return;
	    if (!e.getView().getTitle().equals("Modificadores da Sala")) return;

	    e.setCancelled(true);
	    Player p = (Player) e.getWhoClicked();
	    if (Arenas.getArena(p) != this) return;

	    if (!canManageRoom(p)) {
	        p.sendMessage(ChatColor.RED + "Apenas o líder da sala ou staff pode alterar modificadores!");
	        return;
	    }

	    ItemStack item = e.getCurrentItem();
	    if (item == null || item.getType() == Material.AIR || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;

	    String clicked = ChatColor.stripColor(item.getItemMeta().getDisplayName()).toLowerCase();
	    Room room = getRoomForThisArena();
	    if (room == null) {
	        p.sendMessage(ChatColor.RED + "Esta sala não suporta modificadores.");
	        return;
	    }

	    if (clicked.contains("cursed gold")) {
        if (RoomFeatureLockManager.isModifierDisabled(plugin, RoomModifier.CURSED_GOLD)) {
            p.sendMessage(ChatColor.RED + "Este modificador está desativado pela staff no momento.");
            return;
        }
        boolean enabled = room.toggleModifier(RoomModifier.CURSED_GOLD);
        p.sendMessage((enabled ? ChatColor.GREEN + "Cursed Gold ativado!" : ChatColor.RED + "Cursed Gold desativado!"));
        openRoomModifiersMenu(p);
        return;
    }

    if (clicked.contains("sabotage") || clicked.contains("among us") || clicked.contains("amongus")) {
        if (RoomFeatureLockManager.isModifierDisabled(plugin, RoomModifier.SABOTAGE)) {
            p.sendMessage(ChatColor.RED + "Este modificador está desativado pela staff no momento.");
            return;
        }
        boolean enabled = room.toggleModifier(RoomModifier.SABOTAGE);
        p.sendMessage((enabled ? ChatColor.GREEN + "AMONG US ativado!" : ChatColor.RED + "AMONG US desativado!"));
        refreshRoomScoreboards();
        openRoomModifiersMenu(p);
        return;
    }
	}


    @EventHandler
    public void onRoomStaffMenuClick(org.bukkit.event.inventory.InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        String title = e.getView().getTitle();
        if (!title.equals("§8Área Staff §7- §cControle")
                && !title.equals("§8Staff §7- §aModos de Jogo")
                && !title.equals("§8Staff §7- §6Modificadores")
                && !title.equals("§8Staff §7- §bKits")
                && !title.equals("§8Staff §7- §dMapas")) return;

        e.setCancelled(true);
        Player p = (Player) e.getWhoClicked();
        if (!isStaff(p)) {
            p.sendMessage(ChatColor.RED + "Apenas staff pode usar esta área.");
            p.closeInventory();
            return;
        }

        ItemStack item = e.getCurrentItem();
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;

        long now = System.currentTimeMillis();
        Long last = staffMenuClickCooldown.get(p.getUniqueId());
        if (last != null && now - last.longValue() < 450L) return;
        staffMenuClickCooldown.put(p.getUniqueId(), now);

        String clicked = ChatColor.stripColor(item.getItemMeta().getDisplayName()).toLowerCase();

        if (clicked.contains("voltar")) {
            if (title.equals("§8Área Staff §7- §cControle")) openManageMenu(p);
            else openRoomStaffMenu(p);
            return;
        }

        if (title.equals("§8Área Staff §7- §cControle")) {
            if (clicked.contains("modos de jogo")) { openStaffModesMenu(p); return; }
            if (clicked.contains("modificadores")) { openStaffModifiersMenu(p); return; }
            if (clicked.contains("kits")) { openStaffKitsMenu(p); return; }
            if (clicked.contains("mapas")) { openStaffMapsMenu(p); return; }
            return;
        }

        if (title.equals("§8Staff §7- §aModos de Jogo")) {
            for (GameModeType mode : GameModeType.values()) {
                if (clicked.equalsIgnoreCase("modo: " + mode.getDisplayName().toLowerCase())) {
                    boolean disabled = RoomFeatureLockManager.toggleMode(plugin, mode);
                    p.sendMessage(disabled ? ChatColor.RED + "Modo " + mode.getDisplayName() + " desativado." : ChatColor.GREEN + "Modo " + mode.getDisplayName() + " ativado.");
                    Bukkit.getScheduler().runTaskLater(plugin, new Runnable() { public void run() { if (p.isOnline()) openStaffModesMenu(p); } }, 2L);
                    return;
                }
            }
            return;
        }

        if (title.equals("§8Staff §7- §6Modificadores")) {
            if (clicked.equalsIgnoreCase("modificador: cursed gold")) {
                boolean disabled = RoomFeatureLockManager.toggleModifier(plugin, RoomModifier.CURSED_GOLD);
                p.sendMessage(disabled ? ChatColor.RED + "Cursed Gold desativado." : ChatColor.GREEN + "Cursed Gold ativado.");
                Bukkit.getScheduler().runTaskLater(plugin, new Runnable() { public void run() { if (p.isOnline()) openStaffModifiersMenu(p); } }, 2L);
                return;
            }
            if (clicked.equalsIgnoreCase("modificador: sabotage") || clicked.equalsIgnoreCase("modificador: among us") || clicked.equalsIgnoreCase("modificador: amongus")) {
                boolean disabled = RoomFeatureLockManager.toggleModifier(plugin, RoomModifier.SABOTAGE);
                p.sendMessage(disabled ? ChatColor.RED + "AMONG US desativado." : ChatColor.GREEN + "AMONG US ativado.");
                Bukkit.getScheduler().runTaskLater(plugin, new Runnable() { public void run() { if (p.isOnline()) openStaffModifiersMenu(p); } }, 2L);
                return;
            }
            return;
        }

        if (title.equals("§8Staff §7- §bKits")) {
            if (clicked.startsWith("kit: ")) {
                String kitName = clicked.substring(5).trim();
                for (Kit kit : plugin.kitManager.getAllKits()) {
                    if (kit.getName().equalsIgnoreCase(kitName)) {
                        boolean disabled = RoomFeatureLockManager.toggleKit(plugin, kit);
                        p.sendMessage(disabled ? ChatColor.RED + "Kit " + kit.getName() + " desativado." : ChatColor.GREEN + "Kit " + kit.getName() + " ativado.");
                        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() { public void run() { if (p.isOnline()) openStaffKitsMenu(p); } }, 2L);
                        return;
                    }
                }
            }
            return;
        }

        if (title.equals("§8Staff §7- §dMapas")) {
            if (clicked.startsWith("mapa: ")) {
                String mapName = ChatColor.stripColor(item.getItemMeta().getDisplayName()).substring(6).trim();
                boolean disabled = RoomFeatureLockManager.toggleMap(plugin, mapName);
                p.sendMessage(disabled ? ChatColor.RED + "Mapa " + mapName + " desativado." : ChatColor.GREEN + "Mapa " + mapName + " ativado.");
                Bukkit.getScheduler().runTaskLater(plugin, new Runnable() { public void run() { if (p.isOnline()) openStaffMapsMenu(p); } }, 2L);
                return;
            }
        }
    }

	@EventHandler
	public void onReplayMenuClick(org.bukkit.event.inventory.InventoryClickEvent e) {
	    if (!(e.getWhoClicked() instanceof Player)) return;
	    if (!e.getView().getTitle().equals("Replays da Sala")) return;

	    e.setCancelled(true);

	    Player admin = (Player) e.getWhoClicked();
	    if (Arenas.getArena(admin) != this) return;

	    if (!isStaff(admin)) {
	        admin.sendMessage(ChatColor.RED + "Apenas staff pode assistir replays de outros jogadores.");
	        return;
	    }

	    ItemStack item = e.getCurrentItem();
	    if (item == null || item.getType() == Material.AIR || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;
	    if (item.getType() == Material.BARRIER) return;

	    String name = ChatColor.stripColor(item.getItemMeta().getDisplayName()).replace("Replay de ", "");

	    if (plugin.replayManager == null) {
	        admin.sendMessage(ChatColor.RED + "Sistema de replay indisponivel.");
	        return;
	    }

	    if (plugin.replayManager == null) {
	        admin.sendMessage(ChatColor.RED + "Sistema de replay desativado para teste de desempenho.");
	        admin.closeInventory();
	        return;
	    }
	    if (!plugin.replayManager.hasReplayByName(name)) {
	        admin.sendMessage(ChatColor.RED + "Replay nao encontrado ou expirado.");
	        openReplayMenu(admin);
	        return;
	    }

	    plugin.replayManager.playReplayByName(admin, name);
	}

	@EventHandler
	public void onMapVoteClick(org.bukkit.event.inventory.InventoryClickEvent e) {
	    if (!(e.getWhoClicked() instanceof Player)) return;
	    if (!e.getView().getTitle().equals("Votar Mapa")) return;

	    e.setCancelled(true);
	    Player p = (Player) e.getWhoClicked();
	    if (Arenas.getArena(p) != this) return;

	    ItemStack item = e.getCurrentItem();
	    if (item == null || item.getType() == Material.AIR || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;

	    String mapName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
	    Room room = getRoomForThisArena();
	    if (room == null) {
	        p.sendMessage(ChatColor.RED + "Esta sala não possui votação de mapa.");
	        return;
	    }

	    if (!isMapAllowedForCurrentMode(mapName)) {
	        p.sendMessage(ChatColor.RED + "Esse mapa não está liberado para o modo " + getRoomModeNamePlain() + ChatColor.RED + ".");
	        openMapVoteMenu(p);
	        return;
	    }

	    room.voteMap(p, mapName);
	    p.sendMessage(ChatColor.GREEN + "Você votou no mapa " + ChatColor.YELLOW + mapName + ChatColor.GREEN + "!");
	    openMapVoteMenu(p);
	}

	@EventHandler
	public void onModeSelectClick(org.bukkit.event.inventory.InventoryClickEvent e) {
	    if (!(e.getWhoClicked() instanceof Player)) return;
	    if (!e.getView().getTitle().equals("Selecionar Modo")) return;

	    e.setCancelled(true);
	    Player p = (Player) e.getWhoClicked();
	    if (Arenas.getArena(p) != this) return;

	    if (!canManageRoom(p)) {
	        p.sendMessage(ChatColor.RED + "Apenas o líder da sala ou staff pode alterar o modo!");
	        return;
	    }

	    ItemStack item = e.getCurrentItem();
	    if (item == null || item.getType() == Material.AIR || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;

	    String clicked = ChatColor.stripColor(item.getItemMeta().getDisplayName()).toLowerCase();
	    GameModeType newMode = null;

	    if (clicked.contains("normal")) newMode = GameModeType.NORMAL;
	    else if (clicked.contains("ranked")) newMode = GameModeType.RANKED;
        else if (clicked.contains("sabotage") || clicked.contains("sabotagem") || clicked.contains("among us") || clicked.contains("amongus")) newMode = GameModeType.SABOTAGE;
	    else if (clicked.contains("tnttag") || clicked.contains("tnt")) newMode = GameModeType.TNT_TAG;
	    else if (clicked.contains("esconde") || clicked.contains("hide")) newMode = GameModeType.HIDE_AND_SEEK;
	    else if (clicked.contains("todos")) newMode = GameModeType.ALL_MURDER;

	    if (newMode == null) return;

	    if (RoomFeatureLockManager.isModeDisabled(plugin, newMode)) {
	        p.sendMessage(ChatColor.RED + "Este modo está desativado pela staff no momento.");
	        return;
	    }

        Room selectedModeRoom = getRoomForThisArena();
        if (selectedModeRoom != null && selectedModeRoom.isModeLocked()) {
            p.sendMessage(ChatColor.RED + "Esta sala pública tem modo fixo: " + ChatColor.YELLOW + selectedModeRoom.getGameMode().getDisplayName());
            return;
        }
        if (newMode == GameModeType.RANKED && selectedModeRoom != null && !selectedModeRoom.isMainRoom()) {
            p.sendMessage(ChatColor.RED + "Ranked agora e exclusivo das salas publicas do servidor.");
            return;
        }

	    setGameMode(newMode);
	    refreshRoomLobbyItems();
	    refreshRoomScoreboards();
        if (newMode == GameModeType.RANKED) {
            this.maxPlayers = plugin.getConfig().getInt("ranked.public-room-max-players", 20);
            refreshRoomLobbyItems();
        }
	    p.sendMessage("§aModo alterado para §e" + newMode.getDisplayName() + " §aapenas nesta sala!");

	    if (getRoomForThisArena() == null) {
	        plugin.getConfig().set("ArenaMode." + templateName, newMode.toString());
	        plugin.saveConfig();
	    }

	    openManageMenu(p);
	}

	@EventHandler
	public void onRemovePlayerClick(org.bukkit.event.inventory.InventoryClickEvent e) {
	    if (!(e.getWhoClicked() instanceof Player)) return;
	    if (!e.getView().getTitle().equals("Gerenciar Players")) return;

	    e.setCancelled(true);

	    Player admin = (Player) e.getWhoClicked();
	    Arena arena = Arenas.getArena(admin);
	    if (arena == null || arena != this) return;

	    if (!canFullyManageRoom(admin)) {
	        admin.sendMessage(ChatColor.RED + "Apenas o dono da sala ou staff pode gerenciar jogadores!");
	        return;
	    }

	    ItemStack item = e.getCurrentItem();
	    if (item == null || item.getType() == Material.AIR || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;

	    Room room = getRoomForThisArena();
	    String rawName = ChatColor.stripColor(item.getItemMeta().getDisplayName());

	    if (rawName.startsWith("Banido ")) {
	        if (room == null) return;
	        String targetName = rawName.replace("Banido ", "").trim();
	        UUID removeId = null;
	        for (Map.Entry<UUID, String> entry : room.getBannedNames().entrySet()) {
	            if (entry.getValue() != null && entry.getValue().equalsIgnoreCase(targetName)) {
	                removeId = entry.getKey();
	                break;
	            }
	        }
	        if (removeId != null) {
	            room.unbanPlayer(removeId);
	            admin.sendMessage(ChatColor.GREEN + "Jogador desbanido da sala: " + ChatColor.YELLOW + targetName);
	        }
	        openRemovePlayerMenu(admin);
	        return;
	    }

	    if (!rawName.startsWith("Jogador ")) return;
	    String name = rawName.replace("Jogador ", "").trim();
	    Player target = Bukkit.getPlayer(name);
	    if (target == null || !players.contains(target)) {
	        admin.sendMessage(ChatColor.RED + "Jogador não encontrado nesta sala.");
	        openRemovePlayerMenu(admin);
	        return;
	    }

	    if (target.equals(admin)) {
	        admin.sendMessage(ChatColor.RED + "Você não pode usar essa ação em você mesmo aqui.");
	        return;
	    }

	    if (room != null && room.getOwner() != null && room.getOwner().equals(target)) {
	        admin.sendMessage(ChatColor.RED + "Você não pode banir/remover o dono da sala.");
	        return;
	    }

	    ClickType click = e.getClick();
	    if (click == ClickType.RIGHT || click == ClickType.SHIFT_RIGHT) {
	        if (room == null) return;
	        if (room.isBanned(target)) {
	            room.unbanPlayer(target);
	            admin.sendMessage(ChatColor.GREEN + target.getName() + " foi desbanido da sala.");
	        } else {
	            room.banPlayer(target);
	            if (plugin.roomManager != null) {
	                plugin.roomManager.removePlayer(target);
	            } else {
	                removePlayer(target, "leave");
	            }
	            target.sendMessage(ChatColor.RED + "Você foi banido desta sala por " + admin.getName() + ".");
	            admin.sendMessage(ChatColor.GREEN + target.getName() + " foi banido da sala.");
	        }
	        openRemovePlayerMenu(admin);
	        return;
	    }

	    if (click == ClickType.SHIFT_LEFT) {
	        if (room == null) return;
	        boolean enabled = room.toggleModerator(target);
	        target.sendMessage(enabled ? ChatColor.GREEN + "Você agora é moderador desta sala." : ChatColor.RED + "Você não é mais moderador desta sala.");
	        admin.sendMessage((enabled ? ChatColor.GREEN : ChatColor.RED) + target.getName() + (enabled ? " virou moderador da sala." : " foi removido dos moderadores da sala."));
	        openRemovePlayerMenu(admin);
	        return;
	    }

	    if (plugin.roomManager != null && room != null) {
	        plugin.roomManager.removePlayer(target);
	    } else {
	        removePlayer(target, "leave");
	    }

	    target.sendMessage(ChatColor.RED + "Você foi removido da sala por " + admin.getName() + ".");
	    admin.sendMessage(ChatColor.GREEN + "Jogador removido: " + target.getName());
	    openRemovePlayerMenu(admin);
	}


	@EventHandler
	public void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent e) {

	    if (!(e.getWhoClicked() instanceof Player)) return;

	    Player admin = (Player) e.getWhoClicked();

	    if (!e.getView().getTitle().equals("Selecionar Jogador")) return;

	    e.setCancelled(true);
	    

	    ItemStack item = e.getCurrentItem();
	    if (item == null || item.getType() != Material.SKULL_ITEM) return;

	    String name = item.getItemMeta().getDisplayName().replace("§e", "");

	    Player target = Bukkit.getPlayer(name);

	    if (target == null) {
	        admin.sendMessage("§cJogador offline!");
	        return;
	    }

	    Arena arena = Arenas.getArena(admin);

	    if (arena == null || arena.selectedRole == null) return;

	    // Não aplica item/role no lobby de espera.
	    // Salva apenas a role forçada para aplicar no SetUp(), quando a partida iniciar.
	    if (arena.getGameMode() == GameModeType.TNT_TAG) {
	        if (arena.selectedRole == PlayerType.Detective) {
	            admin.sendMessage("§cDetective não pode ser definido no modo TNTTag.");
	            arena.selectedRole = null;
	            admin.closeInventory();
	            return;
	        }
	        arena.forcedRoles.put(target.getUniqueId(), PlayerType.Murderer);
	        target.sendMessage("§6Você foi definido como TAGGER inicial!");
	        admin.sendMessage("§a" + target.getName() + " será o TAGGER inicial quando a partida começar.");
	    } else if (arena.getGameMode() == GameModeType.ALL_MURDER) {
	        if (arena.selectedRole == PlayerType.Detective) {
	            admin.sendMessage("§cDetective não pode ser definido no modo Todos Assassinos.");
	            arena.selectedRole = null;
	            admin.closeInventory();
	            return;
	        }
	        admin.sendMessage("§cNo modo Todos Assassinos todos os jogadores já serão Murderers.");
	    } else {
	        arena.forcedRoles.put(target.getUniqueId(), arena.selectedRole);
	        if (arena.selectedRole == PlayerType.Murderer) {
	            target.sendMessage("§cVocê foi selecionado para ser MURDER quando a partida começar!");
	        } else if (arena.selectedRole == PlayerType.Detective) {
	            target.sendMessage("§bVocê foi selecionado para ser DETECTIVE quando a partida começar!");
	        }
	        admin.sendMessage("§aRole salva para " + target.getName() + " e será aplicada no início da partida.");
	}

	    arena.selectedRole = null;
	    admin.closeInventory();
	    
	}
	
	@EventHandler
	public void onPlayerClick(PlayerInteractEntityEvent e) {

	    Player admin = e.getPlayer();
	    Entity clicked = e.getRightClicked();

	    if (!(clicked instanceof Player)) return;

	    Player target = (Player) clicked;

	    Arena arena = Arenas.getArena(admin);

	    if (arena == null) return;

	    if (arena.selectedRole == null) return;

	    if (!arena.players.contains(target)) return;

	    // Não aplica item/role no lobby de espera. Só agenda para o início da partida.
	    if (arena.getGameMode() == GameModeType.TNT_TAG) {
	        if (arena.selectedRole == PlayerType.Detective) {
	            admin.sendMessage("§cDetective não pode ser definido no modo TNTTag.");
	            arena.selectedRole = null;
	            return;
	        }
	        arena.forcedRoles.put(target.getUniqueId(), PlayerType.Murderer);
	        target.sendMessage("§6Você foi definido como TAGGER inicial!");
	        admin.sendMessage("§a" + target.getName() + " será o TAGGER inicial quando a partida começar.");
	    } else if (arena.getGameMode() == GameModeType.ALL_MURDER) {
	        if (arena.selectedRole == PlayerType.Detective) {
	            admin.sendMessage("§cDetective não pode ser definido no modo Todos Assassinos.");
	            arena.selectedRole = null;
	            return;
	        }
	        admin.sendMessage("§cNo modo Todos Assassinos todos os jogadores já serão Murderers.");
	    } else {
	        arena.forcedRoles.put(target.getUniqueId(), arena.selectedRole);
	        if (arena.selectedRole == PlayerType.Murderer) {
	            target.sendMessage("§cVocê foi selecionado para ser MURDER quando a partida começar!");
	        } else if (arena.selectedRole == PlayerType.Detective) {
	            target.sendMessage("§bVocê foi selecionado para ser DETECTIVE quando a partida começar!");
	        }
	        admin.sendMessage("§aRole salva para " + target.getName() + " e será aplicada no início da partida.");
	}

	    arena.selectedRole = null;
	}
	
    @EventHandler
    public void onPlayerInteract2(PlayerInteractEvent event) {
        // Teleportador do assassino: usa spawn aleatorio da arena, nao apenas o spawn 0.
        Player player = event.getPlayer();
        if (!event.getAction().toString().contains("RIGHT_CLICK")) return;
        if (player.getItemInHand() == null || player.getItemInHand().getType() != Material.ENDER_PEARL) return;
        if (getState() != GameState.INGAME || getType(player) != PlayerType.Murderer) return;
        if (!players.contains(player)) return;

        event.setCancelled(true);

        int totalSpawns = 0;
        try {
            totalSpawns = plugin.SpawnSize2(this);
        } catch (Exception ignored) {}

        if (totalSpawns <= 0) {
            try {
                totalSpawns = plugin.SpawnSize(this);
            } catch (Exception ignored) {}
        }

        if (totalSpawns <= 0) {
            player.sendMessage("§cNenhum spawn valido foi encontrado para esta arena.");
            return;
        }

        int index = new Random().nextInt(totalSpawns);
        player.teleport(plugin.getSpawn(this, index));
        player.getWorld().playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        player.sendMessage("§6§lMURDER §7> §eVoce usou seu teleportador!");
    }


	@EventHandler
	public void onTntTagItemUse(PlayerInteractEvent event) {
	    Player player = event.getPlayer();
	    if (!isTntTagMode() || !isTntHolder(player)) return;
	    if (event.getItem() == null) return;
	    if (!event.getAction().toString().contains("RIGHT_CLICK")) return;

	    ItemStack item = event.getItem();
	    if (isTntRadarItem(item)) {
	        event.setCancelled(true);
	        updateTntRadarTarget(player, true);
	        return;
	    }
	    if (isTntSpeedItem(item)) {
	        event.setCancelled(true);
	        useTntSpeed(player);
	    }
	}

	public void addkill(Player p, Integer a) {
		if (!kills.containsKey(p.getName())) {
			kills.put(p.getName(), a);

			return;
		}
		kills.put(p.getName(), kills.get(p.getName()) + a);

	}

	public Integer getkill(Player p) {
		if (!kills.containsKey(p.getName())) {

			return 0;
		}
		return kills.get(p.getName());
	}


	@SuppressWarnings("deprecation")
	public void addPlayer(Player p) {

		
		
		if (getState() != GameState.LOBBY && getState() != GameState.STARTING) {
			//p.sendMessage(Utils.FormatText(p, plugin.messages.getConfig().getString("join-error")));
			//return;
			String msg = plugin.messages.getConfig().getString("join-error");
			if (msg == null) return;

			Utils.FormatText(p, msg);
		}
		if (players.contains(p) || Arenas.isInArena(p) || specs.contains(p)) {
			//p.sendMessage(Utils.FormatText(p, plugin.messages.getConfig().getString("already-in-arena")));
			//return;
			String msg = plugin.messages.getConfig().getString("already-in-arena");
			if (msg == null) return;

			Utils.FormatText(p, msg);
		}

		if (players.size() >= maxPlayers) {
			//p.sendMessage(Utils.FormatText(p, plugin.messages.getConfig().getString("arena-full")));
			//return;
			String msg = plugin.messages.getConfig().getString("arena-full");
			if (msg == null) return;

			Utils.FormatText(p, msg);
			

	}
		if (players.size() < maxPlayers) {

			players.add(p);
			plugin.playerArena.put(p.getUniqueId(), this);
			plugin.setPlayerState(p, PlayerState.ROOM_LOBBY);
			
		    Bukkit.getScheduler().runTaskLater(plugin, () -> refreshVisibility(), 2L);
		    
			if (players.size() == 1) {
			    owner = p;
			    p.sendMessage(ChatColor.GOLD + "Você é o dono da sala!");
			}
			Arenas.addArena(p, this);
			plugin.saveInventory(p);
			clearFullInventory(p);
			

			org.bukkit.Location waitLocation = plugin.getWait(this);
			if (waitLocation == null) {
				p.sendMessage("cEssa sala/mapa est sem lobby de espera configurado. Avise um staff.");
				players.remove(p);
				plugin.playerArena.remove(p.getUniqueId());
				plugin.setPlayerState(p, PlayerState.MAIN_LOBBY);
				return;
			}
			p.teleport(waitLocation);
			Bukkit.getScheduler().runTaskLater(plugin, () -> {
			    refreshVisibility();
			    for (Arena other : Arenas.getArenas()) {
			        if (other != null && other != this) other.refreshVisibility();
			    }
			}, 5L);
			
	        giveRoomLobbyItems(p);
	        refreshRoomLobbyItems();
			
		}
	
																			
			for (Player players : getPlayers()) {
				players.sendMessage(Utils.FormatText(players,
						plugin.messages.getConfig().getString("player-join-arena-message")
								.replaceAll("%max%", String.valueOf(maxPlayers))
								.replaceAll("%min%", String.valueOf(this.players.size()))
								.replaceAll("%player%", p.getName())));
			
	}
			if (!this.start) {

				if (players.size() >= getMinPlayersToStartGame()) {
					countdown = plugin.settings.getConfig().getInt("countdown");
					for (Player players : getPlayers()) {
						players.sendMessage(Utils.FormatText(players,
								plugin.messages.getConfig().getString("game-soon-start-message")));
					}
					state = GameState.STARTING;
					new BukkitRunnable() {

						@Override
						public void run() {
							if (state == GameState.INGAME) {
								this.cancel();
								return;
							}
							if (players.size() < getMinPlayersToStartGame()) {
								start = false;
								countdownPaused = false;

								countdown = plugin.settings.getConfig().getInt("countdown");
								for (Player p : getPlayers()) {
									//p.sendMessage(Utils.FormatText(p, plugin.messages.getConfig().getString("cancel")));
									String msg = plugin.messages.getConfig().getString("cancel");
									if (msg == null) return;

									Utils.FormatText(p, msg);
								}
								state = GameState.LOBBY;
								this.cancel();
								return;
							}

							if (countdownPaused) {
								return;
							}

							countdown -= 1;
							if (countdown <= 5 && countdown > 0) {
								for (Player p : getPlayers()) {

									TitleAPI.sendTitle(p, 0, 60, 0,
											Utils.FormatText(p, plugin.messages.getConfig().getString("title-countdown")

													.replaceAll("%time%", String.valueOf(countdown))));
									TitleAPI.sendSubtitle(p, 0, 60, 0,
											Utils.FormatText(p,
													plugin.messages.getConfig().getString("subtitle-countdown")

															.replaceAll("%time%", String.valueOf(countdown))));

									if (plugin.settings.getConfig().getBoolean("enable-sounds")) {
										p.playSound(p.getLocation(),
												Sound.valueOf(
														plugin.settings.getConfig().getString("COUNT_DOWN_SOUND")),
												1, 1);
									}
									p.sendMessage(Utils.FormatText(p, plugin.messages.getConfig().getString("countdown")
											.replaceAll("%time%", String.valueOf(countdown))));
								}
							}
							if (countdown <= 0) {
								start();
								this.cancel();
								return;
							}

						}
					}.runTaskTimer(plugin, 20, 20);
					start = true;
				}

			}

		}
	


	public void addscore(Player p, Integer a, String reason) {
		if (!score.containsKey(p.getName())) {
			score.put(p.getName(), a);
			p.sendMessage(Utils.FormatText(p, plugin.messages.getConfig().getString("receive-score-message")
					.replaceAll("%score%", "" + a).replaceAll("%reason%", reason)));
			if (plugin.getPlayerData(p) != null) {
				plugin.getPlayerData(p).addscore(a);
			}
			return;
		}
		score.put(p.getName(), score.get(p.getName()) + a);
		p.sendMessage(Utils.FormatText(p, plugin.messages.getConfig().getString("receive-score-message")
				.replaceAll("%score%", "" + a).replaceAll("%reason%", reason)));
		if (plugin.getPlayerData(p) != null) {
			plugin.getPlayerData(p).addscore(a);
		}

	}

	public Integer getscore(Player p) {
		if (!score.containsKey(p.getName())) {

			return 0;
		}
		return score.get(p.getName());
	}

  //public void clearplayer(Player p) {

	//if (murder.contains(p)) {
		//murder.remove(p);
	//}
	//if (innocents.contains(p)) {
		//innocents.remove(p);
	//}
	//if (detective.contains(p)) {
		//detective.remove(p);
	//}
	//if (heros.contains(p)) {
		//heros.remove(p);
	//}

	public void clearFullInventory(Player p) {
	    p.getInventory().clear();
	    p.getInventory().setArmorContents(null);
	    p.getInventory().setHeldItemSlot(0);
	    p.updateInventory();
	}

	public Arena getArena() {
		return this;
	}

	public ArmorStand[] getArmor() {
		return armor.toArray(new ArmorStand[armor.size()]);
	}

	//public String getBowState() {
		//if (IsBowDropped()) {

			//return Utils.FormatText2(plugin.messages.getConfig().getString("bow-dropped"));
		//}
		//return Utils.FormatText2(plugin.messages.getConfig().getString("bow-not-dropped"));
	//}

	public Player[] getDetectives() {
		return detective.toArray(new Player[detective.size()]);
	}

	public Entity[] getGolds() {
		return golds.toArray(new Entity[golds.size()]);
	}

	public Player[] getInnocents() {
		return innocents.toArray(new Player[innocents.size()]);
	}

	public FlyingItems[] getItems() {
		return items.toArray(new FlyingItems[items.size()]);
	}

	public int getMinPlayersToStartGame() {
		if (plugin.arenas.getConfig().contains("MinPlayers." + getName())) {
			return plugin.arenas.getConfig().getInt("MinPlayers." + getName());
		}
		return 3;
	}

	public Player[] getMurderers() {
		return murder.toArray(new Player[murder.size()]);
	}

	public String getName() {
		return this.name;
	}
	
	public String getTemplateName() {
	    return this.templateName == null ? this.name : this.templateName;
	}

	public String getRoomId() {
	    return this.roomId == null ? this.name : this.roomId;
	}

	public ArrayList<Player> getPlayers() {

		ArrayList<Player> list = new ArrayList<>();

		for (Player b : this.players) {
			list.add(b);
		}

		for (Player b : this.specs) {
			list.add(b);
		}

		return list;

	}

	public Player[] getPlayers2() {
		return players.toArray(new Player[players.size()]);
	}

	public int getRandom(int lower, int upper) {
		Random random = new Random();
		return random.nextInt((upper - lower) + 1) + lower;
	}

	public String getRole(Player p) {
		if (getType(p) == PlayerType.Murderer) {
			return plugin.messages.getConfig().getString("murder-role");
		}
		if (getType(p) == PlayerType.Detective) {
			return plugin.messages.getConfig().getString("detective-role");
		}
		if (getType(p) == PlayerType.Innocents) {
			return plugin.messages.getConfig().getString("innocent-role");
		}
		if (getType(p) == PlayerType.None) {
			return plugin.messages.getConfig().getString("dead-role");
		}
		return plugin.messages.getConfig().getString("dead-role");
	}

	public Player[] getSpectators() {
		return specs.toArray(new Player[specs.size()]);
	}

	public GameState getState() {
		return this.state;
	}

	public ArmorStand[] getSwords() {
		return sword.toArray(new ArmorStand[sword.size()]);
	}

	public PlayerType getType(Player p) {
		if (innocents.contains(p)) {
			return PlayerType.Innocents;
		}
		if (murder.contains(p)) {
			return PlayerType.Murderer;
		}
		if (detective.contains(p)) {
			return PlayerType.Detective;
		}
		if (specs.contains(p)) {
			return PlayerType.None;
		}
		return PlayerType.None;
	}

	public boolean IsBowDropped() {
		if (isdead) {
			return true;
		}
		return false;
	}

	public void clearplayer(Player p) {

	    murder.remove(p);
	    detective.remove(p);
	    innocents.remove(p);
	    heros.remove(p);
	}

	public void loadamount() {
		if (plugin.arenas.getConfig().contains("MurderAmount." + getName())) {
			this.murdereramount = plugin.arenas.getConfig().getInt("MurderAmount." + getName());
		} else {
			murdereramount = 1;
		}

		if (plugin.arenas.getConfig().contains("DetectiveAmount." + getName())) {
			this.detectiveamount = plugin.arenas.getConfig().getInt("DetectiveAmount." + getName());
		} else {
			this.detectiveamount = 1;
		}
	}

	public void RandomDetective() {

	    List<Player> available = new ArrayList<>();

	    for (Player p : players) {

	        if (p == null) continue;
	        if (!p.isOnline()) continue;
	        if (specs.contains(p)) continue;
	        if (murder.contains(p)) continue;
	        if (detective.contains(p)) continue;
	        if (innocents.contains(p)) continue;
	        if (alreadyAssigned.contains(p.getUniqueId())) continue;

	        available.add(p);
	    }

	    if (available.isEmpty()) {
	        Bukkit.getLogger().warning("[Murder] Nenhum jogador disponível para Detective.");
	        return;
	    }

	    Player selected = available.get(random.nextInt(available.size()));

	    alreadyAssigned.add(selected.getUniqueId());
	    setDetective(selected);
	}

	public void randomMurderer() {

	    List<Player> available = new ArrayList<>();

	    for (Player p : players) {

	        if (p == null) continue;
	        if (!p.isOnline()) continue;
	        if (specs.contains(p)) continue;
	        if (murder.contains(p)) continue;
	        if (detective.contains(p)) continue;
	        if (innocents.contains(p)) continue;
	        if (alreadyAssigned.contains(p.getUniqueId())) continue;

	        available.add(p);
	    }

	    if (available.isEmpty()) {
	        Bukkit.getLogger().warning("[Murder] Nenhum jogador disponível para Murder.");
	        return;
	    }

	    Player selected = available.get(random.nextInt(available.size()));

	    alreadyAssigned.add(selected.getUniqueId());
	    setMurder(selected);
	}

	public String removeLast(String s) {
	    if (s == null || s.length() <= 2) return "";

	    return s.substring(0, s.length() - 2); // remove ", "
	}

	@SuppressWarnings("deprecation")
	public void removePlayer(Player p, String reason) {
		if (!Arenas.isInArena(p)) {
			//p.sendMessage(Utils.FormatText(p, plugin.messages.getConfig().getString("not-in-arena")));
			//return;
			String msg = plugin.messages.getConfig().getString("not-in-arena");
			if (msg == null) return;

			Utils.FormatText(p, msg);
		}
		if (!players.contains(p) && !specs.contains(p)) {
			//p.sendMessage(Utils.FormatText(p, plugin.messages.getConfig().getString("not-in-arena")));
			//return;
			String msg = plugin.messages.getConfig().getString("not-in-arena");
			if (msg == null) return;

			Utils.FormatText(p, msg);
		
		}
		

        if (reason != null && reason.equalsIgnoreCase("death") && plugin.replayManager != null) {
            if (plugin.replayManager != null) plugin.replayManager.saveDeathReplay(p, this);
        }

		if (plugin.getConfig().getBoolean("update-data-on-player-leave-arena")) {
			if (plugin.getPlayerData(p) != null) {
				PlayerData data = plugin.getPlayerData(p);
				if (!plugin.getConfig().getBoolean("mysql")) {
					plugin.api.setNonSQLData(data.p, data.getkill(), data.getdeaths(), data.getloses(), data.getwins(),
							data.getcoins(), data.getscore());
				}
				if (plugin.getConfig().getBoolean("mysql")) {
					plugin.api.setSQLData(data.p, data.getkill(), data.getdeaths(), data.getloses(), data.getwins(),
							data.getcoins(), data.getscore());
				}
			}
		}

		if (reason.equalsIgnoreCase("leave")) {

			transferRoomOwnerIfNeeded(p);

			if (getState() == GameState.LOBBY || getState() == GameState.STARTING) {
				for (Player players : getPlayers()) {
					int size = this.players.size() - 1;
					players.sendMessage(Utils.FormatText(players,
							plugin.messages.getConfig().getString("player-leave-arena-message")
									.replaceAll("%max%", String.valueOf(maxPlayers))
									.replaceAll("%min%", String.valueOf(size)).replaceAll("%player%", p.getName())));
				}
			}

			if (players.contains(p)) {
				players.remove(p);
				plugin.playerArena.remove(p.getUniqueId());
				plugin.setPlayerState(p, PlayerState.MAIN_LOBBY);
			}
			if (specs.contains(p)) {
				specs.remove(p);
			}
			if (plugin.spectatorManager != null) {
				plugin.spectatorManager.restore(p);
			}
			plugin.setPlayerState(p, PlayerState.MAIN_LOBBY);
			restoreInventory(p);

			plugin.setup(p);
			Arenas.removeArena(p);

			if (getState() == GameState.INGAME || getState() == GameState.STARTING) {
				if (players.size() <= 0 || getPlayers().size() <= 0) {
					stop("reload");

					if (plugin.settings.getConfig().getBoolean("send-stats-message-on-leave")) {
						if (plugin.getPlayerData(p) != null) {
							for (int i = 1; i < 15; i++) {
								p.sendMessage("");
							}

							List<String> list = plugin.messages.getConfig().getStringList("self-info-message");
							for (String s : list) {
								p.sendMessage(Utils.FormatText(p, s.replaceAll("%player%", p.getName())
										.replaceAll("%loses%", String.valueOf(plugin.getPlayerData(p).getloses()))
										.replaceAll("%wins%", String.valueOf(plugin.getPlayerData(p).getwins()))
										.replaceAll("%kills%", String.valueOf(plugin.getPlayerData(p).getkill()))
										.replaceAll("%score%", String.valueOf(plugin.getPlayerData(p).getscore()))
										.replaceAll("%deaths%", String.valueOf(plugin.getPlayerData(p).getdeaths()))));

							}
						}
					}

					if (!plugin.getConfig().getBoolean("send-to-server-on-leave")) {
						p.teleport(plugin.getLobby());
					}

					plugin.restoreInventory(p);
					clearplayer(p);
					if (plugin.getConfig().getBoolean("send-to-server-on-leave")) {
						ByteArrayDataOutput out = ByteStreams.newDataOutput();
						out.writeUTF("Connect");
						out.writeUTF(plugin.getConfig().getString("lobby-server"));

						p.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
					}
					return;
				}

			}

			if (!isdead) {
				if (getType(p) != PlayerType.Detective) {
					if (lists.contains(p.getName())) {
						plugin.spawnarmorstand(this, p.getLocation());
						isdead = true;
						for (Player b : getInnocents()) {
							plugin.setCompass(b);
						}

						for (Player x : getPlayers()) {
							x.sendMessage(Utils.FormatText(x, plugin.messages.getConfig().getString("bow-dropped")));
						}

					}
				}
				if (getType(p) == PlayerType.Detective) {

					plugin.spawnarmorstand(this, p.getLocation());
					isdead = true;
					for (Player b : getInnocents()) {
						plugin.setCompass(b);
					}

					for (Player x : getPlayers()) {
						x.sendMessage(Utils.FormatText(x, plugin.messages.getConfig().getString("detective-die")));
					}

				}
			}

			if (state == GameState.INGAME) {
				if (getGameMode() != GameModeType.ALL_MURDER && players.size() == murder.size()) {
					win("m");
				}
				if (getType(p) == PlayerType.Murderer && murder.size() - 1 == 0) {
					if (!wincheck) {
						stop("reload");
					}

				}
				if (players.size() == 1) {
					if (getType(players.get(0)) == PlayerType.Innocents) {
						win("p");
					}
					if (getType(players.get(0)) == PlayerType.Detective) {
						win("p");
					}
					if (getType(players.get(0)) == PlayerType.Murderer) {
						win("m");
					}
				}

			}
			if (plugin.settings.getConfig().getBoolean("send-stats-message-on-leave")) {
				if (plugin.getPlayerData(p) != null) {
					for (int i = 1; i < 15; i++) {
						p.sendMessage("");
					}

					List<String> list = plugin.messages.getConfig().getStringList("self-info-message");
					for (String s : list) {
						p.sendMessage(Utils.FormatText(p,
								s.replaceAll("%player%", p.getName())
										.replaceAll("%loses%", String.valueOf(plugin.getPlayerData(p).getloses()))
										.replaceAll("%wins%", String.valueOf(plugin.getPlayerData(p).getwins()))
										.replaceAll("%kills%", String.valueOf(plugin.getPlayerData(p).getkill()))
										.replaceAll("%score%", String.valueOf(plugin.getPlayerData(p).getscore()))
										.replaceAll("%deaths%", String.valueOf(plugin.getPlayerData(p).getdeaths()))));

					}
				}
			}

			if (!plugin.getConfig().getBoolean("send-to-server-on-leave")) {
				p.teleport(plugin.getLobby());
			}

			plugin.restoreInventory(p);
			clearplayer(p);
			if (plugin.getConfig().getBoolean("send-to-server-on-leave")) {
				ByteArrayDataOutput out = ByteStreams.newDataOutput();
				out.writeUTF("Connect");
				out.writeUTF(plugin.getConfig().getString("lobby-server"));

				p.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
			}

		}

		if (reason.equalsIgnoreCase("death")) {
			if (specs.contains(p)) {
				return;
			}
			if (!isdead) {
				if (getType(p) != PlayerType.Detective) {
					if (lists.contains(p.getName())) {
						plugin.spawnarmorstand(this, p.getLocation());
						isdead = true;
						for (Player b : getInnocents()) {
							plugin.setCompass(b);
						}

						for (Player x : getPlayers()) {
							x.sendMessage(Utils.FormatText(x, plugin.messages.getConfig().getString("bow-dropped")));
						}

					}
				}
				if (getType(p) == PlayerType.Detective) {

					plugin.spawnarmorstand(this, p.getLocation());
					isdead = true;
					for (Player b : getInnocents()) {
						plugin.setCompass(b);
					}

					for (Player x : getPlayers()) {
						x.sendMessage(Utils.FormatText(x, plugin.messages.getConfig().getString("detective-die")));
					}

				}
			}

			Kit selectedKitOnDeath = plugin.kitManager != null ? plugin.kitManager.getSelectedKit(p) : null;
			if (selectedKitOnDeath != null) {
				selectedKitOnDeath.remove(p);
			}

			plugin.setup(p);
			p.setHealth(p.getMaxHealth());

			if (players.contains(p)) {
				players.remove(p);
			}
			if (!specs.contains(p)) {
				specs.add(p);
			}

			Player killCamKiller = null;
			if (plugin.replayManager != null) {
			    killCamKiller = plugin.replayManager != null ? plugin.replayManager.getLastKiller(p, this) : null;
			}
			if (plugin.killCamManager != null && plugin.killCamManager.shouldUseKillCam(this, p, killCamKiller)) {
			    plugin.killCamManager.start(p, killCamKiller, this, plugin.messages.getConfig().getString("spectate-message"));
			} else if (plugin.spectatorManager != null) {
			    plugin.spectatorManager.makeSpectator(p, this, plugin.messages.getConfig().getString("spectate-message"));
			}


			// Todos Assassinos: não usar a regra normal players.size()==murder.size(),
			// porque nesse modo todos os vivos são murderers. A partida só acaba
			// quando restar 1 jogador vivo.
			if (getGameMode() == GameModeType.ALL_MURDER) {
				clearplayer(p);
				refreshVisibility();
				if (players.size() <= 1 && state == GameState.INGAME) {
					if (players.size() == 1) {
						win("m");
					} else {
						stop("reload");
					}
				}
				return;
			}

			if (players.size() == 0 || getPlayers().size() == 0) {

				stop("reload");
				return;
			}

			if (getType(p) == PlayerType.Murderer && murder.size() - 1 == 0) {
				clearplayer(p);
				if (players.size() > 0) {
					win("p");
				}
				if (players.size() == 0 || getPlayers().size() == 0) {
					stop("reload");
				}
				return;
			}

			if (getGameMode() != GameModeType.ALL_MURDER && players.size() == murder.size()) {

				win("m");

			}
			if (players.size() == 1 && state == GameState.INGAME) {
				if (getType(players.get(0)) == PlayerType.Innocents) {
					win("p");
				}
				if (getType(players.get(0)) == PlayerType.Detective) {
					win("p");
				}
				if (getType(players.get(0)) == PlayerType.Murderer) {
					win("m");

				}
			}

		}
		clearplayer(p);

	}

	public void reset() {

		if (tntTagTask != -1) {
			Bukkit.getScheduler().cancelTask(tntTagTask);
			tntTagTask = -1;
		}
		tntHolder = null;
	    clearAllTntHitProtections();
		tntRoundTime = 30;

		this.countdown = 0;

		this.spawns = 0;
		this.said = false;
		this.isdead = false;
		this.start = false;
		this.wincheck = false;
		this.Murderer = "Nenhum";
		this.Detective = "Nenhum";
		this.Hero = "Nenhum";
		this.score.clear();
		this.kills.clear();
		this.lists.clear();
		this.pic.clear();
		this.bowloc = null;
		this.detectiveamount = 0;
		this.murdereramount = 0;
	}

		

	@SuppressWarnings("deprecation")
	public void setDetective(Player p) {

	    clearplayer(p);

	    detective.add(p);

	    TitleAPI.sendTitle(p, 0, 60, 0,
	            Utils.FormatText(p, plugin.messages.getConfig().getString("you-are-detective-title")));

	    TitleAPI.sendSubtitle(p, 0, 60, 0,
	            Utils.FormatText(p, plugin.messages.getConfig().getString("you-are-detective-subtitle")));

	    // No modo AMONG US o Detective não usa arma.
	    // Ele joga como investigador: reporta corpo, lê pistas e chama reunião.
	    if (getGameMode() != GameModeType.SABOTAGE) {
	        ItemStack gun;
	        if (plugin.gunSkinManager != null) {
	            gun = plugin.gunSkinManager.createGun(p);
	        } else {
	            gun = new ItemStack(Material.DIAMOND_HOE);
	            ItemMeta meta = gun.getItemMeta();
	            meta.setDisplayName("§6§lMURDER> §aFive Seven");
	            List<String> lore = new ArrayList<String>();
	            lore.add("");
	            lore.add("§eEficiencia: §72");
	            lore.add("§eTempo: §73 Segundos");
	            meta.setLore(lore);
	            gun.setItemMeta(meta);
	        }
	        p.getInventory().addItem(gun);
	    }
	    if (getGameMode() == GameModeType.SABOTAGE && plugin.sabotageTaskManager != null) {
	        // Tasks são feitas pelo holograma/local. Só damos a reunião de emergência.
	        p.getInventory().setItem(5, plugin.sabotageTaskManager.createEmergencyItem());
	    }

	    if (plugin.hatAbilityManager != null) plugin.hatAbilityManager.applyGameAbility(p, this);
	    p.updateInventory();
	}
	
	
	public void setInnocent(Player p) {

	    clearplayer(p);

	    innocents.add(p);

	    p.getInventory().clear();
	    p.getInventory().setArmorContents(null);
	    if (getGameMode() == GameModeType.SABOTAGE && plugin.sabotageTaskManager != null) {
	        // Tasks são por holograma/local, sem item de task no inventário.
	        p.getInventory().setItem(5, plugin.sabotageTaskManager.createEmergencyItem());
	    }
	    if (plugin.hatAbilityManager != null) plugin.hatAbilityManager.applyGameAbility(p, this);
	    p.updateInventory();
	}
	

	public void setInnocents() {

	    for (Player p : getPlayers()) {

	    	if (!murder.contains(p) && !detective.contains(p) && !innocents.contains(p)) {
	    	    setInnocent(p);
	    	}
	    }
	}
	

	@SuppressWarnings("deprecation")
	public void setMurder(Player p) {

	    // 🔥 EVITA DUPLICAÇÃO DE ROLE
	    if (murder.contains(p)) return;

	    clearplayer(p);
	    murder.add(p);

	    // limpa totalmente inventário
	    p.getInventory().clear();
	    p.getInventory().setArmorContents(null);

	    // NÃO usar setItemInHand
	    p.updateInventory();

	    // =====================
	    // ITEMS FIXOS
	    // =====================

	    ItemStack pearl = new ItemStack(Material.ENDER_PEARL);
	    ItemMeta im = pearl.getItemMeta();
	    im.setDisplayName(ChatColor.GREEN + "Teleportador");
	    pearl.setItemMeta(im);
	    p.getInventory().setItem(1, pearl);

	    ItemStack feather = new ItemStack(Material.FEATHER);
	    ItemMeta im2 = feather.getItemMeta();
	    im2.setDisplayName(ChatColor.YELLOW + "Velocidade (Clique)");
	    feather.setItemMeta(im2);
	    p.getInventory().setItem(2, feather);

	    ItemStack fake = new ItemStack(Material.IRON_INGOT);
	    ItemMeta im3 = fake.getItemMeta();
	    im3.setDisplayName(ChatColor.YELLOW + "Item Falso");
	    fake.setItemMeta(im3);
	    p.getInventory().setItem(8, fake);

        Room sabotageRoom = getRoomForThisArena();
        if (((sabotageRoom != null && sabotageRoom.hasModifier(RoomModifier.SABOTAGE)) || getGameMode() == GameModeType.SABOTAGE)
                && plugin.sabotageManager != null) {
            p.getInventory().setItem(3, plugin.sabotageManager.createSabotageItem());
        }

        // AMONG US: cooldown inicial igual Among Us. O Murder nao começa
        // podendo matar instantaneamente assim que recebe a faca.
        if (getGameMode() == GameModeType.SABOTAGE && plugin.sabotageTaskManager != null) {
            plugin.sabotageTaskManager.resetMurderKillCooldown(p);
            plugin.sabotageTaskManager.setWaitingKnife(p);
        }

	    if (plugin.hatAbilityManager != null) plugin.hatAbilityManager.applyGameAbility(p, this);

	    // =====================
	    // TITLE
	    // =====================

	    TitleAPI.sendTitle(p, 0, 60, 0,
	            Utils.FormatText(p, plugin.messages.getConfig().getString("you-are-murderer-title")));

	    TitleAPI.sendSubtitle(p, 0, 60, 0,
	            Utils.FormatText(p, plugin.messages.getConfig().getString("you-are-murderer-subtitle")));

	    // =====================
	    // WEAPON / SKIN DE FACA
	    // =====================
	    // Antes a arena sempre criava a faca usando murderer-weapon da config.
	    // Isso fazia qualquer skin escolhida no menu ser ignorada quando a partida começava.
	    ItemStack sword;
	    if (plugin.knifeSkinManager != null) {
	        sword = plugin.knifeSkinManager.createKnife(p);
	    } else {
	        Material mat = Material.getMaterial(
	                plugin.settings.getConfig().getInt("murderer-weapon.item-id")
	        );

	        if (mat == null) return;

	        sword = new ItemStack(mat);
	        sword.setDurability((short) plugin.settings.getConfig().getInt("murderer-weapon.item-subid"));

	        ItemMeta sm = sword.getItemMeta();
	        sm.setDisplayName(Utils.FormatText(p,
	                plugin.settings.getConfig().getString("murderer-weapon.item-name")));

	        ArrayList<String> lore = new ArrayList<>();
	        lore.add(Utils.FormatText(p,
	                plugin.settings.getConfig().getString("murderer-weapon.item-lore")));

	        sm.setLore(lore);
	        sword.setItemMeta(sm);
	    }

	    // =====================
	    // SCHEDULER (PROTEGIDO)
	    // =====================

	    if (swordTask.containsKey(p.getUniqueId())) {
	        Bukkit.getScheduler().cancelTask(swordTask.get(p.getUniqueId()));
	    }
	    
	    int taskId = new BukkitRunnable() {
	        @Override
	        public void run() {

	            if (getState() != GameState.INGAME) return;
	            if (!p.isOnline()) return;
	            if (specs.contains(p)) return;
	            if (!murder.contains(p)) {
	                cancel(); // 🔥 mata a task se não for mais murder
	                return;
	            }

	            removeMurderKnives(p);

	            // AMONG US: se o kill cooldown do modo ainda estiver ativo,
	            // NAO devolve a faca real. O bug era aqui: a task antiga de
	            // entregar espada via receive-sword-after via o item "Aguarde..."
	            // como item invalido e colocava a faca real de volta no slot 0.
	            if (getGameMode() == GameModeType.SABOTAGE
	                    && plugin.sabotageTaskManager != null
	                    && !plugin.sabotageTaskManager.canMurderKill(p, Arena.this)) {
	                plugin.sabotageTaskManager.setWaitingKnife(p);
	                return;
	            }

	            ItemStack slot0 = p.getInventory().getItem(0);
	            if (slot0 == null || slot0.getType() == Material.AIR ||
	                    (plugin.knifeSkinManager != null && !plugin.knifeSkinManager.isKnife(slot0)) ||
	                    (plugin.knifeSkinManager == null && slot0.getType() != sword.getType())) {

	                p.getInventory().setItem(0, sword);
	            }

	            if (!said) {
	                said = true;
	                for (Player pl : getPlayers()) {
	                    pl.sendMessage(Utils.FormatText(pl,
	                            plugin.messages.getConfig().getString("murder-receive-sword-message")));
	                }
	            }
	        }
	    }.runTaskLater(plugin,
	            plugin.settings.getConfig().getInt("receive-sword-after") * 20).getTaskId();

	    swordTask.put(p.getUniqueId(), taskId);
	    
	}

	
	public void removeItemByName(Player p, String name) {
	    for (int i = 0; i < p.getInventory().getSize(); i++) {
	        ItemStack item = p.getInventory().getItem(i);

	        if (item == null || !item.hasItemMeta()) continue;

	        if (item.getItemMeta().getDisplayName().equalsIgnoreCase(name)) {
	            p.getInventory().setItem(i, null);
	        }
	    }
	}

	public void removeMurderKnives(Player p) {
	    for (int i = 0; i < p.getInventory().getSize(); i++) {
	        ItemStack item = p.getInventory().getItem(i);
	        if (item == null || item.getType() == Material.AIR) continue;

	        if (plugin.knifeSkinManager != null && plugin.knifeSkinManager.isKnife(item)) {
	            p.getInventory().setItem(i, null);
	            continue;
	        }

	        Material cfgMat = Material.getMaterial(plugin.settings.getConfig().getInt("murderer-weapon.item-id"));
	        if (cfgMat != null && item.getType() == cfgMat) {
	            p.getInventory().setItem(i, null);
	        }
	    }
	}


	// =========================

	// HIDE AND SEEK MODE
	public boolean isHideAndSeekMode() {
	    return getGameMode() == GameModeType.HIDE_AND_SEEK && getState() == GameState.INGAME;
	}

	private int getHideSeekHideTime() {
	    if (plugin.getConfig().contains("HideAndSeek.Hide-Time")) {
	        return plugin.getConfig().getInt("HideAndSeek.Hide-Time");
	    }
	    return 30;
	}

	private void applyHideSeekWaitingEffects(Player p, int seconds) {
	    if (p == null || !p.isOnline()) return;
	    int duration = Math.max(1, seconds) * 20 + 40;
	    p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, duration, 255, false, false), true);
	    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration, 255, false, false), true);
	    p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, duration, 200, false, false), true);
	}

	private int getHideSeekRadarUnlockSeconds() {
	    if (plugin.getConfig().contains("HideAndSeek.Radar-Unlock-Seconds")) {
	        return plugin.getConfig().getInt("HideAndSeek.Radar-Unlock-Seconds", 60);
	    }
	    if (plugin.getConfig().contains("hide-and-seek.radar-unlock-seconds")) {
	        return plugin.getConfig().getInt("hide-and-seek.radar-unlock-seconds", 60);
	    }
	    return 60;
	}

	public boolean isHideSeekRadarUnlocked() {
	    if (getGameMode() != GameModeType.HIDE_AND_SEEK || getState() != GameState.INGAME) return false;
	    if (!hideSeekReleased) return false;
	    return time <= getHideSeekRadarUnlockSeconds();
	}

	public int getHideSeekRadarUnlockSecondsRemaining() {
	    if (getGameMode() != GameModeType.HIDE_AND_SEEK || getState() != GameState.INGAME) return 0;
	    int unlockAt = getHideSeekRadarUnlockSeconds();
	    return Math.max(0, time - unlockAt);
	}

	private void clearHideSeekWaitingEffects(Player p) {
	    if (p == null || !p.isOnline()) return;
	    p.removePotionEffect(PotionEffectType.BLINDNESS);
	    p.removePotionEffect(PotionEffectType.SLOW);
	    p.removePotionEffect(PotionEffectType.JUMP);
	}

	private void startHideAndSeekMode() {
	    murder.clear();
	    detective.clear();
	    innocents.clear();
	    for (UUID uuid : new HashSet<UUID>(hideSeekFrozenSeekers)) {
	        Player p = Bukkit.getPlayer(uuid);
	        clearHideSeekWaitingEffects(p);
	    }
	    hideSeekFrozenSeekers.clear();
	    hideSeekFrozenLocations.clear();
	    hideSeekReleased = false;

	    List<Player> available = new ArrayList<Player>();
	    for (Player p : new ArrayList<Player>(players)) {
	        if (p != null && p.isOnline() && !specs.contains(p)) available.add(p);
	    }
	    if (available.isEmpty()) return;

	    Player seeker = null;
	    for (Player p : available) {
	        if (forcedRoles.get(p.getUniqueId()) == PlayerType.Murderer) {
	            seeker = p;
	            break;
	        }
	    }
	    if (seeker == null) seeker = available.get(random.nextInt(available.size()));

	    for (Player p : available) {
	        if (p.equals(seeker)) {
	            setHideSeekSeeker(p, true);
	        } else {
	            setHideSeekHider(p);
	        }
	    }

	    Murderer = seeker.getName();
	    Detective = "Nenhum";
	    refreshVisibility();

	    final int hideTime = getHideSeekHideTime();
	    for (Player p : available) {
	        if (murder.contains(p)) {
	            applyHideSeekWaitingEffects(p, hideTime);
	            TitleAPI.sendTitle(p, 0, 60, 10, "§c§lPROCURADOR", "§eAguarde " + hideTime + "s para procurar!");
	            p.sendMessage("§d§lESCONDE-ESCONDE §7> §cVocê está cego e travado até os escondidos terminarem de se esconder!");
	        } else {
	            TitleAPI.sendTitle(p, 0, 60, 10, "§a§lESCONDIDO", "§eVocê tem " + hideTime + "s para se esconder!");
	            p.sendMessage("§d§lESCONDE-ESCONDE §7> §aSe esconda! Se for pego, você vira Procurador.");
	        }
	    }

	    hideSeekReleaseTask = new BukkitRunnable() {
	        @Override
	        public void run() {
	            if (getState() != GameState.INGAME || getGameMode() != GameModeType.HIDE_AND_SEEK) return;
	            hideSeekReleased = true;
	            hideSeekFrozenSeekers.clear();
	            hideSeekFrozenLocations.clear();
	            for (Player p : new ArrayList<Player>(murder)) {
	                if (p == null || !p.isOnline()) continue;
	                clearHideSeekWaitingEffects(p);
	                giveHideSeekSeekerItems(p);
	                TitleAPI.sendTitle(p, 0, 50, 10, "§c§lLIBERADO!", "§eProcure todos os escondidos!");
	                p.sendMessage("§d§lESCONDE-ESCONDE §7> §cVocê foi liberado para procurar!");
	            }
	            for (Player p : new ArrayList<Player>(innocents)) {
	                if (p == null || !p.isOnline()) continue;
	                p.sendMessage("§d§lESCONDE-ESCONDE §7> §cO Procurador foi liberado!");
	            }
	        }
	    }.runTaskLater(plugin, hideTime * 20L).getTaskId();
	}

	private void setHideSeekHider(Player p) {
	    clearplayer(p);
	    innocents.add(p);
	    p.getInventory().clear();
	    p.getInventory().setArmorContents(null);
	    ItemStack compass = new ItemStack(Material.COMPASS);
	    ItemMeta meta = compass.getItemMeta();
	    meta.setDisplayName("§aDica dos Procuradores");
	    meta.setLore(Arrays.asList("§7Use para se orientar no mapa.", "§7Sobreviva até o tempo acabar."));
	    compass.setItemMeta(meta);
	    p.getInventory().setItem(0, compass);
	    p.updateInventory();
	}

	private void setHideSeekSeeker(Player p, boolean freeze) {
	    clearplayer(p);
	    murder.add(p);
	    p.getInventory().clear();
	    p.getInventory().setArmorContents(null);
	    giveHideSeekSeekerItems(p);
	    if (freeze) {
	        hideSeekFrozenSeekers.add(p.getUniqueId());
	        // A locacao exata e fixada no primeiro PlayerMoveEvent, depois do teleport para o spawn do mapa.
	        hideSeekFrozenLocations.remove(p.getUniqueId());
	    }
	    p.updateInventory();
	}

	private void giveHideSeekSeekerItems(Player p) {
	    ItemStack stick = new ItemStack(Material.STICK);
	    ItemMeta sm = stick.getItemMeta();
	    sm.setDisplayName("§c§lBastão de Captura");
	    sm.setLore(Arrays.asList("§7Bata em um escondido", "§7para transformar ele em Procurador."));
	    stick.setItemMeta(sm);
	    p.getInventory().setItem(0, stick);

	    ItemStack radar = new ItemStack(Material.COMPASS);
	    ItemMeta rm = radar.getItemMeta();
	    rm.setDisplayName("§eRadar");
	    rm.setLore(Arrays.asList("§7Libera apenas no fim da partida.", "§7Quando liberar, aponta para o escondido mais próximo."));
	    radar.setItemMeta(rm);
	    p.getInventory().setItem(1, radar);
	    p.updateInventory();
	}


	public int getHideSeekHidersCount() {
	    if (getGameMode() != GameModeType.HIDE_AND_SEEK) return 0;
	    int total = 0;
	    for (Player p : innocents) {
	        if (p != null && p.isOnline() && players.contains(p) && !specs.contains(p)) total++;
	    }
	    return total;
	}

	public int getHideSeekSeekersCount() {
	    if (getGameMode() != GameModeType.HIDE_AND_SEEK) return 0;
	    int total = 0;
	    for (Player p : murder) {
	        if (p != null && p.isOnline() && players.contains(p) && !specs.contains(p)) total++;
	    }
	    return total;
	}

	public String getHideSeekStatus() {
	    if (getGameMode() != GameModeType.HIDE_AND_SEEK) return "§7-";
	    if (getState() == GameState.LOBBY) return "§eAguardando";
	    if (getState() == GameState.STARTING) return "§6Iniciando";
	    if (getState() != GameState.INGAME) return "§7-";
	    return hideSeekReleased ? "§cProcurando" : "§eEscondendo";
	}

	public String getHideSeekPlayerRole(Player p) {
	    if (p == null || getGameMode() != GameModeType.HIDE_AND_SEEK) return "§7-";
	    if (murder.contains(p)) return "§cProcurador";
	    if (innocents.contains(p)) return "§aEscondido";
	    if (specs.contains(p)) return "§7Espectador";
	    return "§7-";
	}

	public void captureHideAndSeek(Player hider, Player seeker) {
	    if (hider == null || seeker == null) return;
	    if (getGameMode() != GameModeType.HIDE_AND_SEEK || getState() != GameState.INGAME) return;
	    if (!murder.contains(seeker) || !innocents.contains(hider)) return;

	    // Captura nao deve causar morte nem dano: o escondido apenas troca de time.
	    try {
	        hider.setFallDistance(0.0F);
	        hider.setFireTicks(0);
	        hider.setHealth(Math.max(1.0D, hider.getMaxHealth()));
	    } catch (Throwable ignored) {}

	    setHideSeekSeeker(hider, false);
	    try { hider.setHealth(hider.getMaxHealth()); } catch (Throwable ignored) {}
	    hider.teleport(seeker.getLocation());
	    for (Player p : getPlayers()) {
	        if (p == null || !p.isOnline()) continue;
	        p.sendMessage("§d§lESCONDE-ESCONDE §7> §e" + hider.getName() + " §cfoi encontrado por §e" + seeker.getName() + "§c e virou Procurador!");
	    }
	    if (innocents.isEmpty()) {
	        win("m");
	    }
	}

	@EventHandler
	public void onHideSeekMove(PlayerMoveEvent event) {
	    Player p = event.getPlayer();
	    if (!hideSeekFrozenSeekers.contains(p.getUniqueId())) return;
	    if (getGameMode() != GameModeType.HIDE_AND_SEEK || getState() != GameState.INGAME || hideSeekReleased) return;
	    if (Arenas.getArena(p) != this) return;
	    Location base = hideSeekFrozenLocations.get(p.getUniqueId());
	    if (base == null) {
	        base = event.getFrom();
	        hideSeekFrozenLocations.put(p.getUniqueId(), base);
	    }
	    Location to = event.getTo();
	    if (to != null && (to.getX() != base.getX() || to.getY() != base.getY() || to.getZ() != base.getZ())) {
	        Location fixed = base.clone();
	        fixed.setYaw(to.getYaw());
	        fixed.setPitch(to.getPitch());
	        event.setTo(fixed);
	    }
	}

	private void stopHideAndSeekMode() {
	    for (UUID uuid : new HashSet<UUID>(hideSeekFrozenSeekers)) {
	        Player p = Bukkit.getPlayer(uuid);
	        clearHideSeekWaitingEffects(p);
	    }
	    hideSeekFrozenSeekers.clear();
	    hideSeekFrozenLocations.clear();
	    hideSeekReleased = false;
	    if (hideSeekReleaseTask != -1) {
	        Bukkit.getScheduler().cancelTask(hideSeekReleaseTask);
	        hideSeekReleaseTask = -1;
	    }
	}

	// TNT TAG MODE
	// =========================
	public boolean isTntTagMode() {
	    return getGameMode() == GameModeType.TNT_TAG && getState() == GameState.INGAME;
	}

	public Player getTntHolder() {
	    if (tntHolder == null) return null;
	    Player p = Bukkit.getPlayer(tntHolder);
	    if (p == null || !p.isOnline() || specs.contains(p) || !players.contains(p)) return null;
	    return p;
	}

	public boolean isTntHolder(Player p) {
	    return p != null && tntHolder != null && tntHolder.equals(p.getUniqueId());
	}

	public int getTntRoundTime() {
	    return tntRoundTime;
	}

	public boolean isTntHitProtected(Player p) {
	    return p != null && tntHitProtectedPlayers.contains(p.getUniqueId());
	}

	public boolean toggleTntHitProtection(Player p) {
	    if (p == null) return false;
	    UUID uuid = p.getUniqueId();
	    if (tntHitProtectedPlayers.contains(uuid)) {
	        tntHitProtectedPlayers.remove(uuid);
	        return false;
	    }
	    tntHitProtectedPlayers.add(uuid);
	    return true;
	}

	public void clearTntHitProtection(Player p) {
	    if (p != null) {
	        tntHitProtectedPlayers.remove(p.getUniqueId());
	    }
	}

	public void clearAllTntHitProtections() {
	    tntHitProtectedPlayers.clear();
	}

	public String getTntHolderName() {
	    Player holder = getTntHolder();
	    return holder != null ? holder.getName() : "Nenhum";
	}

	public int getAlivePlayersCount() {
	    int alive = 0;
	    for (Player p : players) {
	        if (p != null && p.isOnline() && !specs.contains(p)) {
	            alive++;
	        }
	    }
	    return alive;
	}

	public void tagTnt(Player from, Player to) {
	    if (!isTntTagMode()) return;
	    if (from == null || to == null) return;
	    if (!isTntHolder(from)) return;
	    if (specs.contains(to) || !players.contains(to)) return;
	    if (from.equals(to)) return;

	    clearTntVisual(from);
	    tntHolder = to.getUniqueId();
	    giveTntVisual(to);

	    for (Player p : getPlayers()) {
	        p.sendMessage("§6§lTNT TAG> §e" + from.getName() + " §7passou a TNT para §c" + to.getName() + "§7!");
	    }
	}

	private void giveTntVisual(Player p) {
	    if (p == null) return;
	    ItemStack tnt = new ItemStack(Material.TNT);
	    ItemMeta meta = tnt.getItemMeta();
	    meta.setDisplayName("§c§lVOCÊ ESTÁ COM A TNT!");
	    ArrayList<String> lore = new ArrayList<String>();
	    lore.add("§7Bata em outro jogador para passar a TNT.");
	    lore.add("§7Se o tempo acabar, você explode!");
	    meta.setLore(lore);
	    tnt.setItemMeta(meta);
	    p.getInventory().setHelmet(tnt.clone());
	    p.getInventory().setItem(0, tnt);

	    ItemStack radar = new ItemStack(Material.COMPASS);
	    ItemMeta radarMeta = radar.getItemMeta();
	    radarMeta.setDisplayName("§c§lRadar TNT");
	    radarMeta.setLore(Arrays.asList("§7Aponta para o jogador vivo", "§7mais próximo para passar a TNT.", "", "§eClique para atualizar."));
	    radar.setItemMeta(radarMeta);
	    p.getInventory().setItem(1, radar);

	    ItemStack speed = new ItemStack(Material.SUGAR);
	    ItemMeta speedMeta = speed.getItemMeta();
	    speedMeta.setDisplayName("§e§lVelocidade TNT");
	    speedMeta.setLore(Arrays.asList("§7Use para ganhar velocidade", "§7e alcançar outro jogador.", "", "§eClique para usar."));
	    speed.setItemMeta(speedMeta);
	    p.getInventory().setItem(2, speed);

	    updateTntRadarTarget(p, false);
	    p.updateInventory();
	    TitleAPI.sendTitle(p, 0, 40, 10, "§c§lTNT!", "§eBata em alguém para passar!");
	}

	private void clearTntVisual(Player p) {
	    if (p == null) return;
	    if (p.getInventory().getHelmet() != null && p.getInventory().getHelmet().getType() == Material.TNT) {
	        p.getInventory().setHelmet(null);
	    }
	    for (int i = 0; i < p.getInventory().getSize(); i++) {
	        ItemStack item = p.getInventory().getItem(i);
	        if (item != null && (item.getType() == Material.TNT || isTntRadarItem(item) || isTntSpeedItem(item))) {
	            p.getInventory().setItem(i, null);
	        }
	    }
	    p.updateInventory();
	}


	private boolean isTntRadarItem(ItemStack item) {
	    return item != null && item.getType() == Material.COMPASS && item.hasItemMeta()
	            && item.getItemMeta().hasDisplayName()
	            && ChatColor.stripColor(item.getItemMeta().getDisplayName()).equalsIgnoreCase("Radar TNT");
	}

	private boolean isTntSpeedItem(ItemStack item) {
	    return item != null && item.getType() == Material.SUGAR && item.hasItemMeta()
	            && item.getItemMeta().hasDisplayName()
	            && ChatColor.stripColor(item.getItemMeta().getDisplayName()).equalsIgnoreCase("Velocidade TNT");
	}

	private Player getNearestTntTarget(Player holder) {
	    if (holder == null) return null;
	    Player nearest = null;
	    double best = Double.MAX_VALUE;
	    for (Player target : new ArrayList<Player>(players)) {
	        if (target == null || !target.isOnline() || target.equals(holder) || specs.contains(target)) continue;
	        if (!target.getWorld().equals(holder.getWorld())) continue;
	        double dist = target.getLocation().distanceSquared(holder.getLocation());
	        if (dist < best) {
	            best = dist;
	            nearest = target;
	        }
	    }
	    return nearest;
	}

	private void updateTntRadarTarget(Player holder, boolean message) {
	    Player target = getNearestTntTarget(holder);
	    if (target == null) {
	        if (message) holder.sendMessage("§c§lTNT TAG> §7Nenhum jogador vivo encontrado para o radar.");
	        return;
	    }
	    holder.setCompassTarget(target.getLocation());
	    if (message) {
	        int blocks = (int) Math.sqrt(holder.getLocation().distanceSquared(target.getLocation()));
	        holder.sendMessage("§c§lTNT TAG> §7Radar apontando para §e" + target.getName() + " §8(" + blocks + " blocos)§7.");
	        holder.playSound(holder.getLocation(), Sound.CLICK, 1.0F, 1.5F);
	    }
	}

	private void useTntSpeed(Player holder) {
	    if (holder == null) return;
	    long now = System.currentTimeMillis();
	    long cooldownMs = plugin.getConfig().getInt("tnt-tag.speed-cooldown-seconds", 18) * 1000L;
	    long last = tntSpeedCooldown.containsKey(holder.getUniqueId()) ? tntSpeedCooldown.get(holder.getUniqueId()) : 0L;
	    long remaining = (last + cooldownMs) - now;
	    if (remaining > 0) {
	        holder.sendMessage("§c§lTNT TAG> §7Aguarde §c" + ((remaining + 999) / 1000) + "s §7para usar a velocidade novamente.");
	        return;
	    }
	    int duration = plugin.getConfig().getInt("tnt-tag.speed-duration-seconds", 5);
	    int amplifier = Math.max(0, plugin.getConfig().getInt("tnt-tag.speed-amplifier", 1));
	    holder.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration * 20, amplifier), true);
	    tntSpeedCooldown.put(holder.getUniqueId(), now);
	    holder.playSound(holder.getLocation(), Sound.FIZZ, 1.0F, 1.3F);
	    holder.sendMessage("§c§lTNT TAG> §eVelocidade ativada por " + duration + "s!");
	}

	private void assignRandomTntHolder() {
	    ArrayList<Player> alive = new ArrayList<Player>();
	    for (Player p : players) {
	        if (p != null && p.isOnline() && !specs.contains(p)) alive.add(p);
	    }
	    if (alive.size() <= 1) return;

	    Player selected = null;

	    // No TNTTag, quem foi definido como Murder no gerenciador vira TAGGER inicial.
	    for (Player p : alive) {
	        if (forcedRoles.get(p.getUniqueId()) == PlayerType.Murderer) {
	            selected = p;
	            break;
	        }
	    }

	    if (selected == null) {
	        selected = alive.get(random.nextInt(alive.size()));
	    }

	    tntHolder = selected.getUniqueId();
	    giveTntVisual(selected);
	    for (Player p : getPlayers()) {
	        p.sendMessage("§6§lTNT TAG> §c" + selected.getName() + " §7começou com a TNT!");
	    }
	}

	private void eliminateTntHolder() {
	    Player holder = getTntHolder();
	    if (holder == null) {
	        assignRandomTntHolder();
	        return;
	    }

	    clearTntVisual(holder);
	    holder.getWorld().createExplosion(holder.getLocation(), 0F, false);
	    holder.damage(0.0D);
	    holder.setLastDamageCause(new EntityDamageEvent(holder, EntityDamageEvent.DamageCause.CUSTOM, 0.0D));

	    if (plugin.spectatorManager != null) {
	        plugin.spectatorManager.makeSpectator(holder, this, "§cVocê explodiu e virou espectador!");
	    } else {
	        players.remove(holder);
	        clearplayer(holder);
	        if (!specs.contains(holder)) specs.add(holder);
	        plugin.setPlayerState(holder, PlayerState.SPECTATOR);
	        holder.teleport(plugin.getSpec(this));
	        holder.setGameMode(GameMode.ADVENTURE);
	        refreshVisibility();
	    }

	    for (Player p : getPlayers()) {
	        if (p != null && p.isOnline()) p.sendMessage("§6§lTNT TAG> §c" + holder.getName() + " §7explodiu!");
	    }

	    tntHolder = null;
	    clearAllTntHitProtections();
	    if (players.size() <= 1) {
	        win("p");
	        return;
	    }
	    assignRandomTntHolder();
	}



	public void eliminateTntTagPlayer(Player player, String reason) {
	    if (player == null || !players.contains(player) || specs.contains(player)) return;
	    if (isTntHolder(player)) {
	        clearTntVisual(player);
	        tntHolder = null;
	        clearAllTntHitProtections();
	    }

	    if (plugin.spectatorManager != null) {
	        plugin.spectatorManager.makeSpectator(player, this, "§cVocê morreu" + (reason == null ? "" : " " + reason) + " e virou espectador!");
	    } else {
	        players.remove(player);
	        clearplayer(player);
	        if (!specs.contains(player)) specs.add(player);
	        plugin.setPlayerState(player, PlayerState.SPECTATOR);
	        player.teleport(plugin.getSpec(this));
	        player.setGameMode(GameMode.ADVENTURE);
	        refreshVisibility();
	    }

	    if (players.size() <= 1) {
	        win("p");
	        return;
	    }

	    if (tntHolder == null) {
	        assignRandomTntHolder();
	    }
	}


	private void startTntTagMode() {
	    murder.clear();
	    detective.clear();
	    innocents.clear();
	    specs.clear();
	    tntHolder = null;
	    clearAllTntHitProtections();

	    for (Player p : new ArrayList<Player>(players)) {
	        clearplayer(p);
	        innocents.add(p);
	        p.getInventory().clear();
	        p.getInventory().setArmorContents(null);
	        p.setGameMode(GameMode.ADVENTURE);
	        p.updateInventory();
	        TitleAPI.sendTitle(p, 0, 50, 10, "§c§lTNT TAG", "§ePasse a TNT antes de explodir!");
	    }

	    assignRandomTntHolder();
	    tntRoundTime = plugin.settings.getConfig().contains("tnt-tag-round-time")
	            ? plugin.settings.getConfig().getInt("tnt-tag-round-time") : 30;

	    if (tntTagTask != -1) Bukkit.getScheduler().cancelTask(tntTagTask);
	    tntTagTask = new BukkitRunnable() {
	        @Override
	        public void run() {
	            if (getState() != GameState.INGAME || getGameMode() != GameModeType.TNT_TAG) {
	                cancel();
	                return;
	            }
	            if (players.size() <= 1) {
	                win("p");
	                cancel();
	                return;
	            }
	            Player holder = getTntHolder();
	            if (holder == null) {
	                assignRandomTntHolder();
	            }
	            if (tntRoundTime <= 0) {
	                eliminateTntHolder();
	                tntRoundTime = plugin.settings.getConfig().contains("tnt-tag-round-time")
	                        ? plugin.settings.getConfig().getInt("tnt-tag-round-time") : 30;
	                return;
	            }
	            if (holder != null && (tntRoundTime <= 5 || tntRoundTime % 10 == 0)) {
	                for (Player p : getPlayers()) {
	                    p.sendMessage("§6§lTNT TAG> §e" + holder.getName() + " §7explode em §c" + tntRoundTime + "s§7!");
	                }
	            }
	            tntRoundTime--;
	        }
	    }.runTaskTimer(plugin, 20L, 20L).getTaskId();
	}

	public void SetUp() {

	    loadamount();

	    // Limpa roles antigas antes de qualquer sorteio.
	    murder.clear();
	    detective.clear();
	    innocents.clear();
	    alreadyAssigned.clear();

	    // Quantidades seguras para evitar loop infinito quando tiver pouca gente.
	    if (murdereramount <= 0) murdereramount = 1;
	    if (detectiveamount < 0) detectiveamount = 0;

	    for (Player p : new ArrayList<Player>(players)) {
	        if (p == null || !p.isOnline()) continue;
	        plugin.setPlayerState(p, PlayerState.IN_GAME);
	        p.getInventory().clear();
	        p.getInventory().setArmorContents(null);
	        p.updateInventory();
	    }

	    // =========================
	    // TNT TAG MODE
	    // =========================
	    if (getGameMode() == GameModeType.TNT_TAG) {
	        startTntTagMode();
	        clearForcedRoleSelections();
	        return;
	    }

	    // =========================
	    // ESCONDE-ESCONDE
	    // =========================
	    if (getGameMode() == GameModeType.HIDE_AND_SEEK) {
	        startHideAndSeekMode();
	        clearForcedRoleSelections();
	        return;
	    }

	    // =========================
	    // TODOS ASSASSINOS
	    // =========================
	    if (getGameMode() == GameModeType.ALL_MURDER) {
	        detective.clear();
	        innocents.clear();
	        for (Player p : new ArrayList<Player>(players)) {
	            if (p == null || !p.isOnline() || specs.contains(p)) continue;
	            setMurder(p);
	            alreadyAssigned.add(p.getUniqueId());
	        }
	        Murderer = toPlayerString(murder);
	        Detective = "Nenhum";
	        clearForcedRoleSelections();
	        return;
	    }

	    // =========================
	    // NORMAL MODE - forced roles primeiro
	    // =========================

	    // 1) Murder forçado primeiro.
	    for (Player p : new ArrayList<Player>(players)) {
	        if (p == null || !p.isOnline() || specs.contains(p)) continue;
	        PlayerType forced = forcedRoles.get(p.getUniqueId());
	        if (forced == PlayerType.Murderer) {
	            setMurder(p);
	            alreadyAssigned.add(p.getUniqueId());
	        }
	    }

	    // 2) Detective forçado depois, sem sobrescrever Murder.
	    for (Player p : new ArrayList<Player>(players)) {
	        if (p == null || !p.isOnline() || specs.contains(p)) continue;
	        if (alreadyAssigned.contains(p.getUniqueId())) continue;
	        PlayerType forced = forcedRoles.get(p.getUniqueId());
	        if (forced == PlayerType.Detective) {
	            setDetective(p);
	            alreadyAssigned.add(p.getUniqueId());
	        }
	    }

	    // 3) Innocent forçado.
	    for (Player p : new ArrayList<Player>(players)) {
	        if (p == null || !p.isOnline() || specs.contains(p)) continue;
	        if (alreadyAssigned.contains(p.getUniqueId())) continue;
	        PlayerType forced = forcedRoles.get(p.getUniqueId());
	        if (forced == PlayerType.Innocents) {
	            setInnocent(p);
	            alreadyAssigned.add(p.getUniqueId());
	        }
	    }

	    // 4) Kits de chance entram antes do sorteio normal, sem sobrescrever roles forçadas.
	    applyRoleChanceKits();

	    // 5) Sorteio aleatório só entre quem ainda não tem role.
	    while (murder.size() < murdereramount) {
	        int before = murder.size();
	        randomMurderer();
	        if (murder.size() == before) break;
	    }

	    while (detective.size() < detectiveamount) {
	        int before = detective.size();
	        RandomDetective();
	        if (detective.size() == before) break;
	    }

	    // 6) O resto vira inocente.
	    for (Player p : new ArrayList<Player>(players)) {
	        if (p == null || !p.isOnline() || specs.contains(p)) continue;
	        if (!murder.contains(p) && !detective.contains(p) && !innocents.contains(p)) {
	            setInnocent(p);
	            alreadyAssigned.add(p.getUniqueId());
	        }
	    }

	    Murderer = toPlayerString(murder);
	    Detective = toPlayerString(detective);
	    clearForcedRoleSelections();
	    refreshVisibility();

	    new BukkitRunnable() {
	        @Override
	        public void run() {
	            for (Player p : getPlayers()) {
	                if (p == null || !p.isOnline()) continue;
	                p.setGameMode(GameMode.SURVIVAL);
	                p.updateInventory();
	            }
	            refreshVisibility();
	        }
	    }.runTaskLater(plugin, 20L);
	}

	private void applyRoleChanceKits() {
	    int murderChance = plugin.getConfig().getInt("kits.chance-murder-percent", 20);
	    int detectiveChance = plugin.getConfig().getInt("kits.chance-detective-percent", 20);
	    murderChance = Math.max(0, Math.min(100, murderChance));
	    detectiveChance = Math.max(0, Math.min(100, detectiveChance));

	    for (Player p : new ArrayList<Player>(players)) {
	        if (p == null || !p.isOnline() || specs.contains(p)) continue;
	        if (alreadyAssigned.contains(p.getUniqueId())) continue;
	        Kit kit = plugin.kitManager != null ? plugin.kitManager.getSelectedKit(p) : null;
	        if (!(kit instanceof AdvancedKit)) continue;
	        String id = ((AdvancedKit) kit).getId();

	        if ("chance_murder".equalsIgnoreCase(id) && murder.size() < murdereramount) {
	            if (random.nextInt(100) < murderChance) {
	                setMurder(p);
	                alreadyAssigned.add(p.getUniqueId());
	            }
	        }
	    }

	    for (Player p : new ArrayList<Player>(players)) {
	        if (p == null || !p.isOnline() || specs.contains(p)) continue;
	        if (alreadyAssigned.contains(p.getUniqueId())) continue;
	        Kit kit = plugin.kitManager != null ? plugin.kitManager.getSelectedKit(p) : null;
	        if (!(kit instanceof AdvancedKit)) continue;
	        String id = ((AdvancedKit) kit).getId();

	        if ("chance_detective".equalsIgnoreCase(id) && detective.size() < detectiveamount) {
	            if (random.nextInt(100) < detectiveChance) {
	                setDetective(p);
	                alreadyAssigned.add(p.getUniqueId());
	            }
	        }
	    }
	}


	@SuppressWarnings("deprecation")
	public void start() {
		countdownPaused = false;

		if (state != GameState.STARTING && state != GameState.LOBBY) {
			return;
		}
		if (players.size() == 0 || getPlayers().size() == 0) {
			return;
		}

		applyWinningRoomMap();

		plugin.getSpec(this).getWorld().setThundering(false);
		plugin.getSpec(this).getWorld().setStorm(false);

		state = GameState.INGAME;

		// Ranked V3: inicia a zona assim que a partida entra em jogo.
		startRankedZoneIfNeeded();
		
		// Kits agora são aplicados depois do SetUp(), quando as roles já existem.

		new BukkitRunnable() {

			@Override
			public void run() {

				if (getState() != GameState.INGAME) {
					this.cancel();
					return;
				}

				for (Player p : getPlayers()) {

					if (getType(p) == PlayerType.Innocents) {

						if (IsBowDropped()) {
							for (Entity x : plugin.getNearbyEntities(p.getLocation(),
									plugin.settings.getConfig().getInt("bow-pickup-radius"))) {
								if (!p.getWorld().getName().equalsIgnoreCase(x.getWorld().getName())) {
									return;
								}
								if (x instanceof ArmorStand) {
									ArmorStand z = (ArmorStand) x;

									if (z.getItemInHand().getType() == Material.DIAMOND_HOE) {

										if (specs.contains(p)) {

											return;
										}
										if (pic.contains(p.getName())) {
											return;
										}
										pic.add(p.getName());

										p.getInventory().clear();
										if (!p.getInventory().contains(new ItemStack(Material.DIAMOND_HOE))) {
											p.getInventory().addItem(new ItemStack(Material.DIAMOND_HOE));
										}
										//ItemStack arrow = new ItemStack(Material.ARROW, 64);
										//p.getInventory().setItem(9, arrow);
										p.updateInventory();

										if (plugin.settings.getConfig().getBoolean("enable-sounds")) {
											p.playSound(p.getLocation(),
													Sound.valueOf(plugin.settings.getConfig().getString("PICK_UP")), 1,
													1);
										}

										for (Player b : getInnocents()) {
											plugin.removeCompass(b);
										}
										for (Player xc : getPlayers()) {
											xc.sendMessage(Utils.FormatText(xc,
													plugin.messages.getConfig().getString("pickup-bow")));
										}

										if (isdead) {
											isdead = false;
										}

										if (pic.contains(p.getName())) {

											pic.remove(p.getName());
										}

										if (!lists.contains(p.getName())) {
											lists.add(p.getName());
										}
										if (bowloc != null) {

											bowloc = null;
										}
										if (armor.contains(z)) {
											armor.remove(z);
										}
										z.remove();
									}
								}
							}
						}
					}

				}

				if (time > 0) {
					time -= 1;
				}
				if (plugin.settings.getConfig().getBoolean("tracking-compass")) {
					if (time <= plugin.settings.getConfig().getInt("time-to-give-tracker")) {
						if (murder.size() > 0) {

							for (Player p : murder) {
								if (p != null) {
									ItemStack s = new ItemStack(Material
											.getMaterial(plugin.settings.getConfig().getInt("murder-track.item-id")));
									s.setDurability(
											(short) plugin.settings.getConfig().getInt("murder-track.item-subid"));
									ItemMeta sm = s.getItemMeta();
									sm.setDisplayName(Utils.FormatText(p,
											plugin.settings.getConfig().getString("murder-track.item-name")));
									ArrayList<String> lore = new ArrayList<>();
									lore.add(Utils.FormatText(p,
											plugin.settings.getConfig().getString("murder-track.item-lore")));
									sm.setLore(lore);
									s.setItemMeta(sm);
									if (!p.getInventory().contains(s)) {
										p.getInventory().setItem(3, s);
										p.updateInventory();
									}

								}
							}
						}
					}
				}

				if (time <= 0) {
					if (detective.size() > 0 || innocents.size() > 0) {
						win("p");
					} else {
						stop("reload");
					}
				}

			}
		}.runTaskTimer(plugin, 20, 20);
		new BukkitRunnable() {

			@Override
			public void run() {

				if (getState() != GameState.INGAME) {
					this.cancel();
					return;
				}
				plugin.DropGold(getArena());

				for (ArmorStand e : getArmor()) {
					if (e.isDead() || e == null) {
						if (armor.contains(e)) {
							armor.remove(e);
						}
					}
				}

				for (Entity e : getGolds()) {
					if (e.isDead() || e == null) {
						if (golds.contains(e)) {
							golds.remove(e);
						}
					}
				}

				for (ArmorStand e : getSwords()) {
					if (e.isDead() || e == null) {
						if (sword.contains(e)) {
							sword.remove(e);
						}
					}
				}

			}
		}.runTaskTimer(plugin, plugin.settings.getConfig().getInt("gold-drop-interval"),
				plugin.settings.getConfig().getInt("gold-drop-interval"));

		SetUp();

		// APLICAR KITS UMA VEZ SÓ
		// Antes rodava antes do SetUp(), então o Murder ainda não tinha role e kits/itens
		// como Velocidade podiam falhar ou serem bloqueados.
		for (Player p : getPlayers()) {
		    Kit kit = plugin.kitManager != null ? plugin.kitManager.getSelectedKit(p) : null;
		    if (kit != null) {
		        if (plugin.kitManager.isAllowedForRole(kit, getType(p), getGameMode())) {
		            kit.apply(p);
		        } else {
		            p.sendMessage("§6§lMURDER §7> §cO kit §f" + kit.getName() + " §cnão combina com sua função nessa partida.");
		        }
		    }
		}
		if (getGameMode() == GameModeType.RANKED) {
			String template = getTemplateName();
			if (plugin.arenas.getConfig().contains("RankedTime." + template)) {
				time = plugin.arenas.getConfig().getInt("RankedTime." + template);
			} else if (plugin.getConfig().contains("ranked.match-time-seconds")) {
				time = plugin.getConfig().getInt("ranked.match-time-seconds", 600);
			} else {
				time = 10 * 60;
			}
		} else {
			if (!plugin.arenas.getConfig().contains("Time." + getName())) {
				time = 5 * 60;
			}

			if (plugin.arenas.getConfig().contains("Time." + getName())) {
				time = plugin.arenas.getConfig().getInt("Time." + getName());
			}
		}
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yy");
		Date now = new Date();
		for (Player p : getPlayers()) {

			List<String> list = plugin.messages.getConfig().getStringList("murder-game-start-message");

			for (String s : list) {
				p.sendMessage(Utils.FormatText(p,
						s.replaceAll("%iamount%", String.valueOf(innocents.size())).replaceAll("%map%", getName())
								.replaceAll("%time%", Utils.formattominutes(time))
								.replaceAll("%damount%", String.valueOf(detective.size()))
								.replaceAll("%mamount%", String.valueOf(murder.size()))
								.replaceAll("%date%", String.valueOf(format.format(now)).replaceAll("-", "/"))
								.replaceAll("%players%", String.valueOf(players.size()))));

			}

			p.teleport(plugin.getSpawn(getArena(), spawns));
			

		    

			if (!plugin.scoreboards.containsKey(p.getName())) {
				plugin.setScoreboard(p);
			}

			spawns += 1;
			if (p.getInventory().contains(Material.getMaterial(plugin.settings.getConfig().getInt("quit2.item-id")))) {
				p.getInventory().remove(
						new ItemStack(Material.getMaterial(plugin.settings.getConfig().getInt("quit2.item-id"))));
			}
			if (p.getInventory().contains(Material.getMaterial(plugin.settings.getConfig().getInt("quit.item-id")))) {
				p.getInventory().remove(
						new ItemStack(Material.getMaterial(plugin.settings.getConfig().getInt("quit.item-id"))));
			}
			if (p.getInventory().contains(Material.getMaterial(plugin.settings.getConfig().getInt("quit3.item-id")))) {
				p.getInventory().remove(
						new ItemStack(Material.getMaterial(plugin.settings.getConfig().getInt("quit3.item-id"))));
			}
			p.updateInventory();
		}

        if (getGameMode() == GameModeType.SABOTAGE && plugin.amongUsNameTagManager != null) {
            final Arena arena = this;
            new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.amongUsNameTagManager.applyArena(arena);
                }
            }.runTaskLater(plugin, 20L);
        }

	}

	public void stop(String Reason) {
        if (plugin.amongUsNameTagManager != null) {
            plugin.amongUsNameTagManager.restoreArena(this);
        }
		if (this.knifeManager != null) {
			this.knifeManager.reset();
		}
		stopRankedZone();
		stopHideAndSeekMode();
		if (plugin.sabotageTaskManager != null) {
			plugin.sabotageTaskManager.cleanupArena(this);
		}
		if (Reason.equalsIgnoreCase("stop")) {
			this.state = GameState.LOBBY;
			if (data.size() > 0) {

				for (CorpseData s : data) {
					CorpseAPI.removeCorpse(s);
			       
				}
			}

			if (getPlayers().size() > 0) {
				for (Player p : getPlayers()) {
					
			        Kit kit = plugin.kitManager != null ? plugin.kitManager.getSelectedKit(p) : null;

			        if (kit != null) {
			            kit.remove(p);
			            plugin.kitManager.removeSelectedKit(p);
			        }

					if (plugin.getConfig().getBoolean("update-data-on-game-end")) {
						if (plugin.getPlayerData(p) != null) {
							PlayerData data = plugin.getPlayerData(p);
							if (!plugin.getConfig().getBoolean("mysql")) {
								plugin.api.setNonSQLData(data.p, data.getkill(), data.getdeaths(), data.getloses(),
										data.getwins(), data.getcoins(), data.getscore());
							}
							if (plugin.getConfig().getBoolean("mysql")) {
								plugin.api.setSQLData(data.p, data.getkill(), data.getdeaths(), data.getloses(),
										data.getwins(), data.getcoins(), data.getscore());
							}
						}
					}

					plugin.setup(p);
					if (!plugin.getConfig().getBoolean("send-to-server-on-leave")) {
						p.teleport(plugin.getLobby());
					}

					if (players.contains(p)) {
						players.remove(p);
					}
					if (specs.contains(p)) {
						specs.remove(p);
					}
					restoreInventory(p);
					if (Arenas.isInArena(p)) {
						Arenas.removeArena(p);
					}
					plugin.restoreInventory(p);

					if (plugin.getConfig().getBoolean("send-to-server-on-leave")) {
						ByteArrayDataOutput out = ByteStreams.newDataOutput();
						out.writeUTF("Connect");
						out.writeUTF(plugin.getConfig().getString("lobby-server"));
						if (plugin.isEnabled()) {
							p.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
						}
					}

				}

			}

			innocents.clear();
			murder.clear();
			detective.clear();
			heros.clear();

			if (golds.size() > 0) {
				for (Entity gold : getGolds()) {
					if (!gold.isDead() && gold != null) {
						gold.remove();

					}
					golds.remove(gold);
				}
			}
			if (items.size() > 0) {
				for (FlyingItems gold : getItems()) {
					if (gold != null) {
						gold.remove();

					}
					items.remove(gold);
				}
			}
			if (sword.size() > 0) {
				for (ArmorStand gold : getSwords()) {
					if (!gold.isDead() && gold != null) {
						gold.remove();

					}
					sword.remove(gold);
				}
			}
			if (armor.size() > 0) {
				for (ArmorStand gold : getArmor()) {
					if (!gold.isDead() && gold != null) {
						gold.remove();

					}
					armor.remove(gold);
				}
			}

			reset();
			players.clear();
			specs.clear();
			
			this.maxPlayers = this.originalMaxPlayers;
		}

		if (Reason.equalsIgnoreCase("reload")) {
			ArrayList<Player> replayPlayers = new ArrayList<Player>();
			Player oldOwner = owner;

			if (persistentRoom) {
				if (oldOwner != null && getPlayers().contains(oldOwner)) {
					replayPlayers.add(oldOwner);
				}

				for (Player rp : getPlayers()) {
					if (rp == null) continue;
					if (oldOwner != null && rp.equals(oldOwner)) continue;
					if (!replayPlayers.contains(rp)) {
						replayPlayers.add(rp);
					}
				}
			}

			this.state = GameState.LOBBY;
			if (data.size() > 0) {
				for (CorpseData s : data) {
					CorpseAPI.removeCorpse(s);
				}
			}

			if (getPlayers().size() > 0) {
				for (Player p : getPlayers()) {

					p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
					plugin.setup(p);
					if (!persistentRoom && !plugin.getConfig().getBoolean("send-to-server-on-leave")) {
						p.teleport(plugin.getLobby());
					}

					if (players.contains(p)) {
						players.remove(p);
					}

					if (specs.contains(p)) {
						specs.remove(p);
					}
					if (plugin.settings.getConfig().getBoolean("send-stats-message-on-leave")) {
						if (plugin.getPlayerData(p) != null) {
							for (int i = 1; i < 15; i++) {
								p.sendMessage("");
							}

							List<String> list = plugin.messages.getConfig().getStringList("self-info-message");
							for (String s : list) {
								p.sendMessage(Utils.FormatText(p, s.replaceAll("%player%", p.getName())
										.replaceAll("%loses%", String.valueOf(plugin.getPlayerData(p).getloses()))
										.replaceAll("%wins%", String.valueOf(plugin.getPlayerData(p).getwins()))
										.replaceAll("%kills%", String.valueOf(plugin.getPlayerData(p).getkill()))
										.replaceAll("%score%", String.valueOf(plugin.getPlayerData(p).getscore()))
										.replaceAll("%deaths%", String.valueOf(plugin.getPlayerData(p).getdeaths()))));

							}
						}
					}
					if (Arenas.isInArena(p)) {
						Arenas.removeArena(p);
					}
					plugin.restoreInventory(p);
					restoreInventory(p);

				}

			}

			innocents.clear();
			murder.clear();
			detective.clear();
			heros.clear();

			if (golds.size() > 0) {
				for (Entity gold : getGolds()) {
					if (!gold.isDead() && gold != null) {
						gold.remove();

					}
					golds.remove(gold);
				}
			}
			if (items.size() > 0) {
				for (FlyingItems gold : getItems()) {
					if (gold != null) {
						gold.remove();

					}
					items.remove(gold);
				}
			}
			if (sword.size() > 0) {
				for (ArmorStand gold : getSwords()) {
					if (!gold.isDead() && gold != null) {
						gold.remove();

					}
					sword.remove(gold);
				}
			}
			if (armor.size() > 0) {
				for (ArmorStand gold : getArmor()) {
					if (!gold.isDead() && gold != null) {
						gold.remove();

					}
					armor.remove(gold);
				}
			}
			new BukkitRunnable() {

				@Override
				public void run() {
					if (plugin.getConfig().getBoolean("bungee")) {

						if (Arenas.getArenas().size() == 1) {
							Arena a = Arenas.getArenas().get(0);
							if (Bukkit.getOnlinePlayers().size() > 0) {
								for (Player p : Bukkit.getOnlinePlayers()) {
									if (p.isDead() || p.getHealth() == 0) {
										p.setHealth(p.getMaxHealth());

									}
									a.addPlayer(p);
								}
							}
							return;
						}

						if (Arenas.getArenas().size() > 1) {

							if (Bukkit.getOnlinePlayers().size() > 0) {
								for (Player p : Bukkit.getOnlinePlayers()) {
									if (p.isDead() || p.getHealth() == 0) {
										p.setHealth(p.getMaxHealth());

									}
									plugin.setUpForMultiMaps(p);
									plugin.bungee = null;
									plugin.point.clear();
									plugin.votes.clear();
									for (Arena a : Arenas.getArenas()) {
										plugin.point.put(a.getName(), 0);
									}
								}

							}
							new BukkitRunnable() {

								@Override
								public void run() {
									if (Bukkit.getOnlinePlayers().size() >= plugin.settings.getConfig()
											.getInt("min-players-to-start-bungee")) {
										plugin.startmap = false;
										plugin.StartMap();
										this.cancel();
										return;
									}

								}
							}.runTaskTimer(plugin, 20, 20);
						}
					}

				}
			}.runTaskLater(plugin, 60);
			reset();
			players.clear();
			specs.clear();

			if (persistentRoom) {
				this.owner = oldOwner;

				for (Player rp : replayPlayers) {
					if (rp == null) continue;
					if (!rp.isOnline()) continue;
					if (Arenas.isInArena(rp)) continue;

					addPlayer(rp);
				}

				if (oldOwner != null && replayPlayers.contains(oldOwner)) {
					this.owner = oldOwner;
				}
			}

		}

	}


	public String toPlayerString(ArrayList<Player> list) {

	    if (list == null || list.isEmpty()) return "";

	    StringBuilder sb = new StringBuilder();

	    for (int i = 0; i < list.size(); i++) {
	        sb.append(list.get(i).getName());

	        if (i < list.size() - 1) {
	            sb.append(", ");
	        }
	    }

	    return sb.toString();
	}

	private String getAliveWinnersText() {
		ArrayList<Player> alive = new ArrayList<Player>();
		for (Player p : this.players) {
			if (p != null && p.isOnline() && !this.specs.contains(p)) {
				alive.add(p);
			}
		}
		String result = toPlayerString(alive);
		return result == null || result.isEmpty() ? "Nenhum" : result;
	}

	private String getWinMessageKey(String type) {
		return new WinMessageManager(plugin).getMessageKey(this, type);
	}

	private void sendWinMessage(Player player, String type) {
		if (heros.size() > 0) {
			Hero = toPlayerString(heros);
		}
		new WinMessageManager(plugin).send(player, this, type, Murderer, Detective, Hero, getAliveWinnersText());
	}


    public boolean isRankedMode() {
        return getGameMode() == GameModeType.RANKED;
    }

    

    public RankedBorderManager getRankedBorderManager() {
        return rankedBorderManager;
    }

    public String getRankedZoneDisplay() {
        if (rankedBorderManager == null) return "§7Inativa";
        return rankedBorderManager.getZoneDisplay();
    }

    private void startRankedZoneIfNeeded() {
        if (plugin == null) return;

        if (!isRankedMode()) {
            if (plugin.getConfig().getBoolean("debug-ranked", false)) {
                Bukkit.getConsoleSender().sendMessage("§e[sMurder Ranked] Zona não iniciou: modo atual = " + getRoomModeNamePlain() + " | arena=" + getName());
            }
            return;
        }

        if (!plugin.getConfig().getBoolean("ranked-zone.enabled", true)) {
            Bukkit.getConsoleSender().sendMessage("§c[sMurder Ranked] Zona Ranked desativada na config: ranked-zone.enabled=false");
            return;
        }

        if (rankedBorderManager != null && rankedBorderManager.isActive()) {
            if (plugin.getConfig().getBoolean("debug-ranked", false)) {
                Bukkit.getConsoleSender().sendMessage("§e[sMurder Ranked] Zona já estava ativa para arena " + getName());
            }
            return;
        }

        rankedBorderManager = new RankedBorderManager(plugin, this);
        rankedBorderManager.start();

        Bukkit.getConsoleSender().sendMessage("§a[sMurder Ranked] Zona iniciada para arena " + getName()
                + " | modo=" + getRoomModeNamePlain()
                + " | players=" + players.size());
    }


    public void forceStartRankedZoneForDebug() {
        if (plugin == null) return;
        if (!isRankedMode()) {
            setGameMode(GameModeType.RANKED);
        }
        startRankedZoneIfNeeded();
    }

    private void stopRankedZone() {
        if (rankedBorderManager != null) {
            rankedBorderManager.stop();
            rankedBorderManager = null;
        }
    }

private void applyRankedResult(String type) {
        if (!isRankedMode() || plugin.rankedManager == null) return;

        boolean murdererWin = type != null && type.equalsIgnoreCase("m");

        ArrayList<Player> snapshotPlayers = new ArrayList<Player>();
        for (Player p : getPlayers()) {
            if (p != null && p.isOnline() && !snapshotPlayers.contains(p)) snapshotPlayers.add(p);
        }
        for (Player p : getSpectators()) {
            if (p != null && p.isOnline() && !snapshotPlayers.contains(p)) snapshotPlayers.add(p);
        }

        int murderWinRp = plugin.getConfig().getInt("ranked-v3.win-murderer-rp", 60);
        int detectiveWinRp = plugin.getConfig().getInt("ranked-v3.win-detective-rp", 45);
        int innocentWinRp = plugin.getConfig().getInt("ranked-v3.win-innocent-rp", 35);
        int murderLossRp = plugin.getConfig().getInt("ranked-v3.loss-murderer-rp", -18);
        int playerLossRp = plugin.getConfig().getInt("ranked-v3.loss-player-rp", -12);
        int killRp = plugin.getConfig().getInt("ranked-v3.kill-rp", 6);
        int maxKillBonus = plugin.getConfig().getInt("ranked-v3.max-kill-bonus", 24);
        int survivalBonus = plugin.getConfig().getInt("ranked-v3.survival-bonus-rp", 8);
        int heroBonus = plugin.getConfig().getInt("ranked-v3.hero-bonus-rp", 12);

        for (Player p : snapshotPlayers) {
            if (p == null || !p.isOnline()) continue;

            PlayerType role = getType(p);
            boolean won;
            int baseRp;

            if (murdererWin) {
                won = role == PlayerType.Murderer;
                baseRp = won ? murderWinRp : playerLossRp;
            } else {
                won = role != PlayerType.Murderer;
                if (won) {
                    baseRp = role == PlayerType.Detective ? detectiveWinRp : innocentWinRp;
                } else {
                    baseRp = murderLossRp;
                }
            }

            int kills = getkill(p);
            int killBonus = Math.min(maxKillBonus, Math.max(0, kills * killRp));
            boolean survived = !specs.contains(p);
            int bonus = killBonus + (won && survived ? survivalBonus : 0);

            if (heros.contains(p)) {
                bonus += heroBonus;
            }

            int totalRp = baseRp + bonus;
            plugin.rankedManager.recordMatch(p, won, kills, totalRp, "Resultado Ranked V3");
        }
    }

	public void win(String type) {
		if (!wincheck) {
			wincheck = true;
			applyRankedResult(type);
			new BukkitRunnable() {

				@Override
				public void run() {

					if (!type.equalsIgnoreCase("m")) {
						plugin.StartFireworksPlayers(getArena());

						for (Player p : getInnocents()) {

							if (plugin.getPlayerData(p) != null) {

								plugin.getPlayerData(p).addwins(1);
							}

							plugin.api.winreward(p);
						
						}

						for (Player p : getDetectives()) {

							if (plugin.getPlayerData(p) != null) {
								plugin.getPlayerData(p).addwins(1);

							}

							plugin.api.winreward(p);


						}

						for (Player p : getSpectators()) {

							if (plugin.getPlayerData(p) != null) {
								plugin.getPlayerData(p).addwins(1);

							}

							plugin.api.winreward(p);

						}
							
						for (Player p : getMurderers()) {

							if (plugin.getPlayerData(p) != null) {
								plugin.getPlayerData(p).addlose(1);

							}


					
							plugin.api.losereward(p);

						}

						if (heros.size() >= 0) {
							for (Player p : heros) {
								if (p != null) {
									plugin.api.heroreward(p);
								}

							}
						}

						for (Player x : getPlayers()) {
							sendWinMessage(x, type);
						}

						Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
							@Override
							public void run() {
								stop("reload");
							}
						}, plugin.settings.getConfig().getInt("stop-arena-after-win-time") * 20);
						return;
					}

					if (type.equalsIgnoreCase("m")) {
						plugin.StartFireworksMurder(getArena());
						for (Player p : getInnocents()) {

							if (plugin.getPlayerData(p) != null) {
								plugin.getPlayerData(p).addlose(1);

							}



							plugin.api.losereward(p);
						}
						for (Player p : getDetectives()) {
							if (plugin.getPlayerData(p) != null) {
								plugin.getPlayerData(p).addlose(1);

							}

							plugin.api.losereward(p);
						}

						for (Player p : getSpectators()) {
							if (plugin.getPlayerData(p) != null) {
								plugin.getPlayerData(p).addlose(1);

							}

							plugin.api.losereward(p);
						}
						for (Player p : getMurderers()) {
							if (plugin.getPlayerData(p) != null) {
								plugin.getPlayerData(p).addwins(1);

							}

							plugin.api.winreward(p);
						}
						if (heros.size() >= 0) {
							for (Player p : heros) {
								if (p != null) {
									plugin.api.heroreward(p);
								}

							}
						}
						for (Player x : getPlayers()) {
							sendWinMessage(x, type);
						}
						Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
							@Override
							public void run() {
								stop("reload");
							}
						}, plugin.settings.getConfig().getInt("stop-arena-after-win-time") * 20);
						return;
					}

				}

			}.runTaskLater(plugin, 20);

		}
	}

}
