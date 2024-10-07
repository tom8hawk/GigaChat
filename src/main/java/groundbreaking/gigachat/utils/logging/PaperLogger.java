package groundbreaking.gigachat.utils.logging;

import groundbreaking.gigachat.GigaChat;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class PaperLogger implements ILogger {

    private final ComponentLogger logger;
    private final LegacyComponentSerializer legacySection;

    public PaperLogger(final GigaChat plugin) {
        logger = ComponentLogger.logger(plugin.getMyLogger().getName());
        legacySection = LegacyComponentSerializer.legacySection();
    }

    public void info(final String msg) {
        logger.info(legacySection.deserialize(msg));
    }

    public void warning(final String msg) {
        logger.warn(legacySection.deserialize(msg));
    }
}
