package me.spwtyz.murder.chat;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import me.spwtyz.murder.Main;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class TagManager {

    private final Main plugin;

    // Salva o ID da tag selecionada. Compatível com saves antigos que guardavam o display direto.
    private final Map<UUID, String> selectedCache = new HashMap<UUID, String>();
    private final Map<UUID, String> displayCache = new HashMap<UUID, String>();

    public TagManager(Main plugin) {
        this.plugin = plugin;
    }

    public void setupDefaults() {
        hideLegacyShopTitleTag("shadow");
        hideLegacyShopTitleTag("blood");
        hideLegacyShopTitleTag("nightmare");

        addDefaultTag("vip", "&aVIP", "murder.tag.vip", "vip", "&7Tag exclusiva para VIP.", 10);
        addDefaultTag("vipplus", "&aVIP&6+", "murder.tag.vipplus", "vip", "&7Tag exclusiva para VIP+.", 20);
        addDefaultTag("mvp", "&bMVP", "murder.tag.mvp", "vip", "&7Tag exclusiva para MVP.", 30);

        addDefaultTag("helper", "&9Helper", "murder.tag.staff.helper", "staff", "&7Tag da equipe Helper.", 100);
        addDefaultTag("mod", "&2Mod", "murder.tag.staff.mod", "staff", "&7Tag da equipe Moderador.", 200);
        addDefaultTag("admin", "&cAdmin", "murder.tag.staff.admin", "staff", "&7Tag da equipe Admin.", 300);
        addDefaultTag("dono", "&4Dono", "murder.tag.staff.dono", "staff", "&7Tag da direção.", 500);

        plugin.getConfig().addDefault("tags-settings.use-luckperms", true);
        plugin.getConfig().addDefault("tags-settings.luckperms-prefix-as-default", true);
        plugin.getConfig().addDefault("tags-settings.luckperms-weight-as-priority", true);

        plugin.getConfig().addDefault("chat.global-format", "&8[&aG&8] %tag%&7%player% &8» &f%message%");
        plugin.getConfig().addDefault("chat.local-format", "&8[&eL&8] %tag%&7%player% &8» &7%message%");
        plugin.getConfig().addDefault("chat.local-radius", 50);
        plugin.getConfig().addDefault("chat.local-default", false);
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();
    }

    private void hideLegacyShopTitleTag(String id) {
        if (id == null) return;
        plugin.getConfig().set("tags." + id + ".type", "title");
        plugin.getConfig().set("tags." + id + ".permission", "murder.title.shop-only");
        plugin.getConfig().addDefault("tags." + id + ".weight", 0);
    }

    private void addDefaultTag(String id, String display, String permission, String type, String desc, int weight) {
        plugin.getConfig().addDefault("tags." + id + ".display", display);
        plugin.getConfig().addDefault("tags." + id + ".permission", permission);
        plugin.getConfig().addDefault("tags." + id + ".type", type);
        plugin.getConfig().addDefault("tags." + id + ".description", desc);
        plugin.getConfig().addDefault("tags." + id + ".weight", weight);
    }

    private String color(String value) {
        return value == null ? "" : ChatColor.translateAlternateColorCodes('&', value);
    }

    private String sqlEscape(String value) {
        return value == null ? "" : value.replace("'", "''");
    }

    private boolean isRawDisplay(String value) {
        if (value == null) return false;
        return value.contains("§") || value.contains("&") || value.contains(" ");
    }

    public String getSelectedTagId(Player p) {
        if (p == null) return "";
        UUID uuid = p.getUniqueId();
        if (selectedCache.containsKey(uuid)) return selectedCache.get(uuid);

        String stored = "";
        if (plugin.getConfig().getBoolean("mysql") && plugin.sql != null && plugin.sql.isConnected()) {
            try {
                Statement st = plugin.sql.getConnection().createStatement();
                ResultSet rs = st.executeQuery("SELECT tag FROM MurderTags WHERE uuid='" + uuid.toString() + "'");
                if (rs.next()) stored = rs.getString("tag");
                rs.close();
                st.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (plugin.data != null) {
            stored = plugin.data.getConfig().getString("Tags." + uuid.toString(), "");
        }

        if (stored == null) stored = "";
        selectedCache.put(uuid, stored);
        return stored;
    }

    public String getTag(Player p) {
        if (p == null) return "";
        UUID uuid = p.getUniqueId();
        if (displayCache.containsKey(uuid)) return displayCache.get(uuid);

        String selected = getSelectedTagId(p);
        String display = "";

        if (selected != null && !selected.trim().isEmpty()) {
            if (isRawDisplay(selected) && getConfigTag(selected) == null) {
                display = color(selected);
            } else {
                String configDisplay = getConfigTag(selected);
                display = configDisplay == null ? color(selected) : configDisplay;
            }
        }

        // Se o jogador não escolheu tag do /tag, usa prefixo do LuckPerms como fallback.
        if ((display == null || display.trim().isEmpty())
                && plugin.getConfig().getBoolean("tags-settings.use-luckperms", true)
                && plugin.getConfig().getBoolean("tags-settings.luckperms-prefix-as-default", true)) {
            display = LuckPermsHook.getPrefix(p);
        }

        if (display == null) display = "";
        display = color(display);
        displayCache.put(uuid, display);
        return display;
    }

    public int getTagWeight(Player p) {
        if (p == null) return 0;
        String selected = getSelectedTagId(p);
        int configWeight = 0;
        if (selected != null && !selected.trim().isEmpty() && !isRawDisplay(selected)) {
            configWeight = plugin.getConfig().getInt("tags." + selected.toLowerCase() + ".weight", 0);
        }

        if (plugin.getConfig().getBoolean("tags-settings.use-luckperms", true)
                && plugin.getConfig().getBoolean("tags-settings.luckperms-weight-as-priority", true)) {
            int lpWeight = LuckPermsHook.getWeight(p);
            return Math.max(configWeight, lpWeight);
        }
        return configWeight;
    }

    public void setTag(Player p, String tag) {
        if (p == null) return;
        if (tag == null) tag = "";
        tag = tag.trim();
        selectedCache.put(p.getUniqueId(), tag);
        displayCache.remove(p.getUniqueId());
        saveSelected(p, tag);
        applyVisuals(p);
        refreshScoreboard(p);
    }

    private void saveSelected(Player p, String selected) {
        if (plugin.getConfig().getBoolean("mysql") && plugin.sql != null && plugin.sql.isConnected()) {
            try {
                Statement st = plugin.sql.getConnection().createStatement();
                st.executeUpdate("INSERT INTO MurderTags (uuid, tag) VALUES ('" + p.getUniqueId().toString() + "', '" + sqlEscape(selected) + "') ON DUPLICATE KEY UPDATE tag='" + sqlEscape(selected) + "'");
                st.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (plugin.data != null) {
            plugin.data.getConfig().set("Tags." + p.getUniqueId().toString(), selected);
            plugin.data.save();
        }
    }

    public void clearTag(Player p) {
        if (p == null) return;
        selectedCache.put(p.getUniqueId(), "");
        displayCache.remove(p.getUniqueId());
        if (plugin.getConfig().getBoolean("mysql") && plugin.sql != null && plugin.sql.isConnected()) {
            try {
                Statement st = plugin.sql.getConnection().createStatement();
                st.executeUpdate("DELETE FROM MurderTags WHERE uuid='" + p.getUniqueId().toString() + "'");
                st.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (plugin.data != null) {
            plugin.data.getConfig().set("Tags." + p.getUniqueId().toString(), null);
            plugin.data.save();
        }
        applyVisuals(p);
        refreshScoreboard(p);
    }

    private void refreshScoreboard(Player p) {
        try {
            if (plugin.scoreboards != null) {
                plugin.scoreboards.remove(p.getName());
                plugin.scorestate.remove(p.getName());
                plugin.setScoreboard(p);
            }
        } catch (Throwable ignored) {}
    }

    public void applyVisuals(Player p) {
        if (p == null) return;
        try {
            String tag = getTag(p);
            String cleanTag = tag == null ? "" : tag.trim();
            p.setDisplayName((cleanTag.isEmpty() ? "" : cleanTag + " ") + p.getName());
            p.setPlayerListName(p.getName());

            applyNametagToBoard(Bukkit.getScoreboardManager().getMainScoreboard(), p, cleanTag);
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                if (viewer.getScoreboard() != null) {
                    applyNametagToBoard(viewer.getScoreboard(), p, cleanTag);
                }
            }
        } catch (Throwable ignored) {}
    }

    private void applyNametagToBoard(Scoreboard board, Player p, String cleanTag) {
        if (board == null || p == null) return;

        String entry = p.getName();
        String teamName = buildTeamName(p);

        for (Team t : board.getTeams()) {
            try {
                if (t.getName().startsWith("mtag_") && !t.getName().equals(teamName) && t.getEntries().contains(entry)) {
                    t.removeEntry(entry);
                }
            } catch (Throwable ignored) {}
        }

        Team team = board.getTeam(teamName);
        if (team == null) team = board.registerNewTeam(teamName);
        if (!team.getEntries().contains(entry)) team.addEntry(entry);
        team.setPrefix(makeSafeTeamPrefix(cleanTag));
        team.setSuffix("");
    }

    private String buildTeamName(Player p) {
        int weight = getTagWeight(p);
        if (weight < 0) weight = 0;
        if (weight > 999) weight = 999;
        int sort = 999 - weight;
        String name = p.getName().replaceAll("[^A-Za-z0-9_]", "");
        if (name.length() > 6) name = name.substring(0, 6);
        String out = String.format("mtag_%03d_%s", sort, name);
        if (out.length() > 16) out = out.substring(0, 16);
        return out;
    }

    private String makeSafeTeamPrefix(String tag) {
        if (tag == null || tag.trim().isEmpty()) return "";
        String prefix = tag + " §r";
        return limitWithColors(prefix, 16);
    }

    private String limitWithColors(String text, int max) {
        if (text == null) return "";
        if (text.length() <= max) return text;
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < text.length() && out.length() < max; i++) {
            char c = text.charAt(i);
            if (c == '§') {
                if (i + 1 >= text.length()) break;
                if (out.length() + 2 > max) break;
                out.append(c).append(text.charAt(++i));
                continue;
            }
            out.append(c);
        }
        return out.toString();
    }

    public String getConfigTag(String id) {
        if (id == null) return null;
        String raw = plugin.getConfig().getString("tags." + id.toLowerCase() + ".display", null);
        if (raw == null) return null;
        return color(raw);
    }

    public String getType(String id) {
        if (id == null) return "player";
        return plugin.getConfig().getString("tags." + id.toLowerCase() + ".type", "player").toLowerCase();
    }

    public boolean canUse(Player p, String id) {
        if (p == null || id == null) return false;
        String perm = plugin.getConfig().getString("tags." + id.toLowerCase() + ".permission", "");
        if (perm == null || perm.trim().isEmpty()) return true;
        return p.hasPermission(perm) || LuckPermsHook.hasPermission(p, perm) || p.isOp();
    }

    public List<String> getTagIds() {
        List<String> ids = new ArrayList<String>();
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("tags");
        if (sec != null) ids.addAll(sec.getKeys(false));
        Collections.sort(ids, new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                int wa = plugin.getConfig().getInt("tags." + a + ".weight", 0);
                int wb = plugin.getConfig().getInt("tags." + b + ".weight", 0);
                if (wa != wb) return wb - wa;
                return a.compareToIgnoreCase(b);
            }
        });
        return ids;
    }

    public void openTagSelector(Player p) {
        if (p == null) return;
        p.sendMessage(" ");
        p.sendMessage("§8§m----------------------------");
        p.sendMessage("§6§lTAGS §7- clique para selecionar");
        p.sendMessage("§7Atual: " + (getTag(p).isEmpty() ? "§cNenhuma" : getTag(p)));
        if (plugin.getConfig().getBoolean("tags-settings.use-luckperms", true)) {
            p.sendMessage("§7LuckPerms: §f" + (LuckPermsHook.isAvailable() ? "§aAtivo" : "§cNão encontrado"));
        }
        p.sendMessage(" ");

        sendGroup(p, "player", "§eTítulos da Loja");
        sendGroup(p, "vip", "§aTags VIP");
        sendGroup(p, "staff", "§cTags Staff");

        TextComponent clear = new TextComponent("§c[REMOVER TAG]");
        clear.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tag clear"));
        clear.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§cClique para remover sua tag.\n§7Se LuckPerms estiver ativo, o prefixo do grupo pode aparecer como padrão.").create()));
        p.spigot().sendMessage(clear);
        p.sendMessage("§8§m----------------------------");
    }

    private void sendGroup(Player p, String type, String title) {
        boolean printed = false;
        for (String id : getTagIds()) {
            if (!getType(id).equalsIgnoreCase(type)) continue;
            if (!printed) {
                p.sendMessage(title);
                printed = true;
            }
            sendTagLine(p, id);
        }
        if (printed) p.sendMessage(" ");
    }

    private void sendTagLine(Player p, String id) {
        String display = getConfigTag(id);
        if (display == null) return;
        String desc = color(plugin.getConfig().getString("tags." + id + ".description", "&7Clique para equipar."));
        boolean can = canUse(p, id);
        String perm = plugin.getConfig().getString("tags." + id + ".permission", "");
        int weight = plugin.getConfig().getInt("tags." + id + ".weight", 0);

        TextComponent line = new TextComponent(can ? "§a[USAR] " : "§c[BLOQUEADO] ");
        TextComponent name = new TextComponent(display + " §8(Peso " + weight + ")");
        if (can) {
            line.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tag select " + id));
            line.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§aClique para equipar\n" + desc + "\n§7Peso: §f" + weight).create()));
            name.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tag select " + id));
            name.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§aClique para equipar\n" + desc + "\n§7Peso: §f" + weight).create()));
        } else {
            line.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§cVocê não tem permissão.\n§7Permissão: §f" + perm).create()));
            name.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§cBloqueada\n§7Permissão: §f" + perm + "\n§7Peso: §f" + weight).create()));
        }
        p.spigot().sendMessage(line, name);
    }

    public void selectTag(Player p, String id) {
        if (p == null) return;
        if (id == null) {
            openTagSelector(p);
            return;
        }
        id = id.toLowerCase();
        String display = getConfigTag(id);
        if (display == null) {
            p.sendMessage("§cEssa tag não existe na config.");
            return;
        }
        if (!canUse(p, id)) {
            p.sendMessage("§cVocê não tem permissão para usar essa tag.");
            return;
        }
        setTag(p, id);
        p.sendMessage("§aTag selecionada: " + display);
    }

    /** Hook opcional via reflection para não obrigar o jar do LuckPerms no build. */
    private static class LuckPermsHook {
        private static Object provider;
        private static boolean tried;

        private static Object provider() {
            if (tried) return provider;
            tried = true;
            try {
                Class<?> lpClass = Class.forName("net.luckperms.api.LuckPerms");
                RegisteredServiceProvider<?> rsp = Bukkit.getServicesManager().getRegistration(lpClass);
                if (rsp != null) provider = rsp.getProvider();
            } catch (Throwable ignored) {}
            return provider;
        }

        public static boolean isAvailable() {
            return provider() != null;
        }

        public static String getPrefix(Player p) {
            try {
                Object lp = provider();
                if (lp == null || p == null) return "";
                Object userManager = lp.getClass().getMethod("getUserManager").invoke(lp);
                Object user = userManager.getClass().getMethod("getUser", UUID.class).invoke(userManager, p.getUniqueId());
                if (user == null) return "";
                Object cachedData = user.getClass().getMethod("getCachedData").invoke(user);
                Object metaData = cachedData.getClass().getMethod("getMetaData").invoke(cachedData);
                Object prefix = metaData.getClass().getMethod("getPrefix").invoke(metaData);
                return prefix == null ? "" : ChatColor.translateAlternateColorCodes('&', String.valueOf(prefix));
            } catch (Throwable ignored) {}
            return "";
        }

        public static int getWeight(Player p) {
            try {
                Object lp = provider();
                if (lp == null || p == null) return 0;
                Object userManager = lp.getClass().getMethod("getUserManager").invoke(lp);
                Object user = userManager.getClass().getMethod("getUser", UUID.class).invoke(userManager, p.getUniqueId());
                if (user == null) return 0;
                Object groupName = user.getClass().getMethod("getPrimaryGroup").invoke(user);
                if (groupName == null) return 0;
                Object groupManager = lp.getClass().getMethod("getGroupManager").invoke(lp);
                Object group = groupManager.getClass().getMethod("getGroup", String.class).invoke(groupManager, String.valueOf(groupName));
                if (group == null) return 0;
                Object optional = group.getClass().getMethod("getWeight").invoke(group);
                return readOptionalInt(optional);
            } catch (Throwable ignored) {}
            return 0;
        }

        private static int readOptionalInt(Object optional) {
            if (optional == null) return 0;
            try {
                Object present = optional.getClass().getMethod("isPresent").invoke(optional);
                if (present instanceof Boolean && !((Boolean) present).booleanValue()) return 0;
            } catch (Throwable ignored) {}
            try {
                Object value = optional.getClass().getMethod("getAsInt").invoke(optional);
                if (value instanceof Number) return ((Number) value).intValue();
            } catch (Throwable ignored) {}
            try {
                Object value = optional.getClass().getMethod("get").invoke(optional);
                if (value instanceof Number) return ((Number) value).intValue();
            } catch (Throwable ignored) {}
            return 0;
        }

        public static boolean hasPermission(Player p, String permission) {
            try {
                Object lp = provider();
                if (lp == null || p == null || permission == null || permission.trim().isEmpty()) return false;
                Object userManager = lp.getClass().getMethod("getUserManager").invoke(lp);
                Object user = userManager.getClass().getMethod("getUser", UUID.class).invoke(userManager, p.getUniqueId());
                if (user == null) return false;
                Object cachedData = user.getClass().getMethod("getCachedData").invoke(user);
                Object permData = cachedData.getClass().getMethod("getPermissionData").invoke(cachedData);
                Object result = permData.getClass().getMethod("checkPermission", String.class).invoke(permData, permission);
                Object value = result.getClass().getMethod("asBoolean").invoke(result);
                return value instanceof Boolean && ((Boolean) value).booleanValue();
            } catch (Throwable ignored) {}
            return false;
        }
    }
}
