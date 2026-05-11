package towsongroup.fooddoods;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class RestaurantOwnerController {

    @FXML
    private AnchorPane anchorPane;
    @FXML
    private TextField userNameField;
    @FXML
    private TextField passWordField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField phoneNumberField;
    @FXML
    private TextField eMailAddressField;
    @FXML
    private TextField restaurantNameField;
    @FXML
    private TextField restaurantAddressField;

    @FXML
    private void initialize() {

    }

    @FXML
    private void populateAvailableOrderList() {

    }

    private void getAvailableOrders() {

    }

    @FXML
    private void rejectOrder() {

    }

    @FXML
    private void markReady() {

    }

    @FXML
    private void markAvailable() {

    }

    @FXML
    private void markUnavailable() {

    }

    @FXML
    private void addItem() {

    }

    @FXML
    private void deleteItem() {

    }

    @FXML
    private void attemptUpdate() {
        String userName = userNameField.getText();
        String passWord = passWordField.getText();
        String name = nameField.getText();
        String phoneNumber = phoneNumberField.getText();
        String eMailAddress = eMailAddressField.getText();
        String restaurantName = restaurantNameField.getText();
        String restaurantAddress = restaurantAddressField.getText();
    }

    @FXML
    private void returnToLanding() {
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
