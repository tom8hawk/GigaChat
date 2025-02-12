package com.github.groundbreakingmc.gigachat.commands.args;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.constructors.ArgsConstructor;
import com.github.groundbreakingmc.gigachat.utils.config.values.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class ReloadArgument extends ArgsConstructor {

    private final GigaChat plugin;
    private final Messages messages;

    public ReloadArgument(final GigaChat plugin, final String name, final String permission) {
        super(name, permission);
        this.plugin = plugin;
        this.messages = plugin.getMessages();
    }
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        final long startTime = System.currentTimeMillis();

        this.plugin.getCommandRegisterer().unregisterCustomCommand();
        this.plugin.getAutoMessages().restart();
        this.plugin.reloadConfig();
        this.plugin.setupConfigValues();
        this.plugin.getCooldownCollections().setCooldowns();
        this.plugin.registerPluginCommands();

        final String reloadTime = String.valueOf(System.currentTimeMillis() - startTime);
        final String message = this.messages.getReloadMessage().replace("{time}", reloadTime);
        if (sender instanceof Player) {
            this.plugin.getServer().getConsoleSender().sendMessage(message);
        }
        sender.sendMessage(message);
        return true;
    }
}
