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
                        titulo = punto.titulo,
                        duracion = punto.duracion,
                        rating = punto.rating
                    )
                    DescripcionSection(descripcion = punto.descripcionLarga)
                    InfoPracticaSection(
                        horarios = punto.horarios,
                        ubicacion = punto.ubicacion
                    )
                    ConsejosSection(consejos = punto.consejos)
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

// --- (El resto de tus Composables privados: TituloSection, DescripcionSection, etc.
//      no necesitan cambios y se quedan igual) ---

@Composable
private fun TituloSection(titulo: String, duracion: String, rating: Double) {
    Column {
        Text(
            text = titulo,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccessTime, contentDescription = "Duración", modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(duracion, style = MaterialTheme.typography.bodyMedium)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = "Rating", modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("$rating/5", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun DescripcionSection(descripcion: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Descripción",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun InfoPracticaSection(horarios: String, ubicacion: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Información práctica",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = "Horarios", modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Horarios:", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(8.dp))
                Text(horarios, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.LocationOn, contentDescription = "Ubicación", modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Ubicación:", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(8.dp))
                Text(ubicacion, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { /* TODO: Abrir mapa */ }) {
                Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Ver en mapa")
            }
        }
    }
}

@Composable
private fun ConsejosSection(consejos: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Consejos de visita",
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