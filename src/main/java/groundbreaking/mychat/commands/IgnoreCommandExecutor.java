package groundbreaking.mychat.commands;

import groundbreaking.mychat.MyChat;
import groundbreaking.mychat.utils.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public final class IgnoreCommandExecutor implements CommandExecutor, TabCompleter {

    private final ConfigValues configValues;

    private static final HashMap<String, List<String>> ignored = new HashMap<>();

    public IgnoreCommandExecutor(MyChat plugin) {
        this.configValues = plugin.getConfigValues();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(configValues.getPlayerOnly());
            return true;
        }

        if (!sender.hasPermission("mychat.ignore")) {
            sender.sendMessage(configValues.getNoPermissionMessage());
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(configValues.getIgnoreUsageError());
            return true;
        }

        final Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            sender.sendMessage(configValues.getPlayerNotFoundMessage());
            return true;
        }

        if (target == sender) {
            sender.sendMessage(configValues.getCannotIgnoreSelf());
            return true;
        }

        final String senderName = sender.getName();
        final String targetName = target.getName();

        if (!ignored.containsKey(senderName)) {
            ignored.put(senderName, new ArrayList<>());
        }

        if (ignored.get(senderName).contains(targetName)) {
            ignored.get(senderName).remove(targetName);
            sender.sendMessage(configValues.getIsNotMoreIgnored().replace("{player}", targetName));
        }
        else {
            ignored.get(senderName).add(targetName);
            sender.sendMessage(configValues.getIsNowIgnoring().replace("{player}", targetName));
        }

        return true;
    }

    public static boolean ignores(String playerName, String targetName) {
        if (!ignored.containsKey(playerName)) {
            ignored.put(playerName, new ArrayList<>());
        }

        return ignored.get(playerName).contains(targetName);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            final String input = args[0];
            final List<String> players = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                final String playerName = player.getName();
                if (playerName.startsWith(input)) {
                    players.add(playerName);
                }
            }
            return players;
        }

        return Collections.emptyList();
    }
}
