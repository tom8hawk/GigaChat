package groundbreaking.gigachat.commands.other;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.constructors.Chat;
import groundbreaking.gigachat.utils.Utils;
import groundbreaking.gigachat.utils.config.values.Messages;
import groundbreaking.gigachat.utils.map.ExpiringMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SpyModeCommand {

    private final Messages messages;

    public SpyModeCommand(final GigaChat plugin) {
        this.messages = plugin.getMessages();
    }

    public boolean execute(@NotNull CommandSender sender, @NotNull Chat chat, final ExpiringMap<String, Long> spyCooldowns, final int spyCooldown) {
        if (!(sender instanceof final Player playerSender)) {
            sender.sendMessage(this.messages.getPlayerOnly());
            return true;
        }

        if (!sender.hasPermission("gigachat.command.spy." + chat.getName())) {
            sender.sendMessage(this.messages.getNoPermission());
            return true;
        }

        if (!sender.hasPermission("gigachat.bypass.spycooldown." + chat.getName()) && spyCooldowns.containsKey(sender.getName())) {
            final String senderName = sender.getName();
            final int time = (int) (spyCooldown / 1000 + (spyCooldowns.get(senderName) - System.currentTimeMillis()) / 1000);
            final String restTime = Utils.getTime(time);
            final String cooldownMessage = messages.getCommandCooldownMessage().replace("{time}", restTime);
            sender.sendMessage(cooldownMessage);
            return true;
        }

        final String chatName = chat.getName();
        final String replacement = this.messages.getChatsNames().getOrDefault(chatName, chatName);

        final List<Player> players = chat.getSpyListeners();
        if (players.contains(playerSender)) {
            sender.sendMessage(this.messages.getChatsSpyDisabled().replace("{chat}", replacement));
            players.remove(playerSender);
        } else {
            sender.sendMessage(this.messages.getChatsSpyEnabled().replace("{chat}", replacement));
            players.add(playerSender);
        }

        return true;
    }

}
