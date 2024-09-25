package groundbreaking.mychat.commands;

import groundbreaking.mychat.MyChat;
import groundbreaking.mychat.utils.ConfigValues;
import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SocialSpyCommandExecutor implements CommandExecutor, TabCompleter {

    private final ConfigValues configValues;

    @Getter
    private static final List<String> listening = new ArrayList<>();

    public SocialSpyCommandExecutor(MyChat plugin) {
        this.configValues = plugin.getPluginConfig();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(configValues.getPlayerOnly());
            return true;
        }

        if (!sender.hasPermission("mychat.socialspy")) {
            sender.sendMessage(configValues.getNoPermissionMessage());
            return true;
        }

        final String senderName = sender.getName();

        if (listening.contains(senderName)) {
            listening.remove(sender.getName());
            sender.sendMessage(configValues.getSocialspyDisabled());
        }
        else {
            listening.add(sender.getName());
            sender.sendMessage(configValues.getSocialspyEnabled());
        }

        return true;
    }

    public static void removeFromListening(String name) {
        listening.remove(name);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
