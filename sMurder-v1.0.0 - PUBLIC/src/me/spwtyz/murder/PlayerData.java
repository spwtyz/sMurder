package me.spwtyz.murder;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerData {

	public Player p;

	int kills = 0;
	int deaths = 0;
	int wins = 0;
	int score = 0;
	int loses = 0;
	int coins = 0;

	public boolean isloaded = false;

	Main plugin;

	public PlayerData(Main plugin, Player p) {
		this.plugin = plugin;
		this.p = p;
		reset();
		Update();

	}

	public void addcoins(Integer a) {
		this.coins = getcoins() + a;

	}

	public void adddeaths(Integer a) {
		this.deaths = getdeaths() + a;
		if (plugin != null && plugin.leaderboardManager != null) plugin.leaderboardManager.updateAllSoon();

	}

	public void addkill(Integer a) {
		this.kills = getkill() + a;
		if (plugin != null && plugin.leaderboardManager != null) plugin.leaderboardManager.updateAllSoon();
		if (plugin != null && plugin.levelManager != null && this.p != null && this.p.isOnline()) {
			plugin.levelManager.addXP(this.p, 15 * a, "Kill");
		}

	}

	public void addlose(Integer a) {
		this.loses = getloses() + a;
		if (plugin != null && plugin.leaderboardManager != null) plugin.leaderboardManager.updateAllSoon();
	}

	public void addscore(Integer a) {
		this.score = getscore() + a;

	}

	public void addwins(Integer a) {
		this.wins = getwins() + a;
		if (plugin != null && plugin.leaderboardManager != null) plugin.leaderboardManager.updateAllSoon();
		if (plugin != null && plugin.levelManager != null && this.p != null && this.p.isOnline()) {
			plugin.levelManager.addXP(this.p, 50 * a, "Vitória");
		}

	}

	public Integer getcoins() {
		return this.coins;
	}

	public Integer getdeaths() {
		return this.deaths;
	}

	public Integer getkill() {
		return this.kills;
	}

	public Integer getloses() {
		return this.loses;
	}

	public Integer getscore() {
		return this.score;
	}

	public Integer getwins() {
		return this.wins;
	}

	public boolean isLoaded() {
		return isloaded;
	}

	public void reset() {
		kills = 0;
		deaths = 0;
		wins = 0;
		loses = 0;
		coins = 0;
		score = 0;
		isloaded = false;
	}

	public void setcoins(Integer a) {
		this.coins = a;

	}

	public void setdeaths(Integer a) {
		this.deaths = a;

	}

	public void setkills(Integer a) {
		this.kills = a;

	}

	public void setlose(Integer a) {
		this.loses = a;
	}

	public void setscore(Integer a) {
		this.score = a;

	}

	public void setwins(Integer a) {
		this.wins = a;

	}

	public void Update() {

		if (!plugin.getConfig().getBoolean("mysql")) {
			this.kills = plugin.api.getKills(p);
			this.deaths = plugin.api.getDeaths(p);
			this.wins = plugin.api.getWins(p);
			this.loses = plugin.api.getLoses(p);
			this.score = plugin.api.getScore(p);
			this.coins = plugin.api.getCoins(p);
			plugin.data.getConfig().set("Names." + p.getUniqueId().toString(), p.getName());
			plugin.data.save();
			isloaded = true;

		}
		if (plugin.getConfig().getBoolean("mysql")) {
			new BukkitRunnable() {

				@Override
				public void run() {

					if (!p.isOnline() || p == null) {
						this.cancel();
						return;
					}
					if (plugin.sql != null && plugin.sql.isConnected()) {

						plugin.loadPlayer(p);
						this.cancel();
						return;
					}

				}
			}.runTaskTimer(plugin, 20, 20);

		}
	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}
