import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
public class PermissionManager {

    private static String cachedRole;
    private static Set<String> cachedPermissions;

    public static String getCurrentRole() {
        if (Login.currentRole == null || Login.currentRole.isBlank()) {
            return "USER";
        }
        return Login.currentRole.trim().toUpperCase();
    }

    public static boolean hasPermission(String permissionKey) {
        String role = getCurrentRole();

        if (permissionKey == null || permissionKey.isBlank()) {
            return false;
        }

        ensurePermissionsLoaded(role);

        return cachedPermissions != null && cachedPermissions.contains(permissionKey.trim().toUpperCase());
    }

    private static void ensurePermissionsLoaded(String role) {
        if (role == null || role.isBlank()) {
            cachedRole = null;
            cachedPermissions = new HashSet<>();
            return;
        }

        if (cachedPermissions != null && role.equalsIgnoreCase(cachedRole)) {
            return;
        }

        reloadPermissionsFromDatabase(role);
    }

    private static void reloadPermissionsFromDatabase(String role) {
        Set<String> permissions = new HashSet<>();

        String sql = """
                SELECT UPPER(p.permission_key) AS permission_key
                FROM roles r
                JOIN role_permissions rp ON r.role_id = rp.role_id
                JOIN permissions p ON rp.permission_id = p.permission_id
                WHERE UPPER(r.role_name) = ?
                """;

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, role.trim().toUpperCase());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    permissions.add(rs.getString("permission_key"));
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        cachedRole = role.trim().toUpperCase();
        cachedPermissions = permissions;
    }


    public static boolean requirePermission(String permissionKey, java.awt.Component parent, String featureName) {
        if (hasPermission(permissionKey)) {
            return true;
        }

        JOptionPane.showMessageDialog(
                parent,
                "You do not have permission to access " + featureName + ".",
                "Access Denied",
                JOptionPane.WARNING_MESSAGE
        );
        return false;
    }

    public static boolean canAccessScreen(String screenName) {
        return switch (screenName) {
            case "MakeASale" -> hasPermission("MAKE_SALE");
            case "NewItem" -> hasPermission("NEW_ITEM");
            case "EditItem" -> hasPermission("EDIT_ITEM");
            case "EmployeeManagement" -> hasPermission("EMPLOYEE_MANAGEMENT");
            case "Roles_Permission" -> hasPermission("ROLE_MANAGEMENT");
            default -> false;
        };
    }
    public static void refreshOpenWindows() {
        SwingUtilities.invokeLater(() -> {
            reloadPermissionsFromDatabase(getCurrentRole());
            for (Window window : Window.getWindows()) {
                if (!(window instanceof JFrame frame)) {
                    continue;
                }

                if (!frame.isDisplayable()) {
                    continue;
                }

                String screenName = frame.getClass().getSimpleName();

                if ("Login".equals(screenName)) {
                    continue;
                }

                if ("Roles_Permission".equals(screenName)) {
                    try {
                        frame.setJMenuBar(AppMenuBar.create(frame, screenName));
                        frame.revalidate();
                        frame.repaint();
                    } catch (Exception ignored) {
                    }
                    continue;
                }

                if (canAccessScreen(screenName)) {
                    try {
                        frame.setJMenuBar(AppMenuBar.create(frame, screenName));
                        frame.revalidate();
                        frame.repaint();
                    } catch (Exception ignored) {
                    }
                } else {
                    JOptionPane.showMessageDialog(
                            frame,
                            "Your permissions changed and you no longer have access to this screen.",
                            "Permissions Updated",
                            JOptionPane.WARNING_MESSAGE
                    );
                    frame.dispose();
                }
            }
        });
    }
}