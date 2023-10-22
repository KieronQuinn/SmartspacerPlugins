package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target

import android.content.ComponentName
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.shape.CornerFamily
import com.google.gson.Gson
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputInfo
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputInfos
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Card
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BoundFragment
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.LockCollapsed
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.ProvidesBack
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.utils.appbar.DragOptionalAppBarLayoutBehaviour.Companion.isDraggable
import com.kieronquinn.app.smartspacer.plugin.shared.utils.appbar.DragOptionalAppBarLayoutBehaviour.Companion.setDraggable
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.applyBottomNavigationInset
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.collapsedState
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.expandProgress
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.getRememberedAppBarCollapsed
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.rememberAppBarCollapsed
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugin.shared.utils.onClicked
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.actions.UpdateTargetAction
import com.kieronquinn.app.smartspacer.plugin.tasker.databinding.FragmentTargetConfigurationBinding
import com.kieronquinn.app.smartspacer.plugin.tasker.model.SmartspacerTargetUpdateTaskerInput
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.activities.TargetConfigurationActivity
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.complication.options.Basic
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.TargetConfigurationViewModel.State
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.carousel.TargetCarouselFragment.Companion.setupCarouselItemResultListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.doorbellstate.TargetDoorbellStateFragment.Companion.setupDoorbellStateResultListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.TargetExpandedFragment.Companion.setupExpandedResultListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.images.TargetImagesFragment.Companion.setupImagesResultListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.listitems.TargetListItemsFragment.Companion.setupListItemsResultListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.Basic.BasicOptionsListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.Button.ButtonOptionsListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.Carousel.CarouselOptionsListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.Doorbell.DoorbellOptionsListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.HeadToHead.HeadToHeadOptionsListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.Image.ImageOptionsListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.Images.ImagesOptionsListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.ListItems.ListItemsOptionsListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.LoyaltyCard.LoyaltyCardOptionsListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.picker.TargetPickerFragment.Companion.setupTargetPickerListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.template.TargetTemplatePickerFragment.Companion.setupTargetTemplatePickerListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.icon.IconPickerFragment.Companion.setupIconResultListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.icon.IconPickerFragment.Companion.setupIconResultListenerNullable
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.limittosurfaces.LimitToSurfacesFragment.Companion.setupLimitToSurfacesResultListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.string.StringInputFragment.Companion.setupStringResultListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.tapaction.TapActionFragment.Companion.setupTapActionResultListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.tapaction.TapActionFragment.Companion.setupTapActionResultListenerNullable
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.text.TextInputFragment.Companion.setupTextResultListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.weatherdata.WeatherDataFragment.Companion.setupWeatherDataResultListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.views.DemoBcSmartspaceView.Companion.TARGET_ID_PREVIEW
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.views.DemoBcSmartspaceView.Companion.toDemoTarget
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.DateTimeFormatter
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions.takeIfNotBlank
import com.kieronquinn.app.smartspacer.sdk.client.SmartspacerClient
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.monetcompat.extensions.views.applyMonet
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import android.graphics.drawable.Icon as AndroidIcon
import com.kieronquinn.app.shared.R as SharedR
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon as SmartspacerIcon
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction as SmartspacerTapAction
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate as SmartspacerTargetTemplate

class TargetConfigurationFragment: BoundFragment<FragmentTargetConfigurationBinding>(
    FragmentTargetConfigurationBinding::inflate
), BackAvailable, ProvidesBack, LockCollapsed, BasicOptionsListener, ButtonOptionsListener, CarouselOptionsListener, DoorbellOptionsListener, HeadToHeadOptionsListener, ImageOptionsListener, ImagesOptionsListener, ListItemsOptionsListener, LoyaltyCardOptionsListener, Basic.BasicOptionsListener {

    companion object {
        private const val FRAGMENT_ARGUMENTS_APP_BAR_COLLAPSED = "app_bar_collapsed_inner"
        const val REQUEST_KEY_TARGET_TITLE = "target_title"
        const val REQUEST_KEY_TARGET_SUBTITLE = "target_subtitle"
        const val REQUEST_KEY_TARGET_ICON = "target_icon"
        const val REQUEST_KEY_TARGET_TAP_ACTION = "target_tap_action"
        const val REQUEST_KEY_REFRESH_PERIOD = "target_refresh_period"

        const val REQUEST_KEY_TARGET_BUTTON_TEXT = "button_text"
        const val REQUEST_KEY_TARGET_BUTTON_ICON = "button_icon"

        const val REQUEST_KEY_TARGET_DOORBELL_STATE = "doorbell_state"
        const val REQUEST_KEY_TARGET_DOORBELL_ICON = "doorbell_icon"
        const val REQUEST_KEY_TARGET_DOORBELL_IMAGE = "doorbell_image"
        const val REQUEST_KEY_TARGET_DOORBELL_IMAGES = "doorbell_images"
        const val REQUEST_KEY_TARGET_DOORBELL_FRAME_DURATION = "doorbell_frame_duration"
        const val REQUEST_KEY_TARGET_DOORBELL_WIDTH = "doorbell_width"
        const val REQUEST_KEY_TARGET_DOORBELL_HEIGHT = "doorbell_height"
        const val REQUEST_KEY_TARGET_DOORBELL_RATIO_WIDTH = "doorbell_ratio_width"
        const val REQUEST_KEY_TARGET_DOORBELL_RATIO_HEIGHT = "doorbell_ratio_height"

        const val REQUEST_KEY_TARGET_HEAD_TO_HEAD_TITLE = "head_to_head_title"
        const val REQUEST_KEY_TARGET_HEAD_TO_HEAD_TEAM_1_TEXT = "head_to_head_team_1_text"
        const val REQUEST_KEY_TARGET_HEAD_TO_HEAD_TEAM_1_ICON = "head_to_head_team_1_icon"
        const val REQUEST_KEY_TARGET_HEAD_TO_HEAD_TEAM_2_TEXT = "head_to_head_team_2_text"
        const val REQUEST_KEY_TARGET_HEAD_TO_HEAD_TEAM_2_ICON = "head_to_head_team_2_icon"

        const val REQUEST_KEY_TARGET_IMAGE_IMAGE = "image_image"

        const val REQUEST_KEY_TARGET_LIST_ITEMS_ITEMS = "list_items_items"
        const val REQUEST_KEY_TARGET_LIST_ITEMS_ICON = "list_items_icon"
        const val REQUEST_KEY_TARGET_LIST_ITEMS_EMPTY_LABEL = "list_items_empty_label"

        const val REQUEST_KEY_TARGET_IMAGES_ASPECT_RATIO = "images_aspect_ratio"
        const val REQUEST_KEY_TARGET_IMAGES_FRAME_DURATION = "images_frame_duration"
        const val REQUEST_KEY_TARGET_IMAGES_IMAGES = "images_images"
        const val REQUEST_KEY_TARGET_IMAGES_TAP_ACTION = "images_tap_action"

        const val REQUEST_KEY_TARGET_LOYALTY_CARD_ICON = "loyalty_card_icon"
        const val REQUEST_KEY_TARGET_LOYALTY_CARD_WIDTH = "loyalty_card_width"
        const val REQUEST_KEY_TARGET_LOYALTY_CARD_HEIGHT = "loyalty_card_height"
        const val REQUEST_KEY_TARGET_LOYALTY_CARD_PROMPT = "loyalty_card_prompt"

        const val REQUEST_KEY_TARGET_CAROUSEL_ITEMS = "carousel_items"
        const val REQUEST_KEY_TARGET_CAROUSEL_TAP_ACTION = "carousel_tap_action"

        const val REQUEST_KEY_TARGET_EXPANDED_STATE = "expanded_state"
        const val REQUEST_KEY_TARGET_SOURCE_NOTIFICATION_KEY = "source_notification_key"
        const val REQUEST_KEY_TARGET_LIMIT_TO_SURFACES = "limit_to_surfaces"
        const val REQUEST_KEY_TARGET_ABOUT_TAP_ACTION = "about_tap_action"
        const val REQUEST_KEY_TARGET_FEEDBACK_TAP_ACTION = "feedback_tap_action"

        const val REQUEST_KEY_TARGET_SUB_ICON = "sub_icon"
        const val REQUEST_KEY_TARGET_SUB_CONTENT = "sub_content"
        const val REQUEST_KEY_TARGET_SUB_TAP_ACTION = "sub_tap_action"
        const val REQUEST_KEY_TARGET_SUB_LIMIT_TO_SURFACES = "sub_limit_to_surfaces"
        const val REQUEST_KEY_TARGET_SUB_WEATHER_DATA = "sub_weather_data"
    }

    private val viewModel by viewModel<TargetConfigurationViewModel>()
    private val gson by inject<Gson>()

    private val permissionResult = registerForActivityResult(StartIntentSenderForResult()) {
        val current = (viewModel.state.value as? State.Target)?.preview
            ?: return@registerForActivityResult
        updatePreview(current)
    }

    private val client by lazy {
        SmartspacerClient.getInstance(requireContext())
    }

    private val configurationActivity by lazy {
        requireActivity() as TargetConfigurationActivity
    }

    private val taskerAction by lazy {
        UpdateTargetAction(configurationActivity)
    }

    private val contentAdapter by lazy {
        SettingsAdapter()
    }

    private val dateTimeFormatter by lazy {
        DateTimeFormatter(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        taskerAction.onCreate()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val background = monet.getBackgroundColorSecondary(requireContext())
            ?: monet.getBackgroundColor(requireContext())
        binding.root.setBackgroundColor(background)
        setupState()
        setupBlocker()
        setupMonet()
        setupCollapsedState()
        setupCard()
        setupPreview()
        setupContent()
        setupListeners()
        viewModel.setSmartspacerId(configurationActivity.taskerInput?.smartspacerId)
        viewModel.setup(configurationActivity.taskerInput, createNewTargetTemplate())
    }

    override fun onBackPressed(): Boolean {
        val target = (viewModel.state.value as? State.Target) ?: return false
        val databaseTarget = target.target ?: return false
        val variables = target.template.getVariables().map {
            TaskerInputInfo(it, it, null, true, it)
        }.let {
            TaskerInputInfos().apply {
                addAll(it)
            }
        }
        val templateJson = gson.toJson(target.template)
        configurationActivity.setDynamicInputs(variables)
        configurationActivity.setStaticInputs(
            SmartspacerTargetUpdateTaskerInput(
                databaseTarget.smartspacerId,
                databaseTarget.name,
                templateJson,
                target.refreshPeriod,
                target.refreshIfNotVisible
            )
        )
        taskerAction.finishForTasker()
        return true
    }

    private fun setupPreview() = with(binding.targetConfigurationSmartspace) {
        whenResumed {
            binding.targetConfigurationAppBar.expandProgress().collect {
                alpha = maxOf((it - 0.6666f) * 3f, 0f)
            }
        }
    }

    private fun setupContent() = with(binding.targetConfigurationRecyclerview) {
        layoutManager = LinearLayoutManager(context)
        adapter = contentAdapter
        applyBottomNavigationInset(resources.getDimension(SharedR.dimen.margin_16))
    }

    private fun setupMonet() = with(binding.targetConfigurationLoading){
        loadingProgress.applyMonet()
    }

    private fun setupCollapsedState() = whenResumed {
        binding.targetConfigurationAppBar.collapsedState().collect {
            if(binding.targetConfigurationAppBar.isDraggable()) {
                rememberAppBarCollapsed(it, FRAGMENT_ARGUMENTS_APP_BAR_COLLAPSED)
            }
        }
    }

    private fun setupCard() = with(binding.targetConfigurationCardView) {
        setCardBackgroundColor(monet.getBackgroundColor(context))
        val roundedCornerSize = resources.getDimension(SharedR.dimen.margin_16)
        whenResumed {
            binding.targetConfigurationAppBar.expandProgress().collect {
                shapeAppearanceModel = shapeAppearanceModel.toBuilder()
                    .setTopLeftCorner(CornerFamily.ROUNDED, roundedCornerSize * it)
                    .setTopRightCorner(CornerFamily.ROUNDED, roundedCornerSize * it)
                    .build()
            }
        }
    }

    private fun setupListeners() {
        setupTargetPickerListener { viewModel.setSmartspacerId(it) }
        setupTargetTemplatePickerListener {
            val current = (viewModel.state.value as? State.Target)?.template
            //Don't overwrite the current if it's the same as selected
            if(current?.javaClass == it) return@setupTargetTemplatePickerListener
            val provider = TargetOptionsProvider.getProvider(it)
            viewModel.setTargetTemplate(provider.createBlank(requireContext()))
        }
        setupTextResultListener(REQUEST_KEY_TARGET_TITLE) {
            viewModel.onTargetTitleChanged(it)
        }
        setupTextResultListener(REQUEST_KEY_TARGET_SUBTITLE) {
            viewModel.onTargetSubtitleChanged(it)
        }
        setupIconResultListenerNullable(REQUEST_KEY_TARGET_ICON) {
            viewModel.onTargetIconChanged(it)
        }
        setupTapActionResultListener(REQUEST_KEY_TARGET_TAP_ACTION) {
            viewModel.onTargetTapActionChanged(it)
        }
        setupStringResultListener(REQUEST_KEY_REFRESH_PERIOD) {
            viewModel.onTargetRefreshPeriodChanged(it)
        }
        setupTextResultListener(REQUEST_KEY_TARGET_BUTTON_TEXT) {
            viewModel.onTargetButtonTextChanged(it)
        }
        setupIconResultListener(REQUEST_KEY_TARGET_BUTTON_ICON) {
            viewModel.onTargetButtonIconChanged(it)
        }
        setupTextResultListener(REQUEST_KEY_TARGET_HEAD_TO_HEAD_TITLE) {
            viewModel.onTargetHeadToHeadTitleChanged(it)
        }
        setupTextResultListener(REQUEST_KEY_TARGET_HEAD_TO_HEAD_TEAM_1_TEXT) {
            viewModel.onTargetHeadToHeadTeamOneTextChanged(it)
        }
        setupIconResultListener(REQUEST_KEY_TARGET_HEAD_TO_HEAD_TEAM_1_ICON) {
            viewModel.onTargetHeadToHeadTeamOneIconChanged(it)
        }
        setupTextResultListener(REQUEST_KEY_TARGET_HEAD_TO_HEAD_TEAM_2_TEXT) {
            viewModel.onTargetHeadToHeadTeamTwoTextChanged(it)
        }
        setupIconResultListener(REQUEST_KEY_TARGET_HEAD_TO_HEAD_TEAM_2_ICON) {
            viewModel.onTargetHeadToHeadTeamTwoIconChanged(it)
        }
        setupDoorbellStateResultListener(REQUEST_KEY_TARGET_DOORBELL_STATE) {
            viewModel.onTargetDoorbellStateChanged(it)
        }
        setupIconResultListener(REQUEST_KEY_TARGET_DOORBELL_ICON) {
            viewModel.onTargetDoorbellIconChanged(it)
        }
        setupIconResultListener(REQUEST_KEY_TARGET_DOORBELL_IMAGE) {
            viewModel.onTargetDoorbellImageChanged(it)
        }
        setupImagesResultListener(REQUEST_KEY_TARGET_IMAGES_IMAGES) {
            viewModel.onTargetDoorbellImagesChanged(it)
        }
        setupStringResultListener(REQUEST_KEY_TARGET_DOORBELL_FRAME_DURATION) {
            viewModel.onTargetDoorbellFrameDurationChanged(it)
        }
        setupStringResultListener(REQUEST_KEY_TARGET_DOORBELL_WIDTH) {
            viewModel.onTargetDoorbellWidthChanged(it.takeIfNotBlank())
        }
        setupStringResultListener(REQUEST_KEY_TARGET_DOORBELL_HEIGHT) {
            viewModel.onTargetDoorbellHeightChanged(it.takeIfNotBlank())
        }
        setupStringResultListener(REQUEST_KEY_TARGET_DOORBELL_RATIO_WIDTH) {
            viewModel.onTargetDoorbellRatioWidthChanged(it.takeIfNotBlank())
        }
        setupStringResultListener(REQUEST_KEY_TARGET_DOORBELL_RATIO_HEIGHT) {
            viewModel.onTargetDoorbellRatioHeightChanged(it.takeIfNotBlank())
        }
        setupImagesResultListener(REQUEST_KEY_TARGET_DOORBELL_IMAGES) {
            viewModel.onTargetDoorbellImagesChanged(it)
        }
        setupIconResultListener(REQUEST_KEY_TARGET_IMAGE_IMAGE) {
            viewModel.onTargetImageImageChanged(it)
        }
        setupStringResultListener(REQUEST_KEY_TARGET_IMAGES_ASPECT_RATIO) {
            viewModel.onTargetImagesAspectRatioChanged(it.takeIfNotBlank())
        }
        setupStringResultListener(REQUEST_KEY_TARGET_IMAGES_FRAME_DURATION) {
            viewModel.onTargetImagesFrameDurationChanged(it)
        }
        setupImagesResultListener(REQUEST_KEY_TARGET_IMAGES_IMAGES) {
            viewModel.onTargetImagesImagesChanged(it)
        }
        setupTapActionResultListenerNullable(REQUEST_KEY_TARGET_IMAGES_TAP_ACTION) {
            viewModel.onTargetImagesTapActionChanged(it)
        }
        setupListItemsResultListener(REQUEST_KEY_TARGET_LIST_ITEMS_ITEMS) {
            viewModel.onTargetListItemsItemsChanged(it)
        }
        setupIconResultListener(REQUEST_KEY_TARGET_LIST_ITEMS_ICON) {
            viewModel.onTargetListItemsIconChanged(it)
        }
        setupStringResultListener(REQUEST_KEY_TARGET_LIST_ITEMS_EMPTY_LABEL) {
            viewModel.onTargetListItemsEmptyLabelChanged(it)
        }
        setupIconResultListener(REQUEST_KEY_TARGET_LOYALTY_CARD_ICON) {
            viewModel.onTargetLoyaltyCardIconChanged(it)
        }
        setupStringResultListener(REQUEST_KEY_TARGET_LOYALTY_CARD_WIDTH) {
            viewModel.onTargetLoyaltyCardWidthChanged(it.takeIfNotBlank())
        }
        setupStringResultListener(REQUEST_KEY_TARGET_LOYALTY_CARD_HEIGHT) {
            viewModel.onTargetLoyaltyCardHeightChanged(it.takeIfNotBlank())
        }
        setupTextResultListener(REQUEST_KEY_TARGET_LOYALTY_CARD_PROMPT) {
            viewModel.onTargetLoyaltyCardPromptChanged(it)
        }
        setupCarouselItemResultListener(REQUEST_KEY_TARGET_CAROUSEL_ITEMS) {
            viewModel.onTargetCarouselItemsChanged(it)
        }
        setupTapActionResultListenerNullable(REQUEST_KEY_TARGET_CAROUSEL_TAP_ACTION) {
            viewModel.onTargetCarouselTapActionChanged(it)
        }
        setupExpandedResultListener(REQUEST_KEY_TARGET_EXPANDED_STATE) {
            viewModel.onTargetExpandedStateChanged(it)
        }
        setupStringResultListener(REQUEST_KEY_TARGET_SOURCE_NOTIFICATION_KEY) {
            viewModel.onTargetSourceNotificationKeyChanged(it)
        }
        setupLimitToSurfacesResultListener(REQUEST_KEY_TARGET_LIMIT_TO_SURFACES) {
            viewModel.onTargetLimitToSurfacesChanged(it)
        }
        setupTapActionResultListenerNullable(REQUEST_KEY_TARGET_ABOUT_TAP_ACTION) {
            viewModel.onTargetAboutIntentChanged(it)
        }
        setupTapActionResultListenerNullable(REQUEST_KEY_TARGET_FEEDBACK_TAP_ACTION) {
            viewModel.onTargetFeedbackIntentChanged(it)
        }
        setupIconResultListenerNullable(REQUEST_KEY_TARGET_SUB_ICON) {
            viewModel.onTargetSubComplicationIconChanged(it)
        }
        setupTextResultListener(REQUEST_KEY_TARGET_SUB_CONTENT) {
            viewModel.onTargetSubComplicationContentChanged(it)
        }
        setupTapActionResultListener(REQUEST_KEY_TARGET_SUB_TAP_ACTION) {
            viewModel.onTargetSubComplicationTapActionChanged(it)
        }
        setupLimitToSurfacesResultListener(REQUEST_KEY_TARGET_SUB_LIMIT_TO_SURFACES) {
            viewModel.onTargetSubComplicationLimitToSurfacesChanged(it)
        }
        setupWeatherDataResultListener(REQUEST_KEY_TARGET_SUB_WEATHER_DATA) {
            viewModel.onTargetSubComplicationWeatherDataChanged(it)
        }
    }

    private fun setupState() {
        handleState(viewModel.state.value)
        whenResumed {
            viewModel.state.collect {
                handleState(it)
            }
        }
    }

    private fun setupBlocker() = with(binding.targetConfigurationSmartspaceBlocker) {
        whenResumed {
            onClicked().collect {
                if(client.checkCallingPermission() == false){
                    val intentSender = client.createPermissionRequestIntentSender()
                        ?: return@collect
                    permissionResult.launch(IntentSenderRequest.Builder(intentSender).build())
                }
            }
        }
    }

    private fun handleState(state: State) {
        when(state) {
            is State.Loading -> {
                binding.targetConfigurationAppBar.setDraggable(false)
                binding.targetConfigurationAppBar.setExpanded(false, false)
                binding.targetConfigurationLoading.root.isVisible = true
                binding.targetConfigurationRecyclerview.isVisible = false
            }
            is State.SelectTarget -> {
                binding.targetConfigurationAppBar.setDraggable(false)
                binding.targetConfigurationAppBar.setExpanded(false, false)
                binding.targetConfigurationLoading.root.isVisible = false
                binding.targetConfigurationRecyclerview.isVisible = true
                contentAdapter.update(loadItems(), binding.targetConfigurationRecyclerview)
            }
            is State.Target -> {
                binding.targetConfigurationAppBar.setDraggable(true)
                binding.targetConfigurationAppBar.setExpanded(
                    !getRememberedAppBarCollapsed(FRAGMENT_ARGUMENTS_APP_BAR_COLLAPSED)
                )
                binding.targetConfigurationLoading.root.isVisible = false
                binding.targetConfigurationRecyclerview.isVisible = true
                contentAdapter.update(state.loadItems(), binding.targetConfigurationRecyclerview)
                updatePreview(state.preview)
            }
        }
    }

    private fun loadItems(): List<BaseSettingsItem> {
        return listOf(
            Setting(
                getString(R.string.configuration_target_select_target_title),
                getString(R.string.configuration_target_select_target_content),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_tasker),
                onClick = viewModel::onSelectTargetClicked
            )
        )
    }

    private fun State.Target.loadItems(): List<BaseSettingsItem> {
        val provider = TargetOptionsProvider.getProviderForTemplate(template)
        val options = provider.getOptionsWithCast(
            requireContext(),
            template,
            this@TargetConfigurationFragment,
            dateTimeFormatter,
            refreshPeriod,
            refreshIfNotVisible
        )
        return listOfNotNull(
            Card(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_warning),
                getString(R.string.configuration_target_hidden)
            ).takeIf { target?.isVisible == false },
            Setting(
                getString(R.string.configuration_target_select_target_title),
                target?.name ?: "",
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_tasker),
                onClick = viewModel::onSelectTargetClicked
            )
        ) + options
    }

    private fun updatePreview(
        template: TargetTemplate
    ) = whenResumed {
        with(binding.targetConfigurationSmartspace) {
            val target = if(client.checkCallingPermission() == true) {
                template.toDemoTarget(requireContext())
            }else{
                getPermissionTarget()
            }
            this.onSmartspaceTargetsUpdate(listOf(target))
        }
    }

    private fun getPermissionTarget(): SmartspaceTarget {
        return SmartspacerTargetTemplate.Basic(
            TARGET_ID_PREVIEW,
            ComponentName(requireContext(), TargetConfigurationFragment::class.java),
            SmartspaceTarget.FEATURE_UNDEFINED,
            Text(getString(R.string.configuration_preview_permission_title)),
            Text(getString(R.string.configuration_preview_permission_content)),
            SmartspacerIcon(AndroidIcon.createWithResource(requireContext(), R.drawable.ic_tasker)),
            SmartspacerTapAction()
        ).create()
    }

    private fun createNewTargetTemplate(): TargetTemplate {
        return TargetOptionsProvider.getProvider<TargetTemplate.Basic>()
            .createBlank(requireContext())
    }

    override fun onTargetTemplateClicked() = viewModel.onTargetTemplateClicked()
    override fun onTargetTitleClicked() = viewModel.onTargetTitleClicked()
    override fun onTargetSubtitleClicked() = viewModel.onTargetSubtitleClicked()
    override fun onTargetIconClicked() = viewModel.onTargetIconClicked()
    override fun onTargetTapActionClicked() = viewModel.onTargetTapActionClicked()
    override fun onTargetRefreshPeriodClicked() = viewModel.onTargetRefreshPeriodClicked()
    override fun onTargetRefreshIfNotVisibleChanged(enabled: Boolean) =
        viewModel.onTargetRefreshWhenNotVisibleChanged(enabled)

    override fun onTargetSubComplicationEnabledChanged(enabled: Boolean) =
        viewModel.onTargetSubComplicationEnabledChanged(requireContext(), enabled)

    override fun onButtonTitleClicked() = viewModel.onTargetButtonTextClicked()
    override fun onButtonIconClicked() = viewModel.onTargetButtonIconClicked()

    override fun onCarouselItemsClicked() = viewModel.onTargetCarouselItemsClicked()
    override fun onCarouselTapActionClicked() = viewModel.onTargetCarouselTapActionClicked()

    override fun onDoorbellStateClicked() = viewModel.onTargetDoorbellStateClicked()
    override fun onDoorbellWidthClicked() = viewModel.onTargetDoorbellWidthClicked()
    override fun onDoorbellHeightClicked() = viewModel.onTargetDoorbellHeightClicked()
    override fun onDoorbellRatioWidthClicked() = viewModel.onTargetDoorbellRatioWidthClicked()
    override fun onDoorbellRatioHeightClicked() = viewModel.onTargetDoorbellRatioHeightClicked()
    override fun onDoorbellIconClicked() = viewModel.onTargetDoorbellIconClicked()
    override fun onDoorbellShowLoadingBarChanged(enabled: Boolean) =
        viewModel.onTargetDoorbellShowLoadingBarChanged(enabled)
    override fun onDoorbellImageBitmapClicked() = viewModel.onTargetDoorbellImageClicked()
    override fun onDoorbellImageScaleTypeChanged(scaleType: ImageView.ScaleType) =
        viewModel.onTargetDoorbellImageScaleTypeChanged(scaleType)
    override fun onDoorbellImagesClicked() = viewModel.onTargetDoorbellImagesClicked()
    override fun onDoorbellFrameDurationClicked() = viewModel.onTargetDoorbellFrameDurationClicked()

    override fun onHeadToHeadTitleClicked() = viewModel.onTargetHeadToHeadTitleClicked()
    override fun onHeadToHeadFirstTeamNameClicked() = viewModel.onTargetHeadToHeadTeamOneTextClicked()
    override fun onHeadToHeadFirstTeamIconClicked() = viewModel.onTargetHeadToHeadTeamOneIconClicked()
    override fun onHeadToHeadSecondTeamNameClicked() = viewModel.onTargetHeadToHeadTeamTwoTextClicked()
    override fun onHeadToHeadSecondTeamIconClicked() = viewModel.onTargetHeadToHeadTeamTwoIconClicked()

    override fun onImageImageClicked() = viewModel.onTargetImageImageClicked()
    override fun onImagesImagesClicked() = viewModel.onTargetImagesImagesClicked()
    override fun onImagesTapActionClicked() = viewModel.onTargetImagesTapActionClicked()
    override fun onImagesDimensionRatioClicked() = viewModel.onTargetImagesAspectRatioClicked()
    override fun onImagesFrameDurationClicked() = viewModel.onTargetImagesFrameDurationClicked()

    override fun onListItemsItemsClicked() = viewModel.onTargetListItemsItemsClicked()
    override fun onListItemsIconClicked() = viewModel.onTargetListItemsIconClicked()
    override fun onListItemsEmptyMessageClicked() =
        viewModel.onTargetListItemsEmptyLabelClicked()

    override fun onLoyaltyCardIconClicked() = viewModel.onTargetLoyaltyCardIconClicked()
    override fun onLoyaltyCardPromptClicked() = viewModel.onTargetLoyaltyCardPromptClicked()
    override fun onLoyaltyCardScaleTypeChanged(scaleType: ImageView.ScaleType) =
        viewModel.onTargetLoyaltyCardScaleTypeChanged(scaleType)
    override fun onLoyaltyCardWidthClicked() = viewModel.onTargetLoyaltyCardWidthClicked()
    override fun onLoyaltyCardHeightClicked() = viewModel.onTargetLoyaltyCardHeightClicked()

    override fun onTargetExpandedStateClicked() = viewModel.onTargetExpandedStateClicked()
    override fun onTargetSourceNotificationKeyClicked() =
        viewModel.onTargetSourceNotificationKeyClicked()
    override fun onTargetAllowDismissChanged(enabled: Boolean) =
        viewModel.onTargetAllowDismissChanged(enabled)
    override fun onTargetCanTakeTwoComplicationsChanged(enabled: Boolean) =
        viewModel.onTargetCanTakeTwoComplicationsChanged(enabled)
    override fun onTargetHideIfNoComplicationsChanged(enabled: Boolean) =
        viewModel.onTargetHideIfNoComplicationsChanged(enabled)
    override fun onTargetLimitToSurfacesClicked() = viewModel.onTargetLimitToSurfacesClicked()
    override fun onTargetAboutIntentClicked() = viewModel.onTargetAboutIntentClicked()
    override fun onTargetFeedbackIntentClicked() = viewModel.onTargetFeedbackIntentClicked()
    override fun onTargetHideTitleOnAodChanged(enabled: Boolean) =
        viewModel.onTargetHideTitleOnAodChanged(enabled)
    override fun onTargetHideSubtitleOnAodChanged(enabled: Boolean) =
        viewModel.onTargetHideSubtitleOnAodChanged(enabled)

    override fun onComplicationIconClicked() = viewModel.onTargetSubComplicationIconClicked()
    override fun onComplicationContentClicked() = viewModel.onTargetSubComplicationContentClicked()
    override fun onComplicationTapActionClicked() =
        viewModel.onTargetSubComplicationTapActionClicked()
    override fun onComplicationLimitToSurfacesClicked() =
        viewModel.onTargetSubComplicationLimitToSurfacesClicked()
    override fun onComplicationWeatherDataClicked() =
        viewModel.onTargetSubComplicationWeatherDataClicked()
    override fun onComplicationRefreshPeriodClicked() = viewModel.onTargetRefreshPeriodClicked()
    override fun onComplicationRefreshIfNotVisibleChanged(enabled: Boolean) =
        viewModel.onTargetRefreshWhenNotVisibleChanged(enabled)

    inner class SettingsAdapter: BaseSettingsAdapter(binding.targetConfigurationRecyclerview, emptyList())

}