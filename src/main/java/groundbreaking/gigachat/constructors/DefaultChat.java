package groundbreaking.gigachat.constructors;

import java.util.Map;

public record DefaultChat(
        int spyCooldown,
        int chatCooldown,
        Map<String, String> groupColors,
        NoOneHead noOneHead,
        Hover hover,
        Hover adminHover
) {
}
