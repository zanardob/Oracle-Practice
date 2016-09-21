package database;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import utils.*;

import java.sql.*;
import java.util.ArrayList;

public class DataManager {

    public ArrayList<Entity> getEntityList() throws SQLException, ClassNotFoundException {
        Connection connection = DatabaseConnector.getConnection();
        Statement statement = connection.createStatement();
        ArrayList<Entity> entities = new ArrayList<>();
        String query;
        ResultSet rs;

        query = "SELECT table_name FROM user_tables MINUS SELECT table_name FROM user_snapshots";
        rs = statement.executeQuery(query);
        while(rs.next()) {
            entities.add(new Entity(rs.getString("table_name"), EntityType.TABLE));
        }

        query = "SELECT view_name FROM user_views";
        rs = statement.executeQuery(query);
        while(rs.next()) {
            entities.add(new Entity(rs.getString("view_name"), EntityType.VIEW));
        }

        query = "SELECT table_name FROM user_snapshots";
        rs = statement.executeQuery(query);
        while(rs.next()) {
            entities.add(new Entity(rs.getString("table_name"), EntityType.SNAPSHOT));
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

    public ResultSet getColumnMetadata(String entityName) throws SQLException, ClassNotFoundException {
        Connection connection = DatabaseConnector.getConnection();
        Statement statement = connection.createStatement();
        String query = "SELECT COLUMN_NAME, DATA_TYPE, DATA_LENGTH FROM USER_TAB_COLUMNS WHERE TABLE_NAME = '" + entityName + "'";
        ResultSet rs = statement.executeQuery(query);

        return rs;
    }

    public ObservableList<UserPrivilege> getPrivileges(Entity entity) throws SQLException, ClassNotFoundException {
        Connection connection = DatabaseConnector.getConnection();
        Statement statement = connection.createStatement();
        ObservableList<UserPrivilege> privileges = FXCollections.observableArrayList();

        String query;
        String entityName = entity.getRealName();
        if(entity.getEntityType() == EntityType.VIEW ){
            query = "SELECT OWNER FROM ALL_VIEWS WHERE VIEW_NAME = '" + entityName + "'";
        } else {
            query = "SELECT OWNER FROM ALL_TABLES WHERE TABLE_NAME = '" + entityName + "'";
        }
        ResultSet rs = statement.executeQuery(query);

        rs.next();
        privileges.add(new UserPrivilege(rs.getString("OWNER"), "OWNER"));

        query = "SELECT GRANTEE, PRIVILEGE FROM USER_TAB_PRIVS WHERE TABLE_NAME = '" + entityName + "'";
        rs = statement.executeQuery(query);
        while(rs.next()){
            privileges.add(new UserPrivilege(rs.getString("GRANTEE"), rs.getString("PRIVILEGE")));
        }

        return privileges;
    }

    public void addEntry(String entityName, ArrayList<Field> fields) throws SQLException, ClassNotFoundException {
        Connection connection = DatabaseConnector.getConnection();
        Statement statement = connection.createStatement();

        String fieldNames = "";
        String fieldValues = "";
        for(int i = 0; i < fields.size(); i++){
            Field f = fields.get(i);

            if (f.getType() == FieldType.NUMBER) {
                fieldNames = fieldNames + f.getName();
                fieldValues = fieldValues + f.getValue();
            }else if (f.getType() == FieldType.DATE){
                // TODO
            }else {
                fieldNames = fieldNames + f.getName();
                fieldValues = fieldValues + "'" + f.getValue() + "'";
            }

            if((i + 1) < fields.size()){
                fieldNames = fieldNames + ", ";
                fieldValues = fieldValues + ", ";
            }
        }

        String query = "INSERT INTO " + entityName + " (" + fieldNames + ") VALUES (" + fieldValues + ")";
        System.out.println(query);
        statement.executeQuery(query);
    }
}
