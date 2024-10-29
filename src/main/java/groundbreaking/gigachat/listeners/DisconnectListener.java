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
    private final CooldownsMap cooldownsMap;
    private final PmSoundsMap pmSoundsMap;
    private final DisabledPrivateMessagesMap disabledPrivateMessagesMap;

    public DisconnectListener(final GigaChat plugin) {
        this.chatValues = plugin.getChatValues();
        this.cooldownsMap = plugin.getCooldownsMap();
        this.pmSoundsMap = plugin.getPmSoundsMap();
        this.disabledPrivateMessagesMap = plugin.getDisabled();
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        final String name = event.getPlayer().getName();
        this.loadData(name);
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        final String name = event.getPlayer().getName();
        this.saveData(name);
        this.removeCooldown(name);
    }

    @EventHandler(ignoreCancelled = true)
    public void onKick(final PlayerKickEvent event) {
        final String name = event.getPlayer().getName();
        this.saveData(name);
        this.removeCooldown(name);
    }

    private void loadData(final String name) {
        if (DatabaseQueries.disabledChatContainsPlayer(name)) {
            DisabledChatMap.add(name);
        }
        if (DatabaseQueries.disabledPrivateMessagesContainsPlayer(name)) {
            this.disabledPrivateMessagesMap.add(name);
        }
        if (DatabaseQueries.ignoreChatContains(name)) {
            IgnoreMap.addToIgnoredChat(name, DatabaseQueries.getIgnoredChat(name));
        }
        if (DatabaseQueries.ignorePrivateContainsPlayer(name)) {
            IgnoreMap.addToIgnoredPrivate(name, DatabaseQueries.getIgnoredPrivate(name));
        }
        if (DatabaseQueries.localSpyContainsPlayer(name)) {
            LocalSpyMap.add(name);
        }
        if (DatabaseQueries.privateMessagesSoundsContainsPlayer(name)) {
            this.pmSoundsMap.setSound(name, DatabaseQueries.getSound(name));
        }
        if (DatabaseQueries.socialSpyContainsPlayer(name)) {
            SocialSpyMap.add(name);
        }
        if (DatabaseQueries.containsPlayerFromAutoMessages(name)) {
            AutoMessagesMap.add(name);
        }
    }

    private void saveData(final String name) {
        if (DisabledChatMap.contains(name)) {
            DatabaseQueries.addPlayerToDisabledChat(name);
        }
        if (this.disabledPrivateMessagesMap.contains(name)) {
            DatabaseQueries.addPlayerToDisabledPrivateMessages(name);
        }
        if (IgnoreMap.playerIgnoresChatAnyOne(name)) {
            DatabaseQueries.addPlayerToIgnoreChat(name, IgnoreMap.getAllIgnoredChat(name));
        }
        if (IgnoreMap.playerIgnoresPrivateAnyOne(name)) {
            DatabaseQueries.addPlayerToIgnorePrivate(name, IgnoreMap.getAllIgnoredPrivate(name));
        }
        if (LocalSpyMap.contains(name)) {
            DatabaseQueries.addPlayerToLocalSpy(name);
        }
        if (this.pmSoundsMap.contains(name)) {
            DatabaseQueries.addPlayerPmSoundToPmSounds(name, this.pmSoundsMap.getSound(name).toString());
        }
        if (SocialSpyMap.contains(name)) {
            DatabaseQueries.addPlayerToSocialSpy(name);
        }
        if (AutoMessagesMap.contains(name)) {
            DatabaseQueries.addPlayerToAutoMessages(name);
        }
    }

    private void removeCooldown(final String name) {
        final Object2ObjectOpenHashMap<Character, Chat> chats = this.chatValues.getChats();
        for (final Map.Entry<Character, Chat> entry : chats.object2ObjectEntrySet()) {
            entry.getValue().getChatCooldowns().remove(name);
        }
        this.cooldownsMap.removePlayerPrivateCooldown(name);
        this.cooldownsMap.removePlayerIgnoreCooldown(name);
        this.cooldownsMap.removePlayerSpyCooldown(name);
        this.cooldownsMap.removeBroadcastCooldown(name);
        DisabledChatMap.remove(name);
        this.disabledPrivateMessagesMap.remove(name);
        IgnoreMap.removeFromIgnoredChat(name);
        IgnoreMap.removeFromIgnoredPrivate(name);
        LocalSpyMap.remove(name);
        this.pmSoundsMap.remove(name);
        ReplyMap.removeFromAll(name);
        SocialSpyMap.remove(name);
        AutoMessagesMap.remove(name);
    }
}
