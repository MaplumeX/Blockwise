package com.maplume.blockwise.core.domain.repository

import com.maplume.blockwise.core.domain.model.ActivityType
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for activity type operations.
 */
interface ActivityTypeRepository {

    /**
     * Get all active (non-archived) activity types.
     */
    fun getAllActiveActivityTypes(): Flow<List<ActivityType>>

    /**
     * Get all activity types including archived ones.
     */
    fun getAllActivityTypes(): Flow<List<ActivityType>>

    /**
     * Get an activity type by its ID.
     */
    suspend fun getActivityTypeById(id: Long): ActivityType?

    /**
     * Get root-level activity types (no parent).
     */
    fun getRootActivityTypes(): Flow<List<ActivityType>>

    /**
     * Get child activity types for a given parent.
     */
    fun getChildActivityTypes(parentId: Long): Flow<List<ActivityType>>

    /**
     * Insert a new activity type.
     * @return The ID of the inserted activity type
     */
    suspend fun insertActivityType(activityType: ActivityType): Long

    /**
     * Update an existing activity type.
     */
    suspend fun updateActivityType(activityType: ActivityType)

    /**
     * Archive an activity type (soft delete).
     */
    suspend fun archiveActivityType(id: Long)

    /**
     * Unarchive an activity type.
     */
    suspend fun unarchiveActivityType(id: Long)
}
