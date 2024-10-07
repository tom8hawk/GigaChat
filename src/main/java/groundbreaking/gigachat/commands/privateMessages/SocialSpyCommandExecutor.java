package groundbreaking.gigachat.commands.privateMessages;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.collections.Cooldowns;
import groundbreaking.gigachat.collections.SocialSpy;
import groundbreaking.gigachat.utils.Utils;
import groundbreaking.gigachat.utils.config.values.Messages;
import groundbreaking.gigachat.utils.config.values.PrivateMessagesValues;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public final class SocialSpyCommandExecutor implements CommandExecutor, TabCompleter {

    private final PrivateMessagesValues pmValues;
    private final Messages messages;
    private final Cooldowns cooldowns;

    public SocialSpyCommandExecutor(final GigaChat plugin) {
        this.pmValues = plugin.getPmValues();
        this.messages = plugin.getMessages();
        this.cooldowns = plugin.getCooldowns();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player playerSender)) {
            sender.sendMessage(messages.getPlayerOnly());
            return true;
        }

        if (!sender.hasPermission("gigachat.command.socialspy")) {
            sender.sendMessage(messages.getNoPermission());
            return true;
        }

        final String senderName = sender.getName();
        if (cooldowns.hasCooldown(playerSender, senderName, "gigachat.bypass.cooldown.socialspy", cooldowns.getSpyCooldowns())) {
            final String restTime = Utils.getTime(
                    (int) (pmValues.getSpyCooldown() / 1000 + (cooldowns.getSpyCooldowns().get(senderName) - System.currentTimeMillis()) / 1000)
            );
            sender.sendMessage(messages.getCommandCooldownMessage().replace("{time}", restTime));
            return true;
        }

        if (SocialSpy.contains(senderName)) {
            SocialSpy.remove(senderName);
            sender.sendMessage(messages.getSpyDisabled());
        }
        else {
            SocialSpy.add(senderName);
            sender.sendMessage(messages.getSpyEnabled());
        }

        cooldowns.addCooldown(senderName, cooldowns.getSpyCooldowns());

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
