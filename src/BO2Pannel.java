import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

public class BO2Pannel extends JFrame implements ActionListener {

    private JTable table;
    private DefaultTableModel model;
    private JButton addButton;
    private JButton sendButton;

    public BO2Pannel() {
        super("BO2");

        // Create table model
        Vector<String> columns = new Vector<String>();
        columns.add("Date");
        columns.add("Region");
        columns.add("Product");
        columns.add("Qty");
        columns.add("Cost");
        columns.add("Amt");
        columns.add("Tax");
        columns.add("Total");
        columns.add("Sent");
        model = new DefaultTableModel(columns, 0);

        // Create table
        table = new JTable(model);

        JScrollPane scrollPane = new JScrollPane(table);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        // Create buttons
        addButton = new JButton("Add Line");
        sendButton = new JButton("Send");
        addButton.addActionListener(this);
        sendButton.addActionListener(this);

        // Add buttons to panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(sendButton);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        // Set window properties
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setVisible(true);

        BO2 bo2=new BO2();
        Thread t=new Thread(bo2);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addButton) {
            // Add empty row to table
            model.addRow(new Vector<Object>());
        } else if (e.getSource() == sendButton) {
            for (int i = 0; i < model.getRowCount(); i++) {

                // Check if the row has been sent
                String sent = (String) model.getValueAt(i, 8);
                if (!sent.equals("0")) {
                    continue; // Skip if already sent
                }

                // Get the values from the row
                String date = model.getValueAt(i, 0).toString();
                String region = model.getValueAt(i, 1).toString();
                String product = model.getValueAt(i, 2).toString();
                int qty = Integer.parseInt(model.getValueAt(i, 3).toString());
                double cost = Double.parseDouble(model.getValueAt(i, 4).toString());
                double amt = Double.parseDouble(model.getValueAt(i, 5).toString());
                double tax = Double.parseDouble(model.getValueAt(i, 6).toString());
                double total = Double.parseDouble(model.getValueAt(i, 7).toString());
                BO2.insertdb(date, region, product, qty, cost, amt, tax, total);

                model.setValueAt("1", i, 8);
            }
            BO2.send();
        }
    }
    public static void main(String[] args) {
        BO2 bo2=new BO2();
        Thread t=new Thread(bo2);
        t.start();
        new BO2Pannel();
    }
}
