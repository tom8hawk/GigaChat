package groundbreaking.gigachat.utils.counter;

import org.bukkit.entity.Player;

public final class FirstEntryCounter implements ICounter {

    @Override
    public long count(final Player player) {
        return (System.currentTimeMillis() - player.getFirstPlayed()) / 1000;
    }
}
