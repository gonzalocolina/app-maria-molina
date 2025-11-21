package com.example.mariamolina.ui.screens.profile

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mariamolina.R
import com.example.mariamolina.ui.viewmodel.ProfileViewModel
import com.example.mariamolina.ui.theme.AppPrimaryBrown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    // --- ESTADO DEL USUARIO (Firebase) ---
    val uiState by viewModel.uiState.collectAsState()

    // --- ESTADO DEL IDIOMA ---
    val context = LocalContext.current
    val spanish = "Español"
    val english = "English"
    val german = "Deutsch"
    val french = "Français"
    val languages = listOf(spanish, english, german, french)

    val currentLanguageCode = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getString("language", "es") ?: "es"
    val defaultLanguage = when (currentLanguageCode) {
        "es" -> spanish
        "en" -> english
        "de" -> german
        "fr" -> french
        else -> spanish
    }

    var selectedLanguage by remember { mutableStateOf(defaultLanguage) }
    var showDialog by remember { mutableStateOf(false) }

    // --- ESTADO DEL TAMAÑO DE LETRA ---
    val normal = "Normal"
    val grande = "Grande"
    val muyGrande = "Muy grande"
    val fontSizes = listOf(normal, grande, muyGrande)

    val currentFontSize = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getString("font_size", "normal") ?: "normal"
    val defaultFontSize = when (currentFontSize) {
        "normal" -> normal
        "large" -> grande
        "very_large" -> muyGrande
        else -> normal
    }

    var selectedFontSize by remember { mutableStateOf(defaultFontSize) }
    var showFontSizeDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button_description)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- SECCIÓN 1: IDIOMA ---
            Text(
                text = stringResource(R.string.select_language_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AppPrimaryBrown)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(selectedLanguage, modifier = Modifier.align(Alignment.Center))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.align(Alignment.CenterEnd))
                }
            }

            if (showDialog) {
                BasicAlertDialog(
                    onDismissRequest = { showDialog = false }
                ) {
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.select_language_title),
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            languages.forEach { language ->
                                TextButton(
                                    onClick = {
                                        selectedLanguage = language
                                        showDialog = false
                                        val languageCode = when (language) {
                                            spanish -> "es"
                                            english -> "en"
                                            german -> "de"
                                            french -> "fr"
                                            else -> "es"
                                        }
                                        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                                        prefs.edit { putString("language", languageCode) }
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.language_changed_toast, language),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        (context as? androidx.activity.ComponentActivity)?.recreate()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(language)
                                }
                            }
                        }
                    }
                }
            }

            // Separador
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // --- SECCIÓN 2: DATOS DE USUARIO ---

            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = AppPrimaryBrown
            )

            // ¡CAMBIO! Mostramos el Nickname real si existe, o el título genérico
            Text(
                text = uiState.nickname.ifBlank { stringResource(id = R.string.profile_user_section_title) },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                OutlinedTextField(
                    value = uiState.nickname,
                    onValueChange = { viewModel.updateNickname(it) },
                    label = { Text(stringResource(id = R.string.profile_nickname_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.saveProfile() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppPrimaryBrown)
                ) {
                    Text(stringResource(id = R.string.profile_save_button))
                }

                if (uiState.isSaved) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4CAF50))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(id = R.string.profile_saved_success), color = Color(0xFF4CAF50))
                    }
                }

                if (uiState.error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(id = R.string.profile_error_message, uiState.error!!),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Separador
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // --- SECCIÓN 2.5: TAMAÑO DE LETRA ---
            Text(
                text = "Tamaño de letra",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { showFontSizeDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AppPrimaryBrown)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(selectedFontSize, modifier = Modifier.align(Alignment.Center))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.align(Alignment.CenterEnd))
                }
            }

            if (showFontSizeDialog) {
                BasicAlertDialog(
                    onDismissRequest = { showFontSizeDialog = false }
                ) {
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Seleccionar tamaño de letra",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            fontSizes.forEach { size ->
                                TextButton(
                                    onClick = {
                                        selectedFontSize = size
                                        showFontSizeDialog = false
                                        val sizeCode = when (size) {
                                            normal -> "normal"
                                            grande -> "large"
                                            muyGrande -> "very_large"
                                            else -> "normal"
                                        }
                                        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                                        prefs.edit { putString("font_size", sizeCode) }
                                        Toast.makeText(
                                            context,
                                            "Tamaño de letra cambiado a $size",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        (context as? androidx.activity.ComponentActivity)?.recreate()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(size)
                                }
                            }
                        }
                    }
                }
            }

            // Separador
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // --- SECCIÓN 3: INFORMACIÓN ---

            Text(
                text = stringResource(R.string.about_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(R.string.about_description),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(onBackClick = {})
}