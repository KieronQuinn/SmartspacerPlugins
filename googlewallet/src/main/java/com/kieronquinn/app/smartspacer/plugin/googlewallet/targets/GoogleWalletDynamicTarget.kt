package com.kieronquinn.app.smartspacer.plugin.googlewallet.targets

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.TextUtils
import android.text.format.DateFormat
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import com.google.internal.tapandpay.v1.valuables.FlightProto.Flight
import com.google.internal.tapandpay.v1.valuables.FlightProto.Flight.AirportInfo
import com.kieronquinn.app.smartspacer.plugin.googlewallet.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.googlewallet.R
import com.kieronquinn.app.smartspacer.plugin.googlewallet.model.extras.TransitCardExtrasProto.TransitCardExtras
import com.kieronquinn.app.smartspacer.plugin.googlewallet.model.extras.TransitCardExtrasProto.TransitCardExtras.TransitLeg
import com.kieronquinn.app.smartspacer.plugin.googlewallet.model.extras.TransitCardExtrasProto.TransitCardExtras.TransitLeg.Mode
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.GoogleApiRepository
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.GoogleWalletRepository
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.GoogleWalletRepository.Valuable
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.GoogleWalletRepository.Valuable.RefreshPeriod
import com.kieronquinn.app.smartspacer.plugin.googlewallet.ui.activities.ConfigurationActivity.NavGraphMapping
import com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.extensions.getArrival
import com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.extensions.getDeparture
import com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.extensions.matches
import com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.extensions.merge
import com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.extensions.toBitmap
import com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.extensions.toColour
import com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.extensions.toZonedDateTime
import com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.extensions.toZonedDateTimeOrNull
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.BaseConfigurationActivity.Companion.createIntent
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.takeEllipsised
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate
import org.koin.android.ext.android.inject
import java.time.Duration
import java.time.ZonedDateTime
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import android.graphics.drawable.Icon as AndroidIcon
import java.text.DateFormat as JavaDateFormat

class GoogleWalletDynamicTarget: SmartspacerTargetProvider() {

    companion object {
        private val GATE_CLOSING_MAX_DURATION = Duration.ofMinutes(30)
        private val GATE_OPEN_MAX_DURATION = Duration.ofHours(1)
        private val FLIGHT_ASCEND_DESCEND_DURATION = Duration.ofMinutes(30)
        const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.target.googlewalletdynamic"
        const val ID_PREFIX = "valuable_dynamic_"
    }

    private val googleWalletRepository by inject<GoogleWalletRepository>()
    private val googleApiRepository by inject<GoogleApiRepository>()

    private val timeFormat by lazy {
        DateFormat.getTimeFormat(provideContext())
    }

    override fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        getSignInTarget(smartspacerId)?.let {
            return listOf(it)
        }
        return googleWalletRepository.getActiveDynamicValuables().mapNotNull { it.toTarget() }
    }

    private fun getSignInTarget(smartspacerId: String): SmartspaceTarget? {
        if(googleApiRepository.isSignedIn()) return null
        return TargetTemplate.Basic(
            "${smartspacerId}_sign_in",
            ComponentName(provideContext(), GoogleWalletDynamicTarget::class.java),
            SmartspaceTarget.FEATURE_UNDEFINED,
            Text(provideContext().getString(R.string.target_sign_in_again_title)),
            Text(provideContext().getString(R.string.target_sign_in_again_subtitle)),
            Icon(AndroidIcon.createWithResource(provideContext(), R.drawable.ic_google_wallet)),
            TapAction()
        ).create()
    }

    private fun Valuable.toTarget(): SmartspaceTarget? {
        return when(this){
            is Valuable.Flight -> toTarget()
            is Valuable.EventTicket -> toTarget()
            is Valuable.TransitCard -> toTarget()
            else -> null
        }
    }

    private fun Valuable.getTapAction(): TapAction {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://pay.google.com/gp/v/valuable/$id?vs=gp_lp")
        }
        return TapAction(intent = intent)
    }

    private fun Valuable.EventTicket.toTarget(): SmartspaceTarget = with(proto) {
        val title = groupingInfo.groupingTitle
        val eventVenue = venue?.name
        val subtitle = if(eventVenue != null){
            "${groupingInfo.groupingSubtitle}, $eventVenue"
        }else groupingInfo.groupingSubtitle
        val eventIcon = this@toTarget.image?.toBitmap()?.let {
            AndroidIcon.createWithBitmap(it)
        } ?: AndroidIcon.createWithResource(provideContext(), R.drawable.ic_google_wallet)
        val seat = seatInfo?.seat?.merge()
        val row = seatInfo?.row?.merge()
        val section = seatInfo?.section?.merge()
        val gate = seatInfo?.gate?.merge()
        val items = listOfNotNull(seat, row, section, gate)
        if(items.isNotEmpty()){
            TargetTemplate.ListItems(
                "$ID_PREFIX$id",
                ComponentName(provideContext(), GoogleWalletDynamicTarget::class.java),
                provideContext(),
                Text(title),
                Text(subtitle),
                Icon(AndroidIcon.createWithResource(provideContext(), R.drawable.ic_event_ticket)),
                items.map { Text(it) },
                Icon(eventIcon, shouldTint = false),
                Text(""),
                getTapAction()
            ).create()
        }else{
            TargetTemplate.Basic(
                "$ID_PREFIX$id",
                ComponentName(provideContext(), GoogleWalletDynamicTarget::class.java),
                SmartspaceTarget.FEATURE_UNDEFINED,
                Text(title),
                Text(subtitle),
                Icon(AndroidIcon.createWithResource(provideContext(), R.drawable.ic_event_ticket)),
                getTapAction()
            ).create()
        }
    }

    private fun Valuable.Flight.toTarget(): SmartspaceTarget = with(proto) {
        val title = getGateBasedTitle() ?: getStatusBasedTitle()
        val subtitle = getSubtitle()
        val items = getItems()
        val airlineIcon = this@toTarget.image?.toBitmap()?.let {
            Icon(AndroidIcon.createWithBitmap(it), shouldTint = false)
        } ?: Icon(
            AndroidIcon.createWithResource(provideContext(), R.drawable.ic_google_wallet),
            shouldTint = true
        )
        if(items.size > 1) {
            TargetTemplate.ListItems(
                "$ID_PREFIX$id",
                ComponentName(provideContext(), GoogleWalletDynamicTarget::class.java),
                provideContext(),
                title,
                Text(subtitle),
                Icon(AndroidIcon.createWithResource(provideContext(), getIcon())),
                items.map { Text(it) },
                airlineIcon,
                Text(""),
                getTapAction()
            ).create()
        }else{
            TargetTemplate.LoyaltyCard(
                provideContext(),
                "$ID_PREFIX$id",
                ComponentName(provideContext(), GoogleWalletDynamicTarget::class.java),
                title,
                Text(subtitle),
                Icon(AndroidIcon.createWithResource(provideContext(), getIcon())),
                airlineIcon,
                Text(items.first()),
                onClick = getTapAction()
            ).create()
        }
    }

    private fun Flight.getGateBasedTitle(): Text? {
        val flightStatus = googleFlightStatus ?: airlineFlightStatus
        val gateCloseTime = flightStatus.gateCloses?.toZonedDateTimeOrNull()
        val departureTime = flightStatus.actualDeparture.toZonedDateTimeOrNull()
            ?: flightStatus.scheduledDeparture.toZonedDateTime()
        val arrivalTime = flightStatus.actualArrival.toZonedDateTimeOrNull()
            ?: flightStatus.scheduledArrival.toZonedDateTime()
        val statusMessage = flightStatus.statusMessage.takeIf { it.isNotBlank() }
        val gate = origin.getGateOrNull()
        val now = ZonedDateTime.now()
        val timeUntilClose = gateCloseTime?.let {
            Duration.between(now, it)
        }
        val timeUntilDeparture = Duration.between(now, departureTime)
        return when {
            departureTime.isBefore(now) -> {
                //Flight has departed
                resources.getString(
                    R.string.target_wallet_dynamic_flight_title_departed,
                    arrivalTime.formatTime(arrivalTime.zone != departureTime.zone)
                ).let { Text(it) }
            }
            gateCloseTime?.isBefore(now) == true -> {
                //Gate has closed
                resources.getString(
                    R.string.target_wallet_dynamic_flight_title_gate_closed,
                    origin.airportIataCode,
                    destination.airportIataCode
                ).let { Text(it) }
            }
            //Status message trumps all custom messages
            statusMessage != null -> {
                //Usually the most important data (time, gate) is at the end, so prioritise that
                Text(statusMessage, truncateAtType = TextUtils.TruncateAt.START)
            }
            gate != null && timeUntilClose != null && timeUntilClose <= GATE_CLOSING_MAX_DURATION -> {
                //Gate is defined, open, but closing soon
                resources.getString(
                    R.string.target_wallet_dynamic_flight_title_gate_closing,
                    gate,
                    timeUntilClose.toMinutes()
                ).let { Text(it) }
            }
            timeUntilClose != null && timeUntilClose <= GATE_OPEN_MAX_DURATION -> {
                //Gate is defined, is open with a remaining time greater than the warning
                resources.getString(
                    R.string.target_wallet_dynamic_flight_title_gate_open,
                    origin.gate
                ).let { Text(it) }
            }
            timeUntilDeparture <= GATE_OPEN_MAX_DURATION -> {
                //Gate is defined but with no closing time. Show the gate but with no time warning
                resources.getString(
                    R.string.target_wallet_dynamic_flight_title_gate_open,
                    origin.gate
                ).let { Text(it) }
            }
            else -> null
        }
    }

    private fun Flight.getStatusBasedTitle(): Text {
        val flightStatus = googleFlightStatus ?: airlineFlightStatus
        return resources.getString(
            R.string.target_wallet_dynamic_flight_title_status,
            carrierCodeAndFlightNumber,
            flightStatus.status
        ).let { Text(it) }
    }

    private fun AirportInfo.getGateOrNull(): String? {
        return gate.takeIf { it.isNotBlank() && it.trim() != "-" }
    }

    private fun Flight.getSubtitle(): CharSequence {
        val status = googleFlightStatus ?: airlineFlightStatus
        val now = ZonedDateTime.now()
        val departure = status.actualDeparture?.toZonedDateTimeOrNull()
            ?: status.scheduledDeparture.toZonedDateTime()
        return if(departure.isAfter(now)){
            getStatusBasedSubtitle()
        }else{
            resources.getString(
                R.string.target_wallet_dynamic_flight_subtitle_to,
                carrierCodeAndFlightNumber,
                destination.servesCity
            )
        }
    }

    private fun Flight.getStatusBasedSubtitle(): CharSequence = SpannableStringBuilder().apply {
        val flightStatus = googleFlightStatus ?: airlineFlightStatus
        val departure = flightStatus.actualDeparture.toZonedDateTime()
        if(!flightStatus.actualDeparture.matches(flightStatus.scheduledDeparture)){
            val scheduledDeparture = flightStatus.scheduledDeparture.toZonedDateTime().formatTime()
            append(scheduledDeparture, StrikethroughSpan(), SPAN_EXCLUSIVE_EXCLUSIVE)
            appendSpace()
            val actualDeparture = departure.formatTime()
            append(actualDeparture)
        }else{
            val scheduledDeparture = flightStatus.scheduledDeparture.toZonedDateTime().formatTime()
            append(scheduledDeparture)
        }
        appendSpace()
        append(
            resources.getString(R.string.target_wallet_dynamic_flight_subtitle_arrow),
            ForegroundColorSpan(flightStatus.statusColor.toColour()),
            SPAN_EXCLUSIVE_EXCLUSIVE
        )
        appendSpace()
        val arrival = flightStatus.actualArrival.toZonedDateTime()
        if(!flightStatus.actualArrival.matches(flightStatus.scheduledArrival)){
            val scheduledArrival = flightStatus.scheduledArrival.toZonedDateTime().formatTime()
            append(scheduledArrival, StrikethroughSpan(), SPAN_EXCLUSIVE_EXCLUSIVE)
            appendSpace()
            val actualArrival = arrival.formatTime(arrival.zone != departure.zone)
            append(actualArrival)
        }else{
            val scheduledArrival = flightStatus.scheduledArrival.toZonedDateTime()
                .formatTime(arrival.zone != departure.zone)
            append(scheduledArrival)
        }
    }

    private fun Flight.getItems(): List<String> {
        val flightNumber = carrierCodeAndFlightNumber
        val seat = boardingAndSeatingInfo?.seatNumber?.merge()
        val gate = origin?.getGateOrNull()?.takeIf { it.isNotBlank() }?.let {
            resources.getString(R.string.target_wallet_dynamic_flight_gate, it)
        }
        val terminal = origin?.terminal?.takeIf { it.isNotBlank() }?.let {
            resources.getString(R.string.target_wallet_dynamic_flight_terminal, it)
        }
        return listOfNotNull(flightNumber, seat, gate, terminal)
    }

    private fun Flight.getIcon(): Int {
        val now = ZonedDateTime.now()
        val flightStatus = googleFlightStatus ?: airlineFlightStatus
        val departure = flightStatus.actualDeparture.toZonedDateTime()
        val arrival = flightStatus.actualArrival.toZonedDateTime()
        return when {
            departure.isAfter(now) -> {
                R.drawable.ic_flight_depart
            }
            arrival.isBefore(now) -> {
                R.drawable.ic_flight_land
            }
            Duration.between(now, departure).abs() < FLIGHT_ASCEND_DESCEND_DURATION -> {
                R.drawable.ic_flight_depart
            }
            Duration.between(now, arrival).abs() < FLIGHT_ASCEND_DESCEND_DURATION -> {
                R.drawable.ic_flight_land
            }
            else -> R.drawable.ic_flight
        }
    }

    private fun Valuable.TransitCard.toTarget(): SmartspaceTarget {
        val icon = image?.toBitmap()?.let {
            Icon(AndroidIcon.createWithBitmap(it), shouldTint = false)
        } ?: Icon(AndroidIcon.createWithResource(provideContext(), R.drawable.ic_google_wallet))
        val tapAction = getTapAction()
        return getExtrasBasedTarget(icon, tapAction) ?: getFallbackTarget(icon, tapAction)
    }

    private fun Valuable.TransitCard.getExtrasBasedTarget(
        cardIcon: Icon,
        onClick: TapAction
    ): SmartspaceTarget? {
        val extras = extras ?: return null
        val currentLeg = extras.getCurrentLeg()
        val nextLeg = extras.getNextLeg()
        val cancelledLeg = extras.legList.firstOrNull { it.cancelled }
        val impossibleLegChange = extras.impossibleLegChange
        val from = extras.legList.firstOrNull()?.origin
        val to = extras.legList.lastOrNull()?.destination
        val departureTime = extras.legList.firstOrNull()?.let {
            val scheduled = it.scheduledDeparture.toZonedDateTime()
            val actual = if(it.hasActualDeparture()){
                it.actualDeparture.toZonedDateTimeOrNull()
            }else null
            val builder = SpannableStringBuilder()
            if(actual != null && actual != scheduled){
                builder.append(scheduled.formatLocalTime(), StrikethroughSpan(), SPAN_EXCLUSIVE_EXCLUSIVE)
                builder.appendSpace()
                builder.append(actual.formatLocalTime())
            }else{
                builder.append(scheduled.formatLocalTime())
            } as CharSequence
        }
        val arrivalTime = extras.legList.lastOrNull()?.let {
            val scheduled = it.scheduledArrival.toZonedDateTime()
            val actual = if(it.hasActualArrival()){
                it.actualArrival.toZonedDateTimeOrNull()
            }else null
            val builder = SpannableStringBuilder()
            if(actual != null && actual != scheduled){
                builder.append(scheduled.formatLocalTime(), StrikethroughSpan(), SPAN_EXCLUSIVE_EXCLUSIVE)
                builder.appendSpace()
                builder.append(actual.formatLocalTime())
            }else{
                builder.append(scheduled.formatLocalTime())
            } as CharSequence
        }
        val title = when {
            cancelledLeg != null -> {
                provideContext().getString(
                    R.string.target_wallet_dynamic_transit_cancelled_at_title,
                    cancelledLeg.origin
                )
            }
            impossibleLegChange != -1 && extras.legList.isNotEmpty() -> {
                val impossibleLeg = extras.legList[impossibleLegChange]
                provideContext().getString(
                    R.string.target_wallet_dynamic_transit_impossible_at_title,
                    impossibleLeg.origin
                )
            }
            currentLeg?.getArrival() != null -> {
                if(currentLeg.mode == Mode.WALK && nextLeg != null){
                    //Interchange transfer
                    provideContext().getString(
                        R.string.target_wallet_dynamic_transit_walk_title,
                        nextLeg.platformOrGate,
                        nextLeg.origin
                    )
                }else {
                    "${currentLeg.getArrival()!!.formatLocalTime()}: ${currentLeg.destination}"
                }
            }
            nextLeg != null -> {
                provideContext().getString(
                    R.string.target_wallet_dynamic_transit_go_to_platform_title,
                    " ${nextLeg.platformOrGate}" ?: "",
                    nextLeg.origin
                )
            }
            from != null && to != null -> {
                provideContext().getString(
                    R.string.target_wallet_dynamic_transit_generic_title,
                    from,
                    to
                )
            }
            else -> null
        } ?: return null
        val fullJourneySubtitle = {
            SpannableStringBuilder().apply {
                append(departureTime)
                appendSpace()
                append(provideContext().getString(
                    R.string.target_wallet_dynamic_transit_generic_subtitle_arrow
                ))
                appendSpace()
                append(arrivalTime)
            }
        }
        val nextLegSubtitle = { leg: TransitLeg ->
            val scheduled = leg.scheduledDeparture.toZonedDateTime()
            val actual = if(leg.hasActualDeparture()){
                leg.actualDeparture.toZonedDateTimeOrNull()
            }else null
            val builder = SpannableStringBuilder()
            if(actual != null && actual != scheduled){
                builder.append(scheduled.formatLocalTime(), StrikethroughSpan(), SPAN_EXCLUSIVE_EXCLUSIVE)
                builder.appendSpace()
                builder.append(actual.formatLocalTime())
            }else{
                builder.append(scheduled.formatLocalTime())
            }
            builder.appendSpace()
            builder.append(provideContext().getString(
                R.string.target_wallet_dynamic_transit_go_to_platform_subtitle
            ))
            builder.appendSpace()
            builder.append(leg.destination)
        }
        val subtitle = when {
            cancelledLeg != null -> {
                provideContext().getString(
                    R.string.target_wallet_dynamic_transit_cancelled_at_subtitle
                )
            }
            impossibleLegChange != -1 -> {
                provideContext().getString(
                    R.string.target_wallet_dynamic_transit_impossible_at_subtitle
                )
            }
            currentLeg?.getArrival() != null -> {
                if(currentLeg.mode == Mode.WALK && nextLeg != null) {
                    //Interchange transfer
                    nextLegSubtitle(nextLeg)
                }else{
                    fullJourneySubtitle()
                }
            }
            nextLeg != null -> {
                nextLegSubtitle(nextLeg)
            }
            from != null && to != null -> {
                fullJourneySubtitle()
            }
            else -> null
        } ?: return null
        val icon = when {
            currentLeg != null -> {
                currentLeg.mode.getIcon()
            }
            else -> null
        }?.let { Icon(it) } ?: cardIcon
        return if(extras.itemsCount != 0) {
            TargetTemplate.ListItems(
                "$ID_PREFIX$id",
                ComponentName(provideContext(), GoogleWalletDynamicTarget::class.java),
                provideContext(),
                Text(title),
                Text(subtitle),
                icon,
                extras.itemsList.map { Text(it.takeEllipsised(8)) },
                Icon(AndroidIcon.createWithResource(provideContext(), R.drawable.ic_google_wallet)),
                Text(""),
                onClick
            ).create()
        }else{
            TargetTemplate.Basic(
                "$ID_PREFIX$id",
                ComponentName(provideContext(), GoogleWalletDynamicTarget::class.java),
                SmartspaceTarget.FEATURE_UNDEFINED,
                Text(title),
                Text(subtitle),
                icon,
                onClick
            ).create()
        }
    }

    private fun Valuable.TransitCard.getFallbackTarget(
        icon: Icon,
        onClick: TapAction
    ): SmartspaceTarget {
        val title = getGroupingInfo().groupingTitle
        val subtitle = when {
            proto.hasDepartureTime() && proto.hasArrivalTime() -> {
                provideContext().getString(
                    R.string.target_wallet_dynamic_transit_generic_title,
                    proto.departureTime.toZonedDateTime().formatLocalTime(),
                    proto.arrivalTime.toZonedDateTime().formatLocalTime()
                )
            }
            proto.hasDepartureTime() -> {
                proto.departureTime.toZonedDateTime().formatLocalTime()
            }
            else -> {
                getGroupingInfo().groupingSubtitle
            }
        }
        return TargetTemplate.Basic(
            "$ID_PREFIX$id",
            ComponentName(provideContext(), GoogleWalletDynamicTarget::class.java),
            SmartspaceTarget.FEATURE_UNDEFINED,
            Text(title),
            Text(subtitle),
            icon,
            onClick
        ).create()
    }

    private fun TransitCardExtras.getCurrentLeg(): TransitLeg? {
        val now = ZonedDateTime.now()
        return legList.firstOrNull {
            it.getDeparture()?.isBefore(now) == true && it.getArrival()?.isAfter(now) == true
        }
    }

    private fun TransitCardExtras.getNextLeg(): TransitLeg? {
        val now = ZonedDateTime.now()
        return legList.mapNotNull {
            Pair(it.getDeparture() ?: return@mapNotNull null, it)
        }.filter { it.first.isAfter(now) }.minByOrNull { it.first }?.second
    }

    private fun Mode.getIcon(): AndroidIcon? {
        val res = when(this){
            Mode.TRAIN -> R.drawable.ic_train
            Mode.BUS -> R.drawable.ic_bus
            Mode.FERRY -> R.drawable.ic_boat
            Mode.TRAM -> R.drawable.ic_tram
            Mode.WALK -> R.drawable.ic_walk
            else -> null
        } ?: return null
        return AndroidIcon.createWithResource(provideContext(), res)
    }

    private fun SpannableStringBuilder.appendSpace() {
        append(" ")
    }

    private fun ZonedDateTime.formatTime(includeZone: Boolean = false): String {
        return if(includeZone){
            val timeZone = TimeZone.getTimeZone(zone)
                .getDisplayName(false, TimeZone.SHORT, Locale.getDefault())
            "${timeFormat.formatTime(this)} $timeZone"
        }else timeFormat.formatTime(this)
    }

    private fun ZonedDateTime.formatLocalTime(): String {
        return timeFormat.format(Date.from(toInstant()))
    }

    private fun JavaDateFormat.formatTime(time: ZonedDateTime): String {
        val instant = time.toInstant()
        return format(Date.from(instant))
    }

    override fun onDismiss(smartspacerId: String, targetId: String): Boolean {
        googleWalletRepository.dismissTarget(targetId)
        return true
    }

    override fun getConfig(smartspacerId: String?): Config {
        val refreshPeriodMinutes = when(googleWalletRepository.getDynamicValuableRefreshPeriod()) {
            null, RefreshPeriod.NO_REFRESH -> 0
            RefreshPeriod.EXPEDITED -> 1
            RefreshPeriod.PERIODIC -> 10
        }
        return Config(
            label = resources.getString(R.string.target_wallet_dynamic_title),
            description = resources.getString(R.string.target_wallet_dynamic_description),
            icon = AndroidIcon.createWithResource(provideContext(), R.drawable.ic_google_wallet),
            notificationProvider = "${BuildConfig.APPLICATION_ID}.notification.googlewalletdynamic",
            refreshPeriodMinutes = refreshPeriodMinutes,
            configActivity = createIntent(
                provideContext(), NavGraphMapping.TARGET_WALLET_DYNAMIC
            ),
            //Force a login since the user may not see this otherwise
            setupActivity = createIntent(
                provideContext(), NavGraphMapping.TARGET_WALLET_DYNAMIC
            )
        )
    }

}