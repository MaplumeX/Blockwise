package com.maplume.blockwise.feature.timeentry.domain.usecase.activitytype

import com.maplume.blockwise.core.domain.model.ActivityType
import com.maplume.blockwise.core.domain.repository.ActivityTypeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving activity types.
 * Supports filtering by archived status.
 */
class GetActivityTypesUseCase @Inject constructor(
    private val repository: ActivityTypeRepository
) {
    /**
     * Get all activity types.
     * @param includeArchived Whether to include archived types. Defaults to false.
     * @return Flow of activity type list.
     */
    operator fun invoke(includeArchived: Boolean = false): Flow<List<ActivityType>> {
        return if (includeArchived) {
            repository.getAll()
        } else {
            repository.getAllActive()
        }
    }

    /**
     * Get root level activity types only.
     */
    fun getRootLevel(): Flow<List<ActivityType>> {
        return repository.getRootLevel()
    }

    /**
     * Get children of a specific activity type.
     */
    fun getChildren(parentId: Long): Flow<List<ActivityType>> {
        return repository.getChildren(parentId)
    }
}
