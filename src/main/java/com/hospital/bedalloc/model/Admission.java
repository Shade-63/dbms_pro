package com.hospital.bedalloc.model;

import java.sql.Date;
import java.sql.Timestamp;

public class Admission {
    private int admissionId;
    private int patientId;
    private int bedId;
    private Integer doctorId;
    private Timestamp admissionDate;
    private Date expectedDischarge;
    private Timestamp actualDischarge;
    private String status;
    private String notes;

    // For display
    private String patientName;
    private String bedNumber;
    private String doctorName;

    public Admission() {}

    public int getAdmissionId() { return admissionId; }
    public void setAdmissionId(int admissionId) { this.admissionId = admissionId; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public int getBedId() { return bedId; }
    public void setBedId(int bedId) { this.bedId = bedId; }

    public Integer getDoctorId() { return doctorId; }
    public void setDoctorId(Integer doctorId) { this.doctorId = doctorId; }

    public Timestamp getAdmissionDate() { return admissionDate; }
    public void setAdmissionDate(Timestamp admissionDate) { this.admissionDate = admissionDate; }

    public Date getExpectedDischarge() { return expectedDischarge; }
    public void setExpectedDischarge(Date expectedDischarge) { this.expectedDischarge = expectedDischarge; }

    public Timestamp getActualDischarge() { return actualDischarge; }
    public void setActualDischarge(Timestamp actualDischarge) { this.actualDischarge = actualDischarge; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getBedNumber() { return bedNumber; }
    public void setBedNumber(String bedNumber) { this.bedNumber = bedNumber; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
}
