# Induztek Protocolos

Aplicación móvil Android para **formalizar en terreno los protocolos de prueba eléctrica** de Induztek Ingeniería. Reemplaza el papel y el Excel: el técnico registra las mediciones en el lugar, la app calcula el resultado de cada prueba con las reglas del caso, guarda el historial por equipo y permite compararlo con pruebas anteriores. Funciona sin conexión y usa **solo datos ficticios**.

Proyecto de la asignatura **DSY1105 · Desarrollo de Aplicaciones Móviles** (Evaluación Parcial 2), Duoc UC.

## Integrantes
- Vicente Bravo — vic.bravoe@duocuc.cl
- Juan Carlos Hernández — juc.hernandez@duocuc.cl

Equipo N° 5 · Sección 001D · Docente: Victor Rosendo

## Descripción del proyecto
Induztek realiza pruebas eléctricas en terreno (transformadores, interruptores, celdas de media tensión, cables de poder y relés de protección). La app cubre el ciclo completo de un protocolo:

1. Se crea una **visita** a una instalación del cliente.
2. Por cada equipo se abre un **protocolo** con condiciones ambientales, pruebas, mediciones y evidencia.
3. La app **calcula el resultado** de cada prueba y el resultado global del protocolo.
4. El protocolo pasa por estados (*Borrador → Registrado en terreno → Formalizado / Observado / Anulado*) según el rol de quien lo revisa.
5. Cada equipo conserva su **historial**, con la variación respecto de la prueba anterior.

**Tecnologías:** Kotlin · Jetpack Compose · Material 3 · Navigation Compose · MVVM (`AndroidViewModel` + `StateFlow`) · SQLite (`SQLiteOpenHelper`) · SharedPreferences.

## Funcionalidades implementadas
**Interfaz y navegación**
- 10 pantallas con diseño Material 3 (tema claro/oscuro): login, inicio, visitas, nueva visita, detalle de visita, protocolo, historial, instrumentos, revisión y vista de cliente.
- Menú de inicio distinto según el rol: técnico, ingeniero, jefe técnico o cliente.

**Formularios con validación** (ícono, color y mensaje de error por campo)
- Condiciones ambientales (temperatura y humedad con rango), mediciones numéricas, motivo obligatorio, TAG del equipo, fechas de calibración y RUT chileno (módulo 11) al cerrar una visita.
- Las reglas de validación están separadas de la interfaz (`domain/Validaciones.kt`).

**Reglas del caso**
- TAG único por instalación · numeración automática de protocolos (PT-) y órdenes de trabajo (OT-).
- Catálogo de pruebas según el tipo de equipo: aislación (con índice de polarización), relación de transformación (TTR), resistencia de contactos, operación de relé y resistencia de devanados (resultado definido por el ingeniero).
- Resultado automático Aprobado / Observado / Rechazado y **resultado global = el peor** de sus pruebas.
- Instrumentos con calibración vencida bloqueados; aviso de vencimiento próximo.
- Cambios de estado según rol, con motivo obligatorio al observar o anular; un protocolo formalizado ya no se modifica. Se guarda quién, cuándo y por qué.
- La visita solo se cierra cuando todos sus protocolos están registrados o anulados.
- Historial por equipo con porcentaje de variación y alerta de **variación significativa (> 25 %)**.
- El técnico edita solo sus protocolos; el cliente ve únicamente los protocolos formalizados de su empresa.

**Recursos nativos** (con solicitud de permiso, y aviso con acceso a *Ajustes* si se rechaza)
- **Cámara:** foto de evidencia del equipo.
- **Galería:** adjuntar una foto existente.
- **Ubicación (GPS):** coordenadas de dónde se hizo la prueba.

**Animaciones**
- Transición deslizante entre pantallas, aparición escalonada de las listas, formulario de prueba que se despliega, insignia «Guardado» al grabar y cambio animado de color en los estados.

**Almacenamiento local**
- Base SQLite `induztek.db` (10 tablas con claves foráneas y transacciones), sesión en SharedPreferences y fotos guardadas como BLOB. No necesita internet.

## Estructura del proyecto
```
app/src/main/java/cl/induztek/protocolos
├── MainActivity.kt
├── navigation/   Rutas (sealed class) y AppNavegacion
├── viewmodel/    InduztekViewModel, ProtocoloViewModel
├── model/        data classes, UiState y modelos para pantalla
├── domain/       reglas de negocio en Kotlin puro (validaciones, estados, cálculo de resultados, RUT, permisos por rol)
├── data/         InduztekDbHelper, InduztekRepositorio, SesionPreferencias, ConvertidorImagen
├── permisos/     Permisos (cámara, galería, ubicación) y Ubicacion (GPS)
└── ui/           theme, styles, components, screens
```
Flujo: **Pantalla → ViewModel → Repositorio → InduztekDbHelper (SQLite)**. La pantalla solo dibuja el estado y avisa los eventos del usuario.

## Pasos para ejecutar
**Requisitos:** Android Studio (versión compatible con Android Gradle Plugin 9.4.1, la misma usada en clases), JDK incluido en el IDE y un emulador o teléfono con Android 7.0 (API 24) o superior.

1. Clonar el repositorio: `git clone <URL-del-repositorio>`
2. En Android Studio: **File → Open** y elegir la carpeta del proyecto.
3. Esperar a que termine **Gradle Sync**.
4. Elegir un emulador o dispositivo y pulsar **Run ▶** (módulo `app`).
5. *(Opcional)* Para probar el GPS en el emulador: **Extended controls (⋯) → Location → Set location**.
6. *(Opcional)* Pruebas unitarias de la lógica: clic derecho en `app/src/test/.../DominioTest.kt` → **Run**.

La primera vez que se abre la app se crea la base de datos con datos de ejemplo. Para volver a empezar de cero: *Settings → Apps → Induztek Protocolos → Storage → Clear data*.

## Cuentas de prueba (inicio de sesión simulado)
Al abrir la app se elige una cuenta ficticia en la lista.

| Rol | Cuenta | Puede |
|---|---|---|
| Técnico de terreno | Matías Godoy Herrera | crear visitas, registrar equipos y protocolos, cerrar visitas |
| Ingeniero de pruebas | Andrea Contreras Silva | lo anterior + formalizar, observar, anular y definir el resultado de devanados |
| Jefe técnico | Ricardo Espinoza Lagos | formalizar, observar, anular y gestionar instrumentos |
| Usuario cliente | Envasadora del Pacífico S.A. | ver el historial formalizado de sus equipos |

## Recorrido sugerido
1. Entrar como **técnico** → *Visitas a terreno* → *Nueva visita* en Planta Lampa.
2. Abrir un equipo → *Iniciar protocolo* → guardar temperatura y humedad.
3. Tomar una foto, elegir otra de la galería y registrar la ubicación.
4. *Agregar prueba* de aislación y ver el resultado calculado. Probar con el Micro-ohmímetro (calibración vencida).
5. *Registrar en terreno* y luego *Cerrar visita* con un RUT válido.
6. Entrar como **ingeniero** → *Revisión de protocolos* → formalizar u observar.
7. Entrar como **cliente** → revisar el historial del equipo.

## Planificación

Tablero Trello: https://trello.com/b/9zLuWaPz/proyecto-desarrollo-de-aplicaciones 

Repositorio: https://github.com/vicbravoe/Proyecto-Desarrollo-de-Aplicaciones
