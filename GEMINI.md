# Hospital Bed Allocation System
## Project Context for AI Assistant

**Type:** College DBMS Course Project  
**Stack:** Java Swing + JDBC + MySQL 8.0+  
**Goal:** Build a desktop hospital bed management app that demonstrates core DBMS concepts through functional code.

---

## 1. What to Build

A single-user desktop application (Java Swing) that lets hospital staff:
- View all beds in a color-coded grid (Green=Available, Red=Occupied, Yellow=Maintenance)
- Admit patients by allocating available beds atomically
- Discharge patients and auto-allocate freed beds to waitlisted patients
- Manage a priority-based waitlist
- View occupancy reports and bed turnover analytics
- Administer wards, beds, and staff

The database is the star of this project — not the GUI. Every major feature must demonstrate a DBMS concept.

---

## 2. Tech Stack

```
Frontend:     Java Swing (desktop, not web)
Backend:      Pure JDBC (no Hibernate/JPA — professors want to see SQL)
Database:     MySQL 8.0+ (InnoDB engine required for transactions)
Build:        Maven
JDK:          17+
```

**Critical rule:** No Spring Boot, no web frameworks, no ORM. Raw JDBC + CallableStatement for stored procedures.

---

## 3. Architecture

```
Swing GUI (JFrame/JPanel/JDialog)
    ↓ ActionListener
Service Layer (BedAllocationService, DischargeService)
    ↓ calls
DAO Layer (WardDAO, BedDAO, PatientDAO, AdmissionDAO, WaitlistDAO, StaffDAO, ReportDAO)
    ↓ JDBC
MySQL Database (Tables, Triggers, Procedures, Views, Indexes)
```

**Patterns:**
- DAO pattern for all database access
- Singleton for DBConnection
- SwingWorker for ALL database calls (never block EDT)
- Custom TableCellRenderer for color-coded bed status

---

## 4. Database Schema (8 Tables, 3NF/BCNF)

### Tables
1. **wards** — ward_id, ward_type (General/ICU/Emergency/Private), floor, capacity, daily_charge
2. **beds** — bed_id, ward_id (FK), bed_number, equipment_status, current_status (Available/Occupied/Maintenance)
3. **patients** — patient_id, name, age, blood_group, contact, address, emergency_contact
4. **staff** — staff_id, name, role (Doctor/Nurse/Admin), department, phone
5. **admissions** — admission_id, patient_id (FK), bed_id (FK), doctor_id (FK), admission_date, expected_discharge, actual_discharge, status (Active/Discharged/Transferred)
6. **waitlist** — waitlist_id, patient_id (FK), requested_ward_type, priority_score, request_time, status (Waiting/Allocated/Cancelled)
7. **bed_maintenance_log** — log_id, bed_id (FK), start_time, end_time, reason, resolved_by (FK), status
8. **bed_status_audit** — audit_id, bed_id (FK), old_status, new_status, changed_at, changed_by, reason

### Normalization Proof
The project MUST document the normalization trace:
- Start with one UNF monster table (patient+bed+ward+doctor+medicines all in one row with repeating groups)
- Show decomposition to 1NF (atomic values, no repeating groups)
- Show decomposition to 2NF (remove partial dependencies: bed details depend on bed_id, not admission_id)
- Show decomposition to 3NF (remove transitive dependencies: ward charge depends on ward_type, not bed_id)
- Verify BCNF (every determinant is a candidate key)

### Constraints to Implement
- PRIMARY KEY on every table
- FOREIGN KEY with ON DELETE RESTRICT/CASCADE/SET NULL as appropriate
- CHECK constraints for: status enums, age range (0-150), positive capacity/charge, valid blood groups
- UNIQUE constraints: bed_number per ward, patient contact, ward_type+floor combination
- NOT NULL on critical fields

### Triggers (6 total)
1. `trg_prevent_double_active_admission` — BEFORE INSERT on admissions: rejects if bed already has Active admission (assertion equivalent)
2. `trg_audit_bed_status_update` — AFTER UPDATE on beds: logs every status change to bed_status_audit
3. `trg_bed_occupied_on_admit` — AFTER INSERT on admissions: auto-updates beds.current_status to 'Occupied'
4. `trg_bed_available_on_discharge` — AFTER UPDATE on admissions: auto-frees bed when status changes to 'Discharged'
5. `trg_prevent_maint_occupied` — BEFORE INSERT on bed_maintenance_log: rejects if bed is Occupied
6. `trg_enforce_ward_capacity` — BEFORE INSERT on admissions: rejects if ward already at capacity

### Stored Procedures (4 total, must use cursors)
1. `sp_allocate_bed(IN patient_id, ward_type, doctor_id, expected_discharge, OUT bed_id, admission_id, success, message)`
   - START TRANSACTION
   - SELECT ... FOR UPDATE to lock available bed row
   - INSERT admission, UPDATE bed status
   - If no bed: INSERT into waitlist instead
   - COMMIT or ROLLBACK

2. `sp_discharge_patient(IN admission_id, maintenance_needed, OUT message)`
   - START TRANSACTION
   - UPDATE admission to 'Discharged'
   - If maintenance: bed→Maintenance + INSERT maintenance_log
   - If available: OPEN CURSOR on waitlist for matching ward type
   - FETCH highest priority patient, auto-admit if found
   - COMMIT

3. `sp_process_waitlist(OUT allocated_count, remaining_count)`
   - DECLARE CURSOR for all Waiting patients ordered by priority_score DESC, request_time ASC
   - LOOP through cursor: for each, find available bed in requested ward type
   - If found: allocate, UPDATE waitlist→Allocated, INSERT admission, UPDATE bed→Occupied
   - Count results and return

4. `sp_update_waitlist_priority()` — recalculates priority scores based on waiting time

### Views (6 total)
1. `vw_current_occupancy` — JOIN wards+beds, GROUP BY ward_type, COUNT totals/occupied/available/maintenance, calculate occupancy_rate
2. `vw_active_admissions` — 5-table JOIN (admissions+patients+beds+wards+staff), WHERE status='Active', calculate days_admitted
3. `vw_available_beds` — JOIN beds+wards, WHERE current_status='Available', show remaining capacity
4. `vw_waitlist_details` — JOIN waitlist+patients, WHERE status='Waiting', calculate waiting_hours, ORDER BY priority_score DESC
5. `vw_bed_turnover` — JOIN admissions+beds+wards, WHERE status='Discharged', AVG/MIN/MAX stay duration, last 30 days
6. `vw_doctor_workload` — JOIN staff+admissions+beds+wards, WHERE role='Doctor', COUNT current_patients, GROUP_CONCAT assigned wards

### Indexes
- idx_bed_status (beds.current_status)
- idx_bed_ward_status (beds.ward_id, current_status)
- idx_adm_status (admissions.status)
- idx_adm_bed_active (admissions.bed_id, status)
- idx_waitlist_priority (waitlist.priority_score DESC, request_time ASC)
- idx_waitlist_status (waitlist.status, requested_ward_type)
- idx_patient_name (patients.name)
- idx_audit_bed (bed_status_audit.bed_id, changed_at DESC)
- idx_maint_bed (bed_maintenance_log.bed_id, start_time DESC)

---

## 5. Java Implementation Details

### Package Structure
```
com.hospital.bedalloc
├── db/
│   ├── DBConnection.java          // Singleton, getConnection(), beginTransaction(), commit(), rollback()
│   └── DatabaseConfig.java        // Reads config.properties for DB credentials
├── model/
│   ├── Ward.java, Bed.java, Patient.java, Staff.java
│   ├── Admission.java, Waitlist.java, BedStatusAudit.java
├── dao/
│   ├── WardDAO.java               // CRUD + getAll()
│   ├── BedDAO.java                // getAllBedsWithWard(), findAvailableBed(wardType), updateStatus(), getStatistics()
│   ├── PatientDAO.java            // insert(), searchByName(), getById()
│   ├── AdmissionDAO.java          // allocateBed() calls CallableStatement for sp_allocate_bed
│   │                              // dischargePatient() calls sp_discharge_patient
│   │                              // getActiveAdmissions(), getPatientHistory()
│   ├── WaitlistDAO.java           // getWaitlist(), updatePriority(), removeFromWaitlist()
│   ├── StaffDAO.java              // getDoctors(), getAllStaff(), insert()
│   └── ReportDAO.java             // getOccupancyReport(), getTurnoverReport(), getDoctorWorkload()
├── service/
│   ├── BedAllocationService.java  // Orchestrates allocation logic, handles transaction coordination
│   └── DischargeService.java      // Orchestrates discharge + waitlist check
├── gui/
│   ├── MainFrame.java             // JSplitPane: sidebar (220px) + main content
│   ├── LoginFrame.java            // Centered card, role toggle (Receptionist/Admin)
│   ├── DashboardPanel.java        // Stats cards (4) + bed grid (responsive 4-6 cols) + ward filter tabs
│   ├── AdmitPatientDialog.java    // JDialog, 2-col form, validation, "Admit" or "Add to Waitlist"
│   ├── DischargeDialog.java       // JDialog, patient summary, discharge notes, maintenance checkbox
│   ├── WaitlistPanel.java         // JTable with priority colors, "Allocate" / "Remove" buttons
│   ├── PatientManagerFrame.java   // Search + JTable of patients
│   ├── ReportsFrame.java          // JTabbedPane: Occupancy, Turnover, Doctor Workload
│   ├── AdminPanel.java            // JTabbedPane: Wards, Beds, Staff, Maintenance Log
│   └── components/
│       ├── BedStatusRenderer.java // extends DefaultTableCellRenderer, color by status
│       ├── StatusBadge.java       // JPanel pill shape with background color
│       ├── ToastNotification.java // JWindow, auto-dismiss 4s, slide-in from top-right
│       └── StatCard.java          // JPanel with icon circle + big number + label
└── util/
    ├── DateUtil.java              // Formatting, parsing, date arithmetic
    ├── ValidationUtil.java        // Field validators (not empty, regex, range checks)
    └── SwingWorkerUtil.java       // Helper to execute DB calls in background
```

### Critical Code Patterns

**DBConnection.java** — Must handle transactions:
```java
public class DBConnection {
    private static Connection connection;
    private static final String URL = "jdbc:mysql://localhost:3306/hospital_bed_db?useSSL=false&serverTimezone=UTC";

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASS);
        }
        return connection;
    }

    public static void beginTransaction() throws SQLException {
        getConnection().setAutoCommit(false);
        getConnection().setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
    }

    public static void commit() throws SQLException {
        getConnection().commit();
        getConnection().setAutoCommit(true);
    }

    public static void rollback() {
        try {
            getConnection().rollback();
            getConnection().setAutoCommit(true);
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
```

**Calling Stored Procedure** (AdmissionDAO):
```java
public AllocationResult allocateBed(int patientId, String wardType, int doctorId, Date expectedDischarge) {
    AllocationResult result = new AllocationResult();
    try (Connection conn = DBConnection.getConnection();
         CallableStatement stmt = conn.prepareCall("{CALL sp_allocate_bed(?, ?, ?, ?, ?, ?, ?, ?, ?)}")) {

        stmt.setInt(1, patientId);
        stmt.setString(2, wardType);
        stmt.setInt(3, doctorId);
        stmt.setDate(4, expectedDischarge);
        stmt.setNull(5, Types.VARCHAR); // notes

        stmt.registerOutParameter(6, Types.INTEGER);  // bed_id
        stmt.registerOutParameter(7, Types.INTEGER);  // admission_id
        stmt.registerOutParameter(8, Types.BOOLEAN);   // success
        stmt.registerOutParameter(9, Types.VARCHAR);  // message

        stmt.execute();

        result.setBedId(stmt.getInt(6));
        result.setAdmissionId(stmt.getInt(7));
        result.setSuccess(stmt.getBoolean(8));
        result.setMessage(stmt.getString(9));

    } catch (SQLException e) {
        result.setSuccess(false);
        result.setMessage("Error: " + e.getMessage());
    }
    return result;
}
```

**SwingWorker Pattern** (DashboardPanel):
```java
private void loadBeds() {
    SwingWorker<List<Bed>, Void> worker = new SwingWorker<>() {
        @Override
        protected List<Bed> doInBackground() throws Exception {
            return bedDAO.getAllBedsWithWard();
        }

        @Override
        protected void done() {
            try {
                List<Bed> beds = get();
                // Update GUI on EDT
                refreshBedGrid(beds);
            } catch (Exception e) {
                ToastNotification.showError("Failed to load beds: " + e.getMessage());
            }
        }
    };
    worker.execute();
}
```

**Custom Table Renderer** (BedStatusRenderer):
```java
public class BedStatusRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, 
            boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(...);
        String status = (String) value;
        switch(status) {
            case "Available":   c.setBackground(new Color(16, 185, 129)); break;   // Green
            case "Occupied":    c.setBackground(new Color(239, 68, 68)); break;   // Red
            case "Maintenance": c.setBackground(new Color(245, 158, 11)); break; // Yellow
        }
        c.setForeground(Color.WHITE);
        return c;
    }
}
```

---

## 6. GUI Screens (7 Required)

1. **LoginFrame** — 400×500, centered card, role toggle, username/password, shake on error
2. **DashboardPanel** (MainFrame default) — Stats row (4 cards), ward filter pills, bed grid (180×140 cards with colored top strip), sidebar navigation
3. **AdmitPatientDialog** (modal, 600×650) — 2-column form: patient info + admission details + notes, validation, "Admit" or "Add to Waitlist"
4. **DischargeDialog** (modal, 500×450) — Patient summary card, discharge notes, maintenance checkbox, confirmation overlay, danger-style confirm button
5. **WaitlistPanel** — JTable with priority color coding (red>500, orange 300-500, blue<300), Allocate/Remove buttons, auto-refresh countdown
6. **ReportsFrame** — JTabbedPane with 3 tabs: Occupancy (vw_current_occupancy), Turnover (vw_bed_turnover), Doctor Workload (vw_doctor_workload)
7. **AdminPanel** — JTabbedPane: Wards (CRUD table), Beds (filter+table), Staff (CRUD), Maintenance Log (table with date filters)

**Color Scheme:**
- Background: `#F9FAFB` (light gray)
- Cards: White with `#E5E7EB` border, 8px radius
- Primary: `#2563EB` (blue buttons)
- Success: `#10B981` (green badges)
- Danger: `#EF4444` (red badges/buttons)
- Warning: `#F59E0B` (yellow badges)
- Text: `#111827` (headings), `#4B5563` (body), `#9CA3AF` (placeholders)

---

## 7. Key DBMS Concepts to Demonstrate

| Concept | Where It's Shown |
|---|---|
| **Normalization** | Project report: UNF→1NF→2NF→3NF→BCNF trace with FDs |
| **ER Diagram** | Crow's Foot notation in report, 1:N and M:N relationships |
| **Primary/Foreign Keys** | Every table has PK; beds→wards, admissions→beds/patients/staff |
| **CHECK Constraints** | Status enums, age range, positive numbers, regex patterns |
| **UNIQUE Constraints** | bed_number per ward, patient contact, ward_type+floor |
| **Triggers** | 6 triggers enforcing business rules + audit logging |
| **Stored Procedures** | 4 procedures encapsulating complex logic |
| **Cursors** | sp_discharge_patient and sp_process_waitlist iterate waitlist |
| **Views** | 6 views for reporting (complex JOINs + aggregations) |
| **Indexes** | 9 indexes for performance, documented in report |
| **Transactions** | JDBC setAutoCommit(false), commit/rollback in allocation/discharge |
| **Isolation Levels** | REPEATABLE READ for allocation, SERIALIZABLE for batch |
| **Joins** | INNER JOIN (active admissions), LEFT JOIN (all beds), aggregate JOINs (reports) |
| **Assertions** | Implemented via triggers (MySQL doesn't support CREATE ASSERTION) |

---

## 8. Testing Requirements

### Must Test
1. Admit patient to available bed → bed turns Occupied in DB + GUI
2. Admit patient when ward full → added to waitlist
3. Discharge patient with no waitlist → bed becomes Available
4. Discharge patient with waitlist → auto-allocated to highest priority
5. Two users click same available bed simultaneously → one succeeds, one gets error
6. Try to double-book occupied bed → trigger rejects with SQLSTATE 45000
7. Try maintenance on occupied bed → trigger rejects
8. Delete ward with beds → FK RESTRICT prevents deletion
9. Generate occupancy report → percentages match manual count
10. Process waitlist batch → cursor allocates multiple patients

### Test Data (Seed)
- 4 wards: General (floor 1, 10 beds), ICU (floor 2, 5 beds), Emergency (floor 1, 8 beds), Private (floor 3, 6 beds)
- 20 beds total
- 7 staff (4 doctors, 2 nurses, 1 admin)
- 6 patients
- 4 active admissions
- 2 waitlisted patients

---

## 9. Build Instructions

### Prerequisites
- JDK 17+
- MySQL 8.0+ running locally
- Maven 3.9+
- MySQL Workbench (for schema design/testing)

### Setup Steps
1. Create database: `CREATE DATABASE hospital_bed_db;`
2. Run SQL scripts in order:
   ```bash
   mysql -u root -p hospital_bed_db < sql/01_schema.sql
   mysql -u root -p hospital_bed_db < sql/02_constraints.sql
   mysql -u root -p hospital_bed_db < sql/03_indexes.sql
   mysql -u root -p hospital_bed_db < sql/04_triggers.sql
   mysql -u root -p hospital_bed_db < sql/05_procedures.sql
   mysql -u root -p hospital_bed_db < sql/06_views.sql
   mysql -u root -p hospital_bed_db < sql/07_seed_data.sql
   ```
3. Update `src/main/resources/config.properties` with DB credentials
4. Build: `mvn clean compile`
5. Run: `mvn exec:java -Dexec.mainClass="com.hospital.bedalloc.gui.MainFrame"`
6. Or build JAR: `mvn clean package` → run `target/HospitalBedSystem-1.0.jar`

---

## 10. Project Deliverables

| File | Description |
|---|---|
| `pom.xml` | Maven build config with mysql-connector-j dependency |
| `sql/01_schema.sql` | CREATE TABLE for all 8 tables |
| `sql/02_constraints.sql` | CHECK, FK, UNIQUE, NOT NULL |
| `sql/03_indexes.sql` | Performance indexes |
| `sql/04_triggers.sql` | 6 triggers |
| `sql/05_procedures.sql` | 4 stored procedures with cursors |
| `sql/06_views.sql` | 6 reporting views |
| `sql/07_seed_data.sql` | Sample data for testing |
| Java source | All packages: db, model, dao, service, gui, util |
| `docs/er_diagram.png` | From MySQL Workbench |
| `docs/normalization.pdf` | UNF to BCNF trace with FDs |
| `docs/project_report.pdf` | Complete report with screenshots |
| `README.md` | Setup and run instructions |

---

## 11. Common Pitfalls to Avoid

1. **Don't use Hibernate/JPA** — Professors want to see raw SQL, PreparedStatement, CallableStatement
2. **Don't run DB queries on EDT** — Always use SwingWorker, GUI will freeze otherwise
3. **Don't forget FOR UPDATE** — In sp_allocate_bed, without row locking, concurrent users will double-book
4. **Don't skip transaction handling** — Allocation must be atomic: SELECT bed + INSERT admission + UPDATE bed status
5. **Don't hardcode DB credentials** — Use config.properties, document in README
6. **Don't make GUI too complex** — Functional and clean is better than over-engineered; focus energy on database
7. **Don't skip seed data** — Empty app is hard to demo; populate realistic data
8. **Don't forget audit trail** — bed_status_audit table proves you understand logging

---

## 12. Future Scope (For Report Conclusion)

- Multi-hospital deployment with centralized database
- Web interface using Spring Boot + React
- SMS/email notifications for waitlist patients
- IoT bed sensors for automatic occupancy detection
- Machine learning for bed demand prediction
- Integration with hospital billing and EMR systems
- Role-based access control with LDAP/Active Directory
- Mobile app for nurses to update bed status

---

*This document serves as the single source of truth for building the Hospital Bed Allocation System. All implementation decisions should align with the DBMS-first philosophy demonstrated above.*
