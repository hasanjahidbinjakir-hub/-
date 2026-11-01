package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.entity.NoticeEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MadrasahViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NoticesScreen(
  viewModel: MadrasahViewModel,
  modifier: Modifier = Modifier
) {
  val isBengali by viewModel.isBengali.collectAsState()
  val notices by viewModel.notices.collectAsState()
  val context = LocalContext.current

  var showCreateDialog by remember { mutableStateOf(false) }
  var selectedCategory by remember { mutableStateOf<String?>(null) }

  val categories = listOf("All", "General", "Academic", "Holiday", "Exam", "Fees")

  val filteredNotices = remember(notices, selectedCategory) {
    if (selectedCategory == null || selectedCategory == "All") notices else notices.filter { it.category == selectedCategory }
  }

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
          text = if (isBengali) "নোটিশ ও ঘোষণা বোর্ড" else "Notice Board",
          fontWeight = FontWeight.Bold,
          fontSize = 17.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = if (isBengali) "মাদ্রাসার জরুরি বিজ্ঞপ্তি ও বার্তা" else "Announcements & Circulars",
          fontSize = 12.sp,
          color = Slate600
        )
      }

      Button(
        onClick = { showCreateDialog = true },
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
        modifier = Modifier.testTag("publish_notice_button")
      ) {
        Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(if (isBengali) "নতুন নোটিশ" else "Post Notice", fontSize = 12.sp)
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Categories Row
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
      items(categories) { cat ->
        val isSel = (cat == "All" && selectedCategory == null) || (selectedCategory == cat)
        FilterChip(
          selected = isSel,
          onClick = { selectedCategory = if (cat == "All") null else cat },
          label = { Text(cat) }
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    if (filteredNotices.isEmpty()) {
      Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
        Text(text = if (isBengali) "কোনো নোটিশ পাওয়া যায়নি" else "No notices available", color = Slate600)
      }
    } else {
      LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
        items(filteredNotices) { notice ->
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = if (notice.isUrgent) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                  if (notice.isUrgent) {
                    Icon(Icons.Default.PriorityHigh, contentDescription = "Urgent", tint = CrimsonError, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                  }
                  Text(
                    text = if (isBengali) notice.titleBn else notice.titleEn,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (notice.isUrgent) GoldDark else NavyPrimary
                  )
                }
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = EmeraldContainer
                ) {
                  Text(
                    text = notice.category,
                    color = EmeraldOnContainer,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = if (isBengali) notice.contentBn else notice.contentEn,
                fontSize = 13.sp,
                color = Slate800,
                lineHeight = 18.sp
              )

              Spacer(modifier = Modifier.height(10.dp))
              HorizontalDivider(color = Slate200)
              Spacer(modifier = Modifier.height(6.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(text = "প্রকাশের তারিখ: ${notice.publishDate}", fontSize = 11.sp, color = Slate600)

                Row {
                  IconButton(
                    onClick = {
                      val shareText = "${notice.titleBn}\n${notice.publishDate}\n\n${notice.contentBn}\n\n- জাবালে নূর গার্লস মাদ্রাসা, রামগড়"
                      val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                      }
                      context.startActivity(Intent.createChooser(sendIntent, "Share Notice"))
                    }
                  ) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                  }
                  IconButton(
                    onClick = { viewModel.deleteNotice(notice) }
                  ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = CrimsonError, modifier = Modifier.size(18.dp))
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // Create Notice Dialog
  if (showCreateDialog) {
    var titleBn by remember { mutableStateOf("") }
    var titleEn by remember { mutableStateOf("") }
    var contentBn by remember { mutableStateOf("") }
    var contentEn by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }
    var isUrgent by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { showCreateDialog = false }) {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(text = if (isBengali) "নতুন নোটিশ প্রকাশ করুন" else "Publish New Notice", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyPrimary)
          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = titleBn,
            onValueChange = { titleBn = it },
            label = { Text(if (isBengali) "নোটিশের শিরোনাম (বাংলা) *" else "Title (Bengali) *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedTextField(
            value = titleEn,
            onValueChange = { titleEn = it },
            label = { Text(if (isBengali) "নোটিশের শিরোনাম (ইংরেজি)" else "Title (English)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedTextField(
            value = contentBn,
            onValueChange = { contentBn = it },
            label = { Text(if (isBengali) "বিস্তারিত বক্তব্য (বাংলা) *" else "Content (Bengali) *") },
            modifier = Modifier
              .fillMaxWidth()
              .height(100.dp),
            maxLines = 4
          )
          Spacer(modifier = Modifier.height(6.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(text = if (isBengali) "জরুরি নোটিশ হিসেবে চিহ্নিত করুন" else "Mark as Urgent", fontSize = 12.sp)
            Switch(checked = isUrgent, onCheckedChange = { isUrgent = it })
          }

          Spacer(modifier = Modifier.height(14.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { showCreateDialog = false }) { Text(if (isBengali) "বাতিল" else "Cancel") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = {
                if (titleBn.isBlank() || contentBn.isBlank()) return@Button
                val notice = NoticeEntity(
                  titleBn = titleBn,
                  titleEn = titleEn.ifEmpty { titleBn },
                  contentBn = contentBn,
                  contentEn = contentEn.ifEmpty { contentBn },
                  category = category,
                  publishDate = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date()),
                  isUrgent = isUrgent
                )
                viewModel.postNotice(notice) {
                  Toast.makeText(context, if (isBengali) "নোটিশ প্রকাশিত হয়েছে!" else "Notice published!", Toast.LENGTH_SHORT).show()
                  showCreateDialog = false
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
              Text(if (isBengali) "প্রকাশ করুন" else "Publish")
            }
          }
        }
      }
    }
  }
}
