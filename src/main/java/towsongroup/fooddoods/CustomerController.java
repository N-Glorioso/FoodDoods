package towsongroup.fooddoods;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class CustomerController {

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
    private TextField addressField;
    @FXML
    private TextField paymentField;
    @FXML
    private ListView<String> restaurantListView;
    @FXML
    private ListView<String> menuListView;
    @FXML
    private ListView<String> orderListView;
    @FXML
    private ListView<String> ordersListView;
    private ArrayList<Integer> preOrderItems;

    @FXML
    private void initialize() {
        try {
            preOrderItems = new ArrayList<>();
            conn = State.getConn();
            populateRestaurantList();

            populateOrdersList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void populateRestaurantList() throws SQLException {
        PreparedStatement ps = conn.prepareStatement("SELECT RestaurantName FROM Restaurant");
        ResultSet rs = ps.executeQuery();

        try {
            while (rs.next()) {
                restaurantListView.getItems().add(rs.getString(1));
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("unable to load restaurants");
            alert.show();
        }

        restaurantListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                populateMenu();
            }
        });
    }

    private void populateMenu() {
        try {
            menuListView.getItems().clear();
            if (!preOrderItems.isEmpty()) {
                preOrderItems.clear();
                populateOrderList();
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText("Order cleared. You can only order from one restaurant at a time.");
                alert.show();
            }

            PreparedStatement getRestID = conn.prepareStatement("SELECT Restaurant_ID FROM Restaurant WHERE RestaurantName = ?");
            getRestID.setString(1, restaurantListView.getSelectionModel().getSelectedItem());
            ResultSet restIDResult = getRestID.executeQuery();
            restIDResult.next();
            String restID = restIDResult.getString(1);

            PreparedStatement ps = conn.prepareStatement("SELECT MenuItemName, Price, Availability FROM Menu_Item WHERE MI_Restaurant_ID = ?");
            ps.setString(1, restID);
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                menuListView.getItems().add(rs.getString(1) + " | $" + rs.getString(2) + " | " + rs.getString(3));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void addToOrder() {
        try {
            // Get menu_item id and add it to a list to be put together in an order
            String selectedItem = menuListView.getSelectionModel().getSelectedItem();

            if (selectedItem == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("No item selected");
                alert.show();
                return;
            }

            selectedItem = selectedItem.split("\\|")[0];
            selectedItem = selectedItem.trim();

            PreparedStatement getMenuItemID = conn.prepareStatement("SELECT MenuItem_ID FROM Menu_Item WHERE MenuItemName = ?");
            getMenuItemID.setString(1, selectedItem);

            ResultSet menuItemID = getMenuItemID.executeQuery();
            menuItemID.next();
            preOrderItems.add(menuItemID.getInt(1));

            populateOrderList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void removeFromOrder() {
        try {
            String selectedItem = orderListView.getSelectionModel().getSelectedItem();

            if (selectedItem == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("No item selected");
                alert.show();
                return;
            }

            selectedItem = selectedItem.split("\\|")[0];
            selectedItem = selectedItem.trim();

            PreparedStatement getMenuItemID = conn.prepareStatement("SELECT MenuItem_ID FROM Menu_Item WHERE MenuItemName = ?");
            getMenuItemID.setString(1, selectedItem);

            ResultSet menuItemID = getMenuItemID.executeQuery();
            menuItemID.next();
            preOrderItems.remove((Integer) menuItemID.getInt(1));

            populateOrderList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void placeOrder(){
        try {
            // Insert order into order table
            PreparedStatement addOrder = conn.prepareStatement("INSERT INTO Orders (Order_ID, O_Restaurant_ID, O_Customer_ID, OrderStatus)\n" +
                    "VALUES (?, ?, ?, ?)");

            // Generate ID and check availability
            int orderID = (int) (Math.random() * 1000000);
            ArrayList<Integer> usedIDs = State.getIDs();

            while (true) {
                if (usedIDs.contains(orderID)) {
                    orderID = (int) (Math.random() * 1000000);
                } else {
                    break;
                }
            }

            addOrder.setString(1, Integer.toString(orderID));

            PreparedStatement getRestID = conn.prepareStatement("SELECT Restaurant_ID FROM Restaurant WHERE RestaurantName = ?");
            getRestID.setString(1, restaurantListView.getSelectionModel().getSelectedItem());
            ResultSet restIDResult = getRestID.executeQuery();
            restIDResult.next();
            String restID = restIDResult.getString(1);

            addOrder.setString(2, restID);

            addOrder.setInt(3, State.id);

            addOrder.setString(4, "Order sent");

            addOrder.executeUpdate();

            PreparedStatement addOrderItem = conn.prepareStatement("INSERT INTO Order_Item (OrderItem_ID, OI_Order_ID, OI_MenuItem_ID)" +
                    "VALUES (?, ?, ?)");

            for (Integer item : preOrderItems) {
                int orderItemID = (int) (Math.random() * 1000000);
                usedIDs = State.getIDs();

                while (true) {
                    if (usedIDs.contains(orderID)) {
                        orderItemID = (int) (Math.random() * 1000000);
                    } else {
                        break;
                    }
                }

                addOrderItem.setInt(1, orderItemID);
                addOrderItem.setInt(2, orderID);
                addOrderItem.setInt(3, item);

                addOrderItem.executeUpdate();
            }

            preOrderItems.clear();
            populateOrderList();
            populateOrdersList();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    private void populateOrderList() {
        try {
            orderListView.getItems().clear();

            PreparedStatement getMenuItemInfo = conn.prepareStatement(
                    "SELECT Menu_Item.MenuItemName, Menu_Item.price FROM Menu_Item WHERE MenuItem_ID = ?"
            );

            int i = 0;
            for (Integer item : preOrderItems) {
                getMenuItemInfo.setInt(1, preOrderItems.get(i++));

                ResultSet menuItemInfo = getMenuItemInfo.executeQuery();
                menuItemInfo.next();

                orderListView.getItems().add(menuItemInfo.getString(1) + " | $" + menuItemInfo.getString(2));
            }

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    private void populateOrdersList() {
        try {
            ordersListView.getItems().clear();

//            SELECT o.Order_ID, o.OrderStatus, r.RestaurantName
//            FROM Orders o
//            JOIN Restaurant r ON o.O_Restaurant_ID = r.Restaurant_ID
//            JOIN Customer c ON o.O_Customer_ID = c.C_ID
//            WHERE c.C_Username = 'lpressi1';

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT o.Order_ID, o.OrderStatus, r.RestaurantName " +
                            "FROM Orders o " +
                            "JOIN Restaurant r on o.O_Restaurant_ID = r.Restaurant_ID " +
                            "JOIN Customer c on o.O_Customer_ID = c.C_ID " +
                            "WHERE C_ID = ?"
            );

            ps.setInt(1, State.id);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ordersListView.getItems().add(
                        rs.getString("Order_ID") + " | " +
                                rs.getString("OrderStatus") + " | " +
                                rs.getString("RestaurantName")
                );
            }

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    @FXML
    private void updateAccount() {
        String userName = (userNameField.getText().isEmpty() ? State.userName : userNameField.getText());
        String passWord = (passWordField.getText().isEmpty() ? State.passWord : passWordField.getText());
        String name = (nameField.getText().isEmpty() ? State.name : nameField.getText());
        String phoneNumber = (phoneNumberField.getText().isEmpty() ? State.phoneNumber : phoneNumberField.getText());
        String eMailAddress = (eMailAddressField.getText().isEmpty() ? State.eMailAddress : eMailAddressField.getText());
        String address = (addressField.getText().isEmpty() ? State.homeAddress : addressField.getText());
        String payment = (paymentField.getText().isEmpty() ? State.payment : paymentField.getText());

        try {
            PreparedStatement updateAccount = conn.prepareStatement(
                    "UPDATE Customer SET C_Name=?, C_Phone_Num=?, C_Email=?, Address=?, Payment_Info=?, C_Username=?, C_Password=? WHERE C_ID=?;"
            );

            updateAccount.setString(1, name);
            updateAccount.setString(2, phoneNumber);
            updateAccount.setString(3, eMailAddress);
            updateAccount.setString(4, address);
            updateAccount.setString(5, payment);
            updateAccount.setString(6, userName);
            updateAccount.setString(7, passWord);
            updateAccount.setInt(8, State.id);

            updateAccount.executeUpdate();

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

    @FXML
    private void deleteAccount() {
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM Customer WHERE C_Username=?;");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
