package com.awa2027.galleryvault

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import app.cash.turbine.test
import com.awa2027.galleryvault.data.toEntity
import com.awa2027.galleryvault.domain.AppError
import com.awa2027.galleryvault.domain.SyncResult
import com.awa2027.galleryvault.ui.LoadMoreEffect
import com.awa2027.galleryvault.ui.VaultUiState
import com.awa2027.galleryvault.ui.VaultViewModel
import com.awa2027.galleryvault.ui.shouldLoadMore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Taller guiado: 13 tests, rojos en el starter. */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class WorkshopTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private val env = VaultTestEnv()

    @After
    fun tearDown() = env.close()

    // Paso 1. Mapper y upsert que preserva el estado local

    @Test
    fun `paso1 toEntity sin fila previa copia lo remoto y deja lo local en blanco`() {
        val page = pageFixture("artworks_page1.json")

        val entity = page.data.first().toEntity(existing = null, position = 0, syncedAt = 42L)

        assertEquals(1, entity.id)
        assertEquals("Nathaniel Hurd", entity.title)
        assertEquals("John Singleton Copley", entity.artist)
        assertEquals("United States", entity.culture)
        assertEquals("https://openaccess-cdn.clevelandart.org/1915.534/1915.534_web.jpg", entity.imageUrl)
        assertEquals(42L, entity.syncedAt)
        assertFalse(entity.isFavorite)
        assertNull(entity.favoritedAt)
        val anonymous = page.data.last().toEntity(existing = null, position = 5, syncedAt = 42L)
        assertEquals("Autor desconocido", anonymous.artist)
        assertEquals(5, anonymous.catalogPosition)
    }

    @Test
    fun `paso1 toEntity conserva isFavorite y actualiza lo remoto`() {
        val old = pageFixture("artworks_page1.json").data.first().toEntity(null, position = 0, syncedAt = 1L)
        val favorite = old.copy(isFavorite = true)
        val fresh = pageFixture("artworks_page1_updated.json").data.first()

        val merged = fresh.toEntity(existing = favorite, position = 0, syncedAt = 2L)

        assertTrue(merged.isFavorite)
        assertEquals("1765", merged.creationDate)
        assertEquals(2L, merged.syncedAt)
    }

    @Test
    fun `paso1 upsertFromRemote dos veces no borra el favorito`() = runTest {
        env.dao.upsertFromRemote(pageFixture("artworks_page1.json").data, firstPosition = 0, syncedAt = 1L)
        val twilight = requireNotNull(env.dao.getById(3))
        env.dao.upsertAll(listOf(twilight.copy(isFavorite = true)))

        env.dao.upsertFromRemote(pageFixture("artworks_page1_updated.json").data, firstPosition = 0, syncedAt = 2L)

        val after = requireNotNull(env.dao.getById(3))
        assertTrue(after.isFavorite)
        assertEquals("United States, 19th century", after.culture)
        assertEquals(6, env.dao.count())
    }

    // Paso 2. Repositorio offline-first y errores tipados

    @Test
    fun `paso2 refresh guarda la pagina 1 en Room y reporta Synced`() = runTest {
        env.server.enqueueFixture("artworks_page1.json")

        val result = env.repository.refresh()

        assertEquals(SyncResult.Synced(received = 6, endReached = false), result)
        assertEquals(6, env.dao.count())
        val request = env.server.takeRequest().url
        assertEquals("0", request.queryParameter("skip"))
        assertEquals("6", request.queryParameter("limit"))
    }

    @Test
    fun `paso2 refresh con HTTP 500 devuelve Http y conserva la cache`() = runTest {
        env.server.enqueueFixture("artworks_page1.json")
        env.repository.refresh()
        env.server.enqueueStatus(500)

        val result = env.repository.refresh()

        assertEquals(SyncResult.Failed(AppError.Http(500)), result)
        assertEquals(6, env.dao.count())
    }

    @Test
    fun `paso2 refresh con JSON que no cumple el DTO devuelve Parse`() = runTest {
        env.server.enqueueFixture("artworks_malformed.json")

        assertEquals(SyncResult.Failed(AppError.Parse), env.repository.refresh())
        assertEquals(0, env.dao.count())
    }

    @Test
    fun `paso2 refresh sin servidor devuelve Network`() = runTest {
        env.server.close()

        assertEquals(SyncResult.Failed(AppError.Network), env.repository.refresh())
    }

    @Test
    fun `paso2 loadNextPage calcula el skip y se detiene con info total`() = runTest {
        env.server.enqueueFixture("artworks_page1.json")
        env.server.enqueueFixture("artworks_page2.json")
        env.repository.refresh()

        val second = env.repository.loadNextPage()
        val third = env.repository.loadNextPage()

        assertEquals(SyncResult.Synced(received = 6, endReached = true), second)
        assertEquals(SyncResult.Synced(received = 0, endReached = true), third)
        assertEquals(12, env.dao.count())
        assertEquals(2, env.server.requestCount)
        env.server.takeRequest()
        assertEquals("6", env.server.takeRequest().url.queryParameter("skip"))
    }

    // Paso 3. ViewModel que observa Room con stateIn

    @Test
    fun `paso3 uiState pasa de Loading a Content con lo que hay en Room`() = runTest {
        val repo = FakeVaultRepository(listOf(artwork(1, "Nathaniel Hurd"), artwork(2, "Portrait of Dora Wheeler")))
        val viewModel = VaultViewModel(repo)

        viewModel.uiState.test {
            assertEquals(VaultUiState.Loading, awaitItem())
            val content = awaitUntil { it is VaultUiState.Content } as VaultUiState.Content
            assertEquals(listOf(1, 2), content.artworks.map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `paso3 onRefresh enciende isRefreshing y lo apaga al terminar`() = runTest {
        val repo = FakeVaultRepository(listOf(artwork(1, "Nathaniel Hurd")))
        repo.refreshGate = CompletableDeferred()
        val viewModel = VaultViewModel(repo)

        viewModel.uiState.test {
            val refreshing = awaitUntil { it is VaultUiState.Content } as VaultUiState.Content
            assertTrue(refreshing.isRefreshing)

            repo.refreshGate?.complete(Unit)

            val done = awaitUntil { it is VaultUiState.Content && !it.isRefreshing } as VaultUiState.Content
            assertEquals(1, done.artworks.size)
            assertEquals(1, repo.refreshCalls)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `paso3 uiState sigue suscrito 5 segundos despues del ultimo observador`() = runTest {
        val repo = FakeVaultRepository(listOf(artwork(1, "Nathaniel Hurd")))
        val viewModel = VaultViewModel(repo)
        val collector = backgroundScope.launch { viewModel.uiState.collect {} }
        runCurrent()
        assertEquals(1, repo.activeObservers)

        collector.cancel()
        advanceTimeBy(4_999)
        runCurrent()
        assertEquals(1, repo.activeObservers)

        advanceTimeBy(2)
        runCurrent()
        assertEquals(0, repo.activeObservers)
    }

    @Test
    fun `paso3 shouldLoadMore pide pagina solo dentro del umbral`() {
        assertFalse(shouldLoadMore(lastVisibleIndex = 33, totalItems = 40, threshold = 6))
        assertTrue(shouldLoadMore(lastVisibleIndex = 34, totalItems = 40, threshold = 6))
        assertTrue(shouldLoadMore(lastVisibleIndex = 39, totalItems = 40, threshold = 6))
        assertFalse(shouldLoadMore(lastVisibleIndex = -1, totalItems = 0, threshold = 6))
        assertTrue(shouldLoadMore(lastVisibleIndex = 0, totalItems = 3))
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `paso3 LoadMoreEffect pide la siguiente pagina una sola vez al acercarse al final`() = runComposeUiTest {
        var calls = 0
        lateinit var gridState: LazyGridState
        setContent {
            gridState = rememberLazyGridState()
            LazyVerticalGrid(columns = GridCells.Fixed(2), state = gridState, modifier = Modifier.height(200.dp)) {
                items(40) { index -> Text("Celda $index", Modifier.height(50.dp)) }
            }
            LoadMoreEffect(gridState = gridState, enabled = true, onLoadMore = { calls++ })
        }
        val grid = onNode(hasScrollAction())

        waitForIdle()
        assertEquals("Al inicio no pide nada", 0, calls)

        grid.performScrollToIndex(20)
        waitForIdle()
        assertEquals("Lejos del final no pide nada", 0, calls)

        grid.performScrollToIndex(36)
        waitForIdle()
        assertEquals("Al cruzar el umbral pide una vez", 1, calls)

        grid.performScrollToIndex(30)
        waitForIdle()
        grid.performScrollToIndex(38)
        waitForIdle()
        assertEquals("Seguir cerca del final no vuelve a pedir", 1, calls)
    }
}
