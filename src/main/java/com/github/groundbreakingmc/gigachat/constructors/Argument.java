package com.github.groundbreakingmc.gigachat.constructors;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.database.Database;
import com.github.groundbreakingmc.gigachat.utils.configvalues.Messages;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class Argument {

    private final GigaChat plugin;
    private final Messages messages;
    private final Database database;
    private final String name;
    private final String permission;

    protected Argument(GigaChat plugin, String name, String permission) {
        this.plugin = plugin;
        this.messages = this.plugin.getMessages();
        this.database = this.plugin.getDatabase();
        this.name = name;
        this.permission = permission;
    }

    public abstract boolean execute(@NotNull CommandSender sender, @NotNull String[] args);
}
