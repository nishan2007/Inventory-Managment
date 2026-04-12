import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.util.Vector;

public class ViewSales extends JFrame {

    private JTable salesTable;
    private DefaultTableModel salesTableModel;
    private JTextField searchField;
    private JTextField fromDateField;
    private JTextField toDateField;
    private JLabel summaryLabel;

    private final DateTimeFormatter dbDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();

    public ViewSales() {
        setTitle("View Sales");
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));

        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(new EmptyBorder(14, 14, 14, 14));
        add(mainPanel, BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.add(buildHeaderPanel(), BorderLayout.NORTH);
        topPanel.add(buildFilterPanel(), BorderLayout.CENTER);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        salesTableModel = new DefaultTableModel(
                new Object[]{"Sale ID", "Date / Time", "Cashier", "Store", "Items", "Payment", "Total"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        salesTable = new JTable(salesTableModel);
        salesTable.setRowHeight(26);
        salesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        salesTable.getTableHeader().setReorderingAllowed(false);
        salesTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        salesTable.getColumnModel().getColumn(1).setPreferredWidth(170);
        salesTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        salesTable.getColumnModel().getColumn(3).setPreferredWidth(160);
        salesTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        salesTable.getColumnModel().getColumn(5).setPreferredWidth(120);
        salesTable.getColumnModel().getColumn(6).setPreferredWidth(120);

        JScrollPane scrollPane = new JScrollPane(salesTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        summaryLabel = new JLabel("Transactions: 0");
        summaryLabel.setBorder(new EmptyBorder(6, 2, 0, 2));
        bottomPanel.add(summaryLabel, BorderLayout.WEST);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        loadSales();
    }

    private JPanel buildHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("Previous Transactions");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));

        JLabel subtitleLabel = new JLabel("Search and review completed sales.");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(subtitleLabel);

        headerPanel.add(textPanel, BorderLayout.WEST);
        return headerPanel;
    }

    private JPanel buildFilterPanel() {
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210), 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel searchLabel = new JLabel("Search");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        filterPanel.add(searchLabel, gbc);

        searchField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        filterPanel.add(searchField, gbc);

        JLabel fromLabel = new JLabel("From Date");
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0;
        filterPanel.add(fromLabel, gbc);

        fromDateField = new JTextField();
        fromDateField.setToolTipText("yyyy-MM-dd");
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 0.35;
        filterPanel.add(fromDateField, gbc);

        JLabel toLabel = new JLabel("To Date");
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.weightx = 0;
        filterPanel.add(toLabel, gbc);

        toDateField = new JTextField();
        toDateField.setToolTipText("yyyy-MM-dd");
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.weightx = 0.35;
        filterPanel.add(toDateField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton refreshButton = new JButton("Refresh");
        JButton clearButton = new JButton("Clear Filters");
        JButton detailsButton = new JButton("View Details");

        buttonPanel.add(refreshButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(detailsButton);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 6;
        gbc.weightx = 1;
        filterPanel.add(buttonPanel, gbc);

        refreshButton.addActionListener(e -> loadSales());
        clearButton.addActionListener(e -> clearFilters());
        detailsButton.addActionListener(e -> showSelectedSaleDetails());

        searchField.addActionListener(e -> loadSales());
        fromDateField.addActionListener(e -> loadSales());
        toDateField.addActionListener(e -> loadSales());

        return filterPanel;
    }

    private void clearFilters() {
        searchField.setText("");
        fromDateField.setText("");
        toDateField.setText("");
        loadSales();
    }

    private void loadSales() {
        salesTableModel.setRowCount(0);

        StringBuilder sql = new StringBuilder(
                "SELECT s.sale_id, s.created_at, " +
                        "COALESCE(u.full_name, u.username, 'Unknown') AS cashier_name, " +
                        "COALESCE(l.name, 'Unknown') AS store_name, " +
                        "COUNT(si.sale_item_id) AS item_count, " +
                        "COALESCE(s.payment_method, '') AS payment_method, " +
                        "COALESCE(s.total_amount, 0) AS total_amount " +
                        "FROM sales s " +
                        "LEFT JOIN users u ON s.user_id = u.user_id " +
                        "LEFT JOIN locations l ON s.location_id = l.location_id " +
                        "LEFT JOIN sale_items si ON s.sale_id = si.sale_id " +
                        "WHERE 1=1 "
        );

        Vector<Object> parameters = new Vector<>();

        String searchText = searchField.getText().trim();
        if (!searchText.isEmpty()) {
            sql.append("AND (CAST(s.sale_id AS TEXT) ILIKE ? OR ")
                    .append("COALESCE(u.full_name, u.username, '') ILIKE ? OR ")
                    .append("COALESCE(l.name, '') ILIKE ? OR ")
                    .append("COALESCE(s.payment_method, '') ILIKE ?) ");
            String likeValue = "%" + searchText + "%";
            parameters.add(likeValue);
            parameters.add(likeValue);
            parameters.add(likeValue);
            parameters.add(likeValue);
        }

        LocalDate fromDate = parseDate(fromDateField.getText().trim(), "From Date");
        if (fromDate == null && !fromDateField.getText().trim().isEmpty()) {
            return;
        }

        LocalDate toDate = parseDate(toDateField.getText().trim(), "To Date");
        if (toDate == null && !toDateField.getText().trim().isEmpty()) {
            return;
        }

        if (fromDate != null) {
            sql.append("AND s.created_at >= ? ");
            parameters.add(Timestamp.valueOf(fromDate.atStartOfDay()));
        }

        if (toDate != null) {
            sql.append("AND s.created_at < ? ");
            parameters.add(Timestamp.valueOf(toDate.plusDays(1).atStartOfDay()));
        }

        sql.append("GROUP BY s.sale_id, s.created_at, cashier_name, store_name, s.payment_method, s.total_amount ")
                .append("ORDER BY s.created_at DESC");

        double grandTotal = 0;
        int transactionCount = 0;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                ps.setObject(i + 1, parameters.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int saleId = rs.getInt("sale_id");
                    Timestamp saleTimestamp = rs.getTimestamp("created_at");
                    String cashier = rs.getString("cashier_name");
                    String store = rs.getString("store_name");
                    int itemCount = rs.getInt("item_count");
                    String paymentMethod = rs.getString("payment_method");
                    double total = rs.getDouble("total_amount");

                    salesTableModel.addRow(new Object[]{
                            saleId,
                            formatTimestamp(saleTimestamp),
                            cashier,
                            store,
                            itemCount,
                            paymentMethod,
                            currencyFormat.format(total)
                    });

                    grandTotal += total;
                    transactionCount++;
                }
            }

            summaryLabel.setText("Transactions: " + transactionCount + "    Total Sales: " + currencyFormat.format(grandTotal));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load sales.\n\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSelectedSaleDetails() {
        int selectedRow = salesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a transaction first.",
                    "No Transaction Selected",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int saleId = Integer.parseInt(String.valueOf(salesTableModel.getValueAt(selectedRow, 0)));
        showSaleDetailsDialog(saleId);
    }

    private void showSaleDetailsDialog(int saleId) {
        DefaultTableModel detailsModel = new DefaultTableModel(
                new Object[]{"Product ID", "Item Name", "Qty", "Unit Price", "Line Total"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        String detailsSql =
                "SELECT COALESCE(p.product_id, 0) AS product_id, " +
                        "COALESCE(p.name, 'Deleted Item') AS product_name, " +
                        "COALESCE(si.quantity, 0) AS quantity, " +
                        "COALESCE(si.unit_price, 0) AS unit_price, " +
                        "COALESCE(si.quantity, 0) * COALESCE(si.unit_price, 0) AS line_total " +
                        "FROM sale_items si " +
                        "LEFT JOIN products p ON si.product_id = p.product_id " +
                        "WHERE si.sale_id = ? " +
                        "ORDER BY si.sale_item_id ASC";

        double total = 0;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(detailsSql)) {

            ps.setInt(1, saleId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double lineTotal = rs.getDouble("line_total");
                    detailsModel.addRow(new Object[]{
                            rs.getInt("product_id"),
                            rs.getString("product_name"),
                            rs.getInt("quantity"),
                            currencyFormat.format(rs.getDouble("unit_price")),
                            currencyFormat.format(lineTotal)
                    });
                    total += lineTotal;
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load sale details.\n\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        JTable detailsTable = new JTable(detailsModel);
        detailsTable.setRowHeight(24);
        detailsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        detailsTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(detailsTable);
        scrollPane.setPreferredSize(new Dimension(700, 300));

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(scrollPane, BorderLayout.CENTER);

        JLabel totalLabel = new JLabel("Sale Total: " + currencyFormat.format(total));
        totalLabel.setBorder(new EmptyBorder(4, 4, 0, 4));
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        panel.add(totalLabel, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(
                this,
                panel,
                "Transaction Details - Sale #" + saleId,
                JOptionPane.PLAIN_MESSAGE
        );
    }

    private LocalDate parseDate(String text, String label) {
        if (text == null || text.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                    label + " must be in yyyy-MM-dd format.",
                    "Invalid Date",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
    }

    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) {
            return "";
        }

        LocalDateTime utcDateTime = timestamp.toLocalDateTime();
        ZonedDateTime localDateTime = utcDateTime.atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.systemDefault());
        return dbDateTimeFormatter.format(localDateTime);
    }

    private Connection getConnection() throws SQLException {
        return DB.getConnection();
    }
}