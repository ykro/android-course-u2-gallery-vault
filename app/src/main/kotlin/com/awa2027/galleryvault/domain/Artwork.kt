package com.awa2027.galleryvault.domain

/**
 * Modelo que consume la UI. No sabe nada de JSON ni de SQLite:
 * la capa de datos lo construye a partir de ArtworkEntity (Room).
 */
data class Artwork(
    val id: Int,
    val title: String,
    val artist: String,
    val culture: String,
    val creationDate: String,
    val imageUrl: String,
    val isFavorite: Boolean
)
