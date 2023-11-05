package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.complication

import android.content.ComponentName
import android.graphics.drawable.Icon
import android.os.Bundle
import android.view.View
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.core.app.ActivityOptionsCompat
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
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.allowBackground
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.applyBottomNavigationInset
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.collapsedState
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.expandProgress
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.getRememberedAppBarCollapsed
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.rememberAppBarCollapsed
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugin.shared.utils.onClicked
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.actions.UpdateComplicationAction
import com.kieronquinn.app.smartspacer.plugin.tasker.databinding.FragmentComplicationConfigurationBinding
import com.kieronquinn.app.smartspacer.plugin.tasker.model.ComplicationTemplate
import com.kieronquinn.app.smartspacer.plugin.tasker.model.SmartspacerComplicationUpdateTaskerInput
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.activities.ComplicationConfigurationActivity
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.complication.ComplicationConfigurationViewModel.State
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.complication.options.Basic.BasicOptionsListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.complication.options.ComplicationOptionsProvider
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.complication.picker.ComplicationPickerFragment.Companion.setupComplicationPickerListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.TargetConfigurationFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.icon.IconPickerFragment.Companion.setupIconResultListenerNullable
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.limittosurfaces.LimitToSurfacesFragment.Companion.setupLimitToSurfacesResultListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.string.StringInputFragment.Companion.setupStringResultListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.tapaction.TapActionFragment.Companion.setupTapActionResultListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.text.TextInputFragment.Companion.setupTextResultListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.weatherdata.WeatherDataFragment.Companion.setupWeatherDataResultListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.views.DemoBcSmartspaceView.Companion.TARGET_ID_PREVIEW
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.views.DemoBcSmartspaceView.Companion.toDemoComplication
import com.kieronquinn.app.smartspacer.sdk.client.SmartspacerClient
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate
import com.kieronquinn.monetcompat.extensions.views.applyMonet
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon as SmartspacerIcon

class ComplicationConfigurationFragment: BoundFragment<FragmentComplicationConfigurationBinding>(
    FragmentComplicationConfigurationBinding::inflate
), BackAvailable, ProvidesBack, LockCollapsed, BasicOptionsListener {

    companion object {
        private const val FRAGMENT_ARGUMENTS_APP_BAR_COLLAPSED = "app_bar_collapsed_inner"

        const val REQUEST_KEY_COMPLICATION_ICON = "complication_icon"
        const val REQUEST_KEY_COMPLICATION_CONTENT = "complication_content"
        const val REQUEST_KEY_COMPLICATION_TAP_ACTION = "complication_tap_action"
        const val REQUEST_KEY_COMPLICATION_LIMIT_TO_SURFACES = "complication_limit_to_surfaces"
        const val REQUEST_KEY_COMPLICATION_WEATHER_DATA = "complication_weather_data"
        const val REQUEST_KEY_REFRESH_PERIOD = "complication_refresh_period"
    }

    private val viewModel by viewModel<ComplicationConfigurationViewModel>()
    private val gson by inject<Gson>()

    private val permissionResult = registerForActivityResult(StartIntentSenderForResult()) {
        val current = (viewModel.state.value as? State.Complication)?.preview
            ?: return@registerForActivityResult
        updatePreview(current)
    }

    private val configurationActivity by lazy {
        requireActivity() as ComplicationConfigurationActivity
    }

    private val client by lazy {
        SmartspacerClient.getInstance(requireContext())
    }

    private val taskerAction by lazy {
        UpdateComplicationAction(configurationActivity)
    }

    private val contentAdapter by lazy {
        SettingsAdapter()
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
        viewModel.setInitialSmartspacerId(configurationActivity.taskerInput?.smartspacerId)
        viewModel.setup(configurationActivity.taskerInput, createNewComplicationTemplate())
    }

    override fun onBackPressed(): Boolean {
        val complication = (viewModel.state.value as? State.Complication) ?: return false
        val databaseComplication = complication.complication ?: return false
        val variables = complication.template.getVariables().map {
            TaskerInputInfo(it, it, null, true, it)
        }.let {
            TaskerInputInfos().apply {
                addAll(it)
            }
        }
        val templateJson = gson.toJson(complication.template)
        configurationActivity.setDynamicInputs(variables)
        configurationActivity.setStaticInputs(
            SmartspacerComplicationUpdateTaskerInput(
                databaseComplication.smartspacerId,
                databaseComplication.name,
                templateJson,
                complication.refreshPeriod,
                complication.refreshIfNotVisible
            )
        )
        taskerAction.finishForTasker()
        return true
    }

    private fun setupPreview() = with(binding.complicationConfigurationSmartspace) {
        whenResumed {
            binding.complicationConfigurationAppBar.expandProgress().collect {
                alpha = maxOf((it - 0.6666f) * 3f, 0f)
            }
        }
    }

    private fun setupContent() = with(binding.complicationConfigurationRecyclerview) {
        layoutManager = LinearLayoutManager(context)
        adapter = contentAdapter
        applyBottomNavigationInset(resources.getDimension(SharedR.dimen.margin_16))
    }

    private fun setupMonet() = with(binding.complicationConfigurationLoading){
        loadingProgress.applyMonet()
    }

    private fun setupCollapsedState() = whenResumed {
        binding.complicationConfigurationAppBar.collapsedState().collect {
            if(binding.complicationConfigurationAppBar.isDraggable()) {
                rememberAppBarCollapsed(it, FRAGMENT_ARGUMENTS_APP_BAR_COLLAPSED)
            }
        }
    }

    private fun setupCard() = with(binding.complicationConfigurationCardView) {
        setCardBackgroundColor(monet.getBackgroundColor(context))
        val roundedCornerSize = resources.getDimension(SharedR.dimen.margin_16)
        whenResumed {
            binding.complicationConfigurationAppBar.expandProgress().collect {
                shapeAppearanceModel = shapeAppearanceModel.toBuilder()
                    .setTopLeftCorner(CornerFamily.ROUNDED, roundedCornerSize * it)
                    .setTopRightCorner(CornerFamily.ROUNDED, roundedCornerSize * it)
                    .build()
            }
        }
    }

    private fun setupListeners() {
        setupComplicationPickerListener { viewModel.setSmartspacerId(it) }
        setupIconResultListenerNullable(REQUEST_KEY_COMPLICATION_ICON) {
            viewModel.onComplicationIconChanged(it)
        }
        setupTextResultListener(REQUEST_KEY_COMPLICATION_CONTENT) {
            viewModel.onComplicationContentChanged(it)
        }
        setupTapActionResultListener(REQUEST_KEY_COMPLICATION_TAP_ACTION) {
            viewModel.onComplicationTapActionChanged(it)
        }
        setupLimitToSurfacesResultListener(REQUEST_KEY_COMPLICATION_LIMIT_TO_SURFACES) {
            viewModel.onComplicationLimitToSurfacesChanged(it)
        }
        setupWeatherDataResultListener(REQUEST_KEY_COMPLICATION_WEATHER_DATA) {
            viewModel.onComplicationWeatherDataChanged(it)
        }
        setupStringResultListener(REQUEST_KEY_REFRESH_PERIOD) {
            viewModel.onComplicationRefreshPeriodChanged(it)
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

    private fun setupBlocker() = with(binding.complicationConfigurationSmartspaceBlocker) {
        whenResumed {
            onClicked().collect {
                if(client.checkCallingPermission() == false){
                    val intentSender = client.createPermissionRequestIntentSender()
                        ?: return@collect
                    permissionResult.launch(
                        IntentSenderRequest.Builder(intentSender).build(),
                        ActivityOptionsCompat.makeBasic().allowBackground()
                    )
                }
            }
        }
    }

    private fun handleState(state: State) {
        when(state) {
            is State.Loading -> {
                binding.complicationConfigurationAppBar.setDraggable(false)
                binding.complicationConfigurationAppBar.setExpanded(false, false)
                binding.complicationConfigurationLoading.root.isVisible = true
                binding.complicationConfigurationRecyclerview.isVisible = false
            }
            is State.SelectComplication -> {
                binding.complicationConfigurationAppBar.setDraggable(false)
                binding.complicationConfigurationAppBar.setExpanded(false, false)
                binding.complicationConfigurationLoading.root.isVisible = false
                binding.complicationConfigurationRecyclerview.isVisible = true
                contentAdapter.update(loadItems(), binding.complicationConfigurationRecyclerview)
            }
            is State.Complication -> {
                binding.complicationConfigurationAppBar.setDraggable(true)
                binding.complicationConfigurationAppBar.setExpanded(
                    !getRememberedAppBarCollapsed(FRAGMENT_ARGUMENTS_APP_BAR_COLLAPSED)
                )
                binding.complicationConfigurationLoading.root.isVisible = false
                binding.complicationConfigurationRecyclerview.isVisible = true
                contentAdapter.update(state.loadItems(), binding.complicationConfigurationRecyclerview)
                updatePreview(state.preview)
            }
        }
    }

    private fun loadItems(): List<BaseSettingsItem> {
        return listOf(
            Setting(
                getString(R.string.configuration_complication_select_complication_title),
                getString(R.string.configuration_complication_select_complication_content),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_tasker),
                onClick = viewModel::onSelectComplicationClicked
            )
        )
    }

    private fun State.Complication.loadItems(): List<BaseSettingsItem> {
        val provider = ComplicationOptionsProvider.getProviderForTemplate(template)
        val options = provider.getOptionsWithCast(
            requireContext(),
            template,
            this@ComplicationConfigurationFragment,
            refreshPeriod,
            refreshIfNotVisible
        )
        return listOfNotNull(
            Card(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_warning),
                getString(R.string.configuration_complication_hidden)
            ).takeIf { complication?.isVisible == false },
            Setting(
                getString(R.string.configuration_complication_select_complication_title),
                complication?.name ?: "",
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_tasker),
                onClick = viewModel::onSelectComplicationClicked
            )
        ) + options
    }

    private fun updatePreview(
        template: ComplicationTemplate
    ) = whenResumed {
        with(binding.complicationConfigurationSmartspace) {
            val complication = if(client.checkCallingPermission() == true) {
                template.toDemoComplication(requireContext())
            }else{
                getPermissionTarget()
            }
            this.onSmartspaceTargetsUpdate(listOf(complication))
        }
    }

    private fun getPermissionTarget(): SmartspaceTarget {
        return TargetTemplate.Basic(
            TARGET_ID_PREVIEW,
            ComponentName(requireContext(), TargetConfigurationFragment::class.java),
            SmartspaceTarget.FEATURE_UNDEFINED,
            Text(getString(R.string.configuration_preview_permission_title)),
            Text(getString(R.string.configuration_preview_permission_content)),
            SmartspacerIcon(Icon.createWithResource(requireContext(), R.drawable.ic_tasker)),
            TapAction()
        ).create()
    }

    private fun createNewComplicationTemplate(): ComplicationTemplate {
        return ComplicationOptionsProvider.getProvider<ComplicationTemplate.Basic>()
            .createBlank(requireContext())
    }

    override fun onComplicationIconClicked() = viewModel.onComplicationIconClicked()
    override fun onComplicationContentClicked() = viewModel.onComplicationContentClicked()
    override fun onComplicationTapActionClicked() = viewModel.onComplicationTapActionClicked()
    override fun onComplicationLimitToSurfacesClicked() =
        viewModel.onComplicationLimitToSurfacesClicked()
    override fun onComplicationWeatherDataClicked() = viewModel.onComplicationWeatherDataClicked()
    override fun onComplicationRefreshPeriodClicked() =
        viewModel.onComplicationRefreshPeriodClicked()
    override fun onComplicationRefreshIfNotVisibleChanged(enabled: Boolean) =
        viewModel.onComplicationRefreshWhenNotVisibleChanged(enabled)

    inner class SettingsAdapter: BaseSettingsAdapter(binding.complicationConfigurationRecyclerview, emptyList())

}