package groundbreaking.gigachat.utils.logging;

import groundbreaking.gigachat.GigaChat;

import java.util.logging.Level;

public final class BukkitLogger implements Logger {

    private final java.util.logging.Logger logger;

    public BukkitLogger(final GigaChat plugin) {
        this.logger = plugin.getLogger();
    }

    public void info(final String msg) {
        this.logger.log(Level.INFO, msg);
    }

    public void warning(final String msg) {
        this.logger.log(Level.WARNING, msg);
    }
}
