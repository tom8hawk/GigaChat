package com.github.groundbreakingmc.gigachat.automessages;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.utils.configvalues.AutoMessagesValues;
import com.github.groundbreakingmc.mylib.collections.cases.Pair;
import com.github.groundbreakingmc.mylib.utils.player.PlayerUtils;
import com.github.groundbreakingmc.mylib.utils.player.settings.SoundSettings;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collections;
import java.util.List;

public final class AutoMessages {

    private final GigaChat plugin;
    private final AutoMessagesValues autoMessagesValues;

    private final List<Pair<String, SoundSettings>> autoMessagesClone = new ObjectArrayList<>();

    private BukkitTask task;

    public AutoMessages(final GigaChat plugin) {
        this.plugin = plugin;
        this.autoMessagesValues = plugin.getAutoMessagesValues();
    }

    public void run() {
        this.task = (new BukkitRunnable() {
            public void run() {
                final Pair<String, SoundSettings> autoMessageConstructor = AutoMessages.this.getAutoMessage();
                if (autoMessageConstructor == null) {
                    return;
                }

                final String autoMessage = autoMessageConstructor.getLeft();
                final SoundSettings sound = autoMessageConstructor.getRight();
                AutoMessages.this.send(autoMessage, sound);
            }
        }).runTaskTimerAsynchronously(this.plugin, 0L, this.autoMessagesValues.getSendInterval() * 20L);
    }

    public void cancel() {
        this.task.cancel();
    }

    private void send(final String autoMessage, final SoundSettings soundSettings) {
        for (final Player target : Bukkit.getOnlinePlayers()) {
            target.sendMessage(autoMessage);
            if (soundSettings != null) {
                PlayerUtils.playSound(target, soundSettings);
            }
        }
    }

    private Pair<String, SoundSettings> getAutoMessage() {
        if (this.autoMessagesClone.isEmpty()) {
            final List<Pair<String, SoundSettings>> autoMessages = this.autoMessagesValues.getAutoMessages();
            if (autoMessages == null || autoMessages.isEmpty()) {
                this.plugin.getCustomLogger().warn("Failed to load \"auto-messages\". Please check your configuration file, or delete it and restart your server!");
                this.plugin.getCustomLogger().warn("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
                cancel();
                return null;
            }

            this.autoMessagesClone.addAll(autoMessages);
            if (this.autoMessagesValues.isRandom()) {
                Collections.shuffle(this.autoMessagesClone);
            }
        }

        return this.autoMessagesClone.remove(0);
    }

    public void clearClonedAutoMessages() {
        this.autoMessagesClone.clear();
    }
}