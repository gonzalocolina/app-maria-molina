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
import androidx.compose.material3.TopAppBarScrollBehavior
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mariamolina.ui.theme.MariaMolinaTheme
import com.example.mariamolina.ui.screens.home.HomeScreen
import com.example.mariamolina.ui.screens.home.ImageScreen
import com.example.mariamolina.ui.screens.map.MapScreen
import com.example.mariamolina.ui.screens.pointsOfInterest.PointsListScreen
import com.example.mariamolina.ui.screens.poi.PointDetailScreen
import com.example.mariamolina.ui.screens.profile.ProfileScreen
import com.example.mariamolina.data.model.puntosDeInteres
import com.example.mariamolina.R
import com.example.mariamolina.data.model.Dificultad
import com.example.mariamolina.ui.screens.kids.AdminLobbyScreen
import com.example.mariamolina.ui.screens.kids.QuizGameScreen
import com.example.mariamolina.ui.screens.kids.KidsEntryScreen
import com.example.mariamolina.ui.screens.kids.KidsSlidesScreen
import com.example.mariamolina.ui.screens.kids.KidsQuizMenuScreen
import com.example.mariamolina.ui.screens.kids.RankingScreen
import com.example.mariamolina.ui.screens.kids.JoinGameScreen
import com.example.mariamolina.ui.screens.kids.StudentLobbyScreen

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

    // Scroll behaviour para esconder/mostrar la TopAppBar cuando se scrollea
    val topAppBarScrollBehavior: TopAppBarScrollBehavior =
        TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            // Comprobamos si la ruta actual ES la ruta de detalle de puntos de interés
            val esRutaDetallePoi =
                currentDestination?.route?.startsWith("${Pantalla.PointsOfInterest.ruta}/detail") == true

            // Comprobamos si estamos en la pantalla de slides
            val esRutaSlides = currentDestination?.route == "${Pantalla.Kids.ruta}/slides"

            // Solo mostramos la barra si NO estamos en el detalle, NO en perfil, y NO en slides en modo horizontal
            if (!esRutaDetallePoi && !isProfile && !(esRutaSlides && isLandscape)) {
                //Ocultamos también en el Admin Lobby
                val esRutaAdmin = currentDestination?.route == "admin_lobby"
                val esRutaJoin = currentDestination?.route == "join_game"
                val esRutaStudentLobby =
                    currentDestination?.route?.startsWith("student_lobby") == true
                val esRutaJuego =
                    currentDestination?.route?.startsWith("${Pantalla.Kids.ruta}/game") == true

                //Solo mostramos la barra si NO estamos en ninguna de esas pantallas
                if (!esRutaDetallePoi && !isProfile && !esRutaAdmin && !esRutaJoin && !esRutaStudentLobby && !esRutaJuego) {
                    AppTopBar(
                        titulo = stringResource(currentScreen.tituloTopBarResId),
                        subtitulo = currentScreen.subtituloResId?.let { stringResource(it) },
                        onProfileClick = { navControllerPrincipal.navigate(Pantalla.Profile.ruta) },
                        scrollBehavior = topAppBarScrollBehavior
                    )
                }
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
                                currentDestination?.route?.startsWith(pantalla.ruta) == true

                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    navControllerPrincipal.navigate(pantalla.ruta) {
                                        popUpTo(navControllerPrincipal.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
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
                        onBackClick = { navControllerPrincipal.popBackStack() },
                        onOpenMapClick = {
                            navControllerPrincipal.navigate("map?destinoId=${punto.id}") {
                                launchSingleTop = true
                            }
                        },
                        onOpenSubPointMapClick = { subpunto ->
                            navControllerPrincipal.navigate("map?destinoId=${subpunto.id}") {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }

            // --- SECCION INFANTIL ---

            // A. Pantalla de Entrada (Elegir Diapositivas o Quiz)
            composable(Pantalla.Kids.ruta) {
                // Pantalla de entrada para la sección Kids: elegir entre Diapositivas o Cuestionarios
                KidsEntryScreen(
                    onNavigateToSlides = { navControllerPrincipal.navigate("${Pantalla.Kids.ruta}/slides") },
                    onNavigateToQuizzes = { navControllerPrincipal.navigate("${Pantalla.Kids.ruta}/quiz") }
                )
            }

            // B. Menú del Quiz (Elegir Dificultad / Admin / Unirse)
            composable("${Pantalla.Kids.ruta}/quiz") {
                KidsQuizMenuScreen(
                    onBack = {
                        navControllerPrincipal.navigate(Pantalla.Kids.ruta) {
                            popUpTo(navControllerPrincipal.graph.findStartDestination().id) { }
                        }
                    },
                    onStartQuiz = { dificultad ->
                        navControllerPrincipal.navigate("${Pantalla.Kids.ruta}/game/${dificultad.name}")
                    },
                    onNavigateToRanking = {
                        navControllerPrincipal.navigate("${Pantalla.Kids.ruta}/ranking")
                    },
                    // Conectamos la navegación al admin
                    onNavigateToAdmin = {
                        navControllerPrincipal.navigate("admin_lobby")
                    },
                    // Navegamos a la pantalla de unirse
                    onJoinGame = {
                        navControllerPrincipal.navigate("join_game")
                    }
                )
            }

            composable("map?destinoId={destinoId}") { backStackEntry ->
                val destinoId = backStackEntry.arguments?.getString("destinoId")
                val destino = puntosDeInteres.find { it.id == destinoId }

                MapScreen(
                    destinoInicial = destino,
                    onNavigateToDetail = { punto ->
                        navControllerPrincipal.navigate(
                            "${Pantalla.PointsOfInterest.ruta}/detail/${punto.id}"
                        )
                    }
                )
            }

            // C. Diapositivas
            composable("${Pantalla.Kids.ruta}/slides") {
                KidsSlidesScreen(onBackToEntry = {
                    navControllerPrincipal.navigate(Pantalla.Kids.ruta) {
                        popUpTo(navControllerPrincipal.graph.findStartDestination().id) { }
                    }
                })
            }

            // D. Juego (Quiz) - Modo Solitario o Multijugador (una vez iniciado)
            composable("${Pantalla.Kids.ruta}/game/{dificultad}") { backStackEntry ->
                val dificultadString = backStackEntry.arguments?.getString("dificultad")
                val dificultad = Dificultad.valueOf(dificultadString ?: Dificultad.FACIL.name)

                QuizGameScreen(
                    dificultad = dificultad,
                    onQuizFinished = {
                        // Vuelve al sub-menú de cuestionarios (/quiz)
                        navControllerPrincipal.navigate("${Pantalla.Kids.ruta}/quiz") {
                            popUpTo("${Pantalla.Kids.ruta}/quiz") { inclusive = true }
                        }
                    },
                    onNavigateToRanking = {
                        // Navega al ranking desde la pantalla de resultados
                        navControllerPrincipal.navigate("${Pantalla.Kids.ruta}/ranking") {
                            // Opcional: cierra la pantalla de quiz
                            popUpTo(Pantalla.Kids.ruta) { inclusive = true }
                        }
                    }
                )
            }
            // E. Pantalla de Ranking
            composable("${Pantalla.Kids.ruta}/ranking") {
                RankingScreen(
                    onBackClick = { navControllerPrincipal.popBackStack() }
                )
            }

            // F. Admin Lobby (Profesor - Crear Partida)
            composable("admin_lobby") {
                AdminLobbyScreen(
                    onBack = { navControllerPrincipal.popBackStack() }
                )
            }

            // G. Unirse a Partida (Alumno - Introducir PIN)
            composable("join_game") {
                JoinGameScreen(
                    onBack = { navControllerPrincipal.popBackStack() },
                    onJoinSuccess = { pin ->
                        // Navegamos a la SALA DE ESPERA
                        navControllerPrincipal.navigate("student_lobby/$pin") {
                            // Borramos la pantalla de "poner PIN" del historial para no volver a ella
                            popUpTo("join_game") { inclusive = true }
                        }
                    }
                )
            }

            // H. Sala de Espera (Alumno - Esperando al profesor)
            composable("student_lobby/{pin}") { backStackEntry ->
                val pin = backStackEntry.arguments?.getString("pin") ?: ""

                StudentLobbyScreen(
                    pin = pin,
                    onBack = { navControllerPrincipal.popBackStack() },
                    onGameStarted = { dificultad ->
                        // ¡El juego empieza! Navegamos a la pantalla de juego
                        // Usamos replace (popUpTo) para que no pueda volver a la sala de espera
                        navControllerPrincipal.navigate("${Pantalla.Kids.ruta}/game/$dificultad") {
                            popUpTo("student_lobby/{pin}") { inclusive = true }
                        }
                    }
                )
            }
            composable(Pantalla.Profile.ruta) { ProfileScreen(onBackClick = { navControllerPrincipal.popBackStack() }) }
        }
    }
}

// (Tu función AppTopBar y el Preview quedan exactamente igual)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    titulo: String,
    subtitulo: String?,
    onProfileClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
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
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            scrolledContainerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
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