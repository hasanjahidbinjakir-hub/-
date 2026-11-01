package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = EmeraldLight,
  onPrimary = Color(0xFF003830),
  primaryContainer = EmeraldPrimary,
  onPrimaryContainer = EmeraldContainer,
  secondary = GoldAccent,
  onSecondary = Color(0xFF452B00),
  secondaryContainer = GoldDark,
  onSecondaryContainer = GoldLight,
  tertiary = Color(0xFF90CAF9),
  background = Color(0xFF0B132B),
  onBackground = Color(0xFFF1F5F9),
  surface = Color(0xFF131D38),
  onSurface = Color(0xFFF8FAFC),
  surfaceVariant = Color(0xFF1C2A4F),
  onSurfaceVariant = Color(0xFFCBD5E1),
  outline = Color(0xFF475569),
  error = Color(0xFFEF4444)
)

private val LightColorScheme = lightColorScheme(
  primary = NavyPrimary,
  onPrimary = Color.White,
  primaryContainer = NavyContainer,
  onPrimaryContainer = NavyPrimary,
  secondary = EmeraldPrimary,
  onSecondary = Color.White,
  secondaryContainer = EmeraldContainer,
  onSecondaryContainer = EmeraldOnContainer,
  tertiary = GoldAccent,
  onTertiary = Color.White,
  tertiaryContainer = GoldLight,
  onTertiaryContainer = GoldDark,
  background = Slate50,
  onBackground = Slate900,
  surface = Color.White,
  onSurface = Slate900,
  surfaceVariant = Slate100,
  onSurfaceVariant = Slate700,
  outline = Slate200,
  error = CrimsonError,
  errorContainer = CrimsonContainer
)

@Composable
fun JabaleNoorTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
