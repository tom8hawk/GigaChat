package groundbreaking.gigachat.automessages;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.collections.AutoMessagesCollection;
import groundbreaking.gigachat.constructors.AutoMessageConstructor;
import groundbreaking.gigachat.utils.config.values.AutoMessagesValues;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class AutoMessages {

    private final GigaChat plugin;
    private final AutoMessagesValues autoMessagesValues;

    public final List<AutoMessageConstructor> autoMessagesClone = new ObjectArrayList<>();

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
            if (AutoMessagesCollection.contains(player.getName())) {
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
                Collections.shuffle(autoMessages);
            }
        }

        final AutoMessageConstructor autoMessage = this.autoMessagesClone.get(0);
        this.autoMessagesClone.remove(0);

        return autoMessage;
    }
}