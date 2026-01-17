package com.maplume.blockwise.core.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * JUnit5 extension for replacing Main dispatcher in tests.
 * This is necessary for testing coroutines that use Dispatchers.Main.
 *
 * Usage:
 * ```kotlin
 * @ExtendWith(TestDispatcherExtension::class)
 * class MyViewModelTest {
 *     // Tests can now use Dispatchers.Main
 * }
 * ```
 *
 * Or with custom dispatcher:
 * ```kotlin
 * @RegisterExtension
 * val testDispatcherExtension = TestDispatcherExtension(UnconfinedTestDispatcher())
 * ```
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TestDispatcherExtension(
    val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : BeforeEachCallback, AfterEachCallback {

    override fun beforeEach(context: ExtensionContext?) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun afterEach(context: ExtensionContext?) {
        Dispatchers.resetMain()
    }
}
