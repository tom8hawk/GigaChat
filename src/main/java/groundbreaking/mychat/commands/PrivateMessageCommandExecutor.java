package groundbreaking.mychat.commands;

import groundbreaking.mychat.MyChat;
import groundbreaking.mychat.utils.ConfigValues;
import groundbreaking.mychat.utils.Utils;
import groundbreaking.mychat.utils.colorizer.IColorizer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class PrivateMessageCommandExecutor implements CommandExecutor, TabCompleter {

    private final MyChat plugin;
    private final ConfigValues pluginConfig;
    private final IColorizer colorizer;

    private final String[] placeholders = { "{from-prefix}", "{from-name}", "{from-suffix}", "{to-prefix}", "{to-name}", "{to-suffix}", "{message}" };

    public PrivateMessageCommandExecutor(MyChat plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
        this.colorizer = plugin.getColorizer();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("mychat.privatemessage")) {
            sender.sendMessage(pluginConfig.getNoPermissionMessage());
            return true;
        }

        final Player recipient = Bukkit.getPlayer(args[0]);

        if (recipient == null) {
            sender.sendMessage(pluginConfig.getPlayerNotFoundMessage());
            return true;
        }

        if (recipient == sender) {
            sender.sendMessage(pluginConfig.getCannotPmSelf());
            return true;
        }

        final String senderName = sender.getName();
        final String recipientName = recipient.getName();

        if (!IgnoreCommandExecutor.ignores(recipientName, senderName)) {
            sender.sendMessage(pluginConfig.getRecipientIgnoresSender());
            return true;
        }

        if (!IgnoreCommandExecutor.ignores(senderName, recipientName)) {
            sender.sendMessage(pluginConfig.getSenderIgnoresRecipient());
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

        sender.sendMessage(colorizer.colorize(Utils.replaceEach(pluginConfig.getPmSenderFormat(), placeholders, replacementList)));
        recipient.sendMessage(colorizer.colorize(Utils.replaceEach(pluginConfig.getPmRecipientFormat(), placeholders, replacementList)));

        if (pluginConfig.isPmSoundEnabled()) {
            recipient.playSound(recipient, pluginConfig.getPmSound(), pluginConfig.getPmSoundVolume(), pluginConfig.getPmSoundPitch());
        }

        processSocialSpy(replacementList);

        return true;
    }

    private void processSocialSpy(String[] replacementList) {
        final List<String> players = SocialSpyCommandExecutor.getListening();
        for (int i = 0; i < players.size(); i++) {
            final Player player = Bukkit.getPlayer(players.get(i));
            if (player != null) {
                player.sendMessage(colorizer.colorize(Utils.replaceEach(pluginConfig.getPmSocialSpyFormat(), placeholders, replacementList)));
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            return null;
        }

        return Collections.emptyList();
    }
}
