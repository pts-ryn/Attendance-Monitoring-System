package app;

import auth.LoginFrame;
import javax.swing.SwingUtilities;

/**
 * Main.java
 * -----------------------------------------------------
 * This is where the program begins running.
 * It matches the "START" oval at the very top of your flowchart.
 *
 * All it does is open the LoginFrame window - everything else  
 * (role checking, dashboards, logout) happens from there.
 */
public class Main {
    public static void main(String[] args) {
        // SwingUtilities.invokeLater makes sure the GUI is built and shown
        // safely on Java's dedicated UI thread (standard practice for Swing apps).
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true); // shows the window on screen
        });
    }
}
