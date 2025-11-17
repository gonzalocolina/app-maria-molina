package com.example.mariamolina.ui.screens.profile

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mariamolina.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val spanish = stringResource(R.string.spanish)
    val english = stringResource(R.string.english)
    val languages = listOf(spanish, english)
    val currentLanguageCode = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getString("language", "es") ?: "es"
    val defaultLanguage = if (currentLanguageCode == "es") spanish else english
    var selectedLanguage by remember { mutableStateOf(defaultLanguage) }
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_button_description))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.select_language_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(selectedLanguage)
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }

            if (showDialog) {
                BasicAlertDialog(
                    onDismissRequest = { showDialog = false }
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.select_language_title),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        languages.forEach { language ->
                            TextButton(
                                onClick = {
                                    selectedLanguage = language
                                    showDialog = false
                                    val languageCode = if (language == spanish) "es" else "en"
                                    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                                    prefs.edit().putString("language", languageCode).apply()
                                    Toast.makeText(context, context.getString(R.string.language_changed_toast, language), Toast.LENGTH_SHORT).show()
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

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.about_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(R.string.about_description),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(onBackClick = {})
}
