package groundbreaking.gigachat.listeners;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.commands.args.DisableServerChatArgument;
import groundbreaking.gigachat.constructors.Chat;
import groundbreaking.gigachat.utils.StringUtil;
import groundbreaking.gigachat.utils.Utils;
import groundbreaking.gigachat.utils.config.values.ChatValues;
import groundbreaking.gigachat.utils.config.values.Messages;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.List;

public final class ChatListener implements Listener {

    private final GigaChat plugin;
    private final ChatValues chatValues;
    private final Messages messages;

    private boolean isRegistered = false;

    private final String[] placeholders = { "{player}", "{prefix}", "{suffix}", "{color}" };

    public ChatListener(final GigaChat plugin) {
        this.plugin = plugin;
        this.chatValues = plugin.getChatValues();
        this.messages = plugin.getMessages();
    }

    @EventHandler
    public void onMessageSend(final AsyncPlayerChatEvent event) {
        this.processEvent(event);
    }

    public void registerEvent() {
        if (this.isRegistered) {
            this.unregisterEvent();
        }

        final Class<? extends Event> eventClass = AsyncPlayerChatEvent.class;
        final EventPriority eventPriority = this.plugin.getEventPriority(this.chatValues.getPriority(), "chats.yml");

        this.plugin.getServer().getPluginManager().registerEvent(eventClass, this, eventPriority,
                (listener, event) -> this.onMessageSend((AsyncPlayerChatEvent) event),
                this.plugin
        );

        this.isRegistered = true;
    }

    private void unregisterEvent() {
        HandlerList.unregisterAll(this);
        this.isRegistered = false;
    }

    private void processEvent(final AsyncPlayerChatEvent event) {
        final Player sender = event.getPlayer();

        if (this.isDisabled(sender, event)) {
            return;
        }

        String message = event.getMessage();
        final Chat chat = this.getChat(message);

        if (chat.hasCooldown(sender, this.messages, event)) {
            return;
        }

        message = this.getValidMessage(sender, message, event);
        if (message == null) {
            return;
        }

        if (chat != this.chatValues.getDefaultChat()) {
            message = message.substring(1);
        }

        final List<Player> recipients = chat.getRecipients(sender);
        if (chat.isNoOneHeard(sender, recipients, plugin.getVanishChecker())) {
            sender.sendMessage(messages.getNoOneHear());
        }

        final String[] replacementList = this.getReplacements(sender, chat);

        final String spyFormat = chat.getSpyFormat();
        if (spyFormat != null && !spyFormat.isEmpty()) {
            final List<Player> spyListeners = chat.getSpyListeners();
            this.sendSpy(sender, message, spyFormat, spyListeners, replacementList);
        }

        final String chatFormat = chat.getFormat();
        final String formattedMessage = this.getFormattedMessage(sender, message, chatFormat, replacementList);

        if (chatValues.isAdminHoverEnabled()) {
            final String adminHoverText = this.chatValues.getAdminHoverText();
            final String adminHoverAction = this.chatValues.getHoverAction();
            final String adminHoverValue = this.chatValues.getHoverValue();
            final List<Player> adminRecipients = this.getAdminRecipients(recipients);

            this.sendHover(sender, formattedMessage, adminHoverText, adminHoverAction, adminHoverValue, adminRecipients, replacementList);
        }

        if (chatValues.isHoverEnabled()) {
            final String hoverText = this.chatValues.getHoverText();
            final String hoverAction = this.chatValues.getHoverAction();
            final String hoverValue = this.chatValues.getHoverValue();

            this.sendHover(sender, formattedMessage, hoverText, hoverAction, hoverValue, recipients, replacementList);

            event.setCancelled(true);
        } else {
            event.getRecipients().clear();
            event.getRecipients().addAll(recipients);
            event.setFormat(formattedMessage);
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
        return chatValues.getChats().getOrDefault(firstChar, chatValues.getDefaultChat());
    }

    private String[] getReplacements(final Player sender, final Chat chat) {
        final String name = sender.getName();
        final String prefix = this.plugin.getChat().getPlayerPrefix(sender);
        final String suffix = this.plugin.getChat().getPlayerSuffix(sender);
        final String groupName = this.plugin.getPerms().getPrimaryGroup(sender);
        final String color = chat.getColor(groupName);

        return new String[]{ name, prefix, suffix, color };
    }

    private String getValidMessage(final Player sender, String message, final AsyncPlayerChatEvent event) {
        final StringUtil stringUtil = this.chatValues.getStringUtil();
        if (stringUtil.hasBlockedChars(message)) {
            if (this.chatValues.isCharsValidatorBlockMessage()) {
                final String denyMessage = this.messages.getCharsValidationFailedMessage();
                if (!denyMessage.isEmpty()) {
                    sender.sendMessage(denyMessage);
                }
                this.playDenySoundCharsCheckFailed(sender);
                event.setCancelled(true);
                return null;
            }

            message = stringUtil.getFormattedCharsMessage(message);
        }

        if (stringUtil.isUpperCasePercentageExceeded(message)) {
            if (this.chatValues.isCapsValidatorBlockMessageSend()) {
                final String denyMessage = this.messages.getCharsValidationFailedMessage();
                if (!denyMessage.isEmpty()) {
                    sender.sendMessage(denyMessage);
                }
                this.playDenySoundCapsCheckFailed(sender);
                event.setCancelled(true);
                return null;
            }

            message = message.toLowerCase();
        }

        if (stringUtil.hasBlockedWords(message)) {
            if (this.chatValues.isWordsValidatorBlockMessageSend()) {
                final String denyMessage = this.messages.getWordsValidationFailedMessage();
                if (!denyMessage.isEmpty()) {
                    sender.sendMessage(denyMessage);
                }
                this.playDenySoundWordsCheckFailed(sender);
                event.setCancelled(true);
                return null;
            }

            message = stringUtil.getFormattedWordsMessage(message);
        }

        return message;
    }

    private void playDenySoundCharsCheckFailed(final Player messageSender) {
        if (this.chatValues.isCharsValidatorDenySoundEnabled()) {
            final Location location = messageSender.getLocation();
            final Sound sound = this.chatValues.getTextValidatorDenySound();
            final float volume = this.chatValues.getTextValidatorDenySoundVolume();
            final float pitch = this.chatValues.getTextValidatorDenySoundPitch();

            messageSender.playSound(location, sound, volume, pitch);
        }
    }

    private void playDenySoundCapsCheckFailed(final Player messageSender) {
        if (this.chatValues.isCapsValidatorDenySoundEnabled()) {
            final Location location = messageSender.getLocation();
            final Sound sound = this.chatValues.getCapsValidatorDenySound();
            final float volume = this.chatValues.getCapsValidatorDenySoundVolume();
            final float pitch = this.chatValues.getCapsValidatorDenySoundPitch();

            messageSender.playSound(location, sound, volume, pitch);
        }
    }

    private void playDenySoundWordsCheckFailed(final Player messageSender) {
        if (this.chatValues.isWordsValidatorDenySoundEnabled()) {
            final Location location = messageSender.getLocation();
            final Sound sound = this.chatValues.getWordsValidatorDenySound();
            final float volume = this.chatValues.getWordsValidatorDenySoundVolume();
            final float pitch = this.chatValues.getWordsValidatorDenySoundPitch();

            messageSender.playSound(location, sound, volume, pitch);
        }
    }

    private void sendHover(final Player player, final String formattedMessage, final String hoverText, final String hoverAction, String hoverValue, final List<Player> recipients, final String[] replacementList) {
        final String hoverString = this.chatValues.getFormatsColorizer().colorize(
                Utils.replacePlaceholders(
                        player,
                        Utils.replaceEach(hoverText, this.placeholders, replacementList)
                )
        );
        final ClickEvent.Action clickEventAction = ClickEvent.Action.valueOf(hoverAction);
        hoverValue = hoverValue.replace("{player}", player.getName());

        final HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText(hoverString)));
        final ClickEvent clickEvent = new ClickEvent(clickEventAction, hoverValue);
        final BaseComponent[] comp = TextComponent.fromLegacyText(formattedMessage);

        for (int i = 0; i < comp.length; i++) {
            comp[i].setHoverEvent(hoverEvent);
            comp[i].setClickEvent(clickEvent);
        }
        for (int i = 0; i < recipients.size(); i++) {
            recipients.get(i).spigot().sendMessage(comp);
        }

        this.plugin.getServer().getConsoleSender().sendMessage(formattedMessage);
    }

    private void sendSpy(final Player sender, final String message, final String spyFormat, final List<Player> recipients, final String[] replacementList) {
        final String formattedMessage = this.getFormattedMessage(sender, message, spyFormat, replacementList);

        if (this.chatValues.isHoverEnabled()) {
            final String hoverText = this.chatValues.getHoverText();
            final String hoverAction = this.chatValues.getHoverAction();
            final String hoverValue = this.chatValues.getHoverValue();

            this.sendHover(sender, formattedMessage, hoverText, hoverAction, hoverValue, recipients, replacementList);
            return;
        }

        for (int i = 0; i < recipients.size(); i++) {
            final Player recipient = recipients.get(i);
            recipient.sendMessage(formattedMessage);
        }
    }

    private List<Player> getAdminRecipients(final List<Player> recipients) {
        final List<Player> adminRecipients = new ArrayList<>();
        for (int i = recipients.size() - 1; i >= 0; i--) {
            final Player target = recipients.get(i);
            if (target.hasPermission("gigachat.adminhover")) {
                adminRecipients.add(target);
                recipients.remove(i);
            }
        }

        return adminRecipients;
    }

    public String getFormattedMessage(final Player messageSender, String message, final String format, final String[] replacementList) {
        final String formattedMessage = this.chatValues.getFormatsColorizer().colorize(
                Utils.replacePlaceholders(
                        messageSender,
                        Utils.replaceEach(format, this.placeholders, replacementList)
                )
        );

        message = this.chatValues.getChatsColorizer().colorize(messageSender, message);

        return formattedMessage.replace("{message}", message).replace("%", "%%");
    }
}
