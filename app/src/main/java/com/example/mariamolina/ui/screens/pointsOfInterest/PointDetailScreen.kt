package com.example.mariamolina.ui.screens.poi

import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
// import androidx.compose.material.icons.filled.AccessTime // Eliminado
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mariamolina.data.model.PuntoInteres
import com.example.mariamolina.data.model.SubPuntoInteres
import com.example.mariamolina.R
import com.example.mariamolina.ui.theme.AppPrimaryBrown
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointDetailScreen(
    punto: PuntoInteres,
    onBackClick: () -> Unit
) {
    Scaffold(
        bottomBar = {
            Surface(
                color = AppPrimaryBrown,
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* TODO: Lógica de Firebase para marcar visitado */ }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                        tint = MaterialTheme.colorScheme.primaryContainer
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(
                        stringResource(id = R.string.detalle_marcar_visitado),
                        color = MaterialTheme.colorScheme.primaryContainer
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                // ... Imagen ...
                AsyncImage(
                    model = punto.urlImagen,
                    contentDescription = stringResource(id = punto.tituloResId),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop
                )

                // --- Contenido de Texto ---
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // ¡CAMBIO! Ya no pasamos duracionResId
                    TituloSection(
                        tituloResId = punto.tituloResId,
                        rating = punto.rating // Pasamos el rating nullable
                    )

                    // Esta sigue igual, descripcionLargaResId no es nullable
                    DescripcionSection(descripcionResId = punto.descripcionLargaResId)

                    // ¡CAMBIO! Comprobamos si hay datos antes de llamar
                    if (punto.horariosResId != null && punto.ubicacionResId != null) {
                        InfoPracticaSection(
                            horariosResId = punto.horariosResId,
                            ubicacionResId = punto.ubicacionResId
                        )
                    }

                    // ¡CAMBIO! Comprobamos si hay subpuntos
                    if (punto.subpuntos.isNotEmpty()) {
                        SubPuntosSection(subpuntos = punto.subpuntos)
                    }

                    // ¡CAMBIO! La sección Consejos ya no existe
                }
            }

            // ... (Botón "Atrás" flotante) ...
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart)
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_volver),
                    tint = Color.White
                )
            }
        }
    }
}

// --- COMPOSABLES INTERNOS (Actualizados) ---

@Composable
private fun TituloSection(
    @StringRes tituloResId: Int,
    rating: Double? // ¡CAMBIO! Acepta nullable
) {
    Column {
        Text(
            text = stringResource(id = tituloResId),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // ¡CAMBIO! La duración se ha ido

            // ¡CAMBIO! Mostramos el rating solo si no es nulo
            if (rating != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = stringResource(R.string.cd_rating), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.fmt_rating_per_5, rating), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

// ... (DescripcionSection no cambia) ...
@Composable
private fun DescripcionSection(@StringRes descripcionResId: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(id = R.string.detalle_descripcion),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = descripcionResId),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun InfoPracticaSection(
    @StringRes horariosResId: Int, // ¡CAMBIO! Ya no es nullable aquí
    @StringRes ubicacionResId: Int // ¡CAMBIO! Ya no es nullable aquí
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(id = R.string.detalle_info_practica),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = stringResource(R.string.cd_horarios), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(id = R.string.detalle_horarios), fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(id = horariosResId), style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.LocationOn, contentDescription = stringResource(R.string.cd_ubicacion), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(id = R.string.detalle_ubicacion), fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(id = ubicacionResId), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { /* TODO: Abrir mapa */ }) {
                Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(id = R.string.detalle_ver_en_mapa))
            }
        }
    }
}

// --- NUEVA SECCIÓN! ---
@Composable
private fun SubPuntosSection(subpuntos: List<SubPuntoInteres>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(id = R.string.detalle_subpuntos_titulo), // Necesitarás añadir esto a strings.xml
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Iteramos sobre los subpuntos
            subpuntos.forEachIndexed { index, subpunto ->
                SubPuntoItem(subpunto = subpunto)
                if (index < subpuntos.lastIndex) {
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                }
            }
        }
    }
}

@Composable
private fun SubPuntoItem(subpunto: SubPuntoInteres) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Título y Rating
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = subpunto.nombreResId),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (subpunto.rating != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = stringResource(R.string.cd_rating), modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.fmt_rating_per_5, subpunto.rating), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Horarios
        if (subpunto.horariosResId != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = stringResource(R.string.cd_horarios), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(id = R.string.detalle_horarios), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(id = subpunto.horariosResId), style = MaterialTheme.typography.bodySmall)
            }
        }

        // Ubicación
        if (subpunto.ubicacionResId != null) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.LocationOn, contentDescription = stringResource(R.string.cd_ubicacion), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(id = R.string.detalle_ubicacion), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(id = subpunto.ubicacionResId), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            }
        }
    }
}