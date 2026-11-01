package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.FeeCategoryEntity
import com.example.data.entity.FeePaymentDetailEntity
import com.example.data.entity.FeePaymentEntity
import com.example.data.entity.StudentEntity
import com.example.data.model.PaymentMethod
import com.example.ui.theme.*
import com.example.ui.viewmodel.MadrasahViewModel
import com.example.util.ERPLocale
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeeCollectionScreen(
  viewModel: MadrasahViewModel,
  onShowReceipt: (FeePaymentEntity) -> Unit,
  modifier: Modifier = Modifier
) {
  val isBengali by viewModel.isBengali.collectAsState()
  val students by viewModel.students.collectAsState()
  val classes by viewModel.classes.collectAsState()
  val feeCategories by viewModel.feeCategories.collectAsState()
  val currentSession by viewModel.currentSession.collectAsState()
  val allPayments by viewModel.feePayments.collectAsState()
  val context = LocalContext.current

  var selectedClassId by remember { mutableStateOf<Long?>(classes.firstOrNull()?.id ?: 3L) }
  var selectedStudent by remember { mutableStateOf<StudentEntity?>(null) }
  var monthForText by remember { mutableStateOf("অক্টোবর ২০২৬ (October 2026)") }

  // Map of categoryId to (Boolean selected, Double amount)
  val categorySelections = remember { mutableStateMapOf<Long, Pair<Boolean, Double>>() }

  // Initialize or reset selections when category list loads
  LaunchedEffect(feeCategories) {
    if (categorySelections.isEmpty()) {
      feeCategories.forEach { cat ->
        categorySelections[cat.id] = Pair(cat.id == 1L, cat.defaultAmount)
      }
    }
  }

  // Update selected student when class changes
  val studentsInClass = remember(students, selectedClassId) {
    if (selectedClassId == null) emptyList() else students.filter { it.classId == selectedClassId }
  }

  LaunchedEffect(studentsInClass) {
    if (selectedStudent == null || selectedStudent !in studentsInClass) {
      selectedStudent = studentsInClass.firstOrNull()
    }
  }

  var discountText by remember { mutableStateOf("0") }
  var paidAmountText by remember { mutableStateOf("") }
  var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.CASH) }
  var receivedByText by remember { mutableStateOf("মোহাম্মদ রফিকুল ইসলাম (হিসাবরক্ষক)") }
  var remarksText by remember { mutableStateOf("") }

  // Calculations
  val subtotal = categorySelections.entries
    .filter { it.value.first }
    .sumOf { it.value.second }

  val discount = discountText.toDoubleOrNull() ?: 0.0
  val netPayable = (subtotal - discount).coerceAtLeast(0.0)

  // Auto-sync paid amount if blank
  LaunchedEffect(netPayable) {
    if (paidAmountText.isEmpty() || paidAmountText.toDoubleOrNull() == 0.0) {
      paidAmountText = netPayable.toInt().toString()
    }
  }

  val actualPaid = paidAmountText.toDoubleOrNull() ?: netPayable
  val remainingDue = (netPayable - actualPaid).coerceAtLeast(0.0)

  var showHistoryTab by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    // Top Tabs: "নতুন ফি আদায় (New Collection)" and "আদায়ের ইতিহাস (History)"
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      TabRow(
        selectedTabIndex = if (showHistoryTab) 1 else 0,
        modifier = Modifier.weight(1f)
      ) {
        Tab(
          selected = !showHistoryTab,
          onClick = { showHistoryTab = false },
          text = { Text(if (isBengali) "ফি আদায় চালান (Invoice)" else "Fee Collection") }
        )
        Tab(
          selected = showHistoryTab,
          onClick = { showHistoryTab = true },
          text = { Text(if (isBengali) "রশিদ ইতিহাস (${allPayments.size})" else "Receipts (${allPayments.size})") }
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    if (showHistoryTab) {
      // Payment History List
      if (allPayments.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text(text = if (isBengali) "কোনো রসিদ পাওয়া যায়নি" else "No payment records")
        }
      } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          items(allPayments) { payment ->
            Card(
              shape = RoundedCornerShape(10.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onShowReceipt(payment) },
              elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(text = payment.studentName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                  Text(text = "${payment.receiptNumber} • ${payment.date}", fontSize = 11.sp, color = Slate600)
                  Text(text = "${payment.className} • রোল: ${payment.rollNumber}", fontSize = 11.sp, color = Slate600)
                }
                Column(horizontalAlignment = Alignment.End) {
                  Text(text = ERPLocale.formatTaka(payment.paidAmount), fontWeight = FontWeight.Bold, color = EmeraldDark, fontSize = 14.sp)
                  Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = EmeraldContainer
                  ) {
                    Text(
                      text = payment.paymentMethod,
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
    } else {
      // New Fee Collection Invoice Layout
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
      ) {
        // Step 1: Select Class & Student
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = NavyContainer),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = if (isBengali) "১. ছাত্রী ও শ্রেণি নির্বাচন" else "1. Select Class & Student",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = NavyPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Class Dropdown
            var classMenuExpanded by remember { mutableStateOf(false) }
            val currentClass = classes.find { it.id == selectedClassId }
            ExposedDropdownMenuBox(
              expanded = classMenuExpanded,
              onExpandedChange = { classMenuExpanded = it }
            ) {
              OutlinedTextField(
                value = currentClass?.let { if (isBengali) it.nameBn else it.nameEn } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(if (isBengali) "শ্রেণি" else "Class") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classMenuExpanded) },
                modifier = Modifier
                  .fillMaxWidth()
                  .menuAnchor()
              )
              ExposedDropdownMenu(
                expanded = classMenuExpanded,
                onDismissRequest = { classMenuExpanded = false }
              ) {
                classes.forEach { c ->
                  DropdownMenuItem(
                    text = { Text(if (isBengali) c.nameBn else c.nameEn) },
                    onClick = {
                      selectedClassId = c.id
                      classMenuExpanded = false
                    }
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Student Dropdown
            var studentMenuExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
              expanded = studentMenuExpanded,
              onExpandedChange = { studentMenuExpanded = it }
            ) {
              OutlinedTextField(
                value = selectedStudent?.let { "${it.rollNumber}. ${it.fullNameBn} (${it.studentId})" } ?: (if (isBengali) "কোনো ছাত্রী নেই" else "No students in this class"),
                onValueChange = {},
                readOnly = true,
                label = { Text(if (isBengali) "ছাত্রীর নাম ও রোল" else "Student & Roll") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = studentMenuExpanded) },
                modifier = Modifier
                  .fillMaxWidth()
                  .menuAnchor()
              )
              ExposedDropdownMenu(
                expanded = studentMenuExpanded,
                onDismissRequest = { studentMenuExpanded = false }
              ) {
                studentsInClass.forEach { s ->
                  DropdownMenuItem(
                    text = { Text("রোল ${s.rollNumber} - ${s.fullNameBn} (${s.studentId})") },
                    onClick = {
                      selectedStudent = s
                      studentMenuExpanded = false
                    }
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Step 2: Fee Category Multi-Selection
        Text(
          text = if (isBengali) "২. ফি খাতসমূহ নির্বাচন করুন" else "2. Select Fee Heads",
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(6.dp))

        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth(),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            feeCategories.forEach { category ->
              val isSelected = categorySelections[category.id]?.first ?: false
              val currentAmount = categorySelections[category.id]?.second ?: category.defaultAmount

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier
                    .weight(1f)
                    .clickable {
                      categorySelections[category.id] = Pair(!isSelected, currentAmount)
                    }
                ) {
                  Checkbox(
                    checked = isSelected,
                    onCheckedChange = { checked ->
                      categorySelections[category.id] = Pair(checked, currentAmount)
                    }
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Column {
                    Text(
                      text = if (isBengali) category.nameBn else category.nameEn,
                      fontSize = 13.sp,
                      fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                    Text(
                      text = if (category.type == "RECURRING_MONTHLY") (if (isBengali) "মাসিক ফি" else "Monthly") else (if (isBengali) "এককালীন ফি" else "One-time"),
                      fontSize = 10.sp,
                      color = Slate600
                    )
                  }
                }

                if (isSelected) {
                  OutlinedTextField(
                    value = currentAmount.toInt().toString(),
                    onValueChange = { newVal ->
                      val amt = newVal.toDoubleOrNull() ?: 0.0
                      categorySelections[category.id] = Pair(true, amt)
                    },
                    modifier = Modifier.width(110.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    prefix = { Text("৳", fontSize = 11.sp) }
                  )
                } else {
                  Text(
                    text = ERPLocale.formatTaka(category.defaultAmount),
                    fontSize = 12.sp,
                    color = Slate600,
                    modifier = Modifier.padding(end = 12.dp)
                  )
                }
              }
              HorizontalDivider(color = Slate100)
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Step 3: Payment Details, Discount & Method
        Text(
          text = if (isBengali) "৩. পরিশোধের বিবরণ ও রশিদ প্রস্তুত" else "3. Payment Calculation",
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(6.dp))

        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Slate100),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            // Month label
            OutlinedTextField(
              value = monthForText,
              onValueChange = { monthForText = it },
              label = { Text(if (isBengali) "মাসের নাম / বাবদ" else "Fee For Month / Purpose") },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtotal row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(text = if (isBengali) "নির্ধারিত ফি মোট:" else "Subtotal Fee:", fontSize = 13.sp)
              Text(text = ERPLocale.formatTaka(subtotal), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Discount Input
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(text = if (isBengali) "বিশেষ ছাড় / মওকুফ (৳):" else "Discount / Waiver (৳):", fontSize = 13.sp)
              OutlinedTextField(
                value = discountText,
                onValueChange = { discountText = it },
                modifier = Modifier.width(130.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
              )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Net Payable row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(text = if (isBengali) "প্রদেয় সর্বমোট:" else "Net Payable:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyPrimary)
              Text(text = ERPLocale.formatTaka(netPayable), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyPrimary)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Actual Paid Input
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(text = if (isBengali) "আদায়ের পরিমাণ (৳):" else "Paid Amount (৳):", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = EmeraldDark)
              OutlinedTextField(
                value = paidAmountText,
                onValueChange = { paidAmountText = it },
                modifier = Modifier.width(130.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
              )
            }

            if (remainingDue > 0) {
              Spacer(modifier = Modifier.height(4.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(text = if (isBengali) "বকেয়া থাকবে:" else "Remaining Due:", fontSize = 12.sp, color = CrimsonError)
                Text(text = ERPLocale.formatTaka(remainingDue), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CrimsonError)
              }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(10.dp))

            // Payment Method Selector
            Text(text = if (isBengali) "পরিশোধের মাধ্যম (Payment Method):" else "Payment Method:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              PaymentMethod.values().forEach { method ->
                val isSel = selectedPaymentMethod == method
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = if (isSel) NavyPrimary else Color.White,
                  modifier = Modifier
                    .weight(1f)
                    .clickable { selectedPaymentMethod = method }
                ) {
                  Box(
                    modifier = Modifier.padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = if (isBengali) method.titleBn else method.titleEn,
                      color = if (isSel) Color.White else Slate800,
                      fontSize = 11.sp,
                      fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                    )
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Submit & Print Receipt Button
        Button(
          onClick = {
            val student = selectedStudent
            if (student == null) {
              Toast.makeText(context, if (isBengali) "অনুগ্রহ করে ছাত্রী নির্বাচন করুন" else "Please select a student", Toast.LENGTH_SHORT).show()
              return@Button
            }
            if (actualPaid <= 0 && netPayable > 0) {
              Toast.makeText(context, if (isBengali) "পরিশোধের পরিমাণ প্রদান করুন" else "Enter valid payment amount", Toast.LENGTH_SHORT).show()
              return@Button
            }

            val receiptSeq = allPayments.size + 1
            val receiptNo = "REC-$currentSession-${String.format(Locale.US, "%04d", 100 + receiptSeq)}"
            val txnId = "TXN-${System.currentTimeMillis() % 1000000}"

            val paymentEntity = FeePaymentEntity(
              receiptNumber = receiptNo,
              transactionId = txnId,
              studentId = student.studentId,
              studentName = "${student.fullNameBn} (${student.fullNameEn})",
              classId = student.classId,
              className = student.className,
              rollNumber = student.rollNumber,
              academicSession = currentSession,
              date = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date()),
              monthFor = monthForText,
              totalAmount = subtotal,
              discountAmount = discount,
              paidAmount = actualPaid,
              dueAmount = remainingDue,
              paymentMethod = selectedPaymentMethod.name,
              feeDescription = monthForText,
              receivedBy = receivedByText,
              remarks = remarksText
            )

            val details = categorySelections.entries
              .filter { it.value.first }
              .map { entry ->
                val cat = feeCategories.find { it.id == entry.key }
                FeePaymentDetailEntity(
                  paymentId = 0,
                  feeCategoryId = entry.key,
                  feeCategoryName = cat?.let { if (isBengali) it.nameBn else it.nameEn } ?: "ফি",
                  amount = entry.value.second
                )
              }

            viewModel.collectFee(paymentEntity, details) { res ->
              res.fold(
                onSuccess = {
                  Toast.makeText(context, if (isBengali) "ফি গ্রহণ সম্পন্ন হয়েছে ও রশিদ তৈরি হয়েছে!" else "Fee received and receipt generated!", Toast.LENGTH_SHORT).show()
                  onShowReceipt(paymentEntity)
                },
                onFailure = { ex ->
                  Toast.makeText(context, "ত্রুটি: ${ex.message}", Toast.LENGTH_LONG).show()
                }
              )
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("collect_fee_submit_button")
        ) {
          Icon(Icons.Default.ReceiptLong, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isBengali) "ফি গ্রহণ ও অফিসিয়াল মানি রশিদ তৈরি করুন" else "Receive Fee & Generate Money Receipt",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
        }

        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}
