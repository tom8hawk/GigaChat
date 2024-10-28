package groundbreaking.gigachat.collections;

import java.util.HashMap;

public final class ReplyMap {

    private static final HashMap<String, String> reply = new HashMap<>();

    public static String getRecipientName(String name) {
        return reply.get(name);
    }

    public static void add(final String name, final String target) {
        reply.put(name, target);
    }

    public static void remove(final String name) {
        reply.remove(name);
    }

    public static void removeFromAll(final String name) {
        reply.remove(name);
        for (String key : reply.keySet()) {
            final String keyName = reply.get(key);
            if (keyName.equals(name)) {
                reply.remove(key);
            }
        }
    }
}
