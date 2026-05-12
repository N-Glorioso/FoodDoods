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

public class RestaurantOwnerSignUpController {

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
    private void attemptSignIn() {
        try {
            // Set up connection
            Connection conn = State.getConn();

            // Set up owner account
            PreparedStatement ownerStatement = conn.prepareStatement("INSERT INTO Restaurant_Owner (Owner_ID, OwnerName, RO_PhoneNum, Email, RO_Username, RO_Password)\n" +
                    "VALUES (?, ?, ?, ?, ?, ?);");

            String userName = userNameField.getText();
            String passWord = passWordField.getText();
            String name = nameField.getText();
            String phoneNumber = phoneNumberField.getText();
            String eMailAddress = eMailAddressField.getText();
            String restaurantName = restaurantNameField.getText();
            String restaurantAddress = restaurantAddressField.getText();

            // Generate Owner ID and check availability
            int ownerID = (int) (Math.random() * 1000000);
            ArrayList<Integer> usedIDs = State.getIDs();

            while (true) {
                if (usedIDs.contains(ownerID)) {
                    ownerID = (int) (Math.random() * 1000000);
                } else {
                    break;
                }
            }

            ownerStatement.setString(1, Integer.toString(ownerID));

            ownerStatement.setString(2, name);
            State.name = name;
            ownerStatement.setString(3, phoneNumber);
            State.phoneNumber = phoneNumber;
            ownerStatement.setString(4, eMailAddress);
            State.eMailAddress = eMailAddress;
            ownerStatement.setString(5, userName);
            State.userName = userName;
            ownerStatement.setString(6, passWord);
            State.passWord = passWord;

            // Set up restaurant entry
            PreparedStatement restaurantStatement = conn.prepareStatement("INSERT INTO Restaurant (Restaurant_ID, R_Owner_ID, Address, RestaurantName)\n" +
                    "VALUES (?, ?, ?, ?);");

            // Generate Restaurant ID and check availability
            int restaurantID = (int) (Math.random() * 1000000);

            while (true) {
                if (usedIDs.contains(restaurantID)) {
                    restaurantID = (int) (Math.random() * 1000000);
                } else {
                    break;
                }
            }

            restaurantStatement.setString(1, Integer.toString(restaurantID));
            restaurantStatement.setString(2, Integer.toString(ownerID));
            restaurantStatement.setString(3, restaurantAddress);
            restaurantStatement.setString(4, restaurantName);

            conn.setAutoCommit(false);
            ownerStatement.executeUpdate();
            restaurantStatement.executeUpdate();

            PreparedStatement assignRestID = conn.prepareStatement("UPDATE Restaurant_Owner\n" +
                    "SET RO_Restaurant_ID = ?\n" +
                    "WHERE Owner_ID = ?");
            assignRestID.setString(1, Integer.toString(restaurantID));
            assignRestID.setString(2, Integer.toString(ownerID));

            assignRestID.executeUpdate();

            try {
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw new Exception("Error");
            }

            Alert successsAlert = new Alert(Alert.AlertType.INFORMATION);
            successsAlert.setContentText("Account creation successful");
            successsAlert.show();
        } catch (Exception e) {
            Alert exceptionAlert = new Alert(Alert.AlertType.ERROR);
            exceptionAlert.setContentText(e.getMessage());
            exceptionAlert.show();
            return;
        }

        FXMLLoader loader = new FXMLLoader(App.class.getResource("restaurantOwnerScene.fxml"));
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
