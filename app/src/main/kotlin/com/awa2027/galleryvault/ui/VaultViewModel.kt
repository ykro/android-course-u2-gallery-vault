package com.awa2027.galleryvault.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.awa2027.galleryvault.domain.AppError
import com.awa2027.galleryvault.domain.Artwork
import com.awa2027.galleryvault.domain.SyncResult
import com.awa2027.galleryvault.domain.VaultRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado de la sincronización que NO vive en Room: banderas de carga y errores. */
data class SyncStatus(
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val endReached: Boolean = false,
    val staleError: AppError? = null,
    val blockingError: AppError? = null
)

private data class Filters(val query: String = "", val onlyFavorites: Boolean = false)

class VaultViewModel(private val repository: VaultRepository) : ViewModel() {
    private val filters = MutableStateFlow(Filters())
    private val sync = MutableStateFlow(SyncStatus())

    /**
     * Taller, paso 3: reemplaza este valor fijo por el Flow de Room combinado con [filters] y [sync],
     * convertido en StateFlow con stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Loading).
     */
    val uiState: StateFlow<VaultUiState> = MutableStateFlow(VaultUiState.Loading)

    init {
        onRefresh()
    }

    fun onRefresh() {
        // Taller, paso 3: enciende isRefreshing, llama a repository.refresh() y apágalo al terminar.
        // Reto, parte 3: error con caché frente a sin caché, y un solo refresh a la vez.
    }

    fun onRetry() = onRefresh()

    fun onLoadMore() {
        val current = sync.value
        if (current.isLoadingMore || current.isRefreshing || current.endReached) return
        sync.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            try {
                when (val result = repository.loadNextPage()) {
                    is SyncResult.Synced -> sync.update { it.copy(endReached = result.endReached) }
                    is SyncResult.Failed -> sync.update { it.copy(staleError = result.error) }
                }
            } finally {
                sync.update { it.copy(isLoadingMore = false) }
            }
        }
    }

    fun onQueryChange(query: String) {
        filters.update { it.copy(query = query) }
    }

    fun onToggleOnlyFavorites() {
        filters.update { it.copy(onlyFavorites = !it.onlyFavorites) }
    }

    fun onToggleFavorite(artwork: Artwork) {
        viewModelScope.launch { repository.setFavorite(artwork.id, !artwork.isFavorite) }
    }

    fun onDismissError() {
        sync.update { it.copy(staleError = null) }
    }

    private fun buildState(artworks: List<Artwork>, f: Filters, s: SyncStatus): VaultUiState {
        val blocking = s.blockingError
        if (blocking != null) return VaultUiState.Error(blocking, isRetrying = s.isRefreshing)
        return VaultUiState.Content(
            artworks = artworks,
            query = f.query,
            onlyFavorites = f.onlyFavorites,
            isRefreshing = s.isRefreshing,
            isLoadingMore = s.isLoadingMore,
            endReached = s.endReached,
            staleError = s.staleError
        )
    }
}
