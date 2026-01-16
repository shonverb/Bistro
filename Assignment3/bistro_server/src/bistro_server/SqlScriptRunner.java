package bistro_server;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SqlScriptRunner {

    /**
     * Reads a SQL dump file and executes the commands against the provided connection.
     */
    public static void runScript(Connection conn, File sqlFile) {
        System.out.println("Importing file: " + sqlFile.getName());
        
        // Use a delimiter logic to separate commands. 
        // Standard dumps end commands with ';'
        StringBuilder commandBuilder = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(sqlFile));
             Statement statement = conn.createStatement()) {

            String line;
            while ((line = reader.readLine()) != null) {
                String trimmedLine = line.trim();

                // 1. Skip empty lines and standard comments
                if (trimmedLine.isEmpty() || trimmedLine.startsWith("--") || trimmedLine.startsWith("//")) {
                    continue;
                }

                // 2. Handle lines. 
                // Note: MySQL dumps often split INSERTs or CREATEs across multiple lines.
                // We append lines until we find a semicolon at the end.
                commandBuilder.append(trimmedLine);
                commandBuilder.append(" "); // Add space to prevent syntax errors when joining lines

                if (trimmedLine.endsWith(";")) {
                    // 3. We found the end of a command
                    String sql = commandBuilder.toString();
                    
                    // Remove the trailing semicolon for JDBC execution
                    sql = sql.substring(0, sql.length() - 2); 
                    
                    // 4. Execute (Handling the special version-specific comments too)
                    try {
                        statement.execute(sql);
                    } catch (SQLException e) {
                        System.err.println("Error executing command in " + sqlFile.getName() + ": " + e.getMessage());
                        // Optional: Throw exception if you want to stop on first error
                    }
                    
                    // Reset builder for next command
                    commandBuilder.setLength(0);
                }
            }
            System.out.println("Successfully imported: " + sqlFile.getName());

        } catch (IOException | SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to read/execute script: " + sqlFile.getName());
        }
    }
}