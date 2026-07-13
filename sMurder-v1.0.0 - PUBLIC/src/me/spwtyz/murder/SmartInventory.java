package me.spwtyz.murder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.lang.reflect.Field;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import me.spwtyz.murder.rooms.Room;

public class SmartInventory {

	Player p;

	HashMap<Inventory, Inventory> hash = new HashMap<>();

	HashMap<Inventory, Inventory> hash2 = new HashMap<>();
	Inventory main = null;

	boolean op = false;

	public SmartInventory(Player p) {
		this.p = p;
		Update();
	}

	@SuppressWarnings("deprecation")
	public void CallOnEmpty(ItemStack s, Inventory i) {

		if (this.hash.containsKey(i)) {

			for (ItemStack c : hash.get(i).getContents()) {
				if (c != null) {
					if (ChatColor.stripColor(c.getItemMeta().getDisplayName())
							.equalsIgnoreCase(ChatColor.stripColor(s.getItemMeta().getDisplayName()))) {
						hash.get(i).remove(c);
						if (!isInventoryFull(this.hash.get(i))) {

							this.hash.get(i).setItem(this.hash.get(i).firstEmpty(), s);

						} else {
							CallOnEmpty(s, this.hash.get(i));
						}
						return;
					}
				}
			}

			if (!isInventoryFull(this.hash.get(i))) {

				this.hash.get(i).setItem(this.hash.get(i).firstEmpty(), s);

			} else {
				CallOnEmpty(s, this.hash.get(i));
			}

		}

		if (!hash.containsKey(i)) {

			Inventory inv = Bukkit.createInventory(null, 54,
					Utils.FormatText2(Main.getInstance().settings.getConfig().getString("arenas-inventory-title")));

			clearinv(inv);
			hash.put(i, inv);

			hash2.put(inv, i);

			ItemStack next = new ItemStack(
					Material.getMaterial(Main.getInstance().settings.getConfig().getInt("GUI.next-page-item-id")), 1,
					(short) Main.getInstance().settings.getConfig().getInt("GUI.next-page-item-durability"));
			ItemMeta sm = next.getItemMeta();
			sm.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS,
					ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_UNBREAKABLE);
			sm.setDisplayName(Utils.FormatText2(Main.getInstance().settings.getConfig().getString("GUI.next-page")));
			next.setItemMeta(sm);

			i.setItem(26, next);
			i.setItem(35, next);

			ItemStack previous = new ItemStack(
					Material.getMaterial(Main.getInstance().settings.getConfig().getInt("GUI.previous-page-item-id")),
					1, (short) Main.getInstance().settings.getConfig().getInt("GUI.previous-page-item-durability"));

			ItemMeta sm1 = previous.getItemMeta();
			sm1.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS,
					ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_UNBREAKABLE);
			sm1.setDisplayName(
					Utils.FormatText2(Main.getInstance().settings.getConfig().getString("GUI.previous-page")));
			previous.setItemMeta(sm1);

			inv.setItem(18, previous);
			inv.setItem(27, previous);

		}

	}

	public boolean checkifenough() {

		int all = Arenas.getArenas().size();
		int x = hash.size() * 28;

		if (x >= all) {
			return true;
		}
		return false;

	}

	public void clearinv(Inventory inv) {
		for (int i = 10; i < 43; i++) {
			if (i != 26 && i != 35 && i != 18 && i != 27) {

				inv.setItem(i, new ItemStack(Material.AIR));

			}
		}

		LoadFrame(inv);
	}

	public boolean isInventoryFull(Inventory p) {
		return p.firstEmpty() == -1;
	}

	private ItemStack createRoomItem() {
		// Head verde de + para o botão de criar sala no menu /m partidas.
		// Texture: Green Plus (custom head).
		String texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2U3ZDhjMjQyZDJlNGY4MDI4ZjkzMGJlNzZmMzUwMTRiMjFiNTI1NTIwOGIxYzA0MTgxYjI1NzQxMzFiNzVhIn19fQ==";

		ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta meta = (SkullMeta) item.getItemMeta();

		meta.setDisplayName(ChatColor.GREEN + "Criar Sala");
		List<String> lore = new ArrayList<>();
		lore.add(ChatColor.GRAY + "Clique para criar sua própria sala.");
		lore.add(ChatColor.YELLOW + "Depois você poderá gerenciar ela.");
		meta.setLore(lore);

		try {
			GameProfile profile = new GameProfile(UUID.randomUUID(), null);
			profile.getProperties().put("textures", new Property("textures", texture));
			Field profileField = meta.getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			profileField.set(meta, profile);
		} catch (Exception ex) {
			// Fallback: se o servidor bloquear texture por reflexão, ainda mostra uma head comum.
			meta.setOwner("MHF_Question");
		}

		item.setItemMeta(meta);
		return item;
	}


	private static final String PUBLIC_ROOM_HEAD_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzg5ZmUxZmY0OTE1MzQyZDNiYzgzNmU0NmM3ZjYyNDRjNjFjMjk0NzI0NmNiMzA1ZDU1NDU2NTU0NTM3MTkyNiJ9fX0=";
	private static final String PRIVATE_ROOM_HEAD_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDZmZGUxZmNlNDI2ZjA3YjU4ZGIzMjhlNmE2NzVkMjZjMTUyYjNkZDYwNThlZDNhOGU4NWIwMzRkOTk4ZTcwYyJ9fX0=";

	private boolean shouldShowInPartidas(Arena arena) {
		if (arena == null || Main.getInstance().roomManager == null) return false;
		Room room = Main.getInstance().roomManager.getRoomByArena(arena);
		if (room == null) return false;
		return room.isFixedPublicRoom() || !room.isMainRoom();
	}

	private ItemStack applyRoomHead(ItemStack original, Room room) {
		if (room == null) return original;
		// Salas públicas fixas e salas privadas nunca usam o item da config do SmartInventory.
		// Isso evita ficar preso no stained glass pane verde configurado nos menus antigos.
		if (room.isMainRoom() && !room.isFixedPublicRoom()) return original;

		String texture = room.isFixedPublicRoom() ? PUBLIC_ROOM_HEAD_TEXTURE : PRIVATE_ROOM_HEAD_TEXTURE;
		ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta meta = (SkullMeta) item.getItemMeta();

		if (original != null && original.hasItemMeta()) {
			ItemMeta old = original.getItemMeta();
			if (old.hasDisplayName()) meta.setDisplayName(old.getDisplayName());
			if (old.hasLore()) meta.setLore(old.getLore());
		}
		if (!meta.hasDisplayName()) {
			String name = room.isFixedPublicRoom() && Main.getInstance().roomManager != null
					? Main.getInstance().roomManager.getPublicRoomDisplayName(room)
					: room.getDisplayName();
			meta.setDisplayName((room.isFixedPublicRoom() ? ChatColor.GREEN : ChatColor.LIGHT_PURPLE) + name);
		}

		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS,
				ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_UNBREAKABLE);

		try {
			GameProfile profile = new GameProfile(UUID.randomUUID(), null);
			profile.getProperties().put("textures", new Property("textures", texture));
			Field profileField = meta.getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			profileField.set(meta, profile);
		} catch (Exception ex) {
			meta.setOwner(room.isFixedPublicRoom() ? "MHF_Globe" : "MHF_Chest");
		}

		item.setItemMeta(meta);
		return item;
	}

	private String getRoomLeaderName(Room room) {
		if (room == null || room.isMainRoom() || room.getOwner() == null) {
			return "Nenhum";
		}

		return room.getOwner().getName();
	}

	private String getRoomModeName(Room room) {
		GameModeType mode = room == null ? GameModeType.NORMAL : room.getGameMode();

		if (mode == GameModeType.ALL_MURDER) {
			return "Todos Assassinos";
		}
		if (mode == GameModeType.TNT_TAG) {
			return "TntTag";
		}
		if (mode == GameModeType.RANKED) {
			return "Ranked";
		}

		return "Normal";
	}

	private void addRoomInfoLore(List<String> lore, Room room) {
		String leader = getRoomLeaderName(room);
		String mode = getRoomModeName(room);

		boolean hasLeader = false;
		boolean hasMode = false;
		int insertAt = lore.size();

		for (int i = 0; i < lore.size(); i++) {
			String line = lore.get(i);
			String clean = ChatColor.stripColor(line).toLowerCase();

			if (clean.contains("clique")) {
				insertAt = i;
			}

			if (clean.contains("lider") || clean.contains("líder")) {
				hasLeader = true;
			}
			if (clean.contains("modo")) {
				hasMode = true;
			}
		}

		List<String> add = new ArrayList<String>();
		if (!hasMode) {
			add.add(ChatColor.LIGHT_PURPLE + "Modo: " + ChatColor.WHITE + mode);
		}
		if (!hasLeader && room != null && !room.isMainRoom()) {
			add.add(ChatColor.AQUA + "Líder: " + ChatColor.WHITE + leader);
		}

		if (!add.isEmpty()) {
			lore.addAll(insertAt, add);
		}
	}

	public void LoadFrame(Inventory inv) {
		@SuppressWarnings("deprecation")
		ItemStack s = new ItemStack(
				Material.getMaterial(Main.getInstance().settings.getConfig().getInt("frame-item-id")), 1,
				(short) Main.getInstance().settings.getConfig().getInt("frame-item-data"));
		ItemMeta sm = s.getItemMeta();
		sm.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS,
				ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_UNBREAKABLE);
		sm.setDisplayName(ChatColor.BLACK + "");
		s.setItemMeta(sm);
		inv.setItem(0, s);
		inv.setItem(1, s);
		inv.setItem(2, s);
		inv.setItem(3, s);
		inv.setItem(4, s);
		inv.setItem(5, s);
		inv.setItem(6, s);
		inv.setItem(7, s);
		inv.setItem(8, s);
		inv.setItem(9, s);
		if (inv.getItem(18) == null || inv.getItem(18).getType() == Material.AIR) {
			inv.setItem(18, s);
		}
		if (inv.getItem(27) == null || inv.getItem(27).getType() == Material.AIR) {
			inv.setItem(27, s);
		}
		inv.setItem(36, s);
		inv.setItem(45, s);
		inv.setItem(46, s);
		inv.setItem(47, s);
		inv.setItem(48, s);
		inv.setItem(49, s);
		inv.setItem(50, s);
		inv.setItem(51, s);
		inv.setItem(52, s);
		inv.setItem(53, s);
		inv.setItem(17, s);

		if (inv.getItem(26) == null || inv.getItem(26).getType() == Material.AIR) {
			inv.setItem(26, s);
		}
		if (inv.getItem(35) == null || inv.getItem(35).getType() == Material.AIR) {
			inv.setItem(35, s);
		}

		inv.setItem(44, s);

	}

	@SuppressWarnings("deprecation")
	public void openInventory() {

		if (Main.getInstance().getConfig().getBoolean("bungee")) {
			return;
		}
		if (Arenas.isInArena(p)) {
			return;
		}

		if (Arenas.getArenas() == null || Arenas.getArenas().size() == 0) {
			p.sendMessage(Utils.FormatText(p, Main.getInstance().messages.getConfig().getString("no-arenas")));
			return;
		}

		if (checkifenough()) {
			hash.clear();
			hash2.clear();

		}
		if (main != null) {

			clearinv(main);
			main.setItem(53, createRoomItem());
			for (Arena a : Arenas.getArenas()) {
				if (!shouldShowInPartidas(a)) {
					continue;
				}
				ItemStack s = null;
				if (a.getState() == GameState.LOBBY) {

					s = new ItemStack(
							Material.getMaterial(
									Main.getInstance().settings.getConfig().getInt("arenas-lobby-state-item-id")),
							1, (short) Main.getInstance().settings.getConfig().getInt("arenas-lobby-state-item-data"));

					ItemMeta sm = s.getItemMeta();
					sm.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS,
							ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_UNBREAKABLE);

					sm.setDisplayName(Utils.FormatText(p, Main.getInstance().settings.getConfig()
							.getString("arenas-lobby-state-item-name").replaceAll("%arena%", a.getRoomDisplayName())));

					List<String> lore = Main.getInstance().settings.getConfig()
							.getStringList("arenas-lobby-state-item-lore");
					List<String> coloredLore = new ArrayList<>();

					String state = a.getState().toString().toLowerCase();
					Room room = Main.getInstance().roomManager == null ? null : Main.getInstance().roomManager.getRoomByArena(a);
					String leader = getRoomLeaderName(room);
					String mode = getRoomModeName(room);

					for (String s1 : lore) {
						String line = s1.replaceAll("%state%", Utils.capitalizeFirstLetter(state))
								.replaceAll("%map%", Utils.capitalizeFirstLetter(a.getTemplateName()))
								.replaceAll("%room%", a.getRoomDisplayName())
								.replaceAll("%max%", String.valueOf(Main.getInstance().SpawnSize(a)))
								.replaceAll("%players%", String.valueOf(a.players.size()))
								.replaceAll("%leader%", leader)
								.replaceAll("%lider%", leader)
								.replaceAll("%mode%", mode)
								.replaceAll("%modo%", mode)
								.replaceAll("%room%", a.getRoomDisplayName());

						coloredLore.add(Utils.FormatText(p, line));

					}

					addRoomInfoLore(coloredLore, room);
					sm.setLore(coloredLore);
					s.setItemMeta(sm);
					s = applyRoomHead(s, room);

				}

				if (a.getState() == GameState.STARTING) {

					s = new ItemStack(
							Material.getMaterial(
									Main.getInstance().settings.getConfig().getInt("arenas-starting-state-item-id")),
							1,
							(short) Main.getInstance().settings.getConfig().getInt("arenas-starting-state-item-data"));

					ItemMeta sm = s.getItemMeta();
					sm.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS,
							ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_UNBREAKABLE);

					sm.setDisplayName(Utils.FormatText(p, Main.getInstance().settings.getConfig()
							.getString("arenas-starting-state-item-name").replaceAll("%arena%", a.getRoomDisplayName())));

					List<String> lore = Main.getInstance().settings.getConfig()
							.getStringList("arenas-starting-state-item-lore");
					List<String> coloredLore = new ArrayList<>();

					String state = a.getState().toString().toLowerCase();
					Room room = Main.getInstance().roomManager == null ? null : Main.getInstance().roomManager.getRoomByArena(a);
					String leader = getRoomLeaderName(room);
					String mode = getRoomModeName(room);

					for (String s1 : lore) {
						String line = s1.replaceAll("%state%", Utils.capitalizeFirstLetter(state))
								.replaceAll("%map%", Utils.capitalizeFirstLetter(a.getTemplateName()))
								.replaceAll("%room%", a.getRoomDisplayName())
								.replaceAll("%max%", String.valueOf(Main.getInstance().SpawnSize(a)))
								.replaceAll("%players%", String.valueOf(a.players.size()))
								.replaceAll("%leader%", leader)
								.replaceAll("%lider%", leader)
								.replaceAll("%mode%", mode)
								.replaceAll("%modo%", mode)
								.replaceAll("%room%", a.getRoomDisplayName());

						coloredLore.add(Utils.FormatText(p, line));

					}

					addRoomInfoLore(coloredLore, room);
					sm.setLore(coloredLore);
					s.setItemMeta(sm);
					s = applyRoomHead(s, room);

				}

				if (a.getState() == GameState.INGAME) {

					s = new ItemStack(
							Material.getMaterial(
									Main.getInstance().settings.getConfig().getInt("arenas-ingame-state-item-id")),
							1, (short) Main.getInstance().settings.getConfig().getInt("arenas-ingame-state-item-data"));

					ItemMeta sm = s.getItemMeta();
					sm.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS,
							ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_UNBREAKABLE);

					sm.setDisplayName(Utils.FormatText(p, Main.getInstance().settings.getConfig()
							.getString("arenas-ingame-state-item-name").replaceAll("%arena%", a.getRoomDisplayName())));

					List<String> lore = Main.getInstance().settings.getConfig()
							.getStringList("arenas-ingame-state-item-lore");
					List<String> coloredLore = new ArrayList<>();

					String state = a.getState().toString().toLowerCase();
					Room room = Main.getInstance().roomManager == null ? null : Main.getInstance().roomManager.getRoomByArena(a);
					String leader = getRoomLeaderName(room);
					String mode = getRoomModeName(room);

					for (String s1 : lore) {
						String line = s1.replaceAll("%state%", Utils.capitalizeFirstLetter(state))
								.replaceAll("%map%", Utils.capitalizeFirstLetter(a.getTemplateName()))
								.replaceAll("%room%", a.getRoomDisplayName())
								.replaceAll("%max%", String.valueOf(Main.getInstance().SpawnSize(a)))
								.replaceAll("%players%", String.valueOf(a.players.size()))
								.replaceAll("%leader%", leader)
								.replaceAll("%lider%", leader)
								.replaceAll("%mode%", mode)
								.replaceAll("%modo%", mode)
								.replaceAll("%room%", a.getRoomDisplayName());

						coloredLore.add(Utils.FormatText(p, line));

					}

					addRoomInfoLore(coloredLore, room);
					sm.setLore(coloredLore);
					s.setItemMeta(sm);
					s = applyRoomHead(s, room);

				}
				if (s == null) {
					continue;
				}
				if (!isInventoryFull(main)) {

					main.setItem(main.firstEmpty(), s);

				} else {
					CallOnEmpty(s, main);
				}

			}

			return;
		}
		main = Bukkit.createInventory(null, 54,
				Utils.FormatText2(Main.getInstance().settings.getConfig().getString("arenas-inventory-title")));

		clearinv(main);
		main.setItem(53, createRoomItem());
		for (Arena a : Arenas.getArenas()) {
			if (!shouldShowInPartidas(a)) {
				continue;
			}
			ItemStack s = null;
			if (a.getState() == GameState.LOBBY) {

				s = new ItemStack(
						Material.getMaterial(
								Main.getInstance().settings.getConfig().getInt("arenas-lobby-state-item-id")),
						1, (short) Main.getInstance().settings.getConfig().getInt("arenas-lobby-state-item-data"));

				ItemMeta sm = s.getItemMeta();
				sm.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS,
						ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_UNBREAKABLE);

				sm.setDisplayName(Utils.FormatText(p, Main.getInstance().settings.getConfig()
						.getString("arenas-lobby-state-item-name").replaceAll("%arena%", a.getRoomDisplayName())));

				List<String> lore = Main.getInstance().settings.getConfig()
						.getStringList("arenas-lobby-state-item-lore");
				List<String> coloredLore = new ArrayList<>();

				String state = a.getState().toString().toLowerCase();
				Room room = Main.getInstance().roomManager == null ? null : Main.getInstance().roomManager.getRoomByArena(a);
				String leader = getRoomLeaderName(room);
				String mode = getRoomModeName(room);

				for (String s1 : lore) {
					String line = s1.replaceAll("%state%", Utils.capitalizeFirstLetter(state))
							.replaceAll("%map%", Utils.capitalizeFirstLetter(a.getTemplateName()))
								.replaceAll("%room%", a.getRoomDisplayName())
							.replaceAll("%max%", String.valueOf(Main.getInstance().SpawnSize(a)))
							.replaceAll("%players%", String.valueOf(a.players.size()))
							.replaceAll("%leader%", leader)
							.replaceAll("%lider%", leader)
							.replaceAll("%mode%", mode)
							.replaceAll("%modo%", mode)
								.replaceAll("%room%", a.getRoomDisplayName());

					coloredLore.add(Utils.FormatText(p, line));

				}

				addRoomInfoLore(coloredLore, room);
				sm.setLore(coloredLore);
				s.setItemMeta(sm);
				s = applyRoomHead(s, room);

			}

			if (a.getState() == GameState.STARTING) {

				s = new ItemStack(
						Material.getMaterial(
								Main.getInstance().settings.getConfig().getInt("arenas-starting-state-item-id")),
						1, (short) Main.getInstance().settings.getConfig().getInt("arenas-starting-state-item-data"));

				ItemMeta sm = s.getItemMeta();
				sm.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS,
						ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_UNBREAKABLE);

				sm.setDisplayName(Utils.FormatText(p, Main.getInstance().settings.getConfig()
						.getString("arenas-starting-state-item-name").replaceAll("%arena%", a.getRoomDisplayName())));

				List<String> lore = Main.getInstance().settings.getConfig()
						.getStringList("arenas-starting-state-item-lore");
				List<String> coloredLore = new ArrayList<>();

				String state = a.getState().toString().toLowerCase();
				Room room = Main.getInstance().roomManager == null ? null : Main.getInstance().roomManager.getRoomByArena(a);
				String leader = getRoomLeaderName(room);
				String mode = getRoomModeName(room);

				for (String s1 : lore) {
					String line = s1.replaceAll("%state%", Utils.capitalizeFirstLetter(state))
							.replaceAll("%map%", Utils.capitalizeFirstLetter(a.getTemplateName()))
								.replaceAll("%room%", a.getRoomDisplayName())
							.replaceAll("%max%", String.valueOf(Main.getInstance().SpawnSize(a)))
							.replaceAll("%players%", String.valueOf(a.players.size()))
							.replaceAll("%leader%", leader)
							.replaceAll("%lider%", leader)
							.replaceAll("%mode%", mode)
							.replaceAll("%modo%", mode)
								.replaceAll("%room%", a.getRoomDisplayName());

					coloredLore.add(Utils.FormatText(p, line));

				}

				addRoomInfoLore(coloredLore, room);
				sm.setLore(coloredLore);
				s.setItemMeta(sm);
				s = applyRoomHead(s, room);

			}

			if (a.getState() == GameState.INGAME) {

				s = new ItemStack(
						Material.getMaterial(
								Main.getInstance().settings.getConfig().getInt("arenas-ingame-state-item-id")),
						1, (short) Main.getInstance().settings.getConfig().getInt("arenas-ingame-state-item-data"));

				ItemMeta sm = s.getItemMeta();
				sm.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS,
						ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_UNBREAKABLE);

				sm.setDisplayName(Utils.FormatText(p, Main.getInstance().settings.getConfig()
						.getString("arenas-ingame-state-item-name").replaceAll("%arena%", a.getRoomDisplayName())));

				List<String> lore = Main.getInstance().settings.getConfig()
						.getStringList("arenas-ingame-state-item-lore");
				List<String> coloredLore = new ArrayList<>();

				String state = a.getState().toString().toLowerCase();
				Room room = Main.getInstance().roomManager == null ? null : Main.getInstance().roomManager.getRoomByArena(a);
				String leader = getRoomLeaderName(room);
				String mode = getRoomModeName(room);

				for (String s1 : lore) {
					String line = s1.replaceAll("%state%", Utils.capitalizeFirstLetter(state))
							.replaceAll("%map%", Utils.capitalizeFirstLetter(a.getTemplateName()))
								.replaceAll("%room%", a.getRoomDisplayName())
							.replaceAll("%max%", String.valueOf(Main.getInstance().SpawnSize(a)))
							.replaceAll("%players%", String.valueOf(a.players.size()))
							.replaceAll("%leader%", leader)
							.replaceAll("%lider%", leader)
							.replaceAll("%mode%", mode)
							.replaceAll("%modo%", mode)
								.replaceAll("%room%", a.getRoomDisplayName());

					coloredLore.add(Utils.FormatText(p, line));

				}

				addRoomInfoLore(coloredLore, room);
				sm.setLore(coloredLore);
				s.setItemMeta(sm);
				s = applyRoomHead(s, room);

			}
			if (s == null) {
				continue;
			}
			if (!isInventoryFull(main)) {

				main.setItem(main.firstEmpty(), s);

			} else {
				CallOnEmpty(s, main);
			}

		}
		if (!op) {
			op = true;
			p.openInventory(main);
		}
	}

	public void Update() {

		new BukkitRunnable() {

			@Override
			public void run() {

				if (p.getOpenInventory().getType() != InventoryType.CHEST || !p.isOnline()) {
					op = false;
					hash.clear();
					hash2.clear();

					p.closeInventory();

					if (Main.getInstance().sd.containsKey(p.getName())) {
						Main.getInstance().sd.remove(p.getName());
					}

					this.cancel();
					return;
				}

				openInventory();

			}
		}.runTaskTimer(Main.getInstance(), 20, 20);

	}

}
