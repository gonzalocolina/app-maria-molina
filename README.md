# María Molina App

Aplicación educativa para Android que permite conocer la figura histórica de María de Molina y los lugares de interés relacionados con ella en Valladolid.

## Características

- 🗺️ **Mapa interactivo** con puntos de interés históricos
- 📍 **Navegación GPS** hacia los lugares de María Molina
- 🎮 **Quiz educativo** en modo solitario y multijugador
- 👥 **Modo profesor** para crear partidas en clase
- 🌓 **Tema claro/oscuro** adaptativo
- 📱 **Soporte para tablets** y diferentes orientaciones

## Tecnologías

- **Kotlin** + **Jetpack Compose**
- **Firebase** (Auth, Firestore, Crashlytics)
- **Hilt** para inyección de dependencias
- **OpenStreetMap** para mapas

## Instalación

1. Clona el repositorio:
   ```bash
   git clone https://gitlab.inf.uva.es/goncoli/edunovamariamolina.git
   ```

2. Abre el proyecto en Android Studio

3. Sincroniza Gradle y ejecuta la app

## Política de Privacidad

📜 **[Ver Política de Privacidad](https://goncoli.pages.gitlab.inf.uva.es/edunovamariamolina)**

La política de privacidad está disponible en línea y cumple con los requisitos de Google Play Store.

## Estructura del Proyecto

```
app/src/main/java/com/example/mariamolina/
├── data/
│   ├── model/          # Modelos de datos
│   └── repository/     # Repositorios
├── di/                 # Módulos de Hilt
├── ui/
│   ├── navigation/     # Navegación
│   ├── screens/        # Pantallas
│   ├── theme/          # Tema Material 3
│   └── viewmodel/      # ViewModels
└── MariaMolinaApplication.kt
```

## Publicación en Google Play

### Requisitos previos

1. ✅ Política de privacidad publicada
2. ⬜ Cambiar `applicationId` de `com.example.*` a uno definitivo
3. ⬜ Generar keystore de firma para release
4. ⬜ Configurar firma en `build.gradle.kts`
5. ⬜ Crear cuenta de desarrollador en Google Play Console

### Generar APK/AAB de release

```bash
./gradlew bundleRelease
```

## Licencia

© 2025 Universidad de Valladolid

## Contacto

- **Email:** mariamolina.app@gmail.com
- **Institución:** Universidad de Valladolid

