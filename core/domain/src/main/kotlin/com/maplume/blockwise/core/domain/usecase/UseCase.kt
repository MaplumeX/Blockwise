package com.maplume.blockwise.core.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Base class for Use Cases that return a result.
 * Executes on the IO dispatcher by default.
 *
 * @param T Input parameter type
 * @param R Result type
 */
abstract class UseCase<in T, R>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    /**
     * Execute the use case.
     */
    suspend operator fun invoke(params: T): Result<R> = withContext(dispatcher) {
        try {
            Result.success(execute(params))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Override this method to implement the use case logic.
     */
    protected abstract suspend fun execute(params: T): R
}

/**
 * Base class for Use Cases without input parameters.
 *
 * @param R Result type
 */
abstract class NoParamUseCase<R>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    /**
     * Execute the use case.
     */
    suspend operator fun invoke(): Result<R> = withContext(dispatcher) {
        try {
            Result.success(execute())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Override this method to implement the use case logic.
     */
    protected abstract suspend fun execute(): R
}
