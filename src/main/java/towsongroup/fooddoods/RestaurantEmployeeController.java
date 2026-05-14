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

public class RestaurantEmployeeController {

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
    private TextField restaurantNameField;
    @FXML
    private ListView<String> availableOrderList;
    @FXML
    private ListView<String> menuListView;

    @FXML
    private void initialize() {
        try {
            conn = State.getConn();

            populateAccountFields();
            populateAvailableOrderList();
            populateMenuList();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void populateAccountFields() {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT rw.RW_Name, rw.RW_PhoneNum, rw.Email, rw.RW_Username, rw.RW_Password, " +
                            "r.RestaurantName " +
                            "FROM Restaurant_Worker rw " +
                            "JOIN Restaurant r ON rw.RW_Restaurant_ID = r.Restaurant_ID " +
                            "WHERE rw.RW_Username = ?"
            );

            ps.setString(1, State.userName);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                nameField.setText(rs.getString("RW_Name"));
                phoneNumberField.setText(rs.getString("RW_PhoneNum"));
                eMailAddressField.setText(rs.getString("Email"));
                userNameField.setText(rs.getString("RW_Username"));
                passWordField.setText(rs.getString("RW_Password"));
                restaurantNameField.setText(rs.getString("RestaurantName"));
            }

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    private void populateAvailableOrderList() {
        try {
            availableOrderList.getItems().clear();

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT Order_ID, O_Customer_ID, OrderStatus " +
                            "FROM Orders " +
                            "WHERE O_Restaurant_ID = ? " +
                            "AND OrderStatus = 'Order Placed'"
            );

            ps.setInt(1, getRestaurantID());

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                availableOrderList.getItems().add(
                        rs.getString("Order_ID") + " | " +
                                rs.getString("O_Customer_ID") + " | " +
                                rs.getString("OrderStatus")
                );
            }

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    private void populateMenuList() {
        try {
            menuListView.getItems().clear();

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT MenuItem_ID, MenuItemName, Price, Availability " +
                            "FROM Menu_Item " +
                            "WHERE MI_Restaurant_ID = ?"
            );

            ps.setInt(1, getRestaurantID());

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                menuListView.getItems().add(
                        rs.getString("MenuItem_ID") + " | " +
                                rs.getString("MenuItemName") + " | $" +
                                rs.getString("Price") + " | " +
                                (rs.getInt("Availability") == 1 ? "Available" : "Unavailable")
                );
            }

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    @FXML
    private void rejectOrder() {
        try {
            String selectedOrder = availableOrderList.getSelectionModel().getSelectedItem();

            if (selectedOrder == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("No order selected");
                alert.show();
                return;
            }

            String orderID = selectedOrder.split("\\|")[0].trim();

            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Orders " +
                            "SET OrderStatus = 'Rejected' " +
                            "WHERE Order_ID = ? " +
                            "AND O_Restaurant_ID = ? " +
                            "AND OrderStatus = 'Order Placed'"
            );

            ps.setString(1, orderID);
            ps.setInt(2, getRestaurantID());

            ps.executeUpdate();

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setContentText("Order rejected");
            alert.show();

            populateAvailableOrderList();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    @FXML
    private void markReady() {
        try {
            String selectedOrder = availableOrderList.getSelectionModel().getSelectedItem();

            if (selectedOrder == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("No order selected");
                alert.show();
                return;
            }

            String orderID = selectedOrder.split("\\|")[0].trim();

            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Orders " +
                            "SET OrderStatus = 'Ready For Pickup' " +
                            "WHERE Order_ID = ? " +
                            "AND O_Restaurant_ID = ?"
            );

            ps.setString(1, orderID);
            ps.setInt(2, getRestaurantID());

            ps.executeUpdate();

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setContentText("Order marked ready for pickup");
            alert.show();

            populateAvailableOrderList();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    @FXML
    private void markAvailable() {
        updateMenuItemAvailability(1);
    }

    @FXML
    private void markUnavailable() {
        updateMenuItemAvailability(0);
    }

    private void updateMenuItemAvailability(int availability) {
        try {
            String selectedItem = menuListView.getSelectionModel().getSelectedItem();

            if (selectedItem == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("No menu item selected");
                alert.show();
                return;
            }

            String menuItemID = selectedItem.split("\\|")[0].trim();

            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Menu_Item " +
                            "SET Availability = ? " +
                            "WHERE MenuItem_ID = ? " +
                            "AND MI_Restaurant_ID = ?"
            );

            ps.setInt(1, availability);
            ps.setString(2, menuItemID);
            ps.setInt(3, getRestaurantID());

            ps.executeUpdate();

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setContentText("Menu item updated");
            alert.show();

            populateMenuList();

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

        try {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Restaurant_Worker " +
                            "SET RW_Name = ?, RW_PhoneNum = ?, Email = ?, RW_Username = ?, RW_Password = ? " +
                            "WHERE RW_Username = ?"
            );

            ps.setString(1, name);
            ps.setString(2, phoneNumber);
            ps.setString(3, eMailAddress);
            ps.setString(4, userName);
            ps.setString(5, passWord);
            ps.setString(6, State.userName);

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

    private int getRestaurantID() throws Exception {
        PreparedStatement ps = conn.prepareStatement(
                "SELECT RW_Restaurant_ID " +
                        "FROM Restaurant_Worker " +
                        "WHERE RW_Username = ?"
        );

        ps.setString(1, State.userName);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt("RW_Restaurant_ID");
        }

        throw new Exception("Could not find restaurant for this worker");
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