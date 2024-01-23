package com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings

import com.google.android.material.progressindicator.LinearProgressIndicator
import com.kieronquinn.app.shared.databinding.FragmentSettingsBaseBinding
import com.kieronquinn.app.smartspacer.plugin.shared.ui.views.LifecycleAwareRecyclerView

abstract class BaseSettingsFragment: BaseFragment<FragmentSettingsBaseBinding>(FragmentSettingsBaseBinding::inflate) {

    override val recyclerView: LifecycleAwareRecyclerView
        get() = binding.settingsBaseRecyclerView

    override val loadingView: LinearProgressIndicator
        get() = binding.settingsBaseLoadingProgress

}