package groundbreaking.gigachat.collections;

import groundbreaking.gigachat.GigaChat;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.Sound;

import java.util.Map;

public final class PmSounds {

    private final Map<String, String> sounds = new Object2ObjectOpenHashMap<>();
    private String defaultSound; public void setDefaultSound(final String defaultSound) {
        this.plugin.getMyLogger().info("");
        this.plugin.getMyLogger().info("Gave sound: " + defaultSound);
        this.plugin.getMyLogger().info("");
        this.defaultSound = defaultSound;
    }

    private final GigaChat plugin;

    public PmSounds(final GigaChat plugin) {
        this.plugin = plugin;
    }

    public Sound getSound(final String name) {
        if (sounds.isEmpty()) {
            return Sound.valueOf(defaultSound);
        }

        return Sound.valueOf(sounds.getOrDefault(name, defaultSound));
    }

    public void setSound(final String name, final String soundName) {
        sounds.put(name, soundName);
    }

    public void remove(final String name) {
        sounds.remove(name);
    }

    public boolean contains(final String name) {
        return sounds.containsKey(name);
    }
}
