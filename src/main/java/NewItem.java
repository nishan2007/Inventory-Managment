import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NewItem extends JFrame {

    private JTextField nameField;
    private JTextField skuField;
    private JTextField priceField;
    private JTextField categoryIdField;
    private JTextField quantityField;
    private JButton saveButton;
    private JButton clearButton;
    private JButton cancelButton;

    public NewItem() {
        setTitle("Add New Item");
        setSize(450, 360);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));

        nameField = new JTextField();
        skuField = new JTextField();
        priceField = new JTextField();
        categoryIdField = new JTextField();
        quantityField = new JTextField("0");

        formPanel.add(new JLabel("Item Name:"));
        formPanel.add(nameField);

        formPanel.add(new JLabel("SKU:"));
        formPanel.add(skuField);

        formPanel.add(new JLabel("Price:"));
        formPanel.add(priceField);

        formPanel.add(new JLabel("Category ID (optional):"));
        formPanel.add(categoryIdField);

        formPanel.add(new JLabel("Starting Quantity:"));
        formPanel.add(quantityField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        saveButton = new JButton("Save Item");
        clearButton = new JButton("Clear");
        cancelButton = new JButton("Close");

        buttonPanel.add(saveButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(cancelButton);

        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveItem();
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFields();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        setVisible(true);
    }

    private void saveItem() {
        String name = nameField.getText().trim();
        String sku = skuField.getText().trim();
        String priceText = priceField.getText().trim();
        String categoryIdText = categoryIdField.getText().trim();
        String quantityText = quantityField.getText().trim();

        if (name.isEmpty() || sku.isEmpty() || priceText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name, SKU, and Price are required.");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Price must be a valid number.");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Starting quantity must be a whole number.");
            return;
        }

        Integer categoryId = null;
        if (!categoryIdText.isEmpty()) {
            try {
                categoryId = Integer.parseInt(categoryIdText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Category ID must be a whole number.");
                return;
            }
        }

        String sql;
        if (categoryId == null) {
            sql = "INSERT INTO products (name, sku, price) VALUES (?, ?, ?)";
        } else {
            sql = "INSERT INTO products (name, sku, price, category_id) VALUES (?, ?, ?, ?)";
        }

        String inventorySql = "INSERT INTO inventory (product_id, location_id, quantity_on_hand, reorder_level) VALUES (?, ?, ?, 0)";
        String locationsSql = "SELECT location_id FROM locations";

        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
                 PreparedStatement inventoryPs = conn.prepareStatement(inventorySql);
                 PreparedStatement locationsPs = conn.prepareStatement(locationsSql)) {

                ps.setString(1, name);
                ps.setString(2, sku);
                ps.setDouble(3, price);

                if (categoryId != null) {
                    ps.setInt(4, categoryId);
                }

                ps.executeUpdate();

                int productId;
                try (java.sql.ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new SQLException("Failed to get new product ID.");
                    }
                    productId = rs.getInt(1);
                }

                try (ResultSet locationsRs = locationsPs.executeQuery()) {
                    while (locationsRs.next()) {
                        int locationId = locationsRs.getInt("location_id");
                        int startingQuantity = (locationId == 1) ? quantity : 0;

                        inventoryPs.setInt(1, productId);
                        inventoryPs.setInt(2, locationId);
                        inventoryPs.setInt(3, startingQuantity);
                        inventoryPs.addBatch();
                    }
                }

                inventoryPs.executeBatch();

                conn.commit();
                JOptionPane.showMessageDialog(this, "Item added successfully.");
                clearFields();

            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to save item: " + ex.getMessage());
        }
    }

    private void clearFields() {
        nameField.setText("");
        skuField.setText("");
        priceField.setText("");
        categoryIdField.setText("");
        quantityField.setText("0");
        nameField.requestFocusInWindow();
    }
}
