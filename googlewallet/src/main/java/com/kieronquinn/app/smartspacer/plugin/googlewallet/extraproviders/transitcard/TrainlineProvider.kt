package com.kieronquinn.app.smartspacer.plugin.googlewallet.extraproviders.transitcard

import com.google.internal.tapandpay.v1.passes.templates.CardProto
import com.google.internal.tapandpay.v1.passes.templates.CardProto.PassCardRowItems
import com.google.internal.tapandpay.v1.passes.templates.CardProto.PassCardRowItems.PassCardRowItem
import com.kieronquinn.app.smartspacer.plugin.googlewallet.extraproviders.BaseTransitCardProvider
import com.kieronquinn.app.smartspacer.plugin.googlewallet.model.Result
import com.kieronquinn.app.smartspacer.plugin.googlewallet.model.extras.TransitCardExtrasProto.TransitCardExtras
import com.kieronquinn.app.smartspacer.plugin.googlewallet.model.extras.TransitCardExtrasProto.TransitCardExtras.TransitLeg
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.GoogleWalletRepository
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.GoogleWalletRepository.Valuable.RefreshPeriod
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.GoogleWalletRepository.Valuable.TransitCard
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.datasources.TrainlineRepository
import com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.extensions.asString
import com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.extensions.getArrival
import com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.extensions.getDeparture
import com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.extensions.toZonedDateTimeOrNull
import org.koin.core.component.inject
import java.time.Duration
import java.time.ZonedDateTime

object TrainlineProvider: BaseTransitCardProvider() {

    /**
     *  The time period to expedite refreshes before/after a departure/arrival as timings are more
     *  likely to change.
     */
    private val LEG_CHANGE_EXPEDITE_PERIOD = Duration.ofMinutes(10)

    /**
     *  Titles of extras to show from the card template, and whether to include their titles.
     *  Intentionally does not localise as the cards do not necessarily match the system language.
     */
    private val LIST_EXTRAS = mapOf(
        "COACH/SEAT" to false,
        "ROUTE" to false,
        "TICKET TYPE" to false
    )

    /**
     *  Regexes to extract from and to for when no origin & destination are set in the template.
     *  Intentionally does not localise as the cards do not necessarily match the system language.
     */
    private val FALLBACK_ROUTE_REGEXES = setOf(
        "(.*) to (.*)"
    ).map {
        it.toRegex()
    }

    /**
     *  Field is left unfilled but the UI is showing a value. Ignore.
     */
    private val FIELD_UNFILLED = setOf("*", "*/***")

    private val trainlineRepository by inject<TrainlineRepository>()

    //This is the fallback, always return true (until a denylist of issuers becomes available)
    override val doesMatch = { _: GoogleWalletRepository.Valuable -> true }

    override val valuableClasses = setOf(TransitCard::class.java)

    override suspend fun getExtras(
        valuable: TransitCard,
        currentExtras: TransitCardExtras?
    ): TransitCardExtras? {
        if(currentExtras.shouldSkip()) return currentExtras
        val from = valuable.getStartLocation() ?: return null
        val to = valuable.getEndLocation() ?: return null
        val departure = valuable.getDepartureTime() ?: return null
        val arrival = valuable.getArrivalTime() ?: return null
        val extras = trainlineRepository.getTransitCardExtrasForTrain(
            from, to, departure, arrival, currentExtras
        )
        return when(extras){
            is Result.Success -> extras.data
            is Result.Failed -> {
                if(extras.code == 999){
                    //Connection issue, return null so we can try again next time
                    null
                }else{
                    //Failed to lookup something, return cache if it exists or an empty extra
                    extras.cached ?: TransitCardExtras.newBuilder()
                        .setServiceId("$from:$to:$departure:$arrival")
                        .build()
                }
            }
        }?.addListExtrasIfRequired(valuable)
    }

    override fun getRefreshPeriod(
        valuable: TransitCard,
        time: ZonedDateTime
    ): RefreshPeriod {
        //If there's no current extras, always expedite since the initial load failed
        val currentExtras = getExtrasFromCurrentValuable(valuable) ?: return RefreshPeriod.EXPEDITED
        //If there are no realtime legs, the data is effectively static and can be treated as such
        if(currentExtras.legList.all { !it.hasRealtime }) return RefreshPeriod.NO_REFRESH
        //If near leg changes, expedite it
        if(currentExtras.legList.isNearChange(time)) return RefreshPeriod.EXPEDITED
        //Otherwise, just refresh periodically
        return RefreshPeriod.PERIODIC
    }

    override fun getEndTime(valuable: TransitCard): ZonedDateTime? {
        val currentExtras = getExtrasFromCurrentValuable(valuable) ?: return null
        return currentExtras.legList.lastOrNull()?.getArrival()
    }

    /**
     *  Returns if the current time meets the constraints of [LEG_CHANGE_EXPEDITE_PERIOD]
     */
    private fun List<TransitLeg>.isNearChange(now: ZonedDateTime): Boolean {
        return any {
            val times = listOfNotNull(it.getDeparture(), it.getArrival())
            times.any { time ->
                val duration = Duration.between(now, time)
                duration.abs() < LEG_CHANGE_EXPEDITE_PERIOD
            }
        }
    }

    private fun TransitCardExtras?.shouldSkip(): Boolean {
        //If not set, never skip (try to load at least once)
        if(this == null) return false
        //Skip if there is data, but there are no legs (previous lookup failed & nothing to load)
        return legCount == 0
    }

    private fun TransitCard.getStartLocation(): String? {
        val startLocation = findTransitCardInfos().firstOrNull {
            it.hasStartLocation()
        }?.startLocation ?: return getFallbackLocations()?.first
        if(startLocation.hasName()) return startLocation.name?.asString()?.toString()
        if(startLocation.hasShortName()) return startLocation.shortName?.asString()?.toString()
        return null
    }

    private fun TransitCard.getEndLocation(): String? {
        val endLocation = findTransitCardInfos().firstOrNull {
            it.hasEndLocation()
        }?.endLocation ?: return getFallbackLocations()?.second
        if(endLocation.hasName()) return endLocation.name?.asString()?.toString()
        if(endLocation.hasShortName()) return endLocation.shortName?.asString()?.toString()
        return null
    }

    private fun TransitCard.getFallbackLocations(): Pair<String, String>? {
        val groupingTitle = getGroupingInfo().groupingTitle
        return FALLBACK_ROUTE_REGEXES.firstOrNull {
            it.matches(groupingTitle)
        }?.let {
            val values = it.matchEntire(groupingTitle)!!.groupValues
            Pair(values[1], values[2])
        }
    }

    private fun TransitCard.findTransitCardInfos(): List<CardProto.PassCardRowTransit> {
        return proto.templateInfo.detailsCardInfo.cardRowInfoList.filter {
            it.hasTransit()
        }.map {
            it.transit
        }
    }

    private fun TransitCard.getDepartureTime(): ZonedDateTime? {
        return proto.departureTime?.toZonedDateTimeOrNull()
    }

    private fun TransitCard.getArrivalTime(): ZonedDateTime? {
        return proto.arrivalTime?.toZonedDateTimeOrNull()
    }

    private fun TransitCardExtras.addListExtrasIfRequired(valuable: TransitCard): TransitCardExtras {
        val items = valuable.proto.templateInfo.detailsCardInfo.cardRowInfoList.filter {
            it.hasItems()
        }.map {
            it.items
        }
        return toBuilder()
            .clearItems()
            .addAllItems(items.getRowListExtras().take(4))
            .build()
    }

    private fun List<PassCardRowItems>.getRowListExtras() = mapNotNull {
        it.itemList.getItemListExtras()
    }.flatten()

    private fun List<PassCardRowItem>.getItemListExtras() = mapNotNull {
        if(!it.hasReferenceValue()) return@mapNotNull null
        val ref = it.referenceValue.itemReference
        if(!ref.hasTitle()) return@mapNotNull null
        if(!ref.hasSubtitle()) return@mapNotNull null
        val title = ref.title.asString()?.toString() ?: return@mapNotNull null
        val subtitle = ref.subtitle.asString()?.toString() ?: return@mapNotNull null
        if(FIELD_UNFILLED.contains(subtitle)) return@mapNotNull null
        if(LIST_EXTRAS[title] ?: return@mapNotNull null){
            "$title: $subtitle"
        }else{
            subtitle
        }
    }

}