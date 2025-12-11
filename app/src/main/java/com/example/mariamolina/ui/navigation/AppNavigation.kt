package com.example.mariamolina.ui.navigation

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mariamolina.ui.theme.MariaMolinaTheme
import com.example.mariamolina.ui.screens.home.HomeScreen
import com.example.mariamolina.ui.screens.home.ImageScreen
import com.example.mariamolina.ui.screens.map.MapScreen
import com.example.mariamolina.ui.screens.pointsOfInterest.PointsOfInterestScreen
import com.example.mariamolina.ui.screens.pointsOfInterest.PointDetailScreen
import com.example.mariamolina.ui.screens.profile.ProfileScreen
import com.example.mariamolina.data.model.puntosDeInteres
import com.example.mariamolina.R
import com.example.mariamolina.data.model.Dificultad
import com.example.mariamolina.ui.screens.kids.QuizGameScreen
import com.example.mariamolina.ui.screens.kids.KidsEntryScreen
import com.example.mariamolina.ui.screens.kids.KidsSlidesScreen
import com.example.mariamolina.ui.screens.kids.RankingScreen
import com.example.mariamolina.ui.screens.kids.JoinGameScreen
import com.example.mariamolina.ui.screens.kids.StudentLobbyScreen
import com.example.mariamolina.ui.screens.kids.TeacherLobbyScreen
import com.example.mariamolina.ui.screens.kids.TeacherMenuScreen
import com.example.mariamolina.ui.screens.kids.AddQuestionScreen
import com.example.mariamolina.ui.screens.kids.TeacherGameScreen
import com.example.mariamolina.ui.screens.kids.StudentGameScreen
import com.example.mariamolina.ui.screens.kids.MultiplayerRankingScreen
import com.example.mariamolina.ui.screens.kids.RankRewardScreen
import com.example.mariamolina.ui.screens.panorama.Panorama360Screen
import com.example.mariamolina.ui.viewmodel.PointsOfInterestViewModel
import androidx.hilt.navigation.compose.hiltViewModel



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navControllerPrincipal = rememberNavController()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTablet = configuration.smallestScreenWidthDp >= 600

    // 1. Escuchamos al controlador principal (para títulos y barra inferior)
    val navBackStackEntry by navControllerPrincipal.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentScreen = itemsNavegacion.find { it.ruta == currentDestination?.route }
        ?: Pantalla.Home

    val isProfile = currentDestination?.route == Pantalla.Profile.ruta
    // Detectamos si la ruta actual es la del mapa
    val esRutaMapaGlobal = currentDestination?.route?.startsWith("map") == true

    // Notamos que el estado del TopAppBar debe resetearse cuando cambiamos de pantalla. Para
    // garantizar una recreación limpia, creamos el estado y el scrollBehavior dentro del
    // bloque `key(currentDestination?.route) { ... }` que forzará una recomposición completa de
    // ese árbol al navegar a una ruta distinta.
    key(currentDestination?.route) {
        // Detectar pantallas que tienen su propio Scaffold y NO deben usar nestedScroll del padre
        val esRutaDetallePoi =
            currentDestination?.route?.startsWith("${Pantalla.PointsOfInterest.ruta}/detail") == true
        val esRutaAddQuestion = currentDestination?.route == "add_question"
        val esRutaTeacherLobby = currentDestination?.route == "teacher_lobby"
        val esRutaAdminLobby = currentDestination?.route == "admin_lobby"
        val esRutaProfile = currentDestination?.route == "profile"
        val esRankRewardScreen = currentDestination?.route == "rank_reward/{pin}/{posicion}/{total}/{puntuacion}"
        val esRutaConScaffoldPropio = esRutaDetallePoi || esRutaAddQuestion || esRutaTeacherLobby || esRutaAdminLobby || esRutaProfile || esRankRewardScreen
        
        // Scroll behaviour para esconder/mostrar la TopAppBar cuando se scrollea
        // En tablets no queremos ocultar la barra superior al scrollear -> no usar scrollBehavior
        // Evitamos usar scrollBehavior cuando la pantalla destino es el mapa en portrait, para
        // garantizar que la AppBar aparezca al navegar desde una pantalla donde estaba escondida.
        // También evitamos en pantallas con su propio Scaffold para no interferir con su scroll interno.
        val shouldUseScrollBehavior = !isTablet && !(esRutaMapaGlobal && !isLandscape) && !esRutaConScaffoldPropio

        val topAppBarScrollBehavior: TopAppBarScrollBehavior? = if (shouldUseScrollBehavior) {
            TopAppBarDefaults.enterAlwaysScrollBehavior()
        } else {
            null
        }

        val scaffoldModifier = if (topAppBarScrollBehavior != null) {
            Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
        } else {
            Modifier
        }

        Scaffold(
            modifier = scaffoldModifier,
            topBar = {
                // Comprobamos si estamos en la pantalla de slides
                val esRutaSlides = currentDestination?.route == "${Pantalla.Kids.ruta}/slides"

                // Detectamos si estamos en la pantalla del mapa (cualquier variante con query params)
                val esRutaMapa = esRutaMapaGlobal

                // Ocultamos también en Join, Student Lobby y Juego
                val esRutaJoin = currentDestination?.route == "join_game"
                val esRutaStudentLobby =
                    currentDestination?.route?.startsWith("student_lobby") == true
                val esRutaJuego =
                    currentDestination?.route?.startsWith("${Pantalla.Kids.ruta}/game") == true

                // Solo mostramos la barra si se cumplen estas condiciones. Para la pantalla del mapa
                // la regla es especial: siempre visible en vertical (portrait) y nunca en horizontal (landscape).
                val shouldShowTopBar = when {
                    esRutaDetallePoi -> false
                    isProfile -> false
                    esRutaSlides && isLandscape -> false
                    esRutaMapa -> !isLandscape // Mostrar en portrait, ocultar en landscape
                    esRutaConScaffoldPropio || esRutaJoin || esRutaStudentLobby || esRutaJuego -> false
                    else -> true
                }

                if (shouldShowTopBar) {
                    // Si estamos en la pantalla del mapa (en portrait) no le pasamos el scrollBehavior
                    // para forzar que la TopAppBar se muestre aunque vinieras de una pantalla donde
                    // estaba escondida por scroll.
                    val effectiveScrollBehavior = if (esRutaMapa) null else topAppBarScrollBehavior

                    AppTopBar(
                        titulo = stringResource(currentScreen.tituloTopBarResId),
                        subtitulo = currentScreen.subtituloResId?.let { stringResource(it) },
                        onProfileClick = { navControllerPrincipal.navigate(Pantalla.Profile.ruta) },
                        scrollBehavior = effectiveScrollBehavior
                    )
                }
            },

            bottomBar = {
                if (!isProfile) {
                    // Calculamos la altura del NavigationBar según el dispositivo y orientación
                    val navBarHeight = when {
                        isTablet && isLandscape -> 90.dp  // Tablets en horizontal: más grande
                        isLandscape -> 45.dp              // Teléfonos en horizontal: pequeño
                        else -> 75.dp                     // Vertical (cualquier dispositivo): estándar
                    }

                    // Tamaño de iconos y texto adaptativo
                    val iconSize = if (isTablet && isLandscape) 32.dp else 24.dp
                    val textStyle = if (isTablet && isLandscape) {
                        MaterialTheme.typography.labelMedium
                    } else {
                        MaterialTheme.typography.labelSmall
                    }

                Column {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                    NavigationBar(
                        modifier = Modifier.height(navBarHeight),
                        containerColor = MaterialTheme.colorScheme.background,
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
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                pantalla.icono,
                                                contentDescription = stringResource(pantalla.tituloBottomBarResId),
                                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.height(iconSize)
                                            )
                                            Text(
                                                stringResource(pantalla.tituloBottomBarResId),
                                                style = textStyle,
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
            // Creamos una sola instancia compartida del ViewModel aquí (scoped al Composable AppNavigation)
            val sharedViewModel: PointsOfInterestViewModel = hiltViewModel()

            NavHost(
                navController = navControllerPrincipal,
                startDestination = Pantalla.Home.ruta,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Pantalla.Home.ruta) {
                    HomeScreen(
                        onNavigateToImage = { navControllerPrincipal.navigate("image") },
                        onNavigateToPanorama = { navControllerPrincipal.navigate("panorama360") }
                    )
                }
                // nueva ruta para la pantalla de imagen
                composable("image") {
                    ImageScreen(onBackClick = { navControllerPrincipal.popBackStack() })
                }

                // Ruta para la vista panorámica 360°
                composable("panorama360") {
                    Panorama360Screen(onBackClick = { navControllerPrincipal.popBackStack() })
                }

                // Rutas de Puntos de Interés (ahora en el NavHost principal para evitar NavHost anidado)
                composable(Pantalla.PointsOfInterest.ruta) {
                    PointsOfInterestScreen(navController = navControllerPrincipal, viewModel = sharedViewModel)
                }

                composable("${Pantalla.PointsOfInterest.ruta}/detail/{puntoId}") { backStackEntry ->
                    val puntoId = backStackEntry.arguments?.getString("puntoId")
                    val punto = puntosDeInteres.find { it.id == puntoId }
                    if (punto != null) {
                        val visitadosState = sharedViewModel.visitados.collectAsState()
                        val visitados = visitadosState.value
                        val isVisited = punto.id in visitados
                        PointDetailScreen(
                            punto = punto,
                            onBackClick = { navControllerPrincipal.popBackStack() },
                            onOpenMapClick = {
                                navControllerPrincipal.navigate("map?destinoId=${punto.id}") {
                                    launchSingleTop = true
                            }
                        },
                            onOpenSubPointMapClick = { subpunto ->
                                navControllerPrincipal.navigate("map?subPuntoId=${subpunto.id}") {
                                    launchSingleTop = true
                            }
                        },
                            onMarkAsVisited = { sharedViewModel.toggleVisited(punto.id) },
                            isVisited = isVisited
                        )
                    }
                }

                // --- SECCION INFANTIL ---

                // A. Pantalla de Entrada Unificada (Diapositivas + Quiz + Admin)
                composable(Pantalla.Kids.ruta) {
                    KidsEntryScreen(
                        onNavigateToSlides = { navControllerPrincipal.navigate("${Pantalla.Kids.ruta}/slides") },
                        onStartQuiz = { dificultad ->
                            navControllerPrincipal.navigate("${Pantalla.Kids.ruta}/game/${dificultad.name}")
                        },
                        onNavigateToAdmin = {
                            navControllerPrincipal.navigate("admin_lobby")
                        },
                        onJoinGame = {
                            navControllerPrincipal.navigate("join_game")
                        },
                        onReconnectToGame = { route ->
                            navControllerPrincipal.navigate(route) {
                                // No usar inclusive = true para mantener Kids en el backstack
                                popUpTo(Pantalla.Kids.ruta) { inclusive = false }
                            }
                        }
                    )
                }

                composable("map?destinoId={destinoId}&subPuntoId={subPuntoId}") { backStackEntry ->
                    val destinoId = backStackEntry.arguments?.getString("destinoId")
                    val subPuntoId = backStackEntry.arguments?.getString("subPuntoId")

                    val destino = puntosDeInteres.find { it.id == destinoId }

                    // Buscar el subpunto específico que se quiere mostrar
                    val subPunto = if (!subPuntoId.isNullOrEmpty()) {
                        puntosDeInteres
                            .flatMap { it.subpuntos }
                            .find { it.id == subPuntoId }
                    } else {
                        null
                    }

                    MapScreen(
                        destinoInicial = destino,
                        subPuntoInicial = subPunto, // Solo se mostrará si no es null
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

                // B. Juego (Quiz) - Modo Solitario
                composable("${Pantalla.Kids.ruta}/game/{dificultad}") { backStackEntry ->
                    val dificultadString = backStackEntry.arguments?.getString("dificultad")
                    val dificultad = Dificultad.valueOf(dificultadString ?: Dificultad.FACIL.name)

                    QuizGameScreen(
                        dificultad = dificultad,
                        onQuizFinished = {
                            // Vuelve a la pantalla principal de Kids
                            navControllerPrincipal.navigate(Pantalla.Kids.ruta) {
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

                // F. Menú del Profesor (después de introducir contraseña)
                composable("admin_lobby") {
                    val kidsSessionViewModel: com.example.mariamolina.ui.viewmodel.KidsSessionViewModel = hiltViewModel()
                    TeacherMenuScreen(
                        onBack = { navControllerPrincipal.popBackStack() },
                        onLogout = { kidsSessionViewModel.logoutTeacher() },
                        onCreateRoom = {
                            navControllerPrincipal.navigate("teacher_lobby")
                        },
                        onAddQuestions = {
                            navControllerPrincipal.navigate("add_question")
                        }
                    )
                }
                
                // F1. Crear Sala (Profesor) - acepta PIN opcional para reconectar
                composable(
                    route = "teacher_lobby?pin={pin}",
                    arguments = listOf(
                        navArgument("pin") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val existingPin = backStackEntry.arguments?.getString("pin")
                    TeacherLobbyScreen(
                        onBack = {
                            // Navegar a admin_lobby limpiando el backstack del lobby
                            navControllerPrincipal.navigate("admin_lobby") {
                                popUpTo("admin_lobby") { inclusive = true }
                            }
                        },
                        onGameStarted = { pin ->
                            navControllerPrincipal.navigate("teacher_game/$pin") {
                                popUpTo("teacher_lobby?pin={pin}") { inclusive = true }
                            }
                        },
                        existingPin = existingPin
                    )
                }
                
                // F2. Añadir Preguntas (Profesor)
                composable("add_question") {
                    AddQuestionScreen(
                        onBack = { navControllerPrincipal.popBackStack() }
                    )
                }

                // F2. Pantalla del juego del profesor
                composable("teacher_game/{pin}") { backStackEntry ->
                    val pin = backStackEntry.arguments?.getString("pin") ?: ""
                    TeacherGameScreen(
                        pin = pin,
                        onGameFinished = {
                            navControllerPrincipal.navigate("multiplayer_ranking/$pin") {
                                popUpTo("teacher_game/{pin}") { inclusive = true }
                            }
                        }
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
                        onGameStarted = { _ ->
                            // ¡El juego empieza! Navegamos a la pantalla de juego multijugador
                            navControllerPrincipal.navigate("student_game/$pin") {
                                popUpTo("student_lobby/{pin}") { inclusive = true }
                            }
                        }
                    )
                }

                // H2. Pantalla del juego del alumno (multijugador)
                composable("student_game/{pin}") { backStackEntry ->
                    val pin = backStackEntry.arguments?.getString("pin") ?: ""
                    StudentGameScreen(
                        pin = pin,
                        onGameFinished = { finishedPin, posicion, totalJugadores, puntuacion ->
                            // Navegar a la pantalla de rango antes del ranking
                            navControllerPrincipal.navigate(
                                "rank_reward/$finishedPin/$posicion/$totalJugadores/$puntuacion"
                            ) {
                                popUpTo("student_game/{pin}") { inclusive = true }
                            }
                        }
                    )
                }

                // H3. Pantalla de Rango (antes del ranking multijugador)
                composable(
                    route = "rank_reward/{pin}/{posicion}/{total}/{puntuacion}",
                    arguments = listOf(
                        navArgument("pin") { type = NavType.StringType },
                        navArgument("posicion") { type = NavType.IntType },
                        navArgument("total") { type = NavType.IntType },
                        navArgument("puntuacion") { type = NavType.IntType }
                    )
                ) { backStackEntry ->
                    val pin = backStackEntry.arguments?.getString("pin") ?: ""
                    val posicion = backStackEntry.arguments?.getInt("posicion") ?: 1
                    val total = backStackEntry.arguments?.getInt("total") ?: 1
                    val puntuacion = backStackEntry.arguments?.getInt("puntuacion") ?: 0
                    
                    RankRewardScreen(
                        posicion = posicion,
                        total = total,
                        puntuacion = puntuacion,
                        esModoSolitario = false,
                        onContinue = {
                            // Continuar al ranking final
                            navControllerPrincipal.navigate("multiplayer_ranking/$pin") {
                                popUpTo("rank_reward/{pin}/{posicion}/{total}/{puntuacion}") { inclusive = true }
                            }
                        }
                    )
                }

                // I. Ranking Multijugador Final
                composable("multiplayer_ranking/{pin}") { backStackEntry ->
                    val pin = backStackEntry.arguments?.getString("pin") ?: ""
                    MultiplayerRankingScreen(
                        pin = pin,
                        onBackToMenu = {
                            navControllerPrincipal.navigate(Pantalla.Kids.ruta) {
                                popUpTo(Pantalla.Kids.ruta) { inclusive = false }
                            }
                        }
                    )
                }

                composable(Pantalla.Profile.ruta) { ProfileScreen(onBackClick = { navControllerPrincipal.popBackStack() }) }
            }
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
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.cd_ajustes),
                    tint = MaterialTheme.colorScheme.onPrimary
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