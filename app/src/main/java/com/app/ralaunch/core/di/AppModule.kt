package com.app.ralaunch.core.di

import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import com.app.ralaunch.core.common.GameLaunchManager
import com.app.ralaunch.core.di.contract.IGameRepositoryServiceV3
import com.app.ralaunch.core.di.contract.ISettingsRepositoryServiceV2
import com.app.ralaunch.core.di.contract.IThemeManagerServiceV1
import com.app.ralaunch.core.di.service.GameRepositoryServiceV3
import com.app.ralaunch.core.di.service.PermissionManagerServiceV1
import com.app.ralaunch.core.di.service.SettingsRepositoryServiceV2
import com.app.ralaunch.core.di.service.StoragePathsProviderServiceV1
import com.app.ralaunch.core.di.service.ThemeManagerServiceV1
import com.app.ralaunch.core.di.service.VibrationManagerServiceV1
import com.app.ralaunch.feature.announcement.AnnouncementRepositoryService
import com.app.ralaunch.feature.controls.packs.ControlPackManager
import com.app.ralaunch.feature.installer.vm.InstallerViewModel
import com.app.ralaunch.feature.main.update.LauncherUpdateChecker
import com.app.ralaunch.feature.main.vm.MainViewModel
import com.app.ralaunch.feature.patch.data.PatchManager
import com.app.ralaunch.feature.script.JavaScriptExecutor
import com.app.ralaunch.feature.settings.vm.AppInfo
import com.app.ralaunch.feature.settings.vm.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * App 模块 Koin 依赖配置
 *
 * 包含共享、Android 平台与应用层依赖
 */
val appModule = module {

    // ==================== 数据存储 ====================

    single<StoragePathsProviderServiceV1> {
        StoragePathsProviderServiceV1(androidContext())
    }

    // ==================== Repositories ====================

    single<IGameRepositoryServiceV3> {
        GameRepositoryServiceV3(pathsProvider = get())
    }

    single<ISettingsRepositoryServiceV2> {
        SettingsRepositoryServiceV2(storagePathsProvider = get())
    }

    // ==================== App Info ====================

    single {
        try {
            val context = androidContext()
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionCode = packageInfo.longVersionCode
            AppInfo(
                versionName = packageInfo.versionName ?: versionCode.toString(),
                versionCode = versionCode
            )
        } catch (e: Exception) {
            AppInfo()
        }
    }

    // ==================== Managers ====================

    single {
        VibrationManagerServiceV1(androidContext())
    }

    single {
        ControlPackManager(androidContext())
    }

    single {
        AnnouncementRepositoryService(androidContext())
    }

    single {
        LauncherUpdateChecker(androidContext())
    }

    single {
        GameLaunchManager(androidContext())
    }

    single {
        JavaScriptExecutor()
    }

    single<PatchManager?> {
        try {
            PatchManager(null, false)
        } catch (e: Exception) {
            null
        }
    }

    // ==================== ViewModels ====================

    viewModel {
        SettingsViewModel(
            settingsRepository = get<ISettingsRepositoryServiceV2>(),
            appInfo = getOrNull<AppInfo>() ?: AppInfo()
        )
    }

    viewModel {
        MainViewModel(
            appContext = androidContext(),
            gameRepository = get(),
            gameLaunchManager = get(),
            settingsRepository = get(),
            announcementRepositoryService = get(),
            launcherUpdateChecker = get()
        )
    }

    viewModel {
        InstallerViewModel(
            appContext = androidContext(),
            gameRepository = get()
        )
    }

    // ==================== UI Managers (参数化工厂) ====================

    factory<ThemeManagerServiceV1> { (activity: AppCompatActivity) ->
        ThemeManagerServiceV1(activity)
    }

    factory<IThemeManagerServiceV1> { (activity: AppCompatActivity) ->
        ThemeManagerServiceV1(activity)
    }

    factory<PermissionManagerServiceV1> { (activity: ComponentActivity) ->
        PermissionManagerServiceV1(activity)
    }
}

/**
 * 获取所有 App 模块
 */
fun getAppModules() = listOf(appModule)
