package com.maplume.blockwise.feature.goal.di

import com.maplume.blockwise.core.domain.repository.GoalRepository
import com.maplume.blockwise.feature.goal.data.repository.GoalRepositoryImpl
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
    abstract fun bindGoalRepository(
        impl: GoalRepositoryImpl
    ): GoalRepository
}

