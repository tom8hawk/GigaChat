package com.github.groundbreakingmc.gigachat.utils.configvalues;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.commands.other.BroadcastExecutor;
import com.github.groundbreakingmc.gigachat.constructors.CommandParams;
import com.github.groundbreakingmc.gigachat.constructors.Hover;
import com.github.groundbreakingmc.gigachat.utils.colorizer.messages.BroadcastColorizer;
import com.github.groundbreakingmc.gigachat.utils.colorizer.messages.PermissionColorizer;
import com.github.groundbreakingmc.mylib.colorizer.Colorizer;
import com.github.groundbreakingmc.mylib.colorizer.ColorizerFactory;
import com.github.groundbreakingmc.mylib.config.ConfigLoader;
import com.github.groundbreakingmc.mylib.utils.player.settings.SoundSettings;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

@Getter
public final class BroadcastValues {

    @Getter(AccessLevel.NONE)
    private final GigaChat plugin;

    private String format;

    private int cooldown;

    private SoundSettings soundSettings;

    private Hover hover;

    private Colorizer formatColorizer;

    private final PermissionColorizer messageColorizer;

    @Getter(AccessLevel.NONE)
    private CommandParams broadcastCommand;

    public BroadcastValues(final GigaChat plugin) {
        this.plugin = plugin;
        this.messageColorizer = new BroadcastColorizer(plugin);
    }

    public void setupValues() {
        final FileConfiguration config = ConfigLoader.builder(this.plugin, this.plugin.getCustomLogger())
                .fileName("broadcast.yml")
                .fileVersion(1.0)
                .fileVersionPath("settings.config-version")
                .build();

        this.setupSettings(config);
    }

    private void setupSettings(final FileConfiguration config) {
        final ConfigurationSection broadcast = config.getConfigurationSection("settings");

        this.formatColorizer = ColorizerFactory.createColorizer(config.getString("settings.colorizer-mode"));
        this.format = this.formatColorizer.colorize(broadcast.getString("format"));
        this.cooldown = broadcast.getInt("cooldown");

        this.soundSettings = SoundSettings.get(broadcast.getString("sound"));

        if (this.broadcastCommand == null) {
            this.broadcastCommand = new CommandParams(plugin, new BroadcastExecutor(plugin));
        }

        this.broadcastCommand.process(broadcast);

        if (this.broadcastCommand.getCommand() != null) {
            this.setupHover(broadcast);
        }
    }

    private void setupHover(final ConfigurationSection broadcast) {
        final ConfigurationSection hover = broadcast.getConfigurationSection("hover");
        if (hover != null) {
            this.hover = Hover.get(hover, null);
        } else {
            this.plugin.getCustomLogger().warn("Failed to load section \"settings.hover\" from file \"broadcast.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getCustomLogger().warn("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }
}
