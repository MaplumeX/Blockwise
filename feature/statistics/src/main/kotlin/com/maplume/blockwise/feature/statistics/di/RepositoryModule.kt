package com.maplume.blockwise.feature.statistics.di

import com.maplume.blockwise.core.domain.repository.StatisticsRepository
import com.maplume.blockwise.feature.statistics.data.repository.StatisticsRepositoryImpl
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
    abstract fun bindStatisticsRepository(
        impl: StatisticsRepositoryImpl
    ): StatisticsRepository
}

