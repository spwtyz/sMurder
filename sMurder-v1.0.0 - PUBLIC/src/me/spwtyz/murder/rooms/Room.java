package me.spwtyz.murder.rooms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

import me.spwtyz.murder.Arena;
import me.spwtyz.murder.GameModeType;

public class Room {

    private final String id;
    private final String originalOwnerName;
    private Player owner;
    private Arena arena;

    private String password;
    private boolean privateRoom;
    private GameModeType gameMode = GameModeType.NORMAL;
    private final Map<UUID, String> mapVotes = new HashMap<>();
    private String selectedMapName;
    private String customName;
    private final Set<RoomModifier> modifiers = new HashSet<>();
    private final Set<UUID> bannedPlayers = new HashSet<UUID>();
    private final Map<UUID, String> bannedNames = new HashMap<UUID, String>();
    private final Set<UUID> roomModerators = new HashSet<UUID>();
    private boolean fixedPublicRoom = false;
    private boolean modeLocked = false;

    public Room(String id, Player owner, Arena arena) {
        this.id = id;
        this.originalOwnerName = owner != null ? owner.getName() : "Desconhecido";
        this.owner = owner;
        this.arena = arena;
        this.privateRoom = false;
        this.password = null;
    }

    public String getId() {
        return id;
    }

    public Player getOwner() {
        return owner;
    }

    public String getDisplayName() {
        if (isFixedPublicRoom()) {
            try {
                if (me.spwtyz.murder.Main.getInstance() != null && me.spwtyz.murder.Main.getInstance().roomManager != null) {
                    return me.spwtyz.murder.Main.getInstance().roomManager.getPublicRoomDisplayName(this);
                }
            } catch (Exception ignored) {}
            return getGameMode().getDisplayName() + " #1";
        }
        if (isMainRoom()) {
            return "Sala Principal";
        }
        if (customName != null && !customName.trim().isEmpty()) {
            return customName;
        }
        String ownerName = owner != null ? owner.getName() : originalOwnerName;
        if (ownerName == null || ownerName.trim().isEmpty()) {
            ownerName = "Desconhecido";
        }
        return "Privada de " + ownerName;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        if (customName == null || customName.trim().isEmpty()) {
            this.customName = null;
            return;
        }
        this.customName = customName.trim();
    }

    public void resetCustomName() {
        this.customName = null;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public Arena getArena() {
        return arena;
    }

    public void setArena(Arena arena) {
        this.arena = arena;
    }

    public GameModeType getGameMode() {
        return gameMode == null ? GameModeType.NORMAL : gameMode;
    }

    public void setGameMode(GameModeType gameMode) {
        if (modeLocked) return;
        this.gameMode = gameMode == null ? GameModeType.NORMAL : gameMode;
    }

    public void forceGameMode(GameModeType gameMode) {
        this.gameMode = gameMode == null ? GameModeType.NORMAL : gameMode;
    }

    public boolean isFixedPublicRoom() {
        return fixedPublicRoom;
    }

    public void setFixedPublicRoom(boolean fixedPublicRoom) {
        this.fixedPublicRoom = fixedPublicRoom;
        if (fixedPublicRoom) {
            this.privateRoom = false;
            this.password = null;
        }
    }

    public boolean isModeLocked() {
        return modeLocked;
    }

    public void setModeLocked(boolean modeLocked) {
        this.modeLocked = modeLocked;
    }

    // =========================
    // MAP VOTING POR SALA
    // =========================

    public void voteMap(Player player, String mapName) {
        if (player == null || mapName == null || mapName.isEmpty()) return;
        mapVotes.put(player.getUniqueId(), mapName);
        selectedMapName = getWinningMapName();
    }

    public String getVote(Player player) {
        if (player == null) return null;
        return mapVotes.get(player.getUniqueId());
    }

    public int getVotesForMap(String mapName) {
        if (mapName == null) return 0;
        int amount = 0;
        for (String vote : mapVotes.values()) {
            if (mapName.equalsIgnoreCase(vote)) amount++;
        }
        return amount;
    }

    public boolean hasMapVotes() {
        return !mapVotes.isEmpty();
    }

    public boolean hasVotedMap(Player player) {
        return player != null && mapVotes.containsKey(player.getUniqueId());
    }

    public String getPlayerVote(Player player) {
        return getVote(player);
    }

    public String getWinningMapName() {
        String winner = selectedMapName;
        int highest = -1;

        for (String vote : mapVotes.values()) {
            int count = getVotesForMap(vote);
            if (count > highest) {
                highest = count;
                winner = vote;
            }
        }

        return winner;
    }

    public String getSelectedMapName() {
        String winner = getWinningMapName();
        return winner == null || winner.isEmpty() ? (arena != null ? arena.getTemplateName() : "Nenhum") : winner;
    }

    public void setSelectedMapName(String selectedMapName) {
        this.selectedMapName = selectedMapName;
    }

    public void clearMapVotes() {
        mapVotes.clear();
    }

    public boolean isPrivateRoom() {
        return privateRoom;
    }

    public void setPrivateRoom(boolean privateRoom) {
        this.privateRoom = privateRoom;
    }

    public boolean hasPassword() {
        return password != null && !password.isEmpty();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;

        if (password != null && !password.isEmpty()) {
            this.privateRoom = true;
        }
    }

    public boolean checkPassword(String input) {
        if (!hasPassword()) return true;
        return password.equals(input);
    }

    // =========================
    // MODIFICADORES POR SALA
    // =========================

    public boolean hasModifier(RoomModifier modifier) {
        return modifier != null && modifiers.contains(modifier);
    }

    public boolean toggleModifier(RoomModifier modifier) {
        if (modifier == null) return false;

        if (modifiers.contains(modifier)) {
            modifiers.remove(modifier);
            return false;
        }

        modifiers.add(modifier);
        return true;
    }

    public Set<RoomModifier> getModifiers() {
        return modifiers;
    }

    // =========================
    // BANIDOS / MODERADORES DA SALA
    // =========================

    public boolean isBanned(Player player) {
        return player != null && bannedPlayers.contains(player.getUniqueId());
    }

    public void banPlayer(Player player) {
        if (player == null) return;
        bannedPlayers.add(player.getUniqueId());
        bannedNames.put(player.getUniqueId(), player.getName());
        roomModerators.remove(player.getUniqueId());
    }

    public void unbanPlayer(Player player) {
        if (player == null) return;
        bannedPlayers.remove(player.getUniqueId());
        bannedNames.remove(player.getUniqueId());
    }

    public void unbanPlayer(UUID uuid) {
        if (uuid == null) return;
        bannedPlayers.remove(uuid);
        bannedNames.remove(uuid);
    }

    public Set<UUID> getBannedPlayers() {
        return bannedPlayers;
    }

    public Map<UUID, String> getBannedNames() {
        return bannedNames;
    }

    public boolean isModerator(Player player) {
        return player != null && roomModerators.contains(player.getUniqueId());
    }

    public boolean toggleModerator(Player player) {
        if (player == null) return false;
        UUID id = player.getUniqueId();
        if (roomModerators.contains(id)) {
            roomModerators.remove(id);
            return false;
        }
        if (isBanned(player)) return false;
        roomModerators.add(id);
        return true;
    }

    public void removeModerator(Player player) {
        if (player != null) roomModerators.remove(player.getUniqueId());
    }

    public Set<UUID> getRoomModerators() {
        return roomModerators;
    }

    public boolean isMainRoom() {
        if (fixedPublicRoom) return true;
        if (owner == null) return true;
        if (id == null) return false;
        return id.equalsIgnoreCase("principal")
                || id.equalsIgnoreCase("main")
                || id.equalsIgnoreCase("fixa")
                || id.equalsIgnoreCase("default");
    }
}
