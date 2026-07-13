package me.spwtyz.murder.objects;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.spwtyz.murder.Arena;
import me.spwtyz.murder.Arenas;
import me.spwtyz.murder.GameState;
import me.spwtyz.murder.Main;
import me.spwtyz.murder.PlayerType;

public class SpeedEvent implements Listener {

    private final Main plugin;
    private final HashMap<UUID, Long> cooldowns = new HashMap<UUID, Long>();

    public SpeedEvent(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack hand = player.getItemInHand();
        if (hand == null || hand.getType() != Material.FEATHER) return;
        if (!hand.hasItemMeta() || !hand.getItemMeta().hasDisplayName()) return;
        if (!ChatColor.stripColor(hand.getItemMeta().getDisplayName()).toLowerCase().contains("velocidade")) return;

        Arena arena = Arenas.getArena(player);
        if (arena == null || arena.getState() != GameState.INGAME) return;
        if (arena.getType(player) != PlayerType.Murderer) return;
        // Funciona em SURVIVAL e ADVENTURE. Depois do rework de espectador/roles,
        // os assassinos entram em SURVIVAL, então bloquear por GameMode fazia o item parar.
        if (player.getGameMode() == GameMode.SPECTATOR || player.getGameMode() == GameMode.CREATIVE) return;

        event.setCancelled(true);

        long now = System.currentTimeMillis();
        long last = cooldowns.containsKey(player.getUniqueId()) ? cooldowns.get(player.getUniqueId()) : 0L;
        long cooldownMillis = 30000L;

        if (last > 0L && now - last < cooldownMillis) {
            long timeLeft = (cooldownMillis - (now - last) + 999L) / 1000L;
            sendActionBar(player, ChatColor.RED + "Aguarde " + timeLeft + " segundos!");
            return;
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 5 * 20, 1), true);

        ItemMeta im = hand.getItemMeta();
        im.setDisplayName(ChatColor.YELLOW + "Velocidade (Clique)");
        hand.setItemMeta(im);

        player.getWorld().playSound(player.getLocation(), Sound.GLASS, 1.0F, 1.0F);
        player.sendMessage("§6§lMURDER §7> §eVocê usou sua Velocidade!");

        cooldowns.put(player.getUniqueId(), now);
        new BukkitRunnable() {
            @Override
            public void run() {
                cooldowns.remove(player.getUniqueId());
            }
        }.runTaskLater(plugin, 600L);
    }

    private void sendActionBar(Player player, String message) {
        try {
            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + getServerVersion() + ".entity.CraftPlayer");
            Object craftPlayer = craftPlayerClass.cast(player);
            Class<?> packetClass = Class.forName("net.minecraft.server." + getServerVersion() + ".PacketPlayOutChat");
            Class<?> chatComponentTextClass = Class.forName("net.minecraft.server." + getServerVersion() + ".ChatComponentText");
            Class<?> iChatBaseComponentClass = Class.forName("net.minecraft.server." + getServerVersion() + ".IChatBaseComponent");
            Constructor<?> chatComponentTextConstructor = chatComponentTextClass.getConstructor(new Class<?>[]{String.class});
            Object chatComponentText = chatComponentTextConstructor.newInstance(message);
            Object packet = packetClass.getConstructor(new Class<?>[]{iChatBaseComponentClass, byte.class}).newInstance(chatComponentText, (byte) 2);
            Method craftPlayerHandleMethod = craftPlayerClass.getMethod("getHandle");
            Object craftPlayerHandle = craftPlayerHandleMethod.invoke(craftPlayer);
            Field playerConnectionField = craftPlayerHandle.getClass().getField("playerConnection");
            Object playerConnection = playerConnectionField.get(craftPlayerHandle);
            Method sendPacketMethod = playerConnection.getClass().getMethod("sendPacket", Class.forName("net.minecraft.server." + getServerVersion() + ".Packet"));
            sendPacketMethod.invoke(playerConnection, packet);
        } catch (Exception e) {
            player.sendMessage(message);
        }
    }

    private String getServerVersion() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }
}
