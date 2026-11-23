package com.awa2027.galleryvault.data.local

import androidx.room3.Database
import androidx.room3.RoomDatabase

@Database(entities = [ArtworkEntity::class], version = 1, exportSchema = false)
abstract class VaultDatabase : RoomDatabase() {
    abstract fun artworkDao(): ArtworkDao

    companion object {
        const val NAME = "gallery-vault.db"
    }
}
