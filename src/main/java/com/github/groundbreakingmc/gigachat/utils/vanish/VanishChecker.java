package com.github.groundbreakingmc.gigachat.utils.vanish;

import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import java.util.List;

public final class VanishChecker {
    
    public boolean isVanished(final Player player) {
        final List<MetadataValue> metadata = player.getMetadata("vanished");
        return !metadata.isEmpty() && metadata.get(0).asBoolean();
    }
}
