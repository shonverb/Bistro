package bistro_server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SystemManager {

	public boolean bistroSchemaExists(String dbUser, String dbPassword) {
	    // Connect to the MySQL server directly, not the specific 'bistro' DB
	    String serverUrl = "jdbc:mysql://localhost:3306/"; 
	    String query = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = ?";

	    try (Connection conn = DriverManager.getConnection(serverUrl, dbUser, dbPassword);
	         PreparedStatement stmt = conn.prepareStatement(query)) {
	        
	        stmt.setString(1, "bistro");
	        
	        try (ResultSet rs = stmt.executeQuery()) {
	            // If we get a row back, the schema exists
	            return rs.next(); 
	        }

	    } catch (SQLException e) {
	        System.err.println("Error checking for schema: " + e.getMessage());
	        return false;
	    }
	}

	public boolean AllTablesExist() {
		Connection conn = ConnectionPool.getInstance().getConnection();
	    String [] requiredTables = {
	        "`order`",
	        "`table`",
	        "day",
	        "date",
	        "`user`"
	    };
	    // Build a query like: AND TABLE_NAME IN (?, ?, ?)
	    StringBuilder sql = new StringBuilder(
	        "SELECT COUNT(TABLE_NAME) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'bistro' AND TABLE_NAME IN ("
	    );
	    
	    for (int i = 0; i < requiredTables.length; i++) {
	        sql.append(i == 0 ? "?" : ", ?");
	    }
	    sql.append(")");

	    try(PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

	        // Fill the parameters
	        for (int i = 0; i < requiredTables.length; i++) {
	            stmt.setString(i + 1, requiredTables[i]);
	        }

	        try (ResultSet rs = stmt.executeQuery()) {
	            if (rs.next()) {
	                int foundCount = rs.getInt(1);
	                // Return true only if we found exactly as many tables as we asked for
	                return foundCount == requiredTables.length;
	            }
	        }
	    } catch (SQLException e) {
	        System.err.println("Error checking tables: " + e.getMessage());
	    } finally {
	    	ConnectionPool.getInstance().returnConnection(conn);
	    }
	    return false;
	
	}

}
