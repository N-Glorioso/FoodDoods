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

public class DriverSignUpController {

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
    private TextField vehicleField;

    @FXML
    private void attemptSignUp() {
        try {
            Connection conn = State.getConn();

            PreparedStatement ps = conn.prepareStatement("INSERT INTO Driver (Driver_ID, D_Name, Vehicle_Info, Phone_Num, D_Email, D_Username, D_Password)\n" +
                    "VALUES (?, ?, ?, ?, ?, ?, ?);");

            String userName = userNameField.getText();
            String passWord = passWordField.getText();
            String name = nameField.getText();
            String phoneNumber = phoneNumberField.getText();
            String eMailAddress = eMailAddressField.getText();
            String vehicleInfo = vehicleField.getText();

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
            ps.setString(2, name);
            State.name = name;
            ps.setString(3, vehicleInfo);
            State.vehicleNumber = vehicleInfo;
            ps.setString(4, phoneNumber);
            State.phoneNumber = phoneNumber;
            ps.setString(5, eMailAddress);
            State.eMailAddress = eMailAddress;
            ps.setString(6, userName);
            State.userName = userName;
            ps.setString(7, passWord);
            State.passWord = passWord;

            ps.executeUpdate();

            Alert successsAlert = new Alert(Alert.AlertType.INFORMATION);
            successsAlert.setContentText("Account creation successful");
            successsAlert.show();
        } catch (Exception e) {
            Alert exceptionAlert = new Alert(Alert.AlertType.ERROR);
            exceptionAlert.setContentText(e.getMessage());
            exceptionAlert.show();
            return;
        }

        FXMLLoader loader = new FXMLLoader(App.class.getResource("driverScene.fxml"));
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