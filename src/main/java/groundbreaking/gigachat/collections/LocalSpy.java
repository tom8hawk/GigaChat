package groundbreaking.gigachat.collections;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class LocalSpy {

    private static final Set<String> PLAYERS = new HashSet<>();

    public static void add(final String name) {
        PLAYERS.add(name);
    }

    public static void remove(final String name) {
        if (PLAYERS.isEmpty()) {
            return;
        }

        PLAYERS.remove(name);
    }

    public static boolean contains(final String name) {
        if (PLAYERS.isEmpty()) {
            return false;
        }

        return PLAYERS.contains(name);
    }

    public static List<Player> getAll() {
        final List<Player> players = new ArrayList<>();
        for (final String playerName : PLAYERS) {
            final Player player = Bukkit.getPlayer(playerName);
            if (player != null) {
                players.add(player);
            }
        }

        return players;
    }
}
