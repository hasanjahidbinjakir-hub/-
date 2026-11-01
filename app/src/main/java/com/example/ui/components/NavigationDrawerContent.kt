package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.ERPScreen

@Composable
fun NavigationDrawerContent(
  currentScreen: ERPScreen,
  currentRole: UserRole,
  isBengali: Boolean,
  onSelectScreen: (ERPScreen) -> Unit,
  onCloseDrawer: () -> Unit
) {
  val scrollState = rememberScrollState()

  ModalDrawerSheet(
    modifier = Modifier.width(300.dp),
    drawerContainerColor = MaterialTheme.colorScheme.surface
  ) {
    Column(
      modifier = Modifier
        .fillMaxHeight()
        .verticalScroll(scrollState)
    ) {
      // Header with Official Logo
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(NavyPrimary)
          .padding(18.dp)
      ) {
        Column {
          Surface(
            shape = CircleShape,
            color = Color.White,
            modifier = Modifier.size(54.dp)
          ) {
            Image(
              painter = painterResource(id = R.drawable.ic_jngm_logo),
              contentDescription = "Madrasah Crest",
              contentScale = ContentScale.Fit,
              modifier = Modifier.fillMaxSize().padding(3.dp)
            )
          }
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = if (isBengali) "জাবালে নূর গার্লস মাদ্রাসা" else "Jabale Noor Girls Madrasah",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
          )
          Text(
            text = if (isBengali) "“এসো, আলোকিত হও, আলোকিত কর বিশ্ব”" else "ESTD: 2023 • Ramgarh",
            color = GoldAccent,
            fontSize = 11.sp
          )
          Text(
            text = "রামগড়, খাগড়াছড়ি • J.N.G.M",
            color = Slate200,
            fontSize = 11.sp
          )
          Spacer(modifier = Modifier.height(8.dp))
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = EmeraldPrimary
          ) {
            Text(
              text = if (isBengali) "ভূমিকা: ${currentRole.titleBn}" else "Role: ${currentRole.titleEn}",
              color = Color.White,
              fontSize = 11.sp,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      val menuItems = listOf(
        Pair(ERPScreen.Dashboard, Icons.Default.Dashboard),
        Pair(ERPScreen.Students, Icons.Default.People),
        Pair(ERPScreen.Admissions, Icons.Default.PersonAdd),
        Pair(ERPScreen.FeeCollection, Icons.Default.Receipt),
        Pair(ERPScreen.Accounts, Icons.Default.AccountBalance),
        Pair(ERPScreen.Teachers, Icons.Default.Badge),
        Pair(ERPScreen.Attendance, Icons.Default.FactCheck),
        Pair(ERPScreen.Examinations, Icons.Default.School),
        Pair(ERPScreen.Homework, Icons.Default.MenuBook),
        Pair(ERPScreen.Notices, Icons.Default.Campaign),
        Pair(ERPScreen.GuardianPortal, Icons.Default.FamilyRestroom),
        Pair(ERPScreen.Settings, Icons.Default.Settings)
      )

      menuItems.forEach { (screen, icon) ->
        val isSelected = currentScreen == screen
        NavigationDrawerItem(
          icon = {
            Icon(
              imageVector = icon,
              contentDescription = null,
              tint = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
          },
          label = {
            Text(
              text = if (isBengali) screen.titleBn else screen.titleEn,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
            )
          },
          selected = isSelected,
          onClick = {
            onSelectScreen(screen)
            onCloseDrawer()
          },
          modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .testTag("nav_item_${screen.titleEn.lowercase().replace(" ", "_")}"),
          shape = RoundedCornerShape(12.dp),
          colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = EmeraldContainer
          )
        )
      }

      Spacer(modifier = Modifier.height(20.dp))
      HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "ESTD: 2023 • রামগড়, খাগড়াছড়ি",
        fontSize = 11.sp,
        color = Slate600,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
      )
    }
  }
}
