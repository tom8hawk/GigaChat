package groundbreaking.gigachat.commands.args;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.constructors.ArgsConstructor;
import groundbreaking.gigachat.utils.config.values.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class ReloadArgument extends ArgsConstructor {

    private final GigaChat plugin;
    private final Messages messages;

    public ReloadArgument(final GigaChat plugin, final String name, final String permission) {
        super(name, permission);
        this.plugin = plugin;
        this.messages = plugin.getMessages();
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {

        final long startTime = System.currentTimeMillis();

        this.plugin.reloadConfig();
        this.plugin.setupVanishChecker();
        this.plugin.setupAll();
        this.plugin.getCooldownsMap().setCooldowns();
        this.plugin.getAutoMessages().run();
        this.plugin.registerReloadableEvents();
        this.plugin.registerPluginCommands();

        final long endTime = System.currentTimeMillis();
        final String result = String.valueOf(endTime - startTime);
        final String message = this.messages.getReloadMessage().replace("{time}", result);
        if (sender instanceof Player) {
            this.plugin.getServer().getConsoleSender().sendMessage(message);
        }
        sender.sendMessage(message);

        return true;
    }
}
