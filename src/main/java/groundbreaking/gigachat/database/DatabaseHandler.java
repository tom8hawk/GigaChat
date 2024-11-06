package groundbreaking.gigachat.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import groundbreaking.gigachat.GigaChat;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ForkJoinPool;

public final class DatabaseHandler {

    public static final ForkJoinPool customThreadPool = new ForkJoinPool(3);

    private static HikariDataSource dataSource;

    private DatabaseHandler() {

    }

    public static void createConnection(final GigaChat plugin) {
        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(getDriverUrl(plugin));
        hikariConfig.setMaximumPoolSize(8);
        hikariConfig.setMinimumIdle(4);
        hikariConfig.setConnectionTimeout(10000);
        hikariConfig.setIdleTimeout(600000);
        hikariConfig.setMaxLifetime(1800000);

        dataSource = new HikariDataSource(hikariConfig);
    }

    private static String getDriverUrl(final GigaChat plugin) {
        final File dbFile = new File(plugin.getDataFolder() + File.separator + "database.db");
        return "jdbc:sqlite:" + dbFile;
    }

    static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void closeConnection() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
