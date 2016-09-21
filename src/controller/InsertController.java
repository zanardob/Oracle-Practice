package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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

        fillDialog();
    }

    public void confirm(ActionEvent actionEvent) {
        for(int i = 0; i < txtFields.size(); i++){
            fields.get(i).setValue(txtFields.get(i).getText());
            System.out.println(fields.get(i).getValue());
        }

        try {
            dm.addEntry(entityName, fields);
            txtError.setText("Entry successfully added.");

            Stage stage = (Stage) vboxFields.getScene().getWindow();
            stage.close();
        } catch (ClassNotFoundException e) {
            txtError.setText("Check your JDBC driver.");
            e.printStackTrace();
        } catch (SQLException e) {
            txtError.setText("Erro SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void cancel(ActionEvent actionEvent) {
        Stage stage = (Stage) vboxFields.getScene().getWindow();
        stage.close();
    }

    public void fillDialog(){
        System.out.println(entityName);
        try {
            ResultSet rs = dm.getColumnMetadata(entityName);

            while(rs.next()) {
                String dataType = rs.getString("data_type");
                String fieldName = rs.getString("column_name");

                FieldType fieldType;
                if(Objects.equals(dataType, "NUMBER"))
                    fieldType = FieldType.NUMBER;
                else if(Objects.equals(dataType, "DATE"))
                    fieldType = FieldType.DATE;
                else
                    fieldType = FieldType.STRING;

                Label lblField = new Label(fieldName + ":");
                TextField txtField = new TextField();
                BorderPane bpnField = new BorderPane(null, null, txtField, null, lblField);

                vboxFields.getChildren().add(bpnField);

                // Add the txtField and the Field class to a list to be accessed later
                txtFields.add(txtField);
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
