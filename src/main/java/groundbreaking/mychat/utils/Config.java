package groundbreaking.mychat.utils;

import groundbreaking.mychat.MyChat;
import groundbreaking.mychat.utils.colorizer.IColorizer;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class Config {

    @Getter
    private String localFormat, globalFormat, pmSenderFormat, pmRecipientFormat, pmSocialSpyFormat;

    @Getter
    private int chatRadius;

    @Getter
    private boolean newbieChatEnable, newbieCommandsEnable;

    @Getter
    private boolean autoMessageEnable, hoverTextEnable;

    @Getter
    public char globalSymbol;
    
    @Getter
    private boolean forceGlobal, isAutoMessagesRandom;

    @Getter
    private int globalCooldown, localCooldown, newbieChatCooldown, newbieCommandsCooldown;

    @Getter
    private String cooldownMessage;

    @Getter
    private String newbieChatMessage, newbieCommandsMessage;

    @Getter
    private final Set<String> newbieBlockedCommands = new HashSet<>();

    @Getter
    private String hoverMessage;

    @Getter
    private final Map<String, String> groupsColors = new HashMap<>();

    @Getter
    private final Int2ObjectOpenHashMap<List<String>> autoMessages = new Int2ObjectOpenHashMap<>();

    @Getter
    private String
            playerOnly, noPermissionMessage, reloadMessage, playerNotFoundMessage, cannotPmSelf, cannotIgnoreSelf,
            recipientIgnoresSender, senderIgnoresRecipient, socialspyEnabled, socialspyDisabled, isNowIgnoring, IsNotMoreIgnored;

    @Getter
    private static String hoursText, minutesText, secondsText;

    @Getter
    private Sound pmSound;

    @Getter
    private float pmSoundVolume, pmSoundPitch;

    private final IColorizer colorizer;

    public Config(MyChat plugin) {
        this.colorizer = plugin.getColorizer();
    }

    public void setupMessages(FileConfiguration config) {
        ConfigurationSection messages = config.getConfigurationSection("messages");
        playerOnly = colorizer.colorize(messages.getString("player-only"));
        noPermissionMessage = colorizer.colorize(messages.getString("no-perm"));
        playerNotFoundMessage = colorizer.colorize(messages.getString("player-not-found"));
        cannotPmSelf = colorizer.colorize(messages.getString("cannot-pm-self"));
        cannotIgnoreSelf = colorizer.colorize(messages.getString("cannot-ignore-self"));
        recipientIgnoresSender = colorizer.colorize(messages.getString("recipient-ignores-sender"));
        senderIgnoresRecipient = colorizer.colorize(messages.getString("sender-ignores-recipient"));
        reloadMessage = colorizer.colorize(messages.getString("reload"));
        socialspyEnabled = colorizer.colorize(messages.getString("socialspy-enabled"));
        socialspyDisabled = colorizer.colorize(messages.getString("socialspy-disabled"));
        isNowIgnoring = colorizer.colorize(messages.getString("ignore-enabled"));
        IsNotMoreIgnored = colorizer.colorize(messages.getString("ignore-disabled"));
        hoursText = messages.getString("time.hours");
        minutesText = messages.getString("time.minutes");
        secondsText = messages.getString("time.seconds");
    }

    public void setupPrivateMessages(FileConfiguration config) {
        ConfigurationSection privateMessages = config.getConfigurationSection("privateMessages");
        pmSenderFormat = privateMessages.getString("sender-format");
        pmRecipientFormat = privateMessages.getString("recipient-format");
        pmSocialSpyFormat = privateMessages.getString("socialspy-format");
        String[] params = privateMessages.getString("sound").split(";");
        pmSound = Sound.valueOf(params[0]);
        pmSoundVolume = Float.parseFloat(params[1]);
        pmSoundPitch = Float.parseFloat(params[0]);
    }

    public void setupFormats(FileConfiguration config) {
        ConfigurationSection local = config.getConfigurationSection("local");
        localFormat = local.getString("format");
        chatRadius = local.getInt("distance");
        localCooldown = local.getInt("cooldown");

        ConfigurationSection global = config.getConfigurationSection("global");
        globalFormat = global.getString("format");
        globalSymbol = global.getString("symbol").charAt(0);
        forceGlobal = global.getBoolean("force");
        globalCooldown = global.getInt("cooldown");

        cooldownMessage = colorizer.colorize(config.getString("cooldownMessage"));

        ConfigurationSection groupColors = config.getConfigurationSection("groupColors");
        for (String key : groupColors.getKeys(false)) {
            groupsColors.put(key, groupColors.getString(key));
        }
    }

    public void setupHover(FileConfiguration config) {
        ConfigurationSection hoverText = config.getConfigurationSection("hoverText");
        hoverTextEnable = hoverText.getBoolean("enable");
        hoverMessage = hoverText.getString("format");
    }

    public void setupNewbie(FileConfiguration config) {
        ConfigurationSection newbieChat = config.getConfigurationSection("newbieChat");
        newbieChatEnable =  newbieChat.getBoolean("enable");
        newbieChatCooldown = newbieChat.getInt("cooldown");
        newbieChatMessage = colorizer.colorize(newbieChat.getString("denyMessage"));

        ConfigurationSection newbieCommands = config.getConfigurationSection("newbieCommands");
        newbieCommandsEnable =  newbieCommands.getBoolean("enable");
        newbieCommandsCooldown = newbieCommands.getInt("cooldown");
        newbieCommandsMessage = colorizer.colorize(newbieCommands.getString("denyMessage"));
        newbieBlockedCommands.addAll(newbieCommands.getStringList("blockedCommands"));
    }

    public void setupAutoMessage(FileConfiguration config) {
        ConfigurationSection autoMessage = config.getConfigurationSection("autoMessage");
        autoMessageEnable = autoMessage.getBoolean("enable");
        if (!autoMessageEnable) {
            return;
        }

        ConfigurationSection messagesSection = autoMessage.getConfigurationSection("messages");
        for (String messageName : messagesSection.getKeys(false)) {
            if (!Utils.isNumeric(messageName)) {
                break;
            }
            int messageID = Integer.parseInt(messageName);

            List<String> messages = messagesSection.getStringList(messageName);
            for (int i = 0; i < messages.size(); i++) {
                messages.set(i, colorizer.colorize(messages.get(i)));
            }
            this.autoMessages.put(messageID, messagesSection.getStringList(messageName));
        }

        isAutoMessagesRandom = autoMessage.getBoolean("random");
    }
}
