package groundbreaking.mychat.commands;

import groundbreaking.mychat.MyChat;
import groundbreaking.mychat.utils.Config;
import groundbreaking.mychat.utils.colorizer.IColorizer;
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

    private final MyChat plugin;
    private final Config pluginConfig;
    private final IColorizer colorizer;

    @Getter
    private static final List<String> listening = new ArrayList<>();

    public SocialSpyCommandExecutor(MyChat plugin) {
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

        final String senderName = sender.getName();

        if (listening.contains(senderName)) {
            listening.remove(sender.getName());
            sender.sendMessage(pluginConfig.getSocialspyDisabled());
        }
        else {
            listening.add(sender.getName());
            sender.sendMessage(pluginConfig.getSocialspyEnabled());
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
