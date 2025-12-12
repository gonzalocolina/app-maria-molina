package es.uva.inf.mariamolina.ui.screens.pointsOfInterest

// Este objeto guarda las rutas de navegación como constantes.
// Es una buena práctica para evitar errores de escritura.
object PoiRoutes {
    // La ruta para la pantalla de lista
    const val LIST = "poi_list"

    // El prefijo para la ruta de detalle
    const val DETAIL_PREFIX = "poi_detail"

    // El nombre del argumento (el ID del punto)
    const val DETAIL_ARG = "puntoId"

    // La ruta completa de detalle, incluyendo el argumento
    // (Quedará como "poi_detail/{puntoId}")
    const val DETAIL = "$DETAIL_PREFIX/{$DETAIL_ARG}"
}