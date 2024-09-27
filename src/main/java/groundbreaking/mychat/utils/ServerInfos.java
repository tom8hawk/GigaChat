package groundbreaking.mychat.utils;

import groundbreaking.mychat.MyChat;
import lombok.Getter;
import org.bukkit.Bukkit;

public class ServerInfos {

    @Getter
    private final boolean isPapiExist = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;

    @Getter
    private final int subVersion;

    @Getter
    private final boolean isAbove16;

    @Getter
    private final boolean isPaperOrFork = checkIsPaperOrFork();

    public ServerInfos(MyChat plugin) {
        subVersion = extractMainVersion(plugin);
        isAbove16 = subVersion >= 16;
    }

    public int extractMainVersion(MyChat plugin) {
        try {
            return Integer.parseInt(plugin.getServer().getMinecraftVersion().split("\\.", 3)[1]);
        } catch (NumberFormatException ex) {
            plugin.getLogger().warning("\u001b[32mFailed to extract server version. Plugin may not work correctly!");
            return 0;
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
