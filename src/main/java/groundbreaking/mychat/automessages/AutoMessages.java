package groundbreaking.mychat.automessages;

import groundbreaking.mychat.MyChat;
import groundbreaking.mychat.utils.ConfigValues;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class AutoMessages {

    private final MyChat plugin;
    private final ConfigValues configValues;

    public AutoMessages(MyChat plugin) {
        this.plugin = plugin;
        this.configValues = plugin.getPluginConfig();
    }

    public void startMSG(FileConfiguration config) {
        new BukkitRunnable() {
            public void run() {
                if (!configValues.isAutoMessageEnable()) {
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
        }.runTaskTimerAsynchronously(plugin, 20L, config.getInt("autoMessage.messageInterval") * 20L);
    }

    private int i = 0;

    private List<String> getAutoMessage() {
        if (configValues.isAutoMessagesRandom()) {
            return configValues.getAutoMessages().get(getRandomKey(configValues.getAutoMessages().keySet()));
        }
        if (i++ >= configValues.getAutoMessages().keySet().size()) {
            i = 0;
        }

        return configValues.getAutoMessages().get(i);
    }

    private int getRandomKey(IntSet intSet) {
        return ThreadLocalRandom.current().nextInt(intSet.size());
    }
}