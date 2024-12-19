package com.github.groundbreakingmc.gigachat.commands.args;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.collections.AutoMessagesCollection;
import com.github.groundbreakingmc.gigachat.constructors.ArgsConstructor;
import com.github.groundbreakingmc.gigachat.database.DatabaseHandler;
import com.github.groundbreakingmc.gigachat.database.DatabaseQueries;
import com.github.groundbreakingmc.gigachat.utils.config.values.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

public final class DisableAutoMessagesArgument extends ArgsConstructor {

    private final GigaChat plugin;
    private final Messages messages;

    public DisableAutoMessagesArgument(final GigaChat plugin, final String name, final String permission) {
        super(name, permission);
        this.plugin = plugin;
        this.messages = plugin.getMessages();
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(this.messages.getDisableAutoMessagesUsageError());
            return true;
        }

        final Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(this.messages.getPlayerNotFound());
            return true;
        }

        return this.process(sender, target);
    }

    private boolean process(final CommandSender sender, final Player target) {
        final UUID targetUUID = target.getUniqueId();
        if (AutoMessagesCollection.contains(targetUUID)) {
            AutoMessagesCollection.remove(targetUUID);
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                try (final Connection connection = DatabaseHandler.getConnection()) {
                    DatabaseQueries.executeUpdateQuery(DatabaseQueries.REMOVE_PLAYER_FROM_AUTO_MESSAGES, connection, targetUUID.toString());
                } catch (final SQLException ex) {
                    ex.printStackTrace();
                }
            });
            sender.sendMessage(this.messages.getAutoMessagesEnabledOther().replace("{player}", target.getName()));
            target.sendMessage(this.messages.getAutoMessagesEnabledByOther().replace("{player}", sender.getName()));
        } else {
            AutoMessagesCollection.add(targetUUID);
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                try (final Connection connection = DatabaseHandler.getConnection()) {
                    DatabaseQueries.executeUpdateQuery(DatabaseQueries.ADD_PLAYER_TO_AUTO_MESSAGES, connection, targetUUID.toString());
                } catch (final SQLException ex) {
                    ex.printStackTrace();
                }
            });
            sender.sendMessage(this.messages.getAutoMessagesDisabledOther().replace("{player}", target.getName()));
            target.sendMessage(this.messages.getAutoMessagesDisabledByOther().replace("{player}", sender.getName()));
        }

        return true;
    }
}
