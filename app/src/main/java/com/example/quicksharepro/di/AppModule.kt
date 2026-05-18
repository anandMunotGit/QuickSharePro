package com.example.quicksharepro.di

import android.content.Context
import androidx.room.Room
import com.example.quicksharepro.data.local.QuickShareDatabase
import com.example.quicksharepro.data.local.TransferDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): QuickShareDatabase {
        return Room.databaseBuilder(
            context,
            QuickShareDatabase::class.java,
            "quickshare_db"
        ).build()
    }

    @Provides
    fun provideTransferDao(db: QuickShareDatabase): TransferDao {
        return db.transferDao
    }
}
