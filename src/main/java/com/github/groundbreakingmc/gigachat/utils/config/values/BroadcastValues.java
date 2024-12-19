package com.github.groundbreakingmc.gigachat.utils.config.values;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.commands.other.BroadcastCommand;
import com.github.groundbreakingmc.gigachat.constructors.Hover;
import com.github.groundbreakingmc.gigachat.utils.colorizer.basic.Colorizer;
import com.github.groundbreakingmc.gigachat.utils.colorizer.messages.BroadcastColorizer;
import com.github.groundbreakingmc.gigachat.utils.colorizer.messages.PermissionsColorizer;
import com.github.groundbreakingmc.gigachat.utils.config.ConfigLoader;
import lombok.AccessLevel;
import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Locale;

@Getter
public final class BroadcastValues {

    @Getter(AccessLevel.NONE)
    private final GigaChat plugin;

    private String format;

    private int cooldown;

    private Hover hover;

    private boolean isSoundEnabled;

    private Sound sound;

    private float soundVolume;
    private float soundPitch;

    private Colorizer colorizer;

    private final PermissionsColorizer messageColorizer;

    public BroadcastValues(final GigaChat plugin) {
        this.plugin = plugin;
        this.messageColorizer = new BroadcastColorizer(plugin);
    }

    public void setValues() {
        final FileConfiguration config = new ConfigLoader(this.plugin).loadAndGet("broadcast.yml", 1.0);

        final ConfigurationSection broadcast = config.getConfigurationSection("settings");
        if (broadcast != null) {
            this.colorizer = this.plugin.getColorizer(config, "settings.serializer");
            this.format = this.colorizer.colorize(broadcast.getString("format"));
            this.cooldown = broadcast.getInt("cooldown");

            final boolean registered = this.plugin.registerCommand(broadcast, BroadcastCommand.class, BroadcastCommand.class);
            if (!registered) {
                return;
            }

            this.setupHover(broadcast);
            this.setupSound(broadcast);
        } else {
            this.plugin.getMyLogger().warning("Failed to load section \"settings\" from file \"broadcast.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupHover(final ConfigurationSection broadcast) {
        final ConfigurationSection hover = broadcast.getConfigurationSection("hover");
        if (hover != null) {
            final boolean isHoverEnabled = hover.getBoolean("enable");
            final String hoverAction = hover.getString("click-action");
            final String hoverValue = hover.getString("click-value");
            final String hoverText = hover.getString("text");

            this.hover = new Hover(isHoverEnabled,
                    ClickEvent.Action.valueOf(hoverAction),
                    hoverValue,
                    hoverText
            );
        } else {
            this.plugin.getMyLogger().warning("Failed to load section \"settings.hover\" from file \"broadcast.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupSound(final ConfigurationSection settings) {
        final String soundString = settings.getString("sound");
        if (soundString == null) {
            this.plugin.getMyLogger().warning("Failed to load sound on path \"settings.sound\" from file \"broadcast.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
            this.isSoundEnabled = false;
        } else if (soundString.equalsIgnoreCase("disabled")) {
            this.isSoundEnabled = false;
        } else {
            this.isSoundEnabled = true;
            final String[] params = soundString.split(";");
            this.sound = params.length >= 1 ? Sound.valueOf(params[0].toUpperCase(Locale.ENGLISH)) : Sound.BLOCK_BREWING_STAND_BREW;
            this.soundVolume = params.length >= 2 ? Float.parseFloat(params[1]) : 1.0f;
            this.soundPitch = params.length >= 3 ? Float.parseFloat(params[2]) : 1.0f;
        }
    }
}
