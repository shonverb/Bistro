package bistro_server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ConnectionPool {

    private static ConnectionPool instance;
    private BlockingQueue<Connection> pool;
    private final int MAX_POOL_SIZE = 10;
    
	//conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/bistro", "root", "");
    //conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/bistro?allowLoadLocalInfile=true&serverTimezone=Asia/Jerusalem&useSSL=false", "root", "Hodvak123!");
    // Database credentials
    private final String DB_URL = "jdbc:mysql://localhost:3306/bistro";
    private final String USER = "root"; 
    private final String PASS = "";

    // Private constructor (Singleton Pattern)
    private ConnectionPool() {
        pool = new ArrayBlockingQueue<>(MAX_POOL_SIZE);
        try {
            // Fill the pool with connections
            for (int i = 0; i < MAX_POOL_SIZE; i++) {
                Connection conn = createNewConnection();
                pool.offer(conn);
            }
            System.out.println("Connection Pool initialized with " + MAX_POOL_SIZE + " connections.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static synchronized ConnectionPool getInstance() {
        if (instance == null) {
            instance = new ConnectionPool();
        }
        return instance;
    }

    private Connection createNewConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    /**
     * Gets a connection from the pool. Waits if none are available.
     */
    public Connection getConnection() {
        try {
            // .take() blocks the thread until a connection is available
            return pool.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns a connection to the pool. call this in a 'finally' block!
     */
    public void returnConnection(Connection conn) {
        if (conn != null) {
            pool.offer(conn);
        }
    }
    /**
     * Closes all connections in the pool
     * @throws SQLException
     */
    public void shutdown() throws SQLException {
        System.out.println("Closing all DB connections...");
        for (Connection conn : pool) {
            if (conn != null && !conn.isClosed()) {
                conn.close(); // Actually closes the TCP socket
            }
        }
        pool.clear();
        System.out.println("All connections closed.");
    }
}