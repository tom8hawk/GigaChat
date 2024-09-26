package groundbreaking.mychat.utils;

import groundbreaking.mychat.utils.colorizer.IColorizer;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.logging.Logger;

public class ConfigValues {

    @Getter
    private String
            localFormat,
            globalFormat,
            pmSenderFormat,
            pmRecipientFormat,
            pmSocialSpyFormat,
            pmConsoleFormat;

    @Getter
    private int
            chatRadius,
            globalCooldown,
            localCooldown,
            newbieChatCooldown,
            newbieCommandsCooldown;

    @Getter
    private boolean
            forceGlobal,
            hoverTextEnable,
            newbieChatEnable,
            newbieCommandsEnable,
            autoMessageEnable,
            isAutoMessagesRandom,
            printPmToConsole,
            pmSoundEnabled = true;

    @Getter
    public char globalSymbol;

    @Getter
    private String
            clickAction,
            clickValue,
            cooldownMessage,
            hoverMessage,
            newbieChatMessage,
            newbieCommandsMessage;

    @Getter
    private final Set<String>
            newbieBlockedCommands = new HashSet<>();

    @Getter
    private final Map<String, String>
            localGroupsColors = new HashMap<>(),
            globalGroupsColors = new HashMap<>();

    @Getter
    private final Int2ObjectOpenHashMap<List<String>>
            autoMessages = new Int2ObjectOpenHashMap<>();

    @Getter
    private String
            playerOnly,
            noPermissionMessage,
            reloadMessage,
            nobodyToAnswer,
            playerNotFoundMessage,
            cannotPmSelf,
            cannotIgnoreSelf,
            recipientIgnoresSender,
            senderIgnoresRecipient,
            socialspyEnabled,
            socialspyDisabled,
            isNowIgnoring,
            isNotMoreIgnored,
            pmUsageError,
            socialspyUsageError,
            ignoreUsageError,
            replyUsageError;

    @Getter
    private static String
            hoursText,
            minutesText,
            secondsText;

    @Getter
    private Sound pmSound;

    @Getter
    private float
            pmSoundVolume,
            pmSoundPitch;

    public void setupValues(IColorizer colorizer, FileConfiguration config, Logger logger) {
        setupLocal(config, logger);
        setupGlobal(config, logger);
        setupLocalGroupsColors(config, logger);
        setupGlobalGroupsColors(config, logger);
        setupHover(config, logger);
        setupNewbieChat(colorizer, config, logger);
        setupNewbieCommands(colorizer, config, logger);
        setupAutoMessage(colorizer, config, logger);
        setupPrivateMessages(config, logger);
        setupMessages(colorizer, config, logger);
    }

    public void setupLocal(FileConfiguration config, Logger logger) {
        final ConfigurationSection local = config.getConfigurationSection("local");
        if (local != null) {
            localFormat = local.getString("format");
            chatRadius = local.getInt("distance");
            localCooldown = local.getInt("cooldown");
        }
        else {
            logger.warning("\u001b[91mFailed to load values from \"local\" section. Please check your configuration file, or delete it and restart your server!\u001b[0m");
        }
    }

    public void setupGlobal(FileConfiguration config, Logger logger) {
        final ConfigurationSection global = config.getConfigurationSection("global");
        if (global != null) {
            globalFormat = global.getString("format");
            globalSymbol = global.getString("symbol").charAt(0);
            forceGlobal = global.getBoolean("force");
            globalCooldown = global.getInt("cooldown");
        }
        else {
            logger.warning("\u001b[91mFailed to load values from \"local\" section. Please check your configuration file, or delete it and restart your server!\u001b[0m");
        }
    }

    public void setupLocalGroupsColors(FileConfiguration config, Logger logger) {
        final ConfigurationSection groupColors = config.getConfigurationSection("groupColors.local");
        if (groupColors != null) {
            localGroupsColors.clear();
            for (String key : groupColors.getKeys(false)) {
                localGroupsColors.put(key, groupColors.getString(key));
            }
        }
        else {
            logger.warning("\u001b[91mFailed to load values from \"groupColors.local\" section. Please check your configuration file, or delete it and restart your server!\u001b[0m");
        }
    }

    public void setupGlobalGroupsColors(FileConfiguration config, Logger logger) {
        final ConfigurationSection groupColors = config.getConfigurationSection("groupColors.global");
        if (groupColors != null) {
            globalGroupsColors.clear();
            for (String key : groupColors.getKeys(false)) {
                globalGroupsColors.put(key, groupColors.getString(key));
            }
        }
        else {
            logger.warning("\u001b[91mFailed to load values from \"groupColors.global\" section. Please check your configuration file, or delete it and restart your server!\u001b[0m");
        }
    }

    public void setupHover(FileConfiguration config, Logger logger) {
        final ConfigurationSection hoverText = config.getConfigurationSection("hoverText");
        if (hoverText != null) {
            hoverTextEnable = hoverText.getBoolean("enable");
            clickAction = hoverText.getString("click-action");
            clickValue = hoverText.getString("click-value");
            hoverMessage = hoverText.getString("format");
        }
        else {
            logger.warning("\u001b[91mFailed to load values from \"hoverText\" section. Please check your configuration file, or delete it and restart your server!\u001b[0m");
        }
    }

    public void setupNewbieChat(IColorizer colorizer, FileConfiguration config, Logger logger) {
        final ConfigurationSection newbieChat = config.getConfigurationSection("newbieChat");
        if (newbieChat != null) {
            newbieChatEnable =  newbieChat.getBoolean("enable");
            newbieChatCooldown = newbieChat.getInt("cooldown");
            newbieChatMessage = colorizer.colorize(newbieChat.getString("denyMessage"));
        }
        else {
            logger.warning("\u001b[91mFailed to load values from \"newbieChat\" section. Please check your configuration file, or delete it and restart your server!\u001b[0m");
        }
    }

    public void setupNewbieCommands(IColorizer colorizer, FileConfiguration config, Logger logger) {
        final ConfigurationSection newbieCommands = config.getConfigurationSection("newbieCommands");
        if (newbieCommands != null) {
            newbieCommandsEnable =  newbieCommands.getBoolean("enable");
            newbieCommandsCooldown = newbieCommands.getInt("cooldown");
            newbieCommandsMessage = colorizer.colorize(newbieCommands.getString("denyMessage"));
            newbieBlockedCommands.clear();
            newbieBlockedCommands.addAll(newbieCommands.getStringList("blockedCommands"));
        }
        else {
            logger.warning("\u001b[91mFailed to load values from \"newbieCommands\" section. Please check your configuration file, or delete it and restart your server!\u001b[0m");
        }
    }

    public void setupAutoMessage(IColorizer colorizer, FileConfiguration config, Logger logger) {
        final ConfigurationSection autoMessage = config.getConfigurationSection("autoMessage");
        if (autoMessage != null) {
            autoMessageEnable = autoMessage.getBoolean("enable");
            if (!autoMessageEnable) {
                return;
            }

            final ConfigurationSection messagesSection = autoMessage.getConfigurationSection("messages");
            if (messagesSection != null) {
                autoMessages.clear();
                final List<String> keysList = messagesSection.getKeys(false).stream().toList();
                for (int i = 0; i < keysList.size(); i++) {
                    List<String> messages = messagesSection.getStringList(keysList.get(i));
                    for (int r = 0; r < messages.size(); r++) {
                        messages.set(r, colorizer.colorize(messages.get(r)));
                    }
                    this.autoMessages.put(i, messages);
                }
            }
            else {
                logger.warning("\u001b[91mFailed to load values from \"autoMessage.messages\" section. Please check your configuration file, or delete it and restart your server!\u001b[0m");
            }

            isAutoMessagesRandom = autoMessage.getBoolean("random");
        }
        else {
            logger.warning("\u001b[91mFailed to load values from \"autoMessage\" section. Please check your configuration file, or delete it and restart your server!\u001b[0m");
        }
    }

    public void setupPrivateMessages(FileConfiguration config, Logger logger) {
        final ConfigurationSection privateMessages = config.getConfigurationSection("privateMessages");
        if (privateMessages != null) {
            pmSenderFormat = privateMessages.getString("sender-format");
            pmRecipientFormat = privateMessages.getString("recipient-format");
            pmSocialSpyFormat = privateMessages.getString("socialspy-format");
            pmConsoleFormat = privateMessages.getString("console-format");
            printPmToConsole = privateMessages.getBoolean("print-to-console");
            final String soundString = privateMessages.getString("sound");
            if (soundString == null) {
                logger.warning("\u001b[91mFailed to load sound for private messages from \"privateMessages.sound\". Sounds for private messages will be disabled!\u001b[0m");
                pmSoundEnabled = false;
            } else if (soundString.equalsIgnoreCase("disabled")) {
                pmSoundEnabled = false;
            } else {
                final String[] params = soundString.split(";");
                pmSound = params.length == 1 && params[0] != null ? Sound.valueOf(params[0].toUpperCase(Locale.ENGLISH)) : Sound.BLOCK_BREWING_STAND_BREW;
                pmSoundVolume = params.length == 2 && params[1] != null ? Float.parseFloat(params[1]) : 1.0f;
                pmSoundPitch = params.length == 3 && params[2] != null ? Float.parseFloat(params[2]) : 1.0f;
            }
        }
        else {
            logger.warning("\u001b[91mFailed to load values from \"privateMessages\" section. Please check your configuration file, or delete it and restart your server!\u001b[0m");
        }
    }

    public void setupMessages(IColorizer colorizer, FileConfiguration config, Logger logger) {
        final ConfigurationSection messages = config.getConfigurationSection("messages");
        if (messages != null) {
            playerOnly = colorizer.colorize(messages.getString("player-only"));
            noPermissionMessage = colorizer.colorize(messages.getString("no-perm"));
            nobodyToAnswer = colorizer.colorize(messages.getString("nobody-to-answer"));
            playerNotFoundMessage = colorizer.colorize(messages.getString("player-not-found"));
            cooldownMessage = colorizer.colorize(config.getString("cooldown-message"));
            cannotPmSelf = colorizer.colorize(messages.getString("cannot-pm-self"));
            cannotIgnoreSelf = colorizer.colorize(messages.getString("cannot-ignore-self"));
            recipientIgnoresSender = colorizer.colorize(messages.getString("recipient-ignores-sender"));
            senderIgnoresRecipient = colorizer.colorize(messages.getString("sender-ignores-recipient"));
            reloadMessage = colorizer.colorize(messages.getString("reload"));
            socialspyEnabled = colorizer.colorize(messages.getString("socialspy-enabled"));
            socialspyDisabled = colorizer.colorize(messages.getString("socialspy-disabled"));
            isNowIgnoring = colorizer.colorize(messages.getString("ignore-enabled"));
            isNotMoreIgnored = colorizer.colorize(messages.getString("ignore-disabled"));
            pmUsageError = colorizer.colorize(messages.getString("pm-usage-error"));
            socialspyUsageError = colorizer.colorize(messages.getString("socialspy-usage-error"));
            ignoreUsageError = colorizer.colorize(messages.getString("ignore-usage-error"));
            replyUsageError = colorizer.colorize(messages.getString("reply-usage-error"));
            hoursText = messages.getString("time.hours");
            minutesText = messages.getString("time.minutes");
            secondsText = messages.getString("time.seconds");
        }
        else {
            logger.warning("\u001b[91mFailed to load values from \"messages\" section. Please check your configuration file, or delete it and restart your server!\u001b[0m");
        }
    }
}
