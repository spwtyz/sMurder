package me.spwtyz.murder.listeners;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.spwtyz.murder.Arena;
import me.spwtyz.murder.Arenas;
import me.spwtyz.murder.GameModeType;
import me.spwtyz.murder.GameState;
import me.spwtyz.murder.Main;
import me.spwtyz.murder.PlayerType;

public class EntityDamageByEntityEvent implements Listener {

    private final Main plugin;

    public EntityDamageByEntityEvent(Main plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void DamageEvent(org.bukkit.event.entity.EntityDamageByEntityEvent e) {

        if (e.getEntity().getType() == EntityType.ARMOR_STAND) {
            if (e.getDamager() instanceof Player) {
                Player p = (Player) e.getDamager();
                if (Arenas.isInArena(p) || plugin.getConfig().getBoolean("bungee")) {
                    e.setCancelled(true);
                }
            }
            return;
        }

        if (e.getDamager() instanceof Snowball && e.getEntity() instanceof Player) {
            Player victim = (Player) e.getEntity();
            Arena arena = Arenas.getArena(victim);

            if (arena == null) return;

            if (arena.getState() != GameState.INGAME || arena.specs.contains(victim)) {
                e.setCancelled(true);
                e.setDamage(0.0D);
                return;
            }

            Snowball snowball = (Snowball) e.getDamager();
            if (!(snowball.getShooter() instanceof Player)) return;

            Player damager = (Player) snowball.getShooter();
            if (damager.equals(victim)) {
                e.setCancelled(true);
                e.setDamage(0.0D);
                return;
            }

            if (plugin.replayManager != null) {
                if (plugin.replayManager != null) plugin.replayManager.markHit(victim, damager, arena);
            }

            if (Arenas.getArena(damager) != arena || arena.specs.contains(damager)) {
                e.setCancelled(true);
                e.setDamage(0.0D);
                return;
            }

            if (arena.time <= 0) {
                e.setCancelled(true);
                e.setDamage(0.0D);
                return;
            }

            if (arena.isTntTagMode()) {
                e.setCancelled(true);
                e.setDamage(0.0D);
                return;
            }

            if (arena.getType(victim) == PlayerType.Murderer) {
                arena.heros.add(damager);
            }

            // Performance fix: nao salva replay pesado no EntityDamage.
            // Aqui apenas marcamos o ultimo hit. O replay e salvo uma unica vez no DeathEvent/removePlayer.

            if (arena.getType(victim) == PlayerType.Murderer && arena.getType(damager) == PlayerType.Murderer) {
                e.setCancelled(true);
                e.setDamage(0.0D);
                return;
            }
            if (arena.getType(victim) == PlayerType.Detective && arena.getType(damager) == PlayerType.Detective) {
                e.setCancelled(true);
                e.setDamage(0.0D);
                return;
            }

            if (plugin.hatAbilityManager != null && plugin.hatAbilityManager.tryPreventLethalHit(victim, damager, arena, "gun")) {
                e.setCancelled(true);
                e.setDamage(0.0D);
                return;
            }

            e.setCancelled(false);
            e.setDamage(1000.0D);
            return;
        }

        if (!(e.getDamager() instanceof Player) || !(e.getEntity() instanceof Player)) return;

        Player damager = (Player) e.getDamager();
        Player victim = (Player) e.getEntity();

        Arena damagerArena = Arenas.getArena(damager);
        Arena victimArena = Arenas.getArena(victim);

        if (damagerArena == null || victimArena == null) {
            if (damagerArena != null || victimArena != null) {
                e.setCancelled(true);
                e.setDamage(0.0D);
            }
            return;
        }

        if (damagerArena != victimArena) {
            e.setCancelled(true);
            e.setDamage(0.0D);
            return;
        }

        Arena arena = damagerArena;

        if (plugin.replayManager != null) {
            if (plugin.replayManager != null) plugin.replayManager.markHit(victim, damager, arena);
        }

        if (arena.getState() != GameState.INGAME) {
            e.setCancelled(true);
            e.setDamage(0.0D);
            return;
        }

        if (arena.specs.contains(damager) || arena.specs.contains(victim)) {
            e.setCancelled(true);
            e.setDamage(0.0D);
            return;
        }

        // AMONG US: trava forte do cooldown antes de qualquer lógica de dano.
        // Mesmo se a skin da faca não for reconhecida ou outro listener tentar liberar o dano,
        // o Murder não consegue bater/matar enquanto o cooldown do modo estiver ativo.
        if (arena.getGameMode() == GameModeType.SABOTAGE
                && arena.getType(damager) == PlayerType.Murderer
                && plugin.sabotageTaskManager != null
                && !plugin.sabotageTaskManager.canMurderKill(damager, arena)) {
            e.setCancelled(true);
            e.setDamage(0.0D);
            plugin.sabotageTaskManager.setWaitingKnife(damager);
            damager.sendMessage("§cAguarde " + plugin.sabotageTaskManager.getMurderKillCooldownLeft(damager)
                    + "s para matar novamente no AMONG US.");
            return;
        }

        // Faca em cooldown/inativa nao pode matar por hit.
        // Isso corrige o bug de bater com "§cAguarde..." e matar antes da faca recarregar.
        if (isWaitingKnife(damager.getItemInHand())) {
            e.setCancelled(true);
            e.setDamage(0.0D);
            return;
        }

        if (arena.getType(damager) == PlayerType.Murderer
                && arena.getKnifeManager() != null
                && arena.getKnifeManager().isCooldown(damager)
                && (arena.getGameMode() == GameModeType.SABOTAGE || !plugin.hasKnifeNoCooldown(damager))) {
            e.setCancelled(true);
            e.setDamage(0.0D);
            return;
        }

        if (arena.isTntTagMode()) {
            boolean damagerHasTnt = arena.isTntHolder(damager);
            boolean victimHasTnt = arena.isTntHolder(victim);
            boolean victimProtected = arena.isTntHitProtected(victim);

            // Se o alvo ativou /m tnthit e ele NAO esta com TNT,
            // jogadores sem TNT nao conseguem bater nele.
            // Se o alvo estiver com TNT, a protecao nao funciona e todos podem bater nele.
            if (victimProtected && !victimHasTnt && !damagerHasTnt) {
                e.setCancelled(true);
                e.setDamage(0.0D);
                return;
            }

            // Quem esta com TNT sempre pode bater para passar a TNT.
            if (damagerHasTnt) {
                arena.tagTnt(damager, victim);
                e.setCancelled(false); // permite animacao/knockback do hit
                e.setDamage(0.0D);     // nao abaixa coracao no TNTTag
                return;
            }

            // Sem TNT: hit normal/knockback permitido, mas sem dano de coracao no TNTTag.
            e.setCancelled(false);
            e.setDamage(0.0D);
            return;
        }

        if (arena.getGameMode() == GameModeType.HIDE_AND_SEEK) {
            // No Esconde-Esconde o hit do Procurador captura, mas nunca mata nem tira coracao.
            e.setCancelled(true);
            e.setDamage(0.0D);
            if (arena.getType(damager) == PlayerType.Murderer && arena.getType(victim) == PlayerType.Innocents) {
                try {
                    victim.setFallDistance(0.0F);
                    victim.setFireTicks(0);
                    victim.setHealth(victim.getMaxHealth());
                } catch (Throwable ignored) {}
                arena.captureHideAndSeek(victim, damager);
                try { victim.setHealth(victim.getMaxHealth()); } catch (Throwable ignored) {}
            }
            return;
        }

        if (arena.getGameMode() == GameModeType.ALL_MURDER) {
            // Todos Assassinos usa a mesma regra de faca dos modos normais.
            // Antes qualquer hit matava, mesmo usando item normal ou a espada "Aguarde".
            if (!isMurderKnife(damager.getItemInHand())) {
                e.setCancelled(true);
                e.setDamage(0.0D);
                return;
            }

            if (arena.getKnifeManager() != null && arena.getKnifeManager().isCooldown(damager)
                    && !plugin.hasKnifeNoCooldown(damager)) {
                e.setCancelled(true);
                e.setDamage(0.0D);
                return;
            }

            if (plugin.hatAbilityManager != null && plugin.hatAbilityManager.tryPreventLethalHit(victim, damager, arena, "knife")) {
                e.setCancelled(true);
                e.setDamage(0.0D);
                return;
            }
            if (plugin.replayManager != null) {
                if (plugin.replayManager != null) plugin.replayManager.saveKillReplay(victim, damager, arena);
            }
            e.setCancelled(false);
            e.setDamage(1000.0D);
            return;
        }

        PlayerType damagerType = arena.getType(damager);
        PlayerType victimType = arena.getType(victim);

        // Bloqueio extra: item "Aguarde..." nunca causa dano/morte.
        // Isso fecha o bypass de arremessar a faca e bater no mesmo instante.
        if (isWaitingKnife(damager.getItemInHand())) {
            e.setCancelled(true);
            e.setDamage(0.0D);
            return;
        }

        if (damagerType == PlayerType.Murderer) {
            if (victimType == PlayerType.Murderer) {
                e.setCancelled(true);
                e.setDamage(0.0D);
                return;
            }

            if (isMurderKnife(damager.getItemInHand())) {
                // AMONG US: o hit direto da faca usa o mesmo cooldown configuravel do modo.
                // Antes apenas a faca arremessada respeitava AMONG US.MurderKillCooldownSeconds.
                if (arena.getGameMode() == GameModeType.SABOTAGE
                        && plugin.sabotageTaskManager != null
                        && !plugin.sabotageTaskManager.canMurderKill(damager, arena)) {
                    e.setCancelled(true);
                    e.setDamage(0.0D);
                    damager.sendMessage("§cAguarde " + plugin.sabotageTaskManager.getMurderKillCooldownLeft(damager)
                            + "s para matar novamente no AMONG US.");
                    return;
                }

                if (plugin.hatAbilityManager != null && plugin.hatAbilityManager.tryPreventLethalHit(victim, damager, arena, "knife")) {
                    e.setCancelled(true);
                    e.setDamage(0.0D);
                    return;
                }
                if (arena.getGameMode() == GameModeType.SABOTAGE && plugin.sabotageTaskManager != null) {
                    plugin.sabotageTaskManager.applyMurderKillCooldown(damager, arena);
                    plugin.sabotageTaskManager.setWaitingKnife(damager);
                }
                if (plugin.replayManager != null) {
                    if (plugin.replayManager != null) plugin.replayManager.saveKillReplay(victim, damager, arena);
                }
                e.setCancelled(false);
                e.setDamage(1000.0D);
                return;
            }
        }

        e.setCancelled(true);
        e.setDamage(0.0D);
    }

    private boolean isWaitingKnife(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta.hasDisplayName() && meta.getDisplayName().startsWith("§cAguarde")) return true;
        if (meta.hasLore()) {
            for (String line : meta.getLore()) {
                if (line != null && (line.contains("wait-knife") || line.contains("amongus-wait-knife"))) return true;
            }
        }
        return false;
    }

    private boolean isMurderKnife(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;

        if (plugin.knifeSkinManager != null && plugin.knifeSkinManager.isKnife(item)) {
            return true;
        }

        Material cfgKnife = Material.getMaterial(plugin.settings.getConfig().getInt("murderer-weapon.item-id"));
        return cfgKnife != null && item.getType() == cfgKnife;
    }
}
