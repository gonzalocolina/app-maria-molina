package com.example.mariamolina.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.mariamolina.ui.theme.LightGrey


private val DarkColorScheme = darkColorScheme(
    primary = AppPrimaryBrown,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    primaryContainer = AppTextPrimaryColor,
    onSurfaceVariant = AppUnselectedGray,
    background = DarkGrey,
    // Color del icono/texto NO seleccionado
    onPrimary = AppTextPrimaryColor,
    onSecondary = AppTextSecondaryColor,
    surface = LightLightBlack,
    outline = LightLightBlack

    )

private val LightColorScheme = lightColorScheme(
    primary = AppPrimaryBrown,

    primaryContainer = AppTextPrimaryColor,
    // Color del icono/texto NO seleccionado
    onSurfaceVariant = AppUnselectedGray,
    onPrimary = AppTextPrimaryColor,
    onSecondary = AppTextSecondaryColor,
    background = White,
    surface =  LightGrey,
    outline = LightGrey
)

@Composable
fun MariaMolinaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    fontSizeScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography.scaled(fontSizeScale),
        content = content
    )
}