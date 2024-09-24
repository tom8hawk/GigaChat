package groundbreaking.mychat.commands;

import groundbreaking.mychat.MyChat;
import groundbreaking.mychat.utils.Config;
import groundbreaking.mychat.utils.colorizer.IColorizer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public final class IgnoreCommandExecutor implements CommandExecutor, TabCompleter {

    private final MyChat plugin;
    private final Config pluginConfig;
    private final IColorizer colorizer;

    private final String[] placeholders = { "{from-prefix}", "{from-name}", "{from-suffix}", "{to-prefix}", "{to-name}", "{to-suffix}", "{message}" };

    private static final HashMap<String, Set<String>> ignored = new HashMap<>();

    public IgnoreCommandExecutor(MyChat plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
        this.colorizer = plugin.getColorizer();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(pluginConfig.getPlayerOnly());
            return true;
        }

        if (!sender.hasPermission("chat.socialspy")) {
            sender.sendMessage(pluginConfig.getNoPermissionMessage());
            return true;
        }

        final Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            sender.sendMessage(pluginConfig.getPlayerNotFoundMessage());
            return true;
        }

        if (target == sender) {
            sender.sendMessage(pluginConfig.getCannotIgnoreSelf());
            return true;
        }

        final String senderName = sender.getName();
        final String targetName = target.getName();

        if (ignored.get(senderName).contains(targetName)) {
            ignored.get(senderName).remove(targetName);
            sender.sendMessage(pluginConfig.getIsNotMoreIgnored().replace("%player%", targetName));
        }
        else {
            ignored.get(senderName).add(targetName);
            sender.sendMessage(pluginConfig.getIsNowIgnoring().replace("%player%", targetName));
        }

        return true;
    }

    public static boolean ignores(String playerName, String targetName) {
        return ignored.get(playerName).contains(targetName);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            return null;
        }

        return Collections.emptyList();
    }
}
