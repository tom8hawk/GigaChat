package com.github.groundbreakingmc.gigachat.utils.logging;

import com.github.groundbreakingmc.gigachat.GigaChat;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class PaperLogger implements Logger {

    private final ComponentLogger logger;
    private final LegacyComponentSerializer legacySection;

    public PaperLogger(final GigaChat plugin) {
        this.logger = ComponentLogger.logger(plugin.getLogger().getName());
        this.legacySection = LegacyComponentSerializer.legacySection();
    }

    public void info(final String msg) {
        this.logger.info(legacySection.deserialize(msg));
    }

    public void warning(final String msg) {
        this.logger.warn(legacySection.deserialize(msg));
    }
}
