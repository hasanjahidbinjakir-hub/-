package com.example.data.model

enum class UserRole(val titleEn: String, val titleBn: String) {
  SUPER_ADMIN("Super Administrator", "প্রধান প্রশাসক"),
  PRINCIPAL("Principal", "মুহতামিম / অধ্যক্ষ"),
  DIRECTOR("Director", "পরিচালক"),
  ACCOUNTANT("Accountant", "হিসাবরক্ষক"),
  TEACHER("Teacher", "মুদাররিস / শিক্ষক"),
  ATTENDANCE_OPERATOR("Attendance Operator", "হাজিরা অপারেটর")
}

enum class StudentStatus(val titleEn: String, val titleBn: String) {
  ACTIVE("Active", "নিয়মিত"),
  INACTIVE("Inactive", "অনিয়মিত"),
  TRANSFERRED("Transferred", "ছাড়পত্রপ্রাপ্ত"),
  GRADUATED("Graduated", "সমাপ্ত")
}

enum class AttendanceStatus(val code: String, val titleEn: String, val titleBn: String) {
  PRESENT("P", "Present", "উপস্থিত"),
  ABSENT("A", "Absent", "অনুপস্থিত"),
  LATE("L", "Late", "বিলম্ব"),
  LEAVE("V", "Leave", "ছুটি")
}

enum class PaymentMethod(val titleEn: String, val titleBn: String) {
  CASH("Cash", "নগদ"),
  BKASH("bKash", "বিকাশ"),
  NAGAD("Nagad", "নগদ (MFS)"),
  ROCKET("Rocket", "রকেট"),
  BANK("Bank Transfer", "ব্যাংক জমা")
}

enum class FeeType {
  RECURRING_MONTHLY,
  ONE_TIME
}
