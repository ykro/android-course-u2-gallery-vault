package com.awa2027.galleryvault.domain

import kotlinx.coroutines.flow.Flow

/**
 * Contrato del repositorio offline-first. Room es la fuente única de verdad:
 * los métodos observe* leen SOLO de Room; refresh y loadNextPage escriben en Room
 * y nunca devuelven la lista directamente.
 */
interface VaultRepository {
    /** Obras guardadas, en el orden del catálogo. Reto: [query] y [onlyFavorites] filtran en SQL. */
    fun observeArtworks(query: String = "", onlyFavorites: Boolean = false): Flow<List<Artwork>>

    /** Reto: favoritos, el marcado más recientemente primero; en empate, por id. */
    fun observeFavorites(): Flow<List<Artwork>>

    /** true si Room tiene al menos una obra guardada (sin importar filtros). */
    suspend fun hasCache(): Boolean

    /** Descarga la página 1 de la API y la guarda en Room preservando el estado local. */
    suspend fun refresh(): SyncResult

    /** Descarga la siguiente página (skip = página anterior x tamaño) mientras info.total diga que hay más. */
    suspend fun loadNextPage(): SyncResult

    /** Reto: marca o desmarca un favorito. Devuelve false si el id no está en Room. */
    suspend fun setFavorite(id: Int, favorite: Boolean): Boolean
}
