package groundbreaking.gigachat.commands.privateMessages;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.collections.Ignore;
import groundbreaking.gigachat.collections.Reply;
import groundbreaking.gigachat.collections.SocialSpy;
import groundbreaking.gigachat.utils.Utils;
import groundbreaking.gigachat.utils.config.values.Messages;
import groundbreaking.gigachat.utils.config.values.PrivateMessagesValues;
import groundbreaking.gigachat.utils.vanish.IVanishChecker;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public final class ReplyCommandExecutor implements CommandExecutor, TabCompleter {

    private final GigaChat plugin;
    private final PrivateMessagesValues pmValues;
    private final Messages messages;
    private final IVanishChecker vanishChecker;
    private final ConsoleCommandSender consoleSender;

    private final String[] placeholders = { "{from-prefix}", "{from-name}", "{from-suffix}", "{to-prefix}", "{to-name}", "{to-suffix}" };

    public ReplyCommandExecutor(final GigaChat plugin) {
        this.plugin = plugin;
        this.pmValues = plugin.getPmValues();
        this.messages = plugin.getMessages();
        this.vanishChecker = plugin.getVanishChecker();
        this.consoleSender = plugin.getServer().getConsoleSender();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("gigachat.command.reply")) {
            sender.sendMessage(messages.getNoPermission());
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(messages.getReplyUsageError());
            return true;
        }

        final String senderName = sender.getName();
        final String recipientName = Reply.getRecipientName(senderName);

        if (recipientName == null) {
            sender.sendMessage(messages.getNobodyToAnswer());
            return true;
        }

        final Player recipient = Bukkit.getPlayer(recipientName);

        final boolean isPlayerSender = sender instanceof Player;
        if (recipient == null || (isPlayerSender && !((Player) sender).canSee(recipient)) || vanishChecker.isVanished(recipient)) {
            sender.sendMessage(messages.getPlayerNotFound());
            return true;
        }

        if (isPlayerSender && !sender.hasPermission("gigachat.bypass.ignore")) {
            if (isIgnored(recipientName, senderName)) {
                sender.sendMessage(messages.getRecipientIgnoresSender());
                return true;
            }

            if (isIgnored(senderName, recipientName)) {
                sender.sendMessage(messages.getSenderIgnoresRecipient());
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
        final String formattedMessageForSender, formattedMessageForRecipient, formattedMessageForConsole, formattedMessageForSocialSpy;

        if (isPlayerSender) {
            final Player playerSender = (Player) sender;
            final String message = getMessage(playerSender, args);
            formattedMessageForSender = getFormattedMessage(playerSender, message, pmValues.getSenderFormat(), replacementList);
            formattedMessageForRecipient = getFormattedMessage(playerSender, message, pmValues.getRecipientFormat(),replacementList);
            formattedMessageForConsole = getFormattedMessage(playerSender, message, pmValues.getConsoleFormat(),replacementList);
            formattedMessageForSocialSpy = getFormattedMessage(playerSender, message, pmValues.getSocialSpyFormat(),replacementList);

            Reply.add(recipientName, senderName);
            processLogs(formattedMessageForConsole);
            SocialSpy.sendAll(playerSender, recipient, formattedMessageForSocialSpy);
        } else {
            final String message = getMessage(args);
            formattedMessageForSender = getFormattedMessage(message, pmValues.getSenderFormat(), replacementList);
            formattedMessageForRecipient = getFormattedMessage(message, pmValues.getRecipientFormat(),replacementList);
        }

        sender.sendMessage(formattedMessageForSender);
        recipient.sendMessage(formattedMessageForRecipient);

        playSound(recipient);

        return true;
    }

    private boolean isIgnored(final String ignoredName, final String ignoringName) {
        return Ignore.isIgnoredPrivate(ignoringName, ignoredName);
    }

    private String getPrefix(final Player player) {
        return plugin.getChat().getPlayerPrefix(player);
    }

    private String getSuffix(final Player player) {
        return plugin.getChat().getPlayerSuffix(player);
    }

    public String getFormattedMessage(final Player player, final String message, final String format, final String[] replacementList) {
        final String formatted = pmValues.getMessagesColorizer().colorize(
                Utils.replacePlaceholders(player,
                        Utils.replaceEach(format, placeholders, replacementList)
                )
        );

        return formatted.replace("{message}", message).replace("%", "%%");
    }

    public String getFormattedMessage(final String message, final String format, final String[] replacementList) {
        final String formatted = pmValues.getMessagesColorizer().colorize(
                Utils.replaceEach(format, placeholders, replacementList)
        );

        return formatted.replace("{message}", message).replace("%", "%%");
    }

    private String getMessage(final Player sender, final String[] args) {
        return pmValues.getMessagesColorizer().colorize(sender, String.join(" ", args).trim() );
    }

    private String getMessage(final String[] args) {
        return pmValues.getMessagesColorizer().colorize( String.join(" ", args).trim() );
    }

    private void playSound(final Player recipient) {
        if (pmValues.isSoundEnabled()) {
            recipient.playSound(recipient.getLocation(), pmValues.getSound(), pmValues.getSoundVolume(), pmValues.getSoundPitch());
        }
    }

    private void processLogs(final String formattedMessage) {
        consoleSender.sendMessage(formattedMessage);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
