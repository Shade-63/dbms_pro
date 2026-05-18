package com.hospital.bedalloc.service;

import com.hospital.bedalloc.dao.AdmissionDAO;
import java.sql.SQLException;

public class DischargeService {
    private AdmissionDAO admissionDAO = new AdmissionDAO();

    public String dischargePatient(int admissionId, boolean maintenanceNeeded, String maintenanceReason) throws SQLException {
        return admissionDAO.dischargePatient(admissionId, maintenanceNeeded, maintenanceReason);
    }
}
