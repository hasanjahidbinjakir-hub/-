package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.HomeworkEntity
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.MadrasahViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeworkScreen(
  viewModel: MadrasahViewModel,
  modifier: Modifier = Modifier
) {
  val isBengali by viewModel.isBengali.collectAsState()
  val currentRole by viewModel.currentRole.collectAsState()
  val authenticatedTeacher by viewModel.authenticatedTeacher.collectAsState()
  val homeworkList by viewModel.homeworkList.collectAsState()
  val classes by viewModel.classes.collectAsState()
  val subjects by viewModel.subjects.collectAsState()

  var selectedClass by remember { mutableStateOf<String?>(null) }
  var showAddDialog by remember { mutableStateOf(false) }

  val canManage = currentRole == UserRole.SUPER_ADMIN || currentRole == UserRole.PRINCIPAL || currentRole == UserRole.TEACHER || authenticatedTeacher != null

  val filteredHomework = homeworkList.filter { hw ->
    selectedClass == null || hw.className == selectedClass
  }

  Scaffold(
    floatingActionButton = {
      if (canManage) {
        ExtendedFloatingActionButton(
          onClick = { showAddDialog = true },
          icon = { Icon(Icons.Default.Add, contentDescription = "Add Homework") },
          text = { Text(if (isBengali) "নতুন বাড়ির কাজ" else "Assign Homework") },
          containerColor = EmeraldPrimary,
          contentColor = Color.White,
          modifier = Modifier.testTag("add_homework_fab")
        )
      }
    }
  ) { paddingValues ->
    Column(
      modifier = modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Header Banner
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NavyPrimary),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Default.MenuBook, contentDescription = null, tint = GoldAccent)
            Text(
              text = if (isBengali) "দৈনিক বাড়ির কাজ (Homework)" else "Daily Homework & Assignments",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp
            )
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = if (isBengali)
              "শিক্ষকদের দেওয়া পাঠ পরিকল্পনা ও বাড়ির কাজ সরাসরি অভিভাবক পোর্টালে দৃশ্যমান হয়।"
            else
              "Class assignments submitted by teachers are instantly visible on the Guardian Portal.",
            color = Slate200,
            fontSize = 11.sp
          )
        }
      }

      // Class Filter Chips
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        item {
          FilterChip(
            selected = selectedClass == null,
            onClick = { selectedClass = null },
            label = { Text(if (isBengali) "সকল শ্রেণি" else "All Classes") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = EmeraldPrimary,
              selectedLabelColor = Color.White
            )
          )
        }
        items(classes) { cls ->
          val className = if (isBengali) cls.nameBn else cls.nameEn
          FilterChip(
            selected = selectedClass == className,
            onClick = { selectedClass = className },
            label = { Text(className) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = EmeraldPrimary,
              selectedLabelColor = Color.White
            )
          )
        }
      }

      // Homework List
      if (filteredHomework.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              Icons.Default.AssignmentLate,
              contentDescription = null,
              tint = Slate400,
              modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = if (isBengali) "কোনো বাড়ির কাজ পাওয়া যায়নি" else "No homework found for this class",
              color = Slate500,
              fontSize = 14.sp
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(filteredHomework) { hw ->
            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                      color = NavyPrimary,
                      shape = RoundedCornerShape(6.dp)
                    ) {
                      Text(
                        text = hw.className,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                      )
                    }
                    Surface(
                      color = EmeraldContainer,
                      shape = RoundedCornerShape(6.dp)
                    ) {
                      Text(
                        text = hw.subjectName,
                        color = EmeraldDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                      )
                    }
                  }

                  if (canManage) {
                    IconButton(
                      onClick = { viewModel.deleteHomework(hw) },
                      modifier = Modifier.size(28.dp)
                    ) {
                      Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = CrimsonLight,
                        modifier = Modifier.size(18.dp)
                      )
                    }
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                  text = hw.title,
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = MaterialTheme.colorScheme.onSurface
                )

                if (hw.description.isNotBlank()) {
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = hw.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Slate200)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      Icons.Default.CalendarToday,
                      contentDescription = null,
                      tint = Slate400,
                      modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "${if (isBengali) "প্রদানের তারিখ: " else "Assigned: "}${hw.date}",
                      fontSize = 11.sp,
                      color = Slate500
                    )
                  }

                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      Icons.Default.EventAvailable,
                      contentDescription = null,
                      tint = EmeraldPrimary,
                      modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "${if (isBengali) "জমার তারিখ: " else "Due: "}${hw.submissionDate}",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Medium,
                      color = EmeraldDark
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

  // Dialog to assign new homework
  if (showAddDialog) {
    val classList = classes.ifEmpty { emptyList() }
    val defaultClass = classList.firstOrNull()?.nameBn ?: "১ম শ্রেণি"
    var selectedClassInput by remember { mutableStateOf(selectedClass ?: defaultClass) }
    val selectedClassObj = classList.find { it.nameBn == selectedClassInput }
    val classId = selectedClassObj?.id ?: 1L

    val classSubjects = subjects.filter { it.classId == classId }
    val defaultSubject = classSubjects.firstOrNull()?.nameBn ?: "আরবি ও কুরআন"
    var selectedSubjectInput by remember { mutableStateOf(defaultSubject) }
    var selectedSubjectId by remember { mutableStateOf(classSubjects.firstOrNull()?.id ?: 1L) }

    var titleInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }

    val todayStr = remember { SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date()) }
    val tomorrowCalendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
    val tomorrowStr = remember { SimpleDateFormat("dd-MM-yyyy", Locale.US).format(tomorrowCalendar.time) }

    var assignDateInput by remember { mutableStateOf(todayStr) }
    var dueDateInput by remember { mutableStateOf(tomorrowStr) }

    var expandedClassDropdown by remember { mutableStateOf(false) }
    var expandedSubjectDropdown by remember { mutableStateOf(false) }

    AlertDialog(
      onDismissRequest = { showAddDialog = false },
      title = {
        Text(
          text = if (isBengali) "নতুন বাড়ির কাজ বরাদ্দ করুন" else "Assign New Homework",
          fontWeight = FontWeight.Bold,
          fontSize = 17.sp
        )
      },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Class Dropdown
          Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
              value = selectedClassInput,
              onValueChange = {},
              readOnly = true,
              label = { Text(if (isBengali) "শ্রেণি" else "Class") },
              trailingIcon = {
                IconButton(onClick = { expandedClassDropdown = !expandedClassDropdown }) {
                  Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
              },
              modifier = Modifier.fillMaxWidth()
            )
            DropdownMenu(
              expanded = expandedClassDropdown,
              onDismissRequest = { expandedClassDropdown = false }
            ) {
              for (cls in classList) {
                DropdownMenuItem(
                  text = { Text(cls.nameBn) },
                  onClick = {
                    selectedClassInput = cls.nameBn
                    expandedClassDropdown = false
                    val subs = subjects.filter { it.classId == cls.id }
                    val firstSub = subs.firstOrNull()
                    selectedSubjectInput = firstSub?.nameBn ?: "সাধারণ পাঠ"
                    selectedSubjectId = firstSub?.id ?: 1L
                  }
                )
              }
            }
          }

          // Subject Dropdown
          Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
              value = selectedSubjectInput,
              onValueChange = {},
              readOnly = true,
              label = { Text(if (isBengali) "বিষয়" else "Subject") },
              trailingIcon = {
                IconButton(onClick = { expandedSubjectDropdown = !expandedSubjectDropdown }) {
                  Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
              },
              modifier = Modifier.fillMaxWidth()
            )
            DropdownMenu(
              expanded = expandedSubjectDropdown,
              onDismissRequest = { expandedSubjectDropdown = false }
            ) {
              for (sub in classSubjects) {
                DropdownMenuItem(
                  text = { Text(sub.nameBn) },
                  onClick = {
                    selectedSubjectInput = sub.nameBn
                    selectedSubjectId = sub.id
                    expandedSubjectDropdown = false
                  }
                )
              }
            }
          }

          OutlinedTextField(
            value = titleInput,
            onValueChange = { titleInput = it },
            label = { Text(if (isBengali) "পাঠ / কাজের শিরোনাম *" else "Title / Topic *") },
            placeholder = { Text(if (isBengali) "যেমন: পৃষ্ঠা ১২ এর প্রশ্ন ১ ও ২ লেখা" else "e.g. Page 12 Questions") },
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = descInput,
            onValueChange = { descInput = it },
            label = { Text(if (isBengali) "বিস্তারিত নির্দেশাবলী" else "Instructions / Details") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedTextField(
              value = assignDateInput,
              onValueChange = { assignDateInput = it },
              label = { Text(if (isBengali) "প্রদানের তারিখ" else "Assign Date") },
              modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
              value = dueDateInput,
              onValueChange = { dueDateInput = it },
              label = { Text(if (isBengali) "জমার তারিখ" else "Due Date") },
              modifier = Modifier.weight(1f)
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (titleInput.isNotBlank()) {
              val teacherName = authenticatedTeacher?.nameBn ?: "শ্রেণি শিক্ষক"
              val hw = HomeworkEntity(
                classId = classId,
                className = selectedClassInput,
                subjectId = selectedSubjectId,
                subjectName = selectedSubjectInput,
                date = assignDateInput.trim(),
                title = titleInput.trim(),
                description = descInput.trim(),
                assignedBy = teacherName,
                submissionDate = dueDateInput.trim()
              )
              viewModel.postHomework(hw) {
                showAddDialog = false
              }
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
        ) {
          Text(if (isBengali) "সংরক্ষণ ও প্রকাশ" else "Save & Publish")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddDialog = false }) {
          Text(if (isBengali) "বাতিল" else "Cancel")
        }
      }
    )
  }
}
