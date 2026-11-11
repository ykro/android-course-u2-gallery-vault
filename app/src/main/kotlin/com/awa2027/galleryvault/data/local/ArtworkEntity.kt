package com.awa2027.galleryvault.data.local

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

/**
 * Fila de la tabla `artworks`. Mezcla dos tipos de columnas:
 * - remotas: las reescribe cada sincronización con lo que diga la API;
 * - locales (is_favorite, favorited_at): la API no las conoce y NINGUNA sincronización las borra.
 *
 * `catalog_position` guarda el orden en que la API devolvió la obra (skip + índice en la página):
 * los ids del museo no siguen ese orden, y la grilla debe mostrar las páginas una detrás de otra.
 */
@Entity(
    tableName = "artworks",
    indices = [Index(value = ["search_text"]), Index(value = ["is_favorite"]), Index(value = ["catalog_position"])]
)
data class ArtworkEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val artist: String,
    val culture: String,
    @ColumnInfo(name = "creation_date") val creationDate: String,
    @ColumnInfo(name = "image_url") val imageUrl: String,
    @ColumnInfo(name = "catalog_position") val catalogPosition: Int,
    @ColumnInfo(name = "search_text") val searchText: String,
    @ColumnInfo(name = "synced_at") val syncedAt: Long,
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean = false,
    @ColumnInfo(name = "favorited_at") val favoritedAt: Long? = null
)
