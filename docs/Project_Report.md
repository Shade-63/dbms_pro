# Project Report: Hospital Bed Allocation System

## 1. Introduction
The **Hospital Bed Allocation System** is a Java Swing-based desktop application designed for medical facility administrators. It provides a robust interface for managing bed inventory, patient admissions, discharges, and waitlists. The project emphasizes core DBMS concepts including normalization, transaction management, and automated business rules via database triggers.

## 2. Database Design & Normalization

### 2.1 Entity-Relationship Diagram (ERD)
The system is built on a relational schema with 8 normalized tables:
- **wards**: Stores ward types, floors, and charges.
- **beds**: Tracks individual bed availability and equipment.
- **patients**: Central registry for patient details.
- **staff**: Registry for doctors, nurses, and admins.
- **admissions**: Transactional records for patient-bed-doctor associations.
- **waitlist**: Priority queue for patients when beds are unavailable.
- **bed_maintenance_log**: Logs for cleaning and repairs.
- **bed_status_audit**: Automated history of every bed status change.

### 2.2 Normalization Trace
- **UNF to 1NF:** Removed repeating groups (e.g., medical history) and multi-valued attributes (e.g., equipment lists).
- **1NF to 2NF:** Removed partial dependencies by separating ward details from bed records and staff details from admission records.
- **2NF to 3NF/BCNF:** Eliminated transitive dependencies. Ward charges depend on the `ward_type`, not the `bed_id`. The final schema satisfies BCNF as every determinant is a candidate key.

## 3. Database Implementation (Advanced Concepts)

### 3.1 Triggers (Automated Business Rules)
- `trg_prevent_double_active_admission`: Enforces that a bed can have only one active patient at any time.
- `trg_bed_occupied_on_admit`: Automatically updates bed status to 'Occupied' upon admission.
- `trg_bed_available_on_discharge`: Frees the bed automatically when a patient is discharged.
- `trg_enforce_ward_capacity`: Prevents admissions if the specific ward's capacity is reached.
- `trg_audit_bed_status_update`: Maintains a permanent audit trail of status changes for compliance.

### 3.2 Stored Procedures & Cursors
- `sp_allocate_bed`: An atomic transaction that selects an available bed (using `FOR UPDATE` locking) and creates an admission record.
- `sp_discharge_patient`: Uses a **CURSOR** to scan the waitlist immediately after a patient is discharged, auto-allocating the bed to the highest-priority patient.
- `sp_process_waitlist`: A batch operation utilizing a loop and cursor to allocate multiple beds to waiting patients.

### 3.3 Views (Simplified Reporting)
- `vw_current_occupancy`: Real-time analytics on bed utilization rates per ward.
- `vw_active_admissions`: A 5-table join providing a complete snapshot of current patients.
- `vw_bed_turnover`: Historical data on average length of stay.

## 4. Application Implementation

### 4.1 Architecture
The system follows a **Layered Architecture**:
1.  **Presentation Layer:** Java Swing (JFrames, JPanels, Custom Renderers).
2.  **Service Layer:** Business logic orchestration and transaction coordination.
3.  **DAO Layer:** Data Access Objects using **Pure JDBC** (PreparedStatement, CallableStatement) for direct SQL interaction.
4.  **Data Layer:** MySQL 8.0 with InnoDB for ACID compliance.

### 4.2 Key Features
- **Color-Coded Dashboard:** Real-time visual feedback on bed statuses.
- **Priority-Based Waitlist:** Automated calculation of patient priority based on request time and severity.
- **Responsive UI:** Utilizes `SwingWorker` threads to ensure DB operations never block the user interface.
- **Security:** SQL Injection prevention via Parameterized Queries and basic role-based access (Receptionist/Admin).

## 5. Testing & Validation
- **Integration Testing:** Verified end-to-end flows (Admission -> Discharge -> Auto-Allocation).
- **Constraint Testing:** Confirmed that triggers correctly reject double-booking and capacity overages.
- **Concurrent Access:** Tested transaction isolation levels (REPEATABLE READ) to ensure data integrity during simultaneous updates.

## 6. Conclusion
The Hospital Bed Allocation System successfully demonstrates how advanced DBMS features can be integrated with a desktop application to create a scalable, reliable, and transaction-safe management tool.
