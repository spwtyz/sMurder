package me.spwtyz.murder.rooms;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import me.spwtyz.murder.Arena;
import me.spwtyz.murder.Arenas;
import me.spwtyz.murder.Main;
import me.spwtyz.murder.GameModeType;

public class RoomManager {

    private final Main plugin;

    private final Map<String, Room> rooms = new HashMap<>();
    private final Map<UUID, Room> playerRooms = new HashMap<>();
    private final Map<UUID, Room> pendingRoomRename = new HashMap<>();

    public RoomManager(Main plugin) {
        this.plugin = plugin;
    }

    public Room createRoom(Player owner, Arena arena) {
        String id = generateId();

        Room room = new Room(id, owner, arena);

        rooms.put(id, room);
        playerRooms.put(owner.getUniqueId(), room);

        arena.owner = owner;
        arena.persistentRoom = true;
        plugin.setPlayerState(owner, me.spwtyz.murder.PlayerState.ROOM_LOBBY);

        return room;
    }


    public Room createPublicRoom(String id, Arena arena, GameModeType mode) {
        if (id == null || id.trim().isEmpty()) id = generatePublicId();
        Room room = new Room(id, null, arena);
        room.setFixedPublicRoom(true);
        room.setModeLocked(true);
        room.forceGameMode(mode == null ? GameModeType.NORMAL : mode);
        room.setPrivateRoom(false);
        rooms.put(id, room);
        if (arena != null) {
            arena.owner = null;
            arena.persistentRoom = true;
            arena.setGameMode(room.getGameMode());
        }
        return room;
    }

    public void savePublicRooms() {
        if (plugin == null) return;
        plugin.getConfig().set("public-rooms", null);
        int i = 1;
        for (Room room : rooms.values()) {
            if (room == null || !room.isFixedPublicRoom() || room.getArena() == null) continue;
            String path = "public-rooms." + i;
            plugin.getConfig().set(path + ".id", room.getId());
            plugin.getConfig().set(path + ".mode", room.getGameMode().name());
            plugin.getConfig().set(path + ".max-players", room.getArena().getMaxPlayers());
            i++;
        }
        plugin.saveConfig();
    }

    public void loadPublicRooms() {
        if (plugin == null || !plugin.getConfig().contains("public-rooms")) return;
        org.bukkit.configuration.ConfigurationSection sec = plugin.getConfig().getConfigurationSection("public-rooms");
        if (sec == null) return;
        for (String key : sec.getKeys(false)) {
            String id = sec.getString(key + ".id", "PUBLIC-" + key);
            String modeName = sec.getString(key + ".mode", "NORMAL");
            GameModeType mode = GameModeType.NORMAL;
            try { mode = plugin.parsePublicRoomMode(modeName); } catch (Exception ignored) {}
            String template = plugin.getRandomAllowedPublicRoomMap(mode);
            if (template == null || template.trim().isEmpty()) {
                plugin.getLogger().warning("[sMurder] Sala publica " + id + " nao carregou: nenhum mapa liberado para " + mode.name());
                continue;
            }
            Arena arena = new Arena(id, template, plugin);
            arena.maxPlayers = sec.getInt(key + ".max-players", mode == GameModeType.RANKED ? plugin.getConfig().getInt("ranked.public-room-max-players", 20) : plugin.SpawnSizeByName(template));
            Arenas.addArena(arena);
            Room created = createPublicRoom(id, arena, mode);
            created.setSelectedMapName(template);
        }
    }

    public void deleteRoom(Room room) {
        if (room == null || room.getArena() == null) return;

        Arena arena = room.getArena();

        for (Player p : new java.util.ArrayList<Player>(arena.getPlayers())) {
            playerRooms.remove(p.getUniqueId());
            plugin.setPlayerState(p, me.spwtyz.murder.PlayerState.MAIN_LOBBY);
            arena.removePlayer(p, "leave");
        }

        rooms.remove(room.getId());
        arena.persistentRoom = false;
        arena.owner = null;
        Main.getInstance().playerArena.values().removeIf(a -> a == arena);
        me.spwtyz.murder.Arenas.removeArena(arena);
    }

    public void addPlayerToRoom(Player player, Room room) {
        if (player == null || room == null) return;

        if (room.isBanned(player)) {
            player.sendMessage("§cVocê está banido desta sala.");
            return;
        }

        plugin.setPlayerState(player, me.spwtyz.murder.PlayerState.ROOM_LOBBY);
        room.getArena().addPlayer(player);
        playerRooms.put(player.getUniqueId(), room);
    }

    public void removePlayer(Player player) {
        if (player == null) return;

        Room room = getRoom(player);

        if (room == null) return;

        Arena arena = room.getArena();
        if (arena != null) {
            // Não transfere liderança automaticamente. O dono original continua líder da sala.
            arena.removePlayer(player, "leave");
        }

        playerRooms.remove(player.getUniqueId());
        plugin.setPlayerState(player, me.spwtyz.murder.PlayerState.MAIN_LOBBY);

        // Não deleta a sala automaticamente quando fica vazia.
        // O líder original continua sendo dono e a sala só fecha pelo botão Fechar Sala.
    }


    public void transferOwner(Room room, Player newOwner) {
        if (room == null) return;
        room.setOwner(newOwner);

        if (room.getArena() != null) {
            room.getArena().owner = newOwner;
        }

        if (newOwner != null) {
            playerRooms.put(newOwner.getUniqueId(), room);
        }
    }

    public Room getRoom(Player player) {
        if (player == null) return null;
        return playerRooms.get(player.getUniqueId());
    }

    public Room getRoom(String id) {
        return rooms.get(id);
    }

    public Room getRoomByArena(Arena arena) {
        if (arena == null) return null;
        for (Room room : rooms.values()) {
            if (room != null && room.getArena() == arena) return room;
        }
        return null;
    }


    public String getPublicRoomDisplayName(Room room) {
        if (room == null) return "Sala";
        if (room.isFixedPublicRoom()) {
            return room.getGameMode().getDisplayName() + " #" + getFixedPublicRoomIndex(room);
        }
        if (!room.isMainRoom()) {
            return room.getDisplayName();
        }
        Arena arena = room.getArena();
        return getPublicArenaDisplayName(arena);
    }

    public int getFixedPublicRoomIndex(Room target) {
        int index = 1;
        for (Room room : rooms.values()) {
            if (room == null || !room.isFixedPublicRoom()) continue;
            if (room == target) return index;
            index++;
        }
        return 1;
    }

    public String getPublicArenaDisplayName(Arena arena) {
        int index = 1;
        for (Arena a : Arenas.getArenas()) {
            if (a == null) continue;
            if (a.getName() != null && a.getName().toUpperCase().startsWith("ROOM-")) continue;
            Room r = getRoomByArena(a);
            if (r != null && !r.isMainRoom()) continue;
            if (a == arena) {
                return "Pública #" + index;
            }
            index++;
        }
        return "Pública #1";
    }

    public Collection<Room> getRooms() {
        return rooms.values();
    }

    public void startRename(Player player, Room room) {
        if (player == null || room == null) return;
        pendingRoomRename.put(player.getUniqueId(), room);
    }

    public boolean isRenaming(Player player) {
        return player != null && pendingRoomRename.containsKey(player.getUniqueId());
    }

    public Room consumeRename(Player player) {
        if (player == null) return null;
        return pendingRoomRename.remove(player.getUniqueId());
    }

    public void cancelRename(Player player) {
        if (player != null) pendingRoomRename.remove(player.getUniqueId());
    }

    public boolean isInRoom(Player player) {
        return getRoom(player) != null;
    }

    private String generatePublicId() {
        int i = 1;
        String id;
        do {
            id = "PUBLIC-" + i++;
        } while (rooms.containsKey(id));
        return id;
    }

    private String generateId() {
        String id;

        do {
            id = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        } while (rooms.containsKey(id));

        return id;
    }
}
