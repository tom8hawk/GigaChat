package com.github.groundbreakingmc.gigachat.listeners;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.commands.args.DisableServerChatArgument;
import com.github.groundbreakingmc.gigachat.constructors.Chat;
import com.github.groundbreakingmc.gigachat.constructors.DenySound;
import com.github.groundbreakingmc.gigachat.constructors.Hover;
import com.github.groundbreakingmc.gigachat.utils.HoverUtils;
import com.github.groundbreakingmc.gigachat.utils.StringValidator;
import com.github.groundbreakingmc.gigachat.utils.Utils;
import com.github.groundbreakingmc.gigachat.utils.colorizer.basic.Colorizer;
import com.github.groundbreakingmc.gigachat.utils.config.values.ChatValues;
import com.github.groundbreakingmc.gigachat.utils.config.values.Messages;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class ChatListener implements Listener {

    private final GigaChat plugin;
    private final ChatValues chatValues;
    private final Messages messages;

    private boolean isRegistered = false;

    public ChatListener(final GigaChat plugin) {
        this.plugin = plugin;
        this.chatValues = plugin.getChatValues();
        this.messages = plugin.getMessages();
    }

    @EventHandler
    public void onMessageSend(final AsyncPlayerChatEvent event) {
        final Player sender = event.getPlayer();

        if (this.isDisabled(sender, event)) {
            return;
        }

        String message = event.getMessage();
        final Chat chat = this.getChat(message);

        if (chat.hasCooldown(sender, this.messages)) {
            event.setCancelled(true);
            return;
        }

        message = this.getValidMessage(sender, message, event);
        if (message == null) {
            return;
        }

        if (chat != this.chatValues.getDefaultChat()) {
            message = message.substring(1);
        }

        final Set<Player> recipients = chat.getRecipients(sender);

        final String prefix = this.plugin.getChat().getPlayerPrefix(sender);
        final String suffix = this.plugin.getChat().getPlayerSuffix(sender);
        final String groupName = this.plugin.getPerms().getPrimaryGroup(sender);
        final String color = chat.getColor(groupName);

        final String spyFormat = chat.getSpyFormat();
        if (spyFormat != null && !spyFormat.isEmpty()) {
            final Set<UUID> spyListenersUUIDs = chat.getSpyListeners();
            if (!spyListenersUUIDs.isEmpty()) {
                final Set<Player> spyListeners = spyListenersUUIDs.stream().map(Bukkit::getPlayer).collect(Collectors.toSet());
                spyListeners.remove(sender);
                this.sendSpy(sender, chat, message, spyFormat, spyListeners, prefix, suffix, color);
            }
        }

        final String chatFormat = chat.getFormat();
        final String formattedMessage = this.getFormattedMessage(sender, message, chatFormat, prefix, suffix, color);

        final Hover adminHover = chat.getAdminHover();
        if (adminHover.isEnabled()) {
            final Set<Player> adminRecipients = this.getAdminRecipients(recipients);
            this.sendHover(sender, formattedMessage, adminHover, adminRecipients, prefix, suffix, color);
        }

        chat.addCooldown(sender);

        final Hover hover = chat.getHover();
        if (hover.isEnabled()) {
            this.sendHover(sender, formattedMessage, hover, recipients, prefix, suffix, color);
            this.plugin.getServer().getConsoleSender().sendMessage(formattedMessage);
            event.setCancelled(true);
        } else {
            event.getRecipients().clear();
            event.getRecipients().addAll(recipients);
            event.setFormat(formattedMessage);
        }
        if (chat.isNoOneHeard(sender, recipients, this.plugin.getVanishChecker())) {
            sender.sendMessage(this.messages.getNoOneHear());
        }
    }

    private boolean isDisabled(final Player sender, final AsyncPlayerChatEvent event) {
        if (DisableServerChatArgument.isChatDisabled() && !sender.hasPermission("gigachat.bypass.disabledchat")) {
            sender.sendMessage(this.messages.getServerChatIsDisabled());
            event.setCancelled(true);
            return true;
        }

        return false;
    }

    private Chat getChat(final String message) {
        final char firstChar = message.charAt(0);
        return this.chatValues.getChats().getOrDefault(firstChar, this.chatValues.getDefaultChat());
    }

    private String getValidMessage(final Player sender, String message, final AsyncPlayerChatEvent event) {
        final StringValidator stringValidator = this.chatValues.getStringValidator();
        if (stringValidator.hasBlockedChars(message)) {
            if (this.chatValues.isCharsValidatorBlockMessage()) {
                final String denyMessage = this.messages.getCharsValidationFailedMessage();
                if (!denyMessage.isEmpty()) {
                    sender.sendMessage(denyMessage);
                }
                this.playDenySound(sender, this.chatValues.getCharValidatorDenySound());
                event.setCancelled(true);
                return null;
            }

            message = stringValidator.getFormattedCharsMessage(message);
        }

        if (stringValidator.isUpperCasePercentageExceeded(message)) {
            if (this.chatValues.isCapsValidatorBlockMessageSend()) {
                final String denyMessage = this.messages.getCharsValidationFailedMessage();
                if (!denyMessage.isEmpty()) {
                    sender.sendMessage(denyMessage);
                }
                this.playDenySound(sender, this.chatValues.getCapsValidatorDenySound());
                event.setCancelled(true);
                return null;
            }

            message = message.toLowerCase();
        }

        if (stringValidator.hasBlockedWords(message)) {
            if (this.chatValues.isWordsValidatorBlockMessageSend()) {
                final String denyMessage = this.messages.getWordsValidationFailedMessage();
                if (!denyMessage.isEmpty()) {
                    sender.sendMessage(denyMessage);
                }
                this.playDenySound(sender, this.chatValues.getWordsValidatorDenySound());
                event.setCancelled(true);
                return null;
            }

            message = stringValidator.getFormattedWordsMessage(message);
        }

        return message;
    }

    private void playDenySound(final Player player, final DenySound denySound) {
        if (denySound != null) {
            denySound.play(player);
        }
    }

    private void sendHover(final Player sender, final String message, final Hover hover, final Set<Player> recipients,
                           final String prefix, final String suffix, final String color) {
        final String hoverText = hover.hoverText()
                .replace("{player}", sender.getName())
                .replace("{prefix}", prefix)
                .replace("{suffix}", suffix)
                .replace("{color}", color);
        final Colorizer colorizer = this.chatValues.getFormatsColorizer();
        final BaseComponent[] components = HoverUtils.get(sender, hover, hoverText, message, colorizer);
        for (final Player recipient : recipients) {
            recipient.spigot().sendMessage(components);
        }
    }

    private void sendSpy(final Player sender, final Chat chat, final String message, final String spyFormat, final Set<Player> recipients,
                         final String prefix, final String suffix, final String color) {
        final String formattedMessage = this.getFormattedMessage(sender, message, spyFormat, prefix, suffix, color);

        final Hover hover = chat.getHover();
        if (hover.isEnabled()) {
            this.sendHover(sender, formattedMessage, hover, recipients, prefix, suffix, color);
            return;
        }

        for (final Player recipient : recipients) {
            recipient.sendMessage(formattedMessage);
        }
    }

    private Set<Player> getAdminRecipients(final Set<Player> recipients) {
        final Set<Player> adminRecipients = new HashSet<>();
        for (final Iterator<Player> iterator = recipients.iterator(); iterator.hasNext(); ) {
            final Player target = iterator.next();
            if (target.hasPermission("gigachat.adminhover")) {
                adminRecipients.add(target);
                iterator.remove();
            }
        }

        return adminRecipients;
    }

    public String getFormattedMessage(final Player sender, String message, final String format,
                                      final String prefix, final String suffix, final String color) {
        final String formattedMessage = this.chatValues.getFormatsColorizer().colorize(
                Utils.replacePlaceholders(
                        sender,
                        format.replace("{player}", sender.getName())
                                .replace("{prefix}", prefix)
                                .replace("{suffix}", suffix)
                                .replace("{color}", color)
                )
        );

        message = this.chatValues.getChatsColorizer().colorize(sender, message);
        return formattedMessage.replace("{message}", message);
    }
}
