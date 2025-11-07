package com.example.mariamolina.ui.screens.pointsOfInterest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mariamolina.data.model.PuntoInteres
import com.example.mariamolina.ui.theme.AppPrimaryBrown // Importa tu color marrón
// import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointDetailScreen(
    punto: PuntoInteres,
    // onBackClick se elimina, usaremos el botón 'Atrás' del sistema
) {
    // Usamos Scaffold para tener una barra inferior fija
    Scaffold(
        bottomBar = {
            BottomAppBar(
                containerColor = AppPrimaryBrown, // Tu color marrón
                //contentColor = Color.White
            ) {
                Button(
                    onClick = { /* TODO: Lógica de Firebase para marcar visitado */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary, // Botón marron
                        contentColor = MaterialTheme.colorScheme.primaryContainer // Texto dorado
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
        // Contenido principal con scroll
        Column(
            modifier = Modifier
                .padding(innerPadding) // Padding de la barra inferior
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            // --- Imagen Superior ---
            // Placeholder para la imagen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            )
            /*
            // CUANDO AÑADAS COIL:
            AsyncImage(
                model = punto.urlImagen,
                contentDescription = "Imagen de ${punto.titulo}",
                modifier = Modifier.fillMaxWidth().height(250.dp),
                contentScale = ContentScale.Crop
            )
            */

            // --- Contenido de Texto (se desplaza) ---
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp) // Espacio entre secciones
            ) {
                // --- Sección Título ---
                TituloSection(
                    titulo = punto.titulo,
                    duracion = punto.duracion,
                    rating = punto.rating
                )

                // --- Sección Descripción ---
                DescripcionSection(descripcion = punto.descripcionLarga)

                // --- Sección Información Práctica ---
                InfoPracticaSection(
                    horarios = punto.horarios,
                    ubicacion = punto.ubicacion
                )

                // --- Sección Consejos ---
                ConsejosSection(consejos = punto.consejos)
            }
        }
    }
}

// --- COMPOSABLES INTERNOS PARA ORGANIZAR ---

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