import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import database.DataManager;

import java.sql.ResultSet;
import java.sql.SQLException;

public class InsertController {
    private DataManager dm;
    private VBox vbox;

    public void fillDialog(String entityName, TextField txtError){
        try {
            ResultSet rs = dm.getColumnMetadata(entityName);


        } catch (ClassNotFoundException e) {
            txtError.setText("Check your JDBC driver.");
            e.printStackTrace();
        } catch (SQLException e) {
            txtError.setText("Erro SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public InsertController(VBox vbox) {
        this.vbox = vbox;

        dm = new DataManager();
    }
}
