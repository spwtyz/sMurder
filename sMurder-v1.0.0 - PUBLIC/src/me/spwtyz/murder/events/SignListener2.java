
package me.spwtyz.murder.events;

import java.util.ArrayList;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import me.spwtyz.murder.Arena;
import me.spwtyz.murder.Arenas;
import me.spwtyz.murder.GameState;
import me.spwtyz.murder.Main;
import me.spwtyz.murder.Utils;

public class SignListener2 implements Listener {

	Main plugin;

	public SignListener2(Main plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSignCreate(SignChangeEvent e) {
		Player player = e.getPlayer();
		if ((e.getLine(0).equalsIgnoreCase("[MurderAuto]")) && (player.hasPermission("murder.admin"))) {

			e.setLine(0, Utils.FormatText2(plugin.messages.getConfig().getString("auto-join-sign-line-1")));
			e.setLine(1, Utils.FormatText2(plugin.messages.getConfig().getString("auto-join-sign-line-2")));

			e.setLine(2, Utils.FormatText2(plugin.messages.getConfig().getString("auto-join-sign-line-3")));

			e.setLine(3, Utils.FormatText2(plugin.messages.getConfig().getString("auto-join-sign-line-4")));

		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSignInteract(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (!e.getPlayer().isSneaking()) {
				Block b = e.getClickedBlock();
				if (b != null) {
					if (b.getState() instanceof Sign) {
						Sign sign = (Sign) b.getState();
						if (sign.getLine(0).equalsIgnoreCase(
								Utils.FormatText2(plugin.messages.getConfig().getString("auto-join-sign-line-1")))) {

							if (Arenas.getArenas() == null || Arenas.getArenas().size() == 0) {
								e.getPlayer().sendMessage(
										Utils.FormatText2(plugin.messages.getConfig().getString("no-arenas")));
								return;
							}

							ArrayList<Arena> arenaz = new ArrayList<>();
							if (Arenas.getArenas().size() > 0) {
								for (Arena arena1 : Arenas.getArenas()) {
									if (arena1.getState() == GameState.STARTING
											&& arena1.players.size() < plugin.SpawnSize(arena1)) {
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

								if (!Arenas.isInArena(e.getPlayer()) && a != null) {
									a.addPlayer(e.getPlayer());
								}

							}
						}
					}
				}
			}
		}
	}
}