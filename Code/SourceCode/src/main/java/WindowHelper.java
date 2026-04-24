import javax.swing.*;
import java.awt.*;

public class WindowHelper {

    public static <T extends JFrame> T findOpenWindow(Class<T> screenClass) {
        for (Window window : Window.getWindows()) {
            if (!window.isDisplayable()) {
                continue;
            }

            if (screenClass.isInstance(window)) {
                return screenClass.cast(window);
            }
        }
        return null;
    }

    public static boolean focusIfAlreadyOpen(Class<? extends JFrame> screenClass) {
        JFrame frame = findOpenWindow(screenClass);
        if (frame == null) {
            return false;
        }

        if (frame.getState() == Frame.ICONIFIED) {
            frame.setState(Frame.NORMAL);
        }

        frame.toFront();
        frame.requestFocus();
        frame.setAlwaysOnTop(true);
        frame.setAlwaysOnTop(false);

        return true;
    }
}