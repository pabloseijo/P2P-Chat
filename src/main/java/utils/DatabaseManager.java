package utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
            // Verifica si las tablas ya existen
            String checkUsersTable = "SELECT name FROM sqlite_master WHERE type='table' AND name='users';";
            String checkFriendshipsTable = "SELECT name FROM sqlite_master WHERE type='table' AND name='friendships';";
            String checkFriendRequestsTable = "SELECT name FROM sqlite_master WHERE type='table' AND name='friend_requests';";

            boolean usersTableExists = stmt.executeQuery(checkUsersTable).next();
            boolean friendshipsTableExists = stmt.executeQuery(checkFriendshipsTable).next();
            boolean friendRequestsTableExists = stmt.executeQuery(checkFriendRequestsTable).next();

            // Crear tablas solo si no existen
            if (!usersTableExists) {
                String usersTable = "CREATE TABLE users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "username TEXT UNIQUE NOT NULL, " +
                        "password TEXT NOT NULL);";
                stmt.execute(usersTable);
                System.out.println("Tabla 'users' creada correctamente.");
            }

            if (!friendshipsTableExists) {
                String friendshipsTable = "CREATE TABLE friendships (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user1_id INTEGER NOT NULL, " +
                        "user2_id INTEGER NOT NULL, " +
                        "FOREIGN KEY (user1_id) REFERENCES users(id), " +
                        "FOREIGN KEY (user2_id) REFERENCES users(id));";
                stmt.execute(friendshipsTable);
                System.out.println("Tabla 'friendships' creada correctamente.");
            }

            if (!friendRequestsTableExists) {
                String friendRequestsTable = "CREATE TABLE friend_requests (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "from_user_id INTEGER NOT NULL, " +
                        "to_user_id INTEGER NOT NULL, " +
                        "status TEXT NOT NULL CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED')), " +
                        "FOREIGN KEY (from_user_id) REFERENCES users(id), " +
                        "FOREIGN KEY (to_user_id) REFERENCES users(id));";
                stmt.execute(friendRequestsTable);
                System.out.println("Tabla 'friend_requests' creada correctamente.");
            }

        } catch (SQLException e) {
            System.err.println("Error verificando o creando tablas: " + e.getMessage());
        }
    }


    // Verifica si un usuario existe en la base de datos
    public boolean usuarioExiste(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Error verificando usuario: " + e.getMessage());
            return false;
        }
    }

    // Agrega un usuario nuevo
    public void addUser(String username, String password) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error agregando usuario: " + e.getMessage());
        }
    }

    // Obtiene la lista de amigos de un usuario
    public List<String> obtenerAmigos(String username) {
        List<String> amigos = new ArrayList<>();
        String sql = "SELECT u.username FROM friendships f " +
                "JOIN users u ON (f.user1_id = u.id OR f.user2_id = u.id) " +
                "WHERE (f.user1_id = (SELECT id FROM users WHERE username = ?) " +
                "OR f.user2_id = (SELECT id FROM users WHERE username = ?)) " +
                "AND u.username != ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, username);
            pstmt.setString(3, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                amigos.add(rs.getString("username"));
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo lista de amigos: " + e.getMessage());
        }
        return amigos;
    }

    // Obtiene las solicitudes de amistad pendientes de un usuario
    public List<String> obtenerSolicitudesPendientes(String username) {
        List<String> solicitudes = new ArrayList<>();
        String sql = "SELECT u.username FROM friend_requests fr " +
                "JOIN users u ON fr.from_user_id = u.id " +
                "WHERE fr.to_user_id = (SELECT id FROM users WHERE username = ?) " +
                "AND fr.status = 'PENDING'";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                solicitudes.add(rs.getString("username"));
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo solicitudes pendientes: " + e.getMessage());
        }
        return solicitudes;
    }

    // Agrega una solicitud de amistad
    public void addFriendRequest(String fromUser, String toUser) {
        String sql = "INSERT INTO friend_requests (from_user_id, to_user_id, status) " +
                "VALUES ((SELECT id FROM users WHERE username = ?), " +
                "(SELECT id FROM users WHERE username = ?), 'PENDING')";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fromUser);
            pstmt.setString(2, toUser);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error agregando solicitud de amistad: " + e.getMessage());
        }
    }

    // Acepta una solicitud de amistad
    public boolean aceptarSolicitudAmistad(String fromUser, String toUser) {
        String updateSql = "UPDATE friend_requests SET status = 'ACCEPTED' " +
                "WHERE from_user_id = (SELECT id FROM users WHERE username = ?) " +
                "AND to_user_id = (SELECT id FROM users WHERE username = ?)";
        String insertSql = "INSERT INTO friendships (user1_id, user2_id) " +
                "SELECT from_user_id, to_user_id FROM friend_requests " +
                "WHERE from_user_id = (SELECT id FROM users WHERE username = ?) " +
                "AND to_user_id = (SELECT id FROM users WHERE username = ?)";

        try (Connection conn = connect()) {
            conn.setAutoCommit(false); // Transacción
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                 PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                updateStmt.setString(1, fromUser);
                updateStmt.setString(2, toUser);
                updateStmt.executeUpdate();

                insertStmt.setString(1, fromUser);
                insertStmt.setString(2, toUser);
                insertStmt.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Error aceptando solicitud de amistad: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("Error en transacción de solicitud de amistad: " + e.getMessage());
        }
        return false;
    }

    public boolean validarUsuario(String username, String password) {
        String sql = "SELECT 1 FROM users WHERE username = ? AND password = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Error validando usuario: " + e.getMessage());
            return false;
        }
    }


    public static void main(String[] args) {
        DatabaseManager dbManager = new DatabaseManager();
        dbManager.addUser("testUser", "testPassword");
        System.out.println("Usuario testUser agregado.");
    }
}
