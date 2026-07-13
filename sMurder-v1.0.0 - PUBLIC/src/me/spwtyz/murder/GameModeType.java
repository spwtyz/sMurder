package me.spwtyz.murder;

public enum GameModeType {
    NORMAL("Normal"),
    ALL_MURDER("Todos Assassinos"),
    TNT_TAG("TntTag"),
    RANKED("Ranked"),
    HIDE_AND_SEEK("Esconde-Esconde"),
    SABOTAGE("AMONG US");

    private final String displayName;

    GameModeType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
