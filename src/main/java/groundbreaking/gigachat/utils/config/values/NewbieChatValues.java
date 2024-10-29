package groundbreaking.gigachat.utils.config.values;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.utils.colorizer.basic.IColorizer;
import groundbreaking.gigachat.utils.config.ConfigLoader;
import groundbreaking.gigachat.utils.counter.FirstEntryCounter;
import groundbreaking.gigachat.utils.counter.ICounter;
import groundbreaking.gigachat.utils.counter.OnlineTimeCounter;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Locale;

@Getter
public final class NewbieChatValues {

    @Getter(AccessLevel.NONE)
    private final GigaChat plugin;

    private boolean isEnabled;

    private String priority;

    private ICounter counter;

    private boolean isGiveBypassPermissionEnabled;

    private int requiredTime;
    private int requiredTimeToGetBypassPerm;

    private String denyMessage;

    private boolean isDenySoundEnabled;

    private Sound denySound;

    private float denySoundVolume;
    private float denySoundPitch;

    public NewbieChatValues(final GigaChat plugin) {
        this.plugin = plugin;
    }

    public void setValues() {
        final FileConfiguration config = new ConfigLoader(this.plugin).loadAndGet("newbie-chat", 1.0);
        final IColorizer colorizer = this.plugin.getColorizer(config, "settings.serializer");

        this.setupSettings(config, colorizer);
    }

    private void setupSettings(final FileConfiguration config, final IColorizer colorizer) {
        final ConfigurationSection settings = config.getConfigurationSection("settings");
        if (settings != null) {
            this.isEnabled = settings.getBoolean("enable");
            this.priority = settings.getString("listener-priority").toUpperCase(Locale.ENGLISH);
            this.counter = settings.getBoolean("count-time-from-first-join") ? new FirstEntryCounter() : new OnlineTimeCounter();
            this.requiredTime = settings.getInt("required-time");
            this.isGiveBypassPermissionEnabled = settings.getBoolean("if-reached.give-permission");
            this.requiredTimeToGetBypassPerm = settings.getInt("if-reached.required-time");
            this.denyMessage = colorizer.colorize(settings.getString("deny-message"));

            this.setupSound(settings);
        } else {
            this.plugin.getMyLogger().warning("Failed to load section \"settings\" from file \"newbie-chat.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupSound(final ConfigurationSection settings) {
        final String soundString = settings.getString("deny-sound");
        if (soundString == null) {
            this.plugin.getMyLogger().warning("Failed to load sound on path \"settings.deny-sound\" from file \"newbie-chat.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
            this.isDenySoundEnabled = false;
        } else if (soundString.equalsIgnoreCase("disabled")) {
            this.isDenySoundEnabled = false;
        } else {
            this.isDenySoundEnabled = true;
            final String[] params = soundString.split(";");
            this.denySound = params.length >= 1 ? Sound.valueOf(params[0].toUpperCase(Locale.ENGLISH)) : Sound.BLOCK_BREWING_STAND_BREW;
            this.denySoundVolume = params.length >= 2 ? Float.parseFloat(params[1]) : 1.0f;
            this.denySoundPitch = params.length >= 3 ? Float.parseFloat(params[2]) : 1.0f;
        }
    }
}
