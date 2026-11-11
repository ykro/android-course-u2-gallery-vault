package com.awa2027.galleryvault.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Solo modo claro: la app no define esquema oscuro. */
private val VaultColors = lightColorScheme(
    primary = Color(0xFF0F766E),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF6D28D9),
    background = Color(0xFFF8F7F4),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF1F0EC),
    error = Color(0xFFB91C1C),
    tertiaryContainer = Color(0xFFFFFBEB),
    onTertiaryContainer = Color(0xFF78350F)
)

@Composable
fun GalleryVaultTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = VaultColors, content = content)
}
