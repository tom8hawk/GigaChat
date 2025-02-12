package com.github.groundbreakingmc.gigachat.utils.colorizer;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class ColorizerUtils {

    public static boolean isColorCharacter(char c) {
        return c >= '0' && c <= '9'
                || c >= 'a' && c <= 'f'
                || c >= 'A' && c <= 'F';
    }

    public static boolean isStyleCharacter(char c) {
        return c == 'r'
                || c >= 'k' && c <= 'o'
                || c == 'x'
                || c == 'R'
                || c >= 'K' && c <= 'O'
                || c == 'X';
    }
}
