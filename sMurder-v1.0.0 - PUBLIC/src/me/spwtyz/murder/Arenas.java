package me.spwtyz.murder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Arenas {

	private static HashMap<String, Arena> arenas = new HashMap<>();
	private static HashMap<String, Arena> playerArena = new HashMap<>();
	private static List<Arena> list = new ArrayList<>();

	
	
	public static void addArena(Arena arena) {
	    if (!arenas.containsKey(arena.getName())) {
	        arenas.put(arena.getName(), arena);

	        if (!list.contains(arena)) {
	            list.add(arena);
	        }

	        // 🔥 REGISTRA O EVENTO AQUI
	        org.bukkit.Bukkit.getPluginManager().registerEvents(arena, arena.plugin);
	    }
	}
	
	//public static void addArena(Arena arena) {
		//if (!arenas.containsKey(arena.getName())) {
			//arenas.put(arena.getName(), arena);
			//if (!list.contains(arena)) {
				//list.add(arena);
			//}
		//}
	//}
	
	public Plugin getPlugin() {
	    return plugin;
	}
	
	
	public static void addArena(Player player, Arena arena) {
		if (!playerArena.containsKey(player.getName())) {
			playerArena.put(player.getName(), arena);
		}
	}

	public static boolean arenaExists(String ArenaName) {
		if (arenas.containsKey(ArenaName)) {
			return true;
		}
		return false;
	}

	public static Arena getArena(Player player) {
		String name = player.getName();
		if (playerArena.containsKey(name)) {
			Arena arena = playerArena.get(name);

			return arena;
		}
		return null;
	}

	public static Arena getArena(String ArenaName) {
		if (arenas.containsKey(ArenaName)) {
			Arena arena = arenas.get(ArenaName);
			return arena;
		}
		return null;
	}

	public static List<Arena> getArenas() {
		return list;
	}

	public static boolean isInArena(Player p) {
		if (playerArena.containsKey(p.getName())) {
			return true;
		}
		return false;
	}

	public static void removeArena(Player player) {
		if (playerArena.containsKey(player.getName())) {
			playerArena.remove(player.getName());
		}

	}

	public static void removeArena(Arena arena) {
		if (arena == null) return;
		arenas.remove(arena.getName());
		list.remove(arena);
	}

	Main plugin;

	public Arenas(Main plugin) {
		this.plugin = plugin;
	}
}