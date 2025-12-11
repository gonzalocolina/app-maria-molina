# Pasos de María - Guía para publicación en Google Play

## ✅ Tarea 1: Contraseña corregida
- **Problema resuelto:** La contraseña del test individual infantil ahora es "6906" (antes era "6909")
- **Archivo modificado:** `KidsEntryScreen.kt`
- **Mejoras adicionales:**
  - Ahora muestra error si la contraseña es incorrecta
  - Los diálogos se cierran correctamente al ingresar

---

## 📋 Checklist para publicar en Google Play

### PASO 1: Generar Keystore (Firma de la aplicación)

```bash
# En Windows PowerShell o CMD:
keytool -genkey -v -keystore mariamolina-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias mariamolina-key

# Responde las preguntas:
# - Contraseña del keystore: [GUARDA ESTO DE FORMA SEGURA]
# - Nombre, organización, ciudad, etc.
# - Contraseña de la clave (alias): [PUEDE SER LA MISMA O DIFERENTE]
```

**IMPORTANTE:** 
- Guarda el archivo `.jks` en un lugar seguro (NO lo subas a git)
- Guarda las contraseñas en un gestor de contraseñas
- Si pierdes el keystore, NO podrás actualizar la app en Google Play

### PASO 2: Configurar las variables de firma

Edita `gradle.properties` (local, NO subir a git) y añade:

```properties
KEYSTORE_FILE=C:/ruta/completa/a/mariamolina-release-key.jks
KEYSTORE_PASSWORD=tu_contraseña_del_keystore
KEY_ALIAS=mariamolina-key
KEY_PASSWORD=tu_contraseña_de_la_clave
```

**O usa variables de entorno del sistema:**

```powershell
# En Windows PowerShell:
[System.Environment]::SetEnvironmentVariable('KEYSTORE_FILE', 'C:\ruta\a\mariamolina-release-key.jks', 'User')
[System.Environment]::SetEnvironmentVariable('KEYSTORE_PASSWORD', 'tu_contraseña', 'User')
[System.Environment]::SetEnvironmentVariable('KEY_ALIAS', 'mariamolina-key', 'User')
[System.Environment]::SetEnvironmentVariable('KEY_PASSWORD', 'tu_contraseña', 'User')
```

### PASO 3: Descomentar la configuración de firma

En `app/build.gradle.kts`, descomenta estas líneas:

```kotlin
// En signingConfigs > create("release"):
storeFile = file(System.getenv("KEYSTORE_FILE") ?: project.property("KEYSTORE_FILE") as String)
storePassword = System.getenv("KEYSTORE_PASSWORD") ?: project.property("KEYSTORE_PASSWORD") as String
keyAlias = System.getenv("KEY_ALIAS") ?: project.property("KEY_ALIAS") as String
keyPassword = System.getenv("KEY_PASSWORD") ?: project.property("KEY_PASSWORD") as String

// En buildTypes > release:
signingConfig = signingConfigs.getByName("release")
```

### PASO 4: Generar el APK/AAB firmado

```bash
# Generar Android App Bundle (AAB) - RECOMENDADO para Google Play:
./gradlew bundleRelease

# O generar APK firmado:
./gradlew assembleRelease
```

El archivo generado estará en:
- **AAB:** `app/build/outputs/bundle/release/app-release.aab`
- **APK:** `app/build/outputs/apk/release/app-release.apk`

### PASO 5: Preparar recursos gráficos para Google Play

Necesitas crear:

1. **Icono de la aplicación:** 512x512px PNG (ya lo tienes en `mipmap`)
2. **Captura de pantalla del teléfono:** Mínimo 2, máximo 8 (1080x1920px o similar)
3. **Captura de pantalla de tablet 7":** Opcional pero recomendado (1200x1920px)
4. **Gráfico destacado:** 1024x500px PNG o JPG
5. **Banner de TV:** Opcional (1280x720px) - solo si soportas Android TV

**Capturas recomendadas:**
- Pantalla principal con menú
- Mapa con puntos de interés
- Vista de un punto de interés
- Cuestionario infantil
- Vista panorámica 360°

### PASO 6: Preparar textos para Google Play Console

#### Descripción corta (máx. 80 caracteres):
```
Explora el Monasterio de las Huelgas y aprende sobre María de Molina
```

#### Descripción completa (máx. 4000 caracteres):
```
Descubre la historia del Monasterio de Santa María la Real de las Huelgas y la vida de María de Molina, reina de Castilla, a través de una experiencia educativa interactiva.

🏰 CARACTERÍSTICAS PRINCIPALES:

✨ Puntos de Interés Interactivos
Explora más de 10 ubicaciones del monasterio con información detallada, imágenes y contexto histórico. Marca los lugares que has visitado y sigue tu progreso.

🗺️ Mapa Interactivo con Rutas
Encuentra cada punto de interés en el mapa y obtén direcciones desde tu ubicación actual. Visualiza rutas optimizadas para recorrer el monasterio.

📸 Vistas Panorámicas 360°
Sumérgete en una experiencia inmersiva con vistas panorámicas en 360° del interior del monasterio.

👶 Sección Infantil Educativa
Modo especial para niños con:
- Cuestionarios adaptados por dificultad (Fácil, Media, Difícil)
- Diapositivas educativas con ilustraciones
- Modo multijugador para jugar en grupo

🎮 Cuestionario Multijugador
Los profesores pueden crear salas de juego con PIN para que múltiples estudiantes compitan en tiempo real, perfecto para visitas escolares.

🌍 Multiidioma
Disponible en Español, Inglés, Alemán y Francés con traducción automática de contenido.

♿ Accesibilidad
- Tamaños de letra ajustables
- Modo oscuro disponible
- Compatible con lectores de pantalla

📚 Ideal Para:
- Estudiantes de historia
- Visitantes del monasterio
- Grupos escolares
- Turistas
- Investigadores
- Amantes del patrimonio cultural

Esta aplicación es un proyecto educativo desarrollado para enriquecer la experiencia de visita al Monasterio de Santa María la Real de las Huelgas y divulgar la figura histórica de María de Molina.

---
No requiere registro para funciones básicas. La sección de profesor y juegos multijugador requieren autenticación mínima.
```

### PASO 7: Publicar política de privacidad

1. **Opción A - GitHub Pages (Gratis):**
   - Sube `PRIVACY_POLICY.md` a un repositorio público en GitHub
   - Activa GitHub Pages en la configuración del repositorio
   - URL será: `https://tu-usuario.github.io/nombre-repo/PRIVACY_POLICY.html`

2. **Opción B - Servidor web propio:**
   - Sube el archivo a tu servidor web
   - Asegúrate de que sea accesible públicamente

3. **Completa en Google Play Console:**
   - Ve a "Política de privacidad"
   - Pega la URL pública de tu política

### PASO 8: Completar el cuestionario de seguridad de datos

En Google Play Console > "Seguridad de datos", responde:

**¿Recopila datos?** Sí

**Tipos de datos:**
- ✅ Ubicación aproximada (para mapa)
- ✅ Ubicación precisa (para rutas)
- ✅ Preferencias del usuario (idioma, tamaño de fuente)
- ✅ Información del juego (puntuaciones, progreso)

**¿Los datos se cifran en tránsito?** Sí (HTTPS)

**¿Los datos se pueden eliminar?** Sí (desinstalando la app)

**¿Se comparten datos con terceros?** Sí
- Firebase (Google) para funcionalidad de juego
- Servicios de mapas (solo coordenadas temporales)

### PASO 9: Configurar clasificación de contenido

Completa el cuestionario de clasificación (Content Rating):
- **Categoría:** Educación
- **Violencia:** No
- **Contenido sexual:** No
- **Lenguaje:** No ofensivo
- **Apuestas:** No
- **Drogas/alcohol:** No

Resultado esperado: **PEGI 3 / Everyone**

### PASO 10: Preparar la ficha de Google Play Console

1. **Categoría:** Educación
2. **Etiquetas:** Historia, Cultura, Educación, Monasterio, Patrimonio
3. **Sitio web:** (opcional)
4. **Correo de contacto:** [REQUERIDO]
5. **Tipo de aplicación:** Aplicación
6. **Precio:** Gratis

### PASO 11: Subir el AAB/APK

1. Ve a "Producción" en Google Play Console
2. Crea una nueva versión
3. Sube el archivo `app-release.aab`
4. Completa las notas de la versión:

**Notas de la versión v1.0.0:**
```
🎉 Primera versión pública

Características:
✨ 10+ puntos de interés del Monasterio de las Huelgas
🗺️ Mapa interactivo con rutas
📸 Vista panorámica 360°
👶 Sección infantil educativa
🎮 Cuestionarios individuales y multijugador
🌍 Soporte multiidioma (ES, EN, DE, FR)
♿ Opciones de accesibilidad
```

### PASO 12: Enviar para revisión

1. Revisa toda la información
2. Click en "Enviar para revisión"
3. Espera 1-7 días para la aprobación de Google

---

## 🔧 Cambios técnicos realizados

### ✅ Completados:

1. ✅ **Contraseña corregida:** "6906" en test infantil
2. ✅ **ProGuard configurado:** Reglas completas para Firebase, Hilt, Compose, etc.
3. ✅ **Minificación habilitada:** R8 activado para reducir tamaño del APK
4. ✅ **Firma configurada:** Estructura lista (falta generar keystore)
5. ✅ **Versioning:** v1.0.0 con versionCode 1
6. ✅ **Política de privacidad:** Documento completo creado
7. ✅ **Build variants:** Debug y Release configurados
8. ✅ **Network security:** Configuración para OSRM y BRouter
9. ✅ **Comentarios añadidos:** Documentación en manifest sobre permisos

### ⚠️ Pendientes (requieren acción manual):

1. ⚠️ **Generar keystore:** Ejecutar comando keytool
2. ⚠️ **Configurar variables de firma:** Editar gradle.properties o env variables
3. ⚠️ **Descomentar signingConfig:** En build.gradle.kts
4. ⚠️ **Publicar política de privacidad:** Subir a web pública
5. ⚠️ **Crear recursos gráficos:** Capturas de pantalla, gráfico destacado
6. ⚠️ **Actualizar datos de contacto:** En PRIVACY_POLICY.md y Google Play Console
7. ⚠️ **Crear cuenta de Google Play Console:** Si no existe (25 USD única vez)
8. ⚠️ **Completar cuestionarios:** En Google Play Console

### 🔧 Correcciones adicionales aplicadas:

1. ✅ **Eliminado applicationIdSuffix:** El variant debug ya no añade `.debug` al package name para evitar conflictos con Firebase
2. ✅ **Eliminado archivesBaseName:** Propiedad obsoleta que causaba error en Gradle moderno

---

## 📊 Información del proyecto

- **Package ID:** com.example.mariamolina
- **Version Code:** 1
- **Version Name:** 1.0.0
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 36 (Android 16)
- **Compile SDK:** 36

---

## 🆘 Solución de problemas

### Error: "No se encuentra el keystore"
**Solución:** Verifica que la ruta en KEYSTORE_FILE sea absoluta y correcta

### Error: "Contraseña incorrecta del keystore"
**Solución:** Verifica las variables KEYSTORE_PASSWORD y KEY_PASSWORD

### Error de ProGuard/R8 en release
**Solución:** Revisa `app/proguard-rules.pro` - las reglas están configuradas

### El AAB es muy grande (>150MB)
**Solución:** La minificación ya está activada. Si sigue siendo grande:
```kotlin
android {
    bundle {
        language {
            enableSplit = true
        }
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
    }
}
```

---

## 📝 Notas finales

- **IMPORTANTE:** NO subas el keystore ni las contraseñas a git
- Añade a `.gitignore`:
  ```
  *.jks
  *.keystore
  gradle.properties
  local.properties
  ```
- Mantén backup del keystore en lugar seguro
- Incrementa versionCode en cada actualización
- Sigue versionName semántico (Major.Minor.Patch)

---

## 📞 Contacto

Para dudas sobre la publicación, consulta:
- [Documentación oficial de Google Play](https://support.google.com/googleplay/android-developer)
- [Guía de firma de apps](https://developer.android.com/studio/publish/app-signing)

