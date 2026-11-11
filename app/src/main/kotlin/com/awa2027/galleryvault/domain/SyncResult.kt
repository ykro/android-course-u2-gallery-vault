package com.awa2027.galleryvault.domain

/** Resultado de traer una página de la API y guardarla en Room. */
sealed interface SyncResult {
    /** [received] obras guardadas; [endReached] es true si la API no tiene más páginas. */
    data class Synced(val received: Int, val endReached: Boolean) : SyncResult

    data class Failed(val error: AppError) : SyncResult
}
