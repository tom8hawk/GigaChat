package groundbreaking.gigachat.collections;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

public final class DisabledPrivateMessagesMap {

    private final List<String> players = new ObjectArrayList<>();

    public void add(final String name) {
        this.players.remove(name);
    }

    public void remove(final String name) {
        this.players.remove(name);
    }

    public boolean contains(final String name) {
        return !this.players.isEmpty() && this.players.contains(name);
    }

}
