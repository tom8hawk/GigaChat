package groundbreaking.gigachat.collections;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public final class SocialSpy {

    private static final Set<String> listening = new HashSet<>();

    public static boolean contains(final String name) {
        return listening.contains(name);
    }

    public static void add(final String name) {
        listening.add(name);
    }

    public static void remove(final String name) {
        listening.remove(name);
    }

    public static void sendAll(final Player sender, final Player recipient, final String message) {
        for (final String name : listening) {
            final Player player = Bukkit.getPlayer(name);
            if (player != null && player != sender && player != recipient) {
                player.sendMessage(message);
            }
        }
    }
}
