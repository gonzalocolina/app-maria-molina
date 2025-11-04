package com.example.mariamolina.ui.screens.pointsOfInterest

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.mariamolina.ui.theme.MariaMolinaTheme


// Nueva pantalla de "Puntos de interés"

@Composable
fun PointsOfInterestScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Puntos de Interés", style = MaterialTheme.typography.headlineMedium)
    }
}

@Preview(showBackground = true)
@Composable
fun PointsOfInterestScreenPreview() {
    MariaMolinaTheme {
        PointsOfInterestScreen()
    }
}