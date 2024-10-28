package groundbreaking.gigachat.commands.privateMessages;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.collections.CooldownsMap;
import groundbreaking.gigachat.collections.SocialSpyMap;
import groundbreaking.gigachat.database.DatabaseQueries;
import groundbreaking.gigachat.utils.Utils;
import groundbreaking.gigachat.utils.config.values.Messages;
import groundbreaking.gigachat.utils.config.values.PrivateMessagesValues;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public final class SocialSpyCommandExecutor implements CommandExecutor, TabCompleter {

    private final PrivateMessagesValues pmValues;
    private final Messages messages;
    private final CooldownsMap cooldownsMap;

    public SocialSpyCommandExecutor(final GigaChat plugin) {
        this.pmValues = plugin.getPmValues();
        this.messages = plugin.getMessages();
        this.cooldownsMap = plugin.getCooldownsMap();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player playerSender)) {
            sender.sendMessage(this.messages.getPlayerOnly());
            return true;
        }

        if (!sender.hasPermission("gigachat.command.socialspy")) {
            sender.sendMessage(this.messages.getNoPermission());
            return true;
        }

        final String senderName = sender.getName();
        if (this.hasCooldown(playerSender, senderName)) {
            this.sendMessageHasCooldown(playerSender, senderName);
            return true;
        }

        if (SocialSpyMap.contains(senderName)) {
            SocialSpyMap.remove(senderName);
            DatabaseQueries.removePlayerFromSocialSpy(senderName);
            sender.sendMessage(this.messages.getSpyDisabled());
        } else {
            SocialSpyMap.add(senderName);
            sender.sendMessage(this.messages.getSpyEnabled());
        }

        this.cooldownsMap.addCooldown(senderName, this.cooldownsMap.getSpyCooldowns());

        return true;
    }

    private boolean hasCooldown(final Player playerSender, final String senderName) {
        return this.cooldownsMap.hasCooldown(playerSender, senderName, "gigachat.bypass.cooldown.socialspy", this.cooldownsMap.getSpyCooldowns());
    }

    private void sendMessageHasCooldown(final Player playerSender, final String senderName) {
        final long timeLeftInMillis = this.cooldownsMap.getSpyCooldowns().get(senderName) - System.currentTimeMillis();
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
