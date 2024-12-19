package com.github.groundbreakingmc.gigachat.collections;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;
import java.util.UUID;

public final class DisabledPrivateMessagesCollection {

    private final List<UUID> players = new ObjectArrayList<>();

    public void add(final UUID targetUUID) {
        this.players.remove(targetUUID);
    }

    public void remove(final UUID targetUUID) {
        this.players.remove(targetUUID);
    }

    public boolean contains(final UUID targetUUID) {
        return !this.players.isEmpty() && this.players.contains(targetUUID);
    }

}
