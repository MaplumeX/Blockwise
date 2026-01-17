package com.maplume.blockwise.core.data.testing

import android.content.Context
import androidx.room.Room
import com.maplume.blockwise.core.data.database.AppDatabase

/**
 * Test module for creating in-memory databases.
 * Provides utility functions for database testing.
 */
object TestDatabaseModule {

    /**
     * Create an in-memory database for testing.
     * The database is destroyed when the process is killed.
     *
     * @param context Application context
     * @return In-memory AppDatabase instance
     */
    fun createInMemoryDatabase(context: Context): AppDatabase {
        return Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .allowMainThreadQueries() // Allow queries on main thread for testing
            .build()
    }

    /**
     * Create an in-memory database with pre-populated test data.
     *
     * @param context Application context
     * @param initializer Callback to populate initial data
     * @return In-memory AppDatabase instance with initial data
     */
    suspend fun createInMemoryDatabaseWithData(
        context: Context,
        initializer: suspend (AppDatabase) -> Unit
    ): AppDatabase {
        val database = createInMemoryDatabase(context)
        initializer(database)
        return database
    }
}
