package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.entity.FeePaymentDetailEntity
import com.example.data.entity.FeePaymentEntity
import com.example.data.entity.StudentEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.ERPScreen
import com.example.ui.viewmodel.MadrasahViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdmissionScreen(
  viewModel: MadrasahViewModel,
  onNavigate: (ERPScreen) -> Unit,
  onShowReceipt: (FeePaymentEntity) -> Unit,
  modifier: Modifier = Modifier
) {
  val isBengali by viewModel.isBengali.collectAsState()
  val classes by viewModel.classes.collectAsState()
  val students by viewModel.students.collectAsState()
  val currentSession by viewModel.currentSession.collectAsState()
  val context = LocalContext.current

  // Form fields
  var fullNameBn by remember { mutableStateOf("") }
  var fullNameEn by remember { mutableStateOf("") }
  var fatherName by remember { mutableStateOf("") }
  var motherName by remember { mutableStateOf("") }
  var guardianPhone by remember { mutableStateOf("") }
  var presentAddress by remember { mutableStateOf("") }
  var dateOfBirth by remember { mutableStateOf("15-05-2017") }
  var bloodGroup by remember { mutableStateOf("O+") }

  // Gender selection: Play to Class 5 allows both Boys (ছাত্র) and Girls (ছাত্রী)
  var selectedGender by remember { mutableStateOf("Female") }
  var photoUriString by remember { mutableStateOf("") }

  // Photo picker
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    uri?.let { photoUriString = it.toString() }
  }

  var selectedClassId by remember { mutableStateOf(classes.firstOrNull()?.id ?: 3L) }
  var rollNumberText by remember { mutableStateOf("") }
  var admissionFeeText by remember { mutableStateOf("2000") }
  var collectFeeImmediately by remember { mutableStateOf(true) }

  var errorMessage by remember { mutableStateOf<String?>(null) }
  var isSubmitting by remember { mutableStateOf(false) }

  val selectedClass = classes.find { it.id == selectedClassId }
  val studentsInThisClass = students.filter { it.classId == selectedClassId && it.academicSession == currentSession }
  val suggestedRoll = (studentsInThisClass.maxOfOrNull { it.rollNumber } ?: 0) + 1

  LaunchedEffect(selectedClassId, studentsInThisClass.size) {
    if (rollNumberText.isEmpty() || rollNumberText.toIntOrNull() != suggestedRoll) {
      rollNumberText = suggestedRoll.toString()
    }
  }

  val nextStudentSeq = students.size + 1
  val generatedStudentId = "JNGM-$currentSession-${String.format(Locale.US, "%03d", nextStudentSeq)}"
  val generatedAdmissionNo = "ADM-$currentSession-${100 + nextStudentSeq}"
  val generatedGuardianPass = "1234"

  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(16.dp)
  ) {
    // Top banner with Madrasah Logo
    Card(
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = NavyPrimary),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = CircleShape,
          color = Color.White,
          modifier = Modifier.size(46.dp)
        ) {
          Image(
            painter = painterResource(id = R.drawable.ic_jngm_logo),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize().padding(3.dp)
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = if (isBengali) "নতুন শিক্ষার্থী ভর্তি ফরম" else "New Student Admission",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
          )
          Text(
            text = if (isBengali) "জাবালে নূর গার্লস মাদ্রাসা • সেশন: $currentSession" else "Jabale Noor Girls Madrasah • Session: $currentSession",
            color = GoldAccent,
            fontSize = 12.sp
          )
          Text(
            text = if (isBengali) "(প্লে হতে পঞ্চম শ্রেণি পর্যন্ত ছাত্র ও ছাত্রী উভয়ের ভর্তি উন্মুক্ত)" else "(Play to Class 5: Both Boys & Girls admitted)",
            color = EmeraldLight,
            fontSize = 10.sp
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Generated IDs & Guardian Credentials Preview
    Surface(
      shape = RoundedCornerShape(10.dp),
      color = Slate100,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text(text = if (isBengali) "স্বয়ংক্রিয় শিক্ষার্থী আইডি:" else "Student ID:", fontSize = 11.sp, color = Slate600)
            Text(text = generatedStudentId, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyPrimary)
          }
          Column(horizontalAlignment = Alignment.End) {
            Text(text = if (isBengali) "ভর্তি আবেদন নং:" else "Admission No:", fontSize = 11.sp, color = Slate600)
            Text(text = generatedAdmissionNo, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EmeraldDark)
          }
        }
        Spacer(modifier = Modifier.height(6.dp))
        HorizontalDivider(color = Slate200)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = if (isBengali) "অভিভাবক লগইন আইডি: $generatedStudentId" else "Guardian ID: $generatedStudentId",
            fontSize = 11.sp,
            color = Slate700
          )
          Text(
            text = if (isBengali) "ডিফল্ট পাসওয়ার্ড: $generatedGuardianPass" else "Default PIN: $generatedGuardianPass",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = EmeraldDark
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Student Photograph Option
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        shape = CircleShape,
        color = Slate200,
        modifier = Modifier
          .size(68.dp)
          .clickable { photoPickerLauncher.launch("image/*") }
      ) {
        if (photoUriString.isNotEmpty()) {
          AsyncImage(
            model = photoUriString,
            contentDescription = "Student Photo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        } else {
          Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Default.AddAPhoto, contentDescription = "Add Photo", tint = NavyPrimary, modifier = Modifier.size(28.dp))
          }
        }
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column {
        Text(
          text = if (isBengali) "শিক্ষার্থীর পাসপোর্ট ছবি যুক্ত করুন" else "Add Student Photograph",
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp
        )
        Text(
          text = if (isBengali) "গ্যালারি হতে ছবি বাছাই করতে ট্যাপ করুন" else "Tap avatar circle to choose image",
          fontSize = 11.sp,
          color = Slate600
        )
        if (photoUriString.isNotEmpty()) {
          TextButton(onClick = { photoUriString = "" }, contentPadding = PaddingValues(0.dp)) {
            Text(if (isBengali) "ছবি মুছুন" else "Remove Photo", color = CrimsonError, fontSize = 11.sp)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Form inputs
    Text(
      text = if (isBengali) "ব্যক্তিগত ও অভিভাবকের তথ্য" else "Personal & Guardian Information",
      fontWeight = FontWeight.Bold,
      fontSize = 14.sp
    )
    Spacer(modifier = Modifier.height(8.dp))

    // Gender Selection (Play to Class 5: Co-ed)
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = if (isBengali) "শিক্ষার্থীর লিঙ্গ:" else "Gender:",
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.width(110.dp)
      )
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
          selected = selectedGender == "Female",
          onClick = { selectedGender = "Female" },
          label = { Text(if (isBengali) "ছাত্রী (Female)" else "Female") },
          leadingIcon = {
            if (selectedGender == "Female") Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
          }
        )
        FilterChip(
          selected = selectedGender == "Male",
          onClick = { selectedGender = "Male" },
          label = { Text(if (isBengali) "ছাত্র (Male)" else "Male") },
          leadingIcon = {
            if (selectedGender == "Male") Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
          }
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
      value = fullNameBn,
      onValueChange = { fullNameBn = it },
      label = { Text(if (isBengali) "শিক্ষার্থীর পূর্ণ নাম (বাংলা) *" else "Full Name (Bengali) *") },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("student_name_bn_input"),
      singleLine = true
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
      value = fullNameEn,
      onValueChange = { fullNameEn = it },
      label = { Text(if (isBengali) "শিক্ষার্থীর পূর্ণ নাম (ইংরেজি) *" else "Full Name (English) *") },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("student_name_en_input"),
      singleLine = true
    )

    Spacer(modifier = Modifier.height(8.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      OutlinedTextField(
        value = fatherName,
        onValueChange = { fatherName = it },
        label = { Text(if (isBengali) "পিতার নাম *" else "Father's Name *") },
        modifier = Modifier.weight(1f),
        singleLine = true
      )
      OutlinedTextField(
        value = motherName,
        onValueChange = { motherName = it },
        label = { Text(if (isBengali) "মাতার নাম *" else "Mother's Name *") },
        modifier = Modifier.weight(1f),
        singleLine = true
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      OutlinedTextField(
        value = guardianPhone,
        onValueChange = { guardianPhone = it },
        label = { Text(if (isBengali) "অভিভাবকের মোবাইল নং *" else "Guardian Mobile *") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = Modifier.weight(1f),
        singleLine = true
      )
      OutlinedTextField(
        value = bloodGroup,
        onValueChange = { bloodGroup = it },
        label = { Text(if (isBengali) "রক্তের গ্রুপ" else "Blood Group") },
        modifier = Modifier.weight(0.6f),
        singleLine = true
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      OutlinedTextField(
        value = dateOfBirth,
        onValueChange = { dateOfBirth = it },
        label = { Text(if (isBengali) "জন্ম তারিখ (DD-MM-YYYY)" else "Birth Date") },
        modifier = Modifier.weight(1f),
        singleLine = true
      )
      OutlinedTextField(
        value = presentAddress,
        onValueChange = { presentAddress = it },
        label = { Text(if (isBengali) "ঠিকানা *" else "Address *") },
        modifier = Modifier.weight(1.2f),
        singleLine = true
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    Text(
      text = if (isBengali) "শ্রেণি ও রোল নির্ধারণ" else "Class & Roll Assignment",
      fontWeight = FontWeight.Bold,
      fontSize = 14.sp
    )
    Spacer(modifier = Modifier.height(8.dp))

    var expandedClassMenu by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
      expanded = expandedClassMenu,
      onExpandedChange = { expandedClassMenu = it },
      modifier = Modifier.fillMaxWidth()
    ) {
      OutlinedTextField(
        value = selectedClass?.let { if (isBengali) it.nameBn else it.nameEn } ?: "",
        onValueChange = {},
        readOnly = true,
        label = { Text(if (isBengali) "ভর্তির শ্রেণি নির্ধারণ করুন *" else "Select Admission Class *") },
        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedClassMenu) },
        modifier = Modifier
          .fillMaxWidth()
          .menuAnchor()
      )
      ExposedDropdownMenu(
        expanded = expandedClassMenu,
        onDismissRequest = { expandedClassMenu = false }
      ) {
        classes.forEach { c ->
          DropdownMenuItem(
            text = { Text(if (isBengali) c.nameBn else c.nameEn) },
            onClick = {
              selectedClassId = c.id
              expandedClassMenu = false
            }
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
      value = rollNumberText,
      onValueChange = { rollNumberText = it },
      label = { Text(if (isBengali) "ক্লাস রোল নম্বর *" else "Class Roll Number *") },
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      modifier = Modifier.fillMaxWidth(),
      singleLine = true
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Admission Fee Section
    Card(
      shape = RoundedCornerShape(10.dp),
      colors = CardDefaults.cardColors(containerColor = Slate100),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = if (isBengali) "ভর্তি ফি তাৎক্ষণিক গ্রহণ ও রশিদ" else "Collect Admission Fee Now",
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
          )
          Switch(
            checked = collectFeeImmediately,
            onCheckedChange = { collectFeeImmediately = it }
          )
        }

        if (collectFeeImmediately) {
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = admissionFeeText,
            onValueChange = { admissionFeeText = it },
            label = { Text(if (isBengali) "ভর্তি ফি পরিমাণ (৳)" else "Admission Fee (৳)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
        }
      }
    }

    // Error banner
    errorMessage?.let { err ->
      Spacer(modifier = Modifier.height(10.dp))
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = CrimsonContainer,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = CrimsonError)
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = err, color = CrimsonError, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Submit Button
    Button(
      onClick = {
        if (fullNameBn.isBlank() || fullNameEn.isBlank() || fatherName.isBlank() || guardianPhone.isBlank()) {
          errorMessage = if (isBengali) "অনুগ্রহ করে সকল আবশ্যকীয় তথ্য পূরণ করুন।" else "Please fill in all mandatory fields."
          return@Button
        }
        val roll = rollNumberText.toIntOrNull() ?: 1
        val feeAmount = admissionFeeText.toDoubleOrNull() ?: 2000.0

        isSubmitting = true
        errorMessage = null

        val newStudent = StudentEntity(
          studentId = generatedStudentId,
          admissionNumber = generatedAdmissionNo,
          fullNameBn = fullNameBn.trim(),
          fullNameEn = fullNameEn.trim(),
          fatherName = fatherName.trim(),
          motherName = motherName.trim(),
          guardianName = fatherName.trim(),
          guardianPhone = guardianPhone.trim(),
          dateOfBirth = dateOfBirth.trim(),
          gender = selectedGender,
          bloodGroup = bloodGroup.trim(),
          presentAddress = presentAddress.trim().ifEmpty { "রামগড়, খাগড়াছড়ি" },
          permanentAddress = presentAddress.trim().ifEmpty { "রামগড়, খাগড়াছড়ি" },
          admissionDate = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date()),
          academicSession = currentSession,
          classId = selectedClassId,
          className = selectedClass?.let { if (isBengali) it.nameBn else it.nameEn } ?: "শ্রেণি",
          section = "A",
          rollNumber = roll,
          photoUri = photoUriString,
          guardianLoginId = generatedStudentId,
          guardianPassword = generatedGuardianPass,
          status = "ACTIVE"
        )

        viewModel.addStudent(newStudent) { result ->
          isSubmitting = false
          result.fold(
            onSuccess = {
              Toast.makeText(context, if (isBengali) "শিক্ষার্থী সফলভাবে ভর্তি করা হয়েছে!" else "Student admitted successfully!", Toast.LENGTH_SHORT).show()

              if (collectFeeImmediately && feeAmount > 0) {
                val receiptNo = "REC-$currentSession-${String.format(Locale.US, "%04d", (100 + students.size))}"
                val txnId = "TXN-${System.currentTimeMillis() % 1000000}"
                val payment = FeePaymentEntity(
                  receiptNumber = receiptNo,
                  transactionId = txnId,
                  studentId = generatedStudentId,
                  studentName = "${fullNameBn.trim()} (${fullNameEn.trim()})",
                  classId = selectedClassId,
                  className = newStudent.className,
                  rollNumber = roll,
                  academicSession = currentSession,
                  date = newStudent.admissionDate,
                  monthFor = "ভর্তি ফি ২০২৬",
                  totalAmount = feeAmount,
                  discountAmount = 0.0,
                  paidAmount = feeAmount,
                  dueAmount = 0.0,
                  paymentMethod = "CASH",
                  feeDescription = "নতুন ভর্তি ফি",
                  receivedBy = "অফিস সহকারী",
                  remarks = "ভর্তিকালীন প্রাপ্তি"
                )
                val detail = listOf(
                  FeePaymentDetailEntity(
                    paymentId = 0,
                    feeCategoryId = 4, // Admission Fee
                    feeCategoryName = "ভর্তি ফি (Admission Fee)",
                    amount = feeAmount
                  )
                )
                viewModel.collectFee(payment, detail) {
                  onShowReceipt(payment)
                }
              }

              onNavigate(ERPScreen.Students)
            },
            onFailure = { ex ->
              errorMessage = ex.message ?: "ত্রুটি ঘটেছে।"
            }
          )
        }
      },
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .testTag("submit_admission_button"),
      colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
      enabled = !isSubmitting
    ) {
      if (isSubmitting) {
        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
      } else {
        Icon(Icons.Default.CheckCircle, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (isBengali) "ভর্তি নিশ্চিত ও সম্পন্ন করুন" else "Confirm Admission",
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp
        )
      }
    }
  }
}
