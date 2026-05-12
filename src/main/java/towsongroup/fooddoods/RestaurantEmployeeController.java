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
    private ListView<String> menuListView;
    @FXML
    private TextField orderIDField;
    @FXML
    private TextField menuItemIDField;
    @FXML
    private ListView<String> availableOrderList;

    @FXML
    private void initialize() {
        try {
            Connection conn = State.getConn();

            PreparedStatement statement = conn.prepareStatement(
                    "SELECT rw.RW_Name, rw.RW_PhoneNum, rw.Email, rw.RW_Username, rw.RW_Password, " +
                            "r.RestaurantName " +
                            "FROM Restaurant_Worker rw " +
                            "JOIN Restaurant r ON rw.RW_Restaurant_ID = r.Restaurant_ID " +
                            "WHERE rw.RW_Username = ?"
            );

            statement.setString(1, State.userName);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                nameField.setText(rs.getString("RW_Name"));
                phoneNumberField.setText(rs.getString("RW_PhoneNum"));
                eMailAddressField.setText(rs.getString("Email"));
                userNameField.setText(rs.getString("RW_Username"));
                passWordField.setText(rs.getString("RW_Password"));
                restaurantNameField.setText(rs.getString("RestaurantName"));
            }

            populateMenuList();
            populateAvailableOrderList();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void populateMenuList() {
        if (menuListView == null) {
            return;
        }

        menuListView.getItems().clear();

        try {
            Connection conn = State.getConn();

            PreparedStatement statement = conn.prepareStatement(
                    "SELECT MenuItem_ID, MenuItemName, Price, Availability " +
                            "FROM Menu_Item " +
                            "WHERE MI_Restaurant_ID = ?"
            );

            statement.setString(1, getRestaurantID());

            ResultSet rs = statement.executeQuery();

            while (rs.next()) {

                String item =
                        rs.getString("MenuItem_ID") + " | " +
                                rs.getString("MenuItemName") + " | $" +
                                rs.getString("Price") + " | " +
                                (rs.getInt("Availability") == 1 ? "Available" : "Unavailable");

                menuListView.getItems().add(item);
            }

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void populateAvailableOrderList() {
        if (availableOrderList == null) {
            return;
        }

        availableOrderList.getItems().clear();

        try {
            ResultSet rs = getAvailableOrders();

            while (rs.next()) {
                String orderInfo = "Order ID: " + rs.getString("Order_ID") +
                        " | Customer ID: " + rs.getString("O_Customer_ID") +
                        " | Status: " + rs.getString("Status");

                availableOrderList.getItems().add(orderInfo);
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private ResultSet getAvailableOrders() throws Exception {
        Connection conn = State.getConn();

        PreparedStatement statement = conn.prepareStatement(
                "SELECT * " +
                        "FROM Orders " +
                        "WHERE O_Restaurant_ID = ? " +
                        "AND Status = 'Preparing'"
        );

        statement.setString(1, getRestaurantID());
        return statement.executeQuery();
    }

    @FXML
    private void rejectOrder() {
        try {
            String orderID = orderIDField.getText();

            Connection conn = State.getConn();
            PreparedStatement statement = conn.prepareStatement(
                    "UPDATE Orders " +
                            "SET Status = 'Rejected' " +
                            "WHERE Order_ID = ? " +
                            "AND O_Restaurant_ID = ? " +
                            "AND Status = 'Preparing'"
            );

            statement.setString(1, orderID);
            statement.setString(2, getRestaurantID());

            int rowsChanged = statement.executeUpdate();

            if (rowsChanged > 0) {
                showInfo("Order rejected");
                populateAvailableOrderList();
            } else {
                showError("No preparing order found with that ID");
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void markReady() {
        try {
            String orderID = orderIDField.getText();

            Connection conn = State.getConn();
            PreparedStatement statement = conn.prepareStatement(
                    "UPDATE Orders " +
                            "SET Status = 'Ready To Pickup' " +
                            "WHERE Order_ID = ? " +
                            "AND O_Restaurant_ID = ? " +
                            "AND Status = 'Preparing'"
            );

            statement.setString(1, orderID);
            statement.setString(2, getRestaurantID());

            int rowsChanged = statement.executeUpdate();

            if (rowsChanged > 0) {
                showInfo("Order marked ready to pickup");
                populateAvailableOrderList();
            } else {
                showError("No preparing order found with that ID");
            }
        } catch (Exception e) {
            showError(e.getMessage());
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
            String menuItemID = menuItemIDField.getText();

            Connection conn = State.getConn();
            PreparedStatement statement = conn.prepareStatement(
                    "UPDATE Menu_Item " +
                            "SET Availability = ? " +
                            "WHERE MenuItem_ID = ? " +
                            "AND MI_Restaurant_ID = ?"
            );

            String availString;

            if (availability == 1) {
                availString = "Available";
            } else {
                availString = "Unavailable";
            }

            statement.setInt(1, availability);
            statement.setString(2, menuItemID);
            statement.setString(3, getRestaurantID());

            int rowsChanged = statement.executeUpdate();

            if (rowsChanged > 0) {
                showInfo("Menu item updated");
            } else {
                showError("No menu item found with that ID");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void updateAccount() {
        try {
            String oldUserName = State.userName;

            String userName = userNameField.getText();
            String passWord = passWordField.getText();
            String name = nameField.getText();
            String phoneNumber = phoneNumberField.getText();
            String eMailAddress = eMailAddressField.getText();

            Connection conn = State.getConn();
            PreparedStatement statement = conn.prepareStatement(
                    "UPDATE Restaurant_Worker " +
                            "SET RW_Name = ?, RW_PhoneNum = ?, Email = ?, RW_Username = ?, RW_Password = ? " +
                            "WHERE RW_Username = ?"
            );

            statement.setString(1, name);
            statement.setString(2, phoneNumber);
            statement.setString(3, eMailAddress);
            statement.setString(4, userName);
            statement.setString(5, passWord);
            statement.setString(6, oldUserName);

            int rowsChanged = statement.executeUpdate();

            if (rowsChanged > 0) {
                State.userName = userName;
                State.passWord = passWord;
                State.name = name;
                State.phoneNumber = phoneNumber;
                State.eMailAddress = eMailAddress;

                showInfo("Account updated");
            } else {
                showError("Account could not be updated");
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private String getRestaurantID() throws Exception {
        Connection conn = State.getConn();

        PreparedStatement statement = conn.prepareStatement(
                "SELECT RW_Restaurant_ID " +
                        "FROM Restaurant_Worker " +
                        "WHERE RW_Username = ?"
        );

        statement.setString(1, State.userName);
        ResultSet rs = statement.executeQuery();

        if (rs.next()) {
            return rs.getString("RW_Restaurant_ID");
        }

        throw new Exception("Could not find restaurant for this worker");
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.show();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.show();
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