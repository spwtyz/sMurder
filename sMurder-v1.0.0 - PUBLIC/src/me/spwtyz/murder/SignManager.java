package me.spwtyz.murder;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class SignManager {
	Main plugin;

	public SignManager(Main plugin) {
		this.plugin = plugin;
	}

	public void addSign(Arena a, Location loc) {
		String Arena = a.getName();
		plugin.arenas.getConfig().set("Signs." + Arena + ".X", Double.valueOf(loc.getX()));
		plugin.arenas.getConfig().set("Signs." + Arena + ".Y", Double.valueOf(loc.getY()));
		plugin.arenas.getConfig().set("Signs." + Arena + ".Z", Double.valueOf(loc.getZ()));
		plugin.arenas.getConfig().set("Signs." + Arena + ".World", loc.getWorld().getName());
		plugin.arenas.save();

	}

	public Block getBlockAttachedToSign(Sign s) {
		org.bukkit.material.Sign s_ = (org.bukkit.material.Sign) s.getBlock().getState().getData();
		Block attachedBlock = s.getBlock().getRelative(s_.getAttachedFace());
		return attachedBlock;

	}

	public List<Location> getSigns(Arena a) {
		String ArenaName = a.getName();
		if (!plugin.arenas.getConfig().contains("Signs." + ArenaName)) {
			return null;
		}
		List<Location> locs = new ArrayList<>();
		double x = plugin.arenas.getConfig().getDouble("Signs." + ArenaName + ".X");
		double y = plugin.arenas.getConfig().getDouble("Signs." + ArenaName + ".Y");
		double z = plugin.arenas.getConfig().getDouble("Signs." + ArenaName + ".Z");
		World world = Bukkit.getWorld(plugin.arenas.getConfig().getString("Signs." + ArenaName + ".World"));
		Location loc = new Location(world, x, y, z);
		locs.add(loc);
		return locs;
	}

	public void removeSign(Arena a, Location loc) {
		String ArenaName = a.getName();
		plugin.arenas.getConfig().set("Signs." + ArenaName, null);

		resetSigns(a);
		plugin.arenas.save();
	}

	private void resetSigns(Arena a) {
		String ArenaName = a.getName();

		if (plugin.arenas.getConfig().contains("Signs." + ArenaName + ".X.")) {

			double x = plugin.arenas.getConfig().getDouble("Signs." + ArenaName + ".X");
			double y = plugin.arenas.getConfig().getDouble("Signs." + ArenaName + ".Y");
			double z = plugin.arenas.getConfig().getDouble("Signs." + ArenaName + ".Z");
			String world = plugin.arenas.getConfig().getString("Signs." + ArenaName + ".World");

			plugin.arenas.getConfig().set("Signs." + ArenaName + ".X", null);
			plugin.arenas.getConfig().set("Signs." + ArenaName + ".Y", null);
			plugin.arenas.getConfig().set("Signs." + ArenaName + ".Z", null);
			plugin.arenas.getConfig().set("Signs." + ArenaName + ".World", null);

			plugin.arenas.getConfig().set("Signs." + ArenaName + ".X", x);
			plugin.arenas.getConfig().set("Signs." + ArenaName + ".Y", y);
			plugin.arenas.getConfig().set("Signs." + ArenaName + ".Z", z);
			plugin.arenas.getConfig().set("Signs." + ArenaName + ".World", world);
			plugin.arenas.save();
		}

	}

	@SuppressWarnings("deprecation")
	public void updateSigns(Arena a) {

		if (!plugin.arenas.getConfig().contains("Signs." + a.getName())) {
			return;
		}
		for (Location loc : getSigns(a)) {

			if ((loc.getBlock().getState() instanceof Sign)) {
				Sign e = (Sign) loc.getBlock().getState();
				e.setLine(3,
						Utils.FormatText2(plugin.messages.getConfig()
								.getString("players").replaceAll("max", String.valueOf(plugin.SpawnSize(a)))
								.replaceAll("min", String.valueOf(a.players.size()))));
				e.setLine(0, Utils.FormatText2(plugin.messages.getConfig().getString("sign-header")));
				e.setLine(1, Utils.FormatText2(
						plugin.messages.getConfig().getString("sign-arena").replaceAll("%arena%", a.getName())));

				if (a.getState() == GameState.INGAME) {
					e.setLine(2, Utils.FormatText2(plugin.messages.getConfig().getString("sign-ingame")));
					if (plugin.settings.getConfig().getBoolean("block-state-behind-signs")) {
						getBlockAttachedToSign(e).setType(Material
								.getMaterial(plugin.settings.getConfig().getInt("block-behind-sign-ingame-state-id")));
						getBlockAttachedToSign(e).setData(
								(byte) plugin.settings.getConfig().getInt("block-behind-sign-ingame-state-durability"));
					}

				} else {
					if (a.getState() == GameState.LOBBY) {
						e.setLine(2, Utils.FormatText2(plugin.messages.getConfig().getString("sign-lobby")));
						if (plugin.settings.getConfig().getBoolean("block-state-behind-signs")) {
							getBlockAttachedToSign(e).setType(Material.getMaterial(
									plugin.settings.getConfig().getInt("block-behind-sign-lobby-state-id")));
							getBlockAttachedToSign(e).setData((byte) plugin.settings.getConfig()
									.getInt("block-behind-sign-lobby-state-durability"));
						}

					} else {

						if (a.getState() == GameState.STARTING) {
							e.setLine(2, Utils.FormatText2(plugin.messages.getConfig().getString("sign-starting")));
							if (plugin.settings.getConfig().getBoolean("block-state-behind-signs")) {
								getBlockAttachedToSign(e).setType(Material.getMaterial(
										plugin.settings.getConfig().getInt("block-behind-sign-starting-state-id")));
								getBlockAttachedToSign(e).setData((byte) plugin.settings.getConfig()
										.getInt("block-behind-sign-starting-state-durability"));
							}
						}
					}
				}

				e.update();
			}
		}
	}

}
