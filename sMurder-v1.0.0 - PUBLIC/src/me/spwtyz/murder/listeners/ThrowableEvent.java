package me.spwtyz.murder.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import me.spwtyz.murder.Arena;
import me.spwtyz.murder.Arenas;
import me.spwtyz.murder.GameState;
import me.spwtyz.murder.Main;
import me.spwtyz.murder.PlayerType;
import me.spwtyz.murder.Utils;

public class ThrowableEvent implements Listener {

    Main plugin;

    public ThrowableEvent(Main plugin) {
        this.plugin = plugin;
    }

    public void removesword(Arena a, ArmorStand s) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (a.sword.contains(s) && !s.isDead() && s != null) {
                    a.sword.remove(s);
                    s.remove();
                }
            }
        }.runTaskLater(plugin, 20 * plugin.getConfig().getInt("remove-sword-after-time"));
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void Sword(PlayerInteractEvent e) {
        // Sistema antigo de arremesso por ArmorStand.
        // Desativado quando o KnifeSkinManager existe, porque ele ignorava o cooldown
        // do AMONG US e podia matar via p.damage(1000) durante recarga.
        // O arremesso atual fica centralizado no ThrowerEvent + KnifeManager.
        if (plugin.knifeSkinManager != null) {
            return;
        }
        if (e.getPlayer().getItemInHand() != null) {
            final Player p = e.getPlayer();

            if (e.getAction().name().toLowerCase().contains("right")) {
                if (p.getItemInHand().getType() == Material.AIR) {
                    return;
                }

                ItemStack is;
                if ((is = p.getItemInHand()).hasItemMeta()) {
                    ItemMeta im;
                    if ((im = is.getItemMeta()).hasDisplayName()) {
                        String name;
                        if ((name = im.getDisplayName()) != null) {

                            if (name.equalsIgnoreCase(Utils.FormatText(p,
                                    plugin.settings.getConfig().getString("murderer-weapon.item-name")))) {

                                if (Arenas.isInArena(e.getPlayer())) {
                                    Arena a = Arenas.getArena(e.getPlayer());

                                    if (a.specs.contains(e.getPlayer())) {
                                        return;
                                    }

                                    if (a.getState() != GameState.INGAME
                                            || !plugin.settings.getConfig().getBoolean("enable-sword-throw")) {
                                        return;
                                    }

                                    if (!plugin.cooldownTime.containsKey(p.getName())) {

                                        plugin.cooldownTime.put(p.getName(),
                                                plugin.settings.getConfig().getInt("throw-sword-cooldown"));

                                        int progress = 0;

                                        if (plugin.cooldownTime.containsKey(p.getName())) {
                                            progress = plugin.cooldownTime.get(p.getName());
                                        }
                                        StringBuilder veryStringWow = new StringBuilder();

                                        for (int i = 0; i < plugin.settings.getConfig()
                                                .getInt("throw-sword-cooldown"); i++) {
                                            if (i < progress) {
                                                veryStringWow.append(Utils.FormatText(p,
                                                        plugin.messages.getConfig().getString("progress-bar-1")));
                                            } else {
                                                veryStringWow.append(Utils.FormatText(p,
                                                        plugin.messages.getConfig().getString("progress-bar-2")));
                                            }
                                        }

                                        String sx = veryStringWow.toString();

                                        plugin.api.sendActionBar(p,
                                                Utils.FormatText(p,
                                                        plugin.messages.getConfig()
                                                                .getString("sword-actionbar-cooldown")
                                                                .replaceAll("%progress%", sx)));

                                        plugin.cooldownTask.put(p.getName(), new BukkitRunnable() {
                                            @Override
                                            public void run() {
                                                
                                                if (!plugin.cooldownTask.containsKey(p.getName())
                                                        || !plugin.cooldownTime.containsKey(p.getName())) {
                                                    cancel();
                                                }
                                                if (plugin.cooldownTime.containsKey(p.getName())) {
                                                    plugin.cooldownTime.put(p.getName(),
                                                            plugin.cooldownTime.get(p.getName()) - 1);
                                                }
                                                if (plugin.cooldownTime.containsKey(p.getName())) {
                                                    if (plugin.cooldownTime.get(p.getName()) <= 0) {
                                                        if (plugin.cooldownTask.containsKey(p.getName())) {
                                                            plugin.cooldownTask.remove(p.getName());
                                                        }
                                                        if (plugin.cooldownTime.containsKey(p.getName())) {
                                                            plugin.cooldownTime.remove(p.getName());
                                                        }
                                                        cancel();
                                                    }
                                                }
                                            }
                                        });

                                        if (plugin.cooldownTask.containsKey(p.getName())) {
                                            plugin.cooldownTask.get(p.getName()).runTaskTimer(plugin, 20, 20);
                                        }

                                        new BukkitRunnable() {
                                            @Override
                                            public void run() {
                                                if (!plugin.cooldownTask.containsKey(p.getName())
                                                        || !plugin.cooldownTime.containsKey(p.getName())) {
                                                    cancel();
                                                }
                                                if (!p.isOnline() && plugin.cooldownTime.containsKey(p.getName())
                                                        && plugin.cooldownTask.containsKey(p.getName())) {
                                                    if (plugin.cooldownTask.containsKey(p.getName())) {
                                                        plugin.cooldownTask.remove(p.getName());
                                                    }
                                                    if (plugin.cooldownTime.containsKey(p.getName())) {
                                                        plugin.cooldownTime.remove(p.getName());
                                                    }
                                                    cancel();
                                                }

                                                if (plugin.cooldownTime.containsKey(p.getName())) {
                                                    int progress = plugin.cooldownTime.get(p.getName());
                                                    StringBuilder veryStringWow = new StringBuilder();

                                                    for (int i = 0; i < plugin.settings.getConfig()
                                                            .getInt("throw-sword-cooldown"); i++) {
                                                        if (i < progress - 1) {
                                                            veryStringWow.append(Utils.FormatText(p, plugin.messages
                                                                    .getConfig().getString("progress-bar-1")));
                                                        } else {
                                                            veryStringWow.append(Utils.FormatText(p, plugin.messages
                                                                    .getConfig().getString("progress-bar-2")));
                                                        }
                                                    }
                                                    String s = veryStringWow.toString();

                                                    plugin.api.sendActionBar(p,
                                                            Utils.FormatText(p,
                                                                    plugin.messages.getConfig()
                                                                            .getString("sword-actionbar-cooldown")
                                                                            .replaceAll("%progress%", s)));
                                                } else {
                                                    this.cancel();
                                                    plugin.api.sendActionBar(p, Utils.FormatText(p,
                                                            plugin.messages.getConfig().getString("sword-can-use-again")));

                                                    
                                                    //if (Arenas.isInArena(p)) {
                                                    //p.getWorld().strikeLightningEffect(p.getLocation());

                
                                                    //if (e.getPlayer().getInventory().getItem(0) != null) {
                                                        
                                                      //  e.getPlayer().getInventory().setItem(0, null);
                                                    //}
                                                    
                                                    if (Arenas.isInArena(p)) {
                                                        p.getWorld().strikeLightningEffect(p.getLocation());

                                                        
                                                        if (e.getPlayer().getInventory().getItem(0) != null) {
                                                           
                                                            e.getPlayer().getInventory().setItem(0, null);
                                                        }
                                                        
                                                    } else {
                                                    	
                                                        return;
                                                    }                                                                                         
                                                           
                                                	

                                                    
                                                    ItemStack DiamondSword = new ItemStack(Material.DIAMOND_SWORD);
                                                    ItemMeta meta2 = DiamondSword.getItemMeta();
                                                    meta2.setDisplayName("§6§lMURDER> §bFaca §7(§a1§7)");
                                                    DiamondSword.setItemMeta(meta2);
                                                   
                                                    e.getPlayer().getInventory().setItem(0, DiamondSword);
                                                
                                            
                                            
                                            if (a.getState() == GameState.INGAME) {
                                            	
                                            } else {
                                                
                                                ItemStack Zero = new ItemStack(Material.AIR);
                                                e.getPlayer().getInventory().setItem(0, Zero);
                                            }
                                          }
                                        }
                               
                                        }.runTaskTimer(plugin, 10, 10);
                                        Location loc = p.getLocation();
                                        p.getWorld().playSound(p.getLocation(), Sound.WITHER_SHOOT, 1.0F, 1.0F);

                                        
                                        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                                            @Override
                                            public void run() {
                                                
                                                ItemStack WaitSword = new ItemStack(Material.WOOD_SWORD);
                                                ItemMeta meta2 = WaitSword.getItemMeta();
                                                meta2.setDisplayName("§cAguarde §e" + plugin.settings.getConfig().getInt("throw-sword-cooldown") + "s");
                                                WaitSword.setItemMeta(meta2);
                                                
                                                e.getPlayer().getInventory().setItem(0, WaitSword);
                                            }
                                        }, 5);
                                               

                                        ArmorStand s = e.getPlayer().getWorld()
                                                .spawn(loc.add(plugin.getLeftHeadDirection(p).multiply(1)),
                                                        ArmorStand.class);
                                        removesword(a, s);
                                        s.setArms(true);
                                        s.setBasePlate(false);
                                        s.setVisible(false);
                                        //s.setSmall(true);
                                        s.setGravity(false);

                                        s.setRightArmPose(new EulerAngle(
                                                Math.toRadians(plugin.settings.getConfig()
                                                        .getInt("throwing-sword-angle-rotation")),
                                                Math.toRadians(-p.getLocation().getPitch()),
                                                Math.toRadians(90.0D)));

                                        s.setItemInHand(e.getPlayer().getItemInHand());
                                        a.sword.add(s);

                                        Location pl = s.getLocation();
                                        Location pl2 = s.getLocation()
                                                .add(plugin.getRightHeadDirection(s).multiply(1));
                                        Location pl1 = s.getLocation().add(0.5,
                                                plugin.settings.getConfig().getInt("murder-sword-remove-height"),
                                                0.5);
                                        double px = pl.getX();
                                        double py = pl.getY();
                                        double pz = pl.getZ();

                                        double yaw = Math.toRadians(pl.getYaw() + 90.0F);
                                        double pitch = Math.toRadians(pl.getPitch() + 90.0F);

                                        double x = Math.sin(pitch) * Math.cos(yaw);
                                        double y = Math.sin(pitch) * Math.sin(yaw);
                                        double z = Math.cos(pitch);

                                        double px1 = pl1.getX();
                                        double py1 = pl1.getY();
                                        double pz1 = pl1.getZ();

                                        double yaw1 = Math.toRadians(pl1.getYaw() + 90.0F);
                                        double pitch1 = Math.toRadians(pl1.getPitch() + 90.0F);

                                        double x1 = Math.sin(pitch1) * Math.cos(yaw1);
                                        double y1 = Math.sin(pitch1) * Math.sin(yaw1);
                                        double z1 = Math.cos(pitch1);

                                        double px2 = pl2.getX();
                                        double py2 = pl2.getY();
                                        double pz2 = pl2.getZ();

                                        double yaw2 = Math.toRadians(pl2.getYaw() + 90.0F);
                                        double pitch2 = Math.toRadians(pl2.getPitch() + 90.0F);

                                        double x2 = Math.sin(pitch2) * Math.cos(yaw2);
                                        double y2 = Math.sin(pitch2) * Math.sin(yaw2);
                                        double z2 = Math.cos(pitch2);
                                        plugin.Yaw.put(p.getName(), p.getLocation().getYaw());
                                        plugin.Pitch.put(p.getName(), p.getLocation().getPitch());

                                        new BukkitRunnable() {
                                            int a = 0;

                                            @Override
                                            public void run() {
                                                Location loc = new Location(p.getWorld(), px + a * x, py + a * z,
                                                        pz + a * y);
                                                Location loc1 = new Location(p.getWorld(), px1 + a * x1,
                                                        py1 + a * z1, pz1 + a * y1);

                                                Location loc2 = new Location(p.getWorld(), px2 + a * x2,
                                                        py2 + a * z2, pz2 + a * y2);
                                                Arena a = Arenas.getArena(p);

                                                if (s.isDead() || s == null) {
                                                    this.cancel();
                                                    return;
                                                }

                                                if (!Arenas.isInArena(p) || s.isDead() || s == null
                                                        || !a.sword.contains(s) || a.getState() != GameState.INGAME
                                                        || !s.getWorld().getName().equalsIgnoreCase(
                                                                plugin.getSpawn(a, 0).getWorld().getName())) {
                                                    this.cancel();
                                                    return;
                                                }

                                                loc.setYaw(plugin.Yaw.get(p.getName()));
                                                loc.setPitch(plugin.Pitch.get(p.getName()));

                                                Block x = loc1.getBlock();
                                                if (!plugin.passable.contains(x.getType())) {
                                                    if (a.sword.contains(s) && s != null && !s.isDead()) {
                                                        s.remove();
                                                    }
                                                    this.cancel();
                                                    return;
                                                }

                                                for (Entity entity : plugin.getNearbyEntities(loc2,
                                                        (int) plugin.settings.getConfig()
                                                                .getDouble("throw-sword-damage-radius"))) {
                                                    if (entity instanceof LivingEntity
                                                            && !(entity instanceof ArmorStand)) {
                                                        if (entity instanceof Player) {
                                                            Player p = (Player) entity;
                                                            if (p != e.getPlayer()) {
                                                                if (Arenas.isInArena(p)) {
                                                                    if (Arenas.getArena(p)
                                                                            .getType(p) != PlayerType.Murderer
                                                                            && !Arenas.getArena(p).specs
                                                                                    .contains(p)) {
                                                                        if (plugin.hatAbilityManager != null && plugin.hatAbilityManager.tryPreventLethalHit(p, e.getPlayer(), a, "throw_knife")) {
                                                                            if (a.sword.contains(s) && s != null && !s.isDead()) {
                                                                                s.remove();
                                                                            }
                                                                            this.cancel();
                                                                            return;
                                                                        }
                                                                        p.damage(1000);
                                                                        if (a.sword.contains(s) && s != null
                                                                                && !s.isDead()) {
                                                                            s.remove();
                                                                        }
                                                                        this.cancel();
                                                                        return;
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                s.teleport(loc);
                                                this.a += plugin.settings.getConfig().getInt("sword-throw-speed");
                                            }
                                        }.runTaskTimer(plugin, 2, 2);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}