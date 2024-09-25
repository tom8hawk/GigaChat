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
        colorCodesPermissions.put("mychat.color.black", ChatColor.BLACK);
        colorCodesPermissions.put("mychat.color.dark_blue", ChatColor.DARK_BLUE);
        colorCodesPermissions.put("mychat.color.dark_green", ChatColor.DARK_GREEN);
        colorCodesPermissions.put("mychat.color.dark_aqua", ChatColor.DARK_AQUA);
        colorCodesPermissions.put("mychat.color.dark_red", ChatColor.DARK_RED);
        colorCodesPermissions.put("mychat.color.dark_purple", ChatColor.DARK_PURPLE);
        colorCodesPermissions.put("mychat.color.gold", ChatColor.GOLD);
        colorCodesPermissions.put("mychat.color.gray", ChatColor.GRAY);
        colorCodesPermissions.put("mychat.color.dark_gray", ChatColor.DARK_GRAY);
        colorCodesPermissions.put("mychat.color.blue", ChatColor.BLUE);
        colorCodesPermissions.put("mychat.color.green", ChatColor.GREEN);
        colorCodesPermissions.put("mychat.color.aqua", ChatColor.AQUA);
        colorCodesPermissions.put("mychat.color.red", ChatColor.RED);
        colorCodesPermissions.put("mychat.color.light_purple", ChatColor.LIGHT_PURPLE);
        colorCodesPermissions.put("mychat.color.yellow", ChatColor.YELLOW);
        colorCodesPermissions.put("mychat.color.white", ChatColor.WHITE);
        colorStylesPermissions.put("mychat.style.obfuscated", ChatColor.MAGIC);
        colorStylesPermissions.put("mychat.style.bold", ChatColor.BOLD);
        colorStylesPermissions.put("mychat.style.strikethrough", ChatColor.STRIKETHROUGH);
        colorStylesPermissions.put("mychat.style.underline", ChatColor.UNDERLINE);
        colorStylesPermissions.put("mychat.style.italic", ChatColor.ITALIC);
        colorStylesPermissions.put("mychat.style.reset", ChatColor.RESET);

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
        if (player.hasPermission("mychat.hex")) {
            return LEGACY_COLORIZER.colorize(message);
        }
        Matcher matcher = colorPattern.matcher(message);

        while (matcher.find()) {
            String code = matcher.group(1);
            String colorPerm = "mychat.color." + colorCodesMap.get(code);
            String stylePerm = "mychat.style." + colorStylesMap.get(code);
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
