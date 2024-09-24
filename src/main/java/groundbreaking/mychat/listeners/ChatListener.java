package groundbreaking.mychat.listeners;

import groundbreaking.mychat.MyChat;
import groundbreaking.mychat.utils.Config;
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
    private final Config pluginConfig;
    private final IColorizer colorizer;
    private final ExpiringMap<String, Long> localCooldowns;
    private final ExpiringMap<String, Long> globalCooldowns;

    public ChatListener(MyChat plugin) {
        this.plugin = plugin;
        pluginConfig = plugin.getPluginConfig();
        colorizer = plugin.getColorizer();
        localCooldowns = new ExpiringMap<>(pluginConfig.getLocalCooldown(), TimeUnit.MILLISECONDS);
        globalCooldowns = new ExpiringMap<>(pluginConfig.getGlobalCooldown(), TimeUnit.MILLISECONDS);
    }

    private final String[] searchList = {"<player>", "<prefix>", "<suffix>", "<color>"};

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onMessageSend(AsyncPlayerChatEvent e) {
        final Player player = e.getPlayer();

        long time = (System.currentTimeMillis() - player.getFirstPlayed()) / 1000;
        if (steelNewbie(player, time)) {
            String restTime = Utils.getTime((int) (pluginConfig.getNewbieChatCooldown() - time));
            player.sendMessage(pluginConfig.getNewbieChatMessage().replace("%time%", restTime));
            e.setCancelled(true);
            return;
        }

        final String name = player.getName();
        final String prefix = plugin.getChat().getPlayerPrefix(player);
        final String suffix = plugin.getChat().getPlayerSuffix(player);
        final String color = this.getColor(player);

        final String[] replacementList = { name, prefix, suffix, color };

        final String message = e.getMessage();
        String globalMessage = removeGlobalPrefix(message);

        if ((pluginConfig.isForceGlobal() || startsWithExclamation(message)) && !globalMessage.isEmpty()) {

            if (hasCooldown(player, name, globalCooldowns)) {
                String restTime = Utils.getTime((int) (pluginConfig.getGlobalCooldown() / 1000 + (globalCooldowns.get(name) - System.currentTimeMillis()) / 1000));
                player.sendMessage(pluginConfig.getCooldownMessage().replace("%time%", restTime));
                e.setCancelled(true);
            }

            String gf = colorizer.colorize(Utils.replacePlaceholders(player, Utils.replaceEach(pluginConfig.getGlobalFormat(), searchList, replacementList)));
            String chatMessage = Utils.formatByPerm(player, globalMessage);
            if (pluginConfig.isHoverTextEnable()) {
                e.setCancelled(true);
                sendHover(player, replacementList, gf, new ArrayList<>(Bukkit.getOnlinePlayers()), chatMessage);
            }
            else {
                e.setFormat(gf.replace("<message>", chatMessage).replace("%", "%%"));
            }
        }
        else {
            if (hasCooldown(player, name, localCooldowns)) {
                String restTime = Utils.getTime((int) (pluginConfig.getGlobalCooldown() / 1000 + (globalCooldowns.get(name) - System.currentTimeMillis()) / 1000));
                player.sendMessage(pluginConfig.getCooldownMessage().replace("%time%", restTime));
                e.setCancelled(true);
            }

            String localMessage = colorizer.colorize(Utils.replacePlaceholders(player, Utils.replaceEach(pluginConfig.getLocalFormat(), searchList, replacementList)));
            e.getRecipients().clear();
            e.getRecipients().add(player);
            List<Player> radiusInfo = getRadius(player);
            if (!radiusInfo.isEmpty()) {
                e.getRecipients().addAll(radiusInfo);
            }
            String chatMessage = Utils.formatByPerm(player, message);
            if (pluginConfig.isHoverTextEnable()) {
                radiusInfo.add(player);
                e.setCancelled(true);
                sendHover(player, replacementList, localMessage, radiusInfo, chatMessage);
            } else {
                e.setFormat(localMessage.replace("<message>", chatMessage).replace("%", "%%"));
            }
        }
    }

    private boolean steelNewbie(Player player, long time) {
        if (!pluginConfig.isNewbieChatEnable() && player.hasPermission("pchat.bypass.newbie")) {
            return false;
        }

        return time <= pluginConfig.getNewbieChatCooldown();
    }

    public boolean startsWithExclamation(String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }

        int length = message.length();
        char c;
        for (int i = 0; i < length; i++) {
            c = message.charAt(i);
            if (c == ' ') {
                continue;
            }

            return c == pluginConfig.getGlobalSymbol();
        }
        return false;
    }

    private String getColor(Player p) {
        String playerGroup = plugin.getPerms().getPrimaryGroup(p);
        return pluginConfig.getGroupsColors().getOrDefault(playerGroup, "");
    }

    private boolean hasCooldown(Player player, String name, ExpiringMap<String, Long> playerCooldown) {
        if (player.hasPermission("pchat.bypass.cooldown")) {
            return false;
        }

        if (playerCooldown.containsKey(name)) {
            return true;
        }

        playerCooldown.put(name, System.currentTimeMillis());
        return false;
    }

    public String removeGlobalPrefix(String message) {
        return pluginConfig.isForceGlobal() ? message : message.substring(1).trim();
    }

    private void sendHover(Player p, String[] replacementList, String format, List<Player> recipients, String chatMessage) {
        String hoverText = colorizer.colorize(Utils.replacePlaceholders(p, Utils.replaceEach(pluginConfig.getHoverMessage(), searchList, replacementList)));
        HoverEvent hover = new HoverEvent(Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText(hoverText)));
        BaseComponent[] comp = TextComponent.fromLegacyText(format.replace("<message>", chatMessage).replace("%", "%%"));
        for (BaseComponent component : comp) {
            component.setHoverEvent(hover);
        }
        for (Player ps : recipients) {
            ps.spigot().sendMessage(comp);
        }

        Bukkit.getConsoleSender().sendMessage(format.replace("<message>", chatMessage).replace("%", "%%"));
    }

    private List<Player> getRadius(Player player) {
        List<Player> playerList = new ArrayList<>();
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.getWorld() == player.getWorld()) {
                boolean dist = player.getLocation().distanceSquared(target.getLocation()) <= Math.pow(pluginConfig.getChatRadius(), 2.0D);
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
