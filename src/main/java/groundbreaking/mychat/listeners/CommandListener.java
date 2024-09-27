package groundbreaking.mychat.listeners;

import groundbreaking.mychat.MyChat;
import groundbreaking.mychat.utils.ConfigValues;
import groundbreaking.mychat.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandListener implements Listener {

    private final ConfigValues pluginConfig;

    public CommandListener(MyChat plugin) {
        pluginConfig = plugin.getConfigValues();
    }

    @EventHandler(ignoreCancelled = true)
    public void playerCommand(PlayerCommandPreprocessEvent e) {
        if (!pluginConfig.isNewbieCommandsEnable()) {
            return;
        }

        final Player player = e.getPlayer();
        if (player.hasPermission("mychat.bypass.newbie.commands")) {
            return;
        }

        final String[] params = e.getMessage().split(" ");
        final String command = params[0];

        final long time = (System.currentTimeMillis() - player.getFirstPlayed()) / 1000;
        if (time <= pluginConfig.getNewbieCommandsCooldown()) {
            for (String cmd : pluginConfig.getNewbieBlockedCommands()) {
                if (command.startsWith(cmd + " ") || command.equalsIgnoreCase(cmd)) {
                    final String cd = Utils.getTime((int) (pluginConfig.getNewbieCommandsCooldown() - time));
                    player.sendMessage(pluginConfig.getNewbieCommandsMessage().replace("{time}", cd));
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }
}
