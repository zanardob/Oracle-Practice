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

    public void fillTableView(ActionEvent actionEvent) {
        ResultSet rs;
        ObservableList<ObservableList> data = FXCollections.observableArrayList();
        tableView.getItems().clear();
        tableView.getColumns().clear();

        try {
            // Find the entity for which the viewName is the same as the name on the ComboBox
            Optional<Entity> entity = entities
                    .stream()
                    .filter(e -> e.getViewName() == cboxTableSelect.getValue())
                    .findFirst();

            // Extract the realName of the entity
            String entityName = entity.orElseThrow(() -> new ClassNotFoundException()).getRealName();

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
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/insertview.fxml"));
            insertion.setScene(new Scene(root));
            insertion.setTitle("Add new entry");
            insertion.initModality(Modality.APPLICATION_MODAL);
            insertion.initOwner(txtError.getScene().getWindow());
            insertion.showAndWait();
        } catch (IOException e) {
            txtError.setText("Application files corrupted.");
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dm = new DataManager();
        fillComboBox();
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
            txtError.setText("Erro SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
