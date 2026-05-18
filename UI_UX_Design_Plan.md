# UI/UX Design Plan
## Hospital Bed Allocation System

**Version:** 1.0  
**Date:** 2026-05-17  
**Target Platform:** Java Swing Desktop Application  
**Design Philosophy:** Modern Flat UI with Medical-Grade Clarity

---

## 1. Design System

### 1.1 Color Palette

| Token | Hex | Usage |
|---|---|---|
| **Primary** | `#2563EB` | Buttons, active states, headers, action items |
| **Primary Hover** | `#1D4ED8` | Button hover states |
| **Success** | `#10B981` | Available beds, success messages, discharge confirmations |
| **Danger** | `#EF4444` | Occupied beds, errors, critical alerts |
| **Warning** | `#F59E0B` | Maintenance beds, waitlist items, cautions |
| **Neutral-900** | `#111827` | Primary text, headings |
| **Neutral-600** | `#4B5563` | Secondary text, labels |
| **Neutral-400** | `#9CA3AF` | Disabled states, placeholders |
| **Neutral-200** | `#E5E7EB` | Borders, dividers |
| **Neutral-100** | `#F3F4F6` | Table alternating rows, card backgrounds |
| **Neutral-50** | `#F9FAFB` | Main window background |
| **Surface** | `#FFFFFF` | Panels, cards, dialogs |
| **Overlay** | `rgba(0,0,0,0.5)` | Modal backdrops |

### 1.2 Typography

| Element | Font | Size | Weight | Color |
|---|---|---|---|---|
| Window Title | Segoe UI / System | 20px | Bold | Neutral-900 |
| Section Header | Segoe UI | 16px | Semibold | Neutral-900 |
| Card Title | Segoe UI | 14px | Semibold | Neutral-900 |
| Body Text | Segoe UI | 13px | Regular | Neutral-600 |
| Table Header | Segoe UI | 12px | Semibold | Neutral-600 |
| Table Cell | Segoe UI | 13px | Regular | Neutral-900 |
| Button Text | Segoe UI | 13px | Semibold | White / Primary |
| Label | Segoe UI | 12px | Medium | Neutral-600 |
| Badge Text | Segoe UI | 11px | Semibold | White |

### 1.3 Spacing System

| Token | Value | Usage |
|---|---|---|
| `xs` | 4px | Icon padding, tight gaps |
| `sm` | 8px | Inner element padding |
| `md` | 16px | Card padding, form field gaps |
| `lg` | 24px | Section gaps, dialog padding |
| `xl` | 32px | Major section separators |
| `2xl` | 48px | Page-level padding |

### 1.4 Component Styles

#### Primary Button
- Background: `#2563EB`, Text: White, 13px Semibold
- Padding: 10px vertical, 20px horizontal
- Border radius: 6px
- Hover: Background `#1D4ED8`, slight shadow `0 2px 4px rgba(37,99,235,0.3)`
- Pressed: Background `#1E40AF`

#### Secondary Button
- Background: White, Border: 1px `#E5E7EB`, Text: `#4B5563`
- Hover: Background `#F9FAFB`, Border `#D1D5DB`

#### Danger Button
- Background: `#EF4444`, Text: White
- Hover: `#DC2626`

#### Status Badge
- Padding: 4px 10px, Border radius: 12px (pill shape)
- Font: 11px Semibold
- Available: Background `#D1FAE5`, Text `#065F46`
- Occupied: Background `#FEE2E2`, Text `#991B1B`
- Maintenance: Background `#FEF3C7`, Text `#92400E`

#### Input Field
- Background: White, Border: 1px `#E5E7EB`
- Border radius: 6px, Padding: 10px 14px
- Focus: Border `#2563EB`, subtle blue glow `0 0 0 3px rgba(37,99,235,0.1)`
- Error: Border `#EF4444`, glow red

#### Card Panel
- Background: White, Border: 1px `#E5E7EB`
- Border radius: 8px
- Shadow: `0 1px 3px rgba(0,0,0,0.1)`
- Padding: 20px

#### Data Table
- Header: Background `#F9FAFB`, Text `#4B5563`, bottom border `#E5E7EB`
- Row height: 44px
- Alternating rows: White / `#F9FAFB`
- Hover row: `#EFF6FF` (very light blue)
- Selected row: `#DBEAFE` with left border 3px `#2563EB`

---

## 2. Screen Inventory

### 2.1 Screen List

| # | Screen Name | Purpose | Priority |
|---|---|---|---|
| 1 | **Login Screen** | Authenticate user (admin/receptionist) | High |
| 2 | **Dashboard** | Real-time bed overview, stats, quick actions | High |
| 3 | **Admit Patient Dialog** | Register and admit a new or existing patient | High |
| 4 | **Discharge Dialog** | Process patient discharge with options | High |
| 5 | **Patient Manager** | Search, view, edit patient records | Medium |
| 6 | **Waitlist Panel** | View and manage waiting patients | High |
| 7 | **Reports Center** | Generate and view occupancy/turnover reports | Medium |
| 8 | **Admin Panel** | Manage wards, beds, staff, maintenance | Medium |
| 9 | **Bed Detail View** | Detailed view of a single bed's history | Low |

---

## 3. Screen Specifications

### 3.1 Login Screen

**Layout:** Centered card on neutral background  
**Window Size:** 400×500, Non-resizable, Centered on screen  
**Background:** `#F3F4F6` (full window)

**Elements (Top to Bottom):**
1. **App Icon** — 64×64 hospital cross icon, centered, color `#2563EB`
2. **Title** — "Hospital Bed Manager", 20px Bold, `#111827`, centered
3. **Subtitle** — "Sign in to your account", 13px Regular, `#4B5563`, centered, margin-top 8px
4. **Card Container** — White, rounded 12px, shadow, padding 32px, width 340px centered
   - **Role Label** — "Select Role", 12px Medium, `#4B5563`
   - **Role Toggle** — Two buttons side by side: "Receptionist" | "Admin". Selected state: Primary background, white text. Unselected: `#F3F4F6` background, `#4B5563` text. Border radius 6px.
   - **Username Label** — "Username", 12px Medium
   - **Username Input** — Standard input field, placeholder "Enter username"
   - **Password Label** — "Password", 12px Medium
   - **Password Input** — Standard input field, placeholder "Enter password", masked characters
   - **Sign In Button** — Full width, Primary button style, text "Sign In", margin-top 24px
   - **Error Message** — Hidden by default. Red text `#EF4444`, 12px, appears below button on failed login.

**Interactions:**
- Enter key submits form
- Failed login shakes the card horizontally (animation) and shows error
- Successful login transitions to Dashboard with fade effect

---

### 3.2 Dashboard (Main Window)

**Layout:** Sidebar + Main Content Area  
**Window Size:** 1200×800, Resizable, Maximized by default  
**Background:** `#F9FAFB`

#### 3.2.1 Top Navigation Bar
- **Height:** 56px
- **Background:** White, bottom border 1px `#E5E7EB`
- **Left:** App logo (24px) + "Bed Manager" (14px Semibold)
- **Right:** Current user name + Role badge (pill badge, primary color) + Logout icon button

#### 3.2.2 Left Sidebar
- **Width:** 220px
- **Background:** White, right border 1px `#E5E7EB`
- **Items (vertical list, 44px height each):**
  - Dashboard (icon: grid) — Active state: left border 3px `#2563EB`, background `#EFF6FF`, text `#2563EB`
  - Admit Patient (icon: user-plus)
  - Discharge (icon: user-minus)
  - Waitlist (icon: clock) — Shows count badge if >0 patients waiting
  - Patients (icon: users)
  - Reports (icon: bar-chart)
  - Admin (icon: settings) — Only visible for Admin role
- **Hover:** Background `#F9FAFB`
- **Icon:** 20px, `#9CA3AF` (inactive), `#2563EB` (active)
- **Text:** 13px Regular, `#4B5563` (inactive), `#2563EB` (active)

#### 3.2.3 Main Content Area — Dashboard View

**Stats Row (4 cards, horizontal, gap 20px):**

| Card | Icon Color | Value | Label |
|---|---|---|---|
| Total Beds | `#6B7280` | 48 | Total Beds |
| Available | `#10B981` | 12 | Available Now |
| Occupied | `#EF4444` | 32 | Occupied |
| Maintenance | `#F59E0B` | 4 | Under Maintenance |

- **Card Style:** White, rounded 8px, shadow `0 1px 3px rgba(0,0,0,0.1)`, padding 20px
- **Icon:** 40px circle background (very light tint of icon color), icon centered
- **Value:** 28px Bold, `#111827`
- **Label:** 13px Regular, `#6B7280`

**Ward Filter Tabs:**
- Horizontal row of pill buttons: "All Wards" | "General" | "ICU" | "Emergency" | "Private"
- Active tab: Primary background, white text
- Inactive: `#F3F4F6` background, `#4B5563` text
- Margin-top: 24px from stats row

**Bed Grid (Main Visual):**
- **Layout:** Responsive grid, 4–6 columns depending on window width
- **Bed Card Size:** ~180×140px
- **Card Style:**
  - White background, rounded 8px, border 1px `#E5E7EB`
  - Top colored strip: 4px height — Green (Available), Red (Occupied), Yellow (Maintenance)
  - Padding: 16px
  - **Bed Number:** 16px Bold, `#111827`, top-left
  - **Ward Badge:** Pill badge top-right (e.g., "ICU"), background `#F3F4F6`, text `#4B5563`
  - **Status Badge:** Below bed number, margin-top 8px (Available/Occupied/Maintenance pill)
  - **Patient Name:** If occupied, 12px Regular, `#4B5563`, "Rahul Singh"
  - **Days:** If occupied, 11px, `#9CA3AF`, "Day 3"
  - **Equipment:** Small icons row at bottom (oxygen, ventilator, monitor)

**Interactions:**
- Click Available bed → Opens Admit Dialog pre-filled with that bed
- Click Occupied bed → Opens Discharge Dialog for that patient
- Click Maintenance bed → Shows maintenance details tooltip
- Right-click any bed → Context menu: "View Details", "Mark Maintenance", "History"
- Hover card → Slight lift shadow `0 4px 6px rgba(0,0,0,0.1)`

**Recent Activity Panel (Bottom Right, optional):**
- Card titled "Recent Activity"
- List of last 5 admissions/discharges with timestamp
- Small colored dot (green=admit, red=discharge)

---

### 3.3 Admit Patient Dialog

**Type:** Modal Dialog (JDialog)  
**Size:** 600×650  
**Backdrop:** Overlay `rgba(0,0,0,0.5)` on parent window  
**Animation:** Fade in 150ms, slide up 20px

**Header:**
- Title: "Admit Patient", 18px Bold
- Close button (X) top-right, `#9CA3AF`, hover `#EF4444`
- Bottom border 1px `#E5E7EB`

**Body (Scrollable JPanel with padding 24px):**

**Section 1: Patient Information**
- Section title: "Patient Information", 14px Semibold, `#111827`
- Divider line below title
- **Two-column layout:**
  - Left: "Patient ID" label + input (if existing patient) OR "New Patient" toggle
  - Right: "Full Name" label + input
- **Two-column:**
  - Left: "Age" label + number input (spinner, 0-150)
  - Right: "Blood Group" label + dropdown (A+, A-, B+, B-, O+, O-, AB+, AB-)
- **Two-column:**
  - Left: "Contact" label + phone input
  - Right: "Emergency Contact" label + phone input
- **Full width:** "Address" label + multi-line text area (2 rows)

**Section 2: Admission Details**
- Section title: "Admission Details", 14px Semibold
- **Two-column:**
  - Left: "Ward Type" label + dropdown (General, ICU, Emergency, Private)
  - Right: "Preferred Bed" label + dropdown (optional, lists available beds in selected ward)
- **Two-column:**
  - Left: "Assigned Doctor" label + dropdown (populated from staff table, Doctors only)
  - Right: "Severity Level" label + dropdown (1-Low to 5-Critical)
- **Two-column:**
  - Left: "Admission Date" label + date picker (default today)
  - Right: "Expected Discharge" label + date picker

**Section 3: Notes**
- "Admission Notes" label + text area (3 rows)

**Footer (Sticky bottom, white background, top border 1px `#E5E7EB`):**
- **Left:** "Cancel" button — Secondary style
- **Right:** "Admit Patient" button — Primary style, disabled until all required fields valid
- **Right of that:** "Add to Waitlist" button — Warning style (only enabled if no beds available in selected ward)

**Validation:**
- Name: Required, min 2 chars, letters only
- Age: Required, 0-150
- Contact: Required, 10 digits
- Ward Type: Required
- Doctor: Required
- Expected Discharge: Must be after Admission Date

**Error Display:**
- Invalid fields get red border `#EF4444` and small red text below: "This field is required"
- Dialog shakes if submit attempted with errors

---

### 3.4 Discharge Dialog

**Type:** Modal Dialog  
**Size:** 500×450  
**Trigger:** Click occupied bed OR select from sidebar menu

**Header:** "Discharge Patient", 18px Bold

**Body:**
- **Patient Card (highlighted):**
  - Large text: Patient Name, 20px Bold
  - Subtitle: "Bed ICU-03 · Admitted 3 days ago"
  - Patient details row: Age, Blood Group, Contact
  - Doctor: "Dr. Rajesh Sharma"

- **Discharge Details:**
  - "Actual Discharge Date" — Date picker (default today), disabled (auto-filled)
  - "Discharge Notes" — Text area (2 rows)

- **Bed Status After Discharge:**
  - Radio buttons:
    - "Make Available Immediately" (default selected)
    - "Send for Cleaning/Maintenance"
  - If Maintenance selected: "Reason" dropdown (Cleaning, Equipment Check, Repair, Other) + text field for Other

**Footer:**
- "Cancel" — Secondary
- "Confirm Discharge" — Danger style (red background)

**Confirmation Step:**
- On click "Confirm Discharge", show confirmation overlay within dialog:
  - "Are you sure you want to discharge [Patient Name]?"
  - "Yes, Discharge" (Danger) | "No, Go Back" (Secondary)

**Post-Discharge:**
- If waitlist auto-allocation occurs, show success toast: "Bed auto-allocated to waitlisted patient: [Name]"
- Toast appears top-right of main window, auto-dismiss 4 seconds, green background

---

### 3.5 Waitlist Panel

**Layout:** Full main content area (when selected from sidebar)

**Header Row:**
- Title: "Waitlist Queue", 20px Bold
- Right: "Total Waiting: 8" badge (warning color pill)
- Right: "Auto-Allocate All" button (Primary, only enabled if beds available)

**Filter Tabs:**
- "All" | "ICU" | "Emergency" | "General" | "Private"
- Same pill style as dashboard

**Data Table:**
- Columns: Priority Score | Patient Name | Age | Ward Requested | Waiting Time | Status | Actions
- **Priority Score:** Bold, color-coded — 500+ Red (critical), 300-500 Orange, <300 Blue
- **Waiting Time:** Auto-updating (e.g., "2h 15m"), refreshes every minute
- **Status:** Pill badge — "Waiting" (warning), "Allocated" (success)
- **Actions:**
  - "Allocate" button (Primary, small) — only if matching bed available
  - "Remove" button (Secondary, small) — removes from waitlist
  - "View" icon — opens patient detail

**Row Color Coding:**
- Critical patients (score >400): Very light red background `#FEF2F2`
- Hover: `#EFF6FF`

**Empty State:**
- Large checkmark icon, green
- "All caught up! No patients in waitlist."
- Centered, generous padding

---

### 3.6 Reports Center

**Layout:** Full main content area

**Header:** "Reports & Analytics", 20px Bold

**Report Cards Grid (2 columns):**

| Report Card | Icon | Description |
|---|---|---|
| Occupancy Overview | Pie chart icon | Real-time bed occupancy by ward |
| Bed Turnover | Clock icon | Average stay duration per ward |
| Doctor Workload | User icon | Current admissions per doctor |
| Waitlist History | Clock icon | Historical waitlist trends |
| Maintenance Log | Wrench icon | Bed maintenance history |
| Admission Trends | Trending up icon | Daily admission/discharge counts |

- **Card Style:** White, rounded 8px, padding 24px, hover shadow increase
- **Icon:** 48px, Primary color, top-left
- **Title:** 16px Semibold
- **Description:** 13px Regular, `#6B7280`
- **Click:** Opens detailed report view

**Detailed Report View (Example: Occupancy Overview):**
- Back button (arrow + "Reports")
- Title: "Occupancy Overview"
- Date range picker: "Last 7 Days" | "Last 30 Days" | "Custom"
- **Summary Cards (3):**
  - "Average Occupancy Rate: 78%"
  - "Most Occupied Ward: ICU (95%)"
  - "Total Admissions: 142"
- **Data Table:** Ward | Total Beds | Occupied | Available | Occupancy % | Trend
- **Export Button:** "Export to CSV" (Secondary, small)

---

### 3.7 Admin Panel

**Layout:** Full main content area, tabbed interface

**Tabs:** Wards | Beds | Staff | Maintenance Log

**Wards Tab:**
- "Add Ward" button top-right (Primary)
- Table: Ward ID | Type | Floor | Capacity | Daily Charge | Beds Count | Actions (Edit, Delete)
- Edit opens inline or dialog

**Beds Tab:**
- Filter: Ward dropdown + Status dropdown + Search
- Table: Bed ID | Ward | Number | Equipment | Status | Current Patient | Actions
- Bulk actions: "Mark Selected as Maintenance"

**Staff Tab:**
- "Add Staff" button
- Table: ID | Name | Role | Department | Phone | Current Admissions | Actions
- Role filter dropdown

**Maintenance Log Tab:**
- Table: Log ID | Bed | Start Time | End Time | Duration | Reason | Resolved By
- Filter by date range, bed, status

---

## 4. Interaction Patterns

### 4.1 Toast Notifications
- **Position:** Top-right corner, stacked vertically with 8px gap
- **Types:**
  - **Success:** Green left border, checkmark icon, white background
  - **Error:** Red left border, X icon, white background
  - **Warning:** Yellow left border, alert icon, white background
  - **Info:** Blue left border, info icon, white background
- **Behavior:** Auto-dismiss 4 seconds, manual close (X), slide in from right
- **Max:** 5 toasts visible, older ones fade

### 4.2 Loading States
- **Button Loading:** Spinner icon replaces text, button disabled, opacity 70%
- **Table Loading:** Skeleton rows (gray shimmer blocks) for 1–3 rows
- **Page Loading:** Centered spinner with "Loading..." text
- **Background Tasks:** Use `SwingWorker` to prevent UI freeze

### 4.3 Confirmation Patterns
- **Destructive Actions:** Two-step confirmation (e.g., discharge, delete)
- **Modal Backdrop:** Click outside modal to cancel (optional setting)
- **Keyboard:** Escape to cancel, Enter to confirm

### 4.4 Form Validation
- **Real-time:** Validate on blur (field loses focus)
- **Submit-time:** Validate all fields, scroll to first error
- **Visual:** Red border + red text below field
- **Success:** Green checkmark icon inside field (right side)

### 4.5 Search & Filter
- **Global Search:** Top bar search box, searches patients and beds
- **Table Filters:** Inline dropdown filters per column
- **Search Results:** Dropdown with highlighted matches, Enter to select first

---

## 5. Accessibility Considerations

- All interactive elements have focus indicators (blue outline 2px)
- Tab order follows visual order (left-to-right, top-to-bottom)
- Color is not the only indicator — icons + text labels for all statuses
- Minimum touch target: 44×44px for buttons
- High contrast mode support (darker text, stronger borders)

---

## 6. Responsive Behavior

Since Swing is desktop-only, "responsive" means:
- **Window Resize:** Bed grid reflows (4 cols → 3 cols → 2 cols)
- **Minimum Size:** 900×600, below this show scrollbars
- **Maximized:** Stats cards expand, bed grid shows 6 columns
- **Sidebar:** Collapses to icon-only mode at window width < 1000px (hover to expand)

---

*End of UI/UX Design Plan*
