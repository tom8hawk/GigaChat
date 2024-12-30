package com.github.groundbreakingmc.gigachat;

import com.github.groundbreakingmc.gigachat.automessages.AutoMessages;
import com.github.groundbreakingmc.gigachat.collections.CooldownCollections;
import com.github.groundbreakingmc.gigachat.collections.DisabledPrivateMessagesCollection;
import com.github.groundbreakingmc.gigachat.collections.PmSoundsCollection;
import com.github.groundbreakingmc.gigachat.commands.MainCommandHandler;
import com.github.groundbreakingmc.gigachat.commands.args.*;
import com.github.groundbreakingmc.gigachat.commands.other.DisableAutoMessagesCommand;
import com.github.groundbreakingmc.gigachat.commands.other.DisableOwnChatExecutor;
import com.github.groundbreakingmc.gigachat.database.DatabaseHandler;
import com.github.groundbreakingmc.gigachat.database.DatabaseQueries;
import com.github.groundbreakingmc.gigachat.exceptions.UnsupportedPrioritySpecified;
import com.github.groundbreakingmc.gigachat.listeners.ChatListener;
import com.github.groundbreakingmc.gigachat.listeners.DisconnectListener;
import com.github.groundbreakingmc.gigachat.listeners.NewbieChatListener;
import com.github.groundbreakingmc.gigachat.listeners.NewbieCommandListener;
import com.github.groundbreakingmc.gigachat.utils.CommandRegisterer;
import com.github.groundbreakingmc.gigachat.utils.ServerInfo;
import com.github.groundbreakingmc.gigachat.utils.colorizer.basic.*;
import com.github.groundbreakingmc.gigachat.utils.config.values.*;
import com.github.groundbreakingmc.gigachat.utils.logging.BukkitLogger;
import com.github.groundbreakingmc.gigachat.utils.logging.Logger;
import com.github.groundbreakingmc.gigachat.utils.logging.PaperLogger;
import com.github.groundbreakingmc.gigachat.utils.updateschecker.UpdatesChecker;
import com.github.groundbreakingmc.gigachat.utils.vanish.VanishChecker;
import lombok.Getter;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

@Getter
public final class GigaChat extends JavaPlugin {

    private Chat chat;

    private Permission perms;

    private AutoMessages autoMessages;

    private AutoMessagesValues autoMessagesValues;
    private BroadcastValues broadcastValues;
    private ChatValues chatValues;
    private Messages messages;
    private NewbieChatValues newbieChatValues;
    private NewbieCommandsValues newbieCommandsValues;
    private PrivateMessagesValues pmValues;

    private CooldownCollections cooldownCollections;
    private PmSoundsCollection pmSoundsCollection;

    private DisabledPrivateMessagesCollection disabled;

    private final VanishChecker vanishChecker = new VanishChecker();

    private Logger myLogger;

    private ChatListener chatListener;
    private NewbieCommandListener newbieCommandListener;
    private NewbieChatListener newbieChatListener;

    private final CommandRegisterer commandRegisterer = new CommandRegisterer(this);

    @Override
    public void onEnable() {
        final long startTime = System.currentTimeMillis();

        final ServerInfo serverInfo = new ServerInfo();
        if (!serverInfo.isPaperOrFork()) {
            this.logPaperWarning();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        new Metrics(this, 23871);

        final int subVersion = serverInfo.getSubVersion(this);
        this.setupLogger(subVersion);
        this.logLoggerType();

        super.saveDefaultConfig();
        this.loadClasses();
        this.setupConfigValues();

        DatabaseHandler.createConnection(this);
        DatabaseQueries.createDatabaseTables();

        this.autoMessages.run();

        this.setupProviders();

        this.registerEvents();
        this.registerMainPluginCommand();
        this.registerPluginCommands();

        super.getServer().getScheduler().runTaskLaterAsynchronously(this, () -> new UpdatesChecker(this).check(), 300L);

        final long endTime = System.currentTimeMillis();
        this.myLogger.info("Plugin successfully started in " + (endTime - startTime) + "ms.");
    }

    @Override
    public void onDisable() {
        DatabaseHandler.closeConnection();
        super.getServer().getScheduler().cancelTasks(this);
    }

    private void logPaperWarning() {
        final java.util.logging.Logger logger = super.getLogger();
        logger.warning("\u001b[91m=============== \u001b[31mWARNING \u001b[91m===============\u001b[0m");
        logger.warning("\u001b[91mThe plugin dev is against using Bukkit, Spigot etc.!\u001b[0m");
        logger.warning("\u001b[91mSwitch to Paper or its fork. To download Paper visit:\u001b[0m");
        logger.warning("\u001b[91mhttps://papermc.io/downloads/all\u001b[0m");
        logger.warning("\u001b[91m=======================================\u001b[0m");
    }

    private void logLoggerType() {
        if (this.myLogger instanceof PaperLogger) {
            this.myLogger.info("Plugin will use new ComponentLogger for logging.");
        } else if (this.myLogger instanceof BukkitLogger) {
            this.myLogger.info("Plugin will use default old BukkitLogger for logging. Because your server version is under 19!");
        }
    }

    private void setupLogger(final int subVersion) {
        this.myLogger = subVersion >= 19
                ? new PaperLogger(this)
                : new BukkitLogger(this);
    }

    private void setupProviders() {
        final ServicesManager servicesManager = super.getServer().getServicesManager();
        this.chat = this.getProvider(servicesManager, Chat.class);
        this.perms = this.getProvider(servicesManager, Permission.class);
    }

    private <T> T getProvider(final ServicesManager servicesManager, final Class<T> clazz) {
        final RegisteredServiceProvider<T> provider = servicesManager.getRegistration(clazz);
        return provider != null ? provider.getProvider() : null;
    }

    private void loadClasses() {
        this.messages = new Messages(this);
        this.autoMessagesValues = new AutoMessagesValues(this);
        this.broadcastValues = new BroadcastValues(this);
        this.chatValues = new ChatValues(this);
        this.newbieChatValues = new NewbieChatValues(this);
        this.pmValues = new PrivateMessagesValues(this);
        this.newbieCommandsValues = new NewbieCommandsValues(this);
        this.cooldownCollections = new CooldownCollections(this);
        this.disabled = new DisabledPrivateMessagesCollection();
        this.chatListener = new ChatListener(this);
        this.newbieCommandListener = new NewbieCommandListener(this);
        this.newbieChatListener = new NewbieChatListener(this);
        this.autoMessages = new AutoMessages(this);
        this.pmSoundsCollection = new PmSoundsCollection();
    }

    public void setupConfigValues() {
        this.messages.setupMessages();
        this.autoMessagesValues.setValues();
        this.broadcastValues.setValues();
        this.chatValues.setValues();
        final PluginManager pluginManager = super.getServer().getPluginManager();
        if (!pluginManager.isPluginEnabled("NewbieGuard")) {
            this.newbieChatValues.setValues();
            this.newbieCommandsValues.setValues();
        } else {
            this.myLogger.info("Newbie protections will be disabled because NewbieGuard is detected.");
        }
        this.pmValues.setValues();
        this.cooldownCollections.setCooldowns();
    }

    private void registerEvents() {
        final PluginManager pluginManager = super.getServer().getPluginManager();
        pluginManager.registerEvents(new DisconnectListener(this), this);
    }

    private void registerMainPluginCommand() {
        final MainCommandHandler mainCommandHandler = new MainCommandHandler(this);
        super.getCommand("gigachat").setExecutor(mainCommandHandler);

        final ClearChatArgument clearChat = new ClearChatArgument(this, "clearchat", "gigachat.command.clearchat");
        final DisableAutoMessagesArgument disableAutoMessagesArgument = new DisableAutoMessagesArgument(this, "disableam", "gigachat.command.disableam.other");
        final DisableServerChatArgument disableServerChat = new DisableServerChatArgument(this, "disablechat", "gigachat.command.disablechat");
        final ReloadArgument reload = new ReloadArgument(this, "reload", "gigachat.command.reload");
        final SetPmSoundArgument pmSoundSetter = new SetPmSoundArgument(this, "setpmsound", "gigachat.command.setpmsound");
        final SpyArgument spyArgument = new SpyArgument(this, "spy", "gigachat.command.spy.other");
        final UpdateArgument updateArgument = new UpdateArgument(this, "update", "gigachat.command.spy.other");

        mainCommandHandler.registerArgument(clearChat);
        mainCommandHandler.registerArgument(disableAutoMessagesArgument);
        mainCommandHandler.registerArgument(disableServerChat);
        mainCommandHandler.registerArgument(reload);
        mainCommandHandler.registerArgument(pmSoundSetter);
        mainCommandHandler.registerArgument(spyArgument);
        mainCommandHandler.registerArgument(updateArgument);
    }

    public void registerPluginCommands() {
        this.registerCommand("disable-own-chat", DisableOwnChatExecutor.class, DisableOwnChatExecutor.class);
        this.registerCommand("disable-auto-messages", DisableAutoMessagesCommand.class, DisableAutoMessagesCommand.class);
    }

    private void registerCommand(final String configSectionName, final Class<? extends CommandExecutor> commandClass, final Class<? extends TabCompleter> commandTabClass) {
        final ConfigurationSection configSection = super.getConfig().getConfigurationSection(configSectionName);
        this.registerCommand(configSection, commandClass, commandTabClass);
    }

    public boolean registerCommand(final ConfigurationSection configSection, final Class<? extends CommandExecutor> commandClass, final Class<? extends TabCompleter> commandTabClass) {
        if (configSection != null) {
            final String command = configSection.getString("command");
            if (command != null && !command.isEmpty()) {
                final List<String> aliases = configSection.getStringList("aliases");
                try {
                    final CommandExecutor commandInstance = commandClass.getConstructor(GigaChat.class).newInstance(this);
                    final TabCompleter tabInstance = commandTabClass.getConstructor(GigaChat.class).newInstance(this);
                    this.commandRegisterer.register(command, aliases, commandInstance, tabInstance);
                    return true;
                } catch (final Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        return false;
    }

    public Colorizer getColorizer(final FileConfiguration config, final String configPath) {
        final String colorizerMode = config.getString(configPath).toUpperCase();

        return switch (colorizerMode) {
            case "MINIMESSAGE" -> new MiniMessageColorizer();
            case "LEGACY" -> new LegacyColorizer();
            case "LEGACY_ADVANCED" -> new LegacyAdvancedColorizer();
            default -> new VanillaColorizer();
        };
    }

    public Colorizer getColorizerByVersion() {
        final ServerInfo serverInfo = new ServerInfo();
        final boolean is16OrAbove = serverInfo.getSubVersion(this) >= 16;
        return is16OrAbove
                ? new LegacyColorizer()
                : new VanillaColorizer();
    }

    public EventPriority getEventPriority(final String priority, final String fileName) {
        return switch (priority) {
            case "LOWEST" -> EventPriority.LOWEST;
            case "LOW" -> EventPriority.LOW;
            case "NORMAL" -> EventPriority.NORMAL;
            case "HIGH" -> EventPriority.HIGH;
            case "HIGHEST" -> EventPriority.HIGHEST;
            default -> {
                this.myLogger.warning("Failed to parse value from \"settings.listener-priority\" from file \"" + fileName + "\". Please check your configuration file, or delete it and restart your server!");
                this.myLogger.warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
                throw new UnsupportedPrioritySpecified("Failed to get event priority, please check your configuration files!");
            }
        };
    }
}
