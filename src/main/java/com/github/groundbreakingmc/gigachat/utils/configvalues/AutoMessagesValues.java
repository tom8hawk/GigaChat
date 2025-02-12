package com.github.groundbreakingmc.gigachat.utils.configvalues;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.mylib.collections.cases.Pair;
import com.github.groundbreakingmc.mylib.colorizer.Colorizer;
import com.github.groundbreakingmc.mylib.colorizer.ColorizerFactory;
import com.github.groundbreakingmc.mylib.config.ConfigLoader;
import com.github.groundbreakingmc.mylib.utils.player.settings.SoundSettings;
import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

@Getter
public final class AutoMessagesValues {

    @Getter(AccessLevel.NONE)
    private final GigaChat plugin;

    private boolean isEnabled;
    private boolean isRandom;

    private int sendInterval;

    private SoundSettings defaultSoundSettings;

    private List<Pair<String, SoundSettings>> autoMessages;

    public AutoMessagesValues(final GigaChat plugin) {
        this.plugin = plugin;
    }

    public void setValues() {
        final FileConfiguration config = ConfigLoader.builder(this.plugin, this.plugin.getCustomLogger())
                .fileName("auto-messages.yml")
                .fileVersion(1.0)
                .fileVersionPath("settings.config-version")
                .build();

        final Colorizer colorizer = ColorizerFactory.createColorizer(config.getString("settings.colorizer-mode"));

        this.setupSettings(config);
        this.setupAutoMessages(config, colorizer);
    }

    private void setupSettings(final FileConfiguration config) {
        final ConfigurationSection settings = config.getConfigurationSection("settings");
        if (settings != null) {
            this.isEnabled = settings.getBoolean("enable");
            this.isRandom = settings.getBoolean("random");
            this.sendInterval = settings.getInt("send-interval");
            this.defaultSoundSettings = SoundSettings.get(settings.getString("default-sound"));
        } else {
            this.plugin.getCustomLogger().warn("Failed to load section \"settings\" from file \"auto-messages.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getCustomLogger().warn("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupAutoMessages(final FileConfiguration config, final Colorizer colorizer) {
        final ConfigurationSection autoMessagesSection = config.getConfigurationSection("auto-messages");
        if (autoMessagesSection != null) {
            // I know stream is shit, but...
            this.autoMessages = autoMessagesSection.getKeys(false).stream()
                    .map(autoMessagesSection::getConfigurationSection)
                    .map(section -> {
                        final String message = colorizer.colorize(section.getString("message"));
                        final String soundString = section.getString("sound");
                        final SoundSettings sound = soundString == null ? this.defaultSoundSettings : SoundSettings.get(soundString.toUpperCase());

                        return new Pair<>(message, sound);
                    })
                    .collect(ImmutableList.toImmutableList());
        } else {
            this.plugin.getCustomLogger().warn("Failed to load section \"auto-messages\" from file \"auto-messages.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getCustomLogger().warn("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }
}
