package me.spwtyz.murder.kits;


import me.spwtyz.murder.Arena;
import me.spwtyz.murder.GameModeType;
import me.spwtyz.murder.Main;
import me.spwtyz.murder.PlayerType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

public class KitManager {
	
	private Map<UUID, Kit> selectedKits = new HashMap<>();

    private Main plugin;

    public KitManager(Main plugin) {
        this.plugin = plugin;
    }

    private List<Kit> kits = new ArrayList<>();

    public void registerKit(Kit kit) {
        kits.add(kit);
    }

    public List<Kit> getAllKits() {
        return kits;
    }
    
    public void setSelectedKit(Player p, Kit kit) {
        selectedKits.put(p.getUniqueId(), kit);
    }

    public Kit getSelectedKit(Player p) {
        return selectedKits.get(p.getUniqueId());
    }

    public void removeSelectedKit(Player p) {
        selectedKits.remove(p.getUniqueId());
    }

    public List<Kit> getAvailableKits(Player p, GameModeType type) {

        List<Kit> result = new ArrayList<Kit>();

        Arena arena = plugin.playerArena.get(p.getUniqueId());
        PlayerType role = arena != null ? arena.getType(p) : PlayerType.None;

        for (Kit kit : kits) {
            if (!isAllowedForMode(kit, type)) continue;

            // Antes do sorteio de roles, mostra os kits do modo para o player poder escolher.
            if (role == PlayerType.None) {
                result.add(kit);
                continue;
            }

            if (isAllowedForRole(kit, role, type)) {
                result.add(kit);
            }
        }

        return result;
    }

    public boolean isAllowedForMode(Kit kit, GameModeType mode) {
        if (kit == null || mode == null) return false;
        String id = getKitId(kit);

        if (mode == GameModeType.HIDE_AND_SEEK) {
            return id.startsWith("hide_");
        }
        if (mode == GameModeType.TNT_TAG) {
            return id.startsWith("tnt_");
        }
        if (mode == GameModeType.SABOTAGE) {
            return id.startsWith("sabotage_");
        }
        if (mode == GameModeType.RANKED) {
            // Ranked fica limpo/competitivo por enquanto.
            return false;
        }
        if (mode == GameModeType.ALL_MURDER) {
            return kit.getType() == KitType.ALL_MURDER || kit.getType() == KitType.MURDERER;
        }

        // Normal: não mostra kits de outros modos.
        return !id.startsWith("hide_") && !id.startsWith("tnt_") && !id.startsWith("sabotage_");
    }

    public String getKitId(Kit kit) {
        if (kit instanceof AdvancedKit) return ((AdvancedKit) kit).getId().toLowerCase();
        return kit.getName().toLowerCase().replace(" ", "_");
    }

    public int getPrice(Kit kit) {
        String id = getKitId(kit);
        int def = 2500;
        if (id.contains("chance")) def = 6000;
        if (id.contains("teleporter")) def = 4500;
        if (id.startsWith("tnt_")) def = 3500;
        if (id.startsWith("hide_")) def = 3000;
        if (id.startsWith("sabotage_")) def = 4000;
        return plugin.getConfig().getInt("KitPrices." + id, def);
    }

    public boolean ownsKit(Player p, Kit kit) {
        if (p == null || kit == null) return false;
        String id = getKitId(kit);
        if (plugin.getConfig().getBoolean("Kits.FreeAll", false)) return true;
        return plugin.data.getConfig().getBoolean("Kits." + p.getUniqueId() + "." + id, false);
    }

    public void unlockKit(Player p, Kit kit) {
        if (p == null || kit == null) return;
        plugin.data.getConfig().set("Kits." + p.getUniqueId() + "." + getKitId(kit), true);
        plugin.data.save();
    }

    public boolean isAllowedForRole(Kit kit, PlayerType role, GameModeType mode) {
        if (kit == null) return false;
        if (!isAllowedForMode(kit, mode)) return false;
        if (mode == GameModeType.ALL_MURDER) {
            return kit.getType() == KitType.ALL_MURDER || kit.getType() == KitType.MURDERER;
        }
        if (kit.getType() == KitType.ALL_MURDER) return true;
        if (kit.getType() == KitType.ROLE_CHANCE) return true;
        if (role == PlayerType.Murderer) return kit.getType() == KitType.MURDERER;
        if (role == PlayerType.Detective) return kit.getType() == KitType.DETECTIVE || kit.getType() == KitType.INNOCENT_DETECTIVE;
        if (role == PlayerType.Innocents) return kit.getType() == KitType.INNOCENT || kit.getType() == KitType.INNOCENT_DETECTIVE;
        return true;
    }

    public String getTypeDisplay(Kit kit) {
        if (kit == null) return "§7Todos";
        if (kit.getType() == KitType.MURDERER) return "§cMurder";
        if (kit.getType() == KitType.DETECTIVE) return "§bDetetive";
        if (kit.getType() == KitType.INNOCENT) return "§aInocente";
        if (kit.getType() == KitType.INNOCENT_DETECTIVE) return "§aInocente §7/ §bDetetive";
        if (kit.getType() == KitType.ROLE_CHANCE) return "§6Chance de Role";
        return "§6Todos Assassinos";
    }

}
