package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;

public class ConnectionPool {
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=Airline;encrypt=true;trustServerCertificate=true";
    private static final String USER = "java";
    private static final String PASSWORD = "12345678";
    private static final int POOL_SIZE = 200;

    private final Queue<Connection> pool = new LinkedList<>();

    private static ConnectionPool instance;

    private ConnectionPool() {
        try {
            for (int i = 0; i < POOL_SIZE; i++) {
                pool.add(DriverManager.getConnection(DB_URL, USER, PASSWORD));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка создания пула соединений", e);
        }
    }

    public static synchronized ConnectionPool getInstance() {
        if (instance == null) {
            instance = new ConnectionPool();
        }
        return instance;
    }

    public synchronized Connection getConnection() {
        return pool.poll();
    }

    public synchronized void releaseConnection(Connection connection) {
        pool.offer(connection);
    }
}
