package com.example.shoeshop.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.example.shoeshop.R

// Инициализация Google Fonts Provider
private val fontProvider = androidx.compose.ui.text.googlefonts.GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// Определяем Google Font
private val ralewayName = GoogleFont("Raleway")

// Создаем отдельные FontFamily для каждого начертания
val ralewayRegular = FontFamily(
    Font(
        googleFont = ralewayName,
        fontProvider = fontProvider,
        weight = androidx.compose.ui.text.font.FontWeight.Normal
    )
)

val ralewayMedium = FontFamily(
    Font(
        googleFont = ralewayName,
        fontProvider = fontProvider,
        weight = androidx.compose.ui.text.font.FontWeight.Medium
    )
)

val ralewaySemiBold = FontFamily(
    Font(
        googleFont = ralewayName,
        fontProvider = fontProvider,
        weight = androidx.compose.ui.text.font.FontWeight.SemiBold
    )
)

val ralewayBold = FontFamily(
    Font(
        googleFont = ralewayName,
        fontProvider = fontProvider,
        weight = androidx.compose.ui.text.font.FontWeight.Bold
    )
)

// Светлая цветовая схема
private val LightColorScheme = lightColorScheme(
    primary = Accent,
    secondary = Disable,
    surface = Block,
    background = Background,
    onBackground = Hint,
    onSurface = Text,
    onPrimary = Color.White,
    onSecondary = Color.White,
    error = Red,
    outline = SubtextDark
)

// Темная цветовая схема
private val DarkColorScheme = darkColorScheme(
    primary = Accent,
    secondary = Disable,
    surface = DarkBlock,
    background = DarkBackground,
    onBackground = DarkText,
    onSurface = DarkText,
    onPrimary = Color.White,
    onSecondary = Color.White,
    error = Red,
    outline = SubtextDark
)

@Composable
fun ShoeShopTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}