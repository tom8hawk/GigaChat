package com.github.groundbreakingmc.gigachat.commands.args;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.constructors.ArgsConstructor;
import com.github.groundbreakingmc.gigachat.utils.config.values.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class ClearChatArgument extends ArgsConstructor {

    private final Messages messages;
    private final String clearMessage;

    public ClearChatArgument(final GigaChat plugin, final String name, final String permission) {
        super(name, permission);
        this.messages = plugin.getMessages();
        this.clearMessage = "\n ".repeat(100);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("gigachat.bypass.clearchat") || player.equals(sender)) {
                player.sendMessage(this.messages.getChatHasBeenClearedByAdministrator());
                continue;
            }

            player.sendMessage(this.clearMessage);
        }

        return true;
    }
}