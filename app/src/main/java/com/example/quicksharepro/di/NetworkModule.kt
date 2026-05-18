package com.example.quicksharepro.di

import com.example.quicksharepro.data.transfer.SocketClient
import com.example.quicksharepro.data.transfer.SocketServer
import com.example.quicksharepro.data.transfer.TransferEngine
import com.example.quicksharepro.data.wifi.WifiDirectManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideWifiDirectManager(@dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context): WifiDirectManager {
        return WifiDirectManager(context)
    }

    @Provides
    @Singleton
    fun provideTransferEngine(@dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context): TransferEngine {
        return TransferEngine(context)
    }
}
