package com.github.groundbreakingmc.gigachat.utils;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.List;
import java.util.Set;

public final class StringValidator {

    private boolean isCharsValidatorEnabled;
    private final Set<Character> allowedCharsHashSet = new ObjectOpenHashSet<>();
    private char charsCensorshipChar;

    private boolean isCapsValidatorEnabled;
    private int capsValidatorMaxPercentage;

    private boolean isWordsValidatorEnabled;
    private final Set<String> blockedWords = new ObjectOpenHashSet<>();
    private String wordsCensorshipChar;

    public boolean hasBlockedChars(final String message) {
        if (!this.isCharsValidatorEnabled) {
            return false;
        }

        final int messageLength = message.length();
        for (int i = 0; i < messageLength; i++) {
            final char currentChar = message.charAt(i);
            if (currentChar != ' ' && !this.allowedCharsHashSet.contains(currentChar)) {
                return true;
            }
        }

        return false;
    }

    public String getFormattedCharsMessage(final String message) {
        final int messageLength = message.length();
        final StringBuilder stringBuilder = new StringBuilder(messageLength);
        for (int i = 0; i < messageLength; i++) {
            final char currentChar = message.charAt(i);
            if (currentChar == ' ' || this.allowedCharsHashSet.contains(currentChar)) {
                stringBuilder.append(currentChar);
            } else {
                stringBuilder.append(this.charsCensorshipChar);
            }
        }

        return stringBuilder.toString();
    }

    public boolean isUpperCasePercentageExceeded(final String message) {
        if (!this.isCapsValidatorEnabled) {
            return false;
        }

        final int totalChars = message.length();
        int uppercaseCount = 0;
        for (int i = 0; i < totalChars; i++) {
            final char currentChar = message.charAt(i);
            if (Character.isUpperCase(currentChar)) {
                uppercaseCount++;
            }
        }

        final double percentage = (double) uppercaseCount / totalChars * 100;

        return percentage > capsValidatorMaxPercentage;
    }

    public boolean hasBlockedWords(final String message) {
        if (!this.isWordsValidatorEnabled) {
            return false;
        }

        for (final String blockedWord : this.blockedWords) {
            final int index = message.indexOf(blockedWord);
            if (index != -1) {
                return true;
            }
        }

        return false;
    }

    public String getFormattedWordsMessage(final String message) {
        final StringBuilder result = new StringBuilder(message);
        for (final String blockedWord : this.blockedWords) {
            int index = result.indexOf(blockedWord);

            while (index != -1) {
                final int length = blockedWord.length();
                final String censorshipChars = this.wordsCensorshipChar.repeat(length);
                result.replace(index, index + length, censorshipChars);
                index = result.indexOf(blockedWord, index + length);
            }
        }

        return result.toString();
    }

    public void setupCharsValidator(final boolean isCharsValidatorEnabled, final char[] allowedChars, final char charsCensorshipChar) {
        this.isCharsValidatorEnabled = isCharsValidatorEnabled;

        this.allowedCharsHashSet.clear();
        for (int i = 0; i < allowedChars.length; i++) {
            final char currentChar = allowedChars[i];
            this.allowedCharsHashSet.add(currentChar);
        }

        this.charsCensorshipChar = charsCensorshipChar;
    }

    public void setupCapsValidator(final boolean isCapsValidatorEnabled, final int capsValidatorMaxPercentage) {
        this.isCapsValidatorEnabled = isCapsValidatorEnabled;
        this.capsValidatorMaxPercentage = capsValidatorMaxPercentage;
    }

    public void setupWordsValidator(final boolean isWordsValidatorEnabled, final List<String> blockedWords, final char wordsCensorshipChar) {
        this.isWordsValidatorEnabled = isWordsValidatorEnabled;
        this.blockedWords.clear();
        this.blockedWords.addAll(blockedWords);
        this.wordsCensorshipChar = wordsCensorshipChar + "";
    }
}
