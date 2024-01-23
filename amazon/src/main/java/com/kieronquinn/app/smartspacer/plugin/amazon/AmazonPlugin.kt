package com.kieronquinn.app.smartspacer.plugin.amazon

import android.content.Context
import android.webkit.CookieManager
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.google.gson.Gson
import com.kieronquinn.app.smartspacer.plugin.amazon.model.database.AmazonDatabase
import com.kieronquinn.app.smartspacer.plugin.amazon.repositories.AmazonRepository
import com.kieronquinn.app.smartspacer.plugin.amazon.repositories.AmazonRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.amazon.repositories.AmazonSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.amazon.repositories.AmazonSettingsRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.amazon.repositories.DatabaseRepository
import com.kieronquinn.app.smartspacer.plugin.amazon.repositories.DatabaseRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.amazon.repositories.NavGraphRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.amazon.repositories.NotificationRepository
import com.kieronquinn.app.smartspacer.plugin.amazon.repositories.NotificationRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.amazon.ui.screens.domain.DomainPickerViewModel
import com.kieronquinn.app.smartspacer.plugin.amazon.ui.screens.domain.DomainPickerViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.amazon.ui.screens.packages.PackagesViewModel
import com.kieronquinn.app.smartspacer.plugin.amazon.ui.screens.packages.PackagesViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.amazon.ui.screens.packages.options.PackageOptionsBottomSheetViewModel
import com.kieronquinn.app.smartspacer.plugin.amazon.ui.screens.packages.options.PackageOptionsBottomSheetViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class AmazonPlugin: SmartspacerPlugin() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        base.deleteStaleFiles()
    }

    override fun getModule(context: Context) = module {
        single { createGson() }
        single { createGlide(context) }
        single { AmazonDatabase.getDatabase(context) }
        single { createCookieManager() }
        single<NotificationRepository> { NotificationRepositoryImpl(get()) }
        single<NavGraphRepository> { NavGraphRepositoryImpl() }
        single<AmazonRepository> { AmazonRepositoryImpl(get(), get(), get(), get(), get(), get(), get()) }
        single<DatabaseRepository> { DatabaseRepositoryImpl(get()) }
        single<AmazonSettingsRepository> { AmazonSettingsRepositoryImpl(get()) }
        viewModel<PackagesViewModel> { PackagesViewModelImpl(get(), get(), get(), get(), get()) }
        viewModel<DomainPickerViewModel> { DomainPickerViewModelImpl(get(), get(), get()) }
        viewModel<PackageOptionsBottomSheetViewModel> { PackageOptionsBottomSheetViewModelImpl(get(), get()) }
    }

    private fun createGson(): Gson {
        return Gson()
    }

    private fun createGlide(context: Context): RequestManager {
        return Glide.with(context)
    }

    private fun createCookieManager(): CookieManager {
        return CookieManager.getInstance().apply {
            setAcceptCookie(true)
        }
    }

    private fun Context.deleteStaleFiles() {
        val oldDatabase = getDatabasePath("amazon")
        oldDatabase.delete()
        val oldSettings = getSharedPreferences(
            "${BuildConfig.APPLICATION_ID}_prefs",
            Context.MODE_PRIVATE
        )
        oldSettings.edit().clear().commit()
    }

}