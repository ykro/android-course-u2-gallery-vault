package com.awa2027.galleryvault

import app.cash.turbine.test
import com.awa2027.galleryvault.domain.AppError
import com.awa2027.galleryvault.ui.VaultUiState
import com.awa2027.galleryvault.ui.VaultViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Reto: tests PÚBLICOS. No cubren todos los bordes del contrato:
 * el contrato completo de la guía del reto tiene más casos.
 */
@RunWith(RobolectricTestRunner::class)
class ChallengeTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private val env = VaultTestEnv()

    @After
    fun tearDown() = env.close()

    private suspend fun seed(fixtureName: String) {
        env.server.enqueueFixture(fixtureName)
        env.repository.refresh()
    }

    // Parte 1. Favoritos persistentes

    @Test
    fun `parte1 setFavorite marca la obra y observeFavorites la devuelve`() = runTest {
        seed("artworks_page1.json")

        assertTrue(env.repository.setFavorite(2, favorite = true))

        assertEquals(listOf(2), env.repository.observeFavorites().first().map { it.id })
        assertTrue(env.repository.observeArtworks().first().first { it.id == 2 }.isFavorite)
    }

    @Test
    fun `parte1 setFavorite de un id que no esta en Room devuelve false`() = runTest {
        seed("artworks_page1.json")

        assertFalse(env.repository.setFavorite(999, favorite = true))
        assertTrue(env.repository.observeFavorites().first().isEmpty())
    }

    @Test
    fun `parte1 observeFavorites ordena del mas reciente al mas antiguo`() = runTest {
        seed("artworks_page1.json")
        env.now = 1_000L
        env.repository.setFavorite(1, favorite = true)
        env.now = 2_000L
        env.repository.setFavorite(4, favorite = true)

        assertEquals(listOf(4, 1), env.repository.observeFavorites().first().map { it.id })
    }

    // Parte 2. Búsqueda local en el DAO

    @Test
    fun `parte2 la busqueda ignora mayusculas y respeta el orden del catalogo`() = runTest {
        seed("artworks_search.json")

        val result = env.repository.observeArtworks(query = "PORTRAIT").first()

        assertEquals(listOf(101, 105, 106, 108), result.map { it.id })
    }

    @Test
    fun `parte2 onlyFavorites sin texto devuelve solo favoritos`() = runTest {
        seed("artworks_page1.json")
        env.repository.setFavorite(5, favorite = true)

        val result = env.repository.observeArtworks(query = "", onlyFavorites = true).first()

        assertEquals(listOf(5), result.map { it.id })
    }

    // Parte 3. Error diferenciado: con caché frente a sin caché

    @Test
    fun `parte3 si falla el refresh con cache se muestran los datos y un aviso`() = runTest {
        val repo = FakeVaultRepository(listOf(artwork(1, "Nathaniel Hurd")))
        repo.refreshResults.add(failure(AppError.Network))
        val viewModel = VaultViewModel(repo)

        viewModel.uiState.test {
            val state = awaitUntil { it is VaultUiState.Content && it.staleError != null } as VaultUiState.Content
            assertEquals(AppError.Network, state.staleError)
            assertEquals(listOf(1), state.artworks.map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `parte3 si falla el refresh sin cache se muestra error a pantalla completa`() = runTest {
        val repo = FakeVaultRepository()
        repo.refreshResults.add(failure(AppError.Http(503)))
        val viewModel = VaultViewModel(repo)

        viewModel.uiState.test {
            val state = awaitUntil { it is VaultUiState.Error } as VaultUiState.Error
            assertEquals(AppError.Http(503), state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `parte3 reintentar con exito sale del error a pantalla completa`() = runTest {
        val repo = FakeVaultRepository()
        repo.refreshResults.add(failure(AppError.Network))
        val viewModel = VaultViewModel(repo)

        viewModel.uiState.test {
            awaitUntil { it is VaultUiState.Error }
            repo.rows.value = listOf(artwork(7, "Portrait of a Family Playing Music"))

            viewModel.onRetry()

            val state = awaitUntil { it is VaultUiState.Content && !it.isRefreshing } as VaultUiState.Content
            assertEquals(listOf(7), state.artworks.map { it.id })
            assertEquals(2, repo.refreshCalls)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
