package towsongroup.fooddoods;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("anchorScene.fxml"));
        Pane anchorPane = loader.load();
        Scene scene = new Scene(anchorPane);
        stage.setTitle("Food Dudes");
        stage.setScene(scene);
        stage.show();

        anchorPane.getChildren().add(new FXMLLoader(App.class.getResource("landingScene.fxml")).load());
    }

    public static void main(String[] args) {
        launch();
    }
}