package com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.datasources

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import com.kieronquinn.app.smartspacer.plugin.googlewallet.model.Result
import com.kieronquinn.app.smartspacer.plugin.googlewallet.model.datasources.trainline.SearchLocationsResponse
import com.kieronquinn.app.smartspacer.plugin.googlewallet.model.datasources.trainline.SearchRoutesRequest
import com.kieronquinn.app.smartspacer.plugin.googlewallet.model.datasources.trainline.SearchRoutesResponse
import com.kieronquinn.app.smartspacer.plugin.googlewallet.model.datasources.trainline.SearchRoutesResponse.Leg
import com.kieronquinn.app.smartspacer.plugin.googlewallet.model.datasources.trainline.SearchRoutesResponse.OutwardJourney
import com.kieronquinn.app.smartspacer.plugin.googlewallet.model.datasources.trainline.SetupRequest
import com.kieronquinn.app.smartspacer.plugin.googlewallet.model.datasources.trainline.Station
import com.kieronquinn.app.smartspacer.plugin.googlewallet.model.datasources.trainline.TravelServiceInformationResponse
import com.kieronquinn.app.smartspacer.plugin.googlewallet.model.extras.TransitCardExtrasProto.TransitCardExtras
import com.kieronquinn.app.smartspacer.plugin.googlewallet.model.extras.TransitCardExtrasProto.TransitCardExtras.TransitLeg
import com.kieronquinn.app.smartspacer.plugin.googlewallet.model.extras.TransitCardExtrasProto.TransitCardExtras.TransitLeg.Mode
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.datasources.TrainlineService.Companion.HEADER_CONTEXT_ID
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.datasources.TrainlineService.Companion.HEADER_CONVERSATION_ID
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.datasources.TrainlineService.Companion.getUserAgentHeader
import com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.extensions.atUtc
import com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.extensions.getArrival
import com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.extensions.getDeparture
import com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.extensions.toDateTime
import com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.extensions.toZonedDateTime
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.getPackageInfoCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import okhttp3.Headers as ResponseHeaders

interface TrainlineRepository {

    suspend fun getTransitCardExtrasForTrain(
        fromStation: String,
        toStation: String,
        departsAt: ZonedDateTime,
        arrivesAt: ZonedDateTime,
        existing: TransitCardExtras? = null
    ): Result<TransitCardExtras>

}

class TrainlineRepositoryImpl(context: Context): TrainlineRepository {

    companion object {
        private const val PACKAGE_NAME_TRAINLINE = "com.thetrainline"
        //Fallback for if app isn't installed that we can pull version data from
        private const val FALLBACK_VERSION_NAME = "246.0.0.96799"
        private const val FALLBACK_VERSION_CODE = 1196799L

        //Pulled from latest APK, may change in future
        private val CONNECTIONS = arrayOf(
            "urn:trainline:connection:atoc",
            "urn:trainline:connection:eurostardirect",
            "urn:trainline:connection:pao_sncf",
            "urn:trainline:connection:pao_ouigo",
            "urn:trainline:connection:benerail",
            "urn:trainline:connection:renfe",
            "urn:trainline:connection:trenitalia",
            "urn:trainline:connection:ntv",
            "urn:trainline:connection:obb",
            "urn:trainline:connection:cff",
            "urn:trainline:connection:westbahn",
            "urn:trainline:connection:db",
            "urn:trainline:connection:distribusion",
            "urn:trainline:connection:busbud",
            "urn:trainline:connection:flixbus_affiliate",
            "urn:trainline:connection:ilsa"
        )

        /**
         *  Trainline uses three modes:
         *  - Train: Main, used for trains
         *  - Bus: Used for bus replacements
         *  - Walk: Used for walking between stations
         */
        private val MODES = mapOf(
            "tramod:train" to Mode.TRAIN,
            "tramod:bus" to Mode.BUS,
            "tramod:walk" to Mode.WALK
        )
    }

    private val trainlineService = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(TrainlineService.BASE_URL)
        .build()
        .create<TrainlineService>()

    private var contextId: String? = null
    private var conversationId: String? = null

    private val securityHeaderLock = Mutex()
    private val packageManager = context.packageManager

    private val appVersion by lazy {
        getTrainlinePackageInfo()?.versionName ?: FALLBACK_VERSION_NAME
    }

    private val appVersionCode by lazy {
        getTrainlinePackageInfo()?.longVersionCode ?: FALLBACK_VERSION_CODE
    }

    override suspend fun getTransitCardExtrasForTrain(
        fromStation: String,
        toStation: String,
        departsAt: ZonedDateTime,
        arrivesAt: ZonedDateTime,
        existing: TransitCardExtras?
    ): Result<TransitCardExtras> {
        return if(existing != null) {
            //A failed update response can indicate a partial fail, but the cache may be updated
            getTransitCardExtrasForTrain(existing)
        }else{
            //Getting the initial can fail or have network issues, so return a response
            getTransitCardExtrasForTrain(fromStation, toStation, departsAt, arrivesAt)
        }
    }

    private suspend fun getTransitCardExtrasForTrain(
        fromStation: String,
        toStation: String,
        departsAt: ZonedDateTime,
        arrivesAt: ZonedDateTime
    ): Result<TransitCardExtras> {
        val extras = TransitCardExtras.newBuilder()
            .setServiceId("$fromStation:$toStation:$departsAt:$arrivesAt")
        val from = when(val station = getStationForName(fromStation)){
            is Result.Success -> station.data
            is Result.Failed -> return station.mutate()
        }
        val to = when(val station = getStationForName(toStation)){
            is Result.Success -> station.data
            is Result.Failed -> return station.mutate()
        }
        val journey = when(val exact = getExactJourney(from.code, to.code, departsAt, arrivesAt)) {
            is Result.Success -> exact.data
            is Result.Failed -> return exact.mutate()
        }
        val statuses = journey.legs.associate { leg ->
            if(leg.timetableId != null) {
                getTrackingInfo(
                    leg.carrier.code,
                    leg.timetableId,
                    leg.departureStation.code,
                    leg.localDepartAt.parseDateTime(),
                    leg.arrivalStation.code,
                    leg.localArriveAt.parseDateTime()
                ).let {
                    Pair(leg, it.unwrap())
                }
            }else{
                Pair(leg, null)
            }
        }
        return extras.apply {
            serviceId = "$from;$to;$departsAt;$arrivesAt"
            val legs = statuses.map { it.toTransitLeg() }
            addAllLeg(legs)
            impossibleLegChange = legs.getImpossibleLegChangeIndex()
        }.build().let {
            Result.Success(it)
        }
    }

    private suspend fun getTransitCardExtrasForTrain(
        existing: TransitCardExtras
    ): Result<TransitCardExtras> {
        //We only need to update each of the legs with realtime, since we already have the info
        var updateSucceeded = true
        val extras = existing.toBuilder().apply {
            legList.forEachIndexed { index, transitLeg ->
                val newLeg = when(val response = transitLeg.update()){
                    is Result.Success -> response.data
                    is Result.Failed -> {
                        updateSucceeded = false
                        response.cached
                    }
                } ?: return@forEachIndexed //Already set to false in block
                setLeg(index, newLeg)
            }
            impossibleLegChange = legList.getImpossibleLegChangeIndex()
        }.build()
        //If one or more legs failed updating, return Failed but with the partial update as cache
        return if(updateSucceeded){
            Result.Success(extras)
        }else{
            Result.Failed(cached = extras)
        }
    }

    private suspend fun TransitLeg.update(): Result<TransitLeg> {
        //Skip if realtime not available, treat as a success
        if(!hasRealtime) return Result.Success(this)
        val tracking = getTrackingInfo(
            operatorId,
            serviceId,
            originId,
            scheduledDeparture.toZonedDateTime(),
            destinationId,
            scheduledArrival.toZonedDateTime()
        )
        return when(tracking) {
            is Result.Success -> {
                val leg = toBuilder()
                    .mergeFrom(tracking.data.toTransitLeg(serviceId, operatorId, operator))
                    .build()
                Result.Success(leg)
            }
            is Result.Failed -> {
                Result.Failed(tracking.code, this)
            }
        }
    }

    private suspend fun getExactJourney(
        from: String,
        to: String,
        departsAt: ZonedDateTime,
        arrivesAt: ZonedDateTime
    ): Result<OutwardJourney> {
        getContextHeaders()
        val response = trainlineService.call {
            trainlineService.searchRoutes(from, to, departsAt)
        }
        val journeys = when(response) {
            is Result.Success -> response.data
            is Result.Failed -> return response.mutate()
        }
        return journeys.outwardJourneys.firstOrNull {
            it.departAtTimestamp.parseDateTime() == departsAt
                    && it.arriveAtTimestamp.parseDateTime() == arrivesAt
        }?.let {
            Result.Success(it)
        } ?: Result.Failed(404)
    }

    private suspend fun getStationForName(name: String): Result<Station> {
        getContextHeaders()
        val response = trainlineService.call {
            searchLocations(name)
        }
        val searchLocations = when(response){
            is Result.Success -> response.data
            is Result.Failed -> return response.mutate()
        }
        //Prefer an exact match, which may not be first, if it exists
        val bestLocation = searchLocations.searchLocations
            .firstOrNull { it.name == name } ?: searchLocations.searchLocations.firstOrNull()
        return bestLocation?.let {
            Result.Success(Station(it.name, it.code, it.timezone))
        } ?: Result.Failed(404)
    }

    private suspend fun getTrackingInfo(
        carrier: String,
        serviceId: String,
        origin: String,
        departureTime: ZonedDateTime,
        destination: String,
        arrivalTime: ZonedDateTime
    ): Result<TravelServiceInformationResponse> {
        getContextHeaders()
        return trainlineService.call {
            getTracking(carrier, serviceId, departureTime, arrivalTime, origin, destination)
        }
    }

    /**
     *  Makes sure the context headers required for requests are loaded by pinging the setup
     *  endpoint with random UUIDs. The response from this endpoint contains valid IDs which we
     *  can then use to make further requests. They're only stored in memory to reduce the
     *  likelihood of age-related issues (the actual Trainline app does the same)
     */
    private suspend fun getContextHeaders(): Boolean {
        return securityHeaderLock.withLock {
            if(contextId != null && conversationId != null) return@withLock true
            val headers = trainlineService.getHeaders {
                getSecurityHeaders()
            }.unwrap() ?: return@withLock false
            conversationId = headers[HEADER_CONVERSATION_ID] ?: return@withLock false
            contextId = headers[HEADER_CONTEXT_ID] ?: return@withLock false
            true
        }
    }

    private suspend fun <T> TrainlineService.call(
        block: TrainlineService.() -> Call<T>
    ): Result<T> {
        return withContext(Dispatchers.IO){
            val result = try {
                block(this@call).execute()
            }catch (e: Exception) {
                return@withContext Result.Failed(999)
            }
            val body = result.body()
            if(result.isSuccessful && body != null) {
                Result.Success(body)
            }else{
                Result.Failed(result.code())
            }
        }
    }

    private suspend fun <T> TrainlineService.getHeaders(
        block: TrainlineService.() -> Call<T>
    ): Result<ResponseHeaders> {
        return withContext(Dispatchers.IO){
            val result = try {
                block(this@getHeaders).execute()
            }catch (e: Exception){
                return@withContext Result.Failed(999)
            }
            if(result.isSuccessful) {
                Result.Success(result.headers())
            }else{
                Result.Failed(result.code())
            }
        }
    }

    private fun getTrainlinePackageInfo(): PackageInfo? {
        return try {
            packageManager.getPackageInfoCompat(PACKAGE_NAME_TRAINLINE)
        }catch (e: NameNotFoundException){
            null
        }
    }

    private fun TrainlineService.getSecurityHeaders() = getSecurityHeaders(
        appVersion,
        getUserAgentHeader(appVersion, appVersionCode)
    )

    private fun TrainlineService.searchLocations(
        searchTerm: String
    ) = searchLocations(
        appVersion,
        getUserAgentHeader(appVersion, appVersionCode),
        conversationId!!,
        contextId!!,
        connection = CONNECTIONS,
        searchTerm = searchTerm
    )

    private fun TrainlineService.searchRoutes(
        origin: String,
        destination: String,
        at: ZonedDateTime
    ) = searchRoutes(
        appVersion,
        getUserAgentHeader(appVersion, appVersionCode),
        conversationId!!,
        contextId!!,
        request = SearchRoutesRequest(
            origin = origin,
            destination = destination,
            outwardJourney = SearchRoutesRequest.OutwardJourney(
                dateTime = at.atUtc().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            ),
            connections = CONNECTIONS.map { SearchRoutesRequest.Connection(it) }
        )
    )

    private fun TrainlineService.getTracking(
        carrier: String,
        serviceId: String,
        departureDate: ZonedDateTime,
        arrivalDate: ZonedDateTime,
        origin: String,
        destination: String
    ) = getTracking(
        appVersion,
        getUserAgentHeader(appVersion, appVersionCode),
        conversationId!!,
        contextId!!,
        carrier = carrier,
        serviceId = serviceId,
        departureDate = departureDate.atUtc().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        arrivalDate = arrivalDate.atUtc().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        origin = origin,
        destination = destination
    )

    private fun String.parseDateTime(): ZonedDateTime {
        return ZonedDateTime.parse(this, DateTimeFormatter.ISO_ZONED_DATE_TIME)
    }

    private fun Map.Entry<Leg, TravelServiceInformationResponse?>.toTransitLeg(): TransitLeg {
        return value?.let {
            key.toTransitLeg(it)
        } ?: key.toTransitLeg()
    }

    private fun Leg.toTransitLeg(status: TravelServiceInformationResponse): TransitLeg? {
        return status.toTransitLeg(timetableId ?: return null, carrier.code, carrier.name)
    }

    private fun TravelServiceInformationResponse.toTransitLeg(
        serviceId: String,
        operatorId: String,
        operator: String
    ): TransitLeg {
        val origin = callingPoints[customerOriginIndex]
        val destination = callingPoints[customerDestinationIndex]
        return TransitLeg.newBuilder().apply {
            hasRealtime = true
            this.serviceId = serviceId
            this.operatorId = operatorId
            this.origin = origin.location.name
            this.destination = destination.location.name
            originId = origin.location.inventoryCode
            destinationId = destination.location.inventoryCode
            scheduledDeparture = origin.departure!!.schedule.time
                .parseDateTime().toDateTime()
            scheduledArrival = destination.arrival!!.schedule.time
                .parseDateTime().toDateTime()
            val actualDeparture = origin.departure.realTime?.time
                ?.parseDateTime()?.toDateTime()
            if(actualDeparture != null){
                this.actualDeparture = actualDeparture
            }
            val actualArrival = destination.arrival.realTime?.time
                ?.parseDateTime()?.toDateTime()
            if(actualArrival != null){
                this.actualArrival = actualArrival
            }
            val platformOrGate = origin.departure.realTime?.platform
                ?: origin.departure.schedule.platform
            platformOrGate?.let {
                this.platformOrGate = it
            }
            cancelled = realTime.isCancelled
            mode = transportMode.code.getMode()
            this.operator = operator
        }.build()
    }

    private fun Leg.toTransitLeg(): TransitLeg {
        return TransitLeg.newBuilder().apply {
            hasRealtime = false
            serviceId = timetableId ?: ""
            operatorId = carrier.code
            origin = departureStation.name
            destination = arrivalStation.name
            originId = departureStation.code
            destinationId = arrivalStation.code
            scheduledDeparture = localDepartAt.parseDateTime().toDateTime()
            scheduledArrival = localArriveAt.parseDateTime().toDateTime()
            cancelled = false //Can only be handled by wrapper or realtime
            mode = transport.code.getMode()
            operator = carrier.name
        }.build()
    }

    private fun String.getMode(): Mode {
        return MODES.entries.firstOrNull {
            endsWith(it.key)
        }?.value ?: Mode.MODE_UNSPECIFIED
    }

    private fun List<TransitLeg>.getImpossibleLegChangeIndex(): Int {
        var previousTime: ZonedDateTime? = null
        forEachIndexed { index, leg ->
            val previous = previousTime
            val originDeparture = leg.getDeparture()
            if(previous != null && previous.isAfter(originDeparture)) return index
            previousTime = leg.getArrival()
        }
        return -1
    }

}

interface TrainlineService {

    companion object {
        const val BASE_URL = "https://api.thetrainline.com/"

        private const val MANAGED_GROUP_NAME = "X-Api-Managedgroupname: TRAINLINE"
        private const val API_VERSION_1 = "X-Api-Version: 1.0"
        private const val API_VERSION_4 = "X-Api-Version: 4.0"
        private const val PLATFORM_TYPE = "X-Platform-Type: Android"
        private const val HEADER_APP_VERSION = "X-App-Version"
        private const val HEADER_USER_AGENT = "User-Agent"
        private const val HEADER_ACCEPT_LANGUAGE = "Accept-Language"
        const val HEADER_CONVERSATION_ID = "X-Api-Conversationid"
        const val HEADER_CONTEXT_ID = "X-Api-Contextid"

        fun getUserAgentHeader(version: String, versionCode: Long): String {
            return "Trainline/$version (Android ${Build.VERSION.RELEASE}, Build/$versionCode)"
        }

        private fun getDummyConversationId(): String {
            return "bonsai-android-${UUID.randomUUID()}"
        }
    }

    @Headers(
        MANAGED_GROUP_NAME,
        API_VERSION_1,
        PLATFORM_TYPE
    )
    @POST("mobile/op/setup")
    fun getSecurityHeaders(
        @Header(HEADER_APP_VERSION) appVersionName: String,
        @Header(HEADER_USER_AGENT) userAgent: String,
        @Header(HEADER_CONVERSATION_ID) dummyConversationId: String = getDummyConversationId(),
        @Header(HEADER_ACCEPT_LANGUAGE) acceptLanguage: String = Locale.getDefault().toLanguageTag(),
        @Body request: SetupRequest = SetupRequest()
    ): Call<Unit>

    @Headers(
        MANAGED_GROUP_NAME,
        API_VERSION_1,
        PLATFORM_TYPE
    )
    @GET("locations-search/v2/search")
    fun searchLocations(
        @Header(HEADER_APP_VERSION) appVersionName: String,
        @Header(HEADER_USER_AGENT) userAgent: String,
        @Header(HEADER_CONVERSATION_ID) conversationId: String,
        @Header(HEADER_CONTEXT_ID) contextId: String,
        @Header(HEADER_ACCEPT_LANGUAGE) acceptLanguage: String = Locale.getDefault().toLanguageTag(),
        @Query("locale") locale: String = Locale.getDefault().toLanguageTag(),
        @Query("connections") vararg connection: String,
        @Query("searchTerm") searchTerm: String
    ): Call<SearchLocationsResponse>

    @Headers(
        MANAGED_GROUP_NAME,
        API_VERSION_4,
        PLATFORM_TYPE
    )
    @POST("gateway/search")
    fun searchRoutes(
        @Header(HEADER_APP_VERSION) appVersionName: String,
        @Header(HEADER_USER_AGENT) userAgent: String,
        @Header(HEADER_CONVERSATION_ID) conversationId: String,
        @Header(HEADER_CONTEXT_ID) contextId: String,
        @Header(HEADER_ACCEPT_LANGUAGE) acceptLanguage: String = Locale.getDefault().toLanguageTag(),
        @Body request: SearchRoutesRequest
    ): Call<SearchRoutesResponse>

    @Headers(
        MANAGED_GROUP_NAME,
        API_VERSION_1,
        PLATFORM_TYPE
    )
    @GET("travelserviceinformation/carriers/{carrier}/services/{serviceId}")
    fun getTracking(
        @Header(HEADER_APP_VERSION) appVersionName: String,
        @Header(HEADER_USER_AGENT) userAgent: String,
        @Header(HEADER_CONVERSATION_ID) conversationId: String,
        @Header(HEADER_CONTEXT_ID) contextId: String,
        @Header(HEADER_ACCEPT_LANGUAGE) acceptLanguage: String = Locale.getDefault().toLanguageTag(),
        @Path("carrier") carrier: String,
        @Path("serviceId") serviceId: String,
        @Query("customerDepartureDate") departureDate: String,
        @Query("customerArrivalDate") arrivalDate: String,
        @Query("customerOrigin") origin: String,
        @Query("customerDestination") destination: String
    ): Call<TravelServiceInformationResponse>

}