package com.example.mariamolina.data.model

/** Proveedor de slides de prueba */
object SlidesProvider {
    val testSlides: List<Slide> = listOf(
        Slide(
            id = "s1",
            title = "María de Molina: Infancia",
            description = "Breve resumen sobre la infancia de María de Molina.",
            imageUrl = "https://picsum.photos/seed/maria1/800/400"
        ),
        Slide(
            id = "s2",
            title = "Regreso a Valladolid",
            description = "Historia del retorno y su impacto en la ciudad.",
            imageUrl = "https://picsum.photos/seed/maria2/800/400"
        ),
        Slide(
            id = "s3",
            title = "Legado",
            description = "El legado cultural y patrimonial que dejó María de Molina.",
            imageUrl = "https://picsum.photos/seed/maria3/800/400"
        )
    )
}

