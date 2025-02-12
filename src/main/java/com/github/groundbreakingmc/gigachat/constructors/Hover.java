package com.github.groundbreakingmc.gigachat.constructors;

import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.configuration.ConfigurationSection;

public record Hover(
        boolean isEnabled,
        ClickEvent.Action clickAction,
        String clickValue,
        String hoverText
) {
    public static Hover get(final ConfigurationSection section, final Hover defaultHover) {
        if (section != null) {
            final boolean isEnabled = section.getBoolean("enable", defaultHover != null && defaultHover.isEnabled());
            final String action = section.getString("click-action", defaultHover != null ? defaultHover.clickAction().name() : null);
            final String value = section.getString("click-value", defaultHover != null ? defaultHover.clickValue() : null);
            final String text = section.getString("text", defaultHover != null ? defaultHover.hoverText() : null);

            return new Hover(isEnabled, ClickEvent.Action.valueOf(action), value, text);
        }

        return defaultHover;
    }
}
