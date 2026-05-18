package com.hospital.bedalloc.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static Connection connection;

    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            DatabaseConfig config = new DatabaseConfig();
            connection = DriverManager.getConnection(
                config.getUrl(), 
                config.getUser(), 
                config.getPassword()
            );
        }
        return connection;
    }

    public static void beginTransaction() throws SQLException {
        getConnection().setAutoCommit(false);
        getConnection().setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
    }

    public static void commit() throws SQLException {
        if (connection != null && !connection.getAutoCommit()) {
            connection.commit();
            connection.setAutoCommit(true);
        }
    }

    public static void rollback() {
        try {
            if (connection != null && !connection.getAutoCommit()) {
                connection.rollback();
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
