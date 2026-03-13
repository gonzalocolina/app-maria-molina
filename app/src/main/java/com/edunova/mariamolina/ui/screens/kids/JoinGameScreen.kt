package com.edunova.mariamolina.ui.screens.kids

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.edunova.mariamolina.R
import com.edunova.mariamolina.ui.viewmodel.StudentGameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinGameScreen(
    onBack: () -> Unit,
    onJoinSuccess: (String) -> Unit, // Pasamos el PIN para saber a qué sala ir
    viewModel: StudentGameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var nickname by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }

    // Reaccionar al estado de éxito
    LaunchedEffect(uiState.joinSuccess) {
        if (uiState.joinSuccess) {
            onJoinSuccess(pin) // Navegamos a la sala de espera
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.unirse_a_partida)) },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(id = R.string.join_game_enter_data),
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = nickname,
                onValueChange = { if (it.length <= 20) nickname = it },
                label = { Text(stringResource(id = R.string.tu_nombre)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = { Text("${nickname.length}/20") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = pin,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() }) {
                        pin = newValue
                    }
                },
                label = { Text(stringResource(id = R.string.pin_de_la_partida)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = { viewModel.joinGame(pin, nickname) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = nickname.isNotBlank() && pin.isNotBlank()
                ) {
                    Text(stringResource(id = R.string.entrar_button))
                }
            }

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}