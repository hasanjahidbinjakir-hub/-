package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.entity.MadrasahClassEntity
import com.example.data.entity.StudentEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.ERPScreen
import com.example.ui.viewmodel.MadrasahViewModel
import com.example.util.PrintHelper

@Composable
fun StudentsScreen(
  viewModel: MadrasahViewModel,
  onNavigate: (ERPScreen) -> Unit,
  modifier: Modifier = Modifier
) {
  val isBengali by viewModel.isBengali.collectAsState()
  val students by viewModel.students.collectAsState()
  val classes by viewModel.classes.collectAsState()
  val currentSession by viewModel.currentSession.collectAsState()
  val context = LocalContext.current

  var searchQuery by remember { mutableStateOf("") }
  var selectedClassId by remember { mutableStateOf<Long?>(null) }
  var selectedStudentForProfile by remember { mutableStateOf<StudentEntity?>(null) }
  var studentToDelete by remember { mutableStateOf<StudentEntity?>(null) }

  val filteredStudents = remember(students, searchQuery, selectedClassId) {
    students.filter { s ->
      val matchesSearch = searchQuery.isBlank() ||
        s.fullNameBn.contains(searchQuery, ignoreCase = true) ||
        s.fullNameEn.contains(searchQuery, ignoreCase = true) ||
        s.studentId.contains(searchQuery, ignoreCase = true) ||
        s.admissionNumber.contains(searchQuery, ignoreCase = true) ||
        s.rollNumber.toString() == searchQuery.trim()

      val matchesClass = selectedClassId == null || s.classId == selectedClassId
      matchesSearch && matchesClass
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    // Header & Actions Row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = if (isBengali) "শিক্ষার্থী তথ্য ভাণ্ডার" else "Student Management",
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = if (isBengali) "মোট নথিভুক্ত: ${students.size} জন শিক্ষার্থী" else "Total Enrolled: ${students.size} students",
          fontSize = 12.sp,
          color = Slate600
        )
      }

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        // Export CSV button
        OutlinedButton(
          onClick = {
            exportStudentsCsv(context, filteredStudents)
          },
          modifier = Modifier.testTag("export_students_csv")
        ) {
          Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(if (isBengali) "সিএসভি" else "CSV", fontSize = 12.sp)
        }

        // Add New Student Button
        Button(
          onClick = { onNavigate(ERPScreen.Admissions) },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
          modifier = Modifier.testTag("new_admission_button")
        ) {
          Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(if (isBengali) "ভর্তি" else "Admit", fontSize = 12.sp)
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Search Bar
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("student_search_input"),
      placeholder = {
        Text(if (isBengali) "নাম, আইডি, রোল অথবা ভর্তি নং দিয়ে খুঁজুন..." else "Search by name, ID, roll or adm no...")
      },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate600) },
      trailingIcon = {
        if (searchQuery.isNotEmpty()) {
          IconButton(onClick = { searchQuery = "" }) {
            Icon(Icons.Default.Clear, contentDescription = "Clear")
          }
        }
      },
      singleLine = true,
      shape = RoundedCornerShape(12.dp)
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Class Filter Chips
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      item {
        FilterChip(
          selected = selectedClassId == null,
          onClick = { selectedClassId = null },
          label = { Text(if (isBengali) "সকল শ্রেণি" else "All Classes") }
        )
      }
      items(classes) { c ->
        FilterChip(
          selected = selectedClassId == c.id,
          onClick = { selectedClassId = if (selectedClassId == c.id) null else c.id },
          label = { Text(if (isBengali) c.nameBn else c.nameEn) }
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Student List
    if (filteredStudents.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Default.SearchOff, contentDescription = null, tint = Slate600, modifier = Modifier.size(48.dp))
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = if (isBengali) "কোনো শিক্ষার্থীর তথ্য পাওয়া যায়নি" else "No student records matched",
            color = Slate600,
            fontSize = 14.sp
          )
        }
      }
    } else {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.weight(1f)
      ) {
        items(filteredStudents, key = { it.id }) { student ->
          StudentItemCard(
            student = student,
            isBengali = isBengali,
            onViewProfile = { selectedStudentForProfile = student },
            onDelete = { studentToDelete = student }
          )
        }
      }
    }
  }

  // Delete Confirmation Dialog
  studentToDelete?.let { student ->
    AlertDialog(
      onDismissRequest = { studentToDelete = null },
      title = { Text(if (isBengali) "শিক্ষার্থী নথি ডিলিট নিশ্চিতকরণ" else "Confirm Deletion") },
      text = {
        Text(
          if (isBengali)
            "আপনি কি নিশ্চিত যে '${student.fullNameBn}' (${student.studentId})-এর রেকর্ড চিরতরে মুছে ফেলতে চান?"
          else
            "Are you sure you want to permanently delete '${student.fullNameEn}' (${student.studentId})?"
        )
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteStudent(student)
            studentToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = CrimsonError)
        ) {
          Text(if (isBengali) "মুছে ফেলুন" else "Delete")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { studentToDelete = null }) {
          Text(if (isBengali) "বাতিল" else "Cancel")
        }
      }
    )
  }

  // Complete Student Profile Dialog with 1-click Certificate printing and Guardian credentials
  selectedStudentForProfile?.let { student ->
    StudentProfileDialog(
      student = student,
      isBengali = isBengali,
      onPrintCertificate = {
        val certHtml = PrintHelper.buildCertificateHtml(student)
        PrintHelper.printHtml(context, certHtml, "Testimonial_${student.studentId}", isA5 = false)
      },
      onDismiss = { selectedStudentForProfile = null }
    )
  }
}

@Composable
fun StudentItemCard(
  student: StudentEntity,
  isBengali: Boolean,
  onViewProfile: () -> Unit,
  onDelete: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onViewProfile() }
      .testTag("student_card_${student.rollNumber}"),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
        // Photo or Initial Avatar circle
        Surface(
          shape = CircleShape,
          color = EmeraldPrimary,
          modifier = Modifier.size(46.dp)
        ) {
          if (student.photoUri.isNotEmpty()) {
            AsyncImage(
              model = student.photoUri,
              contentDescription = "Photo",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )
          } else {
            Box(contentAlignment = Alignment.Center) {
              Text(
                text = "রোল ${student.rollNumber}",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = student.fullNameBn,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = if (student.gender == "Male") Color(0xFFE0F2FE) else Color(0xFFFCE7F3)
            ) {
              Text(
                text = if (student.gender == "Male") "ছাত্র" else "ছাত্রী",
                color = if (student.gender == "Male") Color(0xFF0369A1) else Color(0xFFBE185D),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
              )
            }
          }
          Text(
            text = student.fullNameEn,
            fontSize = 11.sp,
            color = Slate600
          )
          Spacer(modifier = Modifier.height(2.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = NavyContainer
            ) {
              Text(
                text = student.className,
                color = NavyOnContainer,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "আইডি: ${student.studentId}",
              fontSize = 11.sp,
              color = Slate600
            )
          }
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onViewProfile) {
          Icon(Icons.Default.Visibility, contentDescription = "View Profile", tint = EmeraldPrimary)
        }
        IconButton(onClick = onDelete) {
          Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = CrimsonError)
        }
      }
    }
  }
}

@Composable
fun StudentProfileDialog(
  student: StudentEntity,
  isBengali: Boolean,
  onPrintCertificate: () -> Unit,
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surface,
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = CircleShape,
              color = EmeraldPrimary,
              modifier = Modifier.size(42.dp)
            ) {
              if (student.photoUri.isNotEmpty()) {
                AsyncImage(
                  model = student.photoUri,
                  contentDescription = null,
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxSize()
                )
              } else {
                Box(contentAlignment = Alignment.Center) {
                  Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                }
              }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = if (isBengali) "শিক্ষার্থীর পূর্ণাঙ্গ পরিচিতি" else "Student Profile",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = NavyPrimary
              )
              Text(
                text = "জাবালে নূর গার্লস মাদ্রাসা",
                fontSize = 11.sp,
                color = EmeraldDark
              )
            }
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        HorizontalDivider(color = Slate200, modifier = Modifier.padding(vertical = 8.dp))

        // Guardian Credentials Card
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = EmeraldContainer,
          modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
          Column(modifier = Modifier.padding(8.dp)) {
            Text(
              text = if (isBengali) "অভিভাবক পোর্টাল লগইন তথ্য:" else "Guardian Portal Credentials:",
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              color = EmeraldOnContainer
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(text = "ইউজার আইডি: ${student.studentId}", fontSize = 11.sp, color = EmeraldOnContainer)
              Text(text = "পাসওয়ার্ড: ${student.guardianPassword}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldOnContainer)
            }
          }
        }

        ProfileDetailRow(label = if (isBengali) "নাম (বাংলা)" else "Name (Bn)", value = student.fullNameBn)
        ProfileDetailRow(label = if (isBengali) "নাম (ইংরেজি)" else "Name (En)", value = student.fullNameEn)
        ProfileDetailRow(label = if (isBengali) "শিক্ষার্থী আইডি" else "Student ID", value = student.studentId)
        ProfileDetailRow(label = if (isBengali) "ভর্তি নম্বর" else "Admission No", value = student.admissionNumber)
        ProfileDetailRow(label = if (isBengali) "লিঙ্গ" else "Gender", value = if (student.gender == "Male") "ছাত্র (Male)" else "ছাত্রী (Female)")
        ProfileDetailRow(label = if (isBengali) "শ্রেণি ও রোল" else "Class & Roll", value = "${student.className} • রোল: ${student.rollNumber}")
        ProfileDetailRow(label = if (isBengali) "পিতার নাম" else "Father's Name", value = student.fatherName)
        ProfileDetailRow(label = if (isBengali) "মাতার নাম" else "Mother's Name", value = student.motherName)
        ProfileDetailRow(label = if (isBengali) "অভিভাবকের ফোন" else "Guardian Phone", value = student.guardianPhone)
        ProfileDetailRow(label = if (isBengali) "জন্ম তারিখ" else "Date of Birth", value = student.dateOfBirth)
        ProfileDetailRow(label = if (isBengali) "রক্তের গ্রুপ" else "Blood Group", value = student.bloodGroup.ifEmpty { "N/A" })
        ProfileDetailRow(label = if (isBengali) "ঠিকানা" else "Address", value = student.presentAddress)

        Spacer(modifier = Modifier.height(14.dp))

        // Action Buttons: One-Click Print Certificate & Close
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = onPrintCertificate,
            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
            modifier = Modifier.weight(1f)
          ) {
            Icon(Icons.Default.CardMembership, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(if (isBengali) "প্রত্যয়নপত্র প্রিন্ট" else "Print Certificate", fontSize = 12.sp)
          }

          Button(
            onClick = onDismiss,
            modifier = Modifier.weight(0.7f),
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
          ) {
            Text(if (isBengali) "বন্ধ করুন" else "Close", fontSize = 12.sp)
          }
        }
      }
    }
  }
}

@Composable
fun ProfileDetailRow(label: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 2.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(text = "$label:", fontSize = 12.sp, color = Slate600, modifier = Modifier.width(110.dp))
    Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
  }
}

private fun exportStudentsCsv(context: Context, students: List<StudentEntity>) {
  val sb = StringBuilder()
  sb.appendLine("StudentID,AdmissionNo,NameBn,NameEn,Gender,Class,Roll,FatherName,Phone,GuardianLogin,GuardianPass,Status")
  students.forEach { s ->
    sb.appendLine("${s.studentId},${s.admissionNumber},\"${s.fullNameBn}\",\"${s.fullNameEn}\",${s.gender},\"${s.className}\",${s.rollNumber},\"${s.fatherName}\",\"${s.guardianPhone}\",${s.studentId},${s.guardianPassword},${s.status}")
  }
  val intent = Intent(Intent.ACTION_SEND).apply {
    type = "text/csv"
    putExtra(Intent.EXTRA_SUBJECT, "Jabale Noor Girls Madrasah - Student List")
    putExtra(Intent.EXTRA_TEXT, sb.toString())
  }
  context.startActivity(Intent.createChooser(intent, "Export Students CSV"))
}
