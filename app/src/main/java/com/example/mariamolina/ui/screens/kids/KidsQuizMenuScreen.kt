package com.example.mariamolina.ui.screens.kids

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mariamolina.R
import com.example.mariamolina.data.model.Dificultad

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KidsQuizMenuScreen(
    onBack: () -> Unit,
    onStartQuiz: (Dificultad) -> Unit,
    onNavigateToRanking: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onJoinGame: () -> Unit
) {
    var dificultadSeleccionada by remember { mutableStateOf(Dificultad.FACIL) }

    // --- ESTADOS PARA EL DIÁLOGO DE SEGURIDAD ---
    var showAdminDialog by remember { mutableStateOf(false) }
    var passwordInput by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.btn_volver))
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.kids_menu_titulo),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(id = R.string.kids_menu_descripcion),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(stringResource(id = R.string.elige_dificultad), style = MaterialTheme.typography.titleMedium)

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DificultadChip(
                        texto = stringResource(id = R.string.kids_dificultad_facil),
                        seleccionado = dificultadSeleccionada == Dificultad.FACIL,
                        onClick = { dificultadSeleccionada = Dificultad.FACIL }
                    )
                    DificultadChip(
                        texto = stringResource(id = R.string.kids_dificultad_media),
                        seleccionado = dificultadSeleccionada == Dificultad.MEDIA,
                        onClick = { dificultadSeleccionada = Dificultad.MEDIA }
                    )
                    DificultadChip(
                        texto = stringResource(id = R.string.kids_dificultad_dificil),
                        seleccionado = dificultadSeleccionada == Dificultad.DIFICIL,
                        onClick = { dificultadSeleccionada = Dificultad.DIFICIL }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Botón Iniciar Quiz (Solitario)
                Button(
                    onClick = { onStartQuiz(dificultadSeleccionada) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(stringResource(id = R.string.kids_iniciar_quiz))
                }

                // --- ¡CAMBIO! Botón UNIRSE A PARTIDA ---
                FilledTonalButton(
                    onClick = onJoinGame,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(id = R.string.unirse_a_partida_con_pin))
                }

                OutlinedButton(
                    onClick = onNavigateToRanking,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(id = R.string.kids_quiz_ranking))
                }

                // Botón "Acceso Profesor" protegido
                TextButton(
                    onClick = {
                        // Al pulsar, abrimos el diálogo y reseteamos campos
                        showAdminDialog = true
                        passwordInput = ""
                        passwordError = false
                    }
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(id = R.string.acceso_profesor), color = MaterialTheme.colorScheme.secondary)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // --- DIÁLOGO DE CONTRASEÑA ---
        if (showAdminDialog) {
            AlertDialog(
                onDismissRequest = { showAdminDialog = false },
                title = { Text(stringResource(id = R.string.zona_de_profesores)) },
                text = {
                    Column {
                        Text(stringResource(id = R.string.introduce_contrasena_panel))
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = {
                                passwordInput = it
                                passwordError = false
                            },
                            label = { Text(stringResource(id = R.string.contrasena)) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            isError = passwordError,
                            supportingText = {
                                if (passwordError) Text(stringResource(id = R.string.contrasena_incorrecta))
                            }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // --- AQUÍ DEFINES TU CONTRASEÑA ---
                            if (passwordInput == "1234") {
                                showAdminDialog = false
                                onNavigateToAdmin() // Navegamos solo si es correcta
                            } else {
                                passwordError = true
                            }
                        }
                    ) {
                        Text(stringResource(id = R.string.entrar))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAdminDialog = false }) {
                        Text(stringResource(id = R.string.cancelar))
                    }
                }
            )
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