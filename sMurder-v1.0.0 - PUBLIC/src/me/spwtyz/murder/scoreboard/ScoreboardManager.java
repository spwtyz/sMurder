package me.spwtyz.murder.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class ScoreboardManager {

	private Scoreboard scoreboard;
	private int lastPage;
	private final String uuid;

	public ScoreboardManager(String uuid) {
		scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

		this.uuid = uuid;

		Objective obj = getPage(0);

		obj.setDisplaySlot(DisplaySlot.SIDEBAR);

	}

	public void addTeam(String teamName, OfflinePlayer[] players) {
		if (scoreboard.getTeam(teamName) == null) {
			Team team = scoreboard.registerNewTeam(teamName);
			team.setNameTagVisibility(NameTagVisibility.NEVER);

			team.setPrefix("" + ChatColor.RESET);

			for (OfflinePlayer player : players) {
				if (!team.hasPlayer(player)) {
					team.addPlayer(player);
				}
			}

		}
	}

	public void changePage(int page) {

		lastPage = page;

		if (scoreboard.getObjective(DisplaySlot.SIDEBAR) != null)
			getPage(page).setDisplaySlot(DisplaySlot.SIDEBAR);
	}

	public int getLastPage() {
		return lastPage;
	}

	private Objective getPage(int page) {

		if (page > 15)
			throw new IllegalArgumentException("Page number must be between 0 and 15");

		Objective obj = scoreboard.getObjective("page" + page);
		if (obj == null) {

			obj = scoreboard.registerNewObjective("page" + page, "dummy");

			for (int i = 0; i < 15; i++)
				scoreboard.registerNewTeam(ChatColor.getByChar(Integer.toHexString(page))
						+ ChatColor.getByChar(Integer.toHexString(i)).toString());
		}
		return obj;
	}

	private Player getPlayer() {
		return Bukkit.getPlayer(uuid);
	}

	public Scoreboard getScoreboard() {
		return scoreboard;
	}

	public void setLine(int page, int index, String string) {
		setLine(page, index, string, true);
	}

	public void setLine(int page, int index, String string, boolean copyPreviousColors) {

		if (string.length() > 16)
			setLine(page, index, string.substring(0, 16), string.substring(16), copyPreviousColors);
		else
			setLine(page, index, string, "", copyPreviousColors);
	}

	public void setLine(int page, int index, String prefix, String suffix) {
		setLine(page, index, prefix, suffix, true);
	}

	public void setLine(int page, int index, String prefix, String suffix, boolean copyPreviousColors) {
		if (prefix == null) prefix = "";
		if (suffix == null) suffix = "";
		prefix = ChatColor.translateAlternateColorCodes('&', prefix);
		suffix = ChatColor.translateAlternateColorCodes('&', suffix);

		if (prefix.length() > 16)
			prefix = prefix.substring(0, 16);
		if (suffix.length() > 16)
			suffix = suffix.substring(0, 16);
		// Minecraft 1.8 sidebar supports only 15 lines: 0 - 14.
		// Ignore extra configured lines instead of crashing the repeating scoreboard task.
		if (index < 0 || index > 14)
			return;

		Objective obj = getPage(page);

		String name = ChatColor.getByChar(Integer.toHexString(page))
				+ ChatColor.getByChar(Integer.toHexString(index)).toString();

		Score score = obj.getScore(name + ChatColor.RESET);
		Team team = scoreboard.getTeam(name);

		if (!score.isScoreSet()) {
			score.setScore(index);
			team.addEntry(score.getEntry());
		}
		team.setPrefix(prefix);

		if (copyPreviousColors) {

			suffix = ChatColor.getLastColors(prefix) + suffix;
			if (suffix.length() > 16)

				suffix = suffix.substring(0, 16);
		}

		team.setSuffix(suffix);

	}

	public void setLineBlank(int page, int index) {
		setLine(page, index, "", "", false);
	}

	public void setTitle(int page, String title) {

		if (title == null)
			title = "";

		title = ChatColor.translateAlternateColorCodes('&', title);

		if (title.length() > 32)
			title = title.substring(0, 32);

		getPage(page).setDisplayName(title);
	}

	public void toggleScoreboard() {

		if (getPlayer() != null && !getPlayer().getScoreboard().equals(scoreboard)) {

			getPlayer().setScoreboard(scoreboard);
			if (scoreboard.getTeam("team") != null) {
				scoreboard.getTeam("team").unregister();
			}

		} else

		if (scoreboard.getObjective(DisplaySlot.SIDEBAR) == null) {
			getPage(lastPage).setDisplaySlot(DisplaySlot.SIDEBAR);

		}
	}

}
