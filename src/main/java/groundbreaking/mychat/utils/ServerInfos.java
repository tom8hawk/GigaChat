package groundbreaking.mychat.utils;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Server;

import java.util.logging.Logger;

public class ServerInfos {

    @Getter
    private final boolean isPapiExist = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;

    @Getter
    private final int subVersion = extractMainVersion();

    @Getter
    private final boolean isAbove16 = subVersion >= 16;

    @Getter
    private final boolean isPaperOrFork = checkIsPaperOrFork();


    private final Server server;
    private final Logger logger;

    public ServerInfos(Server server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    public int extractMainVersion() {
        try {
            return Integer.parseInt(server.getMinecraftVersion().split("\\.", 3)[1]);
        } catch (NumberFormatException ex) {
            logger.warning("\u001b[32mFailed to extract server version. Plugin may not work correctly!");
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
