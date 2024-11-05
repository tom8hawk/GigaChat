package groundbreaking.gigachat.commands.privateMessages;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.collections.CooldownsCollection;
import groundbreaking.gigachat.collections.IgnoreCollection;
import groundbreaking.gigachat.database.DatabaseQueries;
import groundbreaking.gigachat.utils.Utils;
import groundbreaking.gigachat.utils.config.values.Messages;
import groundbreaking.gigachat.utils.config.values.PrivateMessagesValues;
import groundbreaking.gigachat.utils.vanish.VanishChecker;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class IgnoreCommandExecutor implements CommandExecutor, TabCompleter {

    private final PrivateMessagesValues pmValues;
    private final Messages messages;
    private final CooldownsCollection cooldownsCollection;
    private final VanishChecker vanishChecker;

    public IgnoreCommandExecutor(final GigaChat plugin) {
        this.pmValues = plugin.getPmValues();
        this.messages = plugin.getMessages();
        this.cooldownsCollection = plugin.getCooldownsCollection();
        this.vanishChecker = plugin.getVanishChecker();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player playerSender)) {
            sender.sendMessage(this.messages.getPlayerOnly());
            return true;
        }

        final String senderName = playerSender.getName();
        if (this.hasCooldown(playerSender, senderName)) {
            this.sendMessageHasCooldown(playerSender, senderName);
            return true;
        }

        final boolean hasChatIgnorePerm = sender.hasPermission("gigachat.command.ignore.chat");
        final boolean hasPrivateIgnorePerm = sender.hasPermission("gigachat.command.ignore.private");

        int argsLength = 1;
        IgnoreCommandExecutor.IgnoreType ignoreType = hasChatIgnorePerm ? IgnoreCommandExecutor.IgnoreType.CHAT : IgnoreCommandExecutor.IgnoreType.PRIVATE;

        if (hasChatIgnorePerm && hasPrivateIgnorePerm) {
            argsLength = 2;

            switch (args[0].toLowerCase()) {
                case "chat" -> {
                }
                case "private" -> ignoreType = IgnoreCommandExecutor.IgnoreType.PRIVATE;
                default -> {
                    playerSender.sendMessage(this.messages.getIgnoreUsageError());
                    return true;
                }
            }
        }

        if (args.length < argsLength) {
            sender.sendMessage(this.messages.getIgnoreUsageError());
            return true;
        }

        final Player target = Bukkit.getPlayer(args[argsLength]);

        if (target == null || !playerSender.canSee(target) || this.vanishChecker.isVanished(target)) {
            playerSender.sendMessage(this.messages.getPlayerNotFound());
            return true;
        }

        if (target == playerSender) {
            playerSender.sendMessage(this.messages.getCannotIgnoreHimself());
            return true;
        }

        final String targetName = target.getName();

        switch (ignoreType) {
            case CHAT -> this.processChat(playerSender, senderName, targetName);
            case PRIVATE -> this.processPrivate(playerSender, senderName, targetName);
            default -> playerSender.sendMessage(this.messages.getIgnoreUsageError());
        }

        return true;
    }

    private boolean hasCooldown(final Player playerSender, final String senderName) {
        return this.cooldownsCollection.hasCooldown(playerSender, senderName, "gigachat.bypass.cooldown.ignore", this.cooldownsCollection.getIgnoreCooldowns());
    }

    private void sendMessageHasCooldown(final Player playerSender, final String senderName) {
        final long timeLeftInMillis = this.cooldownsCollection.getIgnoreCooldowns().get(senderName) - System.currentTimeMillis();
        final int result = (int) (this.pmValues.getPmCooldown() / 1000 + timeLeftInMillis / 1000);
        final String restTime = Utils.getTime(result);
        final String message = this.messages.getCommandCooldownMessage().replace("{time}", restTime);
        playerSender.sendMessage(message);
    }

    private void processChat(final Player sender, final String senderName, final String targetName) {
        if (!IgnoreCollection.ignoredChatContains(senderName)) {
            IgnoreCollection.addToIgnoredChat(senderName, new ArrayList<>(List.of(targetName)));
            sender.sendMessage(this.messages.getChatIgnoreEnabled().replace("{player}", targetName));
            return;
        }

        if (IgnoreCollection.ignoredChatContains(senderName, targetName)) {
            IgnoreCollection.removeFromIgnoredChat(senderName, targetName);
            DatabaseQueries.removePlayerFromIgnoreChat(senderName);
            sender.sendMessage(this.messages.getChatIgnoreDisabled().replace("{player}", targetName));
        } else {
            IgnoreCollection.addToIgnoredChat(senderName, targetName);
            sender.sendMessage(this.messages.getChatIgnoreEnabled().replace("{player}", targetName));
        }

        this.cooldownsCollection.addCooldown(senderName, this.cooldownsCollection.getIgnoreCooldowns());
    }

    private void processPrivate(final Player sender, final String senderName, final String targetName) {
        if (!IgnoreCollection.ignoredPrivateContains(senderName)) {
            IgnoreCollection.addToIgnoredPrivate(senderName, new ArrayList<>(List.of(targetName)));
            sender.sendMessage(this.messages.getPrivateIgnoreEnabled().replace("{player}", targetName));
            return;
        }

        if (IgnoreCollection.ignoredPrivateContains(senderName, targetName)) {
            IgnoreCollection.removeFromIgnoredPrivate(senderName, targetName);
            DatabaseQueries.removePlayerFromIgnorePrivate(senderName);
            sender.sendMessage(this.messages.getPrivateIgnoreDisabled().replace("{player}", targetName));
        } else {
            IgnoreCollection.addToIgnoredPrivate(senderName, targetName);
            sender.sendMessage(this.messages.getPrivateIgnoreEnabled().replace("{player}", targetName));
        }

        this.cooldownsCollection.addCooldown(senderName, this.cooldownsCollection.getIgnoreCooldowns());
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player playerSender) {

            final boolean hasChatIgnorePerm = sender.hasPermission("gigachat.command.ignore.chat");
            final boolean hasPrivateIgnorePerm = sender.hasPermission("gigachat.command.ignore.private");

            if (hasChatIgnorePerm && hasPrivateIgnorePerm) {
                if (args.length == 1) {
                    return List.of("chat", "private");
                }

                if (args.length == 2) {
                    final String input = args[1].toLowerCase();
                    return this.getPlayer(playerSender, input);
                }
            } else if (args.length == 1) {
                final String input = args[0].toLowerCase();
                return this.getPlayer(playerSender, input);
            }
        }


        return Collections.emptyList();
    }

    private List<String> getPlayer(final Player sender, final String input) {
        final String senderName = sender.getName();

        final List<String> players = new ArrayList<>();
        final Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();

        for (final Player target : onlinePlayers) {
            if (sender == target) {
                continue;
            }

            final String playerName = target.getName();
            if (IgnoreCollection.isIgnoredChat(senderName, playerName) || IgnoreCollection.isIgnoredChat(playerName, senderName)) {
                continue;
            }

            if (playerName.toLowerCase().startsWith(input) && !this.vanishChecker.isVanished(target)) {
                players.add(playerName);
            }
        }

        return players;
    }

    private enum IgnoreType {
        CHAT,
        PRIVATE
    }
}
