package com.kieronquinn.app.smartspacer.plugin.googlehealth.ui.screens.focus

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import com.kieronquinn.app.smartspacer.plugin.googlehealth.R
import com.kieronquinn.app.smartspacer.plugin.googlehealth.model.HealthMetric
import com.kieronquinn.app.smartspacer.plugin.googlehealth.ui.screens.focus.HealthConfigurationFocusViewModel.State
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.whenResumed
import com.kieronquinn.app.smartspacer.sdk.utils.getSerializableCompat
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class HealthConfigurationFocusFragment: BaseSettingsFragment(), BackAvailable {

    companion object {
        private const val REQUEST_KEY = "minimum_trend"
        private const val KEY_RESULT = "result"

        @SuppressLint("RestrictedApi")
        fun Fragment.setupMetricListener(callback: (result: HealthMetric) -> Unit) {
            setFragmentResultListener(REQUEST_KEY) { requestKey, bundle ->
                if(requestKey != REQUEST_KEY) return@setFragmentResultListener
                val result = bundle.getSerializableCompat(KEY_RESULT, HealthMetric::class.java)
                    ?: return@setFragmentResultListener
                callback.invoke(result)
            }
        }
    }

    override val additionalPadding by lazy {
        resources.getDimension(SharedR.dimen.margin_8)
    }

    override val adapter by lazy {
        Adapter()
    }
    
    private val viewModel by viewModel<HealthConfigurationFocusViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupState()
    }

    private fun setupState() {
        handleState(viewModel.state.value)
        whenResumed {
            viewModel.state.collect {
                handleState(it)
            }
        }
    }

    private fun handleState(state: State) {
        when(state) {
            is State.Loading -> {
                binding.settingsBaseLoading.isVisible = true
                binding.settingsBaseRecyclerView.isVisible = false
            }
            is State.Loaded -> {
                binding.settingsBaseLoading.isVisible = false
                binding.settingsBaseRecyclerView.isVisible = true
                adapter.update(state.loadItems(), binding.settingsBaseRecyclerView)
            }
        }
    }

    private fun State.Loaded.loadItems(): List<BaseSettingsItem> {
        return items.map {
            GenericSettingsItem.Setting(
                title = getString(it.metric.label),
                subtitle = if (!it.available) {
                    getString(R.string.configuration_focus_not_available)
                } else "",
                isEnabled = it.available,
                icon = ContextCompat.getDrawable(requireContext(), it.metric.iconFilled),
            ) {
                dismissWithResult(it.metric)
            }
        }
    }

    private fun dismissWithResult(result: HealthMetric) {
        setFragmentResult(REQUEST_KEY, bundleOf(KEY_RESULT to result))
        viewModel.dismiss()
    }

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())
    
}