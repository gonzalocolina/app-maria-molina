# María Molina App

Aplicación educativa para Android que permite conocer la figura histórica de María de Molina y los lugares de interés relacionados con ella en Valladolid disponible en [Play Store](https://play.google.com/store/apps/details?id=com.edunova.mariamolina).

## Características

- 🗺️ **Mapa interactivo** con puntos de interés históricos
- 📍 **Navegación GPS** hacia los lugares de María Molina
- 🎮 **Quiz educativo** en modo solitario y multijugador
- 👥 **Modo profesor** para crear partidas en clase
- 🌓 **Tema claro/oscuro** adaptativo
- 📱 **Soporte para tablets** y diferentes orientaciones
- 🌍 Implementada por completo en en **español, inglés, francés y alemán**

## Tecnologías

- **Kotlin** + **Jetpack Compose**
- **Firebase** (Auth, Firestore, Crashlytics)
- **Hilt** para inyección de dependencias
- **OpenStreetMap** para mapas

## Instalación
Puedes descargar la aplicación en [Play Store](https://play.google.com/store/apps/details?id=com.edunova.mariamolina).

También existe la opción de instalarla mediante el repositorio siguiendo estos pasos:
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


## Licencia

© 2025 Universidad de Valladolid

## Contacto

- **Email:** mariamolina.app@gmail.com
- **Institución:** Universidad de Valladolid

