package controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import database.DataManager;
import utils.Entity;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Callback;
import utils.EntityType;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MainController implements Initializable {
    private DataManager dm;
    private ArrayList<Entity> entities;

    @FXML public ComboBox cboxTableSelect;
    @FXML public TableView tableView;
    @FXML public TextField txtError;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dm = new DataManager();
        fillComboBox();
    }

    public void fillComboBox() {
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
            txtError.setText("Erro SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showSelectionAlert(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("No table selected!");
        alert.setContentText("Please select a TABLE from the list.");

        cboxTableSelect.requestFocus();
        alert.showAndWait();
    }

    // Gets the realName of an entity from the list of fntities
    private Entity getEntity(){
        Optional<Entity> entity = entities
                .stream()
                .filter(e -> e.getViewName() == cboxTableSelect.getValue())
                .findFirst();

        return entity.orElseThrow(NullPointerException::new);
    }

    public void fillTableView() {
        ResultSet rs;
        ObservableList<ObservableList> data = FXCollections.observableArrayList();
        tableView.getItems().clear();
        tableView.getColumns().clear();

        try {
            String entityName = getEntity().getRealName();

            // Query the appropriate entity in the schema
            rs = dm.getEntity(entityName);

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
            txtError.setText("Erro SQL: " + e.getMessage());
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
                showSelectionAlert();
                return;
            }
            String entityName = entity.getRealName();

            InsertController controller = loader.getController();
            controller.setEntityName(entityName);
            controller.setTxtError(txtError);
            controller.fillDialog(null);

            insertion.setScene(new Scene(root));
            insertion.setTitle("Add new entry into " + entityName);
            insertion.initModality(Modality.APPLICATION_MODAL);
            insertion.initOwner(txtError.getScene().getWindow());
            insertion.showAndWait();
            fillTableView();
        } catch (IOException e) {
            txtError.setText("Application files corrupted.");
            e.printStackTrace();
        } catch (NullPointerException e){
            showSelectionAlert();
            e.printStackTrace();
        }
    }

    public void viewPrivileges(ActionEvent actionEvent) {
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

            privileges.setScene(new Scene(root));
            privileges.setTitle("Privileges for " + entityName);
            privileges.initModality(Modality.APPLICATION_MODAL);
            privileges.initOwner(txtError.getScene().getWindow());
            privileges.showAndWait();
        } catch (IOException e) {
            txtError.setText("Application files corrupted.");
            e.printStackTrace();
        } catch (NullPointerException e){
            showSelectionAlert();
            e.printStackTrace();
        }
    }
}
