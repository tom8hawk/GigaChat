//package groundbreaking.gigachat.listeners;
//
//import groundbreaking.gigachat.GigaChat;
//import groundbreaking.gigachat.collections.Cooldowns;
//import groundbreaking.gigachat.collections.DisabledChat;
//import groundbreaking.gigachat.collections.Ignore;
//import groundbreaking.gigachat.collections.LocalSpy;
//import groundbreaking.gigachat.commands.args.DisableServerChatArgument;
//import groundbreaking.gigachat.utils.StringUtil;
//import groundbreaking.gigachat.utils.Utils;
//import groundbreaking.gigachat.utils.config.values.ChatValues;
//import groundbreaking.gigachat.utils.config.values.Messages;
//import net.md_5.bungee.api.chat.BaseComponent;
//import net.md_5.bungee.api.chat.ClickEvent;
//import net.md_5.bungee.api.chat.HoverEvent;
//import net.md_5.bungee.api.chat.HoverEvent.Action;
//import net.md_5.bungee.api.chat.TextComponent;
//import net.md_5.bungee.api.chat.hover.content.Text;
//import org.bukkit.*;
//import org.bukkit.entity.Player;
//import org.bukkit.event.*;
//import org.bukkit.event.player.AsyncPlayerChatEvent;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//
//public final class ChatListener implements Listener {
//
//    private final GigaChat plugin;
//    private final ChatValues chatValues;
//    private final Messages messages;
//    private final Cooldowns cooldowns;
//
//    private final StringUtil stringUtil = new StringUtil();
//
//    private boolean isRegistered = false;
//
//    private final String[] placeholders = { "{player}", "{prefix}", "{suffix}", "{color}" };
//
//    public ChatListener(final GigaChat plugin) {
//        this.plugin = plugin;
//        this.chatValues = plugin.getChatValues();
//        this.messages = plugin.getMessages();
//        this.cooldowns = plugin.getCooldowns();
//    }
//
//    @EventHandler
//    public void onMessageSend(final AsyncPlayerChatEvent event) {
//        this.processEvent(event);
//    }
//
//    public void registerEvent() {
//        if (this.isRegistered) {
//            this.unregisterEvent();
//        }
//
//        final Class<? extends Event> eventClass = AsyncPlayerChatEvent.class;
//        final EventPriority eventPriority = this.plugin.getEventPriority(this.chatValues.getPriority(), "chats.yml");
//
//        this.plugin.getServer().getPluginManager().registerEvent(eventClass, this, eventPriority,
//                (listener, event) -> this.onMessageSend((AsyncPlayerChatEvent) event),
//                this.plugin
//        );
//
//        this.isRegistered = true;
//    }
//
//    private void unregisterEvent() {
//        HandlerList.unregisterAll(this);
//        this.isRegistered = false;
//    }
//
//    private void processEvent(final AsyncPlayerChatEvent event) {
//        final Player messageSender = event.getPlayer();
//
//        if (DisableServerChatArgument.isChatDisabled() && !messageSender.hasPermission("gigachat.bypass.disabledchat")) {
//            messageSender.sendMessage(this.messages.getServerChatIsDisabled());
//            event.setCancelled(true);
//            return;
//        }
//
//        final String name = messageSender.getName();
//        final String prefix = this.plugin.getChat().getPlayerPrefix(messageSender);
//        final String suffix = this.plugin.getChat().getPlayerSuffix(messageSender);
//
//        final String[] replacementList = { name, prefix, suffix, "" };
//
//        String message = event.getMessage();
//
//        if (this.chatValues.isCharsValidatorEnabled() && this.stringUtil.hasBlockedChars(message)) {
//            if (this.chatValues.isCharsValidatorBlockMessage()) {
//                messageSender.sendMessage(this.messages.getCharsValidationFailedMessage());
//                this.playDenySoundCharsFailed(messageSender);
//                event.setCancelled(true);
//                return;
//            }
//
//            message = this.stringUtil.getFormattedCharsMessage(message);
//        }
//
//        if (this.chatValues.isCapsValidatorEnabled() && this.stringUtil.isUpperCasePercentageExceeded(message, this.chatValues.getCapsValidatorMaxPercentage())) {
//            if (this.chatValues.isCapsValidatorBlockMessageSend()) {
//                messageSender.sendMessage(this.messages.getCapsValidationFailedMessage());
//                this.playDenySoundCapsFailed(messageSender);
//                event.setCancelled(true);
//                return;
//            }
//
//            message = message.toLowerCase();
//        }
//
//        final String formattedMessage;
//        if (this.chatValues.isGlobalForce() || this.isValidForGlobal(message)) {
//            if (this.cooldowns.hasCooldown(messageSender, name, "gigachat.bypass.cooldown.global", this.cooldowns.getGlobalCooldowns())) {
//                final String restTime = Utils.getTime(
//                        (int) (this.chatValues.getGlobalCooldown() / 1000 + (this.cooldowns.getGlobalCooldowns().get(name) - System.currentTimeMillis()) / 1000)
//                );
//                final String cooldownMessage = this.messages.getChatCooldownMessage().replace("{time}", restTime);
//                messageSender.sendMessage(cooldownMessage);
//                event.setCancelled(true);
//                return;
//            }
//
//            event.getRecipients().clear();
//            event.getRecipients().addAll(this.getNotIgnored(messageSender));
//
//            final String color = this.getGlobalColor(messageSender);
//            replacementList[3] = color;
//
//            final String globalMessage = this.removeGlobalPrefix(message);
//            formattedMessage = this.getFormattedMessage(messageSender, globalMessage, chatValues.getGlobalFormat(), replacementList);
//        } else {
//            if (cooldowns.hasCooldown(messageSender, name, "gigachat.bypass.cooldown.local", cooldowns.getLocalCooldowns())) {
//                String restTime = Utils.getTime(
//                        (int) (chatValues.getLocalCooldown() / 1000 + (cooldowns.getLocalCooldowns().get(name) - System.currentTimeMillis()) / 1000)
//                );
//                messageSender.sendMessage(messages.getChatCooldownMessage().replace("{time}", restTime));
//                event.setCancelled(true);
//                return;
//            }
//
//            final String color = this.getLocalColor(messageSender);
//            replacementList[3] = color;
//
//            event.getRecipients().clear();
//            event.getRecipients().addAll(this.getRadius(messageSender));
//
//            formattedMessage = this.getFormattedMessage(messageSender, message, chatValues.getLocalFormat(), replacementList);
//
//            if (chatValues.isNoOneHearEnabled()) {
//                final List<Player> validRecipients = new ArrayList<>(event.getRecipients());
//                for (int i = validRecipients.size() - 1; i >= 0; i--) {
//                    final Player recipient = validRecipients.get(i);
//                    if (chatValues.isNoOneHearHideHidden() && !messageSender.canSee(recipient)) {
//                        validRecipients.remove(i);
//                    } else if (chatValues.isNoOneHearHideVanished() && plugin.getVanishChecker().isVanished(recipient)) {
//                        validRecipients.remove(i);
//                    } else if (chatValues.isNoOneHearHideSpectators() && recipient.getGameMode() == GameMode.SPECTATOR) {
//                        validRecipients.remove(i);
//                    }
//                }
//
//                if (validRecipients.size() == 1) {
//                    messageSender.sendMessage(messages.getNoOneHear());
//                }
//            }
//
//            final List<Player> localSpyRecipients = LocalSpy.getAll();
//            if (!localSpyRecipients.isEmpty()) {
//                for (int i = localSpyRecipients.size() - 1; i >= 0; i--) {
//                    if (event.getRecipients().contains(localSpyRecipients.get(i))) {
//                        localSpyRecipients.remove(i);
//                    }
//                }
//
//                this.sendLocalSpy(messageSender, message, localSpyRecipients, replacementList);
//            }
//        }
//
//        final List<Player> recipients = new ArrayList<>(event.getRecipients());
//        if (this.chatValues.isHoverEnabled() && this.chatValues.isAdminHoverEnabled()) {
//            this.sendBothHover(messageSender, formattedMessage, recipients, replacementList);
//            event.setCancelled(true);
//        } else if (this.chatValues.isHoverEnabled()) {
//            final String hoverText = this.chatValues.getHoverText();
//            final String hoverAction = this.chatValues.getHoverAction();
//            final String hoverValue = this.chatValues.getHoverValue();
//            final List<Player> recipient = new ArrayList<>(event.getRecipients());
//
//            this.sendHover(messageSender, formattedMessage, hoverText, hoverAction, hoverValue, recipient, replacementList);
//
//            event.setCancelled(true);
//        } else if (this.chatValues.isAdminHoverEnabled()) {
//            final String adminHoverText = this.chatValues.getAdminHoverText();
//            final String adminHoverAction = this.chatValues.getHoverAction();
//            final String adminHoverValue = this.chatValues.getHoverValue();
//            final List<Player> adminRecipients = this.getAdminRecipients(recipients);
//
//            this.sendHover(messageSender, formattedMessage, adminHoverText, adminHoverAction, adminHoverValue, adminRecipients, replacementList);
//
//            event.setCancelled(true);
//        } else {
//            event.setFormat(formattedMessage);
//        }
//    }
//
//    private void sendBothHover(final Player player, final String formattedMessage, final List<Player> recipients, final String[] replacementList) {
//        final String hoverText = this.chatValues.getHoverText();
//        final String hoverAction = this.chatValues.getHoverAction();
//        final String hoverValue = this.chatValues.getHoverValue();
//
//        this.sendHover(player, formattedMessage, hoverText, hoverAction, hoverValue, recipients, replacementList);
//
//        final List<Player> adminRecipients = this.getAdminRecipients(recipients);
//        if (!adminRecipients.isEmpty()) {
//            final String adminHoverText = this.chatValues.getAdminHoverText();
//            final String adminHoverAction = this.chatValues.getHoverAction();
//            final String adminHoverValue = this.chatValues.getHoverValue();
//
//            this.sendHover(player, formattedMessage, adminHoverText, adminHoverAction, adminHoverValue, adminRecipients, replacementList);
//        }
//    }
//
//    private void sendHover(final Player player, final String formattedMessage, final String hoverText, final String hoverAction, String hoverValue, final List<Player> recipients, final String[] replacementList) {
//        final String hoverString = this.chatValues.getFormatsColorizer().colorize(
//                Utils.replacePlaceholders(
//                        player,
//                        Utils.replaceEach(hoverText, this.placeholders, replacementList)
//                )
//        );
//        final ClickEvent.Action clickEventAction = ClickEvent.Action.valueOf(hoverAction);
//        hoverValue = hoverValue.replace("{player}", player.getName());
//
//        final HoverEvent hoverEvent = new HoverEvent(Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText(hoverString)));
//        final ClickEvent clickEvent = new ClickEvent(clickEventAction, hoverValue);
//        final BaseComponent[] comp = TextComponent.fromLegacyText(formattedMessage);
//
//        for (int i = 0; i < comp.length; i++) {
//            comp[i].setHoverEvent(hoverEvent);
//            comp[i].setClickEvent(clickEvent);
//        }
//        for (int i = 0; i < recipients.size(); i++) {
//            recipients.get(i).spigot().sendMessage(comp);
//        }
//
//        this.plugin.getServer().getConsoleSender().sendMessage(formattedMessage);
//    }
//
//    private void sendLocalSpy(final Player sender, final String message, final List<Player> localSpyRecipients, final String[] replacementList) {
//        final String formattedMessage = this.getFormattedMessage(sender, message, this.chatValues.getLocalSpyFormat(), replacementList);
//
//        if (this.chatValues.isHoverEnabled()) {
//            final String hoverText = this.chatValues.getHoverText();
//            final String hoverAction = this.chatValues.getHoverAction();
//            final String hoverValue = this.chatValues.getHoverValue();
//
//            this.sendHover(sender, formattedMessage, hoverText, hoverAction, hoverValue, localSpyRecipients, replacementList);
//            return;
//        }
//
//        for (int i = 0; i < localSpyRecipients.size(); i++) {
//            localSpyRecipients.get(i).sendMessage(formattedMessage);
//        }
//    }
//
//    private void playDenySoundCharsFailed(final Player messageSender) {
//        if (this.chatValues.isCharsValidatorDenySoundEnabled()) {
//            final Location location = messageSender.getLocation();
//            final Sound sound = this.chatValues.getTextValidatorDenySound();
//            final float volume = this.chatValues.getTextValidatorDenySoundVolume();
//            final float pitch = this.chatValues.getTextValidatorDenySoundPitch();
//
//            messageSender.playSound(location, sound, volume, pitch);
//        }
//    }
//
//    private void playDenySoundCapsFailed(final Player messageSender) {
//        if (this.chatValues.isCapsValidatorDenySoundEnabled()) {
//            final Location location = messageSender.getLocation();
//            final Sound sound = this.chatValues.getCapsValidatorDenySound();
//            final float volume = this.chatValues.getCapsValidatorDenySoundVolume();
//            final float pitch = this.chatValues.getCapsValidatorDenySoundPitch();
//
//            messageSender.playSound(location, sound, volume, pitch);
//        }
//    }
//
//    private String getLocalColor(final Player player) {
//        final String playerGroup = this.plugin.getPerms().getPrimaryGroup(player);
//        return this.chatValues.getLocalGroupsColors().getOrDefault(playerGroup, "");
//    }
//
//    private String getGlobalColor(final Player player) {
//        final String playerGroup = this.plugin.getPerms().getPrimaryGroup(player);
//        return this.chatValues.getGlobalGroupsColors().getOrDefault(playerGroup, "");
//    }
//
//    private List<Player> getAdminRecipients(final List<Player> recipients) {
//        final List<Player> adminRecipients = new ArrayList<>();
//        for (int i = recipients.size() - 1; i >= 0; i--) {
//            final Player target = recipients.get(i);
//            if (target.hasPermission("gigachat.adminhover")) {
//                adminRecipients.add(target);
//                recipients.remove(i);
//            }
//        }
//
//        return adminRecipients;
//    }
//
//    public boolean isValidForGlobal(final String message) {
//        return message.trim().length() != 1 && message.charAt(0) == this.chatValues.getGlobalSymbol();
//    }
//
//    public String removeGlobalPrefix(final String message) {
//        return this.chatValues.isGlobalForce() ? message : message.substring(1).trim();
//    }
//
//    private List<Player> getRadius(final Player messageSender) {
//        final String senderName = messageSender.getName();
//        final World senderWorld = messageSender.getWorld();
//        final Location senderLocation = messageSender.getLocation();
//
//        final List<Player> playerList = new ArrayList<>();
//        final Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
//
//        final int localDistance = this.chatValues.getLocalDistance();
//        final double maxDist = Math.pow(localDistance, 2.0D);
//
//        for (final Player target : onlinePlayers) {
//            final String targetName = target.getName();
//            if (Ignore.isIgnoredChat(targetName, senderName) || DisabledChat.contains(targetName)) {
//                continue;
//            }
//
//            if (target.getWorld() == senderWorld) {
//                final Location targetLocation = target.getLocation();
//                final boolean distance = senderLocation.distanceSquared(targetLocation) <= maxDist;
//                if (distance) {
//                    playerList.add(target);
//                }
//            }
//        }
//
//        return playerList;
//    }
//
//    private List<Player> getNotIgnored(final Player messageSender) {
//        final List<Player> playerList = new ArrayList<>();
//        for (final Player target : Bukkit.getOnlinePlayers()) {
//            final String senderName = messageSender.getName();
//            final String targetName = target.getName();
//            if (!Ignore.isIgnoredChat(targetName, senderName)) {
//                playerList.add(target);
//            }
//        }
//
//        return playerList;
//    }
//
//    public String getFormattedMessage(final Player messageSender, String message, final String format, final String[] replacementList) {
//        final String formattedMessage = this.chatValues.getFormatsColorizer().colorize(
//                Utils.replacePlaceholders(
//                        messageSender,
//                        Utils.replaceEach(format, this.placeholders, replacementList)
//                )
//        );
//
//        message = this.chatValues.getChatsColorizer().colorize(messageSender, message);
//
//        return formattedMessage.replace("{message}", message).replace("%", "%%");
//    }
//
//    public void setupValidator() {
//        final char[] allowedChars = this.chatValues.getTextValidatorAllowedChars();
//        final char censorshipChar = this.chatValues.getTextValidatorCensorshipChar();
//        this.stringUtil.setupChars(allowedChars, censorshipChar);
//    }
//}
