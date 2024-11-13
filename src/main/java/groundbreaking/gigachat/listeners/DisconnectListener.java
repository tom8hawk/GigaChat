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
    private final CooldownsCollection cooldownsCollection;
    private final PmSoundsCollection pmSoundsCollection;
    private final DisabledPrivateMessagesCollection disabledPrivateMessagesCollection;

    public DisconnectListener(final GigaChat plugin) {
        this.plugin = plugin;
        this.chatValues = plugin.getChatValues();
        this.cooldownsCollection = plugin.getCooldownsCollection();
        this.pmSoundsCollection = plugin.getPmSoundsCollection();
        this.disabledPrivateMessagesCollection = plugin.getDisabled();
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        final UUID targetUUID = event.getPlayer().getUniqueId();
        this.loadData(targetUUID);
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        final UUID targetUUID = event.getPlayer().getUniqueId();
        this.removeCooldown(targetUUID);
    }

    @EventHandler(ignoreCancelled = true)
    public void onKick(final PlayerKickEvent event) {
        final UUID targetUUID = event.getPlayer().getUniqueId();
        this.removeCooldown(targetUUID);
    }

    private void loadData(final UUID targetUUID) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            if (DatabaseQueries.disabledChatContainsPlayer(targetUUID)) {
                DisabledChatCollection.add(targetUUID);
            }
            if (DatabaseQueries.disabledPrivateMessagesContainsPlayer(targetUUID)) {
                this.disabledPrivateMessagesCollection.add(targetUUID);
            }
            final List<UUID> ignoredChat = DatabaseQueries.getIgnoredChat(targetUUID);
            if (!ignoredChat.isEmpty()) {
                IgnoreCollection.addToIgnoredChat(targetUUID, ignoredChat);
            }
            final List<UUID> ignoredPrivate = DatabaseQueries.getIgnoredPrivate(targetUUID);
            if (!ignoredPrivate.isEmpty()) {
                IgnoreCollection.addToIgnoredPrivate(targetUUID, ignoredPrivate);
            }
            final Sound sound = DatabaseQueries.getSound(targetUUID);
            if (sound != null) {
                this.pmSoundsCollection.setSound(targetUUID, sound);
            }
            if (DatabaseQueries.socialSpyContainsPlayer(targetUUID)) {
                SocialSpyCollection.add(targetUUID);
            }
            if (DatabaseQueries.containsPlayerFromAutoMessages(targetUUID)) {
                AutoMessagesCollection.add(targetUUID);
            }
        });
    }

    private void removeCooldown(final UUID targetUUID) {
        final Object2ObjectOpenHashMap<Character, Chat> chats = this.chatValues.getChats();
        for (final Map.Entry<Character, Chat> entry : chats.object2ObjectEntrySet()) {
            final Chat chat = entry.getValue();
            chat.getChatCooldowns().remove(targetUUID);
            chat.getSpyCooldowns().remove(targetUUID);
            chat.getSpyListeners().remove(targetUUID);
        }
        this.cooldownsCollection.removePlayerPrivateCooldown(targetUUID);
        this.cooldownsCollection.removePlayerIgnoreCooldown(targetUUID);
        this.cooldownsCollection.removePlayerSpyCooldown(targetUUID);
        this.cooldownsCollection.removeBroadcastCooldown(targetUUID);
        DisabledChatCollection.remove(targetUUID);
        this.disabledPrivateMessagesCollection.remove(targetUUID);
        IgnoreCollection.removeFromIgnoredChat(targetUUID);
        IgnoreCollection.removeFromIgnoredPrivate(targetUUID);
        this.pmSoundsCollection.remove(targetUUID);
        ReplyCollection.removeFromAll(targetUUID);
        SocialSpyCollection.remove(targetUUID);
        AutoMessagesCollection.remove(targetUUID);
    }
}
