package groundbreaking.gigachat.collections;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.Set;

public final class AutoMessagesCollection {

    private static final Set<String> players = new ObjectOpenHashSet<>();

    public static void add(final String name) {
        players.add(name);
    }

    public static void remove(final String name) {
        players.remove(name);
    }

    public static boolean contains(final String name) {
        return players.contains(name);
    }
}
