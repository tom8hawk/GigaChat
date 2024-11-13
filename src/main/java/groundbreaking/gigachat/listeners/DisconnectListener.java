package groundbreaking.gigachat.listeners;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.collections.*;
import groundbreaking.gigachat.constructors.Chat;
import groundbreaking.gigachat.database.DatabaseQueries;
import groundbreaking.gigachat.utils.config.values.ChatValues;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class DisconnectListener implements Listener {

    private final GigaChat plugin;
    private final ChatValues chatValues;
    private final PmSoundsCollection pmSoundsCollection;
    private final DisabledPrivateMessagesCollection disabledPrivateMessagesCollection;

    public DisconnectListener(final GigaChat plugin) {
        this.plugin = plugin;
        this.chatValues = plugin.getChatValues();
        this.pmSoundsCollection = plugin.getPmSoundsCollection();
        this.disabledPrivateMessagesCollection = plugin.getDisabled();
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        final UUID playerUUID = event.getPlayer().getUniqueId();
        this.loadData(playerUUID);
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        final UUID playerUUID = event.getPlayer().getUniqueId();
        this.removeData(playerUUID);
    }

    @EventHandler(ignoreCancelled = true)
    public void onKick(final PlayerKickEvent event) {
        final UUID playerUUID = event.getPlayer().getUniqueId();
        this.removeData(playerUUID);
    }

    private void loadData(final UUID playerUUID) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            if (DatabaseQueries.disabledChatContainsPlayer(playerUUID)) {
                DisabledChatCollection.add(playerUUID);
            }
            if (DatabaseQueries.disabledPrivateMessagesContainsPlayer(playerUUID)) {
                this.disabledPrivateMessagesCollection.add(playerUUID);
            }
            final List<UUID> ignoredChat = DatabaseQueries.getIgnoredChat(playerUUID);
            if (!ignoredChat.isEmpty()) {
                IgnoreCollections.addToIgnoredChat(playerUUID, ignoredChat);
            }
            final List<UUID> ignoredPrivate = DatabaseQueries.getIgnoredPrivate(playerUUID);
            if (!ignoredPrivate.isEmpty()) {
                IgnoreCollections.addToIgnoredPrivate(playerUUID, ignoredPrivate);
            }
            final Sound sound = DatabaseQueries.getSound(playerUUID);
            if (sound != null) {
                this.pmSoundsCollection.setSound(playerUUID, sound);
            }
            if (DatabaseQueries.socialSpyContainsPlayer(playerUUID)) {
                SocialSpyCollection.add(playerUUID);
            }
            if (DatabaseQueries.autoMessagesContainsPlayer(playerUUID)) {
                AutoMessagesCollection.add(playerUUID);
            }

            this.loadPlayerListenData(playerUUID);
        });
    }

    private void loadPlayerListenData(final UUID playerUUID) {
        final List<String> chatsWherePlayerListen = DatabaseQueries.getChatsWherePlayerListen(playerUUID);
        final Object2ObjectOpenHashMap<Character, Chat> chats = this.chatValues.getChats();
        for (final Map.Entry<Character, Chat> entry : chats.object2ObjectEntrySet()) {
            final Chat chat = entry.getValue();
            if (chatsWherePlayerListen.contains(chat.getName())) {
                chat.getSpyListeners().add(playerUUID);
                chatsWherePlayerListen.remove(chat.getName());
            }
        }
        for (int i = 0; i < chatsWherePlayerListen.size(); i++) {
            final String chatName = chatsWherePlayerListen.get(i);
            DatabaseQueries.removeChatForPlayerFromChatsListeners(playerUUID, chatName);
        }
    }

    private void removeData(final UUID playerUUID) {
        DisabledChatCollection.remove(playerUUID);
        this.disabledPrivateMessagesCollection.remove(playerUUID);
        IgnoreCollections.removeFromIgnoredChat(playerUUID);
        IgnoreCollections.removeFromIgnoredPrivate(playerUUID);
        this.pmSoundsCollection.remove(playerUUID);
        ReplyCollection.removeFromAll(playerUUID);
        SocialSpyCollection.remove(playerUUID);
        AutoMessagesCollection.remove(playerUUID);
    }
}
