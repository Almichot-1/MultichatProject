// server/DatabaseManager.java
package server;

import java.sql.*;
import java.util.logging.Logger;
import org.mindrot.jbcrypt.BCrypt;

public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/chat_app_db";
    private static final String USER = System.getenv("DB_USER");
    private static final String PASSWORD = System.getenv("DB_PASSWORD");

    private static final Logger logger = Logger.getLogger(DatabaseManager.class.getName());
    private Connection conn;

    public DatabaseManager() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Load MySQL driver
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to MySQL database.");
        } catch (ClassNotFoundException e) {
            logger.severe("MySQL JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            logger.severe("Database connection failed: " + e.getMessage());
        }
    }

    public boolean registerUser(String username, String password) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.warning("Registration failed: " + e.getMessage());
            return false;
        }
    }

    public boolean authenticateUser(String username, String password) {
        String sql = "SELECT password FROM users WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedHashedPassword = rs.getString("password");
                return BCrypt.checkpw(password, storedHashedPassword);
            }
        } catch (SQLException e) {
            logger.warning("Authentication failed: " + e.getMessage());
        }
        return false;
    }

    public void saveMessage(int senderId, Integer receiverId, String content) {
        String sql = "INSERT INTO messages (sender_id, receiver_id, content) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, senderId);
            if (receiverId != null) {
                stmt.setInt(2, receiverId);
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setString(3, content);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.warning("Failed to save message: " + e.getMessage());
        }
    }

    public int getUserId(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            logger.warning("Failed to get user ID: " + e.getMessage());
        }
        return -1;
    }

    public void close() {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                logger.warning("Failed to close database connection: " + e.getMessage());
            }
        }
    }
}
