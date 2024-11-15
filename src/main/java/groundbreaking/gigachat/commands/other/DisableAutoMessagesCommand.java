package groundbreaking.gigachat.commands.other;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.collections.AutoMessagesCollection;
import groundbreaking.gigachat.database.DatabaseHandler;
import groundbreaking.gigachat.database.DatabaseQueries;
import groundbreaking.gigachat.utils.config.values.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class DisableAutoMessagesCommand implements CommandExecutor, TabCompleter {

    private final GigaChat plugin;
    private final Messages messages;

    public DisableAutoMessagesCommand(final GigaChat plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player playerSender)) {
            sender.sendMessage(this.messages.getPlayerOnly());
            return true;
        }

        if (!sender.hasPermission("gigachat.command.disableam.own")) {
            sender.sendMessage(this.messages.getNoPermission());
            return true;
        }

        return this.processDisable(playerSender);
    }

    private boolean processDisable(final Player sender) {
        final UUID senderUUID = sender.getUniqueId();
        if (AutoMessagesCollection.contains(senderUUID)) {
            AutoMessagesCollection.remove(senderUUID);
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                try (final Connection connection = DatabaseHandler.getConnection()) {
                    DatabaseQueries.executeUpdateQuery(DatabaseQueries.REMOVE_PLAYER_FROM_AUTO_MESSAGES, connection, senderUUID.toString());
                } catch (final SQLException ex) {
                    ex.printStackTrace();
                }
            });
            sender.sendMessage(this.messages.getAutoMessagesEnabled());
        } else {
            AutoMessagesCollection.add(senderUUID);
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                try (final Connection connection = DatabaseHandler.getConnection()) {
                    DatabaseQueries.executeUpdateQuery(DatabaseQueries.ADD_PLAYER_TO_AUTO_MESSAGES, connection, senderUUID.toString());
                } catch (final SQLException ex) {
                    ex.printStackTrace();
                }
            });
            sender.sendMessage(this.messages.getAutoMessagesDisabled());
        }

        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
