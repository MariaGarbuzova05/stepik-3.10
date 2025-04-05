import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateDatabaseAndTable {

    public static void main(String[] args) {
        String databaseName = "My_cats.db";
        String tableName = "types";

        // Create the database if it doesn't exist
        String url = "jdbc:sqlite:" + databaseName;

        try {
            Class.forName("org.sqlite.JDBC"); // Or the correct driver class name
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found!");
            e.printStackTrace();
            return; // Exit the program
        }

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");

                // Check if the table exists
                String checkTableSQL = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'";
                try (Statement stmt = conn.createStatement()) {
                    boolean tableExists = false;
                    try {
                        if (stmt.executeQuery(checkTableSQL).next()) {
                            tableExists = true;
                        }
                    } catch (SQLException e) {
                        System.out.println("Error checking if table exists: " + e.getMessage());
                    }

                    if (!tableExists) {
                        // Create the table if it doesn't exist
                        String createTableSQL = "CREATE TABLE " + tableName + " (\n"
                                + "   id INTEGER PRIMARY KEY,\n"
                                + "   type VARCHAR(100) NOT NULL\n"
                                + ");";

                        try (Statement createStmt = conn.createStatement()) {
                            createStmt.execute(createTableSQL);
                            System.out.println("Table 'types' created successfully.");
                        } catch (SQLException e) {
                            System.out.println("Error creating table: " + e.getMessage());
                        }
                    } else {
                        System.out.println("Table '" + tableName + "' already exists.  Skipping creation.");
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}