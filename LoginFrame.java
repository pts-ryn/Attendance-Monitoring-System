package auth;

import database.DataStore;
import instructor.InstructorDashboard;
import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;
import model.User;
import student.StudentDashboard;
import ui.BackgroundPanel;
import ui.UIConstants;

/**
 * LoginFrame.java
 * Same 550x420 window as every other screen.
 * The campus photo fills the whole window (cropped to fit) and the
 * login form sits on a semi-transparent white card in the middle.
 *
 * Flowchart: START -> LOGIN -> "Enter Username & Password" -> "Valid Login?"
 *   -> (No) "Invalid Login" -> back to START
 *   -> (Yes) "User Role?" -> Instructor Dashboard / Student Dashboard
 */
public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame() {
        setTitle("USTP Campus Attendance Monitoring System - Login");
        setSize(UIConstants.WINDOW_WIDTH, UIConstants.WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // ===== Background: the cropped photo fills the whole window =====
        // 60 = darkness over the photo, 0.5f = which part of the photo to keep
        BackgroundPanel background = new BackgroundPanel(
                UIConstants.BACKGROUND_IMAGE, new GridBagLayout(), 60, 0.5f);

        // ===== The white card in the middle =====
        CardPanel card = new CardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(18, 30, 18, 30));
        card.setPreferredSize(new Dimension(340, 340));

        JLabel title = new JLabel("USTP");
        title.setFont(new Font("Arial", Font.BOLD, 44));
        title.setForeground(UIConstants.NAVY);

        JLabel subtitle = new JLabel("Campus Attendance Monitoring System");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitle.setForeground(UIConstants.GRAY_TEXT);

        usernameField = new JTextField();
        passwordField = new JPasswordField();
        styleField(usernameField);
        styleField(passwordField);

        JButton loginButton = styledButton("Log in", UIConstants.YELLOW);
        loginButton.addActionListener(this::attemptLogin);

        JButton signUpButton = styledButton("Sign Up", UIConstants.NAVY);
        signUpButton.addActionListener(e -> {
            SignUpFrame signUpFrame = new SignUpFrame();
            signUpFrame.setVisible(true);
            this.dispose();
        });

        JLabel signUpHint = new JLabel("Don't have an account yet?");
        signUpHint.setFont(new Font("Arial", Font.BOLD, 12));
        signUpHint.setForeground(UIConstants.ORANGE_TEXT);

        getRootPane().setDefaultButton(loginButton); // pressing Enter = Log in

        card.add(centered(title));
        card.add(centered(subtitle));
        card.add(Box.createVerticalStrut(16));
        card.add(fieldLabel("Username"));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(10));
        card.add(fieldLabel("Password"));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(16));
        card.add(centered(loginButton));
        card.add(Box.createVerticalStrut(12));
        card.add(divider());
        card.add(Box.createVerticalStrut(10));
        card.add(centered(signUpHint));
        card.add(Box.createVerticalStrut(6));
        card.add(centered(signUpButton));

        background.add(card); // GridBagLayout centers it automatically
        add(background);
    }

    // A panel that paints a rounded, semi-transparent white card
    private static class CardPanel extends JPanel {
        CardPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 255, 255, 230)); // last number = see-through level
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ---------- Small helpers ----------

    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        label.setForeground(UIConstants.GRAY_TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    // Field with only a line underneath, like the reference
    private void styleField(JTextField field) {
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setOpaque(false);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, UIConstants.LINE_GRAY),
                BorderFactory.createEmptyBorder(4, 2, 4, 2)));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
    }

    private JButton styledButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(background);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(160, 36));
        return button;
    }

    private JPanel centered(JComponent component) {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrapper.setOpaque(false);
        wrapper.add(component);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, wrapper.getPreferredSize().height));
        return wrapper;
    }

    private JSeparator divider() {
        JSeparator line = new JSeparator();
        line.setForeground(UIConstants.LINE_GRAY);
        line.setAlignmentX(Component.LEFT_ALIGNMENT);
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return line;
    }

    /**
     * Runs when the Log in button is pressed.
     * This is the "Valid Login?" decision from your flowchart.
     */
    private void attemptLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        User user = DataStore.getInstance().validateLogin(username, password);

        if (user == null) {
            JOptionPane.showMessageDialog(this, "Invalid username or password.",
                    "Invalid Login", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
            return;
        }

        this.dispose();

        if (user.getRole() == User.Role.INSTRUCTOR) {
            new InstructorDashboard(user).setVisible(true);
        } else {
            new StudentDashboard(user).setVisible(true);
        }
    }
}