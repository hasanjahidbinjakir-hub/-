package com.example.util

object ERPLocale {

  fun formatTaka(amount: Double): String {
    return "৳%,.2f".format(amount)
  }

  fun calculateGradeAndGpa(marks: Double): Pair<String, Double> {
    return when {
      marks >= 80.0 -> Pair("A+", 5.0)
      marks >= 70.0 -> Pair("A", 4.0)
      marks >= 60.0 -> Pair("A-", 3.5)
      marks >= 50.0 -> Pair("B", 3.0)
      marks >= 40.0 -> Pair("C", 2.0)
      marks >= 33.0 -> Pair("D", 1.0)
      else -> Pair("F", 0.0)
    }
  }

  fun formatBanglaNumber(input: String): String {
    val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    val sb = StringBuilder()
    for (ch in input) {
      if (ch in '0'..'9') {
        sb.append(bnDigits[ch - '0'])
      } else {
        sb.append(ch)
      }
    }
    return sb.toString()
  }
}
