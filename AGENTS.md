# AGENTS.md: reglas para asistentes de IA (Gallery Vault)

Este archivo describe las invariantes que cualquier asistente de IA (Gemini en Android Studio, Copilot, Cursor, Claude Code, Antigravity) y cualquier persona deben respetar al modificar este proyecto. Es una **base**: como parte de la entrega se amplía con al menos una regla propia (ver la pregunta 6 de `ARCHITECTURE_DECISIONS.md`).

## 1. Fuente única de verdad

- La UI y el `VaultViewModel` nunca leen respuestas de Retrofit. `refresh()` y `loadNextPage()` escriben en Room y devuelven solo un `SyncResult`; la lista llega a la pantalla por el `Flow` de Room.
- `ArtworkDto` (JSON) solo existe en `data/remote/` y en los mappers. La UI usa `Artwork` (dominio); Room usa `ArtworkEntity`.
- La app solo pide obras con licencia CC0 (`cc0=1` en `ClevelandArtApi`): no se quita ese filtro ni se cargan imágenes de otras fuentes.
- El `uiState` del ViewModel es un `StateFlow` construido con `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), VaultUiState.Loading)`.

## 2. Room

- Las columnas locales (`is_favorite`, `favorited_at`) nunca se escriben a partir de un DTO. Toda sincronización pasa por `ArtworkDao.upsertFromRemote`, que lee las filas existentes y escribe en **una** `@Transaction`.
- Prohibido `@Insert(onConflict = OnConflictStrategy.REPLACE)` sobre `artworks`: en SQLite, `REPLACE` borra la fila y la vuelve a insertar, y con ella las columnas locales.
- Toda consulta usa `@Query` con parámetros (`:nombre`); nunca SQL armado concatenando texto del usuario.
- Este proyecto usa **Room 3** (`androidx.room3`). Un asistente que proponga imports `androidx.room.*` o `room-ktx` está pensando en Room 2: corrígelo.

## 3. Red y errores

- Los errores que cruzan capas son `AppError` (`sealed interface`). Las excepciones de Retrofit, kotlinx.serialization y Room se traducen en `toAppError()`; ninguna excepción ni mensaje técnico llega a la UI.
- Un `catch (e: Exception)` dentro de una corrutina relanza primero `CancellationException`.
- Sin `runBlocking` en código de la app y sin llamadas de red en el hilo principal.

## 4. Compose

- Los valores que cambian en cada frame (`firstVisibleItemIndex`, `layoutInfo`) se leen dentro de `derivedStateOf` antes de usarlos como clave de un `LaunchedEffect`.
- Solo modo claro: la app no define un esquema de colores oscuro.

## 5. Tests y calidad

- Los tests oficiales (`DemoTest.kt`, `WorkshopTest.kt`, `ChallengeTest.kt`, `TestSupport.kt`), los fixtures, los archivos de Gradle y `.github/workflows/ci.yml` no se modifican.
- Ningún test usa la red real: la API se simula con `MockWebServer` y fixtures JSON.
- Los tests propios viven en `app/src/test/kotlin/com/awa2027/galleryvault/student/`.
- Antes de cada commit: `./gradlew check` (tests JVM, ktlint y Android lint) en verde.
- El código de `app/src/main` nunca detecta si corre bajo un test ni lee archivos del proyecto.

## 6. Reglas propias del equipo

<!-- Agrega aquí al menos una regla tuya, concreta y verificable. -->
