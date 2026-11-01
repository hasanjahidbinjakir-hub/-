package com.example.ui.screens

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ERPScreen
import com.example.ui.viewmodel.MadrasahViewModel
import com.example.util.ERPLocale

@Composable
fun DashboardScreen(
  viewModel: MadrasahViewModel,
  onNavigate: (ERPScreen) -> Unit,
  onShowReceipt: (FeePaymentEntity) -> Unit,
  modifier: Modifier = Modifier
) {
  val isBengali by viewModel.isBengali.collectAsState()
  val students by viewModel.students.collectAsState()
  val classes by viewModel.classes.collectAsState()
  val payments by viewModel.feePayments.collectAsState()
  val expenses by viewModel.expenses.collectAsState()
  val incomes by viewModel.incomes.collectAsState()
  val employees by viewModel.employees.collectAsState()
  val notices by viewModel.notices.collectAsState()

  val todayCollection by viewModel.todayCollection.collectAsState()
  val totalCollection by viewModel.totalFeeCollection.collectAsState()
  val totalDue by viewModel.totalFeeDue.collectAsState()
  val totalExpenses by viewModel.totalExpenses.collectAsState()
  val totalOtherIncome by viewModel.totalOtherIncome.collectAsState()
  val activeStudentCount by viewModel.activeStudentCount.collectAsState()
  val todayPresentCount by viewModel.todayPresentCount.collectAsState()
  val todayAbsentCount by viewModel.todayAbsentCount.collectAsState()

  val totalIncomeAll = totalCollection + totalOtherIncome
  val netBalance = totalIncomeAll - totalExpenses

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("dashboard_scroll_column"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Institution Welcome & Status Banner
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavyPrimary),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (isBengali) "আসসালামু আলাইকুম" else "Assalamu Alaikum",
              color = GoldAccent,
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (isBengali) "জাবালে নূর গার্লস মাদ্রাসা ইআরপি" else "Jabale Noor Girls Madrasah ERP",
              color = Color.White,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (isBengali) "রামগড়, খাগড়াছড়ি • শিক্ষা সেশন ২০২৬" else "Ramgarh, Khagrachhari • Session 2026",
              color = Slate200,
              fontSize = 12.sp
            )
          }
          Icon(
            imageVector = Icons.Default.Mosque,
            contentDescription = null,
            tint = EmeraldLight,
            modifier = Modifier.size(48.dp)
          )
        }
      }
    }

    // 2. Quick Action Bar
    item {
      Text(
        text = if (isBengali) "দ্রুত কার্যক্রম (Quick Actions)" else "Quick Actions",
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        QuickActionButton(
          title = if (isBengali) "নতুন ভর্তি" else "Admission",
          icon = Icons.Default.PersonAdd,
          color = EmeraldPrimary,
          modifier = Modifier.weight(1f)
        ) {
          onNavigate(ERPScreen.Admissions)
        }
        QuickActionButton(
          title = if (isBengali) "ফি কালেকশন" else "Fee Collection",
          icon = Icons.Default.Receipt,
          color = NavyPrimary,
          modifier = Modifier.weight(1f)
        ) {
          onNavigate(ERPScreen.FeeCollection)
        }
        QuickActionButton(
          title = if (isBengali) "ব্যয় যুক্ত" else "Add Expense",
          icon = Icons.Default.AddCard,
          color = GoldAccent,
          modifier = Modifier.weight(1f)
        ) {
          onNavigate(ERPScreen.Accounts)
        }
        QuickActionButton(
          title = if (isBengali) "হাজিরা" else "Attendance",
          icon = Icons.Default.FactCheck,
          color = Color(0xFF6366F1),
          modifier = Modifier.weight(1f)
        ) {
          onNavigate(ERPScreen.Attendance)
        }
      }
    }

    // 3. Core Financial Stats (Income, Expense, Balance)
    item {
      Text(
        text = if (isBengali) "আর্থিক সারসংক্ষেপ (Financial Overview)" else "Financial Overview",
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp
      )
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        StatBox(
          title = if (isBengali) "মোট আয় (Total Income)" else "Total Income",
          value = ERPLocale.formatTaka(totalIncomeAll),
          subtitle = if (isBengali) "ফি ও অনুদান" else "Fees & Grants",
          icon = Icons.Default.TrendingUp,
          color = EmeraldPrimary,
          modifier = Modifier.weight(1f)
        )
        StatBox(
          title = if (isBengali) "মোট ব্যয় (Expenses)" else "Total Expenses",
          value = ERPLocale.formatTaka(totalExpenses),
          subtitle = if (isBengali) "বেতন ও পরিচালনা" else "Salaries & Ops",
          icon = Icons.Default.TrendingDown,
          color = CrimsonError,
          modifier = Modifier.weight(1f)
        )
        StatBox(
          title = if (isBengali) "বর্তমান তহবিল (Balance)" else "Net Balance",
          value = ERPLocale.formatTaka(netBalance),
          subtitle = if (isBengali) "মাদ্রাসার স্থিতি" else "Current Cash",
          icon = Icons.Default.AccountBalanceWallet,
          color = if (netBalance >= 0) NavyPrimary else CrimsonError,
          modifier = Modifier.weight(1f)
        )
      }
    }

    // 4. Institutional Operations Metrics
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        StatBox(
          title = if (isBengali) "আজকের ফি আদায়" else "Today's Fee",
          value = ERPLocale.formatTaka(todayCollection),
          subtitle = if (isBengali) "দৈনিক কালেকশন" else "Daily Total",
          icon = Icons.Default.Payments,
          color = EmeraldPrimary,
          modifier = Modifier.weight(1f)
        )
        StatBox(
          title = if (isBengali) "মোট ছাত্রী" else "Active Students",
          value = "${students.size}",
          subtitle = if (isBengali) "নিয়মিত ছাত্রী" else "Enrolled",
          icon = Icons.Default.Groups,
          color = NavyPrimary,
          modifier = Modifier.weight(1f)
        )
        StatBox(
          title = if (isBengali) "শিক্ষক ও স্টাফ" else "Teachers & Staff",
          value = "${employees.size}",
          subtitle = if (isBengali) "কর্মরত সদস্য" else "Total Employees",
          icon = Icons.Default.Badge,
          color = GoldAccent,
          modifier = Modifier.weight(1f)
        )
      }
    }

    // 5. Class-wise Student Distribution Visual Chart
    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (isBengali) "শ্রেণিভিত্তিক ছাত্রী পরিসংখ্যান" else "Class-wise Student Distribution",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
            Text(
              text = "${students.size} ${if (isBengali) "জন" else "Total"}",
              color = EmeraldPrimary,
              fontWeight = FontWeight.SemiBold,
              fontSize = 12.sp
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          classes.forEach { c ->
            val countInClass = students.count { it.classId == c.id }
            val fraction = if (students.isNotEmpty()) countInClass.toFloat() / students.size else 0f

            Column(modifier = Modifier.padding(vertical = 4.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(text = if (isBengali) c.nameBn else c.nameEn, fontSize = 12.sp)
                Text(text = "$countInClass ${if (isBengali) "জন" else "Students"}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
              }
              Spacer(modifier = Modifier.height(2.dp))
              LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(8.dp)
                  .clip(RoundedCornerShape(4.dp)),
                color = EmeraldPrimary,
                trackColor = Slate100,
              )
            }
          }
        }
      }
    }

    // 6. Recent Fee Collections & Receipts
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (isBengali) "সাম্প্রতিক ফি কালেকশন রশিদ" else "Recent Fee Receipts",
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp
        )
        TextButton(onClick = { onNavigate(ERPScreen.FeeCollection) }) {
          Text(if (isBengali) "সব দেখুন" else "View All", color = EmeraldPrimary)
        }
      }

      if (payments.isEmpty()) {
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = Slate100),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = if (isBengali) "এখনো কোনো ফি আদায়ের রেকর্ড নেই" else "No payment records found yet",
            color = Slate600,
            fontSize = 13.sp,
            modifier = Modifier.padding(16.dp)
          )
        }
      } else {
        payments.take(4).forEach { payment ->
          Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp)
              .clickable { onShowReceipt(payment) },
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
                  shape = CircleShape,
                  color = EmeraldContainer,
                  modifier = Modifier.size(36.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Receipt, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(20.dp))
                  }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(text = payment.studentName, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                  Text(text = "${payment.receiptNumber} • ${payment.date}", fontSize = 11.sp, color = Slate600)
                }
              }

              Column(horizontalAlignment = Alignment.End) {
                Text(text = ERPLocale.formatTaka(payment.paidAmount), fontWeight = FontWeight.Bold, color = EmeraldDark, fontSize = 14.sp)
                Text(text = payment.paymentMethod, fontSize = 10.sp, color = Slate600)
              }
            }
          }
        }
      }
    }

    // 7. Important Notices Preview
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (isBengali) "জরুরি নোটিশ বোর্ড" else "Important Notices",
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp
        )
        TextButton(onClick = { onNavigate(ERPScreen.Notices) }) {
          Text(if (isBengali) "সকল নোটিশ" else "All Notices", color = EmeraldPrimary)
        }
      }

      notices.take(2).forEach { notice ->
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = if (notice.isUrgent) Color(0xFFFFFBEB) else MaterialTheme.colorScheme.surface),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                if (notice.isUrgent) {
                  Icon(Icons.Default.Warning, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                  text = if (isBengali) notice.titleBn else notice.titleEn,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = if (notice.isUrgent) GoldDark else MaterialTheme.colorScheme.onSurface
                )
              }
              Text(text = notice.publishDate, fontSize = 10.sp, color = Slate600)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (isBengali) notice.contentBn else notice.contentEn,
              fontSize = 12.sp,
              color = Slate700,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }
    }
  }
}

@Composable
fun QuickActionButton(
  title: String,
  icon: ImageVector,
  color: Color,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = color,
    modifier = modifier
      .height(64.dp)
      .clickable { onClick() }
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(6.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(imageVector = icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(20.dp))
      Spacer(modifier = Modifier.height(3.dp))
      Text(
        text = title,
        color = Color.White,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
fun StatBox(
  title: String,
  value: String,
  subtitle: String,
  icon: ImageVector,
  color: Color,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    modifier = modifier,
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = title, fontSize = 10.sp, color = Slate600, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
      Spacer(modifier = Modifier.height(2.dp))
      Text(text = subtitle, fontSize = 9.sp, color = Slate600, maxLines = 1)
    }
  }
}
