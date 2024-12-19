package com.github.groundbreakingmc.gigachat.commands.privateMessages;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.collections.CooldownCollections;
import com.github.groundbreakingmc.gigachat.collections.SocialSpyCollection;
import com.github.groundbreakingmc.gigachat.database.DatabaseHandler;
import com.github.groundbreakingmc.gigachat.database.DatabaseQueries;
import com.github.groundbreakingmc.gigachat.utils.Utils;
import com.github.groundbreakingmc.gigachat.utils.config.values.Messages;
import com.github.groundbreakingmc.gigachat.utils.config.values.PrivateMessagesValues;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public final class SocialSpyCommandExecutor implements CommandExecutor, TabCompleter {

    private final GigaChat plugin;
    private final PrivateMessagesValues pmValues;
    private final Messages messages;
    private final CooldownCollections cooldownCollections;

    public SocialSpyCommandExecutor(final GigaChat plugin) {
        this.plugin = plugin;
        this.pmValues = plugin.getPmValues();
        this.messages = plugin.getMessages();
        this.cooldownCollections = plugin.getCooldownCollections();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof final Player playerSender)) {
            sender.sendMessage(this.messages.getPlayerOnly());
            return true;
        }

        if (!sender.hasPermission("gigachat.command.socialspy")) {
            sender.sendMessage(this.messages.getNoPermission());
            return true;
        }

        if (this.hasCooldown(playerSender)) {
            this.sendMessageHasCooldown(playerSender);
            return true;
        }

        final UUID senderUUID = playerSender.getUniqueId();
        if (SocialSpyCollection.contains(senderUUID)) {
            SocialSpyCollection.remove(senderUUID);
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                try (final Connection connection = DatabaseHandler.getConnection()) {
                    DatabaseQueries.executeUpdateQuery(DatabaseQueries.REMOVE_PLAYER_FROM_SOCIAL_SPY, connection, senderUUID.toString());
                } catch (final SQLException ex) {
                    ex.printStackTrace();
                }
            });
            sender.sendMessage(this.messages.getSpyDisabled());
        } else {
            SocialSpyCollection.add(senderUUID);
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                try (final Connection connection = DatabaseHandler.getConnection()) {
                    DatabaseQueries.executeUpdateQuery(DatabaseQueries.ADD_PLAYER_TO_SOCIAL_SPY, connection, senderUUID.toString());
                } catch (final SQLException ex) {
                    ex.printStackTrace();
                }
            });
            sender.sendMessage(this.messages.getSpyEnabled());
        }

        this.cooldownCollections.addCooldown(senderUUID, this.cooldownCollections.getSpyCooldowns());

        return true;
    }

    private boolean hasCooldown(final Player sender) {
        return this.cooldownCollections.hasCooldown(sender, "gigachat.bypass.cooldown.socialspy", this.cooldownCollections.getSpyCooldowns());
    }

    private void sendMessageHasCooldown(final Player sender) {
        final long timeLeftInMillis = this.cooldownCollections.getSpyCooldowns()
                .get(sender.getUniqueId()) - System.currentTimeMillis();
        final int result = (int) (this.pmValues.getSpyCooldown() / 1000 + timeLeftInMillis / 1000);
        final String restTime = Utils.getTime(result);
        final String message = this.messages.getCommandCooldownMessage().replace("{time}", restTime);
        sender.sendMessage(message);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
