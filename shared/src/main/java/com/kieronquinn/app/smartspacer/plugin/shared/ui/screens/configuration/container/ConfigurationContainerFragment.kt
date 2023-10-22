package com.kieronquinn.app.smartspacer.plugin.shared.ui.screens.configuration.container

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kieronquinn.app.shared.R
import com.kieronquinn.app.shared.databinding.FragmentConfigurationBinding
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BaseContainerFragment
import org.koin.android.ext.android.inject

class ConfigurationContainerFragment: BaseContainerFragment<FragmentConfigurationBinding>(FragmentConfigurationBinding::inflate) {

    private val navGraphRepository by inject<NavGraphRepository>()

    override val navigation by inject<ContainerNavigation>()
    override val bottomNavigation: BottomNavigationView? = null

    override val appBar by lazy {
        binding.configurationContainerAppBar
    }

    override val toolbar by lazy {
        binding.configurationContainerToolbar
    }

    override val collapsingToolbar by lazy {
        binding.configurationContainerCollapsingToolbar
    }

    override val navHostFragment by lazy {
        childFragmentManager.findFragmentById(R.id.nav_host_fragment_configuration) as NavHostFragment
    }

    override val fragment by lazy {
        binding.navHostFragmentConfiguration
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navGraph  = getNavGraphMapping()
            ?: throw RuntimeException("No Nav Graph specified for ConfigurationActivity")
        val navController = navHostFragment.navController
        navController.graph = navController.navInflater.inflate(navGraph.graph)
    }

    private fun getNavGraphMapping(): NavGraphRepository.NavGraphMapping? {
        return navGraphRepository.getNavGraph(
            requireActivity().intent.component?.shortClassName ?: return null
        )
    }

}