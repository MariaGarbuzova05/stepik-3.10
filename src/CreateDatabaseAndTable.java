import java.sql.*;

public class CreateDatabaseAndTable {

    private static final String DATABASE_NAME = "My_cats.db";
    private static final String TABLE_NAME = "types";

    public static void main(String[] args) {
        // Create the database if it doesn't exist
        String url = "jdbc:sqlite:" + DATABASE_NAME;

        try {
            Class.forName("org.sqlite.JDBC"); // Or the correct driver class name
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found!");
            e.printStackTrace();
            return; // Exit the program
        }

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                System.out.println("Connected to database.");

                // Create the table if it doesn't exist
                createTableIfNotExists(conn);

                // Insert the cat types
                insertType(conn, "Абиссинская кошка");
                insertType(conn, "Австралийский мист");
                insertType(conn, "Американская жесткошерстная");

            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Method to create the table if it doesn't exist
    private static void createTableIfNotExists(Connection conn) throws SQLException {
        String checkTableSQL = "SELECT name FROM sqlite_master WHERE type='table' AND name=?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkTableSQL)) {
            checkStmt.setString(1, TABLE_NAME);
            if (!checkStmt.executeQuery().next()) {
                String createTableSQL = "CREATE TABLE " + TABLE_NAME + " (\n"
                        + "   id INTEGER PRIMARY KEY AUTOINCREMENT,\n" // Added AUTOINCREMENT
                        + "   type VARCHAR(100) NOT NULL\n"
                        + ");";
                try (Statement createStmt = conn.createStatement()) {
                    createStmt.execute(createTableSQL);
                    System.out.println("Table '" + TABLE_NAME + "' created successfully.");
                }
            } else {
                System.out.println("Table '" + TABLE_NAME + "' already exists. Skipping creation.");
            }
        }
    }

    // Method to insert a cat type
    public static void insertType(Connection conn, String type) {
        String sql = "INSERT INTO " + TABLE_NAME + " (type) VALUES (?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type);
            pstmt.executeUpdate();
            System.out.println("Inserted type: " + type);
        } catch (SQLException e) {
            System.err.println("Error inserting type '" + type + "': " + e.getMessage());
        }
    }
}