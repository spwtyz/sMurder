package me.spwtyz.murder.rooms;

import java.util.ArrayList;
import java.util.List;

import me.spwtyz.murder.GameModeType;
import me.spwtyz.murder.Main;
import me.spwtyz.murder.kits.Kit;

public class RoomFeatureLockManager {

    private static final String MODE_PATH = "RoomStaff.DisabledModes";
    private static final String MOD_PATH = "RoomStaff.DisabledModifiers";
    private static final String KIT_PATH = "RoomStaff.DisabledKits";
    private static final String MAP_PATH = "RoomStaff.DisabledMaps";

    public static boolean isModeDisabled(Main plugin, GameModeType mode) {
        if (plugin == null || mode == null) return false;
        return plugin.getConfig().getStringList(MODE_PATH).contains(mode.name());
    }

    public static boolean isModifierDisabled(Main plugin, RoomModifier modifier) {
        if (plugin == null || modifier == null) return false;
        return plugin.getConfig().getStringList(MOD_PATH).contains(modifier.name());
    }

    public static boolean isKitDisabled(Main plugin, Kit kit) {
        if (plugin == null || kit == null) return false;
        return plugin.getConfig().getStringList(KIT_PATH).contains(kit.getName().toLowerCase());
    }

    public static boolean isMapDisabled(Main plugin, String mapName) {
        if (plugin == null || mapName == null) return false;
        return plugin.getConfig().getStringList(MAP_PATH).contains(mapName.toLowerCase());
    }

    public static boolean toggleKit(Main plugin, Kit kit) {
        if (plugin == null || kit == null) return false;
        String key = kit.getName().toLowerCase();
        List<String> list = new ArrayList<String>(plugin.getConfig().getStringList(KIT_PATH));
        boolean disabled;
        if (list.contains(key)) {
            list.remove(key);
            disabled = false;
        } else {
            list.add(key);
            disabled = true;
        }
        plugin.getConfig().set(KIT_PATH, list);
        plugin.saveConfig();
        return disabled;
    }

    public static boolean toggleMap(Main plugin, String mapName) {
        if (plugin == null || mapName == null) return false;
        String key = mapName.toLowerCase();
        List<String> list = new ArrayList<String>(plugin.getConfig().getStringList(MAP_PATH));
        boolean disabled;
        if (list.contains(key)) {
            list.remove(key);
            disabled = false;
        } else {
            list.add(key);
            disabled = true;
        }
        plugin.getConfig().set(MAP_PATH, list);
        plugin.saveConfig();
        return disabled;
    }

    public static boolean toggleMode(Main plugin, GameModeType mode) {
        if (plugin == null || mode == null) return false;
        List<String> list = new ArrayList<String>(plugin.getConfig().getStringList(MODE_PATH));
        boolean disabled;
        if (list.contains(mode.name())) {
            list.remove(mode.name());
            disabled = false;
        } else {
            list.add(mode.name());
            disabled = true;
        }
        plugin.getConfig().set(MODE_PATH, list);
        plugin.saveConfig();
        return disabled;
    }

    public static boolean toggleModifier(Main plugin, RoomModifier modifier) {
        if (plugin == null || modifier == null) return false;
        List<String> list = new ArrayList<String>(plugin.getConfig().getStringList(MOD_PATH));
        boolean disabled;
        if (list.contains(modifier.name())) {
            list.remove(modifier.name());
            disabled = false;
        } else {
            list.add(modifier.name());
            disabled = true;
        }
        plugin.getConfig().set(MOD_PATH, list);
        plugin.saveConfig();
        return disabled;
    }
}
