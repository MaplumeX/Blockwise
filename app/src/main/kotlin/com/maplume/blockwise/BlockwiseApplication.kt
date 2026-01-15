package com.maplume.blockwise

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Main application class for Blockwise.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 */
@HiltAndroidApp
class BlockwiseApplication : Application()
