# JABAL-E-NOOR GIRLS MADRASAH ERP
## Institutional Management System Documentation & Deployment Guide

**Institution:** Jabale Noor Girls Madrasah  
**Location:** Ramgarh, Khagrachhari, Bangladesh  
**Platform:** Android (Kotlin, Jetpack Compose, Material 3, Room SQLite Database)  
**Primary Language:** Bengali (বাংলা)  
**Secondary Language:** English (EN)  
**Currency:** Bangladeshi Taka (৳)  
**Default Timezone:** Asia/Dhaka  

---

### 1. Cost & Free-Tier Assurance
- **100% Free-Tier / Spark Plan Compatible:** The application operates without requiring any paid APIs, credit cards, or billing plans.
- **Offline-First:** All records (students, admissions, fee payments, expenses, incomes, staff, attendance, examinations, and notices) are stored directly in the local encrypted Room database (`jabalenoor_erp.db`).
- **Cloud Migration Ready:** A comprehensive zero-trust `firestore.rules` specification is included at the project root for seamless migration to Firebase Firestore under the Spark (Free) plan if multi-device cloud synchronization is desired.

---

### 2. Implemented Modules & Features

| Module | Features & Status |
|---|---|
| **1. Institutional Dashboard** | Live financial summary (Income, Expenses, Net Balance), Today's Fee Collection, Class-wise student distribution progress bars, Recent fee receipts, Important announcements, Quick Action navigation. **[Complete]** |
| **2. Student Management** | Full database (ID, Admission No, Bengali & English names, Father, Mother, Guardian Phone, Present Address, DOB, Blood Group, Class, Roll). Search, class filters, full student profile dialog, CSV export, duplicate roll/admission prevention. **[Complete]** |
| **3. Admission Management** | Automated sequential Student ID (`JNGM-2026-XXX`) and Admission No (`ADM-2026-XXX`) generation. Class and roll assignment, instant admission fee collection and Money Receipt printing. **[Complete]** |
| **4. Fee Collection & Receipts** | Multi-category invoice collection (Tuition, Transport, Exam, Admission, Session, Registration, Development, Books, Others). Auto-calculated subtotal, discount, paid amount, due balance. Payment via Cash, bKash, Nagad, Rocket, Bank Transfer. Official printable Money Receipt voucher with share & print dialog. **[Complete]** |
| **5. Accounts & Daily Finance** | Real-time Income ledger (donations, grants, fee collection), Expense ledger (salaries, rent, utilities, maintenance, supplies). Daily and session balance calculation. **[Complete]** |
| **6. Teachers & Staff** | Employee directory (Designation, Department, Salary, Phone), monthly salary disbursement voucher, expense linking. **[Complete]** |
| **7. Attendance Tracking** | Student daily attendance sheet with quick toggle (Present, Absent, Late, Leave). Staff daily attendance sheet. Unique index preventing duplicate attendance on the same date. **[Complete]** |
| **8. Examinations & Results** | Exam scheduler, subject marks entry, automated Bangladesh Grading & GPA 5.0 scale (A+, A, A-, B, C, D, F). Printable Academic Report Card modal with subject breakdown and overall grade. **[Complete]** |
| **9. Academic Sessions** | Multi-session support (2026, 2027, 2028). Active session switcher. Historical record preservation safeguard. **[Complete]** |
| **10. Notice Board** | Circulars & announcements, urgent badges, categorization (General, Academic, Holiday, Exam, Fees), text sharing. **[Complete]** |
| **11. Role-Based Access Control** | Fast role switcher for Super Administrator, Principal, Accountant, Teacher, and Attendance Operator with predefined PINs (1111, 2222, 3333, 4444, 5555). **[Complete]** |
| **12. Language Toggle** | Instant switching between Bengali (বাংলা) and English (EN) across all screens and receipts. **[Complete]** |

---

### 3. Database Architecture & Backup Instructions

#### Local Database (Room SQLite):
- Database file: `/data/data/com.aistudio.jabalenoor.mgkeq/databases/jabalenoor_erp.db`
- **CSV Data Export:** Go to **Students** > tap **CSV** to instantly export student records in standard spreadsheet format.
- **Receipt Sharing:** Tap **Print / Share** on any fee receipt to generate a shareable plain-text or PDF-printable receipt via Android system share.

#### Firestore Rules (`/firestore.rules`):
- Role-based zero-trust security rules with immutable append-only audit logging and financial ledger protection.

---

### 4. Default Login PINs (Quick Switcher)
- **Super Administrator (প্রধান প্রশাসক):** `1111`
- **Principal (মুহতামিম / অধ্যক্ষ):** `2222`
- **Accountant (হিসাবরক্ষক):** `3333`
- **Teacher (মুদাররিস / শিক্ষিকা):** `4444`
- **Attendance Operator (হাজিরা অপারেটর):** `5555`

---

### 5. Services Requiring Payment
- **None:** The entire system is built strictly using free, open-source Android libraries and local Room SQLite storage with zero paid service dependencies.
