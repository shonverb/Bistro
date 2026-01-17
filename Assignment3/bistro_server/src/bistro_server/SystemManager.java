package bistro_server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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

	public boolean AllTablesExist(String user, String pass) {
		String serverUrl = "jdbc:mysql://localhost:3306/"; 
		
	    String [] requiredTables = {
	        "order",
	        "table",
	        "day",
	        "date",
	        "user"
	    };
	    // Build a query like: AND TABLE_NAME IN (?, ?, ?)
	    StringBuilder sql = new StringBuilder(
	        "SELECT COUNT(TABLE_NAME) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'bistro' AND TABLE_NAME IN ("
	    );
	    
	    for (int i = 0; i < requiredTables.length; i++) {
	        sql.append(i == 0 ? "?" : ", ?");
	    }
	    sql.append(")");

	    try(Connection conn = DriverManager.getConnection(serverUrl, user, pass);
	    		PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

	        // Fill the parameters
	        for (int i = 0; i < requiredTables.length; i++) {
	            stmt.setString(i + 1, requiredTables[i]);
	        }

	        try (ResultSet rs = stmt.executeQuery()) {
	            if (rs.next()) {
	                int foundCount = rs.getInt(1);
	                System.out.println("Found " + foundCount + " out of " + requiredTables.length + " required tables.");
	                conn.close();
	                // Return true only if we found exactly as many tables as we asked for
	                return foundCount == requiredTables.length;
	            }
	        }
	    } catch (SQLException e) {
	        System.err.println("Error checking tables: " + e.getMessage());
	    }
	    return false;
	
	}

	public void createBistroSchema(String user, String pass) {
	    String url = "jdbc:mysql://localhost:3306/"; 

	    try (Connection conn = DriverManager.getConnection(url, user, pass);
	         Statement stmt = conn.createStatement()) {

	        System.out.println("Checking for 'bistro' schema...");
	        stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `bistro`");
	        System.out.println("Schema 'bistro' checked/created successfully.");

	    } catch (SQLException e) {
	        System.err.println("Error creating schema: " + e.getMessage());
	        e.printStackTrace();
	    }
	}
	public void createAllTables(String user, String pass) {
	    String url = "jdbc:mysql://localhost:3306/bistro?allowLoadLocalInfile=true&serverTimezone=Asia/Jerusalem&useSSL=false";
	    
	    // SQL Definitions based on your screenshots
	    String createDayTable = "CREATE TABLE IF NOT EXISTS `day` (" +
	            "  `day_of_week` INT NOT NULL," +
	            "  `open_hour` TIME DEFAULT NULL," +
	            "  `close_hour` TIME DEFAULT NULL," +
	            "  `status` ENUM('OPEN','CLOSE') DEFAULT NULL," +
	            "  PRIMARY KEY (`day_of_week`)" +
	            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

	    String createUserTable = "CREATE TABLE IF NOT EXISTS `user` (" +
	            "  `full_name` VARCHAR(100) DEFAULT NULL," +
	            "  `subscriber_id` INT NOT NULL," +
	            "  `username` VARCHAR(100) DEFAULT NULL," +
	            "  `phone_number` VARCHAR(10) DEFAULT NULL," +
	            "  `email` VARCHAR(100) DEFAULT NULL," +
	            "  `status` ENUM('CLIENT','EMPLOYEE','MANAGER') DEFAULT NULL," +
	            "  PRIMARY KEY (`subscriber_id`)" +
	            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

	    String createPhysicalTable = "CREATE TABLE IF NOT EXISTS `table` (" +
	            "  `table_number` INT NOT NULL," +
	            "  `number_of_seats` INT DEFAULT NULL," +
	            "  `current_order` INT DEFAULT '0'," +
	            "  `pending_removal` TINYINT(1) DEFAULT '0'," +
	            "  PRIMARY KEY (`table_number`)" +
	            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

	    String createOrderTable = "CREATE TABLE IF NOT EXISTS `order` (" +
	            "  `order_number` INT NOT NULL," +
	            "  `order_datetime` DATETIME DEFAULT NULL," +
	            "  `number_of_guests` INT DEFAULT NULL," +
	            "  `confirmation_code` INT DEFAULT NULL," +
	            "  `subscriber_id` INT DEFAULT NULL," +
	            "  `date_of_placing_order` DATE DEFAULT NULL," +
	            "  `contact` VARCHAR(45) DEFAULT NULL," +
	            "  `status` ENUM('OPEN','CLOSED','CANCELLED','WAITING') DEFAULT NULL," +
	            "  `actual_arrival` DATETIME DEFAULT NULL," +
	            "  `seated_time` DATETIME DEFAULT NULL," +
	            "  `leave_time` DATETIME DEFAULT NULL," +
	            "  `type_of_order` ENUM('IN_ADVANCE','ON_THE_SPOT') DEFAULT NULL," +
	            "  PRIMARY KEY (`order_number`)" +
	            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

	    String createDateTable = "CREATE TABLE IF NOT EXISTS `date` (" +
	             "  `specific_date` DATE NOT NULL,"+
	             " `open_hour` TIME DEFAULT NULL," +
	             "  `close_hour` TIME DEFAULT NULL," +
	             "  `status` ENUM('OPEN','CLOSE') DEFAULT NULL," +
	             "  PRIMARY KEY (`specific_date`)" +
	             ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

	    try (Connection conn = DriverManager.getConnection(url, user, pass);
	         Statement stmt = conn.createStatement()) {

	        System.out.println("Creating tables...");
	        
	        stmt.executeUpdate(createDayTable);
	        stmt.executeUpdate(createUserTable);
	        stmt.executeUpdate(createPhysicalTable);
	        stmt.executeUpdate(createOrderTable);
	        stmt.executeUpdate(createDateTable);
	        
	        System.out.println("All tables created successfully.");
	        conn.close();
	    } catch (SQLException e) {
	        System.err.println("Error creating tables: " + e.getMessage());
	        e.printStackTrace();
	    }
	}
}
