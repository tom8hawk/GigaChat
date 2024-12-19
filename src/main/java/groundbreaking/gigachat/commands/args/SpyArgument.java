package groundbreaking.gigachat.commands.args;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.constructors.ArgsConstructor;
import groundbreaking.gigachat.constructors.Chat;
import groundbreaking.gigachat.utils.config.values.ChatValues;
import groundbreaking.gigachat.utils.config.values.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SpyArgument extends ArgsConstructor {

    private final Messages messages;
    private final ChatValues chatValues;

    public SpyArgument(final GigaChat plugin, final String name, final String permission) {
        super(name, permission);
        this.messages = plugin.getMessages();
        this.chatValues = plugin.getChatValues();
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {

        if (args.length < 3) {
            sender.sendMessage(this.messages.getSpyUsageError());
            return true;
        }

        final Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(this.messages.getPlayerNotFound());
            return true;
        }

        final String specifiedName = args[2];
        final Chat defaultChat = this.chatValues.getDefaultChat();
        if (defaultChat.getName().equals(specifiedName)) {
            return this.process(sender, target, defaultChat);
        } else {
            final Map<Character, Chat> chats = this.chatValues.getChats();
            final Set<Character> keys = chats.keySet();
            for (final char key : keys) {
                final Chat chat = chats.get(key);
                final String chatName = chat.getName();
                if (chatName.equals(specifiedName)) {
                    return this.process(sender, target, chat);
                }
            }
        }

        sender.sendMessage(this.messages.getChatNotFound());
        return true;
    }

    private boolean process(final CommandSender sender, final Player target, final Chat chat) {
        final Set<UUID> players = chat.getSpyListeners();
        final String chatName = chat.getName();
        final String replacement = this.messages.getChatsNames().getOrDefault(chatName, chatName);
        final String targetName = target.getName();
        final UUID targetUUID = target.getUniqueId();
        if (players.contains(targetUUID)) {
            sender.sendMessage(this.messages.getChatsSpyDisabledOther().replace("{player}", targetName).replace("{chat}", replacement));
            final String message = this.messages.getChatsSpyDisabledByOther();
            if (!message.isEmpty()){
                target.sendMessage(message.replace("{chat}", replacement));
            }
            players.remove(targetUUID);
        } else {
            sender.sendMessage(this.messages.getChatsSpyEnabledOther().replace("{player}", targetName).replace("{chat}", replacement));
            final String message = this.messages.getChatsSpyEnabledByOther();
            if (!message.isEmpty()){
                target.sendMessage(message.replace("{chat}", replacement));
            }
            players.add(targetUUID);
        }

        return true;
    }
}
