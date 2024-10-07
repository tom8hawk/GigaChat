package groundbreaking.gigachat.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DatabaseQueries {

    public static void createTables() {
        final String disabledChatQuery = """
                    CREATE TABLE IF NOT EXISTS disabledChat (
                        username TEXT NOT NULL UNIQUE
                    );
                """;

        final String disabledPrivateMessagesQuery = """
                    CREATE TABLE IF NOT EXISTS disabledPrivateMessages (
                        username TEXT NOT NULL UNIQUE
                    );
                """;

        final String ignoreChatQuery = """
                    CREATE TABLE IF NOT EXISTS ignoreChat (
                        username TEXT NOT NULL UNIQUE,
                        ignored TEXT NOT NULL
                    );
                """;

        final String ignorePrivateQuery = """
                    CREATE TABLE IF NOT EXISTS ignorePrivate (
                        username TEXT NOT NULL UNIQUE,
                        ignored TEXT NOT NULL
                    );
                """;

        final String localSpyQuery = """
                    CREATE TABLE IF NOT EXISTS localSpy (
                        username TEXT NOT NULL UNIQUE
                    );
                """;

        final String privateMessagesSoundsQuery = """
                    CREATE TABLE IF NOT EXISTS privateMessagesSounds (
                        username TEXT NOT NULL UNIQUE,
                        sound TEXT NOT NULL
                    );
                """;

        final String socialSpyQuery = """
                    CREATE TABLE IF NOT EXISTS socialSpy (
                        username TEXT NOT NULL UNIQUE
                    );
                """;

        try (final Statement statement = DatabaseHandler.getConnection().createStatement()) {
            statement.execute(disabledChatQuery);
            statement.execute(disabledPrivateMessagesQuery);
            statement.execute(ignoreChatQuery);
            statement.execute(ignorePrivateQuery);
            statement.execute(localSpyQuery);
            statement.execute(privateMessagesSoundsQuery);
            statement.execute(socialSpyQuery);
        } catch(SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void addPlayerToDisabledChat(final String username) {
        final String query = "INSERT OR IGNORE INTO disabledChat(username) VALUES(?)";

        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            statement.executeUpdate();
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void removePlayerFromDisabledChat(final String username) {
        final String query = "DELETE FROM disabledChat WHERE username = ?";

        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            statement.executeUpdate();
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static boolean disabledChatContainsPlayer(final String username) {
        final String query = "SELECT * FROM disabledChat WHERE username = ?";

        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            final ResultSet result = statement.executeQuery();
            return result.next();
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public static void addPlayerToIgnoreChat(final String username, final List<String> ignored) {
        final String query = "INSERT OR IGNORE INTO ignoreChat(username, ignored) VALUES(?, ?)";

        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, String.join(";", ignored));
            statement.executeUpdate();
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void removePlayerFromIgnoreChat(final String username) {
        final String query = "DELETE FROM ignoreChat WHERE username = ?";

        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            statement.executeUpdate();
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static boolean ignoreChatContains(final String username) {
        final String query = "SELECT * FROM ignoreChat WHERE username = ?";

        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            final ResultSet result = statement.executeQuery();
            return result.next();
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public static List<String> getIgnoredChat(final String username) {
        final String query = "SELECT ignored FROM ignoreChat WHERE username = ?";

        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            final ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                final String listString = rs.getString("ignored");
                return Arrays.asList(listString.split(";"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return Collections.emptyList();
    }

    // disabledPrivateMessages
    public static void addPlayerToDisabledPrivateMessages(final String username) {
        final String query = "INSERT OR IGNORE INTO disabledPrivateMessages(username) VALUES(?)";

        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            statement.executeUpdate();
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void removePlayerFromDisabledPrivateMessages(final String username) {
        final String query = "DELETE FROM disabledPrivateMessages WHERE username = ?";

        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            statement.executeUpdate();
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    public static boolean disabledPrivateMessagesContainsPlayer(final String username) {
        final String query = "SELECT * FROM disabledPrivateMessages WHERE username = ?";

        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            final ResultSet result = statement.executeQuery();
            return result.next();
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    // ignorePrivate
    public static void addPlayerToIgnorePrivate(final String username, final List<String> ignored) {
        final String query = "INSERT OR IGNORE INTO ignorePrivate(username, ignored) VALUES(?, ?)";

        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, String.join(";", ignored));
            statement.executeUpdate();
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void removePlayerFromIgnorePrivate(final String username) {
        final String query = "DELETE FROM ignorePrivate WHERE username = ?";

        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            statement.executeUpdate();
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static boolean ignorePrivateContainsPlayer(final String username) {
        final String query = "SELECT * FROM ignorePrivate WHERE username = ?";

        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            final ResultSet result = statement.executeQuery();
            return result.next();
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public static List<String> getIgnoredPrivate(final String username) {
        final String query = "SELECT ignored FROM ignorePrivate WHERE username = ?";

        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            final ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                final String listString = rs.getString("ignored");
                return Arrays.asList(listString.split(";"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return Collections.emptyList();
    }

    // localSpy
    public static void addPlayerToLocalSpy(final String username) {
        final String query = "INSERT OR IGNORE INTO localSpy(username) VALUES(?)";

        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            statement.executeUpdate();
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void removePlayerFromLocalSpy(final String username) {
        final String query = "DELETE FROM localSpy WHERE username = ?";

        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            statement.executeUpdate();
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static boolean localSpyContainsPlayer(final String username) {
        final String query = "SELECT * FROM localSpy WHERE username = ?";

        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            final ResultSet result = statement.executeQuery();
            return result.next();
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    // privateMessagesSounds
    public static void addPlayerPmSoundToPmSounds(final String username, final String sound) {
        final String query = "INSERT OR IGNORE INTO privateMessagesSounds(username, sound) VALUES(?, ?)";

        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, sound);
            statement.executeUpdate();
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void removePlayerFromPmSounds(final String username) {
        final String query = "DELETE FROM privateMessagesSounds WHERE username = ?";

        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            statement.executeUpdate();
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static String getSound(final String username) {
        final String query = "SELECT sound FROM privateMessagesSounds WHERE username = ?";

        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            final ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return rs.getString("sound");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    // socialSpy
    public static void addPlayerToSocialSpy(final String username) {
        final String query = "INSERT OR IGNORE INTO socialSpy(username) VALUES(?)";

        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            statement.executeUpdate();
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void removePlayerFromSocialSpy(final String username) {
        final String query = "DELETE FROM socialSpy WHERE username = ?";

        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            statement.executeUpdate();
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static boolean socialSpyContainsPlayer(final String username) {
        final String query = "SELECT * FROM socialSpy WHERE username = ?";

        try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            final ResultSet result = statement.executeQuery();
            return result.next();
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }
}
