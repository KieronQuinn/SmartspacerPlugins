package com.kieronquinn.app.smartspacer.plugin.googlewallet

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.googlewallet.model.database.WalletDatabase
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.DatabaseRepository
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.DatabaseRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.EncryptedSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.EncryptedSettingsRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.GoogleApiRepository
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.GoogleApiRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.GoogleWalletRepository
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.GoogleWalletRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.NavGraphRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.datasources.TrainlineRepository
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.datasources.TrainlineRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.googlewallet.ui.screens.configuration.dynamic.ConfigurationGoogleWalletDynamicViewModel
import com.kieronquinn.app.smartspacer.plugin.googlewallet.ui.screens.configuration.dynamic.ConfigurationGoogleWalletDynamicViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.googlewallet.ui.screens.configuration.signin.SignInWithGoogleViewModel
import com.kieronquinn.app.smartspacer.plugin.googlewallet.ui.screens.configuration.signin.SignInWithGoogleViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.googlewallet.ui.screens.configuration.valuable.ConfigurationGoogleWalletValuableViewModel
import com.kieronquinn.app.smartspacer.plugin.googlewallet.ui.screens.configuration.valuable.ConfigurationGoogleWalletValuableViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.googlewallet.ui.screens.configuration.valuable.picker.ConfigurationGoogleWalletValuablePickerViewModel
import com.kieronquinn.app.smartspacer.plugin.googlewallet.ui.screens.configuration.valuable.picker.ConfigurationGoogleWalletValuablePickerViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.googlewallet.ui.screens.popup.PopupWalletDialogViewModel
import com.kieronquinn.app.smartspacer.plugin.googlewallet.ui.screens.popup.PopupWalletDialogViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.RoomEncryptedSettingsRepository.RoomEncryptionFailedCallback
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DatabaseRepository as SharedDatabaseRepository

const val PACKAGE_GMS = "com.google.android.gms"
const val PACKAGE_WALLET = "com.google.android.apps.walletnfcrel"

class GoogleWalletPluginApplication: SmartspacerPlugin() {

    override fun getModule(context: Context) = module {
        single { WalletDatabase.getDatabase(get()) }
        single<NavGraphRepository> { NavGraphRepositoryImpl() }
        single<EncryptedSettingsRepository> { EncryptedSettingsRepositoryImpl(context) }
        single<SharedDatabaseRepository> { DatabaseRepositoryImpl(get()) }
        single<DatabaseRepository> { DatabaseRepositoryImpl(get()) }
        single { get<DatabaseRepository>() as RoomEncryptionFailedCallback }
        single<GoogleApiRepository>(createdAtStart = true) { GoogleApiRepositoryImpl(get(), get()) }
        single<GoogleWalletRepository> { GoogleWalletRepositoryImpl(get(), get(), get(), get()) }
        single<TrainlineRepository> { TrainlineRepositoryImpl(get()) }
        viewModel<SignInWithGoogleViewModel> { SignInWithGoogleViewModelImpl(get(), get(), get()) }
        viewModel<ConfigurationGoogleWalletValuableViewModel> { ConfigurationGoogleWalletValuableViewModelImpl(get(), get(), get()) }
        viewModel<ConfigurationGoogleWalletValuablePickerViewModel> { ConfigurationGoogleWalletValuablePickerViewModelImpl(get(), get(), get(), get()) }
        viewModel<ConfigurationGoogleWalletDynamicViewModel> { ConfigurationGoogleWalletDynamicViewModelImpl(get(), get(), get(), get()) }
        viewModel<PopupWalletDialogViewModel> { PopupWalletDialogViewModelImpl(get()) }
    }

}