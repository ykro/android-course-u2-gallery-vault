package com.awa2027.galleryvault

import android.content.Context
import androidx.room3.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.ReceiveTurbine
import com.awa2027.galleryvault.data.OfflineFirstVaultRepository
import com.awa2027.galleryvault.data.local.ArtworkDao
import com.awa2027.galleryvault.data.local.VaultDatabase
import com.awa2027.galleryvault.data.remote.ArtworkPageDto
import com.awa2027.galleryvault.di.createApi
import com.awa2027.galleryvault.di.vaultJson
import com.awa2027.galleryvault.domain.AppError
import com.awa2027.galleryvault.domain.Artwork
import com.awa2027.galleryvault.domain.SyncResult
import com.awa2027.galleryvault.domain.VaultRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/*
 * Utilidades oficiales de prueba. No las modifiques: son parte de la suite oficial.
 * Tus tests en student/ pueden usarlas.
 */

/** Lee un JSON fijo de src/test/resources/fixtures. */
fun fixture(name: String): String =
    requireNotNull(VaultTestEnv::class.java.classLoader?.getResource("fixtures/$name")) {
        "No existe el fixture $name"
    }.readText()

fun pageFixture(name: String): ArtworkPageDto = vaultJson.decodeFromString(fixture(name))

fun MockWebServer.enqueueFixture(name: String) = enqueue(MockResponse(code = 200, body = fixture(name)))

fun MockWebServer.enqueueStatus(code: Int) = enqueue(MockResponse(code = code, body = """{"error":"x"}"""))

/** Base de Room en memoria. Requiere @RunWith(RobolectricTestRunner::class) en la clase de test. */
fun inMemoryVault(): VaultDatabase =
    Room.inMemoryDatabaseBuilder<VaultDatabase>(ApplicationProvider.getApplicationContext<Context>()).build()

/** Tamaño de página de los fixtures: 6 obras por página, 12 en total. */
const val TEST_PAGE_SIZE = 6

/**
 * Entorno completo sin red real: MockWebServer + Retrofit + Room en memoria + reloj controlado.
 * Cambia [now] para simular el paso del tiempo (lo usa favorited_at y synced_at).
 */
class VaultTestEnv : AutoCloseable {
    val server = MockWebServer().apply { start() }
    val db: VaultDatabase = inMemoryVault()
    val dao: ArtworkDao = db.artworkDao()
    var now: Long = 1_000L
    val repository = OfflineFirstVaultRepository(
        api = createApi(server.url("/api/").toString()),
        dao = dao,
        clock = { now },
        pageSize = TEST_PAGE_SIZE
    )

    override fun close() {
        db.close()
        server.close()
    }
}

/** Reemplaza Dispatchers.Main por un dispatcher de prueba (viewModelScope usa Main). */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(val dispatcher: TestDispatcher = StandardTestDispatcher()) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)

    override fun finished(description: Description) = Dispatchers.resetMain()
}

fun artwork(id: Int, title: String, isFavorite: Boolean = false) = Artwork(
    id = id,
    title = title,
    artist = "Claude Monet",
    culture = "France, 19th century",
    creationDate = "c. 1870",
    imageUrl = "https://openaccess-cdn.clevelandart.org/test/$id.jpg",
    isFavorite = isFavorite
)

/**
 * Repositorio falso en memoria para probar el ViewModel sin Room ni red.
 * - [rows]: lo que "hay en Room".
 * - [refreshResults]: respuestas de refresh() en orden (si se agotan, Synced).
 * - [refreshGate]: si no es null, refresh() espera a que lo completes (para observar isRefreshing).
 */
class FakeVaultRepository(initial: List<Artwork> = emptyList()) : VaultRepository {
    val rows = MutableStateFlow(initial)
    val refreshResults = ArrayDeque<SyncResult>()
    var refreshGate: CompletableDeferred<Unit>? = null
    var refreshCalls = 0
        private set
    var loadNextCalls = 0
        private set

    /** Cuántos colectores observan hoy la "tabla" (sirve para verificar WhileSubscribed). */
    val activeObservers get() = rows.subscriptionCount.value

    override fun observeArtworks(query: String, onlyFavorites: Boolean): Flow<List<Artwork>> = rows.map { list ->
        val text = query.trim()
        list.filter {
            val matches = text.isEmpty() ||
                it.title.contains(text, ignoreCase = true) ||
                it.artist.contains(text, ignoreCase = true)
            matches && (!onlyFavorites || it.isFavorite)
        }
    }

    override fun observeFavorites(): Flow<List<Artwork>> = rows.map { list -> list.filter { it.isFavorite } }

    override suspend fun hasCache(): Boolean = rows.value.isNotEmpty()

    override suspend fun refresh(): SyncResult {
        refreshCalls++
        refreshGate?.await()
        return refreshResults.removeFirstOrNull() ?: SyncResult.Synced(received = rows.value.size, endReached = false)
    }

    override suspend fun loadNextPage(): SyncResult {
        loadNextCalls++
        return SyncResult.Synced(received = 0, endReached = true)
    }

    override suspend fun setFavorite(id: Int, favorite: Boolean): Boolean {
        if (rows.value.none { it.id == id }) return false
        rows.value = rows.value.map { if (it.id == id) it.copy(isFavorite = favorite) else it }
        return true
    }
}

fun failure(error: AppError = AppError.Network) = SyncResult.Failed(error)

/** Consume emisiones hasta encontrar una que cumpla [predicate] (StateFlow puede fusionar estados intermedios). */
suspend fun <T> ReceiveTurbine<T>.awaitUntil(predicate: (T) -> Boolean): T {
    while (true) {
        val item = awaitItem()
        if (predicate(item)) return item
    }
}
