package com.example.mariamolina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.mariamolina.ui.navigation.AppNavigation
import com.example.mariamolina.ui.theme.MariaMolinaTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MariaMolinaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 1. MainActivity ahora solo llama a nuestro
                    // Composable de navegación principal. ¡Mucho más limpio!
                    AppNavigation()
                }
            }
        }
    }
}