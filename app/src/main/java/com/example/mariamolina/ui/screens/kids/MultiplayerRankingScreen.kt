package com.example.mariamolina.ui.screens.kids

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mariamolina.data.model.Jugador
import com.example.mariamolina.ui.viewmodel.StudentGameViewModel
import kotlinx.coroutines.delay
import com.example.mariamolina.R


/**
 * Pantalla de ranking final de una partida multijugador.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiplayerRankingScreen(
    pin: String,
    onBackToMenu: () -> Unit,
    viewModel: StudentGameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Comprobar si aún no está observando
    LaunchedEffect(pin) {
        if (uiState.pin.isEmpty()) {
            viewModel.startObservingGame(pin)
        }
    }

    // Animación de celebración
    var showConfetti by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(500)
        showConfetti = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ranking_final)) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.reset()
                        onBackToMenu()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_volver)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Título con confetti
            if (showConfetti) {
                item {
                    Text(
                        stringResource(R.string.partida_finalizada),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Mi resultado
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            stringResource(R.string.tu_resultado),
                            style = MaterialTheme.typography.labelLarge
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (uiState.miPosicion in 1..3) {
                                Icon(
                                    Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = when(uiState.miPosicion) {
                                        1 -> Color(0xFFFFD700)  // Oro
                                        2 -> Color(0xFFC0C0C0)  // Plata
                                        else -> Color(0xFFCD7F32)  // Bronce
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            
                            Text(
                                "#${uiState.miPosicion}",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "${uiState.miPuntuacionTotal} ${stringResource(R.string.puntos_lower)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Título de clasificación
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    stringResource(R.string.clasificacion_completa),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Lista completa de ranking
            itemsIndexed(uiState.ranking) { index, jugador ->
                RankingItem(
                    posicion = index + 1,
                    jugador = jugador,
                    isCurrentUser = jugador.puntuacion == uiState.miPuntuacionTotal && 
                                   uiState.miPosicion == index + 1
                )
            }

            // Botón volver al menú
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        viewModel.reset()
                        onBackToMenu()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(Icons.Default.Home, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.volver_al_menu), fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun RankingItem(
    posicion: Int,
    jugador: Jugador,
    isCurrentUser: Boolean
) {
    val backgroundColor = when {
        isCurrentUser -> MaterialTheme.colorScheme.primaryContainer
        posicion == 1 -> Color(0xFFFFD700).copy(alpha = 0.2f)
        posicion == 2 -> Color(0xFFC0C0C0).copy(alpha = 0.2f)
        posicion == 3 -> Color(0xFFCD7F32).copy(alpha = 0.2f)
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Posición
                Box(
                    modifier = Modifier.width(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (posicion <= 3) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = when(posicion) {
                                1 -> Color(0xFFFFD700)
                                2 -> Color(0xFFC0C0C0)
                                else -> Color(0xFFCD7F32)
                            },
                            modifier = Modifier.size(28.dp)
                        )
                    } else {
                        Text(
                            "#$posicion",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        jugador.nickname,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Normal
                    )
                    if (isCurrentUser) {
                        Text(
                            stringResource(R.string.tu_emoji),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Text(
                "${jugador.puntuacion} ${stringResource(R.string.pts)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
