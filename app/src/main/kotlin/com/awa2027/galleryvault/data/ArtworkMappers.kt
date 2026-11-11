package com.awa2027.galleryvault.data

import com.awa2027.galleryvault.data.local.ArtworkEntity
import com.awa2027.galleryvault.data.remote.ArtworkDto
import com.awa2027.galleryvault.domain.Artwork

/**
 * Taller, paso 1. Convierte un DTO de la API en la fila de Room.
 * Las columnas remotas salen del DTO (y [position], su lugar en el catálogo); las locales salen de
 * [existing] (la fila que ya estaba), porque la API no sabe qué obras marcó el usuario.
 */
fun ArtworkDto.toEntity(existing: ArtworkEntity?, position: Int, syncedAt: Long): ArtworkEntity =
    TODO("Taller, paso 1: mapear el DTO conservando las columnas locales de existing")

fun ArtworkEntity.toDomain(): Artwork = Artwork(
    id = id,
    title = title,
    artist = artist,
    culture = culture,
    creationDate = creationDate,
    imageUrl = imageUrl,
    isFavorite = isFavorite
)
