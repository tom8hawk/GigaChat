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

        this.plugin.reloadConfig();
        this.plugin.setupVanishChecker();
        this.plugin.setupAll();
        this.plugin.getCooldowns().setCooldowns();
        this.plugin.getAutoMessages().run();
        this.plugin.registerReloadableEvents();
        this.plugin.getPmSounds().setDefaultSound(this.plugin);

        final long endTime = System.currentTimeMillis();
        final String result = String.valueOf(endTime - startTime);
        final String message = this.messages.getReloadMessage().replace("{time}", result);
        if (sender instanceof Player) {
            this.plugin.getServer().getConsoleSender().sendMessage();
        }
        sender.sendMessage(message);

        return true;
    }
}
