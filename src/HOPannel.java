import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Vector;

public class HOPannel extends JFrame {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/BO1_sales";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    private final JTable table;
    private final DefaultTableModel tableModel;

    public HOPannel() {
        // Set up the main window
        super("HO");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Create the table model and table
        tableModel = new DefaultTableModel(new String[]{"Date", "Region", "Product", "Qty", "Cost", "Amt", "Tax", "Total"}, 0);
        table = new JTable(tableModel);

        // Add the table to a scroll pane and the scroll pane to the main window
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Add the synchronize button to the main window
        JButton syncButton = new JButton("Synchronize");
        // syncButton.addActionListener(e ->);
        add(syncButton, BorderLayout.SOUTH);

        ActionListener syncbutton=new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              HO.synchronize(tableModel);
            }
        };
        // Show the main window
        setVisible(true);
    }


        public static void main(String[] args) {
        HO ho=new HO();
        Thread t=new Thread(ho);
        t.start();
        new HOPannel();
    }
}
