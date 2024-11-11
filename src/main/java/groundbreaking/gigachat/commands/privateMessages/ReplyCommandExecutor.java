package groundbreaking.gigachat.commands.privateMessages;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.collections.*;
import groundbreaking.gigachat.utils.StringValidator;
import groundbreaking.gigachat.utils.Utils;
import groundbreaking.gigachat.utils.config.values.Messages;
import groundbreaking.gigachat.utils.config.values.PrivateMessagesValues;
import groundbreaking.gigachat.utils.vanish.VanishChecker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class ReplyCommandExecutor implements CommandExecutor, TabCompleter {

    private final GigaChat plugin;
    private final PrivateMessagesValues pmValues;
    private final Messages messages;
    private final VanishChecker vanishChecker;
    private final ConsoleCommandSender consoleSender;
    private final CooldownsCollection cooldownsCollection;
    private final PmSoundsCollection pmSoundsCollection;

    private final String[] placeholders = { "{from-prefix}", "{from-name}", "{from-suffix}", "{to-prefix}", "{to-name}", "{to-suffix}" };

    public ReplyCommandExecutor(final GigaChat plugin) {
        this.plugin = plugin;
        this.pmValues = plugin.getPmValues();
        this.messages = plugin.getMessages();
        this.vanishChecker = plugin.getVanishChecker();
        this.consoleSender = plugin.getServer().getConsoleSender();
        this.cooldownsCollection = plugin.getCooldownsCollection();
        this.pmSoundsCollection = plugin.getPmSoundsCollection();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof final Player playerSender)) {
            sender.sendMessage(this.messages.getPlayerOnly());
            return true;
        }

        if (!sender.hasPermission("gigachat.command.reply")) {
            sender.sendMessage(this.messages.getNoPermission());
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(this.messages.getReplyUsageError());
            return true;
        }

        final UUID senderUUID = playerSender.getUniqueId();
        if (this.hasCooldown(playerSender, senderUUID)) {
            this.sendMessageHasCooldown(playerSender, senderUUID);
            return true;
        }

        final UUID recipientUUID = ReplyCollection.getRecipientName(senderUUID);
        if (recipientUUID == null) {
            sender.sendMessage(this.messages.getNobodyToAnswer());
            return true;
        }

        final Player recipient = Bukkit.getPlayer(recipientUUID);
        if (recipient == null || !playerSender.canSee(recipient) || this.vanishChecker.isVanished(recipient)) {
            sender.sendMessage(this.messages.getPlayerNotFound());
            return true;
        }

        if (!sender.hasPermission("gigachat.bypass.ignore.private")) {
            if (isIgnored(senderUUID, recipientUUID)) {
                sender.sendMessage(this.messages.getRecipientIgnoresSender());
                return true;
            }

            if (isIgnored(recipientUUID, senderUUID)) {
                sender.sendMessage(this.messages.getSenderIgnoresRecipient());
                return true;
            }
        }

        String message = this.getMessage(playerSender, args);
        message = this.getValidMessage(playerSender, message);
        if (message == null) {
            return true;
        }

        final String senderPrefix = this.getPrefix(playerSender);
        final String senderSuffix = this.getSuffix(playerSender);
        final String recipientPrefix = this.getPrefix(recipient);
        final String recipientSuffix = getSuffix(recipient);

        final String senderName = playerSender.getName();
        final String recipientName = recipient.getName();
        final String[] replacementList = { senderPrefix, senderName, senderSuffix, recipientPrefix, recipientName, recipientSuffix };

        final String formattedMessageForSender = this.getFormattedMessage(playerSender, message, this.pmValues.getSenderFormat(), replacementList);
        final String formattedMessageForRecipient = this.getFormattedMessage(playerSender, message, this.pmValues.getRecipientFormat(), replacementList);
        final String formattedMessageForConsole = this.getFormattedMessage(playerSender, message, this.pmValues.getConsoleFormat(), replacementList);
        final String formattedMessageForSocialSpy = this.getFormattedMessage(playerSender, message, this.pmValues.getSocialSpyFormat(), replacementList);

        ReplyCollection.add(recipientUUID, senderUUID);
        this.processLogs(formattedMessageForConsole);
        SocialSpyCollection.sendAll(playerSender, recipient, formattedMessageForSocialSpy);

        playerSender.sendMessage(formattedMessageForSender);
        recipient.sendMessage(formattedMessageForRecipient);

        this.playSound(recipient);

        return true;
    }

    private boolean hasCooldown(final Player playerSender, final UUID senderUUID) {
        return this.cooldownsCollection.hasCooldown(playerSender, senderUUID, "gigachat.bypass.cooldown.socialspy", this.cooldownsCollection.getPrivateCooldowns());
    }

    private void sendMessageHasCooldown(final Player playerSender, final UUID senderUUID) {
        final long timeLeftInMillis = this.cooldownsCollection.getPrivateCooldowns().get(senderUUID) - System.currentTimeMillis();
        final int result = (int) (this.pmValues.getPmCooldown() / 1000 + timeLeftInMillis / 1000);
        final String restTime = Utils.getTime(result);
        final String message = this.messages.getCommandCooldownMessage().replace("{time}", restTime);
        playerSender.sendMessage(message);
    }

    private boolean isIgnored(final UUID ignoringUUID, final UUID ignoredUUID) {
        return IgnoreCollection.isIgnoredPrivate(ignoringUUID, ignoredUUID);
    }

    private String getPrefix(final Player player) {
        return this.plugin.getChat().getPlayerPrefix(player);
    }

    private String getSuffix(final Player player) {
        return this.plugin.getChat().getPlayerSuffix(player);
    }

    public String getFormattedMessage(final Player player, final String message, final String format, final String[] replacementList) {
        final String formatted = this.pmValues.getMessagesColorizer().colorize(
                Utils.replacePlaceholders(
                        player,
                        Utils.replaceEach(format, this.placeholders, replacementList)
                )
        );

        return formatted.replace("{message}", message).replace("%", "%%");
    }

    private String getMessage(final Player sender, final String[] args) {
        return this.pmValues.getMessagesColorizer().colorize(sender, String.join(" ", args).trim());
    }

    private void playDenySoundCharsFailed(final Player messageSender) {
        if (this.pmValues.isCharsValidatorDenySoundEnabled()) {
            final Location location = messageSender.getLocation();
            final Sound sound = this.pmValues.getTextValidatorDenySound();
            final float volume = this.pmValues.getTextValidatorDenySoundVolume();
            final float pitch = this.pmValues.getTextValidatorDenySoundPitch();

            messageSender.playSound(location, sound, volume, pitch);
        }
    }

    private void playSound(final Player recipient) {
        final Sound sound = this.pmSoundsCollection.getSound(recipient.getUniqueId());
        if (this.pmValues.isSoundEnabled()) {
            final Location recipientLocation = recipient.getLocation();
            final float volume = this.pmValues.getSoundVolume();
            final float pitch = this.pmValues.getSoundPitch();
            recipient.playSound(recipientLocation, sound, volume, pitch);
        }
    }

    private String getValidMessage(final Player sender, String message) {
        final StringValidator stringValidator = this.pmValues.getStringValidator();
        if (stringValidator.hasBlockedChars(message)) {
            if (this.pmValues.isCharsValidatorBlockMessage()) {
                final String denyMessage = this.messages.getCharsValidationFailedMessage();
                if (!denyMessage.isEmpty()) {
                    sender.sendMessage(denyMessage);
                }
                this.playDenySoundCharsCheckFailed(sender);
                return null;
            }

            message = stringValidator.getFormattedCharsMessage(message);
        }

        if (stringValidator.isUpperCasePercentageExceeded(message)) {
            if (this.pmValues.isCapsValidatorBlockMessageSend()) {
                final String denyMessage = this.messages.getCharsValidationFailedMessage();
                if (!denyMessage.isEmpty()) {
                    sender.sendMessage(denyMessage);
                }
                this.playDenySoundCapsCheckFailed(sender);
                return null;
            }

            message = message.toLowerCase();
        }

        if (stringValidator.hasBlockedWords(message)) {
            if (this.pmValues.isWordsValidatorBlockMessageSend()) {
                final String denyMessage = this.messages.getWordsValidationFailedMessage();
                if (!denyMessage.isEmpty()) {
                    sender.sendMessage(denyMessage);
                }
                this.playDenySoundWordsCheckFailed(sender);
                return null;
            }

            message = stringValidator.getFormattedWordsMessage(message);
        }

        return message;
    }

    private void playDenySoundCharsCheckFailed(final Player messageSender) {
        if (this.pmValues.isCharsValidatorDenySoundEnabled()) {
            final Location location = messageSender.getLocation();
            final Sound sound = this.pmValues.getTextValidatorDenySound();
            final float volume = this.pmValues.getTextValidatorDenySoundVolume();
            final float pitch = this.pmValues.getTextValidatorDenySoundPitch();

            messageSender.playSound(location, sound, volume, pitch);
        }
    }

    private void playDenySoundCapsCheckFailed(final Player messageSender) {
        if (this.pmValues.isCapsValidatorDenySoundEnabled()) {
            final Location location = messageSender.getLocation();
            final Sound sound = this.pmValues.getCapsValidatorDenySound();
            final float volume = this.pmValues.getCapsValidatorDenySoundVolume();
            final float pitch = this.pmValues.getCapsValidatorDenySoundPitch();

            messageSender.playSound(location, sound, volume, pitch);
        }
    }

    private void playDenySoundWordsCheckFailed(final Player messageSender) {
        if (this.pmValues.isWordsValidatorDenySoundEnabled()) {
            final Location location = messageSender.getLocation();
            final Sound sound = this.pmValues.getWordsValidatorDenySound();
            final float volume = this.pmValues.getWordsValidatorDenySoundVolume();
            final float pitch = this.pmValues.getWordsValidatorDenySoundPitch();

            messageSender.playSound(location, sound, volume, pitch);
        }
    }

    private void processLogs(final String formattedMessage) {
        if (this.pmValues.isPrintLogsToConsole()) {
            this.consoleSender.sendMessage(formattedMessage);
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
