package groundbreaking.gigachat.database;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Sound;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class DatabaseQueries {

    private DatabaseQueries() {

    }

    /**
     * Adds the player name to the "disabledChat" table, to save the player's choice
     */
    public static final String ADD_PLAYER_TO_DISABLED_CHAT = "INSERT OR IGNORE INTO disabledChat(playerUUID) VALUES(?);";

    /**
     * Removes the player's name from the "disabledChat" table
     */
    public static final String REMOVE_PLAYER_FROM_DISABLED_CHAT = "DELETE FROM disabledChat WHERE playerUUID = ?;";

    /**
     * Checks the "disabledChat" table for a player's name, to see if he has chat disabled.
     */
    public static final String DISABLED_CHAT_CONTAINS_PLAYER = "SELECT EXISTS(SELECT 1 FROM disabledChat WHERE playerUUID = ?);";

    /**
     * Adds the player name to the "ignoreChat" table, to save the player's choice
     *
     * @param playerUUID UUID of the player
     */
    public static final String ADD_PLAYER_TO_IGNORE_CHAT = "INSERT OR IGNORE INTO ignoreChat(playerUUID, ignored) VALUES(?, ?);";

    /**
     * Removes the player's name from the "ignoreChat" table
     */
    public static final String REMOVE_PLAYER_FROM_IGNORE_CHAT = "DELETE FROM ignoreChat WHERE playerUUID = ?;";

    /**
     * Removes the player's name from the "ignoreChat" table
     */
    public static final String REMOVE_IGNORED_FROM_IGNORE_CHAT = "DELETE FROM ignoreChat WHERE playerUUID = ? and ignoredUUID = ?;";

    /**
     * Checks the "ignoreChat" table for a player's name, to see if he ignores anybody.
     */
    public static final String GET_IGNORED_PLAYERS_FROM_CHAT = "SELECT ignoredUUID FROM ignoreChat WHERE playerUUID = ?;";

    /**
     * Adds the player name to the "disabledPrivateMessages" table, to save the player's choice
     */
    public static final String ADD_PLAYER_TO_DISABLED_PRIVATE_MESSAGES = "INSERT OR IGNORE INTO disabledPrivateMessages(playerUUID) VALUES(?);";

    /**
     * Removes the player's name from the "disabledPrivateMessages" table
     */
    public static final String REMOVE_PLAYER_FROM_DISABLED_PRIVATE_MESSAGES = "DELETE FROM disabledPrivateMessages WHERE playerUUID = ?;";

    /**
     * Checks the "disabledPrivateMessages" table for a player's name, to see if he has private messages disabled.
     */
    public static final String CHECK_IF_PLAYER_DISABLED_PRIVATE_MESSAGES = "SELECT EXISTS(SELECT 1 FROM disabledPrivateMessages WHERE playerUUID = ?);";

    /**
     * Adds the player name to the "ignorePrivate" table, to save the player's choice
     */
    public static final String ADD_PLAYER_TO_IGNORE_PRIVATE = "SELECT EXISTS(SELECT 1 FROM disabledPrivateMessages WHERE playerUUID = ?);";

    /**
     * Removes the player's name from the "ignorePrivate" table
     */
    public static final String REMOVE_PLAYER_FROM_IGNORE_PRIVATE_PRIVATE = "DELETE FROM ignorePrivate WHERE playerUUID = ?;";

    /**
     * Removes the player's name from the "ignorePrivate" table
     */
    public static final String REMOVE_IGNORED_PLAYER_FROM_IGNORE_PRIVATE = "DELETE FROM ignorePrivate WHERE playerUUID = ? and ignoredUUID = ?;";

    /**
     * Retrieves all players ignored by the player from the "ignorePrivate" table.
     */
    public static final String GET_IGNORED_PRIVATE = "SELECT ignoredUUID FROM ignorePrivate WHERE playerUUID = ?;";

    /**
     * Adds the player name to the "privateMessagesSounds" table, to save the player's choice
     */
    public static final String ADD_PLAYER_PM_SOUND_TO_PRIVATE_MESSAGES_SOUNDS = """
                INSERT INTO privateMessagesSounds(playerUUID, soundName)
                VALUES(?, ?)
                ON CONFLICT(playerUUID) DO UPDATE SET soundName = excluded.soundName;
            """;

    /**
     * Removes the player's name from the "privateMessagesSounds" table
     */
    public static final String REMOVE_PLAYER_FROM_PRIVATE_MESSAGES_SOUNDS = "DELETE FROM privateMessagesSounds WHERE playerUUID = ?;";

    /**
     * Adds the player name to the "socialSpy" table, to save the player's choice
     */
    public static final String ADD_PLAYER_TO_SOCIAL_SPY = "INSERT OR IGNORE INTO socialSpy(playerUUID) VALUES(?);";

    /**
     * Removes the player's name from the "socialSpy" table
     */
    public static final String REMOVE_PLAYER_FROM_SOCIAL_SPY = "DELETE FROM socialSpy WHERE playerUUID = ?;";

    /**
     * Checks the "localSpy" table for a player's name, to see if he is spying on the private messages.
     */
    public static final String CHECK_IF_PLAYER_ENABLED_SOCIAL_SPY = "SELECT EXISTS(SELECT 1 FROM socialSpy WHERE playerUUID = ?);";

    /**
     * Adds the player name to the "autoMessages" table, to save the player's choice.
     */
    public static final String ADD_PLAYER_TO_AUTO_MESSAGES = "INSERT OR IGNORE INTO autoMessages(playerUUID) VALUES(?);";

    /**
     * Removes the player's name from the "autoMessages" table.
     */
    public static final String REMOVE_PLAYER_FROM_AUTO_MESSAGES = "DELETE FROM autoMessages WHERE playerUUID = ?;";

    /**
     * Checks the "autoMessages" table for a player's name, to see if he has disabled auto messages.
     */
    public static final String CHECK_IF_PLAYER_ENABLED_AUTO_MESSAGES = "SELECT EXISTS(SELECT 1 FROM autoMessages WHERE playerUUID = ?);";

    /**
     * Adds the player name to the "chatListeners" table, to save the player's choice.
     */
    public static final String ADD_PLAYER_TO_CHAT_LISTENERS = "INSERT OR IGNORE INTO chatListeners(playerUUID, chatName) VALUES(?, ?);";

    /**
     * Removes the chat for the player from the "chatListeners" table.
     */
    public static final String REMOVE_CHAT_FOR_PLAYER_FROM_CHATS_LISTENERS = "DELETE FROM chatListeners WHERE playerUUID = ? and chatName = ?;";

    /**
     * Creates tables in the database if they do not already exist
     * If the tables are already present, no changes will be made
     */
    public static void createDatabaseTables() {
        final String[] queries = {
                "CREATE TABLE IF NOT EXISTS disabledChat(playerUUID TEXT NOT NULL UNIQUE);",
                "CREATE TABLE IF NOT EXISTS disabledPrivateMessages(playerUUID TEXT NOT NULL UNIQUE);",
                "CREATE TABLE IF NOT EXISTS ignoreChat(playerUUID TEXT NOT NULL, ignoredUUID TEXT NOT NULL, PRIMARY KEY(playerUUID, ignoredUUID));",
                "CREATE TABLE IF NOT EXISTS ignorePrivate(playerUUID TEXT NOT NULL, ignoredUUID TEXT NOT NULL, PRIMARY KEY(playerUUID, ignoredUUID));",
                "CREATE TABLE IF NOT EXISTS privateMessagesSounds(playerUUID TEXT NOT NULL UNIQUE, soundName TEXT NOT NULL);",
                "CREATE TABLE IF NOT EXISTS socialSpy(playerUUID TEXT NOT NULL UNIQUE);",
                "CREATE TABLE IF NOT EXISTS autoMessages(playerUUID TEXT NOT NULL UNIQUE);",
                "CREATE TABLE IF NOT EXISTS chatListeners (playerUUID TEXT NOT NULL, chatName TEXT NOT NULL, PRIMARY KEY(playerUUID, chatName));"
        };

        try (final Connection connection = DatabaseHandler.getConnection(); final Statement statement = connection.createStatement()) {
            for (int i = 0; i < queries.length; i++) {
                statement.execute(queries[i]);
            }
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Executes the specified query.
     *
     * @param query      query to execute
     * @param connection opened connection
     * @param strings    params to set
     */
    public static void executeUpdateQuery(final String query, final Connection connection, final String... strings) throws SQLException {
        connection.setAutoCommit(false);
        try (final PreparedStatement statement = connection.prepareStatement(query)) {
            int i = 0;
            for (final String string : strings) {
                statement.setString(++i, string);
            }
            statement.executeUpdate();
            connection.commit();
        } catch (final SQLException ex) {
            connection.rollback();
            throw ex;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    /**
     * Executes the specified query and returns bo.
     *
     * @param query      query to execute
     * @param connection opened connection
     * @param playerUUID UUID of the player
     * @return true if the player contains in table
     */
    public static boolean containsPlayerInTable(final String query, final Connection connection, final UUID playerUUID) throws SQLException {
        try (final PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setObject(1, playerUUID);
            try (final ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public static List<UUID> getListOfIgnoredPlayers(final String query, final Connection connection, final UUID playerUUID) throws SQLException {
        final List<UUID> ignoredPlayers = new ArrayList<>();
        try (final PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, playerUUID.toString());
            try (final ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    final String string = result.getString("ignoredUUID");
                    final UUID ignoredUUID = UUID.fromString(string);
                    ignoredPlayers.add(ignoredUUID);
                }
            }
        }

        return ignoredPlayers;
    }

    /**
     * Retrieves the sound selected by player from the "privateMessagesSounds" table.
     *
     * @param playerUUID UUID of the player
     * @return name of the sound player has chosen
     */
    public static Sound getPlayerSelectedSound(final Connection connection, final UUID playerUUID) throws SQLException {
        final String query = "SELECT soundName FROM privateMessagesSounds WHERE playerUUID = ?;";
        try (final PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, playerUUID.toString());
            try (final ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    final String string = result.getString("soundName");
                    return Sound.valueOf(string);
                }
            }
        }

        return null;
    }

    /**
     * Returns the list from "chatListeners" table with chat names of chats where a player has spy mode enabled.
     *
     * @param playerUUID UUID of the player
     * @return list with the chat names
     */
    public static List<String> getChatsWherePlayerIsListening(final Connection connection, final UUID playerUUID) throws SQLException {
        final String query = "SELECT chatName FROM chatListeners WHERE playerUUID = ?;";
        final List<String> chats = new ObjectArrayList<>();
        try (final PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, playerUUID.toString());
            try (final ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    final String chatName = result.getString("chatName");
                    chats.add(chatName);
                }
            }
        }

        return chats;
    }
}
