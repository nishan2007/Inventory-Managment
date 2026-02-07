import javax.swing.*;
import java.awt.*;
import java.sql.Connection;

public class WelcomeFrame extends JFrame {

    private final JLabel statusLabel = new JLabel("Status: Not checked");
    private final JButton testBtn = new JButton("Test Database Connection");
    private final JButton continueBtn = new JButton("Continue");

    public WelcomeFrame() {
        super("SmartStock");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 260);
        setLocationRelativeTo(null);

        JLabel title = new JLabel("Welcome to SmartStock");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));

        JLabel subtitle = new JLabel("Inventory + Sales Management System");
        subtitle.setFont(subtitle.getFont().deriveFont(14f));

        continueBtn.setEnabled(false);

        testBtn.addActionListener(e -> testConnection());
        continueBtn.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Next: open Main Dashboard (Products / Inventory / Sales)."));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(title);
        top.add(Box.createVerticalStrut(6));
        top.add(subtitle);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        testBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        continueBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        center.add(statusLabel);
        center.add(Box.createVerticalStrut(10));
        center.add(testBtn);
        center.add(Box.createVerticalStrut(10));
        center.add(continueBtn);

        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        root.add(top, BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);

        setContentPane(root);
    }

    private void testConnection() {
        statusLabel.setText("Status: Checking...");
        testBtn.setEnabled(false);

        // Run DB work off the UI thread
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try (Connection conn = DB.getConnection()) {
                    // If we got here, connection worked
                } catch (Exception ex) {
                    // Re-throw to handle in done()
                    throw new RuntimeException(ex);
                }
                return null;
            }

            @Override
            protected void done() {
                testBtn.setEnabled(true);
                try {
                    get(); // will throw if connection failed
                    statusLabel.setText("Status: Connected ✅");
                    continueBtn.setEnabled(true);
                } catch (Exception ex) {
                    statusLabel.setText("Status: Failed ❌");
                    continueBtn.setEnabled(false);
                    JOptionPane.showMessageDialog(WelcomeFrame.this,
                            "Database connection failed:\n" + ex.getCause().getMessage(),
                            "Connection Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }
}