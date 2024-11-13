package groundbreaking.gigachat.commands.privateMessages;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.collections.CooldownCollections;
import groundbreaking.gigachat.collections.SocialSpyCollection;
import groundbreaking.gigachat.database.DatabaseQueries;
import groundbreaking.gigachat.utils.Utils;
import groundbreaking.gigachat.utils.config.values.Messages;
import groundbreaking.gigachat.utils.config.values.PrivateMessagesValues;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
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

        final UUID senderUUID = playerSender.getUniqueId();
        if (this.hasCooldown(playerSender, senderUUID)) {
            this.sendMessageHasCooldown(playerSender, senderUUID);
            return true;
        }

        if (SocialSpyCollection.contains(senderUUID)) {
            SocialSpyCollection.remove(senderUUID);
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () ->
                    DatabaseQueries.removePlayerFromSocialSpy(senderUUID)
            );
            sender.sendMessage(this.messages.getSpyDisabled());
        } else {
            SocialSpyCollection.add(senderUUID);
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () ->
                    DatabaseQueries.addPlayerToSocialSpy(senderUUID)
            );
            sender.sendMessage(this.messages.getSpyEnabled());
        }

        this.cooldownCollections.addCooldown(senderUUID, this.cooldownCollections.getSpyCooldowns());

        return true;
    }

    private boolean hasCooldown(final Player playerSender, final UUID senderUUID) {
        return this.cooldownCollections.hasCooldown(playerSender, senderUUID, "gigachat.bypass.cooldown.socialspy", this.cooldownCollections.getSpyCooldowns());
    }

    private void sendMessageHasCooldown(final Player playerSender, final UUID senderUUID) {
        final long timeLeftInMillis = this.cooldownCollections.getSpyCooldowns().get(senderUUID) - System.currentTimeMillis();
        final int result = (int) (this.pmValues.getSpyCooldown() / 1000 + timeLeftInMillis / 1000);
        final String restTime = Utils.getTime(result);
        final String message = this.messages.getCommandCooldownMessage().replace("{time}", restTime);
        playerSender.sendMessage(message);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
