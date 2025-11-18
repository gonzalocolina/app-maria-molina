package com.example.mariamolina.ui.navigation

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.mariamolina.ui.screens.home.ImageScreen
import com.example.mariamolina.ui.screens.kids.KidsScreen
import com.example.mariamolina.ui.screens.map.MapScreen
import com.example.mariamolina.ui.screens.pointsOfInterest.PointsListScreen
import com.example.mariamolina.ui.screens.poi.PointDetailScreen
import com.example.mariamolina.ui.screens.profile.ProfileScreen
import com.example.mariamolina.data.model.puntosDeInteres
import com.example.mariamolina.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navControllerPrincipal = rememberNavController()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // 1. Escuchamos al controlador principal (para títulos y barra inferior)
    val navBackStackEntry by navControllerPrincipal.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentScreen = itemsNavegacion.find { it.ruta == currentDestination?.route }
        ?: Pantalla.Home

    val isProfile = currentDestination?.route == Pantalla.Profile.ruta

    Scaffold(
        topBar = {
            // Comprobamos si la ruta actual ES la ruta de detalle de puntos de interés
            val esRutaDetallePoi = currentDestination?.route?.startsWith("${Pantalla.PointsOfInterest.ruta}/detail") == true

            // Solo mostramos la barra si NO estamos en el detalle y NO en perfil
            if (!esRutaDetallePoi && !isProfile) {
                AppTopBar(
                    titulo = stringResource(currentScreen.tituloTopBarResId),
                    subtitulo = currentScreen.subtituloResId?.let { stringResource(it) },
                    onProfileClick = { navControllerPrincipal.navigate(Pantalla.Profile.ruta) }
                )
            }
        },
        bottomBar = {
            if (!isProfile) {
                Column {
                    HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
                    NavigationBar(
                        modifier = Modifier.height(if (isLandscape) 45.dp else 75.dp)
                    ) {
                        itemsNavegacion.forEach { pantalla ->
                            val isSelected =
                                currentDestination?.hierarchy?.any { it.route == pantalla.ruta } == true

                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    navControllerPrincipal.navigate(pantalla.ruta) {
                                        popUpTo(navControllerPrincipal.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Icon(
                                            pantalla.icono,
                                            contentDescription = stringResource(pantalla.tituloBottomBarResId),
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            stringResource(pantalla.tituloBottomBarResId),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navControllerPrincipal,
            startDestination = Pantalla.Home.ruta,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Pantalla.Home.ruta) {
                HomeScreen(onNavigateToImage = { navControllerPrincipal.navigate("image") })
            }
            // nueva ruta para la pantalla de imagen
            composable("image") {
                ImageScreen(onBackClick = { navControllerPrincipal.popBackStack() })
            }

            // Rutas de Puntos de Interés (ahora en el NavHost principal para evitar NavHost anidado)
            composable(Pantalla.PointsOfInterest.ruta) {
                PointsListScreen(
                    puntos = puntosDeInteres,
                    onPuntoClick = { puntoId ->
                        navControllerPrincipal.navigate("${Pantalla.PointsOfInterest.ruta}/detail/$puntoId")
                    }
                )
            }

            composable("${Pantalla.PointsOfInterest.ruta}/detail/{puntoId}") { backStackEntry ->
                val puntoId = backStackEntry.arguments?.getString("puntoId")
                val punto = puntosDeInteres.find { it.id == puntoId }
                if (punto != null) {
                    PointDetailScreen(
                        punto = punto,
                        onBackClick = { navControllerPrincipal.popBackStack() }
                    )
                }
            }

            composable(Pantalla.Map.ruta) { MapScreen() }
            composable(Pantalla.Kids.ruta) { KidsScreen() }
            composable(Pantalla.Profile.ruta) { ProfileScreen(onBackClick = { navControllerPrincipal.popBackStack() }) }
        }
    }
}

// (Tu función AppTopBar y el Preview quedan exactamente igual)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(titulo: String, subtitulo: String?, onProfileClick: () -> Unit) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                if (subtitulo != null) {
                    Text(
                        text = subtitulo,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        actions = {
            IconButton(onClick = onProfileClick) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = stringResource(R.string.cd_perfil),
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