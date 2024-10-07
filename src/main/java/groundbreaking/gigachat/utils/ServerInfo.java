package groundbreaking.gigachat.utils;

import groundbreaking.gigachat.GigaChat;
import lombok.Getter;
import org.bukkit.Bukkit;

public final class ServerInfo {

    @Getter
    private final boolean isPapiExist = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;

    @Getter
    private final boolean isPaperOrFork = checkIsPaperOrFork();

    public boolean is16OrAbove(final GigaChat plugin) {
        try {
            return Integer.parseInt(plugin.getServer().getMinecraftVersion().split("\\.", 3)[1]) >= 16;
        } catch (NumberFormatException ex) {
            plugin.getLogger().warning("\u001b[32mFailed to extract server version. Plugin may not work correctly!");
            return false;
        }
    }

    public boolean checkIsPaperOrFork() {
        try {
            Class.forName("com.destroystokyo.paper.utils.PaperPluginLogger");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
