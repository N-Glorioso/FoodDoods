package towsongroup.fooddoods;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.ArrayList;

public class CustomerController {

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
    private TextField addressField;
    @FXML
    private TextField paymentField;

    @FXML
    private void initialize() {
        // Populate restaurant list
    }

    @FXML
    private void selectRestaurant(){

    }

    @FXML
    private void populateMenu() {
        // Get menuItems

        // populate the scrollbox
    }

    private void getMenuItems() {
        // Get the menu items, decide what to return them as
    }

    @FXML
    private void addToOrder() {

    }

    @FXML
    private void removeFromOrder() {

    }

    @FXML
    private void placeOrder(){

    }

    @FXML
    private void populateOrderList() {

    }

    private void getOrders() {

    }

    @FXML
    private void updateAccount() {
        String userName = userNameField.getText();
        String passWord = passWordField.getText();
        String name = nameField.getText();
        String phoneNumber = phoneNumberField.getText();
        String eMailAddress = eMailAddressField.getText();
        String address = addressField.getText();
        String payment = paymentField.getText();

        // implement the SQL query
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

    @FXML
    private void deleteAccount() {

    }
}
