import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.util.Callback;
import javax.xml.transform.Result;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.scene.control.TableColumn.CellDataFeatures;

public class MainController {
    private DataManager dm;

    private ComboBox<String> cbox;
    private TableView table;
    private Button btnInsert;
    private TextField txtError;

    void fillComboBox() {
        try {
            cbox.setItems(dm.getTableNames());
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
        table.getItems().clear();
        table.getColumns().clear();

        try {
            rs = dm.getTable(cbox.getValue());
            for(int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                final int j = i;
                TableColumn column = new TableColumn(rs.getMetaData().getColumnName(i + 1));
                column.setCellValueFactory(new Callback<CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(CellDataFeatures<ObservableList, String> param) {
                        return new SimpleStringProperty(param.getValue().get(j).toString());
                    }
                });

                table.getColumns().addAll(column);
            }

            while(rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for(int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    row.add(rs.getString(i));
                }
                data.add(row);
            }

            table.setItems(data);

        } catch (ClassNotFoundException e) {
            txtError.setText("Check your JDBC driver.");
            e.printStackTrace();
        } catch (SQLException e) {
            txtError.setText("Erro SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    MainController(ComboBox<String> cbox, TableView table, Button btnInsert, TextField txtError) {
        this.cbox = cbox;
        this.table = table;
        this.btnInsert = btnInsert;
        this.txtError = txtError;

        cbox.setOnAction(event -> fillTable());

        dm = new DataManager();
    }
}
