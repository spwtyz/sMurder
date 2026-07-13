package me.spwtyz.murder.knife;

import java.util.*;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import me.spwtyz.murder.Arena;

public class KnifeManager {

    private final Arena arena;

    private final Set<Player> cooldown = new HashSet<>();
    private final Set<Player> picked = new HashSet<>();
    private final Map<Item, Knife> knives = new HashMap<>();
    public long spawnTime;
    public KnifeManager(Arena arena) {
        this.arena = arena;
    }

    // =========================
    // 🔥 KNIFE OBJECT
    // =========================
    public static class Knife {

        public Player owner;
        public boolean active = true;

        // tempo que a faca foi criada
        public long spawnTime;

        public Knife(Player owner) {
            this.owner = owner;
        }
    }

    // =========================
    // 🎯 COOLDOWN
    // =========================
    public boolean isCooldown(Player p) {
        return cooldown.contains(p);
    }

    public void addCooldown(Player p) {
        cooldown.add(p);
    }

    public void removeCooldown(Player p) {
        cooldown.remove(p);
    }

    // =========================
    // 🧲 PICKUP
    // =========================
    public boolean hasPicked(Player p) {
        return picked.contains(p);
    }

    public void setPicked(Player p) {
        picked.add(p);
    }

    public void clearPicked(Player p) {
        picked.remove(p);
    }

    // =========================
    // 🗡 KNIVES
    // =========================
    public Knife addKnife(Item item, Player owner) {

        Knife knife = new Knife(owner);

        knife.spawnTime = System.currentTimeMillis();

        knives.put(item, knife);

        return knife;
    }

    public Knife getKnife(Item item) {
        return knives.get(item);
    }

    public void removeKnife(Item item) {
        knives.remove(item);
    }

    public Collection<Knife> getAllKnives() {
        return knives.values();
    }

    public Set<Item> getKnifeItems() {
        return knives.keySet();
        
    }

    // =========================
    // 🧹 CLEANUP (IMPORTANTE)
    // =========================
    public void reset() {
        cooldown.clear();
        picked.clear();

        for (Item i : new ArrayList<>(knives.keySet())) {
            if (i != null && !i.isDead()) {
                i.remove();
            }
        }

        knives.clear();
    }
}
