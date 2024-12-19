package com.github.groundbreakingmc.gigachat.commands.args;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.constructors.ArgsConstructor;
import com.github.groundbreakingmc.gigachat.utils.updateschecker.UpdatesChecker;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

public class UpdateArgument extends ArgsConstructor {

    private final GigaChat plugin;

    public UpdateArgument(final GigaChat plugin, final String name, final String permission) {
        super(name, permission);
        this.plugin = plugin;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage("§4[GigaChat] §cThis command can only be executed only by the console!");
            return true;
        }

        if (!UpdatesChecker.hasUpdate()) {
            sender.sendMessage("§4[GigaChat] §cNothing to update!");
            return true;
        }

        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> new UpdatesChecker(plugin).downloadJar());
        return true;
    }
}
