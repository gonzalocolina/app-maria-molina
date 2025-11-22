package com.example.mariamolina.ui.screens.kids

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mariamolina.data.model.Dificultad
import com.example.mariamolina.ui.viewmodel.AdminGameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLobbyScreen(
    onBack: () -> Unit,
    // Callback para ir a la pantalla de juego (monitor)
    onGameStarted: (String) -> Unit = {},
    viewModel: AdminGameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Estado local para la selección de dificultad antes de crear la partida
    var dificultadSeleccionada by remember { mutableStateOf(Dificultad.FACIL) }

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

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                // --- PANTALLA DE CARGA ---
                CircularProgressIndicator()
                Text("Configurando la sala...", modifier = Modifier.padding(top = 16.dp))

            } else if (uiState.pinGenerado == null) {
                // --- FASE 1: CONFIGURACIÓN (SELECTOR DE DIFICULTAD) ---
                Text(
                    text = "Configuración de la Partida",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text("Elige la dificultad para la clase:", style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.height(16.dp))

                // Selector de Chips
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DificultadChip(
                        texto = "Fácil",
                        seleccionado = dificultadSeleccionada == Dificultad.FACIL,
                        onClick = { dificultadSeleccionada = Dificultad.FACIL }
                    )
                    DificultadChip(
                        texto = "Media",
                        seleccionado = dificultadSeleccionada == Dificultad.MEDIA,
                        onClick = { dificultadSeleccionada = Dificultad.MEDIA }
                    )
                    DificultadChip(
                        texto = "Difícil",
                        seleccionado = dificultadSeleccionada == Dificultad.DIFICIL,
                        onClick = { dificultadSeleccionada = Dificultad.DIFICIL }
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = { viewModel.crearPartida(dificultadSeleccionada) },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("CREAR SALA", fontSize = 18.sp)
                }

            } else {
                // --- FASE 2: LOBBY (PIN Y JUGADORES) ---

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("CÓDIGO DE LA PARTIDA (PIN)", style = MaterialTheme.typography.labelLarge)
                        Text(
                            text = uiState.pinGenerado!!,
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Badge informativo de la dificultad
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "Dificultad: ${dificultadSeleccionada.name}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    "Jugadores conectados: ${uiState.jugadoresUnidos.size}",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.jugadoresUnidos) { jugador ->
                        JugadorCard(nombre = jugador.nickname)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.empezarJuego()
                        // Navegamos a la pantalla de juego pasando el PIN
                        onGameStarted(uiState.pinGenerado!!)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = uiState.jugadoresUnidos.isNotEmpty()
                ) {
                    Text("¡EMPEZAR EL JUEGO!", fontSize = 18.sp)
                }
            }

            if (uiState.error != null) {
                Text("Error: ${uiState.error}", color = Color.Red)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DificultadChip(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = seleccionado,
        onClick = onClick,
        label = { Text(texto) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun JugadorCard(nombre: String) {
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
            Text(text = nombre, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
    }
}