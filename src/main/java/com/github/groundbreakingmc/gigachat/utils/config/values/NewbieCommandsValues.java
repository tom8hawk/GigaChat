package com.github.groundbreakingmc.gigachat.utils.config.values;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.listeners.NewbieCommandListener;
import com.github.groundbreakingmc.gigachat.utils.ListenerRegisterUtil;
import com.github.groundbreakingmc.gigachat.utils.colorizer.basic.Colorizer;
import com.github.groundbreakingmc.gigachat.utils.config.ConfigLoader;
import com.github.groundbreakingmc.gigachat.utils.counter.Counter;
import com.github.groundbreakingmc.gigachat.utils.counter.FirstEntryCounter;
import com.github.groundbreakingmc.gigachat.utils.counter.OnlineTimeCounter;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.EventExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Getter
public final class NewbieCommandsValues {

    @Getter(AccessLevel.NONE)
    private final GigaChat plugin;

    private Counter counter;

    private boolean isGiveBypassPermissionEnabled;

    private int requiredTime;
    private int requiredTimeToGetBypassPerm;

    private String denyMessage;

    private boolean isDenySoundEnabled;

    private Sound denySound;

    private float denySoundVolume;
    private float denySoundPitch;

    private final List<String> blockedCommands = new ArrayList<>();

    public NewbieCommandsValues(final GigaChat plugin) {
        this.plugin = plugin;
    }

    public void setValues() {
        final FileConfiguration config = new ConfigLoader(this.plugin).loadAndGet("newbie-commands", 1.0);
        final Colorizer colorizer = this.plugin.getColorizer(config, "settings.serializer");

        this.setupSettings(config, colorizer);
    }

    private void setupSettings(final FileConfiguration config, final Colorizer colorizer) {
        final ConfigurationSection settings = config.getConfigurationSection("settings");
        if (settings != null) {
            final NewbieCommandListener newbieCommandListener = this.plugin.getNewbieCommandListener();
            if (settings.getBoolean("enable") && !this.plugin.getServer().getPluginManager().isPluginEnabled("NewbieGuard")) {
                final String priority = settings.getString("listener-priority").toUpperCase(Locale.ENGLISH);
                final boolean ignoreCanceled = settings.getBoolean("ignore-canceled");

                final EventPriority eventPriority = this.plugin.getEventPriority(priority, "newbie-chat.yml");
                final EventExecutor eventExecutor = (listener, event) -> newbieCommandListener.onCommandUse((PlayerCommandPreprocessEvent) event);
                ListenerRegisterUtil.register(this.plugin, newbieCommandListener, PlayerCommandPreprocessEvent.class, eventPriority, ignoreCanceled, eventExecutor);
                this.counter = settings.getBoolean("count-time-from-first-join") ? new FirstEntryCounter() : new OnlineTimeCounter();
                this.requiredTime = settings.getInt("required-time");
                this.isGiveBypassPermissionEnabled = settings.getBoolean("if-reached.give-permission");
                this.requiredTimeToGetBypassPerm = settings.getInt("if-reached.required-time");
                this.denyMessage = colorizer.colorize(settings.getString("deny-message"));

                this.setupSound(settings);

                this.blockedCommands.clear();
                this.blockedCommands.addAll(settings.getStringList("blocked-commands"));
            } else {
                ListenerRegisterUtil.unregister(newbieCommandListener);
            }
        }
        else {
            this.plugin.getMyLogger().warning("Failed to load section \"settings\" from file \"newbie-commands.yml\". Please check your configuration file, or delete it and restart your server!");
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
