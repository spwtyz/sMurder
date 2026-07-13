package me.spwtyz.murder.objects;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.spwtyz.murder.Arena;
import me.spwtyz.murder.Arenas;
import me.spwtyz.murder.GameModeType;
import me.spwtyz.murder.Main;
import me.spwtyz.murder.PlayerType;
import me.spwtyz.murder.rooms.Room;
import me.spwtyz.murder.rooms.RoomModifier;

public class FragEvent implements Listener {

    private final Random random = new Random();

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        ItemStack pickupItem = event.getItem().getItemStack();
        Arena arena = Arenas.getArena(player);

        if (arena == null) {
            return;
        }

        if (pickupItem == null || pickupItem.getType() != Material.IRON_INGOT) {
            return;
        }

        if (arena.getState() != me.spwtyz.murder.GameState.INGAME) {
            event.setCancelled(true);
            return;
        }

        if (arena.getGameMode() == GameModeType.TNT_TAG || arena.getGameMode() == GameModeType.ALL_MURDER) {
            event.setCancelled(true);
            event.getItem().remove();
            return;
        }

        if (arena.specs.contains(player)) {
            event.setCancelled(true);
            return;
        }

        if (arena.getType(player) == PlayerType.Murderer) {
            event.setCancelled(true);
            event.getItem().remove();
            return;
        }

        event.setCancelled(true);
        event.getItem().remove();

        giveFragment(player);

        if (Main.getInstance() != null && Main.getInstance().levelManager != null) {
            Main.getInstance().levelManager.addXP(player, 2, "Fragmento");
        }

        // Cursed Gold usa a MESMA loc/item dos fragmentos normais.
        // Ao pegar IRON_INGOT, se o modificador da sala estiver ativado, rola a chance aqui.
        if (isCursedGoldEnabled(arena) && rollCursedChance()) {
            applyCursedGold(player);
        }
    }

    private void giveFragment(Player player) {
        ItemStack ironIngot = new ItemStack(Material.IRON_INGOT, 1);
        ItemMeta meta = ironIngot.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Fragmentos");
        ironIngot.setItemMeta(meta);

        ItemStack existingItem = player.getInventory().getItem(8);

        if (existingItem == null || existingItem.getType() == Material.AIR) {
            player.getInventory().setItem(8, ironIngot);
            return;
        }

        if (existingItem.getType() == Material.IRON_INGOT && existingItem.hasItemMeta()
                && existingItem.getItemMeta().hasDisplayName()
                && existingItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Fragmentos")
                && existingItem.getAmount() < 64) {
            existingItem.setAmount(existingItem.getAmount() + 1);
            player.getInventory().setItem(8, existingItem);
        }
    }

    private boolean isCursedGoldEnabled(Arena arena) {
        Main plugin = Main.getInstance();
        if (plugin == null || plugin.roomManager == null || arena == null) return false;

        Room room = plugin.roomManager.getRoomByArena(arena);
        return room != null && room.hasModifier(RoomModifier.CURSED_GOLD);
    }

    private boolean rollCursedChance() {
        int chance = 35;

        try {
            Main plugin = Main.getInstance();
            if (plugin != null && plugin.settings != null && plugin.settings.getConfig().contains("cursed-gold-chance")) {
                chance = plugin.settings.getConfig().getInt("cursed-gold-chance", 35);
            }
        } catch (Exception ignored) {}

        if (chance < 0) chance = 0;
        if (chance > 100) chance = 100;

        return random.nextInt(100) < chance;
    }

    private void applyCursedGold(Player player) {
        int curse = random.nextInt(4);

        player.sendMessage(ChatColor.GOLD + "§lCURSED GOLD! " + ChatColor.GRAY + "Você pegou um fragmento amaldiçoado!");
        player.getWorld().strikeLightningEffect(player.getLocation());
        player.playSound(player.getLocation(), Sound.WITHER_IDLE, 1f, 1f);

        if (curse == 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 5, 0));
            player.sendMessage(ChatColor.RED + "Maldição: cegueira por 5 segundos.");
        } else if (curse == 1) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 6, 1));
            player.sendMessage(ChatColor.RED + "Maldição: lentidão por 6 segundos.");
        } else if (curse == 2) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 20 * 8, 0));
            player.sendMessage(ChatColor.RED + "Maldição: você foi revelado por 8 segundos.");
        } else {
            player.getWorld().playEffect(player.getLocation(), Effect.MOBSPAWNER_FLAMES, 1);
            player.damage(2.0D);
            player.sendMessage(ChatColor.RED + "Maldição: o ouro drenou sua vida.");
        }
    }
}
