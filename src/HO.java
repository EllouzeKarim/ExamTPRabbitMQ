import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.io.IOException;
import java.sql.*;
import java.util.Vector;
import java.util.concurrent.TimeoutException;

public class HO implements  Runnable{

    private static final String DB_URL = "jdbc:mysql://localhost:3306/HO_sales";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    private static final String QUEUE_NAME = "BO1-to-HO";
    private static final String QUEUE_NAME2 = "BO2-to-HO";

    public static void synchronize(DefaultTableModel tableModel) {
        // Clear the table
        tableModel.setRowCount(0);

        try {
            // Connect to the database
            Class.forName("com.mysql.jdbc.Driver");
            java.sql.Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // Query the database for rows where Sent = 0
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM sales WHERE sent=0");
            ResultSet resultSet = statement.executeQuery();

            // Iterate over the rows and add them to the table
            while (resultSet.next()) {
                Vector<String> row = new Vector<>();
                row.add(resultSet.getString("date"));
                row.add(resultSet.getString("region"));
                row.add(resultSet.getString("product"));
                row.add(Integer.toString(resultSet.getInt("qty")));
                row.add(Double.toString(resultSet.getDouble("cost")));
                row.add(Double.toString(resultSet.getDouble("amt")));
                row.add(Double.toString(resultSet.getDouble("tax")));
                row.add(Double.toString(resultSet.getDouble("total")));
                tableModel.addRow(row);

                // Mark the row as sent
                PreparedStatement updateStatement = connection.prepareStatement("UPDATE sales SET sent = 1 WHERE date = ? AND region = ? AND product = ? AND qty = ? AND cost = ? AND amt = ? AND tax = ? AND total = ?");
                updateStatement.setString(1, resultSet.getString("date"));
                updateStatement.setString(2, resultSet.getString("region"));
                updateStatement.setString(3, resultSet.getString("product"));
                updateStatement.setInt(4, resultSet.getInt("qty"));
                updateStatement.setDouble(5, resultSet.getDouble("cost"));
                updateStatement.setDouble(6, resultSet.getDouble("amt"));
                updateStatement.setDouble(7, resultSet.getDouble("tax"));
                updateStatement.setDouble(8, resultSet.getDouble("total"));
                updateStatement.executeUpdate();
            }

            // Close the database connection
            connection.close();

        } catch (ClassNotFoundException | SQLException ex) {
            ex.printStackTrace();
        }
    }


    public void run() {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        Connection connection = null;
        try {
            connection = factory.newConnection();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        Channel channel = null;
        try {
            channel = connection.createChannel();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Waiting for messages from BO1");

        try {
            channel.basicConsume(QUEUE_NAME, true, (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println("Received message: " + message);

                String[] fields = message.split(",");
                String date = fields[0];
                String region = fields[1];
                String product = fields[2];
                int quantity = Integer.parseInt(fields[3]);
                double cost = Double.parseDouble(fields[4]);
                double amount = Double.parseDouble(fields[5]);
                double tax = Double.parseDouble(fields[6]);
                double total = Double.parseDouble(fields[7]);

                try {
                    Class.forName("com.mysql.jdbc.Driver");
                    java.sql.Connection dbConnection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

                    String insertTableSQL = "INSERT INTO sales"
                            + "(date, region, product, qty, cost, amt, tax, total) VALUES"
                            + "(?,?,?,?,?,?,?,?,0)";
                    PreparedStatement preparedStatement = dbConnection.prepareStatement(insertTableSQL);
                    preparedStatement.setString(1, date);
                    preparedStatement.setString(2, region);
                    preparedStatement.setString(3, product);
                    preparedStatement.setInt(4, quantity);
                    preparedStatement.setDouble(5, cost);
                    preparedStatement.setDouble(6, amount);
                    preparedStatement.setDouble(7, tax);
                    preparedStatement.setDouble(8, total);
                    preparedStatement.executeUpdate();

                    System.out.println("Data inserted into HO database");
                } catch (ClassNotFoundException | SQLException e) {
                    e.printStackTrace();
                }
            }, consumerTag -> {});
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Waiting for messages from BO2");
        try {
            channel.queueDeclare(QUEUE_NAME2, false, false, false, null);
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            channel.basicConsume(QUEUE_NAME2, true, (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println("Received message: " + message);

                String[] fields = message.split(",");
                String date = fields[0];
                String region = fields[1];
                String product = fields[2];
                int quantity = Integer.parseInt(fields[3]);
                double cost = Double.parseDouble(fields[4]);
                double amount = Double.parseDouble(fields[5]);
                double tax = Double.parseDouble(fields[6]);
                double total = Double.parseDouble(fields[7]);

                try {
                    Class.forName("com.mysql.jdbc.Driver");
                    java.sql.Connection dbConnection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

                    String insertTableSQL = "INSERT INTO sales"
                            + "(date, region, product, qty, cost, amt, tax, total) VALUES"
                            + "(?,?,?,?,?,?,?,?)";
                    PreparedStatement preparedStatement = dbConnection.prepareStatement(insertTableSQL);
                    preparedStatement.setString(1, date);
                    preparedStatement.setString(2, region);
                    preparedStatement.setString(3, product);
                    preparedStatement.setInt(4, quantity);
                    preparedStatement.setDouble(5, cost);
                    preparedStatement.setDouble(6, amount);
                    preparedStatement.setDouble(7, tax);
                    preparedStatement.setDouble(8, total);
                    preparedStatement.executeUpdate();

                    System.out.println("Data inserted into HO database");
                } catch (ClassNotFoundException | SQLException e) {
                    e.printStackTrace();
                }
            }, consumerTag -> {});
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
