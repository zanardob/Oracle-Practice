package utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Constraint {
    private String columnName;
    private ObservableList<String> values;

    public Constraint() {
        values = FXCollections.observableArrayList();
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public ObservableList<String> getValues() {
        return values;
    }

    public void setValues(ObservableList<String> values) {
        this.values = values;
    }

    public void addValue(String value){
        values.add(value);
    }


}