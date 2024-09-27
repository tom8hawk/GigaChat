package groundbreaking.mychat.commands;

import groundbreaking.mychat.MyChat;
import groundbreaking.mychat.utils.ConfigValues;
import groundbreaking.mychat.utils.Utils;
import groundbreaking.mychat.utils.colorizer.IColorizer;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PrivateMessageCommandExecutor implements CommandExecutor, TabCompleter {

    private final MyChat plugin;
    private final ConfigValues configValues;
    private final IColorizer colorizer, messagesColorizer;
    private final ConsoleCommandSender consoleSender;

    @Getter
    private final String[] placeholders = { "{from-prefix}", "{from-name}", "{from-suffix}", "{to-prefix}", "{to-name}", "{to-suffix}", "{message}" };

    public PrivateMessageCommandExecutor(MyChat plugin) {
        this.plugin = plugin;
        this.configValues = plugin.getConfigValues();
        this.colorizer = plugin.getColorizer();
        this.messagesColorizer = plugin.getPrivateMessagesColorizer();
        this.consoleSender = plugin.getServer().getConsoleSender();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("mychat.privatemessage")) {
            sender.sendMessage(configValues.getNoPermissionMessage());
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(configValues.getPmUsageError());
            return true;
        }

        final Player recipient = Bukkit.getPlayer(args[0]);

        if (recipient == null) {
            sender.sendMessage(configValues.getPlayerNotFoundMessage());
            return true;
        }

        if (recipient == sender) {
            sender.sendMessage(configValues.getCannotPmSelf());
            return true;
        }

        final String senderName = sender.getName();
        final String recipientName = recipient.getName();

        if (sender instanceof Player) {
            if (!IgnoreCommandExecutor.ignores(recipientName, senderName)) {
                sender.sendMessage(configValues.getRecipientIgnoresSender());
                return true;
            }

            if (!IgnoreCommandExecutor.ignores(senderName, recipientName)) {
                sender.sendMessage(configValues.getSenderIgnoresRecipient());
                return true;
            }
        }

        String senderPrefix = "", senderSuffix = "";
        if (sender instanceof Player player) {
            senderPrefix = plugin.getChat().getPlayerPrefix(player);
            senderSuffix = plugin.getChat().getPlayerSuffix(player);
        }

        final String recipientPrefix = plugin.getChat().getPlayerPrefix(recipient);
        final String recipientSuffix = plugin.getChat().getPlayerSuffix(recipient);

        final String message = getMessage(sender, args);

        final String[] replacementList = { senderPrefix, senderName, senderSuffix, recipientPrefix, recipientName, recipientSuffix, message};

        sender.sendMessage(colorizer.colorize(Utils.replaceEach(configValues.getPmSenderFormat(), placeholders, replacementList)));
        recipient.sendMessage(colorizer.colorize(Utils.replaceEach(configValues.getPmRecipientFormat(), placeholders, replacementList)));

        if (configValues.isPmSoundEnabled()) {
            recipient.playSound(recipient, configValues.getPmSound(), configValues.getPmSoundVolume(), configValues.getPmSoundPitch());
        }

        if (!(sender instanceof ConsoleCommandSender)) {
            ReplyCommandExecutor.getReply().put(recipientName, senderName);

            if (configValues.isPrintPmToConsole()) {
                consoleSender.sendMessage(colorizer.colorize(Utils.replaceEach(configValues.getPmConsoleFormat(), placeholders, replacementList)));
            }
        }

        processSocialspy(replacementList);

        return true;
    }

    private String getMessage(CommandSender sender, String[] args) {
        final StringBuilder builder = new StringBuilder();

        for (int i = 0; i < args.length; i++) {
            builder.append(" ").append(args[i]);
        }

        if (sender instanceof Player player) {
            return Utils.colorizePrivateMessage(player, builder.toString().trim());
        } else {
            return messagesColorizer.colorize(builder.toString().trim());
        }
    }

    private void processSocialspy(String[] replacementList) {
        final List<String> players = SocialSpyCommandExecutor.getListening();
        for (int i = 0; i < players.size(); i++) {
            final Player player = Bukkit.getPlayer(players.get(i));
            if (player != null) {
                player.sendMessage(colorizer.colorize(Utils.replaceEach(configValues.getPmSocialSpyFormat(), placeholders, replacementList)));
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            final String input = args[0];
            final List<String> players = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                final String playerName = player.getName();
                if (input.startsWith(playerName)) {
                    players.add(playerName);
                }
            }
            return players;
        }

        return Collections.emptyList();
    }
}
