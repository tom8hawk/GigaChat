package com.github.groundbreakingmc.gigachat.collections;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.Set;
import java.util.UUID;

public final class AutoMessagesCollection {

    private static final Set<UUID> players = new ObjectOpenHashSet<>();

    public static void add(final UUID targetUUID) {
        players.add(targetUUID);
    }

    public static void remove(final UUID targetUUID) {
        players.remove(targetUUID);
    }

    public static boolean contains(final UUID targetUUID) {
        return players.contains(targetUUID);
    }
}
