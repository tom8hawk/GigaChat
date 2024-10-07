package groundbreaking.gigachat.listeners;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.utils.Utils;
import groundbreaking.gigachat.utils.config.values.NewbieCommandsValues;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public final class CommandListener implements Listener {

    private final GigaChat plugin;
    private final NewbieCommandsValues newbieValues;

    public CommandListener(final GigaChat plugin) {
        this.plugin = plugin;
        this.newbieValues = plugin.getNewbieCommands();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommandUseLowest(final PlayerCommandPreprocessEvent event) {
        if (newbieValues.isListenerPriorityLowest()) {
            processEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCommandUseLow(final PlayerCommandPreprocessEvent event) {
        if (newbieValues.isListenerPriorityLow()) {
            processEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCommandUseNormal(final PlayerCommandPreprocessEvent event) {
        if (newbieValues.isListenerPriorityNormal()) {
            processEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCommandUseHigh(final PlayerCommandPreprocessEvent event) {
        if (newbieValues.isListenerPriorityHigh()) {
            processEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommandUseHighest(final PlayerCommandPreprocessEvent event) {
        if (newbieValues.isListenerPriorityHighest()) {
            processEvent(event);
        }
    }

    private void processEvent(final PlayerCommandPreprocessEvent event) {
        if (!newbieValues.isEnabled()) {
            return;
        }

        final Player player = event.getPlayer();
        if (player.hasPermission("gigachat.bypass.commandsnewbie")) {
            return;
        }

        final String enteredCommand = event.getMessage();

        long time = newbieValues.getCounter().count(player);

        if (newbieValues.isGiveBypassPermissions()) {
            if (time <= newbieValues.getBypassRequiredTime()) {
                plugin.getPerms().playerAdd(player, "gigachat.bypass.commandsnewbie");
            }
        }

        if (time > newbieValues.getRequiredTime()) {
            return;
        }

        for (String blockedCommand : newbieValues.getBlockedCommands()) {
            if (isBlocked(enteredCommand, blockedCommand)) {
                final String restTime = Utils.getTime((int) (newbieValues.getRequiredTime() - time));
                player.sendMessage(newbieValues.getDenyMessage().replace("{time}", restTime));

                if (newbieValues.isDenySoundEnabled()) {
                    player.playSound(player.getLocation(), newbieValues.getDenySound(), newbieValues.getDenySoundVolume(), newbieValues.getDenySoundPitch());
                }

                event.setCancelled(true);
                return;
            }
        }
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
