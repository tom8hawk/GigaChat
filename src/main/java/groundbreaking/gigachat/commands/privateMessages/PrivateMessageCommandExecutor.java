package groundbreaking.gigachat.commands.privateMessages;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.collections.*;
import groundbreaking.gigachat.database.DatabaseQueries;
import groundbreaking.gigachat.utils.Utils;
import groundbreaking.gigachat.utils.colorizer.IColorizer;
import groundbreaking.gigachat.utils.config.values.Messages;
import groundbreaking.gigachat.utils.config.values.PrivateMessagesValues;
import groundbreaking.gigachat.utils.vanish.IVanishChecker;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class PrivateMessageCommandExecutor implements CommandExecutor, TabCompleter {

    private final GigaChat plugin;
    private final PrivateMessagesValues pmValues;
    private final Messages messages;
    private final IColorizer hexColorizer;
    private final Cooldowns cooldowns;
    private final DisabledPrivateMessages disabled;
    private final IVanishChecker vanishChecker;
    private final ConsoleCommandSender consoleSender;

    @Getter
    private final String[] placeholders = { "{from-prefix}", "{from-name}", "{from-suffix}", "{to-prefix}", "{to-name}", "{to-suffix}" };

    public PrivateMessageCommandExecutor(final GigaChat plugin) {
        this.plugin = plugin;
        this.pmValues = plugin.getPmValues();
        this.messages = plugin.getMessages();
        this.hexColorizer = plugin.getColorizerByVersion();
        this.cooldowns = plugin.getCooldowns();
        this.disabled = plugin.getDisabled();
        this.vanishChecker = plugin.getVanishChecker();
        this.consoleSender = plugin.getServer().getConsoleSender();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("gigachat.command.pm")) {
            sender.sendMessage(messages.getNoPermission());
            return true;
        }

        final boolean isPlayerSender = sender instanceof Player;
        if (args.length == 1 && isPlayerSender && args[0].equalsIgnoreCase("disable")) {
            processDisable((Player) sender);
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(messages.getPmUsageError());
            return true;
        }

        final Player recipient = Bukkit.getPlayer(args[0]);

        if (recipient == null || (isPlayerSender && !((Player) sender).canSee(recipient)) || vanishChecker.isVanished(recipient)) {
            sender.sendMessage(messages.getPlayerNotFound());
            return true;
        }

        if (recipient == sender) {
            sender.sendMessage(messages.getCannotChatWithHimself());
            return true;
        }

        if (isPlayerSender && cooldowns.hasCooldown((Player) sender, sender.getName(), "gigachat.bypass.cooldown.pm", cooldowns.getPrivateCooldowns())) {
            final String restTime = Utils.getTime(
                    (int) (pmValues.getPmCooldown() / 1000 + (cooldowns.getPrivateCooldowns().get(sender.getName()) - System.currentTimeMillis()) / 1000)
            );
            sender.sendMessage(messages.getCommandCooldownMessage().replace("{time}", restTime));
            return true;
        }

        final String senderName = sender.getName();
        final String recipientName = recipient.getName();

        if (isPlayerSender && !sender.hasPermission("gigachat.bypass.ignore")) {
            if (Ignore.isIgnoredPrivate(recipientName, senderName)) {
                sender.sendMessage(messages.getRecipientIgnoresSender());
                return true;
            }

            if (Ignore.isIgnoredPrivate(senderName, recipientName)) {
                sender.sendMessage(messages.getSenderIgnoresRecipient());
                return true;
            }

            if (disabled.contains(recipientName)) {
                sender.sendMessage(messages.getHasDisabledPm());
                return true;
            }
        }

        final String[] replacementList = getReplacements(sender, recipient, isPlayerSender, senderName, recipientName);
        final String message = getMessage(sender, args, isPlayerSender);

        process(sender, recipient, senderName, recipientName, isPlayerSender, message, replacementList);

        return true;
    }

    private void processDisable(final Player sender) {
        final String name = sender.getName();
        if (disabled.contains(name)) {
            disabled.remove(name);
            DatabaseQueries.removePlayerFromDisabledPrivateMessages(name);
            sender.sendMessage(messages.getPmDisabled());
        } else {
            disabled.add(name);
            sender.sendMessage(messages.getPmEnabled());
        }
    }

    private String[] getReplacements(final CommandSender sender, final Player recipient, final boolean isPlayerSender, final String senderName, final String recipientName) {
        String senderPrefix = "", senderSuffix = "";
        if (isPlayerSender) {
            final Player player = (Player) sender;
            senderPrefix = plugin.getChat().getPlayerPrefix(player);
            senderSuffix = plugin.getChat().getPlayerSuffix(player);
        }

        final String recipientPrefix = plugin.getChat().getPlayerPrefix(recipient);
        final String recipientSuffix = plugin.getChat().getPlayerSuffix(recipient);

        return new String[]{senderPrefix, senderName, senderSuffix, recipientPrefix, recipientName, recipientSuffix};
    }

    private void process(final CommandSender sender, final Player recipient, final String senderName,
                         final String recipientName, final boolean isPlayerSender, final String message, final String[] replacementList) {
        final String formattedMessageForSender, formattedMessageForRecipient, formattedMessageForConsole, formattedMessageForSocialSpy;
        if (isPlayerSender) {
            final Player playerSender = (Player) sender;
            formattedMessageForSender = getFormattedMessage(playerSender, message, pmValues.getSenderFormat(), replacementList);
            formattedMessageForRecipient = getFormattedMessage(playerSender, message, pmValues.getRecipientFormat(), replacementList);
            formattedMessageForConsole = getFormattedMessage(playerSender, message, pmValues.getConsoleFormat(), replacementList);
            formattedMessageForSocialSpy = getFormattedMessage(playerSender, message, pmValues.getSocialSpyFormat(), replacementList);

            Reply.add(recipientName, senderName);
            processLogs(formattedMessageForConsole);
            SocialSpy.sendAll(playerSender, recipient, formattedMessageForSocialSpy);
        } else {
            formattedMessageForSender = getFormattedMessage(message, pmValues.getSenderFormat(), replacementList);
            formattedMessageForRecipient = getFormattedMessage(message, pmValues.getRecipientFormat(), replacementList);
        }

        sender.sendMessage(formattedMessageForSender);
        recipient.sendMessage(formattedMessageForRecipient);

        playSound(recipient);
    }

    public String getFormattedMessage(final Player player, final String message, final String format, final String[] replacementList) {
        final String formatted = hexColorizer.colorize(
                Utils.replacePlaceholders(player,
                        Utils.replaceEach(format, placeholders, replacementList)
                )
        );

        return formatted.replace("{message}", message).replace("%", "%%");
    }

    public String getFormattedMessage(final String message, final String format, final String[] replacementList) {
        final String formatted = hexColorizer.colorize(
                Utils.replaceEach(format, placeholders, replacementList)
        );

        return formatted.replace("{message}", message).replace("%", "%%");
    }

    private String getMessage(final CommandSender sender, final String[] args, final boolean isPlayerSender) {
        if (isPlayerSender) {
            return pmValues.getMessagesColorizer().colorize((Player) sender, String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim());
        }

        return hexColorizer.colorize(String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim());
    }

    private void playSound(final Player recipient) {
        if (pmValues.isSoundEnabled()) {
            final Sound sound = PmSounds.getSound(recipient.getName());
            if (sound != null) {
                recipient.playSound(recipient.getLocation(), sound, pmValues.getSoundVolume(), pmValues.getSoundPitch());
            }
        }
    }

    private void processLogs(final String formattedMessage) {
        if (pmValues.isPrintLogsToConsole()) {
            consoleSender.sendMessage(formattedMessage);
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            final String input = args[0].toLowerCase();
            final List<String> players = new ArrayList<>();
            if (sender instanceof Player playerSender) {
                for (final Player player : Bukkit.getOnlinePlayers()) {
                    final String playerName = player.getName();
                    if (Ignore.isIgnoredChat(playerSender.getName(), playerName) || Ignore.isIgnoredChat(playerName, playerSender.getName())) {
                        continue;
                    }

                    if (playerName.toLowerCase().startsWith(input) && !vanishChecker.isVanished(player)) {
                        players.add(playerName);
                    }
                }
            }
            else {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    final String playerName = player.getName();
                    if (playerName.toLowerCase().startsWith(input)) {
                        players.add(playerName);
                    }
                }
            }

            if ("disable".startsWith(input) && sender.hasPermission("gigachat.disable.pm")) {
                players.add("disable");
            }

            return players;
        }

        return Collections.emptyList();
    }
}
