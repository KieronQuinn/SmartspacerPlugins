package com.kieronquinn.app.smartspacer.plugin.amazon.repositories

import android.app.PendingIntent
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.webkit.CookieManager
import android.webkit.WebView
import androidx.annotation.StringRes
import com.bumptech.glide.RequestManager
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.kieronquinn.app.shared.maps.generateGoogleMap
import com.kieronquinn.app.shared.maps.generateMarker
import com.kieronquinn.app.smartspacer.plugin.amazon.ALLOWED_COOKIE_HOSTS
import com.kieronquinn.app.smartspacer.plugin.amazon.AMZN_APP_CTXT
import com.kieronquinn.app.smartspacer.plugin.amazon.DEFAULT_APP_VERSION
import com.kieronquinn.app.smartspacer.plugin.amazon.R
import com.kieronquinn.app.smartspacer.plugin.amazon.components.notifications.NotificationChannel
import com.kieronquinn.app.smartspacer.plugin.amazon.components.notifications.NotificationId
import com.kieronquinn.app.smartspacer.plugin.amazon.model.api.AState
import com.kieronquinn.app.smartspacer.plugin.amazon.model.api.AmazonDomain
import com.kieronquinn.app.smartspacer.plugin.amazon.model.api.OrdersCookiePayload
import com.kieronquinn.app.smartspacer.plugin.amazon.model.api.TrackingData
import com.kieronquinn.app.smartspacer.plugin.amazon.model.api.TrackingData.PackageLocationDetails.Location
import com.kieronquinn.app.smartspacer.plugin.amazon.model.api.TrackingStatus
import com.kieronquinn.app.smartspacer.plugin.amazon.model.database.AmazonDelivery.Delivery
import com.kieronquinn.app.smartspacer.plugin.amazon.model.database.AmazonDelivery.Status
import com.kieronquinn.app.smartspacer.plugin.amazon.repositories.AmazonRepository.WebViewState
import com.kieronquinn.app.smartspacer.plugin.amazon.repositories.AmazonRepository.WebViewState.Error.Reason
import com.kieronquinn.app.smartspacer.plugin.amazon.service.AmazonTrackingService
import com.kieronquinn.app.smartspacer.plugin.amazon.targets.AmazonTarget
import com.kieronquinn.app.smartspacer.plugin.amazon.ui.activities.ConfigurationActivity
import com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions.clearEncryptedBitmaps
import com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions.deleteEncryptedBitmaps
import com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions.getAmazonAppVersion
import com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions.getAmazonMarketplaceDomain
import com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions.load
import com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions.purge
import com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions.unescape
import com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions.updateCookie
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.BaseConfigurationActivity.Companion.createIntent
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.firstNotNull
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.invoke
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.jsoup.nodes.Document
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URLEncoder
import java.util.TimeZone
import com.kieronquinn.app.shared.R as SharedR

interface AmazonRepository {

    fun getAmazonMarketplaceDomain(): AmazonDomain?
    fun getOrdersUrl(): String
    fun getAppContextCookie(): String
    fun getUserAgent(): String
    fun getDeliveriesAsFlow(): Flow<List<Delivery>>
    fun getDeliveries(): List<Delivery>
    fun getDelivery(orderId: String): Delivery?
    fun updateTrackingData(webView: WebView)
    suspend fun updateDeliveriesAndShowNotificationIfNeeded(
        ordersWebView: WebView,
        orderDetailsWebView: WebView
    )
    fun getClickUrl(delivery: Delivery): Uri
    fun dismissDelivery(orderId: String)
    fun unDismissDelivery(orderId: String)
    fun reloadTarget()
    suspend fun signOut()

    suspend fun persistOrderDetails(
        orderId: String,
        orderDetailsUrl: String,
        trackingId: String,
        customerId: String,
        csrfToken: String
    )

    suspend fun clearOrderDetails(orderId: String)

    fun getWebViewState(
        orderDetailsWebView: WebView,
        document: Document,
        selectingForOrderId: String? = null
    ): Flow<WebViewState?>

    sealed class WebViewState {
        data class UserInteractionRequired(
            val url: String,
            @StringRes val toast: Int? = null
        ): WebViewState()
        data class OrdersUpdated(val trackingUpdated: Boolean): WebViewState()
        data class OrderDetails(
            val orderId: String,
            val orderDetailsUrl: String,
            val trackingId: String,
            val customerId: String,
            val csrfToken: String
        ): WebViewState()
        data class Error(val reason: Reason): WebViewState() {
            enum class Reason(@StringRes val message: Int) {
                LOAD_FAILED(R.string.target_configuration_settings_recent_orders_error),
                PARSE_FAILED(R.string.target_configuration_settings_recent_orders_parse_error),
                ORDER_ID_MISMATCH(R.string.target_configuration_settings_recent_orders_mismatch_error)
            }
        }
    }

}

class AmazonRepositoryImpl(
    private val context: Context,
    private val databaseRepository: DatabaseRepository,
    private val gson: Gson,
    private val glide: RequestManager,
    private val cookieManager: CookieManager,
    private val notificationRepository: NotificationRepository,
    settingsRepository: AmazonSettingsRepository,
): AmazonRepository {

    companion object {
        private const val MAP_WIDTH = 768
        private const val MAP_HEIGHT = 432
    }

    private val scope = MainScope()
    private val persistDetailsLock = Mutex()
    private val trackingUpdateLock = Mutex()
    private val updateDeliveriesLock = Mutex()

    private val trackingService = Retrofit.Builder()
        .baseUrl("http://localhost/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AmazonTrackingService::class.java)

    private val mapPadding = context.resources.getDimensionPixelSize(SharedR.dimen.margin_16).let {
        Rect(it, 0, 0, 0)
    }

    private val deliveries = databaseRepository.getAmazonDeliveries()
        .map { it.map { delivery -> delivery.decrypt(context) } }
        .onEach { processDeliveries(it) }
        .stateIn(scope, SharingStarted.Eagerly, null)

    private val domain = settingsRepository.domain.asFlow()
        .onEach { setupCookies() }
        .stateIn(scope, SharingStarted.Eagerly, settingsRepository.domain.getSync())

    override fun getAmazonMarketplaceDomain(): AmazonDomain? {
        return context.getAmazonMarketplaceDomain()
    }

    override fun getOrdersUrl(): String {
        return "https://www.${getDomainName()}/gp/your-account/order-history?orderFilter=last30"
    }

    override fun getAppContextCookie(): String {
        val displayMetrics = Resources.getSystem().displayMetrics
        return mapOf(
            "an" to "Amazon.com", //App name
            "av" to getAmazonVersion(), //App version
            "xv" to "1.15.0", //API version
            "os" to "Android", //OS
            "ov" to Build.VERSION.RELEASE,
            "cp" to 788760, //Capabilities
            "uiv" to 4, //URL interception version
            "ast" to 3, //App startup type, 3 = warm start
            "nal" to "1", //Native API version
            "di" to mapOf(
                "pr" to Build.PRODUCT,
                "md" to Build.MODEL,
                "v" to Build.DEVICE,
                "mf" to Build.MANUFACTURER,
                "ca" to "", //Carrier, intentionally left blank
                "ct" to "WIFI" //Network type, spoofing to always wifi
            ),
            "dm" to mapOf(
                "w" to displayMetrics.widthPixels,
                "h" to displayMetrics.heightPixels,
                "ld" to displayMetrics.density,
                "dx" to displayMetrics.xdpi,
                "dy" to displayMetrics.ydpi,
                "pt" to 0, //Padding top
                "pb" to 78 //Padding bottom, seems to be hardcoded for some reason to 78
            ),
            "is" to "com.android.vending", //Install source
            "msd" to ".${getDomainName()}"
        ).let {
            JSONObject(it)
        }.let {
            URLEncoder.encode("1.8 $it", Charsets.UTF_8.name())
        }
    }

    private fun getAmazonVersion(): String {
        return context.getAmazonAppVersion() ?: DEFAULT_APP_VERSION
    }

    override fun getUserAgent(): String {
        return "Amazon.com/${getAmazonVersion()} (Android/${Build.VERSION.RELEASE}/${Build.MODEL})"
    }

    override fun getDeliveriesAsFlow(): Flow<List<Delivery>> {
        return deliveries.filterNotNull()
    }

    override fun getDeliveries(): List<Delivery> {
        return runBlocking {
            deliveries.firstNotNull()
        }
    }

    override fun getDelivery(orderId: String): Delivery? {
        return runBlocking {
            deliveries.firstNotNull().firstOrNull { it.orderId == orderId }
        }
    }

    override fun updateTrackingData(webView: WebView) {
        scope.launch {
            if(!updateTrackingDataLocked(webView, deliveries.firstNotNull())) {
                showRefreshErrorNotification()
            } else {
                notificationRepository.cancelNotification(NotificationId.ERROR)
            }
        }
    }

    private suspend fun updateTrackingDataLocked(
        webView: WebView,
        deliveries: List<Delivery>
    ) = trackingUpdateLock.withLock {
        val deliveriesToUpdate = deliveries.filter {
            it.canBeTracked()
        }
        val cookie = cookieManager.getCookie("https://amazon.co.uk").takeIf {
            it.isNotBlank()
        } ?: return@withLock false
        deliveriesToUpdate.forEach {
            val trackingDataAndStatus = it.getTrackingDataAndStatus(webView, cookie)
            val map = trackingDataAndStatus?.first?.getMap()
            val delivery = it.copy(
                trackingData = trackingDataAndStatus?.first,
                trackingStatus = trackingDataAndStatus?.second,
                mapBitmap = map
            )
            databaseRepository.addAmazonDelivery(delivery.encrypt(context))
        }
        true
    }

    private fun getOrderDetailsUrl(orderId: String): String {
        return "https://www.${getDomainName()}/gp/your-account/order-details?ie=UTF8&orderID=$orderId"
    }

    override fun getClickUrl(delivery: Delivery): Uri {
        val url = delivery.orderDetailsUrl ?: run {
            getOrderDetailsUrl(delivery.orderId)
        }
        return Uri.parse(url)
    }

    override fun dismissDelivery(orderId: String) {
        scope.launch {
            val delivery = deliveries.firstNotNull().firstOrNull { it.orderId == orderId }
                ?: return@launch
            databaseRepository.addAmazonDelivery(
                delivery.copy(dismissedAtStatus = delivery.status).encrypt(context)
            )
        }
    }

    override fun unDismissDelivery(orderId: String) {
        scope.launch {
            val delivery = deliveries.firstNotNull().firstOrNull { it.orderId == orderId }
                ?: return@launch
            databaseRepository.addAmazonDelivery(
                delivery.copy(dismissedAtStatus = null).encrypt(context)
            )
        }
    }

    override fun reloadTarget() {
        SmartspacerTargetProvider.notifyChange(context, AmazonTarget::class.java)
    }

    override suspend fun signOut() {
        databaseRepository.clearAll()
        context.clearEncryptedBitmaps()
        cookieManager.purge()
        setupCookies()
    }

    override suspend fun updateDeliveriesAndShowNotificationIfNeeded(
        ordersWebView: WebView,
        orderDetailsWebView: WebView
    ) {
        val shouldShowErrorNotification = !updateDeliveriesLocked(ordersWebView, orderDetailsWebView)
        if(shouldShowErrorNotification) {
            showRefreshErrorNotification()
        }
    }

    private fun showRefreshErrorNotification() = with(context) {
        notificationRepository.showNotification(NotificationId.ERROR, NotificationChannel.ERROR) {
            val intent = createIntent(context, ConfigurationActivity.NavGraphMapping.TARGET_AMAZON)
            it.setContentTitle(getString(R.string.notification_title_error_updating_deliveries))
            it.setContentText(getString(R.string.notification_content_error_updating_deliveries))
            it.setSmallIcon(R.drawable.ic_notification)
            it.setOngoing(false)
            it.setAutoCancel(true)
            it.setContentIntent(
                PendingIntent.getActivity(
                    this,
                    NotificationId.ERROR.ordinal,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            it.setTicker(getString(R.string.notification_title_error_updating_deliveries))
        }
    }

    private fun showNewOrdersNotification() = with(context) {
        notificationRepository.showNotification(
            NotificationId.ORDER_LINK,
            NotificationChannel.ORDER_LINK
        ) {
            val intent = createIntent(context, ConfigurationActivity.NavGraphMapping.TARGET_AMAZON)
            it.setContentTitle(getString(R.string.notification_title_new_delivery))
            it.setContentText(getString(R.string.notification_content_new_delivery))
            it.setSmallIcon(R.drawable.ic_notification)
            it.setOngoing(false)
            it.setAutoCancel(true)
            it.setContentIntent(
                PendingIntent.getActivity(
                    this,
                    NotificationId.ORDER_LINK.ordinal,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            it.setTicker(getString(R.string.notification_title_new_delivery))
        }
    }

    private suspend fun updateDeliveriesLocked(
        ordersWebView: WebView,
        orderDetailsWebView: WebView
    ): Boolean = updateDeliveriesLock.withLock {
        val ordersDocument = ordersWebView.load(getOrdersUrl()).firstNotNull()
        val state = getWebViewState(orderDetailsWebView, ordersDocument).firstNotNull()
        state is WebViewState.OrdersUpdated && state.trackingUpdated
    }

    private suspend fun Delivery.getTrackingDataAndStatus(
        webView: WebView,
        cookie: String,
        isRetry: Boolean = false
    ): Pair<TrackingData, TrackingStatus>? {
        val domain = getDomainName()
        val trackingData = trackingService.getTrackingData(
            domain,
            cookie,
            trackingId ?: return null,
            csrfToken ?: return null
        ).invoke()
        if(trackingData?.isTokenInvalid() == true && !isRetry) {
            //Token has expired, attempt to refresh and then reload once
            if(!updateCsrfToken(webView)) return null //If updating token fails, we cannot get tracking
            return getTrackingDataAndStatus(webView, cookie, true) //Updating worked, try again but only once
        }
        val data = trackingData?.takeIf { it.success } ?: return null
        val trackingStatus = trackingService.getTrackingStatus(
            domain,
            cookie,
            data.packageLocationDetails?.status?.name ?: return null,
            data.packageLocationDetails.stopsRemaining?.toString() ?: "0",
            TimeZone.getDefault().id,
            customerId ?: return null,
            trackingId,
            csrfToken ?: return null
        ).invoke()?.takeIf { it.success } ?: return null
        return Pair(trackingData, trackingStatus)
    }

    private suspend fun Delivery.updateCsrfToken(webView: WebView): Boolean {
        val document = webView.load(orderDetailsUrl ?: return false)
            .firstOrNull() ?: return false
        val state = getWebViewState(webView, document, orderId).firstNotNull()
        return if(state is WebViewState.OrderDetails) {
            csrfToken = state.csrfToken
            true
        }else false
    }

    private suspend fun TrackingData.getMap(): Bitmap? {
        val driverPosition = packageLocationDetails?.transporterDetails?.geoLocation?.toLatLng()
        val deliveryPosition = packageLocationDetails?.destinationAddress?.geoLocation?.toLatLng()
        return context.generateGoogleMap(MAP_WIDTH, MAP_HEIGHT, mapPadding) {
            listOfNotNull(
                if(deliveryPosition != null) {
                    context.generateMarker(deliveryPosition, R.drawable.ic_marker_home)
                }else null,
                if(driverPosition != null) {
                    context.generateMarker(driverPosition, R.drawable.ic_marker_amazon)
                }else null
            )
        }
    }

    private fun Location.GeoLocation.toLatLng(): LatLng? {
        val latitude = latitude.takeIf { lat -> lat != 0.0 } ?: return null
        val longitude = longitude.takeIf { lng -> lng != 0.0 } ?: return null
        return LatLng(latitude, longitude)
    }

    override suspend fun persistOrderDetails(
        orderId: String,
        orderDetailsUrl: String,
        trackingId: String,
        customerId: String,
        csrfToken: String
    ) = persistDetailsLock.withLock {
        withContext(Dispatchers.IO) {
            val delivery = getDelivery(orderId) ?: return@withContext
            val updatedDelivery = delivery.copy(
                orderDetailsUrl = orderDetailsUrl,
                trackingId = trackingId,
                customerId = customerId,
                csrfToken = csrfToken
            )
            databaseRepository.addAmazonDelivery(updatedDelivery.encrypt(context))
        }
    }

    override suspend fun clearOrderDetails(orderId: String) = persistDetailsLock.withLock {
        withContext(Dispatchers.IO) {
            val delivery = getDelivery(orderId) ?: return@withContext
            val updatedDelivery = delivery.copy(
                orderDetailsUrl = null,
                trackingId = null,
                csrfToken = null
            )
            databaseRepository.addAmazonDelivery(updatedDelivery.encrypt(context))
        }
    }

    override fun getWebViewState(
        orderDetailsWebView: WebView,
        document: Document,
        selectingForOrderId: String?
    ) = flow {
        emit(null)
        val result = when {
            document.location().isEmpty() -> WebViewState.Error(Reason.LOAD_FAILED)
            document.isErrorPage() -> WebViewState.Error(Reason.PARSE_FAILED)
            //If we know the page is login or captcha, don't even bother parsing out content
            document.isLoginPage() || document.isCapchaPage() -> {
                WebViewState.UserInteractionRequired(document.location())
            }
            //If the page is the order list, update the database and then allow interaction if needed
            document.isOrdersPage() -> {
                //Parse out and update the orders in the database
                val orders = document.loadOrders()
                orders.persistDeliveries()
                when {
                    selectingForOrderId != null -> {
                        WebViewState.UserInteractionRequired(
                            document.location(),
                            R.string.item_package_link_delivery_toast
                        )
                    }
                    else -> {
                        val trackingUpdated = updateTrackingDataLocked(orderDetailsWebView, orders)
                        WebViewState.OrdersUpdated(trackingUpdated)
                    }
                }
            }
            //If the page is order details, extract the tracking ID & token, return them if selecting
            document.isOrderDetailsPage() -> {
                val aState = document.getAState()
                val csrfToken = document.getCsrfToken()
                if(selectingForOrderId != null && aState != null && csrfToken != null) {
                    if(aState.orderId == selectingForOrderId) {
                        WebViewState.OrderDetails(
                            aState.orderId,
                            document.location(),
                            aState.trackingId,
                            aState.customerId,
                            csrfToken
                        )
                    }else{
                        //The user has selected the wrong order!
                        WebViewState.Error(Reason.ORDER_ID_MISMATCH)
                    }
                }else{
                    WebViewState.UserInteractionRequired(document.location())
                }
            }
            //Otherwise, fall back to interaction required to catch all other cases
            else -> {
                val toast = if(selectingForOrderId != null) {
                    R.string.item_package_link_delivery_toast_optional
                }else null
                WebViewState.UserInteractionRequired(document.location(), toast)
            }
        }
        emit(result)
    }

    private fun Document.isErrorPage(): Boolean {
        return getElementsByTag("a")
            .any { it.attr("href") == "/ref=cs_500_link" }
    }

    private fun Document.isLoginPage(): Boolean {
        return getElementsByTag("form")
            .any { it.attr("name") == "signIn" }
    }

    private fun Document.isCapchaPage(): Boolean {
        return getElementById("captchacharacters") != null
    }

    private fun Document.isOrdersPage(): Boolean {
        return getElementById("ordersContainer") != null
    }

    private fun Document.isOrderDetailsPage(): Boolean {
        return getAState() != null
    }

    private fun Document.getAState(): AState? {
        return getElementsByTag("script").firstOrNull {
            it.attr("type") == "a-state" && it.data().contains("trackingId")
        }?.data()?.parseAState()
    }

    private fun Document.getCsrfToken(): String? {
        return getElementsByTag("script").firstOrNull {
            it.data().contains("csrfToken")
        }?.data()?.let {
            it.split("'")[1]
        }
    }

    private suspend fun Document.loadOrders(): List<Delivery> {
        val orders = getElementsByClass("js-item") ?: return emptyList()
        return orders.mapIndexedNotNull { index, it ->
            val url = it.getElementsByTag("a").firstOrNull()
                ?.attr("href")?.toUri() ?: return@mapIndexedNotNull null
            val orderId = url.getQueryParameter("orderId") ?: return@mapIndexedNotNull null
            val shipmentId = url.getQueryParameter("shipmentId")
            val image = it.getElementsByTag("img").firstOrNull()
                ?: return@mapIndexedNotNull null
            val title = image.attr("title") ?: return@mapIndexedNotNull null
            val src = image.attr("src") ?: return@mapIndexedNotNull null
            val cookiePayload = it.getElementsByClass("js-shipment-info")
                .firstOrNull()?.attr("data-cookiepayload")?.parseCookiePayload()
                ?: return@mapIndexedNotNull null
            Delivery(
                orderId,
                shipmentId,
                index,
                title,
                src,
                null,
                cookiePayload.shortStatus ?: Status.ORDERED,
                cookiePayload.primaryStatus
                    ?: context.getString(R.string.target_amazon_status_ordered),
                null,
                null,
                null,
                null,
                null,
                null,
                src.loadImageBitmap()
            )
        }
    }

    private suspend fun List<Delivery>.persistDeliveries() {
        val current = deliveries.firstNotNull().toMutableList()
        val removed = current.filter { none { delivery -> delivery.orderId == it.orderId } }
        removed.forEach {
            current.remove(it)
            context.deleteEncryptedBitmaps(it.orderId)
            databaseRepository.deleteAmazonDelivery(it.orderId)
        }
        val merged = (this + current).groupBy { it.orderId }.mapNotNull {
            Delivery(
                it.getBestItem { orderId } ?: return@mapNotNull null,
                it.getBestItem { shipmentId } ?: return@mapNotNull null,
                it.getBestItem { index } ?: return@mapNotNull null,
                it.getBestItem { name } ?: return@mapNotNull null,
                it.getBestItem { imageUrl } ?: return@mapNotNull null,
                it.getBestItem { orderDetailsUrl },
                it.getBestItem { status } ?: return@mapNotNull null,
                it.getBestItem { message } ?: return@mapNotNull null,
                it.getBestItem { trackingId },
                it.getBestItem { customerId },
                it.getBestItem { csrfToken },
                it.getBestItem { trackingData },
                it.getBestItem { trackingStatus },
                it.getDismissedAtStatus(),
                it.getBestItem { imageBitmap },
                it.value.firstOrNull()?.mapBitmap,
            )
        }
        merged.forEach {
            databaseRepository.addAmazonDelivery(it.encrypt(context))
        }
    }

    private fun <T> Map.Entry<String, List<Delivery>>.getBestItem(block: Delivery.() -> T?): T? {
        return value.firstNotNullOfOrNull { block(it) }
    }

    private fun Map.Entry<String, List<Delivery>>.getDismissedAtStatus(): Status? {
        return if(value.size == 1 && value.first().status == Status.DELIVERED) {
            //This is a new delivery that's already been delivered, dismiss automatically
            Status.DELIVERED
        } else getBestItem { dismissedAtStatus }
    }

    private fun String.parseCookiePayload(): OrdersCookiePayload? {
        return try {
            gson.fromJson(unescape(), OrdersCookiePayload::class.java)
        }catch (e: Exception) {
            null
        }
    }

    private fun String.parseAState(): AState? {
        return try {
            gson.fromJson(this, AState::class.java)
        }catch (e: Exception) {
            null
        }
    }

    private fun String.toUri(): Uri? {
        return try {
            Uri.parse(this)
        }catch (e: Exception) {
            null
        }
    }

    private fun getDomainName(): String {
        return domain.value.domainName
    }

    private suspend fun String.loadImageBitmap(): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                glide.asBitmap().load(this@loadImageBitmap).submit().get()
            }catch (e: Exception) {
                null
            }
        }
    }

    private fun setupCookies() {
        ALLOWED_COOKIE_HOSTS.forEach {
            cookieManager.updateCookie(it, AMZN_APP_CTXT, getAppContextCookie())
        }
    }

    private fun processDeliveries(deliveries: List<Delivery>) {
        if(deliveries.any { it.requiresLinkingDelivery() }){
            showNewOrdersNotification()
        } else {
            notificationRepository.cancelNotification(NotificationId.ERROR)
            notificationRepository.cancelNotification(NotificationId.ORDER_LINK)
        }
        SmartspacerTargetProvider.notifyChange(context, AmazonTarget::class.java)
    }

}