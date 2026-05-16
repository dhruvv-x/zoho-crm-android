package com.pookie.octfis.di

import android.content.Context
import com.pookie.octfis.data.remote.TokenStore
import com.pookie.octfis.data.remote.ZohoApiClient
import com.pookie.octfis.data.remote.ZohoApiService
import com.pookie.octfis.data.remote.ZohoAuthManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideTokenStore(@ApplicationContext context: Context): TokenStore =
        TokenStore(context)

    @Provides
    @Singleton
    fun provideZohoAuthManager(
        @ApplicationContext context: Context,
        tokenStore: TokenStore,
    ): ZohoAuthManager = ZohoAuthManager(context, tokenStore)

    @Provides
    @Singleton
    fun provideZohoApiService(authManager: ZohoAuthManager): ZohoApiService =
        ZohoApiClient.create(authManager)
}