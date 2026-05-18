package com.hospital.bedalloc.service;

import com.hospital.bedalloc.dao.AdmissionDAO;
import com.hospital.bedalloc.dao.PatientDAO;
import com.hospital.bedalloc.model.Patient;
import java.sql.Date;
import java.sql.SQLException;

public class BedAllocationService {
    private PatientDAO patientDAO = new PatientDAO();
    private AdmissionDAO admissionDAO = new AdmissionDAO();

    public AdmissionDAO.AllocationResult admitPatient(Patient patient, String wardType, Integer doctorId, Date expectedDischarge, String notes) throws SQLException {
        // 1. Check if patient exists or insert new
        Patient existingPatient = patientDAO.getPatientByContact(patient.getContact());
        int patientId;
        if (existingPatient != null) {
            patientId = existingPatient.getPatientId();
        } else {
            patientId = patientDAO.insertPatient(patient);
        }

        // 2. Call stored procedure for atomic allocation
        return admissionDAO.allocateBed(patientId, wardType, doctorId, expectedDischarge, notes);
    }
}
