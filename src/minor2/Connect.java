
package minor2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

class Connect {
    /**
     * Connect to a sample database 
     */
    public static Connection getConnection() {
        Connection conn = null;
        try {
            // db parameters  
            String url = "jdbc:sqlite:demo.db";
            // create a connection to the database  
            conn = DriverManager.getConnection(url);

            System.out.println("Connection to SQLite has been established.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return conn;

    }
}  