package towsongroup.fooddoods;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AdminController {

    private Connection conn;

    @FXML
    private AnchorPane anchorPane;
    @FXML
    private ListView<String> customerListView;
    @FXML
    private ListView<String> driverListView;
    @FXML
    private ListView<String> restaurantOwnerListView;
    @FXML
    private ListView<String> restaurantEmployeeListView;
    @FXML
    private ListView<String> orderListView;
    @FXML
    private TextField userNameField;
    @FXML
    private TextField passWordField;
    @FXML
    private TextField phoneNumberField;
    @FXML
    private TextField eMailAddressField;

    @FXML
    private void initialize() {
        try {
            conn = State.getConn();

            populateCustomerList();
            populateDriverList();
            populateRestaurantOwnerList();
            populateRestaurantEmployeeList();
            populateAccountFields();
            populateOrderList();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void populateCustomerList() {
        try {
            customerListView.getItems().clear();

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT C_ID, C_Name, Address, C_Phone_Num, C_Email, C_Username " +
                            "FROM Customer"
            );

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                customerListView.getItems().add(
                        rs.getString("C_ID") + " | " +
                                rs.getString("C_Name") + " | " +
                                rs.getString("Address") + " | " +
                                rs.getString("C_Phone_Num") + " | " +
                                rs.getString("C_Email") + " | " +
                                rs.getString("C_Username")
                );
            }

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    @FXML
    private void populateDriverList() {
        try {
            driverListView.getItems().clear();

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT Driver_ID, D_Order_ID, D_Name, Vehicle_Info, Phone_Num, D_Email, D_Username " +
                            "FROM Driver"
            );

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String orderID = rs.getString("D_Order_ID");

                if (orderID == null) {
                    orderID = "No Current Order";
                }

                driverListView.getItems().add(
                        rs.getString("Driver_ID") + " | " +
                                orderID + " | " +
                                rs.getString("D_Name") + " | " +
                                rs.getString("Vehicle_Info") + " | " +
                                rs.getString("Phone_Num") + " | " +
                                rs.getString("D_Email") + " | " +
                                rs.getString("D_Username")
                );
            }

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    @FXML
    private void populateRestaurantOwnerList() {
        try {
            restaurantOwnerListView.getItems().clear();

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT ro.Owner_ID, ro.OwnerName, ro.Email, ro.RO_PhoneNum, ro.RO_Username, r.RestaurantName " +
                            "FROM Restaurant_Owner ro " +
                            "JOIN Restaurant r ON ro.RO_Restaurant_ID = r.Restaurant_ID"
            );

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                restaurantOwnerListView.getItems().add(
                        rs.getString("Owner_ID") + " | " +
                                rs.getString("OwnerName") + " | " +
                                rs.getString("RestaurantName") + " | " +
                                rs.getString("RO_PhoneNum") + " | " +
                                rs.getString("Email") + " | " +
                                rs.getString("RO_Username")
                );
            }

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    @FXML
    private void populateRestaurantEmployeeList() {
        try {
            restaurantEmployeeListView.getItems().clear();

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT rw.Worker_ID, rw.RW_Name, rw.Email, rw.RW_PhoneNum, rw.RW_Username, r.RestaurantName " +
                            "FROM Restaurant_Worker rw " +
                            "JOIN Restaurant r ON rw.RW_Restaurant_ID = r.Restaurant_ID"
            );

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                restaurantEmployeeListView.getItems().add(
                        rs.getString("Worker_ID") + " | " +
                                rs.getString("RW_Name") + " | " +
                                rs.getString("RestaurantName") + " | " +
                                rs.getString("RW_PhoneNum") + " | " +
                                rs.getString("Email") + " | " +
                                rs.getString("RW_Username")
                );
            }

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    @FXML
    private void populateOrderList() {
        try {
            orderListView.getItems().clear();

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT o.Order_ID, o.OrderStatus, " +
                            "c.C_Name, r.RestaurantName " +
                            "FROM Orders o " +
                            "JOIN Customer c ON o.O_Customer_ID = c.C_ID " +
                            "JOIN Restaurant r ON o.O_Restaurant_ID = r.Restaurant_ID " +
                            "ORDER BY o.OrderStatus"
            );

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                orderListView.getItems().add(
                        rs.getString("Order_ID") + " | " +
                                rs.getString("OrderStatus") + " | " +
                                rs.getString("C_Name") + " | " +
                                rs.getString("RestaurantName")
                );
            }

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    private void populateAccountFields() {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT A_Username, A_Password, A_PhoneNum, A_Email " +
                            "FROM Administrator " +
                            "WHERE A_Username = ?"
            );

            ps.setString(1, State.userName);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                userNameField.setText(rs.getString("A_Username"));
                passWordField.setText(rs.getString("A_Password"));
                phoneNumberField.setText(rs.getString("A_PhoneNum"));
                eMailAddressField.setText(rs.getString("A_Email"));
            }

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    @FXML
    private void updateAccount() {
        String userName = userNameField.getText().isEmpty() ? State.userName : userNameField.getText();
        String passWord = passWordField.getText().isEmpty() ? State.passWord : passWordField.getText();
        String phoneNumber = phoneNumberField.getText().isEmpty() ? State.phoneNumber : phoneNumberField.getText();
        String eMailAddress = eMailAddressField.getText().isEmpty() ? State.eMailAddress : eMailAddressField.getText();

        try {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Administrator " +
                            "SET A_Username = ?, A_Password = ?, A_PhoneNum = ?, A_Email = ? " +
                            "WHERE A_Username = ?"
            );

            ps.setString(1, userName);
            ps.setString(2, passWord);
            ps.setString(3, phoneNumber);
            ps.setString(4, eMailAddress);
            ps.setString(5, State.userName);

            ps.executeUpdate();

            State.userName = userName;
            State.passWord = passWord;
            State.phoneNumber = phoneNumber;
            State.eMailAddress = eMailAddress;

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setContentText("Account Updated");
            alert.show();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();
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