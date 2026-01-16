package com.maplume.blockwise.core.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class GoalWithTag(
    @Embedded
    val goal: GoalEntity,
    @Relation(
        parentColumn = "tag_id",
        entityColumn = "id"
    )
    val tag: TagEntity
)
