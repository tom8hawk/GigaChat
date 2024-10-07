package groundbreaking.gigachat.commands.privateMessages;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.collections.Cooldowns;
import groundbreaking.gigachat.collections.Ignore;
import groundbreaking.gigachat.database.DatabaseQueries;
import groundbreaking.gigachat.utils.Utils;
import groundbreaking.gigachat.utils.config.values.Messages;
import groundbreaking.gigachat.utils.config.values.PrivateMessagesValues;
import groundbreaking.gigachat.utils.vanish.IVanishChecker;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class IgnoreCommandExecutor implements CommandExecutor, TabCompleter {

    private final PrivateMessagesValues pmValues;
    private final Messages messages;
    private final Cooldowns cooldowns;
    private final IVanishChecker vanishChecker;

    public IgnoreCommandExecutor(final GigaChat plugin) {
        this.pmValues = plugin.getPmValues();
        this.messages = plugin.getMessages();
        this.cooldowns = plugin.getCooldowns();
        this.vanishChecker = plugin.getVanishChecker();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player playerSender)) {
            sender.sendMessage(messages.getPlayerOnly());
            return true;
        }

        final String senderName = playerSender.getName();
        if (cooldowns.hasCooldown(playerSender, senderName, "gigachat.bypass.cooldown.ignore", cooldowns.getIgnoreCooldowns())) {
            final String restTime = Utils.getTime(
                    (int) (pmValues.getPmCooldown() / 1000 + (cooldowns.getIgnoreCooldowns().get(senderName) - System.currentTimeMillis()) / 1000)
            );
            playerSender.sendMessage(messages.getCommandCooldownMessage().replace("{time}", restTime));
            return true;
        }

        final boolean hasChatIgnorePerm = sender.hasPermission("gigachat.command.ignore.chat");
        final boolean hasPrivateIgnorePerm = sender.hasPermission("gigachat.command.ignore.private");

        if (hasChatIgnorePerm && hasPrivateIgnorePerm) {
            if (args.length < 2) {
                sender.sendMessage(messages.getIgnoreUsageError());
                return true;
            }

            final Player target = Bukkit.getPlayer(args[1]);

            if (target == null || !playerSender.canSee(target) || vanishChecker.isVanished(target)) {
                sender.sendMessage(messages.getPlayerNotFound());
                return true;
            }

            if (target == sender) {
                sender.sendMessage(messages.getCannotIgnoreHimself());
                return true;
            }

            final String targetName = target.getName();

            switch(args[0].toLowerCase()) {
                case "chat" -> {
                    processChat(playerSender, senderName, targetName);
                }
                case "private" -> {
                    processPrivate(playerSender, senderName, targetName);
                }
                default -> {
                    playerSender.sendMessage(messages.getIgnoreUsageError());
                }
            }
            return true;
        }

        if (args.length < 1) {
            playerSender.sendMessage(messages.getIgnoreUsageError());
            return true;
        }

        final Player target = Bukkit.getPlayer(args[0]);

        if (target == null || !playerSender.canSee(target) || vanishChecker.isVanished(target)) {
            playerSender.sendMessage(messages.getPlayerNotFound());
            return true;
        }

        if (target == playerSender) {
            playerSender.sendMessage(messages.getCannotIgnoreHimself());
            return true;
        }

        final String targetName = target.getName();

        if (hasChatIgnorePerm) {
            processChat(playerSender, senderName, targetName);
        } else if (hasPrivateIgnorePerm) {
            processPrivate(playerSender, senderName, targetName);
        }

        return true;
    }

    private void processChat(final Player sender, final String senderName, final String targetName) {
        if (!Ignore.ignoredChatContains(senderName)) {
            Ignore.addToIgnoredChat(senderName, new ArrayList<>(List.of(targetName)));
            sender.sendMessage(messages.getChatIgnoreEnabled().replace("{player}", targetName));
            return;
        }

        if (Ignore.ignoredChatContains(senderName, targetName)) {
            Ignore.removeFromIgnoredChat(senderName, targetName);
            DatabaseQueries.removePlayerFromIgnoreChat(senderName);
            sender.sendMessage(messages.getChatIgnoreDisabled().replace("{player}", targetName));
        }
        else {
            Ignore.addToIgnoredChat(senderName, targetName);
            sender.sendMessage(messages.getChatIgnoreEnabled().replace("{player}", targetName));
        }

        cooldowns.addCooldown(senderName, cooldowns.getIgnoreCooldowns());
    }

    private void processPrivate(final Player sender, final String senderName, final String targetName) {
        if (!Ignore.ignoredPrivateContains(senderName)) {
            Ignore.addToIgnoredPrivate(senderName, new ArrayList<>(List.of(targetName)));
            sender.sendMessage(messages.getPrivateIgnoreEnabled().replace("{player}", targetName));
            return;
        }

        if (Ignore.ignoredPrivateContains(senderName, targetName)) {
            Ignore.removeFromIgnoredPrivate(senderName, targetName);
            DatabaseQueries.removePlayerFromIgnorePrivate(senderName);
            sender.sendMessage(messages.getPrivateIgnoreDisabled().replace("{player}", targetName));
        }
        else {
            Ignore.addToIgnoredPrivate(senderName, targetName);
            sender.sendMessage(messages.getPrivateIgnoreEnabled().replace("{player}", targetName));
        }

        cooldowns.addCooldown(senderName, cooldowns.getIgnoreCooldowns());
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player playerSender) {
            if (sender.hasPermission("gigachat.command.ignore.chat")
                    && sender.hasPermission("gigachat.command.ignore.private")) {
                if (args.length == 1) {
                    return List.of("chat", "private");
                }
                if (args.length == 2) {
                    final String input = args[1].toLowerCase();
                    return getPlayer(playerSender, input);
                }
            }
            else if (args.length == 1) {
                final String input = args[0].toLowerCase();
                return getPlayer(playerSender, input);
            }
        }


        return Collections.emptyList();
    }

    private List<String> getPlayer(final Player sender, final String input) {
        final List<String> players = new ArrayList<>();
        for (final Player player : Bukkit.getOnlinePlayers()) {
            final String playerName = player.getName();
            if (Ignore.isIgnoredChat(sender.getName(), playerName) || Ignore.isIgnoredChat(playerName, sender.getName())) {
                continue;
            }

            if (playerName.toLowerCase().startsWith(input) && !vanishChecker.isVanished(player)) {
                players.add(playerName);
            }
        }
        return players;
    }
}
