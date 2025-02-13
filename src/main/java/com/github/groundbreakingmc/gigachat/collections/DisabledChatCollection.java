package com.github.groundbreakingmc.gigachat.collections;

import org.bukkit.Bukkit;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class DisabledChatCollection {

    private static final Set<UUID> disabled = new HashSet<>();

    public static boolean contains(final UUID targetUUID) {
        if (disabled.isEmpty()) {
            return false;
        }

        System.out.println(Bukkit.getPlayer(targetUUID).getName() +  " contains " + disabled.contains(targetUUID));
        return disabled.contains(targetUUID);
    }

    public static boolean add(final UUID targetUUID) {
        return disabled.add(targetUUID);
    }

    public static boolean remove(final UUID targetUUID) {
        return disabled.remove(targetUUID);
    }
}
