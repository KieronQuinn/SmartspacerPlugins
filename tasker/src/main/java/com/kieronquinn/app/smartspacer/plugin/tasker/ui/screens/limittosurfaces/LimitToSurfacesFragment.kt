package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.limittosurfaces

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.navArgs
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.SwitchSetting
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.ProvidesBack
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.limittosurfaces.LimitToSurfacesViewModel.State
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions.UiSurface_validSurfaces
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions.describe
import com.kieronquinn.app.smartspacer.sdk.model.UiSurface
import com.kieronquinn.app.smartspacer.sdk.utils.getParcelableArrayListCompat
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class LimitToSurfacesFragment: BaseSettingsFragment(), BackAvailable, ProvidesBack {

    companion object {
        private const val KEY_RESULT = "result"

        fun Fragment.setupLimitToSurfacesResultListener(
            key: String, callback: (result: Set<UiSurface>) -> Unit
        ) {
            setFragmentResultListener(key) { requestKey, bundle ->
                if(requestKey != key) return@setFragmentResultListener
                val result = bundle.getParcelableArrayListCompat(
                    KEY_RESULT, SurfaceWrapper::class.java
                )?.map { it.surface }?.toSet() ?: return@setFragmentResultListener
                callback.invoke(result)
            }
        }
    }

    private val viewModel by viewModel<LimitToSurfacesViewModel>()
    private val args by navArgs<LimitToSurfacesFragmentArgs>()

    private val config by lazy {
        args.config as Config
    }

    override val adapter by lazy {
        Adapter()
    }

    override val additionalPadding by lazy {
        resources.getDimension(SharedR.dimen.margin_8)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupState()
        viewModel.setup(config.current)
    }

    override fun onBackPressed(): Boolean {
        val current = (viewModel.state.value as? State.Loaded)?.limitToSurfaces
            ?.ifEmpty { UiSurface_validSurfaces() }
        if(current != null) {
            setFragmentResult(config.key, bundleOf(
                KEY_RESULT to current.map { SurfaceWrapper(it) }
            ))
        }
        viewModel.dismiss()
        return true
    }

    private fun setupState() {
        handleState(viewModel.state.value)
        whenResumed {
            viewModel.state.collect {
                handleState(it)
            }
        }
    }

    private fun handleState(state: State) = with(binding) {
        when(state) {
            is State.Loading -> {
                settingsBaseLoading.isVisible = true
                settingsBaseRecyclerView.isVisible = false
            }
            is State.Loaded -> {
                settingsBaseLoading.isVisible = false
                settingsBaseRecyclerView.isVisible = true
                adapter.update(state.loadItems(), settingsBaseRecyclerView)
            }
        }
    }

    private fun State.Loaded.loadItems(): List<BaseSettingsItem> {
        val items = UiSurface_validSurfaces().map { surface ->
            SwitchSetting(
                limitToSurfaces.contains(surface),
                surface.describe(requireContext()) ?: "",
                "",
                icon = null
            ) {
                viewModel.onSurfaceChanged(surface, it)
            }
        }.sortedBy { it.title.toString().lowercase() }
        return listOf(
            GenericSettingsItem.Card(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_info),
                getText(R.string.configuration_limit_to_surfaces_info)
            )
        ) + items
    }

    @Parcelize
    data class Config(
        val key: String,
        val current: List<UiSurface>
    ): Parcelable

    @Parcelize
    data class SurfaceWrapper(val surface: UiSurface): Parcelable

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}