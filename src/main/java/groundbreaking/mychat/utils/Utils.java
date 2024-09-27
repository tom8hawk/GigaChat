package groundbreaking.mychat.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Utils {

    public static String replacePlaceholders(Player player, String message) {
        if (PlaceholderAPI.containsPlaceholders(message)) {
            return PlaceholderAPI.setPlaceholders(player, message);
        }
        return message;
    }

    public static String replaceEach(@Nullable String text, @NotNull String[] searchList, @NotNull String[] replacementList) {
        if (text != null && !text.isEmpty() && searchList.length != 0 && replacementList.length != 0) {
            if (searchList.length != replacementList.length) {
                throw new IllegalArgumentException("Search and replacement arrays must have the same length.");
            } else {
                StringBuilder result = new StringBuilder(text);

                for(int i = 0; i < searchList.length; ++i) {
                    String search = searchList[i];
                    String replacement = replacementList[i];

                    for(int start = 0; (start = result.indexOf(search, start)) != -1; start += replacement.length()) {
                        result.replace(start, start + search.length(), replacement);
                    }
                }

                return result.toString();
            }
        } else {
            return text;
        }
    }

    public static String getTime(int totalSeconds) {
        final int hours = totalSeconds / 3600;
        final int minutes = (totalSeconds % 3600) / 60;
        final int seconds = totalSeconds % 60;

        final StringBuilder result = new StringBuilder();

        if (hours > 0) {
            result.append(hours).append(ConfigValues.getHoursText());
        }

        if (minutes > 0 || hours > 0) {
            result.append(minutes).append(ConfigValues.getMinutesText());
        }

        result.append(seconds).append(ConfigValues.getSecondsText());

        return result.toString();
    }
}
