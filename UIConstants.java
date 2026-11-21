package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JButton;

/**
 * UIConstants.java
 * -----------------------------------------------------
 * Holds shared sizing/styling values so every window in the app
 * looks the same instead of random sizes everywhere.
 */
public class UIConstants {

    // Size used for every MAIN screen: Login, Sign Up,
    // Instructor Dashboard, Student Dashboard.
    public static final int WINDOW_WIDTH = 550;
    public static final int WINDOW_HEIGHT = 420;

    // Size used for every POP-UP window
    public static final int DIALOG_WIDTH = 550;
    public static final int DIALOG_HEIGHT = 420;

    // Shared title font so every screen's heading looks the same
    public static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 20);

    // ---- Background image + colors (taken from the USTeP reference) ----
    public static final String BACKGROUND_IMAGE = "images/background.jpg";
    public static final Color NAVY = new Color(32, 26, 92);
    public static final Color YELLOW = new Color(251, 191, 52);
    public static final Color ORANGE_TEXT = new Color(240, 165, 0);
    public static final Color GRAY_TEXT = new Color(110, 110, 110);
    public static final Color LINE_GRAY = new Color(200, 200, 200);

    // ---- Glossy light-yellow button colors (change the shade here) ----
    public static final Color GLOSS_TOP = new Color(255, 249, 222);     // very pale yellow at the top
    public static final Color GLOSS_SHINE = Color.WHITE;                // the white "reflection" band
    public static final Color GLOSS_BOTTOM = new Color(252, 226, 140);  // soft light yellow at the bottom
    public static final Color GLOSS_BORDER = new Color(214, 184, 96);   // thin outline

    /**
     * Creates a glossy light-yellow button.
     * Use it instead of: new JButton("text")
     * Example: JButton logoutBtn = UIConstants.glossyButton("LOGOUT");
     */
    public static JButton glossyButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                int w = getWidth();
                int h = getHeight();
                int split = (int) (h * 0.3); // where the white shine band sits

                Color top = GLOSS_TOP, shine = GLOSS_SHINE, bottom = GLOSS_BOTTOM;
                if (!isEnabled()) {
                    top = new Color(245, 245, 245);
                    bottom = new Color(222, 222, 222);
                } else if (getModel().isArmed() && getModel().isPressed()) {
                    top = mix(top, Color.BLACK, 0.12);      // pressed: a little darker
                    shine = mix(shine, Color.BLACK, 0.12);
                    bottom = mix(bottom, Color.BLACK, 0.12);
                } else if (getModel().isRollover()) {
                    top = mix(top, Color.WHITE, 0.4);       // mouse over: a little lighter
                    bottom = mix(bottom, Color.WHITE, 0.3);
                }

                g2.setPaint(new GradientPaint(0, 0, top, 0, split, shine));
                g2.fillRect(0, 0, w, split);
                g2.setPaint(new GradientPaint(0, split, shine, 0, h, bottom));
                g2.fillRect(0, split, w, h - split);
                g2.setColor(GLOSS_BORDER);
                g2.drawRect(0, 0, w - 1, h - 1);
                g2.dispose();

                super.paintComponent(g); // draws the text on top
            }
        };
        button.setForeground(NAVY);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        return button;
    }

    // Blends color a toward color b (t = 0 means all a, t = 1 means all b)
    private static Color mix(Color a, Color b, double t) {
        return new Color(
                (int) (a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }
}