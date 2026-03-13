package com.edunova.mariamolina.ui.screens.pointsOfInterest

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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.edunova.mariamolina.data.model.PuntoInteres
import com.edunova.mariamolina.data.model.SubPuntoInteres
import com.edunova.mariamolina.R
import com.edunova.mariamolina.ui.theme.AppPrimaryBrown
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointDetailScreen(
    punto: PuntoInteres,
    onBackClick: () -> Unit,
    onOpenMapClick: () -> Unit,
    onOpenSubPointMapClick: (SubPuntoInteres) -> Unit,
    onMarkAsVisited: () -> Unit,
    isVisited: Boolean
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
                    .clickable { onMarkAsVisited() }
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
                        tint = if (isVisited) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primaryContainer // Verde si visitado
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(
                        stringResource(id = if (isVisited) R.string.detalle_visitado else R.string.detalle_marcar_visitado),
                        color = if (isVisited) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primaryContainer // Verde si visitado
                    )
                }
            }
        }
    ) { innerPadding ->
        // Aplicamos innerPadding al Box para que el contenido tenga en cuenta el bottomBar y otros inset
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    // ...existing code...
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
                    // ¡CAMBIO! Ya no pasamos duracionResId ni rating
                    TituloSection(
                        tituloResId = punto.tituloResId
                    )

                    // Esta sigue igual, descripcionLargaResId no es nullable
                    DescripcionSection(descripcionResId = punto.descripcionLargaResId)

                    // ¡CAMBIO! Comprobamos si hay datos antes de llamar
                    if (punto.horariosResId != null && punto.ubicacionResId != null) {
                        InfoPracticaSection(
                            horariosResId = punto.horariosResId,
                            ubicacionResId = punto.ubicacionResId,
                            onOpenMapClick = onOpenMapClick
                        )
                    }

                    // ¡CAMBIO! Comprobamos si hay subpuntos
                    if (punto.subpuntos.isNotEmpty()) {
                        SubPuntosSection(
                            subpuntos = punto.subpuntos,
                            onOpenSubPointMapClick = onOpenSubPointMapClick
                        )
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
    @StringRes tituloResId: Int
) {
    Text(
        text = stringResource(id = tituloResId),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
    )
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
    @StringRes ubicacionResId: Int, // ¡CAMBIO! Ya no es nullable aquí
    onOpenMapClick: () -> Unit
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
            TextButton(onClick = onOpenMapClick ) {
                Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(id = R.string.detalle_ver_en_mapa))
            }
        }
    }
}

// --- NUEVA SECCIÓN! ---
@Composable
private fun SubPuntosSection(
    subpuntos: List<SubPuntoInteres>,
    onOpenSubPointMapClick: (SubPuntoInteres) -> Unit
) {
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
                SubPuntoItem(
                    subpunto = subpunto,
                    onOpenSubPointMapClick = onOpenSubPointMapClick
                )
                if (index < subpuntos.lastIndex) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                }
            }
        }
    }
}

@Composable
private fun SubPuntoItem(
    subpunto: SubPuntoInteres,
    onOpenSubPointMapClick: (SubPuntoInteres) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Título
        Text(
            text = stringResource(id = subpunto.nombreResId),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

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

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = { onOpenSubPointMapClick(subpunto) }) {
            Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(id = R.string.detalle_ver_en_mapa))
        }
    }
}