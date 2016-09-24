import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] argv) {
        launch(argv);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/loginview.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle("Sisgate - Sistema Gerenciador de Atividades Eleitorais");
        stage.setScene(new Scene(root));
        stage.show();
    }
}
