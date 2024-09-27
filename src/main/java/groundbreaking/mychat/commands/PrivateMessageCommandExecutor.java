package groundbreaking.mychat.commands;

import groundbreaking.mychat.MyChat;
import groundbreaking.mychat.utils.ConfigValues;
import groundbreaking.mychat.utils.Utils;
import groundbreaking.mychat.utils.chatsColorizer.AbstractColorizer;
import groundbreaking.mychat.utils.colorizer.IColorizer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PrivateMessageCommandExecutor implements CommandExecutor, TabCompleter {

    private final MyChat plugin;
    private final ConfigValues configValues;
    private final IColorizer hexColorizer;
    private final ConsoleCommandSender consoleSender;

    @Setter
    public static AbstractColorizer messagesColorizer;

    @Getter
    private final String[] placeholders = { "{from-prefix}", "{from-name}", "{from-suffix}", "{to-prefix}", "{to-name}", "{to-suffix}" };

    public PrivateMessageCommandExecutor(MyChat plugin) {
        this.plugin = plugin;
        this.configValues = plugin.getConfigValues();
        this.hexColorizer = plugin.getColorizerByVersion();
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

        final boolean isPlayerSender = sender instanceof Player;
        if (isPlayerSender) {
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
        if (isPlayerSender) {
            final Player player = (Player) sender;
            senderPrefix = plugin.getChat().getPlayerPrefix(player);
            senderSuffix = plugin.getChat().getPlayerSuffix(player);
        }

        final String recipientPrefix = plugin.getChat().getPlayerPrefix(recipient);
        final String recipientSuffix = plugin.getChat().getPlayerSuffix(recipient);

        final String[] replacementList = { senderPrefix, senderName, senderSuffix, recipientPrefix, recipientName, recipientSuffix};
        final String message = getMessage(sender, args, isPlayerSender);
        final String formattedMessageForSender, formattedMessageForRecipient, formattedMessageForConsole, formattedMessageForSocialSpy;
        if (isPlayerSender) {
            final Player playerSender = (Player) sender;
            formattedMessageForSender = getFormattedMessage(playerSender, message, configValues.getPmSenderFormat(), replacementList);
            formattedMessageForRecipient = getFormattedMessage(playerSender, message, configValues.getPmRecipientFormat(),replacementList);
            formattedMessageForConsole = getFormattedMessage(playerSender, message, configValues.getPmConsoleFormat(),replacementList);
            formattedMessageForSocialSpy = getFormattedMessage(playerSender, message, configValues.getPmSocialSpyFormat(),replacementList);
        } else {
            formattedMessageForSender = getFormattedMessage(message, configValues.getPmSenderFormat(), replacementList);
            formattedMessageForRecipient = getFormattedMessage(message, configValues.getPmRecipientFormat(),replacementList);
            formattedMessageForConsole = getFormattedMessage(message, configValues.getPmConsoleFormat(),replacementList);
            formattedMessageForSocialSpy = getFormattedMessage(message, configValues.getPmSocialSpyFormat(),replacementList);
        }

        sender.sendMessage(formattedMessageForSender);
        recipient.sendMessage(formattedMessageForRecipient);

        if (configValues.isPmSoundEnabled()) {
            recipient.playSound(recipient, configValues.getPmSound(), configValues.getPmSoundVolume(), configValues.getPmSoundPitch());
        }

        if (isPlayerSender) {
            ReplyCommandExecutor.getReply().put(recipientName, senderName);

            if (configValues.isPrintPmToConsole()) {
                consoleSender.sendMessage(formattedMessageForConsole);
            }
        }

        processSocialspy(formattedMessageForSocialSpy);

        return true;
    }

    public String getFormattedMessage(Player player, String message, String format, String[] replacementList) {
        final String formatted = hexColorizer.colorize(
                Utils.replacePlaceholders(player,
                        Utils.replaceEach(format, placeholders, replacementList)
                )
        );

        return formatted.replace("{message}", message).replace("%", "%%");
    }

    public String getFormattedMessage(String message, String format, String[] replacementList) {
        final String formatted = hexColorizer.colorize(
                Utils.replaceEach(format, placeholders, replacementList)
        );

        return formatted.replace("{message}", message).replace("%", "%%");
    }

    private String getMessage(CommandSender sender, String[] args, boolean isPlayerSender) {
        if (isPlayerSender) {
            return messagesColorizer.colorize((Player) sender, String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim());
        }

        return hexColorizer.colorize(String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim());
    }

    private void processSocialspy(String message) {
        final Set<String> players = SocialSpyCommandExecutor.getListening();
        for (String s : players) {
            final Player player = Bukkit.getPlayer(s);
            if (player != null) {
                player.sendMessage(message);
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
                if (playerName.startsWith(input)) {
                    players.add(playerName);
                }
            }
            return players;
        }

        return Collections.emptyList();
    }
}
