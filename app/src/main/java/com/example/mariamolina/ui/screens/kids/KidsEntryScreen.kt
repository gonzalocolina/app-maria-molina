package com.example.mariamolina.ui.screens.kids

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mariamolina.R
import com.example.mariamolina.data.model.Dificultad
import com.example.mariamolina.ui.viewmodel.KidsSessionViewModel

/**
 * Pantalla de entrada unificada para la sección infantil.
 * Muestra: introducción + botón diapositivas + selector dificultad + iniciar quiz + unirse + acceso profesor
 */
@Composable
fun KidsEntryScreen(
    onNavigateToSlides: () -> Unit,
    onStartQuiz: (Dificultad) -> Unit,
    onNavigateToAdmin: () -> Unit,
    onJoinGame: () -> Unit,
    onReconnectToGame: ((String) -> Unit)? = null,
    viewModel: KidsSessionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Estado para selector de dificultad
    var dificultadSeleccionada by remember { mutableStateOf(Dificultad.FACIL) }

    // Reconectar automáticamente si hay sesión activa
    LaunchedEffect(uiState.shouldNavigateTo) {
        uiState.shouldNavigateTo?.let { route ->
            onReconnectToGame?.invoke(route)
            viewModel.onNavigated()
        }
    }

    // Mientras verifica la sesión, mostrar loading
    if (uiState.isChecking) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Responsive: detectar ancho de pantalla
    val windowInfo = LocalWindowInfo.current
    val containerSize: IntSize = windowInfo.containerSize
    val screenWidthDp: Dp = with(LocalDensity.current) { containerSize.width.toDp() }
    val isWideScreen = screenWidthDp >= 600.dp

    val buttonShape = RoundedCornerShape(12.dp)
    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // --- SECCIÓN 1: Introducción ---
        Text(
            text = stringResource(id = R.string.kids_entry_titulo),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Text(
            text = stringResource(id = R.string.kids_entry_descripcion),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // --- SECCIÓN 2: Botón Ver Diapositivas ---
        Button(
            onClick = onNavigateToSlides,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = buttonShape,
            colors = buttonColors
        ) {
            Icon(imageVector = Icons.Default.Slideshow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(id = R.string.kids_ver_diapositivas))
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // --- SECCIÓN 3: Quiz ---
        Text(
            text = stringResource(id = R.string.elige_dificultad),
            style = MaterialTheme.typography.titleMedium
        )

        // Selector de dificultad - Responsive
        if (isWideScreen) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Iniciar Quiz
        Button(
            onClick = {
                onStartQuiz(dificultadSeleccionada)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(stringResource(id = R.string.kids_iniciar_quiz))
        }

        // Botón Unirse a Partida
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

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Acceso Profesor
        TextButton(
            onClick = {
                viewModel.setTeacherAuthenticated()
                onNavigateToAdmin()
            }
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(id = R.string.acceso_profesor), color = MaterialTheme.colorScheme.secondary)
        }

        Spacer(modifier = Modifier.height(32.dp))
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