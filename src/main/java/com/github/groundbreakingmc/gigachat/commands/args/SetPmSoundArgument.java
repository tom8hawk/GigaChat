package com.github.groundbreakingmc.gigachat.commands.args;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.collections.PmSoundsCollection;
import com.github.groundbreakingmc.gigachat.constructors.ArgsConstructor;
import com.github.groundbreakingmc.gigachat.database.DatabaseHandler;
import com.github.groundbreakingmc.gigachat.database.DatabaseQueries;
import com.github.groundbreakingmc.gigachat.utils.config.values.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

public final class SetPmSoundArgument extends ArgsConstructor {

    private final GigaChat plugin;
    private final Messages messages;
    private final PmSoundsCollection pmSoundsCollection;

    public SetPmSoundArgument(final GigaChat plugin, final String name, final String permission) {
        super(name, permission);
        this.plugin = plugin;
        this.messages = plugin.getMessages();
        this.pmSoundsCollection = plugin.getPmSoundsCollection();
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {

        if (args.length != 3) {
            sender.sendMessage(this.messages.getSetpmsoundUsageError());
            return true;
        }

        final Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(this.messages.getPlayerNotFound());
            return true;
        }

        final UUID targetUUID = target.getUniqueId();
        if (args[2].equalsIgnoreCase("none")) {
            this.pmSoundsCollection.remove(targetUUID);
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                try (final Connection connection = DatabaseHandler.getConnection()) {
                    DatabaseQueries.executeUpdateQuery(DatabaseQueries.REMOVE_PLAYER_FROM_PRIVATE_MESSAGES_SOUNDS, connection, targetUUID.toString());
                } catch (final SQLException ex) {
                    ex.printStackTrace();
                }
            });

            final boolean messageForTargetIsEmpty = !this.messages.getPmSoundRemoved().isEmpty();
            if (sender != target || messageForTargetIsEmpty) {
                sender.sendMessage(this.messages.getTargetPmSoundRemoved()
                        .replace("{player}", target.getName())
                );
            }

            if (!messageForTargetIsEmpty) {
                target.sendMessage(this.messages.getPmSoundRemoved());
            }

            return true;
        }

        final Sound sound;
        try {
            sound = Sound.valueOf(args[2]);
        } catch (IllegalArgumentException ignore) {
            sender.sendMessage(this.messages.getSoundNotFound());
            return true;
        }

        this.pmSoundsCollection.setSound(targetUUID, sound);

        final String targetName = target.getName();
        final boolean messageForTargetIsEmpty = !this.messages.getPmSoundRemoved().isEmpty();
        if (sender != target || messageForTargetIsEmpty) {
            sender.sendMessage(this.messages.getTargetPmSoundSet()
                    .replace("{player}", targetName)
                    .replace("{sound}", sound.name())
            );
        }

        if (!messageForTargetIsEmpty) {
            target.sendMessage(this.messages.getPmSoundSet()
                    .replace("{sound}", sound.name())
            );
        }

        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try (final Connection connection = DatabaseHandler.getConnection()) {
                DatabaseQueries.executeUpdateQuery(DatabaseQueries.ADD_PLAYER_PM_SOUND_TO_PRIVATE_MESSAGES_SOUNDS, connection, targetUUID.toString(), sound.name());
            } catch (final SQLException ex) {
                ex.printStackTrace();
            }
        });

        return true;
    }
}
