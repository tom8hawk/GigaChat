package groundbreaking.gigachat.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class DatabaseQueries {

    /**
     * Creates tables in the database if they do not already exist
     * If the tables are already present, no changes will be made
     */
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

        final String autoMessagesQuery = """
                    CREATE TABLE IF NOT EXISTS autoMessages (
                        username TEXT NOT NULL UNIQUE
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
     * @param username of the player
     */
    public static void addPlayerToDisabledChat(final String username) {
        CompletableFuture.supplyAsync(() -> {
            final String query = "INSERT OR IGNORE INTO disabledChat(username) VALUES(?)";
            try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
                statement.setString(1, username);
                statement.executeUpdate();
            } catch (final SQLException ex) {
                ex.printStackTrace();
            }

            return null;
        }, DatabaseHandler.customThreadPool).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    /**
     * Removes the player's name from the "disabledChat" table
     *
     * @param username of the player
     */
    public static void removePlayerFromDisabledChat(final String username) {
        CompletableFuture.supplyAsync(() -> {
            final String query = "DELETE FROM disabledChat WHERE username = ? LIMIT 1";
            try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
                statement.setString(1, username);
                statement.executeUpdate();
            } catch (final SQLException ex) {
                ex.printStackTrace();
            }

            return null;
        }, DatabaseHandler.customThreadPool).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    /**
     * Checks the "disabledChat" table for a player's name, to see if he has chat disabled.
     *
     * @param username of the player
     * @return true if the table contains the player's name
     */
    public static CompletableFuture<Boolean> disabledChatContainsPlayer(final String username) {
        return CompletableFuture.supplyAsync(() -> {
            final String query = "SELECT 1 FROM disabledChat WHERE username = ? LIMIT 1";
            try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
                statement.setString(1, username);

                final ResultSet result = statement.executeQuery();
                return result.next();
            } catch (final SQLException ex) {
                ex.printStackTrace();
                return false;
            }
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return false;
        });
    }

    /**
     * Adds the player name to the "ignoreChat" table, to save the player's choice
     *
     * @param username of the player
     */
    public static void addPlayerToIgnoreChat(final String username, final List<String> ignored) {
        CompletableFuture.supplyAsync(() -> {
            final String query = """
                    INSERT INTO ignoreChat(username, ignored)
                    VALUES(?, ?)
                    ON CONFLICT(username) DO UPDATE SET sound = excluded.ignored;
                """;
            try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
                statement.setString(1, username);
                statement.setString(2, String.join(";", ignored));
                statement.executeUpdate();
            } catch (final SQLException ex) {
                ex.printStackTrace();
            }

            return null;
        }, DatabaseHandler.customThreadPool).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    /**
     * Removes the player's name from the "ignoreChat" table
     *
     * @param username of the player
     */
    public static void removePlayerFromIgnoreChat(final String username) {
        CompletableFuture.supplyAsync(() -> {
            final String query = "DELETE FROM ignoreChat WHERE username = ? LIMIT 1";
            try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
                statement.setString(1, username);
                statement.executeUpdate();
            } catch (final SQLException ex) {
                ex.printStackTrace();
            }

            return null;
        }, DatabaseHandler.customThreadPool).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    /**
     * Checks the "ignoreChat" table for a player's name, to see if he ignores anybody.
     *
     * @param username of the player
     * @return true if the table contains the player's name
     */
    public static CompletableFuture<Boolean> ignoreChatContains(final String username) {
        return CompletableFuture.supplyAsync(() -> {
            final String query = "SELECT 1 FROM ignoreChat WHERE username = ? LIMIT 1";
            try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
                statement.setString(1, username);

                final ResultSet result = statement.executeQuery();
                return result.next();
            } catch (final SQLException ex) {
                ex.printStackTrace();
                return false;
            }
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return false;
        });
    }

    /**
     * Checks the "ignoreChat" table for a player's name, to see if he ignores anybody.
     *
     * @param username of the player
     * @return a list of ignored players' names
     */
    public static CompletableFuture<List<String>> getIgnoredChat(final String username) {
        return CompletableFuture.supplyAsync(() -> {
            final String query = "SELECT ignored FROM ignoreChat WHERE username = ? LIMIT 1";
            try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
                statement.setString(1, username);

                final ResultSet result = statement.executeQuery();
                if (result.next()) {
                    final String listString = result.getString("ignored");
                    return Arrays.asList(listString.split(";"));
                }
            } catch (final SQLException ex) {
                ex.printStackTrace();
            }

            return null;
        }, DatabaseHandler.customThreadPool).exceptionally(ex -> {
            ex.printStackTrace();
            return Collections.emptyList();
        });
    }

    /**
     * Adds the player name to the "disabledPrivateMessages" table, to save the player's choice
     *
     * @param username of the player
     */
    public static void addPlayerToDisabledPrivateMessages(final String username) {
        CompletableFuture.supplyAsync(() -> {
            final String query = "INSERT OR IGNORE INTO disabledPrivateMessages(username) VALUES(?)";
            try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
                statement.setString(1, username);
                statement.executeUpdate();
            } catch (final SQLException ex) {
                ex.printStackTrace();
            }

            return null;
        }, DatabaseHandler.customThreadPool).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    /**
     * Removes the player's name from the "disabledPrivateMessages" table
     *
     * @param username of the player
     */
    public static void removePlayerFromDisabledPrivateMessages(final String username) {
        CompletableFuture.supplyAsync(() -> {
            final String query = "DELETE FROM disabledPrivateMessages WHERE username = ? LIMIT 1";
            try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
                statement.setString(1, username);
                statement.executeUpdate();
            } catch (final SQLException ex) {
                ex.printStackTrace();
            }

            return null;
        }, DatabaseHandler.customThreadPool).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    /**
     * Checks the "disabledPrivateMessages" table for a player's name, to see if he has private messages disabled.
     *
     * @param username of the player
     * @return true if the player has disabled
     */
    public static CompletableFuture<Boolean> disabledPrivateMessagesContainsPlayer(final String username) {
        return CompletableFuture.supplyAsync(() -> {
            final String query = "SELECT 1 FROM disabledPrivateMessages WHERE username = ? LIMIT 1";
            try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
                statement.setString(1, username);

                final ResultSet result = statement.executeQuery();
                return result.next();
            } catch (final SQLException ex) {
                ex.printStackTrace();
                return false;
            }
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return false;
        });
    }

    /**
     * Adds the player name to the "ignorePrivate" table, to save the player's choice
     *
     * @param username of the player
     */
    public static void addPlayerToIgnorePrivate(final String username, final List<String> ignored) {
        CompletableFuture.supplyAsync(() -> {
            final String query = """
                    INSERT INTO ignorePrivate(username, ignored)
                    VALUES(?, ?)
                    ON CONFLICT(username) DO UPDATE SET sound = excluded.ignored;
                """;
            try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
                statement.setString(1, username);
                statement.setString(2, String.join(";", ignored));
                statement.executeUpdate();
            } catch (final SQLException ex) {
                ex.printStackTrace();
            }

            return null;
        }, DatabaseHandler.customThreadPool).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    /**
     * Removes the player's name from the "ignorePrivate" table
     *
     * @param username of the player
     */
    public static void removePlayerFromIgnorePrivate(final String username) {
        CompletableFuture.supplyAsync(() -> {
            final String query = "DELETE FROM ignorePrivate WHERE username = ? LIMIT 1";
            try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
                statement.setString(1, username);
                statement.executeUpdate();
            } catch (final SQLException ex) {
                ex.printStackTrace();
            }

            return null;
        }, DatabaseHandler.customThreadPool).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    /**
     * Checks the "ignorePrivate" table for a player's name, to see if he ignores anybody.
     *
     * @param username of the player
     * @return true if the player is ignoring someone
     */
    public static CompletableFuture<Boolean> ignorePrivateContainsPlayer(final String username) {
        return CompletableFuture.supplyAsync(() -> {
            final String query = "SELECT 1 FROM ignorePrivate WHERE username = ? LIMIT 1";
            try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
                statement.setString(1, username);

                final ResultSet result = statement.executeQuery();
                return result.next();
            } catch (final SQLException ex) {
                ex.printStackTrace();
                return false;
            }
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return false;
        });
    }

    /**
     * Retrieves all players ignored by the player from the "ignorePrivate" table.
     *
     * @param username of the player
     * @return a list of ignored players' names
     */
    public static CompletableFuture<List<String>> getIgnoredPrivate(final String username) {
        return CompletableFuture.supplyAsync(() -> {
            final String query = "SELECT ignored FROM ignorePrivate WHERE username = ? LIMIT 1";
            try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
                statement.setString(1, username);

                final ResultSet result = statement.executeQuery();
                if (result.next()) {
                    final String listString = result.getString("ignored");
                    return Arrays.asList(listString.split(";"));
                }
            } catch (final SQLException ex) {
                ex.printStackTrace();
            }

            return null;
        }, DatabaseHandler.customThreadPool).exceptionally(ex -> {
            ex.printStackTrace();
            return Collections.emptyList();
        });
    }

    /**
     * Adds the player name to the "privateMessagesSounds" table, to save the player's choice
     *
     * @param username of the player
     */
    public static void addPlayerPmSoundToPmSounds(final String username, final String sound) {
        CompletableFuture.supplyAsync(() -> {
            final String query = """
                    INSERT INTO privateMessagesSounds(username, sound)
                    VALUES(?, ?)
                    ON CONFLICT(username) DO UPDATE SET sound = excluded.sound;
                """;
            try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
                statement.setString(1, username);
                statement.setString(2, sound);
                statement.executeUpdate();
            } catch (final SQLException ex) {
                ex.printStackTrace();
            }
            return null;
        }, DatabaseHandler.customThreadPool).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    /**
     * Removes the player's name from the "privateMessagesSounds" table
     *
     * @param username of the player
     */
    public static void removePlayerFromPmSounds(final String username) {
        CompletableFuture.supplyAsync(() -> {
            final String query = "DELETE FROM privateMessagesSounds WHERE username = ? LIMIT 1";
            try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
                statement.setString(1, username);
                statement.executeUpdate();
            } catch (final SQLException ex) {
                ex.printStackTrace();
            }
            return null;
        }, DatabaseHandler.customThreadPool).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    /**
     * Checks the "privateMessagesSounds" table for the player's name.
     *
     * @param username of the player
     * @return true if the table contains the player's name
     */
    public static CompletableFuture<Boolean> privateMessagesSoundsContainsPlayer(final String username) {
        return CompletableFuture.supplyAsync(() -> {
            final String query = "SELECT 1 FROM privateMessagesSounds WHERE username = ? LIMIT 1";
            try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
                statement.setString(1, username);

                final ResultSet result = statement.executeQuery();
                return result.next();
            } catch (final SQLException ex) {
                ex.printStackTrace();
                return false;
            }
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return false;
        });
    }

    /**
     * Retrieves the sound selected by player from the "privateMessagesSounds" table.
     *
     * @param username of the player
     * @return name of the sound player has chosen
     */
    public static CompletableFuture<String> getSound(final String username) {
        return CompletableFuture.supplyAsync(() -> {
            final String query = "SELECT sound FROM privateMessagesSounds WHERE username = ? LIMIT 1";
            try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
                statement.setString(1, username);

                final ResultSet result = statement.executeQuery();
                if (result.next()) {
                    return result.getString("sound");
                }
            } catch (final SQLException ex) {
                ex.printStackTrace();
            }
            return null;
        }, DatabaseHandler.customThreadPool).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    /**
     * Adds the player name to the "socialSpy" table, to save the player's choice
     *
     * @param username of the player
     */
    public static void addPlayerToSocialSpy(final String username) {
        CompletableFuture.supplyAsync(() -> {
            final String query = "INSERT OR IGNORE INTO socialSpy(username) VALUES(?)";
            try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
                statement.setString(1, username);
                statement.executeUpdate();
            } catch (final SQLException ex) {
                ex.printStackTrace();
            }
            return null;
        }, DatabaseHandler.customThreadPool).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    /**
     * Removes the player's name from the "socialSpy" table
     *
     * @param username of the player
     */
    public static void removePlayerFromSocialSpy(final String username) {
        CompletableFuture.supplyAsync(() -> {
            final String query = "DELETE FROM socialSpy WHERE username = ? LIMIT 1";
            try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
                statement.setString(1, username);
                statement.executeUpdate();
            } catch (final SQLException ex) {
                ex.printStackTrace();
            }
            return null;
        }, DatabaseHandler.customThreadPool).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    /**
     * Checks the "localSpy" table for a player's name, to see if he is spying on the private messages.
     *
     * @param username of the player
     * @return true if the table contains the player's name
     */
    public static CompletableFuture<Boolean> socialSpyContainsPlayer(final String username) {
        return CompletableFuture.supplyAsync(() -> {
            final String query = "SELECT 1 FROM socialSpy WHERE username = ? LIMIT 1";
            try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
                statement.setString(1, username);

                final ResultSet result = statement.executeQuery();
                return result.next();
            } catch (final SQLException ex) {
                ex.printStackTrace();
                return false;
            }
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return false;
        });
    }

    public static void addPlayerToAutoMessages(final String name) {
        CompletableFuture.supplyAsync(() -> {
            final String query = "INSERT OR IGNORE autoMessages(name) VALUES(?)";
            try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
                statement.setString(1, name);
                statement.execute();
            } catch (final SQLException ex) {
                ex.printStackTrace();
            }
            return null;
        }, DatabaseHandler.customThreadPool).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    public static void removePlayerFromAutoMessages(final String name) {
        CompletableFuture.supplyAsync(() -> {
            final String query = "DELETE FROM autoMessages WHERE username = ? LIMIT 1";
            try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
                statement.setString(1, name);
                statement.executeUpdate();
            } catch (final SQLException ex) {
                ex.printStackTrace();
            }
            return null;
        }, DatabaseHandler.customThreadPool).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    public static CompletableFuture<Boolean> containsPlayerFromAutoMessages(final String name) {
        return CompletableFuture.supplyAsync(() -> {
            final String query = "SELECT 1 FROM autoMessages WHERE username = ? LIMIT 1";
            try (final PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query)) {
                statement.setString(1, name);

                final ResultSet resultSet = statement.executeQuery();
                return resultSet.next();
            } catch (final SQLException ex) {
                ex.printStackTrace();
                return false;
            }
        }, DatabaseHandler.customThreadPool).exceptionally(ex -> {
            ex.printStackTrace();
            return false;
        });
    }
}
