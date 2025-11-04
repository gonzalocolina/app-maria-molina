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
import com.example.mariamolina.ui.theme.MariaMolinaTheme


// 5. La primera pantalla, en su propio archivo.
// He renombrado "VistaInicio" a "HomeScreen", que es una
// convención de nombres más común.

@Composable
fun HomeScreen() {
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
                    text = "María de Molina",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "María de Molina (1264-1321) fue una de las mujeres más importantes de la historia de Castilla ya que dedicó su vida a mantener la paz y la estabilidad del reino. Fue reina, madre de rey y abuela de rey, pero sobre todo una gobernante prudente y respetada, que supo ganarse el apoyo del pueblo y la nobleza en tiempos difíciles.",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                SectionTitle("Infancia y familia")
                Paragraph(
                    "Nació hacia 1264, probablemente cerca del monasterio de Santa María de Palazuelos situado entre los actuales Corcos del Valle y Cabezón de Pisuerga."
                )
                Paragraph(
                    "María pertenecía a una familia noble y poderosa. Era hija de Alfonso de Molina, hermano del rey Fernando III, y de Mayor Alfonso de Meneses. Desde pequeña creció rodeada de la vida política de la corte castellana. Su educación la preparó para asumir responsabilidades, aprendiendo a leer, escribir y comprender los asuntos del reino, algo poco común en las mujeres de su tiempo."
                )

                Spacer(modifier = Modifier.height(16.dp))

                SectionTitle("Matrimonio con Sancho")
                Paragraph(
                    "En 1282 se casó con el infante Sancho, futuro rey Sancho IV. Su matrimonio no fue bien recibido por todos: eran parientes cercanos y necesitaban una dispensa papal que tardó años en llegar. Aun así, María acompañó a su esposo en los momentos más duros, incluso cuando su derecho al trono fue cuestionado. Tras la muerte de su suegro Alfonso X, Sancho fue coronado rey y ella se convirtió en reina de Castilla."
                )

                Spacer(modifier = Modifier.height(16.dp))

                SectionTitle("Reina junto a Sancho IV")
                Paragraph(
                    "Durante el reinado de Sancho IV (1284–1295), María participó activamente en las decisiones políticas. Se ganó fama de mujer inteligente, prudente y justa. Acompañó al rey en campañas y defendió la legitimidad de su familia frente a los enemigos internos del reino. Su reinado estuvo marcado por las luchas entre nobles y la consolidación de la monarquía castellana."
                )

                Spacer(modifier = Modifier.height(16.dp))

                SectionTitle("Las regencias de María de Molina")
                Paragraph(
                    "Tras la muerte de su esposo Sancho IV en 1295, María asumió la regencia por su hijo Fernando IV, que solo tenía nueve años. Con prudencia y firmeza, logró mantener la corona en medio de conflictos internos, ganándose el respeto de nobles y concejos. Años después, en 1312, volvió a gobernar como regente de su nieto Alfonso XI, demostrando una vez más su sabiduría y su capacidad para mantener la paz y la unidad del reino en tiempos difíciles."
                )

                Spacer(modifier = Modifier.height(16.dp))

                SectionTitle("Legado y obras")
                Paragraph(
                    "María de Molina es recordada como una de las grandes reinas de Castilla por su capacidad política y su sentido de justicia. Fundó y apoyó numerosos monasterios, como el de Las Huelgas de Valladolid, donde fue enterrada. Promovió la educación, la religión y la paz en tiempos de guerra. Su figura simboliza la prudencia, la lealtad y la defensa del bien común."
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
        HomeScreen()
    }
}
