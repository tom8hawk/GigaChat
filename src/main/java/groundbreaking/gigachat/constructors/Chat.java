package groundbreaking.gigachat.constructors;

import groundbreaking.gigachat.collections.DisabledChat;
import groundbreaking.gigachat.collections.Ignore;
import groundbreaking.gigachat.utils.Utils;
import groundbreaking.gigachat.utils.config.values.Messages;
import groundbreaking.gigachat.utils.map.ExpiringMap;
import groundbreaking.gigachat.utils.vanish.IVanishChecker;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Getter
public final class Chat {

    private final String name;
    private final String bypassCooldownPermission;
    private final String format;
    private final String spyFormat;
    private final int distance;
    private final int cooldown;
    private final boolean isNoOneHeardEnabled;
    private final boolean isNoOneHeardHideHidden;
    private final boolean isNoOneHeardHideVanished;
    private final boolean isNoOneHeardHideSpectators;
    private final Map<String, String> groupsColors;
    private final ExpiringMap<String, Long> cooldowns;
    private final List<Player> spyListeners;

    public Chat(
            final @NotNull String name,
            final @NotNull String format,
            final @Nullable String spyFormat,
            final int distance,
            final int cooldown,
            final boolean isNoOneHeardEnabled,
            final boolean isNoOneHeardHideHidden,
            final boolean isNoOneHeardHideVanished,
            final boolean isNoOneHeardHideSpectators,
            final Map<String, String> groupsColors
    ) {
        this.name = name;
        this.bypassCooldownPermission = "gigachat.bypass.cooldown." + name;
        this.format = format;
        this.spyFormat = spyFormat;
        this.distance = distance;
        this.cooldown = cooldown;
        this.isNoOneHeardEnabled = isNoOneHeardEnabled;
        this.isNoOneHeardHideHidden = isNoOneHeardHideHidden;
        this.isNoOneHeardHideVanished = isNoOneHeardHideVanished;
        this.isNoOneHeardHideSpectators = isNoOneHeardHideSpectators;
        this.groupsColors = groupsColors;
        this.cooldowns = new ExpiringMap<>(cooldown, TimeUnit.MILLISECONDS);
        this.spyListeners = new ArrayList<>();
    }

    public boolean hasCooldown(final Player sender, final Messages messages, final AsyncPlayerChatEvent event) {
        if (!sender.hasPermission(this.bypassCooldownPermission) && this.cooldowns.containsKey(sender.getName())) {
            final String senderName = sender.getName();
            final int time = (int) (this.cooldown / 1000 + (this.cooldowns.get(senderName) - System.currentTimeMillis()) / 1000);
            final String restTime = Utils.getTime(time);
            final String cooldownMessage = messages.getChatCooldownMessage().replace("{time}", restTime);
            sender.sendMessage(cooldownMessage);
            event.setCancelled(true);

            return true;
        }

        return false;
    }

    public String getColor(final String group) {
        return this.groupsColors.getOrDefault(group, "");
    }

    public List<Player> getRecipients(final Player sender) {
        if (this.distance == -1) {
            return this.getSenderWorld(sender);
        }

        if (this.distance == -2) {
            return this.getNotIgnored(sender);
        }

        final List<Player> playerList = new ArrayList<>();
        final Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        if (this.distance == -3) {
            playerList.addAll(onlinePlayers);
            return playerList;
        }

        final String senderName = sender.getName();
        final World senderWorld = sender.getWorld();
        final Location senderLocation = sender.getLocation();


        final double maxDist = Math.pow(this.distance, 2.0D);

        for (final Player target : onlinePlayers) {
            final String targetName = target.getName();
            if (Ignore.isIgnoredChat(targetName, senderName) || DisabledChat.contains(targetName)) {
                continue;
            }

            final World targetWorld = target.getWorld();
            if (targetWorld == senderWorld) {
                final Location targetLocation = target.getLocation();
                final double squaredDistance = senderLocation.distanceSquared(targetLocation);
                if (squaredDistance <= maxDist) {
                    playerList.add(target);
                }
            }
        }

        return playerList;
    }

    private List<Player> getNotIgnored(final Player sender) {
        final List<Player> playerList = new ArrayList<>();
        final Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        final String senderName = sender.getName();
        for (final Player target : onlinePlayers) {
            final String targetName = target.getName();
            if (!Ignore.isIgnoredChat(targetName, senderName)) {
                playerList.add(target);
            }
        }

        return playerList;
    }

    private List<Player> getSenderWorld(final Player sender) {
        final List<Player> playerList = new ArrayList<>();
        final Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        final World senderWorld = sender.getWorld();
        for (final Player target : onlinePlayers) {
            final World targetWorld = target.getWorld();
            if (senderWorld == targetWorld) {
                playerList.add(target);
            }
        }

        return playerList;
    }

    public boolean isNoOneHeard(final Player sender, final List<Player> recipients, final IVanishChecker vanishChecker) {
        if (this.isNoOneHeardEnabled) {
            final List<Player> validRecipients = new ArrayList<>(recipients);
            validRecipients.removeIf(recipient ->
                    !this.isRecipientValid(sender, recipient, vanishChecker)
            );

            return validRecipients.size() == 1;
        }

        return false;
    }

    private boolean isRecipientValid(final Player sender, final Player recipient, final IVanishChecker vanishChecker) {
        return this.isRecipientVisible(sender, recipient)
                && this.isRecipientNotVanished(recipient, vanishChecker)
                && this.isRecipientNotSpectator(recipient);
    }

    private boolean isRecipientVisible(final Player sender, final Player recipient) {
        return !this.isNoOneHeardHideHidden || sender.canSee(recipient);
    }

    private boolean isRecipientNotVanished(final Player recipient, final IVanishChecker vanishChecker) {
        return !this.isNoOneHeardHideVanished || !vanishChecker.isVanished(recipient);
    }

    private boolean isRecipientNotSpectator(final Player recipient) {
        return !this.isNoOneHeardHideSpectators || recipient.getGameMode() != GameMode.SPECTATOR;
    }
}
