package com.maplume.blockwise.core.data.database.converter

import androidx.room.TypeConverter
import com.maplume.blockwise.core.domain.model.GoalPeriod
import com.maplume.blockwise.core.domain.model.GoalType

class EnumConverters {

    @TypeConverter
    fun fromGoalType(type: GoalType?): String? = type?.name

    @TypeConverter
    fun toGoalType(name: String?): GoalType? = name?.let { GoalType.valueOf(it) }

    @TypeConverter
    fun fromGoalPeriod(period: GoalPeriod?): String? = period?.name

    @TypeConverter
    fun toGoalPeriod(name: String?): GoalPeriod? = name?.let { GoalPeriod.valueOf(it) }
}

