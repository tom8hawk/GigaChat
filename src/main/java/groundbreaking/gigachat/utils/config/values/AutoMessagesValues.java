package groundbreaking.gigachat.utils.config.values;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.utils.colorizer.basic.IColorizer;
import groundbreaking.gigachat.utils.config.ConfigLoader;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;

@Getter
public final class AutoMessagesValues {

    @Getter(AccessLevel.NONE)
    private final GigaChat plugin;

    private boolean isEnabled;
    private boolean isRandom;

    private int sendInterval;

    private String defaultSound;

    private final Map<String, String>
            autoMessagesSounds = new Object2ObjectOpenHashMap<>();

    private final Map<String, List<String>>
            autoMessages = new Object2ObjectOpenHashMap<>();

    public AutoMessagesValues(final GigaChat plugin) {
        this.plugin = plugin;
    }

    public void setValues() {
        final FileConfiguration config = new ConfigLoader(this.plugin).loadAndGet("auto-messages", 1.0);
        final IColorizer colorizer = this.plugin.getColorizer(config, "settings.serializer");

        this.setupSettings(config);
        this.setupAutoMessages(config, colorizer);
    }

    private void setupSettings(final FileConfiguration config) {
        final ConfigurationSection settings = config.getConfigurationSection("settings");
        if (settings != null) {
            this.isEnabled = settings.getBoolean("enable");
            this.isRandom = settings.getBoolean("random");
            this.sendInterval = settings.getInt("send-interval");
            this.defaultSound = settings.getString("default-sound");
        }
        else {
            this.plugin.getMyLogger().warning("Failed to load section \"settings\" from file \"auto-messages.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupAutoMessages(final FileConfiguration config, final IColorizer colorizer) {
        final ConfigurationSection autoMessagesSection = config.getConfigurationSection("auto-messages");
        if (autoMessagesSection != null) {
            this.autoMessagesSounds.clear();
            this.autoMessages.clear();

            final List<String> autoMessagesKeys = new ObjectArrayList<>(autoMessagesSection.getKeys(false));
            for (int i = 0; i < autoMessagesKeys.size(); i++) {
                final String key = autoMessagesKeys.get(i);
                this.autoMessagesSounds.put(key, autoMessagesSection.getString(key + ".sound", defaultSound));

                final List<String> messages = new ObjectArrayList<>(autoMessagesSection.getStringList(key + ".messages"));
                for (int r = 0; r < messages.size(); r++) {
                    messages.set(r, colorizer.colorize(messages.get(r)));
                }

                this.autoMessages.put(key, messages);
            }
        }
        else {
            this.plugin.getMyLogger().warning("Failed to load section \"auto-messages\" from file \"auto-messages.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }
}
