package groundbreaking.mychat.commands;

import groundbreaking.mychat.MyChat;
import org.bukkit.Server;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Constructor;
import java.util.List;

public class RegisterCommands {

    private final MyChat plugin;
    private final Server server;
    private final PluginManager pluginManager;

    public RegisterCommands(MyChat plugin) {
        this.plugin = plugin;
        this.server = plugin.getServer();
        this.pluginManager = server.getPluginManager();
    }

    public void registerCommand(String command, List<String> aliases, CommandExecutor commandExecutor, TabCompleter tabCompleter) {
        try {
            CommandMap commandMap = server.getCommandMap();
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
            PluginCommand pluginCommand = constructor.newInstance(command, plugin);
            pluginCommand.setAliases(aliases);
            pluginCommand.setExecutor(commandExecutor);
            pluginCommand.setTabCompleter(tabCompleter);
            commandMap.register(plugin.getDescription().getName(), pluginCommand);
        } catch (Exception ex) {
            plugin.getLogger().info("Unable to register" + command + " command!" + ex);
            pluginManager.disablePlugin(plugin);
        }
    }
}
