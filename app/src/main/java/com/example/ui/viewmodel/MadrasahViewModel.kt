package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.MadrasahDatabase
import com.example.data.entity.*
import com.example.data.model.UserRole
import com.example.data.repository.MadrasahRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class ERPScreen(val index: Int, val titleEn: String, val titleBn: String) {
  object Dashboard : ERPScreen(0, "Dashboard", "ড্যাশবোর্ড")
  object Students : ERPScreen(1, "Students", "শিক্ষার্থী")
  object Admissions : ERPScreen(2, "Admission", "নতুন ভর্তি")
  object FeeCollection : ERPScreen(3, "Fees & Receipts", "ফি কালেকশন ও রশিদ")
  object Accounts : ERPScreen(4, "Accounts & Finance", "হিসাব ও অর্থায়ন")
  object Teachers : ERPScreen(5, "Teachers & Staff", "শিক্ষক ও কর্মচারী")
  object Attendance : ERPScreen(6, "Attendance", "দৈনিক হাজিরা")
  object Examinations : ERPScreen(7, "Exams & Results", "পরীক্ষা ও ফলাফল")
  object Homework : ERPScreen(8, "Daily Homework", "বাড়ির কাজ (Homework)")
  object Notices : ERPScreen(9, "Notice Board", "নোটিশ বোর্ড")
  object GuardianPortal : ERPScreen(10, "Guardian Portal", "অভিভাবক পোর্টাল")
  object Settings : ERPScreen(11, "Settings", "প্রতিষ্ঠান সেটিংস")
}

class MadrasahViewModel(application: Application) : AndroidViewModel(application) {
  private val database = MadrasahDatabase.getDatabase(application, viewModelScope)
  val repository = MadrasahRepository(database)

  // Language state (true = Bengali, false = English)
  private val _isBengali = MutableStateFlow(true)
  val isBengali: StateFlow<Boolean> = _isBengali.asStateFlow()

  fun toggleLanguage() {
    _isBengali.value = !_isBengali.value
  }

  // Active User Role
  private val _currentRole = MutableStateFlow(UserRole.SUPER_ADMIN)
  val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

  fun switchRole(role: UserRole) {
    _currentRole.value = role
    if (role != UserRole.TEACHER) {
      _authenticatedTeacher.value = null
    }
  }

  // Teacher Email Authentication
  private val _authenticatedTeacher = MutableStateFlow<EmployeeEntity?>(null)
  val authenticatedTeacher: StateFlow<EmployeeEntity?> = _authenticatedTeacher.asStateFlow()

  fun loginTeacherWithEmail(email: String, pass: String, onResult: (Boolean, String) -> Unit) {
    viewModelScope.launch {
      val emp = repository.getEmployeeByEmail(email.trim())
      if (emp == null) {
        onResult(false, "এই ইমেইল দিয়ে কোনো শিক্ষক নিবন্ধিত নেই")
      } else if (emp.password != pass.trim()) {
        onResult(false, "ভুল পাসওয়ার্ড! অ্যাডমিনের সাথে যোগাযোগ করুন")
      } else {
        _authenticatedTeacher.value = emp
        _currentRole.value = UserRole.TEACHER
        onResult(true, "লগইন সফল: ${emp.nameBn}")
      }
    }
  }

  // Guardian Authentication
  private val _authenticatedGuardianStudent = MutableStateFlow<StudentEntity?>(null)
  val authenticatedGuardianStudent: StateFlow<StudentEntity?> = _authenticatedGuardianStudent.asStateFlow()

  fun loginGuardian(loginIdOrRoll: String, pass: String, onResult: (Boolean, String) -> Unit) {
    viewModelScope.launch {
      val student = repository.authenticateGuardian(loginIdOrRoll, pass)
      if (student != null) {
        _authenticatedGuardianStudent.value = student
        _currentScreen.value = ERPScreen.GuardianPortal
        onResult(true, "অভিভাবক লগইন সফল: ${student.fullNameBn}")
      } else {
        onResult(false, "শিক্ষার্থী আইডি/রোল অথবা পাসওয়ার্ড সঠিক নয়")
      }
    }
  }

  fun logoutGuardian() {
    _authenticatedGuardianStudent.value = null
    _currentScreen.value = ERPScreen.Dashboard
  }

  // Active Academic Session
  private val _currentSession = MutableStateFlow("2026")
  val currentSession: StateFlow<String> = _currentSession.asStateFlow()

  fun switchSession(session: String) {
    _currentSession.value = session
  }

  // Current Screen
  private val _currentScreen = MutableStateFlow<ERPScreen>(ERPScreen.Dashboard)
  val currentScreen: StateFlow<ERPScreen> = _currentScreen.asStateFlow()

  fun navigateTo(screen: ERPScreen) {
    _currentScreen.value = screen
  }

  // Global search
  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  fun onSearchQueryChanged(q: String) {
    _searchQuery.value = q
  }

  // Data streams
  val students: StateFlow<List<StudentEntity>> = repository.allStudents
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val classes: StateFlow<List<MadrasahClassEntity>> = repository.allClasses
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allSessions: StateFlow<List<AcademicSessionEntity>> = repository.allSessions
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val feeCategories: StateFlow<List<FeeCategoryEntity>> = repository.feeCategories
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val feePayments: StateFlow<List<FeePaymentEntity>> = repository.allPayments
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val expenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val incomes: StateFlow<List<IncomeEntity>> = repository.allIncome
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val employees: StateFlow<List<EmployeeEntity>> = repository.allEmployees
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val salaryPayments: StateFlow<List<SalaryPaymentEntity>> = repository.allSalaryPayments
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val examinations: StateFlow<List<ExaminationEntity>> = repository.allExaminations
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val subjects: StateFlow<List<SubjectEntity>> = repository.allSubjects
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val homeworkList: StateFlow<List<HomeworkEntity>> = repository.allHomework
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val notices: StateFlow<List<NoticeEntity>> = repository.allNotices
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val settings: StateFlow<InstitutionSettingsEntity?> = repository.institutionSettings
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  val recentLogs: StateFlow<List<AuditLogEntity>> = repository.recentLogs
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val todayCollection: StateFlow<Double> = repository.getTodayFeeCollection(repository.getTodayDate())
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

  val totalFeeCollection: StateFlow<Double> = repository.getTotalFeeCollection("2026")
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

  val totalFeeDue: StateFlow<Double> = repository.getTotalFeeDue("2026")
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

  val totalExpenses: StateFlow<Double> = repository.getTotalExpenses("2026")
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

  val totalOtherIncome: StateFlow<Double> = repository.getTotalOtherIncome("2026")
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

  val activeStudentCount: StateFlow<Int> = repository.getActiveStudentCount("2026")
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  val todayPresentCount: StateFlow<Int> = repository.getTodayPresentStudents(repository.getTodayDate())
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  val todayAbsentCount: StateFlow<Int> = repository.getTodayAbsentStudents(repository.getTodayDate())
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  // Last generated Receipt for preview/print
  private val _lastPaymentReceipt = MutableStateFlow<Pair<FeePaymentEntity, List<FeePaymentDetailEntity>>?>(null)
  val lastPaymentReceipt: StateFlow<Pair<FeePaymentEntity, List<FeePaymentDetailEntity>>?> = _lastPaymentReceipt.asStateFlow()

  fun showReceipt(payment: FeePaymentEntity) {
    viewModelScope.launch {
      val details = repository.getPaymentDetails(payment.id)
      _lastPaymentReceipt.value = Pair(payment, details)
    }
  }

  fun dismissReceipt() {
    _lastPaymentReceipt.value = null
  }

  // --- Student operations ---
  fun addStudent(student: StudentEntity, onComplete: (Result<Long>) -> Unit) {
    viewModelScope.launch {
      val res = repository.insertStudent(student)
      onComplete(res)
    }
  }

  fun updateStudent(student: StudentEntity) {
    viewModelScope.launch {
      repository.updateStudent(student)
    }
  }

  fun deleteStudent(student: StudentEntity) {
    viewModelScope.launch {
      repository.deleteStudent(student)
    }
  }

  // --- Fee collection ---
  fun collectFee(
    payment: FeePaymentEntity,
    details: List<FeePaymentDetailEntity>,
    onComplete: (Result<Long>) -> Unit
  ) {
    viewModelScope.launch {
      val res = repository.collectFee(payment, details)
      if (res.isSuccess) {
        val paymentId = res.getOrNull() ?: 0L
        val savedPayment = repository.getPaymentById(paymentId) ?: payment
        val savedDetails = repository.getPaymentDetails(paymentId)
        _lastPaymentReceipt.value = Pair(savedPayment, savedDetails)
      }
      onComplete(res)
    }
  }

  // --- Accounts operations ---
  fun recordExpense(expense: ExpenseEntity, onComplete: () -> Unit) {
    viewModelScope.launch {
      repository.recordExpense(expense)
      onComplete()
    }
  }

  fun recordIncome(income: IncomeEntity, onComplete: () -> Unit) {
    viewModelScope.launch {
      repository.recordIncome(income)
      onComplete()
    }
  }

  // --- Employee operations ---
  fun addEmployee(employee: EmployeeEntity, onComplete: () -> Unit) {
    viewModelScope.launch {
      repository.addEmployee(employee)
      onComplete()
    }
  }

  fun updateEmployee(employee: EmployeeEntity, onComplete: () -> Unit) {
    viewModelScope.launch {
      repository.updateEmployee(employee)
      onComplete()
    }
  }

  fun disburseSalary(salary: SalaryPaymentEntity, onComplete: () -> Unit) {
    viewModelScope.launch {
      repository.disburseSalary(salary)
      onComplete()
    }
  }

  // --- Attendance ---
  fun markStudentAttendance(attendances: List<StudentAttendanceEntity>, onComplete: () -> Unit) {
    viewModelScope.launch {
      repository.markStudentAttendance(attendances)
      onComplete()
    }
  }

  fun markStaffAttendance(attendances: List<StaffAttendanceEntity>, onComplete: () -> Unit) {
    viewModelScope.launch {
      repository.markStaffAttendance(attendances)
      onComplete()
    }
  }

  // --- Examinations & Subjects ---
  fun addExamination(exam: ExaminationEntity, onComplete: () -> Unit) {
    viewModelScope.launch {
      repository.addExamination(exam)
      onComplete()
    }
  }

  fun addSubject(subject: SubjectEntity, onComplete: () -> Unit) {
    viewModelScope.launch {
      repository.addSubject(subject)
      onComplete()
    }
  }

  fun updateSubject(subject: SubjectEntity, onComplete: () -> Unit) {
    viewModelScope.launch {
      repository.updateSubject(subject)
      onComplete()
    }
  }

  fun saveExamResults(results: List<ExamResultEntity>, onComplete: () -> Unit) {
    viewModelScope.launch {
      repository.saveExamResults(results)
      onComplete()
    }
  }

  fun addExamSchedule(schedule: ExamScheduleEntity, onComplete: () -> Unit) {
    viewModelScope.launch {
      repository.addExamSchedule(schedule)
      onComplete()
    }
  }

  // --- Homework ---
  fun postHomework(hw: HomeworkEntity, onComplete: () -> Unit) {
    viewModelScope.launch {
      repository.insertHomework(hw)
      onComplete()
    }
  }

  fun deleteHomework(hw: HomeworkEntity) {
    viewModelScope.launch {
      repository.deleteHomework(hw)
    }
  }

  // --- Notices ---
  fun postNotice(notice: NoticeEntity, onComplete: () -> Unit) {
    viewModelScope.launch {
      repository.postNotice(notice)
      onComplete()
    }
  }

  fun deleteNotice(notice: NoticeEntity) {
    viewModelScope.launch {
      repository.deleteNotice(notice)
    }
  }

  // --- Settings ---
  fun updateSettings(settings: InstitutionSettingsEntity, onComplete: () -> Unit) {
    viewModelScope.launch {
      repository.updateSettings(settings)
      onComplete()
    }
  }

  fun addClass(nameBn: String, nameEn: String, code: String, orderIndex: Int) {
    viewModelScope.launch {
      repository.addClass(nameBn, nameEn, code, orderIndex)
    }
  }

  fun addSession(name: String, startDate: String, endDate: String) {
    viewModelScope.launch {
      repository.addSession(name, startDate, endDate)
    }
  }

  fun switchActiveSession(sessionId: Long, sessionName: String) {
    viewModelScope.launch {
      repository.switchActiveSession(sessionId)
      _currentSession.value = sessionName
    }
  }
}
