package groundbreaking.gigachat.collections;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class LocalSpyCollection {

    private static final Set<UUID> PLAYERS = new ObjectOpenHashSet<>();

    public static void add(final UUID playerUUID) {
        PLAYERS.add(playerUUID);
    }

    public static void remove(final UUID playerUUID) {
        if (PLAYERS.isEmpty()) {
            return;
        }

        PLAYERS.remove(playerUUID);
    }

    public static boolean contains(final UUID playerUUID) {
        if (PLAYERS.isEmpty()) {
            return false;
        }

        return PLAYERS.contains(playerUUID);
    }

    public static List<Player> getAll() {
        final List<Player> players = new ArrayList<>();
        for (final UUID playerUuid : PLAYERS) {
            final Player player = Bukkit.getPlayer(playerUuid);
            if (player != null) {
                players.add(player);
            }
        }

        return players;
    }
}
