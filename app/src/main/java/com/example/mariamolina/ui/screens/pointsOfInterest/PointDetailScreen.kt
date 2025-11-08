package com.example.mariamolina.ui.screens.poi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape // ¡CAMBIO! Importación añadida
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // ¡CAMBIO! Importación añadida
import androidx.compose.material.icons.filled.AccessTime
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mariamolina.data.model.PuntoInteres
import com.example.mariamolina.ui.theme.AppPrimaryBrown
import androidx.compose.ui.res.stringArrayResource
import com.example.mariamolina.R
import androidx.compose.ui.res.stringResource
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
// import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointDetailScreen(
    punto: PuntoInteres,
    onBackClick: () -> Unit // ¡CAMBIO! Añadimos el parámetro
) {
    Scaffold(
        bottomBar = {
            BottomAppBar(
                containerColor = AppPrimaryBrown,
            ) {
                Button(
                    onClick = { /* TODO: Lógica de Firebase para marcar visitado */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Marcar como visitado")
                }
            }
        }
    ) { innerPadding ->

        // ¡CAMBIO! Envolvemos todo en un Box para superponer el botón
        Box(modifier = Modifier.fillMaxSize()) {

            // Contenido principal con scroll (va al fondo)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    // ¡CAMBIO! Aplicamos el padding de la barra inferior
                    // solo al final del contenido que se desplaza
                    .padding(bottom = innerPadding.calculateBottomPadding())
            ) {

                // --- Imagen Superior ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    /* ... Tu código de AsyncImage ... */
                }

                // --- Contenido de Texto (se desplaza) ---
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    TituloSection(
                        tituloResId = punto.tituloResId,
                        duracionResId = punto.duracionResId,
                        rating = punto.rating
                    )
                    DescripcionSection(descripcionResId = punto.descripcionLargaResId)
                    InfoPracticaSection(
                        horariosResId = punto.horariosResId,
                        ubicacionResId = punto.ubicacionResId
                    )
                    ConsejosSection(consejosArrayResId = punto.consejosArrayResId)
                }
            } // Fin Column (scrollable)

            // --- Botón "Atrás" Flotante (va encima) ---
            IconButton(
                onClick = onBackClick, // Usa la función que recibimos
                modifier = Modifier
                    .padding(16.dp) // Padding desde la esquina
                    .align(Alignment.TopStart) // Alineado arriba a la izquierda
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape) // Fondo circular
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White // Icono blanco
                )
            }
        } // Fin Box (superposición)
    } // Fin Scaffold
}

// --- COMPOSABLES INTERNOS (Ahora leen los IDs de recurso) ---

@Composable
private fun TituloSection(@StringRes tituloResId: Int, @StringRes duracionResId: Int, rating: Double) {
    Column {
        Text(
            text = stringResource(id = tituloResId),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccessTime, contentDescription = stringResource(R.string.detalle_duracion), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(id = duracionResId), style = MaterialTheme.typography.bodyMedium)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, stringResource(R.string.detalle_rating), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("$rating/5", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

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
private fun InfoPracticaSection(@StringRes horariosResId: Int, @StringRes ubicacionResId: Int) {
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
                Icon(Icons.Default.Info, contentDescription = "Horarios", modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Horarios:", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(id = R.string.detalle_horarios), style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.LocationOn, contentDescription = "Ubicación", modifier = Modifier.size(16.dp))
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

@Composable
private fun ConsejosSection(@ArrayRes consejosArrayResId: Int) {
    // Leemos el array de strings desde los recursos
    val consejos = stringArrayResource(id = consejosArrayResId)
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(id = R.string.detalle_consejos),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            consejos.forEach { consejo ->
                Row(modifier = Modifier.padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.CheckCircleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(text = consejo, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}