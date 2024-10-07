package groundbreaking.gigachat.collections;

import java.util.ArrayList;
import java.util.List;

public final class DisabledChat {

    private static final List<String> disabled = new ArrayList<>();

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
