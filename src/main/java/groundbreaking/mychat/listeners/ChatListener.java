package groundbreaking.mychat.listeners;

import groundbreaking.mychat.MyChat;
import groundbreaking.mychat.utils.ConfigValues;
import groundbreaking.mychat.utils.Utils;
import groundbreaking.mychat.utils.colorizer.IColorizer;
import groundbreaking.mychat.utils.map.ExpiringMap;
import net.md_5.bungee.api.chat.BaseComponent;
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
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ChatListener implements Listener {

    private final MyChat plugin;
    private final ConfigValues configValues;
    private final IColorizer colorizer;
    private final ExpiringMap<String, Long> localCooldowns;
    private final ExpiringMap<String, Long> globalCooldowns;

    public ChatListener(MyChat plugin) {
        this.plugin = plugin;
        configValues = plugin.getPluginConfig();
        colorizer = plugin.getColorizer();
        localCooldowns = new ExpiringMap<>(configValues.getLocalCooldown(), TimeUnit.MILLISECONDS);
        globalCooldowns = new ExpiringMap<>(configValues.getGlobalCooldown(), TimeUnit.MILLISECONDS);
    }

    private final String[] searchList = {"{player}", "{prefix}", "{suffix}", "{local-color}", "{global-color}"};

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onMessageSend(AsyncPlayerChatEvent e) {
        final Player player = e.getPlayer();

        long time = (System.currentTimeMillis() - player.getFirstPlayed()) / 1000;
        if (steelNewbie(player, time)) {
            String restTime = Utils.getTime((int) (configValues.getNewbieChatCooldown() - time));
            player.sendMessage(configValues.getNewbieChatMessage().replace("{time}", restTime));
            e.setCancelled(true);
            return;
        }

        final String name = player.getName();
        final String prefix = plugin.getChat().getPlayerPrefix(player);
        final String suffix = plugin.getChat().getPlayerSuffix(player);
        final String localColor = this.getLocalColor(player);
        final String globalColor = this.getGlobalColor(player);

        final String[] replacementList = { name, prefix, suffix, localColor, globalColor };

        final String message = e.getMessage();
        final String globalMessage = removeGlobalPrefix(message);

        if ((configValues.isForceGlobal() || startsWithExclamation(message)) && !globalMessage.isEmpty()) {

            if (hasCooldown(player, name, globalCooldowns)) {
                String restTime = Utils.getTime((int) (configValues.getGlobalCooldown() / 1000 + (globalCooldowns.get(name) - System.currentTimeMillis()) / 1000));
                player.sendMessage(configValues.getCooldownMessage().replace("{time}", restTime));
                e.setCancelled(true);
            }

            String gf = colorizer.colorize(Utils.replacePlaceholders(player, Utils.replaceEach(configValues.getGlobalFormat(), searchList, replacementList)));
            String chatMessage = Utils.formatByPerm(player, globalMessage);
            if (configValues.isHoverTextEnable()) {
                e.setCancelled(true);
                sendHover(player, replacementList, gf, new ArrayList<>(Bukkit.getOnlinePlayers()), chatMessage);
            }
            else {
                e.setFormat(gf.replace("{message}", chatMessage).replace("%", "%%"));
            }
        }
        else {
            if (hasCooldown(player, name, localCooldowns)) {
                String restTime = Utils.getTime((int) (configValues.getGlobalCooldown() / 1000 + (globalCooldowns.get(name) - System.currentTimeMillis()) / 1000));
                player.sendMessage(configValues.getCooldownMessage().replace("{time}", restTime));
                e.setCancelled(true);
            }

            String localMessage = colorizer.colorize(Utils.replacePlaceholders(player, Utils.replaceEach(configValues.getLocalFormat(), searchList, replacementList)));
            e.getRecipients().clear();
            e.getRecipients().add(player);
            List<Player> radiusInfo = getRadius(player);
            if (!radiusInfo.isEmpty()) {
                e.getRecipients().addAll(radiusInfo);
            }
            String chatMessage = Utils.formatByPerm(player, message);
            if (configValues.isHoverTextEnable()) {
                radiusInfo.add(player);
                e.setCancelled(true);
                sendHover(player, replacementList, localMessage, radiusInfo, chatMessage);
            } else {
                e.setFormat(localMessage.replace("{message}", chatMessage).replace("%", "%%"));
            }
        }
    }

    private boolean steelNewbie(Player player, long time) {
        if (!configValues.isNewbieChatEnable() && player.hasPermission("mychat.bypass.newbie.chat")) {
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

    private void sendHover(Player p, String[] replacementList, String format, List<Player> recipients, String chatMessage) {
        String hoverString = colorizer.colorize(Utils.replacePlaceholders(p, Utils.replaceEach(configValues.getHoverMessage(), searchList, replacementList)));
        HoverEvent hoverText = new HoverEvent(Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText(hoverString)));
        BaseComponent[] comp = TextComponent.fromLegacyText(format.replace("{message}", chatMessage).replace("%", "%%"));
        for (int i = 0; i < comp.length; i++) {
            comp[i].setHoverEvent(hoverText);
            comp[i].setClickEvent(configValues.getHoverEvent());
        }
        for (int i = 0; i < recipients.size(); i++) {
            recipients.get(i).spigot().sendMessage(comp);
        }

        Bukkit.getConsoleSender().sendMessage(format.replace("{message}", chatMessage).replace("%", "%%"));
    }

    private List<Player> getRadius(Player player) {
        List<Player> playerList = new ArrayList<>();
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.getWorld() == player.getWorld()) {
                boolean dist = player.getLocation().distanceSquared(target.getLocation()) <= Math.pow(configValues.getChatRadius(), 2.0D);
                if (target != player && dist) {
                    playerList.add(target);
                }
            }
        }
        return playerList;
    }

    public void removePlayerLocalCooldown(String playerName) {
        localCooldowns.remove(playerName);
    }

    public void removePlayerGlobalCooldown(String playerName) {
        globalCooldowns.remove(playerName);
    }
}
