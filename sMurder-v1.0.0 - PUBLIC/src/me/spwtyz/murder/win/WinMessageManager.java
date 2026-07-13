package me.spwtyz.murder.win;

import java.util.List;

import org.bukkit.entity.Player;

import me.spwtyz.murder.Arena;
import me.spwtyz.murder.GameModeType;
import me.spwtyz.murder.Main;
import me.spwtyz.murder.PlayerType;
import me.spwtyz.murder.Utils;
import me.spwtyz.murder.cosmetics.CosmeticEffectManager;

/**
 * Centraliza as mensagens de vitoria por modo de jogo.
 * Assim novos modos nao precisam ficar misturados dentro do Arena.java.
 */
public class WinMessageManager {

    private final Main plugin;

    public WinMessageManager(Main plugin) {
        this.plugin = plugin;
    }

    public String getMessageKey(Arena arena, String resultType) {
        GameModeType mode = arena == null ? GameModeType.NORMAL : arena.getGameMode();

        if (mode == GameModeType.TNT_TAG) {
            return "tnttag-won-message";
        }

        if (mode == GameModeType.ALL_MURDER) {
            return "all-murder-won-message";
        }

        if (mode == GameModeType.RANKED) {
            return resultType != null && resultType.equalsIgnoreCase("m")
                    ? "ranked-murderer-won-message"
                    : "ranked-players-won-message";
        }

        if (mode == GameModeType.HIDE_AND_SEEK) {
            return resultType != null && resultType.equalsIgnoreCase("m")
                    ? "hideandseek-seekers-won-message"
                    : "hideandseek-hiders-won-message";
        }

        return resultType != null && resultType.equalsIgnoreCase("m")
                ? "murderer-won-message"
                : "innocents-won-message";
    }

    public void send(Player player, Arena arena, String resultType, String murderer, String detective, String hero, String winner) {
        if (player == null || arena == null) return;

        List<String> list = plugin.messages.getConfig().getStringList(getMessageKey(arena, resultType));
        if (list == null || list.isEmpty()) {
            list = plugin.messages.getConfig().getStringList(resultType != null && resultType.equalsIgnoreCase("m")
                    ? "murderer-won-message"
                    : "innocents-won-message");
        }

        String mode = arena.getRoomModeNamePlain();
        String map = arena.getName();

        for (String line : list) {
            player.sendMessage(Utils.FormatText(player, line
                    .replaceAll("%murderer%", safe(murderer))
                    .replaceAll("%detective%", safe(detective))
                    .replaceAll("%winner%", safe(winner))
                    .replaceAll("%mode%", safe(mode))
                    .replaceAll("%map%", safe(map))
                    .replaceAll("%hero%", safe(hero))));
        }

        // Faz os cosmeticos da loja funcionarem no fim da partida.
        // O efeito so toca para quem realmente venceu, nao para todos que receberam a mensagem.
        if (isWinner(player, arena, resultType, winner)) {
            CosmeticEffectManager.playVictoryEffect(plugin, player);
        }
    }

    private boolean isWinner(Player player, Arena arena, String resultType, String winnerText) {
        if (player == null || arena == null) return false;

        GameModeType mode = arena.getGameMode();
        String type = resultType == null ? "" : resultType;

        if (winnerText != null && winnerText.toLowerCase().contains(player.getName().toLowerCase())) {
            return true;
        }

        PlayerType role = arena.getType(player);

        if (mode == GameModeType.TNT_TAG || mode == GameModeType.ALL_MURDER) {
            return winnerText != null && winnerText.toLowerCase().contains(player.getName().toLowerCase());
        }

        if (type.equalsIgnoreCase("m")) {
            return role == PlayerType.Murderer;
        }

        return role == PlayerType.Innocents || role == PlayerType.Detective;
    }

    private String safe(String value) {
        return value == null || value.isEmpty() ? "Nenhum" : value;
    }
}
