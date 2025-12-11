# Política de Privacidad - Pasos de María (María Molina)

**Última actualización:** 11 de diciembre de 2025

## Introducción

Esta aplicación "Pasos de María" (María Molina) es una aplicación educativa diseñada para proporcionar información cultural e histórica sobre el Monasterio de Santa María la Real de las Huelgas y María de Molina.

## Información que recopilamos

### 1. Datos de ubicación
- **Ubicación precisa (GPS):** La aplicación solicita permisos de ubicación para:
  - Mostrar tu ubicación en el mapa de puntos de interés
  - Calcular rutas desde tu ubicación hasta los puntos de interés
  - Estos datos NO se envían a servidores externos ni se almacenan permanentemente
  - La ubicación solo se usa localmente en tu dispositivo mientras usas la función del mapa

### 2. Datos de uso de la aplicación
- **Progreso de aprendizaje:** La aplicación guarda localmente en tu dispositivo:
  - Puntos de interés visitados
  - Progreso en cuestionarios
  - Preferencias de idioma y tamaño de fuente
  - Estos datos se almacenan únicamente en tu dispositivo usando SharedPreferences y DataStore

### 3. Conexión a Internet
- La aplicación requiere conexión a Internet para:
  - Cargar imágenes de los puntos de interés
  - Calcular rutas en el mapa (usando servicios de routing externos: OSRM o BRouter)
  - Funcionalidades del cuestionario multijugador (Firebase Firestore)
  - Traducción automática de contenido (ML Kit de Google)

### 4. Datos de Firebase
- **Firebase Authentication:** Si usas la sección de profesor, se crea una cuenta anónima
- **Firebase Firestore:** Para el cuestionario multijugador, se almacenan:
  - Códigos de sala/PIN de juego
  - Nombres de jugadores (proporcionados voluntariamente)
  - Respuestas y puntuaciones del juego
  - Estos datos se eliminan automáticamente al finalizar la sesión de juego

## Cómo usamos la información

La información recopilada se usa exclusivamente para:
- Proporcionar las funcionalidades de la aplicación
- Mejorar la experiencia del usuario
- Mostrar tu ubicación en el mapa
- Guardar tu progreso de aprendizaje

## Compartir información

**NO compartimos, vendemos ni transferimos tu información personal a terceros**, excepto:
- Servicios de Firebase de Google (para funcionalidad multijugador) según sus términos de servicio
- Servicios de routing de mapas (OSRM, BRouter) que reciben coordenadas GPS temporalmente para calcular rutas

## Servicios de terceros

La aplicación utiliza los siguientes servicios de terceros:
1. **Firebase (Google):**
   - Authentication
   - Firestore
   - Crashlytics
   - Política: https://firebase.google.com/support/privacy

2. **OpenStreetMap (mapas):**
   - Política: https://wiki.osmfoundation.org/wiki/Privacy_Policy

3. **OSRM / BRouter (cálculo de rutas):**
   - Servicios públicos de routing
   - Solo reciben coordenadas temporalmente para calcular rutas

4. **ML Kit (Google):**
   - Traducción automática de textos
   - Procesamiento local en el dispositivo

## Seguridad

Implementamos medidas de seguridad razonables para proteger tu información:
- Comunicaciones cifradas (HTTPS)
- Almacenamiento local seguro en el dispositivo
- No almacenamos contraseñas en texto plano

## Privacidad de menores

Esta aplicación es apta para todas las edades y especialmente diseñada para uso educativo con menores. La sección infantil:
- NO requiere registro ni proporcionar información personal
- Los nombres en el juego multijugador son opcionales y pueden ser pseudónimos
- NO recopilamos información personal de menores
- Recomendamos supervisión parental

## Tus derechos

Tienes derecho a:
- Acceder a tus datos (almacenados localmente en tu dispositivo)
- Eliminar tus datos (desinstalando la aplicación o limpiando datos de la app)
- Revocar permisos de ubicación en cualquier momento desde los ajustes del dispositivo

## Cambios a esta política

Podemos actualizar esta política de privacidad ocasionalmente. Te notificaremos de cambios significativos mediante:
- Actualización de la fecha de "Última actualización"
- Notificación en la aplicación (si es significativo)

## Contacto

Para preguntas sobre esta política de privacidad o sobre tus datos:
- **Email:** mariamolina.app@gmail.com
- **Institución:** Universidad de Valladolid

## Consentimiento

Al usar esta aplicación, aceptas esta política de privacidad.

