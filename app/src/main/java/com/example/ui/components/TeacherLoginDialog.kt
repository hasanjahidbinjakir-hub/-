package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.EmployeeEntity
import com.example.ui.theme.*

@Composable
fun TeacherLoginDialog(
  isBengali: Boolean,
  employees: List<EmployeeEntity>,
  onDismiss: () -> Unit,
  onLogin: (email: String, pass: String, (Boolean, String) -> Unit) -> Unit
) {
  var emailInput by remember { mutableStateOf("") }
  var passwordInput by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var isLoading by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Surface(
          shape = CircleShape,
          color = NavyPrimary,
          modifier = Modifier.size(36.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Badge, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(20.dp))
          }
        }
        Text(
          text = if (isBengali) "শিক্ষক ইমেইল লগইন" else "Teacher Email Login",
          fontWeight = FontWeight.Bold,
          fontSize = 17.sp
        )
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = if (isBengali)
            "এডমিন কর্তৃক নির্ধারিত ইমেইল ও পাসওয়ার্ড দিয়ে লগইন করে নম্বর এন্ট্রি ও বাড়ির কাজ প্রদান করুন।"
          else
            "Log in using the email assigned by Admin to submit student marks and assign homework.",
          fontSize = 12.sp,
          color = Slate500
        )

        errorMessage?.let { err ->
          Surface(
            color = CrimsonContainer,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = err,
              color = CrimsonDark,
              fontSize = 12.sp,
              modifier = Modifier.padding(8.dp)
            )
          }
        }

        OutlinedTextField(
          value = emailInput,
          onValueChange = {
            emailInput = it
            errorMessage = null
          },
          label = { Text(if (isBengali) "নিবন্ধিত ইমেইল *" else "Registered Email *") },
          placeholder = { Text("fatema@jabalenoor.edu.bd") },
          leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = NavyPrimary) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("teacher_login_email_input")
        )

        OutlinedTextField(
          value = passwordInput,
          onValueChange = {
            passwordInput = it
            errorMessage = null
          },
          label = { Text(if (isBengali) "পাসওয়ার্ড / পিন *" else "Password / PIN *") },
          placeholder = { Text("যেমন: 123456") },
          leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = NavyPrimary) },
          trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
              Icon(
                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = null
              )
            }
          },
          visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("teacher_login_password_input")
        )

        // Quick demo email chips
        HorizontalDivider(color = Slate200)
        Text(
          text = if (isBengali) "ডেমো শিক্ষক ইমেইল:" else "Sample teacher email:",
          fontSize = 11.sp,
          color = Slate500
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          employees.take(2).forEach { emp ->
            SuggestionChip(
              onClick = {
                emailInput = emp.email
                passwordInput = emp.password
              },
              label = { Text(emp.email.take(16) + "...", fontSize = 10.sp) }
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (emailInput.isBlank() || passwordInput.isBlank()) {
            errorMessage = if (isBengali) "ইমেইল ও পাসওয়ার্ড লিখুন" else "Enter email and password"
            return@Button
          }
          isLoading = true
          onLogin(emailInput, passwordInput) { success, msg ->
            isLoading = false
            if (!success) {
              errorMessage = msg
            } else {
              onDismiss()
            }
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
        modifier = Modifier.testTag("teacher_login_dialog_submit")
      ) {
        Text(if (isBengali) "লগ ইন" else "Log In")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(if (isBengali) "বাতিল" else "Cancel")
      }
    }
  )
}
