package com.awa2027.galleryvault.data

import com.awa2027.galleryvault.data.local.ArtworkDao
import com.awa2027.galleryvault.data.remote.ClevelandArtApi
import com.awa2027.galleryvault.domain.Artwork
import com.awa2027.galleryvault.domain.SyncResult
import com.awa2027.galleryvault.domain.VaultRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class OfflineFirstVaultRepository(
    private val api: ClevelandArtApi,
    private val dao: ArtworkDao,
    private val clock: () -> Long = System::currentTimeMillis,
    private val pageSize: Int = ClevelandArtApi.PAGE_SIZE
) : VaultRepository {
    private val syncMutex = Mutex()
    private var nextPage: Int? = FIRST_PAGE

    /** Reto, parte 2: hoy ignora query y onlyFavorites. La búsqueda se resuelve en SQL, en el DAO. */
    override fun observeArtworks(query: String, onlyFavorites: Boolean): Flow<List<Artwork>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeFavorites(): Flow<List<Artwork>> = TODO("Reto, parte 1")

    override suspend fun hasCache(): Boolean = dao.count() > 0

    override suspend fun refresh(): SyncResult = syncMutex.withLock { syncPage(FIRST_PAGE) }

    override suspend fun loadNextPage(): SyncResult = syncMutex.withLock {
        val page = nextPage ?: return@withLock SyncResult.Synced(received = 0, endReached = true)
        syncPage(page)
    }

    override suspend fun setFavorite(id: Int, favorite: Boolean): Boolean = TODO("Reto, parte 1")

    /** Taller, paso 2. Red -> Room. Nunca devuelve la lista: la UI la recibe por el Flow de Room. */
    private suspend fun syncPage(page: Int): SyncResult = TODO("Taller, paso 2: API -> Room y errores tipados")

    private companion object {
        const val FIRST_PAGE = 1
    }
}
