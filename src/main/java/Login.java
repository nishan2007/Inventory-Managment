import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Login extends JFrame {

    public static Integer currentUserId;
    public static String currentUsername;
    public static String currentRole;
    public static Integer currentLocationId;
    public static String currentLocationName;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton clearButton;

    public Login() {
        setTitle("SmartStock Login");
        setSize(420, 260);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("SmartStock Login", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        usernameField = new JTextField();
        passwordField = new JPasswordField();

        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        loginButton = new JButton("Login");
        clearButton = new JButton("Clear");
        buttonPanel.add(clearButton);
        buttonPanel.add(loginButton);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loginUser();
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFields();
            }
        });

        getRootPane().setDefaultButton(loginButton);
        setVisible(true);
    }

    private void loginUser() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter username and password.");
            return;
        }

        String userSql = """
                SELECT u.user_id,
                       u.username,
                       COALESCE(r.role_name, 'USER') AS role
                FROM users u
                LEFT JOIN roles r ON u.role_id = r.role_id
                WHERE u.username = ?
                  AND u.password_hash = ?
                """;
        String storesSql = """
                SELECT l.location_id, l.name
                FROM user_locations ul
                JOIN locations l ON ul.location_id = l.location_id
                WHERE ul.user_id = ?
                ORDER BY l.name
                """;

        try (Connection conn = DB.getConnection();
             PreparedStatement userPs = conn.prepareStatement(userSql)) {

            userPs.setString(1, username);
            userPs.setString(2, password);

            try (ResultSet userRs = userPs.executeQuery()) {
                if (!userRs.next()) {
                    JOptionPane.showMessageDialog(this, "Invalid username or password.");
                    return;
                }

                int userId = userRs.getInt("user_id");
                String foundUsername = userRs.getString("username");
                String role = userRs.getString("role");

                List<LocationOption> locations = new ArrayList<>();

                try (PreparedStatement storesPs = conn.prepareStatement(storesSql)) {
                    storesPs.setInt(1, userId);

                    try (ResultSet storesRs = storesPs.executeQuery()) {
                        while (storesRs.next()) {
                            locations.add(new LocationOption(
                                    storesRs.getInt("location_id"),
                                    storesRs.getString("name")
                            ));
                        }
                    }
                }

                if (locations.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "This user has no assigned stores.");
                    return;
                }

                LocationOption selectedLocation;
                if (locations.size() == 1) {
                    selectedLocation = locations.get(0);
                } else {
                    selectedLocation = (LocationOption) JOptionPane.showInputDialog(
                            this,
                            "Select store:",
                            "Store Selection",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            locations.toArray(),
                            locations.get(0)
                    );

                    if (selectedLocation == null) {
                        return;
                    }
                }

                currentUserId = userId;
                currentUsername = foundUsername;
                currentRole = role;
                currentLocationId = selectedLocation.locationId;
                currentLocationName = selectedLocation.locationName;

                JOptionPane.showMessageDialog(
                        this,
                        "Login successful.\nUser: " + currentUsername +
                                "\nRole: " + currentRole +
                                "\nStore: " + currentLocationName
                );

                dispose();
                // Open the main application
                new MainMenu().setVisible(true);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Login failed: " + ex.getMessage());
        }
    }

    private void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        usernameField.requestFocusInWindow();
    }

    private static class LocationOption {
        private final int locationId;
        private final String locationName;

        private LocationOption(int locationId, String locationName) {
            this.locationId = locationId;
            this.locationName = locationName;
        }

        @Override
        public String toString() {
            return locationName + " (ID: " + locationId + ")";
        }
    }
}