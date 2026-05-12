package towsongroup.fooddoods;

import java.sql.*;
import java.util.ArrayList;

public class State {
    // Database information
    private static final String url = "jdbc:mysql://localhost:3306/DeliveryDatabase";
    private static final String user = "root";
    private static final String password = "password";

    // Basic account details
    public static Integer id;
    public static String userName;
    public static String passWord;
    public static String name;
    public static String phoneNumber;
    public static String eMailAddress;

    // for customers
    public static String homeAddress;
    public static String payment;

    // for drivers
    public static String vehicleNumber;

    // for restaurant owners and workers
    public static String restaurantName;

    // for restaurant owners
    public static String restaurantAddress;

    // Database Connection
    public static Connection getConn() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");  // Load MySQL driver
        return DriverManager.getConnection(url, user, password);
    }

    // Get all used IDs
    public static ArrayList<Integer> getIDs() {
        ArrayList<Integer> usedIDs = new ArrayList<>();

        try {
            Connection conn = getConn();

            PreparedStatement getIDs = conn.prepareStatement("SELECT Owner_ID FROM Restaurant_Owner");
            ResultSet ids = getIDs.executeQuery();

            while (ids.next()) {
                usedIDs.add(ids.getInt(1));
            }

            getIDs = conn.prepareStatement("SELECT C_ID FROM Customer");
            ids = getIDs.executeQuery();

            while (ids.next()) {
                usedIDs.add(ids.getInt(1));
            }

            getIDs = conn.prepareStatement("SELECT Driver_ID FROM Driver");
            ids = getIDs.executeQuery();

            while (ids.next()) {
                usedIDs.add(ids.getInt(1));
            }

            getIDs = conn.prepareStatement("SELECT Worker_ID FROM Restaurant_Worker");
            ids = getIDs.executeQuery();

            while (ids.next()) {
                usedIDs.add(ids.getInt(1));
            }

            getIDs = conn.prepareStatement("SELECT Restaurant_ID FROM Restaurant");
            ids = getIDs.executeQuery();

            while (ids.next()) {
                usedIDs.add(ids.getInt(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return usedIDs;
    }

    public static void reset() {
        id = null;
        userName = null;
        passWord = null;
        name = null;
        phoneNumber = null;
        eMailAddress = null;
        homeAddress = null;
        payment = null;
        vehicleNumber = null;
        restaurantName = null;
        restaurantAddress = null;
    }
}
