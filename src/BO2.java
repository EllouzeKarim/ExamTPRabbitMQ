import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.sql.*;
import java.util.concurrent.TimeoutException;

public class BO2 implements Runnable{

    private static final String DB_URL = "jdbc:mysql://localhost:3306/BO2_sales";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    private static final String QUEUE_NAME = "BO2-to-HO";

    public static Channel channel;
    public static java.sql.Connection dbConnection;

    public void run() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            Connection connection = factory.newConnection();
            channel = connection.createChannel();

            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            Class.forName("com.mysql.jdbc.Driver");
            dbConnection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            while (true) {
                send();
                Thread.sleep(15000); // wait for 15 second before sending again
            }
        } catch (ClassNotFoundException | SQLException | TimeoutException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void send(){
        try {
            Statement statement = dbConnection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM sales  WHERE sent=0");

            while (resultSet.next()) {
                String date = resultSet.getString("date");
                String region = resultSet.getString("region");
                String product = resultSet.getString("product");
                int quantity = resultSet.getInt("qty");
                double cost = resultSet.getDouble("cost");
                double amount = resultSet.getDouble("amt");
                double tax = resultSet.getDouble("tax");
                double total = resultSet.getDouble("total");

                String message = date + "," + region + "," + product + "," + quantity + ","
                        + cost + "," + amount + "," + tax + "," + total;

                channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
                System.out.println("Sent message: " + message);

                PreparedStatement markAsCopiedStmt = dbConnection.prepareStatement("UPDATE sales SET sent = 1 WHERE date = ?  AND region= ?  AND product= ? AND qty=" + quantity +
                        " AND cost=" + cost +
                        " AND amt=" + amount +
                        " AND tax=" + tax +
                        " AND total=" + total
                );
                markAsCopiedStmt.setString(1, date);
                markAsCopiedStmt.setString(2, region);
                markAsCopiedStmt.setString(3, product);
                markAsCopiedStmt.executeUpdate();
            }
        }
        catch ( SQLException  | IOException  e) {
            e.printStackTrace();
        }
    }
    public static void insertdb(String date,String region,String product,int qty,double cost,double amt,double tax,double total){
        try (PreparedStatement statement = dbConnection.prepareStatement("INSERT INTO sales (date, region, product, qty, cost, amt, tax, total, sent) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

            statement.setString(1, date);
            statement.setString(2, region);
            statement.setString(3, product);
            statement.setInt(4, qty);
            statement.setDouble(5, cost);
            statement.setDouble(6, amt);
            statement.setDouble(7, tax);
            statement.setDouble(8, total);
            statement.setInt(9, 1); // Set sent to 1

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}

