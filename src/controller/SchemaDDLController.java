package controller;

import database.DataManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;

import javax.sound.sampled.Clip;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class SchemaDDLController implements Initializable {
    @FXML TextArea txaDDL;

    private DataManager dm;
    private TextField txtError;

    final Clipboard clipboard = Clipboard.getSystemClipboard();
    ClipboardContent content;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dm = new DataManager();
        content = new ClipboardContent();
    }

    public void closeWindow(ActionEvent actionEvent) {
        Stage stage = (Stage) txaDDL.getScene().getWindow();
        stage.close();
    }

    public void copyCode(ActionEvent actionEvent) {
        content.clear();
        content.putString(txaDDL.getText());
        clipboard.setContent(content);
    }

    public void fillTextArea(ActionEvent actionEvent){
        try{
            ArrayList<String> tableDDLs = dm.getTableDDL();

            // Clear the TextArea
            txaDDL.setText("");

            // For each table on the list, add its DDL to the TextArea
            for(String s : tableDDLs){
                txaDDL.appendText(s);
            }
        } catch (ClassNotFoundException e) {
            txtError.setText("Check your JDBC driver.");
            e.printStackTrace();
        } catch (SQLException e) {
            txtError.setText("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setTxtError(TextField txtError) {
        this.txtError = txtError;
    }
}
