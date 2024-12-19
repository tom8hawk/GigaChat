package groundbreaking.gigachat.constructors;

import net.md_5.bungee.api.chat.ClickEvent;

public record Hover(
        boolean isEnabled,
        ClickEvent.Action clickAction,
        String clickValue,
        String hoverText
) {
}
