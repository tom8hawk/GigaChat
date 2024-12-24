package com.github.groundbreakingmc.gigachat.commands.privateMessages;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.collections.*;
import com.github.groundbreakingmc.gigachat.database.DatabaseHandler;
import com.github.groundbreakingmc.gigachat.database.DatabaseQueries;
import com.github.groundbreakingmc.gigachat.utils.StringValidator;
import com.github.groundbreakingmc.gigachat.utils.Utils;
import com.github.groundbreakingmc.gigachat.utils.colorizer.basic.Colorizer;
import com.github.groundbreakingmc.gigachat.utils.config.values.Messages;
import com.github.groundbreakingmc.gigachat.utils.config.values.PrivateMessagesValues;
import com.github.groundbreakingmc.gigachat.utils.vanish.VanishChecker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class PrivateMessageCommandExecutor implements CommandExecutor, TabCompleter {

    private final GigaChat plugin;
    private final PrivateMessagesValues pmValues;
    private final Messages messages;
    private final Colorizer hexColorizer;
    private final CooldownCollections cooldownCollections;
    private final PmSoundsCollection pmSoundsCollection;
    private final DisabledPrivateMessagesCollection disabled;
    private final VanishChecker vanishChecker;
    private final ConsoleCommandSender consoleSender;

    public PrivateMessageCommandExecutor(final GigaChat plugin) {
        this.plugin = plugin;
        this.pmValues = plugin.getPmValues();
        this.messages = plugin.getMessages();
        this.hexColorizer = plugin.getColorizerByVersion();
        this.cooldownCollections = plugin.getCooldownCollections();
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

        if (args.length == 1 && args[0].equalsIgnoreCase("disable")) {
            this.processDisable(sender);
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(this.messages.getPmUsageError());
            return true;
        }

        final Player recipient = Bukkit.getPlayer(args[0]);

        final boolean isPlayerSender = sender instanceof Player;
        if (recipient == null || (isPlayerSender && !((Player) sender).canSee(recipient)) || this.vanishChecker.isVanished(recipient)) {
            sender.sendMessage(this.messages.getPlayerNotFound());
            return true;
        }

        if (recipient == sender) {
            sender.sendMessage(this.messages.getCannotChatWithHimself());
            return true;
        }

        if (isPlayerSender && this.hasCooldown((Player) sender)) {
            this.sendMessageHasCooldown((Player) sender);
            return true;
        }

        final UUID recipientUUID = recipient.getUniqueId();

        if (isPlayerSender && !sender.hasPermission("gigachat.bypass.ignore")) {
            final UUID senderUUID = ((Player) sender).getUniqueId();
            if (IgnoreCollections.isIgnoredPrivate(recipientUUID, senderUUID)) {
                sender.sendMessage(this.messages.getRecipientIgnoresSender());
                return true;
            }

            if (IgnoreCollections.isIgnoredPrivate(senderUUID, recipientUUID)) {
                sender.sendMessage(this.messages.getSenderIgnoresRecipient());
                return true;
            }

            if (this.disabled.contains(recipientUUID)) {
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

        this.process(sender, recipient, recipientUUID, isPlayerSender, message);

        return true;
    }

    private boolean hasCooldown(final Player sender) {
        return this.cooldownCollections.hasCooldown(sender, "gigachat.bypass.cooldown.pm", this.cooldownCollections.getPrivateCooldowns());
    }

    private void sendMessageHasCooldown(final Player sender) {
        final long timeLeftInMillis = this.cooldownCollections.getPrivateCooldowns()
                .get(sender.getUniqueId()) - System.currentTimeMillis();
        final int result = (int) (this.pmValues.getPmCooldown() / 1000 + timeLeftInMillis / 1000);
        final String restTime = Utils.getTime(result);
        final String message = this.messages.getCommandCooldownMessage().replace("{time}", restTime);
        sender.sendMessage(message);
    }

    private void processDisable(final CommandSender sender) {
        if (!(sender instanceof final Player player)) {
            sender.sendMessage(this.messages.getPlayerOnly());
            return;
        }

        final UUID senderUUID = player.getUniqueId();
        if (this.disabled.contains(senderUUID)) {
            this.disabled.remove(senderUUID);
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                try (final Connection connection = DatabaseHandler.getConnection()) {
                    DatabaseQueries.executeUpdateQuery(DatabaseQueries.REMOVE_PLAYER_FROM_DISABLED_PRIVATE_MESSAGES, connection, senderUUID.toString());
                } catch (final SQLException ex) {
                    ex.printStackTrace();
                }
            });
            sender.sendMessage(this.messages.getPmDisabled());
        } else {
            this.disabled.add(senderUUID);
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                try (final Connection connection = DatabaseHandler.getConnection()) {
                    DatabaseQueries.executeUpdateQuery(DatabaseQueries.ADD_PLAYER_TO_DISABLED_PRIVATE_MESSAGES, connection, senderUUID.toString());
                } catch (final SQLException ex) {
                    ex.printStackTrace();
                }
            });
            sender.sendMessage(this.messages.getPmEnabled());
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

    private void process(final CommandSender sender, final Player recipient,
                         final UUID recipientUUID, final boolean isPlayerSender, final String message) {
        final String recipientPrefix = this.plugin.getChat().getPlayerPrefix(recipient);
        final String recipientSuffix = this.plugin.getChat().getPlayerSuffix(recipient);

        final String formattedMessageForSender;
        final String formattedMessageForRecipient;
        final String formattedMessageForConsole;
        final String formattedMessageForSocialSpy;
        if (isPlayerSender) {
            final Player playerSender = (Player) sender;
            final String senderPrefix = this.plugin.getChat().getPlayerPrefix(playerSender);
            final String senderSuffix = this.plugin.getChat().getPlayerSuffix(playerSender);
            
            formattedMessageForSender = this.getFormattedMessage(playerSender, recipient, message, this.pmValues.getSenderFormat(), senderPrefix, senderSuffix, recipientPrefix, recipientSuffix);
            formattedMessageForRecipient = this.getFormattedMessage(playerSender, recipient, message, this.pmValues.getRecipientFormat(), senderPrefix, senderSuffix, recipientPrefix, recipientSuffix);
            formattedMessageForConsole = this.getFormattedMessage(playerSender, recipient, message, this.pmValues.getConsoleFormat(), senderPrefix, senderSuffix, recipientPrefix, recipientSuffix);
            formattedMessageForSocialSpy = this.getFormattedMessage(playerSender, recipient, message, this.pmValues.getSocialSpyFormat(), senderPrefix, senderSuffix, recipientPrefix, recipientSuffix);

            final UUID senderUUID = ((Player) sender).getUniqueId();
            ReplyCollection.add(recipientUUID, senderUUID);
            this.processLogs(formattedMessageForConsole);
            SocialSpyCollection.sendAll(playerSender, recipient, formattedMessageForSocialSpy);
        } else {
            formattedMessageForSender = this.getFormattedMessage(sender, recipient, message, this.pmValues.getSenderFormat(), recipientPrefix, recipientSuffix);
            formattedMessageForRecipient = this.getFormattedMessage(sender, recipient, message, this.pmValues.getRecipientFormat(), recipientPrefix, recipientSuffix);
        }

        sender.sendMessage(formattedMessageForSender);
        recipient.sendMessage(formattedMessageForRecipient);

        this.playSound(recipient);
    }

    public String getFormattedMessage(final Player player, final Player recipient,
                                      final String message, final String format,
                                      final String senderPrefix, final String senderSuffix,
                                      final String recipientPrefix, final String recipientSuffix) {
        final String formatted = this.hexColorizer.colorize(
                Utils.replacePlaceholders(
                        player,
                        format.replace("{from-prefix}", senderPrefix)
                                .replace("{from-name}", player.getName())
                                .replace("{from-suffix}", senderSuffix)
                                .replace("{to-prefix}", recipientPrefix)
                                .replace("{to-name}", recipient.getName())
                                .replace("{to-suffix}", recipientSuffix)
                )
        );

        return formatted.replace("{message}", message);
    }

    public String getFormattedMessage(final CommandSender sender, final Player recipient, 
                                      final String message, final String format, 
                                      final String recipientPrefix, final String recipientSuffix) {
        final String formatted = this.hexColorizer.colorize(
                format.replace("{from-prefix}", "")
                        .replace("{from-name}", sender.getName())
                        .replace("{from-suffix}", "")
                        .replace("{to-prefix}", recipientPrefix)
                        .replace("{to-name}", recipient.getName())
                        .replace("{to-suffix}", recipientSuffix)
        );

        return formatted.replace("{message}", message);
    }

    private String getMessage(final CommandSender sender, final String[] args, final boolean isPlayerSender) {
        if (isPlayerSender) {
            return this.pmValues.getMessagesColorizer().colorize((Player) sender, String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim());
        }

        return this.hexColorizer.colorize(String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim());
    }

    private void playSound(final Player recipient) {
        if (pmValues.isSoundEnabled()) {
            final Sound sound = this.pmSoundsCollection.getSound(recipient.getUniqueId());
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
            final String input = args[0];
            final List<String> completions = new ArrayList<>();

            if (sender instanceof final Player playerSender) {
                final UUID senderUUID = playerSender.getUniqueId();
                for (final Player target : Bukkit.getOnlinePlayers()) {
                    final UUID targetUUID = target.getUniqueId();
                    if (IgnoreCollections.isIgnoredPrivate(senderUUID, targetUUID) || IgnoreCollections.isIgnoredPrivate(targetUUID, senderUUID)) {
                        continue;
                    }

                    final String targetName = target.getName();
                    if (Utils.startsWithIgnoreCase(input, targetName) && !this.vanishChecker.isVanished(target)) {
                        completions.add(targetName);
                    }
                }

                if (sender.hasPermission("gigachat.disable.pm") && Utils.startsWithIgnoreCase(input, "disable")) {
                    completions.add("disable");
                }
            } else {
                for (final Player target : Bukkit.getOnlinePlayers()) {
                    final String targetName = target.getName();
                    if (Utils.startsWithIgnoreCase(input, targetName)) {
                        completions.add(targetName);
                    }
                }
            }

            return completions;
        }

        return List.of();
    }
}
