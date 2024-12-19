package com.github.groundbreakingmc.gigachat.commands.args;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.constructors.ArgsConstructor;
import com.github.groundbreakingmc.gigachat.utils.config.values.Messages;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class DisableServerChatArgument extends ArgsConstructor {

    private final Messages messages;

    @Getter
    private static boolean chatDisabled = false;

    public DisableServerChatArgument(final GigaChat plugin, final String name, final String permission) {
        super(name, permission);
        this.messages = plugin.getMessages();
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (chatDisabled) {
            sender.sendMessage(this.messages.getServerChatEnabled());
            chatDisabled = false;
        } else {
            sender.sendMessage(this.messages.getServerChatDisabled());
            chatDisabled = true;
        }

        return true;
    }
}