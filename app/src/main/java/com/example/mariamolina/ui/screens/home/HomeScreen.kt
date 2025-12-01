package com.example.mariamolina.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import android.content.res.Configuration
import com.example.mariamolina.R
import com.example.mariamolina.ui.theme.MariaMolinaTheme


// 5. La primera pantalla, en su propio archivo.
// He renombrado "VistaInicio" a "HomeScreen", que es una
// convención de nombres más común.

@Composable
fun HomeScreen(
    onNavigateToImage: () -> Unit,
    onNavigateToPanorama: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            // Consideramos "phone" a cualquier dispositivo cuya smallestScreenWidthDp < 600.
            // Los phones usarán siempre la vista vertical (imagen encima del texto), incluso en landscape.
            // Solo los tablets (smallestScreenWidthDp >= 600) podrán usar el layout lado-a-lado en landscape.
            val isPhone = configuration.smallestScreenWidthDp < 600
            val isTablet = !isPhone

            // Padding dinámico según tamaño
            val horizontalPadding = if (isTablet) 32.dp else 16.dp

            // Row lateral: solo en tablets en landscape y con ancho/alto suficientes
            val availableWidth = this.maxWidth
            val screenHeightDp = configuration.screenHeightDp
            val useSideBySide = isTablet && isLandscape && availableWidth >= 900.dp && screenHeightDp >= 600

            if (useSideBySide) {
                // Layout en Row para tablets o landscape: imagen a la izquierda, contenido a la derecha
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = horizontalPadding, vertical = 24.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Contenedor de imagen: ocupa máximo la mitad izquierda pero limitado en ancho
                    Card(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(560.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.mariamolina_menu),
                            contentDescription = stringResource(R.string.cd_imagen_inicial),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Contenido textual en columna con ancho limitado para mejorar legibilidad
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(560.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Título principal con estilo destacado
                        ElegantTitle(
                            text = stringResource(R.string.home_screen_title),
                            large = true
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            thickness = 2.dp,
                            color = MaterialTheme.colorScheme.primaryContainer
                        )

                        Text(
                            text = stringResource(R.string.home_screen_intro_paragraph),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = 28.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        ContentSection(
                            title = stringResource(R.string.section_title_childhood_family),
                            paragraphs = listOf(
                                stringResource(R.string.paragraph_childhood_family_1),
                                stringResource(R.string.paragraph_childhood_family_2)
                            )
                        )

                        ContentSection(
                            title = stringResource(R.string.section_title_marriage_sancho),
                            paragraphs = listOf(stringResource(R.string.paragraph_marriage_sancho))
                        )

                        ContentSection(
                            title = stringResource(R.string.section_title_queen_sancho_iv),
                            paragraphs = listOf(stringResource(R.string.paragraph_queen_sancho_iv))
                        )

                        Button(
                            onClick = onNavigateToImage,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                                .shadow(4.dp, RoundedCornerShape(8.dp)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                stringResource(R.string.btn_ver_arbol),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        Button(
                            onClick = onNavigateToPanorama,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .shadow(4.dp, RoundedCornerShape(8.dp)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                stringResource(R.string.btn_ver_panorama),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        ContentSection(
                            title = stringResource(R.string.section_title_regencies),
                            paragraphs = listOf(stringResource(R.string.paragraph_regencies))
                        )

                        ContentSection(
                            title = stringResource(R.string.section_title_legacy_works),
                            paragraphs = listOf(stringResource(R.string.paragraph_legacy_works))
                        )
                    }
                }
            } else {
            // Comportamiento original para móviles en vertical
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Título principal con estilo destacado
                        ElegantTitle(
                            text = stringResource(R.string.home_screen_title),
                            large = false
                        )

                        // Ajuste para tablet vertical: mostrar imagen completa sin recortar
                        val menuPainter = painterResource(id = R.drawable.mariamolina_menu)
                        val intrinsic = menuPainter.intrinsicSize
                        val menuAspectRatio = if (intrinsic.width > 0f && intrinsic.height > 0f) {
                            intrinsic.width / intrinsic.height
                        } else null

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            if (isTablet && !isLandscape) {
                                Image(
                                    painter = menuPainter,
                                    contentDescription = stringResource(R.string.cd_imagen_inicial),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .then(if (menuAspectRatio != null) Modifier.aspectRatio(menuAspectRatio) else Modifier.height(400.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Image(
                                    painter = menuPainter,
                                    contentDescription = stringResource(R.string.cd_imagen_inicial),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(250.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            thickness = 2.dp,
                            color = MaterialTheme.colorScheme.primaryContainer
                        )

                        Text(
                            text = stringResource(R.string.home_screen_intro_paragraph),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = 26.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        ContentSection(
                            title = stringResource(R.string.section_title_childhood_family),
                            paragraphs = listOf(
                                stringResource(R.string.paragraph_childhood_family_1),
                                stringResource(R.string.paragraph_childhood_family_2)
                            )
                        )

                        ContentSection(
                            title = stringResource(R.string.section_title_marriage_sancho),
                            paragraphs = listOf(stringResource(R.string.paragraph_marriage_sancho))
                        )

                        ContentSection(
                            title = stringResource(R.string.section_title_queen_sancho_iv),
                            paragraphs = listOf(stringResource(R.string.paragraph_queen_sancho_iv))
                        )

                        Button(
                            onClick = onNavigateToImage,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .shadow(4.dp, RoundedCornerShape(8.dp)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                stringResource(R.string.btn_ver_arbol),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        Button(
                            onClick = onNavigateToPanorama,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .shadow(4.dp, RoundedCornerShape(8.dp)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                stringResource(R.string.btn_ver_panorama),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        ContentSection(
                            title = stringResource(R.string.section_title_regencies),
                            paragraphs = listOf(stringResource(R.string.paragraph_regencies))
                        )

                        ContentSection(
                            title = stringResource(R.string.section_title_legacy_works),
                            paragraphs = listOf(stringResource(R.string.paragraph_legacy_works))
                        )
                    }
                }
            }
            }
        }
    }
}


@Composable
fun ContentSection(
    title: String,
    paragraphs: List<String>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )

            paragraphs.forEach { paragraph ->
                Text(
                    text = paragraph,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

// Nuevo composable para un título "elegante" inspirado en las tipografías enviadas.
@Composable
fun ElegantTitle(modifier: Modifier = Modifier, text: String, large: Boolean = false ) {
    // Se usa uppercase para emular el aspecto 'display' y se aplica tracking amplio.
    val baseSize = MaterialTheme.typography.headlineLarge.fontSize
    val fontSize = if (large) baseSize * 1.1f else baseSize
    Text(
        text = text.uppercase(),
        modifier = modifier,
        style = MaterialTheme.typography.headlineLarge.copy(
            fontSize = fontSize,
          //  fontWeight = FontWeight.Light, // aspecto más fino similar a las muestras
            letterSpacing = 5.sp,
            fontWeight = FontWeight.Bold
        ),
        color = MaterialTheme.colorScheme.primary
    )
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MariaMolinaTheme {
        HomeScreen(
            onNavigateToImage = {},
            onNavigateToPanorama = {}
        )
    }
}
