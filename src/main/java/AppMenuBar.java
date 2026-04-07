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

        if ("MakeASale".equals(currentScreen)) {
            makeSaleItem.setEnabled(false);
        }
        if ("NewItem".equals(currentScreen)) {
            newItemItem.setEnabled(false);
        }
        if ("EditItem".equals(currentScreen)) {
            newItemItem.setEnabled(false);
        }

        makeSaleItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new MakeASale().setVisible(true);
                parent.dispose();
            }
        });

        newItemItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new NewItem().setVisible(true);
                parent.dispose();
            }
        });

        editItemItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new EditItem().setVisible(true);
                parent.dispose();
            }
        });

        navigateMenu.add(makeSaleItem);
        navigateMenu.add(newItemItem);
        navigateMenu.add(editItemItem);

        JMenu sessionMenu = new JMenu("Session");
        JMenuItem closeItem = new JMenuItem("Close");

        closeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                parent.dispose();
            }
        });

        sessionMenu.add(closeItem);

        menuBar.add(navigateMenu);
        menuBar.add(sessionMenu);

        return menuBar;
    }
}