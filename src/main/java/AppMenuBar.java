import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AppMenuBar {

    public static JMenuBar create(JFrame parent, String currentScreen) {
        JMenuBar menuBar = new JMenuBar();

        JMenu navigateMenu = new JMenu("Navigate");
        JMenu employeeMenu = new JMenu("Employee");

        JMenuItem makeSaleItem = new JMenuItem("Make a Sale");
        JMenuItem newItemItem = new JMenuItem("New Item");
        JMenuItem editItemItem = new JMenuItem("Edit Item");
        JMenuItem employeeMgmtItem = new JMenuItem("Employee Management");
        JMenuItem rolesPermissionItem = new JMenuItem("Roles & Permission");

        boolean canMakeSale = PermissionManager.hasPermission("MAKE_SALE");
        boolean canNewItem = PermissionManager.hasPermission("NEW_ITEM");
        boolean canEditItem = PermissionManager.hasPermission("EDIT_ITEM");
        boolean canEmployeeMgmt = PermissionManager.hasPermission("EMPLOYEE_MANAGEMENT");
        boolean canRoleManagement = PermissionManager.hasPermission("ROLE_MANAGEMENT");
        boolean canChangeStore = PermissionManager.hasPermission("CHANGE_STORE");

        if (!canMakeSale || "MakeASale".equals(currentScreen)) {
            makeSaleItem.setEnabled(false);
        }
        if (!canNewItem || "NewItem".equals(currentScreen)) {
            newItemItem.setEnabled(false);
        }
        if (!canEditItem || "EditItem".equals(currentScreen)) {
            editItemItem.setEnabled(false);
        }
        if (!canEmployeeMgmt || "EmployeeManagement".equals(currentScreen)) {
            employeeMgmtItem.setEnabled(false);
        }
        if (!canRoleManagement || "Roles_Permission".equals(currentScreen)) {
            rolesPermissionItem.setEnabled(false);
        }

        makeSaleItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!PermissionManager.requirePermission("MAKE_SALE", parent, "Make a Sale")) {
                    return;
                }
                if (WindowHelper.focusIfAlreadyOpen(MakeASale.class)) {
                    parent.dispose();
                    return;
                }
                new MakeASale().setVisible(true);
                parent.dispose();
            }
        });

        newItemItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!PermissionManager.requirePermission("NEW_ITEM", parent, "New Item")) {
                    return;
                }
                if (WindowHelper.focusIfAlreadyOpen(NewItem.class)) {
                    return;
                }
                NewItem screen = new NewItem();
                screen.setLocationRelativeTo(parent);
                screen.setVisible(true);
            }
        });

        editItemItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!PermissionManager.requirePermission("EDIT_ITEM", parent, "Edit Item")) {
                    return;
                }
                if (WindowHelper.focusIfAlreadyOpen(EditItem.class)) {
                    return;
                }
                EditItem screen = new EditItem();
                screen.setLocationRelativeTo(parent);
                screen.setVisible(true);
            }
        });

        employeeMgmtItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!PermissionManager.requirePermission("EMPLOYEE_MANAGEMENT", parent, "Employee Management")) {
                    return;
                }
                if (WindowHelper.focusIfAlreadyOpen(EmployeeManagement.class)) {
                    return;
                }
                EmployeeManagement screen = new EmployeeManagement();
                screen.setLocationRelativeTo(parent); // open centered on top
                screen.setVisible(true);
            }
        });

        rolesPermissionItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!PermissionManager.requirePermission("ROLE_MANAGEMENT", parent, "Roles & Permission")) {
                    return;
                }
                if (WindowHelper.focusIfAlreadyOpen(Roles_Permission.class)) {
                    return;
                }
                Roles_Permission screen = new Roles_Permission();
                screen.setLocationRelativeTo(parent);
                screen.setVisible(true);
            }
        });

        navigateMenu.add(makeSaleItem);
        navigateMenu.add(newItemItem);
        navigateMenu.add(editItemItem);

        employeeMenu.add(employeeMgmtItem);
        employeeMenu.add(rolesPermissionItem);

        JMenu sessionMenu = new JMenu("Session");
        JMenuItem changeStoreItem = new JMenuItem("Change Store");
        JMenuItem closeItem = new JMenuItem("Close");
        JMenuItem logoutItem = new JMenuItem("Logout");

        if (!canChangeStore) {
            changeStoreItem.setEnabled(false);
        }

        changeStoreItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showChangeLocationDialog(parent, currentScreen);
            }
        });
        closeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                parent.dispose();
            }
        });

        logoutItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Login login = new Login();
                login.setLocationRelativeTo(parent);
                parent.dispose();
                login.setVisible(true);
            }
        });




        sessionMenu.add(changeStoreItem);
        sessionMenu.addSeparator();
        sessionMenu.add(closeItem);
        sessionMenu.add(logoutItem);


        menuBar.add(navigateMenu);
        menuBar.add(employeeMenu);
        menuBar.add(sessionMenu);

        return menuBar;
    }
    private static void showChangeLocationDialog(JFrame parent, String currentScreen) {
        if (!PermissionManager.hasPermission("CHANGE_STORE")) {
            JOptionPane.showMessageDialog(
                    parent,
                    "You do not have permission to change stores.",
                    "Access Denied",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        List<StoreOption> allowedStores = getAllowedStoresFromSession();

        if (allowedStores.isEmpty()) {
            JOptionPane.showMessageDialog(
                    parent,
                    "No allowed store locations were found for this user.",
                    "Change Store",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        JComboBox<StoreOption> storeCombo = new JComboBox<>(allowedStores.toArray(new StoreOption[0]));
        storeCombo.setSelectedItem(findCurrentStoreOption(allowedStores));

        JPanel panel = new JPanel();
        panel.add(new JLabel("Select store:"));
        panel.add(storeCombo);

        int result = JOptionPane.showConfirmDialog(
                parent,
                panel,
                "Change Store",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            StoreOption selected = (StoreOption) storeCombo.getSelectedItem();
            if (selected != null) {
                boolean updated = setCurrentStoreInSession(selected.id);

                if (updated) {
                    JOptionPane.showMessageDialog(
                            parent,
                            "Current store changed to: " + Login.currentLocationName,
                            "Store Updated",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    refreshCurrentScreen(parent, currentScreen);
                } else {
                    JOptionPane.showMessageDialog(
                            parent,
                            "Could not change the current store.",
                            "Store Update Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }
    }

    private static void refreshCurrentScreen(JFrame parent, String currentScreen) {
        if (currentScreen == null || parent == null) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            switch (currentScreen) {
                case "MakeASale" -> {
                    JFrame refreshed = new MakeASale();
                    refreshed.setLocationRelativeTo(parent);
                    refreshed.setVisible(true);
                    parent.dispose();
                }
                case "NewItem" -> {
                    JFrame refreshed = new NewItem();
                    refreshed.setLocationRelativeTo(parent);
                    refreshed.setVisible(true);
                    parent.dispose();
                }
                case "EditItem" -> {
                    JFrame refreshed = new EditItem();
                    refreshed.setLocationRelativeTo(parent);
                    refreshed.setVisible(true);
                    parent.dispose();
                }
                case "EmployeeManagement" -> {
                    JFrame refreshed = new EmployeeManagement();
                    refreshed.setLocationRelativeTo(parent);
                    refreshed.setVisible(true);
                    parent.dispose();
                }
                case "Roles_Permission" -> {
                    JFrame refreshed = new Roles_Permission();
                    refreshed.setLocationRelativeTo(parent);
                    refreshed.setVisible(true);
                    parent.dispose();
                }
            }
        });
    }

    private static StoreOption findCurrentStoreOption(List<StoreOption> allowedStores) {
        Integer currentId = getCurrentStoreIdFromSession();
        if (currentId == null) {
            return allowedStores.get(0);
        }

        for (StoreOption option : allowedStores) {
            if (option.id == currentId) {
                return option;
            }
        }

        return allowedStores.get(0);
    }

    private static Integer getCurrentStoreIdFromSession() {
        return Login.currentLocationId;
    }

    private static boolean setCurrentStoreInSession(int storeId) {
        List<StoreOption> stores = getAllowedStoresFromSession();

        for (StoreOption store : stores) {
            if (store.id == storeId) {
                Login.currentLocationId = store.id;
                Login.currentLocationName = store.label;
                return true;
            }
        }

        return false;
    }

    private static List<StoreOption> getAllowedStoresFromSession() {
        List<StoreOption> stores = new ArrayList<>();

        if (Login.currentUserId == null) {
            return stores;
        }

        String storesSql = """
                SELECT l.location_id, l.name
                FROM user_locations ul
                JOIN locations l ON ul.location_id = l.location_id
                WHERE ul.user_id = ?
                ORDER BY l.name
                """;

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(storesSql)) {

            ps.setInt(1, Login.currentUserId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    stores.add(new StoreOption(
                            rs.getInt("location_id"),
                            rs.getString("name")
                    ));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    null,
                    "Could not load allowed stores: " + ex.getMessage(),
                    "Store Load Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }

        return stores;
    }


    private static class StoreOption {
        private final int id;
        private final String label;

        private StoreOption(int id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}