import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainStage extends Application {
    private MainController controller;
    private Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        setupScene();
        primaryStage.setTitle("Labd");
        primaryStage.show();
    }

    private void setupScene() {
        VBox vbox = new VBox();

        HBox hbox = new HBox();
        Label lblSelect = new Label("Select table:");
        ComboBox<String> cboxTableSelect = new ComboBox<>();
        cboxTableSelect.setId("comboBox");
        Button btnInsert = new Button("Add entry");

        hbox.getChildren().addAll(lblSelect, cboxTableSelect, btnInsert);

        TableView tableView = new TableView();
        TextField txtError = new TextField("No errors");
        txtError.setDisable(true);

        vbox.getChildren().addAll(hbox, tableView, txtError);

        controller = new MainController(cboxTableSelect, tableView, btnInsert, txtError);
        controller.fillComboBox();

        Scene scene = new Scene(vbox);
        primaryStage.setScene(scene);
    }
}
