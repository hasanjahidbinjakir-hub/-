package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.EmployeeEntity
import com.example.data.entity.SalaryPaymentEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MadrasahViewModel
import com.example.util.ERPLocale
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TeachersScreen(
  viewModel: MadrasahViewModel,
  modifier: Modifier = Modifier
) {
  val isBengali by viewModel.isBengali.collectAsState()
  val employees by viewModel.employees.collectAsState()
  val salaryPayments by viewModel.salaryPayments.collectAsState()
  val context = LocalContext.current

  var showAddDialog by remember { mutableStateOf(false) }
  var employeeForSalary by remember { mutableStateOf<EmployeeEntity?>(null) }
  var selectedTab by remember { mutableStateOf(0) } // 0 = Staff Directory, 1 = Salary History

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
          text = if (isBengali) "শিক্ষক ও কর্মচারী ব্যবস্থাপনা" else "Teachers & Staff",
          fontWeight = FontWeight.Bold,
          fontSize = 17.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = if (isBengali) "মোট কর্মরত সদস্য: ${employees.size} জন" else "Total Employees: ${employees.size}",
          fontSize = 12.sp,
          color = Slate600
        )
      }

      Button(
        onClick = { showAddDialog = true },
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
        modifier = Modifier.testTag("add_employee_button")
      ) {
        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(if (isBengali) "নতুন যোগ" else "Add New", fontSize = 12.sp)
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    TabRow(selectedTabIndex = selectedTab) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = { Text(if (isBengali) "কর্মচারী তালিকা (${employees.size})" else "Staff List (${employees.size})") }
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = { Text(if (isBengali) "বেতন প্রদান খতিয়ান" else "Salary Payments") }
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    if (selectedTab == 0) {
      LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
        items(employees) { emp ->
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth(),
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
                Surface(
                  shape = CircleShape,
                  color = NavyPrimary,
                  modifier = Modifier.size(46.dp)
                ) {
                  if (!emp.photoUri.isNullOrBlank()) {
                    coil.compose.AsyncImage(
                      model = emp.photoUri,
                      contentDescription = "Staff Photo",
                      modifier = Modifier.fillMaxSize(),
                      contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                  } else {
                    Box(contentAlignment = Alignment.Center) {
                      Icon(Icons.Default.Badge, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(24.dp))
                    }
                  }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(text = emp.nameBn, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                  Text(text = "${emp.designation} • ${emp.department}", fontSize = 11.sp, color = Slate600)
                  Text(text = "লগইন ইমেইল: ${emp.email}", fontSize = 10.sp, color = EmeraldDark, fontWeight = FontWeight.SemiBold)
                  Text(text = "মোবাইল: ${emp.phone} • পাসওয়ার্ড: ${emp.password}", fontSize = 10.sp, color = Slate600)
                  Text(text = "নির্ধারিত বেতন: ${ERPLocale.formatTaka(emp.monthlySalary)}", fontSize = 11.sp, color = NavyPrimary, fontWeight = FontWeight.SemiBold)
                }
              }

              // Disburse Salary Button
              Button(
                onClick = { employeeForSalary = emp },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Text(if (isBengali) "বেতন দিন" else "Pay Salary", fontSize = 11.sp, color = Color.White)
              }
            }
          }
        }
      }
    } else {
      // Salary Payments History
      if (salaryPayments.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text(text = if (isBengali) "কোনো বেতন প্রদানের তথ্য পাওয়া যায়নি" else "No salary disbursement records")
        }
      } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
          items(salaryPayments) { sp ->
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
                  Text(text = sp.employeeName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                  Text(text = "${sp.monthYear} • ভাউচার: ${sp.voucherNumber}", fontSize = 11.sp, color = Slate600)
                  Text(text = "তারিখ: ${sp.date} • মাধ্যম: ${sp.paymentMethod}", fontSize = 10.sp, color = Slate600)
                }
                Text(
                  text = ERPLocale.formatTaka(sp.amount),
                  fontWeight = FontWeight.Bold,
                  color = CrimsonError,
                  fontSize = 14.sp
                )
              }
            }
          }
        }
      }
    }
  }

  // Add Employee Dialog
  if (showAddDialog) {
    var nameBn by remember { mutableStateOf("") }
    var nameEn by remember { mutableStateOf("") }
    var designation by remember { mutableStateOf("সহকারী শিক্ষিকা") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("123456") }
    var photoUri by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("Academic") }
    var phone by remember { mutableStateOf("") }
    var salaryText by remember { mutableStateOf("15000") }

    val photoPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
      contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
      uri?.let { photoUri = it.toString() }
    }

    Dialog(onDismissRequest = { showAddDialog = false }) {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
        ) {
          Text(
            text = if (isBengali) "নতুন শিক্ষক / কর্মী সংযুক্তি" else "Add New Employee",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = NavyPrimary
          )
          Spacer(modifier = Modifier.height(10.dp))

          // Photo Picker Row
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Surface(
              shape = CircleShape,
              color = EmeraldContainer,
              modifier = Modifier.size(50.dp)
            ) {
              if (photoUri.isNotBlank()) {
                coil.compose.AsyncImage(
                  model = photoUri,
                  contentDescription = null,
                  modifier = Modifier.fillMaxSize(),
                  contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
              } else {
                Box(contentAlignment = Alignment.Center) {
                  Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = EmeraldDark)
                }
              }
            }
            OutlinedButton(
              onClick = {
                photoPickerLauncher.launch(
                  androidx.activity.result.PickVisualMediaRequest(
                    androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                  )
                )
              }
            ) {
              Text(if (isBengali) "শিক্ষকের ছবি যুক্ত করুন" else "Add Photo", fontSize = 11.sp)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = nameBn,
            onValueChange = { nameBn = it },
            label = { Text(if (isBengali) "নাম (বাংলা) *" else "Name (Bengali) *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(if (isBengali) "লগইন ইমেইল *" else "Login Email *") },
            placeholder = { Text("teacher@jabalenoor.edu.bd") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(if (isBengali) "লগইন পাসওয়ার্ড *" else "Login Password *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedTextField(
            value = designation,
            onValueChange = { designation = it },
            label = { Text(if (isBengali) "পদবি *" else "Designation *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text(if (isBengali) "মোবাইল নম্বর *" else "Phone *") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedTextField(
            value = salaryText,
            onValueChange = { salaryText = it },
            label = { Text(if (isBengali) "মাসিক বেতন (৳) *" else "Monthly Salary (৳) *") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )

          Spacer(modifier = Modifier.height(14.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { showAddDialog = false }) {
              Text(if (isBengali) "বাতিল" else "Cancel")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = {
                if (nameBn.isBlank() || phone.isBlank()) return@Button
                val sal = salaryText.toDoubleOrNull() ?: 15000.0
                val empId = "EMP-${100 + employees.size + 1}"
                val empEmail = if (email.isNotBlank()) email.trim() else "teacher${employees.size + 1}@jabalenoor.edu.bd"
                val emp = EmployeeEntity(
                  employeeId = empId,
                  nameBn = nameBn,
                  nameEn = nameEn.ifEmpty { nameBn },
                  designation = designation,
                  department = department,
                  phone = phone,
                  email = empEmail,
                  password = if (password.isNotBlank()) password.trim() else "123456",
                  photoUri = photoUri,
                  address = "রামগড়, খাগড়াছড়ি",
                  joiningDate = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date()),
                  monthlySalary = sal
                )
                viewModel.addEmployee(emp) {
                  Toast.makeText(context, if (isBengali) "কর্মী সংরক্ষিত হয়েছে" else "Employee saved", Toast.LENGTH_SHORT).show()
                  showAddDialog = false
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
              Text(if (isBengali) "সংরক্ষণ" else "Save")
            }
          }
        }
      }
    }
  }

  // Disburse Salary Dialog
  employeeForSalary?.let { emp ->
    var salaryAmountText by remember { mutableStateOf(emp.monthlySalary.toInt().toString()) }
    var monthYearText by remember { mutableStateOf("অক্টোবর ২০২৬ (October 2026)") }

    Dialog(onDismissRequest = { employeeForSalary = null }) {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = if (isBengali) "মাসিক বেতন প্রদান ভাউচার" else "Disburse Salary Voucher",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = NavyPrimary
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(text = "${emp.nameBn} (${emp.designation})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = EmeraldDark)
          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = monthYearText,
            onValueChange = { monthYearText = it },
            label = { Text(if (isBengali) "বেতনের মাস / সময়কাল *" else "Month / Period *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = salaryAmountText,
            onValueChange = { salaryAmountText = it },
            label = { Text(if (isBengali) "বেতনের পরিমাণ (৳) *" else "Salary Amount (৳) *") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )

          Spacer(modifier = Modifier.height(14.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { employeeForSalary = null }) {
              Text(if (isBengali) "বাতিল" else "Cancel")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = {
                val amt = salaryAmountText.toDoubleOrNull() ?: emp.monthlySalary
                val voucher = "SAL-${System.currentTimeMillis() % 10000}"
                val sp = SalaryPaymentEntity(
                  voucherNumber = voucher,
                  employeeId = emp.employeeId,
                  employeeName = emp.nameBn,
                  monthYear = monthYearText,
                  amount = amt,
                  date = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date()),
                  remarks = "মাসিক বেতন পরিশোধ"
                )
                viewModel.disburseSalary(sp) {
                  Toast.makeText(context, if (isBengali) "বেতন সফলভাবে প্রদান ও ব্যয়ে লিপিবদ্ধ হয়েছে!" else "Salary paid and recorded in expense!", Toast.LENGTH_SHORT).show()
                  employeeForSalary = null
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
            ) {
              Text(if (isBengali) "বেতন নিশ্চিত করুন" else "Confirm Payment")
            }
          }
        }
      }
    }
  }
}
