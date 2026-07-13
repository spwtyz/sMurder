package me.spwtyz.murder.objects;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import me.spwtyz.murder.Arena;
import me.spwtyz.murder.Arenas;
import me.spwtyz.murder.GameState;
import me.spwtyz.murder.Main;
import me.spwtyz.murder.PlayerType;

public class Teleporter implements Listener {

    private final Main plugin;
    private final Random random = new Random();

    public Teleporter(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.ENDER_PEARL) return;

        Arena arena = Arenas.getArena(player);
        if (arena == null || arena.getState() != GameState.INGAME) return;
        boolean kitTeleporter = isKitTeleporter(item);
        PlayerType type = arena.getType(player);
        if (type != PlayerType.Murderer && !(kitTeleporter && (type == PlayerType.Innocents || type == PlayerType.Detective))) return;
        // O plugin usa SURVIVAL em algumas arenas e ADVENTURE em outras.
        // Não bloquear por GameMode para o teleportador do kit funcionar em todos os modos.

        event.setCancelled(true);

        Location destination = getRandomArenaSpawn(arena);
        if (destination == null) {
            player.sendMessage("§cNenhum spawn válido foi encontrado para essa arena.");
            return;
        }

        player.teleport(destination);
        player.getWorld().playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        player.sendMessage("§6§lMURDER §7> §eVocê usou seu teleportador!");

        if (kitTeleporter) {
            if (item.getAmount() <= 1) {
                player.setItemInHand(null);
            } else {
                item.setAmount(item.getAmount() - 1);
                player.setItemInHand(item);
            }
            player.updateInventory();
        }
    }

    private boolean isKitTeleporter(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        return org.bukkit.ChatColor.stripColor(item.getItemMeta().getDisplayName()).equalsIgnoreCase("Teleportador do Kit");
    }

    private Location getRandomArenaSpawn(Arena arena) {
        int size = 0;
        try {
            size = plugin.SpawnSize2(arena);
        } catch (Exception ignored) {}

        if (size <= 0) {
            try {
                size = plugin.SpawnSize(arena);
            } catch (Exception ignored) {}
        }

        if (size <= 0) return null;

        for (int attempts = 0; attempts < 12; attempts++) {
            int index = random.nextInt(size);
            Location loc = plugin.getSpawn(arena, index);
            if (loc != null && loc.getWorld() != null) return loc;
        }

        Location fallback = plugin.getSpawn(arena, 0);
        return fallback != null && fallback.getWorld() != null ? fallback : null;
    }
}
