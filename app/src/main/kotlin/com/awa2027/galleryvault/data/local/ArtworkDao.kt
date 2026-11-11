package com.awa2027.galleryvault.data.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import com.awa2027.galleryvault.data.remote.ArtworkDto
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtworkDao {
    /** Room vuelve a emitir cada vez que cambia la tabla `artworks` (InvalidationTracker). */
    @Query("SELECT * FROM artworks ORDER BY catalog_position, id")
    fun observeAll(): Flow<List<ArtworkEntity>>

    @Query("SELECT COUNT(*) FROM artworks")
    suspend fun count(): Int

    @Query("SELECT * FROM artworks WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Int>): List<ArtworkEntity>

    @Query("SELECT * FROM artworks WHERE id = :id")
    suspend fun getById(id: Int): ArtworkEntity?

    @Upsert
    suspend fun upsertAll(entities: List<ArtworkEntity>)

    /**
     * Taller, paso 1. Lee las filas existentes y escribe las nuevas en UNA transacción:
     * nadie puede marcar un favorito entre la lectura y la escritura.
     * [firstPosition] es la posición en el catálogo de la primera obra de la página (su skip).
     */
    @Transaction
    suspend fun upsertFromRemote(remote: List<ArtworkDto>, firstPosition: Int, syncedAt: Long) {
        TODO("Taller, paso 1: leer las filas existentes y hacer upsert con toEntity")
    }

    // Reto, partes 1 y 2: agrega aquí tus consultas de favoritos y de búsqueda.
}
