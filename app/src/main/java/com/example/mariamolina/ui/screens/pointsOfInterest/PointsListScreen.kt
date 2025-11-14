package com.example.mariamolina.ui.screens.pointsOfInterest

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mariamolina.data.model.PuntoInteres
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.mariamolina.R


// NOTA: Para cargar la imagen desde URL (punto.urlImagen) necesitarás
// la librería Coil. Por ahora, usamos un Box de color como placeholder.
// Para añadirla, ve a tu build.gradle.kts y añade:
// implementation("io.coil-kt:coil-compose:2.6.0")
// import coil.compose.AsyncImage

@Composable
fun PointsListScreen(
    puntos: List<PuntoInteres>,
    onPuntoClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(puntos) { punto ->
            PuntoInteresCard(
                punto = punto,
                onClick = { onPuntoClick(punto.id) }
            )
        }
    }
}

@Composable
fun PuntoInteresCard(
    punto: PuntoInteres,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            // Placeholder para la imagen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer)
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