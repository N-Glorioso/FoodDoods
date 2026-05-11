package towsongroup.fooddoods;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class LogInController {

    @FXML
    AnchorPane anchorPane;
    @FXML
    TextField userNameField;
    @FXML
    TextField passWordField;
    @FXML
    Button continueButton;
    @FXML
    Button cancelButton;

    @FXML
    protected void attemptLogIn() {
        // Implement this
    }

    @FXML
    protected void returnToLanding() {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("landingScene.fxml"));
        try {
            Pane pane = loader.load();
            anchorPane.getChildren().clear();
            anchorPane.getChildren().add(pane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
