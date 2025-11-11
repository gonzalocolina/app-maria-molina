package com.example.mariamolina.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.mariamolina.R
import com.example.mariamolina.ui.theme.MariaMolinaTheme


// 5. La primera pantalla, en su propio archivo.
// He renombrado "VistaInicio" a "HomeScreen", que es una
// convención de nombres más común.

@Composable
fun HomeScreen(onNavigateToImage: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.home_screen_title),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.home_screen_intro_paragraph),
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                androidx.compose.material3.Button(onClick = onNavigateToImage) {
                    Text("Ver árbol genealógico")
                }

                Spacer(modifier = Modifier.height(16.dp))

                SectionTitle(stringResource(R.string.section_title_childhood_family))
                Paragraph(
                    stringResource(R.string.paragraph_childhood_family_1)
                )
                Paragraph(
                    stringResource(R.string.paragraph_childhood_family_2)
                )

                Spacer(modifier = Modifier.height(16.dp))

                SectionTitle(stringResource(R.string.section_title_marriage_sancho))
                Paragraph(
                    stringResource(R.string.paragraph_marriage_sancho)
                )

                Spacer(modifier = Modifier.height(16.dp))

                SectionTitle(stringResource(R.string.section_title_queen_sancho_iv))
                Paragraph(
                    stringResource(R.string.paragraph_queen_sancho_iv)
                )

                Spacer(modifier = Modifier.height(16.dp))

                SectionTitle(stringResource(R.string.section_title_regencies))
                Paragraph(
                    stringResource(R.string.paragraph_regencies)
                )

                Spacer(modifier = Modifier.height(16.dp))

                SectionTitle(stringResource(R.string.section_title_legacy_works))
                Paragraph(
                    stringResource(R.string.paragraph_legacy_works)
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun Paragraph(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge
    )
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MariaMolinaTheme {
        HomeScreen(onNavigateToImage = {}) // función vacía para el preview
    }
}
