package com.example.mariamolina.ui.screens.kids

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.mariamolina.ui.viewmodel.TeacherMenuViewModel

/**
 * Pantalla del menú del profesor con dos opciones:
 * 1. Crear sala (funcionalidad principal)
 * 2. Añadir preguntas (funcionalidad secundaria)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherMenuScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onCreateRoom: () -> Unit,
    onAddQuestions: () -> Unit,
    viewModel: TeacherMenuViewModel = hiltViewModel()
) {

    val uriHandler = LocalUriHandler.current
    val urlFormularioSolicitud = "https://docs.google.com/forms/d/e/1FAIpQLSeLhc10MoLr6Ze5FM16zIlVPmbgMyyqKk5lvKit0SSNMCYNQg/viewform?usp=dialog"

    val canAddQuestions by viewModel.canAddQuestions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel del Profesor") },
                navigationIcon = {
                    IconButton(onClick = {
                        onLogout()
                        onBack()
                    }) {
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Título
            Text(
                text = "¿Qué deseas hacer?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Selecciona una opción para continuar",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Botón principal: Crear Sala
            Button(
                onClick = onCreateRoom,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    Icons.Default.Groups,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        "Crear Sala",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Inicia una partida multijugador",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón secundario:
            if (isLoading) {
                // Mientras carga, mostramos un indicador pequeño
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            } else {
                if (canAddQuestions) {
                    // CASO A: TIENE PERMISO -> Botón normal que lleva a la pantalla de añadir
                    FilledTonalButton(
                        onClick = onAddQuestions,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Añadir Preguntas",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Las preguntas se traducirán automáticamente\na 4 idiomas (ES, EN, FR, DE)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )

                } else {
                    // CASO B: NO TIENE PERMISO -> Botón de "Solicitar Acceso" (Abre formulario)
                    OutlinedButton(
                        onClick = {
                            uriHandler.openUri(urlFormularioSolicitud)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            // Usamos un color rojizo o secundario para indicar que está "bloqueado"
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Default.Lock, // Icono de candado
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Solicitar acceso para añadir preguntas",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Requiere aprobación del administrador",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
        }
    }
}}
