package model;

/**
 * User.java
 * -----------------------------------------------------
 * Represents ONE login account in the system.
 * This matches the "Enter Username & Password" -> "Valid Login?" ->
 * "User Role?" part of your flowchart.
 *
 * A User can be an INSTRUCTOR or a STUDENT.
 * If the user is a student, studentId links this login account
 * to their Student record (so we know whose attendance to show).
 */
public class User {

    // The two possible roles in the system (matches "User Role?" diamond)
    public enum Role {
        INSTRUCTOR,
        STUDENT
    }

    private String username;   // what they type in "Enter Username"
    private String password;   // what they type in "Enter Password"
    private Role role;         // INSTRUCTOR or STUDENT
    private String studentId;  // only used when role == STUDENT (links to Student.java)

    // Constructor: runs when we create a new User object
    public User(String username, String password, Role role, String studentId) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.studentId = studentId; // can be null for instructor accounts
    }

    // --- Getters: simple methods that just return the stored value ---
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public String getStudentId() {
        return studentId;
    }

    /**
     * Checks if the given username/password matches THIS user.
     * Used by LoginFrame to answer the "Valid Login?" question.
     */
    public boolean matches(String inputUsername, String inputPassword) {
        return this.username.equals(inputUsername) && this.password.equals(inputPassword);
    }
}