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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mariamolina.data.model.Dificultad
import com.example.mariamolina.ui.viewmodel.AddQuestionViewModel
import com.example.mariamolina.R

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

    // Strings localizados usados en la pantalla
    val title = stringResource(R.string.add_question_title)
    val translateTitle = stringResource(R.string.translate_auto_title)
    val translateSubtitle = stringResource(R.string.translate_auto_subtitle)
    val modelsNeededTitle = stringResource(R.string.models_needed_title)
    val modelsNeededSubtitle = stringResource(R.string.models_needed_subtitle)
    val downloadingText = stringResource(R.string.downloading)
    val downloadModelsText = stringResource(R.string.download_models)
    val questionLabel = stringResource(R.string.question_label)
    val difficultyLabel = stringResource(R.string.difficulty_label)
    val answerOptionsTitle = stringResource(R.string.answer_options_title)
    val markCorrectNote = stringResource(R.string.mark_correct_note)
    val addOptionText = stringResource(R.string.add_option)
    val saveAndTranslateText = stringResource(R.string.save_and_translate)
    val savingText = stringResource(R.string.saving)
    val okText = stringResource(R.string.ok)
    val contentBack = stringResource(R.string.btn_volver)
    val cdAnswerCorrect = stringResource(R.string.cd_answer_correct)
    val cdMarkAsCorrect = stringResource(R.string.cd_mark_as_correct)
    val deleteOptionDesc = stringResource(R.string.delete_option)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = contentBack
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
                            translateTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            translateSubtitle,
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
                            modelsNeededTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            modelsNeededSubtitle,
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
                                Text(downloadingText)
                            } else {
                                Icon(Icons.Default.Download, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(downloadModelsText)
                            }
                        }
                    }
                }
            }

            // Campo de la pregunta
            OutlinedTextField(
                value = uiState.pregunta,
                onValueChange = { viewModel.updatePregunta(it) },
                label = { Text(questionLabel) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                enabled = !uiState.isLoading
            )

            // Selector de dificultad
            Text(
                difficultyLabel,
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
                answerOptionsTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                markCorrectNote,
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
                                cdAnswerCorrect else cdMarkAsCorrect,
                            tint = if (uiState.opcionCorrectaIndex == index)
                                Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }

                    // Campo de texto para la opción
                    OutlinedTextField(
                        value = opcion,
                        onValueChange = { viewModel.updateOpcion(index, it) },
                        label = { Text(stringResource(R.string.option_label, index + 1)) },
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
                                contentDescription = deleteOptionDesc,
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
                    Text(addOptionText)
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
                    Text(uiState.translationProgress.ifEmpty { savingText })
                } else {
                    Icon(Icons.Default.Translate, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(saveAndTranslateText)
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
                        Text(okText)
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
                        Text(okText)
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

