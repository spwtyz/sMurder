package me.spwtyz.murder.listeners;

import org.bukkit.event.Listener;

import me.spwtyz.murder.Main;

/**
 * O hit do TNTTag agora é tratado centralizado em EntityDamageByEntityEvent.
 * Esta classe fica vazia para evitar listener duplicado cancelando o dano/knockback.
 */
public class TntTagEvent implements Listener {

    @SuppressWarnings("unused")
    private final Main plugin;

    public TntTagEvent(Main plugin) {
        this.plugin = plugin;
    }
}
