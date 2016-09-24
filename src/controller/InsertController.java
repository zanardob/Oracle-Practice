package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import database.DataManager;
import javafx.stage.Stage;
import utils.Constraint;
import utils.Field;
import utils.FieldType;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.*;
import java.util.function.UnaryOperator;

public class InsertController implements Initializable {
    @FXML private VBox vboxFields;

    private DataManager dm;
    private String entityName;
    private TextField txtError;
    private ArrayList<Field> fields;
    private ArrayList<Field> insertedFields;
    private ArrayList<TextField> txtFields;
    private ArrayList<DatePicker> dpFields;
    private ArrayList<ComboBox> cboxFields;
    private ArrayList<Constraint> constraints;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dm = new DataManager();
        fields = new ArrayList<>();
        insertedFields = new ArrayList<>();
        txtFields = new ArrayList<>();
        dpFields = new ArrayList<>();
        cboxFields = new ArrayList<>();
    }

    public void confirm(ActionEvent actionEvent) {
        // Get the text from all the TextFields
        int dp = 0;
        int cb = 0;
        int tf = 0;
        for (Field f : fields) {
            Constraint constraint = getConstraint(constraints, f.getName());
            if (constraint != null) {
                if (!Objects.equals(cboxFields.get(cb).getValue().toString(), "")) {
                    f.setValue(cboxFields.get(cb).getValue().toString());
                    insertedFields.add(f);
                }
                cb++;
            } else if (f.getType() == FieldType.DATE) {
                if (dpFields.get(dp).getValue() != null) {
                    Date date = Date.from(dpFields.get(dp).getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                    f.setValue(new SimpleDateFormat("yyyy/MM/dd").format(date));
                    insertedFields.add(f);
                }
                dp++;
            } else {
                if (!Objects.equals(txtFields.get(tf).getText(), "")) {
                    f.setValue(txtFields.get(tf).getText());
                    insertedFields.add(f);
                }
                tf++;
            }
        }

        try {
            dm.addEntry(entityName, insertedFields);
            txtError.setText("Entry successfully added.");

            closeWindow(null);
        } catch (ClassNotFoundException e) {
            txtError.setText("Check your JDBC driver.");
            insertedFields.clear();
            e.printStackTrace();
        } catch (SQLException e) {
            txtError.setText("SQL Error: " + e.getMessage());
            insertedFields.clear();
            e.printStackTrace();
        }
        closeWindow(null);
    }

    @FXML
    private void closeWindow(ActionEvent actionEvent) {
        Stage stage = (Stage) vboxFields.getScene().getWindow();
        stage.close();
    }

    /**
     * Returns the Constraint that matches the given columnName in the list
     */
    private Constraint getConstraint(ArrayList<Constraint> constraints, String columnName){
        Optional<Constraint> constraint = constraints
                .stream()
                .filter(c -> c.getColumnName().equalsIgnoreCase(columnName))
                .findFirst();

        return constraint.orElse(null);
    }

    void fillDialog(ActionEvent actionEvent){
        try {
            ResultSet rs = dm.getColumnMetadata(entityName);
            constraints = dm.getTableConstraints(entityName);

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

                Label lblField = new Label(fieldName + ": ");

                // Creates the Field object to add to the list
                FieldType fieldType;

                if(Objects.equals(dataType, "NUMBER")) {
                    fieldType = FieldType.NUMBER;
                } else if(Objects.equals(dataType, "DATE")){
                    fieldType = FieldType.DATE;
                } else {
                    fieldType = FieldType.STRING;
                }

                Constraint constraint = getConstraint(constraints, fieldName);
                BorderPane bpnField;
                if(constraint != null){
                    ComboBox<String> cboxValues = new ComboBox<>();
                    cboxValues.setPrefWidth(187);
                    cboxValues.setPrefHeight(31);

                    bpnField = new BorderPane(null, null, cboxValues, null, lblField);

                    cboxValues.setItems(constraint.getValues());
                    cboxFields.add(cboxValues);
                } else if(fieldType == FieldType.DATE) {
                    DatePicker datePicker = new DatePicker();
                    datePicker.setPrefWidth(187);
                    datePicker.setPrefHeight(31);
                    datePicker.setEditable(false);

                    bpnField = new BorderPane(null, null, datePicker, null, lblField);
                    dpFields.add(datePicker);
                } else {
                    TextField txtField = new TextField();
                    bpnField = new BorderPane(null, null, txtField, null, lblField);

                    // Listener to limit the TextField length
                    txtField.lengthProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue.intValue() > oldValue.intValue()) {
                            // Check if the new character is greater than the maxSize
                            if (txtField.getText().length() >= fieldMaxSize) {
                                // Cut the text
                                txtField.setText(txtField.getText().substring(0, fieldMaxSize));
                            }
                        }
                    });

                    if(fieldType == FieldType.NUMBER)
                        txtField.setTextFormatter(new TextFormatter<>(numberFilter));

                    // We need this list to access the values typed in
                    txtFields.add(txtField);
                }

                vboxFields.getChildren().add(bpnField);

                // Add the Field object to a list to be accessed later (in the insert)
                fields.add(new Field(fieldName, fieldType));
            }
        } catch (ClassNotFoundException e) {
            txtError.setText("Check your JDBC driver.");
            e.printStackTrace();
        } catch (SQLException e) {
            txtError.setText("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    void setTxtError(TextField txtError) {
        this.txtError = txtError;
    }
}
