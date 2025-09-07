package com.NGSViz.sqldbOperate;

import java.sql.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Database connection pool for optimized database operations
 * Provides connection pooling, prepared statement caching, and performance monitoring
 */
public class DatabaseConnectionPool {
    
    private static final int DEFAULT_POOL_SIZE = 10;
    private static final int MAX_POOL_SIZE = 20;
    private static final long CONNECTION_TIMEOUT = 30000; // 30 seconds
    private static final long IDLE_TIMEOUT = 600000; // 10 minutes
    
    private final String databaseUrl;
    private final BlockingQueue<Connection> connectionPool;
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private final ConcurrentHashMap<String, PreparedStatement> statementCache = new ConcurrentHashMap<>();
    
    private static DatabaseConnectionPool instance;
    
    private DatabaseConnectionPool(String databaseUrl) {
        this.databaseUrl = databaseUrl;
        this.connectionPool = new LinkedBlockingQueue<>(MAX_POOL_SIZE);
        initializePool();
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized DatabaseConnectionPool getInstance(String databaseUrl) {
        if (instance == null) {
            instance = new DatabaseConnectionPool(databaseUrl);
        }
        return instance;
    }
    
    /**
     * Initialize connection pool
     */
    private void initializePool() {
        try {
            for (int i = 0; i < DEFAULT_POOL_SIZE; i++) {
                Connection connection = createConnection();
                if (connection != null) {
                    connectionPool.offer(connection);
                }
            }
            System.out.println("Database connection pool initialized with " + DEFAULT_POOL_SIZE + " connections");
        } catch (SQLException e) {
            System.err.println("Failed to initialize connection pool: " + e.getMessage());
        }
    }
    
    /**
     * Create a new database connection
     */
    private Connection createConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(databaseUrl);
        connection.setAutoCommit(true);
        return connection;
    }
    
    /**
     * Get connection from pool
     */
    public Connection getConnection() throws SQLException {
        try {
            Connection connection = connectionPool.poll(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
            if (connection != null && !connection.isClosed()) {
                activeConnections.incrementAndGet();
                return connection;
            } else {
                // Create new connection if pool is empty
                Connection newConnection = createConnection();
                activeConnections.incrementAndGet();
                return newConnection;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Timeout waiting for database connection", e);
        }
    }
    
    /**
     * Return connection to pool
     */
    public void returnConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed() && connectionPool.size() < MAX_POOL_SIZE) {
                    connectionPool.offer(connection);
                } else {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error returning connection to pool: " + e.getMessage());
            } finally {
                activeConnections.decrementAndGet();
            }
        }
    }
    
    /**
     * Get prepared statement with caching
     */
    public PreparedStatement getPreparedStatement(String sql) throws SQLException {
        return statementCache.computeIfAbsent(sql, k -> {
            try {
                Connection connection = getConnection();
                return connection.prepareStatement(sql);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to create prepared statement", e);
            }
        });
    }
    
    /**
     * Execute query with connection management
     */
    public ResultSet executeQuery(String sql, Object... parameters) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            
            // Set parameters
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            
            return statement.executeQuery();
        } catch (SQLException e) {
            if (connection != null) {
                returnConnection(connection);
            }
            throw e;
        }
    }
    
    /**
     * Execute update with connection management
     */
    public int executeUpdate(String sql, Object... parameters) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            
            // Set parameters
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            
            return statement.executeUpdate();
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                returnConnection(connection);
            }
        }
    }
    
    /**
     * Batch execute multiple statements
     */
    public int[] executeBatch(String sql, List<Object[]> parameterList) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = getConnection();
            connection.setAutoCommit(false);
            statement = connection.prepareStatement(sql);
            
            for (Object[] parameters : parameterList) {
                for (int i = 0; i < parameters.length; i++) {
                    statement.setObject(i + 1, parameters[i]);
                }
                statement.addBatch();
            }
            
            int[] results = statement.executeBatch();
            connection.commit();
            return results;
        } catch (SQLException e) {
            if (connection != null) {
                connection.rollback();
            }
            throw e;
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.setAutoCommit(true);
                returnConnection(connection);
            }
        }
    }
    
    /**
     * Get pool statistics
     */
    public PoolStats getPoolStats() {
        return new PoolStats(
            connectionPool.size(),
            activeConnections.get(),
            MAX_POOL_SIZE,
            statementCache.size()
        );
    }
    
    /**
     * Print pool statistics
     */
    public void printPoolStats() {
        PoolStats stats = getPoolStats();
        System.out.println("=== Database Connection Pool Statistics ===");
        System.out.printf("Available connections: %d\n", stats.availableConnections);
        System.out.printf("Active connections: %d\n", stats.activeConnections);
        System.out.printf("Max pool size: %d\n", stats.maxPoolSize);
        System.out.printf("Cached statements: %d\n", stats.cachedStatements);
        System.out.printf("Pool utilization: %.1f%%\n", 
                         (double) stats.activeConnections / stats.maxPoolSize * 100);
        System.out.println("==========================================");
    }
    
    /**
     * Close all connections and clear cache
     */
    public void shutdown() {
        // Close all connections in pool
        Connection connection;
        while ((connection = connectionPool.poll()) != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
        
        // Close cached statements
        for (PreparedStatement statement : statementCache.values()) {
            try {
                statement.close();
            } catch (SQLException e) {
                System.err.println("Error closing statement: " + e.getMessage());
            }
        }
        statementCache.clear();
        
        System.out.println("Database connection pool shutdown complete");
    }
    
    /**
     * Pool statistics class
     */
    public static class PoolStats {
        public final int availableConnections;
        public final int activeConnections;
        public final int maxPoolSize;
        public final int cachedStatements;
        
        public PoolStats(int availableConnections, int activeConnections, 
                        int maxPoolSize, int cachedStatements) {
            this.availableConnections = availableConnections;
            this.activeConnections = activeConnections;
            this.maxPoolSize = maxPoolSize;
            this.cachedStatements = cachedStatements;
        }
    }
} 