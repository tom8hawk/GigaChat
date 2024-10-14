package groundbreaking.gigachat.utils;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.Set;

public class StringUtil {

    private final Set<Character> allowedCharsHashSet = new ObjectOpenHashSet<>();

    public boolean isValid(final String message) {
        final int messageLength = message.length();
        for (int i = 0; i < messageLength; i++) {
            final char currentChar = message.charAt(i);
            if (currentChar != ' ' && !allowedCharsHashSet.contains(currentChar)) {
                return false;
            }
        }

        return true;
    }

    public String getFormattedMessage(final String message) {
        final int messageLength = message.length();
        final StringBuilder stringBuilder = new StringBuilder(messageLength);
        for (int i = 0; i < messageLength; i++) {
            final char currentChar = message.charAt(i);
            if (currentChar == ' ' || allowedCharsHashSet.contains(currentChar)) {
                stringBuilder.append(currentChar);
            } else {
                stringBuilder.append('*');
            }
        }

        return stringBuilder.toString();
    }

    public boolean isUpperCasePercentageExceeded(final String message, final double maxPercentage) {
        final int totalChars = message.length();
        int uppercaseCount = 0;
        for (int i = 0; i < totalChars; i++) {
            final char currentChar = message.charAt(i);
            if (Character.isUpperCase(currentChar)) {
                uppercaseCount++;
            }
        }

        final double percentage = (double) uppercaseCount / totalChars * 100;

        return percentage > maxPercentage;
    }

    public void setupChars(final char[] allowedChars) {
        for (int i = 0; i < allowedChars.length; i++) {
            final char currentChar = allowedChars[i];
            allowedCharsHashSet.add(currentChar);
        }
    }
}
