package groundbreaking.gigachat.collections;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.utils.map.ExpiringMap;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Getter
public final class CooldownsCollection {

    @Getter(AccessLevel.NONE)
    private final GigaChat plugin;

    private ExpiringMap<UUID, Long> privateCooldowns;

    private ExpiringMap<UUID, Long> ignoreCooldowns;

    private ExpiringMap<UUID, Long> spyCooldowns;

    private ExpiringMap<UUID, Long> broadcastCooldowns;

    public CooldownsCollection(final GigaChat plugin) {
        this.plugin = plugin;
    }
    
    public void setCooldowns() {
        this.privateCooldowns = new ExpiringMap<>(this.plugin.getPmValues().getPmCooldown(), TimeUnit.MILLISECONDS);
        this.ignoreCooldowns = new ExpiringMap<>(this.plugin.getPmValues().getIgnoreCooldown(), TimeUnit.MILLISECONDS);
        this.spyCooldowns = new ExpiringMap<>(this.plugin.getPmValues().getSpyCooldown(), TimeUnit.MILLISECONDS);
        this.broadcastCooldowns = new ExpiringMap<>(this.plugin.getBroadcastValues().getCooldown(), TimeUnit.MILLISECONDS);
    }

    public void removePlayerPrivateCooldown(final UUID uuid) {
        this.privateCooldowns.remove(uuid);
    }

    public void removePlayerIgnoreCooldown(final UUID uuid) {
        this.ignoreCooldowns.remove(uuid);
    }

    public void removePlayerSpyCooldown(final UUID uuid) {
        this.spyCooldowns.remove(uuid);
    }

    public void removeBroadcastCooldown(final UUID uuid) {
        this.broadcastCooldowns.remove(uuid);
    }

    public boolean hasCooldown(final Player target, final UUID targetUUID, final String permission, final ExpiringMap<UUID, Long> playerCooldown) {
        if (target.hasPermission(permission)) {
            return false;
        }

        return playerCooldown.containsKey(targetUUID);
    }

    public void addCooldown(final UUID uuid, final ExpiringMap<UUID, Long> playerCooldown) {
        playerCooldown.put(uuid, System.currentTimeMillis());
    }
}
