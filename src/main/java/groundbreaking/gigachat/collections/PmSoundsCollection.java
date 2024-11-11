package groundbreaking.gigachat.collections;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.Sound;

import java.util.Map;
import java.util.UUID;

public final class PmSoundsCollection {

    private final Map<UUID, Sound> sounds = new Object2ObjectOpenHashMap<>();
    private Sound defaultSound;

    public void setDefaultSound(final String defaultSound) {
        this.defaultSound = Sound.valueOf(defaultSound);
    }

    public Sound getSound(final UUID uuid) {
        if (sounds.isEmpty()) {
            return defaultSound;
        }

        return sounds.getOrDefault(uuid, defaultSound);
    }

    public void setSound(final UUID uuid, final Sound sound) {
        sounds.put(uuid, sound);
    }

    public void remove(final UUID uuid) {
        sounds.remove(uuid);
    }

    public boolean contains(final UUID uuid) {
        return sounds.containsKey(uuid);
    }
}
