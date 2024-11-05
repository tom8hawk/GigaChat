package groundbreaking.gigachat.commands.privateMessages;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.collections.*;
import groundbreaking.gigachat.database.DatabaseQueries;
import groundbreaking.gigachat.utils.StringValidator;
import groundbreaking.gigachat.utils.Utils;
import groundbreaking.gigachat.utils.colorizer.basic.Colorizer;
import groundbreaking.gigachat.utils.config.values.Messages;
import groundbreaking.gigachat.utils.config.values.PrivateMessagesValues;
import groundbreaking.gigachat.utils.vanish.VanishChecker;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class PrivateMessageCommandExecutor implements CommandExecutor, TabCompleter {

    private final GigaChat plugin;
    private final PrivateMessagesValues pmValues;
    private final Messages messages;
    private final Colorizer hexColorizer;
    private final CooldownsCollection cooldownsCollection;
    private final PmSoundsCollection pmSoundsCollection;
    private final DisabledPrivateMessagesCollection disabled;
    private final VanishChecker vanishChecker;
    private final ConsoleCommandSender consoleSender;

    @Getter
    private final String[] placeholders = {"{from-prefix}", "{from-name}", "{from-suffix}", "{to-prefix}", "{to-name}", "{to-suffix}"};

    public PrivateMessageCommandExecutor(final GigaChat plugin) {
        this.plugin = plugin;
        this.pmValues = plugin.getPmValues();
        this.messages = plugin.getMessages();
        this.hexColorizer = plugin.getColorizerByVersion();
        this.cooldownsCollection = plugin.getCooldownsCollection();
        this.pmSoundsCollection = plugin.getPmSoundsCollection();
        this.disabled = plugin.getDisabled();
        this.vanishChecker = plugin.getVanishChecker();
        this.consoleSender = plugin.getServer().getConsoleSender();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("gigachat.command.pm")) {
            sender.sendMessage(this.messages.getNoPermission());
            return true;
        }

        final boolean isPlayerSender = sender instanceof Player;
        if (args.length == 1 && isPlayerSender && args[0].equalsIgnoreCase("disable")) {
            this.processDisable((Player) sender);
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(this.messages.getPmUsageError());
            return true;
        }

        final Player recipient = Bukkit.getPlayer(args[0]);

        if (recipient == null || (isPlayerSender && !((Player) sender).canSee(recipient)) || this.vanishChecker.isVanished(recipient)) {
            sender.sendMessage(this.messages.getPlayerNotFound());
            return true;
        }

        if (recipient == sender) {
            sender.sendMessage(this.messages.getCannotChatWithHimself());
            return true;
        }

        final String senderName = sender.getName();
        if (isPlayerSender && this.hasCooldown((Player) sender, senderName)) {
            this.sendMessageHasCooldown((Player) sender, senderName);
            return true;
        }

        final String recipientName = recipient.getName();

        if (isPlayerSender && !sender.hasPermission("gigachat.bypass.ignore")) {
            if (IgnoreCollection.isIgnoredPrivate(recipientName, senderName)) {
                sender.sendMessage(this.messages.getRecipientIgnoresSender());
                return true;
            }

            if (IgnoreCollection.isIgnoredPrivate(senderName, recipientName)) {
                sender.sendMessage(this.messages.getSenderIgnoresRecipient());
                return true;
            }

            if (this.disabled.contains(recipientName)) {
                sender.sendMessage(this.messages.getHasDisabledPm());
                return true;
            }
        }

        String message = getMessage(sender, args, isPlayerSender);

        if (isPlayerSender) {
            final Player playerSender = (Player) sender;
            message = this.getValidMessage(playerSender, message);
            if (message == null) {
                return true;
            }
        }

        final String[] replacementList = getReplacements(sender, recipient, isPlayerSender, senderName, recipientName);

        this.process(sender, recipient, senderName, recipientName, isPlayerSender, message, replacementList);

        return true;
    }

    private boolean hasCooldown(final Player playerSender, final String senderName) {
        return this.cooldownsCollection.hasCooldown(playerSender, senderName, "gigachat.bypass.cooldown.pm", this.cooldownsCollection.getPrivateCooldowns());
    }

    private void sendMessageHasCooldown(final Player playerSender, final String senderName) {
        final long timeLeftInMillis = this.cooldownsCollection.getPrivateCooldowns().get(senderName) - System.currentTimeMillis();
        final int result = (int) (this.pmValues.getPmCooldown() / 1000 + timeLeftInMillis / 1000);
        final String restTime = Utils.getTime(result);
        final String message = this.messages.getCommandCooldownMessage().replace("{time}", restTime);
        playerSender.sendMessage(message);
    }

    private void processDisable(final Player sender) {
        final String name = sender.getName();
        if (this.disabled.contains(name)) {
            this.disabled.remove(name);
            DatabaseQueries.removePlayerFromDisabledPrivateMessages(name);
            sender.sendMessage(this.messages.getPmDisabled());
        } else {
            this.disabled.add(name);
            DatabaseQueries.addPlayerToDisabledPrivateMessages(name);
            sender.sendMessage(this.messages.getPmEnabled());
        }
    }

    private String[] getReplacements(final CommandSender sender, final Player recipient, final boolean isPlayerSender, final String senderName, final String recipientName) {
        String senderPrefix = "", senderSuffix = "";
        if (isPlayerSender) {
            final Player player = (Player) sender;
            senderPrefix = this.plugin.getChat().getPlayerPrefix(player);
            senderSuffix = this.plugin.getChat().getPlayerSuffix(player);
        }

        final String recipientPrefix = this.plugin.getChat().getPlayerPrefix(recipient);
        final String recipientSuffix = this.plugin.getChat().getPlayerSuffix(recipient);

        return new String[]{senderPrefix, senderName, senderSuffix, recipientPrefix, recipientName, recipientSuffix};
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
    
    private void process(final CommandSender sender, final Player recipient, final String senderName,
                         final String recipientName, final boolean isPlayerSender, final String message, final String[] replacementList) {
        final String formattedMessageForSender, formattedMessageForRecipient, formattedMessageForConsole, formattedMessageForSocialSpy;
        if (isPlayerSender) {
            final Player playerSender = (Player) sender;
            formattedMessageForSender = getFormattedMessage(playerSender, message, this.pmValues.getSenderFormat(), replacementList);
            formattedMessageForRecipient = getFormattedMessage(playerSender, message, this.pmValues.getRecipientFormat(), replacementList);
            formattedMessageForConsole = getFormattedMessage(playerSender, message, this.pmValues.getConsoleFormat(), replacementList);
            formattedMessageForSocialSpy = getFormattedMessage(playerSender, message, this.pmValues.getSocialSpyFormat(), replacementList);

            ReplyCollection.add(recipientName, senderName);
            processLogs(formattedMessageForConsole);
            SocialSpyCollection.sendAll(playerSender, recipient, formattedMessageForSocialSpy);
        } else {
            formattedMessageForSender = getFormattedMessage(message, this.pmValues.getSenderFormat(), replacementList);
            formattedMessageForRecipient = getFormattedMessage(message, this.pmValues.getRecipientFormat(), replacementList);
        }

        sender.sendMessage(formattedMessageForSender);
        recipient.sendMessage(formattedMessageForRecipient);

        playSound(recipient);
    }

    public String getFormattedMessage(final Player player, final String message, final String format, final String[] replacementList) {
        final String formatted = this.hexColorizer.colorize(
                Utils.replacePlaceholders(
                        player,
                        Utils.replaceEach(format, this.placeholders, replacementList)
                )
        );

        return formatted.replace("{message}", message).replace("%", "%%");
    }

    public String getFormattedMessage(final String message, final String format, final String[] replacementList) {
        final String formatted = this.hexColorizer.colorize(
                Utils.replaceEach(format, this.placeholders, replacementList)
        );

        return formatted.replace("{message}", message).replace("%", "%%");
    }

    private String getMessage(final CommandSender sender, final String[] args, final boolean isPlayerSender) {
        if (isPlayerSender) {
            return this.pmValues.getMessagesColorizer().colorize((Player) sender, String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim());
        }

        return this.hexColorizer.colorize(String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim());
    }

    private void playSound(final Player recipient) {
        if (pmValues.isSoundEnabled()) {
            final Sound sound = this.pmSoundsCollection.getSound(recipient.getName());
            if (sound != null) {
                final Location recipientLocation = recipient.getLocation();
                final float volume = this.pmValues.getSoundVolume();
                final float pitch = this.pmValues.getSoundPitch();
                recipient.playSound(recipientLocation, sound, volume, pitch);
            }
        }
    }

    private void processLogs(final String formattedMessage) {
        if (this.pmValues.isPrintLogsToConsole()) {
            this.consoleSender.sendMessage(formattedMessage);
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            final String input = args[0].toLowerCase();
            final List<String> players = new ArrayList<>();

            if (sender instanceof Player playerSender) {
                for (final Player player : Bukkit.getOnlinePlayers()) {
                    final String playerName = player.getName();
                    if (IgnoreCollection.isIgnoredChat(playerSender.getName(), playerName) || IgnoreCollection.isIgnoredChat(playerName, playerSender.getName())) {
                        continue;
                    }

                    if (playerName.toLowerCase().startsWith(input) && !this.vanishChecker.isVanished(player)) {
                        players.add(playerName);
                    }
                }
            } else {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    final String playerName = player.getName();
                    if (playerName.toLowerCase().startsWith(input)) {
                        players.add(playerName);
                    }
                }
            }

            if ("disable".startsWith(input) && sender.hasPermission("gigachat.disable.pm")) {
                players.add("disable");
            }

            return players;
        }

        return Collections.emptyList();
    }
}
