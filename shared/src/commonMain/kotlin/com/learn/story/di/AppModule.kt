package com.learn.story.di

import io.ktor.client.engine.HttpClientEngine
import com.learn.story.data.network.createHttpClient
import com.learn.story.data.network.createGeocodingHttpClient
import com.learn.story.data.network.getHttpClientEngine
import com.learn.story.data.storage.TokenStorage
import com.learn.story.data.local.AppDatabase
import com.learn.story.data.local.createRoomDatabase
import com.learn.story.data.local.getDatabaseBuilder
import com.learn.story.data.remote.StoryApiService
import com.learn.story.data.remote.GeocodingService
import com.learn.story.data.repository.AuthRepository
import com.learn.story.data.repository.StoryRepository
import com.learn.story.ui.screens.add.AddStoryViewModel
import com.learn.story.ui.screens.auth.LoginViewModel
import com.learn.story.ui.screens.auth.RegisterViewModel
import com.learn.story.ui.screens.detail.DetailViewModel
import com.learn.story.ui.screens.home.HomeViewModel
import com.learn.story.ui.screens.map.StoryMapViewModel
import com.learn.story.data.repository.ThemeRepository
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    // Storage, Preferences & Database (Room KMP)
    single { TokenStorage() }
    single { ThemeRepository() }
    single<AppDatabase> { createRoomDatabase(getDatabaseBuilder()) }
    single { get<AppDatabase>().storyDao() }
    single { get<AppDatabase>().bookmarkDao() }
    single { get<AppDatabase>().offlineDraftDao() }

    // Network Engine
    single<HttpClientEngine> { getHttpClientEngine() }
    single { createHttpClient(get(), get()) }
    single(org.koin.core.qualifier.named("geocodingClient")) { createGeocodingHttpClient(get()) }
    single { StoryApiService(get()) }
    single { GeocodingService(get(org.koin.core.qualifier.named("geocodingClient"))) }

    // Repositories (MVVM + Repository)
    single { AuthRepository(get(), get()) }
    single { StoryRepository(get(), get(), get(), get()) }

    // ViewModels (MVVM)
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::DetailViewModel)
    viewModelOf(::AddStoryViewModel)
    viewModelOf(::StoryMapViewModel)
}
