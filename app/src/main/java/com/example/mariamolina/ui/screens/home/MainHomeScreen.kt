package com.example.mariamolina.ui.screens.home


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mariamolina.ui.screens.home.HomeScreen
import com.example.mariamolina.ui.screens.home.ImageScreen

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(onNavigateToImage = { navController.navigate("image") })
        }
        composable("image") {
            ImageScreen(onBackClick = { navController.popBackStack() })
        }
    }
}
