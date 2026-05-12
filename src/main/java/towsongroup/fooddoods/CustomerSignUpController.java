package towsongroup.fooddoods;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CustomerSignUpController {

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
    private void attemptSignUp() {
        try {
            Connection conn = State.getConn();

            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Customer (C_ID, C_Name, Payment_Info, Address, C_Phone_Num, C_Email, C_Username, C_Password)\n" +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?);");

            String name = nameField.getText();
            String payment = paymentField.getText();
            String address = addressField.getText();
            String phoneNumber = phoneNumberField.getText();
            String eMail = eMailAddressField.getText();
            String userName = userNameField.getText();
            String passWord = passWordField.getText();

            // Generate ID and check availability
            int randID = (int) (Math.random() * 1000000);
            PreparedStatement getIDs = conn.prepareStatement("SELECT C_ID FROM Customer");
            ResultSet ids = getIDs.executeQuery();
            outerloop: while (true) {
                randID = (int) (Math.random() * 1000000);
                while (ids.next()) {
                    if (randID == ids.getInt(1)) {
                        continue outerloop;
                    }
                }
                break;
            }
            ps.setString(1, Integer.toString(randID));
            ps.setString(2, name);
            State.name = name;
            ps.setString(3, payment);
            State.payment = payment;
            ps.setString(4, address);
            State.homeAddress = address;
            ps.setString(5, phoneNumber);
            State.phoneNumber = phoneNumber;
            ps.setString(6, eMail);
            State.eMailAddress = eMail;
            ps.setString(7, userName);
            State.userName = userName;
            ps.setString(8, passWord);
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

        FXMLLoader loader = new FXMLLoader(App.class.getResource("customerScene.fxml"));
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
            State.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
