package com.example.mariamolina.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.mariamolina.ui.theme.MariaMolinaTheme


// 5. La primera pantalla, en su propio archivo.
// He renombrado "VistaInicio" a "HomeScreen", que es una
// convención de nombres más común.

@Composable
fun HomeScreen() {
    // Aquí puedes empezar a construir tu vista de Inicio
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Vista de Inicio", style = MaterialTheme.typography.headlineMedium)
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MariaMolinaTheme {
        HomeScreen()
    }
}