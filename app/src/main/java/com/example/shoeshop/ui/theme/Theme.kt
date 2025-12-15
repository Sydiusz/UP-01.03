package com.example.shoeshop.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.core.view.WindowCompat
import com.example.shoeshop.R

// УДАЛИТЕ или ЗАКОММЕНТИРУЙТЕ старые цвета Purple80 и т.д.
// private val Purple80 = Color(0xFFD0BCFF)
// private val PurpleGrey80 = Color(0xFFCCC2DC)
// private val Pink80 = Color(0xFFEFB8C8)
// private val Purple40 = Color(0xFF6650a4)
// private val PurpleGrey40 = Color(0xFF625b71)
// private val Pink40 = Color(0xFF7D5260)

// НОВАЯ светлая цветовая схема с вашими цветами
private val LightColorScheme = lightColorScheme(
    primary = Accent,           // Основной цвет (#FF48B2E7)
    onPrimary = Block,          // Текст на primary цвете

    secondary = Red,            // Вторичный цвет (#FFF87265)
    onSecondary = Block,        // Текст на secondary цвете

    tertiary = Disable,         // Третичный цвет (#FF2B6B8B)
    onTertiary = Block,         // Текст на tertiary цвете

    background = Background,    // Фон (#FFF7F7F9)
    onBackground = Text,        // Текст на фоне (#FF2B2B2B)

    surface = Block,            // Поверхности (карточки, диалоги) (#FFFFFFFF)
    onSurface = Text,           // Текст на поверхностях

    surfaceVariant = SubTextLight, // Вариация поверхности (#FFD8D8D8)
    onSurfaceVariant = SubtextDark, // Текст на surfaceVariant (#FF707B81)

    error = Red,                // Цвет ошибок
    onError = Block,            // Текст на ошибке

    outline = Hint,             // Обводка (#FF6A6A6A)
    outlineVariant = SubTextLight, // Вариация обводки

    scrim = SubtextDark.copy(alpha = 0.5f) // Затемнение
)

// НОВАЯ темная цветовая схема
private val DarkColorScheme = darkColorScheme(
    primary = Accent,
    onPrimary = DarkText,

    secondary = Red,
    onSecondary = DarkText,

    tertiary = Disable,
    onTertiary = DarkText,

    background = DarkBackground,    // Темный фон
    onBackground = DarkText,        // Светлый текст на темном фоне

    surface = DarkBlock,            // Темные поверхности
    onSurface = DarkText,

    surfaceVariant = SubtextDark,
    onSurfaceVariant = SubTextLight,

    error = Red,
    onError = DarkText,

    outline = DarkHint,
    outlineVariant = SubtextDark,

    scrim = Color.Black.copy(alpha = 0.5f)
)

val Inter = FontFamily(
    Font(R.font.raleway, FontWeight.Normal),
    Font(R.font.raleway_bold, FontWeight.Bold),
    Font(R.font.raleway_medium, FontWeight.Medium),
    Font(R.font.raleway_semibold, FontWeight.SemiBold),
)

@Composable
fun ShoeShopTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Отключите dynamicColor, если хотите использовать только ваши цвета
    dynamicColor: Boolean = false, // ИЗМЕНИТЕ на false
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Если оставите dynamicColor = true, закомментируйте этот блок
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Дополнительно: настройка системного интерфейса
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Будет обновлено в следующем шаге
        content = content
    )
}