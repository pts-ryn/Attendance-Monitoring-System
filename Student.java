package model;

/**
 * Student.java
 * -----------------------------------------------------
 * Represents one student's basic information.
 * Matches "Add/Edit Student Information" in your flowchart
 * (Student ID, full name, course, year level).
 */
public class Student {

    private String studentId;  // unique ID, e.g. "2023-0001"
    private String fullName;
    private String course;
    private String yearLevel;

    public Student(String studentId, String fullName, String course, String yearLevel) {
        this.studentId = studentId;
        this.fullName = fullName;
        this.course = course;
        this.yearLevel = yearLevel;
    }

    // --- Getters ---
    public String getStudentId() {
        return studentId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getCourse() {
        return course;
    }

    public String getYearLevel() {
        return yearLevel;
    }

    // --- Setters: let the instructor EDIT existing student info ---
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public void setYearLevel(String yearLevel) {
        this.yearLevel = yearLevel;
    }

    /**
     * toString() controls how this object looks when placed in a
     * dropdown list (JComboBox) or printed. We show ID + name so
     * instructors can easily pick the right student.
     */
    @Override
    public String toString() {
        return studentId + " - " + fullName;
    }
}
