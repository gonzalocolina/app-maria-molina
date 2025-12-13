package com.example.mariamolina.ui.screens.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.Policy
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
) {
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
    val normal = stringResource(R.string.font_size_normal)
    val grande = stringResource(R.string.font_size_large)
    val muyGrande = stringResource(R.string.font_size_very_large)
    val fontSizes = listOf(normal, grande, muyGrande)

    val currentFontSize = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getString("font_size", "normal") ?: "normal"
    val defaultFontSize = when (currentFontSize) {
        "normal" -> normal
        "large" -> grande
        "very_large" -> muyGrande
        else -> normal
    }

    var selectedFontSize by remember { mutableStateOf(defaultFontSize) }

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
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
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
                // Pre-cargar los mensajes de toast para cada idioma
                val toastMessages = mapOf(
                    spanish to stringResource(R.string.language_changed_toast, spanish),
                    english to stringResource(R.string.language_changed_toast, english),
                    german to stringResource(R.string.language_changed_toast, german),
                    french to stringResource(R.string.language_changed_toast, french)
                )

                BasicAlertDialog(
                    onDismissRequest = { showDialog = false }
                ) {
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.select_language_title),
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = Color.Black
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
                                            toastMessages[language] ?: "",
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

            // --- SECCIÓN 2: TAMAÑO DE LETRA ---
            Text(
                text = stringResource(R.string.font_size_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )

            // Pre-cargar los mensajes de toast para cada tamaño
            val fontSizeToastMessages = mapOf(
                normal to stringResource(R.string.font_size_changed_toast, normal),
                grande to stringResource(R.string.font_size_changed_toast, grande),
                muyGrande to stringResource(R.string.font_size_changed_toast, muyGrande)
            )

            Column {
                fontSizes.forEach { size ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = selectedFontSize == size,
                            onClick = {
                                selectedFontSize = size
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
                                    fontSizeToastMessages[size] ?: "",
                                    Toast.LENGTH_SHORT
                                ).show()
                                (context as? androidx.activity.ComponentActivity)?.recreate()
                            }
                        )
                        Text(size)
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
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )

            Text(
                text = stringResource(R.string.about_description),
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- SECCIÓN 4: POLÍTICA DE PRIVACIDAD ---
            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://gonzalocolina.github.io/mariamolina/"))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Icon(
                    Icons.Outlined.Policy,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.privacy_policy_button))
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(onBackClick = {})
}