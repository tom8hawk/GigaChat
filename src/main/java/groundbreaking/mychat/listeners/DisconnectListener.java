package groundbreaking.mychat.listeners;

import groundbreaking.mychat.commands.ReplyCommandExecutor;
import groundbreaking.mychat.commands.SocialSpyCommandExecutor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class DisconnectListener implements Listener {

    private final ChatListener chatListener;

    public DisconnectListener(ChatListener chatListener) {
        this.chatListener = chatListener;
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
        ReplyCommandExecutor.getReply().remove(name);
        for (String key : ReplyCommandExecutor.getReply().keySet()) {
            final String keyName = ReplyCommandExecutor.getReply().get(key);
            if (keyName.equals(name)) {
                ReplyCommandExecutor.getReply().remove(key);
            }
        }
    }
}
