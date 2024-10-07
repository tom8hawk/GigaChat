package groundbreaking.gigachat.utils.config.values;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.utils.chatsColorizer.AbstractColorizer;
import groundbreaking.gigachat.utils.chatsColorizer.BroadcastColorizer;
import groundbreaking.gigachat.utils.colorizer.IColorizer;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

public class BroadcastValues {

    private final GigaChat plugin;

    @Getter
    private String format;

    @Getter
    private int cooldown;

    @Getter
    private boolean isHoverEnabled;

    @Getter
    private String hoverAction, hoverValue, hoverText;

    @Getter
    private IColorizer colorizer;

    @Getter
    private final AbstractColorizer messageColorizer;

    public BroadcastValues(final GigaChat plugin) {
        this.plugin = plugin;
        messageColorizer = new BroadcastColorizer(plugin);
    }

    public void setValues() {
        final ConfigurationSection broadcast = plugin.getConfig().getConfigurationSection("broadcast");
        if (broadcast != null) {
            colorizer = plugin.getColorizer(plugin.getConfig(), "broadcast.use-minimessages");
            format = colorizer.colorize(broadcast.getString("format"));
            cooldown = broadcast.getInt("cooldown");

            setupHover(broadcast);
        }
        else {
            plugin.getLogger().warning("Failed to load section \"broadcast\" from file \"config.yml\". Please check your configuration file, or delete it and restart your server!");
            plugin.getLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupHover(final ConfigurationSection broadcast) {
        final ConfigurationSection hover = broadcast.getConfigurationSection("hover");
        if (hover != null) {
            isHoverEnabled = hover.getBoolean("enable");
            hoverAction = hover.getString("click-action");
            hoverValue = hover.getString("click-value");
            hoverText = hover.getString("text");
        }
        else {
            plugin.getLogger().warning("Failed to load section \"broadcast.hover\" from file \"config.yml\". Please check your configuration file, or delete it and restart your server!");
            plugin.getLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }
}
