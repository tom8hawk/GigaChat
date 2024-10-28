package groundbreaking.gigachat.collections;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.utils.map.ExpiringMap;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

@Getter
public final class Cooldowns {

    @Getter(AccessLevel.NONE)
    private final GigaChat plugin;

    private ExpiringMap<String, Long> privateCooldowns;

    private ExpiringMap<String, Long> ignoreCooldowns;

    private ExpiringMap<String, Long> spyCooldowns;

    private ExpiringMap<String, Long> broadcastCooldowns;

    public Cooldowns(final GigaChat plugin) {
        this.plugin = plugin;
    }
    
    public void setCooldowns() {
        this.privateCooldowns = new ExpiringMap<>(this.plugin.getPmValues().getPmCooldown(), TimeUnit.MILLISECONDS);
        this.ignoreCooldowns = new ExpiringMap<>(this.plugin.getPmValues().getIgnoreCooldown(), TimeUnit.MILLISECONDS);
        this.spyCooldowns = new ExpiringMap<>(this.plugin.getPmValues().getSpyCooldown(), TimeUnit.MILLISECONDS);
        this.broadcastCooldowns = new ExpiringMap<>(this.plugin.getBroadcastValues().getCooldown(), TimeUnit.MILLISECONDS);
    }

    public void removePlayerPrivateCooldown(final String playerName) {
        this.privateCooldowns.remove(playerName);
    }

    public void removePlayerIgnoreCooldown(final String playerName) {
        this.ignoreCooldowns.remove(playerName);
    }

    public void removePlayerSpyCooldown(final String playerName) {
        this.spyCooldowns.remove(playerName);
    }

    public void removeBroadcastCooldown(final String playerName) {
        this.broadcastCooldowns.remove(playerName);
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
