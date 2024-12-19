package com.github.groundbreakingmc.gigachat.commands.other;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.constructors.Chat;
import com.github.groundbreakingmc.gigachat.database.DatabaseHandler;
import com.github.groundbreakingmc.gigachat.database.DatabaseQueries;
import com.github.groundbreakingmc.gigachat.utils.Utils;
import com.github.groundbreakingmc.gigachat.utils.config.values.Messages;
import com.github.groundbreakingmc.gigachat.utils.map.ExpiringMap;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;
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

        final Set<UUID> players = chat.getSpyListeners();
        if (players.contains(senderUUID)) {
            sender.sendMessage(this.messages.getChatsSpyDisabled().replace("{chat}", replacement));
            players.remove(senderUUID);
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                try (final Connection connection = DatabaseHandler.getConnection()) {
                    DatabaseQueries.executeUpdateQuery(DatabaseQueries.REMOVE_CHAT_FOR_PLAYER_FROM_CHATS_LISTENERS, connection, senderUUID.toString(), chatName);
                } catch (final SQLException ex) {
                    ex.printStackTrace();
                }
            });
        } else {
            sender.sendMessage(this.messages.getChatsSpyEnabled().replace("{chat}", replacement));
            players.add(senderUUID);
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                try (final Connection connection = DatabaseHandler.getConnection()) {
                    DatabaseQueries.executeUpdateQuery(DatabaseQueries.ADD_PLAYER_TO_CHAT_LISTENERS, connection, senderUUID.toString(), chatName);
                } catch (final SQLException ex) {
                    ex.printStackTrace();
                }
            });
        }

        return true;
    }

}
