package com.github.groundbreakingmc.gigachat.utils;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.constructors.Chat;
import com.github.groundbreakingmc.gigachat.constructors.DefaultChat;
import com.github.groundbreakingmc.gigachat.constructors.Hover;
import com.github.groundbreakingmc.gigachat.constructors.NoOneHead;
import com.github.groundbreakingmc.gigachat.exceptions.FormatNullException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;

public class ChatUtil {

    private ChatUtil() {

    }

    public static Chat createChat(final GigaChat plugin, final ConfigurationSection chatSection, final String chatKey, final DefaultChat defaultChat) {
        final Chat.ChatBuilder chatBuilder = Chat.builder(plugin);

        final String format = chatSection.getString("format");
        if (format == null) {
            throw new FormatNullException("Chat format cannot be null! Path: \"chats." + chatKey + ".format\"");
        }

        chatBuilder.setFormat(format);

        chatBuilder.setHover(getHover(chatSection, "hover", defaultChat.hover()));
        chatBuilder.setAdminHover(getHover(chatSection, "admin-hover", defaultChat.adminHover()));

        chatBuilder.setDistance(chatSection.getInt("distance"));
        chatBuilder.setChatCooldown(chatSection.getInt("chat-cooldown", defaultChat.chatCooldown()));

        chatBuilder.setSpyFormat(chatSection.getString("spy-format"));
        chatBuilder.setSpyCommand(chatSection.getString("spy-command"));
        chatBuilder.setSpyCooldown(chatSection.getInt("spy-cooldown", defaultChat.spyCooldown()));

        chatBuilder.setGroupColors(getGroupColors(chatSection, defaultChat.groupColors()));

        chatBuilder.setNoOneHeard(getNoOneHeard(chatSection, defaultChat.noOneHead()));

        return chatBuilder.build();
    }

    public static DefaultChat createDefaultChat(final GigaChat plugin, final ConfigurationSection chatsSection) {
        final ConfigurationSection defaultChatSection = chatsSection.getConfigurationSection("default");
        if (defaultChatSection != null) {
            final int spyCooldown = defaultChatSection.getInt("spy-cooldown");
            final int chatCooldown = defaultChatSection.getInt("chat-cooldown");

            final NoOneHead noOneHeard = getNoOneHeard(defaultChatSection, null);

            final Hover hover = getHover(defaultChatSection, "hover", null);
            final Hover adminHover = getHover(defaultChatSection, "admin-hover", null);

            final Map<String, String> groupColors = getGroupColors(defaultChatSection, null);

            return new DefaultChat(spyCooldown, chatCooldown, groupColors, noOneHeard, hover, adminHover);
        } else {
            plugin.getMyLogger().warning("Failed to load section \"chats.default\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
            return null;
        }
    }

    private static NoOneHead getNoOneHeard(final ConfigurationSection chatSection, final NoOneHead defaultNoOneHeard) {
        final ConfigurationSection noOneHeardSection = chatSection.getConfigurationSection("no-one-heard");
        if (noOneHeardSection != null) {
            final boolean isEnabled = noOneHeardSection.getBoolean("enable", defaultNoOneHeard != null && defaultNoOneHeard.isEnabled());
            final boolean hideHidden = noOneHeardSection.getBoolean("hide-hidden", defaultNoOneHeard != null && defaultNoOneHeard.hideHidden());
            final boolean hideVanished = noOneHeardSection.getBoolean("hide-vanished", defaultNoOneHeard != null && defaultNoOneHeard.hideVanished());
            final boolean hideSpectators = noOneHeardSection.getBoolean("hide-spectators", defaultNoOneHeard != null && defaultNoOneHeard.hideSpectators());

            return new NoOneHead(isEnabled, hideHidden, hideVanished, hideSpectators);
        }

        return defaultNoOneHeard;
    }

    private static Hover getHover(final ConfigurationSection chatSection, final String sectionName, final Hover defaultHover) {
        final ConfigurationSection hoverSection = chatSection.getConfigurationSection(sectionName);
        if (hoverSection != null) {
            final boolean isEnabled = hoverSection.getBoolean("enable", defaultHover != null && defaultHover.isEnabled());
            final ClickEvent.Action clickAction = ClickEvent.Action.valueOf(hoverSection.getString("click-action", defaultHover != null ? defaultHover.clickAction().name() : null));
            final String clickValue = hoverSection.getString("click-value", defaultHover != null ? defaultHover.clickValue() : null);
            final String hoverText = hoverSection.getString("text", defaultHover != null ? defaultHover.hoverText() : null);

            return new Hover(isEnabled, clickAction, clickValue, hoverText);
        }

        return defaultHover;
    }

    private static Map<String, String> getGroupColors(final ConfigurationSection chatSection, final Map<String, String> defaultGroupColors) {
        final ConfigurationSection groupsColorsSection = chatSection.getConfigurationSection("group-colors");
        if (groupsColorsSection != null) {
            final Map<String, String> groupsColors = new Object2ObjectOpenHashMap<>();
            for (final String groupKey : groupsColorsSection.getKeys(false)) {
                groupsColors.put(groupKey, groupsColorsSection.getString(groupKey));
            }

            return groupsColors;
        }

        return defaultGroupColors;
    }
}
