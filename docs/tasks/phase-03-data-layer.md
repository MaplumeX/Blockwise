---
文档类型: 阶段任务文档
阶段编号: 03
阶段名称: 数据层实现
版本: v1.0
创建日期: 2026-01-15
预计工期: 4-5天
前置条件: 阶段一完成
---

# 阶段三：数据层实现

## 1. 阶段目标

### 1.1 核心目标

实现 Blockwise 应用的完整数据层，包括数据库设计、Entity 定义、DAO 接口、领域模型、数据映射以及 Repository 实现，为业务逻辑层提供可靠的数据访问接口。

### 1.2 交付成果

- 完整的 Room 数据库配置
- 所有核心实体（Entity）定义
- 数据访问对象（DAO）接口
- 领域模型（Domain Model）定义
- Entity 与 Domain Model 映射器
- Repository 接口定义与实现
- 数据库性能优化索引

### 1.3 数据模型概览

| 实体 | 说明 | 核心字段 |
|------|------|----------|
| TimeEntry | 时间记录 | startTime, endTime, activityId |
| ActivityType | 活动类型 | name, color, parentId |
| Tag | 标签 | name, color |
| TimeEntryTag | 记录-标签关联 | entryId, tagId |
| Goal | 目标 | tagId, targetMinutes, period |

---

## 2. 任务列表

### 2.1 数据库设计实现

#### T3.1.1 实现TypeConverter

| 属性 | 值 |
|------|-----|
| **任务ID** | T3.1.1 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | T1.3.2, T1.3.5 |

**任务描述**：
实现 Room 数据库类型转换器，处理 Instant、LocalDate、Enum 等特殊类型的存储。

**执行步骤**：
1. 创建 Converters 类
2. 实现 Instant ↔ Long 转换
3. 实现 LocalDate ↔ String 转换
4. 实现 Enum ↔ String 转换
5. 在 Database 类上注册转换器

**关键代码**：
```kotlin
// Converters.kt
package com.blockwise.core.data.database.converter

import androidx.room.TypeConverter
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

class Converters {

    // Instant <-> Long (epoch millis)
    @TypeConverter
    fun fromInstant(instant: Instant?): Long? {
        return instant?.toEpochMilliseconds()
    }

    @TypeConverter
    fun toInstant(epochMillis: Long?): Instant? {
        return epochMillis?.let { Instant.fromEpochMilliseconds(it) }
    }

    // LocalDate <-> String (ISO-8601)
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let { LocalDate.parse(it) }
    }
}

// GoalType Enum
enum class GoalType {
    MIN,    // 最少时长
    MAX,    // 最多时长
    EXACT   // 精确时长
}

// GoalPeriod Enum
enum class GoalPeriod {
    DAILY,
    WEEKLY,
    MONTHLY,
    CUSTOM
}

// Enum Converters
class EnumConverters {

    @TypeConverter
    fun fromGoalType(type: GoalType): String = type.name

    @TypeConverter
    fun toGoalType(name: String): GoalType = GoalType.valueOf(name)

    @TypeConverter
    fun fromGoalPeriod(period: GoalPeriod): String = period.name

    @TypeConverter
    fun toGoalPeriod(name: String): GoalPeriod = GoalPeriod.valueOf(name)
}
```

**验收标准**：
- [ ] Instant 转换正确（UTC时间戳）
- [ ] LocalDate 转换正确（ISO-8601格式）
- [ ] Enum 转换正确
- [ ] 空值处理正确（返回 null）

---

#### T3.1.2 实现ActivityType Entity

| 属性 | 值 |
|------|-----|
| **任务ID** | T3.1.2 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | T3.1.1 |

**任务描述**：
实现活动类型实体类，支持层级结构（父-子关系）。

**执行步骤**：
1. 定义 Entity 类和表名
2. 定义主键和字段
3. 定义外键约束（自关联）
4. 定义索引

**关键代码**：
```kotlin
// ActivityTypeEntity.kt
package com.blockwise.core.data.entity

import androidx.room.*

@Entity(
    tableName = "activity_types",
    foreignKeys = [
        ForeignKey(
            entity = ActivityTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["parent_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["parent_id"]),
        Index(value = ["display_order"])
    ]
)
data class ActivityTypeEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "color_hex")
    val colorHex: String,

    @ColumnInfo(name = "icon")
    val icon: String? = null,

    @ColumnInfo(name = "parent_id")
    val parentId: Long? = null,

    @ColumnInfo(name = "display_order")
    val displayOrder: Int = 0,

    @ColumnInfo(name = "is_archived", defaultValue = "0")
    val isArchived: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Instant,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant
)
```

**验收标准**：
- [ ] 表名为 `activity_types`
- [ ] 主键自增
- [ ] 外键约束正确（self-reference）
- [ ] 索引定义完成
- [ ] 软删除字段 `is_archived`

---

#### T3.1.3 实现Tag Entity

| 属性 | 值 |
|------|-----|
| **任务ID** | T3.1.3 |
| **优先级** | P0 |
| **预计耗时** | 0.5h |
| **依赖任务** | T3.1.1 |

**任务描述**：
实现标签实体类，简单扁平结构。

**执行步骤**：
1. 定义 Entity 类和表名
2. 定义主键和字段
3. 定义唯一约束（name）

**关键代码**：
```kotlin
// TagEntity.kt
package com.blockwise.core.data.entity

import androidx.room.*
import kotlinx.datetime.Instant

@Entity(
    tableName = "tags",
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "color_hex")
    val colorHex: String,

    @ColumnInfo(name = "is_archived", defaultValue = "0")
    val isArchived: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Instant,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant
)
```

**验收标准**：
- [ ] 表名为 `tags`
- [ ] 名称唯一约束
- [ ] 软删除字段 `is_archived`

---

#### T3.1.4 实现TimeEntry Entity

| 属性 | 值 |
|------|-----|
| **任务ID** | T3.1.4 |
| **优先级** | P0 |
| **预计耗时** | 1.5h |
| **依赖任务** | T3.1.1 |

**任务描述**：
实现时间记录实体类，作为核心数据表。

**执行步骤**：
1. 定义 Entity 类和表名
2. 定义主键和字段
3. 定义外键约束（activity_id）
4. 定义复合索引优化查询

**关键代码**：
```kotlin
// TimeEntryEntity.kt
package com.blockwise.core.data.entity

import androidx.room.*
import kotlinx.datetime.Instant

@Entity(
    tableName = "time_entries",
    foreignKeys = [
        ForeignKey(
            entity = ActivityTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["activity_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["activity_id"]),
        Index(value = ["start_time"]),
        Index(value = ["end_time"]),
        Index(value = ["start_time", "activity_id"]) // Composite index for statistics
    ]
)
data class TimeEntryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "activity_id")
    val activityId: Long,

    @ColumnInfo(name = "start_time")
    val startTime: Instant,

    @ColumnInfo(name = "end_time")
    val endTime: Instant,

    @ColumnInfo(name = "duration_minutes")
    val durationMinutes: Int,

    @ColumnInfo(name = "note")
    val note: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Instant,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant
)

// TimeEntry with Activity and Tags (for query results)
data class TimeEntryWithDetails(
    @Embedded
    val entry: TimeEntryEntity,

    @Relation(
        parentColumn = "activity_id",
        entityColumn = "id"
    )
    val activity: ActivityTypeEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TimeEntryTagEntity::class,
            parentColumn = "entry_id",
            entityColumn = "tag_id"
        )
    )
    val tags: List<TagEntity>
)
```

**验收标准**：
- [ ] 表名为 `time_entries`
- [ ] 外键约束正确（RESTRICT 删除策略）
- [ ] 复合索引优化统计查询
- [ ] 关联查询对象定义完成

---

#### T3.1.5 实现TimeEntryTag Entity

| 属性 | 值 |
|------|-----|
| **任务ID** | T3.1.5 |
| **优先级** | P0 |
| **预计耗时** | 0.5h |
| **依赖任务** | T3.1.3, T3.1.4 |

**任务描述**：
实现时间记录-标签关联表（多对多关系）。

**执行步骤**：
1. 定义 Entity 类和表名
2. 定义复合主键
3. 定义外键约束（CASCADE 删除）
4. 定义索引

**关键代码**：
```kotlin
// TimeEntryTagEntity.kt
package com.blockwise.core.data.entity

import androidx.room.*

@Entity(
    tableName = "time_entry_tags",
    primaryKeys = ["entry_id", "tag_id"],
    foreignKeys = [
        ForeignKey(
            entity = TimeEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entry_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tag_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["entry_id"]),
        Index(value = ["tag_id"])
    ]
)
data class TimeEntryTagEntity(
    @ColumnInfo(name = "entry_id")
    val entryId: Long,

    @ColumnInfo(name = "tag_id")
    val tagId: Long
)
```

**验收标准**：
- [ ] 复合主键 (entry_id, tag_id)
- [ ] 双向外键约束
- [ ] CASCADE 删除策略
- [ ] 双向索引

---

#### T3.1.6 实现Goal Entity

| 属性 | 值 |
|------|-----|
| **任务ID** | T3.1.6 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | T3.1.1 |

**任务描述**：
实现目标实体类，支持多种周期类型和目标类型。

**执行步骤**：
1. 定义 Entity 类和表名
2. 定义主键和字段
3. 定义外键约束（tag_id）
4. 定义 Enum 字段

**关键代码**：
```kotlin
// GoalEntity.kt
package com.blockwise.core.data.entity

import androidx.room.*
import com.blockwise.core.data.database.converter.GoalPeriod
import com.blockwise.core.data.database.converter.GoalType
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "goals",
    foreignKeys = [
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tag_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["tag_id"]),
        Index(value = ["is_active"])
    ]
)
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "tag_id")
    val tagId: Long,

    @ColumnInfo(name = "target_minutes")
    val targetMinutes: Int,

    @ColumnInfo(name = "goal_type")
    val goalType: GoalType,

    @ColumnInfo(name = "period")
    val period: GoalPeriod,

    @ColumnInfo(name = "start_date")
    val startDate: LocalDate? = null,

    @ColumnInfo(name = "end_date")
    val endDate: LocalDate? = null,

    @ColumnInfo(name = "is_active", defaultValue = "1")
    val isActive: Boolean = true,

    @ColumnInfo(name = "created_at")
    val createdAt: Instant,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant
)

// Goal with Tag (for query results)
data class GoalWithTag(
    @Embedded
    val goal: GoalEntity,

    @Relation(
        parentColumn = "tag_id",
        entityColumn = "id"
    )
    val tag: TagEntity
)
```

**验收标准**：
- [ ] 表名为 `goals`
- [ ] 外键约束正确（CASCADE 删除）
- [ ] Enum 字段正确映射
- [ ] 自定义周期支持 start_date/end_date

---

#### T3.1.7 创建AppDatabase

| 属性 | 值 |
|------|-----|
| **任务ID** | T3.1.7 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | T3.1.2, T3.1.3, T3.1.4, T3.1.5, T3.1.6 |

**任务描述**：
创建 Room 数据库定义类，注册所有 Entity 和 TypeConverter。

**执行步骤**：
1. 创建 AppDatabase 抽象类
2. 注册所有 Entity
3. 注册 TypeConverter
4. 配置数据库版本
5. 创建 Hilt 模块提供数据库实例

**关键代码**：
```kotlin
// AppDatabase.kt
package com.blockwise.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.blockwise.core.data.database.converter.Converters
import com.blockwise.core.data.database.converter.EnumConverters
import com.blockwise.core.data.dao.*
import com.blockwise.core.data.entity.*

@Database(
    entities = [
        ActivityTypeEntity::class,
        TagEntity::class,
        TimeEntryEntity::class,
        TimeEntryTagEntity::class,
        GoalEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class, EnumConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun activityTypeDao(): ActivityTypeDao
    abstract fun tagDao(): TagDao
    abstract fun timeEntryDao(): TimeEntryDao
    abstract fun timeEntryTagDao(): TimeEntryTagDao
    abstract fun goalDao(): GoalDao
}

// DatabaseModule.kt
package com.blockwise.core.data.di

import android.content.Context
import androidx.room.Room
import com.blockwise.core.data.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "blockwise.db"
        )
            .fallbackToDestructiveMigration() // TODO: Replace with proper migration in production
            .build()
    }

    @Provides
    fun provideActivityTypeDao(database: AppDatabase) = database.activityTypeDao()

    @Provides
    fun provideTagDao(database: AppDatabase) = database.tagDao()

    @Provides
    fun provideTimeEntryDao(database: AppDatabase) = database.timeEntryDao()

    @Provides
    fun provideTimeEntryTagDao(database: AppDatabase) = database.timeEntryTagDao()

    @Provides
    fun provideGoalDao(database: AppDatabase) = database.goalDao()
}
```

**验收标准**：
- [ ] 所有 Entity 注册
- [ ] TypeConverter 注册
- [ ] Database 版本为 1
- [ ] Schema 导出启用
- [ ] Hilt 模块正确配置

---

#### T3.1.8 创建数据库索引

| 属性 | 值 |
|------|-----|
| **任务ID** | T3.1.8 |
| **优先级** | P1 |
| **预计耗时** | 0.5h |
| **依赖任务** | T3.1.7 |

**任务描述**：
验证并优化数据库索引配置，确保查询性能。

**执行步骤**：
1. 审查现有索引配置
2. 添加统计查询优化索引
3. 验证索引生效

**索引清单**：

| 表名 | 索引字段 | 类型 | 用途 |
|------|---------|------|------|
| time_entries | start_time | 单字段 | 按时间查询 |
| time_entries | activity_id | 单字段 | 按活动筛选 |
| time_entries | (start_time, activity_id) | 复合 | 统计聚合 |
| activity_types | parent_id | 单字段 | 层级查询 |
| time_entry_tags | entry_id | 单字段 | 查找记录标签 |
| time_entry_tags | tag_id | 单字段 | 查找标签记录 |
| goals | tag_id | 单字段 | 按标签查目标 |
| goals | is_active | 单字段 | 活动目标查询 |

**验收标准**：
- [ ] 所有索引在 Entity 中定义
- [ ] 复合索引用于统计查询
- [ ] Schema 文件中可见索引定义

---

### 2.2 DAO层实现

#### T3.2.1 实现ActivityTypeDao

| 属性 | 值 |
|------|-----|
| **任务ID** | T3.2.1 |
| **优先级** | P0 |
| **预计耗时** | 1.5h |
| **依赖任务** | T3.1.7 |

**任务描述**：
实现活动类型数据访问对象，包括CRUD操作和层级查询。

**执行步骤**：
1. 定义基础 CRUD 方法
2. 定义层级查询方法
3. 实现 Flow 响应式查询
4. 实现软删除/恢复

**关键代码**：
```kotlin
// ActivityTypeDao.kt
package com.blockwise.core.data.dao

import androidx.room.*
import com.blockwise.core.data.entity.ActivityTypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityTypeDao {

    // Insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activityType: ActivityTypeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(activityTypes: List<ActivityTypeEntity>): List<Long>

    // Update
    @Update
    suspend fun update(activityType: ActivityTypeEntity)

    // Delete
    @Delete
    suspend fun delete(activityType: ActivityTypeEntity)

    @Query("DELETE FROM activity_types WHERE id = :id")
    suspend fun deleteById(id: Long)

    // Soft delete
    @Query("UPDATE activity_types SET is_archived = 1, updated_at = :updatedAt WHERE id = :id")
    suspend fun archive(id: Long, updatedAt: Long)

    @Query("UPDATE activity_types SET is_archived = 0, updated_at = :updatedAt WHERE id = :id")
    suspend fun unarchive(id: Long, updatedAt: Long)

    // Query all (active only)
    @Query("SELECT * FROM activity_types WHERE is_archived = 0 ORDER BY display_order ASC")
    fun getAllActive(): Flow<List<ActivityTypeEntity>>

    // Query all (including archived)
    @Query("SELECT * FROM activity_types ORDER BY display_order ASC")
    fun getAll(): Flow<List<ActivityTypeEntity>>

    // Query by ID
    @Query("SELECT * FROM activity_types WHERE id = :id")
    suspend fun getById(id: Long): ActivityTypeEntity?

    @Query("SELECT * FROM activity_types WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<ActivityTypeEntity?>

    // Query root level (no parent)
    @Query("SELECT * FROM activity_types WHERE parent_id IS NULL AND is_archived = 0 ORDER BY display_order ASC")
    fun getRootLevel(): Flow<List<ActivityTypeEntity>>

    // Query children of a parent
    @Query("SELECT * FROM activity_types WHERE parent_id = :parentId AND is_archived = 0 ORDER BY display_order ASC")
    fun getChildren(parentId: Long): Flow<List<ActivityTypeEntity>>

    // Query by name (for validation)
    @Query("SELECT * FROM activity_types WHERE name = :name AND is_archived = 0 LIMIT 1")
    suspend fun getByName(name: String): ActivityTypeEntity?

    // Update display order
    @Query("UPDATE activity_types SET display_order = :order, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateDisplayOrder(id: Long, order: Int, updatedAt: Long)

    // Count all active
    @Query("SELECT COUNT(*) FROM activity_types WHERE is_archived = 0")
    suspend fun countActive(): Int
}
```

**验收标准**：
- [ ] CRUD 操作完整
- [ ] 软删除/恢复功能
- [ ] Flow 响应式查询
- [ ] 层级查询支持

---

#### T3.2.2 实现TagDao

| 属性 | 值 |
|------|-----|
| **任务ID** | T3.2.2 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | T3.1.7 |

**任务描述**：
实现标签数据访问对象，包括CRUD操作。

**执行步骤**：
1. 定义基础 CRUD 方法
2. 实现 Flow 响应式查询
3. 实现软删除/恢复
4. 实现名称唯一性检查

**关键代码**：
```kotlin
// TagDao.kt
package com.blockwise.core.data.dao

import androidx.room.*
import com.blockwise.core.data.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    // Insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: TagEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tags: List<TagEntity>): List<Long>

    // Update
    @Update
    suspend fun update(tag: TagEntity)

    // Delete
    @Delete
    suspend fun delete(tag: TagEntity)

    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun deleteById(id: Long)

    // Soft delete
    @Query("UPDATE tags SET is_archived = 1, updated_at = :updatedAt WHERE id = :id")
    suspend fun archive(id: Long, updatedAt: Long)

    @Query("UPDATE tags SET is_archived = 0, updated_at = :updatedAt WHERE id = :id")
    suspend fun unarchive(id: Long, updatedAt: Long)

    // Query all (active only)
    @Query("SELECT * FROM tags WHERE is_archived = 0 ORDER BY name ASC")
    fun getAllActive(): Flow<List<TagEntity>>

    // Query all (including archived)
    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAll(): Flow<List<TagEntity>>

    // Query by ID
    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun getById(id: Long): TagEntity?

    @Query("SELECT * FROM tags WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<TagEntity?>

    // Query by IDs
    @Query("SELECT * FROM tags WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<TagEntity>

    // Query by name (for validation)
    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): TagEntity?

    // Check if name exists
    @Query("SELECT EXISTS(SELECT 1 FROM tags WHERE name = :name AND id != :excludeId)")
    suspend fun isNameExists(name: String, excludeId: Long = 0): Boolean

    // Count all active
    @Query("SELECT COUNT(*) FROM tags WHERE is_archived = 0")
    suspend fun countActive(): Int
}
```

**验收标准**：
- [ ] CRUD 操作完整
- [ ] 名称唯一性检查
- [ ] Flow 响应式查询
- [ ] 批量查询支持

---

#### T3.2.3 实现TimeEntryDao

| 属性 | 值 |
|------|-----|
| **任务ID** | T3.2.3 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T3.1.7 |

**任务描述**：
实现时间记录数据访问对象，包括CRUD操作和时间范围查询。

**执行步骤**：
1. 定义基础 CRUD 方法
2. 定义时间范围查询
3. 实现关联查询（带活动类型和标签）
4. 实现分页查询

**关键代码**：
```kotlin
// TimeEntryDao.kt
package com.blockwise.core.data.dao

import androidx.room.*
import com.blockwise.core.data.entity.TimeEntryEntity
import com.blockwise.core.data.entity.TimeEntryWithDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface TimeEntryDao {

    // Insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: TimeEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<TimeEntryEntity>): List<Long>

    // Update
    @Update
    suspend fun update(entry: TimeEntryEntity)

    // Delete
    @Delete
    suspend fun delete(entry: TimeEntryEntity)

    @Query("DELETE FROM time_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    // Query by ID
    @Query("SELECT * FROM time_entries WHERE id = :id")
    suspend fun getById(id: Long): TimeEntryEntity?

    @Transaction
    @Query("SELECT * FROM time_entries WHERE id = :id")
    suspend fun getByIdWithDetails(id: Long): TimeEntryWithDetails?

    @Transaction
    @Query("SELECT * FROM time_entries WHERE id = :id")
    fun getByIdWithDetailsFlow(id: Long): Flow<TimeEntryWithDetails?>

    // Query by time range
    @Query("""
        SELECT * FROM time_entries
        WHERE start_time >= :startTime AND start_time < :endTime
        ORDER BY start_time DESC
    """)
    fun getByTimeRange(startTime: Long, endTime: Long): Flow<List<TimeEntryEntity>>

    @Transaction
    @Query("""
        SELECT * FROM time_entries
        WHERE start_time >= :startTime AND start_time < :endTime
        ORDER BY start_time DESC
    """)
    fun getByTimeRangeWithDetails(startTime: Long, endTime: Long): Flow<List<TimeEntryWithDetails>>

    // Query by day (helper)
    @Transaction
    @Query("""
        SELECT * FROM time_entries
        WHERE start_time >= :dayStart AND start_time < :dayEnd
        ORDER BY start_time ASC
    """)
    fun getByDay(dayStart: Long, dayEnd: Long): Flow<List<TimeEntryWithDetails>>

    // Query by activity type
    @Query("""
        SELECT * FROM time_entries
        WHERE activity_id = :activityId
        ORDER BY start_time DESC
    """)
    fun getByActivityType(activityId: Long): Flow<List<TimeEntryEntity>>

    // Query recent entries (for timeline)
    @Transaction
    @Query("""
        SELECT * FROM time_entries
        ORDER BY start_time DESC
        LIMIT :limit OFFSET :offset
    """)
    fun getRecentWithDetails(limit: Int, offset: Int): Flow<List<TimeEntryWithDetails>>

    // Check for overlapping entries
    @Query("""
        SELECT * FROM time_entries
        WHERE id != :excludeId
          AND ((start_time < :endTime AND end_time > :startTime))
        LIMIT 1
    """)
    suspend fun findOverlapping(startTime: Long, endTime: Long, excludeId: Long = 0): TimeEntryEntity?

    // Count entries in time range
    @Query("""
        SELECT COUNT(*) FROM time_entries
        WHERE start_time >= :startTime AND start_time < :endTime
    """)
    suspend fun countByTimeRange(startTime: Long, endTime: Long): Int

    // Get total duration in time range
    @Query("""
        SELECT COALESCE(SUM(duration_minutes), 0) FROM time_entries
        WHERE start_time >= :startTime AND start_time < :endTime
    """)
    suspend fun getTotalDurationByTimeRange(startTime: Long, endTime: Long): Int

    // Get latest entry
    @Transaction
    @Query("SELECT * FROM time_entries ORDER BY end_time DESC LIMIT 1")
    suspend fun getLatest(): TimeEntryWithDetails?
}
```

**验收标准**：
- [ ] CRUD 操作完整
- [ ] 时间范围查询正确
- [ ] 关联查询正确（带 @Transaction）
- [ ] 重叠检测功能
- [ ] 分页查询支持

---

#### T3.2.4 实现TimeEntryTagDao

| 属性 | 值 |
|------|-----|
| **任务ID** | T3.2.4 |
| **优先级** | P0 |
| **预计耗时** | 0.5h |
| **依赖任务** | T3.1.7 |

**任务描述**：
实现时间记录-标签关联表数据访问对象。

**执行步骤**：
1. 定义插入/删除方法
2. 定义查询方法
3. 支持批量操作

**关键代码**：
```kotlin
// TimeEntryTagDao.kt
package com.blockwise.core.data.dao

import androidx.room.*
import com.blockwise.core.data.entity.TimeEntryTagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeEntryTagDao {

    // Insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(relation: TimeEntryTagEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(relations: List<TimeEntryTagEntity>)

    // Delete
    @Delete
    suspend fun delete(relation: TimeEntryTagEntity)

    @Query("DELETE FROM time_entry_tags WHERE entry_id = :entryId")
    suspend fun deleteByEntryId(entryId: Long)

    @Query("DELETE FROM time_entry_tags WHERE tag_id = :tagId")
    suspend fun deleteByTagId(tagId: Long)

    @Query("DELETE FROM time_entry_tags WHERE entry_id = :entryId AND tag_id = :tagId")
    suspend fun deleteRelation(entryId: Long, tagId: Long)

    // Query
    @Query("SELECT tag_id FROM time_entry_tags WHERE entry_id = :entryId")
    suspend fun getTagIdsByEntryId(entryId: Long): List<Long>

    @Query("SELECT entry_id FROM time_entry_tags WHERE tag_id = :tagId")
    suspend fun getEntryIdsByTagId(tagId: Long): List<Long>

    @Query("SELECT * FROM time_entry_tags WHERE entry_id = :entryId")
    fun getByEntryIdFlow(entryId: Long): Flow<List<TimeEntryTagEntity>>

    // Replace all tags for an entry
    @Transaction
    suspend fun replaceTagsForEntry(entryId: Long, tagIds: List<Long>) {
        deleteByEntryId(entryId)
        val relations = tagIds.map { TimeEntryTagEntity(entryId, it) }
        insertAll(relations)
    }
}
```

**验收标准**：
- [ ] 插入/删除操作完整
- [ ] 批量替换功能
- [ ] 双向查询支持

---

#### T3.2.5 实现GoalDao

| 属性 | 值 |
|------|-----|
| **任务ID** | T3.2.5 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | T3.1.7 |

**任务描述**：
实现目标数据访问对象，包括CRUD操作和进度查询。

**执行步骤**：
1. 定义基础 CRUD 方法
2. 定义活动目标查询
3. 实现关联查询（带标签）

**关键代码**：
```kotlin
// GoalDao.kt
package com.blockwise.core.data.dao

import androidx.room.*
import com.blockwise.core.data.entity.GoalEntity
import com.blockwise.core.data.entity.GoalWithTag
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    // Insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: GoalEntity): Long

    // Update
    @Update
    suspend fun update(goal: GoalEntity)

    // Delete
    @Delete
    suspend fun delete(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteById(id: Long)

    // Activate/Deactivate
    @Query("UPDATE goals SET is_active = :isActive, updated_at = :updatedAt WHERE id = :id")
    suspend fun setActive(id: Long, isActive: Boolean, updatedAt: Long)

    // Query by ID
    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getById(id: Long): GoalEntity?

    @Transaction
    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getByIdWithTag(id: Long): GoalWithTag?

    @Transaction
    @Query("SELECT * FROM goals WHERE id = :id")
    fun getByIdWithTagFlow(id: Long): Flow<GoalWithTag?>

    // Query active goals
    @Transaction
    @Query("SELECT * FROM goals WHERE is_active = 1 ORDER BY id DESC")
    fun getActiveGoals(): Flow<List<GoalWithTag>>

    // Query all goals
    @Transaction
    @Query("SELECT * FROM goals ORDER BY is_active DESC, id DESC")
    fun getAllGoals(): Flow<List<GoalWithTag>>

    // Query goals by tag
    @Transaction
    @Query("SELECT * FROM goals WHERE tag_id = :tagId")
    fun getByTagId(tagId: Long): Flow<List<GoalWithTag>>

    // Query active goals by tag
    @Query("SELECT * FROM goals WHERE tag_id = :tagId AND is_active = 1 LIMIT 1")
    suspend fun getActiveByTagId(tagId: Long): GoalEntity?

    // Count active goals
    @Query("SELECT COUNT(*) FROM goals WHERE is_active = 1")
    suspend fun countActive(): Int
}
```

**验收标准**：
- [ ] CRUD 操作完整
- [ ] 活动/非活动状态切换
- [ ] 关联查询正确
- [ ] 按标签查询支持

---

#### T3.2.6 实现统计查询DAO

| 属性 | 值 |
|------|-----|
| **任务ID** | T3.2.6 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T3.1.7 |

**任务描述**：
实现统计查询 DAO，支持按日/周/月/年聚合查询。

**执行步骤**：
1. 定义统计结果数据类
2. 实现按活动类型统计
3. 实现按标签统计
4. 实现按时段统计

**关键代码**：
```kotlin
// StatisticsDao.kt
package com.blockwise.core.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// Statistics result classes
data class ActivityStatistics(
    @ColumnInfo(name = "activity_id")
    val activityId: Long,
    @ColumnInfo(name = "activity_name")
    val activityName: String,
    @ColumnInfo(name = "color_hex")
    val colorHex: String,
    @ColumnInfo(name = "total_minutes")
    val totalMinutes: Int,
    @ColumnInfo(name = "entry_count")
    val entryCount: Int
)

data class TagStatistics(
    @ColumnInfo(name = "tag_id")
    val tagId: Long,
    @ColumnInfo(name = "tag_name")
    val tagName: String,
    @ColumnInfo(name = "color_hex")
    val colorHex: String,
    @ColumnInfo(name = "total_minutes")
    val totalMinutes: Int,
    @ColumnInfo(name = "entry_count")
    val entryCount: Int
)

data class DailyStatistics(
    @ColumnInfo(name = "date_millis")
    val dateMillis: Long,
    @ColumnInfo(name = "total_minutes")
    val totalMinutes: Int,
    @ColumnInfo(name = "entry_count")
    val entryCount: Int
)

data class HourlyDistribution(
    @ColumnInfo(name = "hour")
    val hour: Int,
    @ColumnInfo(name = "total_minutes")
    val totalMinutes: Int
)

@Dao
interface StatisticsDao {

    // Statistics by activity type
    @Query("""
        SELECT
            te.activity_id,
            at.name AS activity_name,
            at.color_hex,
            COALESCE(SUM(te.duration_minutes), 0) AS total_minutes,
            COUNT(te.id) AS entry_count
        FROM time_entries te
        JOIN activity_types at ON te.activity_id = at.id
        WHERE te.start_time >= :startTime AND te.start_time < :endTime
        GROUP BY te.activity_id
        ORDER BY total_minutes DESC
    """)
    fun getStatsByActivityType(startTime: Long, endTime: Long): Flow<List<ActivityStatistics>>

    // Statistics by tag
    @Query("""
        SELECT
            t.id AS tag_id,
            t.name AS tag_name,
            t.color_hex,
            COALESCE(SUM(te.duration_minutes), 0) AS total_minutes,
            COUNT(DISTINCT te.id) AS entry_count
        FROM time_entries te
        JOIN time_entry_tags tet ON te.id = tet.entry_id
        JOIN tags t ON tet.tag_id = t.id
        WHERE te.start_time >= :startTime AND te.start_time < :endTime
        GROUP BY t.id
        ORDER BY total_minutes DESC
    """)
    fun getStatsByTag(startTime: Long, endTime: Long): Flow<List<TagStatistics>>

    // Daily statistics (for trends)
    @Query("""
        SELECT
            (te.start_time / 86400000) * 86400000 AS date_millis,
            COALESCE(SUM(te.duration_minutes), 0) AS total_minutes,
            COUNT(te.id) AS entry_count
        FROM time_entries te
        WHERE te.start_time >= :startTime AND te.start_time < :endTime
        GROUP BY date_millis
        ORDER BY date_millis ASC
    """)
    fun getDailyStats(startTime: Long, endTime: Long): Flow<List<DailyStatistics>>

    // Hourly distribution (for pattern analysis)
    @Query("""
        SELECT
            CAST((te.start_time / 3600000) % 24 AS INTEGER) AS hour,
            COALESCE(SUM(te.duration_minutes), 0) AS total_minutes
        FROM time_entries te
        WHERE te.start_time >= :startTime AND te.start_time < :endTime
        GROUP BY hour
        ORDER BY hour ASC
    """)
    fun getHourlyDistribution(startTime: Long, endTime: Long): Flow<List<HourlyDistribution>>

    // Total statistics for a time range
    @Query("""
        SELECT
            COALESCE(SUM(duration_minutes), 0) AS total_minutes,
            COUNT(*) AS entry_count
        FROM time_entries
        WHERE start_time >= :startTime AND start_time < :endTime
    """)
    suspend fun getTotalStats(startTime: Long, endTime: Long): DailyStatistics

    // Statistics for a specific tag
    @Query("""
        SELECT
            COALESCE(SUM(te.duration_minutes), 0)
        FROM time_entries te
        JOIN time_entry_tags tet ON te.id = tet.entry_id
        WHERE tet.tag_id = :tagId
          AND te.start_time >= :startTime
          AND te.start_time < :endTime
    """)
    suspend fun getTotalMinutesForTag(tagId: Long, startTime: Long, endTime: Long): Int

    // Daily statistics for a specific tag (for goal progress)
    @Query("""
        SELECT
            (te.start_time / 86400000) * 86400000 AS date_millis,
            COALESCE(SUM(te.duration_minutes), 0) AS total_minutes,
            COUNT(DISTINCT te.id) AS entry_count
        FROM time_entries te
        JOIN time_entry_tags tet ON te.id = tet.entry_id
        WHERE tet.tag_id = :tagId
          AND te.start_time >= :startTime
          AND te.start_time < :endTime
        GROUP BY date_millis
        ORDER BY date_millis ASC
    """)
    fun getDailyStatsForTag(tagId: Long, startTime: Long, endTime: Long): Flow<List<DailyStatistics>>
}
```

**验收标准**：
- [ ] 按活动类型统计正确
- [ ] 按标签统计正确
- [ ] 按日聚合正确
- [ ] 时段分布统计正确
- [ ] 单标签统计支持

---

### 2.3 领域模型与Mapper

#### T3.3.1 定义ActivityType Domain Model

| 属性 | 值 |
|------|-----|
| **任务ID** | T3.3.1 |
| **优先级** | P0 |
| **预计耗时** | 0.5h |
| **依赖任务** | T1.2.4 |

**任务描述**：
定义活动类型领域模型，纯 Kotlin 数据类。

**关键代码**：
```kotlin
// ActivityType.kt
package com.blockwise.core.domain.model

import androidx.compose.ui.graphics.Color

data class ActivityType(
    val id: Long,
    val name: String,
    val color: Color,
    val icon: String?,
    val parentId: Long?,
    val displayOrder: Int,
    val isArchived: Boolean
) {
    val isRootLevel: Boolean get() = parentId == null
}
```

**验收标准**：
- [ ] 纯 Kotlin 数据类
- [ ] 无 Android 依赖（除 Compose Color）
- [ ] 包含业务便捷属性

---

#### T3.3.2 定义Tag Domain Model

| 属性 | 值 |
|------|-----|
| **任务ID** | T3.3.2 |
| **优先级** | P0 |
| **预计耗时** | 0.5h |
| **依赖任务** | T1.2.4 |

**任务描述**：
定义标签领域模型。

**关键代码**：
```kotlin
// Tag.kt
package com.blockwise.core.domain.model

import androidx.compose.ui.graphics.Color

data class Tag(
    val id: Long,
    val name: String,
    val color: Color,
    val isArchived: Boolean
)
```

**验收标准**：
- [ ] 纯 Kotlin 数据类
- [ ] 字段定义完整

---

#### T3.3.3 定义TimeEntry Domain Model

| 属性 | 值 |
|------|-----|
| **任务ID** | T3.3.3 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | T1.2.4 |

**任务描述**：
定义时间记录领域模型，包含关联的活动类型和标签。

**关键代码**：
```kotlin
// TimeEntry.kt
package com.blockwise.core.domain.model

import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

data class TimeEntry(
    val id: Long,
    val activity: ActivityType,
    val startTime: Instant,
    val endTime: Instant,
    val durationMinutes: Int,
    val note: String?,
    val tags: List<Tag>
) {
    val duration: Duration get() = durationMinutes.minutes

    val formattedDuration: String get() {
        val hours = durationMinutes / 60
        val minutes = durationMinutes % 60
        return when {
            hours > 0 && minutes > 0 -> "${hours}小时${minutes}分钟"
            hours > 0 -> "${hours}小时"
            else -> "${minutes}分钟"
        }
    }
}

// For creating/editing
data class TimeEntryInput(
    val activityId: Long,
    val startTime: Instant,
    val endTime: Instant,
    val note: String?,
    val tagIds: List<Long>
) {
    val durationMinutes: Int get() =
        ((endTime.toEpochMilliseconds() - startTime.toEpochMilliseconds()) / 60000).toInt()
}
```

**验收标准**：
- [ ] 包含关联的活动类型
- [ ] 包含关联的标签列表
- [ ] 提供格式化时长属性
- [ ] 定义输入数据类

---

#### T3.3.4 定义Goal Domain Model

| 属性 | 值 |
|------|-----|
| **任务ID** | T3.3.4 |
| **优先级** | P0 |
| **预计耗时** | 0.5h |
| **依赖任务** | T1.2.4 |

**任务描述**：
定义目标领域模型，包含关联的标签。

**关键代码**：
```kotlin
// Goal.kt
package com.blockwise.core.domain.model

import kotlinx.datetime.LocalDate

enum class GoalType {
    MIN,    // 最少时长
    MAX,    // 最多时长
    EXACT   // 精确时长
}

enum class GoalPeriod {
    DAILY,
    WEEKLY,
    MONTHLY,
    CUSTOM
}

data class Goal(
    val id: Long,
    val tag: Tag,
    val targetMinutes: Int,
    val goalType: GoalType,
    val period: GoalPeriod,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val isActive: Boolean
) {
    val formattedTarget: String get() {
        val hours = targetMinutes / 60
        val minutes = targetMinutes % 60
        return when {
            hours > 0 && minutes > 0 -> "${hours}小时${minutes}分钟"
            hours > 0 -> "${hours}小时"
            else -> "${minutes}分钟"
        }
    }

    val periodLabel: String get() = when (period) {
        GoalPeriod.DAILY -> "每天"
        GoalPeriod.WEEKLY -> "每周"
        GoalPeriod.MONTHLY -> "每月"
        GoalPeriod.CUSTOM -> "自定义"
    }

    val goalTypeLabel: String get() = when (goalType) {
        GoalType.MIN -> "至少"
        GoalType.MAX -> "最多"
        GoalType.EXACT -> "精确"
    }
}

data class GoalProgress(
    val goal: Goal,
    val currentMinutes: Int,
    val targetMinutes: Int
) {
    val progress: Float get() = if (targetMinutes > 0) {
        (currentMinutes.toFloat() / targetMinutes).coerceIn(0f, 1f)
    } else 0f

    val progressPercentage: Int get() = (progress * 100).toInt()

    val isCompleted: Boolean get() = when (goal.goalType) {
        GoalType.MIN -> currentMinutes >= targetMinutes
        GoalType.MAX -> currentMinutes <= targetMinutes
        GoalType.EXACT -> currentMinutes == targetMinutes
    }

    val remainingMinutes: Int get() = (targetMinutes - currentMinutes).coerceAtLeast(0)
}
```

**验收标准**：
- [ ] 包含关联的标签
- [ ] 提供格式化属性
- [ ] 定义进度计算模型

---

#### T3.3.5 定义Statistics Domain Model

| 属性 | 值 |
|------|-----|
| **任务ID** | T3.3.5 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | T1.2.4 |

**任务描述**：
定义统计结果领域模型。

**关键代码**：
```kotlin
// Statistics.kt
package com.blockwise.core.domain.model

import androidx.compose.ui.graphics.Color
import kotlinx.datetime.LocalDate

data class StatisticsSummary(
    val totalMinutes: Int,
    val entryCount: Int,
    val previousPeriodMinutes: Int? = null // For comparison
) {
    val formattedTotal: String get() {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 && minutes > 0 -> "${hours}小时${minutes}分钟"
            hours > 0 -> "${hours}小时"
            else -> "${minutes}分钟"
        }
    }

    val changePercentage: Int? get() = previousPeriodMinutes?.let {
        if (it > 0) ((totalMinutes - it) * 100 / it) else null
    }

    val isIncrease: Boolean? get() = previousPeriodMinutes?.let { totalMinutes > it }
}

data class CategoryStatistics(
    val id: Long,
    val name: String,
    val color: Color,
    val totalMinutes: Int,
    val entryCount: Int,
    val percentage: Float // 0-100
) {
    val formattedDuration: String get() {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            else -> "${minutes}m"
        }
    }
}

data class DailyTrend(
    val date: LocalDate,
    val totalMinutes: Int,
    val entryCount: Int
)

data class HourlyPattern(
    val hour: Int,
    val totalMinutes: Int
) {
    val formattedHour: String get() = "${hour}:00"
}

data class PeriodStatistics(
    val summary: StatisticsSummary,
    val byActivity: List<CategoryStatistics>,
    val byTag: List<CategoryStatistics>,
    val dailyTrends: List<DailyTrend>,
    val hourlyPattern: List<HourlyPattern>
)
```

**验收标准**：
- [ ] 摘要统计模型完整
- [ ] 分类统计模型完整
- [ ] 趋势数据模型完整
- [ ] 提供格式化属性

---

#### T3.3.6 实现Entity到Domain Mapper

| 属性 | 值 |
|------|-----|
| **任务ID** | T3.3.6 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T3.3.1, T3.3.2, T3.3.3, T3.3.4, T3.3.5 |

**任务描述**：
实现数据层 Entity 到领域层 Model 的映射。

**执行步骤**：
1. 实现 ActivityType Mapper
2. 实现 Tag Mapper
3. 实现 TimeEntry Mapper
4. 实现 Goal Mapper
5. 实现 Statistics Mapper

**关键代码**：
```kotlin
// Mappers.kt
package com.blockwise.core.data.mapper

import androidx.compose.ui.graphics.Color
import com.blockwise.core.data.dao.*
import com.blockwise.core.data.entity.*
import com.blockwise.core.domain.model.*
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

// Color conversion
fun String.toColor(): Color = Color(android.graphics.Color.parseColor(this))
fun Color.toHexString(): String {
    val red = (this.red * 255).toInt()
    val green = (this.green * 255).toInt()
    val blue = (this.blue * 255).toInt()
    return String.format("#%02X%02X%02X", red, green, blue)
}

// ActivityType Mappers
fun ActivityTypeEntity.toDomain(): ActivityType = ActivityType(
    id = id,
    name = name,
    color = colorHex.toColor(),
    icon = icon,
    parentId = parentId,
    displayOrder = displayOrder,
    isArchived = isArchived
)

fun ActivityType.toEntity(
    createdAt: Instant = kotlinx.datetime.Clock.System.now(),
    updatedAt: Instant = kotlinx.datetime.Clock.System.now()
): ActivityTypeEntity = ActivityTypeEntity(
    id = id,
    name = name,
    colorHex = color.toHexString(),
    icon = icon,
    parentId = parentId,
    displayOrder = displayOrder,
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt
)

// Tag Mappers
fun TagEntity.toDomain(): Tag = Tag(
    id = id,
    name = name,
    color = colorHex.toColor(),
    isArchived = isArchived
)

fun Tag.toEntity(
    createdAt: Instant = kotlinx.datetime.Clock.System.now(),
    updatedAt: Instant = kotlinx.datetime.Clock.System.now()
): TagEntity = TagEntity(
    id = id,
    name = name,
    colorHex = color.toHexString(),
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt
)

// TimeEntry Mappers
fun TimeEntryWithDetails.toDomain(): TimeEntry = TimeEntry(
    id = entry.id,
    activity = activity.toDomain(),
    startTime = entry.startTime,
    endTime = entry.endTime,
    durationMinutes = entry.durationMinutes,
    note = entry.note,
    tags = tags.map { it.toDomain() }
)

fun TimeEntryInput.toEntity(
    id: Long = 0,
    createdAt: Instant = kotlinx.datetime.Clock.System.now(),
    updatedAt: Instant = kotlinx.datetime.Clock.System.now()
): TimeEntryEntity = TimeEntryEntity(
    id = id,
    activityId = activityId,
    startTime = startTime,
    endTime = endTime,
    durationMinutes = durationMinutes,
    note = note,
    createdAt = createdAt,
    updatedAt = updatedAt
)

// Goal Mappers
fun GoalWithTag.toDomain(): Goal = Goal(
    id = goal.id,
    tag = tag.toDomain(),
    targetMinutes = goal.targetMinutes,
    goalType = goal.goalType.toDomain(),
    period = goal.period.toDomain(),
    startDate = goal.startDate,
    endDate = goal.endDate,
    isActive = goal.isActive
)

fun com.blockwise.core.data.database.converter.GoalType.toDomain(): GoalType = when (this) {
    com.blockwise.core.data.database.converter.GoalType.MIN -> GoalType.MIN
    com.blockwise.core.data.database.converter.GoalType.MAX -> GoalType.MAX
    com.blockwise.core.data.database.converter.GoalType.EXACT -> GoalType.EXACT
}

fun GoalType.toEntity(): com.blockwise.core.data.database.converter.GoalType = when (this) {
    GoalType.MIN -> com.blockwise.core.data.database.converter.GoalType.MIN
    GoalType.MAX -> com.blockwise.core.data.database.converter.GoalType.MAX
    GoalType.EXACT -> com.blockwise.core.data.database.converter.GoalType.EXACT
}

// Similar for GoalPeriod...

// Statistics Mappers
fun ActivityStatistics.toDomain(totalMinutes: Int): CategoryStatistics = CategoryStatistics(
    id = activityId,
    name = activityName,
    color = colorHex.toColor(),
    totalMinutes = this.totalMinutes,
    entryCount = entryCount,
    percentage = if (totalMinutes > 0) (this.totalMinutes * 100f / totalMinutes) else 0f
)

fun TagStatistics.toDomain(totalMinutes: Int): CategoryStatistics = CategoryStatistics(
    id = tagId,
    name = tagName,
    color = colorHex.toColor(),
    totalMinutes = this.totalMinutes,
    entryCount = entryCount,
    percentage = if (totalMinutes > 0) (this.totalMinutes * 100f / totalMinutes) else 0f
)

fun DailyStatistics.toDomain(): DailyTrend = DailyTrend(
    date = LocalDate.fromEpochDays((dateMillis / 86400000).toInt()),
    totalMinutes = totalMinutes,
    entryCount = entryCount
)

fun HourlyDistribution.toDomain(): HourlyPattern = HourlyPattern(
    hour = hour,
    totalMinutes = totalMinutes
)
```

**验收标准**：
- [ ] 所有 Entity 有对应 Mapper
- [ ] 颜色转换正确
- [ ] Enum 转换正确
- [ ] 关联对象正确映射

---

### 2.4 Repository实现

#### T3.4.1 - T3.4.5 定义Repository接口

| 任务ID范围 | 任务名称 | 优先级 |
|-----------|---------|--------|
| T3.4.1 | 定义ActivityTypeRepository接口 | P0 |
| T3.4.2 | 定义TagRepository接口 | P0 |
| T3.4.3 | 定义TimeEntryRepository接口 | P0 |
| T3.4.4 | 定义GoalRepository接口 | P0 |
| T3.4.5 | 定义StatisticsRepository接口 | P0 |

**关键代码示例**：
```kotlin
// TimeEntryRepository.kt (Interface)
package com.blockwise.core.domain.repository

import com.blockwise.core.domain.model.TimeEntry
import com.blockwise.core.domain.model.TimeEntryInput
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

interface TimeEntryRepository {

    // CRUD
    suspend fun create(input: TimeEntryInput): Long
    suspend fun update(id: Long, input: TimeEntryInput)
    suspend fun delete(id: Long)
    suspend fun getById(id: Long): TimeEntry?

    // Query
    fun getByIdFlow(id: Long): Flow<TimeEntry?>
    fun getByTimeRange(startTime: Instant, endTime: Instant): Flow<List<TimeEntry>>
    fun getByDay(date: LocalDate): Flow<List<TimeEntry>>
    fun getRecent(limit: Int, offset: Int): Flow<List<TimeEntry>>

    // Validation
    suspend fun hasOverlapping(startTime: Instant, endTime: Instant, excludeId: Long = 0): Boolean

    // Statistics helpers
    suspend fun getTotalDuration(startTime: Instant, endTime: Instant): Int
    suspend fun getEntryCount(startTime: Instant, endTime: Instant): Int
}
```

**验收标准**：
- [ ] 接口定义在 domain 层
- [ ] 无 Android 依赖
- [ ] 使用 Flow 作为响应式返回类型

---

#### T3.4.6 - T3.4.10 实现Repository

| 任务ID范围 | 任务名称 | 优先级 |
|-----------|---------|--------|
| T3.4.6 | 实现ActivityTypeRepositoryImpl | P0 |
| T3.4.7 | 实现TagRepositoryImpl | P0 |
| T3.4.8 | 实现TimeEntryRepositoryImpl | P0 |
| T3.4.9 | 实现GoalRepositoryImpl | P0 |
| T3.4.10 | 实现StatisticsRepositoryImpl | P0 |

**关键代码示例**：
```kotlin
// TimeEntryRepositoryImpl.kt
package com.blockwise.feature.timeentry.data.repository

import com.blockwise.core.data.dao.TimeEntryDao
import com.blockwise.core.data.dao.TimeEntryTagDao
import com.blockwise.core.data.mapper.toDomain
import com.blockwise.core.data.mapper.toEntity
import com.blockwise.core.domain.model.TimeEntry
import com.blockwise.core.domain.model.TimeEntryInput
import com.blockwise.core.domain.repository.TimeEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import javax.inject.Inject
import kotlin.time.Duration.Companion.days

class TimeEntryRepositoryImpl @Inject constructor(
    private val timeEntryDao: TimeEntryDao,
    private val timeEntryTagDao: TimeEntryTagDao
) : TimeEntryRepository {

    override suspend fun create(input: TimeEntryInput): Long {
        val entity = input.toEntity()
        val entryId = timeEntryDao.insert(entity)
        if (input.tagIds.isNotEmpty()) {
            timeEntryTagDao.replaceTagsForEntry(entryId, input.tagIds)
        }
        return entryId
    }

    override suspend fun update(id: Long, input: TimeEntryInput) {
        val existing = timeEntryDao.getById(id) ?: return
        val entity = input.toEntity(
            id = id,
            createdAt = existing.createdAt
        )
        timeEntryDao.update(entity)
        timeEntryTagDao.replaceTagsForEntry(id, input.tagIds)
    }

    override suspend fun delete(id: Long) {
        timeEntryDao.deleteById(id)
    }

    override suspend fun getById(id: Long): TimeEntry? {
        return timeEntryDao.getByIdWithDetails(id)?.toDomain()
    }

    override fun getByIdFlow(id: Long): Flow<TimeEntry?> {
        return timeEntryDao.getByIdWithDetailsFlow(id).map { it?.toDomain() }
    }

    override fun getByTimeRange(startTime: Instant, endTime: Instant): Flow<List<TimeEntry>> {
        return timeEntryDao.getByTimeRangeWithDetails(
            startTime.toEpochMilliseconds(),
            endTime.toEpochMilliseconds()
        ).map { list -> list.map { it.toDomain() } }
    }

    override fun getByDay(date: LocalDate): Flow<List<TimeEntry>> {
        val tz = TimeZone.currentSystemDefault()
        val dayStart = date.atStartOfDayIn(tz)
        val dayEnd = dayStart + 1.days
        return timeEntryDao.getByDay(
            dayStart.toEpochMilliseconds(),
            dayEnd.toEpochMilliseconds()
        ).map { list -> list.map { it.toDomain() } }
    }

    override fun getRecent(limit: Int, offset: Int): Flow<List<TimeEntry>> {
        return timeEntryDao.getRecentWithDetails(limit, offset)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun hasOverlapping(startTime: Instant, endTime: Instant, excludeId: Long): Boolean {
        return timeEntryDao.findOverlapping(
            startTime.toEpochMilliseconds(),
            endTime.toEpochMilliseconds(),
            excludeId
        ) != null
    }

    override suspend fun getTotalDuration(startTime: Instant, endTime: Instant): Int {
        return timeEntryDao.getTotalDurationByTimeRange(
            startTime.toEpochMilliseconds(),
            endTime.toEpochMilliseconds()
        )
    }

    override suspend fun getEntryCount(startTime: Instant, endTime: Instant): Int {
        return timeEntryDao.countByTimeRange(
            startTime.toEpochMilliseconds(),
            endTime.toEpochMilliseconds()
        )
    }
}

// Hilt Module
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindTimeEntryRepository(
        impl: TimeEntryRepositoryImpl
    ): TimeEntryRepository

    // ... other bindings
}
```

**验收标准**：
- [ ] 实现所有接口方法
- [ ] 正确使用 Mapper 转换数据
- [ ] Hilt @Binds 绑定配置
- [ ] 事务操作使用 @Transaction

---

## 3. 依赖关系图

```
T1.3.2 集成Room ──┬── T3.1.1 TypeConverter
T1.3.5 DateTime ──┘         │
                            ▼
                    ┌───────────────────────────────────────┐
                    │      Entity 定义 (T3.1.2 - T3.1.6)    │
                    │  ActivityType, Tag, TimeEntry,        │
                    │  TimeEntryTag, Goal                   │
                    └───────────────────┬───────────────────┘
                                        │
                                        ▼
                            T3.1.7 创建AppDatabase
                                        │
                            T3.1.8 创建数据库索引
                                        │
                                        ▼
                    ┌───────────────────────────────────────┐
                    │       DAO 实现 (T3.2.1 - T3.2.6)      │
                    │  ActivityTypeDao, TagDao,             │
                    │  TimeEntryDao, GoalDao, StatisticsDao │
                    └───────────────────┬───────────────────┘
                                        │
T1.2.4 core:domain ─────────────────────┤
                                        │
                    ┌───────────────────┴───────────────────┐
                    │                                       │
                    ▼                                       ▼
        ┌─────────────────────┐             ┌─────────────────────────┐
        │ Domain Model 定义    │             │ Repository 接口定义      │
        │ (T3.3.1 - T3.3.5)   │             │ (T3.4.1 - T3.4.5)       │
        └─────────┬───────────┘             └────────────┬────────────┘
                  │                                      │
                  └──────────────┬───────────────────────┘
                                 │
                                 ▼
                    T3.3.6 实现 Entity → Domain Mapper
                                 │
                                 ▼
                    ┌───────────────────────────────────────┐
                    │   Repository 实现 (T3.4.6 - T3.4.10)  │
                    │   依赖 DAO + Mapper + 实现接口         │
                    └───────────────────────────────────────┘
```

---

## 4. 验收标准清单

### 4.1 数据库验收

| 验收项 | 要求 | 检查方式 |
|--------|------|----------|
| 数据库创建 | 应用首次运行创建数据库 | 设备文件检查 |
| Schema 导出 | `schemas/` 目录有版本文件 | 文件检查 |
| 表结构正确 | 所有表字段符合设计 | Schema 文件检查 |
| 索引创建 | 索引在 Schema 中可见 | Schema 文件检查 |
| 外键约束 | 删除测试验证约束生效 | 功能测试 |

### 4.2 DAO 验收

| DAO | 验收标准 |
|-----|----------|
| ActivityTypeDao | CRUD + 层级查询 + 软删除 |
| TagDao | CRUD + 唯一性检查 + 软删除 |
| TimeEntryDao | CRUD + 时间范围 + 关联查询 + 分页 |
| TimeEntryTagDao | 关联操作 + 批量替换 |
| GoalDao | CRUD + 活动状态 + 关联查询 |
| StatisticsDao | 多维度聚合查询正确 |

### 4.3 领域模型验收

| 验收项 | 要求 |
|--------|------|
| 模型位置 | 在 `core/domain/model` 包 |
| 无 Android 依赖 | 除 Compose Color 外无 Android 导入 |
| 业务属性 | 提供格式化等便捷属性 |
| Mapper 完整 | Entity ↔ Domain 双向映射 |

### 4.4 Repository 验收

| 验收项 | 要求 |
|--------|------|
| 接口位置 | 在 `core/domain/repository` 包 |
| 实现位置 | 在 feature 模块的 data 包 |
| Hilt 绑定 | @Binds 注解正确配置 |
| Flow 支持 | 响应式查询返回 Flow |
| 事务操作 | 多表操作使用 @Transaction |

---

## 5. 数据库目录结构

```
core/data/
├── build.gradle.kts
└── src/main/kotlin/com/blockwise/core/data/
    ├── database/
    │   ├── AppDatabase.kt
    │   └── converter/
    │       ├── Converters.kt
    │       └── EnumConverters.kt
    ├── entity/
    │   ├── ActivityTypeEntity.kt
    │   ├── TagEntity.kt
    │   ├── TimeEntryEntity.kt
    │   ├── TimeEntryTagEntity.kt
    │   └── GoalEntity.kt
    ├── dao/
    │   ├── ActivityTypeDao.kt
    │   ├── TagDao.kt
    │   ├── TimeEntryDao.kt
    │   ├── TimeEntryTagDao.kt
    │   ├── GoalDao.kt
    │   └── StatisticsDao.kt
    ├── mapper/
    │   └── Mappers.kt
    └── di/
        └── DatabaseModule.kt

core/domain/
├── build.gradle.kts
└── src/main/kotlin/com/blockwise/core/domain/
    ├── model/
    │   ├── ActivityType.kt
    │   ├── Tag.kt
    │   ├── TimeEntry.kt
    │   ├── Goal.kt
    │   └── Statistics.kt
    └── repository/
        ├── ActivityTypeRepository.kt
        ├── TagRepository.kt
        ├── TimeEntryRepository.kt
        ├── GoalRepository.kt
        └── StatisticsRepository.kt
```

---

## 6. 风险与注意事项

### 6.1 潜在风险

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 数据库迁移 | 版本升级数据丢失 | 编写 Migration，测试覆盖 |
| 外键约束 | 删除操作失败 | 明确删除策略，级联配置 |
| 时区问题 | 时间计算错误 | 统一使用 UTC 存储 |
| 查询性能 | 大数据量卡顿 | 索引优化，分页加载 |

### 6.2 注意事项

1. **Instant 存储**：始终存储 UTC 时间戳（毫秒）
2. **LocalDate 存储**：使用 ISO-8601 字符串格式
3. **软删除**：使用 `is_archived` 而非物理删除
4. **关联查询**：必须添加 `@Transaction` 注解
5. **Flow 收集**：在 ViewModel 中使用 `stateIn` 转换

---

*文档版本: v1.0*
*阶段状态: 已完成*
