package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Simple class for connecting into the database
 * The username and password are provided by the user on the first menu
 */
public class DatabaseConnector {
    private static final String CONNECTION = "jdbc:oracle:thin:@grad.icmc.usp.br:15215:orcl";
    private static String username;
    private static String password;
    private static Connection connection = null;

    private static void connect() throws ClassNotFoundException, SQLException {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        connection = DriverManager.getConnection(CONNECTION, username, password);
    }

    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        if(connection != null && !connection.isClosed()) {
            return connection;
        }
        
        connect();
        return connection;
    }

    public static void setCredentials(String username, String password){
        DatabaseConnector.username = username;
        DatabaseConnector.password = password;
    }
}
