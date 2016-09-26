package controller;

import database.DataManager;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import util.Entity;
import util.UserPrivilege;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class PrivilegeController implements Initializable {
    @FXML private TableView<UserPrivilege> privilegeTableView;
    @FXML private TableColumn<UserPrivilege, String> User;
    @FXML private TableColumn<UserPrivilege, String> Privilege;

    private Entity entity;
    private TextField txtError;
    private DataManager dm;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dm = new DataManager();
    }

    /**
     * Get the privileges for the selected table on the ComboBox
     */
    void fillTableView(ActionEvent actionEvent){
        try {
            ObservableList<UserPrivilege> privileges = dm.getPrivileges(entity);
            privilegeTableView.setItems(privileges);

            User.setCellValueFactory(new PropertyValueFactory<>("User"));
            Privilege.setCellValueFactory(new PropertyValueFactory<>("Privilege"));
        } catch (ClassNotFoundException e) {
            txtError.setText("Check your JDBC driver.");
            e.printStackTrace();
        } catch (SQLException e) {
            txtError.setText("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void closeWindow(ActionEvent actionEvent) {
        Stage stage = (Stage) privilegeTableView.getScene().getWindow();
        stage.close();
    }

    void setEntity(Entity entity) {
        this.entity = entity;
    }

    void setTxtError(TextField txtError) {
        this.txtError = txtError;
    }
}
