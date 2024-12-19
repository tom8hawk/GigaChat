package com.github.groundbreakingmc.gigachat.utils.vanish;

import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import java.util.List;

public final class SuperVanishChecker implements VanishChecker {

    @Override
    public boolean isVanished(final Player player) {
        final List<MetadataValue> list = player.getMetadata("vanished");
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).asBoolean()) {
                return true;
            }
        }

        return false;
    }
}
