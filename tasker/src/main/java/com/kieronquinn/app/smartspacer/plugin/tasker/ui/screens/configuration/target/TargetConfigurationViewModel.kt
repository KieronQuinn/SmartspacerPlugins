package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target

import android.content.Context
import android.text.InputType
import android.widget.ImageView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.ComplicationTemplate
import com.kieronquinn.app.smartspacer.plugin.tasker.model.ComplicationTemplate.ComplicationExtras
import com.kieronquinn.app.smartspacer.plugin.tasker.model.ComplicationTemplate.WeatherData
import com.kieronquinn.app.smartspacer.plugin.tasker.model.Icon
import com.kieronquinn.app.smartspacer.plugin.tasker.model.SmartspacerTargetUpdateTaskerInput
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TapAction
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate.Carousel.CarouselItem
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate.Doorbell.DoorbellState
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate.SubComplicationSupportingTarget
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate.TargetExtras
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate.TargetExtras.ExpandedState
import com.kieronquinn.app.smartspacer.plugin.tasker.model.Text
import com.kieronquinn.app.smartspacer.plugin.tasker.repositories.DatabaseRepository
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.complication.options.ComplicationOptionsProvider
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.carousel.TargetCarouselFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.doorbellstate.TargetDoorbellStateFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.TargetExpandedFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.images.TargetImagesFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.listitems.TargetListItemsFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.template.TargetTemplatePickerFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.icon.IconPickerFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.limittosurfaces.LimitToSurfacesFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.string.StringInputFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.string.StringInputFragment.InputValidation
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.string.StringInputFragment.NeutralAction
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.tapaction.TapActionFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.text.TextInputFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.weatherdata.WeatherDataFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions.takeIfNotBlank
import com.kieronquinn.app.smartspacer.sdk.model.UiSurface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import com.kieronquinn.app.smartspacer.plugin.tasker.model.database.Target as DatabaseTarget

abstract class TargetConfigurationViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun setInitialSmartspacerId(smartspacerId: String?)
    abstract fun setSmartspacerId(smartspacerId: String?)
    abstract fun setup(input: SmartspacerTargetUpdateTaskerInput?, blank: TargetTemplate)
    abstract fun setTargetTemplate(targetTemplate: TargetTemplate)

    abstract fun onSelectTargetClicked()
    abstract fun onTargetTemplateClicked()
    abstract fun onTargetTitleClicked()
    abstract fun onTargetTitleChanged(text: Text)
    abstract fun onTargetSubtitleClicked()
    abstract fun onTargetSubtitleChanged(text: Text)
    abstract fun onTargetIconClicked()
    abstract fun onTargetIconChanged(icon: Icon?)
    abstract fun onTargetTapActionClicked()
    abstract fun onTargetTapActionChanged(tapAction: TapAction)
    abstract fun onTargetRefreshPeriodClicked()
    abstract fun onTargetRefreshPeriodChanged(refreshPeriod: String)
    abstract fun onTargetRefreshWhenNotVisibleChanged(enabled: Boolean)

    abstract fun onTargetSubComplicationEnabledChanged(context: Context, enabled: Boolean)

    abstract fun onTargetButtonTextClicked()
    abstract fun onTargetButtonTextChanged(text: Text)
    abstract fun onTargetButtonIconClicked()
    abstract fun onTargetButtonIconChanged(icon: Icon)

    abstract fun onTargetHeadToHeadTitleClicked()
    abstract fun onTargetHeadToHeadTitleChanged(text: Text)
    abstract fun onTargetHeadToHeadTeamOneTextClicked()
    abstract fun onTargetHeadToHeadTeamOneTextChanged(text: Text)
    abstract fun onTargetHeadToHeadTeamOneIconClicked()
    abstract fun onTargetHeadToHeadTeamOneIconChanged(icon: Icon)
    abstract fun onTargetHeadToHeadTeamTwoTextClicked()
    abstract fun onTargetHeadToHeadTeamTwoTextChanged(text: Text)
    abstract fun onTargetHeadToHeadTeamTwoIconClicked()
    abstract fun onTargetHeadToHeadTeamTwoIconChanged(icon: Icon)

    abstract fun onTargetDoorbellStateClicked()
    abstract fun onTargetDoorbellStateChanged(doorbellState: DoorbellState)
    abstract fun onTargetDoorbellIconClicked()
    abstract fun onTargetDoorbellIconChanged(icon: Icon)
    abstract fun onTargetDoorbellShowLoadingBarChanged(enabled: Boolean)
    abstract fun onTargetDoorbellImageClicked()
    abstract fun onTargetDoorbellImageChanged(icon: Icon)
    abstract fun onTargetDoorbellImagesClicked()
    abstract fun onTargetDoorbellImagesChanged(images: List<Icon>)
    abstract fun onTargetDoorbellImageScaleTypeChanged(scaleType: ImageView.ScaleType)
    abstract fun onTargetDoorbellFrameDurationClicked()
    abstract fun onTargetDoorbellFrameDurationChanged(duration: String)
    abstract fun onTargetDoorbellWidthClicked()
    abstract fun onTargetDoorbellWidthChanged(width: String?)
    abstract fun onTargetDoorbellHeightClicked()
    abstract fun onTargetDoorbellHeightChanged(height: String?)
    abstract fun onTargetDoorbellRatioWidthClicked()
    abstract fun onTargetDoorbellRatioWidthChanged(width: String?)
    abstract fun onTargetDoorbellRatioHeightClicked()
    abstract fun onTargetDoorbellRatioHeightChanged(height: String?)

    abstract fun onTargetImageImageClicked()
    abstract fun onTargetImageImageChanged(icon: Icon)

    abstract fun onTargetImagesAspectRatioClicked()
    abstract fun onTargetImagesAspectRatioChanged(ratio: String?)
    abstract fun onTargetImagesFrameDurationClicked()
    abstract fun onTargetImagesFrameDurationChanged(duration: String)
    abstract fun onTargetImagesImagesClicked()
    abstract fun onTargetImagesImagesChanged(images: List<Icon>)
    abstract fun onTargetImagesTapActionClicked()
    abstract fun onTargetImagesTapActionChanged(tapAction: TapAction?)

    abstract fun onTargetListItemsItemsClicked()
    abstract fun onTargetListItemsItemsChanged(items: List<Text>)
    abstract fun onTargetListItemsIconClicked()
    abstract fun onTargetListItemsIconChanged(icon: Icon)
    abstract fun onTargetListItemsEmptyLabelClicked()
    abstract fun onTargetListItemsEmptyLabelChanged(label: String)

    abstract fun onTargetLoyaltyCardIconClicked()
    abstract fun onTargetLoyaltyCardIconChanged(icon: Icon)
    abstract fun onTargetLoyaltyCardScaleTypeChanged(scaleType: ImageView.ScaleType)
    abstract fun onTargetLoyaltyCardWidthClicked()
    abstract fun onTargetLoyaltyCardWidthChanged(width: String?)
    abstract fun onTargetLoyaltyCardHeightClicked()
    abstract fun onTargetLoyaltyCardHeightChanged(height: String?)
    abstract fun onTargetLoyaltyCardPromptClicked()
    abstract fun onTargetLoyaltyCardPromptChanged(prompt: Text)

    abstract fun onTargetCarouselItemsClicked()
    abstract fun onTargetCarouselItemsChanged(items: List<CarouselItem>)
    abstract fun onTargetCarouselTapActionClicked()
    abstract fun onTargetCarouselTapActionChanged(tapAction: TapAction?)

    abstract fun onTargetExpandedStateClicked()
    abstract fun onTargetExpandedStateChanged(state: ExpandedState)
    abstract fun onTargetSourceNotificationKeyClicked()
    abstract fun onTargetSourceNotificationKeyChanged(key: String)
    abstract fun onTargetAllowDismissChanged(enabled: Boolean)
    abstract fun onTargetCanTakeTwoComplicationsChanged(enabled: Boolean)
    abstract fun onTargetHideIfNoComplicationsChanged(enabled: Boolean)
    abstract fun onTargetLimitToSurfacesClicked()
    abstract fun onTargetLimitToSurfacesChanged(surfaces: Set<UiSurface>)
    abstract fun onTargetAboutIntentClicked()
    abstract fun onTargetAboutIntentChanged(tapAction: TapAction?)
    abstract fun onTargetFeedbackIntentClicked()
    abstract fun onTargetFeedbackIntentChanged(tapAction: TapAction?)
    abstract fun onTargetHideTitleOnAodChanged(enabled: Boolean)
    abstract fun onTargetHideSubtitleOnAodChanged(enabled: Boolean)

    abstract fun onTargetSubComplicationIconClicked()
    abstract fun onTargetSubComplicationIconChanged(icon: Icon?)
    abstract fun onTargetSubComplicationContentClicked()
    abstract fun onTargetSubComplicationContentChanged(content: Text)
    abstract fun onTargetSubComplicationTapActionClicked()
    abstract fun onTargetSubComplicationTapActionChanged(tapAction: TapAction)
    abstract fun onTargetSubComplicationLimitToSurfacesClicked()
    abstract fun onTargetSubComplicationLimitToSurfacesChanged(surfaces: Set<UiSurface>)
    abstract fun onTargetSubComplicationWeatherDataClicked()
    abstract fun onTargetSubComplicationWeatherDataChanged(weatherData: WeatherData?)
    abstract fun onTargetSubComplicationDisableTrimChanged(enabled: Boolean)

    sealed class State {
        object Loading: State()
        object SelectTarget: State()
        data class Target(
            val target: DatabaseTarget?,
            val refreshPeriod: String = "0",
            val refreshIfNotVisible: Boolean = false,
            val template: TargetTemplate,
            val preview: TargetTemplate
        ): State()
    }

}

class TargetConfigurationViewModelImpl(
    private val navigation: ContainerNavigation,
    databaseRepository: DatabaseRepository
): TargetConfigurationViewModel(), KoinComponent {

    private val passedSmartspacerId = MutableStateFlow<String?>(null)
    private val setSmartspacerId = MutableStateFlow<String?>(null)

    private val smartspacerId = combine(
        passedSmartspacerId,
        setSmartspacerId
    ) { passed, set ->
        set ?: passed
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val targetTemplate = MutableStateFlow<TargetTemplate?>(null)
    private val refreshPeriod = MutableStateFlow<String?>(null)
    private val refreshIfNotVisible = MutableStateFlow<Boolean?>(null)
    private val applicationContext by inject<Context>()

    private val basicComplicationProvider =
        ComplicationOptionsProvider.getProvider<ComplicationTemplate.Basic>()

    private val previewTemplate = targetTemplate.filterNotNull().map {
        it.copyWithManipulations(applicationContext, emptyMap())
    }

    private val target = smartspacerId.flatMapLatest {
        if(it == null) return@flatMapLatest flowOf(null)
        databaseRepository.getTargetAsFlow(it)
    }.flowOn(Dispatchers.IO)

    private val template = combine(
        target,
        targetTemplate.filterNotNull(),
        previewTemplate.filterNotNull()
    ) { database, target, preview ->
        Triple(database, target, preview)
    }

    override val state = combine(
        smartspacerId,
        template,
        refreshPeriod.filterNotNull(),
        refreshIfNotVisible.filterNotNull(),
    ) { id, target, period, notVisible ->
        if(id == null || target.first == null){
            State.SelectTarget
        }else{
            State.Target(target.first, period, notVisible, target.second, target.third)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setInitialSmartspacerId(smartspacerId: String?) {
        viewModelScope.launch {
            this@TargetConfigurationViewModelImpl.passedSmartspacerId.emit(smartspacerId)
        }
    }

    override fun setSmartspacerId(smartspacerId: String?) {
        viewModelScope.launch {
            this@TargetConfigurationViewModelImpl.setSmartspacerId.emit(smartspacerId)
        }
    }

    override fun setup(input: SmartspacerTargetUpdateTaskerInput?, blank: TargetTemplate) {
        viewModelScope.launch {
            if(this@TargetConfigurationViewModelImpl.targetTemplate.value != null) {
                return@launch
            }
            targetTemplate.emit(input?.targetTemplate ?: blank)
            refreshPeriod.emit(input?.refreshPeriod ?: "0")
            refreshIfNotVisible.emit(input?.refreshIfNotVisible ?: false)
        }
    }

    override fun setTargetTemplate(targetTemplate: TargetTemplate) {
        viewModelScope.launch {
            this@TargetConfigurationViewModelImpl.targetTemplate.emit(targetTemplate)
        }
    }

    override fun onSelectTargetClicked() {
        viewModelScope.launch {
            navigation.navigate(TargetConfigurationFragmentDirections.actionTargetConfigurationFragmentToTargetPickerFragment())
        }
    }

    override fun onTargetTemplateClicked() {
        withCurrent<TargetTemplate> {
            navigation.navigate(TargetConfigurationFragmentDirections.actionTargetConfigurationFragmentToTargetTemplatePickerFragment(
                TargetTemplatePickerFragment.Config(this::class.java)
            ))
        }
    }

    override fun onTargetTitleClicked() {
        withCurrent<TargetTemplate> {
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeText(
                TextInputFragment.Config(
                    R.string.configuration_target_title_title,
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_TITLE,
                    title
                )
            ))
        }
    }

    override fun onTargetTitleChanged(text: Text) {
        updateTarget<TargetTemplate> {
            copy(title = text)
        }
    }

    override fun onTargetSubtitleClicked() {
        withCurrent<TargetTemplate> {
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeText(
                TextInputFragment.Config(
                    R.string.configuration_target_subtitle_title,
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_SUBTITLE,
                    subtitle
                )
            ))
        }
    }

    override fun onTargetSubtitleChanged(text: Text) {
        updateTarget<TargetTemplate> {
            copy(subtitle = text)
        }
    }

    override fun onTargetIconClicked() {
        withCurrent<TargetTemplate> {
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeIcon(
                IconPickerFragment.Config(
                    R.string.configuration_target_icon_title,
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_ICON,
                    icon,
                    showNone = true
                )
            ))
        }
    }

    override fun onTargetIconChanged(icon: Icon?) {
        updateTarget<TargetTemplate> {
            copy(icon = icon)
        }
    }

    override fun onTargetTapActionClicked() {
        withCurrent<TargetTemplate> {
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeTapAction(
                TapActionFragment.Config(
                    R.string.tap_action_title_generic,
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_TAP_ACTION,
                    onClick
                )
            ))
        }
    }

    override fun onTargetTapActionChanged(tapAction: TapAction) {
        updateTarget<TargetTemplate> {
            copy(onClick = tapAction)
        }
    }

    override fun onTargetRefreshPeriodClicked() {
        viewModelScope.launch {
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeString(
                StringInputFragment.Config(
                    refreshPeriod.value.takeIf { it != "0" } ?: "",
                    TargetConfigurationFragment.REQUEST_KEY_REFRESH_PERIOD,
                    R.string.configuration_target_refresh_period_title,
                    R.string.configuration_target_refresh_period_content,
                    R.string.configuration_target_refresh_period_title,
                    inputValidation = InputValidation.REFRESH_PERIOD,
                    neutralAction = NeutralAction.REFRESH_PERIOD
                )
            ))
        }
    }

    override fun onTargetRefreshPeriodChanged(refreshPeriod: String) {
        viewModelScope.launch {
            this@TargetConfigurationViewModelImpl.refreshPeriod.emit(
                refreshPeriod.takeIfNotBlank() ?: "0"
            )
        }
    }

    override fun onTargetRefreshWhenNotVisibleChanged(enabled: Boolean) {
        viewModelScope.launch {
            refreshIfNotVisible.emit(enabled)
        }
    }

    override fun onTargetSubComplicationEnabledChanged(context: Context, enabled: Boolean) {
        viewModelScope.launch {
            updateSubComplication {
                copyWithSubComplication(subComplication = if(enabled) {
                    basicComplicationProvider.createBlank(context)
                } else null)
            }
        }
    }

    override fun onTargetButtonTextClicked() {
        withCurrent<TargetTemplate.Button> {
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeText(
                TextInputFragment.Config(
                    R.string.configuration_target_button_content_title,
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_BUTTON_TEXT,
                    buttonText
                )
            ))
        }
    }

    override fun onTargetButtonTextChanged(text: Text) {
        updateTarget<TargetTemplate.Button> {
            copy(buttonText = text)
        }
    }

    override fun onTargetButtonIconClicked() {
        withCurrent<TargetTemplate.Button> {
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeIcon(
                IconPickerFragment.Config(
                    R.string.configuration_target_button_icon_title,
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_BUTTON_ICON,
                    buttonIcon
                )
            ))
        }
    }

    override fun onTargetButtonIconChanged(icon: Icon) {
        updateTarget<TargetTemplate.Button> {
            copy(buttonIcon = icon)
        }
    }

    override fun onTargetHeadToHeadTitleClicked() {
        withCurrent<TargetTemplate.HeadToHead> {
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeText(
                TextInputFragment.Config(
                    R.string.configuration_target_head_to_head_main_title,
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_HEAD_TO_HEAD_TITLE,
                    headToHeadTitle
                )
            ))
        }
    }

    override fun onTargetHeadToHeadTitleChanged(text: Text) {
        updateTarget<TargetTemplate.HeadToHead> {
            copy(headToHeadTitle = text)
        }
    }

    override fun onTargetHeadToHeadTeamOneTextClicked() {
        withCurrent<TargetTemplate.HeadToHead> {
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeText(
                TextInputFragment.Config(
                    R.string.configuration_target_head_to_head_first_text_title,
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_HEAD_TO_HEAD_TEAM_1_TEXT,
                    headToHeadFirstCompetitorText
                )
            ))
        }
    }

    override fun onTargetHeadToHeadTeamOneTextChanged(text: Text) {
        updateTarget<TargetTemplate.HeadToHead> {
            copy(headToHeadFirstCompetitorText = text)
        }
    }

    override fun onTargetHeadToHeadTeamOneIconClicked() {
        withCurrent<TargetTemplate.HeadToHead> {
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeIcon(
                IconPickerFragment.Config(
                    R.string.configuration_target_head_to_head_first_icon_title,
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_HEAD_TO_HEAD_TEAM_1_ICON,
                    headToHeadFirstCompetitorIcon
                )
            ))
        }
    }

    override fun onTargetHeadToHeadTeamOneIconChanged(icon: Icon) {
        updateTarget<TargetTemplate.HeadToHead> {
            copy(headToHeadFirstCompetitorIcon = icon)
        }
    }

    override fun onTargetHeadToHeadTeamTwoTextClicked() {
        withCurrent<TargetTemplate.HeadToHead> {
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeText(
                TextInputFragment.Config(
                    R.string.configuration_target_head_to_head_second_text_title,
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_HEAD_TO_HEAD_TEAM_2_TEXT,
                    headToHeadSecondCompetitorText
                )
            ))
        }
    }

    override fun onTargetHeadToHeadTeamTwoTextChanged(text: Text) {
        updateTarget<TargetTemplate.HeadToHead> {
            copy(headToHeadSecondCompetitorText = text)
        }
    }

    override fun onTargetHeadToHeadTeamTwoIconClicked() {
        withCurrent<TargetTemplate.HeadToHead> {
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeIcon(
                IconPickerFragment.Config(
                    R.string.configuration_target_head_to_head_second_icon_title,
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_HEAD_TO_HEAD_TEAM_2_ICON,
                    headToHeadSecondCompetitorIcon
                )
            ))
        }
    }

    override fun onTargetHeadToHeadTeamTwoIconChanged(icon: Icon) {
        updateTarget<TargetTemplate.HeadToHead> {
            copy(headToHeadSecondCompetitorIcon = icon)
        }
    }

    override fun onTargetDoorbellStateClicked() {
        withCurrent<TargetTemplate.Doorbell> {
            navigation.navigate(TargetConfigurationFragmentDirections.actionTargetConfigurationFragmentToTargetDoorbellStateFragment(
                TargetDoorbellStateFragment.Config(
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_DOORBELL_STATE,
                    doorbellState.type
                )
            ))
        }
    }

    override fun onTargetDoorbellStateChanged(doorbellState: DoorbellState) {
        updateTarget<TargetTemplate.Doorbell> {
            copy(doorbellState = doorbellState)
        }
    }

    override fun onTargetDoorbellIconClicked() {
        withCurrent<TargetTemplate.Doorbell> {
            val doorbellState = doorbellState as? DoorbellState.Loading ?: return@withCurrent
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeIcon(
                IconPickerFragment.Config(
                    R.string.configuration_doorbell_loading_icon_title,
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_DOORBELL_ICON,
                    doorbellState.icon
                )
            ))
        }
    }

    override fun onTargetDoorbellIconChanged(icon: Icon) {
        updateTarget<TargetTemplate.Doorbell> {
            val doorbellState = doorbellState as? DoorbellState.Loading
                ?: return@updateTarget this
            copy(doorbellState = doorbellState.copy(icon = icon))
        }
    }

    override fun onTargetDoorbellShowLoadingBarChanged(enabled: Boolean) {
        updateTarget<TargetTemplate.Doorbell> {
            val doorbellState = doorbellState as? DoorbellState.Loading
                ?: return@updateTarget this
            copy(doorbellState = doorbellState.copy(showProgressBar = enabled))
        }
    }

    override fun onTargetDoorbellImageClicked() {
        withCurrent<TargetTemplate.Doorbell> {
            val doorbellState = doorbellState as? DoorbellState.ImageBitmap ?: return@withCurrent
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeIcon(
                IconPickerFragment.Config(
                    R.string.configuration_doorbell_image_title,
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_DOORBELL_IMAGE,
                    doorbellState.bitmap,
                    showBuiltIn = false,
                    showTint = false
                )
            ))
        }
    }

    override fun onTargetDoorbellImageChanged(icon: Icon) {
        updateTarget<TargetTemplate.Doorbell> {
            val doorbellState = doorbellState as? DoorbellState.ImageBitmap
                ?: return@updateTarget this
            copy(doorbellState = doorbellState.copy(bitmap = icon))
        }
    }

    override fun onTargetDoorbellImagesClicked() {
        withCurrent<TargetTemplate.Doorbell> {
            val doorbellState = doorbellState as? DoorbellState.ImageUri
                ?: return@withCurrent
            navigation.navigate(TargetConfigurationFragmentDirections.actionTargetConfigurationFragmentToTargetImagesFragment(
                TargetImagesFragment.Config(
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_DOORBELL_IMAGES,
                    doorbellState.imageUris
                )
            ))
        }
    }

    override fun onTargetDoorbellImagesChanged(images: List<Icon>) {
        updateTarget<TargetTemplate.Doorbell> {
            val doorbellState = doorbellState as? DoorbellState.ImageUri
                ?: return@updateTarget this
            copy(doorbellState = doorbellState.copy(imageUris = images))
        }
    }

    override fun onTargetDoorbellImageScaleTypeChanged(scaleType: ImageView.ScaleType) {
        updateTarget<TargetTemplate.Doorbell> {
            val doorbellState = doorbellState as? DoorbellState.ImageBitmap
                ?: return@updateTarget this
            copy(doorbellState = doorbellState.copy(imageScaleType = scaleType))
        }
    }

    override fun onTargetDoorbellFrameDurationClicked() {
        withCurrent<TargetTemplate.Doorbell> {
            val doorbellState = doorbellState as? DoorbellState.ImageUri
                ?: return@withCurrent
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeString(
                StringInputFragment.Config(
                    doorbellState._frameDurationMs,
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_DOORBELL_FRAME_DURATION,
                    R.string.configuration_doorbell_images_frame_duration,
                    R.string.configuration_doorbell_images_frame_duration_description,
                    R.string.configuration_doorbell_images_frame_duration,
                    inputValidation = InputValidation.FRAME_DURATION,
                    suffix = R.string.input_string_suffix_frame_duration,
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                )
            ))
        }
    }

    override fun onTargetDoorbellFrameDurationChanged(duration: String) {
        updateTarget<TargetTemplate.Doorbell> {
            val doorbellState = doorbellState as? DoorbellState.ImageUri
                ?: return@updateTarget this
            copy(doorbellState = doorbellState.copy(_frameDurationMs = duration))
        }
    }

    override fun onTargetDoorbellWidthClicked() {
        withCurrent<TargetTemplate.Doorbell> {
            val current = when(doorbellState) {
                is DoorbellState.Loading -> doorbellState._width
                is DoorbellState.Videocam -> doorbellState._width
                is DoorbellState.VideocamOff -> doorbellState._width
                is DoorbellState.ImageBitmap -> doorbellState._imageWidth
                else -> return@withCurrent
            }
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeString(
                StringInputFragment.Config(
                    current ?: "",
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_DOORBELL_WIDTH,
                    R.string.configuration_doorbell_width_title,
                    R.string.configuration_doorbell_width_description,
                    R.string.configuration_doorbell_width_title,
                    inputValidation = InputValidation.WIDTH,
                    suffix = R.string.input_string_suffix_pixels,
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                )
            ))
        }
    }

    override fun onTargetDoorbellWidthChanged(width: String?) {
        updateTarget<TargetTemplate.Doorbell> {
            val state = when(doorbellState) {
                is DoorbellState.Loading -> doorbellState.copy(_width = width)
                is DoorbellState.Videocam -> doorbellState.copy(_width = width)
                is DoorbellState.VideocamOff -> doorbellState.copy(_width = width)
                is DoorbellState.ImageBitmap -> doorbellState.copy(_imageWidth = width)
                else -> doorbellState
            }
            copy(doorbellState = state)
        }
    }

    override fun onTargetDoorbellHeightClicked() {
        withCurrent<TargetTemplate.Doorbell> {
            val current = when(doorbellState) {
                is DoorbellState.Loading -> doorbellState._height
                is DoorbellState.Videocam -> doorbellState._height
                is DoorbellState.VideocamOff -> doorbellState._height
                is DoorbellState.ImageBitmap -> doorbellState._imageHeight
                else -> return@withCurrent
            }
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeString(
                StringInputFragment.Config(
                    current ?: "",
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_DOORBELL_HEIGHT,
                    R.string.configuration_doorbell_height_title,
                    R.string.configuration_doorbell_height_description,
                    R.string.configuration_doorbell_height_title,
                    inputValidation = InputValidation.HEIGHT,
                    suffix = R.string.input_string_suffix_pixels,
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                )
            ))
        }
    }

    override fun onTargetDoorbellHeightChanged(height: String?) {
        updateTarget<TargetTemplate.Doorbell> {
            val state = when(doorbellState) {
                is DoorbellState.Loading -> doorbellState.copy(_height = height)
                is DoorbellState.Videocam -> doorbellState.copy(_height = height)
                is DoorbellState.VideocamOff -> doorbellState.copy(_height = height)
                is DoorbellState.ImageBitmap -> doorbellState.copy(_imageHeight = height)
                else -> doorbellState
            }
            copy(doorbellState = state)
        }
    }

    override fun onTargetDoorbellRatioWidthClicked() {
        withCurrent<TargetTemplate.Doorbell> {
            val current = when(doorbellState) {
                is DoorbellState.Loading -> doorbellState._ratioWidth
                is DoorbellState.Videocam -> doorbellState._ratioWidth
                is DoorbellState.VideocamOff -> doorbellState._ratioWidth
                else -> return@withCurrent
            }
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeString(
                StringInputFragment.Config(
                    current ?: "",
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_DOORBELL_RATIO_WIDTH,
                    R.string.configuration_doorbell_ratio_width_title,
                    R.string.configuration_doorbell_ratio_width_content,
                    R.string.configuration_doorbell_ratio_width_title,
                    inputValidation = InputValidation.WIDTH,
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                )
            ))
        }
    }

    override fun onTargetDoorbellRatioWidthChanged(width: String?) {
        updateTarget<TargetTemplate.Doorbell> {
            val state = when(doorbellState) {
                is DoorbellState.Loading -> doorbellState.copy(_ratioWidth = width)
                is DoorbellState.Videocam -> doorbellState.copy(_ratioWidth = width)
                is DoorbellState.VideocamOff -> doorbellState.copy(_ratioWidth = width)
                else -> doorbellState
            }
            copy(doorbellState = state)
        }
    }

    override fun onTargetDoorbellRatioHeightClicked() {
        withCurrent<TargetTemplate.Doorbell> {
            val current = when(doorbellState) {
                is DoorbellState.Loading -> doorbellState._ratioHeight
                is DoorbellState.Videocam -> doorbellState._ratioHeight
                is DoorbellState.VideocamOff -> doorbellState._ratioHeight
                else -> return@withCurrent
            }
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeString(
                StringInputFragment.Config(
                    current ?: "",
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_DOORBELL_RATIO_HEIGHT,
                    R.string.configuration_doorbell_ratio_height_title,
                    R.string.configuration_doorbell_ratio_height_content,
                    R.string.configuration_doorbell_ratio_height_title,
                    inputValidation = InputValidation.HEIGHT,
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                )
            ))
        }
    }

    override fun onTargetDoorbellRatioHeightChanged(height: String?) {
        updateTarget<TargetTemplate.Doorbell> {
            val state = when(doorbellState) {
                is DoorbellState.Loading -> doorbellState.copy(_ratioHeight = height)
                is DoorbellState.Videocam -> doorbellState.copy(_ratioHeight = height)
                is DoorbellState.VideocamOff -> doorbellState.copy(_ratioHeight = height)
                else -> doorbellState
            }
            copy(doorbellState = state)
        }
    }

    override fun onTargetImageImageClicked() {
        withCurrent<TargetTemplate.Image> {
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeIcon(
                IconPickerFragment.Config(
                    R.string.configuration_image_image_title,
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_IMAGE_IMAGE,
                    image,
                    showTint = false,
                    showBuiltIn = false
                )
            ))
        }
    }

    override fun onTargetImageImageChanged(icon: Icon) {
        updateTarget<TargetTemplate.Image> {
            copy(image = icon)
        }
    }

    override fun onTargetImagesAspectRatioClicked() {
        withCurrent<TargetTemplate.Images> {
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeString(
                StringInputFragment.Config(
                    imageDimensionRatio ?: "",
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_IMAGES_ASPECT_RATIO,
                    R.string.configuration_images_image_dimension_ratio_title,
                    R.string.configuration_images_image_dimension_ratio_description,
                    R.string.configuration_images_image_dimension_ratio_title,
                    inputValidation = InputValidation.ASPECT_RATIO,
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                )
            ))
        }
    }

    override fun onTargetImagesAspectRatioChanged(ratio: String?) {
        updateTarget<TargetTemplate.Images> {
            copy(imageDimensionRatio = ratio)
        }
    }

    override fun onTargetImagesFrameDurationClicked() {
        withCurrent<TargetTemplate.Images> {
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeString(
                StringInputFragment.Config(
                    _frameDurationMs ?: frameDurationMs.toString(),
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_IMAGES_FRAME_DURATION,
                    R.string.configuration_images_frame_duration,
                    R.string.configuration_images_frame_duration_description,
                    R.string.configuration_images_frame_duration,
                    inputValidation = InputValidation.FRAME_DURATION,
                    suffix = R.string.input_string_suffix_frame_duration,
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                )
            ))
        }
    }

    override fun onTargetImagesFrameDurationChanged(duration: String) {
        updateTarget<TargetTemplate.Images> {
            copy(_frameDurationMs = duration)
        }
    }

    override fun onTargetImagesImagesClicked() {
        withCurrent<TargetTemplate.Images> {
            navigation.navigate(TargetConfigurationFragmentDirections.actionTargetConfigurationFragmentToTargetImagesFragment(
                TargetImagesFragment.Config(
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_IMAGES_IMAGES,
                    images
                )
            ))
        }
    }

    override fun onTargetImagesImagesChanged(images: List<Icon>) {
        updateTarget<TargetTemplate.Images> {
            copy(images = images)
        }
    }

    override fun onTargetImagesTapActionClicked() {
        withCurrent<TargetTemplate.Images> {
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeTapAction(
                TapActionFragment.Config(
                    R.string.tap_action_title_generic,
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_IMAGES_TAP_ACTION,
                    imageClickIntent ?: onClick,
                    showParent = true
                )
            ))
        }
    }

    override fun onTargetImagesTapActionChanged(tapAction: TapAction?) {
        updateTarget<TargetTemplate.Images> {
            copy(imageClickIntent = tapAction)
        }
    }

    override fun onTargetListItemsItemsClicked() {
        withCurrent<TargetTemplate.ListItems> {
            navigation.navigate(TargetConfigurationFragmentDirections.actionTargetConfigurationFragmentToTargetListItemsFragment(
                TargetListItemsFragment.Config(
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_LIST_ITEMS_ITEMS,
                    listItems
                )
            ))
        }
    }

    override fun onTargetListItemsItemsChanged(items: List<Text>) {
        updateTarget<TargetTemplate.ListItems> {
            copy(listItems = items)
        }
    }

    override fun onTargetListItemsIconClicked() {
        withCurrent<TargetTemplate.ListItems> {
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeIcon(
                IconPickerFragment.Config(
                    R.string.configuration_target_list_icon_title,
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_LIST_ITEMS_ICON,
                    listIcon
                )
            ))
        }
    }

    override fun onTargetListItemsIconChanged(icon: Icon) {
        updateTarget<TargetTemplate.ListItems> {
            copy(listIcon = icon)
        }
    }

    override fun onTargetListItemsEmptyLabelClicked() {
        withCurrent<TargetTemplate.ListItems> {
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeString(
                StringInputFragment.Config(
                    emptyListMessage,
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_LIST_ITEMS_EMPTY_LABEL,
                    R.string.configuration_target_list_empty_title,
                    R.string.configuration_target_list_empty_content_description,
                    R.string.configuration_target_list_empty_title,
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
                )
            ))
        }
    }

    override fun onTargetListItemsEmptyLabelChanged(label: String) {
        updateTarget<TargetTemplate.ListItems> {
            copy(emptyListMessage = label)
        }
    }

    override fun onTargetLoyaltyCardIconClicked() {
        withCurrent<TargetTemplate.LoyaltyCard> {
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeIcon(
                IconPickerFragment.Config(
                    R.string.configuration_loyalty_card_icon_title,
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_LOYALTY_CARD_ICON,
                    cardIcon
                )
            ))
        }
    }

    override fun onTargetLoyaltyCardIconChanged(icon: Icon) {
        updateTarget<TargetTemplate.LoyaltyCard> {
            copy(cardIcon = icon)
        }
    }

    override fun onTargetLoyaltyCardScaleTypeChanged(scaleType: ImageView.ScaleType) {
        updateTarget<TargetTemplate.LoyaltyCard> {
            copy(imageScaleType = scaleType)
        }
    }

    override fun onTargetLoyaltyCardWidthClicked() {
        withCurrent<TargetTemplate.LoyaltyCard> {
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeString(
                StringInputFragment.Config(
                    _imageWidth ?: "",
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_LOYALTY_CARD_WIDTH,
                    R.string.configuration_loyalty_card_width_title,
                    R.string.configuration_loyalty_card_width_description,
                    R.string.configuration_loyalty_card_width_title,
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                )
            ))
        }
    }

    override fun onTargetLoyaltyCardWidthChanged(width: String?) {
        updateTarget<TargetTemplate.LoyaltyCard> {
            copy(_imageWidth = width)
        }
    }

    override fun onTargetLoyaltyCardHeightClicked() {
        withCurrent<TargetTemplate.LoyaltyCard> {
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeString(
                StringInputFragment.Config(
                    _imageHeight ?: "",
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_LOYALTY_CARD_HEIGHT,
                    R.string.configuration_loyalty_card_height_title,
                    R.string.configuration_loyalty_card_height_description,
                    R.string.configuration_loyalty_card_height_title,
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                )
            ))
        }
    }

    override fun onTargetLoyaltyCardHeightChanged(height: String?) {
        updateTarget<TargetTemplate.LoyaltyCard> {
            copy(_imageHeight = height)
        }
    }

    override fun onTargetLoyaltyCardPromptClicked() {
        withCurrent<TargetTemplate.LoyaltyCard> {
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeText(
                TextInputFragment.Config(
                    R.string.configuration_loyalty_card_prompt_title,
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_LOYALTY_CARD_PROMPT,
                    cardPrompt
                )
            ))
        }
    }

    override fun onTargetLoyaltyCardPromptChanged(prompt: Text) {
        updateTarget<TargetTemplate.LoyaltyCard> {
            copy(cardPrompt = prompt)
        }
    }

    override fun onTargetCarouselItemsClicked() {
        withCurrent<TargetTemplate.Carousel> {
            navigation.navigate(TargetConfigurationFragmentDirections.actionTargetConfigurationFragmentToTargetCarouselFragment(
                TargetCarouselFragment.Config(
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_CAROUSEL_ITEMS,
                    carouselItems
                )
            ))
        }
    }

    override fun onTargetCarouselItemsChanged(items: List<CarouselItem>) {
        updateTarget<TargetTemplate.Carousel> {
            copy(carouselItems = items)
        }
    }

    override fun onTargetCarouselTapActionClicked() {
        withCurrent<TargetTemplate.Carousel> {
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeTapAction(
                TapActionFragment.Config(
                    R.string.tap_action_title_generic,
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_CAROUSEL_TAP_ACTION,
                    onCarouselClick ?: onClick,
                    showParent = true
                )
            ))
        }
    }

    override fun onTargetCarouselTapActionChanged(tapAction: TapAction?) {
        updateTarget<TargetTemplate.Carousel> {
            copy(onCarouselClick = tapAction)
        }
    }

    override fun onTargetExpandedStateClicked() {
        withCurrentExtras {
            navigation.navigate(TargetConfigurationFragmentDirections.actionTargetConfigurationFragmentToTargetExpandedFragment(
                TargetExpandedFragment.Config(
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_EXPANDED_STATE,
                    expandedState ?: ExpandedState()
                )
            ))
        }
    }

    override fun onTargetExpandedStateChanged(state: ExpandedState) {
        updateTargetExtras {
            copy(expandedState = state)
        }
    }

    override fun onTargetSourceNotificationKeyClicked() {
        withCurrentExtras {
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeString(
                StringInputFragment.Config(
                    sourceNotificationKey ?: "",
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_SOURCE_NOTIFICATION_KEY,
                    R.string.configuration_target_source_notification_key_title,
                    R.string.configuration_target_source_notification_key_description,
                    R.string.configuration_target_source_notification_key_title
                )
            ))
        }
    }

    override fun onTargetSourceNotificationKeyChanged(key: String) {
        updateTargetExtras {
            copy(sourceNotificationKey = key.takeIfNotBlank())
        }
    }

    override fun onTargetAllowDismissChanged(enabled: Boolean) {
        updateTargetExtras {
            copy(canBeDismissed = enabled)
        }
    }

    override fun onTargetCanTakeTwoComplicationsChanged(enabled: Boolean) {
        updateTargetExtras {
            copy(canTakeTwoComplications = enabled)
        }
    }

    override fun onTargetHideIfNoComplicationsChanged(enabled: Boolean) {
        updateTargetExtras {
            copy(hideIfNoComplications = enabled)
        }
    }

    override fun onTargetLimitToSurfacesClicked() {
        withCurrentExtras {
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeSurface(
                LimitToSurfacesFragment.Config(
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_LIMIT_TO_SURFACES,
                    limitToSurfaces.toList()
                )
            ))
        }
    }

    override fun onTargetLimitToSurfacesChanged(surfaces: Set<UiSurface>) {
        updateTargetExtras {
            copy(limitToSurfaces = surfaces)
        }
    }

    override fun onTargetAboutIntentClicked() {
        withCurrentExtras {
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeTapAction(
                TapActionFragment.Config(
                    R.string.tap_action_title_generic,
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_ABOUT_TAP_ACTION,
                    aboutIntent,
                    showNone = true
                )
            ))
        }
    }

    override fun onTargetAboutIntentChanged(tapAction: TapAction?) {
        updateTargetExtras {
            copy(aboutIntent = tapAction)
        }
    }

    override fun onTargetFeedbackIntentClicked() {
        withCurrentExtras {
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeTapAction(
                TapActionFragment.Config(
                    R.string.tap_action_title_generic,
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_FEEDBACK_TAP_ACTION,
                    feedbackIntent,
                    showNone = true
                )
            ))
        }
    }

    override fun onTargetFeedbackIntentChanged(tapAction: TapAction?) {
        updateTargetExtras {
            copy(feedbackIntent = tapAction)
        }
    }

    override fun onTargetHideTitleOnAodChanged(enabled: Boolean) {
        updateTargetExtras {
            copy(hideTitleOnAod = enabled)
        }
    }

    override fun onTargetHideSubtitleOnAodChanged(enabled: Boolean) {
        updateTargetExtras {
            copy(hideSubtitleOnAod = enabled)
        }
    }

    override fun onTargetSubComplicationIconClicked() {
        withSubComplication {
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeIcon(
                IconPickerFragment.Config(
                    R.string.configuration_complication_icon_title,
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_SUB_ICON,
                    icon,
                    showNone = true
                )
            ))
        }
    }

    override fun onTargetSubComplicationIconChanged(icon: Icon?) {
        updateSubComplication {
            copyWithSubComplication(subComplication?.copyCompat(icon = icon))
        }
    }

    override fun onTargetSubComplicationContentClicked() {
        withSubComplication {
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeText(
                TextInputFragment.Config(
                    R.string.configuration_complication_content_title,
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_SUB_CONTENT,
                    content
                )
            ))
        }
    }

    override fun onTargetSubComplicationContentChanged(content: Text) {
        updateSubComplication {
            copyWithSubComplication(subComplication?.copyCompat(content = content))
        }
    }

    override fun onTargetSubComplicationTapActionClicked() {
        withSubComplication {
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeTapAction(
                TapActionFragment.Config(
                    R.string.configuration_complication_tap_action_title,
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_SUB_TAP_ACTION,
                    onClick
                )
            ))
        }
    }

    override fun onTargetSubComplicationTapActionChanged(tapAction: TapAction) {
        updateSubComplication {
            copyWithSubComplication(subComplication?.copyCompat(onClick = tapAction))
        }
    }

    override fun onTargetSubComplicationLimitToSurfacesClicked() {
        withSubComplication {
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeSurface(
                LimitToSurfacesFragment.Config(
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_SUB_LIMIT_TO_SURFACES,
                    complicationExtras.limitToSurfaces.toList()
                )
            ))
        }
    }

    override fun onTargetSubComplicationLimitToSurfacesChanged(surfaces: Set<UiSurface>) {
        updateSubComplication {
            val extras = subComplication?.complicationExtras ?: ComplicationExtras()
            copyWithSubComplication(subComplication?.copyCompat(
                complicationExtras = extras.copy(limitToSurfaces = surfaces))
            )
        }
    }

    override fun onTargetSubComplicationWeatherDataClicked() {
        withSubComplication {
            navigation.navigate(TargetConfigurationFragmentDirections.actionGlobalNavGraphIncludeWeatherData(
                WeatherDataFragment.Config(
                    TargetConfigurationFragment.REQUEST_KEY_TARGET_SUB_WEATHER_DATA,
                    complicationExtras.weatherData
                )
            ))
        }
    }

    override fun onTargetSubComplicationWeatherDataChanged(weatherData: WeatherData?) {
        updateSubComplication {
            val extras = subComplication?.complicationExtras ?: ComplicationExtras()
            copyWithSubComplication(subComplication?.copyCompat(
                complicationExtras = extras.copy(weatherData = weatherData))
            )
        }
    }

    override fun onTargetSubComplicationDisableTrimChanged(enabled: Boolean) {
        updateSubComplication {
            copyWithSubComplication(subComplication?.copyCompat(disableTrim = enabled))
        }
    }

    private fun <T: TargetTemplate> withCurrent(block: suspend T.() -> Unit) {
        viewModelScope.launch {
            val current = (state.value as? State.Target)?.template as? T ?: return@launch
            block(current)
        }
    }

    private fun withCurrentExtras(block: suspend TargetExtras.() -> Unit) {
        withCurrent<TargetTemplate> {
            block(targetExtras)
        }
    }

    private fun <T: TargetTemplate> updateTarget(block: T.() -> T) {
        withCurrent<T> {
            targetTemplate.emit(block(this))
        }
    }

    private fun updateTargetExtras(block: TargetExtras.() -> TargetExtras) {
        updateTarget<TargetTemplate> {
            copy(targetExtras = block(targetExtras))
        }
    }

    private fun withCurrentSubComplicationSupportingTarget(
        block: suspend SubComplicationSupportingTarget.() -> Unit
    ) {
        viewModelScope.launch {
            val current = (state.value as? State.Target)?.template
                    as? SubComplicationSupportingTarget ?: return@launch
            block(current)
        }
    }

    private fun updateSubComplication(
        block: SubComplicationSupportingTarget.() -> SubComplicationSupportingTarget?
    ) {
        withCurrentSubComplicationSupportingTarget {
            targetTemplate.emit(block() as TargetTemplate)
        }
    }

    private fun withSubComplication(block: suspend ComplicationTemplate.() -> Unit) {
        withCurrentSubComplicationSupportingTarget {
            block(subComplication ?: return@withCurrentSubComplicationSupportingTarget)
        }
    }

}