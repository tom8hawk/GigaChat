package com.github.groundbreakingmc.gigachat.exceptions;

import com.github.groundbreakingmc.gigachat.GigaChat;
import org.bukkit.Bukkit;

public final class NotPaperException extends RuntimeException {

    public NotPaperException(final GigaChat plugin) {
        Bukkit.getPluginManager().disablePlugin(plugin);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        this.printStackTrace();
        return this;
    }

    @Override
    public void printStackTrace() {
        Bukkit.getLogger().warning("[GigaChat] \u001b[91m=============== \u001b[31mWARNING \u001b[91m===============\u001b[0m");
        Bukkit.getLogger().warning("[GigaChat] \u001b[91mThe plugin dev is against using Bukkit, Spigot etc.!\u001b[0m");
        Bukkit.getLogger().warning("[GigaChat] \u001b[91mSwitch to Paper or its fork. To download Paper visit:\u001b[0m");
        Bukkit.getLogger().warning("[GigaChat] \u001b[91mhttps://papermc.io/downloads/all\u001b[0m");
        Bukkit.getLogger().warning("[GigaChat] \u001b[91m=======================================\u001b[0m");
    }
}
