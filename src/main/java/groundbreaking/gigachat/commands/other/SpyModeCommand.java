package groundbreaking.gigachat.commands.other;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.constructors.Chat;
import groundbreaking.gigachat.database.DatabaseQueries;
import groundbreaking.gigachat.utils.Utils;
import groundbreaking.gigachat.utils.config.values.Messages;
import groundbreaking.gigachat.utils.map.ExpiringMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class SpyModeCommand {

    private final GigaChat plugin;
    private final Messages messages;

    public SpyModeCommand(final GigaChat plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
    }

    public boolean execute(@NotNull CommandSender sender, @NotNull Chat chat, final ExpiringMap<UUID, Long> spyCooldowns, final int spyCooldown) {
        if (!(sender instanceof final Player playerSender)) {
            sender.sendMessage(this.messages.getPlayerOnly());
            return true;
        }

        if (!sender.hasPermission("gigachat.command.spy." + chat.getName())) {
            sender.sendMessage(this.messages.getNoPermission());
            return true;
        }

        final UUID senderUUID = playerSender.getUniqueId();
        if (!sender.hasPermission("gigachat.bypass.spycooldown." + chat.getName()) && spyCooldowns.containsKey(senderUUID)) {
            final int time = (int) (spyCooldown / 1000 + (spyCooldowns.get(senderUUID) - System.currentTimeMillis()) / 1000);
            final String restTime = Utils.getTime(time);
            final String cooldownMessage = messages.getCommandCooldownMessage().replace("{time}", restTime);
            sender.sendMessage(cooldownMessage);
            return true;
        }

        final String chatName = chat.getName();
        final String replacement = this.messages.getChatsNames().getOrDefault(chatName, chatName);

        final List<UUID> players = chat.getSpyListeners();
        if (players.contains(senderUUID)) {
            sender.sendMessage(this.messages.getChatsSpyDisabled().replace("{chat}", replacement));
            players.remove(senderUUID);
            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () ->
                    DatabaseQueries.removeChatForPlayerFromChatsListeners(senderUUID, chatName)
            );
        } else {
            sender.sendMessage(this.messages.getChatsSpyEnabled().replace("{chat}", replacement));
            players.add(senderUUID);
            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () ->
                    DatabaseQueries.addPlayerToChatListeners(senderUUID, List.of(chatName))
            );
        }

        return true;
    }

}
