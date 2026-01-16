package com.maplume.blockwise.feature.timeentry.domain.usecase.tag

import com.maplume.blockwise.core.domain.model.Tag
import com.maplume.blockwise.core.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving tags.
 * Supports filtering by archived status.
 */
class GetTagsUseCase @Inject constructor(
    private val repository: TagRepository
) {
    /**
     * Get all tags.
     * @param includeArchived Whether to include archived tags. Defaults to false.
     * @return Flow of tag list.
     */
    operator fun invoke(includeArchived: Boolean = false): Flow<List<Tag>> {
        return if (includeArchived) {
            repository.getAll()
        } else {
            repository.getAllActive()
        }
    }

    /**
     * Get tags by IDs.
     * @param ids List of tag IDs.
     * @return List of tags.
     */
    suspend fun getByIds(ids: List<Long>): List<Tag> {
        return repository.getByIds(ids)
    }
}
