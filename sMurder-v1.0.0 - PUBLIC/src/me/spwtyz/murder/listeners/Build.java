package me.spwtyz.murder.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;


public class Build {
	
	  private static List<Player> builders = new ArrayList<>();
	  
	  public static void performCommand(Player player) {
	    if (isBuilder(player)) {
	      builders.remove(player);
	      player.sendMessage("&6&lMURDER &7> &cVoce desativou o modo construtor.");
	    } else {
	      builders.add(player);
	      player.sendMessage("&6&lMURDER &7> &aVoce ativou o modo construtor.");
	    }
	  }
	  
	  public static void clear(Player player) {
	    builders.remove(player);
	  }
	  
	  public static boolean isBuilder(Player player) {
	    return builders.contains(player);
	  }
	}
