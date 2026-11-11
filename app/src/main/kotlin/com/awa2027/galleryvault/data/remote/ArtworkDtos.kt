package com.awa2027.galleryvault.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO: copia del JSON de https://openaccess-api.clevelandart.org/api/artworks/ (Open Access del
 * Cleveland Museum of Art; solo se piden registros CC0, de dominio público). Solo vive en la capa
 * de red; la UI nunca lo ve.
 *
 * La API omite o manda vacíos algunos campos: una obra anónima trae `creators: []`, otras no traen
 * `culture` y una obra sin foto no trae `images.web`. Cada campo opcional tiene un valor por defecto,
 * así un dato que falta no rompe la decodificación. `id` y `title` son obligatorios: sin ellos el JSON
 * no cumple el contrato y la sincronización falla con AppError.Parse.
 */
@Serializable
data class ArtworkPageDto(val info: PageInfoDto, val data: List<ArtworkDto>)

/** La API pagina por desplazamiento (`skip` y `limit`) y dice cuántas obras hay en total. */
@Serializable
data class PageInfoDto(val total: Int)

@Serializable
data class ArtworkDto(
    val id: Int,
    val title: String,
    @SerialName("creation_date") val creationDate: String = "",
    val culture: List<String> = emptyList(),
    val type: String = "",
    val creators: List<CreatorDto> = emptyList(),
    val images: ImagesDto? = null
)

/** `description` llega como "Claude Monet (French, 1840–1926)". */
@Serializable
data class CreatorDto(val description: String = "", val role: String = "")

@Serializable
data class ImagesDto(val web: ImageDto? = null)

@Serializable
data class ImageDto(val url: String)

const val UNKNOWN_ARTIST = "Autor desconocido"

/** Primer creador sin nacionalidad ni fechas: "Claude Monet (French, 1840–1926)" -> "Claude Monet". */
fun ArtworkDto.artistName(): String =
    creators.firstOrNull()?.description?.substringBefore(" (")?.trim().orEmpty().ifBlank { UNKNOWN_ARTIST }

/** Desplazamiento de la página [page] (la primera es 1): la página 3 de 20 en 20 empieza en skip 40. */
fun skipForPage(page: Int, pageSize: Int): Int = (page - 1) * pageSize

/**
 * true si quedan obras después de esta página. La API no manda un enlace a la siguiente página:
 * se calcula con `info.total`. Una página vacía también significa que no hay más.
 */
fun ArtworkPageDto.hasMoreAfter(skip: Int): Boolean = data.isNotEmpty() && skip + data.size < info.total
