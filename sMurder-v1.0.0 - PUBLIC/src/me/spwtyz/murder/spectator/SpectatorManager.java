package me.spwtyz.murder.spectator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import me.spwtyz.murder.Arena;
import me.spwtyz.murder.Arenas;
import me.spwtyz.murder.Main;
import me.spwtyz.murder.PlayerState;
import me.spwtyz.murder.Utils;

public class SpectatorManager {

    private final Main plugin;

    public SpectatorManager(Main plugin) {
        this.plugin = plugin;
    }

    public void makeSpectator(Player p, Arena arena, String reason) {
        if (p == null || arena == null) return;

        arena.players.remove(p);
        if (!arena.specs.contains(p)) arena.specs.add(p);
        arena.clearplayer(p);
        plugin.setPlayerState(p, PlayerState.SPECTATOR);

        p.getInventory().clear();
        p.getInventory().setArmorContents(null);
        p.setFireTicks(0);
        p.setHealth(p.getMaxHealth());
        p.setGameMode(GameMode.ADVENTURE);
        p.setAllowFlight(true);
        p.setFlying(true);

        Location spec = plugin.getSpec(arena);
        if (spec != null) p.teleport(spec);

        giveSpectatorItems(p, arena);
        updateVisibility(arena);
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                updateVisibility(arena);
            }
        }, 5L);
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                updateVisibility(arena);
            }
        }, 20L);

        if (reason != null && reason.length() > 0) {
            p.sendMessage(Utils.FormatText(p, reason));
        } else {
            p.sendMessage("§7Você virou espectador.");
        }
    }

    public void restore(Player p) {
        if (p == null) return;
        p.setFlying(false);
        p.setAllowFlight(false);
        p.getInventory().clear();
        p.getInventory().setArmorContents(null);
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other == null || other.equals(p)) continue;
            p.showPlayer(other);
            other.showPlayer(p);
        }
    }

    public void giveSpectatorItems(Player p, Arena arena) {
        if (p == null) return;
        p.getInventory().clear();
        p.getInventory().setArmorContents(null);

        p.getInventory().setItem(0, item(Material.COMPASS, "§aTeletransportar", "§7Clique para escolher um jogador vivo."));
        p.getInventory().setItem(1, item(Material.WATCH, "§eJogador Aleatório", "§7Teleporta para um jogador vivo aleatório."));
        p.getInventory().setItem(4, item(Material.FEATHER, "§bFly Ativado", "§7Você pode voar automaticamente."));

        if (!plugin.getConfig().getBoolean("bungee") && plugin.settings.getConfig().getBoolean("rejoin-option")) {
            p.getInventory().setItem(7, item(Material.PAPER, "§6Jogar Novamente", "§7Clique para tentar voltar para a fila."));
        }

        Material leave = Material.BED;
        try {
            int id = plugin.settings.getConfig().getInt("quit.item-id");
            Material configured = Material.getMaterial(id);
            if (configured != null) leave = configured;
        } catch (Throwable ignored) {}
        p.getInventory().setItem(8, item(leave, Utils.FormatText(p, plugin.settings.getConfig().getString("quit.item-name")), "§7Sair da partida."));
        p.updateInventory();
    }

    private ItemStack item(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material == null ? Material.STONE : material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    public void openTeleportMenu(Player p) {
        if (p == null || !Arenas.isInArena(p)) return;
        Arena arena = Arenas.getArena(p);
        if (arena == null || !arena.specs.contains(p)) return;

        Inventory inv = Bukkit.createInventory(null, plugin.settings.getConfig().getInt("spectate-inventory-size"),
                Utils.FormatText2(plugin.settings.getConfig().getString("spectate-inventory-title")));

        for (Player alive : arena.getPlayers2()) {
            if (alive == null || !alive.isOnline()) continue;
            ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            meta.setOwner(alive.getName());
            meta.setDisplayName("§a" + alive.getName());
            meta.setLore(Arrays.asList("§7Clique para teleportar até este jogador."));
            item.setItemMeta(meta);
            inv.addItem(item);
        }
        p.openInventory(inv);
    }

    public void teleportRandom(Player p) {
        if (p == null || !Arenas.isInArena(p)) return;
        Arena arena = Arenas.getArena(p);
        if (arena == null || !arena.specs.contains(p)) return;
        List<Player> alive = new ArrayList<Player>();
        for (Player target : arena.getPlayers2()) {
            if (target != null && target.isOnline()) alive.add(target);
        }
        if (alive.isEmpty()) {
            p.sendMessage("§cNão tem jogadores vivos para espectar.");
            return;
        }
        Player target = alive.get((int) (Math.random() * alive.size()));
        p.teleport(target.getLocation());
        p.sendMessage("§aTeleportado para §f" + target.getName() + "§a.");
    }

    public void updateVisibility(Arena arena) {
        if (arena == null) return;

        List<Player> inside = new ArrayList<Player>();
        for (Player p : arena.players) if (p != null && p.isOnline() && !inside.contains(p)) inside.add(p);
        for (Player p : arena.specs) if (p != null && p.isOnline() && !inside.contains(p)) inside.add(p);

        for (Player viewer : inside) {
            if (viewer == null || !viewer.isOnline()) continue;
            boolean viewerSpec = arena.specs.contains(viewer);

            for (Player target : Bukkit.getOnlinePlayers()) {
                if (target == null || !target.isOnline() || target.equals(viewer)) continue;
                Arena targetArena = Arenas.getArena(target);

                if (targetArena != arena) {
                    viewer.hidePlayer(target);
                    continue;
                }

                boolean targetSpec = arena.specs.contains(target);

                if (viewerSpec) {
                    // Espectador deve ver jogadores vivos da partida, mas não outros espectadores.
                    if (targetSpec || !arena.players.contains(target)) viewer.hidePlayer(target);
                    else viewer.showPlayer(target);
                } else {
                    // Player vivo vê vivos, mas não vê espectadores.
                    if (targetSpec) viewer.hidePlayer(target);
                    else viewer.showPlayer(target);
                }
            }
        }

        for (Player outside : Bukkit.getOnlinePlayers()) {
            if (outside == null || !outside.isOnline()) continue;
            Arena outsideArena = Arenas.getArena(outside);
            if (outsideArena == arena) continue;
            for (Player target : inside) {
                if (target == null || !target.isOnline() || target.equals(outside)) continue;
                outside.hidePlayer(target);
            }
        }
    }
}
