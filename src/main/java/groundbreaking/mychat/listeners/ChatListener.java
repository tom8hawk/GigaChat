package groundbreaking.mychat.listeners;

import groundbreaking.mychat.MyChat;
import groundbreaking.mychat.utils.ConfigValues;
import groundbreaking.mychat.utils.Utils;
import groundbreaking.mychat.utils.colorizer.IColorizer;
import groundbreaking.mychat.utils.map.ExpiringMap;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ChatListener implements Listener {

    private final MyChat plugin;
    private final ConfigValues configValues;
    private final IColorizer colorizer;
    private final ExpiringMap<String, Long> localCooldowns;
    private final ExpiringMap<String, Long> globalCooldowns;

    public ChatListener(MyChat plugin) {
        this.plugin = plugin;
        this.configValues = plugin.getConfigValues();
        this.colorizer = plugin.getColorizer();
        this.localCooldowns = new ExpiringMap<>(configValues.getLocalCooldown(), TimeUnit.MILLISECONDS);
        this.globalCooldowns = new ExpiringMap<>(configValues.getGlobalCooldown(), TimeUnit.MILLISECONDS);
    }

    private final String[] searchList = {"{player}", "{prefix}", "{suffix}", "{local-color}", "{global-color}"};

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onMessageSend(AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();

        long time = (System.currentTimeMillis() - player.getFirstPlayed()) / 1000;
        if (steelNewbie(player, time)) {
            String restTime = Utils.getTime((int) (configValues.getNewbieChatCooldown() - time));
            player.sendMessage(configValues.getNewbieChatMessage().replace("{time}", restTime));
            event.setCancelled(true);
            return;
        }

        final String name = player.getName();
        final String prefix = plugin.getChat().getPlayerPrefix(player);
        final String suffix = plugin.getChat().getPlayerSuffix(player);
        final String localColor = this.getLocalColor(player);
        final String globalColor = this.getGlobalColor(player);

        final String[] replacementList = { name, prefix, suffix, localColor, globalColor };

        final String message = event.getMessage();
        final String globalMessage = removeGlobalPrefix(message);

        String formattedMessage;
        if ((configValues.isForceGlobal() || startsWithExclamation(message)) && !globalMessage.isEmpty()) {

            if (hasCooldown(player, name, globalCooldowns)) {
                String restTime = Utils.getTime((int) (configValues.getGlobalCooldown() / 1000 + (globalCooldowns.get(name) - System.currentTimeMillis()) / 1000));
                player.sendMessage(configValues.getCooldownMessage().replace("{time}", restTime));
                event.setCancelled(true);
            }

            formattedMessage = getFormattedMessage(player, globalMessage, configValues.getGlobalFormat(), replacementList);
        }
        else {
            if (hasCooldown(player, name, localCooldowns)) {
                String restTime = Utils.getTime((int) (configValues.getGlobalCooldown() / 1000 + (globalCooldowns.get(name) - System.currentTimeMillis()) / 1000));
                player.sendMessage(configValues.getCooldownMessage().replace("{time}", restTime));
                event.setCancelled(true);
            }

            event.getRecipients().clear();
            final Set<Player> radiusInfo = getRadius(player);
            event.getRecipients().addAll(radiusInfo);

            formattedMessage = getFormattedMessage(player, message, configValues.getLocalFormat(), replacementList);
        }

        if (configValues.isHoverTextEnable()) {
            event.setCancelled(true);
            sendHover(player, formattedMessage, new ArrayList<>(Bukkit.getOnlinePlayers()), replacementList);
        }
        else {
            event.setFormat(formattedMessage);
        }
    }

    private boolean steelNewbie(Player player, long time) {
        if (!configValues.isNewbieChatEnable() || player.hasPermission("mychat.bypass.newbie.chat")) {
            return false;
        }

        return time <= configValues.getNewbieChatCooldown();
    }

    public boolean startsWithExclamation(String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }

        return message.charAt(0) == configValues.getGlobalSymbol();
    }

    private String getLocalColor(Player p) {
        String playerGroup = plugin.getPerms().getPrimaryGroup(p);
        return configValues.getLocalGroupsColors().getOrDefault(playerGroup, "");
    }

    private String getGlobalColor(Player p) {
        String playerGroup = plugin.getPerms().getPrimaryGroup(p);
        return configValues.getGlobalGroupsColors().getOrDefault(playerGroup, "");
    }

    private boolean hasCooldown(Player player, String name, ExpiringMap<String, Long> playerCooldown) {
        if (player.hasPermission("mychat.bypass.cooldown")) {
            return false;
        }

        if (playerCooldown.containsKey(name)) {
            return true;
        }

        playerCooldown.put(name, System.currentTimeMillis());
        return false;
    }

    public String removeGlobalPrefix(String message) {
        return configValues.isForceGlobal() ? message : message.substring(1).trim();
    }

    private void sendHover(Player player, String formattedMessage, List<Player> recipients, String[] replacementList) {
        final String hoverString = colorizer.colorize(
                Utils.replacePlaceholders(player, Utils.replaceEach(configValues.getHoverMessage(), searchList, replacementList))
        );
        final ClickEvent.Action hoverAction = ClickEvent.Action.valueOf(configValues.getClickAction());
        final String hoverValue = configValues.getClickValue().replace("{player}", player.getName());

        final HoverEvent hoverText = new HoverEvent(Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText(hoverString)));
        final ClickEvent clickEvent = new ClickEvent(hoverAction, hoverValue);
        final BaseComponent[] comp = TextComponent.fromLegacyText(formattedMessage);

        for (int i = 0; i < comp.length; i++) {
            comp[i].setHoverEvent(hoverText);
            comp[i].setClickEvent(clickEvent);
        }
        for (int i = 0; i < recipients.size(); i++) {
            recipients.get(i).spigot().sendMessage(comp);
        }

        Bukkit.getConsoleSender().sendMessage(formattedMessage);
    }

    private Set<Player> getRadius(Player player) {
        final Set<Player> playerList = new HashSet<>();
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.getWorld() == player.getWorld()) {
                boolean dist = player.getLocation().distanceSquared(target.getLocation()) <= Math.pow(configValues.getChatRadius(), 2.0D);
                if (target != player && dist) {
                    playerList.add(target);
                }
            }
        }
        playerList.add(player);
        return playerList;
    }

    public void removePlayerLocalCooldown(String playerName) {
        localCooldowns.remove(playerName);
    }

    public void removePlayerGlobalCooldown(String playerName) {
        globalCooldowns.remove(playerName);
    }

    public String getFormattedMessage(Player player, String message, String format, String[] replacementList) {
        final String formatted = colorizer.colorize(Utils.replacePlaceholders(player, Utils.replaceEach(format, searchList, replacementList)));
        final String chatMessage = Utils.colorizeChatMessage(player, message);
        return formatted.replace("{message}", chatMessage).replace("%", "%%");
    }
}
