package net.mmly.openservermap;

import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;

public class Database {

    private static Connection connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        var sql = "CREATE TABLE IF NOT EXISTS players (" +
                "	uuid text PRIMARY KEY" +
                ");";

        Connection connection = DriverManager.getConnection("jdbc:sqlite:plugins/OpenServerMap/hiddenplayers.db");
        Statement statement = connection.createStatement();
        statement.execute(sql);

        return connection;
    }

    public static boolean writeUUID(UUID uuid) {
        String sql = "INSERT INTO players(uuid) VALUES(?);";

        try {
            Connection connection = connect();
            if (connection == null) return false;
            var statement = connection.prepareStatement(sql);
            statement.setString(1, uuid.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 19) return true; //key already exists
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean checkForUUID(UUID uuid) {
        String sql = "SELECT uuid FROM players WHERE uuid == \"" + uuid.toString() + "\"";

        try (ResultSet resultSet = query(sql)){
            if (resultSet == null) return false;

            return resultSet.getString(1).equals(uuid.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }

    public static boolean readUUIDs() {
        String sql = "SELECT * FROM players";

        try {
            Connection connection = connect();
            if (connection == null) return false;
            var statement = connection.prepareStatement(sql);
            ResultSet returnValue = statement.executeQuery();
            System.out.println(returnValue.getString(1));
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static ResultSet query(String sql) throws SQLException {
        Connection connection = connect();
        if (connection == null) return null;

        PreparedStatement statement = connection.prepareStatement(sql);
        connection.close();
        return statement.executeQuery();
    }

    /// Returns true if the player is hidden and false if they aren't.
    /// If an error occurs, this method will revert to the default defined by the visibility-opt-in config option.
    public static boolean playerIsHidden(UUID uuid) {
        try (ResultSet resultSet = query("SELECT uuid FROM players WHERE uuid == \"" + uuid.toString() + "\"")) {
            if (resultSet == null) return getDefaultVisibilityState();
            return resultSet.getString(1).equals(uuid.toString());
        } catch (SQLException e) {
            OpenServerMap.log(Level.WARNING, "Failed to check for visibility of player \"" + uuid + "\", will revert to default state: " + e.getMessage());
            return getDefaultVisibilityState();
        }
    }

    private static boolean getDefaultVisibilityState() {
        return !OpenServerMap.VISIBILITY_OPT_IN;
    }

}