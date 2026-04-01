import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import javax.swing.table.DefaultTableModel;
import java.math.BigDecimal;

public class MakeASale extends JFrame {
    private JTextField searchField;
    private JButton searchBtn;
    private JTable cartTable;
    private DefaultTableModel cartModel;
    private boolean updatingCart = false;
    private JLabel totalLabel;
    private JComboBox<String> paymentMethodBox;
    private JButton checkoutBtn;
    private JLabel selectedStoreLabel;
    private JButton newItemBtn;

   public MakeASale() {
       //Window Setup
       setTitle("Make a Sale");
      // setDefaultCloseOperation(DISPOSE_ON_CLOSE);
      // setExtendedState(JFrame.MAXIMIZED_BOTH);
       setSize(1000, 600);
       setLocationRelativeTo(null);
       setDefaultCloseOperation(DISPOSE_ON_CLOSE);
       setJMenuBar(AppMenuBar.create(this, "MakeASale"));

       // Main container
       JPanel panel = new JPanel(new BorderLayout(10, 10));
       panel.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

       // Search area
       JPanel searchPanel = new JPanel(new BorderLayout(10, 10));

       newItemBtn = new JButton("New Item");
       JLabel searchLabel = new JLabel("Search Product");
       searchField = new JTextField();
       searchBtn = new JButton("Search");
       selectedStoreLabel = new JLabel("Store: Not selected");

        // Top row (store + new item)
       JPanel topRow = new JPanel(new BorderLayout(10, 10));
       topRow.add(selectedStoreLabel, BorderLayout.WEST);
       topRow.add(newItemBtn, BorderLayout.EAST);

        // Search row (THIS is the important part)
       JPanel searchRow = new JPanel(new BorderLayout(10, 10));
       searchRow.add(searchLabel, BorderLayout.WEST);
       searchRow.add(searchField, BorderLayout.CENTER); // EXPANDS
       searchRow.add(searchBtn, BorderLayout.EAST);

       searchPanel.add(topRow, BorderLayout.NORTH);
       searchPanel.add(searchRow, BorderLayout.SOUTH);

       // Cart table
       cartModel = new DefaultTableModel(
               new Object[]{"ID", "Name", "SKU", "Price", "Qty", "Line Total"},
               0
       ) {
           @Override
           public boolean isCellEditable(int row, int column) {
               return column == 3 || column == 4; // Price and Qty editable
           }
       };
       cartTable = new JTable(cartModel);
       JScrollPane cartScrollPane = new JScrollPane(cartTable);
       cartTable.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(new JTextField()));

       panel.add(searchPanel, BorderLayout.NORTH);
       panel.add(cartScrollPane, BorderLayout.CENTER);

       JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
       bottomPanel.add(new JLabel("Payment Method:"));
       paymentMethodBox = new JComboBox<>(new String[]{"CASH", "CARD", "CHEQUE"});
       bottomPanel.add(paymentMethodBox);

       totalLabel = new JLabel("Overall Total: $0.00");
       bottomPanel.add(totalLabel);

       checkoutBtn = new JButton("Checkout");
       bottomPanel.add(checkoutBtn);

       panel.add(bottomPanel, BorderLayout.SOUTH);

       //Add panel to frame
       add(panel);

       //Action Listeners
       newItemBtn.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
              new NewItem().setVisible(true);
           }
       });
       searchBtn.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               Search();
           }
       });
       searchField.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               Search();
           }
       });
       cartModel.addTableModelListener(e -> {
           if (updatingCart) {
               return;
           }
           if (e.getColumn() == 3 || e.getColumn() == 4 || e.getColumn() == javax.swing.event.TableModelEvent.ALL_COLUMNS) {
               updateLineTotals();
           }
       });
       checkoutBtn.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               checkout();
           }
       });
       updateSelectedStoreLabel();
       setVisible(true); //runs last for the main UI to show
   }


    private void Search() {
        String searchText = searchField.getText().trim();

        if (Login.currentLocationId == null) {
            JOptionPane.showMessageDialog(this, "No store is selected for this session.");
            return;
        }

        if (searchText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Type a product name or SKU first.");
            return;
        }

        String sql = """
            SELECT p.product_id, p.name, p.sku, p.price,
                   COALESCE(i.quantity_on_hand, 0) AS quantity_on_hand
            FROM products p
            LEFT JOIN inventory i
                ON p.product_id = i.product_id
               AND i.location_id = ?
            WHERE p.name LIKE ? OR p.sku LIKE ?
            ORDER BY p.name
            """;

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Login.currentLocationId);
            ps.setString(2, "%" + searchText + "%");
            ps.setString(3, "%" + searchText + "%");

            ResultSet rs = ps.executeQuery();

            java.util.List<Object[]> rows = new java.util.ArrayList<>();

            while (rs.next()) {
                rows.add(new Object[]{
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getString("sku"),
                        rs.getDouble("price"),
                        rs.getInt("quantity_on_hand")
                });
            }

            if (rows.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No matching products found.");
                return;
            }

            String[] columns = {"ID", "Name", "SKU", "Price", "Stock"};
            Object[][] data = rows.toArray(new Object[0][]);

            JTable table = new JTable(data, columns);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setPreferredSize(new Dimension(600, 250));

            int result = JOptionPane.showConfirmDialog(
                    this,
                    scrollPane,
                    "Select a Product",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result == JOptionPane.OK_OPTION) {
                int selectedRow = table.getSelectedRow();

                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(this, "Please select a product.");
                    return;
                }

                int productId = (int) table.getValueAt(selectedRow, 0);
                String name = (String) table.getValueAt(selectedRow, 1);
                String sku = (String) table.getValueAt(selectedRow, 2);
                double price = (double) table.getValueAt(selectedRow, 3);
                int stock = (int) table.getValueAt(selectedRow, 4);

                String qtyText = JOptionPane.showInputDialog(this, "Enter quantity:", "1");

                if (qtyText == null) {
                    return;
                }

                int qty;
                try {
                    qty = Integer.parseInt(qtyText);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid number.");
                    return;
                }



                addToCart(productId, name, sku, price, qty);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }
    private void addToCart(int productId, String name, String sku, double price, int qty) {
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            int existingProductId = (int) cartModel.getValueAt(i, 0);

            if (existingProductId == productId) {
                int existingQty = Integer.parseInt(cartModel.getValueAt(i, 4).toString());
                int newQty = existingQty + qty;
                double newLineTotal = price * newQty;

                cartModel.setValueAt(newQty, i, 4);
                cartModel.setValueAt(newLineTotal, i, 5);
                updateOverallTotal();
                return;
            }
        }

        double lineTotal = price * qty;
        cartModel.addRow(new Object[]{productId, name, sku, price, qty, lineTotal});
        updateLineTotals();
    }
    private void updateLineTotals() {
        updatingCart = true;
        try {
            for (int i = 0; i < cartModel.getRowCount(); i++) {
                Object qtyValue = cartModel.getValueAt(i, 4);
                Object priceValue = cartModel.getValueAt(i, 3);

                int qty;
                double price;

                try {
                    qty = Integer.parseInt(qtyValue.toString());
                } catch (NumberFormatException ex) {
                    qty = 1;
                }

                try {
                    price = Double.parseDouble(priceValue.toString());
                } catch (NumberFormatException ex) {
                    price = 0.0;
                }


                cartModel.setValueAt(qty, i, 4);
                cartModel.setValueAt(price * qty, i, 5);
            }
            updateOverallTotal();
        } finally {
            updatingCart = false;
        }
    }

    private double getOverallTotal() {
        double total = 0.0;

        for (int i = 0; i < cartModel.getRowCount(); i++) {
            Object lineTotalValue = cartModel.getValueAt(i, 5);
            try {
                total += Double.parseDouble(lineTotalValue.toString());
            } catch (NumberFormatException ex) {
                // ignore invalid values
            }
        }

        return total;
    }

    private void updateSelectedStoreLabel() {
        if (selectedStoreLabel == null) {
            return;
        }

        if (Login.currentLocationId == null || Login.currentLocationName == null) {
            selectedStoreLabel.setText("Store: Not selected");
        } else {
            selectedStoreLabel.setText("Store: " + Login.currentLocationName + " (ID: " + Login.currentLocationId + ")");
        }
    }

    private void checkout() {
        if (cartModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Cart is empty.");
            return;
        }

        String paymentMethod = (String) paymentMethodBox.getSelectedItem();

        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);

            if (Login.currentLocationId == null) {
                conn.setAutoCommit(true);
                JOptionPane.showMessageDialog(this, "No store is selected for this session.");
                return;
            }

            int locationId = Login.currentLocationId;

            try {
                String insertSaleSql = "INSERT INTO sales (location_id, total_amount, status, payment_method) VALUES (?, ?, ?, ?)";
                int saleId;

                try (PreparedStatement saleStmt = conn.prepareStatement(insertSaleSql, Statement.RETURN_GENERATED_KEYS)) {
                    saleStmt.setInt(1, locationId);
                    saleStmt.setBigDecimal(2, BigDecimal.valueOf(getOverallTotal()));
                    saleStmt.setString(3, "COMPLETED");
                    saleStmt.setString(4, paymentMethod);
                    saleStmt.executeUpdate();

                    try (ResultSet generatedKeys = saleStmt.getGeneratedKeys()) {
                        if (!generatedKeys.next()) {
                            throw new SQLException("Failed to create sale.");
                        }
                        saleId = generatedKeys.getInt(1);
                    }
                }

                String insertItemSql = "INSERT INTO sale_items (sale_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
                String insertMovementSql = "INSERT INTO inventory_movements (product_id, location_id, change_qty, reason, note) VALUES (?, ?, ?, ?, ?)";
                String ensureInventorySql = "INSERT INTO inventory (product_id, location_id, quantity_on_hand, reorder_level) VALUES (?, ?, 0, 0) ON DUPLICATE KEY UPDATE inventory_id = inventory_id";
                String updateInventorySql = "UPDATE inventory SET quantity_on_hand = quantity_on_hand - ? WHERE product_id = ? AND location_id = ?";

                try (PreparedStatement itemStmt = conn.prepareStatement(insertItemSql);
                     PreparedStatement movementStmt = conn.prepareStatement(insertMovementSql);
                     PreparedStatement ensureInventoryStmt = conn.prepareStatement(ensureInventorySql);
                     PreparedStatement updateInventoryStmt = conn.prepareStatement(updateInventorySql)) {

                    for (int i = 0; i < cartModel.getRowCount(); i++) {
                        int productId = Integer.parseInt(cartModel.getValueAt(i, 0).toString());
                        int qty = Integer.parseInt(cartModel.getValueAt(i, 4).toString());
                        double price = Double.parseDouble(cartModel.getValueAt(i, 3).toString());

                        itemStmt.setInt(1, saleId);
                        itemStmt.setInt(2, productId);
                        itemStmt.setInt(3, qty);
                        itemStmt.setBigDecimal(4, BigDecimal.valueOf(price));
                        itemStmt.addBatch();

                        ensureInventoryStmt.setInt(1, productId);
                        ensureInventoryStmt.setInt(2, locationId);
                        ensureInventoryStmt.addBatch();

                        movementStmt.setInt(1, productId);
                        movementStmt.setInt(2, locationId);
                        movementStmt.setInt(3, -qty);
                        movementStmt.setString(4, "SALE");
                        movementStmt.setString(5, "sale_id=" + saleId);
                        movementStmt.addBatch();

                        updateInventoryStmt.setInt(1, qty);
                        updateInventoryStmt.setInt(2, productId);
                        updateInventoryStmt.setInt(3, locationId);
                        updateInventoryStmt.addBatch();
                    }

                    itemStmt.executeBatch();
                    ensureInventoryStmt.executeBatch();
                    movementStmt.executeBatch();
                    updateInventoryStmt.executeBatch();
                }

                conn.commit();
                JOptionPane.showMessageDialog(this, "Sale completed successfully. Sale ID: " + saleId);
                cartModel.setRowCount(0);
                searchField.setText("");
                updateOverallTotal();

            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Checkout failed: " + ex.getMessage());
        }
    }

    private void updateOverallTotal() {
        double total = 0.0;

        for (int i = 0; i < cartModel.getRowCount(); i++) {
            Object lineTotalValue = cartModel.getValueAt(i, 5);

            try {
                total += Double.parseDouble(lineTotalValue.toString());
            } catch (NumberFormatException ex) {
                // ignore invalid values
            }
        }

        totalLabel.setText(String.format("Overall Total: $%.2f", total));
    }

}



/*        JOptionPane.showMessageDialog(this,
                "Text:\n" + searchText,
                "Search Text",
                JOptionPane.INFORMATION_MESSAGE); */
