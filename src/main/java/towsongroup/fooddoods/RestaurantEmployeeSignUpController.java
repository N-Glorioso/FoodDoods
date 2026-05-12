package towsongroup.fooddoods;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class RestaurantEmployeeSignUpController {

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
    private void attemptSignUp() {
        try {
            // Set up connection
            Connection conn = State.getConn();

            // Set up main query
            PreparedStatement ps = conn.prepareStatement("INSERT INTO Restaurant_Worker (Worker_ID, RW_Restaurant_ID, RW_Name, RW_PhoneNum, Email, RW_Username, RW_Password)\n" +
                    "VALUES (?, ?, ?, ?, ?, ?, ?);");

            // Get values from text fields
            String userName = userNameField.getText();
            String passWord = passWordField.getText();
            String name = nameField.getText();
            String phoneNumber = phoneNumberField.getText();
            String eMailAddress = eMailAddressField.getText();
            String restaurantName = restaurantNameField.getText();

            // Generate ID and check availability
            int randID = (int) (Math.random() * 1000000);

            ArrayList<Integer> usedIDs = State.getIDs();

            while (true) {
                if (usedIDs.contains(randID)) {
                    randID = (int) (Math.random() * 1000000);
                } else {
                    break;
                }
            }

            ps.setString(1, Integer.toString(randID));

            // Ensure restaurant exists
            PreparedStatement getRestaurants = conn.prepareStatement("SELECT RestaurantName FROM Restaurant");
            ResultSet restaurantNames = getRestaurants.executeQuery();
            restaurantNames.next();
            while (true) {
                if (restaurantName.equals(restaurantNames.getString(1))) {
                    break;
                }
                if (!restaurantNames.next()) {
                    throw new Exception(restaurantName + " is not signed up with FoodDoods");
                }
            }


            //
            PreparedStatement selectRestaurantID = conn.prepareStatement("SELECT Restaurant_ID FROM Restaurant\n" +
                    "WHERE RestaurantName = ?");
            selectRestaurantID.setString(1, restaurantName);
            ResultSet restaurantID = selectRestaurantID.executeQuery();
            restaurantID.next();
            ps.setString(2, restaurantID.getString(1));
            State.restaurantName = restaurantName;
            ps.setString(3, name);
            State.name = name;
            ps.setString(4, phoneNumber);
            State.phoneNumber = phoneNumber;
            ps.setString(5, eMailAddress);
            State.eMailAddress = eMailAddress;
            ps.setString(6, userName);
            State.userName = userName;
            ps.setString(7, passWord);
            State.passWord = passWord;

            ps.executeUpdate();

            System.out.println(State.restaurantName);

            Alert successsAlert = new Alert(Alert.AlertType.INFORMATION);
            successsAlert.setContentText("Account creation successful");
            successsAlert.show();
        } catch (Exception e) {
            Alert exceptionAlert = new Alert(Alert.AlertType.ERROR);
            exceptionAlert.setContentText(e.getMessage());
            exceptionAlert.show();
            return;
        }

        FXMLLoader loader = new FXMLLoader(App.class.getResource("restaurantEmployeeScene.fxml"));
        try {
            Pane pane = loader.load();
            anchorPane.getChildren().clear();
            anchorPane.getChildren().add(pane);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
