package groundbreaking.mychat.listeners;

import groundbreaking.mychat.MyChat;
import groundbreaking.mychat.utils.ConfigValues;
import groundbreaking.mychat.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandListener implements Listener {

    private final ConfigValues configValues;

    public CommandListener(MyChat plugin) {
        configValues = plugin.getConfigValues();
    }

    @EventHandler(ignoreCancelled = true)
    public void playerCommand(PlayerCommandPreprocessEvent e) {
        if (!configValues.isNewbieCommandsEnable()) {
            return;
        }

        final Player player = e.getPlayer();
        if (player.hasPermission("mychat.bypass.commandsnewbie")) {
            return;
        }

        final String[] params = e.getMessage().split(" ");
        final String command = params[0];

        final long time = (System.currentTimeMillis() - player.getFirstPlayed()) / 1000;
        if (time <= configValues.getNewbieCommandsCooldown()) {
            for (String cmd : configValues.getNewbieBlockedCommands()) {
                if (command.startsWith(cmd + " ") || command.equalsIgnoreCase(cmd)) {
                    final String cd = Utils.getTime((int) (configValues.getNewbieCommandsCooldown() - time));
                    player.sendMessage(configValues.getNewbieCommandsMessage().replace("{time}", cd));

                    if (configValues.isCommandDenySoundEnabled()) {
                        player.playSound(player, configValues.getCommandDenySound(), configValues.getCommandDenySoundVolume(), configValues.getCommandDenySoundPitch());
                    }

                    e.setCancelled(true);
                    return;
                }
            }
        }
    }
}
