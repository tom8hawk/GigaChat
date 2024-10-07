package groundbreaking.gigachat.commands.args;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.commands.ArgsConstructor;
import groundbreaking.gigachat.utils.config.values.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class ReloadArgument extends ArgsConstructor {

    private final GigaChat plugin;
    private final Messages messages;

    public ReloadArgument(final GigaChat plugin, String name, String permission) {
        super(name, permission);
        this.plugin = plugin;
        this.messages = plugin.getMessages();
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {

        final long startTime = System.currentTimeMillis();

        plugin.reloadConfig();
        plugin.setVanishChecker();
        plugin.setupAll();
        plugin.getCooldowns().setCooldowns();
        plugin.runAutoMessagesTask();

        long endTime = System.currentTimeMillis();
        if (sender instanceof Player) {
            plugin.getServer().getConsoleSender().sendMessage(messages.getReloadMessage().replace("{time}", String.valueOf(endTime - startTime)));
        }
        sender.sendMessage(messages.getReloadMessage().replace("{time}", String.valueOf(endTime - startTime)));

        return true;
    }
}
