package controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import database.DataManager;
import util.Entity;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Callback;
import util.EntityType;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MainController implements Initializable {
    @FXML private ComboBox cboxTableSelect;
    @FXML private TableView tableView;
    @FXML private TextField txtError;

    private DataManager dm;
    private ArrayList<Entity> entities;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dm = new DataManager();

        // Calls both functions in the DataManager that build lists that don't
        // change during execution; that's why the "authentication" process
        // takes some time to complete
        try {
            DataManager.buildTableDDLList();
            DataManager.buildForeignKeyConstraintList();
        } catch (SQLException e) {
            txtError.setText("Check your JDBC driver.");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            txtError.setText("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void changeScene(Stage stage, Parent root, String title){
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(txtError.getScene().getWindow());
        stage.setResizable(false);
        stage.showAndWait();
    }

    void fillComboBox() {
        try {
            // Get a list for all the entities in the schema
            entities = dm.getEntityList();
            ObservableList<String> entityNames = FXCollections.observableArrayList();

            // Add the viewName for each entity in an ObservableList to be used in the ComboBox
            entityNames.addAll(
                    entities.stream()
                            .map(Entity::getViewName)
                            .collect(Collectors.toList()));
            cboxTableSelect.setItems(entityNames);
        } catch (ClassNotFoundException e) {
            txtError.setText("Check your JDBC driver.");
            e.printStackTrace();
        } catch (SQLException e) {
            txtError.setText("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Returns the Entity that matches the selected viewName in the ComboBox
     */
    private Entity getEntity(){
        Optional<Entity> entity = entities
                .stream()
                .filter(e -> e.getViewName() == cboxTableSelect.getValue())
                .findFirst();

        return entity.orElseThrow(NullPointerException::new);
    }

    @FXML
    private void fillTableView() {
        ResultSet rs;
        ObservableList<ObservableList> data = FXCollections.observableArrayList();
        tableView.getItems().clear();
        tableView.getColumns().clear();

        try {
            String entityName = getEntity().getRealName();

            // Query the appropriate entity in the schema
            rs = dm.getEntity(entityName);

            // Building the TableColumns
            for(int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                final int j = i;
                TableColumn column = new TableColumn(rs.getMetaData().getColumnName(i + 1));
                column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                        return new SimpleStringProperty(param.getValue().get(j).toString());
                    }
                });

                tableView.getColumns().addAll(column);
            }

            // Populating the table
            while(rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for(int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    String value = rs.getString(i);

                    // Null values handling
                    if(rs.wasNull())
                        value = "<null>";

                    row.add(value);
                }
                data.add(row);
            }

            tableView.setItems(data);
        } catch (ClassNotFoundException e) {
            txtError.setText("Check your JDBC driver.");
            e.printStackTrace();
        } catch (SQLException e) {
            txtError.setText("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void insert(ActionEvent actionEvent) {
        Stage insertion = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/insertview.fxml"));

        try {
            Parent root = loader.load();

            Entity entity = getEntity();
            if(entity.getEntityType() != EntityType.TABLE) {
                txtError.setText("You can't add an entry in a view/snapshot!");
                return;
            }
            String entityName = entity.getRealName();

            InsertController controller = loader.getController();
            controller.setEntityName(entityName);
            controller.setTxtError(txtError);
            controller.fillDialog(null);

            changeScene(insertion, root, "Inserting into " + entityName);
            fillTableView();
        } catch (IOException e) {
            txtError.setText("Application files corrupted.");
            e.printStackTrace();
        } catch (NullPointerException e){
            txtError.setText("No table selected!");
            e.printStackTrace();
        }
    }

    public void checkPrivileges(ActionEvent actionEvent) {
        Stage privileges = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/privilegesview.fxml"));

        try {
            Parent root = loader.load();

            Entity entity = getEntity();
            String entityName = entity.getRealName();

            PrivilegeController controller = loader.getController();
            controller.setEntity(entity);
            controller.setTxtError(txtError);
            controller.fillTableView(null);

            changeScene(privileges, root, "Privileges for " + entityName);
        } catch (IOException e) {
            txtError.setText("Application files corrupted.");
            e.printStackTrace();
        } catch (NullPointerException e){
            txtError.setText("No table selected!");
            e.printStackTrace();
        }
    }

    public void viewSchemaDDL(ActionEvent actionEvent) {
        Stage ddls = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/schemaddlview.fxml"));

        try {
            Parent root = loader.load();

            SchemaDDLController controller = loader.getController();
            controller.fillTextArea(null);

            changeScene(ddls, root, "DDLs for all tables on the schema");
        } catch (IOException e) {
            txtError.setText("Application files corrupted.");
            e.printStackTrace();
        }
    }
}
