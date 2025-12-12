# Política de Privacidad - María Molina App

**Última actualización:** 12 de diciembre de 2025

## Introducción

Esta Política de Privacidad describe cómo la aplicación **María Molina** ("nosotros", "la aplicación") recopila, utiliza y protege la información de los usuarios ("usted", "usuario").

La aplicación María Molina es una herramienta educativa diseñada para dar a conocer la figura histórica de María Molina y los lugares de interés relacionados con ella.

## Información que Recopilamos

### 1. Datos de Autenticación Anónima
- Utilizamos **Firebase Authentication** para crear sesiones anónimas.
- Se genera un identificador único (UID) para cada dispositivo.
- **No recopilamos** nombres reales, correos electrónicos ni contraseñas.

### 2. Datos de Usuario
- **Nickname/Alias:** Si el usuario decide participar en el modo multijugador (quiz), puede introducir un nombre de usuario o alias.
- Este dato se almacena en **Firebase Firestore** y se asocia únicamente al UID anónimo.

### 3. Datos de Ubicación
- La aplicación puede solicitar acceso a la **ubicación del dispositivo** (GPS).
- Este permiso es **opcional** y se utiliza únicamente para:
  - Mostrar su posición en el mapa interactivo.
  - Calcular rutas hacia los puntos de interés.
- **Los datos de ubicación NO se almacenan en nuestros servidores.**
- La ubicación se procesa localmente en el dispositivo.

### 4. Datos de Rendimiento y Errores
- Utilizamos **Firebase Crashlytics** para recopilar información sobre errores y fallos de la aplicación.
- Estos datos incluyen:
  - Tipo de dispositivo y versión del sistema operativo.
  - Estado de la aplicación en el momento del error.
  - Trazas de errores (stack traces).
- Esta información se utiliza exclusivamente para mejorar la estabilidad de la aplicación.

### 5. Datos del Quiz/Juego
- Si participa en el modo de juego (quiz), se almacenan temporalmente:
  - Puntuaciones obtenidas.
  - Progreso en las partidas.
- Estos datos se eliminan al finalizar la sesión de juego.

## Cómo Utilizamos la Información

La información recopilada se utiliza para:

1. **Proporcionar funcionalidades de la aplicación:** Mostrar mapas, puntos de interés y contenido educativo.
2. **Habilitar el modo multijugador:** Gestionar partidas y mostrar rankings.
3. **Mejorar la aplicación:** Identificar y corregir errores técnicos.
4. **Personalizar la experiencia:** Recordar preferencias del usuario (tema oscuro/claro).

## Almacenamiento y Seguridad

- Los datos se almacenan en servidores de **Google Firebase**, ubicados en la Unión Europea.
- Implementamos medidas de seguridad estándar de la industria para proteger la información.
- Los datos de autenticación anónima se gestionan de forma segura mediante Firebase.

## Compartición de Datos

**No vendemos, comercializamos ni transferimos** su información personal a terceros.

Los únicos servicios de terceros que procesan datos son:

| Servicio | Propósito | Política de Privacidad |
|----------|-----------|------------------------|
| Firebase Authentication | Autenticación anónima | [Política de Google](https://policies.google.com/privacy) |
| Firebase Firestore | Almacenamiento de datos | [Política de Google](https://policies.google.com/privacy) |
| Firebase Crashlytics | Informes de errores | [Política de Google](https://policies.google.com/privacy) |
| OpenStreetMap | Mapas y cartografía | [Política de OSM](https://wiki.osmfoundation.org/wiki/Privacy_Policy) |

## Derechos del Usuario

Usted tiene derecho a:

- **Acceder** a sus datos personales.
- **Rectificar** información incorrecta.
- **Eliminar** sus datos (desinstalando la aplicación, los datos anónimos asociados quedarán huérfanos y serán eliminados periódicamente).
- **Revocar permisos** de ubicación en cualquier momento desde la configuración de su dispositivo.

## Uso por Menores de Edad

Esta aplicación está diseñada para uso educativo y puede ser utilizada por menores de edad bajo supervisión de padres, tutores o educadores.

- No recopilamos intencionadamente información personal de menores.
- El modo de juego multijugador requiere únicamente un alias/nickname, sin datos personales reales.

## Cambios en esta Política

Podemos actualizar esta Política de Privacidad ocasionalmente. Notificaremos cualquier cambio significativo mediante:

- Actualización de la fecha de "Última actualización" en esta página.
- Notificación dentro de la aplicación si los cambios son sustanciales.

## Contacto

Si tiene preguntas sobre esta Política de Privacidad, puede contactarnos en:

- **Email:** [mariamolina.app@gmail.com](mailto:mariamolina.app@gmail.com)
- **Institución:** Universidad de Valladolid

## Consentimiento

Al utilizar nuestra aplicación, usted acepta los términos de esta Política de Privacidad.

---

© 2025 María Molina App - Universidad de Valladolid

