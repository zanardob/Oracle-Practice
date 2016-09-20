import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.util.Callback;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.scene.control.TableColumn.CellDataFeatures;

public class MainController {
    private DataManager dm;

    private ComboBox<String> cbox;
    private TableView tableView;
    private Button btnInsert;
    private TextField txtError;
    private ArrayList<Entity> entities;

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
            cbox.setItems(entityNames);
        } catch (ClassNotFoundException e) {
            txtError.setText("Check your JDBC driver.");
            e.printStackTrace();
        } catch (SQLException e) {
            txtError.setText("Erro SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    void fillTable() {
        ResultSet rs;
        ObservableList<ObservableList> data = FXCollections.observableArrayList();
        tableView.getItems().clear();
        tableView.getColumns().clear();

        try {
            // Find the entity for which the viewName is the same as the name on the ComboBox
            Optional<Entity> entity = entities
                    .stream()
                    .filter(e -> e.getViewName() == cbox.getValue())
                    .findFirst();

            // Extract the realName of the entity
            String entityName = entity.orElseThrow(() -> new ClassNotFoundException()).getRealName();

            // Query the appropriate entity in the schema
            rs = dm.getEntity(entityName);

            for(int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                final int j = i;
                TableColumn column = new TableColumn(rs.getMetaData().getColumnName(i + 1));
                column.setCellValueFactory(new Callback<CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(CellDataFeatures<ObservableList, String> param) {
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

    MainController(ComboBox<String> cbox, TableView tableView, Button btnInsert, TextField txtError) {
        this.cbox = cbox;
        this.tableView = tableView;
        this.btnInsert = btnInsert;
        this.txtError = txtError;

        cbox.setOnAction(event -> fillTable());

        dm = new DataManager();
    }
}
