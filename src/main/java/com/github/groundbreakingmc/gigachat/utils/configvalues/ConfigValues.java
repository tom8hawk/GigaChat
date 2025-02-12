package com.github.groundbreakingmc.gigachat.utils.configvalues;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.commands.other.DisableAutoMessagesExecutor;
import com.github.groundbreakingmc.gigachat.commands.other.DisableOwnChatExecutor;
import com.github.groundbreakingmc.gigachat.constructors.CommandParams;
import com.github.groundbreakingmc.mylib.config.ConfigLoader;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public final class ConfigValues {

    @Getter(AccessLevel.NONE)
    private final GigaChat plugin;

    private final CommandParams disableOwnChatCommand;
    private final CommandParams disableAutoMessagesCommand;

    public ConfigValues(final GigaChat plugin) {
        this.plugin = plugin;
        this.disableOwnChatCommand = new CommandParams(plugin, new DisableOwnChatExecutor(plugin));
        this.disableAutoMessagesCommand = new CommandParams(plugin, new DisableAutoMessagesExecutor(plugin));
    }

    public void setupValues() {
        final FileConfiguration config = ConfigLoader.builder(this.plugin, this.plugin.getCustomLogger())
                .fileName("config.yml")
                .fileVersion(1.0)
                .fileVersionPath("settings.config-version")
                .build();

        this.checkForUpdates(config);
        this.processCommands(config);
    }

    private void checkForUpdates(final FileConfiguration config) {
        final ConfigurationSection updates = config.getConfigurationSection("updates");
        final boolean check = updates.getBoolean("check");
        final boolean download = updates.getBoolean("auto-download");
        this.plugin.getServer().getScheduler().runTaskLaterAsynchronously(this.plugin, () ->
                this.plugin.getUpdatesChecker().check(check, download), 300L
        );
    }

    private void processCommands(final FileConfiguration config) {
        final ConfigurationSection disableOwnChat = config.getConfigurationSection("disable-own-chat");
        this.disableOwnChatCommand.process(disableOwnChat);

        final ConfigurationSection disableAutoMessages = config.getConfigurationSection("disable-auto-messages");
        this.disableAutoMessagesCommand.process(disableAutoMessages);
    }
}
