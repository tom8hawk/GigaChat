package com.github.groundbreakingmc.gigachat.automessages;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.collections.AutoMessagesCollection;
import com.github.groundbreakingmc.gigachat.constructors.AutoMessageConstructor;
import com.github.groundbreakingmc.gigachat.utils.config.values.AutoMessagesValues;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public final class AutoMessages {

    private final GigaChat plugin;
    private final AutoMessagesValues autoMessagesValues;

    private final List<AutoMessageConstructor> autoMessagesClone = new ObjectArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private ScheduledFuture<?> task;
    private final ReentrantLock lock = new ReentrantLock();

    public AutoMessages(final GigaChat plugin) {
        this.plugin = plugin;
        this.autoMessagesValues = plugin.getAutoMessagesValues();
    }

    public void run() {
        if (!autoMessagesValues.isEnabled()) {
            return;
        }

        lock.lock();
        try {
            if (task != null && !task.isCancelled()) {
                return;
            }

            int interval = this.autoMessagesValues.getSendInterval();
            task = scheduler.scheduleWithFixedDelay(this::process, 0, interval, TimeUnit.SECONDS);
        } finally {
            lock.unlock();
        }
    }

    public void restart() {
        scheduler.execute(() -> {
            lock.lock();
            try {
                cancel();
                run();
            } finally {
                lock.unlock();
            }
        });
    }

    public void cancel() {
        lock.lock();
        try {
            if (task != null) {
                task.cancel(false);
                autoMessagesClone.clear();

                task = null;
            }
        } finally {
            lock.unlock();
        }
    }

    private void process() {
        final AutoMessageConstructor autoMessageConstructor = this.getAutoMessage();
        final List<String> autoMessage = autoMessageConstructor.autoMessage();
        final String sound = autoMessageConstructor.sound();
        if (!this.sendWithSound(autoMessage, sound)) {
            this.sendSimple(autoMessage);
        }
    }

    private boolean sendWithSound(final List<String> autoMessages, final String soundString) {
        if (soundString == null || soundString.equalsIgnoreCase("disabled")) {
            return false;
        }

        final String[] params = soundString.split(";");
        final Sound sound = params.length >= 1 ? Sound.valueOf(params[0].toUpperCase(Locale.ENGLISH)) : Sound.BLOCK_BREWING_STAND_BREW;
        final float soundVolume = params.length >= 2 ? Float.parseFloat(params[1]) : 1.0f;
        final float soundPitch = params.length >= 3 ? Float.parseFloat(params[2]) : 1.0f;

        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (AutoMessagesCollection.contains(player.getUniqueId())) {
                continue;
            }

            for (int i = 0; i < autoMessages.size(); i++) {
                player.sendMessage(autoMessages.get(i));
                player.playSound(player.getLocation(), sound, soundVolume, soundPitch);
            }
        }

        return true;
    }

    private void sendSimple(final List<String> autoMessages) {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            for (int i = 0; i < autoMessages.size(); i++) {
                player.sendMessage(autoMessages.get(i));
            }
        }
    }

    private AutoMessageConstructor getAutoMessage() {
        if (this.autoMessagesClone.isEmpty()) {
            final List<AutoMessageConstructor> autoMessages = this.autoMessagesValues.getAutoMessages();
            this.autoMessagesClone.addAll(autoMessages);
            if (this.autoMessagesValues.isRandom()) {
                Collections.shuffle(autoMessagesClone);
            }
        }
        return autoMessagesClone.remove(0);
    }
}