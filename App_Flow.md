# Application Flow Document
## Hospital Bed Allocation System

**Version:** 1.0  
**Date:** 2026-05-17  
**Purpose:** Define complete user journeys, system flows, and state transitions

---

## 1. User Personas

### 1.1 Receptionist (Primary User)
- **Role:** Admit patients, process discharges, view bed status, manage waitlist
- **Access:** Dashboard, Admit, Discharge, Waitlist, Patients, Reports (read-only)
- **Goal:** Fast, error-free bed allocation without double-booking

### 1.2 Administrator
- **Role:** Configure wards/beds, manage staff, view all reports, handle maintenance
- **Access:** All screens including Admin Panel
- **Goal:** Maintain accurate inventory and generate analytics

---

## 2. Global Navigation Flow

```
                    ┌─────────────┐
                    │   LOGIN     │
                    │   SCREEN    │
                    └──────┬──────┘
                           │ Credentials Valid
                           ▼
              ┌────────────────────────┐
              │      DASHBOARD         │
              │   (Default Landing)    │
              └───────────┬────────────┘
                          │
        ┌─────────────────┼─────────────────┐
        │                 │                 │
        ▼                 ▼                 ▼
   ┌─────────┐      ┌──────────┐      ┌──────────┐
   │ ADMIT   │      │DISCHARGE │      │ WAITLIST │
   │ DIALOG  │      │  DIALOG  │      │  PANEL   │
   └────┬────┘      └────┬─────┘      └────┬─────┘
        │                │                 │
        └────────────────┴─────────────────┘
                          │
        ┌─────────────────┼─────────────────┐
        │                 │                 │
        ▼                 ▼                 ▼
   ┌─────────┐      ┌──────────┐      ┌──────────┐
   │PATIENTS │      │ REPORTS  │      │  ADMIN   │
   │ MANAGER │      │  CENTER  │      │  PANEL   │
   └─────────┘      └──────────┘      └──────────┘
```

---

## 3. Detailed Flows

### 3.1 Flow 1: User Authentication

```
[START]
   │
   ▼
┌─────────────────────────────────────────┐
│ 1. Display Login Screen                  │
│    - Role selection (Receptionist/Admin) │
│    - Username input                      │
│    - Password input                      │
│    - Sign In button                      │
└─────────────────────────────────────────┘
   │
   ▼ User clicks "Sign In"
┌─────────────────────────────────────────┐
│ 2. Client-Side Validation                │
│    - Check fields not empty              │
│    - Check username format               │
│    - If invalid: Show error, STOP        │
└─────────────────────────────────────────┘
   │
   ▼ Validation passed
┌─────────────────────────────────────────┐
│ 3. Database Authentication               │
│    - Query: SELECT role FROM users       │
│      WHERE username=? AND password=?     │
│    - (Note: In production, use hashed    │
│       passwords. For college demo,       │
│       plaintext or simple hash OK)       │
└─────────────────────────────────────────┘
   │
   ├─ Match Found ───────────────────────►┌─────────────┐
   │                                       │ 4. Store    │
   │                                       │    session: │
   │                                       │    user_id  │
   │                                       │    role     │
   │                                       │    name     │
   │                                       └──────┬──────┘
   │                                              │
   │                                              ▼
   │                                       ┌─────────────┐
   │                                       │ 5. Open     │
   │                                       │    Dashboard│
   │                                       │    (role-    │
   │                                       │    based nav)│
   │                                       └─────────────┘
   │
   └─ No Match ─────────────────────────►┌─────────────┐
                                          │ 6. Show     │
                                          │    error:   │
                                          │    "Invalid │
                                          │    credentials"
                                          │ 7. Shake    │
                                          │    dialog   │
                                          └─────────────┘
```

**Edge Cases:**
- Database connection failure → Show "Cannot connect to database" error, retry button
- Empty username/password → Inline validation error, no DB call
- 3 failed attempts → Optional: lock account for 5 minutes (college demo: just show warning)

---

### 3.2 Flow 2: Admit Patient (Happy Path)

```
[START: User on Dashboard]
   │
   ├─ Path A: Click "Admit Patient" in sidebar
   │
   └─ Path B: Click an Available (green) bed card
      │
      ▼
┌─────────────────────────────────────────┐
│ 1. Open Admit Patient Dialog             │
│    - Fade in + slide up animation        │
│    - Backdrop overlay on main window     │
│    - If Path B: Pre-select ward type     │
│      and preferred bed                   │
└─────────────────────────────────────────┘
   │
   ▼
┌─────────────────────────────────────────┐
│ 2. User fills form:                      │
│    Patient Info:                         │
│    - [New/Existing toggle]               │
│    - Name: "Amit Kumar"                  │
│    - Age: 45                             │
│    - Blood Group: O+                     │
│    - Contact: 9123456789                 │
│    - Address: "Delhi"                    │
│                                          │
│    Admission Details:                    │
│    - Ward Type: "ICU"                    │
│    - Preferred Bed: "ICU-02" (optional)│
│    - Doctor: "Dr. Rajesh Sharma"         │
│    - Severity: 4 (High)                  │
│    - Admission Date: Today               │
│    - Expected Discharge: 2026-05-20      │
│    - Notes: "Chest pain, needs monitor"  │
└─────────────────────────────────────────┘
   │
   ▼ User clicks "Admit Patient"
┌─────────────────────────────────────────┐
│ 3. Real-Time Validation                  │
│    - Name: Not empty, min 2 chars        │
│    - Age: 0-150                          │
│    - Contact: 10 digits                  │
│    - Ward Type: Selected                 │
│    - Doctor: Selected                    │
│    - Expected > Admission date           │
│    - If invalid: Red border + message    │
│    - Scroll to first error               │
└─────────────────────────────────────────┘
   │
   ▼ Validation passed
┌─────────────────────────────────────────┐
│ 4. Check Bed Availability                │
│    - If preferred bed specified:       │
│      Check if that specific bed is       │
│      Available                           │
│    - Else: Find first Available bed      │
│      in selected ward type               │
│    - Query: SELECT bed_id FROM beds...   │
└─────────────────────────────────────────┘
   │
   ├─ Bed Available ───────────────────────►┌─────────────┐
   │                                       │ 5. Start    │
   │                                       │    DB       │
   │                                       │    Transaction│
   │                                       │    (JDBC    │
   │                                       │    autoCommit│
   │                                       │    = false) │
   │                                       └──────┬──────┘
   │                                              │
   │                                              ▼
   │                                       ┌─────────────┐
   │                                       │ 6. Insert   │
   │                                       │    Patient  │
   │                                       │    (if new) │
   │                                       └──────┬──────┘
   │                                              │
   │                                              ▼
   │                                       ┌─────────────┐
   │                                       │ 7. Insert   │
   │                                       │    Admission│
   │                                       │    record   │
   │                                       └──────┬──────┘
   │                                              │
   │                                              ▼
   │                                       ┌─────────────┐
   │                                       │ 8. Update   │
   │                                       │    Bed      │
   │                                       │    status to│
   │                                       │    'Occupied'│
   │                                       └──────┬──────┘
   │                                              │
   │                                              ▼
   │                                       ┌─────────────┐
   │                                       │ 9. COMMIT   │
   │                                       │    transaction│
   │                                       └──────┬──────┘
   │                                              │
   │                                              ▼
   │                                       ┌─────────────┐
   │                                       │ 10. Trigger │
   │                                       │     fires:  │
   │                                       │     bed_status│
   │                                       │     _audit   │
   │                                       │     log entry│
   │                                       └──────┬──────┘
   │                                              │
   │                                              ▼
   │                                       ┌─────────────┐
   │                                       │ 11. Close   │
   │                                       │     dialog  │
   │                                       │ 12. Show    │
   │                                       │     success │
   │                                       │     toast:  │
   │                                       │     "Patient│
   │                                       │     admitted│
   │                                       │     to Bed  │
   │                                       │     ICU-02" │
   │                                       │ 13. Refresh │
   │                                       │     Dashboard│
   │                                       │     bed grid│
   │                                       └─────────────┘
   │
   └─ No Bed Available ──────────────────►┌─────────────┐
                                          │ 14. Show    │
                                          │     dialog: │
                                          │     "No beds│
                                          │     available│
                                          │     in ICU.  │
                                          │     Add to   │
                                          │     waitlist?"│
                                          └──────┬──────┘
                                                 │
                            ┌─ Yes ──────────────┘
                            │
                            ▼
                     ┌─────────────┐
                     │ 15. Add to  │
                     │     Waitlist│
                     │     (INSERT)│
                     │     Priority│
                     │     = 400   │
                     │     (4×100) │
                     └──────┬──────┘
                            │
                            ▼
                     ┌─────────────┐
                     │ 16. Close   │
                     │     dialog  │
                     │ 17. Show    │
                     │     info    │
                     │     toast:  │
                     │     "Added  │
                     │     to ICU  │
                     │     waitlist"│
                     │ 18. Refresh │
                     │     Waitlist│
                     │     count   │
                     └─────────────┘
                            │
                            │ No (user declines)
                            ▼
                     ┌─────────────┐
                     │ 19. Keep    │
                     │     dialog  │
                     │     open,   │
                     │     let user│
                     │     change  │
                     │     ward    │
                     │     preference│
                     └─────────────┘
```

**Error Handling:**
- Transaction fails (connection lost) → ROLLBACK, show error toast, keep dialog open with data
- Concurrent booking (another user took the bed mid-transaction) → ROLLBACK, show "Bed just allocated to another patient", refresh available beds
- Patient contact already exists → Ask "Patient already registered. Use existing record?"

---

### 3.3 Flow 3: Discharge Patient (Happy Path with Auto-Allocation)

```
[START: User on Dashboard]
   │
   ├─ Path A: Click occupied (red) bed card
   │
   └─ Path B: Click "Discharge" in sidebar → Select from list
      │
      ▼
┌─────────────────────────────────────────┐
│ 1. Open Discharge Dialog                 │
│    - Show patient summary card           │
│    - Pre-fill discharge date (today)     │
│    - Default: "Make Available" selected  │
└─────────────────────────────────────────┘
   │
   ▼ User reviews and clicks "Confirm Discharge"
┌─────────────────────────────────────────┐
│ 2. Show Confirmation Overlay             │
│    - "Discharge [Patient Name] from      │
│      Bed [Bed Number]?"                  │
│    - "This action cannot be undone."    │
│    - Buttons: "Yes, Discharge" | "Cancel"│
└─────────────────────────────────────────┘
   │
   ▼ User confirms
┌─────────────────────────────────────────┐
│ 3. Start DB Transaction                  │
│    - autoCommit = false                  │
│    - Isolation: REPEATABLE READ          │
└─────────────────────────────────────────┘
   │
   ▼
┌─────────────────────────────────────────┐
│ 4. Update Admission record               │
│    - status = 'Discharged'               │
│    - actual_discharge = NOW()            │
└─────────────────────────────────────────┘
   │
   ▼
┌─────────────────────────────────────────┐
│ 5. Update Bed Status                     │
│    - If "Make Available":               │
│      current_status = 'Available'        │
│    - If "Maintenance":                   │
│      current_status = 'Maintenance'      │
│      INSERT maintenance_log entry        │
└─────────────────────────────────────────┘
   │
   ▼ (Only if "Make Available")
┌─────────────────────────────────────────┐
│ 6. Check Waitlist                        │
│    - Query: SELECT * FROM waitlist       │
│      WHERE requested_ward_type = ?       │
│      AND status = 'Waiting'              │
│      ORDER BY priority_score DESC,       │
│      request_time ASC                    │
│      LIMIT 1                             │
└─────────────────────────────────────────┘
   │
   ├─ Waitlist Match Found ─────────────►┌─────────────┐
   │                                      │ 7. Auto-    │
   │                                      │    allocate:│
   │                                      │    a. Update│
   │                                      │       waitlist│
   │                                      │       status= │
   │                                      │       'Allocated'│
   │                                      │    b. Insert│
   │                                      │       new     │
   │                                      │       admission│
   │                                      │       for     │
   │                                      │       waitlist│
   │                                      │       patient │
   │                                      │    c. Update│
   │                                      │       bed     │
   │                                      │       status= │
   │                                      │       'Occupied'│
   │                                      └──────┬──────┘
   │                                             │
   │                                             ▼
   │                                      ┌─────────────┐
   │                                      │ 8. COMMIT   │
   │                                      │    transaction│
   │                                      └──────┬──────┘
   │                                             │
   │                                             ▼
   │                                      ┌─────────────┐
   │                                      │ 9. Triggers │
   │                                      │    fire for   │
   │                                      │    both       │
   │                                      │    discharge  │
   │                                      │    and new    │
   │                                      │    admission  │
   │                                      └──────┬──────┘
   │                                             │
   │                                             ▼
   │                                      ┌─────────────┐
   │                                      │ 10. Close   │
   │                                      │     dialog  │
   │                                      │ 11. Show    │
   │                                      │     success │
   │                                      │     toast:  │
   │                                      │     "[Old]  │
   │                                      │     discharged│
   │                                      │     · [New] │
   │                                      │     auto-   │
   │                                      │     admitted"│
   │                                      │ 12. Refresh │
   │                                      │     Dashboard│
   │                                      │     +       │
   │                                      │     Waitlist │
   │                                      └─────────────┘
   │
   └─ No Waitlist Match ────────────────►┌─────────────┐
                                          │ 13. COMMIT  │
                                          │     (simpler)│
                                          │ 14. Show    │
                                          │     toast:  │
                                          │     "Patient│
                                          │     discharged│
                                          │     · Bed   │
                                          │     now     │
                                          │     available"│
                                          │ 15. Refresh │
                                          │     Dashboard│
                                          └─────────────┘
```

**Edge Cases:**
- User selects "Maintenance" → Skip waitlist check, bed unavailable until maintenance completed
- Maintenance completion → Admin manually marks "Available" → THEN check waitlist
- Concurrent discharge of same patient → Transaction isolation prevents conflict

---

### 3.4 Flow 4: Waitlist Management

```
[START: User clicks "Waitlist" in sidebar]
   │
   ▼
┌─────────────────────────────────────────┐
│ 1. Load Waitlist Panel                   │
│    - Query vw_waitlist_details View      │
│    - Sort by priority_score DESC         │
│    - Show count badge in sidebar         │
└─────────────────────────────────────────┘
   │
   ▼
┌─────────────────────────────────────────┐
│ 2. Display Table:                        │
│    Priority | Name | Age | Ward | Time  │
│    ─────────────────────────────────────│
│    500 · Rajesh · 67 · ICU · 2h 15m   │
│    400 · Sunita · 32 · ICU · 3h 40m   │
│    300 · Rahul  · 45 · Gen· 5h 10m    │
│    ...                                  │
└─────────────────────────────────────────┘
   │
   ├─ Action A: Click "Allocate" on row ──►┌─────────────┐
   │                                         │ 3. Check if │
   │                                         │     matching│
   │                                         │     bed     │
   │                                         │     available│
   │                                         └──────┬──────┘
   │                                                │
   │                                   ┌─ Yes ──────┘
   │                                   │
   │                                   ▼
   │                            ┌─────────────┐
   │                            │ 4. Run      │
   │                            │    sp_      │
   │                            │    allocate_│
   │                            │    bed for  │
   │                            │    this     │
   │                            │    patient  │
   │                            └──────┬──────┘
   │                                   │
   │                                   ▼
   │                            ┌─────────────┐
   │                            │ 5. Remove   │
   │                            │    from     │
   │                            │    waitlist │
   │                            │    (UPDATE  │
   │                            │    status)  │
   │                            │ 6. Refresh  │
   │                            │    table    │
   │                            │ 7. Success  │
   │                            │    toast    │
   │                            └─────────────┘
   │
   ├─ Action B: Click "Remove" on row ────►┌─────────────┐
   │                                         │ 8. Confirm  │
   │                                         │    dialog:  │
   │                                         │    "Remove  │
   │                                         │    [Name]   │
   │                                         │    from     │
   │                                         │    waitlist?"│
   │                                         └──────┬──────┘
   │                                                │
   │                                   ┌─ Yes ──────┘
   │                                   │
   │                                   ▼
   │                            ┌─────────────┐
   │                            │ 9. UPDATE   │
   │                            │    waitlist │
   │                            │    status=  │
   │                            │    'Cancelled'│
   │                            │ 10. Refresh │
   │                            │     table   │
   │                            └─────────────┘
   │
   └─ Action C: Click "Auto-Allocate All" ─►┌─────────────┐
                                              │ 11. Cursor  │
                                              │     loops   │
                                              │     through │
                                              │     waitlist│
                                              │     (MySQL  │
                                              │     cursor) │
                                              │     in      │
                                              │     priority│
                                              │     order   │
                                              └──────┬──────┘
                                                     │
                                                     ▼
                                              ┌─────────────┐
                                              │ 12. For each│
                                              │     patient:│
                                              │     Find bed│
                                              │     If found:│
                                              │       Allocate│
                                              │       Mark    │
                                              │       'Allocated'│
                                              │     Else:     │
                                              │       Skip,   │
                                              │       keep    │
                                              │       'Waiting'│
                                              └──────┬──────┘
                                                     │
                                                     ▼
                                              ┌─────────────┐
                                              │ 13. Summary │
                                              │     dialog: │
                                              │     "5      │
                                              │     allocated│
                                              │     · 3     │
                                              │     still   │
                                              │     waiting" │
                                              │ 14. Refresh │
                                              │     all     │
                                              │     panels  │
                                              └─────────────┘
```

---

### 3.5 Flow 5: Generate Report

```
[START: User clicks "Reports" in sidebar]
   │
   ▼
┌─────────────────────────────────────────┐
│ 1. Load Reports Center                   │
│    - Show report category cards          │
│    - Each card: icon, title, desc        │
└─────────────────────────────────────────┘
   │
   ▼ User clicks "Occupancy Overview"
┌─────────────────────────────────────────┐
│ 2. Load Report Detail View               │
│    - Back button (→ Reports Center)    │
│    - Title: "Occupancy Overview"         │
│    - Date range selector                 │
└─────────────────────────────────────────┘
   │
   ▼ Default range: "Last 7 Days"
┌─────────────────────────────────────────┐
│ 3. Query Database                        │
│    - SELECT from vw_current_occupancy    │
│    - Or: Aggregate admissions table      │
│    - Group by ward_type, date range      │
└─────────────────────────────────────────┘
   │
   ▼
┌─────────────────────────────────────────┐
│ 4. Render Results:                       │
│    - Summary cards (3 metrics)           │
│    - Data table: Ward | Total | Occ | %  │
│    - Trend indicator (↑/↓ vs previous) │
│    - Color-coded occupancy %             │
│      (Green <70%, Yellow 70-90%,       │
│       Red >90%)                          │
└─────────────────────────────────────────┘
   │
   ├─ Action: Change date range ────────► Re-run query, refresh table
   │
   └─ Action: Click "Export to CSV" ────► Generate CSV file, open save dialog
```

---

### 3.6 Flow 6: Admin — Add New Ward

```
[START: Admin clicks "Admin Panel" → "Wards" tab]
   │
   ▼
┌─────────────────────────────────────────┐
│ 1. Display Wards Table                   │
│    - Columns: ID, Type, Floor, Cap, etc  │
│    - "Add Ward" button top-right         │
└─────────────────────────────────────────┘
   │
   ▼ Click "Add Ward"
┌─────────────────────────────────────────┐
│ 2. Open Add Ward Dialog                  │
│    - Ward Type: dropdown                 │
│    - Floor: number spinner               │
│    - Capacity: number spinner            │
│    - Daily Charge: decimal input         │
└─────────────────────────────────────────┘
   │
   ▼ Fill and Submit
┌─────────────────────────────────────────┐
│ 3. Validation                            │
│    - Type: Required, from enum           │
│    - Floor: Positive integer             │
│    - Capacity: > 0                       │
│    - Charge: >= 0                        │
└─────────────────────────────────────────┘
   │
   ▼ Valid
┌─────────────────────────────────────────┐
│ 4. INSERT INTO wards                   │
│    - CHECK constraints enforce rules   │
│    - If duplicate ward_type+floor:     │
│      Show error (handled by UNIQUE     │
│      or application check)               │
└─────────────────────────────────────────┘
   │
   ▼ Success
┌─────────────────────────────────────────┐
│ 5. Close dialog, refresh table           │
│    - Show success toast                  │
│    - New ward appears in list            │
│    - Dashboard ward filter updates       │
└─────────────────────────────────────────┘
```

---

## 4. State Machine: Bed Lifecycle

```
                    ┌─────────────┐
                    │  AVAILABLE  │
                    │  (Green)    │
                    └──────┬──────┘
                           │
              ┌────────────┼────────────┐
              │ Admit      │            │ Mark Maint.
              │ Patient    │            │ (Admin)
              ▼            │            ▼
       ┌─────────────┐     │     ┌─────────────┐
       │  OCCUPIED   │     │     │ MAINTENANCE│
       │   (Red)     │     │     │  (Yellow)   │
       └──────┬──────┘     │     └──────┬──────┘
              │            │            │
              │ Discharge  │            │ Complete
              │ (2 paths)  │            │ Maint.
              ▼            │            ▼
       ┌─────────────┐    │     ┌─────────────┐
       │  Available?   │────┘     │  AVAILABLE  │
       │  (Waitlist   │          │  (Green)    │
       │   check)     │          └─────────────┘
       └──────┬──────┘
              │
    ┌─────────┴─────────┐
    │ Yes (match found) │ No match
    │                   │
    ▼                   ▼
┌─────────────┐   ┌─────────────┐
│ Auto-admit  │   │  AVAILABLE  │
│ waitlisted  │   │  (Green)    │
│ patient     │   └─────────────┘
│ (→Occupied) │
└─────────────┘
```

---

## 5. Error Flows

### 5.1 Database Connection Lost
```
[Any DB operation]
   │
   ▼ SQLException: Connection closed
┌─────────────────────────────────────────┐
│ 1. Catch exception in DAO                │
│ 2. Show error dialog:                    │
│    "Database connection lost.             │
│     Please check your network and retry." │
│ 3. "Retry" button → Reconnect            │
│ 4. "Cancel" button → Return to previous  │
│    screen, disable DB-dependent actions    │
└─────────────────────────────────────────┘
```

### 5.2 Concurrent Bed Booking (Race Condition)
```
[User A and User B both click same Available bed within same second]
   │
   ├─ User A's transaction commits first ──► Bed status = 'Occupied'
   │
   └─ User B's transaction tries UPDATE ──► Row changed, conflict detected
                                              │
                                              ▼
                                       ┌─────────────┐
                                       │ ROLLBACK    │
                                       │ Show: "This│
                                       │ bed was just│
                                       │ allocated.  │
                                       │ Refreshing   │
                                       │ available   │
                                       │ beds..."    │
                                       │ Auto-refresh│
                                       │ bed grid    │
                                       └─────────────┘
```

### 5.3 Invalid Data Submission
```
[User submits form with errors]
   │
   ▼
┌─────────────────────────────────────────┐
│ 1. Client-side validation catches:     │
│    - Empty required fields             │
│    - Invalid formats (age = "abc")     │
│    - Logic errors (discharge < admit)  │
│ 2. Visual feedback:                    │
│    - Red border on invalid fields      │
│    - Error text below each field       │
│    - First error field gets focus      │
│ 3. Form does NOT submit to database    │
└─────────────────────────────────────────┘
```

---

## 6. Notification & Feedback Matrix

| Action | Success Feedback | Error Feedback | Duration |
|---|---|---|---|
| Login | None (direct to Dashboard) | Red text below form + shake | Until fixed |
| Admit Patient | Toast: "Admitted to [Bed]" | Dialog stays open, field errors | 4s |
| Discharge Patient | Toast: "Discharged · Bed [status]" | Dialog with retry | 4s |
| Add to Waitlist | Toast: "Added to [Ward] waitlist" | Inline form error | 4s |
| Auto-Allocate | Summary dialog with counts | Toast: "No beds available" | 5s |
| Generate Report | Table populates | Toast: "No data for range" | 4s |
| Add Ward/Bed/Staff | Toast: "[Item] added successfully" | Dialog with field errors | 4s |
| DB Connection Lost | — | Dialog: "Connection lost · Retry" | Persistent |

---

*End of Application Flow Document*
