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
            populateAvailableOrdersList();
            populateCurrentOrderList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void populateAvailableOrdersList() {
        try {
            availableOrdersListView.getItems().clear();

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT Orders.Order_ID, Restaurant.RestaurantName, Customer.Address, Orders.OrderStatus " +
                            "FROM Orders " +
                            "JOIN Restaurant ON Orders.O_Restaurant_ID = Restaurant.Restaurant_ID " +
                            "JOIN Customer ON Orders.O_Customer_ID = Customer.C_ID " +
                            "WHERE Orders.OrderStatus = 'Ready For Pickup' " +
                            "AND Orders.Order_ID NOT IN (" +
                            "SELECT D_Order_ID FROM Driver WHERE D_Order_ID IS NOT NULL)"
            );

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                availableOrdersListView.getItems().add(
                        rs.getString("Order_ID") + " | " +
                                rs.getString("RestaurantName") + " | " +
                                rs.getString("Address") + " | " +
                                rs.getString("OrderStatus")
                );
            }

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    private void populateCurrentOrderList() {
        try {
            currentOrderListView.getItems().clear();

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT Orders.Order_ID, Restaurant.RestaurantName, Customer.Address, Orders.OrderStatus " +
                            "FROM Driver " +
                            "JOIN Orders ON Driver.D_Order_ID = Orders.Order_ID " +
                            "JOIN Restaurant ON Orders.O_Restaurant_ID = Restaurant.Restaurant_ID " +
                            "JOIN Customer ON Orders.O_Customer_ID = Customer.C_ID " +
                            "WHERE Driver.Driver_ID = ?"
            );

            ps.setInt(1, State.id);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                currentOrderListView.getItems().add(
                        rs.getString("Order_ID") + " | " +
                                rs.getString("RestaurantName") + " | " +
                                rs.getString("Address") + " | " +
                                rs.getString("OrderStatus")
                );
            }

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    @FXML
    private void acceptOrder() {
        try {
            String selectedOrder = availableOrdersListView.getSelectionModel().getSelectedItem();

            if (selectedOrder == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("No order selected");
                alert.show();
                return;
            }

            String orderID = selectedOrder.split("\\|")[0].trim();

            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Driver SET D_Order_ID = ? WHERE D_Username = ?"
            );

            ps.setString(1, orderID);
            ps.setString(2, State.userName);

            ps.executeUpdate();

            populateAvailableOrdersList();
            populateCurrentOrderList();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    @FXML
    private void markAsPickedUp() {
        try {
            String selectedOrder = currentOrderListView.getSelectionModel().getSelectedItem();

            if (selectedOrder == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("No order selected");
                alert.show();
                return;
            }

            String orderID = selectedOrder.split("\\|")[0].trim();

            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Orders SET OrderStatus = 'In Transit' " +
                            "WHERE Order_ID = ? AND OrderStatus = 'Ready For Pickup'"
            );

            ps.setString(1, orderID);

            ps.executeUpdate();

            populateCurrentOrderList();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    @FXML
    private void markAsDroppedOff() {
        try {
            String selectedOrder = currentOrderListView.getSelectionModel().getSelectedItem();

            if (selectedOrder == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("No order selected");
                alert.show();
                return;
            }

            String orderID = selectedOrder.split("\\|")[0].trim();

            PreparedStatement updateOrder = conn.prepareStatement(
                    "UPDATE Orders SET OrderStatus = 'Dropped Off' " +
                            "WHERE Order_ID = ? AND OrderStatus = 'In Transit'"
            );

            updateOrder.setString(1, orderID);

            updateOrder.executeUpdate();

            PreparedStatement clearDriverOrder = conn.prepareStatement(
                    "UPDATE Driver SET D_Order_ID = NULL WHERE D_Username = ?"
            );

            clearDriverOrder.setString(1, State.userName);

            clearDriverOrder.executeUpdate();

            populateAvailableOrdersList();
            populateCurrentOrderList();

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
        String name = nameField.getText().isEmpty() ? State.name : nameField.getText();
        String phoneNumber = phoneNumberField.getText().isEmpty() ? State.phoneNumber : phoneNumberField.getText();
        String eMailAddress = eMailAddressField.getText().isEmpty() ? State.eMailAddress : eMailAddressField.getText();
        String vehicleInfo = vehicleField.getText().isEmpty() ? State.vehicleNumber : vehicleField.getText();

        try {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Driver " +
                            "SET D_Password = ?, D_Name = ?, Phone_Num = ?, D_Email = ?, Vehicle_Info = ?, D_Username = ? " +
                            "WHERE Driver_ID = ?"
            );

            ps.setString(1, passWord);
            ps.setString(2, name);
            ps.setString(3, phoneNumber);
            ps.setString(4, eMailAddress);
            ps.setString(5, vehicleInfo);
            ps.setString(6, userName);
            ps.setInt(7, State.id);

            ps.executeUpdate();

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