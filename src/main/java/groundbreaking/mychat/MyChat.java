package groundbreaking.mychat;

import groundbreaking.mychat.automessages.AutoMessages;
import groundbreaking.mychat.commands.*;
import groundbreaking.mychat.listeners.ChatListener;
import groundbreaking.mychat.listeners.CommandListener;
import groundbreaking.mychat.listeners.DisconnectListener;
import groundbreaking.mychat.utils.Config;
import groundbreaking.mychat.utils.ServerInfos;
import groundbreaking.mychat.utils.colorizer.IColorizer;
import groundbreaking.mychat.utils.colorizer.LegacyColorizer;
import groundbreaking.mychat.utils.colorizer.MiniMessagesColorizer;
import groundbreaking.mychat.utils.colorizer.VanilaColorizer;
import lombok.Getter;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class MyChat extends JavaPlugin {

    @Getter
    private Chat chat;

    @Getter
    private Permission perms;

    @Getter
    private final Config pluginConfig = new Config(this);

    @Getter
    private final Logger logger = getLogger();

    @Getter
    private IColorizer colorizer;

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();

        final ServerInfos infos = new ServerInfos(getServer(), logger);
        if (!infos.isPaperOrFork()) {
            logger.warning("\u001b[91m=============== \u001b[31mWARNING \u001b[91m===============\u001b[0m");
            logger.warning("\u001b[91mThe plugin author against using Bukkit, Spigot etc.!\u001b[0m");
            logger.warning("\u001b[91mMove to Paper or his forks. To download Paper visit:\u001b[0m");
            logger.warning("\u001b[91mhttps://papermc.io/downloads/all\u001b[0m");
            logger.warning("\u001b[91m=======================================\u001b[0m");
            Bukkit.getPluginManager().disablePlugin(this);

            return;
        }

        saveDefaultConfig();
        this.setColorizer(getConfig(), infos);

        setupConfig();

        ServicesManager servicesManager = getServer().getServicesManager();
        setupChat(servicesManager);
        setupPerms(servicesManager);

        registerEvents();

        new AutoMessages(this).startMSG(getConfig());

        getCommand("promisedchat").setExecutor(new MainCommandExecutor(this));
        registerCommands();

        long endTime = System.currentTimeMillis();
        getLogger().info("Plugin started in " + (endTime - startTime) + " ms");
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
    }

    private void registerEvents() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new ChatListener(this), this);
        pm.registerEvents(new CommandListener(this), this);
        pm.registerEvents(new DisconnectListener(this), this);
    }

    private void registerCommands() {
        ConfigurationSection pmMessages = getConfig().getConfigurationSection("privateMessages");
        RegisterCommands registerCommands = new RegisterCommands(this);
        PrivateMessageCommandExecutor pmExecutor = new PrivateMessageCommandExecutor(this);
        registerCommands.registerCommand(pmMessages.getString("pm-command"), pmMessages.getStringList("pm-aliases"), pmExecutor, pmExecutor);
        IgnoreCommandExecutor ignoreExecutor = new IgnoreCommandExecutor(this);
        registerCommands.registerCommand(pmMessages.getString("ignore-command"), pmMessages.getStringList("ignore-aliases"), ignoreExecutor, ignoreExecutor);
        SocialSpyCommandExecutor socialspyExecutor = new SocialSpyCommandExecutor(this);
        registerCommands.registerCommand(pmMessages.getString("socialspy-command"), pmMessages.getStringList("socialspy-aliases"), socialspyExecutor, socialspyExecutor);
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

    public void setupConfig() {
        FileConfiguration config = getConfig();
        pluginConfig.setupMessages(config);
        pluginConfig.setupFormats(config);
        pluginConfig.setupHover(config);
        pluginConfig.setupNewbie(config);
        pluginConfig.setupAutoMessage(config);
    }

    public void setColorizer(FileConfiguration config, ServerInfos infos) {
        colorizer = config.getBoolean("use-minimessage") ? new MiniMessagesColorizer() : infos.isAbove16() ? new LegacyColorizer() : new VanilaColorizer();
    }
}
