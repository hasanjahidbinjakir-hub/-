package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.AcademicSessionEntity
import com.example.data.entity.InstitutionSettingsEntity
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.MadrasahViewModel

@Composable
fun SettingsScreen(
  viewModel: MadrasahViewModel,
  modifier: Modifier = Modifier
) {
  val isBengali by viewModel.isBengali.collectAsState()
  val settings by viewModel.settings.collectAsState()
  val sessions by viewModel.allSessions.collectAsState()
  val classes by viewModel.classes.collectAsState()
  val currentRole by viewModel.currentRole.collectAsState()
  val currentSession by viewModel.currentSession.collectAsState()
  val context = LocalContext.current

  var showAddSessionDialog by remember { mutableStateOf(false) }
  var showAddClassDialog by remember { mutableStateOf(false) }
  var showPromotionDialog by remember { mutableStateOf(false) }

  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(16.dp)
  ) {
    // Header
    Card(
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = NavyPrimary),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(Icons.Default.Settings, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(36.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = if (isBengali) "প্রতিষ্ঠান সেটিংস ও সেশন পরিচালনা" else "Institutional Settings & Sessions",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )
          Text(
            text = if (isBengali) "জাবালে নূর গার্লস মাদ্রাসা, রামগড়" else "Jabale Noor Girls Madrasah, Ramgarh",
            color = Slate200,
            fontSize = 11.sp
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 1. Academic Sessions Management
    Text(
      text = if (isBengali) "শিক্ষা সেশন পরিচালনা (Academic Sessions)" else "Academic Sessions Management",
      fontWeight = FontWeight.Bold,
      fontSize = 15.sp,
      color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(8.dp))

    Card(
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      modifier = Modifier.fillMaxWidth(),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (isBengali) "বিদ্যমান সেশনসমূহ:" else "Configured Sessions:",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
          )
          Button(
            onClick = { showAddSessionDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(if (isBengali) "নতুন সেশন" else "Add Session", fontSize = 11.sp)
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        sessions.forEach { s ->
          val isCurrent = s.name == currentSession
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isCurrent) EmeraldContainer else Slate100,
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 3.dp)
              .clickable {
                viewModel.switchActiveSession(s.id, s.name)
                Toast
                  .makeText(
                    context,
                    if (isBengali) "সক্রিয় সেশন পরিবর্তন হয়েছে: ${s.name}" else "Switched to Session ${s.name}",
                    Toast.LENGTH_SHORT
                  )
                  .show()
              }
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = if (isCurrent) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                  contentDescription = null,
                  tint = if (isCurrent) EmeraldDark else Slate600,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "সেশন ${s.name}",
                  fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                  color = if (isCurrent) EmeraldOnContainer else Slate900,
                  fontSize = 13.sp
                )
              }
              Text(
                text = if (isCurrent) (if (isBengali) "সক্রিয়" else "Active") else (if (isBengali) "ট্যাপ করে চালু করুন" else "Tap to switch"),
                fontSize = 11.sp,
                color = if (isCurrent) EmeraldDark else Slate600
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 2. Class Promotion & Archival Safeguard
    Card(
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = Slate100),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isBengali) "তথ্য সুরক্ষা ও প্রচার (Archival Policy)" else "Data Preservation Policy",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = NavyPrimary
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = if (isBengali)
            "সেশন পরিবর্তন করলে পূর্ববর্তী কোনো আর্থিক, হাজিরা বা পরীক্ষার তথ্য মুছে ফেলা হয় না। প্রতিটি রেকর্ড সেশন কোড দ্বারা সুরক্ষিত।"
          else
            "Switching sessions preserves all previous financial, attendance, and exam history intact. Never overwritten.",
          fontSize = 11.sp,
          color = Slate700
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 3. User Roles & PIN Quick Credentials
    Text(
      text = if (isBengali) "ব্যবহারকারী ভূমিকা ও পিন কোড (User Credentials)" else "User Roles & Quick Access PINs",
      fontWeight = FontWeight.Bold,
      fontSize = 15.sp,
      color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(8.dp))

    Card(
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      modifier = Modifier.fillMaxWidth(),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        val roleCredentials = listOf(
          Triple("Super Administrator (প্রধান প্রশাসক)", "1111", UserRole.SUPER_ADMIN),
          Triple("Principal (মুহতামিম / অধ্যক্ষ)", "2222", UserRole.PRINCIPAL),
          Triple("Accountant (হিসাবরক্ষক)", "3333", UserRole.ACCOUNTANT),
          Triple("Teacher (মুদাররিস / শিক্ষিকা)", "4444", UserRole.TEACHER),
          Triple("Attendance Operator (হাজিরা অপারেটর)", "5555", UserRole.ATTENDANCE_OPERATOR)
        )

        roleCredentials.forEach { (title, pin, role) ->
          val isCurrent = currentRole == role
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(text = title, fontSize = 12.sp, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium)
              Text(text = "লগইন পিন: $pin", fontSize = 10.sp, color = Slate600)
            }
            if (isCurrent) {
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = EmeraldContainer
              ) {
                Text(
                  text = if (isBengali) "লগইনকৃত" else "Active",
                  fontSize = 10.sp,
                  color = EmeraldOnContainer,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            } else {
              OutlinedButton(
                onClick = {
                  viewModel.switchRole(role)
                  Toast.makeText(context, "Switched role to ${role.name}", Toast.LENGTH_SHORT).show()
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
              ) {
                Text(if (isBengali) "লগইন" else "Switch", fontSize = 10.sp)
              }
            }
          }
          HorizontalDivider(color = Slate100)
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 4. Free Tier & Cloud Architecture Notice
    Card(
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = NavyContainer),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.CloudQueue, contentDescription = null, tint = NavyPrimary)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isBengali) "ক্লাউড ও স্পার্ক প্ল্যান সামঞ্জস্যতা" else "Cloud & Spark Plan Compatibility",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = NavyPrimary
          )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = if (isBengali)
            "এই অ্যাপ্লিকেশনটি ১০০% ফ্রি-টিয়ার সামঞ্জস্যপূর্ণ। কোনো পেইড API বা সাবস্ক্রিপশন প্রয়োজন নেই। লোকাল রুম ডেটাবেসে অফলাইনে সম্পূর্ণ কার্যকর এবং ক্লাউড ফায়ারস্টোর স্পার্ক প্ল্যানে মাইগ্রেশন উপযোগী।"
          else
            "This application operates 100% on free-tier infrastructure. No credit card or paid subscription required. Fully offline-capable on Room SQLite and exportable.",
          fontSize = 11.sp,
          color = NavyOnContainer
        )
      }
    }

    Spacer(modifier = Modifier.height(24.dp))
  }

  // Add Session Dialog
  if (showAddSessionDialog) {
    var sessionName by remember { mutableStateOf("2028") }
    Dialog(onDismissRequest = { showAddSessionDialog = false }) {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(text = if (isBengali) "নতুন শিক্ষা সেশন যুক্ত করুন" else "Add New Academic Session", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyPrimary)
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = sessionName,
            onValueChange = { sessionName = it },
            label = { Text(if (isBengali) "সেশনের সাল (যেমন ২০২৮)" else "Session Year (e.g. 2028)") },
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(14.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { showAddSessionDialog = false }) { Text(if (isBengali) "বাতিল" else "Cancel") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = {
                if (sessionName.isBlank()) return@Button
                viewModel.addSession(sessionName, "01-01-$sessionName", "31-12-$sessionName")
                Toast.makeText(context, if (isBengali) "সেশন $sessionName তৈরি হয়েছে" else "Session added", Toast.LENGTH_SHORT).show()
                showAddSessionDialog = false
              },
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
              Text(if (isBengali) "যোগ করুন" else "Add")
            }
          }
        }
      }
    }
  }
}
