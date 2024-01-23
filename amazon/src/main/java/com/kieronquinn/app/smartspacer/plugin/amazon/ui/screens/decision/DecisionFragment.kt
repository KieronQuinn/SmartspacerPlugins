package com.kieronquinn.app.smartspacer.plugin.amazon.ui.screens.decision

import android.os.Bundle
import android.view.View
import com.kieronquinn.app.smartspacer.plugin.amazon.repositories.AmazonSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import org.koin.android.ext.android.inject

class DecisionFragment: BaseSettingsFragment() {

    private val navigation by inject<ContainerNavigation>()
    private val settings by inject<AmazonSettingsRepository>()

    override val adapter by lazy {
        Adapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        whenResumed {
            if(settings.domain.exists()) {
                navigation.navigate(
                    DecisionFragmentDirections.actionDecisionFragmentToPackagesFragment()
                )
            }else{
                navigation.navigate(
                    DecisionFragmentDirections.actionDecisionFragmentToDomainPickerFragment(true)
                )
            }
        }
    }

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}