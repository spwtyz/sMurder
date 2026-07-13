package me.spwtyz.murder.titles;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import me.spwtyz.murder.Main;

public class TitleManager implements Listener {

    private final Main plugin;
    private final Map<UUID, ArmorStand> stands = new HashMap<UUID, ArmorStand>();
    private BukkitRunnable task;

    public TitleManager(Main plugin) {
        this.plugin = plugin;
    }

    public void start() {
        // Sistema de títulos desativado temporariamente.
        // Mantemos apenas a limpeza de ArmorStands antigos para não deixar lixo nos mapas.
        cleanupStrayTitleStands();
        stop();
    }

    public void stop() {
        if (task != null) task.cancel();
        task = null;
        for (ArmorStand stand : stands.values()) {
            if (stand != null && !stand.isDead()) stand.remove();
        }
        stands.clear();
    }

    public void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            update(player);
        }

        Iterator<Map.Entry<UUID, ArmorStand>> it = stands.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, ArmorStand> entry = it.next();
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline() || getTitle(player).trim().isEmpty()) {
                ArmorStand stand = entry.getValue();
                if (stand != null && !stand.isDead()) stand.remove();
                it.remove();
            }
        }
    }

    public void update(Player player) {
        // Desativado por enquanto: não cria nem atualiza ArmorStand de título.
        remove(player);
    }

    private ArmorStand spawnStand(Player player, String title) {
        ArmorStand stand = (ArmorStand) player.getWorld().spawnEntity(getTitleLocation(player), EntityType.ARMOR_STAND);
        stand.setCustomName(title);
        stand.setCustomNameVisible(true);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setSmall(true);
        stand.setCanPickupItems(false);
        stand.setMetadata("windmc_player_title", new FixedMetadataValue(plugin, player.getUniqueId().toString()));
        try {
            stand.setHelmet(new ItemStack(Material.AIR));
        } catch (Throwable ignored) {}
        return stand;
    }


    /**
     * Remove ArmorStands de títulos antigos que ficaram presos no mapa após reload/restart.
     * Os novos títulos recebem metadata, mas também removemos nomes conhecidos de versões antigas
     * para limpar mapas que já ficaram com stands travados.
     */
    public void cleanupStrayTitleStands() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof ArmorStand)) continue;
                ArmorStand stand = (ArmorStand) entity;

                if (stand.hasMetadata("windmc_player_title")) {
                    stand.remove();
                    continue;
                }

                String name = stand.getCustomName();
                if (name == null) continue;

                String stripped = ChatColor.stripColor(name).toLowerCase();
                if (stripped.contains("the shadow")
                        || stripped.contains("blood reaper")
                        || stripped.contains("nightmare")
                        || stripped.contains("frozen king")
                        || stripped.contains("storm bringer")
                        || stripped.contains("toxic")
                        || stripped.contains("serial killer")
                        || stripped.contains("lenda do murder")
                        || stripped.contains("divine")
                        || stripped.contains("void walker")) {
                    stand.remove();
                }
            }
        }
    }

    private Location getTitleLocation(Player player) {
        Location loc = player.getLocation().clone();
        // Altura ajustada: antes ficava muito alto acima da nametag.
        loc.setY(loc.getY() + 1.92D);
        return loc;
    }

    public String getTitle(Player player) {
        if (player == null || plugin.data == null) return "";
        String id = plugin.data.getConfig().getString("Cosmetics." + player.getUniqueId() + ".title.selected", "nenhum");
        if (id == null || id.equalsIgnoreCase("nenhum")) return "";
        String raw = plugin.getConfig().getString("shop-titles." + id + ".display", null);
        if (raw == null) raw = plugin.data.getConfig().getString("Cosmetics." + player.getUniqueId() + ".title.display", "");
        if (raw == null || raw.trim().isEmpty()) raw = getDefaultTitleDisplay(id);
        return ChatColor.translateAlternateColorCodes('&', raw == null ? "" : raw);
    }

    private String getDefaultTitleDisplay(String id) {
        if (id == null) return "";
        if (id.equalsIgnoreCase("title_shadow")) return "&8✦ The Shadow";
        if (id.equalsIgnoreCase("title_blood_reaper")) return "&c☠ Blood Reaper";
        if (id.equalsIgnoreCase("title_nightmare")) return "&5☽ Nightmare";
        if (id.equalsIgnoreCase("title_frozen_king")) return "&b❄ Frozen King";
        if (id.equalsIgnoreCase("title_storm")) return "&e⚡ Storm Bringer";
        if (id.equalsIgnoreCase("title_toxic")) return "&a☣ Toxic";
        if (id.equalsIgnoreCase("title_serial")) return "&cSerial Killer";
        if (id.equalsIgnoreCase("title_legend")) return "&6✪ Lenda do Murder";
        if (id.equalsIgnoreCase("title_divine")) return "&d✧ Divine";
        if (id.equalsIgnoreCase("title_void")) return "&0✹ Void Walker";
        return "";
    }

    public void setTitle(Player player, String id, String display) {
        if (player == null || plugin.data == null) return;
        plugin.data.getConfig().set("Cosmetics." + player.getUniqueId() + ".title.selected", id == null ? "nenhum" : id);
        plugin.data.getConfig().set("Cosmetics." + player.getUniqueId() + ".title.display", display == null ? "" : display);
        plugin.data.save();
        remove(player);
    }

    public void clearTitle(Player player) {
        if (player == null || plugin.data == null) return;
        plugin.data.getConfig().set("Cosmetics." + player.getUniqueId() + ".title.selected", "nenhum");
        plugin.data.getConfig().set("Cosmetics." + player.getUniqueId() + ".title.display", null);
        plugin.data.save();
        remove(player);
    }

    public void remove(Player player) {
        if (player == null) return;
        ArmorStand stand = stands.remove(player.getUniqueId());
        if (stand != null && !stand.isDead()) stand.remove();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        remove(event.getPlayer());
    }
}
