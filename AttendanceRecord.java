package model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * AttendanceRecord.java
 * -----------------------------------------------------
 * Represents ONE attendance entry for ONE student on ONE day.
 * Matches the "Time In" -> "Attendance Status" -> "Time Out" ->
 * "Save Record" steps in your flowchart.
 */
public class AttendanceRecord {

    // The 4 possible statuses shown in your flowchart's "Attendance Status" box
    public enum Status {
        PRESENT,
        LATE,
        ABSENT,
        EXCUSED
    }

    private String studentId;      // which student this record belongs to
    private LocalDate date;        // the day of this attendance
    private LocalTime timeIn;      // set when instructor clicks "Time In"
    private LocalTime timeOut;     // set when instructor clicks "Time Out" (can be null if not yet timed out)
    private Status status;         // Present / Late / Absent

    // Formatter used to display time nicely, e.g. "08:15 AM"
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");

    public AttendanceRecord(String studentId, LocalDate date, LocalTime timeIn, Status status) {
        this.studentId = studentId;
        this.date = date;
        this.timeIn = timeIn;
        this.status = status;
        this.timeOut = null; // no time out yet when the record is first created
    }

    // --- Getters ---
    public String getStudentId() {
        return studentId;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTimeIn() {
        return timeIn;
    }

    public LocalTime getTimeOut() {
        return timeOut;
    }

    public Status getStatus() {
        return status;
    }

    // --- Setters used by "Time Out" and status changes ---
    public void setTimeOut(LocalTime timeOut) {
        this.timeOut = timeOut;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    // Helper methods so the UI code doesn't need to format dates/times itself
    public String getTimeInFormatted() {
        return timeIn == null ? "--" : timeIn.format(TIME_FORMAT);
    }

    public String getTimeOutFormatted() {
        return timeOut == null ? "--" : timeOut.format(TIME_FORMAT);
    }
}
