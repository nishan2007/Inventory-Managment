import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EmployeeManagement extends JFrame {

    private JTable employeeTable;
    private DefaultTableModel employeeModel;

    private JTextField usernameField;
    private JTextField passwordField;
    private JTextField fullNameField;
    private JComboBox<String> roleBox;
    private JCheckBox activeCheckBox;

    private JButton addButton;
    private JButton updateButton;
    private JButton clearButton;
    private JButton refreshButton;
    private JButton assignStoresButton;

    private Integer selectedUserId = null;

    public EmployeeManagement() {
        setTitle("Employee Management");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        employeeModel = new DefaultTableModel(
                new Object[]{"User ID", "Username", "Full Name", "Role", "Active"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        employeeTable = new JTable(employeeModel);
        employeeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScrollPane = new JScrollPane(employeeTable);

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));

        usernameField = new JTextField();
        passwordField = new JTextField();
        fullNameField = new JTextField();
        roleBox = new JComboBox<>(new String[]{"ADMIN", "MANAGER", "CASHIER"});
        activeCheckBox = new JCheckBox("Active", true);
        activeCheckBox.setEnabled(false);

        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);

        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);

        formPanel.add(new JLabel("Full Name:"));
        formPanel.add(fullNameField);

        formPanel.add(new JLabel("Role:"));
        formPanel.add(roleBox);

        formPanel.add(new JLabel("Status:"));
        formPanel.add(activeCheckBox);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addButton = new JButton("Add Employee");
        updateButton = new JButton("Update Employee");
        clearButton = new JButton("Clear");
        refreshButton = new JButton("Refresh");
        assignStoresButton = new JButton("Assign Stores");

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(assignStoresButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(refreshButton);

        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.add(formPanel, BorderLayout.CENTER);
        rightPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);

        add(mainPanel);

        //Action Listeners
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addEmployee();
            }
        });

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateEmployee();
            }
        });

        assignStoresButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openAssignStoresDialog();
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFields();
            }
        });

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadEmployees();
            }
        });

        employeeTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedEmployee();
            }
        });

        assignStoresButton.setEnabled(false);
        loadEmployees();
        setVisible(true);
    }

    private void loadEmployees() {
        employeeModel.setRowCount(0);

        String sql = """
                SELECT u.user_id,
                       u.username,
                       u.full_name,
                       COALESCE(r.role_name, 'USER') AS role,
                       TRUE AS is_active
                FROM users u
                LEFT JOIN roles r ON u.role_id = r.role_id
                ORDER BY u.user_id
                """;

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                employeeModel.addRow(new Object[]{
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("full_name"),
                        rs.getString("role"),
                        rs.getBoolean("is_active")
                });
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load employees: " + ex.getMessage());
        }
    }

    private void loadSelectedEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        selectedUserId = Integer.parseInt(employeeModel.getValueAt(selectedRow, 0).toString());
        usernameField.setText(employeeModel.getValueAt(selectedRow, 1).toString());
        fullNameField.setText(employeeModel.getValueAt(selectedRow, 2) == null ? "" : employeeModel.getValueAt(selectedRow, 2).toString());
        roleBox.setSelectedItem(employeeModel.getValueAt(selectedRow, 3).toString());
        activeCheckBox.setSelected(true);
        passwordField.setText("");
        assignStoresButton.setEnabled(true);
    }

    private void addEmployee() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String role = roleBox.getSelectedItem().toString();
        boolean isActive = activeCheckBox.isSelected();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password are required.");
            return;
        }

        String sql = """
                INSERT INTO users (username, password_hash, full_name, role_id)
                VALUES (?, ?, ?, (SELECT role_id FROM roles WHERE role_name = ?))
                """;

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, fullName.isEmpty() ? null : fullName);
            ps.setString(4, role);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Employee added successfully.");
            clearFields();
            loadEmployees();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to add employee: " + ex.getMessage());
        }
    }

    private void updateEmployee() {
        if (selectedUserId == null) {
            JOptionPane.showMessageDialog(this, "Select an employee first.");
            return;
        }

        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String role = roleBox.getSelectedItem().toString();
        boolean isActive = activeCheckBox.isSelected();

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username is required.");
            return;
        }

        String sql;
        boolean updatePassword = !password.isEmpty();

        if (updatePassword) {
            sql = """
                    UPDATE users
                    SET username = ?,
                        password_hash = ?,
                        full_name = ?,
                        role_id = (SELECT role_id FROM roles WHERE role_name = ?)
                    WHERE user_id = ?
                    """;
        } else {
            sql = """
                    UPDATE users
                    SET username = ?,
                        full_name = ?,
                        role_id = (SELECT role_id FROM roles WHERE role_name = ?)
                    WHERE user_id = ?
                    """;
        }

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            if (updatePassword) {
                ps.setString(2, password);
                ps.setString(3, fullName.isEmpty() ? null : fullName);
                ps.setString(4, role);
                ps.setInt(5, selectedUserId);
            } else {
                ps.setString(2, fullName.isEmpty() ? null : fullName);
                ps.setString(3, role);
                ps.setInt(4, selectedUserId);
            }

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Employee updated successfully.");
            clearFields();
            loadEmployees();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to update employee: " + ex.getMessage());
        }
    }

    private void clearFields() {
        selectedUserId = null;
        usernameField.setText("");
        passwordField.setText("");
        fullNameField.setText("");
        roleBox.setSelectedIndex(0);
        activeCheckBox.setSelected(true);
        employeeTable.clearSelection();
        assignStoresButton.setEnabled(false);
        usernameField.requestFocusInWindow();
    }

    private void openAssignStoresDialog() {
        if (selectedUserId == null) {
            JOptionPane.showMessageDialog(this, "Select an employee first.");
            return;
        }

        JDialog dialog = new JDialog(this, "Assign Stores", true);
        dialog.setSize(700, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        JTextField searchField = new JTextField();
        topPanel.add(new JLabel("Search Store:"), BorderLayout.WEST);
        topPanel.add(searchField, BorderLayout.CENTER);

        DefaultTableModel storeModel = new DefaultTableModel(
                new Object[]{"Assigned", "Location ID", "Store Name", "Address"}, 0
        ) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Boolean.class;
                }
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };

        JTable storeTable = new JTable(storeModel);
        storeTable.setRowHeight(24);
        JScrollPane scrollPane = new JScrollPane(storeTable);

        TableColumn assignedColumn = storeTable.getColumnModel().getColumn(0);
        assignedColumn.setPreferredWidth(80);
        assignedColumn.setMaxWidth(100);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save Assignments");
        JButton closeButton = new JButton("Close");
        bottomPanel.add(saveButton);
        bottomPanel.add(closeButton);

        dialog.add(topPanel, BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        Runnable loadStores = () -> {
            storeModel.setRowCount(0);
            String searchText = searchField.getText().trim();

            String sql = """
                    SELECT l.location_id, l.name, l.address,
                           CASE WHEN ul.user_id IS NULL THEN FALSE ELSE TRUE END AS assigned
                    FROM locations l
                    LEFT JOIN user_locations ul
                        ON l.location_id = ul.location_id AND ul.user_id = ?
                    WHERE l.name LIKE ?
                       OR COALESCE(l.address, '') LIKE ?
                       OR CAST(l.location_id AS TEXT) LIKE ?
                    ORDER BY l.location_id
                    """;

            try (Connection conn = DB.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                String searchLike = "%" + searchText + "%";
                ps.setInt(1, selectedUserId);
                ps.setString(2, searchLike);
                ps.setString(3, searchLike);
                ps.setString(4, searchLike);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        storeModel.addRow(new Object[]{
                                rs.getBoolean("assigned"),
                                String.valueOf(rs.getInt("location_id")),
                                rs.getString("name"),
                                rs.getString("address") == null ? "" : rs.getString("address")
                        });
                    }
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Failed to load stores: " + ex.getMessage());
            }
        };

        searchField.addActionListener(e -> loadStores.run());

        saveButton.addActionListener(e -> {
            String deleteSql = "DELETE FROM user_locations WHERE user_id = ?";
            String insertSql = "INSERT INTO user_locations (user_id, location_id) VALUES (?, ?)";

            try (Connection conn = DB.getConnection()) {
                conn.setAutoCommit(false);

                try (PreparedStatement deletePs = conn.prepareStatement(deleteSql);
                     PreparedStatement insertPs = conn.prepareStatement(insertSql)) {

                    deletePs.setInt(1, selectedUserId);
                    deletePs.executeUpdate();

                    for (int i = 0; i < storeModel.getRowCount(); i++) {
                        Object assignedValue = storeModel.getValueAt(i, 0);
                        boolean assigned = assignedValue instanceof Boolean && (Boolean) assignedValue;

                        if (assigned) {
                            int locationId = Integer.parseInt(storeModel.getValueAt(i, 1).toString());
                            insertPs.setInt(1, selectedUserId);
                            insertPs.setInt(2, locationId);
                            insertPs.addBatch();
                        }
                    }

                    insertPs.executeBatch();
                    conn.commit();
                    JOptionPane.showMessageDialog(dialog, "Store assignments saved.");
                    dialog.dispose();

                } catch (SQLException ex) {
                    conn.rollback();
                    throw ex;
                } finally {
                    conn.setAutoCommit(true);
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Failed to save assignments: " + ex.getMessage());
            }
        });

        closeButton.addActionListener(e -> dialog.dispose());

        loadStores.run();
        dialog.setVisible(true);
    }
}
