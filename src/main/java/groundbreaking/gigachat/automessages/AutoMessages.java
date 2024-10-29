package groundbreaking.gigachat.automessages;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.collections.AutoMessagesMap;
import groundbreaking.gigachat.constructors.AutoMessageConstructor;
import groundbreaking.gigachat.utils.config.values.AutoMessagesValues;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Locale;
import java.util.Random;

public final class AutoMessages {

    private final Random random = new Random();
    private int lastIndex = 0;

    private final GigaChat plugin;
    private final AutoMessagesValues autoMessagesValues;

    private final List<AutoMessageConstructor> autoMessagesClone = new ObjectArrayList<>();

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
        final List<String> autoMessage = this.getAutoMessage().autoMessage();
        final String sound = this.getAutoMessage().sound();
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
            if (AutoMessagesMap.contains(player.getName())) {
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
        }

        final AutoMessageConstructor autoMessage;
        final int autoMessagesSize = this.autoMessagesClone.size() - 1;
        if (this.autoMessagesValues.isRandom()) {
            final int randomNumb = this.random.nextInt(autoMessagesSize);
            this.lastIndex = randomNumb;
            autoMessage = this.autoMessagesClone.get(randomNumb);
            this.autoMessagesClone.remove(randomNumb);
        } else {
            if (this.lastIndex >= autoMessagesSize) {
                this.lastIndex = 0;
            }
            autoMessage = this.autoMessagesClone.get(this.lastIndex);
            this.autoMessagesClone.remove(this.lastIndex);
            this.lastIndex++;
        }

        return autoMessage;
    }
}