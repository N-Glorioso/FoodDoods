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
import java.util.ArrayList;

public class RestaurantOwnerController {

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
    private TextField orderIDField;
    @FXML
    private TextField menuItemIDField;
    @FXML
    private TextField menuItemNameField;
    @FXML
    private TextField menuItemPriceField;
    @FXML
    private ListView<String> availableOrderList;
    @FXML
    private ListView<String> menuListView;

    @FXML
    private void initialize() {
        try {
            Connection conn = State.getConn();

            PreparedStatement statement = conn.prepareStatement(
                    "SELECT ro.OwnerName, ro.RO_PhoneNum, ro.Email, ro.RO_Username, ro.RO_Password, " +
                            "r.RestaurantName, r.Address " +
                            "FROM Restaurant_Owner ro " +
                            "JOIN Restaurant r ON ro.RO_Restaurant_ID = r.Restaurant_ID " +
                            "WHERE ro.RO_Username = ?"
            );

            statement.setString(1, State.userName);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                nameField.setText(rs.getString("OwnerName"));
                phoneNumberField.setText(rs.getString("RO_PhoneNum"));
                eMailAddressField.setText(rs.getString("Email"));
                userNameField.setText(rs.getString("RO_Username"));
                passWordField.setText(rs.getString("RO_Password"));
                restaurantNameField.setText(rs.getString("RestaurantName"));
                restaurantAddressField.setText(rs.getString("Address"));
            }

            populateAvailableOrderList();
            populateMenuList();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void populateMenuList() {
        if (menuListView == null) return;

        menuListView.getItems().clear();

        try {
            Connection conn = State.getConn();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT MenuItem_ID, MenuItemName, Price, Availability " +
                            "FROM Menu_Item " +
                            "WHERE MI_Restaurant_ID = ?"
            );

            ps.setString(1, getRestaurantID());
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
            showError(e.getMessage());
        }
    }

    @FXML
    private void addItem() {
        try {
            String itemName = menuItemNameField.getText();
            String itemPrice = menuItemPriceField.getText();

            int menuItemID = generateID();

            Connection conn = State.getConn();
            PreparedStatement statement = conn.prepareStatement(
                    "INSERT INTO Menu_Item (MenuItem_ID, MI_Restaurant_ID, MenuItemName, Price, Availability) " +
                            "VALUES (?, ?, ?, ?, 1)"
            );

            statement.setString(1, Integer.toString(menuItemID));
            statement.setString(2, getRestaurantID());
            statement.setString(3, itemName);
            statement.setString(4, itemPrice);

            statement.executeUpdate();
            showInfo("Menu item added");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void showAddMenuItemDialog() {
        try {
            javafx.scene.control.TextInputDialog nameDialog =
                    new javafx.scene.control.TextInputDialog();
            nameDialog.setTitle("Add Menu Item");
            nameDialog.setHeaderText("Enter Menu Item Name");
            nameDialog.setContentText("Name:");

            java.util.Optional<String> nameResult = nameDialog.showAndWait();
            if (nameResult.isEmpty()) return;

            String itemName = nameResult.get();

            javafx.scene.control.TextInputDialog priceDialog =
                    new javafx.scene.control.TextInputDialog();
            priceDialog.setTitle("Add Menu Item");
            priceDialog.setHeaderText("Enter Price");
            priceDialog.setContentText("Price:");

            java.util.Optional<String> priceResult = priceDialog.showAndWait();
            if (priceResult.isEmpty()) return;

            String itemPrice = priceResult.get();

            if (itemPrice.contains("-")) {
                Alert negativeAlert = new Alert(Alert.AlertType.ERROR);
                negativeAlert.setContentText("Negative prices not allowed");
                negativeAlert.show();
                return;
            }

            int menuItemID = generateID();

            Connection conn = State.getConn();
            PreparedStatement statement = conn.prepareStatement(
                    "INSERT INTO Menu_Item (MenuItem_ID, MI_Restaurant_ID, MenuItemName, Price, Availability) " +
                            "VALUES (?, ?, ?, ?, 1)"
            );

            statement.setInt(1, menuItemID);
            statement.setString(2, getRestaurantID());
            statement.setString(3, itemName);
            statement.setString(4, itemPrice);

            int rows = statement.executeUpdate();

            if (rows > 0) {
                showInfo("Menu item added successfully");
            } else {
                showError("Failed to add menu item");
            }

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void deleteSelectedMenuItem() {
        try {
            if (menuListView == null || menuListView.getSelectionModel().getSelectedItem() == null) {
                showError("Please select a menu item to delete");
                return;
            }

            String selected = menuListView.getSelectionModel().getSelectedItem();

            // format: ID | name | price | availability
            String menuItemID = selected.split(" \\| ")[0];

            Connection conn = State.getConn();

            PreparedStatement statement = conn.prepareStatement(
                    "DELETE FROM Menu_Item " +
                            "WHERE MenuItem_ID = ? " +
                            "AND MI_Restaurant_ID = ?"
            );

            statement.setString(1, menuItemID);
            statement.setString(2, getRestaurantID());

            int rows = statement.executeUpdate();

            if (rows > 0) {
                showInfo("Menu item deleted");
                populateMenuList(); // refresh UI
            } else {
                showError("Could not delete menu item");
            }

        } catch (Exception e) {
            showError(e.getMessage());
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
            String restaurantName = restaurantNameField.getText();
            String restaurantAddress = restaurantAddressField.getText();

            Connection conn = State.getConn();
            conn.setAutoCommit(false);

            try {
                PreparedStatement ownerStatement = conn.prepareStatement(
                        "UPDATE Restaurant_Owner " +
                                "SET OwnerName = ?, RO_PhoneNum = ?, Email = ?, RO_Username = ?, RO_Password = ? " +
                                "WHERE RO_Username = ?"
                );

                ownerStatement.setString(1, name);
                ownerStatement.setString(2, phoneNumber);
                ownerStatement.setString(3, eMailAddress);
                ownerStatement.setString(4, userName);
                ownerStatement.setString(5, passWord);
                ownerStatement.setString(6, oldUserName);
                ownerStatement.executeUpdate();

                PreparedStatement restaurantStatement = conn.prepareStatement(
                        "UPDATE Restaurant " +
                                "SET RestaurantName = ?, Address = ? " +
                                "WHERE Restaurant_ID = ?"
                );

                restaurantStatement.setString(1, restaurantName);
                restaurantStatement.setString(2, restaurantAddress);
                restaurantStatement.setString(3, getRestaurantID());
                restaurantStatement.executeUpdate();

                conn.commit();

                State.userName = userName;
                State.passWord = passWord;
                State.name = name;
                State.phoneNumber = phoneNumber;
                State.eMailAddress = eMailAddress;

                showInfo("Account updated");
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private String getRestaurantID() throws Exception {
        Connection conn = State.getConn();

        PreparedStatement statement = conn.prepareStatement(
                "SELECT RO_Restaurant_ID " +
                        "FROM Restaurant_Owner " +
                        "WHERE RO_Username = ?"
        );

        statement.setString(1, State.userName);
        ResultSet rs = statement.executeQuery();

        if (rs.next()) {
            return rs.getString("RO_Restaurant_ID");
        }

        throw new Exception("Could not find restaurant for this owner");
    }

    private int generateID() {
        int id = (int) (Math.random() * 1000000);
        ArrayList<Integer> usedIDs = State.getIDs();

        while (usedIDs.contains(id)) {
            id = (int) (Math.random() * 1000000);
        }

        return id;
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