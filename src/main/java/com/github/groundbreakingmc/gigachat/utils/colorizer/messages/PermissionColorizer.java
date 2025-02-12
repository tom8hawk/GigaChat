package com.github.groundbreakingmc.gigachat.utils.colorizer.messages;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.mylib.colorizer.Colorizer;
import org.bukkit.entity.Player;

public abstract class PermissionColorizer {

    protected final Colorizer messagesColorizer;
    protected static final char COLOR_CHAR = '&';
    protected static final char MINECRAFT_COLOR_CHAR = '§';

    protected PermissionColorizer(final GigaChat plugin) {
        this.messagesColorizer = plugin.getVersionColorizer();
    }

    public abstract String colorize(Player player, String message);

    public abstract String colorize(String message);
}
