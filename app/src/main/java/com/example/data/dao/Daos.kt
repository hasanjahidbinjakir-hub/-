package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
  @Query("SELECT * FROM students ORDER BY rollNumber ASC")
  fun getAllStudents(): Flow<List<StudentEntity>>

  @Query("SELECT * FROM students WHERE academicSession = :session ORDER BY classId ASC, rollNumber ASC")
  fun getStudentsBySession(session: String): Flow<List<StudentEntity>>

  @Query("SELECT * FROM students WHERE academicSession = :session AND classId = :classId ORDER BY rollNumber ASC")
  fun getStudentsByClass(session: String, classId: Long): Flow<List<StudentEntity>>

  @Query("SELECT * FROM students WHERE studentId = :studentId LIMIT 1")
  suspend fun getStudentByStudentId(studentId: String): StudentEntity?

  @Query("SELECT * FROM students WHERE (studentId = :loginId OR rollNumber = :rollOrId) AND guardianPassword = :password LIMIT 1")
  suspend fun getStudentForGuardian(loginId: String, rollOrId: Int, password: String): StudentEntity?

  @Query("SELECT COUNT(*) FROM students WHERE academicSession = :session")
  fun getStudentCount(session: String): Flow<Int>

  @Query("SELECT COUNT(*) FROM students WHERE academicSession = :session AND status = 'ACTIVE'")
  fun getActiveStudentCount(session: String): Flow<Int>

  @Query("SELECT COUNT(*) FROM students WHERE admissionNumber = :admNo")
  suspend fun countByAdmissionNumber(admNo: String): Int

  @Query("SELECT COUNT(*) FROM students WHERE academicSession = :session AND classId = :classId AND rollNumber = :roll")
  suspend fun countByRollNumber(session: String, classId: Long, roll: Int): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertStudent(student: StudentEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertStudents(students: List<StudentEntity>)

  @Update
  suspend fun updateStudent(student: StudentEntity)

  @Delete
  suspend fun deleteStudent(student: StudentEntity)
}

@Dao
interface AcademicSessionDao {
  @Query("SELECT * FROM academic_sessions ORDER BY name DESC")
  fun getAllSessions(): Flow<List<AcademicSessionEntity>>

  @Query("SELECT * FROM academic_sessions WHERE isActive = 1 LIMIT 1")
  fun getActiveSession(): Flow<AcademicSessionEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSession(session: AcademicSessionEntity): Long

  @Query("UPDATE academic_sessions SET isActive = 0")
  suspend fun deactivateAll()

  @Query("UPDATE academic_sessions SET isActive = 1 WHERE id = :sessionId")
  suspend fun setActive(sessionId: Long)
}

@Dao
interface MadrasahClassDao {
  @Query("SELECT * FROM classes ORDER BY orderIndex ASC")
  fun getAllClasses(): Flow<List<MadrasahClassEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertClass(madrasahClass: MadrasahClassEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertClasses(classes: List<MadrasahClassEntity>)

  @Delete
  suspend fun deleteClass(madrasahClass: MadrasahClassEntity)
}

@Dao
interface FeeDao {
  @Query("SELECT * FROM fee_categories ORDER BY id ASC")
  fun getAllCategories(): Flow<List<FeeCategoryEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCategories(categories: List<FeeCategoryEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCategory(category: FeeCategoryEntity): Long

  @Query("SELECT * FROM fee_structures WHERE academicSession = :session AND classId = :classId")
  fun getStructures(session: String, classId: Long): Flow<List<FeeStructureEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertStructure(structure: FeeStructureEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertStructures(structures: List<FeeStructureEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPayment(payment: FeePaymentEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPaymentDetails(details: List<FeePaymentDetailEntity>)

  @Query("SELECT * FROM fee_payments ORDER BY id DESC")
  fun getAllPayments(): Flow<List<FeePaymentEntity>>

  @Query("SELECT * FROM fee_payments WHERE academicSession = :session ORDER BY id DESC")
  fun getPaymentsBySession(session: String): Flow<List<FeePaymentEntity>>

  @Query("SELECT * FROM fee_payments WHERE studentId = :studentId ORDER BY id DESC")
  fun getPaymentsForStudent(studentId: String): Flow<List<FeePaymentEntity>>

  @Query("SELECT * FROM fee_payments WHERE id = :id LIMIT 1")
  suspend fun getPaymentById(id: Long): FeePaymentEntity?

  @Query("SELECT * FROM payment_details WHERE paymentId = :paymentId")
  suspend fun getPaymentDetails(paymentId: Long): List<FeePaymentDetailEntity>

  @Query("SELECT COALESCE(SUM(paidAmount), 0.0) FROM fee_payments WHERE date = :date")
  fun getTodayCollection(date: String): Flow<Double>

  @Query("SELECT COALESCE(SUM(paidAmount), 0.0) FROM fee_payments WHERE academicSession = :session")
  fun getTotalCollection(session: String): Flow<Double>

  @Query("SELECT COALESCE(SUM(dueAmount), 0.0) FROM fee_payments WHERE academicSession = :session")
  fun getTotalDue(session: String): Flow<Double>
}

@Dao
interface AccountsDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertExpense(expense: ExpenseEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertExpenses(expenses: List<ExpenseEntity>)

  @Query("SELECT * FROM expenses ORDER BY id DESC")
  fun getAllExpenses(): Flow<List<ExpenseEntity>>

  @Query("SELECT * FROM expenses WHERE academicSession = :session ORDER BY id DESC")
  fun getExpensesBySession(session: String): Flow<List<ExpenseEntity>>

  @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE academicSession = :session")
  fun getTotalExpenses(session: String): Flow<Double>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertIncome(income: IncomeEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertIncomes(incomes: List<IncomeEntity>)

  @Query("SELECT * FROM income ORDER BY id DESC")
  fun getAllIncome(): Flow<List<IncomeEntity>>

  @Query("SELECT * FROM income WHERE academicSession = :session ORDER BY id DESC")
  fun getIncomeBySession(session: String): Flow<List<IncomeEntity>>

  @Query("SELECT COALESCE(SUM(amount), 0.0) FROM income WHERE academicSession = :session")
  fun getTotalOtherIncome(session: String): Flow<Double>
}

@Dao
interface EmployeeDao {
  @Query("SELECT * FROM employees ORDER BY id ASC")
  fun getAllEmployees(): Flow<List<EmployeeEntity>>

  @Query("SELECT * FROM employees WHERE email = :email LIMIT 1")
  suspend fun getEmployeeByEmail(email: String): EmployeeEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertEmployee(employee: EmployeeEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertEmployees(employees: List<EmployeeEntity>)

  @Update
  suspend fun updateEmployee(employee: EmployeeEntity)

  @Delete
  suspend fun deleteEmployee(employee: EmployeeEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSalaryPayment(payment: SalaryPaymentEntity): Long

  @Query("SELECT * FROM salary_payments ORDER BY id DESC")
  fun getAllSalaryPayments(): Flow<List<SalaryPaymentEntity>>
}

@Dao
interface AttendanceDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertStudentAttendance(attendance: StudentAttendanceEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertStudentAttendances(attendances: List<StudentAttendanceEntity>)

  @Query("SELECT * FROM student_attendance WHERE date = :date AND classId = :classId")
  fun getAttendanceByClassAndDate(date: String, classId: Long): Flow<List<StudentAttendanceEntity>>

  @Query("SELECT * FROM student_attendance WHERE studentId = :studentId ORDER BY id DESC")
  fun getAttendanceForStudent(studentId: String): Flow<List<StudentAttendanceEntity>>

  @Query("SELECT COUNT(*) FROM student_attendance WHERE date = :date AND status = 'PRESENT'")
  fun getTodayPresentCount(date: String): Flow<Int>

  @Query("SELECT COUNT(*) FROM student_attendance WHERE date = :date AND status = 'ABSENT'")
  fun getTodayAbsentCount(date: String): Flow<Int>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertStaffAttendance(attendance: StaffAttendanceEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertStaffAttendances(attendances: List<StaffAttendanceEntity>)

  @Query("SELECT * FROM staff_attendance WHERE date = :date")
  fun getStaffAttendanceByDate(date: String): Flow<List<StaffAttendanceEntity>>
}

@Dao
interface ExaminationDao {
  @Query("SELECT * FROM examinations ORDER BY id DESC")
  fun getAllExaminations(): Flow<List<ExaminationEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertExamination(exam: ExaminationEntity): Long

  @Query("SELECT * FROM subjects ORDER BY classId ASC, id ASC")
  fun getAllSubjects(): Flow<List<SubjectEntity>>

  @Query("SELECT * FROM subjects WHERE classId = :classId ORDER BY id ASC")
  fun getSubjectsByClass(classId: Long): Flow<List<SubjectEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSubject(subject: SubjectEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSubjects(subjects: List<SubjectEntity>)

  @Update
  suspend fun updateSubject(subject: SubjectEntity)

  @Delete
  suspend fun deleteSubject(subject: SubjectEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertResult(result: ExamResultEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertResults(results: List<ExamResultEntity>)

  @Query("SELECT * FROM exam_results WHERE examId = :examId AND classId = :classId")
  fun getResultsByExamAndClass(examId: Long, classId: Long): Flow<List<ExamResultEntity>>

  @Query("SELECT * FROM exam_results WHERE examId = :examId AND studentId = :studentId")
  fun getResultsByStudent(examId: Long, studentId: String): Flow<List<ExamResultEntity>>

  // Exam Schedules
  @Query("SELECT * FROM exam_schedules WHERE examId = :examId AND classId = :classId ORDER BY examDate ASC")
  fun getSchedulesByExamAndClass(examId: Long, classId: Long): Flow<List<ExamScheduleEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSchedule(schedule: ExamScheduleEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSchedules(schedules: List<ExamScheduleEntity>)
}

@Dao
interface HomeworkDao {
  @Query("SELECT * FROM homework WHERE classId = :classId ORDER BY date DESC")
  fun getHomeworkByClass(classId: Long): Flow<List<HomeworkEntity>>

  @Query("SELECT * FROM homework ORDER BY id DESC")
  fun getAllHomework(): Flow<List<HomeworkEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertHomework(homework: HomeworkEntity): Long

  @Delete
  suspend fun deleteHomework(homework: HomeworkEntity)
}

@Dao
interface NoticeDao {
  @Query("SELECT * FROM notices ORDER BY id DESC")
  fun getAllNotices(): Flow<List<NoticeEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNotice(notice: NoticeEntity): Long

  @Delete
  suspend fun deleteNotice(notice: NoticeEntity)
}

@Dao
interface SettingsDao {
  @Query("SELECT * FROM institution_settings WHERE id = 1 LIMIT 1")
  fun getSettings(): Flow<InstitutionSettingsEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun updateSettings(settings: InstitutionSettingsEntity)
}

@Dao
interface UserDao {
  @Query("SELECT * FROM users ORDER BY id ASC")
  fun getAllUsers(): Flow<List<UserEntity>>

  @Query("SELECT * FROM users WHERE (email = :emailOrUsername OR username = :emailOrUsername) AND pin = :pin AND isActive = 1 LIMIT 1")
  suspend fun authenticateUser(emailOrUsername: String, pin: String): UserEntity?

  @Query("SELECT * FROM users WHERE pin = :pin AND isActive = 1 LIMIT 1")
  suspend fun getUserByPin(pin: String): UserEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUser(user: UserEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUsers(users: List<UserEntity>)
}

@Dao
interface AuditDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLog(log: AuditLogEntity): Long

  @Query("SELECT * FROM audit_logs ORDER BY id DESC LIMIT 50")
  fun getRecentLogs(): Flow<List<AuditLogEntity>>
}
