package me.spwtyz.murder;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;

public class Utils {

	public static String capitalizeFirstLetter(String original) {
		if (original == null || original.length() == 0) {
			return original;
		}
		return original.substring(0, 1).toUpperCase() + original.substring(1);
	}
	

	public static String FormatText(Player p, String s) {
		if (s == null) {
			s = "";
		}
		s = s.replaceAll("&", "§");
		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			return PlaceholderAPI.setPlaceholders(p, s);
		}
		return s;
	}

	public static String FormatText2(String s) {
		if (s == null) {
			return "";
		}
		return s.replaceAll("&", "§");
	}

	public static String formattominutes(int secs) {
		int remainder = secs % 3600;
		int minutes = remainder / 60;
		int seconds = remainder % 60;

		return new StringBuilder().append(minutes).append(":").append(seconds < 10 ? "0" : "").append(seconds)
				.toString();
	}

	public static Color getColor(String paramString) {
		String temp = paramString;
		if (temp.equalsIgnoreCase("AQUA"))
			return Color.AQUA;
		if (temp.equalsIgnoreCase("BLACK"))
			return Color.BLACK;
		if (temp.equalsIgnoreCase("BLUE"))
			return Color.BLUE;
		if (temp.equalsIgnoreCase("FUCHSIA"))
			return Color.FUCHSIA;
		if (temp.equalsIgnoreCase("GRAY"))
			return Color.GRAY;
		if (temp.equalsIgnoreCase("GREEN"))
			return Color.GREEN;
		if (temp.equalsIgnoreCase("LIME"))
			return Color.LIME;
		if (temp.equalsIgnoreCase("MAROON"))
			return Color.MAROON;
		if (temp.equalsIgnoreCase("NAVY"))
			return Color.NAVY;
		if (temp.equalsIgnoreCase("OLIVE"))
			return Color.OLIVE;
		if (temp.equalsIgnoreCase("ORANGE"))
			return Color.ORANGE;
		if (temp.equalsIgnoreCase("PURPLE"))
			return Color.PURPLE;
		if (temp.equalsIgnoreCase("RED"))
			return Color.RED;
		if (temp.equalsIgnoreCase("SILVER"))
			return Color.SILVER;
		if (temp.equalsIgnoreCase("TEAL"))
			return Color.TEAL;
		if (temp.equalsIgnoreCase("WHITE"))
			return Color.WHITE;
		if (temp.equalsIgnoreCase("YELLOW"))
			return Color.YELLOW;
		return null;
	}

	public static boolean isInt(String s) {
		try {
			Integer.parseInt(s);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

}
