package me.spwtyz.murder.listeners;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.metadata.FixedMetadataValue;
import org.golde.bukkit.corpsereborn.CorpseAPI.CorpseAPI;
import org.golde.bukkit.corpsereborn.nms.Corpses.CorpseData;
import org.bukkit.util.Vector;
import me.spwtyz.murder.GameState;

import me.spwtyz.murder.Arena;
import me.spwtyz.murder.Arenas;
import me.spwtyz.murder.Main;
import me.spwtyz.murder.PlayerType;
import me.spwtyz.murder.PlayerState;
import me.spwtyz.murder.knife.KnifeManager;
import me.spwtyz.murder.cosmetics.CosmeticEffectManager;

public class ThrowerEvent implements Listener {

    private Main plugin;

    public ThrowerEvent(Main plugin) {
        this.plugin = plugin;
        startKnifeTask();
    }
    
    
    @EventHandler
    public void onSwordHit(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player attacker = (Player) event.getDamager();

        ItemStack item = attacker.getItemInHand();

        if (item == null) {
            return;
        }

        // Bloqueia qualquer hit usando a faca inativa/de espera.
        // Antes bloqueava apenas WOOD_SWORD; se a skin/config usasse outro material,
        // ainda podia matar durante o cooldown.
        if (isWaitingKnife(item)) {
            event.setCancelled(true);
            event.setDamage(0.0D);
        }
    }

    private boolean isWaitingKnife(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta.hasDisplayName() && meta.getDisplayName().startsWith("§cAguarde")) return true;
        if (meta.hasLore()) {
            for (String line : meta.getLore()) {
                if (line != null && (line.contains("wait-knife") || line.contains("amongus-wait-knife"))) return true;
            }
        }
        return false;
    }

    private void setWaitingKnife(final Player player, final long totalTicks, final String label) {
        if (player == null || !player.isOnline()) return;
        final long started = System.currentTimeMillis();
        final long durationMs = Math.max(20L, totalTicks) * 50L;
        updateWaitingKnife(player, Math.max(1L, (durationMs + 999L) / 1000L), label);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player == null || !player.isOnline()) { cancel(); return; }
                Arena current = Arenas.getArena(player);
                if (current == null || current.getState() != GameState.INGAME) { cancel(); return; }
                ItemStack hand = player.getInventory().getItem(0);
                if (!isWaitingKnife(hand)) { cancel(); return; }
                long leftMs = durationMs - (System.currentTimeMillis() - started);
                if (leftMs <= 0L) { cancel(); return; }
                updateWaitingKnife(player, Math.max(1L, (leftMs + 999L) / 1000L), label);
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void updateWaitingKnife(Player player, long seconds, String label) {
        ItemStack waitSword = new ItemStack(Material.WOOD_SWORD);
        ItemMeta meta = waitSword.getItemMeta();
        meta.setDisplayName("§cAguarde §e" + seconds + "s");
        java.util.List<String> lore = new java.util.ArrayList<String>();
        lore.add("§7" + (label == null ? "Cooldown da faca" : label) + ": §c" + seconds + "s");
        lore.add("§8wait-knife");
        meta.setLore(lore);
        waitSword.setItemMeta(meta);
        player.getInventory().setItem(0, waitSword);
        player.updateInventory();
    }

    // ==================================================
    // 🔥 DAR FACA
    // ==================================================
    public void giveKnife(Player p) {

        ItemStack faca = plugin.knifeSkinManager != null
                ? plugin.knifeSkinManager.createKnife(p)
                : new ItemStack(Material.IRON_SWORD);

        p.getInventory().setItem(0, faca);
    }

    // ==================================================
    // 🔥 ARREMESSAR FACA
    // ==================================================
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();

        if (!Arenas.isInArena(player)) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (player.getItemInHand() == null) {
            return;
        }

        if (plugin.knifeSkinManager == null || !plugin.knifeSkinManager.isKnife(player.getItemInHand())) {
            return;
        }

        // Cancela a ação vanilla do item antes de trocar para a espada de cooldown.
        // Isso é importante para facas custom que são itens usáveis/comestíveis na 1.8
        // como RAW_FISH, CARROT e GOLDEN_CARROT. Sem isso o Minecraft pode sobrescrever
        // a troca do slot 0 e a faca não vira "Aguarde" no Murder normal/Todos Assassinos.
        event.setCancelled(true);

        Arena arena = Arenas.getArena(player);

        if (arena == null) {
            return;
        }

        KnifeManager km = arena.getKnifeManager();

        // AMONG US: a faca do Murder NÃO pode burlar o cooldown do modo.
        // Isso bloqueia arremesso durante cooldown e desativa o bypass do /m knifecooldown
        // para impedir o jogador de arremessar e ainda matar no hit com a faca na mão.
        boolean sabotageMode = arena.getGameMode() == me.spwtyz.murder.GameModeType.SABOTAGE;
        if (sabotageMode && plugin.sabotageTaskManager != null && arena.getType(player) == PlayerType.Murderer
                && !plugin.sabotageTaskManager.canMurderKill(player, arena)) {
            event.setCancelled(true);
            player.sendMessage("§cAguarde " + plugin.sabotageTaskManager.getMurderKillCooldownLeft(player)
                    + "s para matar novamente no AMONG US.");
            return;
        }

        boolean noCooldown = plugin.hasKnifeNoCooldown(player) && !sabotageMode;

        // cooldown normal. No AMONG US o no-cooldown nunca bypassa a faca em espera.
        if (!noCooldown) {
            if (km.isCooldown(player)) {
                return;
            }

            km.addCooldown(player);

            // remove faca da mão e mostra cooldown com segundos no nome.
            long cooldownTicks = plugin.knifeSkinManager != null ? plugin.knifeSkinManager.getCooldownTicks(player) : 200L;
            setWaitingKnife(player, cooldownTicks, sabotageMode ? "Cooldown do AMONG US" : "Cooldown da faca");
        }

        // spawn faca
        Item knife = player.getWorld().dropItem(
                player.getEyeLocation(),
                plugin.knifeSkinManager != null ? plugin.knifeSkinManager.createKnife(player) : new ItemStack(Material.IRON_SWORD)
        );

        knife.setPickupDelay(Integer.MAX_VALUE);
        knife.setMetadata("smurder_thrown_knife", new FixedMetadataValue(plugin, true));

        // AMONG US: usar a faca arremessada conta como tentativa de kill.
        // O cooldown começa no arremesso, bloqueando o hit direto enquanto a faca voa.
        // Esta metadata permite que ESTA faca ainda mate se acertar o alvo.
        if (sabotageMode && plugin.sabotageTaskManager != null && arena.getType(player) == PlayerType.Murderer) {
            knife.setMetadata("smurder_sabotage_throw_allowed", new FixedMetadataValue(plugin, true));
            plugin.sabotageTaskManager.applyMurderKillCooldown(player, arena);
        }

        Vector direction = player.getLocation().getDirection().clone();
        if (plugin.knifeSkinManager != null) {
            double spread = plugin.knifeSkinManager.getSpread(player);
            if (spread > 0D) {
                direction.add(new Vector(
                        plugin.knifeSkinManager.randomSpread() * spread,
                        plugin.knifeSkinManager.randomSpread() * spread,
                        plugin.knifeSkinManager.randomSpread() * spread
                ));
            }
            knife.setVelocity(direction.normalize().multiply(plugin.knifeSkinManager.getVelocityMultiplier(player)));
        } else {
            knife.setVelocity(direction.multiply(1.9));
        }

     // adiciona knife manager
        KnifeManager.Knife k = km.addKnife(knife, player);

        if (plugin.replayManager != null) {
            if (plugin.replayManager != null) plugin.replayManager.recordKnifeThrow(player, knife, arena);
        }

        // som
        player.playSound(
                player.getLocation(),
                Sound.WITHER_SHOOT,
                1f,
                1f
        );

        // Facas sem cooldown não dependem do cooldown para limpar.
        if (noCooldown) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (knife != null && !knife.isDead()) {
                        knife.remove();
                    }
                    km.removeKnife(knife);
                }
            }.runTaskLater(plugin, Math.max(40L, plugin.knifeSkinManager != null ? plugin.knifeSkinManager.getCooldownTicks(player) : 120L));
            return;
        }

     // cooldown reset
        new BukkitRunnable() {

            @Override
            public void run() {

                if (!km.hasPicked(player)) {

                    km.removeCooldown(player);

                    Arena arena = Arenas.getArena(player);

                    if (arena == null) {
                        if (knife != null && !knife.isDead()) {
                            knife.remove();
                        }
                        km.removeKnife(knife);
                        return;
                    }

                    // sempre remove a faca dropada quando o cooldown acabar
                    if (knife != null && !knife.isDead()) {
                        knife.remove();
                    }

                    km.removeKnife(knife);

                    // se não estiver em partida, não devolve a faca
                    if (arena.getState() != GameState.INGAME) {
                        return;
                    }

                    // AMONG US: se o jogador acabou de matar, a faca visual não volta
                    // até acabar o cooldown configurável do modo.
                    if (arena.getGameMode() == me.spwtyz.murder.GameModeType.SABOTAGE
                            && plugin.sabotageTaskManager != null
                            && !plugin.sabotageTaskManager.canMurderKill(player, arena)) {
                        plugin.sabotageTaskManager.setWaitingKnife(player);
                        final long left = Math.max(1L, plugin.sabotageTaskManager.getMurderKillCooldownLeft(player));
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                Arena current = Arenas.getArena(player);
                                if (current != null && current == arena && current.getState() == GameState.INGAME
                                        && plugin.sabotageTaskManager != null
                                        && plugin.sabotageTaskManager.canMurderKill(player, arena)) {
                                    plugin.sabotageTaskManager.giveRealKnife(player);
                                    player.sendMessage("§aSua faca voltou!");
                                }
                            }
                        }.runTaskLater(plugin, left * 20L);
                        return;
                    }

                    giveKnife(player);

                    player.getWorld().strikeLightningEffect(player.getLocation());

                    player.playSound(
                            player.getLocation(),
                            Sound.ITEM_PICKUP,
                            1f,
                            1f
                    );

                    player.sendMessage("§aSua faca voltou!");

                } else {
                    km.clearPicked(player);
                }
            }

        }.runTaskLater(plugin, plugin.knifeSkinManager != null ? plugin.knifeSkinManager.getCooldownTicks(player) : 200L);
    }


    // ==================================================
    // 🔥 PEGAR FACA
    // ==================================================
    @EventHandler
    public void onMove(PlayerMoveEvent event) {

        Player player = event.getPlayer();

        Arena arena = Arenas.getArena(player);

        if (arena == null) {
            return;
        }

        KnifeManager km = arena.getKnifeManager();

        for (Item item : new ArrayList<>(km.getKnifeItems())) {

            if (item == null || item.isDead()) {
                continue;
            }

            KnifeManager.Knife knife = km.getKnife(item);

            if (knife == null) {
                continue;
            }

            // somente dono pega
            if (!knife.owner.equals(player)) {
                continue;
            }

            // evita faca voltar instantaneamente
            if (System.currentTimeMillis() - knife.spawnTime < 700L) {
                continue;
            }

            // mundos diferentes
            if (item.getWorld() == null || player.getWorld() == null) {
                continue;
            }

            if (!item.getWorld().equals(player.getWorld())) {
                continue;
            }

            // distancia pickup
            if (item.getLocation().distance(player.getLocation()) <= 1.5) {

                if (!item.isDead()) {
                    item.remove();
                }

                km.removeKnife(item);

                km.setPicked(player);

                km.removeCooldown(player);

                if (arena.getGameMode() == me.spwtyz.murder.GameModeType.SABOTAGE
                        && plugin.sabotageTaskManager != null
                        && !plugin.sabotageTaskManager.canMurderKill(player, arena)) {
                    plugin.sabotageTaskManager.setWaitingKnife(player);
                } else if (!plugin.hasKnifeNoCooldown(player)) {
                    giveKnife(player);
                }

                // Pickup manual: sem trovão e sem mensagem de "faca voltou".
                // O trovão/mensagem ficam apenas para a recarga automática da faca.
                player.playSound(
                        player.getLocation(),
                        Sound.ITEM_PICKUP,
                        1f,
                        1f
                );

                return;
            }
        }
    }


    private void handleThrowKnifeDeath(Player victim, Player killer, Arena arena) {
        if (victim == null || arena == null) return;

        // Faca arremessada remove o player direto da arena, então PlayerDeathEvent não dispara.
        // No AMONG US precisamos criar o corpo do CorpseReborn manualmente antes de mandar para espectador.
        if (arena.getGameMode() == me.spwtyz.murder.GameModeType.SABOTAGE && plugin.sabotageTaskManager != null) {
            CorpseData corpse = null;
            try {
                if (Bukkit.getPluginManager().isPluginEnabled("CorpseReborn")) {
                    corpse = CorpseAPI.spawnCorpse(victim, victim.getLocation());
                    arena.data.add(corpse);
                }
            } catch (Throwable ignored) {}
            plugin.sabotageTaskManager.handleDeath(victim, killer, arena, corpse);
        }

        CosmeticEffectManager.playDeathEffect(plugin, victim);
        arena.removePlayer(victim, "death");
    }

    // ==================================================
    // 🔥 TASK DA FACA
    // ==================================================
    public void startKnifeTask() {
    	

        new BukkitRunnable() {

            @Override
            public void run() {

                for (Arena arena : Arenas.getArenas()) {

                    if (arena == null) {
                        continue;
                    }

                    KnifeManager km = arena.getKnifeManager();

                    for (Item item : new ArrayList<>(km.getKnifeItems())) {

                        if (item == null || item.isDead()) {

                            km.removeKnife(item);

                            continue;
                        }
                        

                        KnifeManager.Knife knife = km.getKnife(item);

                        if (knife == null) {
                            continue;
                        }
                        
                        // ativa/desativa hit
                        if (item.isOnGround()) {
                            knife.active = false;
                        } else {
                            knife.active = true;
                        }
                        
                     // =====================================
                     // HIT DETECTION
                     // =====================================

                     if (knife.active) {

                         for (org.bukkit.entity.Entity ent : item.getNearbyEntities(1.2, 1.2, 1.2)) {

                             if (!(ent instanceof Player)) {
                                 continue;
                             }

                             Player victim = (Player) ent;

                             // nao mata dono
                             if (victim.equals(knife.owner)) {
                                 continue;
                             }

                             // mesma arena e somente jogadores vivos. Espectador/morto nunca deve ativar kill/effect.
                             if (!arena.players.contains(victim) || arena.specs.contains(victim)) {
                                 continue;
                             }
                             if (plugin.getPlayerState(victim) == PlayerState.SPECTATOR) {
                                 continue;
                             }
                             if (Arenas.getArena(victim) != arena) {
                                 continue;
                             }
                             if (arena.getState() != GameState.INGAME) {
                                 continue;
                             }
                             if (arena.getType(victim) == PlayerType.None) {
                                 continue;
                             }

                             // AMONG US: cooldown do Murder estilo Among Us.
                             if (arena.getGameMode() == me.spwtyz.murder.GameModeType.SABOTAGE
                                     && plugin.sabotageTaskManager != null
                                     && knife.owner != null
                                     && !plugin.sabotageTaskManager.canMurderKill(knife.owner, arena)
                                     && !item.hasMetadata("smurder_sabotage_throw_allowed")) {
                                 knife.owner.sendMessage("§cAguarde " + plugin.sabotageTaskManager.getMurderKillCooldownLeft(knife.owner) + "s para matar novamente no AMONG US.");
                                 item.remove();
                                 km.removeKnife(item);
                                 if (arena.getGameMode() != me.spwtyz.murder.GameModeType.SABOTAGE && plugin.hasKnifeNoCooldown(knife.owner)) giveKnife(knife.owner);
                                 break;
                             }

                             // Hats com habilidade podem bloquear uma morte por faca arremessada antes de aplicar efeito/remover.
                             if (plugin.hatAbilityManager != null && plugin.hatAbilityManager.tryPreventLethalHit(victim, knife.owner, arena, "throw_knife")) {
                                 item.remove();
                                 km.removeKnife(item);
                                 if (knife.owner != null && knife.owner.isOnline() && plugin.hasKnifeNoCooldown(knife.owner)) {
                                     giveKnife(knife.owner);
                                 }
                                 break;
                             }

                             // salva o replay ANTES de remover/virar espectador.
                             // A faca arremessada nao dispara PlayerDeathEvent, entao o replay precisa ser salvo aqui.
                             if (plugin.replayManager != null && knife.owner != null) {
                                 if (plugin.replayManager != null) plugin.replayManager.markHit(victim, knife.owner, arena);
                                 if (plugin.replayManager != null) plugin.replayManager.recordKnifeHit(knife.owner, victim.getLocation());
                                 if (plugin.replayManager != null) plugin.replayManager.saveKillReplay(victim, knife.owner, arena);
                             }

                             handleThrowKnifeDeath(victim, knife.owner, arena);
                             if (arena.getGameMode() == me.spwtyz.murder.GameModeType.SABOTAGE && plugin.sabotageTaskManager != null && knife.owner != null) {
                                 plugin.sabotageTaskManager.applyMurderKillCooldown(knife.owner, arena);
                             }

                             Bukkit.getScheduler().runTask(plugin, () -> arena.refreshVisibility());

                             // remove faca do chão depois do hit.
                             // Se o staff ativou /m knifecooldown no assassino, devolve instantâneo.
                             // Se for cooldown normal, o runnable do cooldown ainda devolve a faca no tempo certo.
                             item.remove();

                             km.removeKnife(item);

                             if (knife.owner != null && knife.owner.isOnline()) {
                                 if (arena.getGameMode() == me.spwtyz.murder.GameModeType.SABOTAGE && plugin.sabotageTaskManager != null) {
                                     plugin.sabotageTaskManager.setWaitingKnife(knife.owner);
                                 } else if (plugin.hasKnifeNoCooldown(knife.owner)) {
                                     giveKnife(knife.owner);
                                 }
                             }

                             break;
                         }
                     }


                        // efeito visual da skin/equipado
                        if (plugin.knifeSkinManager != null && knife.owner != null) {
                            plugin.knifeSkinManager.playTrail(knife.owner, item.getLocation());
                        } else {
                            item.getWorld().playEffect(item.getLocation(), Effect.CRIT, 1);
                        }

                        // girar mais rapido
                        item.setTicksLived(
                                item.getTicksLived() + 5
                        );
                    }
                }
            }

        }.runTaskTimer(plugin, 1L, 2L);
    }

    // ==================================================
    // 🔥 BLOQUEAR DROP
    // ==================================================
    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {

        if (plugin.knifeSkinManager != null && plugin.knifeSkinManager.isKnife(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }
}