import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DataManager {

    public ObservableList<String> getTableNames() throws ClassNotFoundException, SQLException {
        Connection connection = DatabaseConnector.getConnection();
        Statement statement = connection.createStatement();
        ObservableList<String> names = FXCollections.observableArrayList();
        String query;
        ResultSet rs;

        query = "SELECT table_name FROM user_tables MINUS SELECT table_name FROM user_snapshots";
        rs = statement.executeQuery(query);
        while(rs.next()) {
            names.add(rs.getString("table_name"));
        }

        query = "SELECT view_name FROM user_views";
        rs = statement.executeQuery(query);
        while(rs.next()) {
            names.add(rs.getString("view_name") + " (view)");
        }

        query = "SELECT table_name FROM user_snapshots";
        rs = statement.executeQuery(query);
        while(rs.next()) {
            names.add(rs.getString("table_name") + " (snapshot)");
        }

        return names;
    }

    public ResultSet getTable(String tableName) throws SQLException, ClassNotFoundException {
        Connection connection = DatabaseConnector.getConnection();
        Statement statement = connection.createStatement();
        ObservableList<String> names = FXCollections.observableArrayList();
        String query = "SELECT * FROM " + tableName;
        ResultSet rs = statement.executeQuery(query);

        return rs;
    }
}
