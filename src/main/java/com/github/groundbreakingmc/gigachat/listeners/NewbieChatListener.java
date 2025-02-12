package com.github.groundbreakingmc.gigachat.listeners;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.utils.Utils;
import com.github.groundbreakingmc.gigachat.utils.configvalues.NewbieChatValues;
import com.github.groundbreakingmc.mylib.utils.player.PlayerUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public final class NewbieChatListener implements Listener {

    private final GigaChat plugin;
    private final NewbieChatValues newbieValues;

    public NewbieChatListener(final GigaChat plugin) {
        this.plugin = plugin;
        this.newbieValues = plugin.getNewbieChatValues();
    }

    @EventHandler
    public void onMessageSend(final AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        if (player.hasPermission("gigachat.bypass.chatnewbie")) {
            return;
        }

        final long time = this.newbieValues.getCounter().count(player);

        if (this.newbieValues.isGiveBypassPermissionEnabled()
                && (time >= this.newbieValues.getRequiredTimeToGetBypassPerm())) {
            final String bypassPermission = "gigachat.bypass.chatnewbie";
            plugin.getPerms().playerAdd(player, bypassPermission);
            return;
        }

        if (time >= this.newbieValues.getRequiredTime()) {
            return;
        }

        this.sendMessage(player, time);

        if (this.newbieValues.getDenySound() != null) {
            PlayerUtils.playSound(player, this.newbieValues.getDenySound());
        }

        event.setCancelled(true);
    }

    private void sendMessage(final Player player, final long time) {
        final String restTime = Utils.getTime((int) (this.newbieValues.getRequiredTime() - time));
        final String denyMessage = this.newbieValues.getDenyMessage().replace("{time}", restTime);
        player.sendMessage(denyMessage);
    }
}
