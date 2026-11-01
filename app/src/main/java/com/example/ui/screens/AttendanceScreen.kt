package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.StaffAttendanceEntity
import com.example.data.entity.StudentAttendanceEntity
import com.example.data.model.AttendanceStatus
import com.example.ui.theme.*
import com.example.ui.viewmodel.MadrasahViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AttendanceScreen(
  viewModel: MadrasahViewModel,
  modifier: Modifier = Modifier
) {
  val isBengali by viewModel.isBengali.collectAsState()
  val classes by viewModel.classes.collectAsState()
  val students by viewModel.students.collectAsState()
  val employees by viewModel.employees.collectAsState()
  val currentSession by viewModel.currentSession.collectAsState()
  val context = LocalContext.current

  val todayDate = remember { SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date()) }

  var selectedTab by remember { mutableStateOf(0) } // 0 = Student Attendance, 1 = Staff Attendance
  var selectedClassId by remember { mutableStateOf<Long?>(classes.firstOrNull()?.id ?: 3L) }

  // Map of studentId to AttendanceStatus
  val studentAttendanceMap = remember { mutableStateMapOf<String, AttendanceStatus>() }
  val staffAttendanceMap = remember { mutableStateMapOf<String, AttendanceStatus>() }

  val studentsInSelectedClass = remember(students, selectedClassId) {
    if (selectedClassId == null) emptyList() else students.filter { it.classId == selectedClassId }
  }

  // Pre-fill student attendance as PRESENT by default
  LaunchedEffect(studentsInSelectedClass) {
    studentsInSelectedClass.forEach { s ->
      if (!studentAttendanceMap.containsKey(s.studentId)) {
        studentAttendanceMap[s.studentId] = AttendanceStatus.PRESENT
      }
    }
  }

  // Pre-fill staff attendance as PRESENT by default
  LaunchedEffect(employees) {
    employees.forEach { emp ->
      if (!staffAttendanceMap.containsKey(emp.employeeId)) {
        staffAttendanceMap[emp.employeeId] = AttendanceStatus.PRESENT
      }
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = if (isBengali) "দৈনিক হাজিরা ব্যবস্থাপনা" else "Attendance Management",
          fontWeight = FontWeight.Bold,
          fontSize = 17.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = if (isBengali) "তারিখ: $todayDate • সেশন: $currentSession" else "Date: $todayDate • Session: $currentSession",
          fontSize = 12.sp,
          color = Slate600
        )
      }

      Surface(
        shape = RoundedCornerShape(8.dp),
        color = EmeraldContainer
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Today, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(text = todayDate, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldOnContainer)
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    TabRow(selectedTabIndex = selectedTab) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = { Text(if (isBengali) "ছাত্রী হাজিরা" else "Student Attendance") }
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = { Text(if (isBengali) "শিক্ষক ও কর্মী হাজিরা" else "Staff Attendance") }
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    if (selectedTab == 0) {
      // Class selector chips
      Text(text = if (isBengali) "শ্রেণি নির্বাচন করুন:" else "Select Class:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
      Spacer(modifier = Modifier.height(6.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        classes.take(4).forEach { c ->
          val isSel = selectedClassId == c.id
          FilterChip(
            selected = isSel,
            onClick = { selectedClassId = c.id },
            label = { Text(if (isBengali) c.nameBn else c.nameEn, fontSize = 11.sp) }
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Quick Mark All Present Button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (isBengali) "ছাত্রীর তালিকা (${studentsInSelectedClass.size} জন)" else "Students (${studentsInSelectedClass.size})",
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp
        )
        TextButton(
          onClick = {
            studentsInSelectedClass.forEach { s ->
              studentAttendanceMap[s.studentId] = AttendanceStatus.PRESENT
            }
          }
        ) {
          Text(if (isBengali) "সকলকে উপস্থিত করুন" else "Mark All Present", fontSize = 11.sp, color = EmeraldPrimary)
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Student attendance list
      if (studentsInSelectedClass.isEmpty()) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
          Text(text = if (isBengali) "এই শ্রেণীতে কোনো ছাত্রী নথিভুক্ত নেই" else "No students enrolled in this class")
        }
      } else {
        LazyColumn(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          items(studentsInSelectedClass) { student ->
            val status = studentAttendanceMap[student.studentId] ?: AttendanceStatus.PRESENT

            Card(
              shape = RoundedCornerShape(10.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              modifier = Modifier.fillMaxWidth(),
              elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(text = "${student.rollNumber}. ${student.fullNameBn}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                  Text(text = "আইডি: ${student.studentId}", fontSize = 11.sp, color = Slate600)
                }

                // Status Toggle Buttons: P, A, L, V
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  AttendanceStatus.values().forEach { st ->
                    val isCurrent = status == st
                    val bgColor = when (st) {
                      AttendanceStatus.PRESENT -> if (isCurrent) EmeraldPrimary else Slate100
                      AttendanceStatus.ABSENT -> if (isCurrent) CrimsonError else Slate100
                      AttendanceStatus.LATE -> if (isCurrent) GoldAccent else Slate100
                      AttendanceStatus.LEAVE -> if (isCurrent) Color(0xFF6366F1) else Slate100
                    }
                    val textColor = if (isCurrent) Color.White else Slate700

                    Surface(
                      shape = RoundedCornerShape(6.dp),
                      color = bgColor,
                      modifier = Modifier
                        .size(32.dp)
                        .clickable { studentAttendanceMap[student.studentId] = st }
                    ) {
                      Box(contentAlignment = Alignment.Center) {
                        Text(
                          text = st.code,
                          color = textColor,
                          fontWeight = FontWeight.Bold,
                          fontSize = 12.sp
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Save Student Attendance Button
      Button(
        onClick = {
          val records = studentsInSelectedClass.map { s ->
            val st = studentAttendanceMap[s.studentId] ?: AttendanceStatus.PRESENT
            StudentAttendanceEntity(
              date = todayDate,
              studentId = s.studentId,
              studentName = s.fullNameBn,
              classId = s.classId,
              rollNumber = s.rollNumber,
              academicSession = currentSession,
              status = st.name
            )
          }
          viewModel.markStudentAttendance(records) {
            Toast.makeText(context, if (isBengali) "হাজিরা সফলভাবে সংরক্ষিত হয়েছে!" else "Attendance saved successfully!", Toast.LENGTH_SHORT).show()
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("save_student_attendance_button")
      ) {
        Icon(Icons.Default.Check, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(if (isBengali) "শ্রেণির হাজিরা শিট সংরক্ষণ করুন" else "Save Class Attendance Sheet")
      }
    } else {
      // Staff Attendance
      LazyColumn(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        items(employees) { emp ->
          val status = staffAttendanceMap[emp.employeeId] ?: AttendanceStatus.PRESENT

          Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(text = emp.nameBn, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = "${emp.designation} • ${emp.employeeId}", fontSize = 11.sp, color = Slate600)
              }

              Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                AttendanceStatus.values().forEach { st ->
                  val isCurrent = status == st
                  val bgColor = when (st) {
                    AttendanceStatus.PRESENT -> if (isCurrent) EmeraldPrimary else Slate100
                    AttendanceStatus.ABSENT -> if (isCurrent) CrimsonError else Slate100
                    AttendanceStatus.LATE -> if (isCurrent) GoldAccent else Slate100
                    AttendanceStatus.LEAVE -> if (isCurrent) Color(0xFF6366F1) else Slate100
                  }
                  val textColor = if (isCurrent) Color.White else Slate700

                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = bgColor,
                    modifier = Modifier
                      .size(32.dp)
                      .clickable { staffAttendanceMap[emp.employeeId] = st }
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Text(text = st.code, color = textColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                  }
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Button(
        onClick = {
          val records = employees.map { emp ->
            val st = staffAttendanceMap[emp.employeeId] ?: AttendanceStatus.PRESENT
            StaffAttendanceEntity(
              date = todayDate,
              employeeId = emp.employeeId,
              employeeName = emp.nameBn,
              status = st.name
            )
          }
          viewModel.markStaffAttendance(records) {
            Toast.makeText(context, if (isBengali) "কর্মচারীদের হাজিরা সংরক্ষিত হয়েছে!" else "Staff attendance saved!", Toast.LENGTH_SHORT).show()
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
      ) {
        Icon(Icons.Default.Check, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(if (isBengali) "সকল স্টাফ হাজিরা সংরক্ষণ করুন" else "Save Staff Attendance")
      }
    }
  }
}
