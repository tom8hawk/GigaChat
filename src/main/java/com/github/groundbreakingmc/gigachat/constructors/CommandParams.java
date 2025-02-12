package com.github.groundbreakingmc.gigachat.constructors;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.mylib.utils.command.CommandRuntimeUtils;
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

@RequiredArgsConstructor @Getter
public final class CommandParams {

    private final GigaChat plugin;
    private final CommandExecutor executor;
    private final TabCompleter completer;
    private String command;
    private List<String> aliases;

    public CommandParams(final GigaChat plugin, final TabExecutor executor) {
        this.plugin = plugin;
        this.executor = executor;
        this.completer = executor;
    }

    public void process(final ConfigurationSection section) {
        if (this.command != null) {
            CommandRuntimeUtils.unregisterCustomCommand(this.plugin, this.command);
        }

        this.command = section.getString("command");
        if (this.command != null) {
            this.aliases = ImmutableList.copyOf(section.getStringList("aliases"));
            CommandRuntimeUtils.register(this.plugin, this.command, this.aliases, this.executor, this.completer);
        } else {
            this.aliases = null;
        }
    }
}
