package com.example.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Handler
import android.os.Looper
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.data.entity.*

object PrintHelper {

  fun printHtml(
    context: Context,
    htmlContent: String,
    jobName: String,
    isA5: Boolean = false
  ) {
    Handler(Looper.getMainLooper()).post {
      val webView = WebView(context)
      webView.webViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean = false

        override fun onPageFinished(view: WebView?, url: String?) {
          super.onPageFinished(view, url)
          val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
          if (printManager != null) {
            val printAdapter = webView.createPrintDocumentAdapter(jobName)
            val mediaSize = if (isA5) PrintAttributes.MediaSize.ISO_A5 else PrintAttributes.MediaSize.ISO_A4
            val attributes = PrintAttributes.Builder()
              .setMediaSize(mediaSize)
              .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
              .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
              .build()
            printManager.print(jobName, printAdapter, attributes)
          }
        }
      }
      webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }
  }

  // --- 1. Money Receipt HTML ---
  fun buildMoneyReceiptHtml(
    payment: FeePaymentEntity,
    details: List<FeePaymentDetailEntity>,
    isA5: Boolean = false
  ): String {
    val pageSize = if (isA5) "A5" else "A4"
    val itemsRows = if (details.isNotEmpty()) {
      details.mapIndexed { i, d ->
        "<tr><td>${i + 1}</td><td>${d.feeCategoryName}</td><td style='text-align:right;'>৳${d.amount}</td></tr>"
      }.joinToString("")
    } else {
      "<tr><td>1</td><td>${payment.feeDescription.ifEmpty { "ছাত্রী ফি" }}</td><td style='text-align:right;'>৳${payment.totalAmount}</td></tr>"
    }

    return """
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="utf-8">
        <style>
          @page { size: $pageSize; margin: 15mm; }
          body { font-family: 'SolaimanLipi', sans-serif, Arial; color: #0A2540; margin: 0; padding: 10px; }
          .header { text-align: center; border-bottom: 2px solid #0F766E; padding-bottom: 8px; }
          .slogan { font-size: 11px; color: #D97706; margin-bottom: 3px; }
          .inst-name { font-size: 22px; font-weight: bold; margin: 0; color: #0A2540; }
          .inst-sub { font-size: 13px; color: #475569; margin: 2px 0; }
          .voucher-title { display: inline-block; background: #0F766E; color: white; padding: 4px 14px; border-radius: 4px; font-size: 13px; font-weight: bold; margin-top: 6px; }
          .meta-table, .fee-table { width: 100%; border-collapse: collapse; margin-top: 12px; }
          .meta-table td { padding: 4px 6px; font-size: 13px; }
          .fee-table th, .fee-table td { border: 1px solid #CBD5E1; padding: 6px 8px; font-size: 12px; }
          .fee-table th { background: #0A2540; color: white; }
          .total-box { margin-top: 10px; text-align: right; font-size: 13px; }
          .signatures { margin-top: 50px; display: flex; justify-content: space-between; font-size: 12px; }
        </style>
      </head>
      <body>
        <div class="header">
          <div class="slogan">“এসো, আলোকিত হও, আলোকিত কর বিশ্ব” (ESTD: 2023)</div>
          <h1 class="inst-name">জাবালে নূর গার্লস মাদ্রাসা</h1>
          <div class="inst-sub">JABALE NOOR GIRLS MADRASAH (J.N.G.M)</div>
          <div class="inst-sub">রামগড়, খাগড়াছড়ি, বাংলাদেশ • ফোন: +880 1812-345678</div>
          <div class="voucher-title">টাকা জমার রশিদ (MONEY RECEIPT)</div>
        </div>

        <table class="meta-table">
          <tr>
            <td><strong>রশিদ নং:</strong> ${payment.receiptNumber}</td>
            <td style="text-align:right;"><strong>তারিখ:</strong> ${payment.date}</td>
          </tr>
          <tr>
            <td><strong>লেনদেন আইডি:</strong> ${payment.transactionId}</td>
            <td style="text-align:right;"><strong>সেশন:</strong> ${payment.academicSession}</td>
          </tr>
          <tr>
            <td colspan="2" style="background:#F1F5F9; border-radius:4px;">
              <strong>শিক্ষার্থীর নাম:</strong> ${payment.studentName}<br>
              <strong>আইডি:</strong> ${payment.studentId} | <strong>শ্রেণি:</strong> ${payment.className} | <strong>রোল:</strong> ${payment.rollNumber}
              ${if (payment.monthFor.isNotEmpty()) "<br><strong>মাস / বাবদ:</strong> ${payment.monthFor}" else ""}
            </td>
          </tr>
        </table>

        <table class="fee-table">
          <thead>
            <tr>
              <th style="width:40px;">ক্রম</th>
              <th>ফি বিবরণ</th>
              <th style="width:110px; text-align:right;">পরিমাণ (৳)</th>
            </tr>
          </thead>
          <tbody>
            $itemsRows
          </tbody>
        </table>

        <div class="total-box">
          <p><strong>সর্বমোট ফি:</strong> ৳${payment.totalAmount}</p>
          ${if (payment.discountAmount > 0) "<p style='color:#DC2626;'><strong>ছাড় / মওকুফ:</strong> -৳${payment.discountAmount}</p>" else ""}
          <p style="color:#0F766E; font-size:15px;"><strong>পরিশোধিত টাকা:</strong> ৳${payment.paidAmount}</p>
          ${if (payment.dueAmount > 0) "<p style='color:#DC2626;'><strong>অবশিষ্ট বকেয়া:</strong> ৳${payment.dueAmount}</p>" else ""}
          <p><strong>পরিশোধের মাধ্যম:</strong> ${payment.paymentMethod} (PAID)</p>
        </div>

        <table style="width:100%; margin-top:55px; font-size:12px;">
          <tr>
            <td style="text-align:center; width:40%;">
              ------------------------------------<br>
              আদায়কারী: ${payment.receivedBy}
            </td>
            <td style="width:20%;"></td>
            <td style="text-align:center; width:40%;">
              ------------------------------------<br>
              মুহতামিম / অধ্যক্ষের স্বাক্ষর
            </td>
          </tr>
        </table>
      </body>
      </html>
    """.trimIndent()
  }

  // --- 2. Report Card (Mark Sheet) HTML ---
  fun buildReportCardHtml(
    student: StudentEntity,
    examName: String,
    results: List<ExamResultEntity>
  ): String {
    val totalMarks = results.sumOf { it.marksObtained }
    val avg = if (results.isNotEmpty()) totalMarks / results.size else 0.0
    val avgGpa = if (results.isNotEmpty()) results.sumOf { it.gpa } / results.size else 0.0
    val overallGrade = when {
      avgGpa >= 5.0 -> "A+"
      avgGpa >= 4.0 -> "A"
      avgGpa >= 3.5 -> "A-"
      avgGpa >= 3.0 -> "B"
      avgGpa >= 2.0 -> "C"
      avgGpa >= 1.0 -> "D"
      else -> "F"
    }

    val rows = results.mapIndexed { i, r ->
      "<tr><td>${i + 1}</td><td>${r.subjectName}</td><td style='text-align:center;'>100</td><td style='text-align:center;'>${r.marksObtained.toInt()}</td><td style='text-align:center; font-weight:bold;'>${r.grade}</td><td style='text-align:center;'>${"%.2f".format(r.gpa)}</td></tr>"
    }.joinToString("")

    return """
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="utf-8">
        <style>
          @page { size: A4; margin: 15mm; }
          body { font-family: 'SolaimanLipi', sans-serif, Arial; color: #0A2540; margin: 0; padding: 15px; }
          .header { text-align: center; border-bottom: 2px solid #0F766E; padding-bottom: 10px; }
          .title { font-size: 24px; font-weight: bold; margin: 0; }
          .sub { font-size: 13px; color: #475569; }
          .badge { display: inline-block; background: #0A2540; color: white; padding: 4px 16px; border-radius: 4px; font-size: 13px; font-weight: bold; margin-top: 6px; }
          .info-table { width: 100%; border-collapse: collapse; margin-top: 15px; background: #F8FAFC; border-radius: 6px; }
          .info-table td { padding: 6px 10px; font-size: 13px; }
          .results-table { width: 100%; border-collapse: collapse; margin-top: 15px; }
          .results-table th, .results-table td { border: 1px solid #CBD5E1; padding: 7px 10px; font-size: 13px; }
          .results-table th { background: #0F766E; color: white; }
          .summary-card { margin-top: 15px; padding: 12px; background: #CCFBF1; border-radius: 6px; display: flex; justify-content: space-between; font-size: 14px; font-weight: bold; color: #0F766E; }
        </style>
      </head>
      <body>
        <div class="header">
          <div style="font-size:12px; color:#D97706;">“এসো, আলোকিত হও, আলোকিত কর বিশ্ব”</div>
          <h1 class="title">জাবালে নূর গার্লস মাদ্রাসা</h1>
          <div class="sub">JABALE NOOR GIRLS MADRASAH (J.N.G.M) • রামগড়, খাগড়াছড়ি</div>
          <div class="badge">নম্বরপত্র / গ্রেডশিট (ACADEMIC REPORT CARD)</div>
          <div style="font-size:13px; font-weight:bold; color:#0F766E; margin-top:4px;">$examName</div>
        </div>

        <table class="info-table">
          <tr>
            <td><strong>শিক্ষার্থীর নাম:</strong> ${student.fullNameBn} (${student.fullNameEn})</td>
            <td><strong>আইডি:</strong> ${student.studentId}</td>
          </tr>
          <tr>
            <td><strong>শ্রেণি:</strong> ${student.className} | <strong>শাখা:</strong> ${student.section}</td>
            <td><strong>রোল নং:</strong> ${student.rollNumber}</td>
          </tr>
          <tr>
            <td><strong>পিতার নাম:</strong> ${student.fatherName}</td>
            <td><strong>মাতার নাম:</strong> ${student.motherName}</td>
          </tr>
        </table>

        <table class="results-table">
          <thead>
            <tr>
              <th style="width:40px;">ক্রম</th>
              <th>বিষয় (Subject)</th>
              <th style="width:70px; text-align:center;">পূর্ণমান</th>
              <th style="width:80px; text-align:center;">প্রাপ্ত নম্বর</th>
              <th style="width:60px; text-align:center;">গ্রেড</th>
              <th style="width:60px; text-align:center;">জিপিএ</th>
            </tr>
          </thead>
          <tbody>
            $rows
          </tbody>
        </table>

        <div class="summary-card">
          <div>মোট নম্বর: ${totalMarks.toInt()} | গড়: ${"%.1f".format(avg)}%</div>
          <div>চূড়ান্ত গ্রেড: $overallGrade | জিপিএ: ${"%.2f".format(avgGpa)} / ৫.০০ (উত্তীর্ণ)</div>
        </div>

        <div style="margin-top:20px; font-size:11px; color:#64748B;">
          <strong>গ্রেডিং স্কেল (বাংলাদেশ শিক্ষা বোর্ড মান):</strong> 80-100: A+ (5.0), 70-79: A (4.0), 60-69: A- (3.5), 50-59: B (3.0), 40-49: C (2.0), 33-39: D (1.0), 0-32: F (0.0)
        </div>

        <table style="width:100%; margin-top:60px; font-size:12px;">
          <tr>
            <td style="text-align:center; width:33%;">
              -----------------------------<br>
              শ্রেণি শিক্ষিকা
            </td>
            <td style="text-align:center; width:33%;">
              -----------------------------<br>
              পরীক্ষা নিয়ন্ত্রক
            </td>
            <td style="text-align:center; width:33%;">
              -----------------------------<br>
              মুহতামিম / অধ্যক্ষ
            </td>
          </tr>
        </table>
      </body>
      </html>
    """.trimIndent()
  }

  // --- 3. Admit Card (প্রবেশপত্র) HTML ---
  fun buildAdmitCardHtml(
    student: StudentEntity,
    examName: String,
    schedules: List<ExamScheduleEntity>
  ): String {
    val scheduleRows = schedules.mapIndexed { i, s ->
      "<tr><td>${i + 1}</td><td>${s.subjectName}</td><td style='text-align:center;'>${s.examDate}</td><td style='text-align:center;'>${s.startTime} - ${s.endTime}</td><td style='text-align:center;'>${s.roomNo}</td></tr>"
    }.joinToString("")

    return """
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="utf-8">
        <style>
          @page { size: A5 landscape; margin: 10mm; }
          body { font-family: 'SolaimanLipi', sans-serif, Arial; color: #0A2540; margin: 0; padding: 10px; border: 2px dashed #0F766E; border-radius: 8px; }
          .header { text-align: center; border-bottom: 2px solid #0A2540; padding-bottom: 6px; }
          .title { font-size: 19px; font-weight: bold; margin: 0; }
          .badge { display: inline-block; background: #0F766E; color: white; padding: 3px 12px; border-radius: 4px; font-size: 12px; font-weight: bold; margin-top: 4px; }
          .info-table { width: 100%; border-collapse: collapse; margin-top: 8px; font-size: 12px; }
          .info-table td { padding: 3px 6px; }
          .schedule-table { width: 100%; border-collapse: collapse; margin-top: 8px; font-size: 11px; }
          .schedule-table th, .schedule-table td { border: 1px solid #CBD5E1; padding: 4px 6px; }
          .schedule-table th { background: #0A2540; color: white; }
          .rules { font-size: 10px; color: #64748B; margin-top: 8px; }
        </style>
      </head>
      <body>
        <div class="header">
          <div style="font-size:10px; color:#D97706;">“এসো, আলোকিত হও, আলোকিত কর বিশ্ব” (ESTD: 2023)</div>
          <h2 class="title">জাবালে নূর গার্লস মাদ্রাসা (J.N.G.M)</h2>
          <div style="font-size:11px; color:#475569;">রামগড়, খাগড়াছড়ি, বাংলাদেশ</div>
          <div class="badge">পরীক্ষার প্রবেশপত্র (ADMIT CARD)</div>
          <div style="font-size:12px; font-weight:bold; color:#0A2540; margin-top:2px;">$examName</div>
        </div>

        <table class="info-table">
          <tr>
            <td><strong>শিক্ষার্থীর নাম:</strong> ${student.fullNameBn}</td>
            <td><strong>আইডি:</strong> ${student.studentId}</td>
            <td rowspan="3" style="width:70px; text-align:center; border:1px solid #CBD5E1;">
              ছবি<br>(Photo)
            </td>
          </tr>
          <tr>
            <td><strong>শ্রেণি:</strong> ${student.className}</td>
            <td><strong>রোল নং:</strong> ${student.rollNumber}</td>
          </tr>
          <tr>
            <td><strong>পিতার নাম:</strong> ${student.fatherName}</td>
            <td><strong>লিঙ্গ:</strong> ${if (student.gender == "Male") "ছাত্র" else "ছাত্রী"}</td>
          </tr>
        </table>

        ${if (schedules.isNotEmpty()) """
          <table class="schedule-table">
            <thead>
              <tr>
                <th style="width:30px;">ক্রম</th>
                <th>বিষয়</th>
                <th style="width:70px; text-align:center;">তারিখ</th>
                <th style="width:110px; text-align:center;">সময়</th>
                <th style="width:50px; text-align:center;">কক্ষ</th>
              </tr>
            </thead>
            <tbody>
              $scheduleRows
            </tbody>
          </table>
        """ else ""}

        <div class="rules">
          * পরীক্ষার্থীকে অবশ্যই পরীক্ষা শুরুর ১৫ মিনিট পূর্বে কক্ষে উপস্থিত হতে হবে। পরীক্ষার হলে প্রবেশপত্র প্রদর্শন আবশ্যক।
        </div>

        <table style="width:100%; margin-top:25px; font-size:11px;">
          <tr>
            <td style="text-align:center; width:40%;">
              -----------------------------<br>
              শ্রেণি শিক্ষকের স্বাক্ষর
            </td>
            <td style="width:20%;"></td>
            <td style="text-align:center; width:40%;">
              -----------------------------<br>
              মুহতামিম / অধ্যক্ষের স্বাক্ষর
            </td>
          </tr>
        </table>
      </body>
      </html>
    """.trimIndent()
  }

  // --- 4. Class-wise Master Result Tabulation Sheet HTML (এক কাগজে ক্লাস ফলাফল) ---
  fun buildTabulationSheetHtml(
    examName: String,
    className: String,
    students: List<StudentEntity>,
    subjects: List<SubjectEntity>,
    allResults: List<ExamResultEntity>
  ): String {
    val subjectHeaders = subjects.map { "<th>${it.nameBn}</th>" }.joinToString("")

    val rows = students.sortedBy { it.rollNumber }.map { student ->
      val studentResults = allResults.filter { it.studentId == student.studentId }
      val subjectTds = subjects.map { sub ->
        val res = studentResults.find { it.subjectId == sub.id }
        if (res != null) {
          "<td style='text-align:center;'>${res.marksObtained.toInt()}<br><small>(${res.grade})</small></td>"
        } else {
          "<td style='text-align:center;'>-</td>"
        }
      }.joinToString("")

      val total = studentResults.sumOf { it.marksObtained }
      val gpa = if (studentResults.isNotEmpty()) studentResults.sumOf { it.gpa } / studentResults.size else 0.0
      val hasFail = studentResults.any { it.grade == "F" }
      val status = if (hasFail || studentResults.isEmpty()) "<span style='color:red;'>অকৃতকার্য</span>" else "<span style='color:green;'>উত্তীর্ণ</span>"

      "<tr>" +
        "<td style='text-align:center;'>${student.rollNumber}</td>" +
        "<td>${student.fullNameBn}</td>" +
        "<td style='text-align:center;'>${student.studentId}</td>" +
        subjectTds +
        "<td style='text-align:center; font-weight:bold;'>${total.toInt()}</td>" +
        "<td style='text-align:center; font-weight:bold;'>${"%.2f".format(gpa)}</td>" +
        "<td style='text-align:center; font-weight:bold;'>$status</td>" +
      "</tr>"
    }.joinToString("")

    return """
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="utf-8">
        <style>
          @page { size: A4 landscape; margin: 10mm; }
          body { font-family: 'SolaimanLipi', sans-serif, Arial; color: #0A2540; margin: 0; padding: 10px; }
          .header { text-align: center; border-bottom: 2px solid #0F766E; padding-bottom: 8px; margin-bottom: 12px; }
          .title { font-size: 22px; font-weight: bold; margin: 0; }
          .table { width: 100%; border-collapse: collapse; font-size: 11px; }
          .table th, .table td { border: 1px solid #94A3B8; padding: 5px 6px; }
          .table th { background: #0A2540; color: white; }
        </style>
      </head>
      <body>
        <div class="header">
          <h2 class="title">জাবালে নূর গার্লস মাদ্রাসা (J.N.G.M)</h2>
          <div style="font-size:12px; color:#475569;">রামগড়, খাগড়াছড়ি, বাংলাদেশ • শিক্ষা ও মূল্যায়ন শাখা</div>
          <div style="font-size:14px; font-weight:bold; color:#0F766E; margin-top:4px;">শ্রেণিভিত্তিক ফলাফল বিবরণী (TABULATION SHEET)</div>
          <div style="font-size:12px; margin-top:2px;">পরীক্ষা: <strong>$examName</strong> | শ্রেণি: <strong>$className</strong></div>
        </div>

        <table class="table">
          <thead>
            <tr>
              <th style="width:35px;">রোল</th>
              <th>শিক্ষার্থীর নাম</th>
              <th style="width:75px;">আইডি</th>
              $subjectHeaders
              <th style="width:55px;">মোট</th>
              <th style="width:50px;">জিপিএ</th>
              <th style="width:65px;">ফলাফল</th>
            </tr>
          </thead>
          <tbody>
            $rows
          </tbody>
        </table>

        <table style="width:100%; margin-top:50px; font-size:12px;">
          <tr>
            <td style="text-align:center; width:33%;">-------------------------<br>শ্রেণি শিক্ষিকা</td>
            <td style="text-align:center; width:33%;">-------------------------<br>যাচাইকারী কর্মকর্তা</td>
            <td style="text-align:center; width:33%;">-------------------------<br>মুহতামিম / অধ্যক্ষ</td>
          </tr>
        </table>
      </body>
      </html>
    """.trimIndent()
  }

  // --- 5. Testimonial / Certificate (প্রত্যয়নপত্র / সনদ) HTML ---
  fun buildCertificateHtml(student: StudentEntity): String {
    return """
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="utf-8">
        <style>
          @page { size: A4; margin: 15mm; }
          body { font-family: 'SolaimanLipi', sans-serif, Arial; color: #0A2540; margin: 0; padding: 25px; border: 10px double #0F766E; border-radius: 12px; }
          .header { text-align: center; }
          .title { font-size: 26px; font-weight: bold; margin: 0; color: #0A2540; }
          .sub { font-size: 13px; color: #475569; margin-top: 3px; }
          .cert-title { font-size: 22px; font-weight: bold; color: #0F766E; margin: 25px 0 15px 0; text-align: center; text-decoration: underline; }
          .body-text { font-size: 16px; line-height: 2; text-align: justify; margin-top: 20px; }
        </style>
      </head>
      <body>
        <div class="header">
          <div style="font-size:12px; color:#D97706;">“এসো, আলোকিত হও, আলোকিত কর বিশ্ব”</div>
          <h1 class="title">জাবালে নূর গার্লস মাদ্রাসা</h1>
          <div class="sub">JABALE NOOR GIRLS MADRASAH (ESTD: 2023)</div>
          <div class="sub">রামগড়, খাগড়াছড়ি, বাংলাদেশ</div>
        </div>

        <div class="cert-title">প্রত্যয়নপত্র (TESTIMONIAL)</div>

        <div class="body-text">
          এই মর্মে প্রত্যয়ন করা যাচ্ছে যে, <strong>${student.fullNameBn}</strong> (${student.fullNameEn}),
          পিতা: <strong>${student.fatherName}</strong>, মাতা: <strong>${student.motherName}</strong>,
          ঠিকানা: ${student.presentAddress}। সে অত্র জাবালে নূর গার্লস মাদ্রাসার
          <strong>${student.className}</strong>-এর একজন নিয়মিত ${if (student.gender == "Male") "ছাত্র" else "ছাত্রী"}।
          তার শ্রেণি রোল নং <strong>${student.rollNumber}</strong> এবং শিক্ষার্থী আইডি নং <strong>${student.studentId}</strong>।
          <br><br>
          অত্র প্রতিষ্ঠানে অধ্যয়নকালে তার নৈতিক চরিত্র, আচার-ব্যবহার ও লেখাপড়ার মান অত্যন্ত সন্তোষজনক। সে কোনো প্রকার রাষ্ট্র বা সমাজবিরোধী কাজে লিপ্ত নয়।
          <br><br>
          আমি তার ভবিষ্যৎ জীবনের উত্তরোত্তর সমৃদ্ধি, সুস্বাস্থ্য ও সাফল্য কামনা করি।
        </div>

        <table style="width:100%; margin-top:80px; font-size:13px;">
          <tr>
            <td style="text-align:left; width:50%;">
              তারিখ: .............................
            </td>
            <td style="text-align:right; width:50%;">
              ------------------------------------<br>
              <strong>মুহতামিম / অধ্যক্ষ</strong><br>
              জাবালে নূর গার্লস মাদ্রাসা, রামগড়
            </td>
          </tr>
        </table>
      </body>
      </html>
    """.trimIndent()
  }
}
