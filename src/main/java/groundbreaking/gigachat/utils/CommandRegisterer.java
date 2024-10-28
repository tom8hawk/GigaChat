package groundbreaking.gigachat.utils;

import groundbreaking.gigachat.GigaChat;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

public class CommandRegisterer {

    private final GigaChat plugin;

    private final SimpleCommandMap COMMAND_MAP = this.getCommandMap();

    public CommandRegisterer(final GigaChat plugin) {
        this.plugin = plugin;
    }

    public void register(final String command, final List<String> aliases, final CommandExecutor commandExecutor, final TabCompleter tabCompleter) {
        this.registerCommand(command, commandExecutor, tabCompleter);
        for (final String alias : aliases) {
            this.registerCommand(alias, commandExecutor, tabCompleter);
        }

        this.syncCommands();
    }

    private void registerCommand(final String command, final CommandExecutor commandExecutor, final TabCompleter tabCompleter) {
        final PluginCommand custom = this.getCustomCommand(command);
        custom.setExecutor(commandExecutor);
        custom.setTabCompleter(tabCompleter);

        COMMAND_MAP.register(plugin.getDescription().getName(), custom);
    }

    public PluginCommand getCustomCommand(final String name) {
        try {
            final Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            c.setAccessible(true);

            return c.newInstance(name, plugin);
        } catch (final SecurityException | IllegalArgumentException | IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void unregisterCustomCommand(final String command) {
        try {
            final PluginCommand pluginCommand = this.getCustomCommand(command);
            final Field field = SimpleCommandMap.class.getDeclaredField("knownCommands");
            field.setAccessible(true);
            final Object map = field.get(COMMAND_MAP);

            final HashMap<String, Command> knownCommands = (HashMap<String, Command>) map;
            knownCommands.remove(pluginCommand.getName());

            for (final String alias : pluginCommand.getAliases()) {
                if (knownCommands.containsKey(alias) && knownCommands.get(alias).toString().contains(plugin.getName())) {
                    knownCommands.remove(alias);
                }
            }
        } catch (final NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }

    private void syncCommands() {
        try {
            final Method method = Bukkit.getServer().getClass().getDeclaredMethod("syncCommands");
            method.setAccessible(true);
            method.invoke(Bukkit.getServer());
        } catch (final ReflectiveOperationException ex) {
            ex.printStackTrace();
        }
    }

    private SimpleCommandMap getCommandMap() {
        try {
            Field field = SimplePluginManager.class.getDeclaredField("commandMap");
            field.setAccessible(true);

            return (SimpleCommandMap) field.get(Bukkit.getPluginManager());
        } catch (final NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}