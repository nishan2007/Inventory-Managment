import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EditItem extends JFrame {

    private JTextField searchField;
    private JButton searchBtn;

    private JTextField nameField;
    private JTextField skuField;
    private JTextField priceField;
    private JTextField categoryIdField;

    private JButton saveButton;
    private JButton clearButton;
    private JButton cancelButton;

    private int selectedProductId = -1;

    public EditItem() {
        setTitle("Edit Item");
        setSize(500, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setJMenuBar(AppMenuBar.create(this, "EditItem"));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // --- Search Panel ---
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Find Product"));

        searchField = new JTextField();
        searchBtn = new JButton("Search");

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchBtn, BorderLayout.EAST);

        // --- Form Panel ---
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Edit Details"));

        nameField = new JTextField();
        skuField = new JTextField();
        priceField = new JTextField();
        categoryIdField = new JTextField();

        formPanel.add(new JLabel("Item Name:"));
        formPanel.add(nameField);

        formPanel.add(new JLabel("SKU:"));
        formPanel.add(skuField);

        formPanel.add(new JLabel("Price:"));
        formPanel.add(priceField);

        formPanel.add(new JLabel("Category ID (optional):"));
        formPanel.add(categoryIdField);

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        saveButton = new JButton("Save Changes");
        clearButton = new JButton("Clear Selection");
        cancelButton = new JButton("Close");

        buttonPanel.add(saveButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(cancelButton);

        setFormEnabled(false);

        // --- Layout Assembly ---
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(searchPanel, BorderLayout.NORTH);
        centerPanel.add(formPanel, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // --- Action Listeners ---
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

    private void searchProduct() {
        String searchText = searchField.getText().trim();

        if (searchText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a product name or SKU to search.");
            return;
        }

        String sql = """
                SELECT p.product_id,
                       p.name,
                       p.sku,
                       p.price,
                       p.category_id,
                       c.name AS category_name
                FROM products p
                LEFT JOIN categories c ON p.category_id = c.category_id
                WHERE p.name ILIKE ? OR p.sku ILIKE ?
                ORDER BY p.name
                """;

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + searchText + "%");
            ps.setString(2, "%" + searchText + "%");

            ResultSet rs = ps.executeQuery();

            java.util.List<Object[]> rows = new java.util.ArrayList<>();
            while (rs.next()) {
                rows.add(new Object[]{
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getString("sku"),
                        rs.getDouble("price"),
                        rs.getObject("category_id") != null ? rs.getInt("category_id") : "",
                        rs.getString("category_name") != null ? rs.getString("category_name") : ""
                });
            }

            if (rows.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No matching products found.");
                return;
            }

            String[] columns = {"ID", "Name", "SKU", "Price", "Category ID", "Category"};
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

            int result = JOptionPane.showConfirmDialog(
                    this,
                    scrollPane,
                    "Select a Product to Edit",
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
                double price = (double) table.getValueAt(selectedRow, 3);
                Object categoryId = table.getValueAt(selectedRow, 4);
                Object categoryName = table.getValueAt(selectedRow, 5);

                nameField.setText(name);
                skuField.setText(sku);
                priceField.setText(String.valueOf(price));
                categoryIdField.setText(categoryId != null ? categoryId.toString() : "");
                categoryIdField.setToolTipText(categoryName != null && !categoryName.toString().isBlank() ? "Category: " + categoryName : null);

                setFormEnabled(true);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }
    }

    private void saveChanges() {
        if (selectedProductId == -1) {
            JOptionPane.showMessageDialog(this, "No product selected.");
            return;
        }

        String name = nameField.getText().trim();
        String sku = skuField.getText().trim();
        String priceText = priceField.getText().trim();
        String categoryIdText = categoryIdField.getText().trim();

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

        String sql = "UPDATE products SET name = ?, sku = ?, price = ?, category_id = ? WHERE product_id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, sku);
            ps.setDouble(3, price);

            if (categoryId != null) {
                ps.setInt(4, categoryId);
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }

            ps.setInt(5, selectedProductId);

            int rowsUpdated = ps.executeUpdate();

            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(this, "Item updated successfully.");
                clearSelection();
            } else {
                JOptionPane.showMessageDialog(this, "Update failed — product not found.");
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
        priceField.setText("");
        categoryIdField.setText("");
        categoryIdField.setToolTipText(null);
        searchField.setText("");
        setFormEnabled(false);
        searchField.requestFocusInWindow();
    }

    private void setFormEnabled(boolean enabled) {
        nameField.setEnabled(enabled);
        skuField.setEnabled(enabled);
        priceField.setEnabled(enabled);
        categoryIdField.setEnabled(enabled);
        saveButton.setEnabled(enabled);
        clearButton.setEnabled(enabled);
    }
}
