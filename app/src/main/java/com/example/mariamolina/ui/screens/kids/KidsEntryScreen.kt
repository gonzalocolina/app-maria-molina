package com.example.mariamolina.ui.screens.kids

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mariamolina.R
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mariamolina.ui.viewmodel.KidsSessionViewModel

@Composable
fun KidsEntryScreen(
    onNavigateToSlides: () -> Unit,
    onNavigateToQuizzes: () -> Unit,
    onReconnectToGame: ((String) -> Unit)? = null,  // Callback para reconectar a partida
    viewModel: KidsSessionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.kids_entry_titulo),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(id = R.string.kids_entry_descripcion),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Responsive button group: en pantallas anchas mostramos los botones en fila
        // Usamos LocalWindowInfo + LocalDensity para obtener el ancho de la ventana en dp (mejor práctica)
        val windowInfo = LocalWindowInfo.current
        val containerSize: IntSize = windowInfo.containerSize
        val screenWidthDp: Dp = with(LocalDensity.current) { containerSize.width.toDp() }
        val wideThreshold = 600.dp

        val buttonShape = RoundedCornerShape(12.dp)
        val buttonColors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )

        if (screenWidthDp >= wideThreshold) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                Button(
                    onClick = onNavigateToSlides,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = buttonShape,
                    colors = buttonColors
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Slideshow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(id = R.string.kids_ver_diapositivas))
                    }
                }

                Button(
                    onClick = onNavigateToQuizzes,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = buttonShape,
                    colors = buttonColors
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Help,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(id = R.string.kids_opcion_cuestionarios))
                    }
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Center) {
                Button(
                    onClick = onNavigateToSlides,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = buttonShape,
                    colors = buttonColors
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Slideshow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(id = R.string.kids_ver_diapositivas))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onNavigateToQuizzes,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = buttonShape,
                    colors = buttonColors
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Help,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(id = R.string.kids_opcion_cuestionarios))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}