import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CreateDatabaseAndTable {

    private static final String DATABASE_NAME = "My_cats.db";
    private static final String TABLE_NAME = "types";
    private static final String CATS_TABLE_NAME = "cats";
    private static final String TYPES_FILE = "types.txt";
    private static final String NAMES_FILE = "names.txt";

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
                createCatsTableIfNotExists(conn);

                //addMoreCats(conn, 5000);
                // Test delete_cat(int id)
                //deleteCat(conn, 1);
                // Test delete_cat(String where)
                //deleteCat(conn, "age > 10");
                // Test update_cat
                //updateCat(conn, 2, "age = 7, weight = 6.1", "name = 'Murka'");

                // Test the new functions
                Cat cat = getCat(conn, 1);
                System.out.println("Cat with id 1: " + cat);

                System.out.println("Cats where age < 5:");
                getCatWhere(conn, "age < 5");

                System.out.println("All cats:");
                getAllCats(conn);
                // Test insert_cat
                //insertCat(conn, "Barsik", "Абиссинская кошка", 3, 4.5);
                //insertCat(conn, "Murka", "Бенгальская кошка", 5, 5.0);
                //insertCat(conn, "Snowball", "Новая порода", 2, 3.2); // Testing a new cat type

                // Insert all types from the types.txt file
                //addAllTypes(conn, "types.txt");
                // Testing delete_type and update_type
                //deleteType(conn, 5); // Delete the type with id 5
                //updateType(conn, 1, "New Abyssinian Cat"); // Update type with id 1

                // Testing the new functions
                //String type = getType(conn, 1);
                //System.out.println("Type with id 1: " + type);

                //System.out.println("Types where id < 5:");
                //getTypeWhere(conn, "id < 5");

                //System.out.println("All types:");
                //getAllTypes(conn);
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Cat class
    static class Cat {
        int id;
        String name;
        String type;
        int age;
        double weight;

        public Cat(int id, String name, String type, int age, double weight) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.age = age;
            this.weight = weight;
        }

        @Override
        public String toString() {
            return "Cat{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", age=" + age +
                    ", weight=" + weight +
                    '}';
        }
    }

    // Method to get a cat by ID
    public static Cat getCat(Connection conn, int id) {
        String sql = "SELECT cats.id, cats.name, types.type, cats.age, cats.weight " +
                "FROM " + CATS_TABLE_NAME +
                " JOIN " + TABLE_NAME + " ON cats.type_id = types.id " +
                "WHERE cats.id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Cat(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getInt("age"),
                        rs.getDouble("weight")
                );
            } else {
                System.out.println("No cat found with id: " + id);
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Error getting cat with id " + id + ": " + e.getMessage());
            return null;
        }
    }

    // Method to get cats where a condition is met
    public static void getCatWhere(Connection conn, String where) {
        String sql = "SELECT cats.id, cats.name, types.type, cats.age, cats.weight " +
                "FROM " + CATS_TABLE_NAME +
                " JOIN " + TABLE_NAME + " ON cats.type_id = types.id " +
                "WHERE " + where;

        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Cat cat = new Cat(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getInt("age"),
                        rs.getDouble("weight")
                );
                System.out.println(cat);
            }
        } catch (SQLException e) {
            System.err.println("Error getting cats where " + where + ": " + e.getMessage());
        }
    }

    // Method to get all cats
    public static void getAllCats(Connection conn) {
        String sql = "SELECT cats.id, cats.name, types.type, cats.age, cats.weight " +
                "FROM " + CATS_TABLE_NAME +
                " JOIN " + TABLE_NAME + " ON cats.type_id = types.id";

        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Cat cat = new Cat(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getInt("age"),
                        rs.getDouble("weight")
                );
                System.out.println(cat);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all cats: " + e.getMessage());
        }
    }

    //Helper methods
    private static String readString(String str,ResultSet rs) throws SQLException{
        return rs.getString(str)!=null?rs.getString(str):"";
    }
    private static int readInt(String str,ResultSet rs) throws SQLException{
        return rs.getInt(str);
    }
    private static double readDouble(String str,ResultSet rs) throws SQLException{
        return rs.getDouble(str);
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

    // Method to create the cats table if it doesn't exist
    private static void createCatsTableIfNotExists(Connection conn) throws SQLException {
        String checkTableSQL = "SELECT name FROM sqlite_master WHERE type='table' AND name=?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkTableSQL)) {
            checkStmt.setString(1, CATS_TABLE_NAME);
            if (!checkStmt.executeQuery().next()) {
                String createTableSQL = "CREATE TABLE " + CATS_TABLE_NAME + " (\n"
                        + "   id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                        + "   name VARCHAR(20) NOT NULL,\n"
                        + "   type_id INTEGER NOT NULL,\n"
                        + "   age INTEGER NOT NULL,\n"
                        + "   weight DOUBLE NOT NULL,\n"
                        + "   FOREIGN KEY (type_id) REFERENCES " + TABLE_NAME + "(id)\n"
                        + ");";
                try (Statement createStmt = conn.createStatement()) {
                    createStmt.execute(createTableSQL);
                    System.out.println("Table '" + CATS_TABLE_NAME + "' created successfully.");
                }
            } else {
                System.out.println("Table '" + CATS_TABLE_NAME + "' already exists. Skipping creation.");
            }
        }
    }

    //Get or create and then get the id
    private static int getOrCreateTypeId(Connection conn, String type) throws SQLException {
        Integer typeId = getTypeID(conn, type);
        if (typeId == null){
            insertType(conn, type);
            typeId = getTypeID(conn, type);
            if (typeId == null){
                throw new SQLException("Cannot find id from inserted type");
            }
        }
        return typeId;
    }

    //Get list of names
    private static String[] readNamesFromFile() {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(NAMES_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading names from file: " + e.getMessage());
            return new String[0]; // Return an empty array in case of error
        }

        String fileContent = sb.toString();
        String[] names = null;
        //Extract string array from file
        try{
            String namesString = fileContent.substring(fileContent.indexOf("{") + 1, fileContent.lastIndexOf("}"));
            names = namesString.split(",");
            for(int i =0; i<names.length; i++){
                names[i] = names[i].trim().replaceAll("\"", "");
            }
        } catch(Exception ex){
            System.out.println("ERROR: "+ex.getMessage());
            return new String[0];
        }

        return names;
    }

    // Method to add more cats
    public static void addMoreCats(Connection conn, int n) {
        Random random = new Random();

        //Get list of types from database
        List<String> types = new ArrayList<>();
        String sql = "SELECT type FROM " + TABLE_NAME;
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                types.add(rs.getString("type"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all types: " + e.getMessage());
            return;
        }

        String[] catNames = readNamesFromFile();

        for (int i = 0; i < n; i++) {
            String name = catNames[random.nextInt(catNames.length)];
            String type = types.get(random.nextInt(types.size())); // Get a random type from the list
            int age = random.nextInt(15) + 1; // Age between 1 and 15
            double weight = 2.0 + (8.0 * random.nextDouble()); // Weight between 2.0 and 10.0
            insertCat(conn, name, type, age, weight);
        }

        System.out.println("Added " + n + " random cats to the database.");
    }

    //Insert a new cat
    public static void insertCat(Connection conn, String name, String type, int age, Double weight) {
        try {
            int typeId = getOrCreateTypeId(conn, type);

            String sql = "INSERT INTO " + CATS_TABLE_NAME + " (name, type_id, age, weight) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setInt(2, typeId);
                pstmt.setInt(3, age);
                pstmt.setDouble(4, weight);
                pstmt.executeUpdate();
                System.out.println("Inserted cat: " + name);
            }
        } catch (SQLException e) {
            System.err.println("Error inserting cat '" + name + "': " + e.getMessage());
        }
    }

    //Get Type ID
    public static Integer getTypeID(Connection conn, String type){
        String sql = "SELECT id FROM " + TABLE_NAME + " WHERE type = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Error getting type with type " + type + ": " + e.getMessage());
            return null;
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

    // Method to delete a cat by ID
    public static void deleteCat(Connection conn, int id) {
        String sql = "DELETE FROM " + CATS_TABLE_NAME + " WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Deleted cat with id: " + id);
            } else {
                System.out.println("No cat found with id: " + id);
            }
        } catch (SQLException e) {
            System.err.println("Error deleting cat with id " + id + ": " + e.getMessage());
        }
    }

    // Method to delete a cat by condition
    public static void deleteCat(Connection conn, String where) {
        String sql = "DELETE FROM " + CATS_TABLE_NAME + " WHERE " + where;
        try (Statement stmt = conn.createStatement()) {
            int rowsAffected = stmt.executeUpdate(sql);
            if (rowsAffected > 0) {
                System.out.println("Deleted cats where: " + where);
            } else {
                System.out.println("No cats found with condition: " + where);
            }
        } catch (SQLException e) {
            System.err.println("Error deleting cat with condition " + where + ": " + e.getMessage());
        }
    }

    // Method to update a cat by ID
    public static void updateCat(Connection conn, int id, String set, String where) {
        String sql = "UPDATE " + CATS_TABLE_NAME + " SET " + set + " WHERE " + where;
        try (Statement stmt = conn.createStatement()) {
            int rowsAffected = stmt.executeUpdate(sql);
            if (rowsAffected > 0) {
                System.out.println("Updated cat with id: " + id + ", set " + set + ", where " + where);
            } else {
                System.out.println("No cat found with id: " + id + ", where " + where);
            }
        } catch (SQLException e) {
            System.err.println("Error updating cat with id " + id + ": " + e.getMessage());
        }
    }
}