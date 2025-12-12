package com.example.mariamolina.ui.screens.kids

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mariamolina.data.model.Dificultad
import com.example.mariamolina.data.model.GamePhase
import com.example.mariamolina.ui.viewmodel.TeacherGameViewModel

// Opciones de tiempo disponibles (en segundos)
private val OPCIONES_TIEMPO = listOf(10, 15, 20, 30, 45, 60)

/**
 * Pantalla del lobby del profesor donde crea la partida y espera jugadores.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherLobbyScreen(
    onBack: () -> Unit,
    onGameStarted: (String) -> Unit,  // Navegar a TeacherGameScreen con PIN
    existingPin: String? = null,  // PIN de partida existente para reconectar
    viewModel: TeacherGameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Reconectar a partida existente o crear nueva
    LaunchedEffect(Unit) {
        if (uiState.pin == null && !uiState.isLoading) {
            if (existingPin != null) {
                // Reconectar a partida existente
                viewModel.reconnectToLobby(existingPin)
            } else {
                // Crear nueva partida
                viewModel.crearPartida()
            }
        }
    }

    // Navegar cuando el juego empieza
    LaunchedEffect(uiState.partida?.fase) {
        if (uiState.partida?.fase == GamePhase.SHOWING_QUESTION) {
            uiState.pin?.let { onGameStarted(it) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel del Profesor") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
                Text("Generando sala...", modifier = Modifier.padding(top = 16.dp))
            } else if (uiState.pin != null) {
                // --- SELECTOR DE DIFICULTAD ---
                Text(
                    "Dificultad seleccionada:",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Dificultad.entries.forEach { dificultad ->
                        FilterChip(
                            selected = uiState.dificultadSeleccionada == dificultad,
                            onClick = { viewModel.setDificultad(dificultad) },
                            label = { Text(dificultad.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- SELECTOR DE TIEMPO POR PREGUNTA ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Tiempo por pregunta:",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OPCIONES_TIEMPO.forEach { segundos ->
                        FilterChip(
                            selected = uiState.tiempoPorPreguntaSeleccionado == segundos,
                            onClick = { viewModel.setTiempoPorPregunta(segundos) },
                            label = { Text("${segundos}s") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- EL PIN EN GRANDE ---
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "CÓDIGO DE LA PARTIDA",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = uiState.pin!!,
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Los alumnos deben introducir este PIN para unirse",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- LISTA DE JUGADORES ---
                Text(
                    "Jugadores conectados: ${uiState.jugadores.size}",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.jugadores.isEmpty()) {
                    Text(
                        "Esperando jugadores...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.jugadores) { jugador ->
                        Card(
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = jugador.nickname,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- BOTÓN EMPEZAR ---
                Button(
                    onClick = { viewModel.empezarJuego() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = uiState.jugadores.isNotEmpty()
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("¡EMPEZAR EL JUEGO!", fontSize = 18.sp)
                }
                
                if (uiState.jugadores.isEmpty()) {
                    Text(
                        "Necesitas al menos 1 jugador para empezar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Error: ${uiState.error}",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
