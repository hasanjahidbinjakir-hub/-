package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.*
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.MadrasahViewModel
import com.example.util.ERPLocale
import com.example.util.PrintHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamsScreen(
  viewModel: MadrasahViewModel,
  modifier: Modifier = Modifier
) {
  val isBengali by viewModel.isBengali.collectAsState()
  val exams by viewModel.examinations.collectAsState()
  val classes by viewModel.classes.collectAsState()
  val students by viewModel.students.collectAsState()
  val allSubjects by viewModel.subjects.collectAsState()
  val currentRole by viewModel.currentRole.collectAsState()
  val currentSession by viewModel.currentSession.collectAsState()
  val context = LocalContext.current

  // Tabs:
  // 0 = পরীক্ষার তালিকা (Exams List)
  // 1 = নম্বর এন্ট্রি (Marks Entry) - NEW!
  // 2 = ক্লাস ভিত্তিক ফলাফল বিবরণী (Tabulation Sheet) - NEW!
  // 3 = প্রবেশপত্র তৈরি (Admit Card Maker) - NEW!
  // 4 = বিষয় ও পাস-ফেল মান নির্ধারণ (Subject & Pass Marks) - NEW!
  var selectedTab by remember { mutableStateOf(1) } // Default to Marks Entry so teachers immediately see it!

  var selectedExamId by remember { mutableStateOf(exams.firstOrNull()?.id ?: 1L) }
  var selectedClassId by remember { mutableStateOf(3L) }
  var selectedSubjectId by remember { mutableStateOf<Long?>(null) }
  var selectedStudentForReportCard by remember { mutableStateOf<StudentEntity?>(null) }
  var showNewExamDialog by remember { mutableStateOf(false) }

  val selectedExam = exams.find { it.id == selectedExamId } ?: exams.firstOrNull()
  val examName = selectedExam?.name ?: "বার্ষিক পরীক্ষা ২০২৬"
  val currentClassName = classes.find { it.id == selectedClassId }?.nameBn ?: "শ্রেণি"
  val subjectsInClass = allSubjects.filter { it.classId == selectedClassId }
  val studentsInClass = students.filter { it.classId == selectedClassId }

  LaunchedEffect(subjectsInClass) {
    if (selectedSubjectId == null || subjectsInClass.none { it.id == selectedSubjectId }) {
      selectedSubjectId = subjectsInClass.firstOrNull()?.id
    }
  }

  // Results for current class and exam
  val examResults by viewModel.repository.getResultsByExamAndClass(selectedExamId, selectedClassId)
    .collectAsState(initial = emptyList())

  // Schedules for admit card
  val schedules by viewModel.repository.getSchedulesByExamAndClass(selectedExamId, selectedClassId)
    .collectAsState(initial = emptyList())

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(14.dp)
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = if (isBengali) "পরীক্ষা, ফলাফল ও প্রবেশপত্র" else "Exams, Results & Admit Cards",
          fontWeight = FontWeight.Bold,
          fontSize = 17.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = if (isBengali) "জাবালে নূর গার্লস মাদ্রাসা • মাধ্যমিক গ্রেডিং মান (GPA 5.0)" else "Bangladesh Board GPA 5.0 Standard",
          fontSize = 11.sp,
          color = Slate600
        )
      }

      Button(
        onClick = { showNewExamDialog = true },
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        modifier = Modifier.testTag("create_exam_button")
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(if (isBengali) "নতুন পরীক্ষা" else "New Exam", fontSize = 11.sp)
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Navigation Tabs
    ScrollableTabRow(
      selectedTabIndex = selectedTab,
      edgePadding = 0.dp
    ) {
      val tabs = listOf(
        "নম্বর এন্ট্রি (Marks)",
        "ক্লাস ফলাফল (Tabulation)",
        "প্রবেশপত্র (Admit Card)",
        "বিষয় ও পাস মান (Subjects)",
        "পরীক্ষা তালিকা (Exams)"
      )
      tabs.forEachIndexed { index, title ->
        Tab(
          selected = selectedTab == index,
          onClick = { selectedTab = index },
          text = { Text(title, fontSize = 12.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Filters Bar: Select Exam and Select Class
    Card(
      shape = RoundedCornerShape(10.dp),
      colors = CardDefaults.cardColors(containerColor = Slate100),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Exam Dropdown
        var examExpanded by remember { mutableStateOf(false) }
        Box(modifier = Modifier.weight(1.2f)) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            modifier = Modifier
              .fillMaxWidth()
              .clickable { examExpanded = true }
              .padding(horizontal = 8.dp, vertical = 6.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(text = examName.take(20), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
              Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
            }
          }
          DropdownMenu(expanded = examExpanded, onDismissRequest = { examExpanded = false }) {
            exams.forEach { e ->
              DropdownMenuItem(
                text = { Text(e.name, fontSize = 12.sp) },
                onClick = {
                  selectedExamId = e.id
                  examExpanded = false
                }
              )
            }
          }
        }

        // Class Chips / Selector
        var classExpanded by remember { mutableStateOf(false) }
        val currentClassName = classes.find { it.id == selectedClassId }?.let { if (isBengali) it.nameBn else it.nameEn } ?: "শ্রেণি"
        Box(modifier = Modifier.weight(1f)) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            modifier = Modifier
              .fillMaxWidth()
              .clickable { classExpanded = true }
              .padding(horizontal = 8.dp, vertical = 6.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(text = currentClassName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
              Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
            }
          }
          DropdownMenu(expanded = classExpanded, onDismissRequest = { classExpanded = false }) {
            classes.forEach { c ->
              DropdownMenuItem(
                text = { Text(if (isBengali) c.nameBn else c.nameEn, fontSize = 12.sp) },
                onClick = {
                  selectedClassId = c.id
                  classExpanded = false
                }
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Content based on tab
    when (selectedTab) {
      0 -> {
        // --- TAB 0: MARKS ENTRY SYSTEM (নম্বর এন্ট্রি) ---
        MarksEntryView(
          examId = selectedExamId,
          classId = selectedClassId,
          subjects = subjectsInClass,
          students = studentsInClass,
          existingResults = examResults,
          isBengali = isBengali,
          onSaveMarks = { newResults ->
            viewModel.saveExamResults(newResults) {
              Toast.makeText(context, if (isBengali) "নম্বর সফলভাবে সংরক্ষিত হয়েছে!" else "Marks saved successfully!", Toast.LENGTH_SHORT).show()
            }
          }
        )
      }

      1 -> {
        // --- TAB 1: CLASS TABULATION SHEET (এক কাগজে ক্লাস ফলাফল) ---
        ClassTabulationView(
          examName = examName,
          className = currentClassName,
          students = studentsInClass,
          subjects = subjectsInClass,
          allResults = examResults,
          isBengali = isBengali,
          onPrintOneClick = {
            val html = PrintHelper.buildTabulationSheetHtml(examName, currentClassName, studentsInClass, subjectsInClass, examResults)
            PrintHelper.printHtml(context, html, "Tabulation_${currentClassName}", isA5 = false)
          },
          onOpenStudentReport = { student ->
            selectedStudentForReportCard = student
          }
        )
      }

      2 -> {
        // --- TAB 2: ADMIT CARD MAKER (প্রবেশপত্র তৈরি) ---
        AdmitCardMakerView(
          examName = examName,
          className = currentClassName,
          students = studentsInClass,
          schedules = schedules,
          isBengali = isBengali,
          onPrintAdmitCard = { student ->
            val html = PrintHelper.buildAdmitCardHtml(student, examName, schedules)
            PrintHelper.printHtml(context, html, "AdmitCard_${student.studentId}", isA5 = true)
          }
        )
      }

      3 -> {
        // --- TAB 3: SUBJECTS & PASS/FAIL CONFIG (বিষয় ও পাস মান নির্ধারণ) ---
        SubjectsConfigView(
          classId = selectedClassId,
          subjects = subjectsInClass,
          isBengali = isBengali,
          isAdmin = currentRole == UserRole.SUPER_ADMIN || currentRole == UserRole.PRINCIPAL,
          onUpdateSubject = { sub ->
            viewModel.updateSubject(sub) {
              Toast.makeText(context, if (isBengali) "বিষয় ও পাস নম্বর আপডেট হয়েছে" else "Subject updated", Toast.LENGTH_SHORT).show()
            }
          },
          onAddSubject = { sub ->
            viewModel.addSubject(sub) {
              Toast.makeText(context, if (isBengali) "নতুন বিষয় যুক্ত হয়েছে" else "Subject added", Toast.LENGTH_SHORT).show()
            }
          }
        )
      }

      4 -> {
        // --- TAB 4: EXAMS LIST ---
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
          items(exams) { exam ->
            Card(
              shape = RoundedCornerShape(10.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              modifier = Modifier.fillMaxWidth(),
              elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column {
                  Text(text = exam.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                  Text(text = "সেশন: ${exam.academicSession} • ${exam.startDate} হতে ${exam.endDate}", fontSize = 11.sp, color = Slate600)
                }
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = EmeraldContainer
                ) {
                  Text(
                    text = "প্রকাশিত",
                    color = EmeraldOnContainer,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  // Report Card Dialog for selected student
  selectedStudentForReportCard?.let { student ->
    val studentResults = examResults.filter { it.studentId == student.studentId }
    ReportCardDialog(
      student = student,
      examName = examName,
      results = studentResults,
      isBengali = isBengali,
      onPrintOneClick = {
        val html = PrintHelper.buildReportCardHtml(student, examName, studentResults)
        PrintHelper.printHtml(context, html, "ReportCard_${student.studentId}", isA5 = false)
      },
      onDismiss = { selectedStudentForReportCard = null }
    )
  }

  // Create Exam Dialog
  if (showNewExamDialog) {
    var newExamName by remember { mutableStateOf("বার্ষিক পরীক্ষা ২০২৬") }
    var startDate by remember { mutableStateOf("15-11-2026") }
    var endDate by remember { mutableStateOf("28-11-2026") }

    Dialog(onDismissRequest = { showNewExamDialog = false }) {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().padding(10.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(text = if (isBengali) "নতুন পরীক্ষা তৈরি করুন" else "Create Examination", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NavyPrimary)
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = newExamName,
            onValueChange = { newExamName = it },
            label = { Text("পরীক্ষার নাম") },
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedTextField(
            value = startDate,
            onValueChange = { startDate = it },
            label = { Text("শুরুর তারিখ") },
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedTextField(
            value = endDate,
            onValueChange = { endDate = it },
            label = { Text("শেষের তারিখ") },
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(12.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { showNewExamDialog = false }) { Text("বাতিল") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = {
                if (newExamName.isBlank()) return@Button
                val exam = ExaminationEntity(name = newExamName, academicSession = currentSession, startDate = startDate, endDate = endDate, isPublished = true)
                viewModel.addExamination(exam) {
                  showNewExamDialog = false
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
              Text("সংরক্ষণ")
            }
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// SUB-VIEW 0: MARKS ENTRY SYSTEM (নম্বর এন্ট্রি)
// -------------------------------------------------------------
@Composable
fun MarksEntryView(
  examId: Long,
  classId: Long,
  subjects: List<SubjectEntity>,
  students: List<StudentEntity>,
  existingResults: List<ExamResultEntity>,
  isBengali: Boolean,
  onSaveMarks: (List<ExamResultEntity>) -> Unit
) {
  var selectedSubject by remember { mutableStateOf<SubjectEntity?>(subjects.firstOrNull()) }

  LaunchedEffect(subjects) {
    if (selectedSubject == null || selectedSubject !in subjects) {
      selectedSubject = subjects.firstOrNull()
    }
  }

  // Map of studentId -> marks text
  val marksInputMap = remember { mutableStateMapOf<String, String>() }

  // Populate existing marks if already in database
  LaunchedEffect(selectedSubject?.id, existingResults) {
    val subId = selectedSubject?.id ?: return@LaunchedEffect
    students.forEach { s ->
      val res = existingResults.find { it.studentId == s.studentId && it.subjectId == subId }
      marksInputMap[s.studentId] = res?.marksObtained?.toInt()?.toString() ?: ""
    }
  }

  Column(modifier = Modifier.fillMaxSize()) {
    // Subject selector chips
    Text(text = if (isBengali) "নম্বর এন্ট্রি করার বিষয় নির্বাচন করুন:" else "Select Subject for Marks Entry:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    Spacer(modifier = Modifier.height(6.dp))

    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
      items(subjects) { sub ->
        FilterChip(
          selected = selectedSubject?.id == sub.id,
          onClick = { selectedSubject = sub },
          label = { Text(sub.nameBn, fontSize = 11.sp) }
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    val currentSub = selectedSubject
    if (currentSub == null) {
      Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
        Text("এই শ্রেণীতে কোনো বিষয় পাওয়া যায়নি। 'বিষয় ও পাস মান' ট্যাবে বিষয় যোগ করুন।")
      }
    } else {
      // Header Info
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = NavyContainer,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "বিষয়: ${currentSub.nameBn}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyPrimary)
          Text(text = "পূর্ণমান: ${currentSub.maxMarks.toInt()} | পাস নম্বর: ${currentSub.passMarks.toInt()}", fontSize = 11.sp, color = NavyPrimary)
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Students marks list
      LazyColumn(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        items(students.sortedBy { it.rollNumber }) { student ->
          val marksStr = marksInputMap[student.studentId] ?: ""
          val marksVal = marksStr.toDoubleOrNull() ?: 0.0
          val (grade, gpa) = if (marksStr.isNotEmpty()) ERPLocale.calculateGradeAndGpa(marksVal) else Pair("-", 0.0)
          val isPassed = marksVal >= currentSub.passMarks

          Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(text = "${student.rollNumber}. ${student.fullNameBn}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = "আইডি: ${student.studentId} • ${student.className}", fontSize = 10.sp, color = Slate600)
              }

              // Grade & GPA Indicator
              if (marksStr.isNotEmpty()) {
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = if (isPassed) EmeraldContainer else Color(0xFFFEE2E2),
                  modifier = Modifier.padding(end = 8.dp)
                ) {
                  Text(
                    text = "$grade (${"%.1f".format(gpa)})",
                    color = if (isPassed) EmeraldDark else CrimsonError,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }

              // Marks Input Field
              OutlinedTextField(
                value = marksStr,
                onValueChange = { newVal ->
                  if (newVal.length <= 3 && (newVal.isEmpty() || newVal.all { it.isDigit() })) {
                    marksInputMap[student.studentId] = newVal
                  }
                },
                placeholder = { Text("নম্বর", fontSize = 11.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.width(80.dp).height(50.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Save Marks Button
      Button(
        onClick = {
          val newResults = students.mapNotNull { s ->
            val mStr = marksInputMap[s.studentId]
            if (mStr != null && mStr.isNotBlank()) {
              val m = mStr.toDoubleOrNull() ?: 0.0
              val (g, gpa) = ERPLocale.calculateGradeAndGpa(m)
              ExamResultEntity(
                examId = examId,
                studentId = s.studentId,
                classId = classId,
                subjectId = currentSub.id,
                subjectName = currentSub.nameBn,
                marksObtained = m,
                grade = g,
                gpa = gpa
              )
            } else null
          }
          onSaveMarks(newResults)
        },
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_marks_button")
      ) {
        Icon(Icons.Default.Save, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("এই বিষয়ের সকল নম্বর সংরক্ষণ করুন (Save Marks)", fontWeight = FontWeight.Bold)
      }
    }
  }
}

// -------------------------------------------------------------
// SUB-VIEW 1: CLASS TABULATION SHEET (এক কাগজে ক্লাস ফলাফল)
// -------------------------------------------------------------
@Composable
fun ClassTabulationView(
  examName: String,
  className: String,
  students: List<StudentEntity>,
  subjects: List<SubjectEntity>,
  allResults: List<ExamResultEntity>,
  isBengali: Boolean,
  onPrintOneClick: () -> Unit,
  onOpenStudentReport: (StudentEntity) -> Unit
) {
  Column(modifier = Modifier.fillMaxSize()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(text = "শ্রেণিভিত্তিক ফলাফল বিবরণী (Tabulation Sheet)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(text = "সকল শিক্ষার্থীর ফলাফল এক পৃষ্ঠায়", fontSize = 10.sp, color = Slate600)
      }

      // One-click Print Tabulation Sheet!
      Button(
        onClick = onPrintOneClick,
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        modifier = Modifier.testTag("print_tabulation_button")
      ) {
        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("১-ক্লিকে প্রিন্ট করুন", fontSize = 11.sp)
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // List of students with overall totals & individual report card trigger
    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
      items(students.sortedBy { it.rollNumber }) { s ->
        val studentResults = allResults.filter { it.studentId == s.studentId }
        val total = studentResults.sumOf { it.marksObtained }
        val gpa = if (studentResults.isNotEmpty()) studentResults.sumOf { it.gpa } / studentResults.size else 0.0
        val (grade, _) = ERPLocale.calculateGradeAndGpa(if (studentResults.isNotEmpty()) total / studentResults.size else 0.0)

        Card(
          shape = RoundedCornerShape(8.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth().clickable { onOpenStudentReport(s) },
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(text = "রোল ${s.rollNumber}. ${s.fullNameBn}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
              Text(text = "মোট নম্বর: ${total.toInt()} | বিষয় এন্ট্রি: ${studentResults.size}/${subjects.size}", fontSize = 11.sp, color = Slate600)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (grade != "F") EmeraldContainer else Color(0xFFFEE2E2),
                modifier = Modifier.padding(end = 8.dp)
              ) {
                Text(
                  text = "গ্রেড: $grade (GPA ${"%.2f".format(gpa)})",
                  color = if (grade != "F") EmeraldDark else CrimsonError,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
              IconButton(onClick = { onOpenStudentReport(s) }) {
                Icon(Icons.Default.Assessment, contentDescription = "View Marksheet", tint = NavyPrimary)
              }
            }
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// SUB-VIEW 2: ADMIT CARD MAKER (প্রবেশপত্র তৈরি)
// -------------------------------------------------------------
@Composable
fun AdmitCardMakerView(
  examName: String,
  className: String,
  students: List<StudentEntity>,
  schedules: List<ExamScheduleEntity>,
  isBengali: Boolean,
  onPrintAdmitCard: (StudentEntity) -> Unit
) {
  Column(modifier = Modifier.fillMaxSize()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(text = "স্বয়ংক্রিয় পরীক্ষার প্রবেশপত্র (Admit Card)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(text = "শিক্ষার্থীর নামে এক ক্লিকেই প্রবেশপত্র প্রিন্ট করুন", fontSize = 10.sp, color = Slate600)
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
      items(students.sortedBy { it.rollNumber }) { s ->
        Card(
          shape = RoundedCornerShape(8.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth(),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(text = "রোল ${s.rollNumber}. ${s.fullNameBn}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
              Text(text = "আইডি: ${s.studentId} • শ্রেণি: ${s.className}", fontSize = 11.sp, color = Slate600)
            }

            Button(
              onClick = { onPrintAdmitCard(s) },
              colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              modifier = Modifier.testTag("print_admit_card_${s.rollNumber}")
            ) {
              Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(15.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("প্রবেশপত্র প্রিন্ট", fontSize = 11.sp)
            }
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// SUB-VIEW 3: SUBJECTS & PASS MARKS CONFIG (বিষয় ও পাস মান)
// -------------------------------------------------------------
@Composable
fun SubjectsConfigView(
  classId: Long,
  subjects: List<SubjectEntity>,
  isBengali: Boolean,
  isAdmin: Boolean,
  onUpdateSubject: (SubjectEntity) -> Unit,
  onAddSubject: (SubjectEntity) -> Unit
) {
  var subjectToEdit by remember { mutableStateOf<SubjectEntity?>(null) }
  var showAddSubjectDialog by remember { mutableStateOf(false) }

  Column(modifier = Modifier.fillMaxSize()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(text = "সকল ক্লাসের বিষয় ও পাস-ফেল নম্বর কনফিগ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(text = "বিষয় ভিত্তিক পূর্ণমান ও পাস নম্বর নির্ধারণ", fontSize = 10.sp, color = Slate600)
      }

      Button(
        onClick = { showAddSubjectDialog = true },
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("নতুন বিষয়", fontSize = 11.sp)
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
      items(subjects) { sub ->
        Card(
          shape = RoundedCornerShape(8.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth(),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(text = sub.nameBn, fontWeight = FontWeight.Bold, fontSize = 13.sp)
              Text(text = "${sub.nameEn} • কোড: ${sub.code}", fontSize = 11.sp, color = Slate600)
              Text(text = "পূর্ণমান: ${sub.maxMarks.toInt()} | পাস নম্বর: ${sub.passMarks.toInt()}", fontSize = 11.sp, color = EmeraldDark, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
              onClick = { subjectToEdit = sub },
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
              Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("এডিট", fontSize = 11.sp)
            }
          }
        }
      }
    }
  }

  // Edit Subject Dialog (Admin can edit Pass and Max marks)
  subjectToEdit?.let { sub ->
    var nameBn by remember { mutableStateOf(sub.nameBn) }
    var maxMarksStr by remember { mutableStateOf(sub.maxMarks.toInt().toString()) }
    var passMarksStr by remember { mutableStateOf(sub.passMarks.toInt().toString()) }

    Dialog(onDismissRequest = { subjectToEdit = null }) {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().padding(10.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(text = "বিষয় ও পাস-ফেল নম্বর সম্পাদনা", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NavyPrimary)
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(value = nameBn, onValueChange = { nameBn = it }, label = { Text("বিষয়ের নাম") }, modifier = Modifier.fillMaxWidth())
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedTextField(value = maxMarksStr, onValueChange = { maxMarksStr = it }, label = { Text("পূর্ণমান (Max Marks)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedTextField(value = passMarksStr, onValueChange = { passMarksStr = it }, label = { Text("পাস নম্বর (Pass Marks)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
          Spacer(modifier = Modifier.height(12.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { subjectToEdit = null }) { Text("বাতিল") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = {
                val mm = maxMarksStr.toDoubleOrNull() ?: 100.0
                val pm = passMarksStr.toDoubleOrNull() ?: 33.0
                onUpdateSubject(sub.copy(nameBn = nameBn, maxMarks = mm, passMarks = pm))
                subjectToEdit = null
              },
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
              Text("আপডেট করুন")
            }
          }
        }
      }
    }
  }

  // Add Subject Dialog
  if (showAddSubjectDialog) {
    var nameBn by remember { mutableStateOf("") }
    var nameEn by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("SUB-01") }
    var maxMarksStr by remember { mutableStateOf("100") }
    var passMarksStr by remember { mutableStateOf("33") }

    Dialog(onDismissRequest = { showAddSubjectDialog = false }) {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().padding(10.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(text = "নতুন বিষয় যুক্ত করুন", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NavyPrimary)
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(value = nameBn, onValueChange = { nameBn = it }, label = { Text("বিষয়ের নাম (বাংলা)") }, modifier = Modifier.fillMaxWidth())
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedTextField(value = nameEn, onValueChange = { nameEn = it }, label = { Text("বিষয়ের নাম (ইংরেজি)") }, modifier = Modifier.fillMaxWidth())
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedTextField(value = maxMarksStr, onValueChange = { maxMarksStr = it }, label = { Text("পূর্ণমান") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedTextField(value = passMarksStr, onValueChange = { passMarksStr = it }, label = { Text("পাস নম্বর") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
          Spacer(modifier = Modifier.height(12.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { showAddSubjectDialog = false }) { Text("বাতিল") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = {
                if (nameBn.isBlank()) return@Button
                val mm = maxMarksStr.toDoubleOrNull() ?: 100.0
                val pm = passMarksStr.toDoubleOrNull() ?: 33.0
                onAddSubject(SubjectEntity(classId = classId, nameBn = nameBn, nameEn = nameEn.ifEmpty { nameBn }, code = code, maxMarks = mm, passMarks = pm))
                showAddSubjectDialog = false
              },
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
              Text("সংরক্ষণ")
            }
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// REPORT CARD DIALOG WITH 1-CLICK PRINT
// -------------------------------------------------------------
@Composable
fun ReportCardDialog(
  student: StudentEntity,
  examName: String,
  results: List<ExamResultEntity>,
  isBengali: Boolean,
  onPrintOneClick: () -> Unit,
  onDismiss: () -> Unit
) {
  val totalMarks = results.sumOf { it.marksObtained }
  val averageMarks = if (results.isNotEmpty()) totalMarks / results.size else 0.0
  val overallGpa = if (results.isNotEmpty()) results.sumOf { it.gpa } / results.size else 0.0
  val (overallGrade, _) = ERPLocale.calculateGradeAndGpa(if (results.isNotEmpty()) totalMarks / results.size else 0.0)

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surface,
      modifier = Modifier.fillMaxWidth().padding(4.dp)
    ) {
      Column(
        modifier = Modifier
          .padding(14.dp)
          .verticalScroll(rememberScrollState())
      ) {
        // Madrasah Header
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(text = "জাবালে নূর গার্লস মাদ্রাসা", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = NavyPrimary)
          Text(text = "রামগড়, খাগড়াছড়ি • শিক্ষা ও মূল্যায়ন শাখা", fontSize = 11.sp, color = Slate600)
          Spacer(modifier = Modifier.height(4.dp))
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = NavyPrimary
          ) {
            Text(
              text = "শিক্ষার্থী মূল্যায়ন ও নম্বরপত্র (ACADEMIC REPORT CARD)",
              color = Color.White,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
          }
          Text(text = examName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = EmeraldDark, modifier = Modifier.padding(top = 4.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = Slate200)
        Spacer(modifier = Modifier.height(8.dp))

        // Student Info
        Text(text = "শিক্ষার্থীর নাম: ${student.fullNameBn} (${student.fullNameEn})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(text = "শ্রেণি: ${student.className} • রোল নং: ${student.rollNumber} • আইডি: ${student.studentId}", fontSize = 11.sp, color = Slate600)

        Spacer(modifier = Modifier.height(10.dp))

        // Marks Table Header
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(EmeraldPrimary)
            .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
          Text(text = "বিষয় (Subject)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
          Text(text = "নম্বর", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.width(50.dp))
          Text(text = "গ্রেড", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.width(44.dp))
          Text(text = "জিপিএ", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.width(44.dp))
        }

        // Table Rows
        if (results.isEmpty()) {
          Text(
            text = "এই শিক্ষার্থীর কোনো নম্বর এন্ট্রি করা হয়নি।",
            fontSize = 12.sp,
            color = Slate600,
            modifier = Modifier.padding(vertical = 16.dp)
          )
        } else {
          results.forEach { res ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 5.dp)
            ) {
              Text(text = res.subjectName, fontSize = 12.sp, modifier = Modifier.weight(1f))
              Text(text = "${res.marksObtained.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.width(50.dp))
              Text(text = res.grade, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (res.grade != "F") EmeraldDark else CrimsonError, textAlign = TextAlign.Center, modifier = Modifier.width(44.dp))
              Text(text = "%.2f".format(res.gpa), fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.width(44.dp))
            }
            HorizontalDivider(color = Slate100)
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Overall Summary Box
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = EmeraldContainer,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text(text = "মোট নম্বর: ${totalMarks.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldOnContainer)
                Text(text = "গড় নম্বর: ${"%.1f".format(averageMarks)}%", fontSize = 11.sp, color = EmeraldOnContainer)
              }
              Column(horizontalAlignment = Alignment.End) {
                Text(text = "ফলাফল: $overallGrade (${if (overallGrade != "F") "উত্তীর্ণ" else "অকৃতকার্য"})", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = EmeraldOnContainer)
                Text(text = "জিপিএ: ${"%.2f".format(overallGpa)} / ৫.০০", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldOnContainer)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Actions: 1-Click Print & Close
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = onPrintOneClick,
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            modifier = Modifier.weight(1f).testTag("print_report_card_button")
          ) {
            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("১-ক্লিকে A4 প্রিন্ট", fontSize = 12.sp)
          }

          OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.weight(0.6f)
          ) {
            Text("বন্ধ করুন", fontSize = 12.sp)
          }
        }
      }
    }
  }
}
