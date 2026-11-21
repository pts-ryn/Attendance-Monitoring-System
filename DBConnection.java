package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL = "jdbc:postgresql://localhost:5432/Attendance_DB";
    private static final String USER = "postgres";
    private static final String PASSWORD = "Ryan";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Temporary test: run this file to check the connection
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("Connected to PostgreSQL!");
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
    }
}