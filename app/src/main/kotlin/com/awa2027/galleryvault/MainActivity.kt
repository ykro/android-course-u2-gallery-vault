package com.awa2027.galleryvault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.awa2027.galleryvault.ui.VaultRoute
import com.awa2027.galleryvault.ui.theme.GalleryVaultTheme
import org.koin.compose.viewmodel.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GalleryVaultTheme {
                VaultRoute(viewModel = koinViewModel())
            }
        }
    }
}
