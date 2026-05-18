# Hospital Bed Allocation System (DBMS Course Project)

[![Java Version](https://img.shields.io/badge/Java-17%2B-blue.svg)](https://www.oracle.com/java/technologies/javase-jdk17-downloads.html)
[![MySQL Version](https://img.shields.io/badge/MySQL-8.0%2B-orange.svg)](https://dev.mysql.com/downloads/mysql/)
[![Maven](https://img.shields.io/badge/Build-Maven-red.svg)](https://maven.apache.org/)

A robust, transactional desktop application for managing hospital bed inventory, patient admissions, and priority-based waitlists. Built as a college DBMS project to demonstrate advanced relational database concepts.

## 🚀 Key Features

*   **Real-time Dashboard:** Color-coded grid showing Available (Green), Occupied (Red), and Maintenance (Yellow) beds.
*   **Atomic Admissions:** Transaction-safe patient admission logic that updates bed status and records in one operation.
*   **Priority Waitlist:** Automated patient queue that auto-allocates freed beds to the highest-priority waiting patient.
*   **Comprehensive Reporting:** Integrated views for Occupancy rates, Bed turnover analytics, and Doctor workload.
*   **Audit Logging:** Automated database-level auditing of every bed status change.
*   **Modern UI:** Clean, responsive Java Swing interface with Toast notifications and background task processing.

## 🛠 Tech Stack

*   **Frontend:** Java Swing (Desktop App)
*   **Backend:** Pure JDBC (PreparedStatement, CallableStatement)
*   **Database:** MySQL 8.0+ (InnoDB Engine)
*   **Build Tool:** Maven
*   **Design Pattern:** DAO (Data Access Object) & Service Layer

## 🗄️ Database Architecture (DBMS Focus)

This project emphasizes database-level logic over application-level code:
*   **Normalization:** Fully normalized schema (3NF/BCNF) across 8 tables.
*   **Triggers (6):** Enforce business rules like preventing double-booking and auto-updating statuses.
*   **Stored Procedures (4):** Handle complex transactional logic using SQL Cursors.
*   **Views (6):** Simplify complex multi-table joins for dashboard and reports.
*   **Transactions:** ACID compliant operations with `REPEATABLE READ` isolation.

## ⚙️ Installation & Setup

### 1. Database Initialization
1.  Ensure MySQL 8.0+ is running.
2.  Run the scripts in the `/sql` directory in order:
    ```bash
    mysql -u root -p < sql/01_schema.sql
    mysql -u root -p hospital_bed_db < sql/02_constraints.sql
    mysql -u root -p hospital_bed_db < sql/03_indexes.sql
    mysql -u root -p hospital_bed_db < sql/04_triggers.sql
    mysql -u root -p hospital_bed_db < sql/05_procedures.sql
    mysql -u root -p hospital_bed_db < sql/06_views.sql
    mysql -u root -p hospital_bed_db < sql/07_seed_data.sql
    ```

### 2. Configuration
Create/Edit `src/main/resources/config.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/hospital_bed_db?useSSL=false&serverTimezone=UTC
db.user=your_username
db.password=your_password
```

### 3. Build & Run
```bash
# Compile and package
mvn clean compile

# Launch the Application
mvn exec:java
```

## 🔐 Demo Credentials
*   **Username:** `admin`
*   **Password:** `password`

## 📁 Project Structure
```text
D:\dbmss\
├── sql/                # SQL Initialization scripts (01-08)
├── src/main/java/      # Java source code
│   └── com/hospital/bedalloc/
│       ├── dao/        # Data Access Objects (JDBC)
│       ├── db/         # Connection management
│       ├── gui/        # Swing UI & Components
│       ├── model/      # POJO Entities
│       └── service/    # Business Logic
├── src/main/resources/ # Configuration files
├── src/test/java/      # Integration Test Suite
└── docs/               # Technical Report & Documentation
```

## 🧪 Testing
Run the automated integration tests to verify the core DBMS workflows:
```bash
mvn test
```

## 📜 License
This project was created for educational purposes. Feel free to use it for your own learning or college submissions.

---
*Developed for the DBMS Course Project Requirement.*
