package com.github.groundbreakingmc.gigachat.collections;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;
import java.util.UUID;

public final class DisabledChatCollection {

    private static final List<UUID> disabled = new ObjectArrayList<>();

    public static boolean contains(final UUID targetUUID) {
        if (disabled.isEmpty()) {
            return false;
        }

        return disabled.contains(targetUUID);
    }

    public static boolean add(final UUID targetUUID) {
        if (disabled.contains(targetUUID)) {
            return false;
        }

        return disabled.add(targetUUID);
    }

    public static boolean remove(final UUID targetUUID) {
        if (disabled.isEmpty()) {
            return false;
        }

        return disabled.remove(targetUUID);
    }
}
