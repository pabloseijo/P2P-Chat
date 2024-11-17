package utils;

import java.sql.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:chat_app.db";

    public DatabaseManager() {
        createTables();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private void createTables() {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            String usersTable = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL);";

            String friendshipsTable = "CREATE TABLE IF NOT EXISTS friendships (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user1_id INTEGER NOT NULL, " +
                    "user2_id INTEGER NOT NULL, " +
                    "FOREIGN KEY (user1_id) REFERENCES users(id), " +
                    "FOREIGN KEY (user2_id) REFERENCES users(id));";

            String friendRequestsTable = "CREATE TABLE IF NOT EXISTS friend_requests (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "from_user_id INTEGER NOT NULL, " +
                    "to_user_id INTEGER NOT NULL, " +
                    "status TEXT NOT NULL CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED')), " +
                    "FOREIGN KEY (from_user_id) REFERENCES users(id), " +
                    "FOREIGN KEY (to_user_id) REFERENCES users(id));";

            stmt.execute(usersTable);
            stmt.execute(friendshipsTable);
            stmt.execute(friendRequestsTable);

            System.out.println("Tablas creadas correctamente en SQLite.");
        } catch (SQLException e) {
            System.err.println("Error creando tablas: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new DatabaseManager();
    }
}
