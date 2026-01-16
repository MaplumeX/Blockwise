package com.maplume.blockwise.feature.timeentry.di

import com.maplume.blockwise.core.domain.repository.ActivityTypeRepository
import com.maplume.blockwise.core.domain.repository.TagRepository
import com.maplume.blockwise.core.domain.repository.TimeEntryRepository
import com.maplume.blockwise.feature.timeentry.data.repository.ActivityTypeRepositoryImpl
import com.maplume.blockwise.feature.timeentry.data.repository.TagRepositoryImpl
import com.maplume.blockwise.feature.timeentry.data.repository.TimeEntryRepositoryImpl
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
    abstract fun bindActivityTypeRepository(
        impl: ActivityTypeRepositoryImpl
    ): ActivityTypeRepository

    @Binds
    @Singleton
    abstract fun bindTagRepository(
        impl: TagRepositoryImpl
    ): TagRepository

    @Binds
    @Singleton
    abstract fun bindTimeEntryRepository(
        impl: TimeEntryRepositoryImpl
    ): TimeEntryRepository
}

