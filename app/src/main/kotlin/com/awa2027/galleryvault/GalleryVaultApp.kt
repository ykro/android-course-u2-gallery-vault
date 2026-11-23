package com.awa2027.galleryvault

import android.app.Application
import com.awa2027.galleryvault.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class GalleryVaultApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@GalleryVaultApp)
            modules(appModule)
        }
    }
}
