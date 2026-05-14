package towsongroup.fooddoods;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Optional;

public class RestaurantOwnerController {

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
    private TextField restaurantAddressField;
    @FXML
    private ListView<String> availableOrderList;
    @FXML
    private ListView<String> menuListView;
    @FXML
    private ListView<String> historyListView;

    @FXML
    private void initialize() {
        try {
            conn = State.getConn();

            populateAccountFields();
            populateAvailableOrderList();
            populateMenuList();
            populateHistoryList();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void populateAccountFields() {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT ro.OwnerName, ro.RO_PhoneNum, ro.Email, ro.RO_Username, ro.RO_Password, " +
                            "r.RestaurantName, r.Address " +
                            "FROM Restaurant_Owner ro " +
                            "JOIN Restaurant r ON ro.RO_Restaurant_ID = r.Restaurant_ID " +
                            "WHERE ro.RO_Username = ?"
            );

            ps.setString(1, State.userName);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                nameField.setText(rs.getString("OwnerName"));
                phoneNumberField.setText(rs.getString("RO_PhoneNum"));
                eMailAddressField.setText(rs.getString("Email"));
                userNameField.setText(rs.getString("RO_Username"));
                passWordField.setText(rs.getString("RO_Password"));
                restaurantNameField.setText(rs.getString("RestaurantName"));
                restaurantAddressField.setText(rs.getString("Address"));
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

    private void populateHistoryList() {
        try {
            PreparedStatement getTotalOrders = conn.prepareStatement(
                    "SELECT OrderStatus,\n" +
                            "COUNT(*) AS TotalOrders\n" +
                            "FROM Orders\n" +
                            "WHERE O_Restaurant_ID = ?\n" +
                            "GROUP BY OrderStatus;"
            );

            getTotalOrders.setInt(1, getRestaurantID());
            ResultSet totalOrders = getTotalOrders.executeQuery();

            historyListView.getItems().add("Orders:");

            while (totalOrders.next()) {
                historyListView.getItems().add(totalOrders.getString(1) + " " + totalOrders.getString(2));
            }

            PreparedStatement getMostOrdered = conn.prepareStatement(
                    "SELECT Menu_Item.MenuItemName,\n" +
                            "COUNT(*) AS TimesOrdered\n" +
                            "FROM Order_Item\n" +
                            "JOIN Menu_Item\n" +
                            "ON Order_Item.OI_MenuItem_ID = Menu_Item.MenuItem_ID\n" +
                            "WHERE Menu_Item.MI_Restaurant_ID = ?\n" +
                            "GROUP BY Menu_Item.MenuItemName " +
                            "ORDER BY TimesOrdered "
            );

            getMostOrdered.setInt(1, getRestaurantID());
            ResultSet mostOrdered = getMostOrdered.executeQuery();

            historyListView.getItems().add("Items Ordered: ");

            while (mostOrdered.next()) {
                historyListView.getItems().add(mostOrdered.getString(1) + " " + mostOrdered.getString(2));
            }
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
                            "AND O_Restaurant_ID = ? " +
                            "AND OrderStatus = 'Order Placed'"
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
    private void showAddMenuItemDialog() {
        try {
            TextInputDialog nameDialog = new TextInputDialog();
            nameDialog.setTitle("Add Menu Item");
            nameDialog.setHeaderText("Enter Menu Item Name");
            nameDialog.setContentText("Name:");

            Optional<String> nameResult = nameDialog.showAndWait();

            if (nameResult.isEmpty()) {
                return;
            }

            TextInputDialog priceDialog = new TextInputDialog();
            priceDialog.setTitle("Add Menu Item");
            priceDialog.setHeaderText("Enter Price");
            priceDialog.setContentText("Price:");

            Optional<String> priceResult = priceDialog.showAndWait();

            if (priceResult.isEmpty()) {
                return;
            }

            String itemName = nameResult.get();
            String itemPrice = priceResult.get();

            if (itemName.isEmpty() || itemPrice.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Item name and price are required");
                alert.show();
                return;
            }

            if (itemPrice.contains("-")) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Negative prices not allowed");
                alert.show();
                return;
            }

            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Menu_Item " +
                            "(MenuItem_ID, MI_Restaurant_ID, MenuItemName, Price, Availability) " +
                            "VALUES (?, ?, ?, ?, 1)"
            );

            ps.setInt(1, generateID());
            ps.setInt(2, getRestaurantID());
            ps.setString(3, itemName);
            ps.setString(4, itemPrice);

            ps.executeUpdate();

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setContentText("Menu item added");
            alert.show();

            populateMenuList();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    @FXML
    private void deleteSelectedMenuItem() {
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
                    "DELETE FROM Menu_Item " +
                            "WHERE MenuItem_ID = ? " +
                            "AND MI_Restaurant_ID = ?"
            );

            ps.setString(1, menuItemID);
            ps.setInt(2, getRestaurantID());

            ps.executeUpdate();

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setContentText("Menu item deleted");
            alert.show();

            populateMenuList();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();
            e.printStackTrace();
        }
    }

    @FXML
    private void updateAccount() {
        String userName = userNameField.getText().isEmpty() ? State.userName : userNameField.getText();
        String passWord = passWordField.getText().isEmpty() ? State.passWord : passWordField.getText();
        String name = nameField.getText().isEmpty() ? State.name : nameField.getText();
        String phoneNumber = phoneNumberField.getText().isEmpty() ? State.phoneNumber : phoneNumberField.getText();
        String eMailAddress = eMailAddressField.getText().isEmpty() ? State.eMailAddress : eMailAddressField.getText();
        String restaurantName = restaurantNameField.getText();
        String restaurantAddress = restaurantAddressField.getText();

        try {
            conn.setAutoCommit(false);

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
            ownerStatement.setString(6, State.userName);

            ownerStatement.executeUpdate();

            PreparedStatement restaurantStatement = conn.prepareStatement(
                    "UPDATE Restaurant " +
                            "SET RestaurantName = ?, Address = ? " +
                            "WHERE Restaurant_ID = ?"
            );

            restaurantStatement.setString(1, restaurantName);
            restaurantStatement.setString(2, restaurantAddress);
            restaurantStatement.setInt(3, getRestaurantID());

            restaurantStatement.executeUpdate();

            conn.commit();

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setContentText("Account Updated");
            alert.show();

        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (Exception rollbackException) {
                rollbackException.printStackTrace();
            }

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();

        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private int getRestaurantID() throws Exception {
        PreparedStatement ps = conn.prepareStatement(
                "SELECT RO_Restaurant_ID " +
                        "FROM Restaurant_Owner " +
                        "WHERE RO_Username = ?"
        );

        ps.setString(1, State.userName);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt("RO_Restaurant_ID");
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