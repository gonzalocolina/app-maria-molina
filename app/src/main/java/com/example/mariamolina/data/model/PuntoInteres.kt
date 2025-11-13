package com.example.mariamolina.data.model

import androidx.annotation.StringRes
import androidx.annotation.ArrayRes
import com.example.mariamolina.R

// 1. Definimos la estructura de datos actualizada
data class PuntoInteres(
    val id: String,
    @StringRes val tituloResId: Int,
    val urlImagen: String,
    val rating: Double?,
    @StringRes val descripcionLargaResId: Int,
    @StringRes val horariosResId: Int? ,
    @StringRes val ubicacionResId: Int?,
    val subpuntos: List<SubPuntoInteres> = emptyList()
)

data class SubPuntoInteres(
    @StringRes val nombreResId: Int,
    val rating: Double?,
    @StringRes val horariosResId: Int?,
    @StringRes val ubicacionResId: Int?,
)

// 2. Creamos una lista de datos con los diferentes campos
val puntosDeInteres = listOf(
    PuntoInteres(
        id = "p1",
        tituloResId = R.string.p1_titulo,
        urlImagen = "https://placehold.co/600x400/8B261E/FFFFFF?text=Palazuelos",
        rating = null,
        descripcionLargaResId = R.string.p1_desc,
        horariosResId = null,
        ubicacionResId = null,
        subpuntos = listOf(
            SubPuntoInteres(
                nombreResId = R.string.p1_nombre_a,
                rating = 4.4,
                horariosResId = R.string.p1_horarios_a,
                ubicacionResId = R.string.p1_ubicacion_a,
            ),
            SubPuntoInteres(
                nombreResId = R.string.p1_nombre_b,
                rating = 4.9,
                horariosResId = R.string.p1_horarios_b,
                ubicacionResId = R.string.p1_ubicacion_b,
            )
        )
        ),
    PuntoInteres(
        id = "p2",
        tituloResId = R.string.p2_titulo,
        urlImagen = "https://placehold.co/600x400/D4AF69/000000?text=Plaza+Mayor",
        rating = 4.6,
        descripcionLargaResId = R.string.p2_desc,
        horariosResId = R.string.p2_horarios,
        ubicacionResId = R.string.p2_ubicacion
        ),
    PuntoInteres(
        id = "p3",
        tituloResId = R.string.p3_titulo,
        urlImagen = "https://placehold.co/600x400/757575/FFFFFF?text=San+Pablo",
        rating = 4.7,
        descripcionLargaResId = R.string.p3_desc,
        horariosResId = R.string.p3_horarios,
        ubicacionResId = R.string.p3_ubicacion,
        ),
    PuntoInteres(
        id = "p4",
        tituloResId = R.string.p4_titulo,
        urlImagen = "https://placehold.co/600x400/757575/FFFFFF?text=San+Pablo",
        rating = null,
        descripcionLargaResId = R.string.p4_desc,
        horariosResId = R.string.p4_horarios,
        ubicacionResId = R.string.p4_ubicacion,
    ),

    PuntoInteres(
        id = "p5",
        tituloResId = R.string.p5_titulo,
        urlImagen = "https://placehold.co/600x400/757575/FFFFFF?text=San+Pablo",
        rating = 4.5,
        descripcionLargaResId = R.string.p5_desc,
        horariosResId = R.string.p5_horarios,
        ubicacionResId = R.string.p5_ubicacion,
    ),

    PuntoInteres(
        id = "p6",
        tituloResId = R.string.p6_titulo,
        urlImagen = "https://placehold.co/600x400/757575/FFFFFF?text=San+Pablo",
        rating = null,
        descripcionLargaResId = R.string.p6_desc,
        horariosResId = R.string.p6_horarios,
        ubicacionResId = R.string.p6_ubicacion,
    ),

    PuntoInteres(
        id = "p7",
        tituloResId = R.string.p7_titulo,
        urlImagen = "https://placehold.co/600x400/757575/FFFFFF?text=San+Pablo",
        rating = 4.4,
        descripcionLargaResId = R.string.p7_desc,
        horariosResId = R.string.p7_horarios,
        ubicacionResId = R.string.p7_ubicacion,
    ),

    PuntoInteres(
        id = "p8",
        tituloResId = R.string.p8_titulo,
        urlImagen = "https://placehold.co/600x400/757575/FFFFFF?text=San+Pablo",
        rating = 4.3,
        descripcionLargaResId = R.string.p8_desc,
        horariosResId = R.string.p8_horarios,
        ubicacionResId = R.string.p8_ubicacion,
    ),

    PuntoInteres(
        id = "p9",
        tituloResId = R.string.p9_titulo,
        urlImagen = "https://placehold.co/600x400/757575/FFFFFF?text=San+Pablo",
        rating = 3.5,
        descripcionLargaResId = R.string.p9_desc,
        horariosResId = R.string.p9_horarios,
        ubicacionResId = R.string.p9_ubicacion,
    ),

    PuntoInteres(
        id = "p10",
        tituloResId = R.string.p10_titulo,
        urlImagen = "https://placehold.co/600x400/757575/FFFFFF?text=San+Pablo",
        rating = 4.7,
        descripcionLargaResId = R.string.p10_desc,
        horariosResId = R.string.p10_horarios,
        ubicacionResId = R.string.p10_ubicacion,
    ),

    PuntoInteres(
        id = "p11",
        tituloResId = R.string.p11_titulo,
        urlImagen = "https://placehold.co/600x400/757575/FFFFFF?text=San+Pablo",
        rating = 4.5,
        descripcionLargaResId = R.string.p11_desc,
        horariosResId = R.string.p11_horarios,
        ubicacionResId = R.string.p11_ubicacion,
    ),

    PuntoInteres(
        id = "p12",
        tituloResId = R.string.p12_titulo,
        urlImagen = "https://placehold.co/600x400/757575/FFFFFF?text=San+Pablo",
        rating = 3.9,
        descripcionLargaResId = R.string.p12_desc,
        horariosResId = R.string.p12_horarios,
        ubicacionResId = R.string.p12_ubicacion,
    )
)