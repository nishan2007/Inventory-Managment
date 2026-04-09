import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Roles_Permission extends JFrame {

    private JList<RoleItem> roleList;
    private DefaultListModel<RoleItem> roleListModel;

    private Map<String, JCheckBox> permissionCheckboxes = new LinkedHashMap<>();

    private JButton saveButton;
    private JButton addRoleButton;

    public Roles_Permission() {
        setTitle("Role & Permission Management");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout());

        // LEFT: ROLE LIST
        roleListModel = new DefaultListModel<>();
        roleList = new JList<>(roleListModel);
        JScrollPane roleScroll = new JScrollPane(roleList);
        roleScroll.setPreferredSize(new Dimension(200, 0));

        // RIGHT: PERMISSIONS
        JPanel permissionPanel = new JPanel();
        permissionPanel.setLayout(new BoxLayout(permissionPanel, BoxLayout.Y_AXIS));

        addPermission("MAKE_SALE", "Make Sale");
        addPermission("NEW_ITEM", "Add Item");
        addPermission("EDIT_ITEM", "Edit Item");
        addPermission("EMPLOYEE_MANAGEMENT", "Employee Management");
        addPermission("ROLE_MANAGEMENT", "Roles & Permission");
        addPermission("CHANGE_STORE", "Change Store");
        addPermission("VIEW_REPORTS", "View Reports");

        JScrollPane permScroll = new JScrollPane(permissionPanel);

        for (JCheckBox cb : permissionCheckboxes.values()) {
            permissionPanel.add(cb);
        }

        // TOP BUTTONS
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addRoleButton = new JButton("Add Role");
        topPanel.add(addRoleButton);

        // BOTTOM BUTTON
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Save");
        bottomPanel.add(saveButton);

        add(topPanel, BorderLayout.NORTH);
        add(roleScroll, BorderLayout.WEST);
        add(permScroll, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        loadRoles();

        roleList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadPermissionsForSelectedRole();
            }
        });

        saveButton.addActionListener(e -> savePermissions());

        addRoleButton.addActionListener(e -> addNewRole());

        setVisible(true);
    }

    private void addPermission(String key, String label) {
        JCheckBox cb = new JCheckBox(label);
        permissionCheckboxes.put(key, cb);
    }

    private void loadRoles() {
        roleListModel.clear();

        String sql = "SELECT role_id, role_name FROM roles ORDER BY role_name";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                roleListModel.addElement(new RoleItem(
                        rs.getInt("role_id"),
                        rs.getString("role_name")
                ));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void loadPermissionsForSelectedRole() {
        RoleItem selected = roleList.getSelectedValue();
        if (selected == null) return;

        // reset all
        for (JCheckBox cb : permissionCheckboxes.values()) {
            cb.setSelected(false);
        }

        String sql = """
                SELECT p.permission_key
                FROM role_permissions rp
                JOIN permissions p ON rp.permission_id = p.permission_id
                WHERE rp.role_id = ?
                """;

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, selected.id);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String key = rs.getString("permission_key");
                    if (permissionCheckboxes.containsKey(key)) {
                        permissionCheckboxes.get(key).setSelected(true);
                    }
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void savePermissions() {
        RoleItem selected = roleList.getSelectedValue();
        if (selected == null) return;

        try (Connection conn = DB.getConnection()) {

            conn.setAutoCommit(false);

            // DELETE OLD
            try (PreparedStatement delete = conn.prepareStatement(
                    "DELETE FROM role_permissions WHERE role_id = ?")) {
                delete.setInt(1, selected.id);
                delete.executeUpdate();
            }

            // INSERT NEW
            String insertSql = """
                    INSERT INTO role_permissions (role_id, permission_id)
                    SELECT ?, permission_id FROM permissions WHERE permission_key = ?
                    """;

            try (PreparedStatement insert = conn.prepareStatement(insertSql)) {

                for (Map.Entry<String, JCheckBox> entry : permissionCheckboxes.entrySet()) {
                    if (entry.getValue().isSelected()) {
                        insert.setInt(1, selected.id);
                        insert.setString(2, entry.getKey());
                        insert.addBatch();
                    }
                }

                insert.executeBatch();
            }

            conn.commit();

            if (selected.name != null && selected.name.equalsIgnoreCase(PermissionManager.getCurrentRole())) {
                PermissionManager.refreshOpenWindows();
            }

            JOptionPane.showMessageDialog(this, "Permissions updated.");

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void addNewRole() {
        String name = JOptionPane.showInputDialog(this, "Enter new role name:");
        if (name == null || name.isBlank()) return;

        String sql = "INSERT INTO roles (role_name, description) VALUES (?, ?)";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name.toUpperCase());
            ps.setString(2, "Custom role");
            ps.executeUpdate();

            loadRoles();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private static class RoleItem {
        int id;
        String name;

        RoleItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
