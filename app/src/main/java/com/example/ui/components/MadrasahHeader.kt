package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UserRole
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MadrasahHeader(
  isBengali: Boolean,
  currentRole: UserRole,
  currentSession: String,
  allSessions: List<String>,
  onToggleLanguage: () -> Unit,
  onSwitchRole: (UserRole) -> Unit,
  onSwitchSession: (String) -> Unit,
  onOpenDrawer: () -> Unit,
  onOpenGuardianLogin: () -> Unit = {},
  onOpenTeacherLogin: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  var showRoleMenu by remember { mutableStateOf(false) }
  var showSessionMenu by remember { mutableStateOf(false) }

  Surface(
    modifier = modifier.fillMaxWidth(),
    color = MaterialTheme.colorScheme.primary,
    tonalElevation = 4.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Menu drawer button, Madrasah Official Logo and Title
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          IconButton(
            onClick = onOpenDrawer,
            modifier = Modifier.testTag("menu_drawer_button")
          ) {
            Icon(
              imageVector = Icons.Default.Menu,
              contentDescription = "Open Navigation Menu",
              tint = Color.White
            )
          }

          // Official Logo Image
          Surface(
            shape = CircleShape,
            color = Color.White,
            modifier = Modifier.size(38.dp)
          ) {
            Image(
              painter = painterResource(id = R.drawable.ic_jngm_logo),
              contentDescription = "JNGM Official Logo",
              contentScale = ContentScale.Fit,
              modifier = Modifier.fillMaxSize().padding(2.dp)
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          Column {
            Text(
              text = if (isBengali) "জাবালে নূর গার্লস মাদ্রাসা" else "Jabale Noor Girls Madrasah",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = if (isBengali) "রামগড়, খাগড়াছড়ি • J.N.G.M (ESTD: 2023)" else "Ramgarh, Khagrachhari • ESTD: 2023",
              color = EmeraldLight,
              fontSize = 10.sp,
              maxLines = 1
            )
          }
        }

        // Action Buttons: Session chip, Guardian button, Language Toggle, Role Selector
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          // Guardian Portal Login button
          IconButton(
            onClick = onOpenGuardianLogin,
            modifier = Modifier.size(34.dp).testTag("guardian_login_nav_button")
          ) {
            Icon(
              imageVector = Icons.Default.FamilyRestroom,
              contentDescription = "Guardian Portal",
              tint = GoldAccent,
              modifier = Modifier.size(20.dp)
            )
          }

          // Teacher Login button
          IconButton(
            onClick = onOpenTeacherLogin,
            modifier = Modifier.size(34.dp).testTag("teacher_login_nav_button")
          ) {
            Icon(
              imageVector = Icons.Default.LockPerson,
              contentDescription = "Teacher Email Login",
              tint = Color.White,
              modifier = Modifier.size(19.dp)
            )
          }

          // Session selector chip
          Box {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = NavyLight,
              modifier = Modifier
                .clickable { showSessionMenu = true }
                .testTag("session_selector_chip")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
              ) {
                Text(
                  text = currentSession,
                  color = Color.White,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            DropdownMenu(
              expanded = showSessionMenu,
              onDismissRequest = { showSessionMenu = false }
            ) {
              allSessions.forEach { session ->
                DropdownMenuItem(
                  text = { Text("সেশন $session") },
                  onClick = {
                    onSwitchSession(session)
                    showSessionMenu = false
                  }
                )
              }
            }
          }

          // Language Toggle
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isBengali) EmeraldPrimary else GoldAccent,
            modifier = Modifier
              .clickable { onToggleLanguage() }
              .testTag("language_toggle_button")
          ) {
            Text(
              text = if (isBengali) "বাং" else "EN",
              color = Color.White,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
      }
    }
  }
}
