import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;

public class CreateDatabaseAndTable {

    private static final String DATABASE_NAME = "My_cats.db";
    private static final String TABLE_NAME = "types";
    private static final String TYPES_FILE = "types.txt";

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

                // Insert all types from the types.txt file
                //addAllTypes(conn, "types.txt");
                // Testing delete_type and update_type
                //deleteType(conn, 5); // Delete the type with id 5
                //updateType(conn, 1, "New Abyssinian Cat"); // Update type with id 1

                // Testing the new functions
                String type = getType(conn, 1);
                System.out.println("Type with id 1: " + type);

                System.out.println("Types where id < 5:");
                getTypeWhere(conn, "id < 5");

                System.out.println("All types:");
                getAllTypes(conn);
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

    //Insert all types from the types.txt file
    public static void addAllTypes(Connection conn, String file) {
        String[] types = readTypesFromFile();
        for (String type : types) {
            insertType(conn, type);
        }
    }

    //Reads the types from the types.txt file
    private static String[] readTypesFromFile() {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(TYPES_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading types from file: " + e.getMessage());
            return new String[0]; // Return an empty array in case of error
        }

        String fileContent = sb.toString();
        String[] types = null;
        //Extract string array from file
        try{
            String typesString = fileContent.substring(fileContent.indexOf("{") + 1, fileContent.lastIndexOf("}"));
            types = typesString.split(",");
            for(int i =0; i<types.length; i++){
                types[i] = types[i].trim().replaceAll("\"", "");
            }
        } catch(Exception ex){
            System.out.println("ERROR: "+ex.getMessage());
            return new String[0];
        }

        return types;
    }

    // Method to delete a cat type by ID
    public static void deleteType(Connection conn, int id) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Deleted type with id: " + id);
            } else {
                System.out.println("No type found with id: " + id);
            }
        } catch (SQLException e) {
            System.err.println("Error deleting type with id " + id + ": " + e.getMessage());
        }
    }

    // Method to update a cat type by ID
    public static void updateType(Connection conn, int id, String newType) {
        String sql = "UPDATE " + TABLE_NAME + " SET type = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newType);
            pstmt.setInt(2, id);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Updated type with id " + id + " to: " + newType);
            } else {
                System.out.println("No type found with id: " + id);
            }
        } catch (SQLException e) {
            System.err.println("Error updating type with id " + id + ": " + e.getMessage());
        }
    }

    // Method to get a cat type by ID
    public static String getType(Connection conn, int id) {
        String sql = "SELECT type FROM " + TABLE_NAME + " WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("type");
            } else {
                return null; // Or throw an exception if no type is found
            }
        } catch (SQLException e) {
            System.err.println("Error getting type with id " + id + ": " + e.getMessage());
            return null;
        }
    }

    // Method to get types where a condition is met
    public static void getTypeWhere(Connection conn, String where) {
        String sql = "SELECT type FROM " + TABLE_NAME + " WHERE " + where;
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                System.out.println(rs.getString("type"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting types where " + where + ": " + e.getMessage());
        }
    }

    // Method to get all types
    public static void getAllTypes(Connection conn) {
        String sql = "SELECT type FROM " + TABLE_NAME;
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                System.out.println(rs.getString("type"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all types: " + e.getMessage());
        }
    }
}