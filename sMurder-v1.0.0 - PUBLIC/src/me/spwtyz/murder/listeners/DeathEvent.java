package me.spwtyz.murder.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.golde.bukkit.corpsereborn.CorpseAPI.CorpseAPI;
import org.golde.bukkit.corpsereborn.nms.Corpses.CorpseData;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;


import me.spwtyz.murder.Arena;
import me.spwtyz.murder.Arenas;
import me.spwtyz.murder.Main;
import me.spwtyz.murder.PlayerType;
import me.spwtyz.murder.Utils;
import me.spwtyz.murder.cosmetics.CosmeticEffectManager;
import me.spwtyz.murder.events.TitleAPI;



public class DeathEvent implements Listener {

	Main plugin;
	
	
	public DeathEvent(Main plugin) {
		this.plugin = plugin;
	}

	
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Location deathLocation = player.getLocation();

        if (Arenas.isInArena(player)) {
            // TPS fix: holograma de morte agora e opcional. HolographicDisplays em toda kill
            // pode causar travada forte, principalmente quando a arma do detetive mata.
            if (plugin.getConfig().getBoolean("death-hologram-enabled", false)
                    && Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
                ItemStack playerHead = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
                playerHead = setSkullOwner(playerHead, player.getName());

                final Hologram hologram = HologramsAPI.createHologram(plugin, deathLocation.add(0, 2, 0));
                hologram.appendItemLine(playerHead);
                hologram.appendTextLine("");
                hologram.appendTextLine(ChatColor.RED + "O jogador " + player.getName() + " morreu!");

                Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                    @Override
                    public void run() {
                        try { hologram.delete(); } catch (Throwable ignored) {}
                    }
                }, 80L);
            }

            if (plugin.getConfig().getBoolean("death-sound-enabled", true)) {
                player.getWorld().playSound(player.getLocation(), Sound.WITHER_DEATH, 0.7f, 1.0f);
            }
        }
    }

    private ItemStack setSkullOwner(ItemStack itemStack, String ownerName) {
        if (itemStack.getType() == Material.SKULL_ITEM && itemStack.getDurability() == 3) {
            SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
            meta.setOwner(ownerName);
            itemStack.setItemMeta(meta);
            
        }
        return itemStack;
    }

    //@EventHandler
    //public void onPlayerDeath223(PlayerDeathEvent event) {
        
        //Location deathLocation = event.getEntity().getLocation();

        //if (Arenas.isInArena(event.getEntity())) {
            
           // BloodDrop bloodDrop = new BloodDrop(deathLocation);

            
            //bloodDrop.spawn();

            
            //plugin.getServer().getScheduler().runTaskLater(plugin, () -> bloodDrop.remove(), 100L);
        //}
   // }




	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDeath2(org.bukkit.event.entity.PlayerDeathEvent e) {
		Player p = e.getEntity().getPlayer();
		if (Arenas.isInArena(p)) {
			p.setHealth(p.getMaxHealth());
			e.setDroppedExp(0);
			e.getDrops().clear();
			e.setDeathMessage("");

			Arena a = Arenas.getArena(p);

			// Aplica o efeito de morte selecionado na loja antes de mover para espectador/remover da partida.
			CosmeticEffectManager.playDeathEffect(plugin, p);

			CorpseData sabotageCorpseData = null;
			// CorpseReborn deve aparecer somente no modo Sabotage.
			// Nos outros modos o sistema de morte normal continua sem corpo no chão.
			if (a.getGameMode() == me.spwtyz.murder.GameModeType.SABOTAGE
					&& Bukkit.getPluginManager().isPluginEnabled("CorpseReborn")) {
				CorpseData s = CorpseAPI.spawnCorpse(p, p.getLocation());
				sabotageCorpseData = s;
				a.data.add(s);

			}
			if (a.getGameMode() == me.spwtyz.murder.GameModeType.SABOTAGE && plugin.sabotageTaskManager != null) {
				plugin.sabotageTaskManager.handleDeath(p, e.getEntity().getKiller(), a, sabotageCorpseData);
			}

			if (!(e.getEntity().getKiller() instanceof Player)) {
				if (plugin.replayManager != null) {
					if (plugin.replayManager != null) plugin.replayManager.saveDeathReplay(p, a);
				}


				if (plugin.settings.getConfig().getBoolean("death-messages")) {
					for (Player x : a.getPlayers()) {
						x.sendMessage(Utils.FormatText(x, plugin.messages.getConfig().getString("death-message")
								.replaceAll("%player%", e.getEntity().getPlayer().getName())));
					}
				}
				if (plugin.getPlayerData(p) != null) {
					plugin.getPlayerData(p).adddeaths(1);
					plugin.getPlayerData(p).addlose(1);

				}

				if (Bukkit.getPlayer(a.Murderer) != null
						&& !Bukkit.getPlayer(a.Murderer).getName().equalsIgnoreCase(p.getName())) {
					Player replayKiller = Bukkit.getPlayer(a.Murderer);
					if (plugin.replayManager != null) {
						if (plugin.replayManager != null) plugin.replayManager.saveKillReplay(p, replayKiller, a);
					}
					a.addscore(Bukkit.getPlayer(a.Murderer), plugin.settings.getConfig().getInt("score-on-kill"),
							Utils.FormatText(Bukkit.getPlayer(a.Murderer), plugin.messages.getConfig().getString("kill-reason")));
					if (plugin.getPlayerData(Bukkit.getPlayer(a.Murderer)) != null) {
						plugin.getPlayerData(Bukkit.getPlayer(a.Murderer)).addkill(1);
					}

					a.addkill(Bukkit.getPlayer(a.Murderer), 1);
				}
				if (Arenas.isInArena(p)) {
					a.removePlayer(p, "death");
				}
				return;
			}

			if (e.getEntity().getKiller() instanceof Player) {
				Player player = e.getEntity();
				Player killer = e.getEntity().getKiller();
				if (a.getGameMode() == me.spwtyz.murder.GameModeType.SABOTAGE && plugin.sabotageTaskManager != null && a.getType(killer) == PlayerType.Murderer) {
					plugin.sabotageTaskManager.resetMurderKillCooldown(killer);
				}

				// Salva replay somente quando existe uma kill real dentro da partida.
				// Nao grava lobby e nao cria replay de movimento aleatorio.
				if (plugin.replayManager != null) {
					if (plugin.replayManager != null) plugin.replayManager.saveKillReplay(player, killer, a);
				}

				if (plugin.getPlayerData(p) != null) {
					plugin.getPlayerData(p).adddeaths(1);
					plugin.getPlayerData(p).addlose(1);

				}

				if (plugin.getPlayerData(killer) != null) {
					plugin.getPlayerData(killer).addkill(1);

				}

				a.addkill(killer, 1);
				TitleAPI.sendTitle(player, 0, 60, 0, Utils.FormatText(
						player, plugin.messages.getConfig().getString("death-title").replaceAll("%killer%", killer.getName())));
				TitleAPI.sendSubtitle(player, 0, 60, 0, Utils.FormatText(player, plugin.messages.getConfig()
						.getString("death-subtitle").replaceAll("%killer%", killer.getName())));

				if (plugin.settings.getConfig().getBoolean("enable-sounds")) {
					killer.playSound(p.getLocation(),
							Sound.valueOf(plugin.settings.getConfig().getString("KILL_SOUND")), 1, 1);
				}

				if (a.getType(killer) != PlayerType.Murderer && a.getType(p) == PlayerType.Murderer
						&& a.getType(p) != PlayerType.None && a.getType(killer) != PlayerType.None) {
					if (Arenas.isInArena(p)) {
						a.removePlayer(p, "death");
					}
					a.addscore(killer, plugin.settings.getConfig().getInt("score-on-kill"),
							Utils.FormatText(killer, plugin.messages.getConfig().getString("kill-reason")));

				}
				if (a.getType(killer) != PlayerType.Murderer && a.getType(p) != PlayerType.Murderer
						&& a.getType(p) != PlayerType.None && a.getType(killer) != PlayerType.None) {
					if (Arenas.isInArena(p)) {
						a.removePlayer(p, "death");
					}
					if (Arenas.isInArena(killer)) {
						a.removePlayer(killer, "death");
					}

				}
				if (a.getType(killer) == PlayerType.Murderer && a.getType(player) == PlayerType.Detective
						&& a.getType(p) != PlayerType.None && a.getType(killer) != PlayerType.None) {

					if ((plugin.knifeSkinManager != null && plugin.knifeSkinManager.isKnife(killer.getItemInHand())) ||
							(killer.getItemInHand() != null && killer.getItemInHand().getType() == Material
							.getMaterial(plugin.settings.getConfig().getInt("murderer-weapon.item-id")))) {
						if (Arenas.isInArena(p)) {
							a.removePlayer(p, "death");
						}
						a.addscore(killer, plugin.settings.getConfig().getInt("score-on-kill"),
								Utils.FormatText(killer, plugin.messages.getConfig().getString("kill-reason")));
					}

				}

				if (a.getType(killer) == PlayerType.Murderer && a.getType(player) == PlayerType.Innocents
						&& a.getType(p) != PlayerType.None && a.getType(killer) != PlayerType.None) {
					if ((plugin.knifeSkinManager != null && plugin.knifeSkinManager.isKnife(killer.getItemInHand())) ||
							(killer.getItemInHand() != null && killer.getItemInHand().getType() == Material
							.getMaterial(plugin.settings.getConfig().getInt("murderer-weapon.item-id")))) {
						if (Arenas.isInArena(p)) {
							a.removePlayer(p, "death");
						}
						a.addscore(killer, plugin.settings.getConfig().getInt("score-on-kill"),
								Utils.FormatText(killer, plugin.messages.getConfig().getString("kill-reason")));
					}

				}

				if (plugin.settings.getConfig().getBoolean("death-messages")) {
					for (Player x : a.getPlayers()) {
						x.sendMessage(Utils.FormatText(x, plugin.messages.getConfig().getString("death-message")
								.replaceAll("%player%", e.getEntity().getPlayer().getName())));
					}
				}

				if (Arenas.isInArena(p)) {
					a.removePlayer(p, "death");
				}
				return;
			}
			
		}

	}

}



