package com.example.quicksharepro.di

import com.example.quicksharepro.data.repository.*
import com.example.quicksharepro.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWifiDirectRepository(
        impl: WifiDirectRepositoryImpl
    ): WifiDirectRepository

    @Binds
    @Singleton
    abstract fun bindFileTransferRepository(
        impl: FileTransferRepositoryImpl
    ): FileTransferRepository

    @Binds
    @Singleton
    abstract fun bindTransferHistoryRepository(
        impl: TransferHistoryRepositoryImpl
    ): TransferHistoryRepository

    @Binds
    @Singleton
    abstract fun bindFileSelectionRepository(
        impl: FileSelectionRepositoryImpl
    ): FileSelectionRepository
}
