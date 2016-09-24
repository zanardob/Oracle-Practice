package controller;

import database.DatabaseConnector;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {
    @FXML private TextField txtLogin;
    @FXML private PasswordField pwdField;

    public void authenticate(ActionEvent actionEvent) {
        DatabaseConnector.setCredentials(txtLogin.getText(), pwdField.getText());

        try {
            DatabaseConnector.getConnection();

            Stage main = (Stage) txtLogin.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/mainview.fxml"));
            Parent root = loader.load();

            MainController controller = loader.getController();
            controller.fillComboBox();

            main.setScene(new Scene(root));
            main.centerOnScreen();
            main.show();

        } catch (ClassNotFoundException e) {
            showErrorAlert("JDBC driver failure!", "Check your JDBC driver.");
            e.printStackTrace();
        } catch (SQLException e) {
            showErrorAlert("SQL Error!", e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            showErrorAlert("Application Error!", "Application files corrupted.");
            e.printStackTrace();
        }
    }

    private void showErrorAlert(String header, String content){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(header);
        alert.setContentText(content);

        alert.showAndWait();
    }
}
