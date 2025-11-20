package com.example.mariamolina.ui.screens.kids

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mariamolina.data.model.Jugador
import com.example.mariamolina.ui.viewmodel.RankingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(
    pinPartida: String?, // ¡CAMBIO! Ahora puede ser nulo y no tiene valor por defecto fijo
    onBackClick: () -> Unit,
    viewModel: RankingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Cargamos el ranking al entrar, solo si hay PIN
    LaunchedEffect(pinPartida) {
        if (!pinPartida.isNullOrBlank()) {
            viewModel.escucharRanking(pinPartida)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                // Mostramos el PIN si existe, si no, un título genérico
                title = { Text(if (pinPartida != null) "Ranking - Sala $pinPartida" else "Ranking Global") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Caso A: No hay PIN (Entró desde el menú principal)
            if (pinPartida.isNullOrBlank()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Introduce un PIN para ver el ranking", style = MaterialTheme.typography.bodyLarge)
                    // Aquí podrías poner un campo de texto para buscar un ranking manual si quisieras
                }
            }
            // Caso B: Cargando
            else if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            // Caso C: Mostramos lista
            else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color(0xFFFFD700)
                            )
                            Text("Tabla de Clasificación", style = MaterialTheme.typography.headlineSmall)
                        }
                    }

                    if (uiState.jugadores.isEmpty()) {
                        item {
                            Text(
                                "Aún no hay jugadores en este ranking.",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        itemsIndexed(uiState.jugadores) { index, jugador ->
                            RankingItem(posicion = index + 1, jugador = jugador)
                        }
                    }
                }
            }

            if (uiState.error != null) {
                Text(
                    text = "Error: ${uiState.error}",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun RankingItem(posicion: Int, jugador: Jugador) {
    val colorTrofeo = when (posicion) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(colorTrofeo, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#$posicion",
                    fontWeight = FontWeight.Bold,
                    color = if (posicion <= 3) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = jugador.nickname,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "${jugador.puntuacion} pts",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}