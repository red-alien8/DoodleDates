package com.spycodedoodledates.di

import android.content.Context
import com.spycodedoodledates.ui.theme.settings.ThemeSettings
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
    fun provideThemeSettings(
        @ApplicationContext context: Context
    ): ThemeSettings {
        return ThemeSettings(context)
    }
}
