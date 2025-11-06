package com.example.mariamolina.data.model

// 1. Definimos la estructura de datos actualizada
data class PuntoInteres(
    val id: String,
    val titulo: String,
    val urlImagen: String,
    val duracion: String, // ej: "2 horas"
    val rating: Double, // ej: 4.3
    val descripcionLarga: String,
    val horarios: String, // ej: "Acceso libre todo el día"
    val ubicacion: String, // ej: "Plaza de la Universidad, 1"
    val consejos: List<String> // Una lista de consejos
)

// 2. Creamos una lista de datos de prueba (mock data) con los nuevos campos
val puntosDeInteresMock = listOf(
    PuntoInteres(
        id = "p1",
        titulo = "Palazuelos",
        urlImagen = "https://placehold.co/600x400/8B261E/FFFFFF?text=Palazuelos",
        duracion = "2 horas",
        rating = 4.3,
        descripcionLarga = "Palazuelos es un encantador pueblo de Castilla y León que conserva el encanto de la arquitectura tradicional castellana. Sus calles empedradas y casas de piedra y adobe nos transportan a épocas pasadas. El pueblo destaca por su iglesia parroquial y las construcciones rurales tradicionales que reflejan el modo de vida de la región.",
        horarios = "Acceso libre todo el día",
        ubicacion = "Palazuelos de Eresma, Segovia",
        consejos = listOf(
            "Visita la iglesia parroquial del siglo XVI",
            "Pasea por sus calles empedradas tradicionales",
            "Ideal para fotografía de arquitectura rural"
        )
    ),
    PuntoInteres(
        id = "p2",
        titulo = "Plaza Mayor",
        urlImagen = "https://placehold.co/600x400/D4AF69/000000?text=Plaza+Mayor",
        duracion = "1 hora",
        rating = 4.8,
        descripcionLarga = "La Plaza Mayor de Valladolid es una de las más antiguas de España y ha servido de modelo para muchas otras...",
        horarios = "Siempre abierta",
        ubicacion = "Plaza Mayor, 47001 Valladolid",
        consejos = listOf(
            "Ideal para tomar algo en las terrazas",
            "Visita la estatua del Conde Ansúrez"
        )
    ),
    PuntoInteres(
        id = "p3",
        titulo = "Iglesia de San Pablo",
        urlImagen = "https://placehold.co/600x400/757575/FFFFFF?text=San+Pablo",
        duracion = "45 min",
        rating = 4.7,
        descripcionLarga = "La fachada de la iglesia de San Pablo es uno de los ejemplos más impresionantes del gótico isabelino...",
        horarios = "Horario de misas",
        ubicacion = "Plaza de San Pablo, 47011 Valladolid",
        consejos = listOf(
            "Fíjate en los detalles de la fachada",
            "Visita el Colegio de San Gregorio al lado"
        )
    )
)