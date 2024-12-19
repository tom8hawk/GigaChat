package com.github.groundbreakingmc.gigachat.constructors;

import java.util.List;

public record AutoMessageConstructor(
        List<String> autoMessage,
        String sound
) {
}
