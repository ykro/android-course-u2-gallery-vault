package com.awa2027.galleryvault.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit genera la implementación. Las funciones suspend lanzan HttpException si el código no es 2xx.
 */
interface ClevelandArtApi {
    @GET(ARTWORKS)
    suspend fun getArtworks(@Query("skip") skip: Int, @Query("limit") limit: Int): ArtworkPageDto

    companion object {
        const val BASE_URL = "https://openaccess-api.clevelandart.org/api/"
        const val PAGE_SIZE = 20

        /** Filtros fijos: solo pinturas con imagen y licencia CC0 (dominio público), y solo los campos que usa la app. */
        const val ARTWORKS = "artworks/?cc0=1&has_image=1&type=Painting" +
            "&fields=id,title,creation_date,culture,type,creators,images"
    }
}
