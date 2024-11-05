package groundbreaking.gigachat.listeners;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.collections.*;
import groundbreaking.gigachat.constructors.Chat;
import groundbreaking.gigachat.database.DatabaseQueries;
import groundbreaking.gigachat.utils.config.values.ChatValues;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;

public final class DisconnectListener implements Listener {

    private final ChatValues chatValues;
    private final CooldownsCollection cooldownsCollection;
    private final PmSoundsCollection pmSoundsCollection;
    private final DisabledPrivateMessagesCollection disabledPrivateMessagesCollection;

    public DisconnectListener(final GigaChat plugin) {
        this.chatValues = plugin.getChatValues();
        this.cooldownsCollection = plugin.getCooldownsCollection();
        this.pmSoundsCollection = plugin.getPmSoundsCollection();
        this.disabledPrivateMessagesCollection = plugin.getDisabled();
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        final String name = event.getPlayer().getName();
        this.loadData(name);
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        final String name = event.getPlayer().getName();
        this.removeCooldown(name);
    }

    @EventHandler(ignoreCancelled = true)
    public void onKick(final PlayerKickEvent event) {
        final String name = event.getPlayer().getName();
        this.removeCooldown(name);
    }

    private void loadData(final String name) {
        if (DatabaseQueries.disabledChatContainsPlayer(name)) {
            DisabledChatCollection.add(name);
        }
        if (DatabaseQueries.disabledPrivateMessagesContainsPlayer(name)) {
            this.disabledPrivateMessagesCollection.add(name);
        }
        if (DatabaseQueries.ignoreChatContains(name)) {
            IgnoreCollection.addToIgnoredChat(name, DatabaseQueries.getIgnoredChat(name));
        }
        if (DatabaseQueries.ignorePrivateContainsPlayer(name)) {
            IgnoreCollection.addToIgnoredPrivate(name, DatabaseQueries.getIgnoredPrivate(name));
        }
        if (DatabaseQueries.privateMessagesSoundsContainsPlayer(name)) {
            this.pmSoundsCollection.setSound(name, DatabaseQueries.getSound(name));
        }
        if (DatabaseQueries.socialSpyContainsPlayer(name)) {
            SocialSpyCollection.add(name);
        }
        if (DatabaseQueries.containsPlayerFromAutoMessages(name)) {
            AutoMessagesCollection.add(name);
        }
    }

    private void removeCooldown(final String name) {
        final Object2ObjectOpenHashMap<Character, Chat> chats = this.chatValues.getChats();
        for (final Map.Entry<Character, Chat> entry : chats.object2ObjectEntrySet()) {
            entry.getValue().getChatCooldowns().remove(name);
        }
        this.cooldownsCollection.removePlayerPrivateCooldown(name);
        this.cooldownsCollection.removePlayerIgnoreCooldown(name);
        this.cooldownsCollection.removePlayerSpyCooldown(name);
        this.cooldownsCollection.removeBroadcastCooldown(name);
        DisabledChatCollection.remove(name);
        this.disabledPrivateMessagesCollection.remove(name);
        IgnoreCollection.removeFromIgnoredChat(name);
        IgnoreCollection.removeFromIgnoredPrivate(name);
        LocalSpyCollection.remove(name);
        this.pmSoundsCollection.remove(name);
        ReplyCollection.removeFromAll(name);
        SocialSpyCollection.remove(name);
        AutoMessagesCollection.remove(name);
    }
}
