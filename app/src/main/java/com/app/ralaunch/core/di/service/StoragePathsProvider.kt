package com.app.ralaunch.core.di.service

import android.content.Context
import com.app.ralaunch.core.platform.AppConstants
import java.io.File

/**
 * 存储路径提供器。
 */
class StoragePathsProvider(
    private val context: Context
) {
    fun gamesDirPathFull(): String {
        return File(context.getExternalFilesDir(null), AppConstants.Dirs.GAMES).also {
            if (!it.exists()) it.mkdirs()
        }.absolutePath
    }

    fun settingsFilePathFull(): String {
        return File(context.filesDir, AppConstants.Files.SETTINGS).absolutePath
    }
}
