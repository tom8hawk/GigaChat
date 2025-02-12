package com.github.groundbreakingmc.gigachat.utils;

import com.github.groundbreakingmc.gigachat.utils.configvalues.Messages;
import lombok.experimental.UtilityClass;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@UtilityClass
public final class Utils {

    public static String replacePlaceholders(final CommandSender sender, final String string) {
        if (sender instanceof final Player player) {
            return PlaceholderAPI.setPlaceholders(player, string);
        }

        return PlaceholderAPI.setPlaceholders(null, string);
    }

    public static String replacePlaceholders(final Player player, final String string) {
        return PlaceholderAPI.setPlaceholders(player, string);
    }

    public static String getTime(final int totalSeconds) {
        final int hours = totalSeconds / 3600;
        final int minutes = (totalSeconds % 3600) / 60;
        final int seconds = totalSeconds % 60;

        final StringBuilder result = new StringBuilder();

        if (hours > 0) {
            result.append(hours).append(Messages.getHours());
        }

        if (minutes > 0 || hours > 0) {
            result.append(minutes).append(Messages.getMinutes());
        }

        if (seconds > 0) {
            result.append(seconds).append(Messages.getSeconds());
        }

        return result.toString();
    }
}
