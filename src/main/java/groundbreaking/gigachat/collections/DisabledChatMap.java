package groundbreaking.gigachat.collections;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

public final class DisabledChatMap {

    private static final List<String> disabled = new ObjectArrayList<>();

    public static boolean contains(final String name) {
        if (disabled.isEmpty()) {
            return false;
        }

        return disabled.contains(name);
    }

    public static boolean add(final String name) {
        if (disabled.contains(name)) {
            return false;
        }

        return disabled.add(name);
    }

    public static boolean remove(final String name) {
        if (disabled.isEmpty()) {
            return false;
        }

        return disabled.remove(name);
    }
}
