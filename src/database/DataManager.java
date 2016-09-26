package database;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import util.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class DataManager {
    private static ArrayList<ForeignKeyConstraint> foreignKeys;
    private static ArrayList<String> tableDDLs;

    /**
     * Returns all the entities in the schema to fill the ComboBox
     */
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

    /**
     * Simple select from a table to populate the TableView
     */
    public ResultSet getEntity(String entityName) throws SQLException, ClassNotFoundException {
        Connection connection = DatabaseConnector.getConnection();
        Statement statement = connection.createStatement();
        String query = "SELECT * FROM " + entityName;

        return statement.executeQuery(query);
    }

    /**
     * Some metadata query to fill the insertion form
     */
    public ResultSet getColumnMetadata(String entityName) throws SQLException, ClassNotFoundException {
        Connection connection = DatabaseConnector.getConnection();
        Statement statement = connection.createStatement();
        String query = "SELECT COLUMN_NAME, DATA_TYPE, DATA_LENGTH FROM USER_TAB_COLUMNS WHERE TABLE_NAME = '" + entityName + "'";

        return statement.executeQuery(query);
    }

    /**
     * Used to populate the privileges TableView
     * NOTE: the owner of the table is always added with an "OWNER' permission
     */
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

    /**
     * Adding a new entry into a table
     */
    public void addEntry(String entityName, ArrayList<Field> fields) throws SQLException, ClassNotFoundException {
        Connection connection = DatabaseConnector.getConnection();
        Statement statement = connection.createStatement();

        String fieldNames = "";
        String fieldValues = "";
        for(int i = 0; i < fields.size(); i++){
            Field f = fields.get(i);

            fieldNames = fieldNames + f.getName();
            if (f.getType() == FieldType.NUMBER) {
                fieldValues = fieldValues + f.getValue();
            }else if (f.getType() == FieldType.DATE){
                fieldValues = fieldValues + "TO_DATE('" + f.getValue() + "', 'yyyy/mm/dd')";
            }else {
                fieldValues = fieldValues + "'" + f.getValue() + "'";
            }

            if((i + 1) < fields.size()){
                fieldNames = fieldNames + ", ";
                fieldValues = fieldValues + ", ";
            }
        }

        String query = "INSERT INTO " + entityName + " (" + fieldNames + ") VALUES (" + fieldValues + ")";
        statement.executeQuery(query);
    }

    /**
     * Builds the list that holds the DDL for all the tables in the schema
     * Table name needs to start with "LE"
     */
    public static void buildTableDDLList() throws SQLException, ClassNotFoundException {
        // Changes variables in the Oracle database to print a better result
        // (without storing and segmentation information and with a semicolon after every command)
        String transformDatabase = "BEGIN " +
                "dbms_metadata.set_transform_param(dbms_metadata.session_transform,'STORAGE', false); " +
                "dbms_metadata.set_transform_param(dbms_metadata.session_transform,'SEGMENT_ATTRIBUTES', false); " +
                "dbms_metadata.set_transform_param(dbms_metadata.session_transform,'SQLTERMINATOR', true); " +
                "END;";

        // Restores the variables changed above to their default values
        String restoreDatabase = "BEGIN " +
                "dbms_metadata.set_transform_param(dbms_metadata.session_transform,'DEFAULT'); " +
                "END;";

        // Returns only the tables that start with "LE" as cited in the practice specification
        String query = "SELECT DBMS_METADATA.GET_DDL('TABLE', u.table_name) AS TABLE_DDL FROM USER_TABLES u WHERE u.TABLE_NAME LIKE 'LE%'";

        Connection connection = DatabaseConnector.getConnection();
        tableDDLs = new ArrayList<>();

        Statement changeDBStatement = connection.prepareStatement(transformDatabase);
        changeDBStatement.executeUpdate(transformDatabase);

        Statement queryStatement = connection.createStatement();
        ResultSet rs = queryStatement.executeQuery(query);
        while(rs.next()){
            // Add each row to a list of table DDLs
            tableDDLs.add(rs.getString("TABLE_DDL"));
        }

        changeDBStatement = connection.prepareStatement(restoreDatabase);
        changeDBStatement.executeUpdate(restoreDatabase);

        // Sort the list alphabetically (so that LE01 will appear before LE02 and so on)
        Collections.sort(tableDDLs, String.CASE_INSENSITIVE_ORDER);
    }

    /**
     * Simply returns the DDL list or builds it if it's still null
     */
    public ArrayList<String> getTableDDL() throws SQLException, ClassNotFoundException {
        if(tableDDLs != null)
            return tableDDLs;

        buildTableDDLList();
        return tableDDLs;
    }

    /**
     * Builds the list that contains information about the foreign key constraints
     */
    public static void buildForeignKeyConstraintList() throws SQLException, ClassNotFoundException {
        Connection connection = DatabaseConnector.getConnection();
        Statement statement = connection.createStatement();
        foreignKeys = new ArrayList<>();

        String query =
                "SELECT " +
                        "COLS.TABLE_NAME, " +
                        "COLS.COLUMN_NAME, " +
                        "CONS_R.TABLE_NAME AS R_TABLE_NAME, " +
                        "COLS_R.COLUMN_NAME AS R_COLUMN_NAME " +
                        "FROM USER_CONSTRAINTS CONS " +
                        "LEFT JOIN USER_CONS_COLUMNS COLS ON COLS.CONSTRAINT_NAME = CONS.CONSTRAINT_NAME " +
                        "LEFT JOIN USER_CONSTRAINTS CONS_R ON CONS_R.CONSTRAINT_NAME = CONS.R_CONSTRAINT_NAME " +
                        "LEFT JOIN USER_CONS_COLUMNS COLS_R ON COLS_R.CONSTRAINT_NAME = CONS.R_CONSTRAINT_NAME " +
                        "WHERE CONS.CONSTRAINT_TYPE = 'R' AND CONS.CONSTRAINT_NAME NOT IN " +
                        "(SELECT CONSTRAINT_NAME FROM USER_CONS_COLUMNS GROUP BY CONSTRAINT_NAME HAVING COUNT(*) > 1)";

        ResultSet rs = statement.executeQuery(query);

        while(rs.next()) {
            foreignKeys.add(new ForeignKeyConstraint(rs.getString("table_name"), rs.getString("column_name"), rs.getString("r_table_name"), rs.getString("r_column_name")));
        }
    }

    /**
     * Returns a list with the CHECK and FOREIGN KEY constraints for the table received
     */
    public ArrayList<Constraint> getTableConstraints(String tableName) throws SQLException, ClassNotFoundException {
        Connection connection = DatabaseConnector.getConnection();
        Statement statement = connection.createStatement();
        ArrayList<Constraint> constraints = new ArrayList<>();

        String query = "SELECT CONSTRAINT_TYPE, SEARCH_CONDITION FROM USER_CONSTRAINTS WHERE TABLE_NAME = '" + tableName + "' AND (CONSTRAINT_TYPE = 'C' OR CONSTRAINT_TYPE = 'R') AND CONSTRAINT_NAME NOT LIKE 'SYS_%'";

        ResultSet rs = statement.executeQuery(query);
        while(rs.next()){
            String searchCondition = rs.getString("search_condition");

            if(Objects.equals(rs.getString("constraint_type"), "C")){
                String[] split = searchCondition.split(" ");

                if(Objects.equals(split[1], "IN")){
                    String[] values = split[2].split("[(',)]");

                    Constraint constraint = new Constraint();
                    constraint.setColumnName(split[0]);
                    for(String s : values) {
                        if(!Objects.equals(s, ""))
                            constraint.addValue(s);
                    }

                    constraints.add(constraint);
                }
            } else {
                for(ForeignKeyConstraint fkConstraint : foreignKeys) {
                    if(Objects.equals(fkConstraint.getTableName(), tableName)) {
                        Constraint constraint = new Constraint();
                        constraint.setColumnName(fkConstraint.getColumnName());

                        String queryForValues = "SELECT " + fkConstraint.getReferredColumnName() + " FROM " + fkConstraint.getReferredTableName();
                        ResultSet rsValues = connection.createStatement().executeQuery(queryForValues);
                        while(rsValues.next()) {
                            constraint.addValue(rsValues.getString(fkConstraint.getReferredColumnName()));
                        }
                        constraints.add(constraint);
                    }
                }
            }
        }

        return constraints;
    }
}
