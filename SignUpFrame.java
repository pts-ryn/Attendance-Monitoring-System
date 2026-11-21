package auth;

import java.awt.*;
import javax.swing.*;

import database.DataStore;
import model.Student;
import model.User;
import model.User.Role;
import ui.UIConstants;

/**
 * SignUpFrame.java
 * -----------------------------------------------------
 * This is the account-creation screen. Instead of hardcoded demo
 * accounts, users now type in their OWN username/password here,
 * pick whether they are an Instructor or Student, and (if Student)
 * fill in their student info too.
 *
 * Flow: LOGIN screen -> "Sign Up" button -> this screen ->
 *       "Create Account" -> saved into DataStore -> back to LOGIN
 */
public class SignUpFrame extends JFrame {

    private DataStore db = DataStore.getInstance();

    // Common fields (used by both roles)
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<User.Role> roleBox;

    // Student-only fields
    private JTextField studentIdField;
    private JTextField fullNameField;
    private JTextField courseField;
    private JTextField yearLevelField;

    // This panel swaps between "no extra fields" (instructor) and
    // "student info fields" (student) depending on the role picked.
    private JPanel cardPanel;
    private CardLayout cardLayout;

    private static final String INSTRUCTOR_CARD = "INSTRUCTOR";
    private static final String STUDENT_CARD = "STUDENT";

    public SignUpFrame() {
        setTitle("USTP Campus Attendance Monitoring System - Sign Up");
        setSize(UIConstants.WINDOW_WIDTH, UIConstants.WINDOW_HEIGHT); // same size as every other screen
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ----- Title (same font used on every screen, for a uniform look) -----
        JLabel title = new JLabel("CREATE ACCOUNT", SwingConstants.CENTER);
        title.setFont(UIConstants.TITLE_FONT);

        // ----- Top section: username, password, role -----
        JPanel topForm = new JPanel(new GridLayout(3, 2, 10, 10));
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        roleBox = new JComboBox<>(User.Role.values()); // shows INSTRUCTOR and STUDENT

        topForm.add(new JLabel("Username:"));
        topForm.add(usernameField);
        topForm.add(new JLabel("Password:"));
        topForm.add(passwordField);
        topForm.add(new JLabel("I am a:"));
        topForm.add(roleBox);

        // ----- Middle section: swaps depending on chosen role -----
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Card shown when role = INSTRUCTOR (no extra info needed)
        JPanel instructorCard = new JPanel();
        instructorCard.add(new JLabel("No additional info needed for instructors."));

        // Card shown when role = STUDENT (needs student profile info)
        JPanel studentCard = new JPanel(new GridLayout(4, 2, 10, 10));
        studentIdField = new JTextField();
        fullNameField = new JTextField();
        courseField = new JTextField();
        yearLevelField = new JTextField();
        studentCard.add(new JLabel("Student ID:"));
        studentCard.add(studentIdField);
        studentCard.add(new JLabel("Full Name:"));
        studentCard.add(fullNameField);
        studentCard.add(new JLabel("Course:"));
        studentCard.add(courseField);
        studentCard.add(new JLabel("Year Level:"));
        studentCard.add(yearLevelField);

        cardPanel.add(instructorCard, INSTRUCTOR_CARD);
        cardPanel.add(studentCard, STUDENT_CARD);

        // When the role dropdown changes, switch which card is showing
        roleBox.addActionListener(e -> {
            User.Role selected = (User.Role) roleBox.getSelectedItem();
            if (selected == User.Role.STUDENT) {
                cardLayout.show(cardPanel, STUDENT_CARD);
            } else {
                cardLayout.show(cardPanel, INSTRUCTOR_CARD);
            }
        });

        // ----- Bottom section: buttons -----
        JButton createBtn = new JButton("Create Account");
        JButton backBtn = new JButton("Back to Login");
        createBtn.addActionListener(e -> createAccount());
        backBtn.addActionListener(e -> {
            this.dispose();
            new LoginFrame().setVisible(true);
        });

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        buttonPanel.add(createBtn);
        buttonPanel.add(backBtn);

    // Combine the title and the username/password/role form into one "north" section
        JPanel northPanel = new JPanel(new BorderLayout(10,10));
        northPanel.add(title, BorderLayout.NORTH);
        northPanel.add(topForm, BorderLayout.CENTER);

        // Wrap the card panel so it keeps its natural height instead of
        // being stretched to fill all the leftover space in the window
        // (that stretching is what was making the text boxes huge).
        JPanel cardWrapper = new JPanel(new BorderLayout());
        cardWrapper.add(cardPanel, BorderLayout.NORTH);

        // ----- Put it all together -----
        mainPanel.add(northPanel, BorderLayout.NORTH);
        mainPanel.add(cardWrapper, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    /**
     * Runs when "Create Account" is clicked.
     * Validates the input, then saves a new User (and Student, if
     * applicable) into the DataStore.
     */
    private void createAccount() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        User.Role role = (User.Role) roleBox.getSelectedItem();

        // ----- Basic validation -----
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password are required.");
            return;
        }
        if (db.isUsernameTaken(username)) {
            JOptionPane.showMessageDialog(this, "That username is already taken. Please choose another.");
            return;
        }

        if (role == User.Role.STUDENT) {
            // ----- Extra validation + saving for STUDENT role -----
            String studentId = studentIdField.getText().trim();
            String fullName = fullNameField.getText().trim();
            String course = courseField.getText().trim();
            String yearLevel = yearLevelField.getText().trim();

            if (studentId.isEmpty() || fullName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Student ID and Full Name are required.");
                return;
            }

            if (db.isStudentIdTaken(studentId)) {
                JOptionPane.showMessageDialog(this, "That Student ID is already registered.");
                return;
            }

            // Create the student's profile record first...
            Student newStudent = new Student(studentId, fullName, course, yearLevel);
            db.addStudent(newStudent);

            // ...then create their login account, linked to that student ID.
            User newUser = new User(username, password, User.Role.STUDENT, studentId);
            db.registerUser(newUser);

        } else {
            // ----- Saving for INSTRUCTOR role (no student info needed) -----
            User newUser = new User(username, password, User.Role.INSTRUCTOR, null);
            db.registerUser(newUser);
        }

        JOptionPane.showMessageDialog(this, "Account created! You can now log in.");
        this.dispose();
        new LoginFrame().setVisible(true); // send them to the login screen to log in
    }
}