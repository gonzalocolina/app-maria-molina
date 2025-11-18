package com.example.mariamolina.ui.screens.kids

// Esta pantalla es la versión renombrada de la anterior `KidsMenuScreen`.
// Se mantiene la funcionalidad: elegir dificultad y empezar el quiz, además de navegar al ranking.

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mariamolina.R
import com.example.mariamolina.data.model.Dificultad
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

@Composable
fun KidsQuizMenuScreen(
    onBack: () -> Unit,
    onStartQuiz: (Dificultad) -> Unit,
    onNavigateToRanking: () -> Unit
) {
    var dificultadSeleccionada by remember { mutableStateOf(Dificultad.FACIL) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Botón superior izquierdo para volver al menú principal (EntryScreen)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.cd_back))
            }
        }

        Text(
            text = stringResource(id = R.string.kids_menu_titulo),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.kids_menu_descripcion),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        Text("Elige dificultad:", style = MaterialTheme.typography.titleMedium)
        Row(
            Modifier.fillMaxWidth(),
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

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onStartQuiz(dificultadSeleccionada) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(stringResource(id = R.string.kids_iniciar_quiz))
        }

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onNavigateToRanking,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(id = R.string.kids_quiz_ranking))
        }
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
