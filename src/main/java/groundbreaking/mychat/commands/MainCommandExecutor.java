package groundbreaking.mychat.commands;

import groundbreaking.mychat.MyChat;
import groundbreaking.mychat.automessages.AutoMessages;
import groundbreaking.mychat.utils.ConfigValues;
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
        this.configValues = plugin.getPluginConfig();
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String commandlabel, @NotNull String[] args) {

        final long startTime = System.currentTimeMillis();

        if (!sender.hasPermission("mychat.reload")) {
            sender.sendMessage(configValues.getNoPermissionMessage());
            return true;
        }

        plugin.reloadConfig();
        plugin.setupColorizers(plugin.getConfig(), plugin.getInfos());
        plugin.setupConfig();

        Bukkit.getScheduler().cancelTasks(plugin);
        new AutoMessages(plugin).startMSG(plugin.getConfig());

        long endTime = System.currentTimeMillis();
        sender.sendMessage(configValues.getReloadMessage().replace("{time}", String.valueOf(endTime - startTime)));
        return true;
    }
}
