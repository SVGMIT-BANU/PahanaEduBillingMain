package dao;

import model.User;
import db.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class UserDAO {

    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getRole());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public User loginUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String dbPassword = rs.getString("password");
                if (BCrypt.checkpw(password, dbPassword)) {
                    User user = new User();
                    user.setUserid(rs.getInt("userid"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(dbPassword);
                    user.setRole(rs.getString("role"));
                    user.setCreatedAt(rs.getString("created_at"));
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean adminExists() {
        String sql = "SELECT COUNT(*) FROM users WHERE role='admin'";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void createAdminIfNotExists() {
        if (!adminExists()) {
            User admin = factory.UserFactory.createUser("admin", "admin123", "admin");
            registerUser(admin);
            System.out.println("✅ Admin created (username: admin, password: admin123)");
        }
    }

    public boolean deleteUser(String username) {
        String sql = "DELETE FROM users WHERE username = ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, username);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public void createOrResetAdmin() {
        String adminUsername = "admin";
        String fixedPassword = "admin123";
        String hashedPassword = BCrypt.hashpw(fixedPassword, BCrypt.gensalt(10));

        try {
            Connection conn = DBConnection.getConnection();
            // Check if admin exists
            String checkSql = "SELECT userid FROM users WHERE username = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, adminUsername);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // Admin exists, update password
                String updateSql = "UPDATE users SET password = ? WHERE username = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setString(1, hashedPassword);
                updateStmt.setString(2, adminUsername);
                updateStmt.executeUpdate();
                System.out.println("ℹ Admin password reset to 'admin123'");
            } else {
                // Admin not exists, insert
                String insertSql = "INSERT INTO users (username, password, role) VALUES (?, ?, 'admin')";
                PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                insertStmt.setString(1, adminUsername);
                insertStmt.setString(2, hashedPassword);
                insertStmt.executeUpdate();
                System.out.println("✅ Admin account created with password 'admin123'");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


