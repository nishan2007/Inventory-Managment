import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;


public class MainMenu extends JFrame {

    private final JButton makeSaleButton;
    private final JButton viewSalesButton;
    private final JButton addItemButton;
    private final JButton editItemsButton;
    private final JButton employeeManagementButton;
    private final JButton rolesPermissionsButton;
    private final JButton logoutButton;

    public MainMenu() {
        setTitle("SmartStock - Main Menu");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        setJMenuBar(AppMenuBar.create(this, "MainMenu"));

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        mainPanel.setBackground(new Color(245, 247, 250));

        JLabel titleLabel = new JLabel("SmartStock Main Menu");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel subtitleLabel = new JLabel("Choose a section to continue");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        subtitleLabel.setForeground(new Color(90, 90, 90));

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(8));
        headerPanel.add(subtitleLabel);

        JPanel gridPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        gridPanel.setOpaque(false);

        makeSaleButton = createMenuButton("Make a Sale", "Create a new sale transaction", loadIcon("src/ICONS/MakeASale.png"));
        viewSalesButton = createMenuButton("View Sales", "Review previous transactions", loadIcon("src/ICONS/ViewSales.png"));
        addItemButton = createMenuButton("Add Item", "Add a new product to inventory", loadIcon("src/ICONS/NewItem.png"));
        editItemsButton = createMenuButton("Edit Items", "Update product information", loadIcon("src/ICONS/EditItem.png"));
        employeeManagementButton = createMenuButton("Employees", "Manage employee accounts", loadIcon("src/ICONS/Employee.png"));
        rolesPermissionsButton = createMenuButton("Roles & Permissions", "Configure user access", loadIcon("src/ICONS/Security.png"));
        applyPermissions();

        gridPanel.add(makeSaleButton);
        gridPanel.add(viewSalesButton);
        gridPanel.add(addItemButton);
        gridPanel.add(editItemsButton);
        gridPanel.add(employeeManagementButton);
        gridPanel.add(rolesPermissionsButton);

        logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        logoutButton.setFocusPainted(false);
        logoutButton.setPreferredSize(new Dimension(130, 42));

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setOpaque(false);
        footerPanel.add(logoutButton);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(gridPanel, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
        wireActions();
    }
    private ImageIcon loadIcon(String path) {
        ImageIcon icon = new ImageIcon(path);
        Image img = icon.getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    private void applyPermissions() {
        boolean canMakeSale = PermissionManager.hasPermission("MAKE_SALE");
        boolean canViewSales = PermissionManager.hasPermission("MAKE_SALE");
        boolean canAddItem = PermissionManager.hasPermission("NEW_ITEM");
        boolean canEditItem = PermissionManager.hasPermission("EDIT_ITEM");
        boolean canEmployeeManagement = PermissionManager.hasPermission("EMPLOYEE_MANAGEMENT");
        boolean canRolesPermissions = PermissionManager.hasPermission("ROLE_MANAGEMENT");

        makeSaleButton.setEnabled(canMakeSale);
        viewSalesButton.setEnabled(canViewSales);
        addItemButton.setEnabled(canAddItem);
        editItemsButton.setEnabled(canEditItem);
        employeeManagementButton.setEnabled(canEmployeeManagement);
        rolesPermissionsButton.setEnabled(canRolesPermissions);
    }

    private void wireActions() {
        makeSaleButton.addActionListener(e -> {
            if (!PermissionManager.requirePermission("MAKE_SALE", this, "Make a Sale")) {
                return;
            }
            openScreen(new MakeASale());
        });
        viewSalesButton.addActionListener(e -> {
            if (!PermissionManager.requirePermission("MAKE_SALE", this, "View Sales")) {
                return;
            }
            openScreen(new ViewSales());
        });
        addItemButton.addActionListener(e -> {
            if (!PermissionManager.requirePermission("NEW_ITEM", this, "Add Item")) {
                return;
            }
            openScreen(new NewItem());
        });
        editItemsButton.addActionListener(e -> {
            if (!PermissionManager.requirePermission("EDIT_ITEM", this, "Edit Items")) {
                return;
            }
            openScreen(new EditItem());
        });
        employeeManagementButton.addActionListener(e -> {
            if (!PermissionManager.requirePermission("EMPLOYEE_MANAGEMENT", this, "Employee Management")) {
                return;
            }
            openScreen(new EmployeeManagement());
        });
        rolesPermissionsButton.addActionListener(e -> {
            if (!PermissionManager.requirePermission("ROLE_MANAGEMENT", this, "Roles & Permissions")) {
                return;
            }
            openScreen(new Roles_Permission());
        });

        logoutButton.addActionListener(e -> {
            dispose();
            new Login().setVisible(true);
        });
    }

    private void openScreen(JFrame screen) {
        screen.setVisible(true);
        dispose();
    }

    private JButton createMenuButton(String title, String description, Icon icon) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout(10, 10));
        button.setFocusPainted(false);
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setPreferredSize(new Dimension(64, 64));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

        JLabel descriptionLabel = new JLabel("<html><div style='width:180px; color:#666666;'>" + description + "</div></html>");
        descriptionLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(8));
        textPanel.add(descriptionLabel);

        button.add(iconLabel, BorderLayout.NORTH);
        button.add(textPanel, BorderLayout.CENTER);

        return button;
    }

    public JButton getMakeSaleButton() {
        return makeSaleButton;
    }

    public JButton getViewSalesButton() {
        return viewSalesButton;
    }

    public JButton getAddItemButton() {
        return addItemButton;
    }

    public JButton getEditItemsButton() {
        return editItemsButton;
    }

    public JButton getEmployeeManagementButton() {
        return employeeManagementButton;
    }

    public JButton getRolesPermissionsButton() {
        return rolesPermissionsButton;
    }

    public JButton getLogoutButton() {
        return logoutButton;
    }
}
