package es.uva.inf.mariamolina.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)

fun Typography.scaled(scale: Float): Typography {
    return this.copy(
        displayLarge = displayLarge.copy(fontSize = displayLarge.fontSize * scale, lineHeight = displayLarge.lineHeight * scale, letterSpacing = displayLarge.letterSpacing * scale),
        displayMedium = displayMedium.copy(fontSize = displayMedium.fontSize * scale, lineHeight = displayMedium.lineHeight * scale, letterSpacing = displayMedium.letterSpacing * scale),
        displaySmall = displaySmall.copy(fontSize = displaySmall.fontSize * scale, lineHeight = displaySmall.lineHeight * scale, letterSpacing = displaySmall.letterSpacing * scale),
        headlineLarge = headlineLarge.copy(fontSize = headlineLarge.fontSize * scale, lineHeight = headlineLarge.lineHeight * scale, letterSpacing = headlineLarge.letterSpacing * scale),
        headlineMedium = headlineMedium.copy(fontSize = headlineMedium.fontSize * scale, lineHeight = headlineMedium.lineHeight * scale, letterSpacing = headlineMedium.letterSpacing * scale),
        headlineSmall = headlineSmall.copy(fontSize = headlineSmall.fontSize * scale, lineHeight = headlineSmall.lineHeight * scale, letterSpacing = headlineSmall.letterSpacing * scale),
        titleLarge = titleLarge.copy(fontSize = titleLarge.fontSize * scale, lineHeight = titleLarge.lineHeight * scale, letterSpacing = titleLarge.letterSpacing * scale),
        titleMedium = titleMedium.copy(fontSize = titleMedium.fontSize * scale, lineHeight = titleMedium.lineHeight * scale, letterSpacing = titleMedium.letterSpacing * scale),
        titleSmall = titleSmall.copy(fontSize = titleSmall.fontSize * scale, lineHeight = titleSmall.lineHeight * scale, letterSpacing = titleSmall.letterSpacing * scale),
        bodyLarge = bodyLarge.copy(fontSize = bodyLarge.fontSize * scale, lineHeight = bodyLarge.lineHeight * scale, letterSpacing = bodyLarge.letterSpacing * scale),
        bodyMedium = bodyMedium.copy(fontSize = bodyMedium.fontSize * scale, lineHeight = bodyMedium.lineHeight * scale, letterSpacing = bodyMedium.letterSpacing * scale),
        bodySmall = bodySmall.copy(fontSize = bodySmall.fontSize * scale, lineHeight = bodySmall.lineHeight * scale, letterSpacing = bodySmall.letterSpacing * scale),
        labelLarge = labelLarge.copy(fontSize = labelLarge.fontSize * scale, lineHeight = labelLarge.lineHeight * scale, letterSpacing = labelLarge.letterSpacing * scale),
        labelMedium = labelMedium.copy(fontSize = labelMedium.fontSize * scale, lineHeight = labelMedium.lineHeight * scale, letterSpacing = labelMedium.letterSpacing * scale),
        labelSmall = labelSmall.copy(fontSize = labelSmall.fontSize * scale, lineHeight = labelSmall.lineHeight * scale, letterSpacing = labelSmall.letterSpacing * scale),
    )
}
