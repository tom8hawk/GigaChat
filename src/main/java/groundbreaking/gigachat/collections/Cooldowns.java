package groundbreaking.gigachat.collections;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.utils.map.ExpiringMap;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public final class Cooldowns {

    private final GigaChat plugin;

    @Getter
    private ExpiringMap<String, Long> localCooldowns;
    @Getter
    private ExpiringMap<String, Long> globalCooldowns;
    @Getter
    private ExpiringMap<String, Long> privateCooldowns;
    @Getter
    private ExpiringMap<String, Long> ignoreCooldowns;
    @Getter
    private ExpiringMap<String, Long> spyCooldowns;
    @Getter
    private ExpiringMap<String, Long> broadcastCooldowns;

    public Cooldowns(final GigaChat plugin) {
        this.plugin = plugin;
    }
    
    public void setCooldowns() {
        localCooldowns = new ExpiringMap<>(plugin.getChatValues().getLocalCooldown(), TimeUnit.MILLISECONDS);
        globalCooldowns = new ExpiringMap<>(plugin.getChatValues().getGlobalCooldown(), TimeUnit.MILLISECONDS);
        privateCooldowns = new ExpiringMap<>(plugin.getPmValues().getPmCooldown(), TimeUnit.MILLISECONDS);
        ignoreCooldowns = new ExpiringMap<>(plugin.getPmValues().getIgnoreCooldown(), TimeUnit.MILLISECONDS);
        spyCooldowns = new ExpiringMap<>(plugin.getPmValues().getSpyCooldown(), TimeUnit.MILLISECONDS);
        broadcastCooldowns = new ExpiringMap<>(plugin.getBroadcastValues().getCooldown(), TimeUnit.MILLISECONDS);
    }

    public void removePlayerLocalCooldown(final String playerName) {
       localCooldowns.remove(playerName);
    }

    public void removePlayerGlobalCooldown(final String playerName) {
       globalCooldowns.remove(playerName);
    }

    public void removePlayerPrivateCooldown(final String playerName) {
       privateCooldowns.remove(playerName);
    }

    public void removePlayerIgnoreCooldown(final String playerName) {
       ignoreCooldowns.remove(playerName);
    }

    public void removePlayerSpyCooldown(final String playerName) {
       spyCooldowns.remove(playerName);
    }

    public void removeBroadcastCooldown(final String playerName) {
        broadcastCooldowns.remove(playerName);
    }

    public boolean hasCooldown(final Player player, final String name, final String permission, final ExpiringMap<String, Long> playerCooldown) {
        if (player.hasPermission(permission)) {
            return false;
        }

        return playerCooldown.containsKey(name);
    }

    public void addCooldown(final String name, final ExpiringMap<String, Long> playerCooldown) {
        playerCooldown.put(name, System.currentTimeMillis());
    }
}
