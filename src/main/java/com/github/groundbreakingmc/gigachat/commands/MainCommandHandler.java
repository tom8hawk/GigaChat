package com.github.groundbreakingmc.gigachat.commands;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.constructors.ArgsConstructor;
import com.github.groundbreakingmc.gigachat.utils.Utils;
import com.github.groundbreakingmc.gigachat.utils.config.values.Messages;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class MainCommandHandler implements CommandExecutor, TabCompleter {

    private final Messages messages;

    private final HashMap<String, ArgsConstructor> arguments = new HashMap<>();
    public static final List<String> CHATS = new ObjectArrayList<>();

    public MainCommandHandler(final GigaChat plugin) {
        this.messages = plugin.getMessages();
    }

    public void registerArgument(final ArgsConstructor argument) {
        this.arguments.put(argument.name, argument);
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String commandlabel, @NotNull String[] args) {
        if (args.length < 1) {
            if (this.hasAnyPluginPermission(sender)) {
                sender.sendMessage(this.messages.getNonExistArgument());
            } else {
                sender.sendMessage(this.messages.getNoPermission());
            }
            return true;
        }

        final ArgsConstructor argument = this.arguments.get(args[0]);

        if (argument == null) {
            sender.sendMessage(this.messages.getNonExistArgument());
            return true;
        }

        if (!sender.hasPermission(argument.permission)) {
            sender.sendMessage(this.messages.getNoPermission());
            return true;
        }

        return argument.execute(sender, args);
    }

    private boolean hasAnyPluginPermission(final CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) {
            return true;
        }

        return sender.hasPermission("gigachat.command.clearchat")
                || sender.hasPermission("gigachat.command.disableam.other")
                || sender.hasPermission("gigachat.command.disablechat")
                || sender.hasPermission("gigachat.command.reload")
                || sender.hasPermission("gigachat.command.setpmsound")
                || sender.hasPermission("gigachat.command.spy.other");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        final List<String> completions = new ArrayList<>();
        final String input = args[args.length - 1].toUpperCase();

        if (args.length == 1) {
            if (sender.hasPermission("gigachat.command.clearchat") && Utils.startsWithIgnoreCase(input, "clearchat")) {
                completions.add("clearchat");
            }
            if (sender.hasPermission("gigachat.command.disableam.other") && Utils.startsWithIgnoreCase(input, "disableam")) {
                completions.add("disableam");
            }
            if (sender.hasPermission("gigachat.command.disablechat") && Utils.startsWithIgnoreCase(input, "disablechat")) {
                completions.add("disablechat");
            }
            if (sender.hasPermission("gigachat.command.reload") && Utils.startsWithIgnoreCase(input, "reload")) {
                completions.add("reload");
            }
            if (sender.hasPermission("gigachat.command.setpmsound") && Utils.startsWithIgnoreCase(input, "setpmsound")) {
                completions.add("setpmsound");
            }
            if (sender.hasPermission("gigachat.command.spy.other") && Utils.startsWithIgnoreCase(input, "spy")) {
                completions.add("spy");
            }
            if (sender instanceof ConsoleCommandSender && Utils.startsWithIgnoreCase(input, "update")) {
                completions.add("update");
            }

            return completions;
        }

        if (args.length == 2) {
            if (sender.hasPermission("gigachat.command.reload") && args[0].equalsIgnoreCase("reload")) {
                this.reloadCompletion(input, completions);
            } else if (sender.hasPermission("gigachat.command.disableam")
                        || sender.hasPermission("gigachat.command.setpmsound")
                        || sender.hasPermission("gigachat.command.spy.other")) {
                    this.playersCompletion(input, completions);
            }
        }

        if (args[0].equalsIgnoreCase("setpmsound")) {
            if (args.length == 3 && sender.hasPermission("gigachat.command.setpmsound")) {
                if (Utils.startsWithIgnoreCase(input, "none")) {
                    completions.add("none");
                }

                for (final Sound sound : Sound.values()) {
                    final String soundName = sound.name();
                    if (Utils.startsWithIgnoreCase(input, soundName)) {
                        completions.add(soundName);
                    }
                }
            }
        } else if (args[0].equalsIgnoreCase("spy")) {
            return CHATS;
        }

        return completions;
    }

    private void reloadCompletion(final String input, final List<String> completions) {
        if (Utils.startsWithIgnoreCase(input, "auto-messages")) {
            completions.add("auto-messages");
        }
        if (Utils.startsWithIgnoreCase(input, "broadcast")) {
            completions.add("broadcast");
        }
        if (Utils.startsWithIgnoreCase(input, "chats")) {
            completions.add("chats");
        }
        if (Utils.startsWithIgnoreCase(input, "config")) {
            completions.add("config");
        }
        if (Utils.startsWithIgnoreCase(input, "messages")) {
            completions.add("messages");
        }
        if (Utils.startsWithIgnoreCase(input, "newbie")) {
            completions.add("newbie");
        }
        if (Utils.startsWithIgnoreCase(input, "private-messages")) {
            completions.add("private-messages");
        }
    }

    private void playersCompletion(final String input, final List<String> completions) {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            final String playerName = player.getName();
            if (Utils.startsWithIgnoreCase(input, playerName)) {
                completions.add(playerName);
            }
        }
    }
}
