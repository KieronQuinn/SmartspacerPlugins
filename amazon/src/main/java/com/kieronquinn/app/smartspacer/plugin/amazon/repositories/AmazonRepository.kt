package com.kieronquinn.app.smartspacer.plugin.amazon.repositories

import android.content.Context
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.view.View.MeasureSpec
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebView
import com.google.android.gms.maps.model.LatLng
import com.kieronquinn.app.shared.maps.generateGoogleMap
import com.kieronquinn.app.shared.maps.generateMarker
import com.kieronquinn.app.smartspacer.plugin.amazon.R
import com.kieronquinn.app.smartspacer.plugin.amazon.model.AmazonDomain
import com.kieronquinn.app.smartspacer.plugin.amazon.model.TrackingDomain
import com.kieronquinn.app.smartspacer.plugin.amazon.model.database.AmazonDelivery.Delivery
import com.kieronquinn.app.smartspacer.plugin.amazon.model.database.AmazonDelivery.Status
import com.kieronquinn.app.smartspacer.plugin.amazon.model.database.AmazonDelivery.TrackingData
import com.kieronquinn.app.smartspacer.plugin.amazon.targets.AmazonTarget
import com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions.clearEncryptedBitmaps
import com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions.deleteEncryptedBitmaps
import com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions.getAmazonAppVersion
import com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions.getAmazonMarketplaceDomain
import com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions.getCookies
import com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions.getHtml
import com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions.getStringOrNull
import com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions.load
import com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions.loadImageUrl
import com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions.removeAllCookies
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.firstNotNull
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.getDisplayPortraitHeight
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.getDisplayPortraitWidth
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.apache.commons.text.StringEscapeUtils
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import com.kieronquinn.app.shared.R as SharedR

interface AmazonRepository {

    fun isSignedIn(): Boolean
    fun getDeliveries(): List<Delivery>
    fun syncDeliveries(reloadList: Boolean = true)
    fun dismissDelivery(shipmentId: String, smartspacerId: String)
    fun clearDismissedDeliveries()
    fun getClickUrl(delivery: Delivery): String?
    fun getDomain(): Flow<AmazonDomain?>
    fun getOrdersUrl(domain: AmazonDomain): String?
    fun reloadTarget(smartspacerId: String? = null)
    fun clearState()

    suspend fun syncDeliveriesNow(
        reloadList: Boolean = true,
        reloadListIfEmpty: Boolean = false
    ): Boolean

}

class AmazonRepositoryImpl(
    private val context: Context,
    private val databaseRepository: DatabaseRepository,
    private val settings: AmazonSettingsRepository,
): AmazonRepository {

    companion object {
        private const val ORDERS_URL = "/gp/your-account/order-history?orderFilter=last30"
        private const val PROGRESS_TRACKER_URL =
            "/progress-tracker/package/?itemId=%1s&orderId=%1s&shipmentId=%1s"
        private const val COOKIE_SESSION_ID = "session-id"
        private const val MAP_WIDTH = 768
        private const val MAP_HEIGHT = 432

        private val TRACKING_CLASSES = setOf(
            "carrierRelatedInfo-trackingId-text",
            "pt-delivery-card-trackingId"
        )
    }

    private val scope = MainScope()

    private val cookieManager by lazy {
        CookieManager.getInstance()
    }

    private val domain = settings.domain.asFlow()
        .map { it.takeIf { it != AmazonDomain.UNKNOWN } ?: tryGetDomainFromAmazonApp() }
        .stateIn(scope, SharingStarted.Eagerly, null)

    private val service = Retrofit.Builder()
        .baseUrl("http://localhost/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AmazonPackageTrackingService::class.java)

    private val headlessWebView by lazy {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.loadsImagesAutomatically = false
            settings.blockNetworkImage = true
            val displayWidth = context.getDisplayPortraitWidth()
            val displayHeight = context.getDisplayPortraitHeight()
            val width = MeasureSpec.makeMeasureSpec(displayWidth, MeasureSpec.EXACTLY)
            val height = MeasureSpec.makeMeasureSpec(displayHeight, MeasureSpec.EXACTLY)
            measure(width, height)
            layout(0, 0, displayWidth, displayHeight)
            cookieManager.setAcceptThirdPartyCookies(this, true)
        }
    }

    private val mapPadding = context.resources.getDimensionPixelSize(SharedR.dimen.margin_16).let {
        Rect(it, 0, 0, 0)
    }

    private val deliveries = databaseRepository.getAmazonDeliveries()
        .map {
            it.map { delivery -> delivery.decrypt(context) }
        }.flowOn(Dispatchers.IO).stateIn(scope, SharingStarted.Eagerly, emptyList())

    override fun isSignedIn(): Boolean {
        val domain = runBlocking { getOrQueryDomain() } ?: return false
        val ordersUrl = getOrdersUrl(domain) ?: return false
        return getSessionId(ordersUrl) != null
    }

    override fun getDeliveries() = deliveries.value

    private fun getSessionId(ordersUrl: String): String? {
        return cookieManager.getCookies(ordersUrl)[COOKIE_SESSION_ID]
    }

    private fun getCookieHeader(ordersUrl: String): String? {
        return cookieManager.getCookies(ordersUrl).entries.joinToString("; "){
            "${it.key}=${it.value}"
        }.ifEmpty { null }
    }

    override fun syncDeliveries(reloadList: Boolean) {
        scope.launch {
            syncDeliveriesNow(reloadList)
        }
    }

    override fun dismissDelivery(shipmentId: String, smartspacerId: String) {
        scope.launch(Dispatchers.IO) {
            val delivery = deliveries.firstNotNull().firstOrNull {
                it.shipmentId == shipmentId
            } ?: return@launch
            val dismissedDelivery = delivery.copy(dismissedAtStatus = delivery.status)
            databaseRepository.addAmazonDelivery(dismissedDelivery.encrypt(context))
            reloadTarget(smartspacerId)
        }
    }

    override fun clearDismissedDeliveries() {
        scope.launch(Dispatchers.IO){
            val undeliveredDeliveries = deliveries.firstNotNull().filterNot {
                it.dismissedAtStatus == Status.DELIVERED
            }.map { it.copy(dismissedAtStatus = null) }
            undeliveredDeliveries.forEach {
                databaseRepository.addAmazonDelivery(it.encrypt(context))
            }
        }
    }

    override fun getClickUrl(delivery: Delivery) = with(delivery) {
        val domain = domain.value?.domainName ?: return@with null
        val progressTrackerUrl = "https://${domain}$PROGRESS_TRACKER_URL"
        String.format(progressTrackerUrl, lineItemId, orderId, shipmentId)
    }

    override fun getDomain(): Flow<AmazonDomain?> {
        return domain
    }

    override fun getOrdersUrl(domain: AmazonDomain): String? {
        val domainName = domain.domainName ?: return null
        return "https://$domainName$ORDERS_URL"
    }

    override fun reloadTarget(smartspacerId: String?) {
        SmartspacerTargetProvider.notifyChange(context, AmazonTarget::class.java, smartspacerId)
    }

    override suspend fun syncDeliveriesNow(
        reloadList: Boolean,
        reloadListIfEmpty: Boolean
    ): Boolean {
        val domain = getOrQueryDomain() ?: return false
        val domainName = domain.domainName ?: return false
        val ordersUrl = getOrdersUrl(domain) ?: return false
        val trackingDomain = domain.trackingDomain
        val progressTrackerUrl = "https://$domainName$PROGRESS_TRACKER_URL"
        if(getCookieHeader(ordersUrl) == null) return false
        //This gets re-set every load in case the app has updated
        headlessWebView.settings.userAgentString = getUserAgent()
        val currentDeliveries = deliveries.value
        val deliveries = if(reloadList || (reloadListIfEmpty && currentDeliveries.isEmpty())){
            if(!headlessWebView.load(ordersUrl)) return false
            val html = StringEscapeUtils.unescapeJava(headlessWebView.getHtml())
            parseOrdersHtml(html)
        }else currentDeliveries
        val newDeliveries = deliveries.map { new ->
            //Copy across the tracking IDs from the current, if available
            val current = currentDeliveries.firstOrNull { it.shipmentId == new.shipmentId }
                ?: return@map new.dismissIfNeeded()
            new.copy(trackingId = current.trackingId, dismissedAtStatus = current.dismissedAtStatus)
        }.map {
            it.getTrackingIdIfNeeded(progressTrackerUrl)
                .getTrackingInfoIfNeeded(trackingDomain, ordersUrl)
        }
        //Don't clear existing if new list is empty, it may just be a one-off failed load
        if(newDeliveries.isEmpty()) return true
        val removedDeliveries = currentDeliveries.filterNot {
            newDeliveries.any { new -> new.shipmentId == it.shipmentId }
        }
        withContext(Dispatchers.IO){
            //Remove stale deliveries
            removedDeliveries.forEach {
                databaseRepository.deleteAmazonDelivery(it.shipmentId)
                context.deleteEncryptedBitmaps(it.shipmentId)
            }
            //Add and update current deliveries
            newDeliveries.forEach {
                databaseRepository.addAmazonDelivery(it.encrypt(context))
            }
        }
        return true
    }

    /**
     *  If a new Delivery comes through but is already delivered (ie. it's stale), automatically
     *  set the dismissed at status value to the current status so it is not queried or shown
     */
    private fun Delivery.dismissIfNeeded(): Delivery {
        return if(status == Status.DELIVERED) {
            copy(dismissedAtStatus = status)
        }else this
    }

    private fun Document.isLoginPage(): Boolean {
        return getElementsByTag("form")
            .any { it.attr("name") == "signIn" }
    }

    private suspend fun parseOrdersHtml(
        html: String
    ): List<Delivery> = withContext(Dispatchers.IO) {
        val document = try {
            Jsoup.parse(html)
        }catch (e: Exception){
            return@withContext emptyList()
        }
        if(document.isLoginPage()) {
            //User has been signed out, clear state and show login target
            clearState()
            return@withContext emptyList()
        }
        val items = document.getElementsByClass("js-item")
        items.mapNotNull {
            //Link to order details should be first child
            val link = it.child(0).attr("href") ?: return@mapNotNull null
            val url = Uri.parse(link)
            val shipmentId = url.getQueryParameter("shipmentId") ?: return@mapNotNull null
            val lineItemId = url.getItemId() ?: return@mapNotNull null
            val orderId = url.getQueryParameter("orderId") ?: return@mapNotNull null
            //Order image is only `img` tag within list item
            val image = it.getElementsByTag("img")
                .getOrNull(0) ?: return@mapNotNull null
            //We can extract the image URL and the order title from the image
            val imageUrl = image.attr("src")
            val label = image.attr("title")
            //Shipment info as JSON is included as a hidden tag
            val shipmentInfo = it.getElementsByClass("js-shipment-info")
                .getOrNull(0)
                ?.attr("data-cookiepayload")
                ?.let { info -> parseShipmentInfo(info) }
                ?: return@mapNotNull null
            Delivery(
                shipmentId,
                label,
                imageUrl,
                lineItemId,
                orderId,
                shipmentInfo.second,
                shipmentInfo.first,
                null,
                null,
                null,
                context.loadImageUrl(imageUrl),
                null
            )
        }
    }

    private fun Uri.getItemId(): String? {
        return getQueryParameter("lineItemId") ?: getQueryParameter("itemId")
    }

    private fun parseShipmentInfo(json: String): Pair<String, Status>? {
        val parsedJson = JSONObject(json)
        val primaryStatus = parsedJson.getStringOrNull("primaryStatus") ?: return null
        val status = parsedJson.getStringOrNull("shortStatus")?.let { status ->
            Status.values().firstOrNull { it.name == status } ?: Status.ORDERED
        } ?: return null
        return Pair(primaryStatus, status)
    }

    private suspend fun Delivery.getTrackingIdIfNeeded(progressTrackerUrl: String): Delivery {
        if(!isTrackable()) return this //Can't track unless in transit or out for delivery
        if(trackingId != null) return this //No need to reload if already loaded
        val trackerUrl = String.format(progressTrackerUrl, lineItemId, orderId, shipmentId)
        if(!headlessWebView.load(trackerUrl)) return this
        val html = StringEscapeUtils.unescapeJava(headlessWebView.getHtml())
        return withContext(Dispatchers.IO) {
            val document = try {
                Jsoup.parse(html)
            } catch (e: Exception) {
                return@withContext this@getTrackingIdIfNeeded
            }
            if(document.isLoginPage()){
                //User has been signed out, clear state and show login target
                clearState()
                return@withContext this@getTrackingIdIfNeeded
            }
            val trackingId = document.getTrackingElement()?.text()
                ?: return@withContext this@getTrackingIdIfNeeded
            if (!trackingId.contains(":")) return@withContext this@getTrackingIdIfNeeded
            val id = trackingId.split(":")[1].trim()
            copy(trackingId = id)
        }
    }

    private fun Document.getTrackingElement(): Element? {
        return TRACKING_CLASSES.firstNotNullOfOrNull {
            getElementsByClass(it).getOrNull(0)
        }
    }

    private suspend fun Delivery.getTrackingInfoIfNeeded(
        trackingDomain: TrackingDomain?,
        ordersUrl: String
    ): Delivery = withContext(Dispatchers.IO) {
        if(trackingDomain == null) return@withContext this@getTrackingInfoIfNeeded //Can't track
        if(!isTrackable()) return@withContext this@getTrackingInfoIfNeeded
        val trackingId = trackingId ?: return@withContext this@getTrackingInfoIfNeeded
        val cookie = getCookieHeader(ordersUrl) ?: return@withContext this@getTrackingInfoIfNeeded
        val sessionId = getSessionId(ordersUrl) ?: return@withContext this@getTrackingInfoIfNeeded
        val trackingData = try {
            service.getTrackingInfo(trackingDomain.domain, trackingId, cookie, sessionId).execute()
        }catch (e: Exception){
            null
        }?.body() ?: return@withContext this@getTrackingInfoIfNeeded
        if(trackingData == this@getTrackingInfoIfNeeded.trackingData && mapBitmap != null) {
            //Don't bother updating the map if it's going to be identical
            return@withContext this@getTrackingInfoIfNeeded
        }
        val driverPosition = trackingData.transporterDetails?.geoLocation?.let {
            val latitude = it.latitude.takeIf { lat -> lat != 0.0 } ?: return@let null
            val longitude = it.longitude.takeIf { lng -> lng != 0.0 } ?: return@let null
            LatLng(latitude, longitude)
        }
        val deliveryPosition = trackingData.destinationAddress.geoLocation?.let {
            val latitude = it.latitude.takeIf { lat -> lat != 0.0 } ?: return@let null
            val longitude = it.longitude.takeIf { lng -> lng != 0.0 } ?: return@let null
            LatLng(latitude, longitude)
        }
        val map = if(driverPosition != null && deliveryPosition != null){
            context.generateGoogleMap(MAP_WIDTH, MAP_HEIGHT, mapPadding) {
                listOf(
                    context.generateMarker(deliveryPosition, R.drawable.ic_marker_home),
                    context.generateMarker(driverPosition, R.drawable.ic_marker_amazon)
                )
            }
        }else null
        copy(trackingData = trackingData, mapBitmap = map)
    }

    private fun Delivery.isTrackable(): Boolean {
        return status == Status.IN_TRANSIT || status == Status.OUT_FOR_DELIVERY
    }

    private fun setupDeliveries() = scope.launch {
        deliveries.debounce(500L).collect {
            reloadTarget()
        }
    }

    private suspend fun getOrQueryDomain(): AmazonDomain? {
        return domain.firstNotNull().takeIf { it != AmazonDomain.UNKNOWN }
    }

    private suspend fun tryGetDomainFromAmazonApp(): AmazonDomain {
        return withContext(Dispatchers.IO) {
            context.getAmazonMarketplaceDomain().also {
                if(it != AmazonDomain.UNKNOWN) {
                    settings.domain.set(it)
                }
            }
        }
    }

    private fun getUserAgent(): String {
        val version = context.getAmazonAppVersion()
            ?: return WebSettings.getDefaultUserAgent(context)
        return buildUserAgent(version)
    }

    private fun buildUserAgent(appVersion: String): String {
        return StringBuilder().apply {
            append("Amazon.com/")
            append(appVersion)
            append(" (Android/")
            append(osVersion())
            append("/")
            append(Build.DEVICE)
            append(")")
        }.toString()
    }

    private fun osVersion(): String {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                Build.VERSION.RELEASE_OR_PREVIEW_DISPLAY
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                Build.VERSION.RELEASE_OR_CODENAME
            }
            else -> {
                Build.VERSION.RELEASE
            }
        }
    }

    override fun clearState() {
        scope.launch {
            WebStorage.getInstance().deleteAllData()
            cookieManager.removeAllCookies()
            cookieManager.flush()
            headlessWebView.clearCache(true)
            databaseRepository.clearAll()
            context.clearEncryptedBitmaps()
            SmartspacerTargetProvider.notifyChange(context, AmazonTarget::class.java)
        }
    }

    init {
        setupDeliveries()
    }

}

interface AmazonPackageTrackingService {

    @GET("https://{subdomain}.amazon.com/DEANSExternalPackageLocationDetailsProxy/trackingObjectId/{trackingId}/clientName/AMZL")
    fun getTrackingInfo(
        @Path("subdomain") subdomain: String,
        @Path("trackingId") trackingId: String,
        @Header("Cookie") cookie: String,
        @Header("x-amzn-SessionId") sessionId: String
    ): Call<TrackingData>

}