package com.kieronquinn.app.smartspacer.plugin.controls

import android.content.Context
import com.google.gson.GsonBuilder
import com.kieronquinn.app.smartspacer.plugin.controls.adapters.IconAdapter
import com.kieronquinn.app.smartspacer.plugin.controls.model.Icon
import com.kieronquinn.app.smartspacer.plugin.controls.model.database.ControlsDatabase
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.ControlsRepository
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.ControlsRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.ControlsSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.ControlsSettingsRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.DatabaseRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.NavGraphRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.NotificationRepository
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.NotificationRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.ShizukuServiceRepository
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.ShizukuServiceRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.complication.ComplicationConfigurationViewModel
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.complication.ComplicationConfigurationViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.icon.IconPickerViewModel
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.icon.IconPickerViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.icon.file.FilePickerViewModel
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.icon.file.FilePickerViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.icon.font.FontPickerViewModel
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.icon.font.FontPickerViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.icon.font.picker.FontIconPickerViewModel
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.icon.font.picker.FontIconPickerViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.picker.app.AppPickerViewModel
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.picker.app.AppPickerViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.picker.control.ControlPickerViewModel
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.picker.control.ControlPickerViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.requirement.RequirementConfigurationViewModel
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.requirement.RequirementConfigurationViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.target.TargetConfigurationViewModel
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.target.TargetConfigurationViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.target.subtitle.CustomSubtitleViewModel
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.target.subtitle.CustomSubtitleViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.title.CustomTitleViewModel
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.title.CustomTitleViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.popup.PopupControlDialogViewModel
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.popup.PopupControlDialogViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.settings.SettingsViewModel
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.settings.SettingsViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DatabaseRepository
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class ControlsPlugin: SmartspacerPlugin() {

    override fun getModule(context: Context) = module {
        single { ControlsDatabase.getDatabase(get()) }
        single<DatabaseRepository> { DatabaseRepositoryImpl(get()) }
        single<ControlsSettingsRepository> { ControlsSettingsRepositoryImpl(get()) }
        single<ShizukuServiceRepository> { ShizukuServiceRepositoryImpl(get()) }
        single<ControlsRepository>(createdAtStart = true) { ControlsRepositoryImpl(get(), get(), get(), get()) }
        single<NotificationRepository> { NotificationRepositoryImpl(get()) }
        single<NavGraphRepository> { NavGraphRepositoryImpl() }
        viewModel<ComplicationConfigurationViewModel> { ComplicationConfigurationViewModelImpl(get(), get(), get(), get(), get()) }
        viewModel<TargetConfigurationViewModel> { TargetConfigurationViewModelImpl(get(), get(), get(), get(), get()) }
        viewModel<RequirementConfigurationViewModel> { RequirementConfigurationViewModelImpl(get(), get(), get(), get(), get()) }
        viewModel<AppPickerViewModel> { AppPickerViewModelImpl(get(), get()) }
        viewModel<ControlPickerViewModel> { ControlPickerViewModelImpl(get(), get(), get()) }
        viewModel<IconPickerViewModel> { IconPickerViewModelImpl(get()) }
        viewModel<FontPickerViewModel> { FontPickerViewModelImpl(get()) }
        viewModel<FontIconPickerViewModel> { FontIconPickerViewModelImpl(get()) }
        viewModel<FilePickerViewModel> { FilePickerViewModelImpl(get()) }
        viewModel<CustomTitleViewModel> { CustomTitleViewModelImpl(get()) }
        viewModel<CustomSubtitleViewModel> { CustomSubtitleViewModelImpl(get()) }
        viewModel<PopupControlDialogViewModel> { PopupControlDialogViewModelImpl(get(), get()) }
        viewModel<SettingsViewModel> { SettingsViewModelImpl(get()) }
    }

    override fun GsonBuilder.configure() = apply {
        registerTypeAdapter(Icon::class.java, IconAdapter)
    }

}