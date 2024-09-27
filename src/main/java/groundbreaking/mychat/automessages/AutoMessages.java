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

    private int i = 0;

    private final MyChat plugin;
    private final ConfigValues configValues;

    public AutoMessages(MyChat plugin) {
        this.plugin = plugin;
        this.configValues = plugin.getConfigValues();
    }

    public void startMSG(FileConfiguration config) {
        new BukkitRunnable() {
            public void run() {
                if (!configValues.isAutoMessageEnable()) {
                    return;
                }

                final List<String> autoMessages = getAutoMessage();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasPermission("mychat.automessages")) {
                        continue;
                    }
                    if (configValues.isAutoMessagesSoundEnabled()) {
                        for (int i = 0; i < autoMessages.size(); i++) {
                            player.sendMessage(autoMessages.get(i));
                            player.playSound(player, configValues.getAutoMessagesSound(), configValues.getAutoMessagesSoundVolume(), configValues.getAutoMessagesSoundPitch());
                        }
                    } else {
                        for (int i = 0; i < autoMessages.size(); i++) {
                            player.sendMessage(autoMessages.get(i));
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 20L, config.getInt("autoMessage.send-interval") * 20L);
    }

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