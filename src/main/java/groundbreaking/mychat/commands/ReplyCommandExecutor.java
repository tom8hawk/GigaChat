package groundbreaking.mychat.commands;

import groundbreaking.mychat.MyChat;
import groundbreaking.mychat.utils.ConfigValues;
import groundbreaking.mychat.utils.Utils;
import groundbreaking.mychat.utils.colorizer.IColorizer;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ReplyCommandExecutor implements CommandExecutor, TabCompleter {

    private final MyChat plugin;
    private final IColorizer colorizer;
    private final ConfigValues configValues;
    private final ConsoleCommandSender consoleSender;

    @Getter
    private static final HashMap<String, String> reply = new HashMap<>();

    private final String[] placeholders = { "{from-prefix}", "{from-name}", "{from-suffix}", "{to-prefix}", "{to-name}", "{to-suffix}", "{message}" };

    public ReplyCommandExecutor(MyChat plugin) {
        this.plugin = plugin;
        this.colorizer = plugin.getColorizer();
        this.configValues = plugin.getPluginConfig();
        this.consoleSender = plugin.getServer().getConsoleSender();
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String commandlabel, @NotNull String[] args) {

        if (!sender.hasPermission("mychat.privatemessage")) {
            sender.sendMessage(configValues.getNoPermissionMessage());
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(configValues.getReplyUsageError());
            return true;
        }

        final String senderName = sender.getName();
        final Player recipient = Bukkit.getPlayer(reply.get(senderName));

        if (recipient == null || !recipient.isOnline()) {
            sender.sendMessage(configValues.getPlayerNotFoundMessage());
            return true;
        }

        final String recipientName = recipient.getName();

        if (!IgnoreCommandExecutor.ignores(recipientName, senderName)) {
            sender.sendMessage(configValues.getRecipientIgnoresSender());
            return true;
        }

        if (!IgnoreCommandExecutor.ignores(senderName, recipientName)) {
            sender.sendMessage(configValues.getSenderIgnoresRecipient());
            return true;
        }

        String senderPrefix = "", senderSuffix = "";
        if (sender instanceof Player player) {
            senderPrefix = plugin.getChat().getPlayerPrefix(player);
            senderSuffix = plugin.getChat().getPlayerSuffix(player);
        }

        final String recipientPrefix = plugin.getChat().getPlayerPrefix(recipient);
        final String recipientSuffix = plugin.getChat().getPlayerSuffix(recipient);

        final StringBuilder builder = new StringBuilder();

        for (int i = 0; i < args.length; i++) {
            builder.append(" ").append(args[i]);
        }

        final String[] replacementList = { senderPrefix, senderName, senderSuffix, recipientPrefix, recipientName, recipientSuffix, builder.toString().trim()};

        sender.sendMessage(colorizer.colorize(Utils.replaceEach(configValues.getPmSenderFormat(), placeholders, replacementList)));
        recipient.sendMessage(colorizer.colorize(Utils.replaceEach(configValues.getPmRecipientFormat(), placeholders, replacementList)));

        reply.put(recipientName, senderName);

        if (configValues.isPmSoundEnabled()) {
            recipient.playSound(recipient, configValues.getPmSound(), configValues.getPmSoundVolume(), configValues.getPmSoundPitch());
        }

        if (configValues.isPrintPmToConsole() && !(sender instanceof ConsoleCommandSender)) {
            consoleSender.sendMessage(colorizer.colorize(Utils.replaceEach(configValues.getPmConsoleFormat(), placeholders, replacementList)));
        }

        processSocialspy(replacementList);

        return true;
    }

    private void processSocialspy(String[] replacementList) {
        final List<String> players = SocialSpyCommandExecutor.getListening();
        for (int i = 0; i < players.size(); i++) {
            final Player player = Bukkit.getPlayer(players.get(i));
            if (player != null) {
                player.sendMessage(colorizer.colorize(Utils.replaceEach(configValues.getPmSocialSpyFormat(), placeholders, replacementList)));
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
