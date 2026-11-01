package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.*
import com.example.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [
    UserEntity::class,
    AcademicSessionEntity::class,
    MadrasahClassEntity::class,
    StudentEntity::class,
    FeeCategoryEntity::class,
    FeeStructureEntity::class,
    FeePaymentEntity::class,
    FeePaymentDetailEntity::class,
    ExpenseEntity::class,
    IncomeEntity::class,
    EmployeeEntity::class,
    SalaryPaymentEntity::class,
    StudentAttendanceEntity::class,
    StaffAttendanceEntity::class,
    ExaminationEntity::class,
    SubjectEntity::class,
    ExamResultEntity::class,
    ExamScheduleEntity::class,
    HomeworkEntity::class,
    NoticeEntity::class,
    InstitutionSettingsEntity::class,
    AuditLogEntity::class
  ],
  version = 2,
  exportSchema = false
)
abstract class MadrasahDatabase : RoomDatabase() {
  abstract fun studentDao(): StudentDao
  abstract fun sessionDao(): AcademicSessionDao
  abstract fun classDao(): MadrasahClassDao
  abstract fun feeDao(): FeeDao
  abstract fun accountsDao(): AccountsDao
  abstract fun employeeDao(): EmployeeDao
  abstract fun attendanceDao(): AttendanceDao
  abstract fun examDao(): ExaminationDao
  abstract fun homeworkDao(): HomeworkDao
  abstract fun noticeDao(): NoticeDao
  abstract fun settingsDao(): SettingsDao
  abstract fun userDao(): UserDao
  abstract fun auditDao(): AuditDao

  companion object {
    @Volatile
    private var INSTANCE: MadrasahDatabase? = null

    fun getDatabase(context: Context, scope: CoroutineScope): MadrasahDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          MadrasahDatabase::class.java,
          "jabalenoor_erp.db"
        )
        .addCallback(DatabaseCallback(scope))
        .fallbackToDestructiveMigration()
        .build()
        INSTANCE = instance
        instance
      }
    }
  }

  private class DatabaseCallback(
    private val scope: CoroutineScope
  ) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
      super.onCreate(db)
      INSTANCE?.let { database ->
        scope.launch(Dispatchers.IO) {
          populateInitialData(database)
        }
      }
    }

    suspend fun populateInitialData(db: MadrasahDatabase) {
      // 1. Settings - Official Name "জাবালে নূর গার্লস মাদ্রাসা"
      db.settingsDao().updateSettings(
        InstitutionSettingsEntity(
          id = 1,
          institutionNameBn = "জাবালে নূর গার্লস মাদ্রাসা",
          institutionNameEn = "Jabale Noor Girls Madrasah",
          sloganBn = "এসো, আলোকিত হও, আলোকিত কর বিশ্ব",
          locationBn = "রামগড়, খাগড়াছড়ি, বাংলাদেশ",
          locationEn = "Ramgarh, Khagrachhari, Bangladesh",
          estdYear = "2023",
          shortCode = "J.N.G.M",
          phone = "+880 1812-345678",
          email = "contact@jabalenoor.edu.bd",
          portalUrl = "https://jabalenoor.edu.bd/portal",
          currencySymbol = "৳",
          currentSession = "2026"
        )
      )

      // 2. Default Roles / Users
      db.userDao().insertUsers(
        listOf(
          UserEntity(username = "admin", email = "admin@jabalenoor.edu.bd", pin = "1111", fullName = "মুফতি জাহিদুল ইসলাম", role = "SUPER_ADMIN", phone = "01811223344"),
          UserEntity(username = "principal", email = "principal@jabalenoor.edu.bd", pin = "2222", fullName = "মাওলানা আব্দুল করিম", role = "PRINCIPAL", phone = "01822334455"),
          UserEntity(username = "accountant", email = "accounts@jabalenoor.edu.bd", pin = "3333", fullName = "মোহাম্মদ রফিকুল ইসলাম", role = "ACCOUNTANT", phone = "01833445566"),
          UserEntity(username = "khadija", email = "khadija@jabalenoor.edu.bd", pin = "4444", fullName = "উস্তাজা খাদিজা আক্তার", role = "TEACHER", phone = "01844556677"),
          UserEntity(username = "operator", email = "attendance@jabalenoor.edu.bd", pin = "5555", fullName = "সালমা বেগম", role = "ATTENDANCE_OPERATOR", phone = "01855667788")
        )
      )

      // 3. Academic Sessions
      db.sessionDao().insertSession(AcademicSessionEntity(name = "2026", isActive = true, startDate = "01-01-2026", endDate = "31-12-2026"))
      db.sessionDao().insertSession(AcademicSessionEntity(name = "2025", isActive = false, startDate = "01-01-2025", endDate = "31-12-2025"))
      db.sessionDao().insertSession(AcademicSessionEntity(name = "2027", isActive = false, startDate = "01-01-2027", endDate = "31-12-2027"))

      // 4. Madrasah Classes (Play to Class Five co-education, and Class Six)
      val classes = listOf(
        MadrasahClassEntity(id = 1, nameBn = "প্লে", nameEn = "Play", code = "PLY", orderIndex = 1),
        MadrasahClassEntity(id = 2, nameBn = "নার্সারি", nameEn = "Nursery", code = "NUR", orderIndex = 2),
        MadrasahClassEntity(id = 3, nameBn = "প্রথম শ্রেণি", nameEn = "Class One", code = "C01", orderIndex = 3),
        MadrasahClassEntity(id = 4, nameBn = "দ্বিতীয় শ্রেণি", nameEn = "Class Two", code = "C02", orderIndex = 4),
        MadrasahClassEntity(id = 5, nameBn = "তৃতীয় শ্রেণি", nameEn = "Class Three", code = "C03", orderIndex = 5),
        MadrasahClassEntity(id = 6, nameBn = "চতুর্থ শ্রেণি", nameEn = "Class Four", code = "C04", orderIndex = 6),
        MadrasahClassEntity(id = 7, nameBn = "পঞ্চম শ্রেণি", nameEn = "Class Five", code = "C05", orderIndex = 7),
        MadrasahClassEntity(id = 8, nameBn = "ষষ্ঠ শ্রেণি", nameEn = "Class Six", code = "C06", orderIndex = 8)
      )
      db.classDao().insertClasses(classes)

      // 5. Fee Categories
      val feeCategories = listOf(
        FeeCategoryEntity(id = 1, nameBn = "মাসিক বেতন", nameEn = "Monthly Tuition Fee", type = "RECURRING_MONTHLY", defaultAmount = 800.0),
        FeeCategoryEntity(id = 2, nameBn = "গাড়ি / যাতায়াত ফি", nameEn = "Vehicle / Transport Fee", type = "RECURRING_MONTHLY", defaultAmount = 400.0),
        FeeCategoryEntity(id = 3, nameBn = "পরীক্ষা ফি", nameEn = "Examination Fee", type = "ONE_TIME", defaultAmount = 500.0),
        FeeCategoryEntity(id = 4, nameBn = "ভর্তি ফি", nameEn = "Admission Fee", type = "ONE_TIME", defaultAmount = 2000.0),
        FeeCategoryEntity(id = 5, nameBn = "সেশন ফি", nameEn = "Session Fee", type = "ONE_TIME", defaultAmount = 1500.0),
        FeeCategoryEntity(id = 6, nameBn = "রেজিস্ট্রেশন ফি", nameEn = "Registration Fee", type = "ONE_TIME", defaultAmount = 300.0),
        FeeCategoryEntity(id = 7, nameBn = "উন্নয়ন ফি", nameEn = "Development Fee", type = "ONE_TIME", defaultAmount = 600.0),
        FeeCategoryEntity(id = 8, nameBn = "বই ও স্টেশনারি ফি", nameEn = "Books and Stationery Fee", type = "ONE_TIME", defaultAmount = 1200.0),
        FeeCategoryEntity(id = 9, nameBn = "অন্যান্য ফি", nameEn = "Other Fees", type = "ONE_TIME", defaultAmount = 250.0)
      )
      db.feeDao().insertCategories(feeCategories)

      // 6. Fee Structures
      val feeStructures = mutableListOf<FeeStructureEntity>()
      for (c in classes) {
        val tuition = when (c.id) {
          1L, 2L -> 600.0
          3L, 4L -> 800.0
          5L, 6L -> 1000.0
          else -> 1200.0
        }
        feeStructures.add(FeeStructureEntity(classId = c.id, feeCategoryId = 1, academicSession = "2026", amount = tuition))
        feeStructures.add(FeeStructureEntity(classId = c.id, feeCategoryId = 2, academicSession = "2026", amount = 400.0))
        feeStructures.add(FeeStructureEntity(classId = c.id, feeCategoryId = 3, academicSession = "2026", amount = 500.0))
      }
      db.feeDao().insertStructures(feeStructures)

      // 7. Initial Students (Co-ed in Play-Class 5: Boys & Girls with auto guardian login ID and PIN!)
      val students = listOf(
        StudentEntity(
          studentId = "JNGM-2026-001",
          admissionNumber = "ADM-2026-101",
          fullNameBn = "মারিয়াম আক্তার",
          fullNameEn = "Mariam Akhter",
          fatherName = "মোহাম্মদ হাবিবুর রহমান",
          motherName = "ফাতেমা খাতুন",
          guardianName = "মোহাম্মদ হাবিবুর রহমান",
          guardianPhone = "01819123456",
          dateOfBirth = "15-03-2017",
          gender = "Female",
          bloodGroup = "O+",
          presentAddress = "রামগড় বাজার, খাগড়াছড়ি",
          permanentAddress = "রামগড়, খাগড়াছড়ি",
          admissionDate = "05-01-2026",
          academicSession = "2026",
          classId = 3,
          className = "প্রথম শ্রেণি (Class One)",
          section = "A",
          rollNumber = 1,
          status = "ACTIVE",
          guardianLoginId = "JNGM-2026-001",
          guardianPassword = "1234"
        ),
        StudentEntity(
          studentId = "JNGM-2026-002",
          admissionNumber = "ADM-2026-102",
          fullNameBn = "আব্দুল্লাহ আল মাহমুদ",
          fullNameEn = "Abdullah Al Mahmud",
          fatherName = "মাওলানা আব্দুল মান্নান",
          motherName = "রাবেয়া বসরী",
          guardianName = "মাওলানা আব্দুল মান্নান",
          guardianPhone = "01818234567",
          dateOfBirth = "20-07-2017",
          gender = "Male", // Play-Class 5 Co-education
          bloodGroup = "A+",
          presentAddress = "সোনাইপুল, রামগড়",
          permanentAddress = "সোনাইপুল, রামগড়",
          admissionDate = "06-01-2026",
          academicSession = "2026",
          classId = 3,
          className = "প্রথম শ্রেণি (Class One)",
          section = "A",
          rollNumber = 2,
          status = "ACTIVE",
          guardianLoginId = "JNGM-2026-002",
          guardianPassword = "1234"
        ),
        StudentEntity(
          studentId = "JNGM-2026-003",
          admissionNumber = "ADM-2026-103",
          fullNameBn = "ফাতিমা তুয যোহরা",
          fullNameEn = "Fatima Tuz Zohra",
          fatherName = "আমিনুল হক",
          motherName = "রোকেয়া বেগম",
          guardianName = "আমিনুল হক",
          guardianPhone = "01817345678",
          dateOfBirth = "10-11-2016",
          gender = "Female",
          bloodGroup = "B+",
          presentAddress = "রামগড় চৌধুরীপাড়া",
          permanentAddress = "রামগড় চৌধুরীপাড়া",
          admissionDate = "08-01-2026",
          academicSession = "2026",
          classId = 4,
          className = "দ্বিতীয় শ্রেণি (Class Two)",
          section = "A",
          rollNumber = 1,
          status = "ACTIVE",
          guardianLoginId = "JNGM-2026-003",
          guardianPassword = "1234"
        ),
        StudentEntity(
          studentId = "JNGM-2026-004",
          admissionNumber = "ADM-2026-104",
          fullNameBn = "মুহাম্মদ উসমান গণি",
          fullNameEn = "Muhammad Usman Goni",
          fatherName = "কবির হোসেন",
          motherName = "নাসরিন জাহান",
          guardianName = "কবির হোসেন",
          guardianPhone = "01816456789",
          dateOfBirth = "02-05-2015",
          gender = "Male",
          bloodGroup = "AB+",
          presentAddress = "মাটিরাঙ্গা রোড, রামগড়",
          permanentAddress = "মাটিরাঙ্গা রোড, রামগড়",
          admissionDate = "09-01-2026",
          academicSession = "2026",
          classId = 5,
          className = "তৃতীয় শ্রেণি (Class Three)",
          section = "A",
          rollNumber = 1,
          status = "ACTIVE",
          guardianLoginId = "JNGM-2026-004",
          guardianPassword = "1234"
        ),
        StudentEntity(
          studentId = "JNGM-2026-005",
          admissionNumber = "ADM-2026-105",
          fullNameBn = "রুফাইদা ইসলাম",
          fullNameEn = "Rufaida Islam",
          fatherName = "জাহিদুল হক",
          motherName = "সালমা খাতুন",
          guardianName = "জাহিদুল হক",
          guardianPhone = "01815567890",
          dateOfBirth = "12-09-2014",
          gender = "Female",
          bloodGroup = "O-",
          presentAddress = "রামগড় টি এন্ড টি রোড",
          permanentAddress = "রামগড় টি এন্ড টি রোড",
          admissionDate = "10-01-2026",
          academicSession = "2026",
          classId = 6,
          className = "চতুর্থ শ্রেণি (Class Four)",
          section = "A",
          rollNumber = 1,
          status = "ACTIVE",
          guardianLoginId = "JNGM-2026-005",
          guardianPassword = "1234"
        )
      )
      db.studentDao().insertStudents(students)

      // 8. Teachers and Staff with Email and Login Password
      val employees = listOf(
        EmployeeEntity(
          employeeId = "EMP-101",
          nameBn = "মাওলানা আব্দুল করিম",
          nameEn = "Mawlana Abdul Karim",
          email = "principal@jabalenoor.edu.bd",
          password = "2222",
          designation = "মুহতামিম / Principal",
          department = "Administration",
          phone = "01822334455",
          address = "রামগড়, খাগড়াছড়ি",
          joiningDate = "01-01-2020",
          monthlySalary = 30000.0,
          status = "Active"
        ),
        EmployeeEntity(
          employeeId = "EMP-102",
          nameBn = "উস্তাজা খাদিজা আক্তার",
          nameEn = "Ustaza Khadija Akhter",
          email = "khadija@jabalenoor.edu.bd",
          password = "4444",
          designation = "সিনিয়র শিক্ষিকা (হিফজ ও তাজবীদ)",
          department = "Academic",
          phone = "01844556677",
          address = "রামগড় বাজার",
          joiningDate = "15-02-2021",
          monthlySalary = 18000.0,
          status = "Active"
        ),
        EmployeeEntity(
          employeeId = "EMP-103",
          nameBn = "উস্তাজা তাহমিনা বেগম",
          nameEn = "Ustaza Tahmina Begum",
          email = "tahmina@jabalenoor.edu.bd",
          password = "1234",
          designation = "সহকারী শিক্ষিকা (বাংলা ও গণিত)",
          department = "Academic",
          phone = "01855661122",
          address = "রামগড় সোনাইপুল",
          joiningDate = "01-03-2022",
          monthlySalary = 15000.0,
          status = "Active"
        ),
        EmployeeEntity(
          employeeId = "EMP-104",
          nameBn = "মোহাম্মদ রফিকুল ইসলাম",
          nameEn = "Md. Rafiqul Islam",
          email = "accounts@jabalenoor.edu.bd",
          password = "3333",
          designation = "হিসাবরক্ষক (Accountant)",
          department = "Accounts",
          phone = "01833445566",
          address = "রামগড় পৌরসভা",
          joiningDate = "01-07-2022",
          monthlySalary = 16000.0,
          status = "Active"
        )
      )
      db.employeeDao().insertEmployees(employees)

      // 9. Initial Fee Payments
      val p1 = FeePaymentEntity(
        receiptNumber = "REC-2026-0001",
        transactionId = "TXN-829101",
        studentId = "JNGM-2026-001",
        studentName = "মারিয়াম আক্তার (Mariam Akhter)",
        classId = 3,
        className = "প্রথম শ্রেণি (Class One)",
        rollNumber = 1,
        academicSession = "2026",
        date = "15-10-2026",
        monthFor = "October 2026",
        totalAmount = 1200.0,
        discountAmount = 200.0,
        paidAmount = 1000.0,
        dueAmount = 0.0,
        paymentMethod = "BKASH",
        feeDescription = "মাসিক বেতন ও যাতায়াত ফি (অক্টোবর ২০২৬)",
        receivedBy = "মোহাম্মদ রফিকুল ইসলাম",
        remarks = "অনলাইনে পরিশোধিত"
      )
      val paymentId1 = db.feeDao().insertPayment(p1)
      db.feeDao().insertPaymentDetails(
        listOf(
          FeePaymentDetailEntity(paymentId = paymentId1, feeCategoryId = 1, feeCategoryName = "মাসিক বেতন (Tuition)", amount = 800.0),
          FeePaymentDetailEntity(paymentId = paymentId1, feeCategoryId = 2, feeCategoryName = "যাতায়াত ফি (Transport)", amount = 400.0)
        )
      )

      // 10. Initial Incomes and Expenses
      db.accountsDao().insertIncomes(
        listOf(
          IncomeEntity(
            receiptNumber = "INC-2026-001",
            category = "Donations",
            amount = 25000.0,
            date = "02-10-2026",
            academicSession = "2026",
            donorOrSource = "আলহাজ্ব নুর মোহাম্মদ (রামগড়)",
            description = "মাদ্রাসার লিল্লাহ বোর্ডিং ও হেফজ ফান্ডে দান",
            paymentMethod = "BANK"
          )
        )
      )

      db.accountsDao().insertExpenses(
        listOf(
          ExpenseEntity(
            voucherNumber = "EXP-2026-001",
            category = "Electricity",
            amount = 3200.0,
            date = "05-10-2026",
            academicSession = "2026",
            description = "মাদ্রাসা ও হোস্টেলের বিদ্যুৎ বিল",
            recipient = "পল্লী বিদ্যুৎ রামগড়",
            paymentMethod = "BKASH"
          )
        )
      )

      // 11. Subjects Master (Unified once for classes - feeds into Exam Routine, Marks, and Homework)
      val subjects = listOf(
        SubjectEntity(id = 1, classId = 3, nameBn = "কুরআন মাজিদ ও তাজবীদ", nameEn = "Quran Majid & Tajweed", code = "QRN-01", maxMarks = 100.0, passMarks = 33.0),
        SubjectEntity(id = 2, classId = 3, nameBn = "আরবি সাহিত্য ও ব্যাকরণ", nameEn = "Arabic Language", code = "ARB-01", maxMarks = 100.0, passMarks = 33.0),
        SubjectEntity(id = 3, classId = 3, nameBn = "আকাইদ ও ফিকহ", nameEn = "Aqaid & Fiqh", code = "FIQ-01", maxMarks = 100.0, passMarks = 33.0),
        SubjectEntity(id = 4, classId = 3, nameBn = "বাংলা", nameEn = "Bengali", code = "BEN-01", maxMarks = 100.0, passMarks = 33.0),
        SubjectEntity(id = 5, classId = 3, nameBn = "ইংরেজি", nameEn = "English", code = "ENG-01", maxMarks = 100.0, passMarks = 33.0),
        SubjectEntity(id = 6, classId = 3, nameBn = "গণিত", nameEn = "Mathematics", code = "MTH-01", maxMarks = 100.0, passMarks = 33.0)
      )
      db.examDao().insertSubjects(subjects)

      // 12. Examination
      val examId = db.examDao().insertExamination(
        ExaminationEntity(
          name = "বার্ষিক পরীক্ষা ২০২৬ (Annual Examination 2026)",
          academicSession = "2026",
          startDate = "15-11-2026",
          endDate = "28-11-2026",
          isPublished = true
        )
      )

      // 13. Exam Schedules (Routines)
      db.examDao().insertSchedules(
        listOf(
          ExamScheduleEntity(examId = examId, classId = 3, subjectId = 1, subjectName = "কুরআন মাজিদ ও তাজবীদ", examDate = "15-11-2026", startTime = "10:00 AM", endTime = "01:00 PM", roomNo = "101"),
          ExamScheduleEntity(examId = examId, classId = 3, subjectId = 2, subjectName = "আরবি সাহিত্য ও ব্যাকরণ", examDate = "17-11-2026", startTime = "10:00 AM", endTime = "01:00 PM", roomNo = "101"),
          ExamScheduleEntity(examId = examId, classId = 3, subjectId = 3, subjectName = "আকাইদ ও ফিকহ", examDate = "19-11-2026", startTime = "10:00 AM", endTime = "01:00 PM", roomNo = "101"),
          ExamScheduleEntity(examId = examId, classId = 3, subjectId = 4, subjectName = "বাংলা", examDate = "22-11-2026", startTime = "10:00 AM", endTime = "01:00 PM", roomNo = "101"),
          ExamScheduleEntity(examId = examId, classId = 3, subjectId = 5, subjectName = "ইংরেজি", examDate = "24-11-2026", startTime = "10:00 AM", endTime = "01:00 PM", roomNo = "101"),
          ExamScheduleEntity(examId = examId, classId = 3, subjectId = 6, subjectName = "গণিত", examDate = "26-11-2026", startTime = "10:00 AM", endTime = "01:00 PM", roomNo = "101")
        )
      )

      // 14. Sample Exam Results
      db.examDao().insertResults(
        listOf(
          ExamResultEntity(examId = examId, studentId = "JNGM-2026-001", classId = 3, subjectId = 1, subjectName = "কুরআন মাজিদ ও তাজবীদ", marksObtained = 95.0, grade = "A+", gpa = 5.0),
          ExamResultEntity(examId = examId, studentId = "JNGM-2026-001", classId = 3, subjectId = 2, subjectName = "আরবি সাহিত্য ও ব্যাকরণ", marksObtained = 88.0, grade = "A+", gpa = 5.0),
          ExamResultEntity(examId = examId, studentId = "JNGM-2026-001", classId = 3, subjectId = 3, subjectName = "আকাইদ ও ফিকহ", marksObtained = 92.0, grade = "A+", gpa = 5.0),
          ExamResultEntity(examId = examId, studentId = "JNGM-2026-001", classId = 3, subjectId = 4, subjectName = "বাংলা", marksObtained = 84.0, grade = "A+", gpa = 5.0),
          ExamResultEntity(examId = examId, studentId = "JNGM-2026-001", classId = 3, subjectId = 5, subjectName = "ইংরেজি", marksObtained = 78.0, grade = "A", gpa = 4.0),
          ExamResultEntity(examId = examId, studentId = "JNGM-2026-001", classId = 3, subjectId = 6, subjectName = "গণিত", marksObtained = 90.0, grade = "A+", gpa = 5.0),

          ExamResultEntity(examId = examId, studentId = "JNGM-2026-002", classId = 3, subjectId = 1, subjectName = "কুরআন মাজিদ ও তাজবীদ", marksObtained = 82.0, grade = "A+", gpa = 5.0),
          ExamResultEntity(examId = examId, studentId = "JNGM-2026-002", classId = 3, subjectId = 2, subjectName = "আরবি সাহিত্য ও ব্যাকরণ", marksObtained = 74.0, grade = "A", gpa = 4.0),
          ExamResultEntity(examId = examId, studentId = "JNGM-2026-002", classId = 3, subjectId = 3, subjectName = "আকাইদ ও ফিকহ", marksObtained = 85.0, grade = "A+", gpa = 5.0),
          ExamResultEntity(examId = examId, studentId = "JNGM-2026-002", classId = 3, subjectId = 4, subjectName = "বাংলা", marksObtained = 70.0, grade = "A", gpa = 4.0),
          ExamResultEntity(examId = examId, studentId = "JNGM-2026-002", classId = 3, subjectId = 5, subjectName = "ইংরেজি", marksObtained = 65.0, grade = "A-", gpa = 3.5),
          ExamResultEntity(examId = examId, studentId = "JNGM-2026-002", classId = 3, subjectId = 6, subjectName = "গণিত", marksObtained = 88.0, grade = "A+", gpa = 5.0)
        )
      )

      // 15. Sample Daily Homework
      db.homeworkDao().insertHomework(
        HomeworkEntity(
          classId = 3,
          className = "প্রথম শ্রেণি (Class One)",
          subjectId = 1,
          subjectName = "কুরআন মাজিদ ও তাজবীদ",
          date = "01-11-2026",
          title = "সুরা আল-ফাতিহা ও তাজবীদের নিয়ম",
          description = "সুরা আল-ফাতিহা তাজবীদসহ সুন্দর করে পড়ে আসবে এবং মুখস্থ শোনাবে।",
          assignedBy = "উস্তাজা খাদিজা আক্তার",
          submissionDate = "02-11-2026"
        )
      )

      // 16. Notices
      db.noticeDao().insertNotice(
        NoticeEntity(
          titleBn = "আগামী ১৫ নভেম্বর থেকে বার্ষিক পরীক্ষা ২০২৬ শুরু",
          titleEn = "Annual Examination 2026 Commences Nov 15",
          contentBn = "জাবালে নূর গার্লস মাদ্রাসার সকল ছাত্র-ছাত্রী ও অভিভাবকদের জানানো যাচ্ছে যে, আগামী ১৫ নভেম্বর থেকে বার্ষিক পরীক্ষা শুরু হবে। প্রবেশপত্র (Admit Card) সংগ্রহ করার জন্য অনুরোধ করা হলো।",
          contentEn = "Notice to all students and guardians: Annual Exam starts Nov 15. Please collect admit cards.",
          category = "Exam",
          publishDate = "01-11-2026",
          isUrgent = true
        )
      )
    }
  }
}
