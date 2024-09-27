package groundbreaking.mychat.utils;

import groundbreaking.mychat.utils.colorizer.IColorizer;
import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import it.unimi.dsi.fastutil.chars.CharSet;
import lombok.Setter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Utils {

    private static final CharSet COLOR_CODES = new CharOpenHashSet(new char[]{
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f',
            'A', 'B', 'C', 'D', 'E', 'F'
    });

    private static final CharSet STYLE_CODES = new CharOpenHashSet(new char[]{
            'k', 'l', 'm', 'n', 'o', 'r', 'x',
            'K', 'L', 'M', 'N', 'O', 'R', 'X'
    });

    @Setter
    private static IColorizer chatColorizer, privateColorizer;
    private static final char COLOR_CHAR = '&';

    public static String replacePlaceholders(Player player, String message) {
        if (PlaceholderAPI.containsPlaceholders(message)) {
            return PlaceholderAPI.setPlaceholders(player, message);
        }
        return message;
    }

    public static String colorizeChatMessage(Player player, String message) {
        if (player.hasPermission("mychat.chat.hex")) {
            return chatColorizer.colorize(message);
        }

        return formatByPerm(player, message, "chat");
    }

    public static String colorizePrivateMessage(Player player, String message) {
        if (player.hasPermission("mychat.private.hex")) {
            return privateColorizer.colorize(message);
        }

        return formatByPerm(player, message, "private");
    }

    public static String formatByPerm(Player player, String message, String place) {
        char[] letters = message.toCharArray();
        for (int i = 0; i < letters.length; i++) {
            if (letters[i] == COLOR_CHAR) {
                final char code = letters[i + 1];
                if (COLOR_CODES.contains(code) && player.hasPermission("mychat." + place + ".color." + code)) {
                    letters[i++] = '§';
                    letters[i] = Character.toLowerCase(letters[i]);
                }
                else if (STYLE_CODES.contains(code) && player.hasPermission("mychat." + place + ".style." + code)) {
                    letters[i++] = '§';
                    letters[i] = Character.toLowerCase(letters[i]);
                }
            }
        }

        return new String(letters);
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
