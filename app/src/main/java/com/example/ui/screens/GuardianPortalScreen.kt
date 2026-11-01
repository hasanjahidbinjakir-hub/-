package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.entity.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MadrasahViewModel
import com.example.util.ERPLocale
import com.example.util.PrintHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuardianPortalScreen(
  viewModel: MadrasahViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val isBengali by viewModel.isBengali.collectAsState()
  val loggedInStudent by viewModel.authenticatedGuardianStudent.collectAsState()
  val students by viewModel.students.collectAsState()
  val feePayments by viewModel.feePayments.collectAsState()
  val examinations by viewModel.examinations.collectAsState()
  val subjects by viewModel.subjects.collectAsState()
  val homeworkList by viewModel.homeworkList.collectAsState()
  val settings by viewModel.settings.collectAsState()

  var inputLoginId by remember { mutableStateOf("") }
  var inputPassword by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var activeTab by remember { mutableStateOf(0) } // 0: Fees & Receipts, 1: Result, 2: Admit Card, 3: Attendance, 4: Homework

  val portalUrl = settings?.portalUrl ?: "https://jabalenoor.edu.bd/portal"

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Portal Link & Notice Bar
    Card(
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = NavyPrimary),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Public, contentDescription = null, tint = GoldAccent)
            Text(
              text = if (isBengali) "অভিভাবক অনলাইন পোর্টাল" else "Guardian Online Portal",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp
            )
          }

          Surface(
            shape = RoundedCornerShape(20.dp),
            color = EmeraldPrimary
          ) {
            Text(
              text = if (isBengali) "অনলাইন লিঙ্ক" else "Live Link",
              color = Color.White,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(NavyLight)
            .padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = portalUrl,
            color = GoldAccent,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
          )
          IconButton(
            onClick = {
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
              clipboard.setPrimaryClip(ClipData.newPlainText("Portal URL", portalUrl))
              Toast.makeText(context, if (isBengali) "পোর্টাল লিংক কপি করা হয়েছে" else "Portal URL copied", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.size(28.dp)
          ) {
            Icon(
              Icons.Default.ContentCopy,
              contentDescription = "Copy Link",
              tint = Color.White,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }

    if (loggedInStudent == null) {
      // Guardian Login Card
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(
              shape = CircleShape,
              color = EmeraldContainer,
              modifier = Modifier.size(44.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldDark)
              }
            }
            Column {
              Text(
                text = if (isBengali) "অভিভাবক প্রবেশ (লগইন)" else "Guardian Login",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = if (isBengali) "শিক্ষার্থীর রোল / আইডি ও পাসওয়ার্ড লিখুন" else "Enter Student ID / Roll and Password",
                fontSize = 12.sp,
                color = Slate500
              )
            }
          }

          errorMessage?.let { msg ->
            Surface(
              color = CrimsonContainer,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = msg,
                color = CrimsonDark,
                fontSize = 12.sp,
                modifier = Modifier.padding(10.dp)
              )
            }
          }

          OutlinedTextField(
            value = inputLoginId,
            onValueChange = {
              inputLoginId = it
              errorMessage = null
            },
            label = { Text(if (isBengali) "শিক্ষার্থী আইডি বা রোল নম্বর *" else "Student ID or Roll *") },
            placeholder = { Text("যেমন: JNGM-2026-001 বা 01") },
            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = EmeraldPrimary) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("guardian_login_id_input")
          )

          OutlinedTextField(
            value = inputPassword,
            onValueChange = {
              inputPassword = it
              errorMessage = null
            },
            label = { Text(if (isBengali) "পাসওয়ার্ড (স্বয়ংক্রিয় পিন) *" else "Password / PIN *") },
            placeholder = { Text("যেমন: 1234") },
            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = EmeraldPrimary) },
            trailingIcon = {
              IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                  if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                  contentDescription = "Toggle password visibility"
                )
              }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("guardian_password_input")
          )

          Button(
            onClick = {
              if (inputLoginId.isBlank() || inputPassword.isBlank()) {
                errorMessage = if (isBengali) "আইডি ও পাসওয়ার্ড উভয়ই প্রদান করুন" else "Please enter both ID and password"
                return@Button
              }
              viewModel.loginGuardian(inputLoginId, inputPassword) { success, msg ->
                if (!success) {
                  errorMessage = msg
                }
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("guardian_login_submit_button")
          ) {
            Icon(Icons.Default.Login, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isBengali) "লগ ইন করুন" else "Log In to Portal", fontSize = 15.sp, fontWeight = FontWeight.Bold)
          }

          // Demo quick login hint
          HorizontalDivider(color = Slate200)
          Text(
            text = if (isBengali) "দ্রুত পরীক্ষার জন্য যেকোনো শিক্ষার্থী বেছে নিন:" else "Quick test accounts:",
            fontSize = 11.sp,
            color = Slate500
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            students.take(3).forEach { st ->
              SuggestionChip(
                onClick = {
                  inputLoginId = st.studentId
                  inputPassword = st.guardianPassword
                },
                label = { Text("${st.fullNameBn} (${st.guardianPassword})", fontSize = 10.sp) }
              )
            }
          }
        }
      }
    } else {
      // Logged-in Student Guardian Profile & Services
      val student = loggedInStudent!!
      val studentPayments = feePayments.filter { it.studentId == student.studentId }
      val studentHomework = homeworkList.filter { it.className == student.className }

      val totalPaid = studentPayments.sumOf { it.paidAmount }
      val totalDue = studentPayments.sumOf { it.dueAmount }

      // Student Header Card
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Photo
          if (student.photoUri.isNotBlank()) {
            AsyncImage(
              model = student.photoUri,
              contentDescription = "Student Photo",
              contentScale = ContentScale.Crop,
              modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .border(2.dp, EmeraldPrimary, CircleShape)
            )
          } else {
            Surface(
              shape = CircleShape,
              color = EmeraldContainer,
              modifier = Modifier.size(60.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Person, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(36.dp))
              }
            }
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (isBengali) student.fullNameBn else student.fullNameEn,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "আইডি: ${student.studentId} • রোল: ${student.rollNumber}",
              fontSize = 12.sp,
              color = Slate500
            )
            Text(
              text = "শ্রেণি: ${student.className} (${if (student.gender == "Female") "ছাত্রী" else "ছাত্র"}) • অভিভাবক: ${student.guardianName}",
              fontSize = 11.sp,
              color = EmeraldDark,
              fontWeight = FontWeight.Medium
            )
          }

          IconButton(
            onClick = { viewModel.logoutGuardian() },
            modifier = Modifier.testTag("guardian_logout_button")
          ) {
            Icon(Icons.Default.Logout, contentDescription = "Logout", tint = CrimsonLight)
          }
        }
      }

      // Feature Navigation Tabs
      ScrollableTabRow(
        selectedTabIndex = activeTab,
        edgePadding = 0.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = EmeraldPrimary
      ) {
        val tabTitles = if (isBengali)
          listOf("ফি ও রসিদ", "ফলাফল পত্র", "প্রবেশপত্র", "বাড়ির কাজ", "হাজিরা")
        else
          listOf("Fees & Receipts", "Report Card", "Admit Card", "Homework", "Attendance")

        tabTitles.forEachIndexed { index, title ->
          Tab(
            selected = activeTab == index,
            onClick = { activeTab = index },
            text = { Text(title, fontSize = 12.sp, fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Normal) }
          )
        }
      }

      // Tab Content View
      when (activeTab) {
        0 -> {
          // Fees & Receipts
          LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            item {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Card(
                  modifier = Modifier.weight(1f),
                  colors = CardDefaults.cardColors(containerColor = EmeraldContainer)
                ) {
                  Column(modifier = Modifier.padding(12.dp)) {
                    Text(if (isBengali) "মোট পরিশোধিত" else "Total Paid", fontSize = 11.sp, color = EmeraldDark)
                    Text(ERPLocale.formatTaka(totalPaid), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EmeraldDark)
                  }
                }
                Card(
                  modifier = Modifier.weight(1f),
                  colors = CardDefaults.cardColors(containerColor = if (totalDue > 0) CrimsonContainer else EmeraldContainer)
                ) {
                  Column(modifier = Modifier.padding(12.dp)) {
                    Text(if (isBengali) "বকেয়া ফি" else "Due Balance", fontSize = 11.sp, color = if (totalDue > 0) CrimsonDark else EmeraldDark)
                    Text(ERPLocale.formatTaka(totalDue), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (totalDue > 0) CrimsonDark else EmeraldDark)
                  }
                }
              }
            }

            if (studentPayments.isEmpty()) {
              item {
                Text(
                  text = if (isBengali) "কোনো অর্থ প্রদানের রসিদ পাওয়া যায়নি" else "No payment records found",
                  color = Slate400,
                  fontSize = 13.sp,
                  modifier = Modifier.padding(top = 16.dp)
                )
              }
            } else {
              items(studentPayments) { p ->
                Card(
                  shape = RoundedCornerShape(10.dp),
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                  elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column {
                      Text("রসিদ: ${p.receiptNumber}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                      Text("${p.feeDescription.ifEmpty { "ছাত্রী ফি" }} • ${p.date}", fontSize = 11.sp, color = Slate500)
                      Text(ERPLocale.formatTaka(p.paidAmount) + " (${p.paymentMethod})", color = EmeraldDark, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                      Button(
                        onClick = {
                          coroutineScope.launch {
                            val details = viewModel.repository.getPaymentDetails(p.id)
                            val html = PrintHelper.buildMoneyReceiptHtml(p, details, isA5 = true)
                            PrintHelper.printHtml(context, html, "Receipt_A5_${p.receiptNumber}", isA5 = true)
                          }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                      ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("A5 রসিদ", fontSize = 11.sp)
                      }

                      OutlinedButton(
                        onClick = {
                          coroutineScope.launch {
                            val details = viewModel.repository.getPaymentDetails(p.id)
                            val html = PrintHelper.buildMoneyReceiptHtml(p, details, isA5 = false)
                            PrintHelper.printHtml(context, html, "Receipt_A4_${p.receiptNumber}", isA5 = false)
                          }
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                      ) {
                        Text("A4", fontSize = 11.sp)
                      }
                    }
                  }
                }
              }
            }
          }
        }

        1 -> {
          // Report Card / Marksheet
          val exam = examinations.firstOrNull()
          val examName = exam?.name ?: "বার্ষিক পরীক্ষা ২০২৬"
          val examId = exam?.id ?: 1L

          val resultsFlow = remember(student.studentId, examId) {
            viewModel.repository.getResultsByStudent(examId, student.studentId)
          }
          val results by resultsFlow.collectAsState(initial = emptyList())

          LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            item {
              Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth().padding(14.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Text(examName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("বাংলাদেশ শিক্ষা বোর্ড মান অনুযায়ী GPA ৫.০ স্কেল", color = GoldAccent, fontSize = 11.sp)
                  }

                  Button(
                    onClick = {
                      val reportHtml = PrintHelper.buildReportCardHtml(
                        student = student,
                        examName = examName,
                        results = results
                      )
                      PrintHelper.printHtml(context, reportHtml, "Report_Card_${student.rollNumber}", isA5 = false)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                  ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBengali) "A4 মার্কশিট প্রিন্ট" else "Print Marksheet", fontSize = 12.sp)
                  }
                }
              }
            }

            if (results.isEmpty()) {
              item {
                Text(
                  text = if (isBengali) "এই শিক্ষার্থীর ফলাফল এখনো এন্ট্রি বা প্রকাশ করা হয়নি" else "Results not yet published",
                  color = Slate400,
                  fontSize = 13.sp,
                  modifier = Modifier.padding(16.dp)
                )
              }
            } else {
              items(results) { res ->
                Card(
                  shape = RoundedCornerShape(8.dp),
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column {
                      Text(res.subjectName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                      Text("প্রাপ্ত নম্বর: ${res.marksObtained.toInt()} / 100", fontSize = 11.sp, color = Slate500)
                    }

                    Surface(
                      color = if (res.grade == "F") CrimsonContainer else EmeraldContainer,
                      shape = RoundedCornerShape(6.dp)
                    ) {
                      Text(
                        text = "${res.grade} (GP: ${"%.2f".format(res.gpa)})",
                        color = if (res.grade == "F") CrimsonDark else EmeraldDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                      )
                    }
                  }
                }
              }
            }
          }
        }

        2 -> {
          // Admit Card
          val exam = examinations.firstOrNull()
          val examName = exam?.name ?: "বার্ষিক পরীক্ষা ২০২৬"
          val examId = exam?.id ?: 1L

          val schedulesFlow = remember(student.classId, examId) {
            viewModel.repository.getSchedulesByExamAndClass(examId, student.classId)
          }
          val schedules by schedulesFlow.collectAsState(initial = emptyList())

          Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Text(if (isBengali) "পরীক্ষার প্রবেশপত্র" else "Examination Admit Card", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(examName, fontSize = 12.sp, color = EmeraldDark, fontWeight = FontWeight.Medium)
                  }

                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldContainer
                  ) {
                    Text("বৈধ প্রবেশপত্র", color = EmeraldDark, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(6.dp))
                  }
                }

                HorizontalDivider(color = Slate200)

                Text("রোল: ${student.rollNumber} • শ্রেণি: ${student.className}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text("পরীক্ষার্থীর নাম: ${student.fullNameBn} (${student.fullNameEn})", fontSize = 12.sp)
                Text("পরীক্ষা কেন্দ্র: জাবালে নূর গার্লস মাদ্রাসা ক্যাম্পাস, রামগড়", fontSize = 11.sp, color = Slate500)

                Button(
                  onClick = {
                    val admitHtml = PrintHelper.buildAdmitCardHtml(
                      student = student,
                      examName = examName,
                      schedules = schedules
                    )
                    PrintHelper.printHtml(context, admitHtml, "Admit_Card_${student.rollNumber}", isA5 = true)
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Icon(Icons.Default.Print, contentDescription = null)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(if (isBengali) "প্রবেশপত্র A5 প্রিন্ট করুন" else "Print Admit Card (A5)")
                }
              }
            }
          }
        }

        3 -> {
          // Homework
          LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            if (studentHomework.isEmpty()) {
              item {
                Text(
                  text = if (isBengali) "বর্তমান শ্রেণির জন্য কোনো বাড়ির কাজ পাওয়া যায়নি" else "No homework assigned currently",
                  color = Slate400,
                  fontSize = 13.sp,
                  modifier = Modifier.padding(16.dp)
                )
              }
            } else {
              items(studentHomework) { hw ->
                Card(
                  shape = RoundedCornerShape(10.dp),
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                  elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                      Surface(
                        color = EmeraldContainer,
                        shape = RoundedCornerShape(6.dp)
                      ) {
                        Text(hw.subjectName, color = EmeraldDark, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                      }
                      Text("জমা: ${hw.submissionDate}", fontSize = 11.sp, color = EmeraldPrimary, fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(hw.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    if (hw.description.isNotBlank()) {
                      Text(hw.description, fontSize = 12.sp, color = Slate500)
                    }
                  }
                }
              }
            }
          }
        }

        4 -> {
          // Attendance
          Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = EmeraldContainer),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Text(if (isBengali) "হাজিরা পরিসংখ্যান" else "Attendance Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = EmeraldDark)
                Spacer(modifier = Modifier.height(4.dp))
                Text("উপস্থিতি হার: ৯৫%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = EmeraldDark)
                Text("সেশন ২০২৬ • নিয়মিত উপস্থিতি বজায় রাখা হচ্ছে", fontSize = 12.sp, color = EmeraldDark)
              }
            }
          }
        }
      }
    }
  }
}
