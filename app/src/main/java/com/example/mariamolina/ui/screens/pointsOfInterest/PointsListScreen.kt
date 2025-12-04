package com.example.mariamolina.ui.screens.pointsOfInterest

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mariamolina.data.model.PuntoInteres
import androidx.compose.ui.res.stringResource
import com.example.mariamolina.R
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke

@Composable
fun PointsListScreen(
    puntos: List<PuntoInteres>,
    visitados: Set<String>,
    onPuntoClick: (String) -> Unit,
    header: (@Composable () -> Unit)? = null // header opcional para que el indicador de progreso scrollee con la lista
) {
    LazyVerticalGrid(
        // GridCells.Adaptive crea automáticamente columnas según el ancho disponible
        // Cada columna tendrá al menos 300dp de ancho, adaptándose a diferentes pantallas
        columns = GridCells.Adaptive(minSize = 300.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Si se proporciona un header, se añade como primer item de la lista (se desplazará junto a los elementos)
        if (header != null) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                header()
            }
        }

        items(puntos) { punto ->
            PuntoInteresCard(
                punto = punto,
                visitado = punto.id in visitados,
                onClick = { onPuntoClick(punto.id) }
            )
        }
    }
}

@Composable
fun PuntoInteresCard(
    punto: PuntoInteres,
    visitado: Boolean,
    onClick: () -> Unit
) {
    // Siempre en modo claro
    val isDark = false

    // Creamos un modifier que añade un borde claro solo en modo oscuro para simular una sombra "clara".
    val cardModifier = Modifier
        .fillMaxWidth()
        .then(
            if (isDark) Modifier.border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(16.dp)
            ) else Modifier
        )
        .clickable(onClick = onClick)

    Card(
        modifier = cardModifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            // mantenemos el color actual (podrías usar surface si quieres destacar más)
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        Column {
            // Imagen con bordes redondeados en la parte superior
            AsyncImage(
                model = punto.urlImagen,
                contentDescription = stringResource(id = punto.tituloResId),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(id = punto.tituloResId),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Indicador de visitado
                if (visitado) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = stringResource(R.string.visitado),
                            modifier = Modifier.size(18.dp),
                            tint = androidx.compose.ui.graphics.Color(0xFF4CAF50) // Verde
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.visitado),
                            style = MaterialTheme.typography.bodyMedium,
                            color = androidx.compose.ui.graphics.Color(0xFF4CAF50), // Verde
                            fontSize = 14.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}