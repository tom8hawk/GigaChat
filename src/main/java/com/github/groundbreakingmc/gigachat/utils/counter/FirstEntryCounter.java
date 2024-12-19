package com.github.groundbreakingmc.gigachat.utils.counter;

import org.bukkit.entity.Player;

public final class FirstEntryCounter implements Counter {

    @Override
    public long count(final Player player) {
        return (System.currentTimeMillis() - player.getFirstPlayed()) / 1000;
    }
}
