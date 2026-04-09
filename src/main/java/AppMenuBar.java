import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AppMenuBar {

    public static JMenuBar create(JFrame parent, String currentScreen) {
        JMenuBar menuBar = new JMenuBar();

        JMenu navigateMenu = new JMenu("Navigate");

        JMenuItem makeSaleItem = new JMenuItem("Make a Sale");
        JMenuItem newItemItem = new JMenuItem("New Item");
        JMenuItem editItemItem = new JMenuItem("Edit Item");
        JMenuItem employeeMgmtItem = new JMenuItem("Employee Management");

        if ("MakeASale".equals(currentScreen)) {
            makeSaleItem.setEnabled(false);
        }
        if ("NewItem".equals(currentScreen)) {
            newItemItem.setEnabled(false);
        }
        if ("EditItem".equals(currentScreen)) {
            editItemItem.setEnabled(false);
        }
        if ("EmployeeManagement".equals(currentScreen)) {
            employeeMgmtItem.setEnabled(false);
        }

        makeSaleItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new MakeASale().setVisible(true);
                parent.dispose();
            }
        });

        newItemItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                NewItem screen = new NewItem();
                screen.setLocationRelativeTo(parent);
                screen.setVisible(true);
            }
        });

        editItemItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                EditItem screen = new EditItem();
                screen.setLocationRelativeTo(parent);
                screen.setVisible(true);
            }
        });

        employeeMgmtItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                EmployeeManagement screen = new EmployeeManagement();
                screen.setLocationRelativeTo(parent); // open centered on top
                screen.setVisible(true);
            }
        });

        navigateMenu.add(makeSaleItem);
        navigateMenu.add(newItemItem);
        navigateMenu.add(editItemItem);
        navigateMenu.add(employeeMgmtItem);

        JMenu sessionMenu = new JMenu("Session");
        JMenuItem closeItem = new JMenuItem("Close");
        JMenuItem logoutItem = new JMenuItem("Logout");

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



        sessionMenu.add(closeItem);
        sessionMenu.add(logoutItem);

        menuBar.add(navigateMenu);
        menuBar.add(sessionMenu);

        return menuBar;
    }
}