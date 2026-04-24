import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class EditItem extends JFrame {

    private JTextField searchField;
    private JButton searchBtn;

    private JTextField nameField;
    private JTextField skuField;
    private JTextField barcodeField;
    private JTextArea descriptionArea;
    private JTextArea barcodesArea;
    private JTextField costPriceField;
    private JTextField priceField;
    private JTextField quantityField;
    private JTextField reorderLevelField;
    private JTextField categoryIdField;

    private JButton saveButton;
    private JButton clearButton;
    private JButton cancelButton;

    private int selectedProductId = -1;

    public EditItem() {
        setTitle("Edit Item");
        setSize(840, 470);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setJMenuBar(AppMenuBar.create(this, "EditItem"));

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createTitledBorder(
                        new RoundedBorder(12, Color.GRAY, 1),
                        "Edit Item",
                        javax.swing.border.TitledBorder.LEFT,
                        javax.swing.border.TitledBorder.TOP,
                        panel.getFont().deriveFont(Font.BOLD)
                )
        ));

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBorder(BorderFactory.createTitledBorder("Find Product"));

        searchField = new JTextField();
        searchBtn = new JButton("Search");

        topPanel.add(searchField, BorderLayout.CENTER);
        topPanel.add(searchBtn, BorderLayout.EAST);

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
        quantityField = new JTextField();
        reorderLevelField = new JTextField();
        categoryIdField = new JTextField();

        JScrollPane descriptionScrollPane = new JScrollPane(descriptionArea);
        descriptionScrollPane.setPreferredSize(new Dimension(240, 120));

        JScrollPane barcodeScrollPane = new JScrollPane(barcodesArea);
        barcodeScrollPane.setPreferredSize(new Dimension(220, 75));

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
        rightColumn.add(new JLabel("Quantity:"), rightGbc);

        rightGbc.gridx = 1;
        rightGbc.weightx = 1;
        rightColumn.add(quantityField, rightGbc);

        rightGbc.gridx = 0;
        rightGbc.gridy = 2;
        rightGbc.weightx = 0;
        rightColumn.add(new JLabel("Reorder Quantity:"), rightGbc);

        rightGbc.gridx = 1;
        rightGbc.weightx = 1;
        rightColumn.add(reorderLevelField, rightGbc);

        rightGbc.gridx = 0;
        rightGbc.gridy = 3;
        rightGbc.weightx = 0;
        rightColumn.add(new JLabel("Category ID (optional):"), rightGbc);

        rightGbc.gridx = 1;
        rightGbc.weightx = 1;
        rightColumn.add(categoryIdField, rightGbc);

        rightGbc.gridx = 0;
        rightGbc.gridy = 4;
        rightGbc.weightx = 0;
        rightGbc.anchor = GridBagConstraints.NORTHWEST;
        rightColumn.add(new JLabel("Additional Barcodes:"), rightGbc);

        rightGbc.gridx = 1;
        rightGbc.weightx = 1;
        rightGbc.weighty = 0;
        rightGbc.fill = GridBagConstraints.HORIZONTAL;
        rightColumn.add(barcodeScrollPane, rightGbc);

        rightGbc.gridx = 0;
        rightGbc.gridy = 5;
        rightGbc.weightx = 0;
        rightGbc.weighty = 1;
        rightColumn.add(Box.createVerticalGlue(), rightGbc);

        formPanel.add(leftColumn);
        formPanel.add(rightColumn);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        saveButton = new JButton("Save Changes");
        clearButton = new JButton("Clear Selection");
        cancelButton = new JButton("Close");

        buttonPanel.add(saveButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(cancelButton);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(topPanel, BorderLayout.NORTH);
        centerPanel.add(formPanel, BorderLayout.CENTER);

        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        panel.setPreferredSize(new Dimension(780, 360));

        add(panel);

        setFormEnabled(false);

        searchBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchProduct();
            }
        });

        searchField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchProduct();
            }
        });

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveChanges();
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearSelection();
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

    private Integer getCurrentSelectedLocationId() {
        return Login.currentLocationId;
    }

    private Integer requireCurrentSelectedLocationId() {
        Integer locationId = getCurrentSelectedLocationId();
        if (locationId == null) {
            JOptionPane.showMessageDialog(this, "No store is selected. Please log in with a store or use Change Store first.");
        }
        return locationId;
    }

    private void searchProduct() {
        Integer selectedLocationId = requireCurrentSelectedLocationId();
        if (selectedLocationId == null) {
            return;
        }

        String searchText = searchField.getText().trim();

        if (searchText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a product name, SKU, or barcode to search.");
            return;
        }

        String sql = """
                SELECT p.product_id,
                       p.name,
                       p.sku,
                       p.barcode,
                       p.description,
                       p.cost_price,
                       p.price,
                       COALESCE(i.quantity_on_hand, 0) AS quantity_on_hand,
                       COALESCE(i.reorder_level, 0) AS reorder_level,
                       p.category_id,
                       c.name AS category_name
                FROM products p
                LEFT JOIN categories c ON p.category_id = c.category_id
                LEFT JOIN inventory i ON p.product_id = i.product_id AND i.location_id = ?
                WHERE p.name ILIKE ? OR p.sku ILIKE ? OR p.barcode ILIKE ?
                ORDER BY p.name
                """;

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, selectedLocationId);
            ps.setString(2, "%" + searchText + "%");
            ps.setString(3, "%" + searchText + "%");
            ps.setString(4, "%" + searchText + "%");

            ResultSet rs = ps.executeQuery();

            java.util.List<Object[]> rows = new java.util.ArrayList<>();
            while (rs.next()) {
                rows.add(new Object[]{
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getString("sku"),
                        rs.getString("barcode"),
                        rs.getString("description") != null ? rs.getString("description") : "",
                        rs.getDouble("cost_price"),
                        rs.getDouble("price"),
                        rs.getInt("quantity_on_hand"),
                        rs.getInt("reorder_level"),
                        rs.getObject("category_id") != null ? rs.getInt("category_id") : "",
                        rs.getString("category_name") != null ? rs.getString("category_name") : ""
                });
            }

            if (rows.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No matching products found.");
                return;
            }

            String[] columns = {"ID", "Name", "SKU", "Barcode", "Description", "Cost Price", "Price", "Quantity", "Reorder Qty", "Category ID", "Category"};
            DefaultTableModel model = new DefaultTableModel(rows.toArray(new Object[0][]), columns) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            JTable table = new JTable(model);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setRowSelectionInterval(0, 0);
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setPreferredSize(new Dimension(550, 200));

            String storeLabel = Login.currentLocationName != null && !Login.currentLocationName.isBlank()
                    ? Login.currentLocationName
                    : "the currently selected store";

            int result = JOptionPane.showConfirmDialog(
                    this,
                    scrollPane,
                    "Select a Product to Edit for " + storeLabel,
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result == JOptionPane.OK_OPTION) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(this, "Please select a product.");
                    return;
                }

                selectedProductId = (int) table.getValueAt(selectedRow, 0);
                String name = (String) table.getValueAt(selectedRow, 1);
                String sku = (String) table.getValueAt(selectedRow, 2);
                String barcode = (String) table.getValueAt(selectedRow, 3);
                String description = (String) table.getValueAt(selectedRow, 4);
                double costPrice = (double) table.getValueAt(selectedRow, 5);
                double price = (double) table.getValueAt(selectedRow, 6);
                Object quantity = table.getValueAt(selectedRow, 7);
                Object reorderLevel = table.getValueAt(selectedRow, 8);
                Object categoryId = table.getValueAt(selectedRow, 9);
                Object categoryName = table.getValueAt(selectedRow, 10);

                nameField.setText(name);
                skuField.setText(sku);
                barcodeField.setText(barcode != null ? barcode : "");
                descriptionArea.setText(description != null ? description : "");
                costPriceField.setText(String.valueOf(costPrice));
                priceField.setText(String.valueOf(price));
                quantityField.setText(quantity != null ? quantity.toString() : "0");
                reorderLevelField.setText(reorderLevel != null ? reorderLevel.toString() : "0");
                categoryIdField.setText(categoryId != null ? categoryId.toString() : "");
                categoryIdField.setToolTipText(categoryName != null && !categoryName.toString().isBlank() ? "Category: " + categoryName : null);
                barcodesArea.setText(loadAdditionalBarcodes(selectedProductId));

                setFormEnabled(true);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }
    }

    private String loadAdditionalBarcodes(int productId) {
        String sql = "SELECT barcode FROM product_barcodes WHERE product_id = ? ORDER BY barcode";
        List<String> barcodes = new ArrayList<>();

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String barcode = rs.getString("barcode");
                    if (barcode != null && !barcode.isBlank()) {
                        barcodes.add(barcode);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load additional barcodes: " + ex.getMessage());
        }

        return String.join("\n", barcodes);
    }

    private int getCurrentInventoryQuantity(Connection conn, int productId, int locationId) throws SQLException {
        String sql = "SELECT quantity_on_hand FROM inventory WHERE product_id = ? AND location_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setInt(2, locationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("quantity_on_hand");
                }
            }
        }
        return 0;
    }

    private void logManualInventoryAdjustment(Connection conn, int productId, int locationId, int previousQuantity, int newQuantity) throws SQLException {
        int quantityChange = newQuantity - previousQuantity;
        if (quantityChange == 0) {
            return;
        }

        String sql = "INSERT INTO inventory_movements (product_id, location_id, change_qty, reason, note) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setInt(2, locationId);
            ps.setInt(3, quantityChange);
            ps.setString(4, "MANUAL_ADJUSTMENT");
            ps.setString(5, "Manual adjustment from Edit Item");
            ps.executeUpdate();
        }
    }

    private void saveChanges() {
        if (selectedProductId == -1) {
            JOptionPane.showMessageDialog(this, "No product selected.");
            return;
        }

        Integer selectedLocationId = requireCurrentSelectedLocationId();
        if (selectedLocationId == null) {
            return;
        }

        String name = nameField.getText().trim();
        String sku = skuField.getText().trim();
        String barcode = barcodeField.getText().trim();
        String description = descriptionArea.getText().trim();
        String barcodesText = barcodesArea.getText().trim();
        String costPriceText = costPriceField.getText().trim();
        String priceText = priceField.getText().trim();
        String quantityText = quantityField.getText().trim();
        String reorderLevelText = reorderLevelField.getText().trim();
        String categoryIdText = categoryIdField.getText().trim();

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

        if (name.isEmpty() || sku.isEmpty() || barcode.isEmpty() || costPriceText.isEmpty() || priceText.isEmpty() || quantityText.isEmpty() || reorderLevelText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name, SKU, Barcode, Cost Price, Price, Quantity, and Reorder Quantity are required.");
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
            JOptionPane.showMessageDialog(this, "Quantity must be a whole number.");
            return;
        }

        int reorderLevel;
        try {
            reorderLevel = Integer.parseInt(reorderLevelText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Reorder Quantity must be a whole number.");
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

        if (categoryId != null) {
            String validateCategorySql = "SELECT 1 FROM categories WHERE category_id = ?";
            try (Connection conn = DB.getConnection();
                 PreparedStatement validatePs = conn.prepareStatement(validateCategorySql)) {
                validatePs.setInt(1, categoryId);
                try (ResultSet rs = validatePs.executeQuery()) {
                    if (!rs.next()) {
                        JOptionPane.showMessageDialog(this, "Category ID " + categoryId + " does not exist.");
                        return;
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to validate category: " + ex.getMessage());
                return;
            }
        }

        String updateProductSql = "UPDATE products SET name = ?, sku = ?, barcode = ?, description = ?, cost_price = ?, price = ?, category_id = ? WHERE product_id = ?";
        String upsertInventorySql = """
                INSERT INTO inventory (product_id, location_id, quantity_on_hand, reorder_level)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (product_id, location_id)
                DO UPDATE SET
                    quantity_on_hand = EXCLUDED.quantity_on_hand,
                    reorder_level = EXCLUDED.reorder_level
                """;
        String deleteBarcodesSql = "DELETE FROM product_barcodes WHERE product_id = ?";
        String insertBarcodeSql = "INSERT INTO product_barcodes (product_id, barcode) VALUES (?, ?)";

        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement updatePs = conn.prepareStatement(updateProductSql);
                 PreparedStatement inventoryPs = conn.prepareStatement(upsertInventorySql);
                 PreparedStatement deletePs = conn.prepareStatement(deleteBarcodesSql);
                 PreparedStatement insertPs = conn.prepareStatement(insertBarcodeSql)) {
                int previousQuantity = getCurrentInventoryQuantity(conn, selectedProductId, selectedLocationId);

                updatePs.setString(1, name);
                updatePs.setString(2, sku);
                updatePs.setString(3, barcode);
                updatePs.setString(4, description);
                updatePs.setDouble(5, costPrice);
                updatePs.setDouble(6, price);

                if (categoryId != null) {
                    updatePs.setInt(7, categoryId);
                } else {
                    updatePs.setNull(7, java.sql.Types.INTEGER);
                }

                updatePs.setInt(8, selectedProductId);

                int rowsUpdated = updatePs.executeUpdate();
                if (rowsUpdated == 0) {
                    throw new SQLException("Update failed — product not found.");
                }

                inventoryPs.setInt(1, selectedProductId);
                inventoryPs.setInt(2, selectedLocationId);
                inventoryPs.setInt(3, quantity);
                inventoryPs.setInt(4, reorderLevel);
                inventoryPs.executeUpdate();

                logManualInventoryAdjustment(conn, selectedProductId, selectedLocationId, previousQuantity, quantity);

                deletePs.setInt(1, selectedProductId);
                deletePs.executeUpdate();

                for (String extraBarcode : extraBarcodes) {
                    insertPs.setInt(1, selectedProductId);
                    insertPs.setString(2, extraBarcode);
                    insertPs.addBatch();
                }
                if (!extraBarcodes.isEmpty()) {
                    insertPs.executeBatch();
                }

                conn.commit();
                JOptionPane.showMessageDialog(this, "Item updated successfully.");
                clearSelection();

            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to update item: " + ex.getMessage());
        }
    }

    private void clearSelection() {
        selectedProductId = -1;
        nameField.setText("");
        skuField.setText("");
        barcodeField.setText("");
        descriptionArea.setText("");
        barcodesArea.setText("");
        costPriceField.setText("");
        priceField.setText("");
        quantityField.setText("");
        reorderLevelField.setText("");
        categoryIdField.setText("");
        categoryIdField.setToolTipText(null);
        searchField.setText("");
        setFormEnabled(false);
        searchField.requestFocusInWindow();
    }

    private void setFormEnabled(boolean enabled) {
        nameField.setEnabled(enabled);
        skuField.setEnabled(enabled);
        barcodeField.setEnabled(enabled);
        descriptionArea.setEnabled(enabled);
        barcodesArea.setEnabled(enabled);
        costPriceField.setEnabled(enabled);
        priceField.setEnabled(enabled);
        quantityField.setEnabled(enabled);
        reorderLevelField.setEnabled(enabled);
        categoryIdField.setEnabled(enabled);
        saveButton.setEnabled(enabled);
        clearButton.setEnabled(enabled);
    }
}
