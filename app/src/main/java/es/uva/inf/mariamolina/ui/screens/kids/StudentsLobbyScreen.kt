package es.uva.inf.mariamolina.ui.screens.kids

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import es.uva.inf.mariamolina.R
import es.uva.inf.mariamolina.data.model.GamePhase
import es.uva.inf.mariamolina.ui.viewmodel.StudentGameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentLobbyScreen(
    pin: String,
    onGameStarted: (String) -> Unit, // Callback para ir al juego
    onBack: () -> Unit,
    viewModel: StudentGameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Iniciamos la escucha al entrar
    LaunchedEffect(pin) {
        viewModel.startObservingGame(pin)
    }

    // Reaccionamos si el juego empieza
    LaunchedEffect(uiState.gamePhase) {
        if (uiState.gamePhase == GamePhase.SHOWING_QUESTION) {
            onGameStarted(pin)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.sala_de_espera)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.btn_volver)
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(id = R.string.conectando_con_la_sala))
                }
                uiState.error != null -> {
                    Text(
                        "Error: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onBack) {
                        Text(stringResource(id = R.string.salir))
                    }
                }
                else -> {
                    Text(
                        stringResource(id = R.string.estas_dentro),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(id = R.string.esperando_profesor_inicie),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    LinearProgressIndicator(modifier = Modifier.width(200.dp))
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                stringResource(id = R.string.pin_de_la_sala),
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                pin,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}