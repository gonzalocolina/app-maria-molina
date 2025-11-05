package com.example.mariamolina.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column // ¡Importado!
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons // ¡Importado!
import androidx.compose.material.icons.filled.AccountCircle // ¡Importado!
import androidx.compose.material3.ExperimentalMaterial3Api // ¡Importado!
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton // ¡Importado!
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar // ¡Importado!
import androidx.compose.material3.TopAppBarDefaults // ¡Importado!
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
// import androidx.compose.runtime.CompositionLocalProvider // Eliminado
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
// Importamos 'Color' para usar 'Color.Transparent'
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow // ¡Importado!
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mariamolina.ui.theme.MariaMolinaTheme
import com.example.mariamolina.ui.screens.home.HomeScreen
import com.example.mariamolina.ui.screens.kids.KidsScreen
import com.example.mariamolina.ui.screens.map.MapScreen
import com.example.mariamolina.ui.screens.pointsOfInterest.PointsOfInterestScreen



@OptIn(ExperimentalMaterial3Api::class) // Necesario para TopAppBar
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // --- ¡CAMBIO 1: Obtenemos la pantalla actual! ---
    // Necesitamos saber cuál es la pantalla actual para coger su título
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentScreen = itemsNavegacion.find { it.ruta == currentDestination?.route }
        ?: Pantalla.Home // Por defecto, si no encuentra, usa Home


    Scaffold(
        // --- ¡CAMBIO 2: Añadimos la barra superior! ---
        topBar = {
            AppTopBar(
                titulo = currentScreen.tituloTopBar,
                subtitulo = currentScreen.subtitulo
            )
        },
        bottomBar = {
            // CompositionLocalProvider ELIMINADO
            NavigationBar {
                // val navBackStackEntry by navController.currentBackStackEntryAsState() // Movido arriba
                // val currentDestination = navBackStackEntry?.destination // Movido arriba

                itemsNavegacion.forEach { pantalla ->
                    val isSelected =
                        currentDestination?.hierarchy?.any { it.route == pantalla.ruta } == true

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navController.navigate(pantalla.ruta) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = { Text(pantalla.tituloBottomBar) },
                        icon = { Icon(pantalla.icono, contentDescription = pantalla.tituloBottomBar) },

                        // Esto soluciona la alineación de "Puntos de interés"
                        //alwaysShowLabel = false,

                        colors = NavigationBarItemDefaults.colors(
                            // Estos colores SÍ se quedan fijos al seleccionar
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,

                            // Estos colores son para los no seleccionados
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,

                            // Hacemos que la píldora fija sea invisible
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Pantalla.Home.ruta,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Pantalla.Home.ruta) { HomeScreen() }
            composable(Pantalla.PointsOfInterest.ruta) { PointsOfInterestScreen() }
            composable(Pantalla.Map.ruta) { MapScreen() }
            composable(Pantalla.Kids.ruta) { KidsScreen() }
        }
    }
}

// --- ¡CAMBIO 3: Creamos nuestro Composable para la barra superior! ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(titulo: String, subtitulo: String?) {
    TopAppBar(
        // 1. Título y Subtítulo
        title = {
            Column {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium, // Puedes probar titleLarge
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis, // Por si el texto es muy largo
                    color = MaterialTheme.colorScheme.onPrimary
                )
                // Solo mostramos el subtítulo si existe
                if (subtitulo != null) {
                    Text(
                        text = subtitulo,
                        style = MaterialTheme.typography.bodySmall, // Puedes probar bodyMedium
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        },
        // 2. Colores (los he puesto fijos para que coincidan con tu imagen)
        colors = TopAppBarDefaults.topAppBarColors(
            // ¡¡AQUÍ ESTÁ TU COLOR MARRÓN/ROJO!!
            containerColor = MaterialTheme.colorScheme.primary,
            // El color del texto (título y subtítulo) será blanco
            //titleContentColor = Color.White,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        // 3. Icono de la derecha (Perfil)
        actions = {
            IconButton(onClick = { /* TODO: Aquí iría la navegación a Perfil */ }) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Perfil",
                    // Hacemos el icono blanco también
                    tint = Color(0xFFF5E6D3)
                )
            }
        }
    )
}


@Preview(showBackground = true)
@Composable
fun AppNavigationPreview() {
    MariaMolinaTheme {
        AppNavigation()
    }
}