package com.example.data.repository

import com.example.data.database.MadrasahDatabase
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MadrasahRepository(private val database: MadrasahDatabase) {

  // Current date in DD-MM-YYYY
  fun getTodayDate(): String {
    val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.US)
    return sdf.format(Date())
  }

  // Current month string
  fun getCurrentMonth(): String {
    val sdf = SimpleDateFormat("MMMM yyyy", Locale.US)
    return sdf.format(Date())
  }

  // --- Students ---
  val allStudents: Flow<List<StudentEntity>> = database.studentDao().getAllStudents()

  fun getStudentsBySession(session: String): Flow<List<StudentEntity>> =
    database.studentDao().getStudentsBySession(session)

  fun getStudentsByClass(session: String, classId: Long): Flow<List<StudentEntity>> =
    database.studentDao().getStudentsByClass(session, classId)

  fun getStudentCount(session: String): Flow<Int> =
    database.studentDao().getStudentCount(session)

  fun getActiveStudentCount(session: String): Flow<Int> =
    database.studentDao().getActiveStudentCount(session)

  suspend fun insertStudent(student: StudentEntity): Result<Long> {
    val admCount = database.studentDao().countByAdmissionNumber(student.admissionNumber)
    if (admCount > 0) {
      return Result.failure(Exception("ভর্তি নম্বর '${student.admissionNumber}' ইতিমধ্যে বিদ্যমান (Duplicate Admission No)"))
    }
    val rollCount = database.studentDao().countByRollNumber(student.academicSession, student.classId, student.rollNumber)
    if (rollCount > 0) {
      return Result.failure(Exception("এই শ্রেণীতে রোল '${student.rollNumber}' ইতিমধ্যে ব্যবহৃত (Duplicate Roll Number)"))
    }
    val id = database.studentDao().insertStudent(student)
    database.auditDao().insertLog(
      AuditLogEntity(action = "STUDENT_ADMISSION", user = "Staff", timestamp = getTodayDate(), details = "Admitted ${student.fullNameBn} (${student.studentId})")
    )
    return Result.success(id)
  }

  suspend fun updateStudent(student: StudentEntity) {
    database.studentDao().updateStudent(student)
  }

  suspend fun deleteStudent(student: StudentEntity) {
    database.studentDao().deleteStudent(student)
    database.auditDao().insertLog(
      AuditLogEntity(action = "STUDENT_DELETED", user = "Admin", timestamp = getTodayDate(), details = "Deleted ${student.studentId}")
    )
  }

  suspend fun authenticateGuardian(loginIdOrRoll: String, password: String): StudentEntity? {
    val roll = loginIdOrRoll.toIntOrNull() ?: -1
    return database.studentDao().getStudentForGuardian(loginIdOrRoll.trim(), roll, password.trim())
  }

  // --- Academic Sessions & Classes ---
  val allSessions: Flow<List<AcademicSessionEntity>> = database.sessionDao().getAllSessions()
  val activeSession: Flow<AcademicSessionEntity?> = database.sessionDao().getActiveSession()
  val allClasses: Flow<List<MadrasahClassEntity>> = database.classDao().getAllClasses()

  suspend fun addSession(name: String, startDate: String, endDate: String) {
    database.sessionDao().insertSession(AcademicSessionEntity(name = name, startDate = startDate, endDate = endDate))
  }

  suspend fun switchActiveSession(sessionId: Long) {
    database.sessionDao().deactivateAll()
    database.sessionDao().setActive(sessionId)
  }

  suspend fun addClass(nameBn: String, nameEn: String, code: String, orderIndex: Int) {
    database.classDao().insertClass(MadrasahClassEntity(nameBn = nameBn, nameEn = nameEn, code = code, orderIndex = orderIndex))
  }

  // --- Fee Management ---
  val feeCategories: Flow<List<FeeCategoryEntity>> = database.feeDao().getAllCategories()
  val allPayments: Flow<List<FeePaymentEntity>> = database.feeDao().getAllPayments()

  fun getFeeStructures(session: String, classId: Long): Flow<List<FeeStructureEntity>> =
    database.feeDao().getStructures(session, classId)

  fun getPaymentsBySession(session: String): Flow<List<FeePaymentEntity>> =
    database.feeDao().getPaymentsBySession(session)

  fun getPaymentsForStudent(studentId: String): Flow<List<FeePaymentEntity>> =
    database.feeDao().getPaymentsForStudent(studentId)

  fun getTodayFeeCollection(date: String): Flow<Double> =
    database.feeDao().getTodayCollection(date)

  fun getTotalFeeCollection(session: String): Flow<Double> =
    database.feeDao().getTotalCollection(session)

  fun getTotalFeeDue(session: String): Flow<Double> =
    database.feeDao().getTotalDue(session)

  suspend fun collectFee(
    payment: FeePaymentEntity,
    details: List<FeePaymentDetailEntity>
  ): Result<Long> {
    return try {
      val paymentId = database.feeDao().insertPayment(payment)
      val detailEntities = details.map { it.copy(paymentId = paymentId) }
      database.feeDao().insertPaymentDetails(detailEntities)

      database.accountsDao().insertIncome(
        IncomeEntity(
          receiptNumber = payment.receiptNumber,
          category = "Student Fee Collection",
          amount = payment.paidAmount,
          date = payment.date,
          academicSession = payment.academicSession,
          donorOrSource = "${payment.studentName} (ID: ${payment.studentId})",
          description = payment.feeDescription,
          paymentMethod = payment.paymentMethod,
          receivedBy = payment.receivedBy
        )
      )

      database.auditDao().insertLog(
        AuditLogEntity(action = "FEE_COLLECTION", user = payment.receivedBy, timestamp = payment.date, details = "Collected ৳${payment.paidAmount} (${payment.receiptNumber})")
      )

      Result.success(paymentId)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun getPaymentById(id: Long): FeePaymentEntity? =
    database.feeDao().getPaymentById(id)

  suspend fun getPaymentDetails(paymentId: Long): List<FeePaymentDetailEntity> =
    database.feeDao().getPaymentDetails(paymentId)

  // --- Accounts & Financials ---
  val allExpenses: Flow<List<ExpenseEntity>> = database.accountsDao().getAllExpenses()
  val allIncome: Flow<List<IncomeEntity>> = database.accountsDao().getAllIncome()

  fun getExpensesBySession(session: String): Flow<List<ExpenseEntity>> =
    database.accountsDao().getExpensesBySession(session)

  fun getTotalExpenses(session: String): Flow<Double> =
    database.accountsDao().getTotalExpenses(session)

  fun getIncomeBySession(session: String): Flow<List<IncomeEntity>> =
    database.accountsDao().getIncomeBySession(session)

  fun getTotalOtherIncome(session: String): Flow<Double> =
    database.accountsDao().getTotalOtherIncome(session)

  suspend fun recordExpense(expense: ExpenseEntity): Long {
    val id = database.accountsDao().insertExpense(expense)
    database.auditDao().insertLog(
      AuditLogEntity(action = "EXPENSE_RECORDED", user = expense.approvedBy, timestamp = expense.date, details = "Expense ৳${expense.amount} for ${expense.category}")
    )
    return id
  }

  suspend fun recordIncome(income: IncomeEntity): Long {
    val id = database.accountsDao().insertIncome(income)
    database.auditDao().insertLog(
      AuditLogEntity(action = "INCOME_RECORDED", user = income.receivedBy, timestamp = income.date, details = "Income ৳${income.amount} from ${income.donorOrSource}")
    )
    return id
  }

  // --- Employees & Salaries ---
  val allEmployees: Flow<List<EmployeeEntity>> = database.employeeDao().getAllEmployees()
  val allSalaryPayments: Flow<List<SalaryPaymentEntity>> = database.employeeDao().getAllSalaryPayments()

  suspend fun addEmployee(employee: EmployeeEntity): Long =
    database.employeeDao().insertEmployee(employee)

  suspend fun updateEmployee(employee: EmployeeEntity) =
    database.employeeDao().updateEmployee(employee)

  suspend fun deleteEmployee(employee: EmployeeEntity) =
    database.employeeDao().deleteEmployee(employee)

  suspend fun getEmployeeByEmail(email: String): EmployeeEntity? =
    database.employeeDao().getEmployeeByEmail(email)

  suspend fun disburseSalary(salary: SalaryPaymentEntity): Long {
    val id = database.employeeDao().insertSalaryPayment(salary)
    database.accountsDao().insertExpense(
      ExpenseEntity(
        voucherNumber = salary.voucherNumber,
        category = "Teacher Salaries",
        amount = salary.amount,
        date = salary.date,
        academicSession = "2026",
        description = "Salary for ${salary.employeeName} (${salary.monthYear})",
        recipient = salary.employeeName,
        paymentMethod = salary.paymentMethod
      )
    )
    return id
  }

  // --- Attendance ---
  fun getStudentAttendance(date: String, classId: Long): Flow<List<StudentAttendanceEntity>> =
    database.attendanceDao().getAttendanceByClassAndDate(date, classId)

  fun getTodayPresentStudents(date: String): Flow<Int> =
    database.attendanceDao().getTodayPresentCount(date)

  fun getTodayAbsentStudents(date: String): Flow<Int> =
    database.attendanceDao().getTodayAbsentCount(date)

  suspend fun markStudentAttendance(attendances: List<StudentAttendanceEntity>) {
    database.attendanceDao().insertStudentAttendances(attendances)
  }

  fun getStaffAttendance(date: String): Flow<List<StaffAttendanceEntity>> =
    database.attendanceDao().getStaffAttendanceByDate(date)

  suspend fun markStaffAttendance(attendances: List<StaffAttendanceEntity>) {
    database.attendanceDao().insertStaffAttendances(attendances)
  }

  // --- Examinations, Routine & Results ---
  val allExaminations: Flow<List<ExaminationEntity>> = database.examDao().getAllExaminations()
  val allSubjects: Flow<List<SubjectEntity>> = database.examDao().getAllSubjects()

  fun getSubjectsByClass(classId: Long): Flow<List<SubjectEntity>> =
    database.examDao().getSubjectsByClass(classId)

  fun getResultsByExamAndClass(examId: Long, classId: Long): Flow<List<ExamResultEntity>> =
    database.examDao().getResultsByExamAndClass(examId, classId)

  fun getResultsByStudent(examId: Long, studentId: String): Flow<List<ExamResultEntity>> =
    database.examDao().getResultsByStudent(examId, studentId)

  fun getSchedulesByExamAndClass(examId: Long, classId: Long): Flow<List<ExamScheduleEntity>> =
    database.examDao().getSchedulesByExamAndClass(examId, classId)

  suspend fun addExamination(exam: ExaminationEntity): Long =
    database.examDao().insertExamination(exam)

  suspend fun addSubject(subject: SubjectEntity): Long =
    database.examDao().insertSubject(subject)

  suspend fun updateSubject(subject: SubjectEntity) =
    database.examDao().updateSubject(subject)

  suspend fun deleteSubject(subject: SubjectEntity) =
    database.examDao().deleteSubject(subject)

  suspend fun saveExamResults(results: List<ExamResultEntity>) =
    database.examDao().insertResults(results)

  suspend fun addExamSchedule(schedule: ExamScheduleEntity): Long =
    database.examDao().insertSchedule(schedule)

  // --- Homework ---
  fun getHomeworkByClass(classId: Long): Flow<List<HomeworkEntity>> =
    database.homeworkDao().getHomeworkByClass(classId)

  val allHomework: Flow<List<HomeworkEntity>> = database.homeworkDao().getAllHomework()

  suspend fun insertHomework(homework: HomeworkEntity): Long =
    database.homeworkDao().insertHomework(homework)

  suspend fun deleteHomework(homework: HomeworkEntity) =
    database.homeworkDao().deleteHomework(homework)

  // --- Notices ---
  val allNotices: Flow<List<NoticeEntity>> = database.noticeDao().getAllNotices()

  suspend fun postNotice(notice: NoticeEntity): Long =
    database.noticeDao().insertNotice(notice)

  suspend fun deleteNotice(notice: NoticeEntity) =
    database.noticeDao().deleteNotice(notice)

  // --- Settings & Users ---
  val institutionSettings: Flow<InstitutionSettingsEntity?> = database.settingsDao().getSettings()
  val allUsers: Flow<List<UserEntity>> = database.userDao().getAllUsers()
  val recentLogs: Flow<List<AuditLogEntity>> = database.auditDao().getRecentLogs()

  suspend fun updateSettings(settings: InstitutionSettingsEntity) =
    database.settingsDao().updateSettings(settings)

  suspend fun getUserByPin(pin: String): UserEntity? =
    database.userDao().getUserByPin(pin)

  suspend fun authenticateUser(emailOrUsername: String, pin: String): UserEntity? =
    database.userDao().authenticateUser(emailOrUsername, pin)
}
