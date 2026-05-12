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

    // =========================
    // ROOT
    // =========================
    @FXML private AnchorPane anchorPane;

    // =========================
    // CUSTOMER UI
    // =========================
    @FXML private ListView<String> customerListView;
    @FXML private TextField custUserNameField;
    @FXML private TextField custNameField;
    @FXML private TextField custEmailField;
    @FXML private TextField custPhoneField;

    // =========================
    // DRIVER UI
    // =========================
    @FXML private ListView<String> driverListView;
    @FXML private TextField driverUserNameField;
    @FXML private TextField driverNameField;
    @FXML private TextField driverEmailField;
    @FXML private TextField driverPhoneField;

    // =========================
    // INIT
    // =========================
    @FXML
    private void initialize() {
        try {
            conn = State.getConn();
            populateCustomerList();
            populateDriverList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // POPULATE CUSTOMERS
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
    // POPULATE DRIVERS
    // =========================
    private void populateDriverList() {
        try {
            driverListView.getItems().clear();

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT D_Username FROM Driver"
            );

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                driverListView.getItems().add(rs.getString("D_Username"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // DELETE CUSTOMER (SAFE)
    // =========================
    @FXML
    private void deleteCustomer() {
        try {
            String username = customerListView.getSelectionModel().getSelectedItem();
            if (username == null) return;

            PreparedStatement delItems = conn.prepareStatement(
                    "DELETE FROM Order_Item WHERE OI_Order_ID IN (" +
                            "SELECT Order_ID FROM Orders WHERE O_Customer_ID = " +
                            "(SELECT C_ID FROM Customer WHERE C_Username = ?))"
            );
            delItems.setString(1, username);
            delItems.executeUpdate();

            PreparedStatement delOrders = conn.prepareStatement(
                    "DELETE FROM Orders WHERE O_Customer_ID = " +
                            "(SELECT C_ID FROM Customer WHERE C_Username = ?)"
            );
            delOrders.setString(1, username);
            delOrders.executeUpdate();

            PreparedStatement delCustomer = conn.prepareStatement(
                    "DELETE FROM Customer WHERE C_Username = ?"
            );
            delCustomer.setString(1, username);
            delCustomer.executeUpdate();

            populateCustomerList();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // DELETE DRIVER (SIMPLER)
    // =========================
    @FXML
    private void deleteDriver() {
        try {
            String username = driverListView.getSelectionModel().getSelectedItem();
            if (username == null) return;

            // clear any assigned orders first
            PreparedStatement clearOrders = conn.prepareStatement(
                    "UPDATE Driver SET D_Order_ID = NULL WHERE D_Username = ?"
            );
            clearOrders.setString(1, username);
            clearOrders.executeUpdate();

            // delete driver
            PreparedStatement delDriver = conn.prepareStatement(
                    "DELETE FROM Driver WHERE D_Username = ?"
            );
            delDriver.setString(1, username);
            delDriver.executeUpdate();

            populateDriverList();

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

    @FXML
    private void refreshDrivers() {
        populateDriverList();
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