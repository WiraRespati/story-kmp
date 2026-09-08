package com.learn.story.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import story.shared.generated.resources.Poppins_Bold
import story.shared.generated.resources.Poppins_Medium
import story.shared.generated.resources.Poppins_Regular
import story.shared.generated.resources.Poppins_SemiBold
import story.shared.generated.resources.Res

enum class AppFontFamily(
    val code: String,
    val title: String,
    val subtitle: String
) {
    POPPINS("poppins", "Poppins", "Modern, dinamis, & stylish (Bawaan)"),
    SYSTEM("system", "Default Sistem", "San Francisco (iOS) / Roboto (Android)"),
    SERIF("serif", "Serif Elegan", "Gaya buku cerita klasik"),
    MONOSPACE("monospace", "Monospace Tech", "Gaya terminal & typewriter");

    companion object {
        fun fromCode(code: String): AppFontFamily =
            entries.find { it.code == code } ?: POPPINS
    }
}

@Composable
fun poppinsFontFamily(): FontFamily = FontFamily(
    Font(Res.font.Poppins_Regular, FontWeight.Normal),
    Font(Res.font.Poppins_Medium, FontWeight.Medium),
    Font(Res.font.Poppins_SemiBold, FontWeight.SemiBold),
    Font(Res.font.Poppins_Bold, FontWeight.Bold)
)

@Composable
fun resolveFontFamily(family: AppFontFamily): FontFamily = when (family) {
    AppFontFamily.POPPINS -> poppinsFontFamily()
    AppFontFamily.SYSTEM -> FontFamily.Default
    AppFontFamily.SERIF -> FontFamily.Serif
    AppFontFamily.MONOSPACE -> FontFamily.Monospace
}

@Composable
fun appTypography(family: AppFontFamily = AppFontFamily.POPPINS): Typography {
    val currentFontFamily = resolveFontFamily(family)
    return Typography(
        headlineLarge = TextStyle(
            fontFamily = currentFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            lineHeight = 38.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = currentFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 32.sp
        ),
        titleLarge = TextStyle(
            fontFamily = currentFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            lineHeight = 26.sp
        ),
        titleMedium = TextStyle(
            fontFamily = currentFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 22.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = currentFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = currentFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        labelLarge = TextStyle(
            fontFamily = currentFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        labelMedium = TextStyle(
            fontFamily = currentFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
    )
}

