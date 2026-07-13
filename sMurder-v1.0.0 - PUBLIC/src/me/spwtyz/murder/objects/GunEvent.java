package me.spwtyz.murder.objects;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import me.spwtyz.murder.Main;
import me.spwtyz.murder.guns.GunSkinManager;

public class GunEvent implements Listener {

    private final Map<UUID, Long> cooldowns = new HashMap<UUID, Long>();
    private final Main plugin;

    public GunEvent(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack hand = p.getItemInHand();
        if (p == null || hand == null) return;

        boolean isGun = plugin.gunSkinManager != null ? plugin.gunSkinManager.isGun(hand) : hand.getType() == Material.DIAMOND_HOE;
        if (!isGun) return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        e.setCancelled(true);
        if (hasCooldown(p)) return;

        GunSkinManager.GunSkin skin = plugin.gunSkinManager != null ? plugin.gunSkinManager.getSkinFromItem(hand) : GunSkinManager.GunSkin.FIVE_SEVEN;
        double velocity = skin.getVelocity();
        int cooldown = skin.getCooldown();

        p.getWorld().playSound(p.getLocation(), Sound.ENDERDRAGON_HIT, 1.0F, p.isSneaking() ? 1.15F : 1.0F);

        // Shift melhora somente a precisao. A velocidade/alcance permanece igual.
        double spread = p.isSneaking() ? 0.003D : 0.018D;
        Vector direction = p.getLocation().getDirection().normalize();
        direction.add(new Vector(
                randomSpread(spread),
                randomSpread(spread),
                randomSpread(spread)
        )).normalize();

        Snowball snowball = p.launchProjectile(Snowball.class);
        snowball.setVelocity(direction.multiply(velocity));
        snowball.setMetadata("smurder_gun", new FixedMetadataValue(plugin, p.getUniqueId().toString()));
        setCooldown(p, cooldown);
    }

    private double randomSpread(double amount) {
        return (Math.random() - 0.5D) * 2.0D * amount;
    }

    private boolean hasCooldown(Player player) {
        Long until = cooldowns.get(player.getUniqueId());
        if (until == null) return false;
        if (System.currentTimeMillis() >= until) {
            cooldowns.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    private void setCooldown(Player player, int seconds) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + (seconds * 1000L));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (e.getPlayer() != null) cooldowns.remove(e.getPlayer().getUniqueId());
    }
}
