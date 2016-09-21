package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import database.DataManager;
import javafx.stage.Stage;
import utils.Field;
import utils.FieldType;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

public class InsertController implements Initializable {
    private DataManager dm;
    private String entityName;
    private TextField txtError;
    private ArrayList<Field> fields;
    private ArrayList<TextField> txtFields;

    @FXML private VBox vboxFields;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dm = new DataManager();
        fields = new ArrayList<>();
        txtFields = new ArrayList<>();

        fillDialog(null);
    }

    public void confirm(ActionEvent actionEvent) {
        for(int i = 0; i < txtFields.size(); i++){
            fields.get(i).setValue(txtFields.get(i).getText());
        }

        try {
            dm.addEntry(entityName, fields);
            txtError.setText("Entry successfully added.");

            closeWindow(null);
        } catch (ClassNotFoundException e) {
            txtError.setText("Check your JDBC driver.");
            e.printStackTrace();
        } catch (SQLException e) {
            txtError.setText("Erro SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void closeWindow(ActionEvent actionEvent) {
        Stage stage = (Stage) vboxFields.getScene().getWindow();
        stage.close();
    }

    public void fillDialog(ActionEvent actionEvent){
        try {
            ResultSet rs = dm.getColumnMetadata(entityName);

            // Filter to be used in number fields to only accept digits
            UnaryOperator<TextFormatter.Change> numberFilter = change -> {
                String text = change.getText();

                if (text.matches("[0-9]*"))
                    return change;

                return null;
            };

            while(rs.next()) {
                String dataType = rs.getString("data_type");
                String fieldName = rs.getString("column_name");
                int fieldMaxSize = rs.getInt("data_length");

                Label lblField = new Label(fieldName + ":");
                TextField txtField = new TextField();
                BorderPane bpnField = new BorderPane(null, null, txtField, null, lblField);
                vboxFields.getChildren().add(bpnField);

                // Listener to limit the textField length
                txtField.lengthProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue.intValue() > oldValue.intValue()) {
                        // Check if the new character is greater than the maxSize
                        if (txtField.getText().length() >= fieldMaxSize) {
                            // Cut the text
                            txtField.setText(txtField.getText().substring(0, fieldMaxSize));
                        }
                    }
                });

                // Creates the Field object to add to the list
                FieldType fieldType;
                if(Objects.equals(dataType, "NUMBER")) {
                    fieldType = FieldType.NUMBER;
                    txtField.setTextFormatter(new TextFormatter<>(numberFilter));
                } else if(Objects.equals(dataType, "DATE")) {
                    fieldType = FieldType.DATE;
                } else {
                    fieldType = FieldType.STRING;
                }

                // We need this list to access the values typed in
                txtFields.add(txtField);
                // Add the Field object to a list to be accessed later (in the insert)
                fields.add(new Field(fieldName, fieldType));
            }
        } catch (ClassNotFoundException e) {
            txtError.setText("Check your JDBC driver.");
            e.printStackTrace();
        } catch (SQLException e) {
            txtError.setText("Erro SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public void setTxtError(TextField txtError) {
        this.txtError = txtError;
    }
}
