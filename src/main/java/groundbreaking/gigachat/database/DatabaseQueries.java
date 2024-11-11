package groundbreaking.gigachat.database;

import org.bukkit.Sound;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class DatabaseQueries {

    private DatabaseQueries() {

    }

    /**
     * Creates tables in the database if they do not already exist
     * If the tables are already present, no changes will be made
     */
    public static void createTables() {
        final String disabledChatQuery = """
                    CREATE TABLE IF NOT EXISTS disabledChat (
                        playerUUID TEXT NOT NULL UNIQUE
                    );
                """;

        final String disabledPrivateMessagesQuery = """
                    CREATE TABLE IF NOT EXISTS disabledPrivateMessages (
                        playerUUID TEXT NOT NULL UNIQUE
                    );
                """;

        final String ignoreChatQuery = """
                    CREATE TABLE IF NOT EXISTS ignoreChat (
                        playerUUID TEXT NOT NULL UNIQUE,
                        ignoredUUID TEXT NOT NULL,
                        PRIMARY KEY(playerUUID, ignoredUUID)
                    );
                """;

        final String ignorePrivateQuery = """
                    CREATE TABLE IF NOT EXISTS ignorePrivate (
                        playerUUID TEXT NOT NULL UNIQUE,
                        ignoredUUID TEXT NOT NULL,
                        PRIMARY KEY(playerUUID, ignoredUUID)
                    );
                """;

        final String privateMessagesSoundsQuery = """
                    CREATE TABLE IF NOT EXISTS privateMessagesSounds (
                        playerUUID TEXT NOT NULL UNIQUE,
                        soundName TEXT NOT NULL
                    );
                """;

        final String socialSpyQuery = """
                    CREATE TABLE IF NOT EXISTS socialSpy (
                        playerUUID TEXT NOT NULL UNIQUE
                    );
                """;

        final String autoMessagesQuery = """
                    CREATE TABLE IF NOT EXISTS autoMessages (
                        playerUUID TEXT NOT NULL UNIQUE
                    );
                """;

        try (final Statement statement = DatabaseHandler.getConnection().createStatement()) {
            statement.execute(disabledChatQuery);
            statement.execute(disabledPrivateMessagesQuery);
            statement.execute(ignoreChatQuery);
            statement.execute(ignorePrivateQuery);
            statement.execute(privateMessagesSoundsQuery);
            statement.execute(socialSpyQuery);
            statement.execute(autoMessagesQuery);
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Adds the player name to the "disabledChat" table, to save the player's choice
     *
     * @param playerUUID UUID of the player
     */
    public static void addPlayerToDisabledChat(final UUID playerUUID) {
        final String query = "INSERT OR IGNORE INTO disabledChat(playerUUID) VALUES(?);";
        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, playerUUID.toString());
            statement.executeUpdate();
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Removes the player's name from the "disabledChat" table
     *
     * @param playerUUID UUID of the player
     */
    public static void removePlayerFromDisabledChat(final UUID playerUUID) {
        final String query = "DELETE FROM disabledChat WHERE playerUUID = ?;";
        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, playerUUID.toString());
            statement.executeUpdate();
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Checks the "disabledChat" table for a player's name, to see if he has chat disabled.
     *
     * @param playerUUID UUID of the player
     * @return true if the table contains the player's name
     */
    public static boolean disabledChatContainsPlayer(final UUID playerUUID) {
        final String query = "SELECT EXISTS(SELECT 1 FROM disabledChat WHERE playerUUID = ?);";
        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, playerUUID.toString());

            final ResultSet result = statement.executeQuery();
            return result.getInt(1) == 1;
        } catch (final SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Adds the player name to the "ignoreChat" table, to save the player's choice
     *
     * @param playerUUID UUID of the player
     */
    public static void addPlayerToIgnoreChat(final UUID playerUUID, final UUID ignoredUUID) {
        final String query = "INSERT OR IGNORE INTO ignoreChat(playerUUID, ignored) VALUES(?, ?);";
        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, playerUUID.toString());
            statement.setString(2, ignoredUUID.toString());
            statement.executeUpdate();
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Removes the player's name from the "ignoreChat" table
     *
     * @param playerUUID UUID of the player
     */
    public static void removePlayerFromIgnoreChat(final UUID playerUUID) {
        final String query = "DELETE FROM ignoreChat WHERE playerUUID = ?;";
        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, playerUUID.toString());
            statement.executeUpdate();
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Removes the player's name from the "ignoreChat" table
     *
     * @param playerUUID UUID of the player
     */
    public static void removePlayerFromIgnoreChat(final UUID playerUUID, final UUID ignoredUUID) {
        final String query = "DELETE FROM ignoreChat WHERE playerUUID = ? and ignoredUUID = ?;";
        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, playerUUID.toString());
            statement.setString(2, ignoredUUID.toString());
            statement.executeUpdate();
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Checks the "ignoreChat" table for a player's name, to see if he ignores anybody.
     *
     * @param playerUUID UUID of the player
     * @return a list of ignored players' names
     */
    public static List<UUID> getIgnoredChat(final UUID playerUUID) {
        final String query = "SELECT ignoredUUID FROM ignoreChat WHERE playerUUID = ?;";
        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, playerUUID.toString());

            final ResultSet result = statement.executeQuery();
            final List<UUID> ignoredPlayers = new ArrayList<>();
            while (result.next()) {
                final String string = result.getString("ignoredUUID");
                final UUID ignoredUUID = UUID.fromString(string);
                ignoredPlayers.add(ignoredUUID);
            }
            return ignoredPlayers;
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }

        return Collections.emptyList();
    }

    /**
     * Adds the player name to the "disabledPrivateMessages" table, to save the player's choice
     *
     * @param playerUUID UUID of the player
     */
    public static void addPlayerToDisabledPrivateMessages(final UUID playerUUID) {
        final String query = "INSERT OR IGNORE INTO disabledPrivateMessages(playerUUID) VALUES(?);";
        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, playerUUID.toString());
            statement.executeUpdate();
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Removes the player's name from the "disabledPrivateMessages" table
     *
     * @param playerUUID UUID of the player
     */
    public static void removePlayerFromDisabledPrivateMessages(final UUID playerUUID) {
        final String query = "DELETE FROM disabledPrivateMessages WHERE playerUUID = ?;";
        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, playerUUID.toString());
            statement.executeUpdate();
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Checks the "disabledPrivateMessages" table for a player's name, to see if he has private messages disabled.
     *
     * @param playerUUID UUID of the player
     * @return true if the player has disabled
     */
    public static boolean disabledPrivateMessagesContainsPlayer(final UUID playerUUID) {
        final String query = "SELECT EXISTS(SELECT 1 FROM disabledPrivateMessages WHERE playerUUID = ?);";
        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, playerUUID.toString());

            final ResultSet result = statement.executeQuery();
            return result.getInt(1) == 1;
        } catch (final SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Adds the player name to the "ignorePrivate" table, to save the player's choice
     *
     * @param playerUUID UUID of the player
     */
    public static void addPlayerToIgnorePrivate(final UUID playerUUID, final UUID ignoredUUID) {
        final String query = "INSERT OR IGNORE INTO ignorePrivate(playerUUID, ignoredUUID) VALUES(?, ?);";
        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, playerUUID.toString());
            statement.setString(2, ignoredUUID.toString());
            statement.executeUpdate();
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Removes the player's name from the "ignorePrivate" table
     *
     * @param playerUUID UUID of the player
     */
    public static void removePlayerFromIgnorePrivate(final UUID playerUUID) {
        final String query = "DELETE FROM ignorePrivate WHERE playerUUID = ?;";
        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, playerUUID.toString());
            statement.executeUpdate();
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Removes the player's name from the "ignorePrivate" table
     *
     * @param playerUUID UUID of the player
     */
    public static void removePlayerFromIgnorePrivate(final UUID playerUUID, final UUID ignoredUUID) {
        final String query = "DELETE FROM ignorePrivate WHERE playerUUID = ? and ignoredUUID = ?;";
        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, playerUUID.toString());
            statement.setString(2, ignoredUUID.toString());
            statement.executeUpdate();
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Retrieves all players ignored by the player from the "ignorePrivate" table.
     *
     * @param playerUUID UUID of the player
     * @return a list of ignored players' names
     */
    public static List<UUID> getIgnoredPrivate(final UUID playerUUID) {
        final String query = "SELECT ignoredUUID FROM ignorePrivate WHERE playerUUID = ?;";
        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, playerUUID.toString());

            final ResultSet result = statement.executeQuery();
            final List<UUID> ignoredPlayers = new ArrayList<>();
            while (result.next()) {
                final String string = result.getString("ignoredUUID");
                final UUID ignoredUUID = UUID.fromString(string);
                ignoredPlayers.add(ignoredUUID);
            }
            return ignoredPlayers;
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }

        return Collections.emptyList();
    }

    /**
     * Adds the player name to the "privateMessagesSounds" table, to save the player's choice
     *
     * @param playerUUID UUID of the player
     */
    public static void addPlayerPmSoundToPmSounds(final UUID playerUUID, final String soundName) {
        final String query = """
                    INSERT INTO privateMessagesSounds(playerUUID, soundName)
                    VALUES(?, ?)
                    ON CONFLICT(playerUUID) DO UPDATE SET soundName = excluded.soundName;
                """;
        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, playerUUID.toString());
            statement.setString(2, soundName);
            statement.executeUpdate();
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Removes the player's name from the "privateMessagesSounds" table
     *
     * @param playerUUID UUID of the player
     */
    public static void removePlayerFromPmSounds(final UUID playerUUID) {
        final String query = "DELETE FROM privateMessagesSounds WHERE playerUUID = ?;";
        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, playerUUID.toString());
            statement.executeUpdate();
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Retrieves the sound selected by player from the "privateMessagesSounds" table.
     *
     * @param playerUUID UUID of the player
     * @return name of the sound player has chosen
     */
    public static Sound getSound(final UUID playerUUID) {
        final String query = "SELECT soundName FROM privateMessagesSounds WHERE playerUUID = ?;";
        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, playerUUID.toString());

            final ResultSet result = statement.executeQuery();
            if (result.next()) {
                final String string = result.getString("soundName");
                return string == null ? null : Sound.valueOf(string);
            }
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * Adds the player name to the "socialSpy" table, to save the player's choice
     *
     * @param playerUUID UUID of the player
     */
    public static void addPlayerToSocialSpy(final UUID playerUUID) {
        final String query = "INSERT OR IGNORE INTO socialSpy(playerUUID) VALUES(?);";
        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, playerUUID.toString());
            statement.executeUpdate();
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Removes the player's name from the "socialSpy" table
     *
     * @param playerUUID UUID of the player
     */
    public static void removePlayerFromSocialSpy(final UUID playerUUID) {
        final String query = "DELETE FROM socialSpy WHERE playerUUID = ?;";
        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, playerUUID.toString());
            statement.executeUpdate();
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Checks the "localSpy" table for a player's name, to see if he is spying on the private messages.
     *
     * @param playerUUID UUID of the player
     * @return true if the table contains the player's name
     */
    public static boolean socialSpyContainsPlayer(final UUID playerUUID) {
        final String query = "SELECT EXISTS(SELECT 1 FROM socialSpy WHERE playerUUID = ?);";
        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, playerUUID.toString());

            final ResultSet result = statement.executeQuery();
            return result.getInt(1) == 1;
        } catch (final SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static void addPlayerToAutoMessages(final UUID playerUUID) {
        final String query = "INSERT OR IGNORE INTO autoMessages(playerUUID) VALUES(?);";
        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, playerUUID.toString());
            statement.execute();
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void removePlayerFromAutoMessages(final UUID playerUUID) {
        final String query = "DELETE FROM autoMessages WHERE playerUUID = ?;";
        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, playerUUID.toString());
            statement.executeUpdate();
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static boolean containsPlayerFromAutoMessages(final UUID playerUUID) {
        final String query = "SELECT EXISTS(SELECT 1 FROM autoMessages WHERE playerUUID = ?);";
        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, playerUUID.toString());

            final ResultSet result = statement.executeQuery();
            return result.getInt(1) == 1;
        } catch (final SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
