package groundbreaking.mychat;

import groundbreaking.mychat.automessages.AutoMessages;
import groundbreaking.mychat.commands.*;
import groundbreaking.mychat.listeners.ChatListener;
import groundbreaking.mychat.listeners.CommandListener;
import groundbreaking.mychat.listeners.DisconnectListener;
import groundbreaking.mychat.utils.ConfigValues;
import groundbreaking.mychat.utils.ServerInfos;
import groundbreaking.mychat.utils.chatsColorizer.AbstractColorizer;
import groundbreaking.mychat.utils.chatsColorizer.ChatColorizer;
import groundbreaking.mychat.utils.chatsColorizer.PrivateMessagesColorizer;
import groundbreaking.mychat.utils.colorizer.IColorizer;
import groundbreaking.mychat.utils.colorizer.LegacyColorizer;
import groundbreaking.mychat.utils.colorizer.MiniMessagesColorizer;
import groundbreaking.mychat.utils.colorizer.VanilaColorizer;
import lombok.Getter;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.util.List;

public final class MyChat extends JavaPlugin {

    @Getter
    private Chat chat;

    @Getter
    private Permission perms;

    @Getter
    private final ConfigValues configValues = new ConfigValues();

    @Getter
    private IColorizer colorizer;

    @Getter
    private ServerInfos infos;

    @Override
    public void onEnable() {
        final long startTime = System.currentTimeMillis();

        infos = new ServerInfos(this);
        if (!infos.isPaperOrFork()) {
            logPaperWarning();
            Bukkit.getPluginManager().disablePlugin(this);
        }

        saveDefaultConfig();

        setColorizers();

        configValues.setupValues(this);

        final ServicesManager servicesManager = getServer().getServicesManager();
        setupChat(servicesManager);
        setupPerms(servicesManager);

        this.registerEvents();

        new AutoMessages(this).startMSG(getConfig());

        getCommand("mychat").setExecutor(new MainCommandExecutor(this));
        registerCommands();

        final long endTime = System.currentTimeMillis();
        getLogger().info("Plugin started in " + (endTime - startTime) + " ms");
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
    }

    private void logPaperWarning() {
        getLogger().warning("\u001b[91m=============== \u001b[31mWARNING \u001b[91m===============\u001b[0m");
        getLogger().warning("\u001b[91mThe plugin author against using Bukkit, Spigot etc.!\u001b[0m");
        getLogger().warning("\u001b[91mMove to Paper or his forks. To download Paper visit:\u001b[0m");
        getLogger().warning("\u001b[91mhttps://papermc.io/downloads/all\u001b[0m");
        getLogger().warning("\u001b[91m=======================================\u001b[0m");
    }

    private void registerEvents() {
        PluginManager pm = getServer().getPluginManager();
        ChatListener chatListener = new ChatListener(this);
        pm.registerEvents(chatListener, this);
        pm.registerEvents(new CommandListener(this), this);
        pm.registerEvents(new DisconnectListener(chatListener), this);
    }

    private void setupChat(ServicesManager servicesManager) {
        RegisteredServiceProvider<Chat> chatProvider = servicesManager.getRegistration(Chat.class);
        if (chatProvider != null) {
            chat = chatProvider.getProvider();
        }
    }

    private void setupPerms(ServicesManager servicesManager) {
        RegisteredServiceProvider<Permission> permissionProvider = servicesManager.getRegistration(Permission.class);
        if (permissionProvider != null) {
            perms = permissionProvider.getProvider();
        }
    }

    public IColorizer getColorizer(String configPath) {
        return getConfig().getBoolean(configPath)
                ? new MiniMessagesColorizer()
                : getColorizerByVersion();
    }

    public IColorizer getColorizerByVersion() {
        return infos.is16OrAbove()
                ? new LegacyColorizer()
                : new VanilaColorizer();
    }

    private void registerCommands() {
        ConfigurationSection pmMessages = getConfig().getConfigurationSection("privateMessages");

        PrivateMessageCommandExecutor pmExecutor = new PrivateMessageCommandExecutor(this);
        registerCommand(pmMessages.getString("pm-command"), pmMessages.getStringList("pm-aliases"), pmExecutor, pmExecutor);

        IgnoreCommandExecutor ignoreExecutor = new IgnoreCommandExecutor(this);
        registerCommand(pmMessages.getString("ignore-command"), pmMessages.getStringList("ignore-aliases"), ignoreExecutor, ignoreExecutor);

        ReplyCommandExecutor replyExecutor = new ReplyCommandExecutor(this);
        registerCommand(pmMessages.getString("reply-command"), pmMessages.getStringList("reply-aliases"), replyExecutor, replyExecutor);

        SocialSpyCommandExecutor socialspyExecutor = new SocialSpyCommandExecutor(this);
        registerCommand(pmMessages.getString("socialspy-command"), pmMessages.getStringList("socialspy-aliases"), socialspyExecutor, socialspyExecutor);
    }

    public void setColorizers() {
        colorizer = getColorizer("messages.use-minimessage");

        final AbstractColorizer pmColorizer = new PrivateMessagesColorizer(this);
        ReplyCommandExecutor.setMessagesColorizer(pmColorizer);
        PrivateMessageCommandExecutor.setMessagesColorizer(pmColorizer);

        final AbstractColorizer chatColorizer = new ChatColorizer(this);
        ChatListener.setMessagesColorizer(chatColorizer);
    }

    public void registerCommand(String command, List<String> aliases, CommandExecutor commandExecutor, TabCompleter tabCompleter) {
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
            getLogger().info("Unable to register" + command + " command!" + ex);
            getServer().getPluginManager().disablePlugin(this);
        }
    }
}
