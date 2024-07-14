package ru.overwrite.chat;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CommandClass implements CommandExecutor {

    private final PromisedChat instance;

    public CommandClass(PromisedChat plugin) {
        instance = plugin;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String commandlabel, @NotNull String[] args) {
        if (!sender.hasPermission("pchat.admin")) {
            sender.sendMessage("§6❖ §7Running §5§lPromisedChat §c§l" + instance.getDescription().getVersion() + "§7 by §5OverwriteMC");
            return true;
        }
        if (args.length == 0 && sender.hasPermission("pchat.admin")) {
            sender.sendMessage("§6/" + commandlabel + " reload - перезагрузить конфиг");
            return true;
        }
        if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("pchat.admin")) {
            long startTime = System.currentTimeMillis();
            instance.reloadConfig();
            instance.setupConfig();
            Bukkit.getScheduler().cancelTasks(instance);
            new AutoMessages(instance).startMSG(instance.getConfig());
            long endTime = System.currentTimeMillis();
            sender.sendMessage("§5§lPromisedChat §7> §aКонфигурация перезагружена за §e" + (endTime - startTime) + " ms");
            return true;
        } else {
            sender.sendMessage("§6❖ §7Running §5§lPromisedChat §c§l" + instance.getDescription().getVersion() + "§7 by §5OverwriteMC");
        }
        return true;
    }
}
