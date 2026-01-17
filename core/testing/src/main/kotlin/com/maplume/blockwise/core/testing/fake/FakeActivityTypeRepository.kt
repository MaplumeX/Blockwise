package com.maplume.blockwise.core.testing.fake

import com.maplume.blockwise.core.domain.model.ActivityType
import com.maplume.blockwise.core.domain.repository.ActivityTypeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Fake implementation of ActivityTypeRepository for testing.
 * Stores activity types in memory and provides controllable behavior.
 */
class FakeActivityTypeRepository : ActivityTypeRepository {

    private val activityTypes = MutableStateFlow<List<ActivityType>>(emptyList())
    private var nextId = 1L

    /**
     * Control whether operations should fail.
     */
    var shouldFail = false
    var failureException: Exception = RuntimeException("Test failure")

    /**
     * Get all activity types for inspection.
     */
    fun getAllActivityTypes(): List<ActivityType> = activityTypes.value

    /**
     * Set activity types directly for testing.
     */
    fun setActivityTypes(types: List<ActivityType>) {
        activityTypes.value = types
        nextId = (types.maxOfOrNull { it.id } ?: 0) + 1
    }

    /**
     * Clear all activity types.
     */
    fun clear() {
        activityTypes.value = emptyList()
        nextId = 1L
    }

    override fun getAllActive(): Flow<List<ActivityType>> {
        return activityTypes.map { list -> list.filter { !it.isArchived } }
    }

    override fun getAll(): Flow<List<ActivityType>> {
        return activityTypes
    }

    override suspend fun getById(id: Long): ActivityType? {
        if (shouldFail) throw failureException
        return activityTypes.value.find { it.id == id }
    }

    override fun getByIdFlow(id: Long): Flow<ActivityType?> {
        return activityTypes.map { list -> list.find { it.id == id } }
    }

    override fun getRootLevel(): Flow<List<ActivityType>> {
        return activityTypes.map { list -> list.filter { it.parentId == null && !it.isArchived } }
    }

    override fun getChildren(parentId: Long): Flow<List<ActivityType>> {
        return activityTypes.map { list -> list.filter { it.parentId == parentId && !it.isArchived } }
    }

    override suspend fun isNameExists(name: String, excludeId: Long): Boolean {
        if (shouldFail) throw failureException
        return activityTypes.value.any { it.name == name && it.id != excludeId && !it.isArchived }
    }

    override suspend fun create(
        name: String,
        colorHex: String,
        icon: String?,
        parentId: Long?
    ): Long {
        if (shouldFail) throw failureException

        val id = nextId++
        val type = ActivityType(
            id = id,
            name = name,
            colorHex = colorHex,
            icon = icon,
            parentId = parentId,
            displayOrder = activityTypes.value.size
        )
        activityTypes.value = activityTypes.value + type
        return id
    }

    override suspend fun update(
        id: Long,
        name: String,
        colorHex: String,
        icon: String?,
        parentId: Long?
    ) {
        if (shouldFail) throw failureException

        activityTypes.value = activityTypes.value.map { type ->
            if (type.id == id) {
                type.copy(
                    name = name,
                    colorHex = colorHex,
                    icon = icon,
                    parentId = parentId
                )
            } else {
                type
            }
        }
    }

    override suspend fun updateDisplayOrder(id: Long, order: Int) {
        if (shouldFail) throw failureException

        activityTypes.value = activityTypes.value.map { type ->
            if (type.id == id) type.copy(displayOrder = order) else type
        }
    }

    override suspend fun delete(id: Long) {
        if (shouldFail) throw failureException
        activityTypes.value = activityTypes.value.map { type ->
            if (type.id == id) type.copy(isArchived = true) else type
        }
    }

    override suspend fun restore(id: Long) {
        if (shouldFail) throw failureException
        activityTypes.value = activityTypes.value.map { type ->
            if (type.id == id) type.copy(isArchived = false) else type
        }
    }
}
