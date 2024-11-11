package groundbreaking.gigachat.collections;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public final class SocialSpyCollection {

    private static final Set<UUID> listening = new ObjectOpenHashSet<>();

    private SocialSpyCollection() {

    }

    public static boolean contains(final UUID uuid) {
        return listening.contains(uuid);
    }

    public static void add(final UUID uuid) {
        listening.add(uuid);
    }

    public static void remove(final UUID uuid) {
        listening.remove(uuid);
    }

    public static void sendAll(final Player sender, final Player recipient, final String message) {
        for (final UUID uuid : listening) {
            final Player player = Bukkit.getPlayer(uuid);
            if (player != null && player != sender && player != recipient) {
                player.sendMessage(message);
            }
        }
    }
}
