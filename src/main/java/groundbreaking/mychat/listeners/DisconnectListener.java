package groundbreaking.mychat.listeners;

import groundbreaking.mychat.MyChat;
import groundbreaking.mychat.commands.SocialSpyCommandExecutor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class DisconnectListener implements Listener {

    private final ChatListener chatListener;

    public DisconnectListener(MyChat plugin) {
        chatListener = new ChatListener(plugin);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        removeCooldown(e);
    }

    @EventHandler(ignoreCancelled = true)
    public void onKick(PlayerKickEvent e) {
        removeCooldown(e);
    }

    private void removeCooldown(PlayerEvent e) {
        String name = e.getPlayer().getName();
        chatListener.removePlayerLocalCooldown(name);
        chatListener.removePlayerGlobalCooldown(name);
        SocialSpyCommandExecutor.removeFromListening(name);
    }
}
