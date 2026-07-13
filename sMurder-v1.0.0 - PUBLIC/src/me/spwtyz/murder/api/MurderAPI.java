package me.spwtyz.murder.api;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.spwtyz.murder.Arena;
import me.spwtyz.murder.Arenas;
import me.spwtyz.murder.Main;
import me.spwtyz.murder.PlayerData;

public class MurderAPI {
    public Main plugin;

    public MurderAPI(Main plugin) {
        this.plugin = plugin;
    }

    // ------------------- GETTERS -------------------
    public int getCoins(Player p) {
        PlayerData pd = plugin.getPlayerData(p);
        if (pd != null) {
            return pd.getcoins();
        }
        return plugin.data.getConfig().getInt("Coins." + p.getUniqueId() + ".coin");
    }

    public int getCash(Player p) {
        return plugin.data.getConfig().getInt("Cash." + p.getUniqueId() + ".cash");
    }

    public int getDeaths(Player p) {
        return plugin.data.getConfig().getInt("Deaths." + p.getUniqueId() + ".death");
    }

    public int getKills(Player p) {
        return plugin.data.getConfig().getInt("Kills." + p.getUniqueId() + ".kill");
    }

    public int getLoses(Player p) {
        return plugin.data.getConfig().getInt("Loses." + p.getUniqueId() + ".lose");
    }

    public int getScore(Player p) {
        return plugin.data.getConfig().getInt("Score." + p.getUniqueId() + ".score");
    }

    public int getWins(Player p) {
        return plugin.data.getConfig().getInt("Wins." + p.getUniqueId() + ".win");
    }

    // ------------------- COINS -------------------
    public void addCoins(Player p, int amount) {
        int coins = getCoins(p) + amount;
        PlayerData pd = plugin.getPlayerData(p);
        if (pd != null) {
            pd.setcoins(coins);
        }
        plugin.data.getConfig().set("Coins." + p.getUniqueId() + ".coin", coins);
        plugin.data.getConfig().set("Cash." + p.getUniqueId() + ".cash", coins);
        plugin.data.save();
    }

    public boolean removeCoins(Player p, int amount) {
        if (amount <= 0) {
            return true;
        }

        int coins = getCoins(p);
        if (coins < amount) {
            return false; // não tem coins suficientes
        }

        int newCoins = coins - amount;

        // IMPORTANTE: atualiza o PlayerData em memoria tambem.
        // Sem isso, ao sair/fechar o servidor o plugin podia salvar os coins antigos por cima da compra.
        PlayerData pd = plugin.getPlayerData(p);
        if (pd != null) {
            pd.setcoins(newCoins);
        }

        plugin.data.getConfig().set("Coins." + p.getUniqueId() + ".coin", newCoins);
        plugin.data.getConfig().set("Cash." + p.getUniqueId() + ".cash", newCoins);
        plugin.data.save();
        return true;
    }

	public void heroreward(Player p) {
		if (!plugin.settings.getConfig().getBoolean("hero-rewards")) {
			return;
		}
		if (Arenas.isInArena(p)) {
			Arena a = Arenas.getArena(p);
			if (!plugin.rewards.getConfig().contains(a.getName() + ".hero-rewards")
					&& plugin.rewards.getConfig().contains("hero-rewards")) {
				int list = plugin.rewards.getConfig().getConfigurationSection("hero-rewards").getKeys(true).size();
				int random = a.getRandom(0, list);

				if (!plugin.rewards.getConfig().contains("hero-rewards." + random)) {
					heroreward(p);
					return;
				}
				List<String> ench = plugin.rewards.getConfig().getStringList("hero-rewards." + random);
				for (String s : ench) {
					Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
							s.replaceAll("%player%", p.getName()));
				}
				return;
			}
			if (plugin.rewards.getConfig().contains(a.getName() + ".hero-rewards")) {

				int list = plugin.rewards.getConfig().getConfigurationSection(a.getName() + ".hero-rewards")
						.getKeys(true).size();
				int random = a.getRandom(0, list);
				if (!plugin.rewards.getConfig().contains(a.getName() + ".hero-rewards." + random)) {
					heroreward(p);
					return;
				}
				List<String> ench = plugin.rewards.getConfig().getStringList(a.getName() + ".hero-rewards." + random);
				for (String s : ench) {
					Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
							s.replaceAll("%player%", p.getName()));
				}
				return;
			}
		}
	}

	public void losereward(Player p) {
		if (!plugin.settings.getConfig().getBoolean("lose-rewards")) {
			return;
		}
		if (Arenas.isInArena(p)) {
			Arena a = Arenas.getArena(p);
			if (!plugin.rewards.getConfig().contains(a.getName() + ".lose-rewards")
					&& plugin.rewards.getConfig().contains("lose-rewards")) {
				int list = plugin.rewards.getConfig().getConfigurationSection("lose-rewards").getKeys(true).size();
				int random = a.getRandom(0, list);

				if (!plugin.rewards.getConfig().contains("lose-rewards." + random)) {
					losereward(p);
					return;
				}
				List<String> ench = plugin.rewards.getConfig().getStringList("lose-rewards." + random);
				for (String s : ench) {
					Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
							s.replaceAll("%player%", p.getName()));
				}
				return;
			}
			if (plugin.rewards.getConfig().contains(a.getName() + ".lose-rewards")) {

				int list = plugin.rewards.getConfig().getConfigurationSection(a.getName() + ".lose-rewards")
						.getKeys(true).size();
				int random = a.getRandom(0, list);
				if (!plugin.rewards.getConfig().contains(a.getName() + ".lose-rewards." + random)) {
					losereward(p);
					return;
				}
				List<String> ench = plugin.rewards.getConfig().getStringList(a.getName() + ".lose-rewards." + random);
				for (String s : ench) {
					Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
							s.replaceAll("%player%", p.getName()));
				}
				return;
			}
		}
	}

	public void sendActionBar(Player player, String message) {
		if (!player.isOnline()) {
			return;
		}

		if (plugin.nmsver.startsWith("v1_12_")) {
			sendActionBarPost112(player, message);
		} else {
			sendActionBarPre112(player, message);
		}
	}

	private void sendActionBarPost112(Player player, String message) {
		if (!player.isOnline()) {
			return; // Player may have logged out
		}

		try {
			Class<?> craftPlayerClass = Class
					.forName("org.bukkit.craftbukkit." + plugin.nmsver + ".entity.CraftPlayer");
			Object craftPlayer = craftPlayerClass.cast(player);
			Object ppoc;
			Class<?> c4 = Class.forName("net.minecraft.server." + plugin.nmsver + ".PacketPlayOutChat");
			Class<?> c5 = Class.forName("net.minecraft.server." + plugin.nmsver + ".Packet");
			Class<?> c2 = Class.forName("net.minecraft.server." + plugin.nmsver + ".ChatComponentText");
			Class<?> c3 = Class.forName("net.minecraft.server." + plugin.nmsver + ".IChatBaseComponent");
			Class<?> chatMessageTypeClass = Class.forName("net.minecraft.server." + plugin.nmsver + ".ChatMessageType");
			Object[] chatMessageTypes = chatMessageTypeClass.getEnumConstants();
			Object chatMessageType = null;
			for (Object obj : chatMessageTypes) {
				if (obj.toString().equals("GAME_INFO")) {
					chatMessageType = obj;
				}
			}
			Object o = c2.getConstructor(new Class<?>[] { String.class }).newInstance(message);
			ppoc = c4.getConstructor(new Class<?>[] { c3, chatMessageTypeClass }).newInstance(o, chatMessageType);
			Method m1 = craftPlayerClass.getDeclaredMethod("getHandle");
			Object h = m1.invoke(craftPlayer);
			Field f1 = h.getClass().getDeclaredField("playerConnection");
			Object pc = f1.get(h);
			Method m5 = pc.getClass().getDeclaredMethod("sendPacket", c5);
			m5.invoke(pc, ppoc);
		} catch (Exception ex) {
			ex.printStackTrace();
			plugin.works = false;
		}
	}

	private void sendActionBarPre112(Player player, String message) {
		if (!player.isOnline()) {
			return;
		}

		try {
			Class<?> craftPlayerClass = Class
					.forName("org.bukkit.craftbukkit." + plugin.nmsver + ".entity.CraftPlayer");
			Object craftPlayer = craftPlayerClass.cast(player);
			Object ppoc;
			Class<?> c4 = Class.forName("net.minecraft.server." + plugin.nmsver + ".PacketPlayOutChat");
			Class<?> c5 = Class.forName("net.minecraft.server." + plugin.nmsver + ".Packet");
			if (plugin.useOldMethods) {
				Class<?> c2 = Class.forName("net.minecraft.server." + plugin.nmsver + ".ChatSerializer");
				Class<?> c3 = Class.forName("net.minecraft.server." + plugin.nmsver + ".IChatBaseComponent");
				Method m3 = c2.getDeclaredMethod("a", String.class);
				Object cbc = c3.cast(m3.invoke(c2, "{\"text\": \"" + message + "\"}"));
				ppoc = c4.getConstructor(new Class<?>[] { c3, byte.class }).newInstance(cbc, (byte) 2);
			} else {
				Class<?> c2 = Class.forName("net.minecraft.server." + plugin.nmsver + ".ChatComponentText");
				Class<?> c3 = Class.forName("net.minecraft.server." + plugin.nmsver + ".IChatBaseComponent");
				Object o = c2.getConstructor(new Class<?>[] { String.class }).newInstance(message);
				ppoc = c4.getConstructor(new Class<?>[] { c3, byte.class }).newInstance(o, (byte) 2);
			}
			Method m1 = craftPlayerClass.getDeclaredMethod("getHandle");
			Object h = m1.invoke(craftPlayer);
			Field f1 = h.getClass().getDeclaredField("playerConnection");
			Object pc = f1.get(h);
			Method m5 = pc.getClass().getDeclaredMethod("sendPacket", c5);
			m5.invoke(pc, ppoc);
		} catch (Exception ex) {
			ex.printStackTrace();
			plugin.works = false;
		}
	}

	public void setNonSQLData(Player p, int kills, int deaths, int loses, int wins, int coins, int score) {

		if (!plugin.getPlayerData(p).isloaded) {
			return;
		}
		plugin.data.getConfig().set("Deaths." + p.getUniqueId() + ".death", deaths);
		plugin.data.getConfig().set("Loses." + p.getUniqueId() + ".lose", loses);
		plugin.data.getConfig().set("Wins." + p.getUniqueId() + ".win", wins);
		plugin.data.getConfig().set("Kills." + p.getUniqueId() + ".kill", kills);
		plugin.data.getConfig().set("Score." + p.getUniqueId() + ".score", score);
		plugin.data.getConfig().set("Coins." + p.getUniqueId() + ".coin", coins);
		plugin.data.getConfig().set("Cash." + p.getUniqueId() + ".cash", coins);
		plugin.data.save();
	}

	public void setSQLData(Player p, int kills, int deaths, int loses, int wins, int coins, int score) {
		if (plugin.sql == null || !plugin.sql.isConnected()) {
			return;
		}
		if (!plugin.getPlayerData(p).isloaded) {
			return;
		}
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				Connection connection = plugin.sql.getConnection();
				try {

					PreparedStatement insert = connection.prepareStatement(
							"UPDATE MurderData SET wins = ?, deaths = ?, loses = ?, kills = ?, coins = ?, score = ? WHERE playername = ?");

					insert.setInt(1, wins);
					insert.setInt(2, deaths);
					insert.setInt(3, loses);
					insert.setInt(4, kills);
					insert.setInt(5, coins);
					insert.setInt(6, score);
					insert.setString(7, p.getName());
					insert.executeUpdate();
					plugin.ClosePreparedStatement(insert);

				} catch (SQLException e) {

					e.printStackTrace();
				}

			}

		});
	}

	public void setSQLDataInstantly(Player p, int kills, int deaths, int loses, int wins, int coins, int score) {
		if (plugin.sql == null || !plugin.sql.isConnected()) {
			return;
		}
		if (!plugin.getPlayerData(p).isloaded) {
			return;
		}
		Connection connection = plugin.sql.getConnection();
		try {
			PreparedStatement insert = connection.prepareStatement(
					"UPDATE MurderData SET wins = ?, deaths = ?, loses = ?, kills = ?, coins = ?, score = ? WHERE playername = ?");
			insert.setInt(1, wins);
			insert.setInt(2, deaths);
			insert.setInt(3, loses);
			insert.setInt(4, kills);
			insert.setInt(5, coins);
			insert.setInt(6, score);
			insert.setString(7, p.getName());
			insert.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void winreward(Player p) {
		if (!plugin.settings.getConfig().getBoolean("win-rewards")) {
			return;
		}
		if (Arenas.isInArena(p)) {
			Arena a = Arenas.getArena(p);
			if (!plugin.rewards.getConfig().contains(a.getName() + ".win-rewards")
					&& plugin.rewards.getConfig().contains("win-rewards")) {
				int list = plugin.rewards.getConfig().getConfigurationSection("win-rewards").getKeys(true).size();
				int random = a.getRandom(0, list);

				if (!plugin.rewards.getConfig().contains("win-rewards." + random)) {
					winreward(p);
					return;
				}
				List<String> ench = plugin.rewards.getConfig().getStringList("win-rewards." + random);
				for (String s : ench) {
					Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
							s.replaceAll("%player%", p.getName()));
				}
				return;
			}
			if (plugin.rewards.getConfig().contains(a.getName() + ".win-rewards")) {

				int list = plugin.rewards.getConfig().getConfigurationSection(a.getName() + ".win-rewards")
						.getKeys(true).size();
				int random = a.getRandom(0, list);
				if (!plugin.rewards.getConfig().contains(a.getName() + ".win-rewards." + random)) {
					winreward(p);
					return;
				}
				List<String> ench = plugin.rewards.getConfig().getStringList(a.getName() + ".win-rewards." + random);
				for (String s : ench) {
					Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
							s.replaceAll("%player%", p.getName()));
				}
				return;
			}
		}
	}

}
