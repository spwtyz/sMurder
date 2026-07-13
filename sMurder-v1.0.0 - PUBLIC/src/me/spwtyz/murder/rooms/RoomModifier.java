package me.spwtyz.murder.rooms;

public enum RoomModifier {
    CURSED_GOLD("Cursed Gold"),
    SABOTAGE("AMONG US");

    private final String displayName;

    RoomModifier(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
