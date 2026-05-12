package towsongroup.fooddoods;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

public class DriverController {

    private Connection conn;

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
    private ListView<String> availableOrdersListView;
    @FXML
    private ListView<String> currentOrderListView;

    @FXML
    private void initialize() {
        try {
            conn = State.getConn();
            getAvailableOrders();
            getCurrentOrder();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getAvailableOrders() {
        try {
            availableOrdersListView.getItems().clear();

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT Orders.Order_ID, Restaurant.RestaurantName, Customer.Address, Orders.Status " +
                            "FROM Orders " +
                            "JOIN Restaurant ON Orders.O_Restaurant_ID = Restaurant.Restaurant_ID " +
                            "JOIN Customer ON Orders.O_Customer_ID = Customer.C_ID " +
                            "WHERE Orders.Status = 'Ready To Pickup' " +
                            "AND Orders.Order_ID NOT IN ( " +
                            "SELECT D_Order_ID FROM Driver WHERE D_Order_ID IS NOT NULL)"
            );

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                availableOrdersListView.getItems().add(
                        rs.getString("Order_ID") + " | " +
                                rs.getString("RestaurantName") + " | " +
                                rs.getString("Address") + " | " +
                                rs.getString("Status")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getCurrentOrder() {
        try {
            currentOrderListView.getItems().clear();

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT Orders.Order_ID, Restaurant.RestaurantName, Customer.Address, Orders.Status " +
                            "FROM Driver " +
                            "JOIN Orders ON Driver.D_Order_ID = Orders.Order_ID " +
                            "JOIN Restaurant ON Orders.O_Restaurant_ID = Restaurant.Restaurant_ID " +
                            "JOIN Customer ON Orders.O_Customer_ID = Customer.C_ID " +
                            "WHERE Driver.D_Username = ?"
            );

            ps.setString(1, State.userName);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                currentOrderListView.getItems().add(
                        rs.getString("Order_ID") + " | " +
                                rs.getString("RestaurantName") + " | " +
                                rs.getString("Address") + " | " +
                                rs.getString("Status")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void rejectOrder() {
        availableOrdersListView.getSelectionModel().clearSelection();
    }

    @FXML
    private void acceptOrder() {
        try {
            String selectedOrder = availableOrdersListView.getSelectionModel().getSelectedItem();

            if (selectedOrder == null) {
                return;
            }

            String orderID = selectedOrder.split(" \\| ")[0];

            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Driver " +
                            "SET D_Order_ID = ? " +
                            "WHERE D_Username = ?"
            );

            ps.setString(1, orderID);
            ps.setString(2, State.userName);

            ps.executeUpdate();

            getAvailableOrders();
            getCurrentOrder();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void markAsPickedUp() {
        try {
            String selectedOrder = currentOrderListView.getSelectionModel().getSelectedItem();

            if (selectedOrder == null) {
                return;
            }

            String orderID = selectedOrder.split(" \\| ")[0];

            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Orders " +
                            "SET Status = 'In Transit' " +
                            "WHERE Order_ID = ? " +
                            "AND Status = 'Ready To Pickup'"
            );

            ps.setString(1, orderID);

            ps.executeUpdate();

            getCurrentOrder();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void markAsDroppedOff() {
        try {
            String selectedOrder = currentOrderListView.getSelectionModel().getSelectedItem();

            if (selectedOrder == null) {
                return;
            }

            String orderID = selectedOrder.split(" \\| ")[0];

            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Orders " +
                            "SET Status = 'Dropped Off' " +
                            "WHERE Order_ID = ? " +
                            "AND Status = 'In Transit'"
            );

            ps.setString(1, orderID);

            ps.executeUpdate();

            PreparedStatement clearDriverOrder = conn.prepareStatement(
                    "UPDATE Driver " +
                            "SET D_Order_ID = NULL " +
                            "WHERE D_Username = ?"
            );

            clearDriverOrder.setString(1, State.userName);
            clearDriverOrder.executeUpdate();

            getAvailableOrders();
            getCurrentOrder();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void updateAccount() {
        String userName = userNameField.getText();
        String passWord = passWordField.getText();
        String name = nameField.getText();
        String phoneNumber = phoneNumberField.getText();
        String eMailAddress = eMailAddressField.getText();
        String vehicleInfo = vehicleField.getText();

        try {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Driver " +
                            "SET D_Password = ?, D_Name = ?, Phone_Num = ?, D_Email = ?, Vehicle_Info = ? " +
                            "WHERE D_Username = ?"
            );

            ps.setString(1, passWord);
            ps.setString(2, name);
            ps.setString(3, phoneNumber);
            ps.setString(4, eMailAddress);
            ps.setString(5, vehicleInfo);
            ps.setString(6, userName);

            ps.executeUpdate();

        } catch (Exception e) {
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