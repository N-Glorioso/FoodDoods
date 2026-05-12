package towsongroup.fooddoods;

import java.sql.Connection;
import java.sql.DriverManager;

public class State {
    // Database information
    private static final String url = "jdbc:mysql://localhost:3306/DeliveryDatabase";
    private static final String user = "root";
    private static final String password = "password";

    // Basic account details
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

    public static void reset() {
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
