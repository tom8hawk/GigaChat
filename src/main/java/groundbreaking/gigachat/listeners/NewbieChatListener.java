package groundbreaking.gigachat.listeners;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.utils.Utils;
import groundbreaking.gigachat.utils.config.values.NewbieChatValues;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public final class NewbieChatListener implements Listener {

    private final GigaChat plugin;
    private final NewbieChatValues newbieValues;

    private boolean isRegistered = false;

    public NewbieChatListener(final GigaChat plugin) {
        this.plugin = plugin;
        this.newbieValues = plugin.getNewbieChatValues();
    }

    @EventHandler
    public void onMessageSend(final AsyncPlayerChatEvent event) {
        processEvent(event);
    }

    public void registerEvent() {
        if (this.isRegistered || !this.newbieValues.isEnabled()) {
            this.unregisterEvent();
        }

        final Class<? extends Event> eventClass = AsyncPlayerChatEvent.class;
        final EventPriority eventPriority = this.plugin.getEventPriority(this.newbieValues.getPriority(), "newbie-chat.yml");

        this.plugin.getServer().getPluginManager().registerEvent(eventClass, this, eventPriority,
                (listener, event) -> this.onMessageSend((AsyncPlayerChatEvent) event),
                this.plugin
        );

        this.isRegistered = true;
    }

    public void unregisterEvent() {
        if (!this.isRegistered) {
            return;
        }

        HandlerList.unregisterAll(this);
        this.isRegistered = false;
    }

    private void processEvent(final AsyncPlayerChatEvent event) {
        if (!this.newbieValues.isEnabled()) {
            return;
        }

        final Player player = event.getPlayer();

        if (player.hasPermission("gigachat.bypass.chatnewbie")) {
            return;
        }

        final long time = this.newbieValues.getCounter().count(player);

        if (this.newbieValues.isGiveBypassPermissionEnabled()) {
            if (time <= this.newbieValues.getRequiredTimeToGetBypassPerm()) {
                final String bypassPermission = "gigachat.bypass.chatnewbie";
                plugin.getPerms().playerAdd(player, bypassPermission);
            }
        }

        if (time > this.newbieValues.getRequiredTime()) {
            return;
        }

        this.sendMessage(player, time);

        if (this.newbieValues.isDenySoundEnabled()) {
            this.playSound(player);
        }

        event.setCancelled(true);
    }

    private void sendMessage(final Player player, final long time) {
        final String restTime = Utils.getTime((int) (this.newbieValues.getRequiredTime() - time));
        final String denyMessage = this.newbieValues.getDenyMessage().replace("{time}", restTime);
        player.sendMessage(denyMessage);
    }

    private void playSound(final Player player) {
        final Location playerLocation = player.getLocation();
        final Sound denySound = this.newbieValues.getDenySound();
        final float denySoundVolume = this.newbieValues.getDenySoundVolume();
        final float denySoundPitch = this.newbieValues.getDenySoundPitch();
        player.playSound(playerLocation, denySound, denySoundVolume, denySoundPitch);
    }
}
