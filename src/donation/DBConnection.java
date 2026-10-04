package donation;

import java.sql.Connection;
import java.sql.DriverManager;

// This class gives a database connection to all the servlets.
public class DBConnection {

    // Change these three values according to your MySQL settings
    private static final String URL      = "jdbc:mysql://localhost:3306/donation_db";
    private static final String USER     = "root";
    private static final String PASSWORD = "root123";

    public static Connection getConnection() throws Exception {
        // Load the MySQL driver (mysql-connector-j jar must be inside WEB-INF/lib)
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
