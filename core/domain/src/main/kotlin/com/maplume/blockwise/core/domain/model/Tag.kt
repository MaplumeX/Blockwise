package com.maplume.blockwise.core.domain.model

/**
 * Domain model representing a tag.
 */
data class Tag(
    val id: Long = 0,
    val name: String,
    val colorHex: String,
    val isArchived: Boolean = false
)
