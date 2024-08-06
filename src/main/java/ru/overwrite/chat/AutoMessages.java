package ru.overwrite.chat;

import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import ru.overwrite.api.commons.StringUtils;
import ru.overwrite.chat.utils.Config;

public class AutoMessages {

    private final PromisedChat instance;
    private final Config pluginConfig;

    private Iterator<Map.Entry<String, List<String>>> autoMessageIterator;

    public AutoMessages(PromisedChat plugin) {
        instance = plugin;
        pluginConfig = plugin.getPluginConfig();
        autoMessageIterator = pluginConfig.autoMessages != null ? pluginConfig.autoMessages.entrySet().iterator() : null;
    }

    public void startMSG(FileConfiguration config) {
        (new BukkitRunnable() {
            public void run() {
                if (!pluginConfig.autoMessage) {
                    return;
                }
                List<String> amsg;
                if (pluginConfig.isRandom) {
                    amsg = pluginConfig.autoMessages.get(getRandomKey(pluginConfig.autoMessages.keySet()));
                } else {
                    if (!autoMessageIterator.hasNext()) {
                        autoMessageIterator = pluginConfig.autoMessages.entrySet().iterator();
                    }
                    Map.Entry<String, List<String>> entry = autoMessageIterator.next();
                    amsg = entry.getValue();
                }
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!p.hasPermission("pchat.automessage")) {
                        continue;
                    }
                    for (String message : amsg) {
                        p.sendMessage(StringUtils.colorize(message));
                    }
                }
            }
        }).runTaskTimerAsynchronously(instance, 20L, config.getInt("autoMessage.messageInterval") * 20L);
    }

    private <K, V> K getRandomKey(Set<K> keySet) {
        if (keySet.isEmpty()) {
            return null;
        }

        int randomIndex = ThreadLocalRandom.current().nextInt(keySet.size());
        Iterator<K> iterator = keySet.iterator();

        for (int i = 0; i < randomIndex; i++) {
            iterator.next();
        }

        return iterator.next();
    }
}