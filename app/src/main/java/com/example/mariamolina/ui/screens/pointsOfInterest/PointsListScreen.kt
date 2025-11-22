package com.example.mariamolina.ui.screens.pointsOfInterest

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mariamolina.data.model.PuntoInteres
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.mariamolina.R
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.mariamolina.ui.viewmodel.PointsOfInterestViewModel

@Composable
fun PointsListScreen(
    puntos: List<PuntoInteres>,
    onPuntoClick: (String) -> Unit,
    viewModel: PointsOfInterestViewModel
) {
    val scrollState = rememberLazyListState()
    val showProgress = remember { mutableStateOf(true) }
    val lastScrollOffset = remember { mutableFloatStateOf(0f) }

    LaunchedEffect(scrollState.firstVisibleItemScrollOffset) {
        val currentOffset = scrollState.firstVisibleItemScrollOffset.toFloat()
        if (currentOffset > lastScrollOffset.floatValue) {
            // Scrolling down, hide progress bar
            showProgress.value = false
        } else if (currentOffset < lastScrollOffset.floatValue) {
            // Scrolling up, show progress bar
            showProgress.value = true
        }
        lastScrollOffset.floatValue = currentOffset
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Barra de progreso encima de la lista, animada
        AnimatedVisibility(
            visible = showProgress.value,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val visitedCount = viewModel.getVisitedCount(puntos.size)
            val totalCount = puntos.size
            val remainingCount = viewModel.getRemainingCount(totalCount)

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Visitados: $visitedCount / Total: $totalCount (Quedan: $remainingCount)",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { if (totalCount > 0) visitedCount.toFloat() / totalCount else 0f },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        LazyColumn(
            state = scrollState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(puntos) { punto ->
                PuntoInteresCard(
                    punto = punto,
                    onClick = { onPuntoClick(punto.id) },
                    isVisited = viewModel.isVisited(punto.id)
                )
            }
        }
    }
}

@Composable
fun PuntoInteresCard(
    punto: PuntoInteres,
    onClick: () -> Unit,
    isVisited: Boolean // Nuevo parámetro para indicar si el punto ha sido visitado
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            AsyncImage(
                model = punto.urlImagen,
                contentDescription = stringResource(id = punto.tituloResId),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop // Esto hace que la imagen "rellene" el espacio
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(id = punto.tituloResId),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Fila para Duración y Rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (punto.rating != null) {
                        InfoChip(
                            icono = Icons.Default.StarBorder,
                            texto = stringResource(R.string.fmt_rating_per_5, punto.rating)
                        )
                    }
                }

                // Indicador de visita
                if (isVisited) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "¡Visitado!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// Pequeño Composable para las "píldoras" de info (duración y rating)
@Composable
fun InfoChip(icono: ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
    }
}