package student;

import auth.LoginFrame;
import database.DataStore;
import java.awt.*;
import java.time.LocalDate;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.AttendanceRecord;
import model.Excuse;
import model.User;
import ui.UIConstants;

/**
 * StudentDashboard.java
 * -----------------------------------------------------
 * This window matches the RIGHT side of your flowchart:
 *   Student Dashboard -> "Choose Action?" ->
 *      View Attendance | Submit Excuse
 *   -> each action finishes and loops back to "Student Dashboard (loop)"
 *   -> Logout -> END
 */
public class StudentDashboard extends JFrame {

    private DataStore db = DataStore.getInstance();
    private User currentUser; // the student who logged in

    public StudentDashboard(User user) {
        this.currentUser = user;

        setTitle("Student Dashboard - " + user.getUsername());
        setSize(UIConstants.WINDOW_WIDTH, UIConstants.WINDOW_HEIGHT); // same size as every other screen
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Choose an Action", SwingConstants.CENTER);
        title.setFont(UIConstants.TITLE_FONT);
        mainPanel.add(title, BorderLayout.NORTH);

                // "Choose Action?" diamond -> 3 branches, shown as 3 buttons
        JPanel actionsPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        JButton viewAttendanceBtn = new JButton("View Attendance");
        JButton submitExcuseBtn = new JButton("Submit Excuse");
        JButton viewExcusesBtn = new JButton("View My Excuses");

        viewAttendanceBtn.addActionListener(e -> openViewAttendance());
        submitExcuseBtn.addActionListener(e -> openSubmitExcuse());
        viewExcusesBtn.addActionListener(e -> openViewExcuses());

        actionsPanel.add(viewAttendanceBtn);
        actionsPanel.add(submitExcuseBtn);
        actionsPanel.add(viewExcusesBtn);
        mainPanel.add(actionsPanel, BorderLayout.CENTER);

        JButton logoutBtn = UIConstants.glossyButton("LOGOUT");
        logoutBtn.addActionListener(e -> logout());
        mainPanel.add(logoutBtn, BorderLayout.SOUTH);

        add(mainPanel);
    }

    // "LOGOUT" -> back to Login screen (same as the dashed line back to START)
    private void logout() {
        this.dispose();
        new LoginFrame().setVisible(true);
    }

    // =========================================================
    // ACTION 1: View Attendance
    // Flow: View Attendance -> Display Personal Records
    // =========================================================
    private void openViewAttendance() {
        JDialog dialog = new JDialog(this, "My Attendance Records", true);
        dialog.setSize(UIConstants.DIALOG_WIDTH, UIConstants.DIALOG_HEIGHT); // same size as every other pop-up
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        // "Display Personal Records" step -> only THIS student's own records, never anyone else's
        String[] columns = {"Date", "Time In", "Time Out", "Status"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0);
        for (AttendanceRecord r : db.getAttendanceForStudent(currentUser.getStudentId())) {
            tableModel.addRow(new Object[]{
                    r.getDate(), r.getTimeInFormatted(), r.getTimeOutFormatted(), r.getStatus()
            });
        }
        JTable table = new JTable(tableModel);
        dialog.add(new JScrollPane(table), BorderLayout.CENTER);

        // "Back to Dashboard" buttton - an explicit way back instead of relying on the X button
        JButton backBtn = UIConstants.glossyButton("Back to Dashboard");
        backBtn.addActionListener(e -> dialog.dispose());
        dialog.add(backBtn, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    // =========================================================
    // ACTION 2: Submit Excuse
    // Flow: Submit Excuse -> Enter Reason for Absence -> Excuse Pending
    // =========================================================
    private void openSubmitExcuse() {
        JDialog dialog = new JDialog(this, "Submit Excuse", true);
        dialog.setSize(UIConstants.DIALOG_WIDTH, UIConstants.DIALOG_HEIGHT); // same size as every other pop-up
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // "Enter Reason for Absence" step -> a text area for the student to type into
        JTextArea reasonArea = new JTextArea(5, 30);
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);

        JPanel textPanel = new JPanel(new BorderLayout(5, 5));
        textPanel.add(new JLabel("Reason for Absence:"), BorderLayout.NORTH);
        textPanel.add(new JScrollPane(reasonArea), BorderLayout.CENTER);

        // Wrap the text panel so it keeps its natural height instead of
        // being stretched to fill all the leftover space in the dialog.
        JPanel textWrapper = new JPanel(new BorderLayout());
        textWrapper.add(textPanel, BorderLayout.NORTH);
        dialog.add(textWrapper, BorderLayout.CENTER);

        // Submit button + an explicit way back instead of relying on the X button
        JButton submitBtn = UIConstants.glossyButton("Submit");
        JButton backBtn = UIConstants.glossyButton("Back to Dashboard");
        backBtn.addActionListener(e -> dialog.dispose());

        JPanel buttonRow = new JPanel(new GridLayout(1,2,10,10));
        buttonRow.add(submitBtn);
        buttonRow.add(backBtn);
        dialog.add(buttonRow, BorderLayout.SOUTH);

        // Clicking Submit -> creates the Excuse object, which starts as "Excuse Pending"
        submitBtn.addActionListener(e -> {
            String reason = reasonArea.getText().trim();
            if (reason.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a reason before submitting.");
                return;
            }
            Excuse excuse = new Excuse(currentUser.getStudentId(), LocalDate.now(), reason);
            db.addExcuse(excuse); // status is automatically PENDING (see Excuse.java constructor)
            JOptionPane.showMessageDialog(dialog, "Excuse submitted. Status: PENDING");
            dialog.dispose(); // closing = looping back to "Student Dashboard (loop)"
        });

                dialog.setVisible(true);
    }

    // =========================================================
    // ACTION 3: View My Excuses
    // Lets the student see whether each excuse they submitted is
    // still Pending, or has been Approved / Rejected by the instructor.
    // =========================================================
    private void openViewExcuses() {
        JDialog dialog = new JDialog(this, "My Excuses", true);
        dialog.setSize(UIConstants.DIALOG_WIDTH, UIConstants.DIALOG_HEIGHT); // same size as every other pop-up
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        // Only THIS student's own excuses, never anyone else's
        String[] columns = {"Date Filed", "Reason", "Status"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0);
        for (Excuse ex : db.getExcusesForStudent(currentUser.getStudentId())) {
            tableModel.addRow(new Object[]{ex.getDateFiled(), ex.getReason(), ex.getStatus()});
        }
        JTable table = new JTable(tableModel);
        dialog.add(new JScrollPane(table), BorderLayout.CENTER);

        // "Back to Dashboard" button - an explicit way back instead of relying on the X button
        JButton backBtn = UIConstants.glossyButton("Back to Dashboard");
        backBtn.addActionListener(e -> dialog.dispose());
        dialog.add(backBtn, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}