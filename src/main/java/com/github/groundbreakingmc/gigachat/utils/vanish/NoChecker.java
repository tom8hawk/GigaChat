package com.github.groundbreakingmc.gigachat.utils.vanish;

import org.bukkit.entity.Player;

public final class NoChecker implements VanishChecker {

    @Override
    public boolean isVanished(final Player player) {
        return false;
    }
}
