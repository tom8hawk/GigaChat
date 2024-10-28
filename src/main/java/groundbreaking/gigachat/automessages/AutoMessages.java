package groundbreaking.gigachat.automessages;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.utils.config.values.AutoMessagesValues;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

public final class AutoMessages {

    private final Random random = new Random();
    private int lastIndex = 0;

    private final GigaChat plugin;
    private final AutoMessagesValues autoMessagesValues;

    private final Set<Integer> sentMessages = new ObjectOpenHashSet<>();

    public AutoMessages(final GigaChat plugin) {
        this.plugin = plugin;
        this.autoMessagesValues = plugin.getAutoMessagesValues();
    }

    public void run() {
        (new BukkitRunnable() {
            public void run() {
                process();
            }
        }).runTaskTimerAsynchronously(this.plugin, 0L, this.autoMessagesValues.getSendInterval() * 20L);
    }

    private void process() {
        final List<String> autoMessages = this.getAutoMessage();
        if (!this.sendWithSound(autoMessages)) {
            this.sendSimple(autoMessages);
        }
    }

    private boolean sendWithSound(final List<String> autoMessages) {
        final String soundString = this.autoMessagesValues.getAutoMessagesSounds().get(this.lastIndex);
        if (soundString == null || soundString.equalsIgnoreCase("disabled")) {
            return false;
        }

        final String[] params = soundString.split(";");
        final Sound sound = params.length == 1 && params[0] != null ? Sound.valueOf(params[0].toUpperCase(Locale.ENGLISH)) : Sound.BLOCK_BREWING_STAND_BREW;
        final float soundVolume = params.length == 2 && params[1] != null ? Float.parseFloat(params[1]) : 1.0f;
        final float soundPitch = params.length == 3 && params[2] != null ? Float.parseFloat(params[2]) : 1.0f;

        for (final Player player : Bukkit.getOnlinePlayers()) {
            // todo Настроить отключение через команду
            if (player.hasPermission("gigachat.automessages")) {
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

    private List<String> getAutoMessage() {
        final Int2ObjectMap<List<String>> autoMessages = this.autoMessagesValues.getAutoMessages();
        if (this.sentMessages.isEmpty()) {
            final IntSet autoMessagesKeys = autoMessages.keySet();
            this.sentMessages.addAll(autoMessagesKeys);
        }

        final int autoMessagesSize = autoMessages.size();
        if (this.autoMessagesValues.isRandom()) {
            while (true) {
                final int randomNumb = this.random.nextInt(autoMessagesSize - 1);
                if (!this.sentMessages.contains(randomNumb)) {
                    this.sentMessages.add(randomNumb);
                    this.lastIndex = randomNumb;
                    return autoMessages.get(randomNumb);
                }
            }
        } else {
            if (this.lastIndex >= autoMessagesSize) {
                this.lastIndex = 0;
            }
            final List<String> message = autoMessages.get(this.lastIndex);
            lastIndex++;
            return message;
        }
    }
}