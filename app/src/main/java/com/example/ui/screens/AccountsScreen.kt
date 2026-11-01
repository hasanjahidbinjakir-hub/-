package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.IncomeEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MadrasahViewModel
import com.example.util.ERPLocale
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AccountsScreen(
  viewModel: MadrasahViewModel,
  modifier: Modifier = Modifier
) {
  val isBengali by viewModel.isBengali.collectAsState()
  val expenses by viewModel.expenses.collectAsState()
  val incomes by viewModel.incomes.collectAsState()
  val totalFeeCollection by viewModel.totalFeeCollection.collectAsState()
  val totalExpenses by viewModel.totalExpenses.collectAsState()
  val totalOtherIncome by viewModel.totalOtherIncome.collectAsState()
  val currentSession by viewModel.currentSession.collectAsState()
  val context = LocalContext.current

  val totalIncomeAll = totalFeeCollection + totalOtherIncome
  val netBalance = totalIncomeAll - totalExpenses

  var selectedTab by remember { mutableStateOf(0) } // 0 = Overview & Transactions, 1 = Add Expense, 2 = Add Income
  var filterCategory by remember { mutableStateOf<String?>(null) }

  val expenseCategories = listOf(
    "Teacher Salaries", "Staff Salaries", "House Rent", "Electricity",
    "Internet", "Transport", "Educational Materials", "Maintenance",
    "Food and Accommodation", "Office Expenses", "Other Expenses"
  )

  val incomeCategories = listOf(
    "Donations", "Grants", "Student Fee Collection", "Admission Income", "Other Income"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    // Top Summary Card
    Card(
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = NavyPrimary),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = if (isBengali) "হিসাব ও অর্থ ব্যবস্থাপনা" else "Accounts & Financials",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp
            )
            Text(
              text = if (isBengali) "প্রকৃত লেনদেনের হিসাব • সেশন $currentSession" else "Real Transaction Ledger • $currentSession",
              color = Slate200,
              fontSize = 11.sp
            )
          }
          Icon(Icons.Default.AccountBalance, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(36.dp))
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = NavyLight)
        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Column {
            Text(text = if (isBengali) "মোট আয়" else "Total Income", color = EmeraldLight, fontSize = 11.sp)
            Text(text = ERPLocale.formatTaka(totalIncomeAll), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
          }
          Column {
            Text(text = if (isBengali) "মোট ব্যয়" else "Total Expense", color = Color(0xFFFCA5A5), fontSize = 11.sp)
            Text(text = ERPLocale.formatTaka(totalExpenses), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
          }
          Column(horizontalAlignment = Alignment.End) {
            Text(text = if (isBengali) "তহবিল স্থিতি" else "Net Balance", color = GoldAccent, fontSize = 11.sp)
            Text(text = ERPLocale.formatTaka(netBalance), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Navigation Tabs
    TabRow(selectedTabIndex = selectedTab) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = { Text(if (isBengali) "লেনদেন খতিয়ান" else "Ledger") }
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = { Text(if (isBengali) "ব্যয় সংযুক্তি (+)" else "+ Expense") }
      )
      Tab(
        selected = selectedTab == 2,
        onClick = { selectedTab = 2 },
        text = { Text(if (isBengali) "আয় সংযুক্তি (+)" else "+ Income") }
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    when (selectedTab) {
      0 -> {
        // Combined Transactions Ledger
        LazyColumn(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          item {
            Text(
              text = if (isBengali) "সকল আয় ও ব্যয়ের তালিকা" else "All Incomes & Expenses",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
          }

          items(expenses) { exp ->
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
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = CrimsonContainer,
                    modifier = Modifier.size(36.dp)
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Icon(Icons.Default.ArrowOutward, contentDescription = null, tint = CrimsonError, modifier = Modifier.size(18.dp))
                    }
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(text = exp.category, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = "${exp.description} (${exp.recipient})", fontSize = 11.sp, color = Slate600)
                    Text(text = "${exp.voucherNumber} • ${exp.date}", fontSize = 10.sp, color = Slate600)
                  }
                }
                Text(
                  text = "-${ERPLocale.formatTaka(exp.amount)}",
                  fontWeight = FontWeight.Bold,
                  color = CrimsonError,
                  fontSize = 14.sp
                )
              }
            }
          }

          items(incomes) { inc ->
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
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = EmeraldContainer,
                    modifier = Modifier.size(36.dp)
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Icon(Icons.Default.SouthWest, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(18.dp))
                    }
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(text = inc.category, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = inc.donorOrSource, fontSize = 11.sp, color = Slate600)
                    Text(text = "${inc.receiptNumber} • ${inc.date}", fontSize = 10.sp, color = Slate600)
                  }
                }
                Text(
                  text = "+${ERPLocale.formatTaka(inc.amount)}",
                  fontWeight = FontWeight.Bold,
                  color = EmeraldDark,
                  fontSize = 14.sp
                )
              }
            }
          }
        }
      }

      1 -> {
        // Record Expense Form
        RecordExpenseForm(
          categories = expenseCategories,
          currentSession = currentSession,
          isBengali = isBengali,
          onSave = { expense ->
            viewModel.recordExpense(expense) {
              Toast.makeText(context, if (isBengali) "ব্যয় সফলভাবে লিপিবদ্ধ করা হয়েছে!" else "Expense recorded successfully!", Toast.LENGTH_SHORT).show()
              selectedTab = 0
            }
          }
        )
      }

      2 -> {
        // Record Income Form
        RecordIncomeForm(
          categories = incomeCategories,
          currentSession = currentSession,
          isBengali = isBengali,
          onSave = { income ->
            viewModel.recordIncome(income) {
              Toast.makeText(context, if (isBengali) "আয় সফলভাবে লিপিবদ্ধ করা হয়েছে!" else "Income recorded successfully!", Toast.LENGTH_SHORT).show()
              selectedTab = 0
            }
          }
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordExpenseForm(
  categories: List<String>,
  currentSession: String,
  isBengali: Boolean,
  onSave: (ExpenseEntity) -> Unit
) {
  var selectedCategory by remember { mutableStateOf(categories.first()) }
  var amountText by remember { mutableStateOf("") }
  var recipientText by remember { mutableStateOf("") }
  var descriptionText by remember { mutableStateOf("") }
  var categoryExpanded by remember { mutableStateOf(false) }

  Column(modifier = Modifier.fillMaxWidth()) {
    Text(
      text = if (isBengali) "নতুন ব্যয়ের ভাউচার এন্ট্রি" else "New Expense Voucher",
      fontWeight = FontWeight.Bold,
      fontSize = 15.sp,
      color = CrimsonError
    )
    Spacer(modifier = Modifier.height(10.dp))

    ExposedDropdownMenuBox(
      expanded = categoryExpanded,
      onExpandedChange = { categoryExpanded = it }
    ) {
      OutlinedTextField(
        value = selectedCategory,
        onValueChange = {},
        readOnly = true,
        label = { Text(if (isBengali) "ব্যয়ের খাত *" else "Expense Category *") },
        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
        modifier = Modifier
          .fillMaxWidth()
          .menuAnchor()
      )
      ExposedDropdownMenu(
        expanded = categoryExpanded,
        onDismissRequest = { categoryExpanded = false }
      ) {
        categories.forEach { cat ->
          DropdownMenuItem(
            text = { Text(cat) },
            onClick = {
              selectedCategory = cat
              categoryExpanded = false
            }
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
      value = amountText,
      onValueChange = { amountText = it },
      label = { Text(if (isBengali) "ব্যয়ের পরিমাণ (৳) *" else "Amount (৳) *") },
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("expense_amount_input"),
      singleLine = true
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
      value = recipientText,
      onValueChange = { recipientText = it },
      label = { Text(if (isBengali) "প্রাপক / সরবরাহকারী *" else "Recipient / Payee *") },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
      value = descriptionText,
      onValueChange = { descriptionText = it },
      label = { Text(if (isBengali) "বিবরণ ও মন্তব্য" else "Description & Notes") },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true
    )

    Spacer(modifier = Modifier.height(16.dp))

    Button(
      onClick = {
        val amt = amountText.toDoubleOrNull() ?: 0.0
        if (amt <= 0 || recipientText.isBlank()) return@Button
        val voucher = "EXP-$currentSession-${System.currentTimeMillis() % 10000}"
        val expense = ExpenseEntity(
          voucherNumber = voucher,
          category = selectedCategory,
          amount = amt,
          date = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date()),
          academicSession = currentSession,
          description = descriptionText.ifEmpty { selectedCategory },
          recipient = recipientText
        )
        onSave(expense)
      },
      colors = ButtonDefaults.buttonColors(containerColor = CrimsonError),
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .testTag("submit_expense_button")
    ) {
      Icon(Icons.Default.AddCircle, contentDescription = null)
      Spacer(modifier = Modifier.width(8.dp))
      Text(if (isBengali) "ব্যয় সংরক্ষণ করুন" else "Save Expense")
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordIncomeForm(
  categories: List<String>,
  currentSession: String,
  isBengali: Boolean,
  onSave: (IncomeEntity) -> Unit
) {
  var selectedCategory by remember { mutableStateOf(categories.first()) }
  var amountText by remember { mutableStateOf("") }
  var donorText by remember { mutableStateOf("") }
  var descriptionText by remember { mutableStateOf("") }
  var categoryExpanded by remember { mutableStateOf(false) }

  Column(modifier = Modifier.fillMaxWidth()) {
    Text(
      text = if (isBengali) "নতুন আয় / অনুদান এন্ট্রি" else "New Income / Donation Entry",
      fontWeight = FontWeight.Bold,
      fontSize = 15.sp,
      color = EmeraldPrimary
    )
    Spacer(modifier = Modifier.height(10.dp))

    ExposedDropdownMenuBox(
      expanded = categoryExpanded,
      onExpandedChange = { categoryExpanded = it }
    ) {
      OutlinedTextField(
        value = selectedCategory,
        onValueChange = {},
        readOnly = true,
        label = { Text(if (isBengali) "আয়ের উৎস / খাত *" else "Income Category *") },
        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
        modifier = Modifier
          .fillMaxWidth()
          .menuAnchor()
      )
      ExposedDropdownMenu(
        expanded = categoryExpanded,
        onDismissRequest = { categoryExpanded = false }
      ) {
        categories.forEach { cat ->
          DropdownMenuItem(
            text = { Text(cat) },
            onClick = {
              selectedCategory = cat
              categoryExpanded = false
            }
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
      value = amountText,
      onValueChange = { amountText = it },
      label = { Text(if (isBengali) "আয়ের পরিমাণ (৳) *" else "Amount (৳) *") },
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("income_amount_input"),
      singleLine = true
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
      value = donorText,
      onValueChange = { donorText = it },
      label = { Text(if (isBengali) "দাতা / উৎসের নাম *" else "Donor / Source *") },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
      value = descriptionText,
      onValueChange = { descriptionText = it },
      label = { Text(if (isBengali) "বিবরণ ও তহবিল নাম" else "Description") },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true
    )

    Spacer(modifier = Modifier.height(16.dp))

    Button(
      onClick = {
        val amt = amountText.toDoubleOrNull() ?: 0.0
        if (amt <= 0 || donorText.isBlank()) return@Button
        val receiptNo = "INC-$currentSession-${System.currentTimeMillis() % 10000}"
        val income = IncomeEntity(
          receiptNumber = receiptNo,
          category = selectedCategory,
          amount = amt,
          date = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date()),
          academicSession = currentSession,
          donorOrSource = donorText,
          description = descriptionText
        )
        onSave(income)
      },
      colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .testTag("submit_income_button")
    ) {
      Icon(Icons.Default.AddCircle, contentDescription = null)
      Spacer(modifier = Modifier.width(8.dp))
      Text(if (isBengali) "আয় সংরক্ষণ করুন" else "Save Income")
    }
  }
}
