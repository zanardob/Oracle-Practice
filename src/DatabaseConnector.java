/**
 * Created by Lucas on 19/09/2016.
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DatabaseConnector {
    private static final String CONNECTION = "jdbc:oracle:thin:@grad.icmc.usp.br:15215:orcl";
    private static final String USERNAME = "n8937250";
    private static final String PASSWORD = "n8937250";
    private static Connection connection = null;

    private static void connect() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            connection = DriverManager.getConnection(CONNECTION, USERNAME, PASSWORD);
        }
        // TODO: Mandar o stack trace pruma text area.
        catch (ClassNotFoundException e) {
            System.out.println("Where is your Oracle JDBC Driver?");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Connection Failed!");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException, ClassNotFoundException{
        if(connection != null && !connection.isClosed()) {
            return connection;
        }

        connect();
        return connection;
    }

}
