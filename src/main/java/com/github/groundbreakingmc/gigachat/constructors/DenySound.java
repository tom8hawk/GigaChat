package com.github.groundbreakingmc.gigachat.constructors;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public record DenySound(
        Sound sound,
        float volume,
        float pitch
) {
    public void play(final Player player) {
        final Location location = player.getLocation();
        player.playSound(location, this.sound, this.volume, this.pitch);
    }
}
