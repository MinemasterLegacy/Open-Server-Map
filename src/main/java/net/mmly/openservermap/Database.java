package net.mmly.openservermap;

import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;

public class Database {

    private static Connection connection;
    private static boolean connectionSuccessful;

    private static Connection connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            return null;
        }

        deleteDatabaseIfNotPersistent();

        var sql = "CREATE TABLE IF NOT EXISTS players (" +
                "	uuid text PRIMARY KEY," +
                "   visible BOOL NOT NULL" +
                ");";

        Connection connection = DriverManager.getConnection("jdbc:sqlite:plugins/OpenServerMap/hiddenplayers.db");
        Statement statement = connection.createStatement();
        statement.execute(sql);

        return connection;
    }

    public static void establishConnection() {
        try {
            connection = connect();
            connectionSuccessful = true;
        } catch (SQLException e) {
            OpenServerMap.log(Level.SEVERE, "Database failed to open, player visibility changes will not work: " + e.getMessage());
            connection = null;
            connectionSuccessful = false;
        }
    }

    private static boolean deleteDatabaseIfNotPersistent() {
        if (!connectionSuccessful) return false;
        if (OpenServerMap.PERSISTENT_VISIBILITY) return true;
        try {
            PreparedStatement statement = connection.prepareStatement("DROP TABLE IF EXISTS players");
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            OpenServerMap.log(Level.WARNING, "Database failed to purge: " + e.getMessage());
            return false;
        }
    }

    public static void closeConnection() {
        try {
            deleteDatabaseIfNotPersistent();
            connection.close();
        } catch (SQLException e) {
            OpenServerMap.log(Level.WARNING, "Database failed to properly close: " + e.getMessage());
        }
    }

    public static boolean initializePlayerEntryIfAbsent(UUID uuid) {
        if (!connectionSuccessful) return false;
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO players VALUES(?,?);");
            statement.setString(1, uuid.toString());
            statement.setBoolean(2, getDefaultVisibilityState());
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getErrorCode() == 19) return true; //19 is (ususally) thrown when the value already exists; it can be ignored
            OpenServerMap.log(Level.WARNING, "Failed to initialize player entry: " + e.getMessage());
            return false;
        }

    }

    public static boolean setPlayerVisibility(UUID uuid, boolean visible) {
        if (!connectionSuccessful) return false;
        try {
            //var statement = connection.prepareStatement("INSERT INTO players(uuid, visible) VALUES(?,?);");
            var statement = connection.prepareStatement("UPDATE players SET visible = ? WHERE uuid = ?");
            statement.setString(2, uuid.toString());
            statement.setBoolean(1, visible);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            OpenServerMap.log(Level.WARNING, "Failed to write player visibility: " + e.getMessage());
            return false;
        }
    }

    public static void printTable() {
        try {
            String statement = "SELECT * FROM players";
            ResultSet set = query(statement);
            System.out.println("---------------------------------------");
            while (set.next()) {
                System.out.println(" | " + set.getString(1) + " = " + set.getBoolean(2));
            }
            System.out.println("---------------------------------------");
        } catch (SQLException e) {
            return;
        }
    }

    private static ResultSet query(String sql) throws SQLException {
        return connection.prepareStatement(sql).executeQuery();
    }

    /// Returns true if the player is hidden and false if they aren't.
    /// If an error occurs, this method will revert to the default defined by the visibility-opt-in config option.
    public static boolean playerIsVisible(UUID uuid) {
        if (!connectionSuccessful) return getDefaultVisibilityState();
        try (ResultSet resultSet = query("SELECT uuid, visible FROM players WHERE uuid = \"" + uuid.toString() + "\"")) {
            if (resultSet == null) return getDefaultVisibilityState();
            //If true is returned from next(), a player matching the query uuid was found (first row valid), so return true (true || x) == true
            //If false, is returned from next(), a player matching the query uuid was NOT found (first row not valid), so return the default state (false || x) == x
            return resultSet.getBoolean(2);
        } catch (SQLException e) {
            OpenServerMap.log(Level.WARNING, "Failed to check for visibility of player \"" + uuid + "\", will revert to default state; " + e.getMessage());
            e.printStackTrace();
            return getDefaultVisibilityState();
        }
    }

    private static boolean getDefaultVisibilityState() {
        //System.out.println("retrieved default visibility state");
        return !OpenServerMap.VISIBILITY_OPT_IN;
    }

}