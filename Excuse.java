package model;

import java.time.LocalDate;

/**
 * Excuse.java
 * -----------------------------------------------------
 * Represents a student's excuse letter for an absence.
 * Matches "Enter Reason for Absence" -> "Excuse Pending" ->
 * "Approve/Reject" -> "Update Excuse Status" in your flowchart.
 */
public class Excuse {

    // The 3 possible states of an excuse
    public enum Status {
        PENDING,
        APPROVED,
        REJECTED
    }

    private int id;              // database id (0 until saved)
    private String studentId;    // who submitted the excuse
    private LocalDate dateFiled; // when it was submitted
    private String reason;       // the student's typed reason
    private Status status;       // starts as PENDING until instructor acts on it

    // Used when a student submits a NEW excuse
    public Excuse(String studentId, LocalDate dateFiled, String reason) {
        this.studentId = studentId;
        this.dateFiled = dateFiled;
        this.reason = reason;
        this.status = Status.PENDING; // every new excuse starts as "Excuse Pending"
    }

    // Used when loading an existing excuse from the database
    public Excuse(int id, String studentId, LocalDate dateFiled, String reason, Status status) {
        this.id = id;
        this.studentId = studentId;
        this.dateFiled = dateFiled;
        this.reason = reason;
        this.status = status;
    }

    // --- Getters ---
    public int getId() {
        return id;
    }

    public String getStudentId() {
        return studentId;
    }

    public LocalDate getDateFiled() {
        return dateFiled;
    }

    public String getReason() {
        return reason;
    }

    public Status getStatus() {
        return status;
    }

    // --- Setters ---
    public void setId(int id) {
        this.id = id;
    }

    // Used by "Update Excuse Status" (Approve / Reject)
    public void setStatus(Status status) {
        this.status = status;
    }
}