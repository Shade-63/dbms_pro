package com.hospital.bedalloc;

import com.hospital.bedalloc.dao.*;
import com.hospital.bedalloc.db.DBConnection;
import com.hospital.bedalloc.model.*;
import com.hospital.bedalloc.service.BedAllocationService;
import com.hospital.bedalloc.service.DischargeService;
import org.junit.jupiter.api.*;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WorkflowIntegrationTest {
    private static BedAllocationService allocationService;
    private static DischargeService dischargeService;
    private static BedDAO bedDAO;
    private static AdmissionDAO admissionDAO;
    private static WaitlistDAO waitlistDAO;

    @BeforeAll
    static void setup() {
        allocationService = new BedAllocationService();
        dischargeService = new DischargeService();
        bedDAO = new BedDAO();
        admissionDAO = new AdmissionDAO();
        waitlistDAO = new WaitlistDAO();
    }

    @Test
    @Order(1)
    @DisplayName("Test Full Admission Flow")
    void testAdmissionFlow() throws SQLException {
        Patient p = new Patient();
        p.setName("Test Patient");
        p.setContact("9999999999");
        p.setAge(30);

        // Admit to General ward
        AdmissionDAO.AllocationResult result = allocationService.admitPatient(
            p, "General", null, new Date(System.currentTimeMillis() + 86400000), "Test Notes"
        );

        assertTrue(result.isSuccess(), "Admission should be successful");
        assertNotNull(result.getBedId(), "Bed ID should be assigned");
        
        // Verify Bed is now Occupied
        List<Bed> beds = bedDAO.getAllBedsWithWard();
        Bed admittedBed = beds.stream().filter(b -> b.getBedId() == result.getBedId()).findFirst().orElse(null);
        assertNotNull(admittedBed);
        assertEquals("Occupied", admittedBed.getCurrentStatus());
    }

    @Test
    @Order(2)
    @DisplayName("Test Waitlist Flow when Ward is Full")
    void testWaitlistFlow() throws SQLException {
        // Find a ward type and fill it (or assume ICU has limited beds from seed)
        // We'll try to admit 10 patients to ICU (which only has 5 beds in seed data)
        for (int i = 0; i < 10; i++) {
            Patient p = new Patient();
            p.setName("Waitlist Patient " + i);
            p.setContact("888888888" + i);
            p.setAge(25);
            
            allocationService.admitPatient(p, "ICU", null, new Date(System.currentTimeMillis() + 86400000), "Waitlist test");
        }

        List<Waitlist> list = waitlistDAO.getActiveWaitlist();
        assertTrue(list.size() > 0, "Waitlist should have patients if ward is full");
    }

    @Test
    @Order(3)
    @DisplayName("Test Discharge and Auto-Allocation")
    void testDischargeAndAutoAllocation() throws SQLException {
        // 1. Ensure someone is on waitlist for ICU
        List<Waitlist> initialWaitlist = waitlistDAO.getActiveWaitlist();
        long icuWaiting = initialWaitlist.stream().filter(w -> w.getRequestedWardType().equals("ICU")).count();
        
        // 2. Find an active admission in ICU
        List<Admission> active = admissionDAO.getActiveAdmissions();
        Admission icuAdmission = active.stream()
                .filter(a -> a.getBedNumber().startsWith("ICU"))
                .findFirst().orElse(null);

        if (icuAdmission != null && icuWaiting > 0) {
            // 3. Discharge
            String msg = dischargeService.dischargePatient(icuAdmission.getAdmissionId(), false, "");
            assertTrue(msg.contains("auto-allocated"), "Bed should be auto-allocated to waitlisted patient");
            
            // 4. Verify waitlist decreased
            List<Waitlist> finalWaitlist = waitlistDAO.getActiveWaitlist();
            assertEquals(icuWaiting - 1, finalWaitlist.stream().filter(w -> w.getRequestedWardType().equals("ICU")).count());
        }
    }

    @Test
    @Order(4)
    @DisplayName("Test Maintenance Flow")
    void testMaintenanceFlow() throws SQLException {
        List<Admission> active = admissionDAO.getActiveAdmissions();
        if (!active.isEmpty()) {
            Admission a = active.get(0);
            int bedId = a.getBedId();
            
            dischargeService.dischargePatient(a.getAdmissionId(), true, "Cleaning needed");
            
            // Verify bed is in Maintenance
            List<Bed> beds = bedDAO.getAllBedsWithWard();
            Bed mBed = beds.stream().filter(b -> b.getBedId() == bedId).findFirst().orElse(null);
            assertNotNull(mBed);
            assertEquals("Maintenance", mBed.getCurrentStatus());
        }
    }
}
