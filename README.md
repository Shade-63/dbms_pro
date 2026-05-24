# Hospital Bed Allocation System (DBMS Course Project)

[![Java Version](https://img.shields.io/badge/Java-17%2B-blue.svg)](https://www.oracle.com/java/technologies/javase-jdk17-downloads.html)
[![MySQL Version](https://img.shields.io/badge/MySQL-8.0%2B-orange.svg)](https://dev.mysql.com/downloads/mysql/)
[![Maven](https://img.shields.io/badge/Build-Maven-red.svg)](https://maven.apache.org/)

A robust, transaction-safe desktop application built in **Java Swing** and powered by **Pure JDBC** and a **MySQL 8.0+** relational database. This system is designed as a college DBMS project to showcase core database management concepts (normalization, integrity constraints, stored procedures, cursors, triggers, views, indexing, and transaction isolation) in a practical hospital environment.

---

## 🗄️ Database Architecture & Design

The backend database represents the core of the project. It handles all critical business logic, data validation, audit logs, and automatic allocations.

### 1. Relational Schema (8 Tables)
The database schema consists of 8 tables structured to manage wards, beds, patients, staff, admissions, waitlist priority queues, maintenance logs, and bed audits:

1. **[wards](file:///D:/dbmss/sql/01_schema.sql#L12)**: Records ward categories (General, ICU, Emergency, Private), floors, capacity, and daily rates.
2. **[beds](file:///D:/dbmss/sql/01_schema.sql#L21)**: Tracks individual beds, their equipment, and current status (`Available`, `Occupied`, `Maintenance`).
3. **[patients](file:///D:/dbmss/sql/01_schema.sql#L30)**: Central registry for patient details including contact and emergency information.
4. **[staff](file:///D:/dbmss/sql/01_schema.sql#L42)**: Registry of clinical and administrative staff (Doctors, Nurses, Admins).
5. **[admissions](file:///D:/dbmss/sql/01_schema.sql#L51)**: Manages active and historical hospital admissions, linking patients, beds, and doctors.
6. **[waitlist](file:///D:/dbmss/sql/01_schema.sql#L64)**: A priority queue for patients waiting for an available bed in a specific ward type.
7. **[bed_maintenance_log](file:///D:/dbmss/sql/01_schema.sql#L75)**: Logs maintenance, repair, and deep cleaning processes.
8. **[bed_status_audit](file:///D:/dbmss/sql/01_schema.sql#L86)**: Keeps a historical log of every status change a bed undergoes.

---

## 📐 Normalization & De-composition (UNF → BCNF)

To eliminate redundancies and prevent database anomalies, the schema was decomposed from an unnormalized form (UNF) to Boyce-Codd Normal Form (BCNF).

### 1. Unnormalized Form (UNF)
In UNF, the system starts with a single large table containing repeating groups and multi-valued attributes:

```text
hospital_records(patient_id, patient_name, age, contact, address, ward_type, floor, bed_number, bed_equipment, bed_status, daily_charge, doctor_id, doctor_name, doctor_role, department, admission_date, expected_discharge, actual_discharge, admission_status, medicines_prescribed)
```

**Anomalies Present in UNF:**
*   **Insertion Anomaly**: Cannot add a new ward or a new bed to the database unless there is a patient admitted to it.
*   **Deletion Anomaly**: If a patient is discharged and their admission record is deleted, all information about the ward, bed details, and assigned doctor is lost.
*   **Update Anomaly**: If the daily charge for a specific ward type (e.g., ICU) changes, we must update every single record where a patient is admitted to an ICU bed.
*   **Multi-valued Attributes**: `bed_equipment` (e.g. "Ventilator, Monitor") and `medicines_prescribed` (e.g. "Paracetamol, Antibiotic") store comma-separated values, violating atomicity.

### 2. First Normal Form (1NF)
*   **Action**: Multi-valued attributes (`bed_equipment` and `medicines_prescribed`) are extracted. All column values are made atomic.
*   **Decomposition**: Separated into core entities: `patients`, `beds`, `admissions`, `staff`, and helper assignment tables.

### 3. Second Normal Form (2NF)
*   **Action**: Removed partial dependencies. In 1NF, bed and ward details depended on parts of a composite key if admissions and beds were combined.
*   **Decomposition**: Created distinct `beds` and `wards` tables, separating bed-level details from ward-level characteristics. Extract `staff` details out of admission records.

### 4. Third Normal Form (3NF)
*   **Action**: Removed transitive dependencies.
*   **Dependency**: `ward_id` → `ward_type` → `daily_charge`. The daily charge depends transitively on the `ward_id` via `ward_type`.
*   **Decomposition**: By isolating the `wards` table, this dependency is resolved. Similarly, patient contact details depend on `patient_id`, not the admission record.

### 5. Boyce-Codd Normal Form (BCNF)
*   **Rule**: For every functional dependency $X \rightarrow Y$, the determinant $X$ must be a superkey.
*   **Verification**:
    *   `wards`: `ward_id` (PK) determines `ward_type`, `floor`, `capacity`, `daily_charge`.
    *   `beds`: `bed_id` (PK) determines `ward_id`, `bed_number`, `equipment_status`, `current_status`.
    *   `patients`: `patient_id` (PK) determines `name`, `age`, `blood_group`, `contact`, `address`. `contact` (UNIQUE) determines `patient_id`, `name`, etc.
    *   `staff`: `staff_id` (PK) determines `name`, `role`, `department`, `phone`.
    *   `admissions`: `admission_id` (PK) determines `patient_id`, `bed_id`, `doctor_id`, `admission_date`, `expected_discharge`, `status`.

Since all determinants in the functional dependencies are superkeys (Primary Keys or Unique Keys), the final schema is fully in BCNF.

---

### 🔑 Functional Dependencies (FDs)
The schema enforces the following functional dependencies across tables:

| Table | Functional Dependency | Type |
| :--- | :--- | :--- |
| **`wards`** | `ward_id` → `ward_type`, `floor`, `capacity`, `daily_charge` | PK Dependency |
| | `ward_type`, `floor` → `ward_id`, `capacity`, `daily_charge` | Unique Candidate Key |
| **`beds`** | `bed_id` → `ward_id`, `bed_number`, `equipment_status`, `current_status` | PK Dependency |
| | `ward_id`, `bed_number` → `bed_id`, `equipment_status`, `current_status` | Unique Candidate Key |
| **`patients`** | `patient_id` → `name`, `age`, `blood_group`, `contact`, `address` | PK Dependency |
| | `contact` → `patient_id`, `name`, `age`, `blood_group`, `address` | Unique Candidate Key |
| **`staff`** | `staff_id` → `name`, `role`, `department`, `phone` | PK Dependency |
| **`admissions`**| `admission_id` → `patient_id`, `bed_id`, `doctor_id`, `admission_date`, `expected_discharge`, `actual_discharge`, `status`, `notes` | PK Dependency |
| | `bed_id` + `status='Active'` → `admission_id`, `patient_id` | Enforced via Trigger |

---

## 🛡️ Database Constraints & Business Integrity

The application uses native MySQL constraints (defined in [02_constraints.sql](file:///D:/dbmss/sql/02_constraints.sql)) to guarantee referential integrity and valid domain values:

*   **Primary Keys**: Defined on every table to enforce entity integrity.
*   **Foreign Keys**:
    *   `beds(ward_id)` references `wards(ward_id)` with `ON DELETE RESTRICT` (prevents deleting a ward that currently contains beds).
    *   `admissions(patient_id)` references `patients(patient_id)` with `ON DELETE RESTRICT`.
    *   `admissions(bed_id)` references `beds(bed_id)` with `ON DELETE RESTRICT`.
    *   `admissions(doctor_id)` references `staff(staff_id)` with `ON DELETE SET NULL` (allows doctor deletion while retaining history).
*   **UNIQUE Constraints**:
    *   `uq_ward_type_floor`: Prevents duplicating a ward type on the same floor.
    *   `uq_bed_ward_number`: Enforces unique bed numbers inside each ward.
    *   `uq_patient_contact`: Prevents multiple patients from registering with the same contact number.
*   **CHECK Constraints**:
    *   `chk_ward_type`: Enforces categories `('General', 'ICU', 'Emergency', 'Private')`.
    *   `chk_patient_age`: Restricts patient age to range `[0, 150]`.
    *   `chk_blood_group`: Enforces standard blood groups.
    *   `chk_patient_contact`/`chk_staff_phone`: Restricts contact number formats via regular expression (`REGEXP '^[0-9]{10}$'`).

---

## ⚡ Triggers (Automated Rules)

A set of 6 triggers (defined in [04_triggers.sql](file:///D:/dbmss/sql/04_triggers.sql)) automate status changes, enforce complex business constraints, and log audit history directly inside the database engine:

1. **`trg_prevent_double_active_admission` (Before Insert on Admissions)**
   *   *Action*: Rejects an admission if the target bed already has an `Active` status row in the admissions table. Emulates a partial database assertion.
2. **`trg_audit_bed_status_update` (After Update on Beds)**
   *   *Action*: Automatically logs changes in bed status (e.g., `Available` → `Occupied` or `Occupied` → `Maintenance`) into the `bed_status_audit` table.
3. **`trg_bed_occupied_on_admit` (After Insert on Admissions)**
   *   *Action*: When a new `Active` admission is recorded, this trigger automatically updates the matching bed's `current_status` to `Occupied`.
4. **`trg_bed_available_on_discharge` (After Update on Admissions)**
   *   *Action*: When an admission status is changed to `Discharged`, it automatically resets the bed status to `Available`.
5. **`trg_prevent_maint_occupied` (Before Insert on Bed Maintenance Log)**
   *   *Action*: Rejects a maintenance log entry if the bed is currently occupied by a patient, protecting active admissions.
6. **`trg_enforce_ward_capacity` (Before Insert on Admissions)**
   *   *Action*: Verifies the ward's current number of active patients. If active patients match or exceed the ward capacity, it aborts the transaction with `SQLSTATE '45000'`.

---

## ⚙️ Stored Procedures & SQL Cursors

All transactional changes in the database are handled by 4 stored procedures (defined in [05_procedures.sql](file:///D:/dbmss/sql/05_procedures.sql)) that guarantee atomicity:

### 1. `sp_allocate_bed`
*   **Purpose**: Admits a patient atomically.
*   **DBMS Mechanism**: 
    1. Opens a transaction (`START TRANSACTION`).
    2. Runs `SELECT bed_id FROM beds ... FOR UPDATE` to lock the bed row, preventing concurrent allocations.
    3. If a bed is found: Inserts an `Active` record into `admissions`, marks the bed `Occupied`, and updates the waitlist status (if the patient was waiting).
    4. If no bed is found: Automatically creates a `Waiting` entry in the `waitlist` table.
    5. Commits or rolls back the transaction in case of error.

### 2. `sp_discharge_patient`
*   **Purpose**: Discharges a patient and handles automatic allocation of the freed bed.
*   **DBMS Mechanism**:
    1. Marks the admission as `Discharged` and logs the `actual_discharge` timestamp.
    2. If maintenance is flagged: Marks the bed status as `Maintenance` and logs it in the `bed_maintenance_log`.
    3. If the bed is cleared for immediate reuse: Declares a **CURSOR** to fetch the highest-priority patient waiting for that specific ward type:
       ```sql
       DECLARE waitlist_cursor CURSOR FOR
           SELECT w.waitlist_id, w.patient_id
           FROM waitlist w
           WHERE w.requested_ward_type = v_ward_type AND w.status = 'Waiting'
           ORDER BY w.priority_score DESC, w.request_time ASC LIMIT 1;
       ```
    4. If a waiting patient is found, the procedure automatically registers an active admission for them and assigns the bed, keeping the bed occupied.

### 3. `sp_process_waitlist`
*   **Purpose**: Processes the waitlist queue in a batch.
*   **DBMS Mechanism**: Uses a **CURSOR** to iterate through all waitlist records ordered by priority. For each record, it attempts to find and lock an available bed, admitting the patient if successful.

### 4. `sp_update_waitlist_priority`
*   **Purpose**: Recalculates priority scores.
*   **DBMS Mechanism**: Increments waitlist priority scores by adding 10 points for every hour the patient has been waiting (`TIMESTAMPDIFF(HOUR, request_time, NOW()) * 10`), preventing starvation in the queue.

---

## 📊 Views for Analytics & Reporting

Reporting modules query the database through 6 optimized SQL views (defined in [06_views.sql](file:///D:/dbmss/sql/06_views.sql)):

1. **`vw_current_occupancy`**: Computes bed capacity metrics per ward type, aggregation of occupied, available, and maintenance beds, and the occupancy rate percentage.
2. **`vw_active_admissions`**: Joins 5 tables (`admissions`, `patients`, `beds`, `wards`, `staff`) to present a real-time list of admitted patients, including calculations for total days admitted.
3. **`vw_available_beds`**: Filters out occupied and maintenance beds and displays remaining capacity per ward.
4. **`vw_waitlist_details`**: Joins the waitlist and patient tables, calculating wait durations dynamically (`waiting_hours` and formatted output).
5. **`vw_bed_turnover`**: Computes the average, minimum, and maximum stay durations in days per ward type for patients discharged in the last 30 days.
6. **`vw_doctor_workload`**: Joins staff, admissions, and ward tables to track the number of active patients and wards assigned to each doctor.

---

## ⚡ Indexing Strategy

To maintain sub-millisecond query execution times under load, B-Tree indexes (defined in [03_indexes.sql](file:///D:/dbmss/sql/03_indexes.sql)) are placed on columns frequently used in filtering, joining, and sorting operations:

*   **`idx_bed_status`** on `beds(current_status)`: Accelerates real-time bed searches.
*   **`idx_bed_ward_status`** on `beds(ward_id, current_status)`: Optimizes dashboard filtering by ward type.
*   **`idx_adm_status`** on `admissions(status)`: Speeds up active vs. historical lookup queries.
*   **`idx_adm_bed_active`** on `admissions(bed_id, status)`: Used by the validation trigger to check for double bookings.
*   **`idx_waitlist_priority`** on `waitlist(priority_score DESC, request_time ASC)`: Speeds up cursor sorting for highest priority waitlist allocation.
*   **`idx_waitlist_status`** on `waitlist(status, requested_ward_type)`: Accelerates queue checks during patient discharge.
*   **`idx_patient_name`** on `patients(name)`: Enhances patient search performance.

---

## 🛠️ Java Implementation Architecture

The application is structured into clearly separated layers to separate concerns:

```text
com.hospital.bedalloc
├── db/                     # DBConnection (Singleton connection, Transaction boundaries)
│   └── DatabaseConfig.java # Loads credentials safely from external config file
├── model/                  # POJO entities corresponding to database tables
├── dao/                    # JDBC data access layers using Prepared/Callable Statements
├── service/                # Transaction orchestration and allocation logic
└── gui/                    # Swing UI (Main dashboard, waitlist tables, forms, custom status renderers)
```

### 1. Concurrency and SwingWorker Threading
To prevent the Java Swing Event Dispatch Thread (EDT) from freezing during database calls, all database interaction is offloaded to background threads using `SwingWorker`:

```java
SwingWorker<List<Bed>, Void> worker = new SwingWorker<>() {
    @Override
    protected List<Bed> doInBackground() throws Exception {
        return bedDAO.getAllBedsWithWard(); // Runs on a background thread pool
    }

    @Override
    protected void done() {
        try {
            List<Bed> beds = get();
            refreshBedGrid(beds); // Updates UI on the EDT thread safely
        } catch (Exception e) {
            ToastNotification.showError("Failed to load beds: " + e.getMessage());
        }
    }
};
worker.execute();
```

### 2. Transaction Management in Java
The system coordinates multiple operations inside Java-based transaction blocks by configuring auto-commit properties on the connection:

```java
public static void beginTransaction() throws SQLException {
    getConnection().setAutoCommit(false);
    getConnection().setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
}
```

---

## 🚀 Setup & Execution

### Prerequisites
*   **JDK 17+**
*   **MySQL 8.0+**
*   **Maven 3.9+**

### 1. Database Setup
Log into your MySQL instance and run the setup scripts in sequential order:

```bash
# Create database
mysql -u root -p -e "CREATE DATABASE hospital_bed_db;"

# Execute scripts in sequence
mysql -u root -p hospital_bed_db < sql/01_schema.sql
mysql -u root -p hospital_bed_db < sql/02_constraints.sql
mysql -u root -p hospital_bed_db < sql/03_indexes.sql
mysql -u root -p hospital_bed_db < sql/04_triggers.sql
mysql -u root -p hospital_bed_db < sql/05_procedures.sql
mysql -u root -p hospital_bed_db < sql/06_views.sql
mysql -u root -p hospital_bed_db < sql/07_seed_data.sql
```

### 2. Configuration
Create a configuration file at `src/main/resources/config.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/hospital_bed_db?useSSL=false&serverTimezone=UTC
db.user=your_mysql_username
db.password=your_mysql_password
```

### 3. Build and Run
Compile and launch the application using Maven:

```bash
# Clean and compile
mvn clean compile

# Run the desktop application
mvn exec:java -Dexec.mainClass="com.hospital.bedalloc.gui.MainFrame"
```

### 4. Run Integration Tests
Run JUnit integration tests to verify database triggers, constraints, procedures, and JDBC transactions:

```bash
mvn test
```

### 🔑 Default Login Credentials
*   **Username**: `admin`
*   **Password**: `password`
