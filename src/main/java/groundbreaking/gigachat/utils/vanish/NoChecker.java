package groundbreaking.gigachat.utils.vanish;

import org.bukkit.entity.Player;

public final class NoChecker implements IVanishChecker {

    @Override
    public boolean isVanished(final Player player) {
        return false;
    }
}
