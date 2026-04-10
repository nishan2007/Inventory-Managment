import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class NewItem extends JFrame {

    private JTextField nameField;
    private JTextField skuField;
    private JTextField barcodeField;
    private JTextArea descriptionArea;
    private JTextArea barcodesArea;
    private JTextField costPriceField;
    private JTextField priceField;
    private JTextField categoryIdField;
    private JTextField quantityField;
    private JButton saveButton;
    private JButton clearButton;
    private JButton cancelButton;
    private final int selectedLocationId;

    public NewItem() {
        this(1);
    }

    public NewItem(int selectedLocationId) {
        this.selectedLocationId = selectedLocationId;
        setTitle("Add New Item");
        setSize(840, 470);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createTitledBorder(
                        new RoundedBorder(12, Color.GRAY, 1), // 👈 control radius here
                        "New Item",
                        javax.swing.border.TitledBorder.LEFT,
                        javax.swing.border.TitledBorder.TOP,
                        panel.getFont().deriveFont(Font.BOLD)
                )
        ));

        JPanel formPanel = new JPanel(new GridLayout(1, 2, 20, 0));

        JPanel leftColumn = new JPanel(new GridBagLayout());
        JPanel rightColumn = new JPanel(new GridBagLayout());

        GridBagConstraints leftGbc = new GridBagConstraints();
        leftGbc.insets = new Insets(5, 5, 5, 5);
        leftGbc.fill = GridBagConstraints.HORIZONTAL;
        leftGbc.anchor = GridBagConstraints.NORTHWEST;

        GridBagConstraints rightGbc = new GridBagConstraints();
        rightGbc.insets = new Insets(5, 5, 5, 5);
        rightGbc.fill = GridBagConstraints.HORIZONTAL;
        rightGbc.anchor = GridBagConstraints.NORTHWEST;

        nameField = new JTextField();
        skuField = new JTextField();
        barcodeField = new JTextField();
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        barcodesArea = new JTextArea(2, 20);
        barcodesArea.setLineWrap(true);
        barcodesArea.setWrapStyleWord(true);
        costPriceField = new JTextField();
        priceField = new JTextField();
        categoryIdField = new JTextField();
        quantityField = new JTextField("0");

        JScrollPane barcodeScrollPane = new JScrollPane(barcodesArea);
        barcodeScrollPane.setPreferredSize(new Dimension(220, 75));
        JScrollPane descriptionScrollPane = new JScrollPane(descriptionArea);
        descriptionScrollPane.setPreferredSize(new Dimension(240, 120));

        leftGbc.gridx = 0;
        leftGbc.gridy = 0;
        leftGbc.weightx = 0;
        leftGbc.weighty = 0;
        leftColumn.add(new JLabel("Item Name:"), leftGbc);

        leftGbc.gridx = 1;
        leftGbc.weightx = 1;
        leftColumn.add(nameField, leftGbc);

        leftGbc.gridx = 0;
        leftGbc.gridy = 1;
        leftGbc.weightx = 0;
        leftColumn.add(new JLabel("SKU:"), leftGbc);

        leftGbc.gridx = 1;
        leftGbc.weightx = 1;
        leftColumn.add(skuField, leftGbc);

        leftGbc.gridx = 0;
        leftGbc.gridy = 2;
        leftGbc.weightx = 0;
        leftGbc.anchor = GridBagConstraints.NORTHWEST;
        leftColumn.add(new JLabel("Description:"), leftGbc);

        leftGbc.gridx = 1;
        leftGbc.weightx = 1;
        leftGbc.weighty = 1;
        leftGbc.fill = GridBagConstraints.BOTH;
        leftColumn.add(descriptionScrollPane, leftGbc);

        leftGbc.fill = GridBagConstraints.HORIZONTAL;
        leftGbc.weighty = 0;
        leftGbc.gridx = 0;
        leftGbc.gridy = 3;
        leftGbc.weightx = 0;
        leftColumn.add(new JLabel("Barcode:"), leftGbc);

        leftGbc.gridx = 1;
        leftGbc.weightx = 1;
        leftColumn.add(barcodeField, leftGbc);

        leftGbc.gridx = 0;
        leftGbc.gridy = 4;
        leftGbc.weightx = 0;
        leftColumn.add(new JLabel("Price:"), leftGbc);

        leftGbc.gridx = 1;
        leftGbc.weightx = 1;
        leftColumn.add(priceField, leftGbc);

        leftGbc.gridx = 0;
        leftGbc.gridy = 5;
        leftGbc.weightx = 0;
        leftColumn.add(new JLabel("Starting Quantity:"), leftGbc);

        leftGbc.gridx = 1;
        leftGbc.weightx = 1;
        leftColumn.add(quantityField, leftGbc);

        leftGbc.gridx = 0;
        leftGbc.gridy = 6;
        leftGbc.weightx = 0;
        leftGbc.weighty = 1;
        leftColumn.add(Box.createVerticalGlue(), leftGbc);



        rightGbc.gridx = 0;
        rightGbc.gridy = 0;
        rightGbc.weightx = 0;
        rightGbc.weighty = 0;
        rightColumn.add(new JLabel("Cost Price:"), rightGbc);

        rightGbc.gridx = 1;
        rightGbc.weightx = 1;
        rightColumn.add(costPriceField, rightGbc);

        rightGbc.gridx = 0;
        rightGbc.gridy = 1;
        rightGbc.weightx = 0;
        rightColumn.add(new JLabel("Category ID (optional):"), rightGbc);

        rightGbc.gridx = 1;
        rightGbc.weightx = 1;
        rightColumn.add(categoryIdField, rightGbc);

        rightGbc.gridx = 0;
        rightGbc.gridy = 2;
        rightGbc.weightx = 0;
        rightGbc.anchor = GridBagConstraints.NORTHWEST;
        rightColumn.add(new JLabel("Additional Barcodes:"), rightGbc);

        rightGbc.gridx = 1;
        rightGbc.weightx = 1;
        rightGbc.weighty = 0;
        rightGbc.fill = GridBagConstraints.HORIZONTAL;
        rightColumn.add(barcodeScrollPane, rightGbc);

        rightGbc.gridx = 0;
        rightGbc.gridy = 3;
        rightGbc.weightx = 0;
        rightGbc.weighty = 1;
        rightColumn.add(Box.createVerticalGlue(), rightGbc);

        formPanel.add(leftColumn);
        formPanel.add(rightColumn);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        saveButton = new JButton("Save Item");
        clearButton = new JButton("Clear");
        cancelButton = new JButton("Close");

        buttonPanel.add(saveButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(cancelButton);

        panel.add(formPanel, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(780, 360));
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
        String barcode = barcodeField.getText().trim();
        String description = descriptionArea.getText().trim();
        String barcodesText = barcodesArea.getText().trim();
        String costPriceText = costPriceField.getText().trim();
        String priceText = priceField.getText().trim();
        String categoryIdText = categoryIdField.getText().trim();
        String quantityText = quantityField.getText().trim();
        List<String> extraBarcodes = new ArrayList<>();
        Set<String> uniqueBarcodes = new LinkedHashSet<>();
        if (!barcodesText.isEmpty()) {
            String[] barcodeLines = barcodesText.split("\\r?\\n");

            for (String line : barcodeLines) {
                String extraBarcode = line.trim();
                if (!extraBarcode.isEmpty()) {
                    uniqueBarcodes.add(extraBarcode);
                }
            }
        }

        uniqueBarcodes.remove(sku);
        uniqueBarcodes.remove(barcode);
        extraBarcodes.addAll(uniqueBarcodes);

        if (name.isEmpty() || sku.isEmpty() || barcode.isEmpty() || costPriceText.isEmpty() || priceText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name, SKU, Barcode, Cost Price, and Price are required.");
            return;
        }

        double costPrice;
        try {
            costPrice = Double.parseDouble(costPriceText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Cost price must be a valid number.");
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
            sql = "INSERT INTO products (name, sku, barcode, description, cost_price, price) VALUES (?, ?, ?, ?, ?, ?)";
        } else {
            sql = "INSERT INTO products (name, sku, barcode, description, cost_price, price, category_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        }

        String updateInventorySql = "UPDATE inventory SET quantity_on_hand = ? WHERE product_id = ? AND location_id = ?";
        String insertBarcodeSql = "INSERT INTO product_barcodes (product_id, barcode) VALUES (?, ?)";

        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
                 PreparedStatement inventoryPs = conn.prepareStatement(updateInventorySql);
                 PreparedStatement barcodePs = conn.prepareStatement(insertBarcodeSql)) {

                ps.setString(1, name);
                ps.setString(2, sku);
                ps.setString(3, barcode);
                ps.setString(4, description);
                ps.setDouble(5, costPrice);
                ps.setDouble(6, price);

                if (categoryId != null) {
                    ps.setInt(7, categoryId);
                }

                ps.executeUpdate();

                int productId;
                try (java.sql.ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new SQLException("Failed to get new product ID.");
                    }
                    productId = rs.getInt(1);
                }

                inventoryPs.setInt(1, quantity);
                inventoryPs.setInt(2, productId);
                inventoryPs.setInt(3, selectedLocationId);
                inventoryPs.executeUpdate();
                if (inventoryPs.getUpdateCount() == 0) {
                    throw new SQLException("No inventory row found for product " + productId + " at location " + selectedLocationId + ".");
                }

                for (String extraBarcode : extraBarcodes) {
                    barcodePs.setInt(1, productId);
                    barcodePs.setString(2, extraBarcode);
                    barcodePs.addBatch();
                }
                if (!extraBarcodes.isEmpty()) {
                    barcodePs.executeBatch();
                }

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
        barcodeField.setText("");
        descriptionArea.setText("");
        barcodesArea.setText("");
        costPriceField.setText("");
        priceField.setText("");
        categoryIdField.setText("");
        quantityField.setText("0");
        nameField.requestFocusInWindow();
    }
}
