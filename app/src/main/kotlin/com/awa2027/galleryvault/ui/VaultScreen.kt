package com.awa2027.galleryvault.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.awa2027.galleryvault.domain.AppError
import com.awa2027.galleryvault.domain.Artwork

/** Cuántas celdas antes del final se pide la siguiente página. */
const val LOAD_MORE_THRESHOLD = 6

/** Las pinturas suelen ser verticales: tarjetas de 4:5 recortan menos que un cuadrado. */
private const val PAINTING_ASPECT_RATIO = 0.8f

/** Regla de paginación como función pura: se prueba sin Compose. */
fun shouldLoadMore(lastVisibleIndex: Int, totalItems: Int, threshold: Int = LOAD_MORE_THRESHOLD): Boolean =
    TODO("Taller, paso 3: true si hay elementos y el último índice visible está a threshold o menos del final")

/**
 * Taller, paso 3: pide la siguiente página una sola vez al acercarse al final de la grilla.
 * Convierte el último índice visible en un booleano con derivedStateOf y shouldLoadMore, y
 * llama a onLoadMore desde un LaunchedEffect que solo se relanza cuando ese booleano cambia.
 * Mientras tanto la grilla solo carga más páginas con el botón "Cargar más".
 */
@Composable
fun LoadMoreEffect(gridState: LazyGridState, enabled: Boolean, onLoadMore: () -> Unit) {
}

@Composable
fun VaultRoute(viewModel: VaultViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    VaultScreen(
        state = state,
        onRefresh = viewModel::onRefresh,
        onRetry = viewModel::onRetry,
        onLoadMore = viewModel::onLoadMore,
        onQueryChange = viewModel::onQueryChange,
        onToggleOnlyFavorites = viewModel::onToggleOnlyFavorites,
        onToggleFavorite = viewModel::onToggleFavorite,
        onDismissError = viewModel::onDismissError
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    state: VaultUiState,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onQueryChange: (String) -> Unit,
    onToggleOnlyFavorites: () -> Unit,
    onToggleFavorite: (Artwork) -> Unit,
    onDismissError: () -> Unit
) {
    Scaffold(topBar = { TopAppBar(title = { Text("Gallery Vault") }) }) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (state) {
                VaultUiState.Loading -> CenteredProgress()

                is VaultUiState.Error -> FullScreenError(state.error, state.isRetrying, onRetry)

                is VaultUiState.Content -> Column(Modifier.fillMaxSize()) {
                    SearchBar(state.query, state.onlyFavorites, onQueryChange, onToggleOnlyFavorites)
                    state.staleError?.let { StaleBanner(it, onRetry, onDismissError) }
                    PullToRefreshBox(
                        isRefreshing = state.isRefreshing,
                        onRefresh = onRefresh,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        ArtworkGrid(state, onLoadMore, onToggleFavorite)
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtworkGrid(state: VaultUiState.Content, onLoadMore: () -> Unit, onToggleFavorite: (Artwork) -> Unit) {
    val gridState: LazyGridState = rememberLazyGridState()
    val canLoadMore = !state.endReached && state.query.isBlank() && !state.onlyFavorites
    LoadMoreEffect(gridState = gridState, enabled = canLoadMore, onLoadMore = onLoadMore)
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 156.dp),
        state = gridState,
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(state.artworks, key = { it.id }) { artwork ->
            ArtworkCard(artwork, onToggleFavorite)
        }
        if (canLoadMore && state.artworks.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                    if (state.isLoadingMore) {
                        CircularProgressIndicator()
                    } else {
                        TextButton(onClick = onLoadMore) { Text("Cargar más") }
                    }
                }
            }
        }
        if (state.artworks.isEmpty() && !state.isRefreshing) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = if (state.query.isBlank()) "La bóveda está vacía." else "Ninguna obra coincide.",
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ArtworkCard(artwork: Artwork, onToggleFavorite: (Artwork) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        AsyncImage(
            model = artwork.imageUrl.ifBlank { null },
            contentDescription = artwork.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(PAINTING_ASPECT_RATIO)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Row(Modifier.padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp)) {
            Column(Modifier.weight(1f)) {
                Text(
                    artwork.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    listOf(artwork.artist, artwork.creationDate).filter { it.isNotBlank() }.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = { onToggleFavorite(artwork) }) {
                Icon(
                    imageVector = if (artwork.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (artwork.isFavorite) "Quitar de favoritos" else "Marcar como favorita",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onlyFavorites: Boolean,
    onQueryChange: (String) -> Unit,
    onToggleOnlyFavorites: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text("Buscar por título o artista") },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.padding(4.dp))
        FilterChip(
            selected = onlyFavorites,
            onClick = onToggleOnlyFavorites,
            label = { Text("Favoritos") }
        )
    }
}

@Composable
private fun StaleBanner(error: AppError, onRetry: () -> Unit, onDismiss: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Mostrando datos guardados. ${error.userMessage()}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onRetry) { Text("Reintentar") }
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    }
}

@Composable
private fun FullScreenError(error: AppError, isRetrying: Boolean, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("No hay obras guardadas todavía.", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(error.userMessage(), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        if (isRetrying) {
            CircularProgressIndicator()
        } else {
            Button(onClick = onRetry) { Text("Reintentar") }
        }
    }
}

@Composable
private fun CenteredProgress() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}
