package utils;

import java.sql.*;
import java.util.ArrayList;

public class DataManager {

    public ArrayList<Entity> getEntityList() throws ClassNotFoundException, SQLException {
        Connection connection = DatabaseConnector.getConnection();
        Statement statement = connection.createStatement();
        ArrayList<Entity> entities = new ArrayList<>();
        String query;
        ResultSet rs;

        query = "SELECT table_name FROM user_tables MINUS SELECT table_name FROM user_snapshots";
        rs = statement.executeQuery(query);
        while(rs.next()) {
            entities.add(new Entity(rs.getString("table_name"), Type.TABLE));
        }

        query = "SELECT view_name FROM user_views";
        rs = statement.executeQuery(query);
        while(rs.next()) {
            entities.add(new Entity(rs.getString("view_name"), Type.VIEW));
        }

        query = "SELECT table_name FROM user_snapshots";
        rs = statement.executeQuery(query);
        while(rs.next()) {
            entities.add(new Entity(rs.getString("table_name"), Type.SNAPSHOT));
        }

        return entities;
    }

    public ResultSet getEntity(String entityName) throws SQLException, ClassNotFoundException {
        Connection connection = DatabaseConnector.getConnection();
        Statement statement = connection.createStatement();
        String query = "SELECT * FROM " + entityName;
        ResultSet rs = statement.executeQuery(query);

        return rs;
    }
}
