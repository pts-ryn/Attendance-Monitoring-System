package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import javax.swing.JOptionPane;

import model.AttendanceRecord;
import model.Excuse;
import model.Student;
import model.User;

/**
 * DataStore.java
 * -----------------------------------------------------
 * Now backed by PostgreSQL (database: attendance_db) instead of
 * in-memory lists. Every screen still only talks to DataStore,
 * so the UI code does not need to know about SQL.
 *
 * Singleton: only ONE DataStore for the whole program.
 */
public class DataStore {

    private static DataStore instance; // the single shared instance

    private DataStore() {
    }

    public static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    // Shows a popup if the database has a problem
    private void showDbError(SQLException e) {
        JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage());
    }

    // ===================== USER / LOGIN =====================

    public User validateLogin(String username, String password) {
        String sql = "SELECT username, password, role, student_id FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getString("username"),
                            rs.getString("password"),
                            User.Role.valueOf(rs.getString("role")),
                            rs.getString("student_id"));
                }
            }
        } catch (SQLException e) {
            showDbError(e);
        }
        return null; // no match = invalid login
    }

    public boolean isUsernameTaken(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            showDbError(e);
        }
        return false;
    }

    public void registerUser(User user) {
        String sql = "INSERT INTO users (username, password, role, student_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole().name());
            ps.setString(4, user.getStudentId()); // null for instructors
            ps.executeUpdate();
        } catch (SQLException e) {
            showDbError(e);
        }
    }

    // ===================== STUDENTS =====================

    public ArrayList<Student> getAllStudents() {
        ArrayList<Student> result = new ArrayList<>();
        String sql = "SELECT s.student_id, s.full_name, s.course, s.year_level "
           + "FROM students s "
           + "JOIN users u ON u.student_id = s.student_id "
           + "WHERE u.role = 'STUDENT' "
           + "ORDER BY s.student_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new Student(
                        rs.getString("student_id"),
                        rs.getString("full_name"),
                        rs.getString("course"),
                        rs.getString("year_level")));
            }
        } catch (SQLException e) {
            showDbError(e);
        }
        return result;
    }

    public void addStudent(Student student) {
        String sql = "INSERT INTO students (student_id, full_name, course, year_level) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, student.getStudentId());
            ps.setString(2, student.getFullName());
            ps.setString(3, student.getCourse());
            ps.setString(4, student.getYearLevel());
            ps.executeUpdate();
        } catch (SQLException e) {
            showDbError(e);
        }
    }

    public Student findStudentById(String studentId) {
        String sql = "SELECT student_id, full_name, course, year_level FROM students WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Student(
                            rs.getString("student_id"),
                            rs.getString("full_name"),
                            rs.getString("course"),
                            rs.getString("year_level"));
                }
            }
        } catch (SQLException e) {
            showDbError(e);
        }
        return null;
    }

    public boolean isStudentIdTaken(String studentId) {
        return findStudentById(studentId) != null;
    }

    // ===================== ATTENDANCE =====================

    // Turns one database row into an AttendanceRecord object
    private AttendanceRecord mapRecord(ResultSet rs) throws SQLException {
        Time in = rs.getTime("time_in");
        Time out = rs.getTime("time_out");
        AttendanceRecord r = new AttendanceRecord(
                rs.getString("student_id"),
                rs.getDate("attendance_date").toLocalDate(),
                in == null ? null : in.toLocalTime(),
                AttendanceRecord.Status.valueOf(rs.getString("status")));
        if (out != null) {
            r.setTimeOut(out.toLocalTime());
        }
        return r;
    }

    private static final String ATTENDANCE_COLUMNS =
            "student_id, attendance_date, time_in, time_out, status";

    public ArrayList<AttendanceRecord> getAllAttendance() {
        ArrayList<AttendanceRecord> result = new ArrayList<>();
        String sql = "SELECT " + ATTENDANCE_COLUMNS
                + " FROM attendance_records ORDER BY attendance_date, student_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRecord(rs));
            }
        } catch (SQLException e) {
            showDbError(e);
        }
        return result;
    }

    // "Save Record" step: adds a new attendance entry
    public void addAttendance(AttendanceRecord r) {
        String sql = "INSERT INTO attendance_records "
                + "(student_id, attendance_date, time_in, time_out, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getStudentId());
            ps.setDate(2, java.sql.Date.valueOf(r.getDate()));
            if (r.getTimeIn() == null) {
                ps.setNull(3, Types.TIME);
            } else {
                ps.setTime(3, Time.valueOf(r.getTimeIn()));
            }
            if (r.getTimeOut() == null) {
                ps.setNull(4, Types.TIME);
            } else {
                ps.setTime(4, Time.valueOf(r.getTimeOut()));
            }
            ps.setString(5, r.getStatus().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            showDbError(e);
        }
    }

    // Saves a new Time Out for an existing record
    public void updateAttendanceTimeOut(AttendanceRecord r) {
        String sql = "UPDATE attendance_records SET time_out = ? WHERE student_id = ? AND attendance_date = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (r.getTimeOut() == null) {
                ps.setNull(1, Types.TIME);
            } else {
                ps.setTime(1, Time.valueOf(r.getTimeOut()));
            }
            ps.setString(2, r.getStudentId());
            ps.setDate(3, java.sql.Date.valueOf(r.getDate()));
            ps.executeUpdate();
        } catch (SQLException e) {
            showDbError(e);
        }
    }

    // Saves a new status for an existing record
    public void updateAttendanceStatus(AttendanceRecord r) {
        String sql = "UPDATE attendance_records SET status = ? WHERE student_id = ? AND attendance_date = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getStatus().name());
            ps.setString(2, r.getStudentId());
            ps.setDate(3, java.sql.Date.valueOf(r.getDate()));
            ps.executeUpdate();
        } catch (SQLException e) {
            showDbError(e);
        }
    }

    // Today's record for a student that has NOT been timed out yet
    public AttendanceRecord findOpenRecordForToday(String studentId) {
        String sql = "SELECT " + ATTENDANCE_COLUMNS
                + " FROM attendance_records WHERE student_id = ? AND attendance_date = ? AND time_out IS NULL";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRecord(rs);
                }
            }
        } catch (SQLException e) {
            showDbError(e);
        }
        return null;
    }

    // A student's record for one specific date (timed out or not)
    public AttendanceRecord findRecordForDate(String studentId, LocalDate date) {
        String sql = "SELECT " + ATTENDANCE_COLUMNS
                + " FROM attendance_records WHERE student_id = ? AND attendance_date = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setDate(2, java.sql.Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRecord(rs);
                }
            }
        } catch (SQLException e) {
            showDbError(e);
        }
        return null;
    }

    // All attendance records belonging to ONE student
    public ArrayList<AttendanceRecord> getAttendanceForStudent(String studentId) {
        ArrayList<AttendanceRecord> result = new ArrayList<>();
        String sql = "SELECT " + ATTENDANCE_COLUMNS
                + " FROM attendance_records WHERE student_id = ? ORDER BY attendance_date";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRecord(rs));
                }
            }
        } catch (SQLException e) {
            showDbError(e);
        }
        return result;
    }

    // Powers the summary counts on the instructor's View Report screen
    public int countAttendanceByStatus(String studentId, AttendanceRecord.Status status) {
        String sql = "SELECT COUNT(*) FROM attendance_records WHERE student_id = ? AND status = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setString(2, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            showDbError(e);
        }
        return 0;
    }

    // ===================== EXCUSES =====================

    private Excuse mapExcuse(ResultSet rs) throws SQLException {
        return new Excuse(
                rs.getInt("id"),
                rs.getString("student_id"),
                rs.getDate("date_filed").toLocalDate(),
                rs.getString("reason"),
                Excuse.Status.valueOf(rs.getString("status")));
    }

    public ArrayList<Excuse> getAllExcuses() {
        ArrayList<Excuse> result = new ArrayList<>();
        String sql = "SELECT id, student_id, date_filed, reason, status FROM excuses ORDER BY id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapExcuse(rs));
            }
        } catch (SQLException e) {
            showDbError(e);
        }
        return result;
    }

    // "Excuse Pending" step: student submits a new excuse
    public void addExcuse(Excuse excuse) {
        String sql = "INSERT INTO excuses (student_id, date_filed, reason, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, excuse.getStudentId());
            ps.setDate(2, java.sql.Date.valueOf(excuse.getDateFiled()));
            ps.setString(3, excuse.getReason());
            ps.setString(4, excuse.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    excuse.setId(keys.getInt("id"));
                }
            }
        } catch (SQLException e) {
            showDbError(e);
        }
    }

    // Saves Approve / Reject for an existing excuse
    public void updateExcuseStatus(Excuse excuse) {
        String sql = "UPDATE excuses SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, excuse.getStatus().name());
            ps.setInt(2, excuse.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            showDbError(e);
        }
    }

    // All excuses submitted by ONE student
    public ArrayList<Excuse> getExcusesForStudent(String studentId) {
        ArrayList<Excuse> result = new ArrayList<>();
        String sql = "SELECT id, student_id, date_filed, reason, status FROM excuses WHERE student_id = ? ORDER BY id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapExcuse(rs));
                }
            }
        } catch (SQLException e) {
            showDbError(e);
        }
        return result;
    }
}