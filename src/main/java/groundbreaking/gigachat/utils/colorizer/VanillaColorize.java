package groundbreaking.gigachat.utils.colorizer;

public final class VanillaColorize implements IColorizer {

    @Override
    public String colorize(final String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        return ColorCodesTranslator.translateAlternateColorCodes('&', message);
    }
}
