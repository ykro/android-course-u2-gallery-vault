package com.awa2027.galleryvault.ui

import com.awa2027.galleryvault.domain.AppError
import com.awa2027.galleryvault.domain.Artwork

sealed interface VaultUiState {
    /** Todavía no llegó la primera emisión de Room. */
    data object Loading : VaultUiState

    /**
     * Hay algo que mostrar desde Room (aunque sea una lista vacía mientras se sincroniza).
     * [staleError] no es null cuando la última sincronización falló pero Room tiene datos:
     * la pantalla muestra los datos guardados y un aviso, nunca un error a pantalla completa.
     */
    data class Content(
        val artworks: List<Artwork>,
        val query: String = "",
        val onlyFavorites: Boolean = false,
        val isRefreshing: Boolean = false,
        val isLoadingMore: Boolean = false,
        val endReached: Boolean = false,
        val staleError: AppError? = null
    ) : VaultUiState

    /** La sincronización falló y Room no tiene ninguna obra: no hay nada que mostrar. */
    data class Error(val error: AppError, val isRetrying: Boolean = false) : VaultUiState
}
