package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    private static final String CONNECTION = "jdbc:oracle:thin:@grad.icmc.usp.br:15215:orcl";
    private static final String USERNAME = "n8937250";
    private static final String PASSWORD = "n8937250";
    private static Connection connection = null;

    private static void connect() throws ClassNotFoundException, SQLException {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        connection = DriverManager.getConnection(CONNECTION, USERNAME, PASSWORD);
    }

    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        if(connection != null && !connection.isClosed()) {
            return connection;
        }
        
        connect();
        return connection;
    }

}
