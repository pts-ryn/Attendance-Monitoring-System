package instructor;

import auth.LoginFrame;
import database.DataStore;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.AttendanceRecord;
import model.Excuse;
import model.Student;
import model.User;
import ui.UIConstants;

/**
 * InstructorDashboard.java
 * -----------------------------------------------------
 * This window matches the LEFT side of your flowchart:
 *   Instructor Dashboard -> "Choose Action?" ->
 *      Manage Student Info | Record Attendance | Manage Excuse | View Report
 *   -> each action finishes and loops back to "Instructor Dashboard (loop)"
 *   -> Logout -> END
 *
 * Each action opens its own small pop-up window (JDialog).
 * When you close that pop-up, you're back on this dashboard -
 * that pop-up/return pattern IS the "(loop)" arrow in your diagram.
 */
public class InstructorDashboard extends JFrame {

    private DataStore db = DataStore.getInstance(); // shortcut to our shared data
    private User currentUser;                       // the instructor who logged in

    public InstructorDashboard(User user) {
        this.currentUser = user;

        setTitle("Instructor Dashboard - " + user.getUsername());
        setSize(UIConstants.WINDOW_WIDTH, UIConstants.WINDOW_HEIGHT);// same size as every other screen
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // ----- Layout: a title on top, 4 big action buttons in the middle, logout at bottom -----
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Choose an Action", SwingConstants.CENTER);
        title.setFont(UIConstants.TITLE_FONT);
        mainPanel.add(title, BorderLayout.NORTH);

        // "Choose Action?" diamond -> 4 branches, shown here as 4 buttons in a grid
        JPanel actionsPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        JButton viewStudentsBtn = new JButton("View Students");
        JButton recordAttendanceBtn = new JButton("Record Attendance");
        JButton manageExcuseBtn = new JButton("Manage Excuse");
        JButton viewReportBtn = new JButton("View Report");

        // Each button opens the matching pop-up window
        viewStudentsBtn.addActionListener(e -> openViewStudents());
        recordAttendanceBtn.addActionListener(e -> openRecordAttendance());
        manageExcuseBtn.addActionListener(e -> openManageExcuse());
        viewReportBtn.addActionListener(e -> openViewReport());

        actionsPanel.add(viewStudentsBtn);
        actionsPanel.add(recordAttendanceBtn);
        actionsPanel.add(manageExcuseBtn);
        actionsPanel.add(viewReportBtn);
        mainPanel.add(actionsPanel, BorderLayout.CENTER);

        // LOGOUT button -> closes this dashboard and goes back to the Login screen
        JButton logoutBtn = UIConstants.glossyButton("LOGOUT");
        logoutBtn.addActionListener(e -> logout());
        mainPanel.add(logoutBtn, BorderLayout.SOUTH);

        add(mainPanel);
    }

    /**
     * "LOGOUT" box in your flowchart: closes this window and reopens
     * the LoginFrame, which is the same as the dashed arrow back to START.
     */
    private void logout() {
        this.dispose();
        new LoginFrame().setVisible(true);
    }

    // =========================================================
    // ACTION 1: View Students
    // Read-only list of every student who signed up. Instructors can't
    // add or edit students here - students create their own record in Sign Up.
    // =========================================================
        private void openViewStudents() {
        JDialog dialog = new JDialog(this, "View Students", true); // true = modal (blocks main window)
        dialog.setSize(UIConstants.DIALOG_WIDTH, UIConstants.DIALOG_HEIGHT); // same size as every other pop-up
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ----- Read-only table of every student who signed up -----
        String[] columns = {"Student ID", "Full Name", "Course", "Year Level"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false; // nobody can type into the cells
            }
        };
        ArrayList<Student> students = db.getAllStudents();
        for (Student s : students) {
            tableModel.addRow(new Object[]{s.getStudentId(), s.getFullName(), s.getCourse(), s.getYearLevel()});
        }
        JTable table = new JTable(tableModel);

        JLabel countLabel = new JLabel("Total registered students: " + students.size());

        // "Back to Dashboard" button - an explicit way back instead of relying on the X button
        JButton backBtn = UIConstants.glossyButton("Back to Dashboard");
        backBtn.addActionListener(e -> dialog.dispose());

        dialog.add(countLabel, BorderLayout.NORTH);
        dialog.add(new JScrollPane(table), BorderLayout.CENTER);
        dialog.add(backBtn, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    // =========================================================
    // ACTION 2: Record Attendance
    // Flow: Select Student/Class -> Time In -> Attendance Status -> Time Out -> Save Record
    // =========================================================
    private void openRecordAttendance() {
        JDialog dialog = new JDialog(this, "Record Attendance", true);
        dialog.setSize(UIConstants.DIALOG_WIDTH, UIConstants.DIALOG_HEIGHT); // same size as every other pop-up
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // All the fields live in their own panel so we can keep them their
        // natural size instead of stretching them to fill the whole dialog.
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 8, 8)); // 4 rows: student, status, time in, time out

        // "Select Student / Class" step -> dropdown of all students
        JComboBox<Student> studentBox = new JComboBox<>(db.getAllStudents().toArray(new Student[0]));

        // "Attendance Status" step -> dropdown of Present/Late/Absent
        JComboBox<AttendanceRecord.Status> statusBox = new JComboBox<>(AttendanceRecord.Status.values());

        // Label that will show the recorded Time In value once the button is clicked
        JLabel timeInLabel = new JLabel("Not yet timed in");
        JLabel timeOutLabel = new JLabel("Not yet timed out");

        JButton timeInBtn = UIConstants.glossyButton("Time In");
        JButton timeOutBtn = UIConstants.glossyButton("Time Out");
        JButton saveBtn = UIConstants.glossyButton("Save Record");

        // Time In: saves right away, only one record per student per day
        timeInBtn.addActionListener(e -> {
            Student selected = (Student) studentBox.getSelectedItem();
            if (selected == null) {
                JOptionPane.showMessageDialog(dialog, "No students yet. Students must sign up first.");
                return;
            }
            if (db.findRecordForDate(selected.getStudentId(), LocalDate.now()) != null) {
                JOptionPane.showMessageDialog(dialog, "This student already has a record for today.");
                return;
            }
            AttendanceRecord.Status status = (AttendanceRecord.Status) statusBox.getSelectedItem();
            AttendanceRecord record = new AttendanceRecord(
                    selected.getStudentId(), LocalDate.now(), LocalTime.now(), status);
            db.addAttendance(record);
            timeInLabel.setText(record.getTimeInFormatted());
        });

        // Time Out: finds today's open record, so it works even after reopening the dialog
        timeOutBtn.addActionListener(e -> {
            Student selected = (Student) studentBox.getSelectedItem();
            if (selected == null) return;
            AttendanceRecord open = db.findOpenRecordForToday(selected.getStudentId());
            if (open == null || open.getTimeIn() == null) {
                JOptionPane.showMessageDialog(dialog, "No open Time In for this student today.");
                return;
            }
            open.setTimeOut(LocalTime.now());
            db.updateAttendanceTimeOut(open);
            timeOutLabel.setText(open.getTimeOutFormatted());
        });

        // Save Record: applies the chosen status to today's record
        saveBtn.addActionListener(e -> {
            Student selected = (Student) studentBox.getSelectedItem();
            AttendanceRecord rec = (selected == null) ? null
                    : db.findRecordForDate(selected.getStudentId(), LocalDate.now());
            if (rec == null) {
                JOptionPane.showMessageDialog(dialog, "Please Time In first.");
                return;
            }
            rec.setStatus((AttendanceRecord.Status) statusBox.getSelectedItem());
            db.updateAttendanceStatus(rec);
            JOptionPane.showMessageDialog(dialog, "Attendance record saved.");
            dialog.dispose();
        });

        // Refresh the Time In / Time Out labels when switching students
        studentBox.addActionListener(e -> {
            Student s = (Student) studentBox.getSelectedItem();
            AttendanceRecord r = (s == null) ? null : db.findRecordForDate(s.getStudentId(), LocalDate.now());
            timeInLabel.setText(r == null ? "Not yet timed in" : r.getTimeInFormatted());
            timeOutLabel.setText((r == null || r.getTimeOut() == null) ? "Not yet timed out" : r.getTimeOutFormatted());
        });        

        formPanel.add(new JLabel("Select Student:"));
        formPanel.add(studentBox);
        formPanel.add(new JLabel("Attendance Status:"));
        formPanel.add(statusBox);
        formPanel.add(timeInBtn);
        formPanel.add(timeInLabel);
        formPanel.add(timeOutBtn);
        formPanel.add(timeOutLabel);

        // "Back to Dashboard" button - an explicit way back instead of relying on the X button
        JButton backBtn = UIConstants.glossyButton("Back to Dashboard");
        backBtn.addActionListener(e -> dialog.dispose());

        // Save Record + Back sit side by side at the bottom, like the other pop-ups
        JPanel buttonRow = new JPanel(new GridLayout(1, 2, 10, 10));
        buttonRow.add(saveBtn);
        buttonRow.add(backBtn);
        dialog.add(buttonRow, BorderLayout.SOUTH);

        // Wrap the form so it keeps its natural height instead of being
        // stretched to fill all the leftover space in the dialog.
        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.add(formPanel, BorderLayout.NORTH);
        dialog.add(formWrapper, BorderLayout.CENTER);

        dialog.setVisible(true);
    }

    // =========================================================
    // ACTION 3: Manage Excuse
    // Flow: View Student Excuse -> Approve/Reject -> Update Excuse Status
    // =========================================================
    private void openManageExcuse() {
        JDialog dialog = new JDialog(this, "Manage Excuse", true);
        dialog.setSize(UIConstants.DIALOG_WIDTH, UIConstants.DIALOG_HEIGHT); // same size as every other pop-up
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        // "View Student Excuse" step -> table listing every excuse letter submitted
        String[] columns = {"Student ID", "Date Filed", "Reason", "Status"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false; // read-only table; status changes go through the buttons below
            }
        };
        ArrayList<Excuse> excuses = db.getAllExcuses();
        for (Excuse ex : excuses) {
            tableModel.addRow(new Object[]{ex.getStudentId(), ex.getDateFiled(), ex.getReason(), ex.getStatus()});
        }
        JTable table = new JTable(tableModel);
        dialog.add(new JScrollPane(table), BorderLayout.CENTER);

        // "Approve/Reject" buttons
        JPanel buttonPanel = new JPanel();
        JButton approveBtn = UIConstants.glossyButton("Approve");
        JButton rejectBtn = UIConstants.glossyButton("Reject");
        JButton backBtn = UIConstants.glossyButton("Back to Dashboard");
        backBtn.addActionListener(e -> dialog.dispose());
        buttonPanel.add(approveBtn);
        buttonPanel.add(rejectBtn);
        buttonPanel.add(backBtn);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // "Update Excuse Status" step: change the status of whichever row is selected
        approveBtn.addActionListener(e -> updateSelectedExcuseStatus(table, excuses, tableModel, Excuse.Status.APPROVED));
        rejectBtn.addActionListener(e -> updateSelectedExcuseStatus(table, excuses, tableModel, Excuse.Status.REJECTED));

        dialog.setVisible(true);
    }

            // Helper method shared by both Approve and Reject buttons above
    private void updateSelectedExcuseStatus(JTable table, ArrayList<Excuse> excuses,
                                             DefaultTableModel tableModel, Excuse.Status newStatus) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(table, "Please select an excuse row first.");
            return;
        }
        Excuse excuse = excuses.get(row);

        // Already decided? Stop here, before anything gets changed.
        if (excuse.getStatus() != Excuse.Status.PENDING) {
            JOptionPane.showMessageDialog(table, "This excuse was already " + excuse.getStatus() + ".");
            return;
        }

        excuse.setStatus(newStatus);              // update the real data
        db.updateExcuseStatus(excuse);            // save to the database
        tableModel.setValueAt(newStatus, row, 3); // update what's shown on screen

        // An approved excuse marks that day EXCUSED; a rejected one marks it ABSENT
        AttendanceRecord.Status attendanceStatus = (newStatus == Excuse.Status.APPROVED)
                ? AttendanceRecord.Status.EXCUSED
                : AttendanceRecord.Status.ABSENT;

        AttendanceRecord existing = db.findRecordForDate(excuse.getStudentId(), excuse.getDateFiled());
        if (existing != null) {
            // Only change records that came from an absence, never a real Time In
            if (existing.getTimeIn() == null) {
                existing.setStatus(attendanceStatus);
                db.updateAttendanceStatus(existing);
            }
        } else {
            // No record for that day yet, so create one (no time in/out)
            db.addAttendance(new AttendanceRecord(excuse.getStudentId(), excuse.getDateFiled(), null, attendanceStatus));
        }
    }

    // =========================================================
    // ACTION 4: View Report
    // Flow: View Summary -> Display Report
    // =========================================================
        private void openViewReport() {
        JDialog dialog = new JDialog(this, "Attendance Report", true);
        dialog.setSize(UIConstants.DIALOG_WIDTH, UIConstants.DIALOG_HEIGHT); // same size as every other pop-up
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        // "View Summary" step -> one row per student, counting how many
        // Present / Late / Absent / Excused records they have. This is built
        // from AttendanceRecord's status field via DataStore.countAttendanceByStatus.
        String[] summaryColumns = {"Student ID", "Full Name", "Present", "Late", "Absent", "Excused", "Total"};
        DefaultTableModel summaryModel = new DefaultTableModel(summaryColumns, 0);
        for (Student s : db.getAllStudents()) {
            int present = db.countAttendanceByStatus(s.getStudentId(), AttendanceRecord.Status.PRESENT);
            int late = db.countAttendanceByStatus(s.getStudentId(), AttendanceRecord.Status.LATE);
            int absent = db.countAttendanceByStatus(s.getStudentId(), AttendanceRecord.Status.ABSENT);
            int excused = db.countAttendanceByStatus(s.getStudentId(), AttendanceRecord.Status.EXCUSED);
            summaryModel.addRow(new Object[]{
                    s.getStudentId(), s.getFullName(), present, late, absent, excused, present + late + absent + excused
            });
        }
        JTable summaryTable = new JTable(summaryModel);

        // "Display Report" step -> the full raw log of every attendance record ever
        // saved, with each student's name looked up alongside their ID.
        String[] logColumns = {"Student ID", "Full Name", "Date", "Time In", "Time Out", "Status"};
        DefaultTableModel logModel = new DefaultTableModel(logColumns, 0);
        for (AttendanceRecord r : db.getAllAttendance()) {
            Student s = db.findStudentById(r.getStudentId());
            String fullName = (s != null) ? s.getFullName() : "Unknown";
            logModel.addRow(new Object[]{
                    r.getStudentId(), fullName, r.getDate(), r.getTimeInFormatted(),
                    r.getTimeOutFormatted(), r.getStatus()
            });
        }
        JTable logTable = new JTable(logModel);

        // Two tabs: the per-student summary counts, and the full detailed log
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Summary", new JScrollPane(summaryTable));
        tabs.addTab("Detailed Log", new JScrollPane(logTable));
        dialog.add(tabs, BorderLayout.CENTER);

        // "Back to Dashboard" button - an explicit way back instead of relying on the X button
        JButton backBtn = UIConstants.glossyButton("Back to Dashboard");
        backBtn.addActionListener(e -> dialog.dispose());
        dialog.add(backBtn, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}