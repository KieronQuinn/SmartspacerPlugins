package com.kieronquinn.app.smartspacer.plugin.amazon.ui.screens.packages

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.kieronquinn.app.smartspacer.plugin.amazon.HEADERS
import com.kieronquinn.app.smartspacer.plugin.amazon.R
import com.kieronquinn.app.smartspacer.plugin.amazon.databinding.FragmentPackagesBinding
import com.kieronquinn.app.smartspacer.plugin.amazon.repositories.AmazonRepository.WebViewState
import com.kieronquinn.app.smartspacer.plugin.amazon.targets.AmazonTarget
import com.kieronquinn.app.smartspacer.plugin.amazon.ui.screens.domain.DomainPickerFragment.Companion.setupDomainChangeListener
import com.kieronquinn.app.smartspacer.plugin.amazon.ui.screens.packages.PackagesViewModel.PackagesSettingsItem
import com.kieronquinn.app.smartspacer.plugin.amazon.ui.screens.packages.PackagesViewModel.State
import com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions.executeJavascript
import com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions.onPageLoaded
import com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions.setup
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Card
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Header
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.SwitchSetting
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.LockCollapsed
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.ProvidesBack
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.ProvidesTitle
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseFragment
import com.kieronquinn.app.smartspacer.plugin.shared.ui.views.LifecycleAwareRecyclerView
import com.kieronquinn.app.smartspacer.plugin.shared.utils.whenResumed
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class PackagesFragment: BaseFragment<FragmentPackagesBinding>(FragmentPackagesBinding::inflate), LockCollapsed, BackAvailable, ProvidesBack, ProvidesTitle {

    companion object {
        private const val EXTRA_IS_SETUP = "is_setup"

        fun Intent.setIsSetup() = apply {
            putExtra(EXTRA_IS_SETUP, true)
        }
    }

    private val viewModel by viewModel<PackagesViewModel>()
    private var lastToast: Int? = null

    private val isSetup by lazy {
        requireActivity().intent.getBooleanExtra(EXTRA_IS_SETUP, false)
    }

    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if(it) {
            onResume()
        }else{
            viewModel.onShowNotificationSettings()
        }
    }

    override val recyclerView: LifecycleAwareRecyclerView
        get() = binding.packagesRecyclerView

    override val loadingView: LinearProgressIndicator
        get() = binding.packagesLoadingProgress

    override val adapter by lazy {
        PackagesAdapter(binding.packagesRecyclerView, emptyList())
    }

    override val additionalPadding by lazy {
        resources.getDimension(SharedR.dimen.margin_8)
    }

    private val cookieManager by inject<CookieManager>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWebView()
        setupWebViewReload()
        setupRecyclerView()
        setupState()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
        SmartspacerTargetProvider.notifyChange(requireContext(), AmazonTarget::class.java)
    }

    override fun onBackPressed(): Boolean {
        val state = viewModel.state.value as? State.Loaded ?: return false
        if(!state.shouldShowWebView) return false
        if(!binding.packagesWebview.canGoBack()) return false
        binding.packagesWebview.goBack()
        return true
    }

    private fun setupWebView() = with(binding.packagesWebview) {
        setup(cookieManager, viewModel.getUserAgent())
        whenResumed {
            onPageLoaded(
                onLoadStart = viewModel::onWebViewStartLoading,
                onLoadFinished = viewModel::onWebViewFinishedLoading
            ) {
                applyWebViewCustomisation(it)
            }.collect {
                viewModel.setWebViewDocument(it)
            }
        }
    }

    private fun setupWebViewReload() {
        whenResumed {
            handleWebViewReload(viewModel.webViewReloadBus.value)
            viewModel.webViewReloadBus.collect {
                handleWebViewReload(it)
            }
        }
        setupDomainChangeListener {
            whenResumed {
                viewModel.onReloadDeliveriesClicked()
            }
        }
    }

    private fun setupRecyclerView() = with(binding.packagesRecyclerView) {
        isNestedScrollingEnabled = false
    }

    private fun handleWebViewReload(tag: Long) = with(binding.packagesWebview) {
        if(this.tag == tag) return@with //Already loaded
        clearHistory()
        loadUrl(viewModel.getOrdersUrl(), HEADERS)
        this.tag = tag
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
                packagesLoading.isVisible = true
                packagesWebview.isInvisible = true
                packagesRecyclerView.isVisible = false
            }
            is State.Loaded -> {
                packagesLoading.isVisible = state.shouldShowLoading
                packagesWebview.isInvisible = !state.shouldShowWebView
                packagesRecyclerView.isVisible = state.shouldShowRecyclerView
                adapter.update(state.loadItems(), recyclerView)
                if(state.webViewState is WebViewState.UserInteractionRequired &&
                    state.webViewState.toast != null && lastToast != state.webViewState.toast) {
                    lastToast = state.webViewState.toast
                    Toast.makeText(
                        requireContext(), state.webViewState.toast, Toast.LENGTH_LONG
                    ).show()
                }
                if(state.webViewState is WebViewState.OrderDetails) {
                    viewModel.persistOrderDetails(
                        state.webViewState.id,
                        state.webViewState.orderId,
                        state.webViewState.orderDetailsUrl,
                        state.webViewState.trackingId,
                        state.webViewState.customerId,
                        state.webViewState.csrfToken
                    )
                }
                if(state.webViewState is WebViewState.OrdersUpdated) {
                    //Clear history so the user cannot accidentally go back to login or details
                    packagesWebview.clearHistory()
                }
                if(state.shouldShowRecyclerView && isSetup) {
                    //Good to go if setting up
                    requireActivity().setResult(Activity.RESULT_OK)
                    requireActivity().finish()
                }
            }
        }
    }

    private fun State.Loaded.loadItems(): List<BaseSettingsItem> {
        val settingsItems = ArrayList<BaseSettingsItem>()
        when {
            !hasDisabledBatteryOptimisation -> {
                settingsItems.add(
                    Card(
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_warning),
                        getString(R.string.target_configuration_settings_battery_optimisation),
                        viewModel::onDisableBatteryOptimisationClicked
                    )
                )
            }
            !hasNotificationPermission -> {
                settingsItems.add(
                    Card(
                        ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_info),
                        getString(R.string.target_configuration_settings_notification_permission),
                        ::onGrantNotificationClicked
                    )
                )
            }
        }
        settingsItems.add(Header(getString(R.string.target_configuration_settings_recent_orders)))
        when (webViewState) {
            is WebViewState.OrdersUpdated -> {
                deliveries.sortedBy { it.index }.map {
                    val isTracking = it.isTracking()
                    PackagesSettingsItem.Package(
                        it,
                        isTracking,
                        viewModel::onDeliveryClicked,
                        viewModel::onDeliveryLongClicked,
                        viewModel::onLinkDeliveryClicked
                    )
                }.ifEmpty {
                    listOf(
                        Card(
                            ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_info),
                            getString(R.string.target_configuration_settings_recent_orders_empty),
                            viewModel::onReloadDeliveriesClicked
                        )
                    )
                }.forEach {
                    settingsItems.add(it)
                }
            }
            is WebViewState.Error -> {
                settingsItems.add(Card(
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_warning),
                    getString(webViewState.reason.message),
                    viewModel::onReloadDeliveriesClicked
                ))
            }
            else -> {
                //Should not be showing anyway, no-op
            }
        }
        settingsItems.add(Header(getString(R.string.target_configuration_settings_settings)))
        settingsItems.add(SwitchSetting(
            settings.showProductImage,
            getString(R.string.target_configuration_settings_show_product_image_title),
            getString(R.string.target_configuration_settings_show_product_image_content),
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_settings_show_image),
            onChanged = viewModel::setShowProductImage
        ))
        settingsItems.add(Setting(
            getString(R.string.target_configuration_settings_domain_title),
            getString(settings.domain.countryRes),
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_settings_domain),
            onClick = viewModel::onSetAmazonDomainClicked
        ))
        settingsItems.add(Setting(
            getString(R.string.target_configuration_settings_sign_out_title),
            getString(R.string.target_configuration_settings_sign_out_content),
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_settings_logout),
            onClick = ::onSignOutClicked
        ))
        return settingsItems
    }

    private fun onGrantNotificationClicked() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }else{
            viewModel.onShowNotificationSettings()
        }
    }

    private fun onSignOutClicked() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(R.string.target_configuration_settings_sign_out_title)
            setMessage(R.string.target_configuration_settings_sign_out_dialog_content)
            setPositiveButton(android.R.string.ok) { _, _ ->
                viewModel.onSignOutClicked()
            }
            setNegativeButton(android.R.string.cancel) { _, _ -> }
        }.show()
    }

    private suspend fun WebView.applyWebViewCustomisation(url: String) {
        when {
            url.endsWith("/gp/your-account/order-history?orderFilter=last30") -> {
                //Replace page content with just order list
                executeJavascript(
                    "document.body.innerHTML = document.getElementById(\"ordersContainer\").innerHTML"
                )
            }
        }
    }

    override fun getTitle() = ""

}