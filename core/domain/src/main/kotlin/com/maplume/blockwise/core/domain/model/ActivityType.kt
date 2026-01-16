package com.maplume.blockwise.core.domain.model

/**
 * Domain model representing an activity type.
 * Supports hierarchical structure through parentId.
 */
data class ActivityType(
    val id: Long = 0,
    val name: String,
    val colorHex: String,
    val icon: String? = null,
    val parentId: Long? = null,
    val displayOrder: Int = 0,
    val isArchived: Boolean = false,
    val children: List<ActivityType> = emptyList()
) {
    /**
     * Check if this activity type is a root (top-level) type.
     */
    val isRoot: Boolean get() = parentId == null

    val isRootLevel: Boolean get() = isRoot
}
