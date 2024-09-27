package groundbreaking.mychat.commands;

import groundbreaking.mychat.MyChat;
import groundbreaking.mychat.automessages.AutoMessages;
import groundbreaking.mychat.utils.ConfigValues;
import groundbreaking.mychat.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class MainCommandExecutor implements CommandExecutor {

    private final MyChat plugin;
    private final ConfigValues configValues;

    public MainCommandExecutor(MyChat plugin) {
        this.plugin = plugin;
        this.configValues = plugin.getConfigValues();
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String commandlabel, @NotNull String[] args) {

        final long startTime = System.currentTimeMillis();

        if (!sender.hasPermission("mychat.reload")) {
            sender.sendMessage(configValues.getNoPermissionMessage());
            return true;
        }

        plugin.reloadConfig();
        plugin.setColorizer(plugin.getColorizer("messages.use-minimessage"));
        configValues.setupValues(plugin);

        Utils.setChatColorizer(plugin.getColorizer("use-minimessage-for-chats"));
        Utils.setChatColorizer(plugin.getColorizer("privateMessages.use-minimessage"));

        Bukkit.getScheduler().cancelTasks(plugin);
        new AutoMessages(plugin).startMSG(plugin.getConfig());

        long endTime = System.currentTimeMillis();
        sender.sendMessage(configValues.getReloadMessage().replace("{time}", String.valueOf(endTime - startTime)));
        return true;
    }
}
