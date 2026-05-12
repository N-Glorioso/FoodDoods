package towsongroup.fooddoods;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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

    @FXML private AnchorPane anchorPane;

    @FXML private ListView<String> customerListView;

    @FXML private TextField userNameField;
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;

    // =========================
    // INIT
    // =========================
    @FXML
    private void initialize() {
        try {
            conn = State.getConn();
            populateCustomerList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // POPULATE LISTVIEW
    // =========================
    private void populateCustomerList() {
        try {
            customerListView.getItems().clear();

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT C_Username FROM Customer"
            );

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                customerListView.getItems().add(rs.getString("C_Username"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // DELETE CUSTOMER
    // =========================
    @FXML
    private void deleteCustomer() {
        try {
            String username = customerListView.getSelectionModel().getSelectedItem();

            if (username == null) {
                System.out.println("No customer selected");
                return;
            }

            // 1. Order Items
            PreparedStatement deleteOrderItems = conn.prepareStatement(
                    "DELETE FROM Order_Item " +
                            "WHERE OI_Order_ID IN (" +
                            "SELECT Order_ID FROM Orders " +
                            "WHERE O_Customer_ID = (SELECT C_ID FROM Customer WHERE C_Username = ?)" +
                            ")"
            );
            deleteOrderItems.setString(1, username);
            deleteOrderItems.executeUpdate();

            // 2. Orders
            PreparedStatement deleteOrders = conn.prepareStatement(
                    "DELETE FROM Orders WHERE O_Customer_ID = " +
                            "(SELECT C_ID FROM Customer WHERE C_Username = ?)"
            );
            deleteOrders.setString(1, username);
            deleteOrders.executeUpdate();

            // 3. Customer
            PreparedStatement deleteCustomer = conn.prepareStatement(
                    "DELETE FROM Customer WHERE C_Username = ?"
            );
            deleteCustomer.setString(1, username);
            deleteCustomer.executeUpdate();

            populateCustomerList();

            System.out.println("Deleted: " + username);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // REFRESH
    // =========================
    @FXML
    private void refreshCustomers() {
        populateCustomerList();
    }

    // =========================
    // NAVIGATION
    // =========================
    @FXML
    private void returnToLanding() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("landingScene.fxml"));
            Pane pane = loader.load();

            anchorPane.getChildren().clear();
            anchorPane.getChildren().add(pane);

            State.reset();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}