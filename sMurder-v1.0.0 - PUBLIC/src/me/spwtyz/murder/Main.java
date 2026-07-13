package me.spwtyz.murder;

import java.util.*;
import java.lang.reflect.*;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import me.spwtyz.murder.api.MurderAPI;
import me.spwtyz.murder.builder.CosmeticsMenu;
import me.spwtyz.murder.builder.ItemEvent;
import me.spwtyz.murder.builder.Modes;
import me.spwtyz.murder.builder.ProfileMenu;
import me.spwtyz.murder.events.SignListener;
import me.spwtyz.murder.events.SignListener2;
import me.spwtyz.murder.kits.KitManager;
import me.spwtyz.murder.kits.MenuManager;
import me.spwtyz.murder.kits.SpeedKit;
import me.spwtyz.murder.kits.AdvancedKit;
import me.spwtyz.murder.kits.KitType;
import me.spwtyz.murder.knife.KnifeSkinManager;
import me.spwtyz.murder.hats.HatAbilityManager;
import me.spwtyz.murder.guns.GunSkinManager;
import me.spwtyz.murder.level.LevelManager;
import me.spwtyz.murder.level.LevelListener;
import me.spwtyz.murder.listeners.BlockEvents;
import me.spwtyz.murder.listeners.ChatEvent;
import me.spwtyz.murder.listeners.CustomHead;
import me.spwtyz.murder.listeners.DamageEvent;
import me.spwtyz.murder.listeners.DeathEvent;
import me.spwtyz.murder.listeners.DropItem;
import me.spwtyz.murder.listeners.EntityDamageByEntityEvent;
import me.spwtyz.murder.listeners.FoodLevel;
import me.spwtyz.murder.listeners.JoinEvent;
import me.spwtyz.murder.listeners.LeaveItem;
import me.spwtyz.murder.listeners.LoginEvent;
import me.spwtyz.murder.listeners.Motd;
import me.spwtyz.murder.listeners.NoPainting;
import me.spwtyz.murder.listeners.NoSpecDamage;
import me.spwtyz.murder.listeners.OpenVoteGUI;
import me.spwtyz.murder.listeners.PickUpEvent;
import me.spwtyz.murder.listeners.QuitEvent;
import me.spwtyz.murder.listeners.SpectateEvent;
import me.spwtyz.murder.listeners.SpectatorItem;
import me.spwtyz.murder.killcam.KillCamManager;
import me.spwtyz.murder.listeners.SwitchItem;
import me.spwtyz.murder.listeners.ThrowerEvent;
import me.spwtyz.murder.listeners.TntTagEvent;
import me.spwtyz.murder.listeners.VoteEvent;
import me.spwtyz.murder.listeners.WeatherBlock;
import me.spwtyz.murder.npcs.NPCManager;
import me.spwtyz.murder.objects.FragEvent;
import me.spwtyz.murder.objects.GunEvent;
import me.spwtyz.murder.objects.HidePlayers;
import me.spwtyz.murder.objects.SpeedEvent;
import me.spwtyz.murder.objects.Teleporter;
import me.spwtyz.murder.particles.BloodSystem;
import me.spwtyz.murder.particles.SnowballParticles;
import me.spwtyz.murder.rooms.RoomManager;
import me.spwtyz.murder.rooms.Room;
import me.spwtyz.murder.rooms.RoomFeatureLockManager;
import me.spwtyz.murder.rooms.RoomPasswordGUI;
import me.spwtyz.murder.sabotage.SabotageManager;
import me.spwtyz.murder.sabotage.SabotageTaskManager;
import me.spwtyz.murder.sabotage.AmongUsColorManager;
import me.spwtyz.murder.sabotage.AmongUsNameTagManager;
import me.spwtyz.murder.replay.ReplayManager;
import me.spwtyz.murder.scoreboard.ScoreboardManager;
import me.spwtyz.murder.scoreboard.ScoreboardType;
import me.spwtyz.murder.cosmeticnpcs.CosmeticNPCManager;
import me.spwtyz.murder.cosmetics.LobbyCosmeticManager;
import me.spwtyz.murder.cosmeticnpcs.CosmeticNPCListener;
import me.spwtyz.murder.mysterybox.MysteryBoxManager;
import me.spwtyz.murder.battlepass.BattlePassManager;
import me.spwtyz.murder.chat.TagManager;
import me.spwtyz.murder.chat.ChatChannelManager;
import me.spwtyz.murder.ranked.RankedManager;
import me.spwtyz.murder.leaderboard.HologramLeaderboardManager;
import me.spwtyz.murder.leaderboard.LeaderboardType;
import me.spwtyz.murder.lunar.LunarRichPresenceManager;
import me.spwtyz.murder.spectator.SpectatorManager;
import me.spwtyz.murder.titles.TitleManager;
import me.spwtyz.murder.seasonal.SeasonalEventManager;
import me.spwtyz.murder.seasonal.SeasonalTreasureManager;
import me.spwtyz.murder.seasonal.SeasonalEventType;



@SuppressWarnings("deprecation")
public class Main extends JavaPlugin implements Listener {
	public static Main instance;
	
	 public ArrayList<Item> itemList = new ArrayList<>();

	public static Main getInstance() {
		return instance;
	}


	public MySQL sql = null;

	public ArrayList<PlayerData> datalist = new ArrayList<>();
	
	ArrayList<Player> haveCooldowns = new ArrayList<Player>();
    
	public HashMap<String, PlayerData> pdata = new HashMap<>();

	public HashMap<String, ScoreboardManager> scoreboards = new HashMap<>();

	public HashMap<String, ScoreboardType> scorestate = new HashMap<>();
	public HashMap<String, Integer> cooldownTime;

	public HashMap<String, BukkitRunnable> cooldownTask;
	public RoomManager roomManager;
	public NPCManager npcManager;
	public ReplayManager replayManager;
	public SabotageManager sabotageManager;
	public SabotageTaskManager sabotageTaskManager;
	public AmongUsColorManager amongUsColorManager;
	public AmongUsNameTagManager amongUsNameTagManager;
	public MysteryBoxManager mysteryBoxManager;
	public BattlePassManager battlePassManager;
	public RankedManager rankedManager;
	public HologramLeaderboardManager leaderboardManager;
	public CosmeticNPCManager cosmeticNPCManager;
	public TagManager tagManager;
	public LunarRichPresenceManager lunarRichPresenceManager;
	public SpectatorManager spectatorManager;
	public KillCamManager killCamManager;
	public TitleManager titleManager;
	public SeasonalEventManager seasonalEventManager;
	public SeasonalTreasureManager seasonalTreasureManager;
	
	public MurderAPI api;

	public SignManager sm;
	public String user = "%%__USER__%%";
	public me.spwtyz.murder.configs.arenas arenas;
	public me.spwtyz.murder.configs.rewards rewards;
	public me.spwtyz.murder.configs.data data;
	public me.spwtyz.murder.configs.messages messages;
	public me.spwtyz.murder.configs.settings settings;
	public KitManager kitManager;
	public MenuManager menuManager;
	public KnifeSkinManager knifeSkinManager;
	public HatAbilityManager hatAbilityManager;
	public GunSkinManager gunSkinManager;
	public LevelManager levelManager;
	public boolean works = true;
	public String nmsver;
	public boolean useOldMethods = false;
	public ArrayList<String> bungeepl = new ArrayList<>();
	public ArrayList<Material> passable = new ArrayList<>();
	public ArrayList<Block> blocks = new ArrayList<>();
	public HashMap<String, Float> Pitch = new HashMap<>();
	public HashMap<String, Float> Yaw = new HashMap<>();
	public Map<UUID, Arena> playerArena = new HashMap<>();
	public Map<UUID, PlayerState> playerStates = new HashMap<>();
	public Set<UUID> knifeNoCooldownPlayers = new HashSet<>();

	public boolean hasKnifeNoCooldown(Player player) {
		return player != null && knifeNoCooldownPlayers.contains(player.getUniqueId());
	}

	public boolean toggleKnifeNoCooldown(Player player) {
		if (player == null) return false;
		UUID uuid = player.getUniqueId();
		if (knifeNoCooldownPlayers.contains(uuid)) {
			knifeNoCooldownPlayers.remove(uuid);
			return false;
		}
		knifeNoCooldownPlayers.add(uuid);
		return true;
	}

	public void setKnifeNoCooldown(Player player, boolean enabled) {
		if (player == null) return;
		if (enabled) knifeNoCooldownPlayers.add(player.getUniqueId());
		else knifeNoCooldownPlayers.remove(player.getUniqueId());
	}

	public PlayerState getPlayerState(Player player) {
		if (player == null) return PlayerState.MAIN_LOBBY;
		PlayerState state = playerStates.get(player.getUniqueId());
		return state == null ? PlayerState.MAIN_LOBBY : state;
	}

	public void setPlayerState(Player player, PlayerState state) {
		if (player == null) return;
		if (state == null) {
			playerStates.remove(player.getUniqueId());
			return;
		}
		playerStates.put(player.getUniqueId(), state);
		if (lunarRichPresenceManager != null) lunarRichPresenceManager.forceUpdate(player);
	}

	public boolean isMainLobby(Player player) {
		return getPlayerState(player) == PlayerState.MAIN_LOBBY;
	}

	public boolean isRoomLobby(Player player) {
		return getPlayerState(player) == PlayerState.ROOM_LOBBY;
	}

	public int getArenaMaxPlayers(Arena arena) {
		if (arena == null) return 0;
		if (arena.maxPlayers > 0) return arena.maxPlayers;
		return SpawnSizeByName(arena.getTemplateName());
	}

	public void cleanupDynamicRoomConfigKeys() {
		boolean changed = false;

		if (getConfig().contains("SpawnSize")) {
			for (String key : new ArrayList<String>(getConfig().getConfigurationSection("SpawnSize").getKeys(false))) {
				if (key != null && key.toUpperCase().startsWith("ROOM-")) {
					getConfig().set("SpawnSize." + key, null);
					changed = true;
				}
			}
		}

		if (getConfig().contains("ArenaMode")) {
			for (String key : new ArrayList<String>(getConfig().getConfigurationSection("ArenaMode").getKeys(false))) {
				if (key != null && key.toUpperCase().startsWith("ROOM-")) {
					getConfig().set("ArenaMode." + key, null);
					changed = true;
				}
			}
		}

		if (changed) {
			saveConfig();
			Bukkit.getConsoleSender().sendMessage("§a[sMurder] Chaves antigas ROOM-* removidas da config.");
		}
	}
	private HashMap<String, ItemStack[]> armourContents = new HashMap<>();
	private HashMap<String, ItemStack[]> inventoryContents = new HashMap<>();
	private HashMap<String, GameMode> gamemode = new HashMap<>();

	private HashMap<String, Integer> level = new HashMap<>();
	private HashMap<String, Float> xp = new HashMap<>();

	boolean startmap = false;

	public Arena bungee = null;

	public HashMap<String, String> votes = new HashMap<>();
	public HashMap<String, Integer> point = new HashMap<>();
	boolean disabled = false;

	public ArrayList<String> opened = new ArrayList<>();

	public HashMap<String, SmartInventory> sd = new HashMap<>();
	
	

	public void addGold(Player p, Arena a, int number) {

		arenas.getConfig().set("Gold." + a.getTemplateName() + "." + number + ".World", p.getWorld().getName());
		arenas.getConfig().set("Gold." + a.getTemplateName() + "." + number + ".x", Double.valueOf(p.getLocation().getX()));
		arenas.getConfig().set("Gold." + a.getTemplateName() + "." + number + ".y", Double.valueOf(p.getLocation().getY()));
		arenas.getConfig().set("Gold." + a.getTemplateName() + "." + number + ".z", Double.valueOf(p.getLocation().getZ()));
		arenas.getConfig().set("Gold." + a.getTemplateName() + "." + number + ".yaw", Double.valueOf(p.getLocation().getYaw()));
		arenas.getConfig().set("Gold." + a.getTemplateName() + "." + number + ".pitch",
				Double.valueOf(p.getLocation().getPitch()));

		arenas.save();
		p.sendMessage(Utils.FormatText(p, messages.getConfig().getString("add-gold-message")));
	}

	public void addPotion(Block b, Arena a) {
		List<String> c = arenas.getConfig().getStringList("Potions." + a.getTemplateName());
		if (c.contains(getStringFromLocation(b.getLocation()))) {
			return;
		}
		c.add(getStringFromLocation(b.getLocation()));
		arenas.getConfig().set("Potions." + a.getTemplateName(), c);
		arenas.save();

	}
    

	public void addSpawn(Player p, Arena a, int number) {

		arenas.getConfig().set("Spawns." + a.getTemplateName() + "." + number + ".World", p.getWorld().getName());
		arenas.getConfig().set("Spawns." + a.getTemplateName() + "." + number + ".x", Double.valueOf(p.getLocation().getX()));
		arenas.getConfig().set("Spawns." + a.getTemplateName() + "." + number + ".y", Double.valueOf(p.getLocation().getY()));
		arenas.getConfig().set("Spawns." + a.getTemplateName() + "." + number + ".z", Double.valueOf(p.getLocation().getZ()));
		arenas.getConfig().set("Spawns." + a.getTemplateName() + "." + number + ".yaw",
				Double.valueOf(p.getLocation().getYaw()));
		arenas.getConfig().set("Spawns." + a.getTemplateName() + "." + number + ".pitch",
				Double.valueOf(p.getLocation().getPitch()));

		arenas.save();
		p.sendMessage(Utils.FormatText(p, messages.getConfig().getString("add-spawn-message")));
		
	}
	

	public void canceltask(Integer sec, FlyingItems fl, Arena a, Block b, Player p, ItemStack i) {
		new BukkitRunnable() {

			@Override
			public void run() {
				if (!a.items.contains(fl)) {
					return;
				}
				if (a.items.contains(fl)) {
					a.items.remove(fl);
				}
				if (blocks.contains(b)) {
					blocks.remove(b);
				}
				if (fl != null) {
					fl.remove();
				}
				if (p.isOnline()) {
					p.getInventory().addItem(i);
				}
			}
		}.runTaskLater(this, 20 * sec);
	}

	public void ClosePreparedStatement(PreparedStatement s) {
		if (getConfig().getBoolean("close-prepared-statements-after-updating-data")) {
			new BukkitRunnable() {

				@Override
				public void run() {

					if (s != null) {
						try {
							s.close();
						} catch (SQLException e) {

							e.printStackTrace();
						}
					}

				}
			}.runTaskLater(this, 20 * getConfig().getInt("close-sql-statements-after"));
		}
	}

	public void CloseResultSet(ResultSet s) {
		if (getConfig().getBoolean("close-prepared-statements-after-updating-data")) {
			new BukkitRunnable() {

				@Override
				public void run() {

					if (s != null) {
						try {

							s.close();

						} catch (SQLException e) {

							e.printStackTrace();
						}
					}

				}
			}.runTaskLater(this, 20 * getConfig().getInt("close-sql-statements-after"));
		}
	}

	public void createarena(String name, Player p) {
		List<String> h = arenas.getConfig().getStringList("arena-list");
		if (h.contains(name)) {
			p.sendMessage(Utils.FormatText(p, messages.getConfig().getString("arena-already-exits")));
			return;
		}
		h.add(name);
		arenas.getConfig().set("arena-list", h);
		arenas.save();
		Arena arena = new Arena(name, this);
		Arenas.addArena(arena);
		p.sendMessage(Utils.FormatText(p, messages.getConfig().getString("arena-created-message")));
	}

	public void DropGold(Arena a) {

		// TNT_TAG e TODOS_ASSASSINOS nao usam fragmentos/ingots no mapa.
		if (a == null || a.getGameMode() == GameModeType.TNT_TAG ||  a.getGameMode() == GameModeType.HIDE_AND_SEEK || a.getGameMode() == GameModeType.ALL_MURDER) {
			return;
		}

		if (getRandomGold(a) != null) {
			Location loc = getRandomGold(a);

			for (Entity e : getNearbyEntities(loc,
					settings.getConfig().getInt("prevent-gold-dropping-near-other-gold-players-radius"))) {
				if (!e.getWorld().getName().equalsIgnoreCase(loc.getWorld().getName())) {

					return;
				}
				if (e.getType() == EntityType.DROPPED_ITEM) {

					return;
				}
				if (e.getType() == EntityType.PLAYER) {

					return;
				}
			}

			Item ax = getSpawn(a, 0).getWorld().dropItemNaturally(loc,
					new ItemStack(Material.getMaterial(settings.getConfig().getInt("dropped-item-id")), 1));
			ax.setVelocity(new Vector(0, 0, 0));
			a.golds.add(ax);
		}

	}

	// Player Stats
	public int getDeaths(Player p) {
		if (getPlayerData(p) != null) {
			return getPlayerData(p).getdeaths();
		}
		return 0;
	}

	public Location getGold(Arena a, int number) {
		if (arenas.getConfig().contains("Gold." + a.getTemplateName() + "." + number)) {
			World w = Bukkit.getWorld(arenas.getConfig().getString("Gold." + a.getTemplateName() + "." + number + ".World"));
			double x = arenas.getConfig().getDouble("Gold." + a.getTemplateName() + "." + number + ".x");
			double y = arenas.getConfig().getDouble("Gold." + a.getTemplateName() + "." + number + ".y");
			double z = arenas.getConfig().getDouble("Gold." + a.getTemplateName() + "." + number + ".z");
			float yaw = arenas.getConfig().getInt("Gold." + a.getTemplateName() + "." + number + ".yaw");
			float pitch = arenas.getConfig().getInt("Gold." + a.getTemplateName() + "." + number + ".pitch");

			return new Location(w, x, y, z, yaw, pitch);
		}
		return getGold(a, 0);
	}

	public String getHighestVote() {
		String highestMap = null;
		int highestVote = 0;
		for (Entry<String, Integer> entry : point.entrySet()) {
			if (entry.getValue() > highestVote) {
				highestMap = entry.getKey();
				highestVote = entry.getValue();
			}
		}
		return highestMap;
	}

	public int getKills(Player p) {

		if (getPlayerData(p) != null) {
			return getPlayerData(p).getkill();
		}
		return 0;
	}

	public Vector getLeftHeadDirection(ArmorStand e) {
		Vector direction = e.getLocation().getDirection().normalize();
		return new Vector(direction.getZ(), 0.0, -direction.getX()).normalize();
	}

	public Vector getLeftHeadDirection(Player player) {
		Vector direction = player.getLocation().getDirection().normalize();
		return new Vector(direction.getZ(), 0.0, -direction.getX()).normalize();
	}

	public Location getLobby() {
		World w = Bukkit.getWorld(arenas.getConfig().getString("Lobby.main.lobby.world"));
		double x = arenas.getConfig().getDouble("Lobby.main.lobby.x");
		double y = arenas.getConfig().getDouble("Lobby.main.lobby.y");
		double z = arenas.getConfig().getDouble("Lobby.main.lobby.z");
		float yaw = arenas.getConfig().getInt("Lobby.main.lobby.yaw");
		float pitch = arenas.getConfig().getInt("Lobby.main.lobby.pitch");
		return new Location(w, x, y, z, yaw, pitch);
	}

	public Location getLocationFromString(String str) {
		String str2loc[] = str.split("\\:");
		Location loc = new Location(getServer().getWorld(str2loc[0]), 0, 0, 0);
		loc.setX(Double.parseDouble(str2loc[1]));
		loc.setY(Double.parseDouble(str2loc[2]));
		loc.setZ(Double.parseDouble(str2loc[3]));
		return loc;
	}

	public int getLoses(Player p) {
		if (getPlayerData(p) != null) {
			return getPlayerData(p).getloses();
		}
		return 0;
	}

	public Entity[] getNearbyEntities(Location l, int radius) {
		int chunkRadius = radius < 16 ? 1 : (radius - (radius % 16)) / 16;
		HashSet<Entity> radiusEntities = new HashSet<>();
		for (int chX = 0 - chunkRadius; chX <= chunkRadius; chX++) {
			for (int chZ = 0 - chunkRadius; chZ <= chunkRadius; chZ++) {
				int x = (int) l.getX(), y = (int) l.getY(), z = (int) l.getZ();
				for (Entity e : new Location(l.getWorld(), x + (chX * 16), y, z + (chZ * 16)).getChunk()
						.getEntities()) {
					if (l.getWorld().getName().equalsIgnoreCase(e.getWorld().getName())) {
						if (e.getLocation().distance(l) <= radius && e.getLocation().getBlock() != l.getBlock()) {

							radiusEntities.add(e);
						}

					}
				}
			}
		}
		return radiusEntities.toArray(new Entity[radiusEntities.size()]);
	}

	public Double getNearestDouble(Player p, Double range) {

		double distance = Double.POSITIVE_INFINITY;

		@SuppressWarnings("unused")
		Player target = null;
		for (Entity e : p.getNearbyEntities(range, range, range)) {
			if (e.getType() == EntityType.PLAYER) {
				if ((Player) e != p) {
					double distanceto = p.getLocation().distance(e.getLocation());
					if (distanceto > distance)
						continue;
					distance = distanceto;
					target = (Player) e;
				}
			}
		}
		return distance;
	}

	public Player getNearestName(Player p, Double range) {
		double distance = Double.POSITIVE_INFINITY;

		Player target = null;
		for (Entity e : p.getNearbyEntities(range, range, range)) {
			if (e.getType() == EntityType.PLAYER) {
				if ((Player) e != p) {

					double distanceto = p.getLocation().distance(e.getLocation());
					if (distanceto > distance)
						continue;
					distance = distanceto;
					target = (Player) e;
				}
			}
		}
		return target;
	}

	public Player getNearestHideSeekHider(Player seeker, Arena arena) {
		if (seeker == null || arena == null) return null;

		double distance = Double.POSITIVE_INFINITY;
		Player target = null;

		for (Player hidden : arena.innocents) {
			if (hidden == null || !hidden.isOnline()) continue;
			if (hidden.equals(seeker)) continue;
			if (!arena.players.contains(hidden)) continue;
			if (arena.specs.contains(hidden)) continue;
			if (!hidden.getWorld().getName().equalsIgnoreCase(seeker.getWorld().getName())) continue;

			double dist = seeker.getLocation().distance(hidden.getLocation());
			if (dist < distance) {
				distance = dist;
				target = hidden;
			}
		}

		return target;
	}

	public PlayerData getPlayerData(Player p) {
		if (pdata.containsKey(p.getName())) {
			PlayerData data = pdata.get(p.getName());

			return data;

		}
		return null;
	}

	public PlayerData[] getPlayersData() {
		return datalist.toArray(new PlayerData[datalist.size()]);
	}

	public int getRandom(int lower, int upper) {
		Random random = new Random();
		return random.nextInt((upper - lower) + 1) + lower;
	}

	public Location getRandomGold(Arena a) {
		if (GoldSize(a) <= 0) {
			return null;
		}
		int random = getRandom(0, GoldSize(a));
		if (arenas.getConfig().contains("Spawns." + a.getTemplateName() + "." + random)) {
			return getGold(a, random);
		}
		return getGold(a, 0);
	}

	public Vector getRightHeadDirection(ArmorStand e) {
		Vector direction = e.getLocation().getDirection().normalize();
		return new Vector(-direction.getZ(), 0.0, direction.getX()).normalize();
	}

	public int getScore(Player p) {

		if (getPlayerData(p) != null) {
			return getPlayerData(p).getscore();
		}
		return 0;
	}

	public Location getSpawn(Arena a, int number) {
		if (arenas.getConfig().contains("Spawns." + a.getTemplateName() + "." + number)) {
			World w = Bukkit.getWorld(arenas.getConfig().getString("Spawns." + a.getTemplateName() + "." + number + ".World"));
			double x = arenas.getConfig().getDouble("Spawns." + a.getTemplateName() + "." + number + ".x");
			double y = arenas.getConfig().getDouble("Spawns." + a.getTemplateName() + "." + number + ".y");
			double z = arenas.getConfig().getDouble("Spawns." + a.getTemplateName() + "." + number + ".z");
			float yaw = arenas.getConfig().getInt("Spawns." + a.getTemplateName() + "." + number + ".yaw");
			float pitch = arenas.getConfig().getInt("Spawns." + a.getTemplateName() + "." + number + ".pitch");

			return new Location(w, x, y, z, yaw, pitch);
		}
		return getSpawn(a, 0);
	}

	public Location getSpec(Arena a) {
		if (a == null || a.getTemplateName() == null) {
			return getGlobalWait();
		}
		String path = "Spectator." + a.getTemplateName() + ".main.lobby.";
		String worldName = arenas.getConfig().getString(path + "world");
		if (worldName == null || worldName.trim().isEmpty()) {
			Location fallback = getGlobalWait();
			if (fallback != null) return fallback;
			try { return getSpawn(a, 0); } catch (Throwable ignored) {}
			return null;
		}
		World w = Bukkit.getWorld(worldName);
		if (w == null) {
			Location fallback = getGlobalWait();
			if (fallback != null) return fallback;
			try { return getSpawn(a, 0); } catch (Throwable ignored) {}
			return null;
		}
		double x = arenas.getConfig().getDouble(path + "x");
		double y = arenas.getConfig().getDouble(path + "y");
		double z = arenas.getConfig().getDouble(path + "z");
		float yaw = (float) arenas.getConfig().getDouble(path + "yaw");
		float pitch = (float) arenas.getConfig().getDouble(path + "pitch");
		return new Location(w, x, y, z, yaw, pitch);
	}

	public String getStringFromLocation(Location loc) {
		return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
	}

	public final Block getTargetBlock(Player player, int range) {
		BlockIterator iter = new BlockIterator(player, range);
		Block lastBlock = iter.next();
		while (iter.hasNext()) {
			lastBlock = iter.next();
			if (lastBlock.getType() == Material.AIR) {
				continue;
			}
			break;
		}
		return lastBlock;
	}

	public Location getWait(Arena a) {
		// Sistema novo: um waiting lobby global para todas as salas, modos e mapas.
		Location globalWait = getGlobalWait();
		if (globalWait != null) {
			return globalWait;
		}

		// Fallback antigo apenas para nao quebrar servidores que ainda nao usaram /m setwait novamente.
		if (a == null) {
			Bukkit.getConsoleSender().sendMessage("§c[sMurder] Waiting lobby global nao configurado. Use /m setwait.");
			return null;
		}

		String template = a.getTemplateName();
		if (template == null || template.trim().isEmpty()) {
			Bukkit.getConsoleSender().sendMessage("§c[sMurder] Waiting lobby global nao configurado e arena sem templateName: " + a.getName());
			return null;
		}

		String path = "Wait." + template + ".main.lobby.";
		String worldName = arenas.getConfig().getString(path + "world");
		if (worldName == null || worldName.trim().isEmpty()) {
			Bukkit.getConsoleSender().sendMessage("§c[sMurder] Waiting lobby global nao configurado. Use /m setwait.");
			return null;
		}

		World w = Bukkit.getWorld(worldName);
		if (w == null) {
			Bukkit.getConsoleSender().sendMessage("§c[sMurder] Mundo do wait lobby nao carregado: " + worldName);
			return null;
		}

		double x = arenas.getConfig().getDouble(path + "x");
		double y = arenas.getConfig().getDouble(path + "y");
		double z = arenas.getConfig().getDouble(path + "z");
		float yaw = (float) arenas.getConfig().getDouble(path + "yaw");
		float pitch = (float) arenas.getConfig().getDouble(path + "pitch");
		return new Location(w, x, y, z, yaw, pitch);
	}

	public Location getGlobalWait() {
		String path = "GlobalWait.main.lobby.";
		String worldName = arenas.getConfig().getString(path + "world");
		if (worldName == null || worldName.trim().isEmpty()) {
			return null;
		}
		World w = Bukkit.getWorld(worldName);
		if (w == null) {
			Bukkit.getConsoleSender().sendMessage("§c[sMurder] Mundo do waiting lobby global nao carregado: " + worldName);
			return null;
		}
		double x = arenas.getConfig().getDouble(path + "x");
		double y = arenas.getConfig().getDouble(path + "y");
		double z = arenas.getConfig().getDouble(path + "z");
		float yaw = (float) arenas.getConfig().getDouble(path + "yaw");
		float pitch = (float) arenas.getConfig().getDouble(path + "pitch");
		return new Location(w, x, y, z, yaw, pitch);
	}

	public int getWins(Player p) {
		if (getPlayerData(p) != null) {
			return getPlayerData(p).getwins();
		}
		return 0;
	}

	public int GoldSize(Arena a) {
		if (!arenas.getConfig().contains("Gold." + a.getTemplateName())) {
			return 0;
		}
		if (!arenas.getConfig().contains("Gold." + a.getTemplateName() + ".0")) {
			return 0;
		}
		return arenas.getConfig().getConfigurationSection("Gold." + a.getTemplateName()).getKeys(false).size();

	}

	public boolean hasPotion(Block b, Arena a) {
		List<String> list = arenas.getConfig().getStringList("Potions." + a.getTemplateName());

		return list.contains(getStringFromLocation(b.getLocation()));

	}

	public boolean isPlayerInDataBase(Player p) {
		Connection connection = sql.getConnection();
		try {
			PreparedStatement select = connection
					.prepareStatement("SELECT * FROM `MurderData` WHERE playername='" + p.getName() + "'");
			ResultSet result = select.executeQuery();
			if (result.next()) {

				CloseResultSet(result);
				ClosePreparedStatement(select);
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void LaunchFirework(Location loc) {
		ArrayList<Color> colors = new ArrayList<>();
		ArrayList<Color> fade = new ArrayList<>();
		List<String> lore = getConfig().getStringList("firework.colors");
		List<String> lore2 = getConfig().getStringList("firework.fade");

		for (String l : lore) {
			colors.add(Utils.getColor(l));
		}

		for (String l : lore2) {
			fade.add(Utils.getColor(l));
		}

		Firework f = loc.getWorld().spawn(loc.add(0.5, getConfig().getInt("firework.height"), 0.5), Firework.class);

		FireworkMeta fm = f.getFireworkMeta();
		fm.addEffect(FireworkEffect.builder().flicker(getConfig().getBoolean("firework.flicker"))
				.trail(getConfig().getBoolean("firework.trail"))
				.with(Type.valueOf(getConfig().getString("firework.type"))).withColor(colors).withFade(fade).build());

		fm.setPower(getConfig().getInt("firework.power"));

		f.setFireworkMeta(fm);
	}

	public void leave2(Player p) {

		if (!Arenas.isInArena(p)) {

			return;
		}
		Arena a = Arenas.getArena(p);
		if (!a.specs.contains(p)) {
			a.removePlayer(p, "leave");

		}

		if (a.specs.contains(p)) {

			setup(p);
			if (a.players.contains(p)) {
				a.players.remove(p);
			}
			if (a.specs.contains(p)) {
				a.specs.remove(p);
			}
			restoreInventory(p);
			Arenas.removeArena(p);
			if (!getConfig().getBoolean("send-to-server-on-leave")) {
				p.teleport(getLobby());
			}
			if (getConfig().getBoolean("send-to-server-on-leave")) {
				ByteArrayDataOutput out = ByteStreams.newDataOutput();
				out.writeUTF("Connect");
				out.writeUTF(getConfig().getString("lobby-server"));

				p.sendPluginMessage(this, "BungeeCord", out.toByteArray());
			}
			p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());

		}
	}

	public void loadarenas() {
		if (arenas.getConfig().contains("arena-list")) {
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "Carregando partidas..");
			for (String s : arenas.getConfig().getStringList("arena-list")) {
				Arena arena = new Arena(s, this);
				Arenas.addArena(arena);
			}
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.BLUE + "Partidas carregadas com sucesso!");

		}
	}


	public void LoadConfigFiles() {
		//Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "");
		getConfig().options().copyDefaults(true);
		saveConfig();
		getConfig().addDefault("close-prepared-statements-after-updating-data", true);
		getConfig().addDefault("close-sql-statements-after", 5);
		getConfig().addDefault("do-not-edit", false);
		getConfig().addDefault("bungee", false);
		getConfig().addDefault("send-to-server-on-leave", false);
		getConfig().addDefault("update-data-on-game-end", false);
		getConfig().addDefault("update-data-on-server-stop", true);
		getConfig().addDefault("update-data-on-player-quit", true);
		getConfig().addDefault("update-data-on-player-leave-arena", true);
		getConfig().addDefault("mysql", false);
		getConfig().addDefault("lobby-server", "lobby-server");
		getConfig().addDefault("LobbySystem.Enabled", true);
		getConfig().addDefault("LobbySystem.Amount", 1);
		getConfig().addDefault("LobbySystem.Lobbies.1.use-main-lobby", true);

		getConfig().addDefault("host", "localhost");
		getConfig().addDefault("port", "3306");
		getConfig().addDefault("database", "smurder");
		getConfig().addDefault("username", "root");
		getConfig().addDefault("password", "");

		getConfig().addDefault("remove-sword-after-time", 5);
		getConfig().addDefault("death-hologram-enabled", false);
		getConfig().addDefault("death-corpse-enabled", false);
		getConfig().addDefault("death-sound-enabled", true);
		getConfig().addDefault("debug-replay", true);
		getConfig().addDefault("debug-ranked", true);
		getConfig().addDefault("ranked-zone.enabled", true);
		getConfig().addDefault("ranked-zone.start-delay-seconds", 120);
		getConfig().addDefault("ranked-zone.initial-radius", 80);
		getConfig().addDefault("ranked-zone.min-size", 18);
		getConfig().addDefault("ranked-zone.shrink-interval-seconds", 10);
		getConfig().addDefault("ranked-zone.shrink-amount", 3);
		getConfig().addDefault("ranked-zone.damage", 1.0D);
		getConfig().addDefault("ranked-zone.visible", true);
		getConfig().addDefault("ranked-zone.visual-step", 4.0D);
		getConfig().addDefault("ranked-zone.visual-height", 3);
		getConfig().addDefault("ranked-zone.visual-every-seconds", 1);
		getConfig().addDefault("ranked.match-time-seconds", 600);
		getConfig().addDefault("ranked-killcam.enabled", false);
		getConfig().addDefault("ranked-killcam.seconds", 4);
		getConfig().addDefault("replay.enabled", false);
		getConfig().addDefault("tnt-tag.speed-cooldown-seconds", 18);
		getConfig().addDefault("tnt-tag.speed-duration-seconds", 5);
		getConfig().addDefault("tnt-tag.speed-amplifier", 1);
		getConfig().addDefault("hide-and-seek.maps", new ArrayList<String>());
		getConfig().addDefault("ranked-v3.win-murderer-rp", 60);
		getConfig().addDefault("ranked-v3.win-detective-rp", 45);
		getConfig().addDefault("ranked-v3.win-innocent-rp", 35);
		getConfig().addDefault("ranked-v3.loss-murderer-rp", -18);
		getConfig().addDefault("ranked-v3.loss-player-rp", -12);
		getConfig().addDefault("ranked-v3.kill-rp", 6);
		getConfig().addDefault("ranked-v3.max-kill-bonus", 24);
		getConfig().addDefault("ranked-v3.survival-bonus-rp", 8);
		getConfig().addDefault("ranked-v3.hero-bonus-rp", 12);
		getConfig().addDefault("leaderboard.update-seconds", 300);
		getConfig().addDefault("leaderboard.top-limit", 10);
		getConfig().addDefault("leaderboard.line-space", 0.27D);

		getConfig().addDefault("per-world-hide-player", false);
		List<String> h1x1x = getConfig().getStringList("hide-players-worlds");
		h1x1x.add("world");
		h1x1x.add("world2");
		getConfig().addDefault("hide-players-worlds", h1x1x);

		getConfig().options().copyDefaults(true);
		saveConfig();
		arenas = new me.spwtyz.murder.configs.arenas(new File(getDataFolder() + "/worlds/arenas.yml"));
		arenas.save();
		arenas.getConfig().options().copyDefaults(true);
		arenas.save();

		rewards = new me.spwtyz.murder.configs.rewards(new File(getDataFolder() + "/rewards/data.yml"));
		rewards.save();
		rewards.getConfig().options().copyDefaults(true);
		List<String> h1x = rewards.getConfig().getStringList("test.win-rewards.0");
		h1x.add("give %player% dirt");
		h1x.add("give %player% dirt");
		rewards.getConfig().addDefault("test.win-rewards.0", h1x);

		List<String> h1x1 = rewards.getConfig().getStringList("test.lose-rewards.0");
		h1x1.add("give %player% dirt");
		h1x1.add("give %player% dirt");
		rewards.getConfig().addDefault("test.lose-rewards.0", h1x1);

		rewards.save();

		data = new me.spwtyz.murder.configs.data(new File(getDataFolder() + "/data/data.yml"));
		data.save();
		data.getConfig().options().copyDefaults(true);
		data.save();

		settings = new me.spwtyz.murder.configs.settings(new File(getDataFolder() + "/settings/data.yml"));
		settings.save();

		settings.getConfig().addDefault("GUI.next-page", "&aProxima Pagina");
		settings.getConfig().addDefault("GUI.next-page-item-id", 160);
		settings.getConfig().addDefault("GUI.next-page-item-durability", 5);
		settings.getConfig().addDefault("GUI.previous-page", "&cPagina Anterior");
		settings.getConfig().addDefault("GUI.previous-page-item-id", 160);
		settings.getConfig().addDefault("GUI.previous-page-item-durability", 14);

		settings.getConfig().addDefault("enable-mystery-potions", true);
		settings.getConfig().addDefault("spectate-with-gamemode-3", true);

		settings.getConfig().addDefault("block-state-behind-signs", true);

		settings.getConfig().addDefault("block-behind-sign-ingame-state-id", 95);
		settings.getConfig().addDefault("block-behind-sign-ingame-state-durability", 14);

		settings.getConfig().addDefault("block-behind-sign-lobby-state-id", 95);
		settings.getConfig().addDefault("block-behind-sign-lobby-state-durability", 5);

		settings.getConfig().addDefault("block-behind-sign-starting-state-id", 95);
		settings.getConfig().addDefault("block-behind-sign-starting-state-durability", 4);

		settings.getConfig().addDefault("rejoin-interval", 3);
		settings.getConfig().addDefault("murder-sword-remove-height", 2);
		settings.getConfig().addDefault("throwing-sword-angle-rotation", 351);
		settings.getConfig().addDefault("death-messages", true);
		settings.getConfig().addDefault("prevent-gold-dropping-near-other-gold-players-radius", 3);

		List<String> v = settings.getConfig().getStringList("whitelisted-commands");
		v.add("/gm 1");
		v.add("/gm 3");
		v.add("/gm 0");
		v.add("/gamemode 1");
		v.add("/gamemode 3");
		v.add("/gamemode 0");
		
		settings.getConfig().addDefault("whitelisted-commands", v);

		List<String> v1 = settings.getConfig().getStringList("stats-board-world-whitelist");
		v1.add("world");
		settings.getConfig().addDefault("stats-board-world-whitelist", v1);

		List<String> v11 = settings.getConfig().getStringList("commands-world-whitelist");
		v11.add("world");
		settings.getConfig().addDefault("commands-world-whitelist", v11);

		settings.getConfig().addDefault("commands-whitelist", false);
		settings.getConfig().addDefault("board-whitelist", false);

		settings.getConfig().addDefault("arenas-inventory-size", 54);

		settings.getConfig().addDefault("arenas-inventory-title", "Partidas");

		settings.getConfig().addDefault("arenas-lobby-state-item-id", 160);
		settings.getConfig().addDefault("arenas-lobby-state-item-data", 5);
		settings.getConfig().addDefault("arenas-lobby-state-item-name", "&a%arena%");

		ArrayList<String> lore = new ArrayList<>();

		lore.add("&eJogadores: &a%players%/%max%");
		lore.add("&eMapa: &a%map%");
		lore.add("&eStatus: &a%state%");
		lore.add("&eModo: &a%mode%");
		lore.add("&eLider: &a%leader%");
		lore.add("");
		lore.add("&aClique para jogar");

		settings.getConfig().addDefault("arenas-lobby-state-item-lore", lore);

		settings.getConfig().addDefault("frame-item-id", 160);
		settings.getConfig().addDefault("frame-item-data", 15);

		settings.getConfig().addDefault("arenas-starting-state-item-id", 160);
		settings.getConfig().addDefault("arenas-starting-state-item-data", 4);
		settings.getConfig().addDefault("arenas-starting-state-item-name", "&6%arena%");
		ArrayList<String> lore1 = new ArrayList<>();

		lore1.add("&eJogadores: &a%players%/%max%");
		lore1.add("&eMapa: &a%map%");
		lore1.add("&eStatus: &a%state%");
		lore1.add("&eModo: &a%mode%");
		lore1.add("&eLider: &a%leader%");
		lore1.add("");
		lore1.add("&aClique para jogar");
		settings.getConfig().addDefault("arenas-starting-state-item-lore", lore1);

		settings.getConfig().addDefault("arenas-ingame-state-item-id", 160);
		settings.getConfig().addDefault("arenas-ingame-state-item-data", 14);
		settings.getConfig().addDefault("arenas-ingame-state-item-name", "&c%arena%");

		ArrayList<String> lore11 = new ArrayList<>();

		lore11.add("&eJogadores: &a%players%/%max%");
		lore11.add("&eMapa: &a%map%");
		lore11.add("&eStatus: &a%state%");
		lore11.add("&eModo: &a%mode%");
		lore11.add("&eLider: &a%leader%");
		lore11.add("");
		lore11.add("&aClique para jogar");
		settings.getConfig().addDefault("arenas-ingame-state-item-lore", lore11);

		settings.getConfig().addDefault("stats-board", true);
		settings.getConfig().addDefault("no-fall-damage", true);
		settings.getConfig().addDefault("tp-lobby-on-join", true);
		settings.getConfig().addDefault("per-arena-chat", true);
		settings.getConfig().addDefault("send-stats-message-on-leave", false);

		settings.getConfig().addDefault("win-rewards", false);
		settings.getConfig().addDefault("hero-rewards", false);
		settings.getConfig().addDefault("lose-rewards", false);

		settings.getConfig().addDefault("start-fireworks-on-players-location", true);
		settings.getConfig().addDefault("show-potion-name-in-floating-item", true);

		settings.getConfig().addDefault("score-on-kill", 3);
		settings.getConfig().addDefault("score-on-gold", 1);

		settings.getConfig().addDefault("time-until-game-start", 5);
		settings.getConfig().addDefault("give-spectate-item-after-ticks", 5);
		settings.getConfig().addDefault("stop-arena-after-win-time", 5);
		settings.getConfig().addDefault("fireworks-ticks", 5);
		settings.getConfig().addDefault("fireworks-time-in-ticks", 10);
		settings.getConfig().addDefault("vote-time", 15);
		settings.getConfig().addDefault("vote-inventory.size", 36);

		settings.getConfig().addDefault("vote-inventory.name", "Votando");
		settings.getConfig().addDefault("map-displayname-in-gui-color", "GREEN");
		settings.getConfig().addDefault("map-item-lore", "&eVotos: &b%votes%");
		settings.getConfig().addDefault("vote-message", "&a[Murder] &eVoce votou no mapa &7%map%&e!");

		settings.getConfig().addDefault("gun-delay", true);
		settings.getConfig().addDefault("gun-delay-seconds", 3);
		settings.getConfig().addDefault("gun-pickup-radius", 2);

		settings.getConfig().addDefault("map.item-id", 339);
		settings.getConfig().addDefault("map.item-subid", 0);
		settings.getConfig().addDefault("map.item-name", "&aMapas");
		settings.getConfig().addDefault("map.item-lore", "&fClique para selecionar um mapa");

		settings.getConfig().addDefault("tracking-compass", false);
		settings.getConfig().addDefault("time-to-give-tracker", 0);
		settings.getConfig().addDefault("murder-track.item-id", 345);
		settings.getConfig().addDefault("murder-track.item-subid", 0);
		settings.getConfig().addDefault("murder-track.item-name", "&bLocalizador");
		settings.getConfig().addDefault("murder-track.item-lore", "&fLocalize Jogadores");
		settings.getConfig().addDefault("murder-track.range", 100);

		//settings.getConfig().addDefault("track.item-id", 166);
		//settings.getConfig().addDefault("track.item-subid", 0);
		//settings.getConfig().addDefault("track.item-name", "&aLocalizar Pistola");
		//settings.getConfig().addDefault("track.item-lore", "&fLocalizar Pistola");

		settings.getConfig().addDefault("quit.item-id", 166);
		settings.getConfig().addDefault("quit.item-subid", 1);
		settings.getConfig().addDefault("quit.item-name", "&cSair");
		settings.getConfig().addDefault("quit.item-lore", "&fClique aqui para sair da partida!");

		settings.getConfig().addDefault("quit2.item-id", 166);
		settings.getConfig().addDefault("quit2.item-subid", 1);
		settings.getConfig().addDefault("quit2.item-name", "&cSair");
		settings.getConfig().addDefault("quit2.item-lore", "&fClique aqui para sair da partida!");

		settings.getConfig().addDefault("quit3.item-id", 166);
		settings.getConfig().addDefault("quit3.item-subid", 1);
		settings.getConfig().addDefault("quit3.item-name", "&cSair");
		settings.getConfig().addDefault("quit3.item-lore", "&fClique aqui para sair da partida!");

		settings.getConfig().addDefault("spectate-inventory-size", 18);
		settings.getConfig().addDefault("spectate-inventory-title", "Espectar Jogadores");
		settings.getConfig().addDefault("Spectate-Display-Name-Color", "RED");
		settings.getConfig().addDefault("Spectate-Display-Lore", "&fClique para teleportar para os jogadores!");
		settings.getConfig().addDefault("spec.item-name", "&aJogadores");
		settings.getConfig().addDefault("spec.item-id", 345);
		settings.getConfig().addDefault("spec.item-subid", 0);

		settings.getConfig().addDefault("rejoin.item-name", "&eNova Partida &7(Clique)");
		settings.getConfig().addDefault("rejoin.item-id", 339);
		settings.getConfig().addDefault("rejoin.item-subid", 0);

		settings.getConfig().addDefault("enable-sword-throw", true);
		settings.getConfig().addDefault("throw-sword-damage-radius", 1);
		settings.getConfig().addDefault("throw-sword-cooldown", 7);
		settings.getConfig().addDefault("sword-throw-speed", 3);
		settings.getConfig().addDefault("receive-sword-after", 0);

		settings.getConfig().addDefault("murderer-weapon.item-id", 276);
		settings.getConfig().addDefault("murderer-weapon.item-subid", 0);
		settings.getConfig().addDefault("murderer-weapon.item-name", "&6&lMURDER> &bFaca &7(&a1&7)");
		settings.getConfig().addDefault("murderer-weapon.item-lore", "&cMate todos os jogadores!");

		settings.getConfig().addDefault("min-players-to-start-bungee", 3);
		settings.getConfig().addDefault("countdown", 15);
		settings.getConfig().addDefault("gold-drop-interval", 20);
		settings.getConfig().addDefault("gold-amount-to-get-bow", 9);
		settings.getConfig().addDefault("gold-amount-to-get-potion", 2);
		settings.getConfig().addDefault("dropped-item-id", 265);

		settings.getConfig().addDefault("enable-sounds", true);
		settings.getConfig().addDefault("COUNT_DOWN_SOUND", "CLICK");
		settings.getConfig().addDefault("KILL_SOUND", "ORB_PICKUP");
		settings.getConfig().addDefault("PICK_UP", "CHICKEN_EGG_POP");
		settings.getConfig().addDefault("rejoin-option", false);
		settings.getConfig().addDefault("scoreboard-update-interval", 10);

		settings.getConfig().options().copyDefaults(true);
		settings.save();

		messages = new me.spwtyz.murder.configs.messages(new File(getDataFolder() + "/language/messages.yml"));
		messages.save();

		messages.getConfig().addDefault("potion-title", "&b%type% Pocao!");
		messages.getConfig().addDefault("potion-subtitle", "&ePor %time% segundos!");

		messages.getConfig().addDefault("progress-bar-1", "&cRecarregando...");
		messages.getConfig().addDefault("progress-bar-2", "&cRecarregando...");
		messages.getConfig().addDefault("sword-actionbar-cooldown", "&cRecarregando...");
		messages.getConfig().addDefault("sword-can-use-again", "&aRecarregada!");
		
		//messages.getConfig().addDefault("throw-cooldown", "&6&lMURDER> &cRecarregando...");
		//messages.getConfig().addDefault("bow-cooldown", "&6&lMURDER> &cRecarregando...");
		
		messages.getConfig().addDefault("bow-actionbar-cooldown", "&cRecarregando...");
		messages.getConfig().addDefault("bow-can-use-again", "&aRecarregada!");

		messages.getConfig().addDefault("stats-reset-error",
				"&7Erro! Nao foi possivel resetar suas estatisticas.");
		messages.getConfig().addDefault("stats-reset-message", "&6&lMURDER> &eEstatisticas resetadas com sucesso!");
		messages.getConfig().addDefault("bow-dropped", "");
		messages.getConfig().addDefault("bow-not-dropped", "");
		messages.getConfig().addDefault("kick-message", "&6&lMURDER> &cVoce saiu da partida.");
		messages.getConfig().addDefault("vote-scoreboard-title", "&aMap Voting");
		messages.getConfig().addDefault("scoreboard-map", "&e%map% &b(%votes%)");
		messages.getConfig().addDefault("voting-time-started",
				"&6&lMURDER> &eEscolha seu mapa favorito!");
		messages.getConfig().addDefault("vote-win",
				"&6&lMURDER> &eO mapa escolhido foi &b%map% &ePartida inicia em alguns segundos.");

		messages.getConfig().addDefault("vote-error-perm",
				"&6&lMURDER> &7Sem permissao para votar!");
		messages.getConfig().addDefault("vote-error",
				"&6&lMURDER> &7O mapa ja foi escolhido, nao e mais possivel escolher um mapa!");
		messages.getConfig().addDefault("gold-reason", "Por pegar scrap");
		messages.getConfig().addDefault("kill-reason", "Por matar um jogador");
		messages.getConfig().addDefault("receive-score-message", "&d+&e%score% &acoins (%reason%)!");
		messages.getConfig().addDefault("spec-chat-prefix", "&eMORTO: &f");
		messages.getConfig().addDefault("chat-format", "%tag%&7%player%: &f%message%");
		messages.getConfig().addDefault("murder-role", "Assassino");
		messages.getConfig().addDefault("detective-role", "Detetive");
		messages.getConfig().addDefault("innocent-role", "Inocente");
		messages.getConfig().addDefault("dead-role", "Morto");
		messages.getConfig().addDefault("death-title", "&cVOCE MORREU");
		messages.getConfig().addDefault("death-subtitle", "&eMorto por: &c%killer%");
		messages.getConfig().addDefault("murder-receive-sword-message",
				"");
		messages.getConfig().addDefault("bow-location-message", "");
		messages.getConfig().addDefault("near-player-location-message",
				"");

		messages.getConfig().addDefault("title-countdown", "&cINICIANDO EM");
		messages.getConfig().addDefault("subtitle-countdown", "&e%time% &esegundos!");

		messages.getConfig().addDefault("you-are-murderer-title", "&cASSASSINO!");
		messages.getConfig().addDefault("you-are-murderer-subtitle", "&eMate todos os jogadores!");

		messages.getConfig().addDefault("you-are-detective-title", "&bDETETIVE!");
		messages.getConfig().addDefault("you-are-detective-subtitle", "&eMate o assassino!");

		messages.getConfig().addDefault("you-have-bow-title", "");
		messages.getConfig().addDefault("you-have-bow-subtitle", "");

		messages.getConfig().addDefault("you-are-innocent-title", "&aINOCENTE!");
		messages.getConfig().addDefault("you-are-innocent-subtitle",
				"&eColete scrap e mate o assassino!");
		messages.getConfig().addDefault("countdown", "&6&lMURDER> &ePartida inicia em %time% segundos!");
		messages.getConfig().addDefault("join-error",
				"&6&lMURDER> &7A partida ja iniciou, Voce pode assistir a partida utilizando /watch <user>!");
		messages.getConfig().addDefault("already-in-arena", "&7Voce ja esta em uma partida!");

		messages.getConfig().addDefault("arena-full", "&6&lMURDER> &7Esta partida esta cheia!");
		messages.getConfig().addDefault("no-arenas", "&6&lMURDER> &7Nenhuma partida disponivel no momento!");
		messages.getConfig().addDefault("no-arenas-2", "&6&lMURDER> &7Esta partida nao esta disponivel no momento!");
		messages.getConfig().addDefault("player-join-arena-message",
				"&6&lMURDER> &e%player% &7Entrou na partida &b(%min%/%max%)&e!");
		messages.getConfig().addDefault("player-leave-arena-message",
				"&6&lMURDER> &7%player% &eSaiu da partida&7!");
		messages.getConfig().addDefault("ingame-motd", "&cEM-JOGO");
		messages.getConfig().addDefault("lobby-motd", "&aAGUARDANDO");
		messages.getConfig().addDefault("starting-motd", "&cINICIANDO");
		messages.getConfig().addDefault("voting-motd", "&dVOTACAO");
		messages.getConfig().addDefault("game-soon-start-message", "&6&lMURDER> &ePartida iniciando em breve!");

		messages.getConfig().addDefault("cancel",
				"&6&lMURDER> &7Partida cancelada, por nao conter jogadores suficientes.");
		messages.getConfig().addDefault("not-in-arena", "&6&lMURDER> &7Voce nao esta em uma partida!");
		messages.getConfig().addDefault("spectate-message", "&6&lMURDER> &eVoce e espectador agora!");
		messages.getConfig().addDefault("spectate-message2",
				"&6&lMURDER> &eVoce espectador agora, caso queira sair digite /leave!");
		messages.getConfig().addDefault("lobby-set-message", "&eLobby principal setado com sucesso!");
		messages.getConfig().addDefault("death-message", "&7%player% &eMorreu!");
		messages.getConfig().addDefault("detective-die", "");
		messages.getConfig().addDefault("arena-created-message", "&6&lMURDER> &ePartida criada com  sucesso!");
		messages.getConfig().addDefault("arena-already-exits", "&6&lMURDER> &ePartida ja existe!");

		messages.getConfig().addDefault("remove-arena-message", "&6&lMURDER> &ePartida foi removida com sucesso!");
		messages.getConfig().addDefault("arena-not-exits", "&6&lMURDER> &7Partida nao existe!");
		messages.getConfig().addDefault("wait-lobby-set-message", "&6&lMURDER> &eLobby de espera setado com sucesso!");
		messages.getConfig().addDefault("add-potion", "&6&lMURDER> &aAdicionado com sucesso bloco de pocoes!");
		messages.getConfig().addDefault("remove-potion", "&6&lMURDER> &cRemovido com sucesso bloco de pocoes!");
		messages.getConfig().addDefault("potion-use-error", "&6&lMURDER> &7Bloco de pocoes ja esta em uso.");
		messages.getConfig().addDefault("potion-use-error-2",
				"&6&lMURDER> &7Voce nao tem fragmentos suficientes.");
		messages.getConfig().addDefault("potion-use-message", "");
		messages.getConfig().addDefault("spectate-set-message", "&6&lMURDER> &eLocalizacao do espectador setado com sucesso.");
		messages.getConfig().addDefault("not-online", "&6&lMURDER> &cEste jogador nao esta online!");

		messages.getConfig().addDefault("add-spawn-message", "&6&lMURDER> &eLocalizacao do spawn do jogador setada!");

		messages.getConfig().addDefault("add-gold-message", "&6&lMURDER> &eLocalizacao do spawn de scrap setada!");

		messages.getConfig().addDefault("lobby-scoreboard-title", "&6&lMURDER");
		messages.getConfig().addDefault("wait-scoreboard-title", "&6&lMURDER");
		messages.getConfig().addDefault("countdown-scoreboard-title", "&6&lMURDER");
		messages.getConfig().addDefault("ingame-scoreboard-title", "&6&lMURDER");
		messages.getConfig().addDefault("stats-scoreboard-title", "&6&lMURDER");

		messages.getConfig().addDefault("sign-header", "&aMurder");
		messages.getConfig().addDefault("sign-ingame", "&eEm-Jogo");
		messages.getConfig().addDefault("sign-lobby", "&eLobby");
		messages.getConfig().addDefault("sign-starting", "&eIniciando");
		messages.getConfig().addDefault("sign-arena", "&e%arena%");
		messages.getConfig().addDefault("players", "&emin/max");

		messages.getConfig().addDefault("auto-join-sign-line-1", "&aMurder");
		messages.getConfig().addDefault("auto-join-sign-line-2", "&7Clique para entrar");
		messages.getConfig().addDefault("auto-join-sign-line-3", "&7Aleatoria");
		messages.getConfig().addDefault("auto-join-sign-line-4", "");

		messages.getConfig().addDefault("won-the-game-title", "&a&lVITORIA");
		messages.getConfig().addDefault("won-the-game-subtitle", "&fVoce venceu a partida!");
		
		messages.getConfig().addDefault("lost-the-game-title", "&c&lPERDEU");
		messages.getConfig().addDefault("lost-the-game-title", "&7Voce perdeu a partida!");
		
		
		messages.getConfig().addDefault("pickup-bow", " ");

		//List<String> z = messages.getConfig().getStringList("murder-game-start-message");
		//z.add("");
		//z.add("&6sMurder &7- &9PARTIDA");
		//z.add("");
		//z.add("&aINICIADA");
		//z.add("&eMapa: &f%map%");
		//z.add("&eTempo Restante: %time%");
		//z.add("&eJogadores: &f%players%");
		//z.add("&eData: &f%date%");
		//z.add("&eAssassinos: &f%mamount%");
		//z.add("&eDetetives: &f%damount%");
		//z.add("&eInocentes: &f%iamount%");
		//messages.getConfig().addDefault("murder-game-start-message", z);

		List<String> xx = messages.getConfig().getStringList("murder-help-message");

		xx.add("&6&lsMURDER &7> &ev1.0.2");
		xx.add("");
		xx.add("&7• &6/join <arena> &8┃ &7Entrar em uma partida.");
		xx.add("&7• &6/leave &8┃ &7Sair de uma partida.");
		xx.add("&7• &6/m ver &8┃ &7Ver suas estatisticas.");
		xx.add("&7• &6/m iniciar &c(YT) &8┃ &7Iniciar partida.");
		xx.add("");

		messages.getConfig().addDefault("murder-help-message", xx);

		List<String> info = messages.getConfig().getStringList("self-info-message");

		info.add("&6&lMURDER &7&l- &a&lSEU PERFIL");
		info.add("&b");
		info.add("&eNome: &e%player%");
		info.add("&eVitorias: &e%wins%");
		info.add("&ePerdidas: &e%loses%");
		info.add("&eAbates: &e%kills%");
		info.add("&eMortes: &e%deaths%");
		info.add("&eCoins: &e%score%");
		info.add("&b");
		messages.getConfig().addDefault("self-info-message", info);

		List<String> info1 = messages.getConfig().getStringList("other-info-message");

		info1.add("&6&lMURDER &7&l- &a&lPERFIL %player%");
		info1.add("&b");
		info1.add("&eNome: &e%player%");
		info1.add("&eVitorias: &e%wins%");
		info1.add("&ePerdidas: &e%loses%");
		info1.add("&eAbates: &e%kills%");
		info1.add("&eMortes: &e%deaths%");
		info1.add("&eCoins: &e%score%");
		info1.add("&b");
		messages.getConfig().addDefault("other-info-message", info1);

		List<String> h1 = messages.getConfig().getStringList("wait-scoreboard-lines");
		h1.add("&r");

		h1.add("  &a■ &fSala: &e%room%");
		h1.add("  &a■ &fMapa: &7%map%");
		h1.add("  &a■ &fJogadores: &b%size%/%max%");
		h1.add("  &a■ &fModo: &e%mode%");
		h1.add("  &a■ &fEvento: %event%");
		h1.add("  &a■ &fTesouros: %treasures%");
		h1.add("  &a■ &fLíder: &e%leader%");
		h1.add("&r&r");
		h1.add("  &aAguardando Jogadores...");
		h1.add("&r&r&r");
		h1.add("  &a■ $eKit: $fNenhum");
		h1.add("  &a■ &fNível: &e%level%");
		h1.add("  &a■ &fXP: &b%xp%");
		h1.add("  &a■ $eCoins: $f0");
		h1.add("&r&r&r");
		h1.add("  &7the-hive.app");
		messages.getConfig().addDefault("wait-scoreboard-lines", h1);

		List<String> h11 = messages.getConfig().getStringList("countdown-scoreboard-lines");
		h11.add("&r");
		h11.add("  &a■ &fSala: &e%room%");
		h11.add("  &a■ &fMapa: &7%map%");
		h11.add("  &a■ &fJogadores: &b%size%/%max%");
		h11.add("  &a■ &fModo: &e%mode%");
		h11.add("  &a■ &fEvento: %event%");
		h11.add("  &a■ &fTesouros: %treasures%");
		h11.add("  &a■ &fLíder: &e%leader%");
		h11.add("&r&r");
		h11.add(" &eIniciando: &b%countdown%s");
		h11.add(" &fTemporizador: &e%timer_status%");
		h11.add("&r&r&r");
		h11.add("  &a■ &fKit: Nenhum");
		h11.add("  &a■ &fNível: &e%level%");
		h11.add("  &a■ &fXP: &b%xp%");
		h11.add("  &a■ &fCoins: 0");
		h11.add("&r&r&r");
		h11.add("  &7the-hive.app");
		messages.getConfig().addDefault("countdown-scoreboard-lines", h11);

		List<String> h111 = messages.getConfig().getStringList("ingame-scoreboard-lines");
		h111.add("&r&r");
		h111.add("  &a■ &fTempo&7: %time%");
		h111.add("  &a■ &fInocentes Vivos: &7%innocents%");
		h111.add("&r&r&r&r");
		h111.add("  &a■ &fEvento: %event%");
		h111.add("  &a■ &fTesouros: %treasures%");
		h111.add("  &a■ &fAbates: &7%kills%");
		h111.add("  &a■ &fCoins&7: %score%");
		h111.add("  &a■ &fNível: &e%level%");
		h111.add("  &a■ &fXP: &b%xp%");
		h111.add("&r&r&r&r&r");
		h111.add("  &7the-hive.app");
		messages.getConfig().addDefault("ingame-scoreboard-lines", h111);

		List<String> tntBoard = messages.getConfig().getStringList("tnttag-scoreboard-lines");
		tntBoard.add("&r&r");
		tntBoard.add("  &c■ &fModo&7: &cTNT Tag");
		tntBoard.add("  &c■ &fTempo TNT&7: &e%tnt_time%s");
		tntBoard.add("  &c■ &fCom TNT&7: &c%tnt_holder%");
		tntBoard.add("&r&r&r");
		tntBoard.add("  &a■ &fVivos&7: &a%alive%");
		tntBoard.add("  &7Passe a TNT batendo em alguém");
		tntBoard.add("&r&r&r&r");
		tntBoard.add("  &7the-hive.app");
		messages.getConfig().addDefault("tnttag-scoreboard-lines", tntBoard);

		List<String> rankedBoard = messages.getConfig().getStringList("ranked-scoreboard-lines");
		rankedBoard.add("&r&r");
		rankedBoard.add("  &6* &fModo&7: &6Ranked");
		rankedBoard.add("  &6* &fMapa&7: &a%map%");
		rankedBoard.add("  &6* &fTempo&7: &e%time%");
		rankedBoard.add("  &6* &fZona&7: %zone%");
		rankedBoard.add("&r&r&r");
		rankedBoard.add("  &b* &fRP&7: &b%ranked_rp%");
		rankedBoard.add("  &b* &fRank&7: %ranked_rank%");
		rankedBoard.add("&r&r&r&r");
		rankedBoard.add("  &c* &fCargo&7: &e%role%");
		rankedBoard.add("  &c* &fKills&7: &c%kills%");
		rankedBoard.add("  &a* &fVivos&7: &a%alive%");
		rankedBoard.add("&r&r&r&r&r");
		rankedBoard.add("  &7the-hive.app");
		messages.getConfig().addDefault("ranked-scoreboard-lines", rankedBoard);

		List<String> hideBoard = messages.getConfig().getStringList("hideandseek-scoreboard-lines");
		hideBoard.add("&r&r");
		hideBoard.add("  &d■ &fMapa&7: &a%map%");
		hideBoard.add("  &d■ &fStatus&7: %hide_status%");
		hideBoard.add("  &d■ &fTempo&7: &e%time%");
		hideBoard.add("&r&r&r");
		hideBoard.add("  &a■ &fEscondidos&7: &a%hiders%");
		hideBoard.add("  &c■ &fProcuradores&7: &c%seekers%");
		hideBoard.add("  &e■ &fVocê&7: %hide_role%");
		hideBoard.add("&r&r&r&r");
		hideBoard.add("  &7Se for pego vira Procurador");
		hideBoard.add("&r&r&r&r&r");
		hideBoard.add("  &7the-hive.app");
		messages.getConfig().addDefault("hideandseek-scoreboard-lines", hideBoard);

		List<String> sabotageBoard = messages.getConfig().getStringList("sabotage-scoreboard-lines");
		sabotageBoard.add("&r&r");
		sabotageBoard.add("  &5■ &fModo&7: &5AMONG US");
		sabotageBoard.add("  &5■ &fMapa&7: &a%map%");
		sabotageBoard.add("  &5■ &fTempo&7: &e%time%");
		sabotageBoard.add("&r&r&r");
		sabotageBoard.add("  &a■ &fProgresso&7: &a%tasks_percent%%");
		sabotageBoard.add("&eTask: %sabotage_tasks%");
		sabotageBoard.add("&r&r&r&r");
		sabotageBoard.add("  &c■ &fCorpos&7: &c%sabotage_corpses%");
		sabotageBoard.add("  &d■ &fReunião&7: &d%sabotage_meeting_time%s");
		sabotageBoard.add("  &b■ &fCargo&7: &b%role%");
		sabotageBoard.add("&r&r&r&r&r");
		sabotageBoard.add("  &7Descubra o Murder");
		sabotageBoard.add("  &7altamc.com.br");
		messages.getConfig().addDefault("sabotage-scoreboard-lines", sabotageBoard);

		List<String> h1111 = messages.getConfig().getStringList("stats-scoreboard-lines");
		h1111.add("");
		h1111.add(" &eInfo:");
		h1111.add(" Cargo: &7Membro");
		h1111.add("");
		h1111.add("  &a■ &fAbates&7 → &7%kills%");
		h1111.add("  &a■ &fMortes&7 → &7%deaths%");
		h1111.add(" &r&r&r");
		h1111.add("  &a■ &fGanhou&7 → &7%wins%");
		h1111.add("  &a■ &fPerdeu&7 → &7%loses%");
		h1111.add(" &r&r&r&r");
		h1111.add(" Evento: %event%");
		h1111.add(" Tesouros: %treasures%");
		h1111.add(" Lobby: &7#1");
		h1111.add(" Coins: &7%coins%");
		h1111.add("");
		h1111.add(" &7the-hive.app");
		messages.getConfig().addDefault("stats-scoreboard-lines", h1111);

		List<String> x = messages.getConfig().getStringList("innocents-won-message");
  
	    x.add("&n&b----&a----&b-----&9#&a-----&b----&a----");
		x.add("&6MURDER " + ChatColor.GRAY + getDescription().getVersion());
		x.add("");
		x.add("&aInocentes Venceram.");
		x.add("");
		x.add("&cAssassino: &7%murderer%");
		x.add("&bDetetive: &7%detective%");
		x.add("");
		x.add("&n&b----&a----&b-----&9#&a-----&b----&a----");
		messages.getConfig().addDefault("innocents-won-message", x);

		List<String> x1 = messages.getConfig().getStringList("murderer-won-message");

	    x1.add("&n&b----&a----&b-----&9#&a-----&b----&a----");
		x1.add("&6MURDER " + ChatColor.GRAY + getDescription().getVersion());
		x1.add("");
		x1.add("&eAssassino Matou Todos.");
		x1.add("");
		x1.add("&cAssassino: &7%murderer%");
		x1.add("&bDetetive: &7%detective%");
		x1.add("");
		x1.add("&n&b----&a----&b-----&9#&a-----&b----&a----");
		messages.getConfig().addDefault("murderer-won-message", x1);
		List<String> x2 = messages.getConfig().getStringList("all-murder-won-message");
		x2.add("&n&4----&c----&4-----&6#&c-----&4----&c----");
		x2.add("&6MURDER " + ChatColor.GRAY + getDescription().getVersion());
		x2.add("");
		x2.add("&c&lTODOS ASSASSINOS");
		x2.add("&7A carnificina acabou!");
		x2.add("");
		x2.add("&eVencedor(es): &f%winner%");
		x2.add("");
		x2.add("&n&4----&c----&4-----&6#&c-----&4----&c----");
		messages.getConfig().addDefault("all-murder-won-message", x2);

		List<String> x3 = messages.getConfig().getStringList("tnttag-won-message");
		x3.add("&n&6----&e----&6-----&c#&e-----&6----&e----");
		x3.add("&6MURDER " + ChatColor.GRAY + getDescription().getVersion());
		x3.add("");
		x3.add("&c&lTNT TAG");
		x3.add("&7A TNT parou de explodir!");
		x3.add("");
		x3.add("&eVencedor: &f%winner%");
		x3.add("&7Modo: &6TNT Tag");
		x3.add("");
		x3.add("&n&6----&e----&6-----&c#&e-----&6----&e----");
		messages.getConfig().addDefault("tnttag-won-message", x3);

		List<String> rankedPlayersWin = messages.getConfig().getStringList("ranked-players-won-message");
		rankedPlayersWin.add("&n&6----&e----&6-----&b#&e-----&6----&e----");
		rankedPlayersWin.add("&6&lRANKED &7" + getDescription().getVersion());
		rankedPlayersWin.add("");
		rankedPlayersWin.add("&aOs jogadores venceram a Ranked!");
		rankedPlayersWin.add("&7Modo: &e%mode%");
		rankedPlayersWin.add("&7Mapa: &e%map%");
		rankedPlayersWin.add("&7Vencedores: &f%winner%");
		rankedPlayersWin.add("&7Assassino: &c%murderer%");
		rankedPlayersWin.add("");
		rankedPlayersWin.add("&n&6----&e----&6-----&b#&e-----&6----&e----");
		messages.getConfig().addDefault("ranked-players-won-message", rankedPlayersWin);

		List<String> rankedMurderWin = messages.getConfig().getStringList("ranked-murderer-won-message");
		rankedMurderWin.add("&n&6----&e----&6-----&c#&e-----&6----&e----");
		rankedMurderWin.add("&6&lRANKED &7" + getDescription().getVersion());
		rankedMurderWin.add("");
		rankedMurderWin.add("&cO Assassino venceu a Ranked!");
		rankedMurderWin.add("&7Modo: &e%mode%");
		rankedMurderWin.add("&7Mapa: &e%map%");
		rankedMurderWin.add("&7Assassino: &c%murderer%");
		rankedMurderWin.add("");
		rankedMurderWin.add("&n&6----&e----&6-----&c#&e-----&6----&e----");
		messages.getConfig().addDefault("ranked-murderer-won-message", rankedMurderWin);

		List<String> hideSeekersWin = messages.getConfig().getStringList("hideandseek-seekers-won-message");
		hideSeekersWin.add("&n&5----&d----&5-----&c#&d-----&5----&d----");
		hideSeekersWin.add("&d&lESCONDE-ESCONDE");
		hideSeekersWin.add("");
		hideSeekersWin.add("&cOs Procuradores venceram!");
		hideSeekersWin.add("&7Todos os escondidos foram encontrados.");
		hideSeekersWin.add("&7Mapa: &e%map%");
		hideSeekersWin.add("");
		hideSeekersWin.add("&n&5----&d----&5-----&c#&d-----&5----&d----");
		messages.getConfig().addDefault("hideandseek-seekers-won-message", hideSeekersWin);

		List<String> hideHidersWin = messages.getConfig().getStringList("hideandseek-hiders-won-message");
		hideHidersWin.add("&n&5----&d----&5-----&a#&d-----&5----&d----");
		hideHidersWin.add("&d&lESCONDE-ESCONDE");
		hideHidersWin.add("");
		hideHidersWin.add("&aOs Escondidos venceram!");
		hideHidersWin.add("&7Sobreviventes: &f%winner%");
		hideHidersWin.add("&7Mapa: &e%map%");
		hideHidersWin.add("");
		hideHidersWin.add("&n&5----&d----&5-----&a#&d-----&5----&d----");
		messages.getConfig().addDefault("hideandseek-hiders-won-message", hideHidersWin);

		messages.getConfig().addDefault("show-titles", true);
		messages.getConfig().options().copyDefaults(true);
		messages.save();

		getConfig().options().copyDefaults(true);
		saveConfig();
		Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "");

	}

	public void loadPlayer(Player p) {
		if (!sql.isConnected()) {
			return;
		}
		if (isPlayerInDataBase(p)) {

			Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), new Runnable() {
				@Override
				public void run() {
					Connection connection = sql.getConnection();
					try {
						PreparedStatement select = connection
								.prepareStatement("SELECT * FROM `MurderData` WHERE playername='" + p.getName() + "'");
						ResultSet result = select.executeQuery();

						if (getPlayerData(p) != null) {
							while (result.next()) {

								getPlayerData(p).setdeaths(result.getInt("deaths"));
								getPlayerData(p).setkills(result.getInt("kills"));
								getPlayerData(p).setwins(result.getInt("wins"));
								getPlayerData(p).setlose(result.getInt("loses"));
								getPlayerData(p).setscore(result.getInt("score"));
								getPlayerData(p).setcoins(result.getInt("coins"));
							}
							CloseResultSet(result);
							ClosePreparedStatement(select);
							getPlayerData(p).isloaded = true;

						}
					} catch (SQLException e) {
						e.printStackTrace();
					}

				}
			});
		} else {
			Connection connection = sql.getConnection();
			Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), new Runnable() {

				@Override
				public void run() {
					try {
						PreparedStatement insert = connection.prepareStatement(
								"INSERT INTO `MurderData` (playername, wins, deaths, loses, kills, coins, score) VALUES (?, ?, ?, ?, ?, ?, ?)");
						insert.setString(1, p.getName());
						insert.setInt(2, 0);
						insert.setInt(3, 0);
						insert.setInt(4, 0);
						insert.setInt(5, 0);
						insert.setInt(6, 0);
						insert.setInt(7, 0);
						insert.executeUpdate();
						ClosePreparedStatement(insert);
						getPlayerData(p).isloaded = true;

					} catch (SQLException e) {
						e.printStackTrace();
					}

				}

			});
		}

	}

	@EventHandler
	public void onChange(PlayerChangedWorldEvent e) {
		if (settings.getConfig().getBoolean("board-whitelist")) {
			List<String> v1 = settings.getConfig().getStringList("stats-board-world-whitelist");
			if (!v1.contains(e.getPlayer().getWorld().getName())) {

				if (scoreboards.containsKey(e.getPlayer().getName())) {
					scoreboards.remove(e.getPlayer().getName());
					e.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
				}

			}

			if (v1.contains(e.getPlayer().getWorld().getName())) {

				if (!scoreboards.containsKey(e.getPlayer().getName())) {
					setScoreboard(e.getPlayer());
				}

			}

		}
	}

	@EventHandler
	public void oncommand(PlayerCommandPreprocessEvent e) {
		String[] words = e.getMessage().split(" ");
		String firstword = words[0].replaceAll("/", "");

		if (Arenas.isInArena(e.getPlayer())) {

			List<String> v = settings.getConfig().getStringList("whitelisted-commands");

			if (e.getMessage().contains("/murder") || e.getMessage().contains("/m")) {
				return;
			}
			if (v.contains(e.getMessage()) || v.contains(firstword)) {
				return;
			}

			e.setCancelled(true);

		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (sender instanceof Player) {
			Player p = (Player) sender;

			if (label.equalsIgnoreCase("tag") || label.equalsIgnoreCase("tags") || label.equalsIgnoreCase("titulo") || label.equalsIgnoreCase("titulos")) {
				if (tagManager == null) return true;
				if (args.length == 0) {
					tagManager.openTagSelector(p);
					return true;
				}
				if (args[0].equalsIgnoreCase("select") || args[0].equalsIgnoreCase("selecionar")) {
					if (args.length < 2) {
						tagManager.openTagSelector(p);
						return true;
					}
					tagManager.selectTag(p, args[1]);
					return true;
				}
				if (args[0].equalsIgnoreCase("clear") || args[0].equalsIgnoreCase("remover") || args[0].equalsIgnoreCase("remove")) {
					tagManager.clearTag(p);
					p.sendMessage("§aSeu título foi removido.");
					return true;
				}
				tagManager.openTagSelector(p);
				return true;
			}

			if (label.equalsIgnoreCase("g") || label.equalsIgnoreCase("global")) {
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < args.length; i++) {
					if (i > 0) sb.append(" ");
					sb.append(args[i]);
				}
				ChatChannelManager.sendGlobal(this, p, sb.toString());
				return true;
			}

			if (label.equalsIgnoreCase("local") || label.equalsIgnoreCase("l")) {
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < args.length; i++) {
					if (i > 0) sb.append(" ");
					sb.append(args[i]);
				}
				ChatChannelManager.sendLocal(this, p, sb.toString());
				return true;
			}
		}

		if (label.equalsIgnoreCase("murder") || label.equalsIgnoreCase("m")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				Player p = (Player) sender;

				if (disabled) {
					return true;
				}

				if (args.length > 0 && (args[0].equalsIgnoreCase("evento") || args[0].equalsIgnoreCase("eventos") || args[0].equalsIgnoreCase("seasonal"))) {
					if (!p.isOp() && !p.hasPermission("murder.admin") && !p.hasPermission("murder.staff")) {
						p.sendMessage("§cApenas staff pode alterar eventos.");
						return true;
					}
					if (seasonalEventManager == null) {
						p.sendMessage("§cSistema de eventos indisponível.");
						return true;
					}
					if (args.length >= 2) {
						if (args[1].equalsIgnoreCase("reload") || args[1].equalsIgnoreCase("recarregar")) {
							seasonalEventManager.reload();
							if (seasonalTreasureManager != null) seasonalTreasureManager.reload();
							p.sendMessage("§aArquivos dos eventos sazonais recarregados.");
							return true;
						}
                        if (args[1].equalsIgnoreCase("tesouro") || args[1].equalsIgnoreCase("treasure")) {
                            if (seasonalTreasureManager == null) {
                                p.sendMessage("§cSistema de tesouros indisponível.");
                                return true;
                            }
                            if (args.length >= 3 && args[2].equalsIgnoreCase("add")) {
                                seasonalTreasureManager.addTreasure(p);
                                return true;
                            }
                            if (args.length >= 3 && (args[2].equalsIgnoreCase("clear") || args[2].equalsIgnoreCase("limpar"))) {
                                seasonalTreasureManager.clearCurrent(p);
                                return true;
                            }
                            seasonalTreasureManager.list(p);
                            return true;
                        }
						SeasonalEventType type = SeasonalEventType.fromString(args[1]);
						seasonalEventManager.setActiveEvent(type);
                        if (seasonalTreasureManager != null) seasonalTreasureManager.respawnAll();
						p.sendMessage("§aEvento sazonal alterado para: " + seasonalEventManager.getDisplayName());
						return true;
					}
					seasonalEventManager.openMenu(p);
					return true;
				}

				if (settings.getConfig().getBoolean("commands-whitelist")) {
					List<String> v11 = settings.getConfig().getStringList("commands-world-whitelist");
					if (!v11.contains(p.getWorld().getName())) {
						return true;
					}
				}

				if (args.length > 0 && args[0].equalsIgnoreCase("admin")) {
					if (!p.isOp() && !p.hasPermission("murder.admin")) {
						return true;
					}
					String page = args.length >= 2 ? args[1] : "principal";
					sendMurderAdminHelp(p, page);
					return true;
				}

				if (args.length == 0) {

					for (int i = 1; i < 15; i++) {
						player.sendMessage("");
					}
					player.sendMessage("");
					List<String> list = messages.getConfig().getStringList("murder-help-message");
					for (String s : list) {
						player.sendMessage(Utils.FormatText(player, s));

					}
					return true;
				}
				
				// AMONG US V1 - tasks do modo Among Us/Murder
				// /m sabotage task add <nome>
				// /m sabotage task list
				// /m sabotage task clear
				if (args[0].equalsIgnoreCase("sabotage") || args[0].equalsIgnoreCase("sabotar")) {
					if (!(p.hasPermission("murder.admin") || p.hasPermission("smurder.admin") || p.hasPermission("murder.staff") || p.isOp())) {
						p.sendMessage("§cApenas staff pode configurar tasks do AMONG US.");
						return true;
					}
					if (sabotageTaskManager == null) {
						p.sendMessage("§cSistema de tasks não carregou.");
						return true;
					}
					if (args.length >= 3 && (args[1].equalsIgnoreCase("colornpc") || args[1].equalsIgnoreCase("corenpc")) && args[2].equalsIgnoreCase("set")) {
						if (amongUsColorManager != null) amongUsColorManager.setNpc(p);
						return true;
					}
					if (args.length >= 3 && (args[1].equalsIgnoreCase("colornpc") || args[1].equalsIgnoreCase("corenpc")) && (args[2].equalsIgnoreCase("delete") || args[2].equalsIgnoreCase("del") || args[2].equalsIgnoreCase("remover"))) {
						if (amongUsColorManager != null) amongUsColorManager.deleteNpc(p);
						return true;
					}
					if (args.length >= 3 && args[1].equalsIgnoreCase("task") && args[2].equalsIgnoreCase("add")) {
						String name = "Task";
						if (args.length >= 4) {
							StringBuilder builder = new StringBuilder();
							for (int i = 3; i < args.length; i++) {
								if (builder.length() > 0) builder.append(" ");
								builder.append(args[i]);
							}
							name = builder.toString();
						}
						sabotageTaskManager.addTaskLocation(p, name);
						return true;
					}
					if (args.length >= 3 && args[1].equalsIgnoreCase("task") && args[2].equalsIgnoreCase("list")) {
						sabotageTaskManager.listTaskLocations(p);
						return true;
					}
					if (args.length >= 3 && args[1].equalsIgnoreCase("task") && (args[2].equalsIgnoreCase("clear") || args[2].equalsIgnoreCase("limpar"))) {
						sabotageTaskManager.clearTaskLocations(p);
						return true;
					}
					p.sendMessage("§e/m sabotage task list §7- ver as 12 tasks disponíveis");
					p.sendMessage("§e/m sabotage task add <task> §7- adicionar no local atual");
					p.sendMessage("§e/m sabotage task clear §7- apagar todas");
					p.sendMessage("§e/m sabotage colornpc set §7- setar NPC de cores no lobby de espera");
					p.sendMessage("§e/m sabotage colornpc delete §7- remover NPC de cores");
					return true;
				}

				// Salas públicas fixas - comando corrigido e independente de mapa base
				// /m salapublica criar <modo> [max]
				// /m salapublica remover <id>
				if (args[0].equalsIgnoreCase("salapublica") || args[0].equalsIgnoreCase("publicroom")) {
					if (!(p.hasPermission("murder.admin") || p.hasPermission("smurder.admin") || p.hasPermission("murder.staff") || p.isOp())) {
						p.sendMessage("§cApenas staff pode criar/remover salas públicas fixas.");
						return true;
					}

					if (args.length < 2) {
						p.sendMessage("§e/m salapublica criar <modo> [max]");
						p.sendMessage("§e/m salapublica remover <id>");
						p.sendMessage("§7Modos: NORMAL, ALL_MURDER, TNT_TAG, RANKED, HIDE_AND_SEEK, SABOTAGE");
						return true;
					}

					if (args[1].equalsIgnoreCase("criar") || args[1].equalsIgnoreCase("create")) {
						if (args.length < 3) {
							p.sendMessage("§cUse: /m salapublica criar <modo> [max]");
							return true;
						}

						GameModeType mode;
						try {
							mode = parsePublicRoomMode(args[2]);
						} catch (Exception ex) {
							p.sendMessage("§cModo inválido: §e" + args[2]);
							p.sendMessage("§7Use: NORMAL, ALL_MURDER, TNT_TAG, RANKED, HIDE_AND_SEEK ou SABOTAGE.");
							return true;
						}

						String selectedMap = getRandomAllowedPublicRoomMap(mode);
						if (selectedMap == null || selectedMap.trim().isEmpty()) {
							p.sendMessage("§cNenhum mapa disponível/liberado para o modo §e" + mode.getDisplayName() + "§c.");
							p.sendMessage("§7Crie mapas com /m criar e verifique a Área Staff > Mapas.");
							return true;
						}

						int max = mode == GameModeType.RANKED ? getConfig().getInt("ranked.public-room-max-players", 20) : 12;
						if (args.length >= 4) {
							try {
								max = Integer.parseInt(args[3]);
							} catch (NumberFormatException ex) {
								p.sendMessage("§cQuantidade máxima inválida: §e" + args[3]);
								p.sendMessage("§7Use: /m salapublica criar <modo> [max]");
								return true;
							}
						}
						if (max <= 1) max = 12;
						if (mode == GameModeType.RANKED) max = getConfig().getInt("ranked.public-room-max-players", 20);

						String roomId = getNextPublicRoomId();
						Arena arena = new Arena(roomId, selectedMap, this);
						arena.maxPlayers = max;
						arena.setGameMode(mode);
						Arenas.addArena(arena);

						Room created = roomManager.createPublicRoom(roomId, arena, mode);
						created.setSelectedMapName(null);
						roomManager.savePublicRooms();

						p.sendMessage("§aSala pública criada: §e" + roomManager.getPublicRoomDisplayName(created) + " §7(ID: " + roomId + ")");
						p.sendMessage("§7Modo fixo: §f" + mode.getDisplayName() + " §8| §7Max: §f" + max);
						p.sendMessage("§7Mapa: §fVotação/Aleatório §8(§7inicial: " + selectedMap + "§8)");
						return true;
					}

					if (args[1].equalsIgnoreCase("remover") || args[1].equalsIgnoreCase("delete") || args[1].equalsIgnoreCase("deletar")) {
						if (args.length < 3) {
							p.sendMessage("§cUse: /m salapublica remover <id>");
							return true;
						}
						Room room = roomManager.getRoom(args[2]);
						if (room == null || !room.isFixedPublicRoom()) {
							p.sendMessage("§cSala pública não encontrada: §e" + args[2]);
							return true;
						}
						roomManager.deleteRoom(room);
						roomManager.savePublicRooms();
						p.sendMessage("§aSala pública removida.");
						return true;
					}

					p.sendMessage("§cUse: /m salapublica criar <modo> [max]");
					p.sendMessage("§cOu: /m salapublica remover <id>");
					return true;
				}

				// Faca sem cooldown por jogador - /m knifecooldown [player|on|off]
				if (args[0].equalsIgnoreCase("knifecooldown") || args[0].equalsIgnoreCase("facacooldown") || args[0].equalsIgnoreCase("semcooldownfaca")) {
					if (!p.isOp() && !p.hasPermission("murder.admin") && !p.hasPermission("murder.staff")) {
						p.sendMessage("§cSem permissão.");
						return true;
					}

					Player target = p;
					String action = null;

					if (args.length >= 2) {
						if (args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("off") || args[1].equalsIgnoreCase("ativar") || args[1].equalsIgnoreCase("desativar")) {
							action = args[1];
						} else {
							Player found = Bukkit.getPlayer(args[1]);
							if (found == null) {
								p.sendMessage("§cJogador offline/não encontrado.");
								return true;
							}
							target = found;
						}
					}

					if (args.length >= 3) {
						action = args[2];
					}

					boolean enabled;
					if (action != null && (action.equalsIgnoreCase("on") || action.equalsIgnoreCase("ativar"))) {
						setKnifeNoCooldown(target, true);
						enabled = true;
					} else if (action != null && (action.equalsIgnoreCase("off") || action.equalsIgnoreCase("desativar"))) {
						setKnifeNoCooldown(target, false);
						enabled = false;
					} else {
						enabled = toggleKnifeNoCooldown(target);
					}

					p.sendMessage("§6§lMURDER §7> §fFaca sem cooldown de §e" + target.getName() + " §fagora está " + (enabled ? "§aATIVADA" : "§cDESATIVADA") + "§f.");
					if (!target.equals(p)) {
						target.sendMessage("§6§lMURDER §7> §fSua faca sem cooldown foi " + (enabled ? "§aATIVADA" : "§cDESATIVADA") + " §fpor um staff.");
					}
					return true;
				}


				// Leaderboards por holograma - /m leaderboard
				if (args[0].equalsIgnoreCase("leaderboard") || args[0].equalsIgnoreCase("leaderboards") || args[0].equalsIgnoreCase("lb")) {
					if (!p.isOp() && !p.hasPermission("murder.admin") && !p.hasPermission("murder.staff")) {
						p.sendMessage("§cSem permissão.");
						return true;
					}
					if (leaderboardManager == null) {
						p.sendMessage("§cSistema de leaderboard não carregou.");
						return true;
					}
					if (args.length == 1) {
						leaderboardManager.openMenu(p);
						return true;
					}
					if (args[1].equalsIgnoreCase("set") || args[1].equalsIgnoreCase("setar")) {
						if (args.length < 3) {
							p.sendMessage("§cUse: /m leaderboard set <tipo>");
							p.sendMessage("§7Tipos: niveis, abates, mortes, partidas, perdidas, ranked_rp, ranked_abates, ranked_partidas, ranked_perdidas");
							return true;
						}
						LeaderboardType type = LeaderboardType.fromId(args[2]);
						if (type == null) {
							p.sendMessage("§cTipo inválido.");
							return true;
						}
						leaderboardManager.setLeaderboard(p, type);
						return true;
					}
					if (args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("remover")) {
						if (args.length < 3) {
							p.sendMessage("§cUse: /m leaderboard remove <tipo>");
							return true;
						}
						LeaderboardType type = LeaderboardType.fromId(args[2]);
						leaderboardManager.remove(p, type);
						return true;
					}
					if (args[1].equalsIgnoreCase("update") || args[1].equalsIgnoreCase("atualizar")) {
						leaderboardManager.updateAll();
						p.sendMessage("§aTodos os leaderboards foram atualizados.");
						return true;
					}
					p.sendMessage("§e/m leaderboard §7- abrir menu");
					p.sendMessage("§e/m leaderboard set <tipo> §7- setar olhando para o local");
					p.sendMessage("§e/m leaderboard remove <tipo> §7- remover holograma");
					p.sendMessage("§e/m leaderboard atualizar §7- atualizar todos");
					return true;
				}

				// Ranked V4 - comandos de perfil, top global e admin RP
				if (args.length >= 1 && (args[0].equalsIgnoreCase("ranked") || args[0].equalsIgnoreCase("topranked"))) {
					if (rankedManager == null) {
						p.sendMessage("§cSistema Ranked não carregou.");
						return true;
					}

					if (args[0].equalsIgnoreCase("topranked")) {
						openRankedTopMenu(p);
						return true;
					}

					if (args.length == 1) {
						rankedManager.sendProfile(p);
						p.sendMessage("§7Use: §e/m ranked top §7para ver o Top Ranked.");
						return true;
					}

					if (args[1].equalsIgnoreCase("top") || args[1].equalsIgnoreCase("leaderboard")) {
						openRankedTopMenu(p);
						return true;
					}

					if (args[1].equalsIgnoreCase("perfil") || args[1].equalsIgnoreCase("profile")) {
						rankedManager.sendProfile(p);
						return true;
					}

					if (args[1].equalsIgnoreCase("chat")) {
						p.sendMessage("§6§lRANKED §7> §fSeu prefixo ranked: " + rankedManager.getRankColor(p) + "[" + rankedManager.getRankName(p).toUpperCase() + "] §f" + p.getName());
						return true;
					}

					if (args[1].equalsIgnoreCase("setrp")) {
						if (!p.isOp() && !p.hasPermission("murder.admin")) {
							p.sendMessage("§cSem permissão.");
							return true;
						}
						if (args.length < 4) {
							p.sendMessage("§cUse: /m ranked setrp <player> <rp>");
							return true;
						}
						Player target = Bukkit.getPlayer(args[2]);
						if (target == null) {
							p.sendMessage("§cJogador offline/não encontrado.");
							return true;
						}
						int rp;
						try {
							rp = Integer.parseInt(args[3]);
						} catch (Exception ex) {
							p.sendMessage("§cRP inválido.");
							return true;
						}
						rankedManager.setRP(target, rp);
						p.sendMessage("§aRP de §f" + target.getName() + " §asetado para §b" + rp + "§a.");
						target.sendMessage("§6§lRANKED §7> §fSeu RP foi setado para §b" + rp + " §7(" + rankedManager.getRankDisplay(target) + "§7).");
						return true;
					}

					if (args[1].equalsIgnoreCase("reset")) {
						if (!p.isOp() && !p.hasPermission("murder.admin")) {
							p.sendMessage("§cSem permissão.");
							return true;
						}
						if (args.length < 3) {
							p.sendMessage("§cUse: /m ranked reset <player>");
							return true;
						}
						Player target = Bukkit.getPlayer(args[2]);
						if (target == null) {
							p.sendMessage("§cJogador offline/não encontrado.");
							return true;
						}
						rankedManager.reset(target);
						p.sendMessage("§aRanked de §f" + target.getName() + " §afoi resetado.");
						target.sendMessage("§6§lRANKED §7> §cSeu Ranked foi resetado.");
						return true;
					}

					p.sendMessage("§6§lRANKED §7> §fComandos:");
					p.sendMessage("§e/m ranked §7- seu perfil ranked");
					p.sendMessage("§e/m ranked top §7- top ranked global");
					p.sendMessage("§e/m ranked chat §7- ver prefixo ranked");
					if (p.isOp() || p.hasPermission("murder.admin")) {
						p.sendMessage("§e/m ranked setrp <player> <rp> §7- setar RP");
						p.sendMessage("§e/m ranked reset <player> §7- resetar Ranked");
					}
					return true;
				}

				// CHAT / TAG
				if (args[0].equalsIgnoreCase("tag") || args[0].equalsIgnoreCase("tags") || args[0].equalsIgnoreCase("titulo") || args[0].equalsIgnoreCase("titulos")) {
				    if (tagManager == null) return true;
				    if (args.length == 1) {
				        tagManager.openTagSelector(p);
				        return true;
				    }
				    if (args[1].equalsIgnoreCase("select") || args[1].equalsIgnoreCase("selecionar")) {
				        if (args.length < 3) {
				            tagManager.openTagSelector(p);
				            return true;
				        }
				        tagManager.selectTag(p, args[2]);
				        return true;
				    }
				    if (args[1].equalsIgnoreCase("clear") || args[1].equalsIgnoreCase("remover") || args[1].equalsIgnoreCase("remove")) {
				        tagManager.clearTag(p);
				        p.sendMessage("§aSeu título foi removido.");
				        return true;
				    }
				    if (args[1].equalsIgnoreCase("set")) {
				        if (args.length < 3) {
				            p.sendMessage("§cUse: /m tag set <tag>");
				            return true;
				        }
				        StringBuilder sb = new StringBuilder();
				        for (int i = 2; i < args.length; i++) {
				            if (i > 2) sb.append(" ");
				            sb.append(args[i]);
				        }
				        String tag = sb.toString();
				        if (tag.length() > 32) tag = tag.substring(0, 32);
				        tagManager.setTag(p, tag);
				        p.sendMessage("§aTítulo setado para: " + ChatColor.translateAlternateColorCodes('&', tag));
				        return true;
				    }
				    if (args[1].equalsIgnoreCase("admin")) {
				        if (!p.isOp() && !p.hasPermission("murder.admin")) {
				            p.sendMessage("§cSem permissão.");
				            return true;
				        }
				        if (args.length < 4) {
				            p.sendMessage("§cUse: /m tag admin <player> <tag|clear>");
				            return true;
				        }
				        Player target = Bukkit.getPlayer(args[2]);
				        if (target == null) {
				            p.sendMessage("§cJogador não encontrado.");
				            return true;
				        }
				        if (args[3].equalsIgnoreCase("clear") || args[3].equalsIgnoreCase("remover")) {
				            tagManager.clearTag(target);
				            p.sendMessage("§aTítulo removido de " + target.getName() + ".");
				            return true;
				        }
				        StringBuilder sb = new StringBuilder();
				        for (int i = 3; i < args.length; i++) {
				            if (i > 3) sb.append(" ");
				            sb.append(args[i]);
				        }
				        String tag = sb.toString();
				        if (tag.length() > 32) tag = tag.substring(0, 32);
				        tagManager.setTag(target, tag);
				        p.sendMessage("§aTítulo setado para " + target.getName() + ": " + ChatColor.translateAlternateColorCodes('&', tag));
				        return true;
				    }
				}

				// LEVEL / XP
				
                
                if (args[0].equalsIgnoreCase("setbattlepassnpc")) {
                    if (!p.isOp() && !p.hasPermission("murder.admin")) {
                        p.sendMessage("§cSem permissão.");
                        return true;
                    }
                    if (cosmeticNPCManager != null) {
                        String skin = args.length >= 2 ? args[1] : p.getName();
                        cosmeticNPCManager.setBattlePassNPC(p, skin);
                    }
                    return true;
                }

                if (args[0].equalsIgnoreCase("delbattlepassnpc") || args[0].equalsIgnoreCase("deletebattlepassnpc")) {
                    if (!p.isOp() && !p.hasPermission("murder.admin")) {
                        p.sendMessage("§cSem permissão.");
                        return true;
                    }
                    if (cosmeticNPCManager != null) {
                        cosmeticNPCManager.deleteBattlePassNPC(p);
                    }
                    return true;
                }

                if (args[0].equalsIgnoreCase("setbox") || args[0].equalsIgnoreCase("setmysterybox")
                        || args[0].equalsIgnoreCase("setboxnpc") || args[0].equalsIgnoreCase("setmysteryboxnpc")) {
                    if (!p.isOp() && !p.hasPermission("murder.admin")) {
                        p.sendMessage("§cSem permissão.");
                        return true;
                    }
                    if (cosmeticNPCManager != null) {
                        cosmeticNPCManager.setMysteryBoxChest(p);
                    }
                    return true;
                }

                if (args[0].equalsIgnoreCase("delbox") || args[0].equalsIgnoreCase("delmysterybox")
                        || args[0].equalsIgnoreCase("deletebox") || args[0].equalsIgnoreCase("deletemysterybox")) {
                    if (!p.isOp() && !p.hasPermission("murder.admin")) {
                        p.sendMessage("§cSem permissão.");
                        return true;
                    }
                    if (cosmeticNPCManager != null) {
                        cosmeticNPCManager.deleteMysteryBoxChest(p);
                    }
                    return true;
                }


				if (args[0].equalsIgnoreCase("box") || args[0].equalsIgnoreCase("mysterybox")) {
                    if (mysteryBoxManager != null) {
                        mysteryBoxManager.openMainMenu(p);
                    }
                    return true;
                }

                if (args[0].equalsIgnoreCase("givebox")) {
                    if (!p.isOp() && !p.hasPermission("murder.admin")) {
                        p.sendMessage("§cSem permissão.");
                        return true;
                    }
                    if (args.length < 3) {
                        p.sendMessage("§cUse: /m givebox <player> <quantia>");
                        return true;
                    }
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        p.sendMessage("§cJogador não encontrado.");
                        return true;
                    }
                    int amount;
                    try {
                        amount = Integer.parseInt(args[2]);
                    } catch (Exception ex) {
                        p.sendMessage("§cQuantidade inválida.");
                        return true;
                    }
                    if (mysteryBoxManager != null) {
                        mysteryBoxManager.addBoxes(target, amount);
                    }
                    p.sendMessage("§aCaixas adicionadas.");
                    return true;
                }

                if (args[0].equalsIgnoreCase("pass") || args[0].equalsIgnoreCase("battlepass")) {
                    if (battlePassManager != null) {
                        battlePassManager.openMenu(p);
                    }
                    return true;
                }

                if (args[0].equalsIgnoreCase("addpassxp")) {
                    if (!p.isOp() && !p.hasPermission("murder.admin")) {
                        p.sendMessage("§cSem permissão.");
                        return true;
                    }
                    if (args.length < 3) {
                        p.sendMessage("§cUse: /m addpassxp <player> <quantia>");
                        return true;
                    }
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        p.sendMessage("§cJogador não encontrado.");
                        return true;
                    }
                    int amount;
                    try {
                        amount = Integer.parseInt(args[2]);
                    } catch (Exception ex) {
                        p.sendMessage("§cQuantidade inválida.");
                        return true;
                    }
                    if (battlePassManager != null) {
                        battlePassManager.addXP(target, amount, "Admin");
                    }
                    p.sendMessage("§aXP do passe adicionado.");
                    return true;
                }

				if (args[0].equalsIgnoreCase("level") || args[0].equalsIgnoreCase("xp")) {
					if (levelManager != null) {
						levelManager.sendStats(p);
					}
					return true;
				}

				if (args[0].equalsIgnoreCase("toplevel") || args[0].equalsIgnoreCase("topxp")) {
					if (levelManager != null) {
						levelManager.sendTop(p);
					}
					return true;
				}

				if (args[0].equalsIgnoreCase("setlevelboard")) {
					p.sendMessage("§cEsse NPC antigo de níveis foi removido. Use §e/m leaderboard §cpara criar leaderboards por holograma.");
					if (levelManager != null) {
						levelManager.setLeaderboard(p.getLocation());
					}
					return true;
				}

				if (args[0].equalsIgnoreCase("addxp")) {
					if (!p.isOp() && !p.hasPermission("murder.admin")) {
						p.sendMessage("§cSem permissão.");
						return true;
					}
					if (args.length < 3) {
						p.sendMessage("§cUse: /m addxp <player> <quantia>");
						return true;
					}
					Player target = Bukkit.getPlayer(args[1]);
					if (target == null) {
						p.sendMessage("§cJogador não encontrado.");
						return true;
					}
					int xp;
					try {
						xp = Integer.parseInt(args[2]);
					} catch (Exception ex) {
						p.sendMessage("§cQuantidade inválida.");
						return true;
					}
					if (levelManager != null) {
						levelManager.addXP(target, xp, "Admin");
						p.sendMessage("§aXP adicionado.");
					}
					return true;
				}

				
				// COINS
				if (args[0].equalsIgnoreCase("coins")) {

				    if (args.length < 2) {
				        sender.sendMessage("§cUse: /murder coins <ver|add|remove> [player] [quantia]");
				        return true;
				    }

				    String sub = args[1].toLowerCase();

				    // VER
				    if (sub.equals("ver")) {

				        if (args.length == 2) {
				            sender.sendMessage("§aVocê tem §f" + getPlayerData(p).getcoins() + " §acoins");
				            return true;
				        }

				        if (args.length == 3) {
				            Player target = Bukkit.getPlayer(args[2]);

				            if (target == null) {
				                sender.sendMessage("§cJogador não encontrado!");
				                return true;
				            }

				            sender.sendMessage("§f" + target.getName() + " §atem §f" + getPlayerData(target).getcoins() + " §acoins");
				            return true;
				        }
				    }

				    // ADD / REMOVE
				    if (sub.equals("add") || sub.equals("remove")) {

				        if (args.length != 4) {
				            sender.sendMessage("§cUse: /murder coins " + sub + " <player> <quantia>");
				            return true;
				        }

				        Player target = Bukkit.getPlayer(args[2]);

				        if (target == null) {
				            sender.sendMessage("§cJogador não encontrado!");
				            return true;
				        }

				        int amount;

				        try {
				            amount = Integer.parseInt(args[3]);
				        } catch (Exception e) {
				            sender.sendMessage("§cQuantidade inválida!");
				            return true;
				        }

				        PlayerData data = getPlayerData(target);
				        MurderAPI api = new MurderAPI(this);

				        if (sub.equals("add")) {
				            data.setcoins(data.getcoins() + amount);
				        } else {
				            data.setcoins(data.getcoins() - amount);
				        }

				        if (getConfig().getBoolean("mysql")) {
				            api.setSQLDataInstantly(target,
				                data.getkill(),
				                data.getdeaths(),
				                data.getloses(),
				                data.getwins(),
				                data.getcoins(),
				                data.getscore()
				            );
				        } else {
				            api.setNonSQLData(target,
				                data.getkill(),
				                data.getdeaths(),
				                data.getloses(),
				                data.getwins(),
				                data.getcoins(),
				                data.getscore()
				            );
				        }

				        sender.sendMessage("§aCoins atualizados!");
				        target.sendMessage("§aAgora você tem §f" + data.getcoins() + " §acoins");

				        return true;
				    }

				    sender.sendMessage("§cUse: /murder coins <ver|add|remove>");
				    return true;
				}
				
				//final coins
				
				// Fix V4: /m setzonecenter <arena> agora funciona fora do bloco args.length == 1
				if (args.length >= 2 && (args[0].equalsIgnoreCase("setzonecenter") || args[0].equalsIgnoreCase("setrankedzone"))) {
					if (!p.isOp() && !p.hasPermission("murder.admin")) {
						p.sendMessage("§cSem permissão.");
						return true;
					}
					List<String> h = arenas.getConfig().getStringList("arena-list");
					if (!h.contains(args[1])) {
						p.sendMessage(Utils.FormatText(p, messages.getConfig().getString("arena-not-exits")));
						return true;
					}
					Arena a = Arenas.getArena(args[1]);
					setRankedZoneCenter(p, a);
					p.sendMessage("§aCentro da zona Ranked setado para a arena §e" + a.getName() + "§a.");
					p.sendMessage("§7A zona visível vai nascer e fechar nesse centro nas partidas Ranked.");
					return true;
				}

				if (args.length == 2 && args[0].equalsIgnoreCase("setnpc")) {
				    if (!p.isOp() && !p.hasPermission("murder.admin")) {
				        p.sendMessage("�cVoc� n�o tem permiss�o.");
				        return true;
				    }

				    if (npcManager == null) {
				        p.sendMessage("�cNPCManager n�o carregou corretamente.");
				        return true;
				    }

				    if (args[1].equalsIgnoreCase("partidas")) {
				        npcManager.setNPC("partidas", p.getLocation());
				        p.sendMessage("�aNPC de Partidas setado na sua localiza��o.");
				        return true;
				    }

				    if (args[1].equalsIgnoreCase("loja") || args[1].equalsIgnoreCase("cosmeticos")) {
				        npcManager.setNPC("loja", p.getLocation());
				        p.sendMessage("�aNPC de Loja setado na sua localiza��o.");
				        return true;
				    }

				    p.sendMessage("�cUse: /m setnpc <partidas|loja>");
				    return true;
				}

				if (args.length == 1) {

					if (args[0].equalsIgnoreCase("tnthit")) {
						Arena arena = Arenas.getArena(p);

						if (arena == null || !arena.isTntTagMode()) {
							p.sendMessage("�cVoc� s� pode usar isso durante uma partida de TNTTag.");
							return true;
						}

						boolean enabled = arena.toggleTntHitProtection(p);

						if (enabled) {
							p.sendMessage("§aProteção ativada: jogadores sem TNT não conseguem bater em você enquanto você também estiver sem TNT.");
						} else {
							p.sendMessage("§cProteção desativada.");
						}
						return true;
					}

					if (args[0].equalsIgnoreCase("reset")) {

						if (getPlayerData(p) != null) {
							if (getPlayerData(p).getdeaths() == 0 && getPlayerData(p).getkill() == 0
									&& getPlayerData(p).getloses() == 0 && getPlayerData(p).getwins() == 0
									&& getPlayerData(p).getscore() == 0) {
								p.sendMessage(Utils.FormatText(p, messages.getConfig().getString("stats-reset-error")));
								return true;
							}

							if (getPlayerData(p) != null) {
								getPlayerData(p).reset();
								p.sendMessage(
										Utils.FormatText(p, messages.getConfig().getString("stats-reset-message")));
							}
						}
					}

					// ...

					if (args[0].equalsIgnoreCase("ver")) {
						if (getPlayerData(p) != null) {
							for (int i = 1; i < 15; i++) {
								player.sendMessage("");
							
							}
						
							List<String> list = messages.getConfig().getStringList("self-info-message");
							for (String s : list) {
								player.sendMessage(Utils.FormatText(player, s.replaceAll("%player%", p.getName())
										.replaceAll("%loses%", String.valueOf(getPlayerData(player).getloses()))
										.replaceAll("%wins%", String.valueOf(getPlayerData(player).getwins()))
										.replaceAll("%kills%", String.valueOf(getPlayerData(player).getkill()))
										.replaceAll("%score%", String.valueOf(getPlayerData(player).getscore()))
										.replaceAll("%coins%", String.valueOf(getPlayerData(player).getcoins()))
										.replaceAll("%deaths%", String.valueOf(getPlayerData(player).getdeaths()))));

							}
						}
					}
					


					if (args[0].equalsIgnoreCase("setzonecenter") || args[0].equalsIgnoreCase("setrankedzone")) {
						if (!p.isOp() && !p.hasPermission("murder.admin")) {
							p.sendMessage("§cSem permissão.");
							return true;
						}
						if (args.length < 2) {
							p.sendMessage("§cUse: /murder setzonecenter <arena>");
							return true;
						}
						List<String> h = arenas.getConfig().getStringList("arena-list");
						if (!h.contains(args[1])) {
							p.sendMessage(Utils.FormatText(p, messages.getConfig().getString("arena-not-exits")));
							return true;
						}
						Arena a = Arenas.getArena(args[1]);
						setRankedZoneCenter(p, a);
						p.sendMessage("§aCentro da zona Ranked setado para a arena §e" + a.getName() + "§a.");
						p.sendMessage("§7A zona visível vai nascer e fechar nesse centro nas partidas Ranked.");
						return true;
					}


					if (args[0].equalsIgnoreCase("rankedzone") || args[0].equalsIgnoreCase("zone")) {
						if (!p.isOp() && !p.hasPermission("murder.admin")) {
							p.sendMessage("§cSem permissão.");
							return true;
						}
						if (!Arenas.isInArena(p)) {
							p.sendMessage("§cVocê precisa estar em uma sala/arena.");
							return true;
						}
						Arena a = Arenas.getArena(p);
						a.forceStartRankedZoneForDebug();
						p.sendMessage("§aTentando iniciar zona Ranked nesta sala. Veja o console.");
						return true;
					}

					if (args[0].equalsIgnoreCase("partidas")) {

						if (!sd.containsKey(p.getName())) {
							SmartInventory si = new SmartInventory(p);
							sd.put(p.getName(), si);
						}

						if (sd.containsKey(p.getName())) {
							sd.get(p.getName()).openInventory();
						}
						return true;
					}
					
					if (args[0].equalsIgnoreCase("loja") || args[0].equalsIgnoreCase("cosmeticos")) {
						new CosmeticsMenu(this).openMainMenu(p);
						return true;
					}
					
					if (args[0].equalsIgnoreCase("salapublica") || args[0].equalsIgnoreCase("publicroom")) {
					    if (!(p.hasPermission("murder.admin") || p.hasPermission("smurder.admin") || p.hasPermission("murder.staff") || p.isOp())) {
					        p.sendMessage("§cApenas staff pode criar/remover salas públicas fixas.");
					        return true;
					    }
					    if (args.length < 2) {
					        p.sendMessage("§e/m salapublica criar <modo> [max]");
					        p.sendMessage("§e/m salapublica remover <id>");
					        p.sendMessage("§7Modos: NORMAL, ALL_MURDER, TNT_TAG, HIDE_AND_SEEK, SABOTAGE, RANKED");
					        p.sendMessage("§7O mapa sera escolhido por votação/aleatório. Não use mapa base.");
					        return true;
					    }
					    if (args[1].equalsIgnoreCase("criar") || args[1].equalsIgnoreCase("create")) {
					        if (args.length < 3) {
					            p.sendMessage("§cUse: /m salapublica criar <modo> [max]");
					            return true;
					        }
					        GameModeType mode;
					        try {
					            mode = parsePublicRoomMode(args[2]);
					        } catch (Exception ex) {
					            p.sendMessage("§cModo inválido. Use NORMAL, ALL_MURDER, TNT_TAG, HIDE_AND_SEEK, SABOTAGE ou RANKED.");
					            return true;
					        }
					        String firstMap = getRandomAllowedPublicRoomMap(mode);
					        if (firstMap == null || firstMap.trim().isEmpty()) {
					            p.sendMessage("§cNenhum mapa liberado para esse modo.");
					            p.sendMessage("§7Crie mapas com /m criar e verifique bloqueios em Staff > Mapas.");
					            return true;
					        }
					        int max = mode == GameModeType.RANKED ? getConfig().getInt("ranked.public-room-max-players", 20) : SpawnSizeByName(firstMap);
					        // Novo formato: /m salapublica criar <modo> [max]
					        // Compatibilidade: se staff digitar o formato antigo com mapa, o mapa e ignorado e o max continua sendo lido.
					        if (args.length >= 4) {
					            try {
					                max = Integer.parseInt(args[3]);
					            } catch (Exception ignored) {
					                if (args.length >= 5) {
					                    try { max = Integer.parseInt(args[4]); } catch (Exception ignored2) {}
					                }
					            }
					        }
					        if (max <= 0) max = mode == GameModeType.RANKED ? 20 : 12;
					        String roomId = "PUBLIC-" + getRandom(100, 999);
					        Arena arena = new Arena(roomId, firstMap, this);
					        arena.maxPlayers = max;
					        Arenas.addArena(arena);
					        Room created = roomManager.createPublicRoom(roomId, arena, mode);
					        created.setSelectedMapName(firstMap);
					        roomManager.savePublicRooms();
					        p.sendMessage("§aSala pública criada: §e" + roomManager.getPublicRoomDisplayName(created) + " §7(ID: " + roomId + ")");
					        p.sendMessage("§7Sem líder. Apenas staff pode gerenciar.");
					        p.sendMessage("§7Mapa inicial aleatório: §e" + firstMap + "§7. Nas partidas, o mapa vem da votação/aleatório.");
					        return true;
					    }
					    if (args[1].equalsIgnoreCase("remover") || args[1].equalsIgnoreCase("delete")) {
					        if (args.length < 3) {
					            p.sendMessage("§cUse: /m salapublica remover <id>");
					            return true;
					        }
					        Room room = roomManager.getRoom(args[2]);
					        if (room == null || !room.isFixedPublicRoom()) {
					            p.sendMessage("§cSala pública não encontrada: §e" + args[2]);
					            return true;
					        }
					        roomManager.deleteRoom(room);
					        roomManager.savePublicRooms();
					        p.sendMessage("§aSala pública removida.");
					        return true;
					    }
					    return true;
					}

					if (args[0].equalsIgnoreCase("criarsala")) {

					    if (!(p.hasPermission("murder.room.create") || p.hasPermission("murder.vip") || p.hasPermission("murder.staff") || p.hasPermission("murder.admin") || p.hasPermission("smurder.admin") || p.isOp())) {
					        p.sendMessage("§cApenas staff ou VIP podem criar salas privadas.");
					        return true;
					    }

					    if (Arenas.isInArena(p)) {
					        p.sendMessage("§cVocê já está em uma sala/partida.");
					        return true;
					    }

					    String roomId = "ROOM-" + getRandom(100, 999);
					    Arena arena = new Arena(roomId, "Cemiterio", this);

					    Arenas.addArena(arena);
					    if (roomManager != null) {
					        roomManager.createRoom(p, arena);
					    } else {
					        arena.persistentRoom = true;
					        arena.owner = p;
					    }

					    arena.addPlayer(p);

					    p.sendMessage("§aSala criada: §fPrivada de " + p.getName());
					    return true;
					}
					
					if (args[0].equalsIgnoreCase("join")) {
						if (Arenas.getArenas() == null || Arenas.getArenas().size() == 0) {
							p.sendMessage(Utils.FormatText(p, messages.getConfig().getString("no-arenas")));
							return true;
						}
						
						 if (p.hasPermission("murder.join")) {
						
							ArrayList<Arena> arenaz = new ArrayList<>();
							if (Arenas.getArenas().size() > 0) {
								for (Arena arena1 : Arenas.getArenas()) {
									if (arena1.getState() == GameState.STARTING
											&& arena1.players.size() < SpawnSize(arena1)) {
										arenaz.add(arena1);
									}
									if (arena1.getState() == GameState.LOBBY) {
										arenaz.add(arena1);
									}
								}
							}

							if (arenaz.size() > 0) {

								Arena a = arenaz.get(0);

								for (Arena ar : arenaz) {
									if (ar.players.size() > a.players.size()) {
										a = ar;
									}
								}

								if (!Arenas.isInArena(p) && a != null) {
									a.addPlayer(player);
								}

							}
							
							

						}
					}
					
					if (args[0].equalsIgnoreCase("watch")) {
					    if (Arenas.getArenas() == null || Arenas.getArenas().size() == 0) {
					        p.sendMessage(Utils.FormatText(p, messages.getConfig().getString("no-arenas")));
					        return true;
					    }

					    if (p.hasPermission("murder.watch")) {
					        ArrayList<Arena> arenas = new ArrayList<>();
					        if (Arenas.getArenas().size() > 0) {
					            for (Arena arena1 : Arenas.getArenas()) {
					                if (arena1.getState() == GameState.INGAME && arena1.players.size() < SpawnSize(arena1)) {
					                    arenas.add(arena1);
					                }
					            }
					        }

					        if (arenas.size() > 0) {
					            Arena a = arenas.get(0);

					            for (Arena ar : arenas) {
					                if (ar.players.size() > a.players.size()) {
					                    a = ar;
					                }
					            }

					            if (!Arenas.isInArena(p) && a != null) {
					                saveInventory(p);
					                Arenas.addArena(p, a);
					                playerArena.put(p.getUniqueId(), a);
					                if (spectatorManager != null) {
					                    spectatorManager.makeSpectator(p, a, "§7Você entrou como espectador.");
					                }
					            }
					        }
					    }
					}
					
					if (args[0].equalsIgnoreCase("reload")) {

						if (!p.isOp() && !p.hasPermission("murder.admin")) {
							return true;
						}
						reloadConfig();
						arenas.reload();
						messages.reload();
						rewards.reload();
						settings.reload();
						data.reload();
						player.sendMessage(ChatColor.GREEN + "Config carregada com sucesso!");

					}
					if (args[0].equalsIgnoreCase("admin")) {

						if (!p.isOp() && !p.hasPermission("murder.admin")) {
							return true;
						}
                        sendMurderAdminHelp(p, "principal");
                        return true;
					}
				    }
					

				if (args.length == 3) {

					if (args[0].equalsIgnoreCase("setmurder")) {
						if (!p.isOp() && !p.hasPermission("murder.admin")) {
							return true;
						}
						List<String> h = arenas.getConfig().getStringList("arena-list");
						if (!h.contains(args[1])) {
							p.sendMessage(Utils.FormatText(p, messages.getConfig().getString("arena-not-exits")));
							return true;
						}

						arenas.getConfig().set("MurderAmount." + args[1], Integer.parseInt(args[2]));
						arenas.save();
						p.sendMessage("§6§lMURDER> §aQuantidade de Murder setada para " + Integer.parseInt(args[2])
								+ "!");

					}

					if (args[0].equalsIgnoreCase("setdete")) {
						if (!p.isOp() && !p.hasPermission("murder.admin")) {
							return true;
						}
						List<String> h = arenas.getConfig().getStringList("arena-list");
						if (!h.contains(args[1])) {
							p.sendMessage(Utils.FormatText(p, messages.getConfig().getString("arena-not-exits")));
							return true;
						}

						arenas.getConfig().set("DetectiveAmount." + args[1], Integer.parseInt(args[2]));
						arenas.save();
						p.sendMessage("§6§lMURDER> §aQuantidade de Detetive setada para " + Integer.parseInt(args[2])
								+ "!");

					}

					if (args[0].equalsIgnoreCase("settime")) {
						if (!p.isOp() && !p.hasPermission("murder.admin")) {
							return true;
						}
						List<String> h = arenas.getConfig().getStringList("arena-list");
						if (!h.contains(args[1])) {
							p.sendMessage(Utils.FormatText(p, messages.getConfig().getString("arena-not-exits")));
							return true;
						}

						arenas.getConfig().set("Time." + args[1], Integer.parseInt(args[2]));
						arenas.save();
						p.sendMessage("§6§lMURDER> §aTempo de partida setado para "
								+ Utils.formattominutes(Integer.parseInt(args[2])) + "!");

					}
					if (args[0].equalsIgnoreCase("setmin")) {
						if (!p.isOp() && !p.hasPermission("murder.admin")) {
							return true;
						}
						List<String> h = arenas.getConfig().getStringList("arena-list");
						if (!h.contains(args[1])) {
							p.sendMessage(Utils.FormatText(p, messages.getConfig().getString("arena-not-exits")));
							return true;
						}

						arenas.getConfig().set("MinPlayers." + args[1], Integer.parseInt(args[2]));
						arenas.save();
						p.sendMessage("§6§lMURDER> §aMinimo de jogadores setado para "
								+ arenas.getConfig().getInt("MinPlayers." + args[1]) + "!");

					}
					if (args[0].equalsIgnoreCase("setspawn")) {
						if (!p.isOp() && !p.hasPermission("murder.admin")) {
							return true;
						}
						List<String> h = arenas.getConfig().getStringList("arena-list");
						if (!h.contains(args[1])) {
							p.sendMessage(Utils.FormatText(p, messages.getConfig().getString("arena-not-exits")));
							return true;
						}
						Arena a = Arenas.getArena(args[1]);
						addSpawn(p, a, Integer.parseInt(args[2]));
					}

					if (args[0].equalsIgnoreCase("setgold")) {
						if (!p.isOp() && !p.hasPermission("murder.admin")) {
							return true;
						}
						List<String> h = arenas.getConfig().getStringList("arena-list");
						if (!h.contains(args[1])) {
							p.sendMessage(Utils.FormatText(p, messages.getConfig().getString("arena-not-exits")));
							return true;
						}
						Arena a = Arenas.getArena(args[1]);
						addGold(p, a, Integer.parseInt(args[2]));
					}
				}
				if (args.length == 2) {

					if (args[0].equalsIgnoreCase("ver")) {
						if (Bukkit.getPlayer(args[1]) == null) {
							p.sendMessage(Utils.FormatText(p, messages.getConfig().getString("not-online")));
							return true;
						}
						Player tar = Bukkit.getPlayer(args[1]);
						if (getPlayerData(tar) != null) {
							for (int i = 1; i < 15; i++) {
								player.sendMessage("");
							}

							List<String> list = messages.getConfig().getStringList("other-info-message");
							for (String s : list) {
								player.sendMessage(Utils.FormatText(tar, s.replaceAll("%player%", tar.getName())
										.replaceAll("%loses%", String.valueOf(getPlayerData(tar).getloses()))
										.replaceAll("%wins%", String.valueOf(getPlayerData(tar).getwins()))
										.replaceAll("%kills%", String.valueOf(getPlayerData(tar).getkill()))
										.replaceAll("%score%", String.valueOf(getPlayerData(tar).getscore()))
										.replaceAll("%deaths%", String.valueOf(getPlayerData(tar).getdeaths()))));

							}

						}
					}

					//if (args[0].equalsIgnoreCase("join")) {
						//List<String> h = arenas.getConfig().getStringList("arena-list");
						//if (!h.contains(args[1])) {
							//p.sendMessage(Utils.FormatText(p, messages.getConfig().getString("arena-not-exits")));
							//return true;
						
						//}
						//Arena a = Arenas.getArena(args[1]);
						//a.addPlayer(p);
					//}

					if (args[0].equalsIgnoreCase("setwait")) {
						if (!p.isOp() && !p.hasPermission("murder.admin")) {
							return true;
						}
						setWait(p);
						p.sendMessage("§aWaiting lobby global setado com sucesso. Todas as salas publicas, privadas e modos vao usar esse local.");
					}
					if (args[0].equalsIgnoreCase("setspec")) {
						if (!p.isOp() && !p.hasPermission("murder.admin")) {
							return true;
						}
						List<String> h = arenas.getConfig().getStringList("arena-list");
						if (!h.contains(args[1])) {
							p.sendMessage(Utils.FormatText(p, messages.getConfig().getString("arena-not-exits")));
							return true;
						}
						Arena a = Arenas.getArena(args[1]);
						setSpec(p, a);
						p.sendMessage(Utils.FormatText(p, messages.getConfig().getString("spectate-set-message")));
					}
					//if (args[0].equalsIgnoreCase("iniciar")) {
						//if (!p.isOp() && !p.hasPermission("murder.iniciar")) {
							//return true;
							
						//}
						//List<String> h = arenas.getConfig().getStringList("arena-list");
						//if (!h.contains(args[1])) {
							//p.sendMessage(Utils.FormatText(p, messages.getConfig().getString("arena-not-exits")));
							//return true;
						//}
						//Arena a = Arenas.getArena(args[1]);
						//if (a.players.size() > 0) {
							//a.start();
						//}
					//}
			
					
					if (args.length > 0 && args[0].equalsIgnoreCase("iniciar")) {
					    if (!p.isOp() && !p.hasPermission("murder.iniciar")) {
					        return true;
					    }
					    List<String> h = arenas.getConfig().getStringList("arena-list");
					    if (args.length < 2 || !h.contains(args[1])) {
					        p.sendMessage(Utils.FormatText(p, messages.getConfig().getString("arena-not-exits")));
					        return true;
					    }
					    Arena a = Arenas.getArena(args[1]);
					    if (a.players.size() > 0) {
					        a.start();
					    }
					}
					

					if (args[0].equalsIgnoreCase("parar")) {
						if (!p.isOp() && !p.hasPermission("murder.stop")) {
							return true;
						}
						List<String> h = arenas.getConfig().getStringList("arena-list");
						if (!h.contains(args[1])) {
							p.sendMessage(Utils.FormatText(p, messages.getConfig().getString("arena-not-exits")));
							return true;
						}
						Arena a = Arenas.getArena(args[1]);
						if (a.getState() != GameState.LOBBY) {
							a.stop("reload");
						}
					}

					if (args[0].equalsIgnoreCase("criar")) {
						if (!p.isOp() && !p.hasPermission("murder.admin")) {
							return true;
						}
						createarena(args[1], p);
					}

					if (args[0].equalsIgnoreCase("apagar")) {
						if (!p.isOp() && !p.hasPermission("murder.admin")) {
							return true;
						}
						removearena(args[1], p);
					}
				}
			

				if (args.length == 1) {

					if (args[0].equalsIgnoreCase("leave")) {
						if (!Arenas.isInArena(p)) {
							p.sendMessage(Utils.FormatText(p, messages.getConfig().getString("not-in-arena")));
							return true;
							
						}
						Arena a = Arenas.getArena(p);
						if (!a.specs.contains(p)) {
							a.removePlayer(p, "leave");
							p.sendMessage("§6§lMURDER §7> §cVoce saiu da partida.");
			
						}

						if (a.specs.contains(p)) {

							setup(p);

							if (a.players.contains(p)) {
								a.players.remove(p);
							}
							a.specs.remove(p);
							restoreInventory(p);
							Arenas.removeArena(p);
							if (!getConfig().getBoolean("send-to-server-on-leave")) {
								p.teleport(getLobby());
							}
							if (getConfig().getBoolean("send-to-server-on-leave")) {
								ByteArrayDataOutput out = ByteStreams.newDataOutput();
								out.writeUTF("Connect");
								out.writeUTF(getConfig().getString("lobby-server"));

								p.sendPluginMessage(this, "BungeeCord", out.toByteArray());
							}
							p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
						}

					}

					if (args[0].equalsIgnoreCase("setlobby")) {
						if (!p.isOp() && !p.hasPermission("murder.admin")) {
							return true;
						}
						setLobby(p);
						p.sendMessage(Utils.FormatText(p, messages.getConfig().getString("lobby-set-message")));
					}

				}
			}
		}
		return true;
	}


	@Override
	public void onDisable() {
		if (lunarRichPresenceManager != null) lunarRichPresenceManager.stop();
		if (titleManager != null) titleManager.stop();
		if (cosmeticNPCManager != null) cosmeticNPCManager.removeAll();
		
	    for (World worlds : Bukkit.getWorlds()) {
	        for (Item entities : worlds.getEntitiesByClass(Item.class)) {
	          if (this.itemList.contains(entities))
	            entities.remove(); 
	        }
	    }
	
		if (getConfig().getBoolean("update-data-on-server-stop")) {
			if (datalist.size() > 0) {
				for (PlayerData data : getPlayersData()) {
					if (data != null) {

						if (!getConfig().getBoolean("mysql")) {
							api.setNonSQLData(data.p, data.getkill(), data.getdeaths(), data.getloses(), data.getwins(),
									data.getcoins(), data.getscore());
						}
						if (getConfig().getBoolean("mysql")) {
							api.setSQLDataInstantly(data.p, data.getkill(), data.getdeaths(), data.getloses(),
									data.getwins(), data.getcoins(), data.getscore());
						}

					}
				}
			}
		}
		if (Arenas.getArenas() != null && Arenas.getArenas().size() >= 0) {
			for (Arena a : Arenas.getArenas()) {

				if (a != null) {
					a.stop("stop");
				}

			}

		}

		if (getConfig().getBoolean("mysql")) {
			if (sql != null) {
				sql.closeConnectionInstantly();
			}
		}
		

		for (World w : Bukkit.getWorlds()) {
			for (Entity e : w.getEntities()) {

				if (e.getType() == EntityType.DROPPED_ITEM) {
					Item a = (Item) e;
					if (a.getItemStack().getType() == Material
							.getMaterial(settings.getConfig().getInt("dropped-item-id"))) {
						e.remove();
					}
				}
				if (e.getType() == EntityType.ARMOR_STAND) {
					ArmorStand a = (ArmorStand) e;
					Material cfgKnife = Material.getMaterial(settings.getConfig().getInt("murderer-weapon.item-id"));
					if ((knifeSkinManager != null && knifeSkinManager.isKnife(a.getItemInHand())) ||
							(cfgKnife != null && a.getItemInHand().getType() == cfgKnife)) {
						e.remove();
					}
				}
				if (e.getType() == EntityType.ARMOR_STAND) {
					ArmorStand a = (ArmorStand) e;
					if ((gunSkinManager != null && gunSkinManager.isGun(a.getItemInHand())) || a.getItemInHand().getType() == Material.DIAMOND_HOE) {
						e.remove();
					}
				}
			}
		}
	}
	
	

	private void configureCorpseRebornForAmongUsOnly() {
		try {
			Plugin corpseReborn = Bukkit.getPluginManager().getPlugin("CorpseReborn");
			if (corpseReborn instanceof JavaPlugin) {
				JavaPlugin corpsePlugin = (JavaPlugin) corpseReborn;
				// CorpseReborn por padrão spawna corpo em qualquer PlayerDeathEvent.
				// No sMurder o corpo deve existir somente no modo AMONG US/Sabotage.
				// Então desativamos o spawn automático global dele e continuamos usando
				// CorpseAPI.spawnCorpse manualmente apenas nos pontos do Sabotage.
				corpsePlugin.getConfig().set("on-death", false);
				corpsePlugin.saveConfig();
				Bukkit.getConsoleSender().sendMessage("§a[sMurder] CorpseReborn configurado: corpos somente no AMONG US/Sabotage.");
			}
		} catch (Throwable t) {
			Bukkit.getConsoleSender().sendMessage("§c[sMurder] Nao foi possivel configurar CorpseReborn: " + t.getMessage());
		}
	}

	@Override
	public void onEnable() {
		
		
		instance = this;
		rankedManager = new RankedManager(this);
		leaderboardManager = new HologramLeaderboardManager(this);
		lunarRichPresenceManager = new LunarRichPresenceManager(this);
		spectatorManager = new SpectatorManager(this);
		killCamManager = null; // Replay/KillCam removido por performance
		cooldownTime = new HashMap<>();
		cooldownTask = new HashMap<>();
		
	    this.kitManager = new KitManager(this);
	    this.menuManager = new MenuManager(this);
	    this.knifeSkinManager = new KnifeSkinManager(this);
	    this.hatAbilityManager = new HatAbilityManager(this);
	    this.gunSkinManager = new GunSkinManager(this);
    this.roomManager = new RoomManager(this);
	    this.npcManager = new NPCManager(this);
	    this.replayManager = null; // Replay/KillCam removido por performance
	    this.sabotageManager = new SabotageManager(this);
    this.sabotageTaskManager = new SabotageTaskManager(this);
    this.amongUsColorManager = new AmongUsColorManager(this);
    this.amongUsNameTagManager = new AmongUsNameTagManager(this);
	    this.tagManager = new TagManager(this);
	    this.tagManager.setupDefaults();
	    this.titleManager = new TitleManager(this);
	    this.seasonalEventManager = new SeasonalEventManager(this);
        this.seasonalTreasureManager = new SeasonalTreasureManager(this);

		

		nmsver = Bukkit.getServer().getClass().getPackage().getName();
		nmsver = nmsver.substring(nmsver.lastIndexOf(".") + 1);

		if (nmsver.equalsIgnoreCase("v1_8_R1") || nmsver.startsWith("v1_7_")) {
			useOldMethods = true;
		}

		this.api = new MurderAPI(this);
		this.sm = new SignManager(this);
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		if (lunarRichPresenceManager != null) lunarRichPresenceManager.start();
		Bukkit.getServer().getPluginManager().registerEvents(leaderboardManager, this);
		Bukkit.getServer().getPluginManager().registerEvents(new CosmeticNPCListener(this), this);
		if (cosmeticNPCManager != null) cosmeticNPCManager.respawnAll();

		String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
		if (version.contains("1.9") || version.contains("1.10") || version.contains("1.11")
				|| version.contains("1.12") || (version.contains("1.13.3") || (version.contains("1.14.4")))) {
			Bukkit.getServer().getPluginManager().registerEvents(new SwitchItem(this), this);
		}

		Bukkit.getServer().getPluginManager().registerEvents(new SignListener(this), this);
		Bukkit.getServer().getPluginManager().registerEvents(new SignListener2(this), this);
		Bukkit.getServer().getPluginManager().registerEvents(new BlockEvents(this), this);
		Bukkit.getServer().getPluginManager().registerEvents(new ChatEvent(this), this);
		Bukkit.getServer().getPluginManager().registerEvents(new DamageEvent(this), this);
		Bukkit.getServer().getPluginManager().registerEvents(new DeathEvent(this), this);
		Bukkit.getServer().getPluginManager().registerEvents(new DropItem(this), this);
		Bukkit.getServer().getPluginManager().registerEvents(new EntityDamageByEntityEvent(this), this);
		Bukkit.getServer().getPluginManager().registerEvents(new FoodLevel(this), this);
		Bukkit.getServer().getPluginManager().registerEvents(new JoinEvent(this), this);
		Bukkit.getServer().getPluginManager().registerEvents(new LeaveItem(this), this);
		Bukkit.getServer().getPluginManager().registerEvents(new LoginEvent(this), this);
		Bukkit.getServer().getPluginManager().registerEvents(new Motd(this), this);
		Bukkit.getServer().getPluginManager().registerEvents(new NoPainting(this), this);
		Bukkit.getServer().getPluginManager().registerEvents(new NoSpecDamage(this), this);
		Bukkit.getServer().getPluginManager().registerEvents(new OpenVoteGUI(this), this);
		Bukkit.getServer().getPluginManager().registerEvents(new PickUpEvent(this), this);
		Bukkit.getServer().getPluginManager().registerEvents(new QuitEvent(this), this);
		Bukkit.getServer().getPluginManager().registerEvents(new SpectateEvent(this), this);
		Bukkit.getServer().getPluginManager().registerEvents(new SpectatorItem(this), this);
		Bukkit.getServer().getPluginManager().registerEvents(titleManager, this);
		Bukkit.getServer().getPluginManager().registerEvents(seasonalEventManager, this);
		Bukkit.getServer().getPluginManager().registerEvents(seasonalTreasureManager, this);
		// Títulos desativados temporariamente. Mantém o manager registrado só para limpar stands antigos.
		if (titleManager != null) titleManager.stop();
		if (titleManager != null) titleManager.cleanupStrayTitleStands();
		//Bukkit.getServer().getPluginManager().registerEvents(new ThrowableEvent(this), this);
		Bukkit.getServer().getPluginManager().registerEvents(new VoteEvent(this), this);
		Bukkit.getServer().getPluginManager().registerEvents(new WeatherBlock(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new GunEvent(this), this);
		Bukkit.getServer().getPluginManager().registerEvents(new SpeedEvent(this), this);
		Bukkit.getServer().getPluginManager().registerEvents(new SnowballParticles(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new BloodSystem(this), this);
		getServer().getPluginManager().registerEvents(new Modes(this), this);
		getServer().getPluginManager().registerEvents(new ProfileMenu(this), this);
		getServer().getPluginManager().registerEvents(new CosmeticsMenu(this), this);
		// Auras e gadgets do lobby precisam ser registrados; sem isso o scheduler/evento nunca inicia.
		getServer().getPluginManager().registerEvents(new LobbyCosmeticManager(this), this);
		Bukkit.getServer().getPluginManager().registerEvents(hatAbilityManager, this);
		Bukkit.getServer().getPluginManager().registerEvents(new Teleporter(this), this);
		Bukkit.getServer().getPluginManager().registerEvents(new ItemEvent(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new CustomHead(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new ThrowerEvent(this), this);
		Bukkit.getServer().getPluginManager().registerEvents(new TntTagEvent(this), this);
		Bukkit.getServer().getPluginManager().registerEvents(new RoomPasswordGUI(this), this);
		Bukkit.getServer().getPluginManager().registerEvents(sabotageManager, this);
		Bukkit.getServer().getPluginManager().registerEvents(sabotageTaskManager, this);
		Bukkit.getServer().getPluginManager().registerEvents(amongUsColorManager, this);
		Bukkit.getServer().getPluginManager().registerEvents(npcManager, this);
		Bukkit.getServer().getPluginManager().registerEvents(new HidePlayers(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new FragEvent(), this);
		Bukkit.getServer().getPluginManager().registerEvents(menuManager, this);

		// Kits reduzidos para ficar mais útil e menos confuso no lobby.
		// Chance Murder/Detective altera o sorteio da role; Teleporter funciona para Inocente e Detetive.
		kitManager.registerKit(new AdvancedKit("chance_murder", "Chance Murder", Material.DIAMOND_SWORD, KitType.ROLE_CHANCE, "Aumenta sua chance de ser Assassino."));
		kitManager.registerKit(new AdvancedKit("chance_detective", "Chance Detetive", Material.BOW, KitType.ROLE_CHANCE, "Aumenta sua chance de ser Detetive."));
		kitManager.registerKit(new AdvancedKit("teleporter", "Teleporter", Material.ENDER_PEARL, KitType.INNOCENT_DETECTIVE, "Recebe um teleportador de uso único."));
		kitManager.registerKit(new AdvancedKit("ghost", "Fantasma", Material.FEATHER, KitType.INNOCENT_DETECTIVE, "Recebe velocidade e invisibilidade por alguns segundos."));
		kitManager.registerKit(new SpeedKit());
		kitManager.registerKit(new AdvancedKit("hide_runner", "Corredor", Material.SUGAR, KitType.INNOCENT, "Kit exclusivo do Esconde-Esconde: Speed I permanente."));
		kitManager.registerKit(new AdvancedKit("hide_camouflage", "Camuflagem", Material.INK_SACK, KitType.INNOCENT, "Kit exclusivo do Esconde-Esconde: invisibilidade curta."));
		kitManager.registerKit(new AdvancedKit("tnt_void_saver", "Salva Void", Material.FEATHER, KitType.INNOCENT, "Kit exclusivo do TNTTag: proteção curta contra morte."));
		kitManager.registerKit(new AdvancedKit("tnt_sprinter", "Velocista TNT", Material.SUGAR, KitType.INNOCENT, "Kit exclusivo do TNTTag: velocidade maior."));
		kitManager.registerKit(new AdvancedKit("sabotage_engineer", "Engenheiro", Material.REDSTONE_COMPARATOR, KitType.INNOCENT_DETECTIVE, "Kit exclusivo do AMONG US: visão e agilidade nas tasks."));
		kitManager.registerKit(new AdvancedKit("sabotage_detective", "Perito", Material.EYE_OF_ENDER, KitType.DETECTIVE, "Kit exclusivo do AMONG US para Detetive."));
		
		
		//kitManager.registerKit(new SpeedKit());
		//for (Arena arena : Arenas.getArenas()) {
		   // Bukkit.getPluginManager().registerEvents(arena, this);
		//}
		
		
		//Bukkit.getServer().getPluginManager().registerEvents(new SetCoinsCommand(this), this);
		//getCommand("setcoins").setExecutor(new SetCoinsCommand(this));
		//Bukkit.getServer().getPluginManager().registerEvents(new NpcEvent(), this);
		//CitizensAPI.getTraitFactory().registerTrait(net.citizensnpcs.api.trait.TraitInfo.create(NPCTrait.class).withName("NPC1"));
		
		
		Bukkit.getConsoleSender().sendMessage("###########################################################");
	    Bukkit.getConsoleSender().sendMessage("");
	    Bukkit.getConsoleSender().sendMessage("§6§lMURDER §7v0.0.2 §eBy §9spwtyz");
	    Bukkit.getConsoleSender().sendMessage("");
	    Bukkit.getConsoleSender().sendMessage("§aMurder iniciado com sucesso!");
	    Bukkit.getConsoleSender().sendMessage("§3> Plugin atualmente em fase de testes!");
	    Bukkit.getConsoleSender().sendMessage("§cAviso: §9HolographicDisplays §7e dependencia recomendada.");
	    LoadConfigFiles();
	    cleanupDynamicRoomConfigKeys();
		mysteryBoxManager = new MysteryBoxManager(this);
		battlePassManager = new BattlePassManager(this);
		cosmeticNPCManager = new CosmeticNPCManager(this);
		cosmeticNPCManager.respawnAll();
        loadarenas();
        if (roomManager != null) {
            roomManager.loadPublicRooms();
        }
        if (npcManager != null) {
            npcManager.spawnSavedNPCs();
        }
	    Bukkit.getConsoleSender().sendMessage("");
	    Bukkit.getConsoleSender().sendMessage("###########################################################");

		if (getConfig().getBoolean("mysql")) {

			String host = getConfig().getString("host");
			String port = getConfig().getString("port");
			String database = getConfig().getString("database");
			String username = getConfig().getString("username");
			String password = getConfig().getString("password");
			this.sql = new MySQL(this, host, port, database, username, password);

		}

		if (this.levelManager == null) {
			this.levelManager = new LevelManager(this);
			Bukkit.getServer().getPluginManager().registerEvents(new LevelListener(this), this);
			this.levelManager.spawnSavedLeaderboard();
			if (this.leaderboardManager != null) {
				this.leaderboardManager.spawnSaved();
				this.leaderboardManager.startAutoUpdate();
			}
			for (Player online : Bukkit.getOnlinePlayers()) {
				this.levelManager.ensure(online);
			}
		}

		if (getConfig().getBoolean("bungee")) {

			Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		}

		new BukkitRunnable() {

			@Override
			public void run() {

				for (Arena x : Arenas.getArenas()) {

					sm.updateSigns(x);

				}

				for (Player p : Bukkit.getOnlinePlayers()) {

					if (getPlayerData(p) == null) {

						registerNewData(p);

					}

					if (Arenas.isInArena(p)) {
						Arena a = Arenas.getArena(p);

						if (a.getState() == GameState.INGAME) {

							if (a.innocents.contains(p)) {

								if (p.getItemInHand().getType() == Material
										.getMaterial(settings.getConfig().getInt("track.item-id"))) {
									if (a.bowloc != null) {
										Location loc = a.bowloc;
										if (loc.getWorld().getName().equalsIgnoreCase(p.getWorld().getName())) {
											double distanceto = p.getLocation().distance(loc);
											if (distanceto != 0 && p.getLocation().distance(loc) > 0) {

												if (p.getItemInHand().getType() == Material.COMPASS) {
													p.setCompassTarget(loc);

												}

												api.sendActionBar(p,
														Utils.FormatText(p, messages.getConfig()
																.getString("bow-location-message").replaceAll("%loc%",
																		String.valueOf(Math.round(distanceto)))));

											}
										}
									}
								}
							}

							if (a.murder.contains(p)) {

								ItemStack hand = p.getItemInHand();
								if (hand != null && hand.getType() == Material.COMPASS && a.getGameMode() == GameModeType.HIDE_AND_SEEK) {
									if (!a.isHideSeekRadarUnlocked()) {
										int remaining = a.getHideSeekRadarUnlockSecondsRemaining();
										api.sendActionBar(p, "§eRadar §7> §cBloqueado. Libera no final da partida" + (remaining > 0 ? " em §f" + remaining + "s" : "§c."));
									} else {
										Player target = getNearestHideSeekHider(p, a);
										if (target != null) {
											p.setCompassTarget(target.getLocation());
											double distance = p.getLocation().distance(target.getLocation());
											api.sendActionBar(p, "§eRadar §7> §fEscondido mais próximo: §a" + Math.round(distance) + "m");
										} else {
											api.sendActionBar(p, "§eRadar §7> §cNenhum escondido encontrado.");
										}
									}
								} else if (hand != null && hand.getType() == Material
										.getMaterial(settings.getConfig().getInt("murder-track.item-id"))) {

									Player target = getNearestName(p,
											settings.getConfig().getDouble("murder-track.range"));
									if (target != null && target.isOnline()) {

										if (a.players.contains(target)) {

											if (target.getWorld().getName().equalsIgnoreCase(p.getWorld().getName())) {

												api.sendActionBar(p, Utils.FormatText(p,
														messages.getConfig().getString("near-player-location-message")
																.replaceAll("%player%", target.getName())
																.replaceAll("%distance%", String.valueOf(Math
																		.round(getNearestDouble(p, settings.getConfig()

																				.getDouble("murder-track.range")))))));
											}
										}
									}
								}

							}
						}
					}

				}

				// VISIBILITY FIX:
				// O plugin antigo fazia hidePlayer/showPlayer em loop para todos os players online.
				// Isso deixava jogadores invisíveis dentro da partida e também pesava o TPS.
				// Agora a visibilidade é atualizada pela Arena somente quando muda o estado do player
				// (entrar, morrer, virar espectador, resetar ou sair).
				for (Player p : Bukkit.getOnlinePlayers()) {
					if (getConfig().getBoolean("bungee")) {
						if (!Arenas.isInArena(p)) {
							if (p.getLocation().getBlockY() <= 0) {
								p.teleport(getLobby());
							}
						}
					}
					if (Arenas.isInArena(p)) {
						Arena a = Arenas.getArena(p);
						if (a.getState() == GameState.LOBBY || a.getState() == GameState.STARTING) {
							if (p.getLocation().getBlockY() <= 0) {
								Location wait = getWait(a);
								if (wait != null) {
									p.teleport(wait);
								}
							}
						}

						if (a.getState() == GameState.INGAME && !a.wincheck) {
							if (p.getLocation().getBlockY() <= 0) {
								if (!p.getAllowFlight()) {
									p.setAllowFlight(true);
									p.setFlying(true);
									p.teleport(getSpec(a));
									if (a.players.contains(p) && Arenas.isInArena(p)) {
										a.removePlayer(p, "death");
									}
									if (getPlayerData(p) != null) {

										getPlayerData(p).adddeaths(1);
										getPlayerData(p).addlose(1);
									}
								}
							}
						}

						if (a.getState() == GameState.INGAME && a.wincheck) {
							if (p.getLocation().getBlockY() <= 0) {
								p.setAllowFlight(true);
								p.setFlying(true);
								p.teleport(getSpec(a));
								setup(p);
							}
						}
					}

				}
			}
		}.runTaskTimer(this, 20, 20);

		if (getConfig().getBoolean("bungee")) {
			new BukkitRunnable() {

				@Override
				public void run() {

					if (Arenas.getArenas().size() == 1 && Arenas.getArenas() != null) {
						Arena a = Arenas.getArenas().get(0);
						if (Bukkit.getOnlinePlayers().size() > 0) {
							for (Player p : Bukkit.getOnlinePlayers()) {

								if (!Arenas.isInArena(p)) {
									if (a.getState() == GameState.INGAME) {
										if (!getConfig().getBoolean("send-to-server-on-leave")) {
											p.teleport(getLobby());
										}

										if (getConfig().getBoolean("send-to-server-on-leave")) {
											ByteArrayDataOutput out = ByteStreams.newDataOutput();
											out.writeUTF("Connect");
											out.writeUTF(getConfig().getString("lobby-server"));

											p.sendPluginMessage(plugin(), "BungeeCord", out.toByteArray());
										}

										return;
									}

									a.addPlayer(p);

								}
							}

						}
					}

				}
			}.runTaskLater(this, 20);
		}
		if (getConfig().getBoolean("bungee"))

		{
			if (Arenas.getArenas().size() > 1) {
				if (Bukkit.getOnlinePlayers().size() > 0) {
					for (Player p : Bukkit.getOnlinePlayers()) {
						setUpForMultiMaps(p);
					}
				}

				for (Arena a : Arenas.getArenas()) {
					point.put(a.getName(), 0);
				}
				new BukkitRunnable() {

					@Override
					public void run() {
						if (Bukkit.getOnlinePlayers().size() >= settings.getConfig()
								.getInt("min-players-to-start-bungee")) {

							StartMap();
							this.cancel();
							return;
						}

					}
				}.runTaskTimer(this, 20, 20);

			}
		}

		passable.add(Material.AIR);
		passable.add(Material.WATER);
		passable.add(Material.STATIONARY_WATER);
		passable.add(Material.WALL_BANNER);
		passable.add(Material.WALL_SIGN);
		passable.add(Material.CARPET);
		passable.add(Material.CARROT_ITEM);
		passable.add(Material.CROPS);
		passable.add(Material.DEAD_BUSH);
		passable.add(Material.DIODE);
		passable.add(Material.DIODE_BLOCK_OFF);
		passable.add(Material.DIODE_BLOCK_ON);

		passable.add(Material.REDSTONE_TORCH_OFF);
		passable.add(Material.REDSTONE_TORCH_OFF);
		passable.add(Material.TORCH);
		passable.add(Material.DOUBLE_PLANT);
		passable.add(Material.LONG_GRASS);

		if (user.equalsIgnoreCase("10045") || user.equalsIgnoreCase("1700") || user.equalsIgnoreCase("159")) {
			disabled = false;

		}
		if (!Utils.isInt(user)) {
			disabled = false;
		}

		for (Player p : Bukkit.getOnlinePlayers()) {

			registerNewData(p);
			setScoreboard(p);

		}

	}


	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (Arenas.isInArena((Player) e.getWhoClicked())) {
			if (e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
				e.setCancelled(true);
			}
		}
		if (!(e.getInventory().getTitle()
				.equalsIgnoreCase(Utils.FormatText2(settings.getConfig().getString("arenas-inventory-title"))))) {
			return;
		}
		if (e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
			e.setCancelled(true);

		}
		if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) {
			return;

		}
		Player p = (Player) e.getWhoClicked();
		if (e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()) {
			String clickedName = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
			if (clickedName.equalsIgnoreCase("Criar Sala")) {
				e.setCancelled(true);
				e.getWhoClicked().closeInventory();
				p.performCommand("m criarsala");
				return;
			}
		}
		if (!e.getInventory().contains(e.getCurrentItem())) {
			return;
		}

		if (e.getCurrentItem().getType() == Material.getMaterial(settings.getConfig().getInt("GUI.next-page-item-id"))
				&& e.getCurrentItem().getDurability() == settings.getConfig().getInt("GUI.next-page-item-durability")) {
			if (e.getSlot() == 26 || e.getSlot() == 35) {

				if (sd.containsKey(p.getName())) {
					p.openInventory(sd.get(p.getName()).hash.get(e.getInventory()));
					return;
				}

			}
		}
		if (e.getCurrentItem().getType() == Material
				.getMaterial(settings.getConfig().getInt("GUI.previous-page-item-id"))
				&& e.getCurrentItem().getDurability() == settings.getConfig()
						.getInt("GUI.previous-page-item-durability")) {
			if (e.getSlot() == 18 || e.getSlot() == 27) {
				if (sd.containsKey(p.getName())) {
					p.openInventory(sd.get(p.getName()).hash2.get(e.getInventory()));

					return;
				}
			}

		}
		String clickedArenaName = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
		Arena A = Arenas.getArena(clickedArenaName);

		// Se o item da sala mostra "Privada de Nome" ou nome customizado,
		// o ID interno continua ROOM-*. Então o join precisa procurar pelo displayName também.
		if (A == null) {
			for (Arena arenaSearch : Arenas.getArenas()) {
				if (arenaSearch == null) continue;
				String display = ChatColor.stripColor(arenaSearch.getRoomDisplayName());
				if (display != null && display.equalsIgnoreCase(clickedArenaName)) {
					A = arenaSearch;
					break;
				}
			}
		}

		if (A != null) {
			Player clicker = (Player) e.getWhoClicked();
			Room room = roomManager == null ? null : roomManager.getRoomByArena(A);
			if (room != null && room.hasPassword() && (roomManager == null || roomManager.getRoom(clicker) != room) && !A.canManageRoom(clicker)) {
				e.getWhoClicked().closeInventory();
				RoomPasswordGUI.openJoinPassword(this, clicker, room);
				return;
			}
			if (room != null && roomManager != null) {
				roomManager.addPlayerToRoom(clicker, room);
			} else {
				A.addPlayer(clicker);
			}
			e.getWhoClicked().closeInventory();
		}

	}

	public void OpenSpec(Player p) {
		if (Arenas.isInArena(p)) {
			Arena a = Arenas.getArena(p);
			if (!a.players.contains(p)) {
				Inventory inv = Bukkit.createInventory(null, settings.getConfig().getInt("spectate-inventory-size"),
						Utils.FormatText2(settings.getConfig().getString("spectate-inventory-title")));

				for (Player online : a.getPlayers2()) {
					ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
					SkullMeta meta = (SkullMeta) item.getItemMeta();
					meta.setOwner(online.getName());
					meta.setDisplayName(ChatColor
							.valueOf(settings.getConfig().getString("Spectate-Display-Name-Color").toUpperCase())
							+ online.getName());
					meta.setLore(Arrays
							.asList(Utils.FormatText(online, settings.getConfig().getString("Spectate-Display-Lore"))));
					item.setItemMeta(meta);
					inv.addItem(item);
				}
				p.openInventory(inv);

			}
		}
	}

	public void OpenVote(Player p) {
		if (Arenas.getArenas().size() <= 1) {
			return;

		}

		Inventory inv = Bukkit.createInventory(null, settings.getConfig().getInt("vote-inventory.size"),
				Utils.FormatText2(settings.getConfig().getString("vote-inventory.name")));
		inv.clear();
		for (Arena a : Arenas.getArenas()) {
			if (!getConfig().contains(a.getName())) {
				ItemStack s = new ItemStack(Material.PAPER);
				ItemMeta sm = s.getItemMeta();
				sm.setDisplayName(
						ChatColor.valueOf(settings.getConfig().getString("map-displayname-in-gui-color").toUpperCase())
								+ a.getName());
				sm.setLore(Arrays.asList(Utils.FormatText(p, settings.getConfig().getString("map-item-lore")
						.replaceAll("%votes%", String.valueOf(point.get(a.getName()))))));
				s.setItemMeta(sm);
				inv.addItem(s);
			}
			if (getConfig().contains(a.getName())) {

				String string = getConfig().getString(a.getName());
				String[] data = string.split(";");

				ItemStack s = new ItemStack(Material.getMaterial(Integer.parseInt(data[0])), 1,
						(short) Integer.parseInt(data[1]));
				ItemMeta sm = s.getItemMeta();
				sm.setDisplayName(
						ChatColor.valueOf(settings.getConfig().getString("map-displayname-in-gui-color").toUpperCase())
								+ a.getName());
				sm.setLore(Arrays.asList(Utils.FormatText(p, settings.getConfig().getString("map-item-lore")
						.replaceAll("%votes%", String.valueOf(point.get(a.getName()))))));
				s.setItemMeta(sm);
				inv.addItem(s);
			}

		}
		p.openInventory(inv);
	}

	public Plugin plugin() {
		return this;
	}

	public void registerNewData(Player p) {
		if (!pdata.containsKey(p.getName())) {
			PlayerData data = new PlayerData(this, p);
			pdata.put(p.getName(), data);
			datalist.add(data);

		}
	}

	public void removearena(String name, Player p) {
		List<String> h = arenas.getConfig().getStringList("arena-list");
		if (!h.contains(name)) {
			p.sendMessage(Utils.FormatText(p, messages.getConfig().getString("arena-not-exits")));
			return;
		}
		h.remove(name);
		arenas.getConfig().set("arena-list", h);
		arenas.save();

		if (arenas.getConfig().contains("MinPlayers." + name)) {
			arenas.getConfig().set("MinPlayers." + name, null);
		}

		if (arenas.getConfig().contains("Spawns." + name)) {
			arenas.getConfig().set("Spawns." + name, null);
		}
		if (arenas.getConfig().contains("Wait." + name)) {
			arenas.getConfig().set("Wait." + name, null);
		}

		if (arenas.getConfig().contains("Spectator." + name)) {
			arenas.getConfig().set("Spectator." + name, null);
		}
		if (arenas.getConfig().contains("Signs." + name)) {
			arenas.getConfig().set("Signs." + name, null);
		}
		if (arenas.getConfig().contains("Gold." + name)) {
			arenas.getConfig().set("Gold." + name, null);
		}
		if (arenas.getConfig().contains("MurderAmount." + name)) {
			arenas.getConfig().set("MurderAmount." + name, null);
		}
		if (arenas.getConfig().contains("DetectiveAmount." + name)) {
			arenas.getConfig().set("DetectiveAmount." + name, null);
		}
		if (arenas.getConfig().contains(name)) {
			arenas.getConfig().set(name, null);
		}
		arenas.save();

		p.sendMessage(Utils.FormatText(p, messages.getConfig().getString("remove-arena-message")));
	}

	public void removeCompass(Player p) {

		// Remove qualquer bússola/rastreador de partida, incluindo o item antigo que ficava com nome "Partidas".
		for (int i = 0; i < p.getInventory().getSize(); i++) {
			ItemStack item = p.getInventory().getItem(i);
			if (item == null || item.getType() != Material.COMPASS) continue;
			if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
				String name = ChatColor.stripColor(item.getItemMeta().getDisplayName()).toLowerCase();
				if (name.contains("partidas") || name.contains("rastreador") || name.contains("tracker")) {
					p.getInventory().setItem(i, null);
				}
			} else {
				p.getInventory().setItem(i, null);
			}
		}
		p.updateInventory();
	}

	public void removeitem(Player p) {
		if (p.getItemInHand().getAmount() == 1) {
			p.setItemInHand(null);
			p.updateInventory();
			return;
		}
		p.getItemInHand().setAmount(p.getItemInHand().getAmount() - 1);
		p.updateInventory();
	}

	public void removePlayerData(Player p) {
		if (pdata.containsKey(p.getName())) {
			PlayerData data = pdata.get(p.getName());

			if (!getConfig().getBoolean("mysql")) {
				api.setNonSQLData(data.p, data.getkill(), data.getdeaths(), data.getloses(), data.getwins(),
						data.getcoins(), data.getscore());
			}
			if (getConfig().getBoolean("mysql")) {
				api.setSQLData(data.p, data.getkill(), data.getdeaths(), data.getloses(), data.getwins(),
						data.getcoins(), data.getscore());
			}
			data.reset();
			if (datalist.contains(data)) {
				datalist.remove(data);
			}
			pdata.remove(p.getName());

		}
	}

	public void removePotion(Block b, Arena a) {
		List<String> c = arenas.getConfig().getStringList("Potions." + a.getTemplateName());
		if (!c.contains(getStringFromLocation(b.getLocation()))) {
			return;
		}
		c.remove(getStringFromLocation(b.getLocation()));
		arenas.getConfig().set("Potions." + a.getTemplateName(), c);
		arenas.save();

	}

	public void restoreInventory(Player player) {

		if (player.isOnline() && player != null) {
			if (level.containsKey(player.getName())) {
				player.setLevel(level.get(player.getName()));
				level.remove(player.getName());
			}
			if (xp.containsKey(player.getName())) {
				player.setExp(xp.get(player.getName()));
				xp.remove(player.getName());
			}
			
			// ARMADURA TESTE
			//if (inventoryContents.containsKey(player.getName())) {
				//player.getInventory().clear();
				//player.getInventory().setContents(inventoryContents.get(player.getName()));
				//inventoryContents.remove(player.getName());
			//}
			//if (armourContents.containsKey(player.getName())) {
				//player.getInventory().setArmorContents(null);
				//player.getInventory().setArmorContents(armourContents.get(player.getName()));
				//armourContents.remove(player.getName());
			//}
			
			if (gamemode.containsKey(player.getName())) {
				player.setGameMode(gamemode.get(player.getName()));
				gamemode.remove(player.getName());
			}

			player.updateInventory();

		}

	}

	public void saveInventory(Player player) {

		level.put(player.getName(), player.getLevel());
		xp.put(player.getName(), player.getExp());
		armourContents.put(player.getName(), player.getInventory().getArmorContents());
		inventoryContents.put(player.getName(), player.getInventory().getContents());
		gamemode.put(player.getName(), player.getGameMode());
		player.getInventory().clear();
		//player.getInventory().setArmorContents(null);
		player.updateInventory();

	}

	public void sendPlayers() {

		new BukkitRunnable() {

			@Override
			public void run() {
				for (Player p : Bukkit.getOnlinePlayers()) {
					bungee.addPlayer(p);
				}

			}
		}.runTaskLater(this, 20 * settings.getConfig().getInt("time-until-game-start"));
	}

	public void setCompass(Player p) {
		if (Arenas.isInArena(p)) {
			Arena a = Arenas.getArena(p);
			if (a != null && a.players.contains(p)) {
				Material trackMaterial = Material.getMaterial(settings.getConfig().getInt("track.item-id", 345));
				if (trackMaterial == null) {
					trackMaterial = Material.COMPASS;
				}

				ItemStack s = new ItemStack(trackMaterial);
				s.setDurability((short) settings.getConfig().getInt("track.item-subid", 0));
				ItemMeta sm = s.getItemMeta();
				String trackerName = settings.getConfig().getString("track.item-name", "&eRastreador &7(Clique)");
				// Correção: dentro da partida esse item é rastreador, não menu de Partidas.
				if (ChatColor.stripColor(Utils.FormatText(p, trackerName)).toLowerCase().contains("partidas")) {
					trackerName = "&eRastreador &7(Clique)";
				}
				sm.setDisplayName(Utils.FormatText(p, trackerName));
				ArrayList<String> lore = new ArrayList<>();
				String trackerLore = settings.getConfig().getString("track.item-lore", "&7Aponta para o jogador mais próximo.");
				lore.add(Utils.FormatText(p, trackerLore));
				sm.setLore(lore);
				s.setItemMeta(sm);
				removeCompass(p);
				p.getInventory().setItem(4, s);
				p.updateInventory();

			}
		}
	}

	public void setLobby(Player p) {
		arenas.getConfig().set("Lobby.main.lobby.world", p.getLocation().getWorld().getName());
		arenas.getConfig().set("Lobby.main.lobby.x", Double.valueOf(p.getLocation().getX()));
		arenas.getConfig().set("Lobby.main.lobby.y", Double.valueOf(p.getLocation().getY()));
		arenas.getConfig().set("Lobby.main.lobby.z", Double.valueOf(p.getLocation().getZ()));

		arenas.getConfig().set("Lobby.main.lobby.yaw", Double.valueOf(p.getLocation().getYaw()));
		arenas.getConfig().set("Lobby.main.lobby.pitch", Double.valueOf(p.getLocation().getPitch()));
		arenas.save();
	}

	public void setMap(Player p) {
		ItemStack s = new ItemStack(Material.getMaterial(settings.getConfig().getInt("map.item-id")));
		s.setDurability((short) settings.getConfig().getInt("map.item-subid"));
		ItemMeta sm = s.getItemMeta();
		sm.setDisplayName(Utils.FormatText(p, settings.getConfig().getString("map.item-name")));
		ArrayList<String> lore = new ArrayList<>();
		lore.add(Utils.FormatText(p, settings.getConfig().getString("map.item-lore")));
		sm.setLore(lore);
		s.setItemMeta(sm);
		p.getInventory().setItem(0, s);
		p.updateInventory();
	}

	public String getScoreboardTitleByMode(Arena arena) {
        if (seasonalEventManager != null && seasonalEventManager.isEventActive()) {
            return seasonalEventManager.getLobbyTitle();
        }
		if (arena == null) {
			return "§e§lMURDER";
		}

		GameModeType mode = arena.getGameMode();

		if (mode == GameModeType.TNT_TAG) {
			return "§c§lTNTTAG";
		}

		if (mode == GameModeType.ALL_MURDER) {
			return "§4§lTODOS ASSASSINOS";
		}

		if (mode == GameModeType.RANKED) {
			return "§6§lRANKED";
		}

		if (mode == GameModeType.HIDE_AND_SEEK) {
			return "§d§lESCONDE-ESCONDE";
		}

		if (mode == GameModeType.SABOTAGE) {
			return "§5§lAMONG US";
		}

		return "§e§lMURDER";
	}


    public String getNextPublicRoomId() {
        int next = 1;
        if (roomManager != null) {
            while (roomManager.getRoom("PUBLIC-" + next) != null) {
                next++;
            }
        }
        return "PUBLIC-" + next;
    }

    public GameModeType parsePublicRoomMode(String input) {
        if (input == null) return GameModeType.NORMAL;
        String key = input.trim().toUpperCase()
                .replace("-", "_")
                .replace(" ", "_");

        if (key.equals("NORMAL") || key.equals("PADRAO") || key.equals("PADRÃO")) return GameModeType.NORMAL;
        if (key.equals("ALL_MURDER") || key.equals("TODOS_MURDER") || key.equals("TODOS_ASSASSINOS") || key.equals("ALL_ASSASSINS") || key.equals("ASSASSINOS")) return GameModeType.ALL_MURDER;
        if (key.equals("TNTTAG") || key.equals("TNT_TAG") || key.equals("TNT")) return GameModeType.TNT_TAG;
        if (key.equals("RANKED") || key.equals("RANQUEADO")) return GameModeType.RANKED;
        if (key.equals("ESCONDE") || key.equals("ESCONDE_ESCONDE") || key.equals("HIDE") || key.equals("HIDE_AND_SEEK")) return GameModeType.HIDE_AND_SEEK;
        if (key.equals("SABOTAGE") || key.equals("AMONGUS") || key.equals("AMONG_US") || key.equals("SABOTAGEM")) return GameModeType.SABOTAGE;

        return GameModeType.valueOf(key);
    }

    public String getRandomAllowedPublicRoomMap(GameModeType mode) {
        List<String> maps = getAllowedPublicRoomMaps(mode);
        if (maps == null || maps.isEmpty()) return null;
        return maps.get(new Random().nextInt(maps.size()));
    }

    public List<String> getAllowedPublicRoomMaps(GameModeType mode) {
        List<String> all = arenas.getConfig().getStringList("arena-list");
        List<String> result = new ArrayList<String>();
        if (all == null) return result;
        List<String> hideMaps = getConfig().getStringList("hide-and-seek.maps");
        for (String map : all) {
            if (map == null || map.trim().isEmpty()) continue;
            if (RoomFeatureLockManager.isMapDisabled(this, map)) continue;

            boolean isHideMap = false;
            if (hideMaps != null) {
                for (String h : hideMaps) {
                    if (h != null && h.trim().equalsIgnoreCase(map.trim())) {
                        isHideMap = true;
                        break;
                    }
                }
            }

            if (mode == GameModeType.HIDE_AND_SEEK) {
                if (hideMaps == null || hideMaps.isEmpty() || isHideMap) result.add(map);
            } else {
                if (!isHideMap) result.add(map);
            }
        }
        return result;
    }

	public String getScoreboardMapName(Arena arena, Player viewer, boolean votingPhase) {
		if (arena == null) {
			return "Votando...";
		}

		Room room = roomManager == null ? null : roomManager.getRoomByArena(arena);

		if (room != null) {
			if (votingPhase && !room.hasVotedMap(viewer)) {
				return "Votando...";
			}

			String selected = room.getSelectedMapName();
			if (selected != null && !selected.trim().isEmpty() && !selected.equalsIgnoreCase("Nenhum")) {
				return selected;
			}
		}

		String template = arena.getTemplateName();
		if (template != null && !template.trim().isEmpty()) {
			return template;
		}

		return arena.getName();
	}

	public void setScoreboard(Player p) {
		if (scoreboards.containsKey(p.getName())) {
			return;
		}
		new BukkitRunnable() {

			@Override
			public void run() {

				if (!p.isOnline()) {
					if (scoreboards.containsKey(p.getName())) {
						scoreboards.remove(p.getName());
					}
					if (scorestate.containsKey(p.getName())) {
						scorestate.remove(p.getName());
					}
					this.cancel();
					return;
				}

				ScoreboardType state = null;

				if (!Arenas.isInArena(p) && getConfig().getBoolean("bungee") && Arenas.getArenas().size() > 1) {
					state = ScoreboardType.VOTING;
				}

				if (!Arenas.isInArena(p) && settings.getConfig().getBoolean("stats-board")
						&& !getConfig().getBoolean("bungee")) {

					if (!settings.getConfig().getBoolean("board-whitelist")) {
						state = ScoreboardType.STATS;
					}

					if (settings.getConfig().getBoolean("board-whitelist")) {
						List<String> v1 = settings.getConfig().getStringList("stats-board-world-whitelist");
						if (v1.contains(p.getWorld().getName())) {
							state = ScoreboardType.STATS;
						}
					}
				}
				if (Arenas.isInArena(p) && Arenas.getArena(p).getState() == GameState.LOBBY) {
					state = ScoreboardType.WAITING;
				}
				if (Arenas.isInArena(p) && Arenas.getArena(p).getState() == GameState.STARTING) {
					state = ScoreboardType.STARTING;
				}
				if (Arenas.isInArena(p) && Arenas.getArena(p).getState() == GameState.INGAME) {
					state = ScoreboardType.INGAME;
				}

				if (state != null) {

					ScoreboardManager wait = null;
					if (!scoreboards.containsKey(p.getName())) {
						wait = new ScoreboardManager(p.getName());
						scoreboards.put(p.getName(), wait);
					}

					if (scoreboards.containsKey(p.getName()) && !scorestate.containsKey(p.getName())) {
						scorestate.put(p.getName(), state);
					}

					if (scoreboards.containsKey(p.getName()) && state != scorestate.get(p.getName())) {
						wait = new ScoreboardManager(p.getName());
						scoreboards.put(p.getName(), wait);
						scorestate.put(p.getName(), state);
					}
					if (wait == null) {
						wait = scoreboards.get(p.getName());
					}
					if (wait != null) {
						if (state == ScoreboardType.VOTING) {
							wait.setTitle(0,
									Utils.FormatText(p, messages.getConfig().getString("vote-scoreboard-title")));
							int size = Math.min(14, Arenas.getArenas().size());
							for (Arena a : Arenas.getArenas()) {

								int votesCount = 0;
								if (point.containsKey(a.getName())) {
									votesCount = point.get(a.getName());
								}
								wait.setLine(0, size,
								        Utils.FormatText(p,
								                messages.getConfig().getString("scoreboard-map")
								                        .replaceAll("%votes%", String.valueOf(votesCount))
								                        .replaceAll("%map%", getScoreboardMapName(a, p, true))));

								size -= 1;
							}
						}

						if (state == ScoreboardType.STATS) {
							if (getPlayerData(p) != null) {
								wait.setTitle(0,
                                        Utils.FormatText(p, (seasonalEventManager != null && seasonalEventManager.isEventActive()) ? seasonalEventManager.getLobbyTitle() : messages.getConfig().getString("stats-scoreboard-title")));
								List<String> h = messages.getConfig().getStringList("stats-scoreboard-lines");
								int size = Math.min(14, messages.getConfig().getStringList("stats-scoreboard-lines").size());
								for (String s : h) {

									wait.setLine(0, size, Utils.FormatText(p, s.replaceAll("%player%", p.getName())
											.replaceAll("%loses%", String.valueOf(getPlayerData(p).getloses()))
											.replaceAll("%wins%", String.valueOf(getPlayerData(p).getwins()))
											.replaceAll("%kills%", String.valueOf(getPlayerData(p).getkill()))
											.replaceAll("%score%", String.valueOf(getPlayerData(p).getscore()))
											.replaceAll("%coins%", String.valueOf(getPlayerData(p).getcoins()))
                                            .replaceAll("%event%", seasonalEventManager != null ? seasonalEventManager.getDisplayName() : "§7-")
                                            .replaceAll("%treasures%", seasonalTreasureManager != null ? seasonalTreasureManager.getDisplay(p) : "§7-")
											.replaceAll("%ranked_rp%", String.valueOf(getRankedRP(p)))
											.replaceAll("%ranked_rank%", getRankedRank(p))
											.replaceAll("%ranked%", getRankedDisplay(p))
											.replaceAll("%zone%", (Arenas.isInArena(p) && Arenas.getArena(p) != null ? Arenas.getArena(p).getRankedZoneDisplay() : "§7-"))
											.replaceAll("%tag%", tagManager != null ? tagManager.getTag(p) : "")
											.replaceAll("%deaths%", String.valueOf(getPlayerData(p).getdeaths()))));
									size -= 1;
								}
							}
						}

						if (state == ScoreboardType.WAITING) {
							Arena a = Arenas.getArena(p);
							wait.setTitle(0, Utils.FormatText(p, getScoreboardTitleByMode(a)));
							List<String> h = messages.getConfig().getStringList("wait-scoreboard-lines");
							int size = Math.min(14, messages.getConfig().getStringList("wait-scoreboard-lines").size());

							for (String s : h) {

								wait.setLine(0, size,
										Utils.FormatText(p,
												s.replaceAll("%max%", String.valueOf(getArenaMaxPlayers(a)))
														.replaceAll("%map%", getScoreboardMapName(a, p, true))
														.replaceAll("%mode%", a.getRoomModeNamePlain())
														.replaceAll("%room%", a.getRoomDisplayName())
														.replaceAll("%timer_status%", a.getCountdownPauseStatus())
														.replaceAll("%leader%", a.getRoomLeaderName())
														.replaceAll("%coins%", String.valueOf(getPlayerData(p).getcoins()))
                                            .replaceAll("%event%", seasonalEventManager != null ? seasonalEventManager.getDisplayName() : "§7-")
                                            .replaceAll("%treasures%", seasonalTreasureManager != null ? seasonalTreasureManager.getDisplay(p) : "§7-")
											.replaceAll("%ranked_rp%", String.valueOf(getRankedRP(p)))
											.replaceAll("%ranked_rank%", getRankedRank(p))
											.replaceAll("%ranked%", getRankedDisplay(p))
											.replaceAll("%zone%", (Arenas.isInArena(p) && Arenas.getArena(p) != null ? Arenas.getArena(p).getRankedZoneDisplay() : "§7-"))
											.replaceAll("%tag%", tagManager != null ? tagManager.getTag(p) : "")

														.replaceAll("%size%", String.valueOf(a.players.size()))));
								size -= 1;
							}

						}

						if (state == ScoreboardType.STARTING) {
							Arena a = Arenas.getArena(p);
							wait.setTitle(0, Utils.FormatText(p, getScoreboardTitleByMode(a)));
							List<String> h = messages.getConfig().getStringList("countdown-scoreboard-lines");
							int size = Math.min(14, messages.getConfig().getStringList("countdown-scoreboard-lines").size());

							for (String s : h) {

								wait.setLine(0, size,
										Utils.FormatText(p, s.replaceAll("%countdown%", String.valueOf(a.countdown))

												.replaceAll("%max%", String.valueOf(getArenaMaxPlayers(a)))
												.replaceAll("%size%", String.valueOf(a.players.size()))
												.replaceAll("%mode%", a.getRoomModeNamePlain())
												.replaceAll("%room%", a.getRoomDisplayName())
												.replaceAll("%timer_status%", a.getCountdownPauseStatus())
												.replaceAll("%leader%", a.getRoomLeaderName())
												.replaceAll("%coins%", String.valueOf(getPlayerData(p).getcoins()))
                                            .replaceAll("%event%", seasonalEventManager != null ? seasonalEventManager.getDisplayName() : "§7-")
                                            .replaceAll("%treasures%", seasonalTreasureManager != null ? seasonalTreasureManager.getDisplay(p) : "§7-")
											.replaceAll("%ranked_rp%", String.valueOf(getRankedRP(p)))
											.replaceAll("%ranked_rank%", getRankedRank(p))
											.replaceAll("%ranked%", getRankedDisplay(p))
											.replaceAll("%zone%", (Arenas.isInArena(p) && Arenas.getArena(p) != null ? Arenas.getArena(p).getRankedZoneDisplay() : "§7-"))
											.replaceAll("%tag%", tagManager != null ? tagManager.getTag(p) : "")
												.replaceAll("%map%", getScoreboardMapName(a, p, true))));

								size -= 1;
							}

						}

						if (state == ScoreboardType.INGAME) {

							Arena a = Arenas.getArena(p);
							wait.addTeam("team", a.getPlayers2());
							Date now = new Date();
							SimpleDateFormat format = new SimpleDateFormat("dd-MM-yy");
							wait.setTitle(0, Utils.FormatText(p, getScoreboardTitleByMode(a)));
							String boardPath = "ingame-scoreboard-lines";
							if (a.getGameMode() == GameModeType.TNT_TAG) {
								boardPath = "tnttag-scoreboard-lines";
							} else if (a.getGameMode() == GameModeType.RANKED) {
								boardPath = "ranked-scoreboard-lines";
							} else if (a.getGameMode() == GameModeType.HIDE_AND_SEEK) {
								boardPath = "hideandseek-scoreboard-lines";
							} else if (a.getGameMode() == GameModeType.SABOTAGE) {
								boardPath = "sabotage-scoreboard-lines";
							}
							List<String> h = messages.getConfig().getStringList(boardPath);
							int size = Math.min(14, h.size());

							for (String s : h) {

								if (a.getGameMode() == GameModeType.SABOTAGE && s != null && s.contains("%sabotage_tasks%")) {
									s = "&eTask: %sabotage_tasks%";
								}

								wait.setLine(0, size,
										Utils.FormatText(p, s.replaceAll("%spectators%", String.valueOf(a.specs.size()))

												.replaceAll("%innocents%", String.valueOf(a.innocents.size()))
												.replaceAll("%kills%", String.valueOf(a.getkill(p)))
												.replaceAll("%score%", String.valueOf(a.getscore(p)))
												.replaceAll("%coins%", String.valueOf(getPlayerData(p).getcoins()))
                                            .replaceAll("%event%", seasonalEventManager != null ? seasonalEventManager.getDisplayName() : "§7-")
                                            .replaceAll("%treasures%", seasonalTreasureManager != null ? seasonalTreasureManager.getDisplay(p) : "§7-")
											.replaceAll("%ranked_rp%", String.valueOf(getRankedRP(p)))
											.replaceAll("%ranked_rank%", getRankedRank(p))
											.replaceAll("%ranked%", getRankedDisplay(p))
											.replaceAll("%zone%", (Arenas.isInArena(p) && Arenas.getArena(p) != null ? Arenas.getArena(p).getRankedZoneDisplay() : "§7-"))
											.replaceAll("%tag%", tagManager != null ? tagManager.getTag(p) : "")
												.replaceAll("%role%", String.valueOf(a.getRole(p)))
												.replaceAll("%mode%", a.getRoomModeNamePlain())
												.replaceAll("%room%", a.getRoomDisplayName())
												.replaceAll("%timer_status%", a.getCountdownPauseStatus())
												.replaceAll("%leader%", a.getRoomLeaderName())
												.replaceAll("%tnt_holder%", a.getTntHolderName())
												.replaceAll("%tnt_time%", String.valueOf(a.getTntRoundTime()))
												.replaceAll("%alive%", String.valueOf(a.getAlivePlayersCount()))
												.replaceAll("%hiders%", String.valueOf(a.getHideSeekHidersCount()))
												.replaceAll("%seekers%", String.valueOf(a.getHideSeekSeekersCount()))
												.replaceAll("%hide_status%", a.getHideSeekStatus())
												.replaceAll("%hide_role%", a.getHideSeekPlayerRole(p))
												.replaceAll("%tasks_percent%", sabotageTaskManager != null ? String.valueOf(sabotageTaskManager.getTaskPercent(a)) : "0")
												.replaceAll("%sabotage_tasks%", sabotageTaskManager != null ? sabotageTaskManager.getPlayerTaskList(p, a) : "§7-")
												.replaceAll("%sabotage_corpses%", sabotageTaskManager != null ? String.valueOf(sabotageTaskManager.getAliveCorpseCount(a)) : "0")
												.replaceAll("%sabotage_meeting_time%", sabotageTaskManager != null ? String.valueOf(sabotageTaskManager.getMeetingTimeLeft(a)) : "0")
												.replaceAll("%date%",
														String.valueOf(format.format(now)).replaceAll("-", "/"))
												.replaceAll("%map%", getScoreboardMapName(a, p, false))
												.replaceAll("%time%", Utils.formattominutes(a.time))));
								size -= 1;
							}

						}

						if (scoreboards.containsKey(p.getName())) {
							ScoreboardManager sb = scoreboards.get(p.getName());

							if (sb.getScoreboard() != p.getScoreboard()) {
								sb.toggleScoreboard();
							}
                            // Reaplica tags/nametag depois que a scoreboard troca.
                            // Isso evita perder tag no TAB/cabeça no lobby de espera e durante a partida.
                            if (tagManager != null) {
                                for (Player online : Bukkit.getOnlinePlayers()) {
                                    tagManager.applyVisuals(online);
                                }
                            }
                            if (amongUsNameTagManager != null) {
                                amongUsNameTagManager.refresh(p);
                            }

						}

					}
				}
			}
		}.runTaskTimer(this, settings.getConfig().getInt("scoreboard-update-interval"),
				settings.getConfig().getInt("scoreboard-update-interval"));

	}


    private ItemStack adminMenuItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }


    public void sendMurderAdminHelp(Player p, String page) {
        if (page == null || page.equalsIgnoreCase("1") || page.equalsIgnoreCase("principal") || page.equalsIgnoreCase("main")) {
            p.sendMessage("§8§m----------------------------------------");
            p.sendMessage("§c§lMURDER ADMIN §7- §fPrincipal §7(§e/m admin 2§7)");
            p.sendMessage("§f/m criar <arena> §7- criar arena");
            p.sendMessage("§f/m apagar <arena> §7- apagar arena");
            p.sendMessage("§f/m setlobby §7- setar lobby principal");
            p.sendMessage("§f/m setwait §7- setar lobby de espera");
            p.sendMessage("§f/m setspawn <arena> <n> §7- setar spawn");
            p.sendMessage("§f/m setspec <arena> §7- setar espectador");
            p.sendMessage("§f/m setgold <arena> <n> §7- setar gold");
            p.sendMessage("§f/m settime <arena> <segundos> §7- tempo da partida");
            p.sendMessage("§f/m setmin <arena> <quantia> §7- minimo de players");
            p.sendMessage("§f/m setmurder <arena> <quantia> §7- murders por partida");
            p.sendMessage("§f/m setdete <arena> <quantia> §7- detectives por partida");
            p.sendMessage("§f/m reload §7- recarregar configs");
            p.sendMessage("§8§m----------------------------------------");
            return;
        }
        if (page.equalsIgnoreCase("2") || page.equalsIgnoreCase("dois") || page.equalsIgnoreCase("salas")) {
            p.sendMessage("§8§m----------------------------------------");
            p.sendMessage("§c§lMURDER ADMIN §7- §fSalas / Modos §7(§e/m admin 3§7)");
            p.sendMessage("§f/m partidas §7- abrir salas");
            p.sendMessage("§f/m criarsala <arena> §7- criar sala");
            p.sendMessage("§f/m publicroom §7- alternar sala publica");
            p.sendMessage("§f/m iniciar §7- iniciar partida/sala");
            p.sendMessage("§f/m parar §7- parar contador");
            p.sendMessage("§f/m tnthit <player> §7- protecao/hit TNTTag");
            p.sendMessage("§f/m semcooldownfaca <player> §7- toggle cooldown faca");
            p.sendMessage("§f/m sabotage task list §7- listar tasks AMONG US");
            p.sendMessage("§f/m sabotage task add <task> §7- adicionar task no mapa");
            p.sendMessage("§f/m sabotage task clear §7- limpar tasks do mapa");
            p.sendMessage("§8§m----------------------------------------");
            return;
        }
        if (page.equalsIgnoreCase("3") || page.equalsIgnoreCase("tres") || page.equalsIgnoreCase("ranked")) {
            p.sendMessage("§8§m----------------------------------------");
            p.sendMessage("§c§lMURDER ADMIN §7- §fRanked / Niveis §7(§e/m admin 4§7)");
            p.sendMessage("§f/m reset <player> §7- resetar dados");
            p.sendMessage("§f/m ver <player> §7- ver dados");
            p.sendMessage("§f/m coins ver <player> §7- ver coins");
            p.sendMessage("§f/m coins add <player> <valor> §7- adicionar coins");
            p.sendMessage("§f/m coins remove <player> <valor> §7- remover coins");
            p.sendMessage("§f/m coins set <player> <valor> §7- setar coins");
            p.sendMessage("§f/m xp <player> §7- ver XP/level");
            p.sendMessage("§f/m addxp <player> <valor> §7- adicionar XP");
            p.sendMessage("§f/m topxp §7- ranking XP");
            p.sendMessage("§f/m topranked §7- ranking ranked");
            p.sendMessage("§f/m setrankedzone <arena> §7- centro zona ranked");
            p.sendMessage("§f/m zone <arena> §7- configurar zona");
            p.sendMessage("§8§m----------------------------------------");
            return;
        }
        if (page.equalsIgnoreCase("4") || page.equalsIgnoreCase("quatro") || page.equalsIgnoreCase("cosmeticos")) {
            p.sendMessage("§8§m----------------------------------------");
            p.sendMessage("§c§lMURDER ADMIN §7- §fCosmeticos / NPCs §7(§e/m admin principal§7)");
            p.sendMessage("§f/m cosmeticos §7- abrir menu cosmeticos");
            p.sendMessage("§f/m titulos §7- abrir titulos/tags");
            p.sendMessage("§f/m setnpc <tipo> §7- setar NPC");
            p.sendMessage("§f/m setmysterybox §7- setar caixa misteriosa");
            p.sendMessage("§f/m setmysteryboxnpc §7- setar NPC da caixa");
            p.sendMessage("§f/m delmysterybox §7- remover caixa/NPC");
            p.sendMessage("§f/m mysterybox §7- abrir sistema de caixas");
            p.sendMessage("§f/m givebox <player> <quantia> §7- dar caixas");
            p.sendMessage("§f/m setbattlepassnpc §7- setar NPC battle pass");
            p.sendMessage("§f/m deletebattlepassnpc §7- remover NPC battle pass");
            p.sendMessage("§f/m battlepass §7- abrir battle pass");
            p.sendMessage("§f/m addpassxp <player> <valor> §7- adicionar XP passe");
            p.sendMessage("§f/m evento §7- menu de eventos sazonais");
            p.sendMessage("§8§m----------------------------------------");
            return;
        }
        p.sendMessage("§cPagina nao encontrada. Use: §f/m admin principal§c, §f/m admin 2§c, §f/m admin 3§c ou §f/m admin 4§c.");
    }

    public void openMurderAdminMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8Murder Admin");
        ItemStack glass = adminMenuItem(Material.STAINED_GLASS_PANE, "§8");
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, glass);

        inv.setItem(4, adminMenuItem(Material.NETHER_STAR, "§c§lMurder Admin",
                "§7Painel rápido de comandos administrativos.",
                "§7Clique nos itens para receber o comando no chat."));

        inv.setItem(10, adminMenuItem(Material.EMERALD_BLOCK, "§aCriar Arena", "§f/murder criar <arena>", "§7Cria uma arena nova."));
        inv.setItem(11, adminMenuItem(Material.REDSTONE_BLOCK, "§cApagar Arena", "§f/murder apagar <arena>", "§7Remove uma arena."));
        inv.setItem(12, adminMenuItem(Material.WATCH, "§eTempo da Partida", "§f/murder settime <arena> <segundos>", "§7Altera tempo normal."));
        inv.setItem(13, adminMenuItem(Material.PAPER, "§eMínimo de Jogadores", "§f/murder setmin <arena> <quantidade>", "§7Altera mínimo para iniciar."));

        inv.setItem(19, adminMenuItem(Material.BED, "§bSetar Lobby Principal", "§f/murder setlobby", "§7Seta onde você está."));
        inv.setItem(20, adminMenuItem(Material.ENDER_PEARL, "§bSetar Lobby de Espera", "§f/murder setwait", "§7Seta o waiting lobby global."));
        inv.setItem(21, adminMenuItem(Material.SKULL_ITEM, "§bSetar Espectador", "§f/murder setspec <arena>", "§7Seta spawn de espectador."));
        inv.setItem(22, adminMenuItem(Material.STAINED_CLAY, "§bSetar Spawn", "§f/murder setspawn <arena> <número>", "§7Seta spawn de player."));
        inv.setItem(23, adminMenuItem(Material.GOLD_INGOT, "§6Setar Gold", "§f/murder setgold <arena> <número>", "§7Seta spawn de fragmento/gold."));
        inv.setItem(24, adminMenuItem(Material.BEACON, "§dCentro da Zona Ranked", "§f/murder setzonecenter <arena>", "§7Seta o centro da zona."));

        inv.setItem(30, adminMenuItem(Material.REDSTONE_COMPARATOR, "§cÁrea Staff das Salas", "§7Abra pelo Gerenciar Sala.", "§7Controla modos, modificadores, kits e mapas."));
        inv.setItem(31, adminMenuItem(Material.PUMPKIN, "§dEventos Sazonais", "§f/murder evento", "§7Ativa eventos por GUI/YML.", "§7Use §f/m evento reload §7após editar arquivos."));
        inv.setItem(40, adminMenuItem(Material.ANVIL, "§aReload", "§f/murder reload", "§7Recarrega config/data/settings."));
        inv.setItem(49, adminMenuItem(Material.BOOK, "§eAjuda Completa", "§7Os comandos aparecem no lore.", "§eClique para enviar ajuda no chat."));
        p.openInventory(inv);
    }

    @EventHandler
    public void onMurderAdminMenuClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (!e.getView().getTitle().equals("§8Murder Admin")) return;
        e.setCancelled(true);
        Player p = (Player) e.getWhoClicked();
        if (!p.isOp() && !p.hasPermission("murder.admin")) return;
        ItemStack item = e.getCurrentItem();
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return;
        if (item.getItemMeta().hasDisplayName() && ChatColor.stripColor(item.getItemMeta().getDisplayName()).equalsIgnoreCase("Eventos Sazonais")) {
            if (seasonalEventManager != null) seasonalEventManager.openMenu(p);
            return;
        }
        if (!item.getItemMeta().hasLore()) return;
        for (String line : item.getItemMeta().getLore()) {
            String clean = ChatColor.stripColor(line);
            if (clean.startsWith("/murder ")) {
                p.closeInventory();
                p.sendMessage("§eComando: §f" + clean);
                p.sendMessage("§7Copie e complete o comando no chat.");
                return;
            }
        }
        p.closeInventory();
        p.sendMessage("§8§m------------------------------");
        p.sendMessage("§c§lMurder Admin §7- comandos principais");
        p.sendMessage("§f/murder criar <arena> §7- criar arena");
        p.sendMessage("§f/murder setspawn <arena> <n> §7- setar spawn");
        p.sendMessage("§f/murder setgold <arena> <n> §7- setar gold");
        p.sendMessage("§f/murder setwait §7- waiting lobby global");
        p.sendMessage("§f/murder setspec <arena> §7- espectador");
        p.sendMessage("§f/murder reload §7- recarregar configs");
        p.sendMessage("§8§m------------------------------");
    }

	public void setSpec(Player p, Arena a) {
		arenas.getConfig().set("Spectator." + a.getTemplateName() + ".main.lobby.world", p.getLocation().getWorld().getName());
		arenas.getConfig().set("Spectator." + a.getTemplateName() + ".main.lobby.x", Double.valueOf(p.getLocation().getX()));
		arenas.getConfig().set("Spectator." + a.getTemplateName() + ".main.lobby.y", Double.valueOf(p.getLocation().getY()));
		arenas.getConfig().set("Spectator." + a.getTemplateName() + ".main.lobby.z", Double.valueOf(p.getLocation().getZ()));
		arenas.getConfig().set("Spectator." + a.getTemplateName() + ".main.lobby.yaw",
				Double.valueOf(p.getLocation().getYaw()));
		arenas.getConfig().set("Spectator." + a.getTemplateName() + ".main.lobby.pitch",
				Double.valueOf(p.getLocation().getPitch()));
		arenas.save();
	}

	//VOLTAR AQUI
	
	public void setup(Player p) {
		p.getInventory().setHeldItemSlot(0);
		p.getInventory().clear();

		p.updateInventory();
		p.setHealth(p.getMaxHealth());
		p.setFoodLevel(20);
		p.setGameMode(GameMode.ADVENTURE);
		p.setExp(0);
		p.setLevel(0);
		p.setAllowFlight(false);
		p.setFlying(false);
		p.setCanPickupItems(true);
		p.setFireTicks(0);
		for (PotionEffect e : p.getActivePotionEffects()) {
			p.removePotionEffect(e.getType());
		}
		p.setFallDistance(0);

	}

	public void setUpForMultiMaps(Player p) {
		if (Arenas.isInArena(p)) {
			Arena a = Arenas.getArena(p);
			a.removePlayer(p, "leave");

		}
		if (Arenas.getArenas().size() > 1) {
			setup(p);

			setMap(p);

			p.teleport(getLobby());

			ItemStack bed = new ItemStack(Material.getMaterial(settings.getConfig().getInt("quit3.item-id")), 1,
					(short) settings.getConfig().getInt("quit3.item-subid"));
			ItemMeta sm = bed.getItemMeta();
			sm.setDisplayName(Utils.FormatText(p, settings.getConfig().getString("quit3.item-name")));
			sm.setLore(Arrays.asList(Utils.FormatText(p, settings.getConfig().getString("quit3.item-lore"))));
			bed.setItemMeta(sm);
			p.getInventory().setItem(8, bed);
		}
	}

	public void setWait(Player p) {
		Location loc = p.getLocation();
		String path = "GlobalWait.main.lobby.";
		arenas.getConfig().set(path + "world", loc.getWorld().getName());
		arenas.getConfig().set(path + "x", Double.valueOf(loc.getX()));
		arenas.getConfig().set(path + "y", Double.valueOf(loc.getY()));
		arenas.getConfig().set(path + "z", Double.valueOf(loc.getZ()));
		arenas.getConfig().set(path + "yaw", Double.valueOf(loc.getYaw()));
		arenas.getConfig().set(path + "pitch", Double.valueOf(loc.getPitch()));
		arenas.save();
	}

	@Deprecated
	public void setWait(Player p, Arena a) {
		setWait(p);
	}

	public void setRankedZoneCenter(Player p, Arena a) {
		if (p == null || a == null) return;
		Location loc = p.getLocation();
		String path = "RankedZoneCenter." + a.getTemplateName() + ".main.lobby.";
		arenas.getConfig().set(path + "world", loc.getWorld().getName());
		arenas.getConfig().set(path + "x", Double.valueOf(loc.getX()));
		arenas.getConfig().set(path + "y", Double.valueOf(loc.getY()));
		arenas.getConfig().set(path + "z", Double.valueOf(loc.getZ()));
		arenas.getConfig().set(path + "yaw", Double.valueOf(loc.getYaw()));
		arenas.getConfig().set(path + "pitch", Double.valueOf(loc.getPitch()));
		arenas.save();
	}

	public Location getRankedZoneCenter(Arena a) {
		if (a == null || a.getTemplateName() == null) return null;
		String path = "RankedZoneCenter." + a.getTemplateName() + ".main.lobby.";
		String worldName = arenas.getConfig().getString(path + "world");
		if (worldName == null || worldName.trim().isEmpty()) return null;

		World w = Bukkit.getWorld(worldName);
		if (w == null) {
			Bukkit.getConsoleSender().sendMessage("§c[sMurder Ranked] Mundo do centro da zona nao carregado: " + worldName + " | arena: " + a.getTemplateName());
			return null;
		}

		double x = arenas.getConfig().getDouble(path + "x");
		double y = arenas.getConfig().getDouble(path + "y");
		double z = arenas.getConfig().getDouble(path + "z");
		float yaw = (float) arenas.getConfig().getDouble(path + "yaw");
		float pitch = (float) arenas.getConfig().getDouble(path + "pitch");
		return new Location(w, x, y, z, yaw, pitch);
	}

	public void spawnarmorstand(Arena a, Location loc) {
		if (a.armor.size() > 0) {
			return;
		}
		ArmorStand armor = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
		armor.setVisible(false);
		armor.setBasePlate(false);
		armor.setGravity(true);
		armor.setSmall(true);
		armor.setArms(true);
		armor.setItemInHand(new ItemStack(Material.DIAMOND_HOE));

		a.bowloc = loc;
		if (!a.armor.contains(armor)) {
			a.armor.add(armor);
		}
		new BukkitRunnable() {

			@Override
			public void run() {
				if (armor.isDead() || armor == null) {

					if (a.armor.contains(armor)) {
						a.armor.remove(armor);
					}
					this.cancel();
					return;
				}
				Location loc2 = loc;
				loc2.setYaw(loc2.getYaw() + 3);
				loc2.setPitch(loc2.getPitch() + 3);
				armor.teleport(loc2);
			}
		}.runTaskTimer(this, 2, 2);
	}

	public int SpawnSize(Arena a) {
		if (a == null) return 0;
		if (a.maxPlayers > 0) return a.maxPlayers;
		if (arenas.getConfig().contains("Spawns." + a.getTemplateName())) {
			return arenas.getConfig().getConfigurationSection("Spawns." + a.getTemplateName()).getKeys(false).size();
		}
		return SpawnSizeByName(a.getTemplateName());
	}
	
	public int SpawnSizeByName(String arenaName) {

	    if (arenas.getConfig().contains("SpawnSize." + arenaName)) {
	        return arenas.getConfig().getInt("SpawnSize." + arenaName);
	    }

	    return 10;
	}

	public int SpawnSize2(Arena a) {

		return arenas.getConfig().getConfigurationSection("Spawns." + a.getTemplateName()).getKeys(false).size();

	}

	private boolean shouldUseDefaultVictoryFireworks(Player player) {
		if (player == null) return false;
		String selected = me.spwtyz.murder.cosmetics.CosmeticEffectManager.getSelected(this, player, "victory");
		return selected == null || selected.trim().isEmpty() || selected.equalsIgnoreCase("nenhum");
	}

	public void StartFireworksMurder(Arena a) {

		new BukkitRunnable() {
			int time = settings.getConfig().getInt("fireworks-time-in-ticks");

			@Override
			public void run() {

				time -= 1;
				if (time <= 0) {
					this.cancel();
					return;
				}
				if (!settings.getConfig().getBoolean("start-fireworks-on-players-location")) {
					boolean useDefault = false;
					for (Player p : a.murder) {
						if (shouldUseDefaultVictoryFireworks(p)) { useDefault = true; break; }
					}
					if (useDefault) {
						Location loc = getSpawn(a, getRandom(0, SpawnSize2(a)));
						LaunchFirework(loc);
					}
				}
				if (settings.getConfig().getBoolean("start-fireworks-on-players-location")) {

					if (a.murder.size() > 0) {
						for (Player p : a.murder) {
							if (p != null && shouldUseDefaultVictoryFireworks(p)) {
								LaunchFirework(p.getLocation());
							}
						}
					}
				}

			}
		}.runTaskTimer(this, settings.getConfig().getInt("fireworks-ticks"),
				settings.getConfig().getInt("fireworks-ticks"));
	}

	public void StartFireworksPlayers(Arena a) {

		new BukkitRunnable() {
			int time = settings.getConfig().getInt("fireworks-time-in-ticks");

			@Override
			public void run() {

				time -= 1;
				if (time <= 0) {
					this.cancel();
					return;
				}
				if (!settings.getConfig().getBoolean("start-fireworks-on-players-location")) {
					boolean useDefault = false;
					for (Player p : a.getPlayers()) {
						if (a.getType(p) != PlayerType.Murderer && shouldUseDefaultVictoryFireworks(p)) { useDefault = true; break; }
					}
					if (useDefault) {
						Location loc = getSpawn(a, getRandom(0, SpawnSize2(a)));
						LaunchFirework(loc);
					}
				}
				if (settings.getConfig().getBoolean("start-fireworks-on-players-location")) {

					for (Player p : a.getPlayers()) {
						if (a.getType(p) != PlayerType.Murderer && shouldUseDefaultVictoryFireworks(p)) {
							LaunchFirework(p.getLocation());
						}
					}
				}

			}
		}.runTaskTimer(this, settings.getConfig().getInt("fireworks-ticks"),
				settings.getConfig().getInt("fireworks-ticks"));
	}

	public void StartMap() {

		if (startmap) {
			return;
		}
		startmap = true;
		if (Arenas.getArenas().size() > 1) {
			for (Player p : Bukkit.getOnlinePlayers()) {
				p.sendMessage(Utils.FormatText(p, messages.getConfig().getString("voting-time-started")));
			}
			new BukkitRunnable() {

				@Override
				public void run() {

					if (getHighestVote() != null) {

						bungee = Arenas.getArena(getHighestVote());
						for (Player p : Bukkit.getOnlinePlayers()) {
							p.sendMessage(Utils.FormatText(p,
									messages.getConfig().getString("vote-win").replaceAll("%map%", getHighestVote())));
						}
					}
					if (getHighestVote() == null) {

						bungee = Arenas.getArenas().get(0);

						for (Player p : Bukkit.getOnlinePlayers()) {
							p.sendMessage(Utils.FormatText(p,
									messages.getConfig().getString("vote-win").replaceAll("%map%", bungee.getName())));
						}

					}

					sendPlayers();

				}
			}.runTaskLater(this, 20 * settings.getConfig().getInt("vote-time"));

		}
	}

	public static Plugin p() {
		
		return instance;
	}

    @EventHandler
    public void onMysteryBattlePassClick(org.bukkit.event.inventory.InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (e.getInventory() == null || e.getView() == null) return;

        String title = e.getView().getTitle();
        Player p = (Player) e.getWhoClicked();

        if (title.equals("§8Top Ranked")) {
            e.setCancelled(true);
            return;
        }

        if (title.equals("§8Caixa Misteriosa") || title.equals("§8✦ Caixa Misteriosa ✦") || title.equals("§8✦ Mystery Box") || title.equals("§8Abrindo Caixa...") || title.equals("§8✦ Abrindo...") || title.equals("§8✦ Abrindo Caixa ✦") || title.equals("§8✦ Mystery Vault ✦")) {
            e.setCancelled(true);
            if (mysteryBoxManager != null) {
                mysteryBoxManager.handleClick(p, e.getCurrentItem());
            }
            return;
        }

        if (title.equals("§8Passe de Batalha") || title.equals("§8✦ Passe de Batalha ✦") || title.equals("§8✦ Battle Pass")) {
            e.setCancelled(true);
            if (battlePassManager != null) {
                battlePassManager.handleClick(p, e.getCurrentItem());
            }
        }
    }

    public void openRankedTopMenu(Player p) {
        if (p == null) return;
        if (rankedManager == null) {
            p.sendMessage("§cSistema Ranked não carregou.");
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, "§8Top Ranked");
        List<me.spwtyz.murder.ranked.RankedManager.RankedEntry> top = rankedManager.getTop(45);

        ItemStack info = new ItemStack(Material.NETHER_STAR);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName("§6§lTOP RANKED GLOBAL");
        infoMeta.setLore(Arrays.asList(
                "§7Ranking ordenado por RP.",
                "§7Desempate: vitórias e kills.",
                "",
                "§fSeu RP: §b" + rankedManager.getRP(p),
                "§fSua patente: " + rankedManager.getRankDisplay(p),
                "§fSua posição: " + (rankedManager.getPosition(p) > 0 ? "§e#" + rankedManager.getPosition(p) : "§7Sem posição")
        ));
        info.setItemMeta(infoMeta);
        inv.setItem(4, info);

        if (top.isEmpty()) {
            ItemStack empty = new ItemStack(Material.BARRIER);
            ItemMeta meta = empty.getItemMeta();
            meta.setDisplayName("§cSem dados ranked");
            meta.setLore(Arrays.asList("§7Jogue partidas Ranked para", "§7aparecer no Top Ranked."));
            empty.setItemMeta(meta);
            inv.setItem(22, empty);
            p.openInventory(inv);
            return;
        }

        int slot = 9;
        int pos = 1;
        for (me.spwtyz.murder.ranked.RankedManager.RankedEntry entry : top) {
            if (slot >= inv.getSize()) break;

            ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            ItemMeta meta = skull.getItemMeta();
            meta.setDisplayName("§e#" + pos + " §f" + entry.getName());
            meta.setLore(Arrays.asList(
                    "§fRP: §b" + entry.getRp(),
                    "§fPatente: " + rankedManager.getRankColor(entry.getRp()) + entry.getRank(),
                    "§fVitórias: §a" + entry.getWins(),
                    "§fDerrotas: §c" + entry.getLosses(),
                    "§fKills: §c" + entry.getKills(),
                    "§fPartidas: §e" + entry.getGamesPlayed()
            ));
            if (meta instanceof SkullMeta) {
                try {
                    ((SkullMeta) meta).setOwner(entry.getName());
                } catch (Exception ignored) {
                }
            }
            skull.setItemMeta(meta);
            inv.setItem(slot, skull);

            slot++;
            pos++;
        }

        p.openInventory(inv);
        rankedManager.sendTop(p, 10);
    }





    public String getLevelDisplay(Player p) {
        if (levelManager == null || p == null) return "0";
        try {
            return String.valueOf(levelManager.getLevel(p));
        } catch (Exception e) {
            return "0";
        }
    }

    
public String getXPDisplay(Player p) {
    if (levelManager == null || p == null) return "0/100";

    try {
        int xpAtual = levelManager.getXP(p);
        int nivel = levelManager.getLevel(p);
        int xpProximo = 100 + (nivel * 40);

        return xpAtual + "/" + xpProximo;
    } catch (Exception e) {
        return "0/100";
    }
}




    public int getCoins(org.bukkit.entity.Player p) {
        if (p == null) return 0;
        if (getPlayerData(p) != null) {
            return getPlayerData(p).getcoins();
        }
        return 0;
    }



    public int getRankedRP(org.bukkit.entity.Player p) {
        if (rankedManager == null || p == null) return 1000;
        return rankedManager.getRP(p);
    }

    public String getRankedRank(org.bukkit.entity.Player p) {
        if (rankedManager == null || p == null) return "Bronze";
        return rankedManager.getRankName(p);
    }

    public String getRankedDisplay(org.bukkit.entity.Player p) {
        if (rankedManager == null || p == null) return "§7Bronze §7(1000 RP)";
        return rankedManager.getRankDisplay(p);
    }

}