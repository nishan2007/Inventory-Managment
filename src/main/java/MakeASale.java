import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.table.JTableHeader;


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
    private JLabel currentUserLabel;
    private JButton editItemBtn;
    private JButton newItemBtn;
    private JLabel currentDateLabel;
    private JLabel currentTimeLabel;
    private String lastShownDate;
    private JPopupMenu searchPopup;
    private JTable searchResultsTable;
    private JScrollPane searchResultsScrollPane;
    private javax.swing.Timer searchDebounceTimer;

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

       JLabel logoLabel = new JLabel();
       logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
       ImageIcon centerLogoIcon = loadCenterLogoIcon();
       if (centerLogoIcon != null) {
           Image scaledImage = centerLogoIcon.getImage().getScaledInstance(180, 80, Image.SCALE_SMOOTH);
           logoLabel.setIcon(new ImageIcon(scaledImage));
       } else {
           logoLabel.setText("SmartStock");
       }

       newItemBtn = new JButton("New Item");
       JLabel searchLabel = new JLabel("Search Product");
       searchField = new JTextField();
       searchBtn = new JButton("Search");
       selectedStoreLabel = new JLabel("Store: Not selected");
       currentUserLabel = new JLabel("No User currently loged in");
       editItemBtn = new JButton("Edit Item");
       currentDateLabel = new JLabel("No date yet");
       currentTimeLabel = new JLabel("no time yet");

       JPanel rightSidePanel = new JPanel();
       rightSidePanel.setLayout(new BoxLayout(rightSidePanel, BoxLayout.Y_AXIS));
       selectedStoreLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
       currentUserLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
       currentDateLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
       currentTimeLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

       updateCurrentDateLabel();
       updateCurrentTimeLabel();
       startDateRefreshTimer();

       rightSidePanel.add(currentDateLabel);
       rightSidePanel.add(Box.createVerticalStrut(5));
       rightSidePanel.add(currentTimeLabel);
       rightSidePanel.add(Box.createVerticalStrut(10));
       rightSidePanel.add(selectedStoreLabel);
       rightSidePanel.add(Box.createVerticalStrut(10));
       rightSidePanel.add(currentUserLabel);


       JPanel leftSidePanel = new JPanel();
       leftSidePanel.setLayout(new BoxLayout(leftSidePanel, BoxLayout.Y_AXIS));
       newItemBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
       editItemBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
       leftSidePanel.add(newItemBtn);
       leftSidePanel.add(Box.createVerticalStrut(30));
       leftSidePanel.add(editItemBtn);

       JPanel centerSection = new JPanel(new BorderLayout(20, 10));
       centerSection.add(leftSidePanel, BorderLayout.WEST);
       centerSection.add(logoLabel, BorderLayout.CENTER);
       centerSection.add(rightSidePanel, BorderLayout.EAST);

        // Search row (THIS is the important part)
       JPanel searchRow = new JPanel(new BorderLayout(10, 10));
       searchRow.add(searchLabel, BorderLayout.WEST);
       searchRow.add(searchField, BorderLayout.CENTER); // EXPANDS
       searchRow.add(searchBtn, BorderLayout.EAST);

       searchPanel.add(centerSection, BorderLayout.CENTER);
       searchPanel.add(searchRow, BorderLayout.SOUTH);

       // Cart table
       cartModel = new DefaultTableModel(
               new Object[]{"ID", "Name", "Description", "SKU", "Price", "Qty", "Line Total"},
               0
       ) {
           @Override
           public boolean isCellEditable(int row, int column) {
               return column == 4 || column == 5; // Price and Qty editable
           }
       };
       cartTable = new JTable(cartModel);
       cartTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
       JScrollPane cartScrollPane = new JScrollPane(cartTable);
       cartTable.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(new JTextField()));
       cartTable.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(new JTextField()));
       configureCartTableColumns();

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
       refreshPermissionButtons();

       //Action Listeners
       newItemBtn.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               if (!PermissionManager.requirePermission("NEW_ITEM", MakeASale.this, "New Item")) {
                   refreshPermissionButtons();
                   return;
               }
               if (Login.currentLocationId == null) {
                   JOptionPane.showMessageDialog(MakeASale.this, "No store is selected for this session.");
                   return;
               }
               if (WindowHelper.focusIfAlreadyOpen(NewItem.class)) {
                   return;
               }
               new NewItem(Login.currentLocationId).setVisible(true);
           }
       });
       editItemBtn.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               if (!PermissionManager.requirePermission("EDIT_ITEM", MakeASale.this, "Edit Item")) {
                   refreshPermissionButtons();
                   return;
               }
               if (WindowHelper.focusIfAlreadyOpen(EditItem.class)) {
                   return;
               }
               EditItem screen = new EditItem();
               screen.setLocationRelativeTo(MakeASale.this);
               screen.setVisible(true);
           }
       });
       searchBtn.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               searchProducts();
           }
       });
       searchField.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               addSelectedSearchResultToCart();
           }
       });
       searchField.getDocument().addDocumentListener(new DocumentListener() {
           private void restartSearchDebounce() {
               if (searchDebounceTimer == null) {
                   searchDebounceTimer = new javax.swing.Timer(250, e -> searchProducts(false));
                   searchDebounceTimer.setRepeats(false);
               }

               searchDebounceTimer.restart();
           }

           @Override
           public void insertUpdate(DocumentEvent e) {
               SwingUtilities.invokeLater(this::restartSearchDebounce);
           }

           @Override
           public void removeUpdate(DocumentEvent e) {
               SwingUtilities.invokeLater(this::restartSearchDebounce);
           }

           @Override
           public void changedUpdate(DocumentEvent e) {
               SwingUtilities.invokeLater(this::restartSearchDebounce);
           }
       });
       searchField.addKeyListener(new java.awt.event.KeyAdapter() {
           @Override
           public void keyPressed(java.awt.event.KeyEvent e) {
               if (searchResultsTable == null || searchResultsTable.getRowCount() == 0) {
                   return;
               }

               int selectedRow = searchResultsTable.getSelectedRow();

               if (e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN) {
                   int nextRow = Math.min(selectedRow + 1, searchResultsTable.getRowCount() - 1);
                   if (nextRow >= 0) {
                       searchResultsTable.setRowSelectionInterval(nextRow, nextRow);
                       searchResultsTable.scrollRectToVisible(searchResultsTable.getCellRect(nextRow, 0, true));
                   }
                   e.consume();
               } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_UP) {
                   int nextRow = Math.max(selectedRow - 1, 0);
                   if (nextRow >= 0) {
                       searchResultsTable.setRowSelectionInterval(nextRow, nextRow);
                       searchResultsTable.scrollRectToVisible(searchResultsTable.getCellRect(nextRow, 0, true));
                   }
                   e.consume();
               } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                   closeSearchPopup();
               }
           }
       });
       cartModel.addTableModelListener(e -> {
           if (updatingCart) {
               return;
           }
           if (e.getColumn() == 4 || e.getColumn() == 5 || e.getColumn() == javax.swing.event.TableModelEvent.ALL_COLUMNS) {
               updateLineTotals();
           }
       });
       checkoutBtn.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               checkout();
           }
       });
       addWindowFocusListener(new java.awt.event.WindowAdapter() {
           @Override
           public void windowGainedFocus(java.awt.event.WindowEvent e) {
               refreshPermissionButtons();
           }
       });
       updateSelectedStoreLabel(); //displays the current store
       updateCurrentUserLabel(); //displays the current user
       setVisible(true); //runs last for the main UI to show
   }


    private ImageIcon loadCenterLogoIcon() {
        String[] resourcePaths = {
                "/Images/CenterLogo.png",
                "Images/CenterLogo.png",
                "/CenterLogo.png",
                "CenterLogo.png"
        };

        for (String path : resourcePaths) {
            URL url = getClass().getResource(path);
            if (url != null) {
                return new ImageIcon(url);
            }
        }

        String[] filePaths = {
                "src/main/Images/CenterLogo.png",
                "src/main/resources/Images/CenterLogo.png",
                "src/Images/CenterLogo.png",
                "Images/CenterLogo.png",
                "CenterLogo.png"
        };

        for (String path : filePaths) {
            ImageIcon icon = new ImageIcon(path);
            if (icon.getIconWidth() > 0) {
                return icon;
            }
        }

        System.out.println("Center logo not found. Checked classpath and common file locations.");
        return null;
    }

    private void refreshPermissionButtons() {
        if (newItemBtn != null) {
            newItemBtn.setEnabled(PermissionManager.hasPermission("NEW_ITEM"));
        }
        if (editItemBtn != null) {
            editItemBtn.setEnabled(PermissionManager.hasPermission("EDIT_ITEM"));
        }
    }

    private void configureCartTableColumns() {
        if (cartTable == null || cartTable.getColumnModel().getColumnCount() < 7) {
            return;
        }

        TableColumnModel columnModel = cartTable.getColumnModel();

        int idWidth = fitColumnWidth(cartTable, 0, 45);
        int nameWidth = fitColumnWidth(cartTable, 1, 120);
        int skuWidth = fitColumnWidth(cartTable, 3, 100);
        int priceWidth = fitColumnWidth(cartTable, 4, 75);
        int qtyWidth = fitColumnWidth(cartTable, 5, 55);
        int lineTotalWidth = fitColumnWidth(cartTable, 6, 95);

        columnModel.getColumn(0).setMinWidth(40);
        columnModel.getColumn(0).setMaxWidth(70);
        columnModel.getColumn(0).setPreferredWidth(idWidth);

        columnModel.getColumn(1).setMinWidth(90);
        columnModel.getColumn(1).setMaxWidth(200);
        columnModel.getColumn(1).setPreferredWidth(nameWidth);

        columnModel.getColumn(2).setMinWidth(220);
        columnModel.getColumn(2).setPreferredWidth(320);
        columnModel.getColumn(2).setCellRenderer(new MultiLineTableCellRenderer());

        columnModel.getColumn(3).setMinWidth(90);
        columnModel.getColumn(3).setPreferredWidth(skuWidth);

        columnModel.getColumn(4).setMinWidth(70);
        columnModel.getColumn(4).setMaxWidth(95);
        columnModel.getColumn(4).setPreferredWidth(priceWidth);

        columnModel.getColumn(5).setMinWidth(50);
        columnModel.getColumn(5).setMaxWidth(70);
        columnModel.getColumn(5).setPreferredWidth(qtyWidth);

        columnModel.getColumn(6).setMinWidth(90);
        columnModel.getColumn(6).setMaxWidth(120);
        columnModel.getColumn(6).setPreferredWidth(lineTotalWidth);

        updateDescriptionRowHeights();
    }

    private int fitColumnWidth(JTable table, int columnIndex, int minWidth) {
        int width = minWidth;
        TableColumnModel columnModel = table.getColumnModel();

        TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();
        Component headerComponent = headerRenderer.getTableCellRendererComponent(
                table,
                columnModel.getColumn(columnIndex).getHeaderValue(),
                false,
                false,
                0,
                columnIndex
        );
        width = Math.max(width, headerComponent.getPreferredSize().width + 16);

        for (int row = 0; row < table.getRowCount(); row++) {
            TableCellRenderer renderer = table.getCellRenderer(row, columnIndex);
            Component component = table.prepareRenderer(renderer, row, columnIndex);
            width = Math.max(width, component.getPreferredSize().width + 16);
        }

        return width;
    }

    private void updateDescriptionRowHeights() {
        if (cartTable == null || cartTable.getRowCount() == 0) {
            return;
        }

        for (int row = 0; row < cartTable.getRowCount(); row++) {
            int rowHeight = 24;
            Object value = cartTable.getValueAt(row, 2);
            String text = value == null ? "" : value.toString();

            TableCellRenderer renderer = cartTable.getCellRenderer(row, 2);
            Component component = renderer.getTableCellRendererComponent(cartTable, text, false, false, row, 2);

            if (component instanceof JTextArea textArea) {
                int columnWidth = cartTable.getColumnModel().getColumn(2).getWidth();
                textArea.setSize(columnWidth, Short.MAX_VALUE);
                rowHeight = Math.max(rowHeight, textArea.getPreferredSize().height + 4);
            }

            cartTable.setRowHeight(row, rowHeight);
        }
    }

    private static class MultiLineTableCellRenderer extends JTextArea implements TableCellRenderer {
        public MultiLineTableCellRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value == null ? "" : value.toString());
            setFont(table.getFont());

            if (isSelected) {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }

            setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
            return this;
        }
    }

    private void searchProducts() {
        searchProducts(true);
    }

    private void searchProducts(boolean showMessages) {
        String searchText = searchField.getText().trim();

        if (Login.currentLocationId == null) {
            JOptionPane.showMessageDialog(this, "No store is selected for this session.");
            return;
        }

        if (searchText.isEmpty()) {
            closeSearchPopup();
            if (showMessages) {
                JOptionPane.showMessageDialog(this, "Type a product name or SKU first.");
            }
            return;
        }

        String sql = """
            SELECT p.product_id, p.name, p.description, p.sku, p.price,
                   COALESCE(i.quantity_on_hand, 0) AS quantity_on_hand
            FROM products p
            LEFT JOIN inventory i
                ON p.product_id = i.product_id
               AND i.location_id = ?
            WHERE p.name ILIKE ? OR p.sku ILIKE ?
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
                        rs.getString("description"),
                        rs.getString("sku"),
                        rs.getDouble("price"),
                        rs.getInt("quantity_on_hand")
                });
            }

            if (rows.isEmpty()) {
                closeSearchPopup();
                if (showMessages) {
                    JOptionPane.showMessageDialog(this, "No matching products found.");
                }
                return;
            }

            showSearchResultsPopup(rows);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }

    private void showSearchResultsPopup(java.util.List<Object[]> rows) {
        if (searchPopup == null) {
            searchPopup = new JPopupMenu();
            searchPopup.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            searchPopup.setFocusable(false);

            String[] columns = {"ID", "Name", "Description", "SKU", "Price", "Stock"};
            DefaultTableModel resultsModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            searchResultsTable = new JTable(resultsModel);
            searchResultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            searchResultsTable.setAutoCreateRowSorter(true);
            searchResultsTable.setRowHeight(24);
            JTableHeader header = searchResultsTable.getTableHeader();
            header.setReorderingAllowed(false);
            header.setPreferredSize(new Dimension(0, 0));
            header.setMinimumSize(new Dimension(0, 0));
            header.setMaximumSize(new Dimension(0, 0));
            header.setVisible(false);
            searchResultsTable.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        addSelectedSearchResultToCart();
                    }
                }
            });

            searchResultsScrollPane = new JScrollPane(searchResultsTable);
            searchResultsScrollPane.setBorder(BorderFactory.createEmptyBorder());
            searchResultsScrollPane.setColumnHeaderView(null);
            searchResultsScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

            searchPopup.setLayout(new BorderLayout());
            searchPopup.add(searchResultsScrollPane, BorderLayout.CENTER);
        }

        DefaultTableModel model = (DefaultTableModel) searchResultsTable.getModel();
        model.setRowCount(0);
        for (Object[] row : rows) {
            model.addRow(row);
        }

        if (searchResultsTable.getRowCount() > 0) {
            searchResultsTable.setRowSelectionInterval(0, 0);
        }

        searchResultsScrollPane.setPreferredSize(new Dimension(Math.max(searchField.getWidth(), 500), 220));

        searchResultsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        searchResultsTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        searchResultsTable.getColumnModel().getColumn(2).setPreferredWidth(220);
        searchResultsTable.getColumnModel().getColumn(3).setPreferredWidth(110);
        searchResultsTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        searchResultsTable.getColumnModel().getColumn(5).setPreferredWidth(70);

        if (searchPopup.isVisible()) {
            searchPopup.setVisible(false);
        }

        searchPopup.show(searchField, 0, searchField.getHeight());
        searchField.requestFocusInWindow();
    }

    private void addSelectedSearchResultToCart() {
        if (searchResultsTable == null || searchResultsTable.getSelectedRow() == -1) {
            if (searchPopup != null && searchPopup.isVisible()) {
                JOptionPane.showMessageDialog(this, "Please select a product.");
            }
            return;
        }

        int selectedRow = searchResultsTable.convertRowIndexToModel(searchResultsTable.getSelectedRow());

        int productId = ((Number) searchResultsTable.getModel().getValueAt(selectedRow, 0)).intValue();
        String name = String.valueOf(searchResultsTable.getModel().getValueAt(selectedRow, 1));
        String description = String.valueOf(searchResultsTable.getModel().getValueAt(selectedRow, 2));
        String sku = String.valueOf(searchResultsTable.getModel().getValueAt(selectedRow, 3));
        double price = ((Number) searchResultsTable.getModel().getValueAt(selectedRow, 4)).doubleValue();

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

        addToCart(productId, name, description, sku, price, qty);
        closeSearchPopup();
        searchField.requestFocusInWindow();
        searchField.selectAll();
    }

    private void closeSearchPopup() {
        if (searchPopup != null) {
            searchPopup.setVisible(false);
        }
    }
    private void addToCart(int productId, String name, String description, String sku, double price, int qty) {
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            int existingProductId = Integer.parseInt(cartModel.getValueAt(i, 0).toString());

            if (existingProductId == productId) {
                int existingQty = Integer.parseInt(cartModel.getValueAt(i, 5).toString());
                int newQty = existingQty + qty;
                double newLineTotal = price * newQty;

                cartModel.setValueAt(newQty, i, 5);
                cartModel.setValueAt(newLineTotal, i, 6);
                updateOverallTotal();
                configureCartTableColumns();
                return;
            }
        }

        double lineTotal = price * qty;
        cartModel.addRow(new Object[]{productId, name, description, sku, price, qty, lineTotal});
        updateLineTotals();
        configureCartTableColumns();
    }
    private void updateLineTotals() {
        updatingCart = true;
        try {
            for (int i = 0; i < cartModel.getRowCount(); i++) {
                Object priceValue = cartModel.getValueAt(i, 4);
                Object qtyValue = cartModel.getValueAt(i, 5);

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

                cartModel.setValueAt(price, i, 4);
                cartModel.setValueAt(qty, i, 5);
                cartModel.setValueAt(price * qty, i, 6);
            }
            updateOverallTotal();
            updateDescriptionRowHeights();
            configureCartTableColumns();
        } finally {
            updatingCart = false;
        }
    }

    private double getOverallTotal() {
        double total = 0.0;

        for (int i = 0; i < cartModel.getRowCount(); i++) {
            Object lineTotalValue = cartModel.getValueAt(i, 6);
            try {
                total += Double.parseDouble(lineTotalValue.toString());
            } catch (NumberFormatException ex) {
                // ignore invalid values
            }
        }

        return total;
    }

    private void updateCurrentDateLabel() {
        if (currentDateLabel == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        lastShownDate = now.format(formatter);
        currentDateLabel.setText("Date: " + lastShownDate);
    }
    private void updateCurrentTimeLabel() {
        if (currentTimeLabel == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        currentTimeLabel.setText("Time: " + now.format(formatter));
    }

    private void startDateRefreshTimer() {
        javax.swing.Timer dateTimer = new javax.swing.Timer(1000, e -> {
            updateCurrentTimeLabel();
            String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            if (!today.equals(lastShownDate)) {
                updateCurrentDateLabel();
            }
        });
        dateTimer.setInitialDelay(0);
        dateTimer.start();
    }
    private void updateCurrentUserLabel() {
       if(currentUserLabel == null){
           return;
       }
       if(Login.currentUserId == null || Login.currentUsername == null){
           currentUserLabel.setText("No User currently loged in");
       }
       else{
           currentUserLabel.setText("Current Cashier: " + Login.currentUsername);
       }
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
            if (Login.currentUserId == null) {
                conn.setAutoCommit(true);
                JOptionPane.showMessageDialog(this, "No cashier is logged in for this session.");
                return;
            }

            int locationId = Login.currentLocationId;

            try {
                String insertSaleSql = "INSERT INTO sales (location_id, user_id, total_amount, status, payment_method) VALUES (?, ?, ?, ?, ?)";
                int saleId;

                try (PreparedStatement saleStmt = conn.prepareStatement(insertSaleSql, Statement.RETURN_GENERATED_KEYS)) {
                    saleStmt.setInt(1, locationId);
                    saleStmt.setInt(2, Login.currentUserId);
                    saleStmt.setBigDecimal(3, BigDecimal.valueOf(getOverallTotal()));
                    saleStmt.setString(4, "COMPLETED");
                    saleStmt.setString(5, paymentMethod);
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
                String ensureInventorySql = "INSERT INTO inventory (product_id, location_id, quantity_on_hand, reorder_level) VALUES (?, ?, 0, 0) ON CONFLICT (product_id, location_id) DO NOTHING";
                String updateInventorySql = "UPDATE inventory SET quantity_on_hand = quantity_on_hand - ? WHERE product_id = ? AND location_id = ?";

                try (PreparedStatement itemStmt = conn.prepareStatement(insertItemSql);
                     PreparedStatement movementStmt = conn.prepareStatement(insertMovementSql);
                     PreparedStatement ensureInventoryStmt = conn.prepareStatement(ensureInventorySql);
                     PreparedStatement updateInventoryStmt = conn.prepareStatement(updateInventorySql)) {

                    for (int i = 0; i < cartModel.getRowCount(); i++) {
                        int productId = Integer.parseInt(cartModel.getValueAt(i, 0).toString());
                        int qty = Integer.parseInt(cartModel.getValueAt(i, 5).toString());
                        double price = Double.parseDouble(cartModel.getValueAt(i, 4).toString());

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
                    updateInventoryStmt.executeBatch();
                    movementStmt.executeBatch();
                }

                conn.commit();
                JOptionPane.showMessageDialog(this, "Sale completed successfully. Sale ID: " + saleId);
                cartModel.setRowCount(0);
                configureCartTableColumns();
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
            Object lineTotalValue = cartModel.getValueAt(i, 6);

            try {
                total += Double.parseDouble(lineTotalValue.toString());
            } catch (NumberFormatException ex) {
                // ignore invalid values
            }
        }

        totalLabel.setText(String.format("Overall Total: $%.2f", total));
    }

}


