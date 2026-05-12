package towsongroup.fooddoods;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
    private int orderID;

    @FXML
    private void initialize() {
        try {
            conn = State.getConn();
            PreparedStatement ps = conn.prepareStatement("SELECT RestaurantName FROM Restaurant");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                restaurantListView.getItems().add(rs.getString(1));
            }

            restaurantListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    populateMenu();
                }
            });

            populateOrdersList();

            // Generate ID and check availability
            orderID = (int) (Math.random() * 1000000);
            ArrayList<Integer> usedIDs = State.getIDs();

            while (true) {
                if (usedIDs.contains(orderID)) {
                    orderID = (int) (Math.random() * 1000000);
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void populateMenu() {
        try {
            menuListView.getItems().clear();

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
            // Insert order item into order item table
            PreparedStatement ps = conn.prepareStatement("INSERT INTO Order_Item (OrderItem_ID, OI_Order_ID, OI_MenuItem_ID, Quantity, Price)\n" +
                    "VALUES (?, ?, ?, ?, ?);");

            String selectedItem = menuListView.getSelectionModel().getSelectedItem();

            if (selectedItem == null) {
                return;
            }

            String menuItemName = selectedItem.split(" \\| ")[0];

            PreparedStatement getMenuItemID = conn.prepareStatement(
                    "SELECT MenuItem_ID FROM Menu_Item WHERE MenuItemName = ?"
            );

            getMenuItemID.setString(1, menuItemName);
            ResultSet menuItemIDResult = getMenuItemID.executeQuery();
            menuItemIDResult.next();
            String menuItemID = menuItemIDResult.getString(1);

            // Generate ID and check availability
            int orderItemID = (int) (Math.random() * 1000000);
            ArrayList<Integer> usedIDs = State.getIDs();

            while (true) {
                if (usedIDs.contains(orderItemID)) {
                    orderItemID = (int) (Math.random() * 1000000);
                } else {
                    break;
                }
            }

            ps.setString(1, Integer.toString(orderItemID));
            ps.setString(2, Integer.toString(orderID));
            ps.setString(3, menuItemID);
            ps.setString(4, "1");

            PreparedStatement getItemPrice = conn.prepareStatement("SELECT Price FROM Menu_Item WHERE MenuItem_ID = ?");
            getItemPrice.setString(1, menuItemID);
            ResultSet menuItemRS = getItemPrice.executeQuery();
            menuItemRS.next();
            String itemPrice = menuItemRS.getString(1);

            ps.setString(5, itemPrice);

            ps.executeUpdate();

            populateOrderList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void removeFromOrder() {
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM Order_Item WHERE OrderItem_ID = ?");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void placeOrder(){
        try {
            // Insert order into order table
            PreparedStatement addOrder = conn.prepareStatement("INSERT INTO Orders (Order_ID, O_Restaurant_ID, O_Customer_ID, Status)\n" +
                    "VALUES (?, ?, ?, ?)");

            addOrder.setString(1, Integer.toString(orderID));

            PreparedStatement getRestID = conn.prepareStatement("SELECT Restaurant_ID FROM Restaurant WHERE RestaurantName = ?");
            getRestID.setString(1, restaurantListView.getSelectionModel().getSelectedItem());
            ResultSet restIDResult = getRestID.executeQuery();
            restIDResult.next();
            String restID = restIDResult.getString(1);

            addOrder.setString(2, restID);

            PreparedStatement getCustID = conn.prepareStatement("SELECT C_ID FROM Customer WHERE C_Name = ?");
            getCustID.setString(1, State.name);
            ResultSet custIDResult = getCustID.executeQuery();
            custIDResult.next();
            String custID = custIDResult.getString(1);

            addOrder.setString(3, custID);

            addOrder.setString(4, "Order sent");

            addOrder.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void populateOrderList() {
        try {
            orderListView.getItems().clear();

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT Menu_Item.MenuItemName, Order_Item.Quantity, Order_Item.Price " +
                            "FROM Order_Item " +
                            "JOIN Menu_Item ON Order_Item.OI_MenuItem_ID = Menu_Item.MenuItem_ID " +
                            "WHERE Order_Item.OI_Order_ID = ?"
            );

            ps.setInt(1, orderID);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                String item =
                        rs.getString("MenuItemName") + " | Qty: " +
                                rs.getString("Quantity") + " | $" +
                                rs.getString("Price");

                orderListView.getItems().add(item);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void populateOrdersList() {
        try {
            ordersListView.getItems().clear();

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT Orders.Order_ID, Restaurant.RestaurantName, Customer.Address, Orders.Status " +
                            "FROM Driver " +
                            "JOIN Orders ON Driver.D_Order_ID = Orders.Order_ID " +
                            "JOIN Restaurant ON Orders.O_Restaurant_ID = Restaurant.Restaurant_ID " +
                            "JOIN Customer ON Orders.O_Customer_ID = Customer.C_ID " +
                            "WHERE Customer.C_Username = ?"
            );

            ps.setString(1, State.userName);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ordersListView.getItems().add(
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
    private void updateAccount() {
        String userName = userNameField.getText();
        String passWord = passWordField.getText();
        String name = nameField.getText();
        String phoneNumber = phoneNumberField.getText();
        String eMailAddress = eMailAddressField.getText();
        String address = addressField.getText();
        String payment = paymentField.getText();

        // implement the SQL query
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
