package com.awa2027.galleryvault

import app.cash.turbine.test
import com.awa2027.galleryvault.data.local.ArtworkEntity
import com.awa2027.galleryvault.data.remote.UNKNOWN_ARTIST
import com.awa2027.galleryvault.data.remote.artistName
import com.awa2027.galleryvault.data.remote.hasMoreAfter
import com.awa2027.galleryvault.di.createApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Demo: ya pasa en el starter. Muestra las dos herramientas de la unidad sin red real. */
@RunWith(RobolectricTestRunner::class)
class DemoTest {
    private val env = VaultTestEnv()

    @After
    fun tearDown() = env.close()

    @Test
    fun `Retrofit decodifica la pagina 1 servida por MockWebServer`() = runTest {
        env.server.enqueueFixture("artworks_page1.json")
        val api = createApi(env.server.url("/api/").toString())

        val page = api.getArtworks(skip = 0, limit = 6)

        assertEquals(6, page.data.size)
        assertEquals("Nathaniel Hurd", page.data.first().title)
        assertEquals("John Singleton Copley", page.data.first().artistName())
        assertEquals("Obra anónima: creators llega vacío", UNKNOWN_ARTIST, page.data.last().artistName())
        assertEquals(12, page.info.total)
        assertTrue("6 de 12: quedan obras", page.hasMoreAfter(skip = 0))
        val request = env.server.takeRequest()
        assertEquals("/api/artworks/", request.url.encodedPath)
        assertEquals("0", request.url.queryParameter("skip"))
        assertEquals("1", request.url.queryParameter("cc0"))
    }

    @Test
    fun `Room vuelve a emitir el Flow cuando cambia la tabla`() = runTest {
        env.dao.observeAll().test {
            assertTrue(awaitItem().isEmpty())

            env.dao.upsertAll(listOf(entity(id = 1, title = "Nathaniel Hurd")))

            assertEquals(listOf("Nathaniel Hurd"), awaitItem().map { it.title })
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun entity(id: Int, title: String) = ArtworkEntity(
        id = id,
        title = title,
        artist = "John Singleton Copley",
        culture = "United States",
        creationDate = "c. 1765",
        imageUrl = "",
        catalogPosition = 0,
        searchText = title.lowercase(),
        syncedAt = 0L
    )
}
