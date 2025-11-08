package com.example.mariamolina.data.model

import androidx.annotation.StringRes
import androidx.annotation.ArrayRes
import com.example.mariamolina.R

// 1. Definimos la estructura de datos actualizada
data class PuntoInteres(
    val id: String,
    @StringRes val tituloResId: Int,
    val urlImagen: String,
    @StringRes val duracionResId: Int,
    val rating: Double,
    @StringRes val descripcionLargaResId: Int,
    @StringRes val horariosResId: Int,
    @StringRes val ubicacionResId: Int,
    @ArrayRes val consejosArrayResId: Int
)

// 2. Creamos una lista de datos de prueba (mock data) con los nuevos campos
val puntosDeInteresMock = listOf(
    PuntoInteres(
        id = "p1",
        tituloResId = R.string.p1_titulo, // <-- CAMBIO
        urlImagen = "https://placehold.co/600x400/8B261E/FFFFFF?text=Palazuelos",
        duracionResId = R.string.p1_duracion, // <-- CAMBIO
        rating = 4.3,
        descripcionLargaResId = R.string.p1_desc, // <-- CAMBIO
        horariosResId = R.string.p1_horarios, // <-- CAMBIO
        ubicacionResId = R.string.p1_ubicacion, // <-- CAMBIO
        consejosArrayResId = R.array.p1_consejos // <-- CAMBIO
        ),
    PuntoInteres(
        id = "p2",
        tituloResId = R.string.p2_titulo,
        urlImagen = "https://placehold.co/600x400/D4AF69/000000?text=Plaza+Mayor",
        duracionResId = R.string.p2_duracion,
        rating = 4.8,
        descripcionLargaResId = R.string.p2_desc,
        horariosResId = R.string.p2_horarios,
        ubicacionResId = R.string.p2_ubicacion,
        consejosArrayResId = R.array.p2_consejos
        ),
    PuntoInteres(
        id = "p3",
        tituloResId = R.string.p3_titulo, // <-- CAMBIO
        urlImagen = "https://placehold.co/600x400/757575/FFFFFF?text=San+Pablo",
        duracionResId = R.string.p3_duracion, // <-- CAMBIO
        rating = 4.7,
        descripcionLargaResId = R.string.p3_desc, // <-- CAMBIO
        horariosResId = R.string.p3_horarios, // <-- CAMBIO
        ubicacionResId = R.string.p3_ubicacion, // <-- CAMBIO
        consejosArrayResId = R.array.p3_consejos // <-- CAMBIO
    )
)