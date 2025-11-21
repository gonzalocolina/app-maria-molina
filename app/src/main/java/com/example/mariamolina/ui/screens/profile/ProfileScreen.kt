package com.example.mariamolina.ui.screens.profile

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
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
import com.example.mariamolina.R
import com.example.mariamolina.ui.theme.AppPrimaryBrown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit
    // Hemos eliminado el ViewModel ya que no guardamos datos de usuario aquí
) {
    // --- ESTADO DEL IDIOMA ---
    val context = LocalContext.current
    val spanish = "\uD83C\uDDEA\uD83C\uDDF8  Español"
    val english = "\uD83C\uDDEC\uD83C\uDDE7  English"
    val german = "\uD83C\uDDE9\uD83C\uDDEA  Deutsch"
    val french = "\uD83C\uDDEB\uD83C\uDDF7  Français"
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- SECCIÓN 1: IDIOMA ---
            Text(
                text = stringResource(R.string.select_language_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AppPrimaryBrown)
            ) {
                Text(selectedLanguage)
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
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
            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(32.dp))

            // --- SECCIÓN 2: INFORMACIÓN (Sobre la App) ---

            Text(
                text = stringResource(R.string.about_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.about_description),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.Start)
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