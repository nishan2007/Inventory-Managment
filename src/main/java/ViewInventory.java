import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.util.Comparator;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ViewInventory extends JFrame {

    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> stockFilterCombo;
    private JLabel totalItemsLabel;
    private JLabel totalProductsLabel;
    private JLabel locationLabel;

    public ViewInventory() {
        setTitle("View Inventory");
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        setJMenuBar(AppMenuBar.create(this, "ViewInventory"));


        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        add(mainPanel, BorderLayout.CENTER);

        mainPanel.add(buildHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(buildTablePanel(), BorderLayout.CENTER);
        mainPanel.add(buildFooterPanel(), BorderLayout.SOUTH);

        loadInventory(null, "All");
    }

    private JPanel buildHeaderPanel() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);

        JLabel titleLabel = new JLabel("Inventory Overview");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        locationLabel = new JLabel("Store: " + getCurrentLocationName());
        locationLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        locationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel filterPanel = new JPanel(new BorderLayout(10, 10));
        filterPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        filterPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.add(new JLabel("Search:"));
        searchField = new JTextField(22);
        leftPanel.add(searchField);

        JButton searchButton = new JButton("Search");
        leftPanel.add(searchButton);

        JButton refreshButton = new JButton("Refresh");
        leftPanel.add(refreshButton);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.add(new JLabel("Stock Filter:"));
        stockFilterCombo = new JComboBox<>(new String[]{"All", "In Stock", "Low Stock", "Out of Stock"});
        rightPanel.add(stockFilterCombo);

        filterPanel.add(leftPanel, BorderLayout.WEST);
        filterPanel.add(rightPanel, BorderLayout.EAST);

        searchButton.addActionListener(e -> loadInventory(searchField.getText().trim(), (String) stockFilterCombo.getSelectedItem()));
        refreshButton.addActionListener(e -> {
            searchField.setText("");
            stockFilterCombo.setSelectedIndex(0);
            loadInventory(null, "All");
        });
        stockFilterCombo.addActionListener(e -> loadInventory(searchField.getText().trim(), (String) stockFilterCombo.getSelectedItem()));
        searchField.addActionListener(e -> loadInventory(searchField.getText().trim(), (String) stockFilterCombo.getSelectedItem()));

        wrapper.add(titleLabel);
        wrapper.add(Box.createVerticalStrut(5));
        wrapper.add(locationLabel);
        wrapper.add(filterPanel);

        return wrapper;
    }

    private JScrollPane buildTablePanel() {
        String[] columns = {"Product ID", "SKU", "Name", "Description", "Category", "Price", "Quantity", "Reorder Level", "Status"};

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        inventoryTable = new JTable(tableModel);
        inventoryTable.setRowHeight(28);
        inventoryTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        inventoryTable.getTableHeader().setReorderingAllowed(false);
        inventoryTable.setFillsViewportHeight(true);
        inventoryTable.setDefaultRenderer(Object.class, new InventoryStatusRenderer());

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        sorter.setComparator(0, Comparator.comparingInt(value -> Integer.parseInt(String.valueOf(value))));
        sorter.setComparator(5, Comparator.comparingDouble(value -> {
            String text = String.valueOf(value).replace("$", "").replace(",", "").trim();
            if (text.isEmpty()) {
                return 0.0;
            }
            return Double.parseDouble(text);
        }));
        sorter.setComparator(6, Comparator.comparingInt(value -> Integer.parseInt(String.valueOf(value))));
        sorter.setComparator(7, Comparator.comparingInt(value -> Integer.parseInt(String.valueOf(value))));
        inventoryTable.setRowSorter(sorter);

        TableColumnModel columnModel = inventoryTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(80);
        columnModel.getColumn(1).setPreferredWidth(130);
        columnModel.getColumn(2).setPreferredWidth(180);
        columnModel.getColumn(3).setPreferredWidth(240);
        columnModel.getColumn(4).setPreferredWidth(120);
        columnModel.getColumn(5).setPreferredWidth(90);
        columnModel.getColumn(6).setPreferredWidth(90);
        columnModel.getColumn(7).setPreferredWidth(110);
        columnModel.getColumn(8).setPreferredWidth(110);

        return new JScrollPane(inventoryTable);
    }

    private JPanel buildFooterPanel() {
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));

        totalProductsLabel = new JLabel("Products: 0");
        totalItemsLabel = new JLabel("Units in Stock: 0");

        footerPanel.add(totalProductsLabel);
        footerPanel.add(totalItemsLabel);

        return footerPanel;
    }

    private class InventoryStatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            int modelRow = table.convertRowIndexToModel(row);
            Object quantityObj = table.getModel().getValueAt(modelRow, 6);
            Object reorderObj = table.getModel().getValueAt(modelRow, 7);

            int quantity = 0;
            int reorderLevel = 0;

            try {
                quantity = Integer.parseInt(String.valueOf(quantityObj));
            } catch (Exception ignored) {
            }

            try {
                reorderLevel = Integer.parseInt(String.valueOf(reorderObj));
            } catch (Exception ignored) {
            }

            if (isSelected) {
                component.setBackground(table.getSelectionBackground());
                component.setForeground(table.getSelectionForeground());
            } else {
                component.setForeground(Color.BLACK);

                if (quantity < 0) {
                    component.setBackground(new Color(255, 229, 204));
                } else if (quantity == 0) {
                    component.setBackground(new Color(255, 199, 206));
                } else if (quantity <= reorderLevel) {
                    component.setBackground(new Color(255, 242, 204));
                } else {
                    component.setBackground(Color.WHITE);
                }
            }

            return component;
        }
    }

    private void loadInventory(String searchText, String stockFilter) {
        tableModel.setRowCount(0);

        StringBuilder sql = new StringBuilder("""
                SELECT p.product_id,
                       p.sku,
                       p.name,
                       COALESCE(p.description, '') AS description,
                       COALESCE(c.name, '') AS category_name,
                       COALESCE(p.price, 0) AS price,
                       COALESCE(i.quantity_on_hand, 0) AS quantity_on_hand,
                       COALESCE(i.reorder_level, 0) AS reorder_level
                FROM products p
                LEFT JOIN inventory i ON p.product_id = i.product_id
                LEFT JOIN categories c ON p.category_id = c.category_id
                WHERE 1 = 1
                """);

        Integer currentLocationId = getCurrentLocationId();
        if (currentLocationId != null) {
            sql.append(" AND (i.location_id = ? OR i.location_id IS NULL)");
        }

        boolean hasSearch = searchText != null && !searchText.isBlank();
        if (hasSearch) {
            sql.append(" AND (CAST(p.product_id AS TEXT) ILIKE ? OR p.sku ILIKE ? OR p.name ILIKE ? OR COALESCE(p.description, '') ILIKE ?)");
        }

        if ("In Stock".equals(stockFilter)) {
            sql.append(" AND COALESCE(i.quantity_on_hand, 0) > 0");
        } else if ("Low Stock".equals(stockFilter)) {
            sql.append(" AND COALESCE(i.quantity_on_hand, 0) <= COALESCE(i.reorder_level, 0) AND COALESCE(i.quantity_on_hand, 0) > 0");
        } else if ("Out of Stock".equals(stockFilter)) {
            sql.append(" AND COALESCE(i.quantity_on_hand, 0) <= 0");
        }

        sql.append(" ORDER BY p.name ASC");

        int totalProducts = 0;
        int totalUnits = 0;

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (currentLocationId != null) {
                ps.setInt(paramIndex++, currentLocationId);
            }

            if (hasSearch) {
                String pattern = "%" + searchText + "%";
                ps.setString(paramIndex++, pattern);
                ps.setString(paramIndex++, pattern);
                ps.setString(paramIndex++, pattern);
                ps.setString(paramIndex++, pattern);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int productId = rs.getInt("product_id");
                    String sku = rs.getString("sku");
                    String name = rs.getString("name");
                    String description = rs.getString("description");
                    String category = rs.getString("category_name");
                    double price = rs.getDouble("price");
                    int quantity = rs.getInt("quantity_on_hand");
                    int reorderLevel = rs.getInt("reorder_level");
                    String status = getStockStatus(quantity, reorderLevel);

                    tableModel.addRow(new Object[]{
                            productId,
                            sku,
                            name,
                            description,
                            category,
                            String.format("$%.2f", price),
                            quantity,
                            reorderLevel,
                            status
                    });

                    totalProducts++;
                    totalUnits += quantity;
                }
            }

            totalProductsLabel.setText("Products: " + totalProducts);
            totalItemsLabel.setText("Units in Stock: " + totalUnits);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load inventory.\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getStockStatus(int quantity, int reorderLevel) {
        if (quantity <= 0) {
            return "Out of Stock";
        }
        if (quantity <= reorderLevel) {
            return "Low Stock";
        }
        return "In Stock";
    }

    private Integer getCurrentLocationId() {
        try {
            return Login.currentLocationId;
        } catch (Exception e) {
            return null;
        }
    }

    private String getCurrentLocationName() {
        try {
            if (Login.currentLocationName != null && !Login.currentLocationName.isBlank()) {
                return Login.currentLocationName;
            }
        } catch (Exception ignored) {
        }
        return "All Locations";
    }
}
