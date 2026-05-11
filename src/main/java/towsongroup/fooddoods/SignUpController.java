package towsongroup.fooddoods;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class SignUpController {

    @FXML
    private AnchorPane anchorPane;
    @FXML
    private ComboBox<String> roleComboBox;

    @FXML void initialize() {
        roleComboBox.getItems().addAll("Customer", "Driver", "Restaurant Employee", "Restaurant Owner");

    }

    @FXML
    protected void showRelevantSignUpScene() {
        String selection = roleComboBox.getValue();
        FXMLLoader loader = new FXMLLoader();

        if (selection == null) {
            Alert chooseAnOption = new Alert(Alert.AlertType.WARNING, "Please select a role");
            chooseAnOption.show();
            return;
        } else if (selection.equals("Driver")) {
            loader = new FXMLLoader(App.class.getResource("driverSignUpScene.fxml"));
        } else if (selection.equals("Customer")) {
            loader = new FXMLLoader(App.class.getResource("customerSignUpScene.fxml"));
        }else if (selection.equals("Restaurant Employee")) {
            loader = new FXMLLoader(App.class.getResource("restaurantEmployeeSignUpScene.fxml"));
        }else if (selection.equals("Restaurant Owner")) {
            loader = new FXMLLoader(App.class.getResource("restaurantOwnerSignUpScene.fxml"));
        } else {
            Alert chooseAnOption = new Alert(Alert.AlertType.WARNING, "Please select a role");
            chooseAnOption.show();
            return;
        }

        try {
            Pane pane = loader.load();
            anchorPane.getChildren().clear();
            anchorPane.getChildren().add(pane);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
