package com.awa2027.galleryvault.di

import androidx.room3.Room
import com.awa2027.galleryvault.data.OfflineFirstVaultRepository
import com.awa2027.galleryvault.data.local.VaultDatabase
import com.awa2027.galleryvault.data.remote.ClevelandArtApi
import com.awa2027.galleryvault.domain.VaultRepository
import com.awa2027.galleryvault.ui.VaultViewModel
import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * ignoreUnknownKeys: un campo nuevo en el JSON no rompe la app.
 * coerceInputValues: un `null` explícito en un campo no nulo con valor por defecto toma ese valor por defecto.
 */
val vaultJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}

fun createApi(baseUrl: String, client: OkHttpClient = OkHttpClient()): ClevelandArtApi = Retrofit.Builder()
    .baseUrl(baseUrl)
    .client(client)
    .addConverterFactory(vaultJson.asConverterFactory("application/json".toMediaType()))
    .build()
    .create(ClevelandArtApi::class.java)

val appModule = module {
    single {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }
    single { createApi(ClevelandArtApi.BASE_URL, get()) }
    single {
        Room.databaseBuilder<VaultDatabase>(androidContext(), VaultDatabase.NAME).build()
    }
    single { get<VaultDatabase>().artworkDao() }
    single<VaultRepository> { OfflineFirstVaultRepository(api = get(), dao = get()) }
    viewModel { VaultViewModel(get()) }
}
