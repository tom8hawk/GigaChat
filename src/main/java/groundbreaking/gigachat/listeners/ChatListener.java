package groundbreaking.gigachat.listeners;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.collections.Cooldowns;
import groundbreaking.gigachat.collections.DisabledChat;
import groundbreaking.gigachat.collections.Ignore;
import groundbreaking.gigachat.collections.LocalSpy;
import groundbreaking.gigachat.commands.args.DisableServerChatArgument;
import groundbreaking.gigachat.utils.Utils;
import groundbreaking.gigachat.utils.config.values.ChatValues;
import groundbreaking.gigachat.utils.config.values.Messages;
import groundbreaking.gigachat.utils.config.values.NewbieChatValues;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.List;

public final class ChatListener implements Listener {

    private final GigaChat plugin;
    private final ChatValues chatValues;
    private final NewbieChatValues newbieValues;
    private final Messages messages;
    private final Cooldowns cooldowns;

    private final String[] placeholders = { "{player}", "{prefix}", "{suffix}", "{color}" };

    public ChatListener(final GigaChat plugin) {
        this.plugin = plugin;
        this.chatValues = plugin.getChatValues();
        this.newbieValues = plugin.getNewbieChat();
        this.messages = plugin.getMessages();
        this.cooldowns = plugin.getCooldowns();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMessageSendLowest(final AsyncPlayerChatEvent event) {
        if (chatValues.isListenerPriorityLowest()) {
            processEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onMessageSendLow(final AsyncPlayerChatEvent event) {
        if (chatValues.isListenerPriorityLow()) {
            processEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onMessageSendNormal(final AsyncPlayerChatEvent event) {
        if (chatValues.isListenerPriorityNormal()) {
            processEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMessageSendHigh(final AsyncPlayerChatEvent event) {
        if (chatValues.isListenerPriorityHigh()) {
            processEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMessageSendHighest(final AsyncPlayerChatEvent event) {
        if (chatValues.isListenerPriorityHighest()) {
            processEvent(event);
        }
    }

    private void processEvent(final AsyncPlayerChatEvent event) {
        final Player messageSender = event.getPlayer();

        if (DisableServerChatArgument.isChatDisabled() && !messageSender.hasPermission("gigachat.bypass.disabledchat")) {
            messageSender.sendMessage(messages.getServerChatIsDisabled());
            event.setCancelled(true);
            return;
        }

        final String name = messageSender.getName();
        final String prefix = plugin.getChat().getPlayerPrefix(messageSender);
        final String suffix = plugin.getChat().getPlayerSuffix(messageSender);

        final String[] replacementList = { name, prefix, suffix, "" };

        final String message = event.getMessage();

        final String formattedMessage;
        if (chatValues.isGlobalForce() || isValidForGlobal(message)) {
            if (cooldowns.hasCooldown(messageSender, name, "gigachat.bypass.cooldown.global", cooldowns.getGlobalCooldowns())) {
                final String restTime = Utils.getTime(
                        (int) (chatValues.getGlobalCooldown() / 1000 + (cooldowns.getGlobalCooldowns().get(name) - System.currentTimeMillis()) / 1000)
                );
                messageSender.sendMessage(messages.getChatCooldownMessage().replace("{time}", restTime));
                event.setCancelled(true);
                return;
            }

            event.getRecipients().clear();
            event.getRecipients().addAll(getNotIgnored(messageSender));

            final String color = this.getGlobalColor(messageSender);
            replacementList[3] = color;

            final String globalMessage = removeGlobalPrefix(message);
            formattedMessage = getFormattedMessage(messageSender, globalMessage, chatValues.getGlobalFormat(), replacementList);
        }
        else {
            if (cooldowns.hasCooldown(messageSender, name, "gigachat.bypass.cooldown.local", cooldowns.getLocalCooldowns())) {
                String restTime = Utils.getTime(
                        (int) (chatValues.getLocalCooldown() / 1000 + (cooldowns.getLocalCooldowns().get(name) - System.currentTimeMillis()) / 1000)
                );
                messageSender.sendMessage(messages.getChatCooldownMessage().replace("{time}", restTime));
                event.setCancelled(true);
                return;
            }

            final String color = this.getLocalColor(messageSender);
            replacementList[3] = color;

            event.getRecipients().clear();
            event.getRecipients().addAll(getRadius(messageSender));

            formattedMessage = getFormattedMessage(messageSender, message, chatValues.getLocalFormat(), replacementList);

            if (chatValues.isNoOneHearEnabled()) {
                final List<Player> validRecipients = new ArrayList<>(event.getRecipients());
                for (int i = validRecipients.size() - 1; i >= 0; i--) {
                    final Player recipient = validRecipients.get(i);
                    if (chatValues.isNoOneHearHideHidden() && !messageSender.canSee(recipient)) {
                        validRecipients.remove(i);
                    }
                    else if (chatValues.isNoOneHearHideVanished() && plugin.getVanishChecker().isVanished(recipient)) {
                        validRecipients.remove(i);
                    }
                    else if (chatValues.isNoOneHearHideSpectators() && recipient.getGameMode() == GameMode.SPECTATOR) {
                        validRecipients.remove(i);
                    }
                }

                if (validRecipients.size() == 1) {
                    messageSender.sendMessage(messages.getNoOneHear());
                }
            }

            final List<Player> localSpyRecipients = LocalSpy.getAll();
            if (localSpyRecipients.size() != 0) {
                for (int i = localSpyRecipients.size() - 1; i >= 0; i--) {
                    if (event.getRecipients().contains(localSpyRecipients.get(i))) {
                        localSpyRecipients.remove(i);
                    }
                }

                sendLocalSpy(messageSender, message, localSpyRecipients, replacementList);
            }
        }

        if (chatValues.isHoverEnabled() && chatValues.isAdminHoverEnabled()) {
            event.setCancelled(true);
            sendBothHover(messageSender, formattedMessage, new ArrayList<>(event.getRecipients()), replacementList);
        }
        else if (chatValues.isHoverEnabled()) {
            event.setCancelled(true);
            sendHover(messageSender, formattedMessage, chatValues.getHoverText(), chatValues.getHoverAction(), chatValues.getHoverValue(), new ArrayList<>(event.getRecipients()), replacementList);
        }
        else if (chatValues.isAdminHoverEnabled()) {
            event.setCancelled(true);
            sendHover(messageSender, formattedMessage, chatValues.getAdminHoverText(), chatValues.getAdminHoverAction(), chatValues.getAdminHoverValue(), new ArrayList<>(event.getRecipients()), replacementList);
        }
        else {
            event.setFormat(formattedMessage);
        }
    }

    private void sendBothHover(final Player player, final String formattedMessage, final List<Player> recipients, final String[] replacementList) {
        final List<Player> adminRecipients = new ArrayList<>();

        for (int i = recipients.size() - 1; i >= 0; i--) {
            final Player target = recipients.get(i);
            if (target.hasPermission("gigachat.adminhover")) {
                adminRecipients.add(target);
                recipients.remove(i);
            }
        }

        sendHover(player, formattedMessage, chatValues.getHoverText(), chatValues.getHoverAction(), chatValues.getHoverValue(), recipients, replacementList);
        sendHover(player, formattedMessage, chatValues.getAdminHoverText(), chatValues.getAdminHoverAction(), chatValues.getAdminHoverValue(), adminRecipients, replacementList);
    }

    private void sendHover(final Player player, final String formattedMessage, final String hoverText, final String hoverAction, String hoverValue, final List<Player> recipients, final String[] replacementList) {
        final String hoverString = chatValues.getFormatsColorizer().colorize(
                Utils.replacePlaceholders(player, Utils.replaceEach(hoverText, placeholders, replacementList))
        );
        final ClickEvent.Action clickEventAction = ClickEvent.Action.valueOf(hoverAction);
        hoverValue = hoverValue.replace("{player}", player.getName());

        final HoverEvent hoverEvent = new HoverEvent(Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText(hoverString)));
        final ClickEvent clickEvent = new ClickEvent(clickEventAction, hoverValue);
        final BaseComponent[] comp = TextComponent.fromLegacyText(formattedMessage);

        for (int i = 0; i < comp.length; i++) {
            comp[i].setHoverEvent(hoverEvent);
            comp[i].setClickEvent(clickEvent);
        }
        for (int i = 0; i < recipients.size(); i++) {
            recipients.get(i).spigot().sendMessage(comp);
        }

        plugin.getServer().getConsoleSender().sendMessage(formattedMessage);
    }

    private void sendLocalSpy(final Player sender, final String message, final List<Player> localSpyRecipients, final String[] replacementList) {
        final String formattedMessage = getFormattedMessage(sender, message, chatValues.getLocalSpyFormat(), replacementList);

        if (chatValues.isHoverEnabled()) {
            sendHover(sender, formattedMessage, chatValues.getHoverText(), chatValues.getHoverAction(), chatValues.getHoverValue(), localSpyRecipients, replacementList);
            return;
        }

        for (int i = 0; i < localSpyRecipients.size(); i++) {
            localSpyRecipients.get(i).sendMessage(formattedMessage);
        }
    }

    private String getLocalColor(final Player player) {
        String playerGroup = plugin.getPerms().getPrimaryGroup(player);
        return chatValues.getLocalGroupsColors().getOrDefault(playerGroup, "");
    }

    private String getGlobalColor(final Player player) {
        String playerGroup = plugin.getPerms().getPrimaryGroup(player);
        return chatValues.getGlobalGroupsColors().getOrDefault(playerGroup, "");
    }

    public boolean isValidForGlobal(final String message) {
        return message.trim().length() != 1 && message.charAt(0) == chatValues.getGlobalSymbol();
    }

    public String removeGlobalPrefix(final String message) {
        return chatValues.isGlobalForce()
                ? message
                : message.substring(1).trim();
    }

    private List<Player> getRadius(final Player player) {
        final List<Player> playerList = new ArrayList<>();
        final double maxDist = Math.pow(chatValues.getLocalDistance(), 2.0D);
        final Location location = player.getLocation();
        for (final Player target : Bukkit.getOnlinePlayers()) {
            if (Ignore.isIgnoredChat(target.getName(), player.getName())) {
                continue;
            }

            if (DisabledChat.contains(target.getName())) {
                continue;
            }

            if (target.getWorld() == player.getWorld()) {
                final boolean distance = location.distanceSquared(target.getLocation()) <= maxDist;
                if (distance) {
                    playerList.add(target);
                }
            }
        }

        return playerList;
    }

    private List<Player> getNotIgnored(final Player player) {
        final List<Player> playerList = new ArrayList<>();
        for (final Player target : Bukkit.getOnlinePlayers()) {
            if (!Ignore.isIgnoredChat(target.getName(), player.getName())) {
                playerList.add(target);
            }
        }

        return playerList;
    }

    public String getFormattedMessage(Player player, String message, String format, String[] replacementList) {
        final String formatted = chatValues.getFormatsColorizer().colorize(
                Utils.replacePlaceholders(player, Utils.replaceEach(format, placeholders, replacementList))
        );
        final String chatMessage = chatValues.getChatsColorizer().colorize(player, message);
        return formatted.replace("{message}", chatMessage).replace("%", "%%");
    }
}
