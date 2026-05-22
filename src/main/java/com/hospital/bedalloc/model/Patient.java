package com.hospital.bedalloc.model;

import java.sql.Timestamp;

public class Patient {
    private int patientId;
    private String name;
    private int age;
    private String bloodGroup;
    private String contact;
    private String address;
    private String emergencyContact;
    private Timestamp registeredDate;

    public Patient() {}

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    public Timestamp getRegisteredDate() { return registeredDate; }
    public void setRegisteredDate(Timestamp registeredDate) { this.registeredDate = registeredDate; }

    @Override
    public String toString() {
        return name + " (" + contact + ")";
    }
}
