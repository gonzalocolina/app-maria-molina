package com.example.mariamolina

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.LocaleList
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.mariamolina.ui.navigation.AppNavigation
import com.example.mariamolina.ui.theme.MariaMolinaTheme
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import java.util.Locale
import coil.imageLoader
import coil.request.ImageRequest
import com.example.mariamolina.data.model.SlidesProvider
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val language = newBase.getSharedPreferences("app_prefs", MODE_PRIVATE).getString("language", "es") ?: "es"
        val locale = Locale.forLanguageTag(language)
        val config = newBase.resources.configuration
        config.setLocales(LocaleList(locale))
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Instalar el SplashScreen ANTES de super.onCreate()
        installSplashScreen()

        super.onCreate(savedInstanceState)
        val ctx = applicationContext
        org.osmdroid.config.Configuration.getInstance().load(
            ctx,
            ctx.getSharedPreferences("osmdroid", MODE_PRIVATE)
        )

        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { /* no hace falta manejar nada */ }

        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isNotEmpty()) {
            requestPermissionLauncher.launch(notGranted.toTypedArray())
        }

        // Prefetch de las imágenes remotas de las diapositivas
        prefetchSlideImages(applicationContext)

        val fontSizeScale = when (getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getString("font_size", "normal")) {
            "normal" -> 1.0f
            "large" -> 1.2f
            "very_large" -> 1.5f
            else -> 1.0f
        }

        setContent {
            MariaMolinaTheme(fontSizeScale = fontSizeScale) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }

    // Función que recorre las slides y encola peticiones de Coil para cachear las imágenes remotas
    private fun prefetchSlideImages(context: Context) {
        try {
            val loader = context.imageLoader
            SlidesProvider.testSlides.forEach { slide ->
                val url = slide.imageUrl
                if (url.startsWith("http") || url.contains("://")) {
                    val req = ImageRequest.Builder(context)
                        .data(url)
                        .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                        .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                        .build()
                    loader.enqueue(req)
                }
            }
        } catch (e: Exception) {
            // No queremos romper el arranque si algo falla en prefetch; sólo logueamos
            e.printStackTrace()
        }
    }
}
