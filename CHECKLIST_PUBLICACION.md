# ✅ CHECKLIST FINAL - PUBLICACIÓN GOOGLE PLAY

## 🎯 ESTADO ACTUAL


### ✅ TAREA 2: PREPARACIÓN GOOGLE PLAY
- [x] Configuración de firma preparada
- [x] ProGuard completo
- [x] Minificación activada
- [x] Versioning configurado (1.0.0)
- [x] Guía completa documentada

---

## 📦 PASOS PARA GENERAR APK/AAB FIRMADO

### PASO 1: Generar Keystore (PRIMERA VEZ SOLAMENTE)
```powershell
# Abre PowerShell y ejecuta:
keytool -genkey -v -keystore mariamolina-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias mariamolina-key
```

**Guarda esto de forma segura:**
- Archivo: `mariamolina-release-key.jks`
- Contraseña del keystore: [LA QUE INGRESES]
- Contraseña de la clave: [LA QUE INGRESES]
- Alias: `mariamolina-key`

### PASO 2: Configurar credenciales

**Opción A - Variables de entorno (RECOMENDADO):**
```powershell
[System.Environment]::SetEnvironmentVariable('KEYSTORE_FILE', 'C:\ruta\completa\mariamolina-release-key.jks', 'User')
[System.Environment]::SetEnvironmentVariable('KEYSTORE_PASSWORD', 'tu_contraseña_keystore', 'User')
[System.Environment]::SetEnvironmentVariable('KEY_ALIAS', 'mariamolina-key', 'User')
[System.Environment]::SetEnvironmentVariable('KEY_PASSWORD', 'tu_contraseña_clave', 'User')

# Reinicia Android Studio después de esto
```

**Opción B - gradle.properties:**
Crea/edita `gradle.properties` en la raíz del proyecto:
```properties
KEYSTORE_FILE=C:/ruta/completa/mariamolina-release-key.jks
KEYSTORE_PASSWORD=tu_contraseña_keystore
KEY_ALIAS=mariamolina-key
KEY_PASSWORD=tu_contraseña_clave
```

### PASO 3: Descomentar configuración de firma

En `app/build.gradle.kts`, busca y descomenta:

```kotlin
// En signingConfigs > create("release"), DESCOMENTAR:
storeFile = file(System.getenv("KEYSTORE_FILE") ?: project.property("KEYSTORE_FILE") as String)
storePassword = System.getenv("KEYSTORE_PASSWORD") ?: project.property("KEYSTORE_PASSWORD") as String
keyAlias = System.getenv("KEY_ALIAS") ?: project.property("KEY_ALIAS") as String
keyPassword = System.getenv("KEY_PASSWORD") ?: project.property("KEY_PASSWORD") as String

// En buildTypes > release, DESCOMENTAR:
signingConfig = signingConfigs.getByName("release")
```

### PASO 4: Compilar

```powershell
# Para Google Play (RECOMENDADO):
.\gradlew bundleRelease

# O para APK:
.\gradlew assembleRelease
```

**Ubicación del archivo generado:**
- AAB: `app\build\outputs\bundle\release\app-release.aab`
- APK: `app\build\outputs\apk\release\app-release.apk`

---

## 🎨 RECURSOS GRÁFICOS NECESARIOS

### Crear antes de subir a Google Play:

- [ ] **Icono 512x512px** (ya existe en `mipmap`)
- [ ] **2-8 Capturas de pantalla** (1080x1920px o 1920x1080px)
  - Pantalla principal
  - Mapa interactivo
  - Punto de interés
  - Test infantil
  - Vista 360°
- [ ] **Gráfico destacado** (1024x500px)
- [ ] **Banner de TV** (opcional, 1280x720px)

### Capturas recomendadas:
1. Pantalla principal con menú de navegación
2. Lista de puntos de interés
3. Detalle de un punto con imagen
4. Mapa con ubicación y ruta
5. Sección infantil - selección de dificultad
6. Vista panorámica 360°
7. Configuración de idioma
8. Cuestionario multijugador

---

## 📄 DOCUMENTOS PARA GOOGLE PLAY CONSOLE

### Política de Privacidad
- [ ] Editar `PRIVACY_POLICY.md`
- [ ] Reemplazar `[TU_EMAIL_DE_CONTACTO]`
- [ ] Reemplazar `[NOMBRE_INSTITUCIÓN_EDUCATIVA]`
- [ ] Subir a web pública (GitHub Pages, servidor, etc.)
- [ ] Obtener URL pública (ej: `https://tu-usuario.github.io/mariamolina/PRIVACY_POLICY.html`)

### Textos preparados (copiar de GOOGLE_PLAY_GUIDE.md):
- [ ] Descripción corta (80 caracteres)
- [ ] Descripción completa (4000 caracteres)
- [ ] Notas de la versión

---

## 🏪 GOOGLE PLAY CONSOLE - CONFIGURACIÓN

### Información básica
- [ ] Nombre: "Pasos de María - María Molina"
- [ ] Descripción corta: [Copiar de guía]
- [ ] Descripción completa: [Copiar de guía]
- [ ] Categoría: **Educación**
- [ ] Etiquetas: Historia, Cultura, Educación, Monasterio
- [ ] Tipo: **Aplicación**
- [ ] Precio: **Gratis**
- [ ] Email de contacto: [TU EMAIL]

### Recursos gráficos
- [ ] Subir icono 512x512px
- [ ] Subir capturas de pantalla (mínimo 2)
- [ ] Subir gráfico destacado
- [ ] (Opcional) Captura tablet
- [ ] (Opcional) Banner TV

### Política de privacidad
- [ ] URL: [TU URL PÚBLICA]

### Clasificación de contenido
- [ ] Completar cuestionario
- [ ] Categoría: **Educación**
- [ ] Violencia: **No**
- [ ] Contenido sexual: **No**
- [ ] Lenguaje: **No ofensivo**
- [ ] Drogas: **No**
- [ ] Apuestas: **No**
- Resultado esperado: **PEGI 3 / Everyone**

### Seguridad de datos
- [ ] ¿Recopila datos? **Sí**
- [ ] Ubicación aproximada: **Sí** (para mapa)
- [ ] Ubicación precisa: **Sí** (para rutas)
- [ ] Preferencias: **Sí** (idioma, fuente)
- [ ] Datos de juego: **Sí** (puntuaciones)
- [ ] ¿Cifrados en tránsito? **Sí**
- [ ] ¿Se pueden eliminar? **Sí**
- [ ] ¿Se comparten? **Sí** (Firebase, servicios de mapas)

### Países y regiones
- [ ] Seleccionar: **España** (principal)
- [ ] Opcional: Más países de habla hispana

### Precios y distribución
- [ ] Precio: **Gratis**
- [ ] Contiene anuncios: **No**
- [ ] Compras dentro de la app: **No**
- [ ] Dirigida principalmente a niños: **No** (es para todas las edades)

---

## 🚀 SUBIR A GOOGLE PLAY

### Última verificación antes de subir:
- [ ] AAB/APK generado correctamente
- [ ] Todos los recursos gráficos listos
- [ ] Política de privacidad accesible públicamente
- [ ] Textos revisados y sin errores
- [ ] Email de contacto configurado
- [ ] Cuenta de Google Play Console activa (25 USD)

### Proceso de subida:
1. [ ] Login en [Google Play Console](https://play.google.com/console)
2. [ ] Crear nueva aplicación
3. [ ] Completar toda la información (usar este checklist)
4. [ ] Ir a "Producción" → "Crear nueva versión"
5. [ ] Subir AAB firmado
6. [ ] Completar notas de la versión
7. [ ] Revisar toda la ficha
8. [ ] Click "Enviar para revisión"

### Después de enviar:
- [ ] Esperar notificación por email (1-7 días)
- [ ] Si es rechazada: corregir y reenviar
- [ ] Si es aprobada: ¡Felicidades! 🎉

---

## ⏱️ TIEMPOS ESTIMADOS

| Tarea | Tiempo estimado |
|-------|----------------|
| Generar keystore | 5 minutos |
| Configurar credenciales | 5 minutos |
| Compilar AAB | 2-5 minutos |
| Crear capturas de pantalla | 30-60 minutos |
| Publicar política privacidad | 10-30 minutos |
| Completar Google Play Console | 60-90 minutos |
| Revisión de Google | 1-7 días |
| **TOTAL (tu tiempo)** | **~3 horas** |

---

## 🆘 PROBLEMAS COMUNES

### "No se encuentra el keystore"
✅ Verifica que la ruta sea absoluta y correcta
✅ Usa barras `/` en lugar de `\` en Windows

### "Contraseña incorrecta"
✅ Verifica KEYSTORE_PASSWORD y KEY_PASSWORD
✅ Reinicia Android Studio después de configurar variables

### "Error de ProGuard"
✅ Ya está configurado correctamente
✅ Si hay error, revisa `app/proguard-rules.pro`

### "AAB muy grande"
✅ La minificación ya está activada
✅ El tamaño debería ser ~15-30MB

### "Política de privacidad no accesible"
✅ Verifica que la URL sea pública (no localhost)
✅ Prueba abrir la URL en navegador de incógnito

---

## ✅ CONFIRMACIÓN FINAL

Antes de hacer click en "Enviar para revisión":

- [ ] He probado la app en al menos 2 dispositivos diferentes
- [ ] Todas las funcionalidades funcionan correctamente
- [ ] La contraseña "6906" funciona
- [ ] El mapa y las rutas funcionan
- [ ] Las vistas 360° se cargan
- [ ] El cuestionario multijugador funciona
- [ ] Los idiomas cambian correctamente
- [ ] He leído toda la política de privacidad
- [ ] Todos los textos están revisados
- [ ] Las capturas de pantalla son de buena calidad
- [ ] El email de contacto es correcto y lo monitoreo
- [ ] Tengo backup del keystore guardado de forma segura

---

## 📞 RECURSOS DE AYUDA

- **Guía completa:** `GOOGLE_PLAY_GUIDE.md`
- **Política de privacidad:** `PRIVACY_POLICY.md`
- **Resumen de cambios:** `RESUMEN_CAMBIOS.md`
- **Documentación oficial:** https://support.google.com/googleplay/android-developer
- **Firma de apps:** https://developer.android.com/studio/publish/app-signing

---

**¡Éxito con tu publicación! 🚀**

*Última actualización: 11 de diciembre de 2025*

