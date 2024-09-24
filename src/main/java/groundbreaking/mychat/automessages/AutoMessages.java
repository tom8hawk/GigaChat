package groundbreaking.mychat.automessages;

import groundbreaking.mychat.MyChat;
import groundbreaking.mychat.utils.Config;
import groundbreaking.mychat.utils.colorizer.IColorizer;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class AutoMessages {

    private final MyChat plugin;
    private final Config pluginConfig;
    private final IColorizer colorizer;

    public AutoMessages(MyChat plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
        this.colorizer = plugin.getColorizer();
    }

    public void startMSG(FileConfiguration config) {
        (new BukkitRunnable() {
            public void run() {
                if (!pluginConfig.isAutoMessageEnable()) {
                    return;
                }

                List<String> amsg = getAutoMessage();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.hasPermission("pchat.automessages")) {
                        continue;
                    }
                    for (String message : amsg) {
                        p.sendMessage(message);
                    }
                }
            }
        }).runTaskTimerAsynchronously(plugin, 20L, config.getInt("autoMessage.messageInterval") * 20L);
    }

    private int i = 0;

    private List<String> getAutoMessage() {
        if (pluginConfig.isAutoMessagesRandom()) {
            return pluginConfig.getAutoMessages().get(getRandomKey(pluginConfig.getAutoMessages().keySet()));
        }
        if (i++ >= pluginConfig.getAutoMessages().keySet().size()) {
            i = 0;
        }

        return pluginConfig.getAutoMessages().get(i);
    }

    private int getRandomKey(IntSet intSet) {
        return ThreadLocalRandom.current().nextInt(intSet.size());
    }
}