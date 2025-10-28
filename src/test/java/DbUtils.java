import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DbUtils {

    private static Connection connection;
    // FINAL ATTEMPT: Using 127.0.0.1 instead of localhost
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3308/airline_db?allowPublicKeyRetrieval=true&useSSL=false";

    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "John@123";

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Increased wait time for better reliability
                System.out.println("Waiting for DB to be ready at 127.0.0.1:3308...");
                Thread.sleep(8000);

                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            } catch (SQLException e) {
                System.err.println("Database connection failed! Check if Docker container is running and port 3308 is available.");
                throw e;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new SQLException("Test was interrupted while waiting for DB.", e);
            }
        }
        return connection;
    }

    // This method is for SELECT queries
    public static List<Map<String, Object>> executeQuery(String query) throws SQLException {
        try (Statement statement = getConnection().createStatement();
             ResultSet rs = statement.executeQuery(query)) {

            ResultSetMetaData md = rs.getMetaData();
            int columns = md.getColumnCount();
            List<Map<String, Object>> rows = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>(columns);
                for (int i = 1; i <= columns; ++i) {
                    row.put(md.getColumnName(i), rs.getObject(i));
                }
                rows.add(row);
            }
            return rows;
        }
    }

    // This method is for INSERT, UPDATE, or DELETE queries
    public static int executeUpdate(String query) throws SQLException {
        try (Statement statement = getConnection().createStatement()) {
            return statement.executeUpdate(query);
        }
    }

    public static void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            System.out.println("Database connection closed.");
        }
    }
}
