package groundbreaking.gigachat.listeners;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.utils.Utils;
import groundbreaking.gigachat.utils.config.values.NewbieChatValues;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public final class NewbieChatListener implements Listener {

    private final GigaChat plugin;
    private final NewbieChatValues newbieValues;

    public NewbieChatListener(final GigaChat plugin) {
        this.plugin = plugin;
        this.newbieValues = plugin.getNewbieChat();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMessageSendLowest(final AsyncPlayerChatEvent event) {
        if (newbieValues.isListenerPriorityLowest()) {
            processEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onMessageSendLow(final AsyncPlayerChatEvent event) {
        if (newbieValues.isListenerPriorityLow()) {
            processEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onMessageSendNormal(final AsyncPlayerChatEvent event) {
        if (newbieValues.isListenerPriorityNormal()) {
            processEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMessageSendHigh(final AsyncPlayerChatEvent event) {
        if (newbieValues.isListenerPriorityHigh()) {
            processEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMessageSendHighest(final AsyncPlayerChatEvent event) {
        if (newbieValues.isListenerPriorityHighest()) {
            processEvent(event);
        }
    }

    private void processEvent(final AsyncPlayerChatEvent event) {
        if (!newbieValues.isEnabled()) {
            return;
        }

        final Player player = event.getPlayer();
        if (player.hasPermission("gigachat.bypass.chatnewbie")) {
            return;
        }

        final long time = newbieValues.getCounter().count(player);

        if (newbieValues.isGiveBypassPermissions()) {
            if (time <= newbieValues.getBypassRequiredTime()) {
                plugin.getPerms().playerAdd(player, "gigachat.bypass.chatnewbie");
            }
        }

        if (time > newbieValues.getRequiredTime()) {
            return;
        }

        final String restTime = Utils.getTime((int) (newbieValues.getRequiredTime() - time));
        player.sendMessage(newbieValues.getDenyMessage().replace("{time}", restTime));

        if (newbieValues.isDenySoundEnabled()) {
            player.playSound(player.getLocation(), newbieValues.getDenySound(), newbieValues.getDenySoundVolume(), newbieValues.getDenySoundPitch());
        }

        event.setCancelled(true);
    }
}
