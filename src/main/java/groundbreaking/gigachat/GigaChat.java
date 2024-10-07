package groundbreaking.gigachat;

import groundbreaking.gigachat.automessages.AutoMessages;
import groundbreaking.gigachat.collections.Cooldowns;
import groundbreaking.gigachat.collections.DisabledPrivateMessages;
import groundbreaking.gigachat.commands.MainCommandHandler;
import groundbreaking.gigachat.commands.args.*;
import groundbreaking.gigachat.commands.other.BroadcastCommand;
import groundbreaking.gigachat.database.DatabaseHandler;
import groundbreaking.gigachat.database.DatabaseQueries;
import groundbreaking.gigachat.listeners.ChatListener;
import groundbreaking.gigachat.listeners.CommandListener;
import groundbreaking.gigachat.listeners.DisconnectListener;
import groundbreaking.gigachat.listeners.NewbieChatListener;
import groundbreaking.gigachat.utils.ServerInfo;
import groundbreaking.gigachat.utils.colorizer.IColorizer;
import groundbreaking.gigachat.utils.colorizer.LegacyColorizer;
import groundbreaking.gigachat.utils.colorizer.MiniMessagesColorizer;
import groundbreaking.gigachat.utils.colorizer.VanillaColorize;
import groundbreaking.gigachat.utils.config.values.*;
import groundbreaking.gigachat.utils.vanish.*;
import lombok.Getter;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Locale;

@Getter
public final class GigaChat extends JavaPlugin {

    private Chat chat;

    private Permission perms;

    private boolean is16OrAbove;

    private BukkitTask task;

    private AutoMessages autoMessages;

    private AutoMessagesValues autoMessagesValues;
    private BroadcastValues broadcastValues;
    private ChatValues chatValues;
    private Messages messages;
    private NewbieChatValues newbieChat;
    private NewbieCommandsValues newbieCommands;
    private PrivateMessagesValues pmValues;

    private Cooldowns cooldowns;

    private DisabledPrivateMessages disabled;

    private IVanishChecker vanishChecker;

    @Override
    public void onEnable() {
        final long startTime = System.currentTimeMillis();

        final ServerInfo serverInfo = new ServerInfo();
        is16OrAbove = serverInfo.is16OrAbove(this);
        if (!serverInfo.isPaperOrFork()) {
            logPaperWarning();
            Bukkit.getPluginManager().disablePlugin(this);
        }

        saveDefaultConfig();
        setVanishChecker();
        loadClasses(); //4
        setupAll(); // 5

        new DatabaseHandler(this).createConnection();
        DatabaseQueries.createTables();

        autoMessages = new AutoMessages(this);
        runAutoMessagesTask();

        final ServicesManager servicesManager = getServer().getServicesManager();
        setupChat(servicesManager);
        setupPerms(servicesManager);

        registerEvents(); // 8
        registerCommands();
        registerBroadcastCommand();

        final long endTime = System.currentTimeMillis();
        getLogger().info("Plugin successfully started in " + (endTime - startTime) + "ms.");
    }

    @Override
    public void onDisable() {
        task.cancel();
        new DatabaseHandler(this).closeConnection();
    }

    private void logPaperWarning() {
        getLogger().warning("\u001b[91m=============== \u001b[31mWARNING \u001b[91m===============\u001b[0m");
        getLogger().warning("\u001b[91mThe plugin dev is against using Bukkit, Spigot etc.!\u001b[0m");
        getLogger().warning("\u001b[91mSwitch to Paper or its fork. To download Paper visit:\u001b[0m");
        getLogger().warning("\u001b[91mhttps://papermc.io/downloads/all\u001b[0m");
        getLogger().warning("\u001b[91m=======================================\u001b[0m");
    }

    private void setupChat(final ServicesManager servicesManager) {
        final RegisteredServiceProvider<Chat> chatProvider = servicesManager.getRegistration(Chat.class);
        if (chatProvider != null) {
            chat = chatProvider.getProvider();
        }
    }

    private void setupPerms(final ServicesManager servicesManager) {
        final RegisteredServiceProvider<Permission> permissionProvider = servicesManager.getRegistration(Permission.class);
        if (permissionProvider != null) {
            perms = permissionProvider.getProvider();
        }
    }

    public IColorizer getColorizer(final FileConfiguration config, final String configPath) {
        return config.getBoolean(configPath)
                ? new MiniMessagesColorizer()
                : getColorizerByVersion();
    }

    public IColorizer getColorizerByVersion() {
        return is16OrAbove
                ? new LegacyColorizer()
                : new VanillaColorize();
    }

    private void registerBroadcastCommand() {
        final String command = getConfig().getString("broadcast.command");
        final List<String> aliases = getConfig().getStringList("broadcast.aliases");
        final BroadcastCommand broadcast = new BroadcastCommand(this);

        registerCommand(command, aliases, broadcast, broadcast);
    }

    public void registerCommand(final String command, final List<String> aliases, final CommandExecutor commandExecutor, final TabCompleter tabCompleter) {
        try {
            CommandMap commandMap = getServer().getCommandMap();
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
            PluginCommand pluginCommand = constructor.newInstance(command, this);
            pluginCommand.setAliases(aliases);
            pluginCommand.setExecutor(commandExecutor);
            pluginCommand.setTabCompleter(tabCompleter);
            commandMap.register(getDescription().getName(), pluginCommand);
        } catch (Exception ex) {
            getLogger().info("Unable to register" + command + " command! " + ex);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void loadClasses() {
        messages = new Messages(this);
        autoMessagesValues = new AutoMessagesValues(this);
        broadcastValues = new BroadcastValues(this);
        chatValues = new ChatValues(this);
        newbieChat = new NewbieChatValues(this);
        pmValues = new PrivateMessagesValues(this);
        newbieCommands = new NewbieCommandsValues(this);
        cooldowns = new Cooldowns(this);
        disabled = new DisabledPrivateMessages();
    }

    public void setupAll() {
        messages.setupMessages();
        autoMessagesValues.setValues();
        broadcastValues.setValues();
        chatValues.setValues();
        newbieChat.setValues();
        pmValues.setValues();
        newbieCommands.setValues();
        cooldowns.setCooldowns();
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new NewbieChatListener(this), this);
        getServer().getPluginManager().registerEvents(new CommandListener(this), this);
        getServer().getPluginManager().registerEvents(new DisconnectListener(this), this);
    }

    private void registerCommands() { // todo
        final MainCommandHandler mainCommandHandler = new MainCommandHandler(this);
        getCommand("gigachat").setExecutor(mainCommandHandler);

        final ClearChatArgument clearChat = new ClearChatArgument(this, "clearchat", "gigachat.command.clearchat");
        final DisableServerChatArgument disableServerChat = new DisableServerChatArgument(this, "disablechat", "gigachat.command.disablechat");
        final LocalSpyArgument localSpy = new LocalSpyArgument(this, "localspy", "gigachat.command.localspy");
        final ReloadArgument reload = new ReloadArgument(this, "reload", "gigachat.command.reload");
        final SetPmSoundArgument pmSoundSetter = new SetPmSoundArgument(this, "setpmsound", "gigachat.command.setpmsound");

        mainCommandHandler.registerArgument(clearChat);
        mainCommandHandler.registerArgument(disableServerChat);
        mainCommandHandler.registerArgument(localSpy);
        mainCommandHandler.registerArgument(reload);
        mainCommandHandler.registerArgument(pmSoundSetter);
    }

    public void runAutoMessagesTask() {
        autoMessages.run();
    }

    public void setVanishChecker() {
        final String checker = getConfig().getString("vanish-provider", "SUPER_VANISH").toUpperCase(Locale.ENGLISH);
        switch (checker) {
            case "SUPER_VANISH":
                if (getServer().getPluginManager().getPlugin("SuperVanish") != null) {
                    getLogger().warning("SuperVanish will be used as vanish provider.");
                    vanishChecker = new SuperVanishChecker();
                    break;
                }
            case "ESSENTIALS":
                final Plugin essentials = getServer().getPluginManager().getPlugin("Essentials");
                if (essentials != null) {
                    getLogger().warning("Essentials will be used as vanish provider.");
                    vanishChecker = new EssentialsChecker(essentials);
                    break;
                }
            case "CMI":
                if (getServer().getPluginManager().getPlugin("CMI") != null) {
                    getLogger().warning("CMI will be used as vanish provider.");
                    vanishChecker = new CMIChecker();
                    break;
                }
            default:
                getLogger().warning("No vanish provider were found! Plugin will not check if the player is vanished.");
                getLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
                vanishChecker = new NoChecker();
        }
    }
}
