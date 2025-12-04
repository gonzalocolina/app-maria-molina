package com.example.mariamolina.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable


private val LightColorScheme = lightColorScheme(
    primary = AppPrimaryBrown,
    primaryContainer = AppTextPrimaryColor,
    // Color del icono/texto NO seleccionado
    onSurfaceVariant = AppUnselectedGray,
    onPrimary = AppTextPrimaryColor,
    onSecondary = AppTextSecondaryColor,
    background = White,
    surface = LightGrey,
    outline = LightGrey
)

@Composable
fun MariaMolinaTheme(
    fontSizeScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    // Siempre usar el esquema de colores claro
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography.scaled(fontSizeScale),
        content = content
    )
}