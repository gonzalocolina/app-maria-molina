package com.example.mariamolina.ui.screens.kids

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
    val isAnonymous by viewModel.isAnonymous.collectAsState()
    val loginError by viewModel.loginError.collectAsState()

    // Estado del diálogo de login
    var showLoginDialog by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }

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
                if (isAnonymous) {
                    // CASO 1: Es anónimo -> Botón para Iniciar Sesión
                    OutlinedButton(
                        onClick = {
                            viewModel.clearError()
                            showLoginDialog = true
                        },
                        modifier = Modifier.fillMaxWidth().height(60.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Login, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Identifícate para añadir preguntas")
                    }
                } else if (canAddQuestions) {
                    // CASO 2: Logueado + Permiso -> Botón Normal
                    FilledTonalButton(
                        onClick = onAddQuestions,
                        modifier = Modifier.fillMaxWidth().height(60.dp)
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Añadir Preguntas", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    // CASO 3: Logueado + SIN Permiso -> Botón Candado (Formulario)
                    OutlinedButton(
                        onClick = { uriHandler.openUri(urlFormularioSolicitud) },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Lock, null)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Solicitar permiso de edición", fontWeight = FontWeight.Bold)
                            Text("Rellena el formulario", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }

    // --- DIÁLOGO DE LOGIN (Ahora vive aquí) ---
    if (showLoginDialog) {
        AlertDialog(
            onDismissRequest = { showLoginDialog = false },
            title = { Text("Acceso Editor") },
            text = {
                Column {
                    Text("Introduce tu correo para acceder al panel de edición.")
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Correo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (loginError != null) {
                        Text(loginError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.signInWithEmail(emailInput, passwordInput) {
                        showLoginDialog = false
                    }
                }) { Text("Entrar") }
            },
            dismissButton = {
                TextButton(onClick = { showLoginDialog = false }) { Text("Cancelar") }
            }
        )
    }

}
