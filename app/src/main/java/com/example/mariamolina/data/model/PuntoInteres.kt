package com.example.mariamolina.data.model

import androidx.annotation.StringRes
import androidx.annotation.ArrayRes
import com.example.mariamolina.R

// 1. Definimos la estructura de datos actualizada
data class PuntoInteres(
    val id: String,
    @StringRes val tituloResId: Int,
    val urlImagen: String,
    @StringRes val descripcionLargaResId: Int,
    @StringRes val horariosResId: Int? ,
    @StringRes val ubicacionResId: Int?,

    // Para el mapa
    val latitud: Double,
    val longitud: Double,

    val subpuntos: List<SubPuntoInteres> = emptyList()
)

data class SubPuntoInteres(
    val id: String,
    @StringRes val nombreResId: Int,
    @StringRes val horariosResId: Int?,
    @StringRes val ubicacionResId: Int?,

    // Para el mapa
    val latitud: Double,
    val longitud: Double,
)

// 2. Creamos una lista de datos con los diferentes campos
val puntosDeInteres = listOf(
    PuntoInteres(
        id = "p1",
        tituloResId = R.string.p1_titulo,
        urlImagen = "https://ik.imagekit.io/fn2wdosiw/AlcazarejoHD.jpg",
        descripcionLargaResId = R.string.p1_desc,
        horariosResId = R.string.p1_horarios,
        ubicacionResId = R.string.p1_ubicacion,

        latitud = 41.65365205507737,
        longitud = -4.730360618893759,

        subpuntos = listOf(
            SubPuntoInteres(
                id = "sp1",
                nombreResId = R.string.p1_nombre_a,
                horariosResId = R.string.p1_horarios_a,
                ubicacionResId = R.string.p1_ubicacion_a,

                latitud = 41.654730127801,
                longitud = -4.73026568816032
            ),
            SubPuntoInteres(
                id = "sp2",
                nombreResId = R.string.p1_nombre_b,
                horariosResId = R.string.p1_horarios_b,
                ubicacionResId = R.string.p1_ubicacion_b,

                latitud = 41.654091266634545,
                longitud = -4.729607288160295
            )
        )
        ),
    PuntoInteres(
        id = "p2",
        tituloResId = R.string.p2_titulo,
        urlImagen = "https://ik.imagekit.io/fn2wdosiw/Santa-Maria-la-Mayor.jpg",
        descripcionLargaResId = R.string.p2_desc,
        horariosResId = R.string.p2_horarios,
        ubicacionResId = R.string.p2_ubicacion,

        latitud = 41.65324216039736,
        longitud = -4.723034965661509
        ),
    PuntoInteres(
        id = "p3",
        tituloResId = R.string.p3_titulo,
        urlImagen = "https://ik.imagekit.io/fn2wdosiw/Valladolid_Antigua.jpg",
        descripcionLargaResId = R.string.p3_desc,
        horariosResId = R.string.p3_horarios,
        ubicacionResId = R.string.p3_ubicacion,

        latitud = 41.65393795160224,
        longitud = -4.722785661247381
        ),
    PuntoInteres(
        id = "p4",
        tituloResId = R.string.p4_titulo,
        urlImagen = "https://ik.imagekit.io/fn2wdosiw/Iglesia%20de%20San%20Miguel%20001.jpg",
        descripcionLargaResId = R.string.p4_desc,
        horariosResId = R.string.p4_horarios,
        ubicacionResId = R.string.p4_ubicacion,

        latitud = 41.65516875619189,
        longitud = -4.727215297435327
    ),

    PuntoInteres(
        id = "p5",
        tituloResId = R.string.p5_titulo,
        urlImagen = "https://ik.imagekit.io/fn2wdosiw/valladolid-iglesia-magdalena.jpg?updatedAt=1763226656600",
        descripcionLargaResId = R.string.p5_desc,
        horariosResId = R.string.p5_horarios,
        ubicacionResId = R.string.p5_ubicacion,

        latitud = 41.65391722856585,
        longitud = -4.717652346507175
    ),

    PuntoInteres(
        id = "p6",
        tituloResId = R.string.p6_titulo,
        urlImagen = "https://ik.imagekit.io/fn2wdosiw/PlazaMercado.jpg",
        descripcionLargaResId = R.string.p6_desc,
        horariosResId = R.string.p6_horarios,
        ubicacionResId = R.string.p6_ubicacion,

        latitud = 41.652197653540284,
        longitud = -4.728655979541548
    ),

    PuntoInteres(
        id = "p7",
        tituloResId = R.string.p7_titulo,
        urlImagen = "https://ik.imagekit.io/fn2wdosiw/PuenteMayor.jpg",
        descripcionLargaResId = R.string.p7_desc,
        horariosResId = R.string.p7_horarios,
        ubicacionResId = R.string.p7_ubicacion,

        latitud = 41.66006310800611,
        longitud = -4.732774565795078
    ),

    PuntoInteres(
        id = "p8",
        tituloResId = R.string.p8_titulo,
        urlImagen = "https://ik.imagekit.io/fn2wdosiw/Palazuelos.jpg",
        descripcionLargaResId = R.string.p8_desc,
        horariosResId = R.string.p8_horarios,
        ubicacionResId = R.string.p8_ubicacion,

        latitud = 41.75279383945776,
        longitud = -4.633373559319185
    ),

    PuntoInteres(
        id = "p9",
        tituloResId = R.string.p9_titulo,
        urlImagen = "https://ik.imagekit.io/fn2wdosiw/PuertaMagdalena.jpg",
        descripcionLargaResId = R.string.p9_desc,
        horariosResId = R.string.p9_horarios,
        ubicacionResId = R.string.p9_ubicacion,

        latitud = 41.653613496183674,
        longitud = -4.717215040419569
    ),

    PuntoInteres(
        id = "p10",
        tituloResId = R.string.p10_titulo,
        urlImagen = "https://ik.imagekit.io/fn2wdosiw/Castillo%20de%20Tiedra.jpg",
        descripcionLargaResId = R.string.p10_desc,
        horariosResId = R.string.p10_horarios,
        ubicacionResId = R.string.p10_ubicacion,

        latitud = 41.65016227317396,
        longitud = -5.269390403503933
    ),

    PuntoInteres(
        id = "p11",
        tituloResId = R.string.p11_titulo,
        urlImagen = "https://ik.imagekit.io/fn2wdosiw/CastilloMontealegre.jpg",
        descripcionLargaResId = R.string.p11_desc,
        horariosResId = R.string.p11_horarios,
        ubicacionResId = R.string.p11_ubicacion,

        latitud = 41.903087071041696,
        longitud = -4.903341269408345
    ),

    PuntoInteres(
        id = "p12",
        tituloResId = R.string.p12_titulo,
        urlImagen = "https://ik.imagekit.io/fn2wdosiw/HuelgasReales_Valladolid.jpg",
        descripcionLargaResId = R.string.p12_desc,
        horariosResId = R.string.p12_horarios,
        ubicacionResId = R.string.p12_ubicacion,

        latitud = 41.654132640841034,
        longitud = -4.716651025115444
    )
)