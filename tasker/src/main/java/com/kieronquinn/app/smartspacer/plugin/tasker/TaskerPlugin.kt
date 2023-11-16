package com.kieronquinn.app.smartspacer.plugin.tasker

import android.content.Context
import com.google.gson.GsonBuilder
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import com.kieronquinn.app.smartspacer.plugin.tasker.adapters.ComplicationTemplateAdapter
import com.kieronquinn.app.smartspacer.plugin.tasker.adapters.DoorbellStateAdapter
import com.kieronquinn.app.smartspacer.plugin.tasker.adapters.IconAdapter
import com.kieronquinn.app.smartspacer.plugin.tasker.adapters.TapActionAdapter
import com.kieronquinn.app.smartspacer.plugin.tasker.adapters.TargetTemplateAdapter
import com.kieronquinn.app.smartspacer.plugin.tasker.fonts.FrameWeatherVF
import com.kieronquinn.app.smartspacer.plugin.tasker.model.ComplicationTemplate
import com.kieronquinn.app.smartspacer.plugin.tasker.model.Icon
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TapAction
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate.Doorbell.DoorbellState
import com.kieronquinn.app.smartspacer.plugin.tasker.model.database.TaskerPluginDatabase
import com.kieronquinn.app.smartspacer.plugin.tasker.repositories.DatabaseRepository
import com.kieronquinn.app.smartspacer.plugin.tasker.repositories.DatabaseRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.repositories.PackageRepository
import com.kieronquinn.app.smartspacer.plugin.tasker.repositories.PackageRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.repositories.WidgetRepository
import com.kieronquinn.app.smartspacer.plugin.tasker.repositories.WidgetRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.app.AppPickerViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.app.AppPickerViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.complication.ComplicationConfigurationViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.complication.ComplicationConfigurationViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.complication.picker.ComplicationPickerViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.complication.picker.ComplicationPickerViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.requirement.RequirementUpdateViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.requirement.RequirementUpdateViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.requirement.picker.RequirementPickerViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.requirement.picker.RequirementPickerViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.tapaction.TapActionEventViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.tapaction.TapActionEventViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.TargetConfigurationViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.TargetConfigurationViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.carousel.TargetCarouselViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.carousel.TargetCarouselViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.dismiss.TargetDismissEventConfigurationViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.dismiss.TargetDismissEventConfigurationViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.doorbellstate.TargetDoorbellStateViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.doorbellstate.TargetDoorbellStateViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.TargetExpandedViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.TargetExpandedViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.appshortcuts.TargetExpandedAppShortcutsViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.appshortcuts.TargetExpandedAppShortcutsViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.shortcuts.TargetExpandedShortcutsViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.shortcuts.TargetExpandedShortcutsViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.shortcuts.shortcut.TargetExpandedShortcutViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.shortcuts.shortcut.TargetExpandedShortcutViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.widget.TargetExpandedWidgetViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.widget.TargetExpandedWidgetViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.widget.picker.TargetExpandedWidgetPickerViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.widget.picker.TargetExpandedWidgetPickerViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.images.TargetImagesViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.images.TargetImagesViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.listitems.TargetListItemsViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.listitems.TargetListItemsViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.picker.TargetPickerViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.picker.TargetPickerViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.template.TargetTemplatePickerViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.template.TargetTemplatePickerViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.visibility.SmartspaceVisibilityEventConfigurationViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.visibility.SmartspaceVisibilityEventConfigurationViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.icon.IconPickerViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.icon.IconPickerViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.icon.file.FilePickerViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.icon.file.FilePickerViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.icon.font.FontPickerViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.icon.font.FontPickerViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.icon.font.picker.FontIconPickerViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.icon.font.picker.FontIconPickerViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.icon.url.UrlPickerViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.icon.url.UrlPickerViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.limittosurfaces.LimitToSurfacesViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.limittosurfaces.LimitToSurfacesViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.setup.complication.ComplicationSetupViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.setup.complication.ComplicationSetupViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.setup.requirement.RequirementSetupViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.setup.requirement.RequirementSetupViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.setup.target.TargetSetupViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.setup.target.TargetSetupViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.string.StringInputViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.string.StringInputViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.tapaction.TapActionViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.tapaction.TapActionViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.text.TextInputViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.text.TextInputViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.update.complication.ComplicationUpdateEventConfigurationViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.update.complication.ComplicationUpdateEventConfigurationViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.update.target.TargetUpdateEventConfigurationViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.update.target.TargetUpdateEventConfigurationViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.visibility.complication.ComplicationVisibilityViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.visibility.complication.ComplicationVisibilityViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.visibility.target.TargetVisibilityViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.visibility.target.TargetVisibilityViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.weatherdata.WeatherDataViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.weatherdata.WeatherDataViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.weatherdata.icon.WeatherDataIconViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.weatherdata.icon.WeatherDataIconViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.markwon.HtmlFontHandler
import com.mikepenz.iconics.Iconics
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.html.HtmlPlugin
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import com.kieronquinn.app.smartspacer.plugin.tasker.repositories.NavGraphRepository as NavGraphRepositoryImpl

class TaskerPlugin: SmartspacerPlugin() {

    override fun getModule(context: Context) = module {
        single { createMarkwon(get()) }
        single { TaskerPluginDatabase.getDatabase(context) }
        single<DatabaseRepository> { DatabaseRepositoryImpl(get()) }
        single<PackageRepository> { PackageRepositoryImpl(get()) }
        single<NavGraphRepository> { NavGraphRepositoryImpl() }
        single<WidgetRepository> { WidgetRepositoryImpl(get()) }
        viewModel<TargetSetupViewModel> { TargetSetupViewModelImpl(get(), get()) }
        viewModel<StringInputViewModel> { StringInputViewModelImpl(get()) }
        viewModel<TextInputViewModel> { TextInputViewModelImpl(get()) }
        viewModel<TargetConfigurationViewModel> { TargetConfigurationViewModelImpl(get(), get()) }
        viewModel<TargetPickerViewModel> { TargetPickerViewModelImpl(get(), get()) }
        viewModel<TargetTemplatePickerViewModel> { TargetTemplatePickerViewModelImpl(get()) }
        viewModel<IconPickerViewModel> { IconPickerViewModelImpl(get()) }
        viewModel<FontPickerViewModel> { FontPickerViewModelImpl(get()) }
        viewModel<FontIconPickerViewModel> { FontIconPickerViewModelImpl(get()) }
        viewModel<FilePickerViewModel> { FilePickerViewModelImpl(get()) }
        viewModel<UrlPickerViewModel> { UrlPickerViewModelImpl(get()) }
        viewModel<TargetCarouselViewModel> { TargetCarouselViewModelImpl(get()) }
        viewModel<TargetImagesViewModel> { TargetImagesViewModelImpl(get()) }
        viewModel<TargetListItemsViewModel> { TargetListItemsViewModelImpl(get()) }
        viewModel<TapActionViewModel> { TapActionViewModelImpl(get()) }
        viewModel<TargetDoorbellStateViewModel> { TargetDoorbellStateViewModelImpl(get()) }
        viewModel<AppPickerViewModel> { args -> AppPickerViewModelImpl(get(), get(), args.get()) }
        viewModel<LimitToSurfacesViewModel> { LimitToSurfacesViewModelImpl(get()) }
        viewModel<TargetExpandedViewModel> { TargetExpandedViewModelImpl(get()) }
        viewModel<TargetExpandedWidgetViewModel> { TargetExpandedWidgetViewModelImpl(get()) }
        viewModel<TargetExpandedShortcutsViewModel> { TargetExpandedShortcutsViewModelImpl(get()) }
        viewModel<TargetExpandedShortcutViewModel> { TargetExpandedShortcutViewModelImpl(get()) }
        viewModel<TargetExpandedAppShortcutsViewModel> { TargetExpandedAppShortcutsViewModelImpl(get()) }
        viewModel<TargetExpandedWidgetPickerViewModel> { TargetExpandedWidgetPickerViewModelImpl(get(), get()) }
        viewModel<WeatherDataViewModel> { WeatherDataViewModelImpl(get()) }
        viewModel<WeatherDataIconViewModel> { WeatherDataIconViewModelImpl(get()) }
        viewModel<TargetVisibilityViewModel> { TargetVisibilityViewModelImpl(get(), get()) }
        viewModel<TapActionEventViewModel> { TapActionEventViewModelImpl(get()) }
        viewModel<TargetDismissEventConfigurationViewModel> { TargetDismissEventConfigurationViewModelImpl(get(), get()) }
        viewModel<TargetUpdateEventConfigurationViewModel> { TargetUpdateEventConfigurationViewModelImpl(get(), get()) }
        viewModel<ComplicationSetupViewModel> { ComplicationSetupViewModelImpl(get(), get()) }
        viewModel<ComplicationConfigurationViewModel> { ComplicationConfigurationViewModelImpl(get(), get()) }
        viewModel<ComplicationPickerViewModel> { ComplicationPickerViewModelImpl(get(), get()) }
        viewModel<ComplicationVisibilityViewModel> { ComplicationVisibilityViewModelImpl(get(), get()) }
        viewModel<ComplicationUpdateEventConfigurationViewModel> { ComplicationUpdateEventConfigurationViewModelImpl(get(), get()) }
        viewModel<RequirementSetupViewModel> { RequirementSetupViewModelImpl(get(), get()) }
        viewModel<RequirementUpdateViewModel> { RequirementUpdateViewModelImpl(get(), get()) }
        viewModel<RequirementPickerViewModel> { RequirementPickerViewModelImpl(get(), get()) }
        viewModel<SmartspaceVisibilityEventConfigurationViewModel> { SmartspaceVisibilityEventConfigurationViewModelImpl() }
    }

    override fun GsonBuilder.configure() = apply {
        registerTypeAdapter(TargetTemplate::class.java, TargetTemplateAdapter)
        registerTypeAdapter(ComplicationTemplate::class.java, ComplicationTemplateAdapter)
        registerTypeAdapter(DoorbellState::class.java, DoorbellStateAdapter)
        registerTypeAdapter(Icon::class.java, IconAdapter)
        registerTypeAdapter(TapAction::class.java, TapActionAdapter)
    }

    override fun Context.configure() {
        Iconics.registerFont(FrameWeatherVF)
    }

    private fun createMarkwon(context: Context): Markwon {
        return Markwon.builder(context)
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(HtmlPlugin.create {
                it.addHandler(HtmlFontHandler(context))
            }).build()
    }

}