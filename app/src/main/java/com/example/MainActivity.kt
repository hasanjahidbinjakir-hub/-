package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.MadrasahHeader
import com.example.ui.components.NavigationDrawerContent
import com.example.ui.components.ReceiptDialog
import com.example.ui.screens.*
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.JabaleNoorTheme
import com.example.ui.theme.NavyPrimary
import com.example.ui.viewmodel.ERPScreen
import com.example.ui.viewmodel.MadrasahViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      JabaleNoorTheme {
        val viewModel: MadrasahViewModel = viewModel()
        val currentScreen by viewModel.currentScreen.collectAsState()
        val isBengali by viewModel.isBengali.collectAsState()
        val currentRole by viewModel.currentRole.collectAsState()
        val currentSession by viewModel.currentSession.collectAsState()
        val allSessions by viewModel.allSessions.collectAsState()
        val lastReceipt by viewModel.lastPaymentReceipt.collectAsState()
        val employees by viewModel.employees.collectAsState()

        var showTeacherLoginDialog by remember { mutableStateOf(false) }

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val coroutineScope = rememberCoroutineScope()

        // Handle physical/gesture back button: return to Dashboard if on a sub-screen
        BackHandler(enabled = currentScreen != ERPScreen.Dashboard) {
          viewModel.navigateTo(ERPScreen.Dashboard)
        }

        ModalNavigationDrawer(
          drawerState = drawerState,
          drawerContent = {
            NavigationDrawerContent(
              currentScreen = currentScreen,
              currentRole = currentRole,
              isBengali = isBengali,
              onSelectScreen = { screen -> viewModel.navigateTo(screen) },
              onCloseDrawer = {
                coroutineScope.launch { drawerState.close() }
              }
            )
          }
        ) {
          Scaffold(
            topBar = {
              MadrasahHeader(
                isBengali = isBengali,
                currentRole = currentRole,
                currentSession = currentSession,
                allSessions = allSessions.map { it.name },
                onToggleLanguage = { viewModel.toggleLanguage() },
                onSwitchRole = { role -> viewModel.switchRole(role) },
                onSwitchSession = { session -> viewModel.switchSession(session) },
                onOpenDrawer = {
                  coroutineScope.launch { drawerState.open() }
                },
                onOpenGuardianLogin = { viewModel.navigateTo(ERPScreen.GuardianPortal) },
                onOpenTeacherLogin = { showTeacherLoginDialog = true }
              )
            },
            bottomBar = {
              NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                windowInsets = WindowInsets.navigationBars
              ) {
                val primaryNavItems = listOf(
                  Triple(ERPScreen.Dashboard, Icons.Default.Dashboard, if (isBengali) "ড্যাশবোর্ড" else "Home"),
                  Triple(ERPScreen.Students, Icons.Default.People, if (isBengali) "ছাত্রী" else "Students"),
                  Triple(ERPScreen.FeeCollection, Icons.Default.Receipt, if (isBengali) "ফি কালেকশন" else "Fees"),
                  Triple(ERPScreen.Accounts, Icons.Default.AccountBalance, if (isBengali) "হিসাব" else "Accounts"),
                  Triple(ERPScreen.Notices, Icons.Default.Campaign, if (isBengali) "নোটিশ" else "Notices")
                )

                primaryNavItems.forEach { (screen, icon, label) ->
                  val isSelected = currentScreen == screen
                  NavigationBarItem(
                    selected = isSelected,
                    onClick = { viewModel.navigateTo(screen) },
                    icon = {
                      Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    },
                    label = {
                      Text(
                        text = label,
                        fontSize = 10.sp,
                        color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    },
                    modifier = Modifier.testTag("bottom_nav_${screen.titleEn.lowercase()}"),
                    colors = NavigationBarItemDefaults.colors(
                      indicatorColor = EmeraldContainer
                    )
                  )
                }
              }
            }
          ) { innerPadding ->
            Box(
              modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
            ) {
              when (currentScreen) {
                ERPScreen.Dashboard -> DashboardScreen(
                  viewModel = viewModel,
                  onNavigate = { screen -> viewModel.navigateTo(screen) },
                  onShowReceipt = { payment -> viewModel.showReceipt(payment) }
                )
                ERPScreen.Students -> StudentsScreen(
                  viewModel = viewModel,
                  onNavigate = { screen -> viewModel.navigateTo(screen) }
                )
                ERPScreen.Admissions -> AdmissionScreen(
                  viewModel = viewModel,
                  onNavigate = { screen -> viewModel.navigateTo(screen) },
                  onShowReceipt = { payment -> viewModel.showReceipt(payment) }
                )
                ERPScreen.FeeCollection -> FeeCollectionScreen(
                  viewModel = viewModel,
                  onShowReceipt = { payment -> viewModel.showReceipt(payment) }
                )
                ERPScreen.Accounts -> AccountsScreen(
                  viewModel = viewModel
                )
                ERPScreen.Teachers -> TeachersScreen(
                  viewModel = viewModel
                )
                ERPScreen.Attendance -> AttendanceScreen(
                  viewModel = viewModel
                )
                ERPScreen.Examinations -> ExamsScreen(
                  viewModel = viewModel
                )
                ERPScreen.Homework -> HomeworkScreen(
                  viewModel = viewModel
                )
                ERPScreen.GuardianPortal -> GuardianPortalScreen(
                  viewModel = viewModel
                )
                ERPScreen.Notices -> NoticesScreen(
                  viewModel = viewModel
                )
                ERPScreen.Settings -> SettingsScreen(
                  viewModel = viewModel
                )
              }
            }
          }
        }

        // Show Teacher Email Login Dialog if triggered
        if (showTeacherLoginDialog) {
          com.example.ui.components.TeacherLoginDialog(
            isBengali = isBengali,
            employees = employees,
            onDismiss = { showTeacherLoginDialog = false },
            onLogin = { email, pass, onResult ->
              viewModel.loginTeacherWithEmail(email, pass) { success, msg ->
                onResult(success, msg)
                if (success) {
                  viewModel.switchRole(com.example.data.model.UserRole.TEACHER)
                  viewModel.navigateTo(ERPScreen.Examinations)
                }
              }
            }
          )
        }

        // Show Money Receipt modal if triggered
        lastReceipt?.let { (payment, details) ->
          ReceiptDialog(
            payment = payment,
            details = details,
            isBengali = isBengali,
            onDismiss = { viewModel.dismissReceipt() }
          )
        }
      }
    }
  }
}
