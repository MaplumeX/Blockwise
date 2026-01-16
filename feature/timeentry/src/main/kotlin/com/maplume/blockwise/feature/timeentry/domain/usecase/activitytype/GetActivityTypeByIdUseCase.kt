package com.maplume.blockwise.feature.timeentry.domain.usecase.activitytype

import com.maplume.blockwise.core.domain.model.ActivityType
import com.maplume.blockwise.core.domain.repository.ActivityTypeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving a single activity type by ID.
 */
class GetActivityTypeByIdUseCase @Inject constructor(
    private val repository: ActivityTypeRepository
) {
    /**
     * Get activity type by ID (suspend version).
     * @param id The activity type ID.
     * @return The activity type or null if not found.
     */
    suspend operator fun invoke(id: Long): ActivityType? {
        return repository.getById(id)
    }

    /**
     * Get activity type by ID as Flow for reactive updates.
     * @param id The activity type ID.
     * @return Flow of the activity type.
     */
    fun asFlow(id: Long): Flow<ActivityType?> {
        return repository.getByIdFlow(id)
    }
}
