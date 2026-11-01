package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.entity.FeePaymentDetailEntity
import com.example.data.entity.FeePaymentEntity
import com.example.ui.theme.*
import com.example.util.ERPLocale
import com.example.util.PrintHelper

@Composable
fun ReceiptDialog(
  payment: FeePaymentEntity,
  details: List<FeePaymentDetailEntity>,
  isBengali: Boolean,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surface,
      modifier = Modifier
        .fillMaxWidth(0.95f)
        .fillMaxHeight(0.94f)
        .testTag("receipt_dialog_container")
    ) {
      Column(
        modifier = Modifier.fillMaxSize()
      ) {
        // Modal Header with Close and Share/Print
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(NavyPrimary)
            .padding(horizontal = 16.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = CircleShape,
              color = Color.White,
              modifier = Modifier.size(28.dp)
            ) {
              Image(
                painter = painterResource(id = R.drawable.ic_jngm_logo),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().padding(2.dp)
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (isBengali) "টাকা জমার অফিশিয়াল রশিদ" else "Official Money Receipt",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp
            )
          }

          Row {
            IconButton(
              onClick = {
                shareReceiptAsText(context, payment, details)
              },
              modifier = Modifier.testTag("share_receipt_button")
            ) {
              Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
            }
            IconButton(
              onClick = onDismiss,
              modifier = Modifier.testTag("close_receipt_button")
            ) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
          }
        }

        // Receipt Scrollable Body
        Column(
          modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
        ) {
          // Printable Voucher Paper Style
          Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFCFF)),
            modifier = Modifier
              .fillMaxWidth()
              .border(1.dp, Slate200, RoundedCornerShape(8.dp))
              .padding(12.dp)
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.fillMaxWidth()
            ) {
              // Madrasah Logo & Name
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Surface(
                  shape = CircleShape,
                  color = Color.White,
                  modifier = Modifier.size(42.dp)
                ) {
                  Image(
                    painter = painterResource(id = R.drawable.ic_jngm_logo),
                    contentDescription = "Madrasah Crest",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(2.dp)
                  )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(
                    text = "“এসো, আলোকিত হও, আলোকিত কর বিশ্ব”",
                    fontSize = 10.sp,
                    color = GoldAccent,
                    fontWeight = FontWeight.SemiBold
                  )
                  Text(
                    text = "জাবালে নূর গার্লস মাদ্রাসা",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NavyPrimary,
                    textAlign = TextAlign.Center
                  )
                  Text(
                    text = "JABALE NOOR GIRLS MADRASAH (ESTD: 2023)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate700
                  )
                  Text(
                    text = "রামগড়, খাগড়াছড়ি, বাংলাদেশ • ফোন: +880 1812-345678",
                    fontSize = 10.sp,
                    color = Slate600
                  )
                }
              }

              Spacer(modifier = Modifier.height(6.dp))

              Surface(
                shape = RoundedCornerShape(4.dp),
                color = EmeraldPrimary
              ) {
                Text(
                  text = "মানি রসিদ (MONEY RECEIPT)",
                  color = Color.White,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                )
              }

              Spacer(modifier = Modifier.height(10.dp))
              HorizontalDivider(color = Slate200)
              Spacer(modifier = Modifier.height(8.dp))

              // Receipt Meta
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column {
                  Text(text = "রশিদ নং (Receipt No):", fontSize = 11.sp, color = Slate600)
                  Text(text = payment.receiptNumber, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyPrimary)
                }
                Column(horizontalAlignment = Alignment.End) {
                  Text(text = "তারিখ (Date):", fontSize = 11.sp, color = Slate600)
                  Text(text = payment.date, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
              }

              Spacer(modifier = Modifier.height(4.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column {
                  Text(text = "লেনদেন আইডি:", fontSize = 11.sp, color = Slate600)
                  Text(text = payment.transactionId, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                  Text(text = "সেশন:", fontSize = 11.sp, color = Slate600)
                  Text(text = payment.academicSession, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
              }

              Spacer(modifier = Modifier.height(8.dp))
              HorizontalDivider(color = Slate200)
              Spacer(modifier = Modifier.height(8.dp))

              // Student Details Grid
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = Slate100,
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Row(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "শিক্ষার্থীর নাম: ", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.width(90.dp))
                    Text(text = payment.studentName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyPrimary)
                  }
                  Spacer(modifier = Modifier.height(3.dp))
                  Row(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "আইডি / রোল: ", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.width(90.dp))
                    Text(text = "${payment.studentId} • রোল: ${payment.rollNumber}", fontSize = 12.sp)
                  }
                  Spacer(modifier = Modifier.height(3.dp))
                  Row(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "শ্রেণি: ", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.width(90.dp))
                    Text(text = payment.className, fontSize = 12.sp)
                  }
                  if (payment.monthFor.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                      Text(text = "মাসের জন্য: ", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.width(90.dp))
                      Text(text = payment.monthFor, fontSize = 12.sp, color = EmeraldDark, fontWeight = FontWeight.Medium)
                    }
                  }
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              // Fee Breakdown Table Header
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(NavyPrimary)
                  .padding(horizontal = 8.dp, vertical = 6.dp)
              ) {
                Text(text = "ক্রম", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp))
                Text(text = "ফি বিবরণ (Fee Heads)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(text = "পরিমাণ (৳)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.width(80.dp))
              }

              // Details rows
              if (details.isNotEmpty()) {
                details.forEachIndexed { index, item ->
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 8.dp, vertical = 5.dp)
                  ) {
                    Text(text = "${index + 1}", fontSize = 11.sp, modifier = Modifier.width(36.dp))
                    Text(text = item.feeCategoryName, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Text(text = ERPLocale.formatTaka(item.amount), fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.width(80.dp))
                  }
                  HorizontalDivider(color = Slate100)
                }
              } else {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                  Text(text = "1", fontSize = 11.sp, modifier = Modifier.width(36.dp))
                  Text(text = payment.feeDescription.ifEmpty { "ছাত্রী ফি সমূহ" }, fontSize = 12.sp, modifier = Modifier.weight(1f))
                  Text(text = ERPLocale.formatTaka(payment.totalAmount), fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.width(80.dp))
                }
              }

              Spacer(modifier = Modifier.height(8.dp))
              HorizontalDivider(color = Slate200, thickness = 1.dp)
              Spacer(modifier = Modifier.height(8.dp))

              // Totals
              Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
              ) {
                Row(modifier = Modifier.width(220.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text(text = "সর্বমোট (Total):", fontSize = 12.sp)
                  Text(text = ERPLocale.formatTaka(payment.totalAmount), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                if (payment.discountAmount > 0) {
                  Spacer(modifier = Modifier.height(2.dp))
                  Row(modifier = Modifier.width(220.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "মওকুফ / ছাড়:", fontSize = 12.sp, color = CrimsonError)
                    Text(text = "-${ERPLocale.formatTaka(payment.discountAmount)}", fontSize = 12.sp, color = CrimsonError, fontWeight = FontWeight.SemiBold)
                  }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Row(modifier = Modifier.width(220.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text(text = "পরিশোধিত (Paid):", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EmeraldDark)
                  Text(text = ERPLocale.formatTaka(payment.paidAmount), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EmeraldDark)
                }
                if (payment.dueAmount > 0) {
                  Spacer(modifier = Modifier.height(2.dp))
                  Row(modifier = Modifier.width(220.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "বকেয়া (Due):", fontSize = 12.sp, color = CrimsonError)
                    Text(text = ERPLocale.formatTaka(payment.dueAmount), fontSize = 12.sp, color = CrimsonError, fontWeight = FontWeight.Bold)
                  }
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              // Payment Method & Status
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = EmeraldContainer,
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(text = "মাধ্যম: ${payment.paymentMethod}", fontSize = 11.sp, color = EmeraldOnContainer, fontWeight = FontWeight.SemiBold)
                  Text(text = "স্ট্যাটাস: পরিশোধিত (PAID)", fontSize = 11.sp, color = EmeraldOnContainer, fontWeight = FontWeight.Bold)
                }
              }

              Spacer(modifier = Modifier.height(24.dp))

              // Signature Area
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(text = "_____________________", fontSize = 11.sp, color = Slate600)
                  Text(text = "আদায়কারী: ${payment.receivedBy}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(text = "_____________________", fontSize = 11.sp, color = Slate600)
                  Text(text = "মুহতামিম / অধ্যক্ষের স্বাক্ষর", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
              }
            }
          }
        }

        // Action Bar: One-click A4 & A5 Print Buttons!
        Surface(
          tonalElevation = 6.dp,
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // One-Click A4 Print
            Button(
              onClick = {
                val html = PrintHelper.buildMoneyReceiptHtml(payment, details, isA5 = false)
                PrintHelper.printHtml(context, html, "Receipt_${payment.receiptNumber}_A4", isA5 = false)
              },
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
              modifier = Modifier.weight(1f).testTag("print_a4_button")
            ) {
              Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("A4 প্রিন্ট", fontSize = 12.sp)
            }

            // One-Click A5 Print
            Button(
              onClick = {
                val html = PrintHelper.buildMoneyReceiptHtml(payment, details, isA5 = true)
                PrintHelper.printHtml(context, html, "Receipt_${payment.receiptNumber}_A5", isA5 = true)
              },
              colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
              modifier = Modifier.weight(1f).testTag("print_a5_button")
            ) {
              Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("A5 প্রিন্ট", fontSize = 12.sp)
            }

            OutlinedButton(
              onClick = onDismiss,
              modifier = Modifier.weight(0.7f).testTag("dismiss_receipt_button")
            ) {
              Text(if (isBengali) "সম্পন্ন" else "Done", fontSize = 12.sp)
            }
          }
        }
      }
    }
  }
}

private fun shareReceiptAsText(
  context: Context,
  payment: FeePaymentEntity,
  details: List<FeePaymentDetailEntity>
) {
  val sb = StringBuilder()
  sb.appendLine("========================================")
  sb.appendLine("       জাবালে নূর গার্লস মাদ্রাসা       ")
  sb.appendLine("      JABALE NOOR GIRLS MADRASAH       ")
  sb.appendLine("  “এসো, আলোকিত হও, আলোকিত কর বিশ্ব”   ")
  sb.appendLine("   রামগড়, খাগড়াছড়ি • +880 1812-345678   ")
  sb.appendLine("========================================")
  sb.appendLine("মানি রসিদ (MONEY RECEIPT)")
  sb.appendLine("রশিদ নং: ${payment.receiptNumber}   তারিখ: ${payment.date}")
  sb.appendLine("লেনদেন আইডি: ${payment.transactionId}   সেশন: ${payment.academicSession}")
  sb.appendLine("----------------------------------------")
  sb.appendLine("শিক্ষার্থীর নাম: ${payment.studentName}")
  sb.appendLine("আইডি: ${payment.studentId}   রোল: ${payment.rollNumber}")
  sb.appendLine("শ্রেণি: ${payment.className}")
  if (payment.monthFor.isNotEmpty()) sb.appendLine("মাস: ${payment.monthFor}")
  sb.appendLine("----------------------------------------")
  sb.appendLine("ফি বিবরণ:")
  if (details.isNotEmpty()) {
    details.forEachIndexed { i, d ->
      sb.appendLine("${i + 1}. ${d.feeCategoryName}: ৳${d.amount}")
    }
  } else {
    sb.appendLine("1. ${payment.feeDescription}: ৳${payment.totalAmount}")
  }
  sb.appendLine("----------------------------------------")
  sb.appendLine("সর্বমোট: ৳${payment.totalAmount}")
  if (payment.discountAmount > 0) sb.appendLine("ছাড়: -৳${payment.discountAmount}")
  sb.appendLine("পরিশোধিত: ৳${payment.paidAmount}")
  if (payment.dueAmount > 0) sb.appendLine("বকেয়া: ৳${payment.dueAmount}")
  sb.appendLine("পদ্ধতি: ${payment.paymentMethod} (PAID)")
  sb.appendLine("আদায়কারী: ${payment.receivedBy}")
  sb.appendLine("========================================")

  val sendIntent: Intent = Intent().apply {
    action = Intent.ACTION_SEND
    putExtra(Intent.EXTRA_TEXT, sb.toString())
    putExtra(Intent.EXTRA_TITLE, "Money Receipt - ${payment.receiptNumber}")
    type = "text/plain"
  }
  val shareIntent = Intent.createChooser(sendIntent, "Print or Share Receipt")
  context.startActivity(shareIntent)
}
