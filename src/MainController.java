import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.SQLException;

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
            txtError.setText("Connection failed.");
            e.printStackTrace();
        }
    }

    MainController(ComboBox<String> cbox, TableView table, Button btnInsert, TextField txtError) {
        this.cbox = cbox;
        this.table = table;
        this.btnInsert = btnInsert;
        this.txtError = txtError;

        dm = new DataManager();
    }
}
