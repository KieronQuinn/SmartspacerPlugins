package com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Base64
import androidx.annotation.CallSuper
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.internal.tapandpay.v1.valuables.CommonProto.GroupingInfo
import com.google.internal.tapandpay.v1.valuables.CommonProto.IssuerInfo
import com.google.internal.tapandpay.v1.valuables.CommonProto.Metadata
import com.google.internal.tapandpay.v1.valuables.CommonProto.RedemptionInfo
import com.google.internal.tapandpay.v1.valuables.EventTicketProto
import com.google.internal.tapandpay.v1.valuables.FlightProto
import com.google.internal.tapandpay.v1.valuables.GenericCardProto
import com.google.internal.tapandpay.v1.valuables.GiftCardProto
import com.google.internal.tapandpay.v1.valuables.HealthCardProto
import com.google.internal.tapandpay.v1.valuables.LoyaltyCardProto
import com.google.internal.tapandpay.v1.valuables.OfferProto
import com.google.internal.tapandpay.v1.valuables.SyncValuablesRequestProto.SyncValuablesRequest
import com.google.internal.tapandpay.v1.valuables.SyncValuablesRequestProto.SyncValuablesRequest.SyncValuablesRequestInner.Request
import com.google.internal.tapandpay.v1.valuables.TransitProto
import com.google.internal.tapandpay.v1.valuables.ValuableWrapperProto.ValuableWrapper
import com.google.internal.tapandpay.v1.valuables.ValuableWrapperProto.ValuableWrapper.ValuableCase
import com.google.protobuf.ByteString
import com.kieronquinn.app.smartspacer.plugin.googlewallet.extraproviders.BaseExtraProvider
import com.kieronquinn.app.smartspacer.plugin.googlewallet.model.database.WalletValuable
import com.kieronquinn.app.smartspacer.plugin.googlewallet.model.extras.TransitCardExtrasProto.TransitCardExtras
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.GoogleApiRepository.Scope
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.GoogleWalletRepository.SyncValuablesResult
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.GoogleWalletRepository.Valuable
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.GoogleWalletRepository.Valuable.RefreshPeriod
import com.kieronquinn.app.smartspacer.plugin.googlewallet.targets.GoogleWalletDynamicTarget
import com.kieronquinn.app.smartspacer.plugin.googlewallet.targets.GoogleWalletValuableTarget
import com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.extensions.compress
import com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.extensions.getRoundedBitmap
import com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.extensions.toColour
import com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.extensions.toDuration
import com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.extensions.toZonedDateTime
import com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.extensions.toZonedDateTimeOrNull
import com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.glide.WalletValuableImageTransformation
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.CONTENT_TYPE_PROTOBUF
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.toRequestBody
import com.kieronquinn.app.smartspacer.plugin.shared.utils.room.EncryptedValue
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt
import com.google.internal.tapandpay.v1.valuables.SyncValuablesResponseProto.SyncValuablesResponse as ProtoSyncValuablesResponse
import com.google.internal.tapandpay.v1.valuables.SyncValuablesResponseProto.SyncValuablesResponse.Inner.Valuables.Valuable as ProtoValuable

interface GoogleWalletRepository {

    /**
     *  Sync the valuables with the server to the local database. Does not return valuable list,
     *  see [getValuables] for that.
     */
    suspend fun syncValuables(): SyncValuablesResult

    /**
     *  Returns whether the valuables list has loaded from the database yet. If not, the action
     *  should silently fail and the Target be reloaded when they have been loaded.
     */
    fun hasLoadedValuables(): Boolean

    /**
     *  Current valuables from the database. Call [syncValuables] to trigger a sync.
     */
    fun getValuables(): Flow<List<Valuable>>

    /**
     *  Get a valuable from the database with the ID of [id].
     */
    fun getValuableById(id: String): Valuable?

    /**
     *  Called when a notification for a pass is received, this can be either new or a change to a
     *  notification.
     */
    fun onPassNotificationReceived()

    /**
     *  Returns a list of all active valuables which should be displayed in a Dynamic Target. This
     *  does not filter out dismissed valuables.
     */
    fun getActiveDynamicValuables(): List<Valuable>

    /**
     *  Returns the refresh period for all current dynamic valuables, using the shortest period
     *  of those currently displayed. For example, if there is a transit ticket for a train which
     *  has not yet departed and an event ticket in an hour, the refresh period for the transit
     *  ticket (expedited) will be used.
     */
    fun getDynamicValuableRefreshPeriod(): RefreshPeriod?

    fun refreshDynamicTargets(smartspacerIds: Array<String>)

    fun dismissTarget(smartspaceId: String)

    sealed class Valuable(
        open val id: String,
        open val hash: Long,
        open val metadata: Metadata?,
        open val image: ByteArray?,
        open val cardImage: ByteArray?,
        open val isDismissed: Boolean
    ) {

        data class LoyaltyCard(
            override val id: String,
            override val hash: Long,
            override val image: ByteArray?,
            override val cardImage: ByteArray?,
            override val isDismissed: Boolean,
            val proto: LoyaltyCardProto.LoyaltyCard
        ): Valuable(id, hash, proto.metadata, image, cardImage, isDismissed) {
            override fun getGroupingInfo(): GroupingInfo {
                return proto.groupingInfo
            }

            override fun getRedemptionInfo(): RedemptionInfo? {
                return proto.redemptionInfo
            }

            override fun getIssuerInfo(): IssuerInfo? {
                return proto.issuerInfo
            }

            override fun equals(other: Any?): Boolean {
                if(!super.equals(other)) return false
                other as LoyaltyCard
                return proto == other.proto
            }

            override fun hashCode(): Int {
                var result = super.hashCode()
                result = 31 * result + proto.hashCode()
                return result
            }
        }

        data class GiftCard(
            override val id: String,
            override val hash: Long,
            override val image: ByteArray?,
            override val cardImage: ByteArray?,
            override val isDismissed: Boolean,
            val proto: GiftCardProto.GiftCard
        ): Valuable(id, hash, proto.metadata, image, cardImage, isDismissed) {
            override fun getGroupingInfo(): GroupingInfo {
                return proto.groupingInfo
            }

            override fun getRedemptionInfo(): RedemptionInfo? {
                return proto.redemptionInfo
            }

            override fun getIssuerInfo(): IssuerInfo? {
                return proto.issuerInfo
            }

            override fun equals(other: Any?): Boolean {
                if(!super.equals(other)) return false
                other as GiftCard
                return proto == other.proto
            }

            override fun hashCode(): Int {
                var result = super.hashCode()
                result = 31 * result + proto.hashCode()
                return result
            }
        }

        data class Offer(
            override val id: String,
            override val hash: Long,
            override val image: ByteArray?,
            override val cardImage: ByteArray?,
            override val isDismissed: Boolean,
            val proto: OfferProto.Offer
        ): Valuable(id, hash, proto.metadata, image, cardImage, isDismissed) {
            override fun getGroupingInfo(): GroupingInfo {
                return proto.groupingInfo
            }

            override fun getRedemptionInfo(): RedemptionInfo? {
                return proto.redemptionInfo
            }

            override fun getIssuerInfo(): IssuerInfo? {
                return proto.issuerInfo
            }

            override fun equals(other: Any?): Boolean {
                if(!super.equals(other)) return false
                other as Offer
                return proto == other.proto
            }

            override fun hashCode(): Int {
                var result = super.hashCode()
                result = 31 * result + proto.hashCode()
                return result
            }
        }

        data class EventTicket(
            override val id: String,
            override val hash: Long,
            override val image: ByteArray?,
            override val cardImage: ByteArray?,
            override val isDismissed: Boolean,
            val proto: EventTicketProto.EventTicket
        ): Valuable(id, hash, proto.metadata, image, cardImage, isDismissed) {
            override fun getGroupingInfo(): GroupingInfo {
                return proto.groupingInfo
            }

            override fun getRedemptionInfo(): RedemptionInfo? {
                return proto.redemptionInfo
            }

            override fun getIssuerInfo(): IssuerInfo? {
                return proto.issuerInfo
            }

            override fun equals(other: Any?): Boolean {
                if(!super.equals(other)) return false
                other as EventTicket
                return proto == other.proto
            }

            override fun hashCode(): Int {
                var result = super.hashCode()
                result = 31 * result + proto.hashCode()
                return result
            }
        }

        data class Flight(
            override val id: String,
            override val hash: Long,
            override val image: ByteArray?,
            override val cardImage: ByteArray?,
            override val isDismissed: Boolean,
            val proto: FlightProto.Flight
        ): Valuable(id, hash, proto.metadata, image, cardImage, isDismissed) {
            override fun getGroupingInfo(): GroupingInfo {
                return proto.groupingInfo
            }

            override fun getRedemptionInfo(): RedemptionInfo? {
                return proto.redemptionInfo
            }

            override fun getIssuerInfo(): IssuerInfo? {
                return proto.issuerInfo
            }

            override fun equals(other: Any?): Boolean {
                if(!super.equals(other)) return false
                other as Flight
                return proto == other.proto
            }

            override fun hashCode(): Int {
                var result = super.hashCode()
                result = 31 * result + proto.hashCode()
                return result
            }
        }

        data class TransitCard(
            override val id: String,
            override val hash: Long,
            override val image: ByteArray?,
            override val cardImage: ByteArray?,
            override val isDismissed: Boolean,
            val proto: TransitProto.TransitCard,
            val extras: TransitCardExtras?
        ): Valuable(id, hash, proto.metadata, image, cardImage, isDismissed) {
            override fun getGroupingInfo(): GroupingInfo {
                return proto.groupingInfo
            }

            override fun getRedemptionInfo(): RedemptionInfo? {
                return proto.redemptionInfo
            }

            override fun getIssuerInfo(): IssuerInfo? {
                return proto.issuerInfo
            }

            override fun equals(other: Any?): Boolean {
                if(!super.equals(other)) return false
                other as TransitCard
                if (proto != other.proto) return false
                if (extras != other.extras) return false
                return true
            }

            override fun hashCode(): Int {
                var result = super.hashCode()
                result = 31 * result + proto.hashCode()
                result = 31 * result + extras.hashCode()
                return result
            }
        }

        data class HealthCard(
            override val id: String,
            override val hash: Long,
            override val image: ByteArray?,
            override val cardImage: ByteArray?,
            override val isDismissed: Boolean,
            val proto: HealthCardProto.HealthCard
        ): Valuable(id, hash, null, image, cardImage, isDismissed) {
            override fun getGroupingInfo(): GroupingInfo? {
                return null
            }

            override fun getRedemptionInfo(): RedemptionInfo? {
                return null
            }

            override fun getIssuerInfo(): IssuerInfo? {
                return null
            }

            override fun equals(other: Any?): Boolean {
                if(!super.equals(other)) return false
                other as HealthCard
                return proto == other.proto
            }

            override fun hashCode(): Int {
                var result = super.hashCode()
                result = 31 * result + proto.hashCode()
                return result
            }
        }

        data class GenericCard(
            override val id: String,
            override val hash: Long,
            override val image: ByteArray?,
            override val cardImage: ByteArray?,
            override val isDismissed: Boolean,
            val proto: GenericCardProto.GenericCard
        ): Valuable(id, hash, proto.metadata, image, cardImage, isDismissed) {
            override fun getGroupingInfo(): GroupingInfo? {
                return proto.groupingInfo
            }

            override fun getRedemptionInfo(): RedemptionInfo? {
                return proto.redemptionInfo
            }

            override fun getIssuerInfo(): IssuerInfo? {
                return proto.issuerInfo
            }

            override fun equals(other: Any?): Boolean {
                if(!super.equals(other)) return false
                other as GenericCard
                return proto == other.proto
            }

            override fun hashCode(): Int {
                var result = super.hashCode()
                result = 31 * result + proto.hashCode()
                return result
            }
        }

        data class SensitiveGenericPass(
            override val id: String,
            override val hash: Long,
            override val image: ByteArray?,
            override val cardImage: ByteArray?,
            override val isDismissed: Boolean,
            val proto: GenericCardProto.GenericCard
        ): Valuable(id, hash, proto.metadata, image, cardImage, isDismissed) {
            override fun getGroupingInfo(): GroupingInfo? {
                return proto.groupingInfo
            }

            override fun getRedemptionInfo(): RedemptionInfo? {
                return proto.redemptionInfo
            }

            override fun getIssuerInfo(): IssuerInfo? {
                return proto.issuerInfo
            }

            override fun equals(other: Any?): Boolean {
                if(!super.equals(other)) return false
                other as SensitiveGenericPass
                return proto == other.proto
            }

            override fun hashCode(): Int {
                var result = super.hashCode()
                result = 31 * result + proto.hashCode()
                return result
            }
        }

        enum class RefreshPeriod {

            /**
             *  Refreshes more often if possible (defaults to every minute). Used when there's a
             *  transit or flight valuable before departure when changes are important.
             */
            EXPEDITED,

            /**
             *  Refreshes periodically, user-specified time (defaults to every 15 minutes). Used
             *  when there's a transit or flight valuable after departure, when changes are less
             *  important.
             */
            PERIODIC,

            /**
             *  All displayed valuables are static, no need to refresh for new data
             */
            NO_REFRESH
        }

        abstract fun getGroupingInfo(): GroupingInfo?
        abstract fun getRedemptionInfo(): RedemptionInfo?
        abstract fun getIssuerInfo(): IssuerInfo?

        @CallSuper
        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + hash.hashCode()
            result = 31 * result + (image?.contentHashCode() ?: 0)
            result = 31 * result + (cardImage?.contentHashCode() ?: 0)
            result = 31 * result + isDismissed.hashCode()
            return result
        }

        @CallSuper
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Valuable

            if (id != other.id) return false
            if (hash != other.hash) return false
            if (image != null) {
                if (other.image == null) return false
                if (!image.contentEquals(other.image)) return false
            } else if (other.image != null) return false
            if (cardImage != null) {
                if (other.cardImage == null) return false
                if (!cardImage.contentEquals(other.cardImage)) return false
            } else if (other.cardImage != null) return false
            if (isDismissed != other.isDismissed) return false
            return true
        }
    }

    enum class SyncValuablesResult {
        /**
         *  Sync was successful
         */
        SUCCESS,

        /**
         *  A non-fatal error, for example a network connection issue, occurred
         */
        ERROR,

        /**
         *  The request returned a bad auth response, and then token refresh also failed. Likely
         *  will require a re-log.
         */
        FATAL_ERROR
    }

}

class GoogleWalletRepositoryImpl(
    private val context: Context,
    private val encryptedSettingsRepository: EncryptedSettingsRepository,
    private val googleApiRepository: GoogleApiRepository,
    private val databaseRepository: DatabaseRepository
): GoogleWalletRepository {

    companion object {
        //Unsure what this represents but it makes a request get all cards (possibly page data?)
        private const val HEADER_REQUEST = "ExoBBA8FCxQMCAcJAwYOCg0QEhYYHA=="

        /**
         *  The additional leeway to give for departures and arrivals when expediting refreshes,
         *  to account for potential last minute slippage. After this time, if the item has not
         *  slipped further, the Target will disappear.
         */
        private const val OFFSET_DEPARTURE_ARRIVAL_MINUTES = 10L
    }

    private val glide = Glide.with(context)
    private val scope = MainScope()

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val service = Retrofit.Builder()
        .baseUrl(GoogleWalletService.BASE_URL)
        .build()
        .create(GoogleWalletService::class.java)

    private val valuables = databaseRepository.getWalletValuables()
        .map { it.toValuables() }
        .flowOn(Dispatchers.IO)
        .onEach { onValuablesChanged() }
        .stateIn(scope, SharingStarted.Eagerly, null)

    override suspend fun syncValuables() = withContext(Dispatchers.IO) {
        syncValuablesInternal()
    }

    override fun hasLoadedValuables(): Boolean {
        return valuables.value != null
    }

    override fun getValuableById(id: String): Valuable? {
        return valuables.value?.firstOrNull { it.id == id }
    }

    override fun getValuables(): Flow<List<Valuable>> {
        return valuables.map { it ?: emptyList() }
    }

    private fun List<WalletValuable>.toValuables() = mapNotNull {
        val extras = it.extras?.bytes
        it.toValuable(extras)
    }

    private fun WalletValuable.toValuable(extras: ByteArray? = null): Valuable? {
        val valuable = ValuableWrapper.parseFrom(valuable.bytes)
        return when(valuable.valuableCase) {
            ValuableCase.LOYALTY_CARD -> {
                Valuable.LoyaltyCard(
                    id, hash, image?.bytes, cardImage?.bytes, isDismissed, valuable.loyaltyCard
                )
            }
            ValuableCase.GIFT_CARD -> {
                Valuable.GiftCard(
                    id, hash, image?.bytes, cardImage?.bytes, isDismissed, valuable.giftCard
                )
            }
            ValuableCase.OFFER -> {
                Valuable.Offer(
                    id, hash, image?.bytes, cardImage?.bytes, isDismissed, valuable.offer
                )
            }
            ValuableCase.EVENT_TICKET -> {
                Valuable.EventTicket(
                    id, hash, image?.bytes, cardImage?.bytes, isDismissed, valuable.eventTicket
                )
            }
            ValuableCase.FLIGHT -> {
                Valuable.Flight(
                    id, hash, image?.bytes, cardImage?.bytes, isDismissed, valuable.flight
                )
            }
            ValuableCase.TRANSIT_CARD -> {
                val transitExtras = extras?.let { TransitCardExtras.parseFrom(it) }
                Valuable.TransitCard(
                    id,
                    hash,
                    image?.bytes,
                    cardImage?.bytes,
                    isDismissed,
                    valuable.transitCard,
                    transitExtras
                )
            }
            ValuableCase.HEALTH_CARD -> {
                Valuable.HealthCard(
                    id, hash, image?.bytes, cardImage?.bytes, isDismissed, valuable.healthCard
                )
            }
            ValuableCase.GENERIC_CARD -> {
                Valuable.GenericCard(
                    id, hash, image?.bytes, cardImage?.bytes, isDismissed, valuable.genericCard
                )
            }
            ValuableCase.SENSITIVE_GENERIC_PASS -> {
                Valuable.SensitiveGenericPass(
                    id,
                    hash,
                    image?.bytes,
                    cardImage?.bytes,
                    isDismissed,
                    valuable.sensitiveGenericPass
                )
            }
            else -> null
        }
    }

    private suspend fun syncValuablesInternal(isRetry: Boolean = false): SyncValuablesResult {
        val token = getToken() ?: return SyncValuablesResult.FATAL_ERROR
        val result = loadValuables(token)
        return when {
            result == SyncValuablesResult.FATAL_ERROR && !isRetry -> {
                encryptedSettingsRepository.walletToken.clear()
                syncValuablesInternal(true)
            }
            else -> result
        }
    }

    private suspend fun loadValuables(token: String): SyncValuablesResult {
        val valuables = service.run {
            syncValuables(token, getCurrentValuables())
        }
        return when(valuables) {
            is SyncValuablesResponse.Success -> {
                val bytes = valuables.body.bytes()
                val response = ProtoSyncValuablesResponse.parseFrom(bytes)
                commitValuables(response)
                SyncValuablesResult.SUCCESS
            }
            is SyncValuablesResponse.GenericError -> SyncValuablesResult.ERROR
            is SyncValuablesResponse.BadAuthentication -> SyncValuablesResult.FATAL_ERROR
        }
    }

    private suspend fun commitValuables(response: ProtoSyncValuablesResponse) {
        val valuableList = response.inner.valuables.valuableList
        val currentValuables = valuables.value ?: emptyList()
        valuableList.forEach {
            if(it.hash != 0L) {
                val images = getImages(it.valuable)
                val currentValuable = currentValuables.firstOrNull { valuable ->
                    valuable.id == it.id
                }
                val valuable = WalletValuable(
                    it.id,
                    it.hash,
                    EncryptedValue(it.valuable.toByteArray()),
                    null,
                    images?.first?.let { image -> EncryptedValue(image) },
                    images?.second?.let { image -> EncryptedValue(image) },
                    isDismissed = currentValuable?.isDismissed ?: false
                )
                databaseRepository.addWalletValuable(valuable)
            }else{
                databaseRepository.deleteWalletValuable(it.id)
            }
        }
        val updatableValuables = getCurrentWalletValuables()
        updatableValuables.forEach {
            val valuable = it.toValuable(it.extras?.bytes) ?: return@forEach
            if(!valuable.isActive() || valuable.isDismissed) return@forEach
            val newValuable = it.copy(
                extras = valuable.getExtras(valuable)?.let { extras -> EncryptedValue(extras) }
            )
            databaseRepository.addWalletValuable(newValuable)
        }
        if(valuableList.isNotEmpty() || updatableValuables.isNotEmpty()){
            SmartspacerTargetProvider.notifyChange(context, GoogleWalletDynamicTarget::class.java)
        }
    }

    private suspend fun Valuable.getExtras(currentValuable: Valuable?): ByteArray? {
        return BaseExtraProvider.getExtrasForValuable(this, currentValuable)?.toByteArray()
    }

    private fun Valuable.getRefreshPeriod(time: ZonedDateTime): RefreshPeriod? {
        return BaseExtraProvider.getRefreshPeriod(this, time)
    }

    private suspend fun getCurrentWalletValuables(): List<WalletValuable> {
        return withContext(Dispatchers.IO){
            databaseRepository.getWalletValuables().first()
        }
    }

    private suspend fun getImages(
        valuable: ValuableWrapper
    ): Pair<ByteArray, ByteArray>? = withContext(Dispatchers.IO) {
        val groupingInfo = valuable.getGroupingInfo() ?: return@withContext null
        val url = groupingInfo.groupingImage.uri
        val backgroundColour = groupingInfo.backgroundColor.toColour()
        val image = try {
            glide.asBitmap()
                .load(url)
                .submit()
                .get()
        }catch (e: Exception){
            return@withContext null
        }
        val cardImage = glide.asBitmap()
            .load(image)
            .transform(
                WalletValuableImageTransformation(backgroundColour),
                RoundedCorners((image.height / 10f).roundToInt())
            )
            .submit()
            .get()
            .compress() ?: return@withContext null
        val roundedImage = image.getRoundedBitmap()
        Pair(
            roundedImage.compress() ?: return@withContext null,
            cardImage
        ).also {
            roundedImage.recycle()
        }
    }

    private fun ValuableWrapper.getGroupingInfo(): GroupingInfo? {
        return when(valuableCase) {
            ValuableCase.LOYALTY_CARD -> {
                loyaltyCard.groupingInfo
            }
            ValuableCase.GIFT_CARD -> {
                giftCard.groupingInfo
            }
            ValuableCase.OFFER -> {
                offer.groupingInfo
            }
            ValuableCase.EVENT_TICKET -> {
                eventTicket.groupingInfo
            }
            ValuableCase.FLIGHT -> {
                flight.groupingInfo
            }
            ValuableCase.TRANSIT_CARD -> {
                transitCard.groupingInfo
            }
            ValuableCase.HEALTH_CARD -> {
                null //Not currently implemented
            }
            ValuableCase.GENERIC_CARD -> {
                genericCard.groupingInfo
            }
            ValuableCase.SENSITIVE_GENERIC_PASS -> {
                sensitiveGenericPass.groupingInfo
            }
            else -> null
        }
    }

    private suspend fun getToken(): String? {
        val token = encryptedSettingsRepository.walletToken.get()
        if(token.isNotEmpty()) return token
        return googleApiRepository.getToken(Scope.WALLET)?.also {
            encryptedSettingsRepository.walletToken.set(it)
        }
    }

    private suspend fun getCurrentValuables(): List<ProtoValuable> {
        return databaseRepository.getWalletValuables().first().map {
            ProtoValuable.newBuilder()
                .setId(it.id)
                .setHash(it.hash)
                .build()
        }
    }

    private fun createSyncValuablesRequest(currentValuables: List<ProtoValuable>): SyncValuablesRequest {
        val request = SyncValuablesRequest.SyncValuablesRequestInner.newBuilder()
            .setRequest(buildRequest(currentValuables))
            .build()
        return SyncValuablesRequest.newBuilder()
            .setRequest(request)
            .build()
    }

    private fun buildRequest(currentValuables: List<ProtoValuable>): Request {
        val cachedValuables = currentValuables.map {
            Request.CachedValuable.newBuilder()
                .setId(it.id)
                .setHash(it.hash)
                .build()
        }
        return Request.newBuilder()
            .setHeader(ByteString.copyFrom(Base64.decode(HEADER_REQUEST, Base64.DEFAULT)))
            .setTimezone(ZoneId.systemDefault().getDisplayName(
                TextStyle.FULL_STANDALONE, Locale.ENGLISH
            ))
            .addAllCachedValuable(cachedValuables)
            .build()
    }

    private fun GoogleWalletService.syncValuables(
        token: String,
        currentValuables: List<ProtoValuable>
    ): SyncValuablesResponse {
        val requestBody = createSyncValuablesRequest(currentValuables).toByteArray()
            .toRequestBody(CONTENT_TYPE_PROTOBUF)
        return try {
            val response = syncValuables("Bearer $token", body = requestBody).execute()
            when(response.code()){
                200 -> {
                    response.body()?.let { body ->
                        SyncValuablesResponse.Success(body)
                    } ?: SyncValuablesResponse.GenericError
                }
                401 -> SyncValuablesResponse.BadAuthentication
                else -> SyncValuablesResponse.GenericError
            }
        }catch (e: Exception){
            SyncValuablesResponse.GenericError
        }
    }

    override fun onPassNotificationReceived() {
        scope.launch {
            syncValuables()
        }
    }

    override fun getActiveDynamicValuables(): List<Valuable> {
        return valuables.value?.filter {
            it.isActive() && !it.isDismissed
        } ?: emptyList()
    }

    override fun getDynamicValuableRefreshPeriod(): RefreshPeriod? {
        return getActiveDynamicValuables().minOfOrNull {
            it.getRefreshPeriod()
        }
    }

    override fun refreshDynamicTargets(smartspacerIds: Array<String>) {
        scope.launch {
            if(shouldAbortLoading()) {
                return@launch
            }
            syncValuables()
        }
        smartspacerIds.forEach {
            SmartspacerTargetProvider.notifyChange(
                context, GoogleWalletDynamicTarget::class.java, it
            )
        }
    }

    private suspend fun shouldAbortLoading(): Boolean {
        return connectivityManager.activeNetwork?.let {
            val capabilities = connectivityManager.getNetworkCapabilities(it) ?: return false
            //Disallow connections with no internet
            if(!capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                return false
            }
            //If metered connection is allowed, never abort loading
            if(!encryptedSettingsRepository.reloadOnMeteredConnection.get()) return false
            //Always allow mobile networks since they are "always" metered
            if(!capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return false
            //Abort on metered connections
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
        } ?: return false
    }

    override fun dismissTarget(smartspaceId: String) {
        scope.launch {
            val valuableId = smartspaceId.removePrefix(GoogleWalletDynamicTarget.ID_PREFIX)
            val valuable = databaseRepository.getWalletValuableById(valuableId).first()
                ?: return@launch
            val dismissedValuable = valuable.copy(isDismissed = true)
            databaseRepository.addWalletValuable(dismissedValuable)
        }
    }

    private fun Valuable.isActive(): Boolean {
        if(metadata?.isActive == false) return false
        val now = ZonedDateTime.now()
        return when(this) {
            is Valuable.Flight -> {
                //A flight is active between the notification start time and actual landing time
                val start = proto.upcomingFlightNotification.dateTimeToShow.toZonedDateTimeOrNull()
                    ?: return false
                if(start.isAfter(now)) return false
                val flightStatus = proto.googleFlightStatus ?: proto.airlineFlightStatus
                val end = (flightStatus.actualArrival ?: flightStatus.scheduledArrival)
                    .toZonedDateTimeOrNull() ?: return false
                val adjustedEnd = end + Duration.ofMinutes(OFFSET_DEPARTURE_ARRIVAL_MINUTES)
                adjustedEnd.isAfter(now) && proto.metadata.isActive
            }
            is Valuable.TransitCard -> {
                //A transit card must have a notification for this to work
                val start = proto.upcomingTransitNotification.dateTimeToShow.toZonedDateTimeOrNull()
                    ?: return false
                val scheduledArrival = if(proto.hasArrivalTime()) {
                    proto.arrivalTime.toZonedDateTimeOrNull()
                } else null
                val arrival = BaseExtraProvider.getEndTimeForValuable(this)
                    ?: scheduledArrival
                    ?: return false
                val adjustedArrival = arrival + Duration.ofMinutes(OFFSET_DEPARTURE_ARRIVAL_MINUTES)
                if(now.isBefore(start)) return false //Too early
                if(now.isAfter(adjustedArrival)) return false //Too late
                return true
            }
            is Valuable.EventTicket -> {
                //An event ticket is active between the notification start time and notif/event end
                val start = proto.upcomingEventNotification.dateTimeToShow.toZonedDateTimeOrNull()
                    ?: return false
                if(start.isAfter(now)) return false
                val end = proto.dateTime.end?.toZonedDateTimeOrNull()
                    ?: (start + proto.upcomingEventNotification.timeoutAfter.toDuration())
                end.isAfter(now)
            }
            is Valuable.Offer -> {
                //An offer is active between the first notification start and expiration time
                val start = proto.notificationsList.minByOrNull {
                    it.dateTimeToShow.toZonedDateTimeOrNull()?.toEpochSecond() ?: Long.MAX_VALUE
                }?.dateTimeToShow?.toZonedDateTimeOrNull() ?: return false
                if(start.isAfter(now)) return false
                val end = proto.expirationDateTime
                end.toZonedDateTimeOrNull()?.isAfter(now) ?: false
            }
            else -> false //No other types can be active right now
        }
    }

    private fun Valuable.getRefreshPeriod(): RefreshPeriod {
        val now = ZonedDateTime.now()
        return getRefreshPeriod(now) ?: when(this) {
            is Valuable.Flight -> {
                //For flights, we expedite if not departed yet and periodically sync after
                val start = proto.upcomingFlightNotification.dateTimeToShow.toZonedDateTimeOrNull()
                    ?: return RefreshPeriod.NO_REFRESH
                if(start.isAfter(now)) return RefreshPeriod.NO_REFRESH
                val flightStatus = proto.googleFlightStatus ?: proto.airlineFlightStatus
                val departure = flightStatus.actualDeparture.toZonedDateTimeOrNull()
                    ?: flightStatus.scheduledDeparture.toZonedDateTime()
                val offsetDeparture = departure.plusMinutes(OFFSET_DEPARTURE_ARRIVAL_MINUTES)
                if(offsetDeparture.isAfter(now)) RefreshPeriod.EXPEDITED else RefreshPeriod.PERIODIC
            }
            is Valuable.TransitCard -> {
                //For transit cards, same behaviour as flights
                val start = proto.upcomingTransitNotification.dateTimeToShow.toZonedDateTimeOrNull()
                    ?: return RefreshPeriod.NO_REFRESH
                if(start.isAfter(now)) return RefreshPeriod.NO_REFRESH
                if(!proto.hasDepartureTime()) return RefreshPeriod.NO_REFRESH
                val departure = proto.departureTime.toZonedDateTime()
                val offsetDeparture = departure.plusMinutes(OFFSET_DEPARTURE_ARRIVAL_MINUTES)
                if(offsetDeparture.isAfter(now)) {
                    RefreshPeriod.EXPEDITED
                }else RefreshPeriod.PERIODIC
            }
            //Other valuable types (including offers & event tickets) are static as data can't change
            else -> RefreshPeriod.NO_REFRESH
        }
    }

    private fun onValuablesChanged() {
        SmartspacerTargetProvider.notifyChange(context, GoogleWalletValuableTarget::class.java)
        SmartspacerTargetProvider.notifyChange(context, GoogleWalletDynamicTarget::class.java)
    }

    sealed class SyncValuablesResponse {
        data class Success(val body: ResponseBody): SyncValuablesResponse()
        object BadAuthentication: SyncValuablesResponse()
        object GenericError: SyncValuablesResponse()
    }

}

interface GoogleWalletService {

    companion object {
        const val BASE_URL = "https://pay-users-pa.googleapis.com/"
    }

    @POST("g/valuables/syncvaluables")
    fun syncValuables(
        @Header("Authorization") authorization: String,
        @Body body: RequestBody
    ): Call<ResponseBody>

}