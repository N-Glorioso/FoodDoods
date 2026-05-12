package towsongroup.fooddoods;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LogInController {

    @FXML
    AnchorPane anchorPane;
    @FXML
    ComboBox<String> roleComboBox;
    @FXML
    TextField userNameField;
    @FXML
    TextField passWordField;

    @FXML void initialize() {
        roleComboBox.getItems().addAll("Customer", "Driver", "Restaurant Employee", "Restaurant Owner", "Admin");
    }

    @FXML
    protected void attemptLogIn() {
        String selection = roleComboBox.getValue();
        FXMLLoader loader;

        try {
            Connection conn = State.getConn();

            if (selection == null) {
                Alert chooseAnOption = new Alert(Alert.AlertType.WARNING, "Please select a role");
                chooseAnOption.show();
                return;
            } else if (selection.equals("Driver")) {
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM Driver " +
                        "WHERE D_Username = ? AND D_Password = ?;");

                ps.setString(1, userNameField.getText());
                ps.setString(2, passWordField.getText());

                ResultSet rs = ps.executeQuery();
                rs.next();

                State.userName = rs.getString("D_Username");
                State.passWord = rs.getString("D_Password");
                State.name = rs.getString("D_Name");
                State.phoneNumber = rs.getString("Phone_Num");
                State.eMailAddress = rs.getString("D_Email");
                State.vehicleNumber = rs.getString("Vehicle_Info");

                System.out.println(State.name);

                loader = new FXMLLoader(App.class.getResource("driverScene.fxml"));
            } else if (selection.equals("Customer")) {
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM Customer " +
                        "WHERE C_Username = ? AND C_Password = ?;");

                ps.setString(1, userNameField.getText());
                ps.setString(2, passWordField.getText());

                ResultSet rs = ps.executeQuery();
                rs.next();

                State.userName = rs.getString("C_Username");
                State.passWord = rs.getString("C_Password");
                State.name = rs.getString("C_Name");
                State.phoneNumber = rs.getString("C_Phone_Num");
                State.eMailAddress = rs.getString("C_Email");
                State.homeAddress = rs.getString("Address");
                State.payment = rs.getString("Payment_Info");

                loader = new FXMLLoader(App.class.getResource("customerScene.fxml"));
            }else if (selection.equals("Restaurant Employee")) {
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM Restaurant_Worker " +
                        "WHERE RW_Username = ? AND RW_Password = ?;");

                ps.setString(1, userNameField.getText());
                ps.setString(2, passWordField.getText());

                ResultSet rs = ps.executeQuery();
                rs.next();

                State.userName = rs.getString("RW_Username");
                State.passWord = rs.getString("RW_Password");
                State.name = rs.getString("RW_Name");
                State.phoneNumber = rs.getString("RW_PhoneNum");
                State.eMailAddress = rs.getString("Email");

                loader = new FXMLLoader(App.class.getResource("restaurantEmployeeScene.fxml"));
            }else if (selection.equals("Restaurant Owner")) {
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM Restaurant_Owner " +
                        "WHERE C_Username = ? AND C_Password = ?;");

                ps.setString(1, userNameField.getText());
                ps.setString(2, passWordField.getText());

                ResultSet rs = ps.executeQuery();
                rs.next();

                State.userName = rs.getString("RO_Username");
                State.passWord = rs.getString("RO_Password");
                State.name = rs.getString("OwnerName");
                State.phoneNumber = rs.getString("RO_PhoneNum");
                State.eMailAddress = rs.getString("Email");

                loader = new FXMLLoader(App.class.getResource("restaurantOwnerScene.fxml"));
            }else if (selection.equals("Admin")) {
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT * FROM Administrator " +
                                "WHERE A_Username = ? AND A_Password = ?;"
                );

                ps.setString(1, userNameField.getText());
                ps.setString(2, passWordField.getText());

                ResultSet rs = ps.executeQuery();
                rs.next();

                State.userName = rs.getString("A_Username");
                State.passWord = rs.getString("A_Password");
                State.eMailAddress = rs.getString("A_Email");
                State.phoneNumber = rs.getString("A_PhoneNum");

                loader = new FXMLLoader(App.class.getResource("adminScene.fxml"));
            } else {
                Alert chooseAnOption = new Alert(Alert.AlertType.WARNING, "Please select a role");
                chooseAnOption.show();
                return;
            }
        } catch (Exception e) {
            Alert exceptionAlert = new Alert(Alert.AlertType.ERROR);
            exceptionAlert.setContentText(e.getMessage());
            exceptionAlert.show();
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
