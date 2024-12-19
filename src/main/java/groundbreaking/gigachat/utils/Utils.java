package groundbreaking.gigachat.utils;

import groundbreaking.gigachat.utils.config.values.Messages;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public final class Utils {

    private Utils() {

    }

    public static String replacePlaceholders(final Player player, final String message) {
        if (PlaceholderAPI.containsPlaceholders(message)) {
            return PlaceholderAPI.setPlaceholders(player, message);
        }

        return message;
    }

    public static boolean startsWithIgnoreCase(final String input, final String completion) {
        if (completion == null || input == null) {
            return false;
        }

        return completion.regionMatches(true, 0, input, 0, input.length());
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

        result.append(seconds).append(Messages.getSeconds());

        return result.toString();
    }
}
