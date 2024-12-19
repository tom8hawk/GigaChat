package com.github.groundbreakingmc.gigachat.constructors;

import lombok.AllArgsConstructor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public abstract class ArgsConstructor {

    public final String name;
    public final String permission;

    public abstract boolean execute(@NotNull CommandSender sender, @NotNull String[] args);
}
