package com.kieronquinn.app.smartspacer.plugin.amazon.ui.screens.configuration

import android.app.Activity
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.kieronquinn.app.smartspacer.plugin.amazon.R
import com.kieronquinn.app.smartspacer.plugin.amazon.databinding.FragmentConfigurationTargetAmazonBinding
import com.kieronquinn.app.smartspacer.plugin.amazon.model.AmazonDomain
import com.kieronquinn.app.smartspacer.plugin.amazon.model.database.AmazonDelivery.Status
import com.kieronquinn.app.smartspacer.plugin.amazon.ui.activities.ConfigurationActivity
import com.kieronquinn.app.smartspacer.plugin.amazon.ui.screens.configuration.AmazonTargetConfigurationViewModel.State
import com.kieronquinn.app.smartspacer.plugin.amazon.ui.screens.configuration.signin.AmazonTargetConfigurationSignInFragment.Companion.registerSignInReceiver
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BoundFragment
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.ProvidesOverflow
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.isDarkMode
import com.kieronquinn.app.smartspacer.plugin.shared.utils.onClicked
import com.kieronquinn.app.smartspacer.plugin.shared.utils.whenResumed
import com.kieronquinn.monetcompat.extensions.views.applyMonet
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class AmazonTargetConfigurationFragment: BoundFragment<FragmentConfigurationTargetAmazonBinding>(FragmentConfigurationTargetAmazonBinding::inflate), BackAvailable, ProvidesOverflow {

    override val backIcon = SharedR.drawable.ic_close

    private val viewModel by viewModel<AmazonTargetConfigurationViewModel>()

    private val adapter by lazy {
        Adapter()
    }

    private val isSettings by lazy {
        ConfigurationActivity.getIsSettings(requireActivity())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListener()
        setupMonet()
        setupRecyclerView()
        setupButton()
        setupState()
    }

    override fun inflateMenu(menuInflater: MenuInflater, menu: Menu) {
        menuInflater.inflate(R.menu.menu_configuration, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when(menuItem.itemId) {
            R.id.menu_configuration_sign_out -> viewModel.signOut()
        }
        return true
    }

    private fun setupListener() {
        registerSignInReceiver {
            viewModel.onReloadClicked()
        }
    }

    private fun setupMonet() {
        val background = monet.getPrimaryColor(requireContext(), !requireContext().isDarkMode)
        binding.configurationAmazonSignInCard.backgroundTintList = ColorStateList.valueOf(background)
        binding.configurationUnsupportedCard.backgroundTintList = ColorStateList.valueOf(background)
        binding.configurationAmazonSignInLoading.loadingProgress.applyMonet()
    }

    private fun setupRecyclerView() = with(binding.configurationAmazonRecyclerView) {
        layoutManager = LinearLayoutManager(context)
        adapter = this@AmazonTargetConfigurationFragment.adapter
    }

    private fun setupButton() = with(binding.configurationAmazonSignInButton) {
        whenResumed {
            onClicked().collect {
                viewModel.onSignInClicked()
            }
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

    private fun handleState(state: State) {
        when(state){
            is State.Loading -> {
                binding.configurationAmazonSignInLoading.root.isVisible = true
                binding.configurationAmazonSignInSignIn.isVisible = false
                binding.configurationAmazonRecyclerView.isVisible = false
                binding.configurationAmazonUnsupported.isVisible = false
            }
            is State.SignIn -> {
                binding.configurationAmazonSignInLoading.root.isVisible = false
                binding.configurationAmazonSignInSignIn.isVisible = true
                binding.configurationAmazonRecyclerView.isVisible = false
                binding.configurationAmazonUnsupported.isVisible = false
            }
            is State.Unsupported -> {
                binding.configurationAmazonSignInLoading.root.isVisible = false
                binding.configurationAmazonSignInSignIn.isVisible = false
                binding.configurationAmazonRecyclerView.isVisible = false
                binding.configurationAmazonUnsupported.isVisible = true
            }
            is State.SignedIn -> {
                binding.configurationAmazonSignInLoading.root.isVisible = false
                binding.configurationAmazonSignInSignIn.isVisible = false
                binding.configurationAmazonRecyclerView.isVisible = true
                binding.configurationAmazonUnsupported.isVisible = false
                requireActivity().setResult(Activity.RESULT_OK)
                if(isSettings){
                    adapter.update(state.loadItems(), binding.configurationAmazonRecyclerView)
                }else {
                    requireActivity().finish()
                }
            }
        }
    }

    private fun State.SignedIn.loadItems(): List<BaseSettingsItem> {
        val advanced = if(showAdvanced) {
            val trackableOrders = deliveries.filter {
                it.getBestStatus() != Status.DELIVERED && it.trackingData != null
            }.size
            listOf(
                GenericSettingsItem.Header(getString(R.string.target_amazon_settings_advanced)),
                GenericSettingsItem.Dropdown(
                    getString(R.string.target_amazon_settings_domain_title),
                    if(domain?.domainName?.isNotBlank() == true){
                        getString(
                            R.string.target_amazon_settings_domain_content,
                            getString(domain.nameRes)
                        )
                    }else{
                        getString(R.string.target_amazon_settings_domain_content_unknown)
                    },
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_settings_domain),
                    domain ?: AmazonDomain.UNKNOWN,
                    viewModel::onDomainChanged,
                    AmazonDomain.values().filterNot { it == AmazonDomain.UNKNOWN }
                ) {
                    it.nameRes
                },
                GenericSettingsItem.Setting(
                    getString(R.string.target_amazon_settings_known_orders_title),
                    getString(R.string.target_amazon_settings_known_orders_content, deliveries.size),
                    icon = null,
                    isEnabled = deliveries.isNotEmpty(),
                    onClick = viewModel::onKnownOrdersClicked
                ),
                GenericSettingsItem.Setting(
                    getString(R.string.target_amazon_settings_trackable_orders_title),
                    getString(
                        R.string.target_amazon_settings_trackable_orders_content,
                        trackableOrders
                    ),
                    icon = null,
                    isEnabled = trackableOrders > 0,
                    onClick = viewModel::onTrackableOrdersClicked
                ),
                GenericSettingsItem.Setting(
                    getString(R.string.target_amazon_settings_tracking_supported_title),
                    if(domain?.trackingDomain != null){
                        getString(
                            R.string.target_amazon_settings_tracking_supported_content_yes,
                            domain.trackingDomain.name
                        )
                    }else{
                        getString(R.string.target_amazon_settings_tracking_supported_content_no)
                    },
                    icon = null,
                    isEnabled = false
                ) {}
            )
        }else emptyList()
        return listOf(
            GenericSettingsItem.SwitchSetting(
                showProductImage,
                getString(R.string.target_amazon_settings_show_image_title),
                getString(R.string.target_amazon_settings_show_image_content),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_settings_show_image),
                onChanged = viewModel::onShowProductImageChanged
            ),
            GenericSettingsItem.Setting(
                getString(R.string.target_amazon_settings_clear_dismissed_title),
                getText(R.string.target_amazon_settings_clear_dismissed_content),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_settings_clear_dismissed),
                onClick = viewModel::onClearDismissedClicked
            ),
            GenericSettingsItem.Setting(
                getString(R.string.target_amazon_settings_reload_title),
                getText(R.string.target_amazon_settings_reload_content),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_settings_reload),
                onClick = viewModel::onReloadClicked,
                onLongClick = viewModel::onShowAdvancedClicked
            ),
            *advanced.toTypedArray()
        )
    }

    inner class Adapter: BaseSettingsAdapter(binding.configurationAmazonRecyclerView, emptyList())

}