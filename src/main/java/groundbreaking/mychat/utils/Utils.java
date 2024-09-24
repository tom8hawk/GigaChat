package groundbreaking.mychat.utils;

import groundbreaking.mychat.utils.colorizer.LegacyColorizer;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final Pattern colorPattern = Pattern.compile("&([0-9a-fA-Fklmnor])");

    private static final Map<String, ChatColor> colorCodesPermissions = new HashMap<>();
    private static final Map<String, String> colorCodesMap = new HashMap<>();

    private static final Map<String, ChatColor> colorStylesPermissions = new HashMap<>();
    private static final Map<String, String> colorStylesMap = new HashMap<>();

    private static final LegacyColorizer LEGACY_COLORIZER = new LegacyColorizer();
    private static final char COLOR_CHAR = '&';

    static {
        colorCodesPermissions.put("pchat.color.black", ChatColor.BLACK);
        colorCodesPermissions.put("pchat.color.dark_blue", ChatColor.DARK_BLUE);
        colorCodesPermissions.put("pchat.color.dark_green", ChatColor.DARK_GREEN);
        colorCodesPermissions.put("pchat.color.dark_aqua", ChatColor.DARK_AQUA);
        colorCodesPermissions.put("pchat.color.dark_red", ChatColor.DARK_RED);
        colorCodesPermissions.put("pchat.color.dark_purple", ChatColor.DARK_PURPLE);
        colorCodesPermissions.put("pchat.color.gold", ChatColor.GOLD);
        colorCodesPermissions.put("pchat.color.gray", ChatColor.GRAY);
        colorCodesPermissions.put("pchat.color.dark_gray", ChatColor.DARK_GRAY);
        colorCodesPermissions.put("pchat.color.blue", ChatColor.BLUE);
        colorCodesPermissions.put("pchat.color.green", ChatColor.GREEN);
        colorCodesPermissions.put("pchat.color.aqua", ChatColor.AQUA);
        colorCodesPermissions.put("pchat.color.red", ChatColor.RED);
        colorCodesPermissions.put("pchat.color.light_purple", ChatColor.LIGHT_PURPLE);
        colorCodesPermissions.put("pchat.color.yellow", ChatColor.YELLOW);
        colorCodesPermissions.put("pchat.color.white", ChatColor.WHITE);
        colorStylesPermissions.put("pchat.style.obfuscated", ChatColor.MAGIC);
        colorStylesPermissions.put("pchat.style.bold", ChatColor.BOLD);
        colorStylesPermissions.put("pchat.style.strikethrough", ChatColor.STRIKETHROUGH);
        colorStylesPermissions.put("pchat.style.underline", ChatColor.UNDERLINE);
        colorStylesPermissions.put("pchat.style.italic", ChatColor.ITALIC);
        colorStylesPermissions.put("pchat.style.reset", ChatColor.RESET);

        colorCodesMap.put("0", "black");
        colorCodesMap.put("1", "dark_blue");
        colorCodesMap.put("2", "dark_green");
        colorCodesMap.put("3", "dark_aqua");
        colorCodesMap.put("4", "dark_red");
        colorCodesMap.put("5", "dark_purple");
        colorCodesMap.put("6", "gold");
        colorCodesMap.put("7", "gray");
        colorCodesMap.put("8", "dark_gray");
        colorCodesMap.put("9", "blue");
        colorCodesMap.put("a", "green");
        colorCodesMap.put("b", "aqua");
        colorCodesMap.put("c", "red");
        colorCodesMap.put("d", "light_purple");
        colorCodesMap.put("e", "yellow");
        colorCodesMap.put("f", "white");

        colorStylesMap.put("l", "bold");
        colorStylesMap.put("k", "obfuscated");
        colorStylesMap.put("m", "strikethrough");
        colorStylesMap.put("n", "underline");
        colorStylesMap.put("o", "italic");
        colorStylesMap.put("r", "reset");
    }

    public static String replacePlaceholders(Player player, String message) {
        if (PlaceholderAPI.containsPlaceholders(message)) {
            message = PlaceholderAPI.setPlaceholders(player, message);
        }
        return message;
    }

    public static String formatByPerm(Player player, String message) {
        if (player.hasPermission("pchat.style.hex")) {
            return LEGACY_COLORIZER.colorize(message);
        }
        Matcher matcher = colorPattern.matcher(message);

        while (matcher.find()) {
            String code = matcher.group(1);
            String colorPerm = "pchat.color." + colorCodesMap.get(code);
            String stylePerm = "pchat.style." + colorStylesMap.get(code);
            ChatColor color = colorCodesPermissions.get(colorPerm);
            ChatColor style = colorStylesPermissions.get(stylePerm);

            if (color != null && player.hasPermission(colorPerm)) {
                message = message.replace(COLOR_CHAR + code, color.toString());
            }
            if (style != null && player.hasPermission(stylePerm)) {
                message = message.replace(COLOR_CHAR + code, style.toString());
            }
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

    public static boolean isNumeric(@Nullable CharSequence cs) {
        if (cs != null && !cs.isEmpty()) {
            int sz = cs.length();

            for(int i = 0; i < sz; ++i) {
                if (!Character.isDigit(cs.charAt(i))) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public static String getTime(int totalSeconds) {
        final int hours = totalSeconds / 3600;
        final int minutes = (totalSeconds % 3600) / 60;
        final int seconds = totalSeconds % 60;

        final StringBuilder result = new StringBuilder();

        if (hours > 0) {
            result.append(hours).append(Config.getHoursText());
        }

        if (minutes > 0 || hours > 0) {
            result.append(minutes).append(Config.getMinutesText());
        }

        result.append(seconds).append(Config.getSecondsText());

        return result.toString();
    }
}
