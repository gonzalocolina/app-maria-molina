package com.example.mariamolina.ui.screens.kids

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mariamolina.data.model.Dificultad
import com.example.mariamolina.ui.viewmodel.AddQuestionViewModel

/**
 * Pantalla para añadir nuevas preguntas al cuestionario.
 * Permite introducir pregunta, opciones, marcar la correcta y seleccionar dificultad.
 * Las preguntas se traducen automáticamente a 4 idiomas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddQuestionScreen(
    onBack: () -> Unit,
    viewModel: AddQuestionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Añadir Pregunta") },
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(0.dp)) // Para el spacedBy inicial
            
            // Aviso de traducción automática
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Translate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Traducción automática",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Escribe en español. Se traducirá automáticamente a inglés, francés y alemán.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Botón para descargar modelos si no están disponibles
            if (!uiState.modelsDownloaded) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Se necesitan los modelos de traducción",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Descarga los modelos para poder traducir las preguntas. Requiere conexión WiFi.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.downloadModels() },
                            enabled = !uiState.isDownloadingModels
                        ) {
                            if (uiState.isDownloadingModels) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Descargando...")
                            } else {
                                Icon(Icons.Default.Download, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Descargar modelos")
                            }
                        }
                    }
                }
            }

            // Campo de la pregunta
            OutlinedTextField(
                value = uiState.pregunta,
                onValueChange = { viewModel.updatePregunta(it) },
                label = { Text("Pregunta (en español)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                enabled = !uiState.isLoading
            )

            // Selector de dificultad
            Text(
                "Dificultad:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Dificultad.entries.forEach { dificultad ->
                    FilterChip(
                        selected = uiState.dificultad == dificultad,
                        onClick = { viewModel.setDificultad(dificultad) },
                        label = { Text(dificultad.name) },
                        enabled = !uiState.isLoading,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            // Opciones de respuesta
            Text(
                "Opciones de respuesta:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Toca el círculo para marcar la respuesta correcta",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            uiState.opciones.forEachIndexed { index, opcion ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Radio button para marcar como correcta
                    IconButton(
                        onClick = { viewModel.setOpcionCorrecta(index) },
                        enabled = !uiState.isLoading
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = if (uiState.opcionCorrectaIndex == index)
                                "Respuesta correcta" else "Marcar como correcta",
                            tint = if (uiState.opcionCorrectaIndex == index)
                                Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }

                    // Campo de texto para la opción
                    OutlinedTextField(
                        value = opcion,
                        onValueChange = { viewModel.updateOpcion(index, it) },
                        label = { Text("Opción ${index + 1}") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        enabled = !uiState.isLoading,
                        colors = if (uiState.opcionCorrectaIndex == index) {
                            OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4CAF50),
                                unfocusedBorderColor = Color(0xFF4CAF50).copy(alpha = 0.5f)
                            )
                        } else {
                            OutlinedTextFieldDefaults.colors()
                        }
                    )

                    // Botón eliminar (solo si hay más de 2 opciones)
                    if (uiState.opciones.size > 2) {
                        IconButton(
                            onClick = { viewModel.removeOpcion(index) },
                            enabled = !uiState.isLoading
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar opción",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Botón añadir opción
            if (uiState.opciones.size < 6) {
                TextButton(
                    onClick = { viewModel.addOpcion() },
                    enabled = !uiState.isLoading
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Añadir otra opción")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón guardar
            Button(
                onClick = { viewModel.saveQuestion() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isLoading && uiState.modelsDownloaded
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(uiState.translationProgress.ifEmpty { "Guardando..." })
                } else {
                    Icon(Icons.Default.Translate, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar y Traducir")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Snackbar para mensajes de error
        if (uiState.error != null) {
            Snackbar(
                modifier = Modifier
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearMessages() }) {
                        Text("OK")
                    }
                },
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ) {
                Text(uiState.error!!)
            }
        }

        // Snackbar para mensajes de éxito
        if (uiState.successMessage != null) {
            Snackbar(
                modifier = Modifier
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearMessages() }) {
                        Text("OK")
                    }
                },
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White
            ) {
                Text(uiState.successMessage!!)
            }
        }
    }
}

