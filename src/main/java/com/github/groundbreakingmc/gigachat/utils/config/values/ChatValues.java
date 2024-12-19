package com.github.groundbreakingmc.gigachat.utils.config.values;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.commands.MainCommandHandler;
import com.github.groundbreakingmc.gigachat.constructors.Chat;
import com.github.groundbreakingmc.gigachat.constructors.DefaultChat;
import com.github.groundbreakingmc.gigachat.constructors.DenySound;
import com.github.groundbreakingmc.gigachat.listeners.ChatListener;
import com.github.groundbreakingmc.gigachat.utils.ChatUtil;
import com.github.groundbreakingmc.gigachat.utils.ListenerRegisterUtil;
import com.github.groundbreakingmc.gigachat.utils.StringValidator;
import com.github.groundbreakingmc.gigachat.utils.colorizer.basic.Colorizer;
import com.github.groundbreakingmc.gigachat.utils.colorizer.messages.ChatColorizer;
import com.github.groundbreakingmc.gigachat.utils.colorizer.messages.PermissionsColorizer;
import com.github.groundbreakingmc.gigachat.utils.config.ConfigLoader;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.EventExecutor;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Getter
public final class ChatValues {

    @Getter(AccessLevel.NONE)
    private final GigaChat plugin;

    private Colorizer formatsColorizer;

    private final PermissionsColorizer chatsColorizer;

    private boolean isCharsValidatorBlockMessage;
    private boolean capsValidatorBlockMessageSend;
    private boolean wordsValidatorBlockMessageSend;

    private DenySound charValidatorDenySound;
    private DenySound capsValidatorDenySound;
    private DenySound wordsValidatorDenySound;

    private final StringValidator stringValidator = new StringValidator();

    private final Object2ObjectOpenHashMap<Character, Chat> chats = new Object2ObjectOpenHashMap<>();
    private Chat defaultChat;

    public ChatValues(final GigaChat plugin) {
        this.plugin = plugin;
        this.chatsColorizer = new ChatColorizer(plugin);
    }

    public void setValues() {
        final FileConfiguration config = new ConfigLoader(this.plugin).loadAndGet("chats", 1.0);

        this.setupChats(config);
        this.setupSettings(config);
        this.setupValidators(config);
    }

    private void setupSettings(final FileConfiguration config) {
        final ConfigurationSection settings = config.getConfigurationSection("settings");
        if (settings != null) {
            final String priority = settings.getString("listener-priority").toUpperCase(Locale.ENGLISH);

            final ChatListener chatListener = this.plugin.getChatListener();
            final EventPriority eventPriority = this.plugin.getEventPriority(priority, "chats.yml");
            final EventExecutor eventExecutor = (listener, event) -> chatListener.onMessageSend((AsyncPlayerChatEvent) event);
            ListenerRegisterUtil.unregister(chatListener);
            ListenerRegisterUtil.register(this.plugin, chatListener, AsyncPlayerChatEvent.class, eventPriority, true, eventExecutor);

            this.formatsColorizer = this.plugin.getColorizer(config, "settings.serializer-for-formats");
        } else {
            this.plugin.getMyLogger().warning("Failed to load section \"settings\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupChats(final FileConfiguration config) {
        final ConfigurationSection chatsSection = config.getConfigurationSection("chats");
        if (chatsSection != null) {
            final Set<String> chatsKeys = chatsSection.getKeys(false);
            MainCommandHandler.CHATS.clear();
            MainCommandHandler.CHATS.addAll(chatsKeys);

            final DefaultChat defaultChatConstructor = ChatUtil.createDefaultChat(this.plugin, chatsSection);
            for (final String key : chatsKeys) {
                if (key.equals("default")) {
                    continue;
                }

                final ConfigurationSection keySection = chatsSection.getConfigurationSection(key);
                if (keySection == null) {
                    this.plugin.getMyLogger().warning("Failed to load section \"chats." + key + "\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
                    this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
                    continue;
                }

                final Chat chat = ChatUtil.createChat(this.plugin, keySection, key, defaultChatConstructor);

                final String symbolString = keySection.getString("symbol");
                if (symbolString.equalsIgnoreCase("default")) {
                    this.defaultChat = chat;
                } else {
                    chats.put(symbolString.charAt(0), chat);
                }
            }
        } else {
            this.plugin.getMyLogger().warning("Failed to load section \"chats\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupValidators(final FileConfiguration config) {
        final ConfigurationSection validators = config.getConfigurationSection("validators");
        if (validators != null) {
            this.setupCharsValidator(validators);
            this.setupCapsValidator(validators);
            this.setupWordsValidator(validators);
        } else {
            this.plugin.getMyLogger().warning("Failed to load section \"validators\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupCharsValidator(final ConfigurationSection validators) {
        final ConfigurationSection charsValidator = validators.getConfigurationSection("chars");
        if (charsValidator != null) {
            final boolean isCharsValidatorEnabled = charsValidator.getBoolean("enable");
            this.isCharsValidatorBlockMessage = charsValidator.getBoolean("block-message");
            final char textValidatorCensorshipChar = charsValidator.getString("censorship-char").charAt(0);
            final char[] textValidatorAllowedChars = charsValidator.getString("allowed").toCharArray();

            this.stringValidator.setupCharsValidator(isCharsValidatorEnabled, textValidatorAllowedChars, textValidatorCensorshipChar);

            this.setupCharsValidatorDenySound(charsValidator);
        } else {
            this.plugin.getMyLogger().warning("Failed to load section \"validators.chars\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupCapsValidator(final ConfigurationSection validators) {
        final ConfigurationSection caseValidator = validators.getConfigurationSection("caps");
        if (caseValidator != null) {
            final boolean isCapsValidatorEnabled = caseValidator.getBoolean("enable");
            final int capsValidatorMaxPercentage = caseValidator.getInt("max-percent");
            this.capsValidatorBlockMessageSend = caseValidator.getBoolean("block-message-send");

            this.stringValidator.setupCapsValidator(isCapsValidatorEnabled, capsValidatorMaxPercentage);

            this.setupCapsValidatorDenySound(caseValidator);
        } else {
            this.plugin.getMyLogger().warning("Failed to load section \"validators.caps\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupWordsValidator(final ConfigurationSection validators) {
        final ConfigurationSection wordsValidator = validators.getConfigurationSection("words");
        if (wordsValidator != null) {
            final boolean isWordsValidatorEnabled = wordsValidator.getBoolean("enable");
            final char wordsValidatorCensorshipChar = wordsValidator.getString("censorship-char").charAt(0);
            final List<String> wordsValidatorBlockedWords = wordsValidator.getStringList("blocked");
            this.wordsValidatorBlockMessageSend = wordsValidator.getBoolean("block-message-send");

            this.stringValidator.setupWordsValidator(isWordsValidatorEnabled, wordsValidatorBlockedWords, wordsValidatorCensorshipChar);

            this.setupWordsValidatorDenySound(wordsValidator);
        } else {
            this.plugin.getMyLogger().warning("Failed to load section \"validators.words\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupCharsValidatorDenySound(final ConfigurationSection caseCheck) {
        final String soundString = caseCheck.getString("deny-sound");
        if (soundString == null) {
            this.plugin.getMyLogger().warning("Failed to load sound on path \"validators.chars.deny-sound\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
            this.charValidatorDenySound = null;
        } else if (soundString.equalsIgnoreCase("disabled")) {
            this.charValidatorDenySound = null;
        } else {
            final String[] params = soundString.split(";");
            final Sound sound = params.length >= 1 ? Sound.valueOf(params[0].toUpperCase(Locale.ENGLISH)) : Sound.ENTITY_VILLAGER_NO;
            final float volume = params.length >= 2 ? Float.parseFloat(params[1]) : 1.0f;
            final float pitch = params.length >= 3 ? Float.parseFloat(params[2]) : 1.0f;
            this.charValidatorDenySound = new DenySound(sound, volume, pitch);
        }
    }

    private void setupCapsValidatorDenySound(final ConfigurationSection capsValidator) {
        final String soundString = capsValidator.getString("deny-sound");
        if (soundString == null) {
            this.plugin.getMyLogger().warning("Failed to load sound on path \"validators.caps.deny-sound\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
            this.capsValidatorDenySound = null;
        } else if (soundString.equalsIgnoreCase("disabled")) {
            this.capsValidatorDenySound = null;
        } else {
            final String[] params = soundString.split(";");
            final Sound sound = params.length >= 1 ? Sound.valueOf(params[0].toUpperCase(Locale.ENGLISH)) : Sound.ENTITY_VILLAGER_NO;
            final float volume = params.length >= 2 ? Float.parseFloat(params[1]) : 1.0f;
            final float pitch = params.length >= 3 ? Float.parseFloat(params[2]) : 1.0f;
            this.capsValidatorDenySound = new DenySound(sound, volume, pitch);
        }
    }

    private void setupWordsValidatorDenySound(final ConfigurationSection wordsValidator) {
        final String soundString = wordsValidator.getString("deny-sound");
        if (soundString == null) {
            this.plugin.getMyLogger().warning("Failed to load sound on path \"validators.words.deny-sound\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
            this.wordsValidatorDenySound = null;
        } else if (soundString.equalsIgnoreCase("disabled")) {
            this.wordsValidatorDenySound = null;
        } else {
            final String[] params = soundString.split(";");
            final Sound sound = params.length >= 1 ? Sound.valueOf(params[0].toUpperCase(Locale.ENGLISH)) : Sound.ENTITY_VILLAGER_NO;
            final float volume = params.length >= 2 ? Float.parseFloat(params[1]) : 1.0f;
            final float pitch = params.length >= 3 ? Float.parseFloat(params[2]) : 1.0f;
            this.wordsValidatorDenySound = new DenySound(sound, volume, pitch);
        }
    }
}
