package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.ERPLocale
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Jabale Noor ERP", appName)
  }

  @Test
  fun `verify Bangladesh grading and GPA 5 scale`() {
    val (grade95, gpa95) = ERPLocale.calculateGradeAndGpa(95.0)
    assertEquals("A+", grade95)
    assertEquals(5.0, gpa95, 0.01)

    val (grade75, gpa75) = ERPLocale.calculateGradeAndGpa(75.0)
    assertEquals("A", grade75)
    assertEquals(4.0, gpa75, 0.01)

    val (grade65, gpa65) = ERPLocale.calculateGradeAndGpa(65.0)
    assertEquals("A-", grade65)
    assertEquals(3.5, gpa65, 0.01)

    val (grade25, gpa25) = ERPLocale.calculateGradeAndGpa(25.0)
    assertEquals("F", grade25)
    assertEquals(0.0, gpa25, 0.01)
  }

  @Test
  fun `verify taka currency formatting`() {
    val takaStr = ERPLocale.formatTaka(1500.0)
    assertEquals("৳1,500.00", takaStr)
  }
}
