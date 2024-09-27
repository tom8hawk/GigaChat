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

public class ReplyCommandExecutor implements CommandExecutor, TabCompleter {

    private final MyChat plugin;
    private final IColorizer hexColorizer;
    private final ConfigValues configValues;
    private final ConsoleCommandSender consoleSender;

    @Getter
    private static final HashMap<String, String> reply = new HashMap<>();

    @Setter
    private static AbstractColorizer messagesColorizer;

    private final String[] placeholders = { "{from-prefix}", "{from-name}", "{from-suffix}", "{to-prefix}", "{to-name}", "{to-suffix}" };

    public ReplyCommandExecutor(MyChat plugin) {
        this.plugin = plugin;
        this.hexColorizer = plugin.getColorizerByVersion();
        this.configValues = plugin.getConfigValues();
        this.consoleSender = plugin.getServer().getConsoleSender();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("mychat.command.pm")) {
            sender.sendMessage(configValues.getNoPermissionMessage());
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(configValues.getReplyUsageError());
            return true;
        }

        final String senderName = sender.getName();
        final String recipientName = reply.get(senderName);

        if (recipientName == null) {
            sender.sendMessage(configValues.getNobodyToAnswer());
            return true;
        }

        final Player recipient = Bukkit.getPlayer(reply.get(senderName));

        if (recipient == null || !recipient.isOnline()) {
            sender.sendMessage(configValues.getPlayerNotFoundMessage());
            return true;
        }

        final boolean isPlayerSender = sender instanceof Player;
        if (isPlayerSender) {
            if (isIgnored(recipientName, senderName)) {
                sender.sendMessage(configValues.getRecipientIgnoresSender());
                return true;
            }

            if (isIgnored(senderName, recipientName)) {
                sender.sendMessage(configValues.getSenderIgnoresRecipient());
                return true;
            }
        }

        String senderPrefix = "", senderSuffix = "";
        if (isPlayerSender) {
            final Player playerSender = (Player) sender;
            senderPrefix = getPrefix(playerSender);
            senderSuffix = getSuffix(playerSender);
        }
        final String recipientPrefix = getPrefix(recipient), recipientSuffix = getSuffix(recipient);

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

        reply.put(recipientName, senderName);

        if (configValues.isPmSoundEnabled()) {
            recipient.playSound(recipient, configValues.getPmSound(), configValues.getPmSoundVolume(), configValues.getPmSoundPitch());
        }

        if (configValues.isPrintPmToConsole() && isPlayerSender) {
            consoleSender.sendMessage(formattedMessageForConsole);
        }

        processSocialspy(formattedMessageForSocialSpy);

        return true;
    }

    private boolean isIgnored(String ignoredName, String ignoringName) {
        return IgnoreCommandExecutor.ignores(ignoringName, ignoredName);
    }

    private String getPrefix(Player player) {
        return plugin.getChat().getPlayerPrefix(player);
    }

    private String getSuffix(Player player) {
        return plugin.getChat().getPlayerSuffix(player);
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

    private void processSocialspy(String formattedMessage) {
        final Set<String> players = SocialSpyCommandExecutor.getListening();
        for (String s : players) {
            final Player player = Bukkit.getPlayer(s);
            if (player != null) {
                player.sendMessage(formattedMessage);
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
