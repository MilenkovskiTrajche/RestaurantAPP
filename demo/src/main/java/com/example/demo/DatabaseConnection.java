package com.example.demo;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/paparaci2";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "postgres";

    private static final HikariDataSource dataSource;

    // Initialize the connection pool
    static {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(URL);
            config.setUsername(USERNAME);
            config.setPassword(PASSWORD);
            config.setMaximumPoolSize(2); // Adjust pool size based on your application's needs
            config.setIdleTimeout(0); // 5minute
            config.setValidationTimeout(5000);
            config.setMaxLifetime(57600000); // 30 minutes
            config.setConnectionTimeout(5000); // 5 seconds to wait for a connection
            config.setConnectionTestQuery("SELECT 1");
            dataSource = new HikariDataSource(config);
        }catch (Exception e) {
            throw new ExceptionInInitializerError("Failed to initialize HikariCp: " + e.getMessage());
        }
    }

    // Get a connection from the pool
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    // Close the connection pool when the application stops
    public static void closePool() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
