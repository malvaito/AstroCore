package dev.malvaito.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    private static DatabaseManager instance;

    private DatabaseManager() {
        // Constructor privado para el patrón Singleton
    }

    private Connection databaseConnection;

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
            instance.establishConnection();
        }
        return instance;
    }

    private void establishConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            databaseConnection = DriverManager.getConnection("jdbc:sqlite:plugins/AstroCore/database.db");
            createDatabaseTables();
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Error connecting to SQLite database: " + e.getMessage());
        }
    }

    private void createDatabaseTables() {
        if (databaseConnection == null) return;

        // SQL para la tabla de stats
        String statsTableSQL = "CREATE TABLE IF NOT EXISTS stats (" +
                "player_uuid VARCHAR(36) PRIMARY KEY," +
                "player_nickname VARCHAR(16) NOT NULL," +
                "kills INT DEFAULT 0," +
                "deaths INT DEFAULT 0," +
                "playtime INTEGER DEFAULT 0," +
                "killstreak INT DEFAULT 0," +
                "best_killstreak INT DEFAULT 0," +
                "blocks_placed INT DEFAULT 0," +
                "blocks_broken INT DEFAULT 0," +
                "entities_killed INT DEFAULT 0," +
                "koths_captured INT DEFAULT 0," +
                "votes INT DEFAULT 0" +
                ");";

        // SQL para la tabla de homes
        String homesTableSQL = "CREATE TABLE IF NOT EXISTS homes (" +
                "home_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "player_uuid VARCHAR(36) NOT NULL," +
                "home_name VARCHAR(255) NOT NULL," +
                "world VARCHAR(255) NOT NULL," +
                "x DOUBLE NOT NULL," +
                "y DOUBLE NOT NULL," +
                "z DOUBLE NOT NULL," +
                "yaw REAL NOT NULL," +
                "pitch REAL NOT NULL," +
                "UNIQUE(player_uuid, home_name)" +
                ");";

        // SQL para la tabla de stones
        String protectionStonesTableSQL = "CREATE TABLE IF NOT EXISTS protection_stones (" +
                "stone_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "owner_uuid VARCHAR(36) NOT NULL," +
                "owner_nickname VARCHAR(255) NOT NULL," +
                "stone_type VARCHAR(255) NOT NULL," +
                "world VARCHAR(255) NOT NULL," +
                "x INT NOT NULL," +
                "y INT NOT NULL," +
                "z INT NOT NULL," +
                "radius INT DEFAULT 5," +
                "UNIQUE(world, x, y, z)" +
                ");";

        // SQL para la tabla de stone members
        String protectionStonesMembersTableSQL = "CREATE TABLE IF NOT EXISTS protection_stones_members (" +
                "member_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "player_uuid VARCHAR(36) NOT NULL," +
                "player_nickname VARCHAR(16) NOT NULL," +
                "stone_id INTEGER NOT NULL," +
                "FOREIGN KEY (stone_id) REFERENCES protection_stones(stone_id) ON DELETE CASCADE," +
                "UNIQUE(player_uuid, stone_id)" +
                ");";

        // SQL para la tabla de economía
        String economyTableSQL = "CREATE TABLE IF NOT EXISTS economy (" +
                "player_uuid VARCHAR(36) PRIMARY KEY," +
                "player_nickname VARCHAR(16) NOT NULL," +
                "balance DOUBLE DEFAULT 0.0," +
                "total_spent DOUBLE DEFAULT 0.0," +
                "total_received DOUBLE DEFAULT 0.0," +
                "total_earned DOUBLE DEFAULT 0.0" +
                ");";

        try (java.sql.Statement statement = databaseConnection.createStatement()) {
            statement.execute(statsTableSQL);
            statement.execute(homesTableSQL);
            statement.execute(protectionStonesTableSQL);
            statement.execute(protectionStonesMembersTableSQL);
            statement.execute(economyTableSQL);
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }

    public void closeConnection() {
        if (databaseConnection != null) {
            try {
                if (!databaseConnection.isClosed()) {
                    databaseConnection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing SQLite database connection: " + e.getMessage());
            }
        }
    }

    public Connection getDatabaseConnection() {
        try {
            if (databaseConnection == null || databaseConnection.isClosed()) {
                establishConnection(); // Re-establish connection if it's closed or null
            }
        } catch (SQLException e) {
            System.err.println("Error checking database connection status: " + e.getMessage());
        }
        return databaseConnection;
    }
}