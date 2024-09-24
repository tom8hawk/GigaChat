package groundbreaking.mychat.commands;

import groundbreaking.mychat.MyChat;
import groundbreaking.mychat.automessages.AutoMessages;
import groundbreaking.mychat.utils.Config;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class MainCommandExecutor implements CommandExecutor {

    private final MyChat plugin;
    private final Config pluginConfig;

    public MainCommandExecutor(MyChat plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String commandlabel, @NotNull String[] args) {

        final long startTime = System.currentTimeMillis();

        if (!sender.hasPermission("pchat.reload")) {
            sender.sendMessage(pluginConfig.getNoPermissionMessage());
            return true;
        }

        plugin.reloadConfig();
        plugin.setupConfig();

        Bukkit.getScheduler().cancelTasks(plugin);
        new AutoMessages(plugin).startMSG(plugin.getConfig());

        long endTime = System.currentTimeMillis();
        sender.sendMessage(pluginConfig.getReloadMessage().replace("%time%", String.valueOf(endTime - startTime)));
        return true;
    }
}
