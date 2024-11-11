package groundbreaking.gigachat.collections;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;
import java.util.UUID;

public final class ReplyCollection {

    private static final Map<UUID, UUID> reply = new Object2ObjectOpenHashMap<>();

    public static UUID getRecipientName(final UUID uuid) {
        return reply.get(uuid);
    }

    public static void add(final UUID uuid, final UUID targetUuid) {
        reply.put(uuid, targetUuid);
    }

    public static void remove(final UUID name) {
        reply.remove(name);
    }

    public static void removeFromAll(final UUID name) {
        reply.remove(name);
        for (final UUID key : reply.keySet()) {
            final UUID targetUuid = reply.get(key);
            if (targetUuid.equals(name)) {
                reply.remove(key);
            }
        }
    }
}
