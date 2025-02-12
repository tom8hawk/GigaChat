package com.github.groundbreakingmc.gigachat.listeners;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.utils.Utils;
import com.github.groundbreakingmc.gigachat.utils.configvalues.NewbieCommandsValues;
import com.github.groundbreakingmc.mylib.utils.player.PlayerUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public final class NewbieCommandListener implements Listener {

    private final GigaChat plugin;
    private final NewbieCommandsValues newbieValues;

    public NewbieCommandListener(final GigaChat plugin) {
        this.plugin = plugin;
        this.newbieValues = plugin.getNewbieCommandsValues();
    }

    @EventHandler
    public void onCommandUse(final PlayerCommandPreprocessEvent event) {
        final Player player = event.getPlayer();
        if (player.hasPermission("gigachat.bypass.commandsnewbie")) {
            return;
        }

        final String enteredCommand = event.getMessage();
        long time = this.newbieValues.getCounter().count(player);

        if (this.newbieValues.isGiveBypassPermissionEnabled()
                && (time >= this.newbieValues.getRequiredTimeToGetBypassPerm())) {
            final String bypassPermission = "gigachat.bypass.commandsnewbie";
            this.plugin.getPerms().playerAdd(player, bypassPermission);
            return;
        }

        if (time >= this.newbieValues.getRequiredTime()) {
            return;
        }

        for (final String blockedCommand : this.newbieValues.getBlockedCommands()) {
            if (this.isBlocked(enteredCommand, blockedCommand)) {
                this.sendMessage(player, time);

                if (this.newbieValues.getDenySound() != null) {
                    PlayerUtils.playSound(player, this.newbieValues.getDenySound());
                }

                event.setCancelled(true);
                break;
            }
        }
    }

    private void sendMessage(final Player player, final long time) {
        final String restTime = Utils.getTime((int) (this.newbieValues.getRequiredTime() - time));
        final String denyMessage = this.newbieValues.getDenyMessage().replace("{time}", restTime);
        player.sendMessage(denyMessage);
    }

    private boolean isBlocked(final String command, final String cmd) {
        if (command.equalsIgnoreCase(cmd)) {
            return true;
        }

        final int length = Math.min(command.length(), cmd.length());

        for (int i = 0; i < length; i++) {
            final char currentChar = command.charAt(i);
            if (currentChar == ' ') {
                return true;
            }
            if (currentChar != cmd.charAt(i)) {
                return false;
            }
        }

        return true;
    }
}
