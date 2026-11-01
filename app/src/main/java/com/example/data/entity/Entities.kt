package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val username: String,
  val email: String = "",
  val pin: String, // Quick PIN / Password
  val fullName: String,
  val role: String, // UserRole enum name
  val phone: String = "",
  val photoUri: String = "",
  val isActive: Boolean = true
)

@Entity(tableName = "academic_sessions")
data class AcademicSessionEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String, // e.g. "2026", "2027"
  val isActive: Boolean = false,
  val startDate: String = "",
  val endDate: String = ""
)

@Entity(tableName = "classes")
data class MadrasahClassEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val nameBn: String,
  val nameEn: String,
  val code: String,
  val orderIndex: Int = 0
)

@Entity(
  tableName = "students",
  indices = [
    Index(value = ["admissionNumber"], unique = true),
    Index(value = ["academicSession", "classId", "rollNumber"], unique = true)
  ]
)
data class StudentEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val studentId: String, // e.g. "JNGM-2026-001"
  val admissionNumber: String,
  val fullNameBn: String,
  val fullNameEn: String,
  val fatherName: String,
  val motherName: String,
  val guardianName: String,
  val guardianPhone: String,
  val altPhone: String = "",
  val dateOfBirth: String,
  val gender: String = "Female", // "Female" (ছাত্রী) or "Male" (ছাত্র) - Play to Class 5 allows both!
  val bloodGroup: String = "",
  val presentAddress: String,
  val permanentAddress: String,
  val admissionDate: String,
  val academicSession: String, // "2026"
  val classId: Long,
  val className: String,
  val section: String = "A",
  val rollNumber: Int,
  val previousInstitution: String = "",
  val specialNotes: String = "",
  val status: String = "ACTIVE",
  val photoUri: String = "", // Student photograph URI
  val guardianLoginId: String = "", // Guardian username e.g. JNGM-2026-001
  val guardianPassword: String = "1234" // Guardian easy PIN
)

@Entity(tableName = "fee_categories")
data class FeeCategoryEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val nameBn: String,
  val nameEn: String,
  val type: String = "RECURRING_MONTHLY",
  val defaultAmount: Double = 0.0
)

@Entity(tableName = "fee_structures")
data class FeeStructureEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val classId: Long,
  val feeCategoryId: Long,
  val academicSession: String,
  val amount: Double
)

@Entity(
  tableName = "fee_payments",
  indices = [
    Index(value = ["receiptNumber"], unique = true),
    Index(value = ["transactionId"], unique = true)
  ]
)
data class FeePaymentEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val receiptNumber: String,
  val transactionId: String,
  val studentId: String,
  val studentName: String,
  val classId: Long,
  val className: String,
  val rollNumber: Int,
  val academicSession: String,
  val date: String, // DD-MM-YYYY
  val monthFor: String = "", // e.g. "October 2026"
  val totalAmount: Double,
  val discountAmount: Double = 0.0,
  val paidAmount: Double,
  val dueAmount: Double = 0.0,
  val paymentMethod: String = "CASH",
  val feeDescription: String = "",
  val receivedBy: String = "Accountant",
  val remarks: String = ""
)

@Entity(tableName = "payment_details")
data class FeePaymentDetailEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val paymentId: Long,
  val feeCategoryId: Long,
  val feeCategoryName: String,
  val amount: Double
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val voucherNumber: String,
  val category: String,
  val amount: Double,
  val date: String, // DD-MM-YYYY
  val academicSession: String,
  val description: String,
  val recipient: String,
  val paymentMethod: String = "CASH",
  val approvedBy: String = "Principal"
)

@Entity(tableName = "income")
data class IncomeEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val receiptNumber: String,
  val category: String,
  val amount: Double,
  val date: String, // DD-MM-YYYY
  val academicSession: String,
  val donorOrSource: String,
  val description: String = "",
  val paymentMethod: String = "CASH",
  val receivedBy: String = "Accountant"
)

@Entity(tableName = "employees")
data class EmployeeEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val employeeId: String, // e.g. "EMP-01"
  val nameBn: String,
  val nameEn: String,
  val email: String = "", // Admin sets email, teacher logs in with this email
  val password: String = "1234", // Password/PIN set by admin
  val designation: String,
  val department: String,
  val phone: String,
  val address: String,
  val joiningDate: String,
  val monthlySalary: Double,
  val photoUri: String = "", // Teacher photo
  val status: String = "Active"
)

@Entity(tableName = "salary_payments")
data class SalaryPaymentEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val voucherNumber: String,
  val employeeId: String,
  val employeeName: String,
  val monthYear: String, // e.g. "October 2026"
  val amount: Double,
  val date: String,
  val paymentMethod: String = "CASH",
  val remarks: String = ""
)

@Entity(
  tableName = "student_attendance",
  indices = [
    Index(value = ["date", "studentId", "academicSession"], unique = true)
  ]
)
data class StudentAttendanceEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val date: String, // DD-MM-YYYY
  val studentId: String,
  val studentName: String,
  val classId: Long,
  val rollNumber: Int,
  val academicSession: String,
  val status: String // "PRESENT", "ABSENT", "LATE", "LEAVE"
)

@Entity(
  tableName = "staff_attendance",
  indices = [
    Index(value = ["date", "employeeId"], unique = true)
  ]
)
data class StaffAttendanceEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val date: String,
  val employeeId: String,
  val employeeName: String,
  val status: String
)

@Entity(tableName = "examinations")
data class ExaminationEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String, // e.g. "First Term Examination 2026"
  val academicSession: String,
  val startDate: String,
  val endDate: String,
  val isPublished: Boolean = false
)

@Entity(tableName = "subjects")
data class SubjectEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val classId: Long,
  val nameBn: String,
  val nameEn: String,
  val code: String,
  val maxMarks: Double = 100.0,
  val passMarks: Double = 33.0
)

@Entity(
  tableName = "exam_results",
  indices = [
    Index(value = ["examId", "studentId", "subjectId"], unique = true)
  ]
)
data class ExamResultEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val examId: Long,
  val studentId: String,
  val classId: Long,
  val subjectId: Long,
  val subjectName: String,
  val marksObtained: Double,
  val grade: String, // A+, A, A-, B, C, D, F
  val gpa: Double // 5.0, 4.0, etc.
)

@Entity(tableName = "exam_schedules")
data class ExamScheduleEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val examId: Long,
  val classId: Long,
  val subjectId: Long,
  val subjectName: String,
  val examDate: String, // DD-MM-YYYY
  val startTime: String = "10:00 AM",
  val endTime: String = "01:00 PM",
  val roomNo: String = "101"
)

@Entity(tableName = "homework")
data class HomeworkEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val classId: Long,
  val className: String,
  val subjectId: Long,
  val subjectName: String,
  val date: String, // DD-MM-YYYY
  val title: String,
  val description: String,
  val assignedBy: String,
  val submissionDate: String
)

@Entity(tableName = "notices")
data class NoticeEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val titleBn: String,
  val titleEn: String,
  val contentBn: String,
  val contentEn: String,
  val category: String, // General, Academic, Holiday, Exam, Fees
  val publishDate: String,
  val isUrgent: Boolean = false
)

@Entity(tableName = "institution_settings")
data class InstitutionSettingsEntity(
  @PrimaryKey val id: Long = 1,
  val institutionNameBn: String = "জাবালে নূর গার্লস মাদ্রাসা",
  val institutionNameEn: String = "Jabale Noor Girls Madrasah",
  val sloganBn: String = "এসো, আলোকিত হও, আলোকিত কর বিশ্ব",
  val locationBn: String = "রামগড়, খাগড়াছড়ি, বাংলাদেশ",
  val locationEn: String = "Ramgarh, Khagrachhari, Bangladesh",
  val estdYear: String = "2023",
  val shortCode: String = "J.N.G.M",
  val phone: String = "+880 1812-345678",
  val email: String = "contact@jabalenoor.edu.bd",
  val portalUrl: String = "https://jabalenoor.edu.bd/portal",
  val currencySymbol: String = "৳",
  val currentSession: String = "2026"
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val action: String,
  val user: String,
  val timestamp: String,
  val details: String = ""
)
