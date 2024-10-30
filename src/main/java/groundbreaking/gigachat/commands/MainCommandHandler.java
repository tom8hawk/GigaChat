package groundbreaking.gigachat.commands;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.constructors.ArgsConstructor;
import groundbreaking.gigachat.utils.config.values.Messages;
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
        this.arguments.put(argument.getName(), argument);
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

        if (!sender.hasPermission(argument.getPermission())) {
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
        final List<String> list = new ArrayList<>();
        final String input = args[args.length - 1].toUpperCase();

        if (args.length == 1) {
            if (sender.hasPermission("gigachat.command.clearchat") && "clearchat".startsWith(input)) {
                list.add("clearchat");
            }
            if (sender.hasPermission("gigachat.command.disableam.other") && "disableam".startsWith(input)) {
                list.add("disableam");
            }
            if (sender.hasPermission("gigachat.command.disablechat") && "disablechat".startsWith(input)) {
                list.add("disablechat");
            }
            if (sender.hasPermission("gigachat.command.reload") && "reload".startsWith(input)) {
                list.add("reload");
            }
            if (sender.hasPermission("gigachat.command.setpmsound") && "setpmsound".startsWith(input)) {
                list.add("setpmsound");
            }
            if (sender.hasPermission("gigachat.command.spy.other") && "spy".startsWith(input)) {
                list.add("spy");
            }
            if (sender instanceof ConsoleCommandSender && "update".startsWith(input)) {
                list.add("update");
            }

            return list;
        }

        if (args.length == 2) {
            final boolean hasAnyPerm = sender.hasPermission("gigachat.command.disableam")
                    || sender.hasPermission("gigachat.command.setpmsound")
                    || sender.hasPermission("gigachat.command.spy.other");
            if (hasAnyPerm) {
                final List<String> playerNames = this.getPlayers(input);
                list.addAll(playerNames);
            }
        }

        if (args[0].equalsIgnoreCase("setpmsound")) {
            final boolean hasPerm = sender.hasPermission("gigachat.command.setpmsound");
            if (args.length == 3 && hasPerm) {
                if ("none".startsWith(input.toLowerCase())) {
                    list.add("none");
                }

                for (final Sound sound : Sound.values()) {
                    final String soundName = sound.name();
                    if (soundName.startsWith(input)) {
                        list.add(soundName);
                    }
                }
            }
        } else if (args[0].equalsIgnoreCase("spy.other")) {
            return CHATS;
        }

        return list;
    }

    private List<String> getPlayers(final String input) {
        final List<String> players = new ArrayList<>();
        for (final Player player : Bukkit.getOnlinePlayers()) {
            final String playerName = player.getName();
            if (playerName.toLowerCase().startsWith(input)) {
                players.add(playerName);
            }
        }

        return players;
    }
}
