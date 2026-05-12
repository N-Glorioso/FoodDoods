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
            PreparedStatement ps = conn.prepareStatement("INSERT INTO Order_Item (OrderItem_ID, Quantity, Price)\n" +
                    "VALUES (?, ?, ?);");

            PreparedStatement getMenuItemID = conn.prepareStatement("SELECT MenuItem_ID FROM Menu_Item WHERE MenuItemName = ?");
            getMenuItemID.setString(1, menuListView.getSelectionModel().getSelectedItem());
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
            ps.setString(2, "1");

            PreparedStatement getItemPrice = conn.prepareStatement("SELECT Price FROM Menu_Item WHERE MenuItem_ID = /");
            getItemPrice.setString(1, menuItemID);
            ResultSet menuItemRS = getItemPrice.executeQuery();
            menuItemRS.next();
            String itemPrice = menuItemRS.getString(1);

            ps.setString(3, itemPrice);

            ps.executeUpdate();

            // Insert order into order table
            PreparedStatement addOrder = conn.prepareStatement("INSERT INTO Order (INSERT INTO Order (Order_ID, O_Restaurant_ID, O_Customer_ID, Status)\n" +
                    "VALUES (?, ?, ?, ?);)");

            // Generate ID and check availability
            int orderID = (int) (Math.random() * 1000000);

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

            PreparedStatement getCustID = conn.prepareStatement("SELECT C_ID FROM Customer WHERE C_Name = ?");
            getCustID.setString(1, State.name);
            ResultSet custIDResult = getCustID.executeQuery();
            custIDResult.next();
            String custID = custIDResult.getString(1);

            addOrder.setString(3, custID);

            addOrder.setString(4, "Order sent");
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
            PreparedStatement ps = conn.prepareStatement("INSERT INTO ‘Order’ (Order_ID, O_Restaurant_ID, O_Customer_ID, Status)\n" +
                    "VALUES (?, ?, ?, 'Preparing')");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void populateOrderList() {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Order WHERE C_ID = ?");
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
