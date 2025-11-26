package com.example.mariamolina.ui.screens.kids

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Importación necesaria para la flecha
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mariamolina.ui.viewmodel.AdminGameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLobbyScreen(
    onBack: () -> Unit, // ¡AQUÍ ESTÁ EL PARÁMETRO CLAVE!
    viewModel: AdminGameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Crear la partida automáticamente al entrar
    LaunchedEffect(Unit) {
        if (uiState.pinGenerado == null) {
            viewModel.crearPartida()
        }
    }

    // Usamos Scaffold para tener la barra superior con la flecha
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel del Profesor") },
                navigationIcon = {
                    IconButton(onClick = onBack) { // Usamos el parámetro onBack
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        // El contenido va dentro del padding del Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Respetamos el espacio de la barra
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator()
                Text("Generando sala...", modifier = Modifier.padding(top = 16.dp))
            } else if (uiState.pinGenerado != null) {

                // --- EL PIN EN GRANDE ---
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
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- LISTA DE JUGADORES ---
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

                // --- BOTÓN EMPEZAR ---
                Button(
                    onClick = { viewModel.empezarJuego() },
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