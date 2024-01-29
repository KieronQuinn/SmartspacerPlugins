package com.kieronquinn.app.smartspacer.plugin.amazon.ui.screens.packages

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.CookieManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.amazon.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.amazon.model.api.AmazonDomain
import com.kieronquinn.app.smartspacer.plugin.amazon.model.database.AmazonDelivery.Delivery
import com.kieronquinn.app.smartspacer.plugin.amazon.repositories.AmazonRepository
import com.kieronquinn.app.smartspacer.plugin.amazon.repositories.AmazonRepository.SelectingFor
import com.kieronquinn.app.smartspacer.plugin.amazon.repositories.AmazonRepository.WebViewState
import com.kieronquinn.app.smartspacer.plugin.amazon.repositories.AmazonSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions.createWebView
import com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions.hasDisabledBatteryOptimisation
import com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions.hasNotificationPermission
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItemType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jsoup.nodes.Document
import android.provider.Settings as AndroidSettings

abstract class PackagesViewModel: ViewModel() {

    abstract val webViewReloadBus: StateFlow<Long>
    abstract val state: StateFlow<State>

    abstract fun onResume()
    abstract fun getOrdersUrl(): String
    abstract fun getUserAgent(): String

    abstract fun onDisableBatteryOptimisationClicked()
    abstract fun onShowNotificationSettings()

    abstract fun onWebViewStartLoading()
    abstract fun onWebViewFinishedLoading()
    abstract fun setWebViewDocument(document: Document)

    abstract fun onReloadDeliveriesClicked()
    abstract fun onDeliveryClicked(delivery: Delivery)
    abstract fun onDeliveryLongClicked(delivery: Delivery)
    abstract fun onLinkDeliveryClicked(delivery: Delivery)

    abstract fun onSetAmazonDomainClicked()
    abstract fun setShowProductImage(enabled: Boolean)
    abstract fun onSignOutClicked()

    abstract fun persistOrderDetails(
        id: String,
        orderId: String,
        orderDetailsUrl: String,
        trackingId: String,
        customerId: String,
        csrfToken: String
    )

    data class Settings(
        val domain: AmazonDomain,
        val showProductImage: Boolean
    )

    sealed class State {
        data object Loading: State()
        data class Loaded(
            val webViewState: WebViewState,
            val deliveries: List<Delivery>,
            val settings: Settings,
            val shouldShowLoading: Boolean,
            val shouldShowWebView: Boolean,
            val shouldShowRecyclerView: Boolean,
            val hasNotificationPermission: Boolean,
            val hasDisabledBatteryOptimisation: Boolean
        ): State()
    }

    sealed class PackagesSettingsItem(type: ItemType): BaseSettingsItem(type) {

        data class Package(
            val delivery: Delivery,
            val isTracking: Boolean,
            val onClicked: (Delivery) -> Unit,
            val onLongClicked: (Delivery) -> Unit,
            val onLinkClicked: (Delivery) -> Unit
        ): PackagesSettingsItem(ItemType.PACKAGE)

        enum class ItemType: BaseSettingsItemType {
            PACKAGE
        }
    }

}

class PackagesViewModelImpl(
    context: Context,
    cookieManager: CookieManager,
    private val amazonRepository: AmazonRepository,
    private val navigation: ContainerNavigation,
    settingsRepository: AmazonSettingsRepository
): PackagesViewModel() {

    override val webViewReloadBus = MutableStateFlow(System.currentTimeMillis())

    private val resumeBus = MutableStateFlow(System.currentTimeMillis())
    private val webViewLoading = MutableStateFlow(true)
    private val webViewDocument = MutableStateFlow<Document?>(null)
    private val selectingFor = MutableStateFlow<SelectingFor?>(null)

    private val hasNotificationPermission = resumeBus.map {
        context.hasNotificationPermission()
    }

    private val hasDisabledBatteryOptimisation = resumeBus.map {
        context.hasDisabledBatteryOptimisation()
    }

    private val deliveries = amazonRepository.getDeliveriesAsFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    @SuppressLint("StaticFieldLeak") //Application context
    private val headlessOrderDetailsWebView =
        createWebView(context, cookieManager, amazonRepository.getUserAgent())

    private val domain = settingsRepository.domain
    private val showProductImage = settingsRepository.showProductImage

    private val settings = combine(
        domain.asFlow(),
        showProductImage.asFlow()
    ) { domain, showProductImage ->
        Settings(domain, showProductImage)
    }

    private val webViewState = combine(
        webViewDocument.filterNotNull(),
        selectingFor
    ) { document, selectingFor ->
        amazonRepository.getWebViewState(headlessOrderDetailsWebView, document, selectingFor)
    }.flowOn(Dispatchers.IO).flattenConcat()

    private val permissions = combine(
        hasNotificationPermission,
        hasDisabledBatteryOptimisation
    ) { notification, battery ->
        Pair(notification, battery)
    }

    override val state = combine(
        webViewLoading,
        webViewState,
        deliveries,
        settings,
        permissions
    ) { loading, state, deliveries, settings, permissions ->
        if(loading || state == null || deliveries == null) {
            State.Loading
        }else{
            val shouldShowLoading = state is WebViewState.OrderDetails
            val shouldShowWebView = state is WebViewState.UserInteractionRequired
            val shouldShowRecyclerView = state is WebViewState.OrdersUpdated ||
                    state is WebViewState.Error
            State.Loaded(
                state,
                deliveries,
                settings,
                shouldShowLoading,
                shouldShowWebView,
                shouldShowRecyclerView,
                permissions.first,
                permissions.second
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun onResume() {
        viewModelScope.launch {
            resumeBus.emit(System.currentTimeMillis())
        }
    }

    override fun getOrdersUrl(): String {
        return amazonRepository.getOrdersUrl()
    }

    @SuppressLint("BatteryLife")
    override fun onDisableBatteryOptimisationClicked() {
        viewModelScope.launch {
            navigation.navigate(
                Intent(AndroidSettings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    data = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
                }
            )
        }
    }

    override fun onShowNotificationSettings() {
        viewModelScope.launch {
            navigation.navigate(Intent(AndroidSettings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(AndroidSettings.EXTRA_APP_PACKAGE, BuildConfig.APPLICATION_ID)
            })
        }
    }

    override fun getUserAgent(): String {
        return amazonRepository.getUserAgent()
    }

    override fun onWebViewStartLoading() {
        viewModelScope.launch {
            webViewLoading.emit(true)
        }
    }

    override fun onWebViewFinishedLoading() {
        viewModelScope.launch {
            webViewLoading.emit(false)
        }
    }

    override fun setWebViewDocument(document: Document) {
        viewModelScope.launch {
            webViewDocument.emit(document)
        }
    }

    override fun onReloadDeliveriesClicked() {
        viewModelScope.launch {
            webViewReloadBus.emit(System.currentTimeMillis())
        }
    }

    override fun onDeliveryClicked(delivery: Delivery) {
        viewModelScope.launch {
            navigation.navigate(amazonRepository.getClickUrl(delivery))
        }
    }

    override fun onDeliveryLongClicked(delivery: Delivery) {
        viewModelScope.launch {
            navigation.navigate(PackagesFragmentDirections
                .actionPackagesFragmentToPackageOptionsBottomSheet(delivery))
        }
    }

    override fun onLinkDeliveryClicked(delivery: Delivery) {
        viewModelScope.launch {
            val selectingFor = SelectingFor(
                delivery.id,
                delivery.orderId
            )
            this@PackagesViewModelImpl.selectingFor.emit(selectingFor)
        }
    }

    override fun setShowProductImage(enabled: Boolean) {
        viewModelScope.launch {
            showProductImage.set(enabled)
            amazonRepository.reloadTarget()
        }
    }

    override fun onSetAmazonDomainClicked() {
        viewModelScope.launch {
            navigation.navigate(
                PackagesFragmentDirections.actionPackagesFragmentToDomainPickerFragment(false)
            )
        }
    }

    override fun onSignOutClicked() {
        viewModelScope.launch {
            amazonRepository.signOut()
            webViewReloadBus.emit(System.currentTimeMillis())
        }
    }

    override fun persistOrderDetails(
        id: String,
        orderId: String,
        orderDetailsUrl: String,
        trackingId: String,
        customerId: String,
        csrfToken: String
    ) {
        viewModelScope.launch {
            amazonRepository.persistOrderDetails(
                id,
                orderId,
                orderDetailsUrl,
                trackingId,
                customerId,
                csrfToken
            )
            selectingFor.emit(null)
            webViewReloadBus.emit(System.currentTimeMillis())
        }
    }

}