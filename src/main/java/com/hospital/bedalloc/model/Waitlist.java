package com.hospital.bedalloc.model;

import java.sql.Timestamp;

public class Waitlist {
    private int waitlistId;
    private int patientId;
    private String requestedWardType;
    private int priorityScore;
    private Timestamp requestTime;
    private String status;
    private String notes;

    // For display
    private String patientName;

    public Waitlist() {}

    public int getWaitlistId() { return waitlistId; }
    public void setWaitlistId(int waitlistId) { this.waitlistId = waitlistId; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public String getRequestedWardType() { return requestedWardType; }
    public void setRequestedWardType(String requestedWardType) { this.requestedWardType = requestedWardType; }

    public int getPriorityScore() { return priorityScore; }
    public void setPriorityScore(int priorityScore) { this.priorityScore = priorityScore; }

    public Timestamp getRequestTime() { return requestTime; }
    public void setRequestTime(Timestamp requestTime) { this.requestTime = requestTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
}
