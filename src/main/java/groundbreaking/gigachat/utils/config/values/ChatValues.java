package groundbreaking.gigachat.utils.config.values;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.utils.colorizer.basic.IColorizer;
import groundbreaking.gigachat.utils.colorizer.messages.AbstractColorizer;
import groundbreaking.gigachat.utils.colorizer.messages.ChatColorizer;
import groundbreaking.gigachat.utils.config.ConfigLoader;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Getter
public final class ChatValues {

    @Getter(AccessLevel.NONE)
    private final GigaChat plugin;

    private String localFormat, localSpyFormat, globalFormat;

    private int localDistance, caseCheckThreshold;

    private int localCooldown, globalCooldown;

    private char globalSymbol;

    private boolean
            noOneHearEnabled,
            noOneHearHideHidden,
            noOneHearHideVanished,
            noOneHearHideSpectators,
            caseCheckBlockMessage;

    private boolean isGlobalForce;

    private final Map<String, String>
            localGroupsColors = new HashMap<>(),
            globalGroupsColors = new HashMap<>();

    private String priority;

    private IColorizer formatsColorizer;

    private final AbstractColorizer chatsColorizer;

    private boolean isCaseCheckEnabled, isHoverEnabled, isAdminHoverEnabled;

    private String
            hoverText, hoverAction, hoverValue,
            adminHoverText, adminHoverAction, adminHoverValue;

    public ChatValues(final GigaChat plugin) {
        this.plugin = plugin;
        this.chatsColorizer = new ChatColorizer(plugin);
    }

    public void setValues() {
        final FileConfiguration config = new ConfigLoader(this.plugin).loadAndGet("chats", 1.0);

        this.setupLocal(config);
        this.setupGlobal(config);
        this.setupSettings(config);
        this.setupHover(config);
        this.setupCaseCheck(config);
        this.setupAdminHover(config);
    }

    private void setupSettings(final FileConfiguration config) {
        final ConfigurationSection settings = config.getConfigurationSection("settings");
        if (settings != null) {
            this.priority = settings.getString("listener-priority").toUpperCase(Locale.ENGLISH);
            this.formatsColorizer = plugin.getColorizer(config, "settings.serializer-for-formats");
        }
        else {
            this.plugin.getMyLogger().warning("Failed to load section \"settings\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupLocal(final FileConfiguration config) {
        final ConfigurationSection local = config.getConfigurationSection("local");
        if (local != null) {
            this.localFormat = local.getString("format");
            this.localSpyFormat = local.getString("spy-format");
            this.localDistance = local.getInt("distance");
            this.localCooldown = local.getInt("cooldown");
            this.noOneHearEnabled = local.getBoolean("no-one-hear-you.enable");
            this.noOneHearHideHidden = local.getBoolean("no-one-hear-you.hide-hidden");
            this.noOneHearHideVanished = local.getBoolean("no-one-hear-you.hide-vanished");
            this.noOneHearHideSpectators = local.getBoolean("no-one-hear-you.hide-spectators");

            final ConfigurationSection groupsColors = local.getConfigurationSection("groups-colors");
            if (groupsColors != null) {
                this.localGroupsColors.clear();
                for (String key : groupsColors.getKeys(false)) {
                    this.localGroupsColors.put(key, groupsColors.getString(key));
                }
            }
            else {
                this.plugin.getMyLogger().warning("Failed to load section \"local.groups-colors\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
                this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
            }
        }
        else {
            this.plugin.getMyLogger().warning("Failed to load section \"local\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupGlobal(final FileConfiguration config) {
        final ConfigurationSection global = config.getConfigurationSection("global");
        if (global != null) {
            this.globalFormat = global.getString("format");
            this.globalSymbol = global.getString("symbol").charAt(0);
            this.isGlobalForce = global.getBoolean("force");
            this.globalCooldown = global.getInt("cooldown");

            final ConfigurationSection groupsColors = global.getConfigurationSection("groups-colors");
            if (groupsColors != null) {
                this.globalGroupsColors.clear();
                for (String key : groupsColors.getKeys(false)) {
                    this.globalGroupsColors.put(key, groupsColors.getString(key));
                }
            }
            else {
                this.plugin.getMyLogger().warning("Failed to load section \"local.groups-colors\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
                this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
            }
        }
        else {
            this.plugin.getMyLogger().warning("Failed to load section \"global\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupCaseCheck(final FileConfiguration config) {
        final ConfigurationSection caseCheck = config.getConfigurationSection("case-check");
        if (caseCheck != null) {
            this.isCaseCheckEnabled = caseCheck.getBoolean("enable");
            this.caseCheckThreshold = caseCheck.getInt("threshold");
            this.caseCheckBlockMessage = caseCheck.getBoolean("block-message");
        }
        else {
            this.plugin.getMyLogger().warning("Failed to load section \"case-check\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupHover(final FileConfiguration config) {
        final ConfigurationSection hover = config.getConfigurationSection("hover");
        if (hover != null) {
            this.isHoverEnabled = hover.getBoolean("enable");
            this.hoverAction = hover.getString("click-action");
            this.hoverValue = hover.getString("click-value");
            this.hoverText = hover.getString("text");
        }
        else {
            this.plugin.getMyLogger().warning("Failed to load section \"hover\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupAdminHover(final FileConfiguration config) {
        final ConfigurationSection hover = config.getConfigurationSection("admin-hover");
        if (hover != null) {
            this.isAdminHoverEnabled = hover.getBoolean("enable");
            this.adminHoverAction = hover.getString("click-action");
            this.adminHoverValue = hover.getString("click-value");
            this.adminHoverText = hover.getString("text");
        }
        else {
            this.plugin.getMyLogger().warning("Failed to load section \"hover\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }
}
