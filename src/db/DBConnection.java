package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static Connection conn; // single instance

    // Private constructor prevents instantiation
    private DBConnection() {}

    // Public method to get the single connection instance
    public static Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/pahana_edu_billing",
                    "root",
                    "newone"
            );
            System.out.println("✅ DB Connection established");
        }
        return conn;
    }
}
